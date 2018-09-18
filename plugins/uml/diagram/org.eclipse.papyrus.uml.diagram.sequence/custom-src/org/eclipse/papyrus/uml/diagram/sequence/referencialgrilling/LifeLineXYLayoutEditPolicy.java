/*****************************************************************************
 * Copyright (c) 2016, 2018 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 526079
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Bug 538466
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.BorderedBorderItemEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest.ViewAndElementDescriptor;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.XYLayoutWithConstrainedResizedEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramEditPartsUtil;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetMoveAllLineAtSamePositionCommand;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetResizeAndLocationCommand;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CLifeLineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionUseEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.SequenceReferenceEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.preferences.CustomDiagramGeneralPreferencePage;
import org.eclipse.papyrus.uml.diagram.sequence.util.ExecutionSpecificationUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.LifelineMessageDeleteHelper;
import org.eclipse.papyrus.uml.diagram.sequence.util.LogOptions;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.papyrus.uml.service.types.element.UMLDIElementTypes;
import org.eclipse.papyrus.uml.service.types.utils.ElementUtil;

/**
 * This class is used to manage node element in the compartment by using grill system.
 * this class has been customized to prevent the strange feedback of lifeline during the move
 *
 */
public class LifeLineXYLayoutEditPolicy extends XYLayoutWithConstrainedResizedEditPolicy implements IGrillingEditpolicy {

	protected DisplayEvent displayEvent;

	/**
	 * Constructor.
	 *
	 */
	public LifeLineXYLayoutEditPolicy() {
		super();

	}

	/**
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#setHost(org.eclipse.gef.EditPart)
	 *
	 * @param host
	 */
	@Override
	public void setHost(EditPart host) {
		super.setHost(host);
		displayEvent = new DisplayEvent(getHost());
	}

	@Override
	protected Command getChangeConstraintCommand(ChangeBoundsRequest request) {
		request.setConstrainedResize(false);
		return super.getChangeConstraintCommand(request);
	}

	/*
	 * Override to use to deal with causes where the point is UNDERFINED
	 * we will ask the layout helper to find a location for us
	 *
	 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#getConstraintFor(org.eclipse.gef.requests.CreateRequest)
	 */
	@Override
	protected Object getConstraintFor(CreateRequest request) {
		// Used during the creation from the palette
		Object constraint = super.getConstraintFor(request);
		if (request instanceof CreateViewAndElementRequest) {
			CreateViewAndElementRequest req = (CreateViewAndElementRequest) request;
			ViewAndElementDescriptor descriptor = (req).getViewAndElementDescriptor();
			IElementType elementType = descriptor.getElementAdapter().getAdapter(IElementType.class);
			if (ElementUtil.isTypeOf(elementType, UMLDIElementTypes.ACTION_EXECUTION_SPECIFICATION_SHAPE) ||
					ElementUtil.isTypeOf(elementType, UMLDIElementTypes.BEHAVIOR_EXECUTION_SPECIFICATION_SHAPE)) {
				Rectangle parentBound = getHostFigure().getBounds();
				if (constraint instanceof Rectangle) {
					Rectangle constraintRect = (Rectangle) constraint;
					RootEditPart drep = getHost().getRoot();
					if (drep instanceof DiagramRootEditPart) {

						double spacing = ((DiagramRootEditPart) drep).getGridSpacing();
						if (constraintRect.height == -1) {
							constraintRect.height = AbstractExecutionSpecificationEditPart.DEFAUT_HEIGHT;
						}
						constraintRect.width = AbstractExecutionSpecificationEditPart.DEFAUT_WIDTH;
						constraintRect.x = (parentBound.width / 2) - (constraintRect.width / 2);

						constraintRect = ExecutionSpecificationUtil.calculateExecutionSpecificationCorrectLocation(((CLifeLineEditPart) getHost()), constraintRect, null);

						if (DiagramEditPartsUtil.isSnapToGridActive(getHost())) {
							int modulo = AbstractExecutionSpecificationEditPart.DEFAUT_HEIGHT / (int) spacing;
							constraintRect.height = modulo * (int) spacing;
						}

						constraint = constraintRect;
					}
				}
			}
		}
		return constraint;
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.XYLayoutEditPolicy#getConstraintFor(org.eclipse.gef.requests.ChangeBoundsRequest, org.eclipse.gef.GraphicalEditPart)
	 *
	 * @param request
	 * @param child
	 * @return
	 */
	@Override
	protected Object getConstraintFor(ChangeBoundsRequest request, GraphicalEditPart child) {


		if (child instanceof BorderedBorderItemEditPart) {
			Rectangle constraint = new Rectangle(child.getFigure().getBounds());

			return constraint;

		}
		return super.getConstraintFor(request, child);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#showLayoutTargetFeedback(org.eclipse.gef.Request)
	 */
	@Override
	protected void showLayoutTargetFeedback(Request request) {
		// feed back during the creation from the palette
		RootEditPart drep = getHost().getRoot();
		Rectangle parentBound = getHostFigure().getBounds().getCopy();
		getHostFigure().getParent().translateToAbsolute(parentBound);
		UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "LifeLineBounds On Screen:" + parentBound); //$NON-NLS-1$
		if (drep instanceof DiagramRootEditPart) {
			double spacing = ((DiagramRootEditPart) drep).getGridSpacing();
			if (request instanceof org.eclipse.papyrus.infra.gmfdiag.common.service.palette.AspectUnspecifiedTypeCreationTool.CreateAspectUnspecifiedTypeRequest) {
				IElementType elementType = (IElementType) ((org.eclipse.papyrus.infra.gmfdiag.common.service.palette.AspectUnspecifiedTypeCreationTool.CreateAspectUnspecifiedTypeRequest) request).getElementTypes().get(0);
				if (ElementUtil.isTypeOf(elementType, UMLDIElementTypes.ACTION_EXECUTION_SPECIFICATION_SHAPE) ||
						ElementUtil.isTypeOf(elementType, UMLDIElementTypes.BEHAVIOR_EXECUTION_SPECIFICATION_SHAPE)) {
					((CreateRequest) request).setLocation(new Point((parentBound.x + (parentBound.width / 2) - AbstractExecutionSpecificationEditPart.DEFAUT_WIDTH / 2), ((CreateRequest) request).getLocation().y));
					int modulo = AbstractExecutionSpecificationEditPart.DEFAUT_HEIGHT / (int) spacing;
					((CreateRequest) request).setSize(new Dimension(AbstractExecutionSpecificationEditPart.DEFAUT_WIDTH, modulo * (int) spacing));

					displayEvent.addFigureEvent(getHostFigure().getParent().getParent(), ((CreateRequest) request).getLocation());
				}
				/*
				 * Fix of Bug 531471 - [SequenceDiagram] Combined Fragment / Interaction Use should be create over a Lifeline.
				 * Recalculation of feedback location of combined fragment creation after update in:
				 * LifelineCreationEditPolicy.getCreateElementAndViewCommand()
				 */
				else if (ElementUtil.isTypeOf(elementType, UMLDIElementTypes.COMBINED_FRAGMENT_SHAPE)
						|| ElementUtil.isTypeOf(elementType, UMLDIElementTypes.INTERACTION_USE_SHAPE)) {

					Rectangle boundsLifeline = getHostFigure().getBounds();
					Point pointCombinedFragment = ((CreateRequest) request).getLocation();

					pointCombinedFragment.x = pointCombinedFragment.x - boundsLifeline.x;
					pointCombinedFragment.y = pointCombinedFragment.y - boundsLifeline.y;

					((CreateRequest) request).setLocation(pointCombinedFragment);
				}

			}

		}

		super.showLayoutTargetFeedback(request);
	}

	/**
	 * @see org.eclipse.gef.editpolicies.GraphicalEditPolicy#addFeedback(org.eclipse.draw2d.IFigure)
	 *
	 * @param figure
	 */
	@Override
	protected void addFeedback(IFigure figure) {
		super.addFeedback(figure);



	}


	/**
	 * @see org.eclipse.gef.editpolicies.GraphicalEditPolicy#removeFeedback(org.eclipse.draw2d.IFigure)
	 *
	 * @param figure
	 */
	@Override
	protected void removeFeedback(IFigure figure) {

		displayEvent.removeFigureEvent(getHostFigure().getParent().getParent());
		super.removeFeedback(figure);

	}

	/**
	 * Called in response to a <tt>REQ_RESIZE_CHILDREN</tt> request.
	 *
	 * This implementation creates a <tt>SetPropertyCommand</i> and sets
	 * the <tt>ID_BOUNDS</tt> property value to the supplied constraints.
	 *
	 * @param child
	 *            the element being resized.
	 * @param constraint
	 *            the elements new bounds.
	 * @return {@link SetResizeAndLocationCommand}
	 */
	@Override
	protected Command createChangeConstraintCommand(
			EditPart child,
			Object constraint) {

		Rectangle newBounds = (Rectangle) constraint;
		View shapeView = (View) child.getModel();

		final CompoundCommand subCommand = new CompoundCommand("Edit Execution Specification positions"); //$NON-NLS-1$

		if (child instanceof AbstractExecutionSpecificationEditPart) {
			RootEditPart drep = getHost().getRoot();
			if (drep instanceof DiagramRootEditPart) {

				// Get the initial Rectangle from the edit part
				Rectangle initialRectangle = null;
				final Object view = ((AbstractExecutionSpecificationEditPart) child).getModel();
				if (view instanceof Node) {
					final Bounds bounds = BoundForEditPart.getBounds((Node) view);
					initialRectangle = new Rectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
				}

				double spacing = ((DiagramRootEditPart) drep).getGridSpacing();
				Rectangle parentBound = getHostFigure().getBounds();
				// Initial default x and y positions
				newBounds.setLocation(new Point((parentBound.width / 2) - (AbstractExecutionSpecificationEditPart.DEFAUT_WIDTH / 2), newBounds.getLocation().y));

				// Manage a collection of edit parts to skip during the calculation of new bounds (because the strong references are moved with the execution specification and the weak references can be moved with the execution specification)
				final Collection<EditPart> editPartsToSkipForCalculation = new HashSet<>();
				if (child.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE) != null) {
					final AbstractExecutionSpecificationEditPart execSpecEditPart = (AbstractExecutionSpecificationEditPart) child;
					final SequenceReferenceEditPolicy references = (SequenceReferenceEditPolicy) execSpecEditPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE);
					editPartsToSkipForCalculation.addAll(references.getStrongReferences().keySet());

					boolean mustMoveBelowAtMovingDown = UMLDiagramEditorPlugin.getInstance().getPreferenceStore().getBoolean(CustomDiagramGeneralPreferencePage.PREF_MOVE_BELOW_ELEMENTS_AT_MESSAGE_DOWN);

					if (mustMoveBelowAtMovingDown) {
						editPartsToSkipForCalculation.addAll(references.getWeakReferences().keySet());
					}
				}
				editPartsToSkipForCalculation.add(child);

				final CLifeLineEditPart lifeLineEditPart = (CLifeLineEditPart) getHost();
				final Map<AbstractExecutionSpecificationEditPart, Rectangle> executionSpecificationRectangles = ExecutionSpecificationUtil.getRectangles(lifeLineEditPart);

				Rectangle boundsToRectangle = null;
				CompoundCommand compoundCommand = null;

				// Loop until found command for the execution specifications bounds (because by moving other execution specification, the first one can be moved another time).
				do {
					// Calculate the moved execution specification bounds
					boundsToRectangle = ExecutionSpecificationUtil.calculateExecutionSpecificationCorrectLocation(
							lifeLineEditPart, executionSpecificationRectangles, new Rectangle(newBounds.x, newBounds.y, newBounds.width, newBounds.height), editPartsToSkipForCalculation);

					if (boundsToRectangle.height == -1) {
						boundsToRectangle.height = AbstractExecutionSpecificationEditPart.DEFAUT_HEIGHT;
					}
					if (DiagramEditPartsUtil.isSnapToGridActive(getHost())) {
						int modulo = boundsToRectangle.height / (int) spacing;
						boundsToRectangle.setSize(new Dimension(AbstractExecutionSpecificationEditPart.DEFAUT_WIDTH, modulo * (int) spacing));
					}

					// Get the possible command of execution specification bounds modification
					compoundCommand = ExecutionSpecificationUtil.getExecutionSpecificationToMove(lifeLineEditPart, executionSpecificationRectangles, initialRectangle, boundsToRectangle, child);
					if (null != compoundCommand && !compoundCommand.isEmpty()) {
						subCommand.add(compoundCommand);
					}
				} while (compoundCommand != null && !compoundCommand.isEmpty());

				newBounds.setBounds(boundsToRectangle);
			}
		}

		TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost())
				.getEditingDomain();
		ICommand boundsCommand = new SetResizeAndLocationCommand(editingDomain,
				DiagramUIMessages.SetLocationCommand_Label_Resize,
				new EObjectAdapter(shapeView),
				newBounds);
		CompoundCommand compoundCommand = new CompoundCommand();
		compoundCommand.add(new ICommandProxy(boundsCommand));

		if (!subCommand.isEmpty()) {
			compoundCommand.add(subCommand);
		}

		return compoundCommand;
	}


	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.XYLayoutWithConstrainedResizedEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
	 *
	 * @param request
	 * @return
	 */
	@Override
	protected Command getCreateCommand(CreateRequest request) {

		request.setLocation(displayEvent.getRealEventLocation(request.getLocation()));

		DiagramEditPart diagramEditPart = getDiagramEditPart(getHost());

		GridManagementEditPolicy grid = (GridManagementEditPolicy) diagramEditPart.getEditPolicy(GridManagementEditPolicy.GRID_MANAGEMENT);
		if (grid != null) {
			CompoundCommand cmd = new CompoundCommand();
			SetMoveAllLineAtSamePositionCommand setMoveAllLineAtSamePositionCommand = new SetMoveAllLineAtSamePositionCommand(grid, false);
			cmd.add(setMoveAllLineAtSamePositionCommand);
			cmd.add(super.getCreateCommand(request));
			setMoveAllLineAtSamePositionCommand = new SetMoveAllLineAtSamePositionCommand(grid, true);
			cmd.add(setMoveAllLineAtSamePositionCommand);
			return cmd;
		}
		return super.getCreateCommand(request);
	}

	/**
	 * In the specific case of Execution Specification, the resize of the Lifeline should trigger the move of the ES to remain centered on it.
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.XYLayoutEditPolicy#getCommand(org.eclipse.gef.Request)
	 *
	 * @param request
	 *            general request
	 * @return the default command in addition with he commands to move all the ExecutionSpecification
	 */
	@Override
	public Command getCommand(Request request) {

		CompoundCommand cmd = new CompoundCommand();

		LifelineEditPart llEditPart = (LifelineEditPart) getHost();
		if (!LifelineMessageDeleteHelper.hasIncomingMessageDelete(llEditPart)) {

			Command superCmd = super.getCommand(request);
			if (null != superCmd && superCmd.canExecute()) {
				cmd.add(superCmd);
			}

			// When resizing Lifeline, move the ES accordingly (only on the x direction)
			if (REQ_RESIZE.equals(request.getType()) && request instanceof ChangeBoundsRequest) {
				ChangeBoundsRequest boundsReq = (ChangeBoundsRequest) request;
				Dimension sizeDelta = boundsReq.getSizeDelta();

				if (sizeDelta.width > 0) {
					List children = getHost().getChildren();
					Iterator iter = children.iterator();
					while (iter.hasNext()) {
						EditPart child = (EditPart) iter.next();
						Command moveChildrenCmd = null;
						if (child instanceof AbstractExecutionSpecificationEditPart) {
							AbstractExecutionSpecificationEditPart ES = (AbstractExecutionSpecificationEditPart) child;
							// Building the new Request for the ES
							ChangeBoundsRequest moveESRequest = new ChangeBoundsRequest(REQ_RESIZE);
							moveESRequest.setEditParts(ES);
							moveESRequest.setResizeDirection(boundsReq.getResizeDirection());
							moveESRequest.setMoveDelta(new Point(sizeDelta.width() / 2, 0));
							// Get the according command
							moveChildrenCmd = ES.getCommand(moveESRequest);

						}

						// for all the ES, add the get command
						if (null != moveChildrenCmd && moveChildrenCmd.canExecute()) {
							cmd.add(moveChildrenCmd);
						}
					}
				}
			}
		}

		if (cmd.isEmpty()) {
			return null;
		}
		return cmd;
	}


	/**
	 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createAddCommand(org.eclipse.gef.requests.ChangeBoundsRequest, org.eclipse.gef.EditPart, java.lang.Object)
	 *
	 * @param request
	 * @param child
	 * @param constraint
	 * @return
	 */
	@Override
	protected Command createAddCommand(ChangeBoundsRequest request, EditPart child, Object constraint) {
		if (child instanceof LifelineEditPart || child instanceof CombinedFragmentEditPart || child instanceof InteractionUseEditPart) {
			return UnexecutableCommand.INSTANCE;
			// The reparent of execution specification in another life line is not allowed
		} else if (child instanceof AbstractExecutionSpecificationEditPart && getHost() instanceof CLifeLineEditPart) {
			final LifelineEditPart parentLifeLine = SequenceUtil.getParentLifelinePart(child);
			if (null != parentLifeLine && !parentLifeLine.equals(getHost())) {
				return UnexecutableCommand.INSTANCE;
			}
		}
		return super.createAddCommand(request, child, constraint);
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#showTargetFeedback(org.eclipse.gef.Request)
	 *
	 * @param request
	 */
	@Override
	public void showTargetFeedback(Request request) {
		if (request instanceof ChangeBoundsRequest) {
			ChangeBoundsRequest changeBoundsRequest = (ChangeBoundsRequest) request;

			if (changeBoundsRequest.getEditParts().get(0) instanceof LifelineEditPart) {
				changeBoundsRequest.setMoveDelta(new Point(changeBoundsRequest.getMoveDelta().x, 0));
			}
		}
		super.showTargetFeedback(request);
	}
}
