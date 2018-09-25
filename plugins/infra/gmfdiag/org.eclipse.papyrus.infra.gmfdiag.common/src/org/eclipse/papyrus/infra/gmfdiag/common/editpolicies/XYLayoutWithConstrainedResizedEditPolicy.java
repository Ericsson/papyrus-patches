/*****************************************************************************
 * Copyright (c) 2014 - 2017 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *		 Vincent Lorenzo (CEA LIST) vincent.lorenzo@cea.fr - Initial API and implementation
 *		 Celine Janssens (ALL4TEC) celine.janssens@all4tec.net - Bug 520154
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.commands.SetBoundsCommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.CompartmentEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.INodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.figures.LayoutHelper;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest;
import org.eclipse.gmf.runtime.emf.commands.core.command.CompositeTransactionalCommand;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.FixEdgeAnchorsDeferredCommand;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.ConnectionEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.FixAnchorHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.ServiceUtilsForEditPart;

/**
 *
 * This edit policy allows to resize parent from NORTH, NORTH_WEST or WEST without moving its children
 *
 */
public class XYLayoutWithConstrainedResizedEditPolicy extends XYLayoutEditPolicy {

	private FixAnchorHelper helper = null;

	/**
	 * <p>
	 * Overriden {@link org.eclipse.gef.editpolicies.XYLayoutEditPolicy#getCurrentConstraintFor},
	 * because the child may have a different (non-rectangle) layout constraint, or be owned by a parent
	 * without a layout manager (e.g. for connections). All these cases would cause exceptions in the default
	 * implementation (Typically when reparenting a Node from a non-XYLayout to a XYLayout compartment)
	 * </p>
	 * <p>
	 * The reparenting use case was not initially intended for XYLayoutEditPolicy (Which is why it always
	 * expects either a Rectangle or null), see https://bugs.eclipse.org/bugs/show_bug.cgi?id=86473
	 * </p>
	 */
	@Override
	protected Rectangle getCurrentConstraintFor(GraphicalEditPart child) {
		if (child instanceof ConnectionEditPart && child.getParent() instanceof DiagramRootEditPart) {
			return null;
		}

		IFigure fig = child.getFigure();
		if (fig.getParent() == null || fig.getParent().getLayoutManager() == null) {
			return null;
		}
		Object constraint = fig.getParent().getLayoutManager().getConstraint(fig);
		if (constraint instanceof Rectangle) {
			return (Rectangle) constraint;
		}

		// This method is typically used to retrieve the current size of the child; so
		// let's return its current bounds.
		return fig.getBounds().getCopy();
	}

	/**
	 * Called in response to a <tt>REQ_CREATE</tt> request. Returns a command
	 * to set each created element bounds and auto-size properties.
	 *
	 * @param request
	 *            a create request (understands instances of {@link CreateViewRequest}).
	 * @return a command to satisfy the request; <tt>null</tt> if the request is not
	 *         understood.
	 */
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		CreateViewRequest req = (CreateViewRequest) request;


		TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();

		CompositeTransactionalCommand cc = new CompositeTransactionalCommand(editingDomain, DiagramUIMessages.AddCommand_Label);
		Iterator<?> iter = req.getViewDescriptors().iterator();
		final Rectangle BOUNDS = (Rectangle) getConstraintFor(request);
		boolean couldBeSnaped = request.getLocation().equals(LayoutHelper.UNDEFINED.getLocation()) && req.isSnapToEnabled();
		while (iter.hasNext()) {
			CreateViewRequest.ViewDescriptor viewDescriptor = (CreateViewRequest.ViewDescriptor) iter.next();
			Rectangle rect = getBoundsOffest(req, BOUNDS, viewDescriptor);

			// see bug 427129: Figures newly created via the palette should be snapped to grid if "snap to grid" is activated
			if (couldBeSnaped) {
				// this code fix the bug in some case...
				int add = 0;
				DiagramRootEditPart drep = (DiagramRootEditPart) getHost().getRoot();
				double spacing = drep.getGridSpacing();
				final double max_value = spacing * 20;
				final SnapToHelper helper = getHost().getAdapter(SnapToHelper.class);
				if (helper != null) {
					final LayoutHelper layoutHelper = new LayoutHelper();
					while (add < max_value) {// we define a max value to do test
						Rectangle LOCAL_BOUNDS = BOUNDS.getCopy();
						LOCAL_BOUNDS.translate(add, add);
						Rectangle tmp_rect = getBoundsOffest(req, LOCAL_BOUNDS, viewDescriptor);
						final PrecisionRectangle resultRect = new PrecisionRectangle(tmp_rect);
						resultRect.setWidth(-1);
						resultRect.setHeight(-1);
						PrecisionPoint res1 = new PrecisionPoint(tmp_rect.getLocation());
						helper.snapPoint(request, PositionConstants.NORTH_WEST, res1.getPreciseCopy(), res1);
						final Point pt = layoutHelper.validatePosition(getHostFigure(), resultRect.setLocation(res1));
						if (couldBeSnaped) {
							if (pt.equals(resultRect.getLocation())) {
								rect.setLocation(resultRect.getLocation());
								break;
							} else {
								add += spacing;
								continue;
							}
						}
					}
				}
			}

			// Default set bounds command.
			cc.compose(new SetBoundsCommand(editingDomain, DiagramUIMessages.SetLocationCommand_Label_Resize, viewDescriptor, rect));
			break;

		}

		if (cc.reduce() == null) {
			return null;
		}
		return chainGuideAttachmentCommands(request, new ICommandProxy(cc.reduce()));
	}


	/**
	 * Returns the <code>Command</code> to resize a group of children.
	 *
	 * @param request
	 *            the ChangeBoundsRequest
	 * @return the Command
	 */
	@Override
	protected Command getChangeConstraintCommand(ChangeBoundsRequest request) {
		final CompoundCommand resize = new CompoundCommand("Resize Command");//$NON-NLS-1$
		IGraphicalEditPart child;
		final List<?> children = request.getEditParts();
		boolean isConstrainedResize = request.isConstrainedResize();
		boolean forceLocation = isConstrainedResize;
		for (int i = 0; i < children.size(); i++) {
			child = (IGraphicalEditPart) children.get(i);
			resize.add(createChangeConstraintCommand(request, child, translateToModelConstraint(getConstraintFor(request, child))));
			final Point move = request.getMoveDelta();
			if (forceLocation) {
				for (Object object : child.getChildren()) {
					if (object instanceof CompartmentEditPart) {
						final EditPolicy layoutPolicy = ((CompartmentEditPart) object).getEditPolicy(EditPolicy.LAYOUT_ROLE);
						if (layoutPolicy instanceof org.eclipse.gef.editpolicies.XYLayoutEditPolicy) {
							for (final Object current : ((CompartmentEditPart) object).getChildren()) {
								if (current instanceof NodeEditPart) {
									final ChangeBoundsRequest forceLocationRequest = new ChangeBoundsRequest();
									forceLocationRequest.setType("move");//$NON-NLS-1$
									forceLocationRequest.setMoveDelta(move.getNegated());
									forceLocationRequest.setEditParts((EditPart) current);
									final Command tmp = ((NodeEditPart) current).getCommand(forceLocationRequest);
									if (tmp != null) {
										resize.add(tmp);
									}
								}
							}
						}
					}
				}
			}
			// we add the command to fix the anchor
			if (isConstrainedResize && child instanceof INodeEditPart) {
				if (getHelper() == null) {
					TransactionalEditingDomain domain = null;
					try {
						domain = ServiceUtilsForEditPart.getInstance().getTransactionalEditingDomain(child);
					} catch (ServiceException e) {
						Activator.log.error(e);
					}
					setHelper(new FixAnchorHelper(domain));
				}
				final Command fixAnchorCommand = getHelper().getFixIdentityAnchorCommand((INodeEditPart) child, request.getMoveDelta(), request.getSizeDelta(), request.getResizeDirection());
				if (fixAnchorCommand != null) {
					resize.add(fixAnchorCommand);
				}
			}
		}
		return resize.unwrap();
	}

	/**
	 * @return the helper
	 * @since 3.1
	 */
	public FixAnchorHelper getHelper() {
		return helper;
	}

	/**
	 * @param helper
	 *            the helper to set
	 * @since 3.1
	 */
	public void setHelper(FixAnchorHelper helper) {
		this.helper = helper;
	}

	@Override
	protected Command createChangeConstraintCommand(ChangeBoundsRequest request, EditPart child, Object constraint) {
		final Command cmd = super.createChangeConstraintCommand(request, child, constraint);
		if (org.eclipse.gef.RequestConstants.REQ_MOVE_CHILDREN.equals(request.getType()) && child instanceof INodeEditPart) {
			Set<Object> notBeingMovedConnections = getConnectionsToElementsNotBeingMoved(request, child);
			if (!notBeingMovedConnections.isEmpty()) {
				final CompoundCommand cc = new CompoundCommand();
				cc.add(cmd);
				// see bug 430702: [Diagram] Moving source of a link moves the target too.
				ICommandProxy moveChildrenFixEdgeAnchorCommand = getMoveChildrenFixEdgeAnchorCommand(notBeingMovedConnections);
				if (null != moveChildrenFixEdgeAnchorCommand) {
					cc.add(moveChildrenFixEdgeAnchorCommand);
				}
				return cc;
			}
		}
		return cmd;
	}

	/**
	 * Get the Command to Fix the Edge Anchor when moving the children
	 *
	 * @param notBeingMovedConnections
	 *            List of not being moved Connections
	 * @return The proxy Command to Fix the Edge Anchor
	 * @since 3.1
	 */
	protected ICommandProxy getMoveChildrenFixEdgeAnchorCommand(Set<Object> notBeingMovedConnections) {
		return new ICommandProxy(new FixEdgeAnchorsDeferredCommand(getEditingDomain(), (IGraphicalEditPart) getHost(), notBeingMovedConnections));
	}

	/*
	 * {@link FixEdgeAnchorsDeferredCommand} should not be applied to the multiple selection move request
	 * see Bug 459839 - [All Diagrams] unexpected behavior moving multiple linked elements
	 */
	private Set<Object> getConnectionsToElementsNotBeingMoved(ChangeBoundsRequest request, EditPart child) {
		List<?> sources = ((INodeEditPart) child).getSourceConnections();
		List<?> targets = ((INodeEditPart) child).getTargetConnections();
		Set<Object> connections = new HashSet<>();
		connections.addAll(sources);
		connections.addAll(targets);
		if (connections.isEmpty()) {
			return Collections.emptySet();
		}
		PapyrusResizableShapeEditPolicy.CachedEditPartsSet movedEditPartsSet = PapyrusResizableShapeEditPolicy.getMovedEditPartsSet(request);
		final Set<Object> result = new HashSet<>();
		final Iterator<?> connectionsIter = connections.iterator();
		while (connectionsIter.hasNext()) {
			final Object nextConnection = connectionsIter.next();
			if (false == nextConnection instanceof AbstractConnectionEditPart) {
				continue;
			}
			AbstractConnectionEditPart nextTypedConnection = (AbstractConnectionEditPart) nextConnection;
			EditPart sourceEnd = nextTypedConnection.getSource();
			EditPart targetEnd = nextTypedConnection.getTarget();
			if (movedEditPartsSet.isMovedEditPart(sourceEnd) && movedEditPartsSet.isMovedEditPart(targetEnd)) {
				continue;
			}
			result.add(nextConnection);
		}
		return result;
	}

	protected TransactionalEditingDomain getEditingDomain() {

		TransactionalEditingDomain domain = null;
		try {
			domain = ServiceUtilsForEditPart.getInstance().getTransactionalEditingDomain(getHost());
		} catch (ServiceException e) {
			Activator.log.error(e);
		}
		return domain;
	}
}
