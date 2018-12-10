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
 *   MickaÃ«l ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 519621, 519756, 526191
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Bug 531596
 *   EclipseSource - Bug 536641
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.FeedbackHelper;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.util.EditPartUtil;
import org.eclipse.gmf.runtime.draw2d.ui.figures.BaseSlidableAnchor;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.NodeEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultGraphicalNodeEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.PapyrusSlidableSnapToGridAnchor;
import org.eclipse.papyrus.infra.gmfdiag.common.service.palette.AspectUnspecifiedTypeConnectionTool.CreateAspectUnspecifiedTypeConnectionRequest;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramEditPartsUtil;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.EditPartUtils;
import org.eclipse.papyrus.infra.services.edit.utils.RequestParameterConstants;
import org.eclipse.papyrus.uml.diagram.sequence.draw2d.routers.MessageRouter;
import org.eclipse.papyrus.uml.diagram.sequence.edit.helpers.AnchorHelper;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageAsyncEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageSyncEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphRequestHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.InteractionGraphCommand;
import org.eclipse.papyrus.uml.diagram.sequence.util.LifelineEditPartUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceDiagramConstants;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceRequestConstant;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.papyrus.uml.service.types.element.UMLDIElementTypes;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.OccurrenceSpecification;

/**
 * This class overload all creation of link between lifelines
 * pay attention : this editpolicy launch a display of event during the move of the mouse
 */
public class LifeLineGraphicalNodeEditPolicy extends DefaultGraphicalNodeEditPolicy {

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
		return super.getConnectionCreateCommand(request);
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
		// TODO: Rely on the model to get the anchor point.
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
		request.setLocation(SequenceUtil.getSnappedLocation(getHost(), request.getLocation()));		
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
	 *
	 */

	@Override
	protected Command getConnectionAndRelationshipCompleteCommand(CreateConnectionViewAndElementRequest request) {
		InteractionGraph graph = InteractionGraphRequestHelper.getOrCreateInteractionGraph(request, (org.eclipse.gef.GraphicalEditPart) getHost());
		if (graph == null) {
			return null;
		}

		Point srcAnchor =  (Point)request.getExtendedData().get("EDGE_SOURCE_POINT");
		Point trgAnchor = request.getLocation();
		
		String hint = request.getConnectionViewDescriptor().getSemanticHint();
		int msgSort = -1;
		switch (hint) {
			case MessageAsyncEditPart.VISUAL_ID:
				msgSort = MessageSort.ASYNCH_CALL; break;
			case MessageSyncEditPart.VISUAL_ID:
				msgSort = MessageSort.SYNCH_CALL; break;
		}
		
		if (msgSort == -1)
			return null;
		
		InteractionGraphCommand cmd = new InteractionGraphCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), 
				"Create Asynchronous Message", graph, null);
		cmd.addMessage(msgSort, request.getConnectionViewAndElementDescriptor().getCreateElementRequestAdapter(), 
				request.getConnectionViewAndElementDescriptor(), 
				(Lifeline)ViewUtil.resolveSemanticElement((View)request.getSourceEditPart().getModel()), srcAnchor, 
				(Lifeline)ViewUtil.resolveSemanticElement((View)request.getTargetEditPart().getModel()), trgAnchor);
		
		return new ICommandProxy(cmd);
	}

	private boolean isMoveMessageRequest(ReconnectRequest req) {
		View v = (View)req.getConnectionEditPart().getModel();
		return v != null && v.getElement() instanceof Message && 
				(Boolean)req.getExtendedData().getOrDefault(SequenceRequestConstant.DO_NOT_MOVE_EDIT_PARTS, Boolean.FALSE);
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

		horizontal = (Math.abs(realDelta) <= SequenceDiagramConstants.HORIZONTAL_MESSAGE_PRECISION_Y_DELTA);

		return horizontal;
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

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultGraphicalNodeEditPolicy#getReconnectSourceCommand(org.eclipse.gef.requests.ReconnectRequest)
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected Command getReconnectSourceCommand(final ReconnectRequest request) {
		if (isMoveMessageRequest(request)) {
			return null;
		}

		// TODO Handle message end changing Lifeline
		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultGraphicalNodeEditPolicy#getReconnectTargetCommand(org.eclipse.gef.requests.ReconnectRequest)
	 */
	@Override
	protected Command getReconnectTargetCommand(final ReconnectRequest request) {
		if (isMoveMessageRequest(request)) {
			return null;
		}

		// TODO Handle message end changing Lifeline
		return null;
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
				// SimpleSnapHelper.snapAPoint(ptOnScreen, getHost().getRoot());
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
					// SimpleSnapHelper.snapAPoint(ptOnScreen, getHost().getRoot());
					computeTargetPosition(request, new PrecisionPoint(ptOnScreen.x, ptOnScreen.y));
				}
			}
		}

		return super.getFeedbackHelper(request);
	}

	public DiagramEditPart getDiagramEditPart(EditPart editPart) {
		while (editPart instanceof IGraphicalEditPart) {
			if (editPart instanceof DiagramEditPart) {
				return (DiagramEditPart) editPart;
			}

			editPart = editPart.getParent();
		}
		if (editPart instanceof DiagramRootEditPart) {
			return (DiagramEditPart) ((DiagramRootEditPart) editPart).getChildren().get(0);
		}
		return null;
	}
}
