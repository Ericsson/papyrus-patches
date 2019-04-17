/*****************************************************************************
 * (c) Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.LocationRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gef.tools.ResizeTracker;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.figures.IBorderItemLocator;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeConnectionRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest.ConnectionViewDescriptor;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.PapyrusResizableShapeEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.snap.PapyrusDragEditPartsTrackerEx;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.AffixedNodeAlignmentEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.helpers.AnchorHelper;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.InteractionGraphGraphicalNodeEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.figures.InteractionUseRectangleFigure;
import org.eclipse.papyrus.uml.diagram.sequence.locator.GateLocator;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.papyrus.uml.service.types.element.UMLElementTypes;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;

/**
 * @author etxacam
 *
 */
public class CustomInteractionUseEditPart extends InteractionUseEditPart implements IGraphicalEditPart {

	public CustomInteractionUseEditPart(View view) {
		super(view);
	}

	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		removeEditPolicy(AffixedNodeAlignmentEditPolicy.AFFIXED_CHILD_ALIGNMENT_ROLE);
		PapyrusResizableShapeEditPolicy resizableEditPolicy = new PapyrusResizableShapeEditPolicy() {
			@Override
			protected ResizeTracker getResizeTracker(int direction) {
				return new ResizeTracker((GraphicalEditPart) getHost(), direction) {
					@Override
					protected Request createSourceRequest() {
						ChangeBoundsRequest request;
						request = new ChangeBoundsRequest(REQ_RESIZE) {

							@Override
							public void setCenteredResize(boolean value) {
								super.setCenteredResize(false);
							}
							
						};
						request.setResizeDirection(getResizeDirection());
						return request;
					}				
				};
			}
			
		};
		
		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, resizableEditPolicy);
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new InteractionGraphGraphicalNodeEditPolicy());
	}

	protected LayoutEditPolicy createLayoutEditPolicy() {
		return new LayoutEditPolicy() {
			@Override
			protected Command getMoveChildrenCommand(Request request) {
				return null;
			}
			
			@Override
			protected Command getCreateCommand(CreateRequest request) {
				return null;
			}
			
			@Override
			protected EditPolicy createChildEditPolicy(EditPart child) {
				EditPolicy result = child.getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
				if (result == null) {
					result = new NonResizableEditPolicy();
				}
				return result;
			}
		};
	}	

	protected boolean addFixedChild(EditPart childEditPart) {
		if (childEditPart instanceof GateEditPart) {
			IBorderItemLocator locator = new GateLocator(getMainFigure());
			getBorderedFigure().getBorderItemContainer().add(((GateEditPart) childEditPart).getFigure(), locator);
			return true;
		}

		return super.addFixedChild(childEditPart);
	}

	@Override
	public DragTracker getDragTracker(Request req) {
		return new PapyrusDragEditPartsTrackerEx(this, true, false, false) {
			@Override
			protected void setCloneActive(boolean cloneActive) {
				super.setCloneActive(false); // Disable cloning
			}			
		};
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#registerVisuals()
	 *
	 */
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		refreshCoveredLifelines();	
	}

	protected void refreshCoveredLifelines() {
		InteractionUse intUse = (InteractionUse)resolveSemanticElement();
		Interaction interaction = intUse.getEnclosingInteraction();
		List<Lifeline> allLifelines = interaction.getLifelines();
		List<Lifeline> coveredLifelines = intUse.getCovereds();
		List<IFigure> figures = new ArrayList<IFigure>();
		Rectangle newBounds = null;
		for (Lifeline lf : allLifelines) {
			View vw = ViewUtilities.getViewForElement(getDiagramView(),lf);
			if (coveredLifelines.contains(lf)) {
				Rectangle r = ViewUtilities.absoluteLayoutConstraint(getViewer(), vw);
				if (newBounds == null)
					newBounds = r.getCopy();
				else
					newBounds.union(r);
				continue;
			}
			GraphicalEditPart ep = (GraphicalEditPart)getViewer().getEditPartRegistry().get(vw);
			figures.add(ep.getFigure());
		}

		if (newBounds != null) {
			Rectangle intUseRect = ViewUtilities.absoluteLayoutConstraint(getViewer(),(View)getModel());
			if ((intUseRect.x != newBounds.x || intUseRect.width != newBounds.width) && newBounds.width > 0) {
				intUseRect.x = newBounds.x;
				intUseRect.width = newBounds.width; 
				intUseRect = ViewUtilities.toRelativeForLayoutConstraints(getViewer(), (View)((View)getModel()).eContainer(), intUseRect);
				((GraphicalEditPart) getParent()).setLayoutConstraint(
						this,
						getFigure(),
						intUseRect);
			}
		}
		if (primaryShape instanceof InteractionUseRectangleFigure)
			((InteractionUseRectangleFigure)primaryShape).setNonCoveredLifelinesFigures(figures);
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		ConnectionAnchor sourceAnchor = createAnchor(request, UMLElementTypes.MESSAGE, AbstractMessageEditPart.class);
		
		if (sourceAnchor == null) {
			sourceAnchor = super.getSourceConnectionAnchor(request);
		}
		return sourceAnchor;
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		ConnectionAnchor sourceAnchor = createAnchor(request, UMLElementTypes.MESSAGE, AbstractMessageEditPart.class);
		
		if (sourceAnchor == null) {
			sourceAnchor = super.getSourceConnectionAnchor(request);
		}
		return sourceAnchor;
	}

	private ConnectionAnchor createAnchor(Request request, IElementType elementType, Class<?> messageType) {
		if (request instanceof CreateUnspecifiedTypeConnectionRequest) {

			CreateUnspecifiedTypeConnectionRequest createRequest = (CreateUnspecifiedTypeConnectionRequest) request;

			List<?> relationshipTypes = createRequest.getElementTypes();
			for (Object obj : relationshipTypes) {
				IElementType type = (IElementType)obj;
				if (type.equals(elementType) || Arrays.asList(type.getAllSuperTypes()).contains(elementType)) {
					return createAnchor(createRequest.getLocation().getCopy());
				}
			}
		} else if (request instanceof CreateConnectionViewRequest) {
			CreateConnectionViewRequest createRequest = (CreateConnectionViewRequest) request;
			ConnectionViewDescriptor connectionViewDescriptor = createRequest.getConnectionViewDescriptor();
			if (connectionViewDescriptor != null) {
				IElementType type = connectionViewDescriptor.getElementAdapter().getAdapter(IElementType.class);
				if (type.equals(elementType) || Arrays.asList(type.getAllSuperTypes()).contains(elementType)) {
					return createAnchor(createRequest.getLocation().getCopy());
				}
			}
		} else if (request instanceof ReconnectRequest) {
			ReconnectRequest reconnectRequest = (ReconnectRequest) request;
			ConnectionEditPart connectionEditPart = reconnectRequest.getConnectionEditPart();
			// Fixed bug creating anchors for MessageLost and MessageFound.
			if (messageType.isInstance(connectionEditPart) && request instanceof LocationRequest) {
				return createAnchor(((LocationRequest) request).getLocation().getCopy());
			}
		}
		return null;
	}
	
	private ConnectionAnchor createAnchor(Point location) {
		Rectangle rect = getFigure().getBounds().getCopy();
		getFigure().translateToAbsolute(rect);
		int leftDiff = Math.abs(location.x - rect.x());
		int rightDif = Math.abs(location.x - rect.right());

		if (leftDiff < rightDif) {
			location.x = rect.x;
		} else {
			location.x = rect.right();
		}
		SequenceUtil.getSnappedLocation(this, location);
		return AnchorHelper.InnerPointAnchor.createAnchorAtLocation(shape, new PrecisionPoint(location));
	}

}
