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

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.Map;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.FeedbackHelper;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.util.SelectInDiagramHelper;
import org.eclipse.gmf.runtime.draw2d.ui.figures.BaseSlidableAnchor;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.NodeEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultGraphicalNodeEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.PapyrusSlidableSnapToGridAnchor;
import org.eclipse.papyrus.infra.gmfdiag.common.service.palette.AspectUnspecifiedTypeConnectionTool.CreateAspectUnspecifiedTypeConnectionRequest;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramEditPartsUtil;
import org.eclipse.papyrus.infra.services.edit.utils.RequestParameterConstants;
import org.eclipse.papyrus.uml.diagram.sequence.draw2d.routers.MessageRouter;
import org.eclipse.papyrus.uml.diagram.sequence.edit.helpers.AnchorHelper;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageAsyncEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageDeleteEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageSyncEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphRequestHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.InteractionGraphCommand;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.KeyboardHandler;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceDiagramConstants;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceRequestConstant;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.papyrus.uml.service.types.element.UMLDIElementTypes;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;

/**
 * This class overload all creation of link between lifelines
 * pay attention : this editpolicy launch a display of event during the move of the mouse
 */
public class InteractionGraphGraphicalNodeEditPolicy extends DefaultGraphicalNodeEditPolicy {
	private static final String CLICK_LOCATION_KEY = "clickLocation";

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

	@Override
	protected Command getConnectionAndRelationshipCreateCommand(
			CreateConnectionViewAndElementRequest request) {
		Command cmd = super.getConnectionAndRelationshipCreateCommand(request);
		if (cmd == null) {
			// No semantic policy, so we delegate to the 
			return super.getConnectionCreateCommand(request);
		}
		
		return cmd;
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
	
	@Override
	protected Command getConnectionAndRelationshipCompleteCommand(CreateConnectionViewAndElementRequest request) {
		InteractionGraph graph = InteractionGraphRequestHelper.getOrCreateInteractionGraph(request, (org.eclipse.gef.GraphicalEditPart) getHost());
		if (graph == null) {
			return null;
		}

		Point srcAnchor =  (Point)request.getExtendedData().get("EDGE_SOURCE_POINT");
		srcAnchor = ViewUtilities.controlToViewer(graph.getEditPartViewer(), srcAnchor.getCopy());
		ConnectionAnchor anchor = ((NodeEditPart) request.getTargetEditPart()).getTargetConnectionAnchor(request);
		if (anchor == null)
			return null;
		
		Point trgAnchor = request.getLocation();
		trgAnchor = ViewUtilities.controlToViewer(graph.getEditPartViewer(), trgAnchor.getCopy());
		
		String hint = request.getConnectionViewDescriptor().getSemanticHint();
		MessageSort msgSort = null;
		switch (hint) {
			case MessageAsyncEditPart.VISUAL_ID:
				msgSort = MessageSort.ASYNCH_CALL_LITERAL; break;
			case MessageSyncEditPart.VISUAL_ID:
				msgSort = MessageSort.SYNCH_CALL_LITERAL; break;
			case MessageCreateEditPart.VISUAL_ID:
				msgSort = MessageSort.CREATE_MESSAGE_LITERAL; break;
			case MessageDeleteEditPart.VISUAL_ID:
				msgSort = MessageSort.DELETE_MESSAGE_LITERAL; break;
		}
		
		if (msgSort == null)
			return null;
		
		InteractionGraphCommand cmd = new InteractionGraphCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), 
				"Create Asynchronous Message", graph, null);
		cmd.addMessage(msgSort, request.getConnectionViewAndElementDescriptor().getCreateElementRequestAdapter(), 
				request.getConnectionViewAndElementDescriptor(), 
				(Element)ViewUtil.resolveSemanticElement((View)request.getSourceEditPart().getModel()),
				srcAnchor,
				//ViewUtilities.controlToViewer(graph.getEditPartViewer(),srcAnchor.getCopy()), 
				(Element)ViewUtil.resolveSemanticElement((View)request.getTargetEditPart().getModel()),
				trgAnchor);
				//ViewUtilities.controlToViewer(graph.getEditPartViewer(),trgAnchor.getCopy()));
		
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
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultGraphicalNodeEditPolicy#getReconnectSourceCommand(org.eclipse.gef.requests.ReconnectRequest)
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected Command getReconnectSourceCommand(final ReconnectRequest request) {
		return getReconnectCommand(request, true);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultGraphicalNodeEditPolicy#getReconnectTargetCommand(org.eclipse.gef.requests.ReconnectRequest)
	 */
	@Override
	protected Command getReconnectTargetCommand(final ReconnectRequest request) {
		return getReconnectCommand(request, false);
	}

	// TODO: @etxacam Handle gates
	protected Command getReconnectCommand(final ReconnectRequest request, boolean isSrc) {
		if (isMoveMessageRequest(request)) {
			return null;
		}

		Point loc = request.getLocation();		
		if ((getHost().getViewer() instanceof ScrollingGraphicalViewer) &&
				(getHost().getViewer().getControl() instanceof FigureCanvas)){
				SelectInDiagramHelper.exposeLocation((FigureCanvas)getHost().getViewer().getControl(),loc);
			}
		
		Connection connection = (Connection)request.getConnectionEditPart().getFigure();
		Edge connectionView = (Edge) request.getConnectionEditPart().getModel();
		if (!(connectionView.getElement() instanceof Message))
			return null;
		Message message = (Message)connectionView.getElement(); 
		InteractionGraph graph = InteractionGraphRequestHelper.getOrCreateInteractionGraph(request, (org.eclipse.gef.GraphicalEditPart) getHost());
		if (graph == null)
			return null;
		
		MessageEnd messageEnd = isSrc ? message.getSendEvent() : message.getReceiveEvent();
		Element element = (Element)((View)getHost().getModel()).getElement();
		InteractionGraphCommand cmd = new InteractionGraphCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), 
				"Move Message", graph, null);
		Point p = SequenceUtil.getSnappedLocation(request.getTarget(),loc.getCopy());
		p = ViewUtilities.controlToViewer(graph.getEditPartViewer(), p);				

		if (element instanceof Lifeline) {
			Lifeline newLifeline = (Lifeline)element;
			if (KeyboardHandler.getKeyboardHandler().isAnyPressed() ) {
				cmd.moveMessageEnd(messageEnd, newLifeline, p);		
			} else {
				if (!(messageEnd instanceof MessageOccurrenceSpecification))
					return UnexecutableCommand.INSTANCE;
				MessageOccurrenceSpecification mos = (MessageOccurrenceSpecification)messageEnd;
				if (mos.getCovered() != newLifeline) {
					return UnexecutableCommand.INSTANCE;
				}
				cmd.nudgeMessageEnd(messageEnd, p); 
			}
		} else {
			InteractionFragment intFragment = (InteractionFragment)element;
			if (!(messageEnd instanceof Gate))
				return UnexecutableCommand.INSTANCE;
			Gate gate = (Gate)messageEnd;			
			if (KeyboardHandler.getKeyboardHandler().isAnyPressed() ) {
				cmd.moveGate(gate, intFragment, p);		
			} else {
				if (gate.getOwner() != intFragment)
					return UnexecutableCommand.INSTANCE;
					
				cmd.nudgeGate(gate, p); 
			}
			
		}

		return new ICommandProxy(cmd);
	
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
