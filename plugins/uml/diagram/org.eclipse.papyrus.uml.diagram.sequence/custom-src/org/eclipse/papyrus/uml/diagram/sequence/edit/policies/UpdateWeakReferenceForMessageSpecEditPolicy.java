/*****************************************************************************
 * Copyright s(c) 2017 CEA LIST and others.
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
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 526191, 526462, 526628, 526803
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
import org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.common.editparts.RoundedCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractMessageEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.util.CoordinateReferentialUtils;
import org.eclipse.papyrus.uml.diagram.sequence.util.LifelineEditPartUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.LogOptions;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.OccurrenceSpecification;

/**
 * this editpolicy is to manage the movement of Execution specification and update move of messages
 * It is like a graphical node
 *
 * @since 4.0
 *
 */
public class UpdateWeakReferenceForMessageSpecEditPolicy extends UpdateWeakReferenceEditPolicy {
	public static final String UDPATE_WEAK_REFERENCE_FOR_MESSAGE = "UpdateWeakReferenceForMessageSpecEditPolicy"; //$NON-NLS-1$

	/**
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#getCommand(org.eclipse.gef.Request)
	 *
	 * @param request
	 * @return
	 *
	 * 		<img src="../../../../../../../../../icons/sequenceScheme.png" width="250" />
	 *         <UL>
	 *         <LI>when move E --> move B on the coordinate Y of E and move A on the coordinate Y of E
	 *         <LI>when Move F (execution specification) (resize)--> move C on the coordinate of C of F and move D on the coordinate of Y of F
	 *         <LI>Move E and F (execution specification) move the execution--> move B on the coordinate of Y of E and move A on the coordinate of Y of E and move C on the coordinate of C of F and move D on the coordinate of Y of F
	 *         <UL>
	 *
	 */
	@Override
	public Command getCommand(Request request) {
		Command command = null;
		if (!(SenderRequestUtils.isASender(request, getHost()))) {
			if (request instanceof ReconnectRequest) {
				command = getUpdateWeakRefForMessageReconnect((ReconnectRequest) request);
			} else if (request instanceof CreateConnectionViewAndElementRequest) {
				command = getUpdateWeakRefForMessageCreate((CreateConnectionViewAndElementRequest) request);
			}
		}
		return null == command ? super.getCommand(request) : command;
	}

	/**
	 * Get the command to update weak references of the message for a creation.
	 *
	 * @param request
	 *            the create connection view and element request
	 * @return the command
	 */
	@SuppressWarnings("unchecked")
	private Command getUpdateWeakRefForMessageCreate(final CreateConnectionViewAndElementRequest request) {
		Command command = null;
		CreateConnectionViewAndElementRequest createRequest = request;
		UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "Message creation request at:" + ((IAdaptable) getHost()).getAdapter(View.class).getElement());
		// Snap to grid Location
		createRequest.setLocation(SequenceUtil.getSnappedLocation(getHost(), createRequest.getLocation()));

		// Get the location on screen
		Point reqlocationOnScreen = createRequest.getLocation().getCopy();
		getHostFigure().getParent().translateToRelative(reqlocationOnScreen);

		UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "=> Request Location on screen: " + reqlocationOnScreen);
		List<OccurrenceSpecification> nextEventsFromPosition = new ArrayList<>();
		List<OccurrenceSpecification> previousEventsFromPosition = new ArrayList<>();

		// Get next and previous event from the lifeline source
		EditPart sourceEditPart = createRequest.getSourceEditPart();
		if (sourceEditPart instanceof LifelineEditPart) {
			nextEventsFromPosition.addAll(LifelineEditPartUtil.getNextEventsFromPosition(reqlocationOnScreen, (LifelineEditPart) sourceEditPart));
			previousEventsFromPosition.addAll(LifelineEditPartUtil.getPreviousEventsFromPosition(new Point(reqlocationOnScreen.x, reqlocationOnScreen.y + deltaMoveAtCreationAndDeletion),
					(LifelineEditPart) sourceEditPart));
		}

		// Get next and previous event from the lifeline target
		EditPart targetEditPart = createRequest.getTargetEditPart();
		if (targetEditPart instanceof LifelineEditPart) {
			nextEventsFromPosition.addAll(LifelineEditPartUtil.getNextEventsFromPosition(reqlocationOnScreen, (LifelineEditPart) targetEditPart));
			previousEventsFromPosition.addAll(LifelineEditPartUtil.getPreviousEventsFromPosition(new Point(reqlocationOnScreen.x, reqlocationOnScreen.y + deltaMoveAtCreationAndDeletion),
					(LifelineEditPart) targetEditPart));
		}

		if (!nextEventsFromPosition.isEmpty()) {
			CompoundCommand compoundCommand = new CompoundCommand();
			// get the list of element just below new created message
			nextEventsFromPosition.retainAll(previousEventsFromPosition);
			// For each next event below
			for (OccurrenceSpecification nextEvent : nextEventsFromPosition) {
				UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "\tNext Event: " + nextEvent);

				GraphicalEditPart sourceLifeLineEP = (GraphicalEditPart) createRequest.getSourceEditPart();
				GraphicalEditPart targetLifeLineEP = (GraphicalEditPart) createRequest.getTargetEditPart();
				ArrayList<EditPart> senderList = SenderRequestUtils.getSenders(request);

				List<?> connectionsAndChildren = new ArrayList<>();
				if (null != sourceLifeLineEP) {
					connectionsAndChildren.addAll(sourceLifeLineEP.getSourceConnections());
					connectionsAndChildren.addAll(sourceLifeLineEP.getTargetConnections());
					connectionsAndChildren.addAll(sourceLifeLineEP.getChildren());
				}
				if (null != targetLifeLineEP) {
					connectionsAndChildren.addAll(targetLifeLineEP.getSourceConnections());
					connectionsAndChildren.addAll(targetLifeLineEP.getTargetConnections());
					connectionsAndChildren.addAll(targetLifeLineEP.getChildren());
				}

				for (Object editPart : connectionsAndChildren) {
					if (editPart instanceof ConnectionEditPart) {
						EObject element = ((View) ((AbstractMessageEditPart) editPart).getAdapter(View.class)).getElement();
						if (element instanceof Message && null != ((Message) element).getSendEvent() && ((Message) element).getSendEvent().equals(nextEvent)
								|| element instanceof Message && null != ((Message) element).getReceiveEvent() && ((Message) element).getReceiveEvent().equals(nextEvent)) {

							// compute Delta
							Point moveDelta = new Point(0, 0);
							PolylineConnectionEx polyline = (PolylineConnectionEx) ((ConnectionEditPart) editPart).getFigure();
							Point anchorPositionOnScreen;
							if (((Message) element).getSendEvent().equals(nextEvent)) {
								anchorPositionOnScreen = polyline.getTargetAnchor().getReferencePoint();
							} else {
								anchorPositionOnScreen = polyline.getSourceAnchor().getReferencePoint();
							}
							UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "\t\tanchorPositionOnScreen:\t" + anchorPositionOnScreen);

							Point newLocation = new Point(0, createRequest.getLocation().y + deltaMoveAtCreationAndDeletion);
							newLocation = SequenceUtil.getSnappedLocation(getHost(), newLocation);
							UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "\t\tNew location to be set:\t" + newLocation);
							moveDelta.y = newLocation.y - anchorPositionOnScreen.y;

							// add move source and target request
							UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "\t\tMoveDela:\t\t" + moveDelta.y);
							moveTargetConnectionEditPart(null, moveDelta, compoundCommand, (ConnectionEditPart) editPart, senderList);
							moveSourceConnectionEditPart(null, moveDelta, compoundCommand, (ConnectionEditPart) editPart, senderList);
						}
					} else if (editPart instanceof AbstractExecutionSpecificationEditPart) {
						EObject element = ((View) ((AbstractExecutionSpecificationEditPart) editPart).getAdapter(View.class)).getElement();

						if (element instanceof ExecutionSpecification && null != ((ExecutionSpecification) element).getStart() && ((ExecutionSpecification) element).getStart().equals(nextEvent)) {
							// compute Delta
							Point moveDelta = new Point(0, 0);
							Point figureLocation = ((AbstractExecutionSpecificationEditPart) editPart).getFigure().getBounds().getLocation();
							UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "\t\tlocationOfFigure:\t" + figureLocation);

							Point newLocation = new Point(0, reqlocationOnScreen.y + deltaMoveAtCreationAndDeletion);
							UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "\t\tNew location to be set:\t" + newLocation);
							moveDelta.y = newLocation.y - figureLocation.y;

							UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "\t\tMoveDela:\t\t" + moveDelta.y);
							moveRoundedEditPart(null, moveDelta, compoundCommand, (EditPart) editPart, senderList);
						}
					}
				}
			}
			if (!compoundCommand.isEmpty()) {
				command = compoundCommand;
			}
		}
		return command;
	}

	/**
	 * Get the command to update weak references of the message for a reconnect.
	 *
	 * @param request
	 *            the reconnect request
	 * @return the command
	 */
	private Command getUpdateWeakRefForMessageReconnect(final ReconnectRequest request) {
		CompoundCommand command = null;
		ReconnectRequest reconnectRequest = request;
		ConnectionEditPart hostConnectionEditPart = reconnectRequest.getConnectionEditPart();
		UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+ MOVE ANCHORS of " + hostConnectionEditPart.getClass().getName());//$NON-NLS-1$
		Point locationOnDiagram = CoordinateReferentialUtils.transformPointFromScreenToDiagramReferential(reconnectRequest.getLocation(), (GraphicalViewer) getHost().getViewer());
		UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+-- LocationOnDiagram " + locationOnDiagram);// $NON-NLS-2$ //$NON-NLS-1$

		// compute Delta
		Point moveDelta = new Point(0, 0);

		PolylineConnectionEx polyline = (PolylineConnectionEx) hostConnectionEditPart.getFigure();

		if (RequestConstants.REQ_RECONNECT_TARGET.equals(reconnectRequest.getType())) {
			Point anchorPositionOnScreen = polyline.getTargetAnchor().getReferencePoint();
			moveDelta.y = reconnectRequest.getLocation().y - anchorPositionOnScreen.y;
		} else {
			Point anchorPositionOnScreen = polyline.getSourceAnchor().getReferencePoint();
			moveDelta.y = reconnectRequest.getLocation().y - anchorPositionOnScreen.y;
		}

		if (moveDelta.y != 0 && mustMove) {
			if (hostConnectionEditPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE) != null) {
				SequenceReferenceEditPolicy references = (SequenceReferenceEditPolicy) hostConnectionEditPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE);
				if (!SenderRequestUtils.isASender(request, getHost())) {
					CompoundCommand compoundCommand = new CompoundCommand();

					// Gets weak references
					HashMap<EditPart, String> weakReferences = new HashMap<>();
					if (moveDelta.y > 0 && mustMoveBelowAtMovingDown) {
						weakReferences.putAll(references.getWeakReferences());
					}

					for (Iterator<EditPart> iterator = weakReferences.keySet().iterator(); iterator.hasNext();) {
						EditPart editPart = iterator.next();
						if (!SenderRequestUtils.isASender(request, editPart)) {
							UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+--> try to Move " + editPart);//$NON-NLS-1$
							ArrayList<EditPart> senderList = SenderRequestUtils.getSenders(request);
							if (editPart instanceof ConnectionEditPart) {
								ConnectionEditPart connectionEditPart = (ConnectionEditPart) editPart;
								// create the request
								if (moveDelta.y > 0) {
									// move down, target must be moved before
									moveTargetConnectionEditPart(hostConnectionEditPart, moveDelta, compoundCommand, connectionEditPart, senderList);
									moveSourceConnectionEditPart(hostConnectionEditPart, moveDelta, compoundCommand, connectionEditPart, senderList);
								} else {
									// move up, source must be moved before
									moveSourceConnectionEditPart(hostConnectionEditPart, moveDelta, compoundCommand, connectionEditPart, senderList);
									moveTargetConnectionEditPart(hostConnectionEditPart, moveDelta, compoundCommand, connectionEditPart, senderList);
								}
							}
							if (editPart instanceof RoundedCompartmentEditPart) {
								moveRoundedEditPart(hostConnectionEditPart, moveDelta, compoundCommand, editPart, senderList);
							}
						}
						if (!compoundCommand.isEmpty()) {
							command = compoundCommand;
						}
					}
				}
			}
		}
		return command;
	}

}
