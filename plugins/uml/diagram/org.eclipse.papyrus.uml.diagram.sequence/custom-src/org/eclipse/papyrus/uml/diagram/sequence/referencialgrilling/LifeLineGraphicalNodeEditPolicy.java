/*****************************************************************************
 * Copyright (c) 2017 CEA LIST, ALL4TEC, EclipseSource and others.
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
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 519621, 519756, 526191
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Bug 531596
 *   EclipseSource - Bug 536641
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.FeedbackHelper;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionNodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
import org.eclipse.gmf.runtime.draw2d.ui.figures.BaseSlidableAnchor;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.papyrus.commands.wrappers.GMFtoGEFCommandWrapper;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.NodeEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultGraphicalNodeEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.PapyrusSlidableSnapToGridAnchor;
import org.eclipse.papyrus.infra.gmfdiag.common.service.palette.AspectUnspecifiedTypeConnectionTool.CreateAspectUnspecifiedTypeConnectionRequest;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramEditPartsUtil;
import org.eclipse.papyrus.infra.services.edit.utils.RequestParameterConstants;
import org.eclipse.papyrus.uml.diagram.sequence.command.CreateExecutionSpecificationWithMessage;
import org.eclipse.papyrus.uml.diagram.sequence.command.DropDestructionOccurenceSpecification;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetResizeAndLocationCommand;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetResizeCommand;
import org.eclipse.papyrus.uml.diagram.sequence.draw2d.routers.MessageRouter;
import org.eclipse.papyrus.uml.diagram.sequence.edit.helpers.AnchorHelper;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CLifeLineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageDeleteEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.CustomGraphicalNodeEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.util.LifelineEditPartUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.LifelineMessageCreateHelper;
import org.eclipse.papyrus.uml.diagram.sequence.util.LifelineMessageDeleteHelper;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceDiagramConstants;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.papyrus.uml.service.types.element.UMLDIElementTypes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.OccurrenceSpecification;

/**
 * This class overload all creation of link between lifelines
 * pay attention : this editpolicy launch a display of event during the move of the mouse
 */
public class LifeLineGraphicalNodeEditPolicy extends DefaultGraphicalNodeEditPolicy implements IGrillingEditpolicy {


	private GraphicalNodeEditPolicy graphicalNodeEditPolicy = null;
	private DisplayEvent displayEvent;
	private boolean precisionMode;


	/** the router to use for messages */
	public static ConnectionRouter messageRouter = new MessageRouter();

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultGraphicalNodeEditPolicy#getConnectionCreateCommand(org.eclipse.gef.requests.CreateConnectionRequest)
	 *
	 * @param request
	 * @return
	 */
	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		// Snap to grid the request location
		request.setLocation(SequenceUtil.getSnappedLocation(getHost(), request.getLocation()));

		displayEvent.addFigureEvent(getHostFigure(), request.getLocation());
		OccurrenceSpecification end = getPreviousEventFromPosition(request.getLocation());
		if (end instanceof MessageEnd) {
			Map<String, Object> extendedData = request.getExtendedData();
			extendedData.put(org.eclipse.papyrus.uml.service.types.utils.SequenceRequestConstant.PREVIOUS_EVENT, end);
			request.setExtendedData(extendedData);
		}

		MessageOccurrenceSpecification mos = displayEvent.getMessageEvent(getHostFigure(), ((CreateRequest) request).getLocation());
		if (mos != null) {
			Point location = request.getLocation();
			if (location != displayEvent.getRealEventLocation(location)) {
				request.setLocation(displayEvent.getRealEventLocation(location));
			}
		}

		OccurrenceSpecification os = displayEvent.getActionExecutionSpecificationEvent(getHostFigure(), ((CreateRequest) request).getLocation());
		// add a param if we must replace an event of the execution specification
		if (os instanceof ExecutionOccurrenceSpecification) {
			Map<String, Object> extendedData = request.getExtendedData();
			extendedData.put(org.eclipse.papyrus.uml.service.types.utils.SequenceRequestConstant.MESSAGE_SENTEVENT_REPLACE_EXECUTIONEVENT, os);
			request.setExtendedData(extendedData);
			// Update the Request Location to match the Event Location
			Point location = request.getLocation();

			if (location != displayEvent.getRealEventLocation(location)) {
				request.setLocation(displayEvent.getRealEventLocation(location));
			}
		} else if (os instanceof MessageEnd) {
			// Event of Exec Spec have been already replaced. Only one message can be related to start or finish.
			return UnexecutableCommand.INSTANCE;
		}

		return super.getConnectionCreateCommand(request);
	}

	/**
	 * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#eraseTargetConnectionFeedback(org.eclipse.gef.requests.DropRequest)
	 *
	 * @param request
	 */
	@Override
	protected void eraseTargetConnectionFeedback(DropRequest request) {
		super.eraseTargetConnectionFeedback(request);
		displayEvent.removeFigureEvent(getHostFigure());
	}


	/**
	 * This method take into account the horizontal Delta to have an horizontal feedback if the target point is in the Y delta.
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.GraphicalNodeEditPolicy#getTargetConnectionAnchor(org.eclipse.gef.requests.CreateConnectionRequest)
	 *
	 * @param request
	 * @return
	 */
	@Override
	protected ConnectionAnchor getTargetConnectionAnchor(CreateConnectionRequest request) {
		// Snap to grid the request location
		request.setLocation(SequenceUtil.getSnappedLocation(getHost(), request.getLocation()));
		ConnectionAnchor targetConnectionAnchor = super.getTargetConnectionAnchor(request);
		ConnectionAnchor newTargetConnectionAnchor = targetConnectionAnchor;
		if (null != targetConnectionAnchor) {
			Point referenceTargetPoint = targetConnectionAnchor.getReferencePoint();
			if (request instanceof CreateAspectUnspecifiedTypeConnectionRequest) {
				CreateRequest requestForType = getCreateMessageRequest((CreateAspectUnspecifiedTypeConnectionRequest) request);
				if (null != requestForType) {
					Map<String, Object> extendedData = requestForType.getExtendedData();
					Point sourceLocation = (Point) extendedData.get(RequestParameterConstants.EDGE_SOURCE_POINT);

					if (referenceTargetPoint != null && sourceLocation != null) {
						if (UMLDIElementTypes.MESSAGE_CREATE_EDGE.getSemanticHint().equals(((CreateConnectionViewAndElementRequest) requestForType).getConnectionViewAndElementDescriptor().getSemanticHint())
								|| (isHorizontalConnection(sourceLocation, referenceTargetPoint))
										&& request.getSourceEditPart() != request.getTargetEditPart()
										&& !UMLDIElementTypes.MESSAGE_LOST_EDGE.getSemanticHint().equals(((CreateConnectionViewAndElementRequest) requestForType).getConnectionViewAndElementDescriptor().getSemanticHint())) {
							newTargetConnectionAnchor = getHorizontalAnchor(targetConnectionAnchor, referenceTargetPoint);
						}
					}
				}
			}
		}

		return newTargetConnectionAnchor;
	}


	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.GraphicalNodeEditPolicy#getSourceConnectionAnchor(org.eclipse.gef.requests.CreateConnectionRequest)
	 */
	@Override
	protected ConnectionAnchor getSourceConnectionAnchor(final CreateConnectionRequest request) {
		// Snap to event if necessary
		request.setLocation(displayEvent.getRealEventLocation(request.getLocation()));
		return super.getSourceConnectionAnchor(request);
	}

	/**
	 * @param request
	 *            initial Request
	 * @return The real request for the creation of the message
	 */
	protected CreateRequest getCreateMessageRequest(CreateAspectUnspecifiedTypeConnectionRequest request) {
		CreateRequest req = null;

		req = request.getRequestForType(UMLDIElementTypes.MESSAGE_ASYNCH_EDGE);

		if (null == req) {
			req = request.getRequestForType(UMLDIElementTypes.MESSAGE_SYNCH_EDGE);
		}

		if (null == req) {
			req = request.getRequestForType(UMLDIElementTypes.MESSAGE_EDGE);
		}

		if (null == req) {
			req = request.getRequestForType(UMLDIElementTypes.MESSAGE_OCCURRENCE_SPECIFICATION_SHAPE);
		}

		if (null == req) {
			req = request.getRequestForType(UMLDIElementTypes.MESSAGE_REPLY_EDGE);
		}

		if (null == req) {
			req = request.getRequestForType(UMLDIElementTypes.MESSAGE_DELETE_EDGE);
		}

		if (null == req) {
			req = request.getRequestForType(UMLDIElementTypes.MESSAGE_FOUND_EDGE);
		}

		if (null == req) {
			req = request.getRequestForType(UMLDIElementTypes.MESSAGE_LOST_EDGE);
		}

		if (null == req) {
			req = request.getRequestForType(UMLDIElementTypes.MESSAGE_CREATE_EDGE);
		}

		return req;
	}

	/**
	 * @param targetConnectionAnchor
	 *            The initial TargetAnchor
	 * @param referenceTargetPoint
	 *            The Target Point
	 * @param sourceLocation
	 *            The Source Point
	 * @return the new ConnectionAnchor forcing the horizontal Position if in the delta.
	 */
	protected ConnectionAnchor getHorizontalAnchor(ConnectionAnchor targetConnectionAnchor, Point referenceTargetPoint) {
		ConnectionAnchor newTargetConnectionAnchor = null;

		if (targetConnectionAnchor.getOwner() instanceof NodeFigure) {
			NodeFigure figure = (NodeFigure) targetConnectionAnchor.getOwner();
			PrecisionPoint pt = BaseSlidableAnchor.getAnchorRelativeLocation(referenceTargetPoint, figure.getBounds());
			newTargetConnectionAnchor = new PapyrusSlidableSnapToGridAnchor(figure, pt) {

				/**
				 * Force the Horizontal position of the feedback
				 *
				 * @see org.eclipse.gmf.runtime.draw2d.ui.figures.BaseSlidableAnchor#getIntersectionPoints(org.eclipse.draw2d.geometry.Point, org.eclipse.draw2d.geometry.Point)
				 */
				@Override
				protected PointList getIntersectionPoints(Point ownReference, Point foreignReference) {
					ownReference.setY(foreignReference.y());
					return super.getIntersectionPoints(ownReference, foreignReference);
				}

			};
		} else {
			newTargetConnectionAnchor = targetConnectionAnchor;
		}
		return newTargetConnectionAnchor;
	}

	/**
	 * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#eraseCreationFeedback(org.eclipse.gef.requests.CreateConnectionRequest)
	 *
	 * @param request
	 */
	@Override
	protected void eraseCreationFeedback(CreateConnectionRequest request) {
		super.eraseCreationFeedback(request);
		displayEvent = new DisplayEvent(getHost());
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

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultGraphicalNodeEditPolicy#getAfterConnectionCompleteCommand(org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest,
	 *      org.eclipse.emf.transaction.TransactionalEditingDomain)
	 *
	 * @param request
	 *            Initial Creation Request
	 * @param editingDomain
	 *            Editing Domain
	 * @return null (this is a special Case for Affixed node, lifeline has it's anchor inside the figure)
	 */
	@Override
	protected ICommand getAfterConnectionCompleteCommand(CreateConnectionViewAndElementRequest request, TransactionalEditingDomain editingDomain) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultGraphicalNodeEditPolicy#getConnectionAndRelationshipCompleteCommand(org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest)
	 */
	@Override
	protected Command getConnectionAndRelationshipCompleteCommand(CreateConnectionViewAndElementRequest request) {
		// Snap to grid the request location
		request.setLocation(SequenceUtil.getSnappedLocation(getHost(), request.getLocation()));
		// Update request with the real Location of the Event if location next to an Event
		Point realEventLocation = displayEvent.getRealEventLocation(request.getLocation());
		if (!request.getLocation().equals(realEventLocation)) {
			request.setLocation(realEventLocation);
		}

		Command cmd = super.getConnectionAndRelationshipCompleteCommand(request);

		// Initialize the modifier Shift Key = Precision Mode
		initModifier();

		// Check if the request is allowed or return an unexecutable Command
		if (!isAllowedMessageEnd(request)) {
			return UnexecutableCommand.INSTANCE;
		}

		// Check if a message can be consider as horizontal and update request accordingly
		forceHorizontalRequest(request);

		updateExtendedData(request);

		if (request.getConnectionViewAndElementDescriptor().getSemanticHint().equals(UMLDIElementTypes.MESSAGE_CREATE_EDGE.getSemanticHint())) {
			return getCreateEdgeCommand(request, cmd);
		}
		if (request.getConnectionViewAndElementDescriptor().getSemanticHint().equals(UMLDIElementTypes.MESSAGE_DELETE_EDGE.getSemanticHint())) {
			return getDeleteEdgeCommand(request, cmd);
		}
		if (request.getConnectionViewAndElementDescriptor().getSemanticHint().equals(UMLDIElementTypes.MESSAGE_ASYNCH_EDGE.getSemanticHint()) ||
				request.getConnectionViewAndElementDescriptor().getSemanticHint().equals(UMLDIElementTypes.MESSAGE_SYNCH_EDGE.getSemanticHint())) {
			return getSyncAsyncEdgeCommand(request, cmd);
		}
		if (request.getConnectionViewAndElementDescriptor().getSemanticHint().equals(UMLDIElementTypes.MESSAGE_FOUND_EDGE.getSemanticHint())) {
			// in the case of the found message, because the serialization is very specific , we must call basic editpolicy of GMF
			// so we create an new instance of the GraphicalNode editpolicy and we delegate the operation.
			return getBasicGraphicalNodeEditPolicy().getCommand(request);
		}

		return cmd;
	}

	/**
	 * This method update the Extended Data of the the Creation Request
	 * 1) Adding the Previous Event of the Target
	 * 2) Adding the OccurenceSpecification which needs to be replaced by the Message ReceiveEvent
	 *
	 * @param request
	 *            initial Request to be updated
	 */
	private void updateExtendedData(CreateConnectionViewAndElementRequest request) {
		OccurrenceSpecification end = getPreviousEventFromPosition(request.getLocation());
		if (end instanceof MessageEnd) {
			Map<String, Object> extendedData = request.getExtendedData();
			extendedData.put(org.eclipse.papyrus.uml.service.types.utils.SequenceRequestConstant.SECOND_PREVIOUS_EVENT, end);
			request.setExtendedData(extendedData);
		}
		// add a param if we must replace an event of the execution specification
		OccurrenceSpecification os = displayEvent.getActionExecutionSpecificationEvent(null, ((CreateRequest) request).getLocation());
		if (os instanceof ExecutionOccurrenceSpecification) {
			Map<String, Object> extendedData = request.getExtendedData();
			extendedData.put(org.eclipse.papyrus.uml.service.types.utils.SequenceRequestConstant.MESSAGE_RECEIVEEVENT_REPLACE_EXECUTIONEVENT, os);
			request.setExtendedData(extendedData);
		}
	}

	/**
	 * Initialize the Modifier
	 * PrecisionMode is defined by the Shift key
	 */
	private void initModifier() {
		PlatformUI.getWorkbench().getDisplay().addFilter(SWT.KeyDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// in case the SHIFT key is down, the creation enter in the precision Mode
				precisionMode = (event.keyCode == SWT.SHIFT);
			}
		});

		PlatformUI.getWorkbench().getDisplay().addFilter(SWT.KeyUp, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// in case the SHIFT key is released, the creation mode goes back to normal
				if (event.keyCode == SWT.SHIFT) {
					precisionMode = false;
				}
			}
		});
	}

	/**
	 * During creation of a message, check if the creating message can be considered as horizontal using a threshold
	 * If this is the case, update the request to force the location as horizontal.
	 *
	 * @param request
	 *            The request of Message creation
	 */
	protected void forceHorizontalRequest(Request request) {
		if (request instanceof CreateConnectionViewAndElementRequest) {
			Map<String, Object> extendedData = request.getExtendedData();
			Object sourceLocation = extendedData.get(RequestParameterConstants.EDGE_SOURCE_POINT);
			// only message with a target lower than the source is allowed.
			if (sourceLocation instanceof Point) {
				Point sourceLocationPoint = (Point) sourceLocation;
				Point targetLocation = ((CreateRequest) request).getLocation();
				// Check if the Connection can be considered as Horizontal
				if (sourceLocationPoint.y() != targetLocation.y()) {
					if (((CreateConnectionViewAndElementRequest) request).getConnectionViewAndElementDescriptor().getSemanticHint().equals(UMLDIElementTypes.MESSAGE_CREATE_EDGE.getSemanticHint())
							|| isHorizontalConnection(sourceLocationPoint, targetLocation)) {
						Point forceHorizontalPoint = new Point(targetLocation);
						forceHorizontalPoint.setY(sourceLocationPoint.y());
						// Update the request accordingly
						((CreateRequest) request).setLocation(forceHorizontalPoint);
					}
				}
			}
		}
	}

	/**
	 * isHorizontalConnection tests whether an asynchronous message is horizontal
	 *
	 * @param conn
	 *            controller representing the link
	 * @param newLine
	 *            points corresponding to message ends
	 * @return false if message is not asynchronous
	 *         true if the message is asynchronous and horizontal
	 */
	private boolean isHorizontalConnection(Point sourcePoint, Point targetPoint) {
		boolean horizontal = true;
		int realDelta = sourcePoint.y - targetPoint.y;

		if (!precisionMode) {
			// If delta not big enough the connection is consider as Horizontal
			horizontal = (Math.abs(realDelta) <= SequenceDiagramConstants.HORIZONTAL_MESSAGE_MAX_Y_DELTA);
		} else {
			horizontal = (Math.abs(realDelta) <= SequenceDiagramConstants.HORIZONTAL_MESSAGE_PRECISION_Y_DELTA);
		}

		return horizontal;
	}

	/**
	 * Get the Command for a Message Sync and Async creation
	 * Adding the command to create the AES or BES and update Grid position accordingly
	 *
	 * @param request
	 *            the initial request
	 * @param cmd
	 *            the initial Command
	 * @return Compound cmd with the Delete Occurrence Specification command
	 */
	protected Command getSyncAsyncEdgeCommand(CreateConnectionViewAndElementRequest request, Command cmd) {
		// in the case of messages of sort: synchCall, asynchCall or asynchSignal
		// an execution specification may be created at target
		OccurrenceSpecification messageEvent = displayEvent.getActionExecutionSpecificationEvent(getHostFigure(), request.getLocation());

		CompoundCommand compoundCommand = new CompoundCommand();
		compoundCommand.add(cmd);
		// If we are not into an existing event we create Execution specification in the same time
		if (null == messageEvent) {
			CreateExecutionSpecificationWithMessage createExecutionSpecificationwithMsg = new CreateExecutionSpecificationWithMessage(getDiagramEditPart(getHost()).getEditingDomain(), request, request.getTargetEditPart());
			compoundCommand.add(new GMFtoGEFCommandWrapper(createExecutionSpecificationwithMsg));
		}
		return compoundCommand;
	}

	/**
	 * Get the Command for a Message delete creation
	 * Adding the Delete Occurrence Specification command
	 *
	 * @param request
	 *            the initial request
	 * @param cmd
	 *            the initial Command
	 * @return Compound cmd with the Delete Occurrence Specification command
	 */
	protected Command getDeleteEdgeCommand(CreateConnectionViewAndElementRequest request, Command cmd) {
		// if it's the first message delete and the last event on the target lifeline
		if (!LifelineMessageDeleteHelper.hasIncomingMessageDelete(request.getTargetEditPart())) {
			Point relativeSnappedLocation = request.getLocation().getCopy();
			relativeSnappedLocation = SequenceUtil.getSnappedLocation(getHost(), relativeSnappedLocation);
			getHostFigure().getParent().translateToRelative(relativeSnappedLocation);

			if (false == request.getTargetEditPart() instanceof LifelineEditPart) {
				// TODO This may happen when creating e.g. a Context link
				return null;
			}
			if (LifelineEditPartUtil.getNextEventsFromPosition(relativeSnappedLocation, (LifelineEditPart) request.getTargetEditPart()).isEmpty()) {

				NodeEditPart targetEditPart = (NodeEditPart) request.getTargetEditPart();
				DropDestructionOccurenceSpecification dropDestructionOccurenceSpecification = new DropDestructionOccurenceSpecification(getDiagramEditPart(getHost()).getEditingDomain(), request, targetEditPart, request.getLocation().getCopy());
				CompoundCommand compoundCommand = new CompoundCommand();
				compoundCommand.add(cmd);
				compoundCommand.add(new GMFtoGEFCommandWrapper(dropDestructionOccurenceSpecification)); // Get resize command
				if (targetEditPart instanceof CLifeLineEditPart) {
					Bounds lifelineBounds = ((Bounds) ((Node) targetEditPart.getModel()).getLayoutConstraint());
					Dimension size = new Dimension(lifelineBounds.getWidth(), relativeSnappedLocation.y - lifelineBounds.getY());

					ICommand setSizeCommand = new SetResizeCommand(getDiagramEditPart(getHost()).getEditingDomain(), "Size LifeLine", new EObjectAdapter(((GraphicalEditPart) targetEditPart).getNotationView()), //$NON-NLS-1$
							size);
					compoundCommand.add(new GMFtoGEFCommandWrapper(setSizeCommand));
				}
				return compoundCommand;
			}
		}
		return UnexecutableCommand.INSTANCE;
	}


	/**
	 * Get the Command for a Message Create creation
	 * Adding the command to update the grid accordingly
	 *
	 * @param request
	 *            the initial request
	 * @param cmd
	 *            the initial Command
	 * @return Command for creating a Message Create command
	 */
	protected Command getCreateEdgeCommand(final CreateConnectionViewAndElementRequest request, final Command cmd) {
		// if it's the first message create
		if (request.getTargetEditPart() instanceof LifelineEditPart && !LifelineMessageCreateHelper.hasIncomingMessageCreate(request.getTargetEditPart())
				&& !LifelineEditPartUtil.hasPreviousEvent(request.getLocation().getCopy(), (LifelineEditPart) request.getTargetEditPart())) {
			NodeEditPart nodeEP = (NodeEditPart) request.getTargetEditPart();
			Map<String, Object> requestParameters = request.getExtendedData();
			final Point sourcePoint = ((Point) requestParameters.get(RequestParameterConstants.EDGE_SOURCE_POINT)).getCopy();
			return getSetLifelinePositionCommand(cmd, nodeEP, sourcePoint);
		}
		return UnexecutableCommand.INSTANCE;
	}

	/**
	 * Get the command to set lifeline position in case of MessageCreate reorient or creation.
	 *
	 * @param originalCommand
	 *            the original command which needs to be completed
	 * @param targetEditPart
	 *            the target edit part
	 * @param sourcePoint
	 *            the position of the target point
	 * @return the command
	 */
	protected CompoundCommand getSetLifelinePositionCommand(final Command originalCommand, final NodeEditPart targetEditPart, final Point sourcePoint) {
		CompoundCommand compoundCommand = new CompoundCommand();
		if (targetEditPart instanceof CLifeLineEditPart) {
			// Get the snapped location
			PrecisionPoint snappedLocation = SequenceUtil.getSnappedLocation(getHost(), sourcePoint);
			// Translate to relative
			getHostFigure().getParent().translateToRelative(snappedLocation);
			int stickerHeight = ((CLifeLineEditPart) targetEditPart).getStickerHeight();

			Rectangle bounds = getHostFigure().getBounds();

			// Calculate the new Y to get the message create at the middle of the life line head
			int newY = snappedLocation.y;
			// Calculate the new height depending to the current height, the new Y position and the life line head
			int newHeight = bounds.height() - snappedLocation.y;
			if (stickerHeight != -1) {
				newY = snappedLocation.y - (stickerHeight / 2);
				newHeight = newHeight + stickerHeight;
			}

			Rectangle newBounds = new Rectangle(new Point(bounds.x(), newY), new Dimension(bounds.width(), bounds.height() - snappedLocation.y + stickerHeight));

			ICommand setBoundsCommand = new SetResizeAndLocationCommand(getDiagramEditPart(getHost()).getEditingDomain(), "Move&Size LifeLine", new EObjectAdapter(((GraphicalEditPart) targetEditPart).getNotationView()), newBounds); //$NON-NLS-1$

			compoundCommand.add(originalCommand);
			compoundCommand.add(new GMFtoGEFCommandWrapper(setBoundsCommand));
		}
		return compoundCommand;
	}

	/**
	 * Check if the request is allowed
	 *
	 * @param request
	 *            The Connection END creation Request
	 * @return true if all the validation are passed
	 */
	private boolean isAllowedMessageEnd(CreateConnectionViewAndElementRequest request) {
		Boolean allowed = true;
		// check if target is lower than source
		if (!precisionMode) {
			Point targetLocation = request.getLocation();
			Map<String, Object> extendedData = request.getExtendedData();
			Object sourceLocation = extendedData.get(RequestParameterConstants.EDGE_SOURCE_POINT);
			if (sourceLocation instanceof Point) {
				allowed &= isTargetLowerThanSource(SequenceUtil.getSnappedLocation(getHost(), (Point) sourceLocation), targetLocation);
			}
		}

		// Check if we are in a execution event, and if the event have been already be replaced, we can't allow it.
		OccurrenceSpecification os = displayEvent.getActionExecutionSpecificationEvent(null, ((CreateRequest) request).getLocation());
		allowed &= !(null != os && !(os instanceof ExecutionOccurrenceSpecification));

		return allowed;
	}

	/**
	 * Validation1:
	 * Check if the Target point is Lower than the Source.
	 * The target should be lower than the source to be valid.
	 *
	 * @param request
	 *            The Connection END creation Request
	 *
	 * @return true if target location point is lower than source location point
	 */
	private Boolean isTargetLowerThanSource(Point sourceLocation, Point targetLocation) {
		// only message with a target lower than the source is allowed.
		return sourceLocation.y() <= targetLocation.y();
	}

	protected GraphicalNodeEditPolicy getBasicGraphicalNodeEditPolicy() {
		if (graphicalNodeEditPolicy == null) {
			graphicalNodeEditPolicy = new CustomGraphicalNodeEditPolicy();
			graphicalNodeEditPolicy.setHost(getHost());
		}
		return graphicalNodeEditPolicy;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultGraphicalNodeEditPolicy#getReconnectSourceCommand(org.eclipse.gef.requests.ReconnectRequest)
	 */
	@Override
	protected Command getReconnectSourceCommand(final ReconnectRequest request) {
		request.setLocation(SequenceUtil.getSnappedLocation(getHost(), request.getLocation()));
		// Check if the target is lower than the source
		Point targetLocation = SequenceUtil.getAbsoluteEdgeExtremity((ConnectionNodeEditPart) request.getConnectionEditPart(), false, true);
		if (!isTargetLowerThanSource(request.getLocation().getCopy(), targetLocation)) {
			Object object = request.getExtendedData().get(SequenceUtil.DO_NOT_CHECK_HORIZONTALITY);
			if (!(object instanceof Boolean) || ((object instanceof Boolean) && !((Boolean) object))) {// If not HorizontalMove parameter true
				return UnexecutableCommand.INSTANCE;
			}
		}

		// check that the location is not at the header
		NodeEditPart nodeEP = (NodeEditPart) request.getTarget();
		if (nodeEP instanceof CLifeLineEditPart) {
			Point location = request.getLocation().getCopy();
			// Translate to relative
			getHostFigure().getParent().translateToRelative(location);
			int stickerHeight = ((CLifeLineEditPart) nodeEP).getStickerHeight();
			if (location.y <= stickerHeight) {
				return UnexecutableCommand.INSTANCE;
			}
		}

		return getBasicGraphicalNodeEditPolicy().getCommand(request);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultGraphicalNodeEditPolicy#getReconnectTargetCommand(org.eclipse.gef.requests.ReconnectRequest)
	 */
	@Override
	protected Command getReconnectTargetCommand(final ReconnectRequest request) {
		Command command = null;
		// Snap to grid the request location
		request.setLocation(SequenceUtil.getSnappedLocation(getHost(), request.getLocation()));

		// Check if the target is lower than the source
		Point sourceLocation = SequenceUtil.getAbsoluteEdgeExtremity((ConnectionNodeEditPart) request.getConnectionEditPart(), true);
		if (sourceLocation != null && !isTargetLowerThanSource(sourceLocation, request.getLocation().getCopy())) {
			Object object = request.getExtendedData().get(SequenceUtil.DO_NOT_CHECK_HORIZONTALITY);
			if (!(object instanceof Boolean) || ((object instanceof Boolean) && !((Boolean) object))) {// If not HorizontalMove parameter true
				return UnexecutableCommand.INSTANCE;
			}
		}
		NodeEditPart nodeEP = (NodeEditPart) request.getTarget();

		// check that the location is not at the header
		if (nodeEP instanceof CLifeLineEditPart) {
			Point location = request.getLocation().getCopy();
			// Translate to relative
			getHostFigure().getParent().translateToRelative(location);
			int stickerHeight = ((CLifeLineEditPart) nodeEP).getStickerHeight();
			if (location.y <= stickerHeight) {
				return UnexecutableCommand.INSTANCE;
			}
		}

		Command reconnectTargetCommand = getBasicGraphicalNodeEditPolicy().getCommand(request);

		// in case of reconnect target for message create it is need to move up the old target and move down the new target
		if (nodeEP instanceof CLifeLineEditPart) {
			Point requestLocationCopy = request.getLocation().getCopy();
			if (request.getConnectionEditPart() instanceof MessageCreateEditPart) {
				if (!LifelineEditPartUtil.hasPreviousEvent(requestLocationCopy, (LifelineEditPart) getHost())) {

					command = new CompoundCommand();
					if (!LifelineMessageCreateHelper.hasIncomingMessageCreate(nodeEP)) {
						// if first message, need to move it down
						((CompoundCommand) command).add(getSetLifelinePositionCommand(reconnectTargetCommand, nodeEP, requestLocationCopy));
					} else {
						((CompoundCommand) command).add(reconnectTargetCommand);
					}
					// move up old target if the target is different of the source
					if (!request.getConnectionEditPart().getTarget().equals(request.getTarget())) {
						((CompoundCommand) command).add(LifelineEditPartUtil.getRestoreLifelinePositionOnMessageCreateRemovedCommand(request.getConnectionEditPart()));
					}
				} else {
					command = reconnectTargetCommand;
				}
			} else if (request.getConnectionEditPart() instanceof MessageDeleteEditPart) {
				if (!LifelineEditPartUtil.hasNextEvent(requestLocationCopy, (LifelineEditPart) getHost())) {
					command = new CompoundCommand();
					if (!LifelineMessageDeleteHelper.hasIncomingMessageDelete(nodeEP)) {

						NodeEditPart targetEditPart = (LifelineEditPart) getHost();
						DropDestructionOccurenceSpecification dropDestructionOccurenceSpecification = new DropDestructionOccurenceSpecification(getDiagramEditPart(getHost()).getEditingDomain(), request, targetEditPart, requestLocationCopy);
						CompoundCommand compoundCommand = new CompoundCommand();
						compoundCommand.add(reconnectTargetCommand);
						compoundCommand.add(new GMFtoGEFCommandWrapper(dropDestructionOccurenceSpecification));
						// Get resize command
						if (targetEditPart instanceof CLifeLineEditPart) {
							Bounds lifeLineBounds = ((Bounds) ((Node) targetEditPart.getModel()).getLayoutConstraint());
							ICommand setSizeCommand = new SetResizeCommand(getDiagramEditPart(getHost()).getEditingDomain(), "Size LifeLine", new EObjectAdapter(((GraphicalEditPart) targetEditPart).getNotationView()), //$NON-NLS-1$
									new Dimension(lifeLineBounds.getWidth(), request.getLocation().y - lifeLineBounds.getY()));
							compoundCommand.add(new GMFtoGEFCommandWrapper(setSizeCommand));
						}
					} else {
						((CompoundCommand) command).add(reconnectTargetCommand);
					}

					// restore position of the old target
					if (!request.getConnectionEditPart().getTarget().equals(request.getTarget())) {
						((CompoundCommand) command).add(LifelineEditPartUtil.getRestoreLifelinePositionOnMessageCreateRemovedCommand(request.getConnectionEditPart()));
					}
				} else {
					command = reconnectTargetCommand;
				}
			} else {
				command = reconnectTargetCommand;
			}
		} else {
			command = reconnectTargetCommand;
		}
		return command;
	}


	/**
	 * This method must look for event that are upper than the given position
	 *
	 * @param point
	 *            the position on the lifeline
	 */
	public OccurrenceSpecification getPreviousEventFromPosition(final Point point) {
		List<OccurrenceSpecification> previousEventsFromPosition = LifelineEditPartUtil.getPreviousEventsFromPosition(point, (LifelineEditPart) getHost());
		return previousEventsFromPosition.isEmpty() ? null : previousEventsFromPosition.get(0);
	}

	/**
	 * This method update the request in order to make the point at the correct position on the grill.
	 *
	 * @param request
	 *            the request
	 * @param wanted
	 *            the position has we want in the serialization
	 */
	protected void computeTargetPosition(CreateConnectionRequest request, PrecisionPoint wanted) {
		ConnectionAnchor anchor = ((NodeEditPart) request.getTargetEditPart()).getTargetConnectionAnchor(request);
		if (anchor instanceof AnchorHelper.InnerPointAnchor) {
			PrecisionPoint resultedPoint = BaseSlidableAnchor.parseTerminalString(((AnchorHelper.InnerPointAnchor) anchor).getTerminal());
			while (resultedPoint.getDistance(wanted) > 2) {
				Point original = request.getLocation().getCopy();
				PrecisionPoint diff = new PrecisionPoint(original.x - resultedPoint.x, original.y - resultedPoint.y);
				PrecisionRectangle ptOnScreen = new PrecisionRectangle(resultedPoint.x, resultedPoint.y, 0, 0);
				SimpleSnapHelper.snapAPoint(ptOnScreen, getHost().getRoot());
				PrecisionPoint Result = new PrecisionPoint(ptOnScreen.x + diff.x, ptOnScreen.y + diff.y);
				request.setLocation(Result);
				anchor = ((NodeEditPart) request.getTargetEditPart()).getTargetConnectionAnchor(request);
				resultedPoint = BaseSlidableAnchor.parseTerminalString(((AnchorHelper.InnerPointAnchor) anchor).getTerminal());
			}
		}
	}

	/**
	 * Get the replacing connection router for routing messages correctly
	 *
	 * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getDummyConnectionRouter(org.eclipse.gef.requests.CreateConnectionRequest)
	 */
	@Override
	protected ConnectionRouter getDummyConnectionRouter(CreateConnectionRequest req) {
		return messageRouter;
	}


	/**
	 *
	 * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getFeedbackHelper(org.eclipse.gef.requests.CreateConnectionRequest)
	 *      This method is used in order to manage the snap to grid of LOST Message
	 */
	@Override
	protected FeedbackHelper getFeedbackHelper(CreateConnectionRequest request) {
		if (request.getTargetEditPart() instanceof NodeEditPart) {
			ConnectionAnchor targetAnchor = ((NodeEditPart) request.getTargetEditPart()).getTargetConnectionAnchor(request);
			if (DiagramEditPartsUtil.isSnapToGridActive(getHost())) {
				// This part is very peculiar for lost and found message because the anchor is not standard.
				if (targetAnchor instanceof AnchorHelper.InnerPointAnchor) {
					PrecisionPoint pt = BaseSlidableAnchor.parseTerminalString(((AnchorHelper.InnerPointAnchor) targetAnchor).getTerminal());
					PrecisionRectangle ptOnScreen = new PrecisionRectangle(pt.x, pt.y, 0, 0);
					SimpleSnapHelper.snapAPoint(ptOnScreen, getHost().getRoot());
					computeTargetPosition(request, new PrecisionPoint(ptOnScreen.x, ptOnScreen.y));
				}
			}
		}

		return super.getFeedbackHelper(request);
	}

}
