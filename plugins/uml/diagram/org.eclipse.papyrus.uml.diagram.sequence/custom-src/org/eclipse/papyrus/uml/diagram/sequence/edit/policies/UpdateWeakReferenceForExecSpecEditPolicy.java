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
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 526191, 526803
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.EditCommandRequestWrapper;
import org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.common.editparts.RoundedCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractMessageEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.GridManagementEditPolicy;
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
public class UpdateWeakReferenceForExecSpecEditPolicy extends UpdateWeakReferenceEditPolicy {
	public static final String UDPATE_WEAK_REFERENCE_FOR_EXECSPEC = "UpdateWeakReferenceForExecSpecEditPolicy"; //$NON-NLS-1$

	/**
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#getCommand(org.eclipse.gef.Request)
	 *
	 * @param request
	 * @return
	 *
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
			if (request instanceof ChangeBoundsRequest
					&& !org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants.REQ_AUTOSIZE.equals(request.getType())
					&& getHost() instanceof AbstractExecutionSpecificationEditPart) {
				// For change bounds request
				command = getUpdateWeakRefForExecSpecResize((ChangeBoundsRequest) request);
			}

			/*
			 * Bug 532071 - [SequenceDiagram] Creation and Deletion of an element on a Lifeline should not move necessarily the other elements
			 * This code is comment due to instability (Oxygen version is not work very well).
			 */
//			else if (request instanceof CreateViewAndElementRequest) {
//				// for creation request
//				command = getUpdateWeakRefForExecSpecCreate((CreateViewAndElementRequest) request);
//			} else if (request instanceof EditCommandRequestWrapper
//					&& (getHost() instanceof AbstractExecutionSpecificationEditPart)) {
//
//				// Check that this is a delete command, in this case, we have to recalculate the other execution specification positions
//				final IEditCommandRequest editCommandRequest = ((EditCommandRequestWrapper) request).getEditCommandRequest();
//				if (editCommandRequest instanceof DestroyElementRequest
//						&& ((DestroyElementRequest) editCommandRequest).getElementToDestroy() instanceof ExecutionSpecification) {
//					return getUpdateWeakRefForExecSpecDelete((EditCommandRequestWrapper) request);
//				}
//			}
		}
		return null == command ? super.getCommand(request) : command;
	}

	/**
	 * Get the command to update weak references at execution specification creation.
	 *
	 * @param request
	 *            the {@link CreateViewAndElementRequest} request
	 * @return the command to update weak references
	 */
	private Command getUpdateWeakRefForExecSpecCreate(final CreateViewAndElementRequest request) {
		Command command = null;
		CreateViewAndElementRequest createRequest = request;
		UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "Execution Specification creation request at:" + ((IAdaptable) getHost()).getAdapter(View.class).getElement());
		// Snap to grid Location
		createRequest.setLocation(SequenceUtil.getSnappedLocation(getHost(), createRequest.getLocation()));

		// Get the location on screen
		Point reqlocationOnScreen = createRequest.getLocation().getCopy();
		getHostFigure().translateToRelative(reqlocationOnScreen);

		UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "=> Request Location on screen: " + reqlocationOnScreen);
		List<OccurrenceSpecification> nextEventsFromPosition = new ArrayList<>();
		List<OccurrenceSpecification> previousEventsFromPosition = new ArrayList<>();

		// Get next and previous event from the lifeline source
		EditPart host = getHost();
		if (host instanceof LifelineEditPart) {
			nextEventsFromPosition.addAll(LifelineEditPartUtil.getNextEventsFromPosition(reqlocationOnScreen.getCopy().translate(0, GridManagementEditPolicy.threshold), (LifelineEditPart) host));
			previousEventsFromPosition.addAll(LifelineEditPartUtil.getPreviousEventsFromPosition(new Point(reqlocationOnScreen.x, reqlocationOnScreen.y + deltaMoveAtCreationAndDeletion + AbstractExecutionSpecificationEditPart.DEFAUT_HEIGHT),
					(LifelineEditPart) host));
		}

		// get the list of element just below new created message
		nextEventsFromPosition.retainAll(previousEventsFromPosition);

		if (!nextEventsFromPosition.isEmpty()) {
			CompoundCommand compoundCommand = new CompoundCommand();
			// only first element need to be moved, other will follow
			OccurrenceSpecification nextEvent = nextEventsFromPosition.get(0);
			UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "\tNext Event: " + nextEvent);

			ArrayList<EditPart> senderList = SenderRequestUtils.getSenders(request);

			List<?> connectionsAndChildren = new ArrayList<>();
			connectionsAndChildren.addAll(((GraphicalEditPart) host).getSourceConnections());
			connectionsAndChildren.addAll(((GraphicalEditPart) host).getTargetConnections());
			connectionsAndChildren.addAll(((GraphicalEditPart) host).getChildren());

			for (Object editPart : connectionsAndChildren) {
				// move messages
				if (editPart instanceof ConnectionEditPart) {
					EObject message = ((View) ((AbstractMessageEditPart) editPart).getAdapter(View.class)).getElement();
					if (message instanceof Message && null != ((Message) message).getSendEvent() && ((Message) message).getSendEvent().equals(nextEvent)
							|| message instanceof Message && null != ((Message) message).getReceiveEvent() && ((Message) message).getReceiveEvent().equals(nextEvent)) {

						// compute Delta
						Point moveDelta = new Point(0, 0);
						PolylineConnectionEx polyline = (PolylineConnectionEx) ((ConnectionEditPart) editPart).getFigure();
						Point anchorPositionOnScreen;
						if (((Message) message).getSendEvent().equals(nextEvent)) {
							anchorPositionOnScreen = polyline.getTargetAnchor().getReferencePoint();
						} else {
							anchorPositionOnScreen = polyline.getSourceAnchor().getReferencePoint();
						}
						UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "\t\tanchorPositionOnScreen:\t" + anchorPositionOnScreen);

						Point newLocation = new Point(0, createRequest.getLocation().y + AbstractExecutionSpecificationEditPart.DEFAUT_HEIGHT + deltaMoveAtCreationAndDeletion);
						newLocation = SequenceUtil.getSnappedLocation(getHost(), newLocation);
						UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "\t\tNew location to be set:\t" + newLocation);
						moveDelta.y = newLocation.y - anchorPositionOnScreen.y;

						// add move source and target request
						UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "\t\tMoveDela:\t\t" + moveDelta.y);
						moveTargetConnectionEditPart(null, moveDelta, compoundCommand, (ConnectionEditPart) editPart, senderList);
						moveSourceConnectionEditPart(null, moveDelta, compoundCommand, (ConnectionEditPart) editPart, senderList);
					}
				} else
				// move execution specification
				if (editPart instanceof AbstractExecutionSpecificationEditPart) {
					EObject element = ((View) ((AbstractExecutionSpecificationEditPart) editPart).getAdapter(View.class)).getElement();

					if (element instanceof ExecutionSpecification && null != ((ExecutionSpecification) element).getStart() && ((ExecutionSpecification) element).getStart().equals(nextEvent)) {
						// compute Delta
						Point moveDelta = new Point(0, 0);
						Point figureLocation = ((AbstractExecutionSpecificationEditPart) editPart).getFigure().getBounds().getLocation();
						UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "\t\tlocationOfFigure:\t" + figureLocation);

						Point newLocation = new Point(0, reqlocationOnScreen.y + AbstractExecutionSpecificationEditPart.DEFAUT_HEIGHT + deltaMoveAtCreationAndDeletion);
						UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "\t\tNew location to be set:\t" + newLocation);
						moveDelta.y = newLocation.y - figureLocation.y;

						UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "\t\tMoveDela:\t\t" + moveDelta.y);
						moveRoundedEditPart(null, moveDelta, compoundCommand, (EditPart) editPart, senderList);
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
	 * Get the command to update weak references at execution specification resize.
	 *
	 * @param request
	 *            the {@link ChangeBoundsRequest} request
	 * @return the command to update weak references
	 */
	private Command getUpdateWeakRefForExecSpecResize(final ChangeBoundsRequest request) {
		CompoundCommand compoundCommand = new CompoundCommand();
		Point nextLocation = request.getLocation();
		UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+ MOVE at " + nextLocation + " of " + getHost());//$NON-NLS-1$ //$NON-NLS-2$
		Rectangle locationAndSize = new PrecisionRectangle(getHostFigure().getBounds());
		Point moveDelta = new Point(0, 0);
		if (RequestConstants.REQ_MOVE.equals(request.getType())) {
			moveDelta = request.getMoveDelta();
		} else if (RequestConstants.REQ_RESIZE.equals(request.getType())) {
			moveDelta = new Point(0, request.getSizeDelta().height + request.getMoveDelta().y);
		}
		if (moveDelta.y != 0 && mustMove) {
			if (getHost() instanceof AbstractExecutionSpecificationEditPart) {
				getHostFigure().translateToAbsolute(locationAndSize);
				locationAndSize = request.getTransformedRectangle(locationAndSize);
			}
			if (getHost().getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE) != null) {
				SequenceReferenceEditPolicy references = (SequenceReferenceEditPolicy) getHost().getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE);
				if (!SenderRequestUtils.isASender(request, getHost())) {

					// Gets weak references
					HashMap<EditPart, String> weakReferences = new HashMap<>();
					if (moveDelta.y > 0 && mustMoveBelowAtMovingDown) {
						weakReferences.putAll(references.getWeakReferences());
					}

					for (Iterator<EditPart> iterator = weakReferences.keySet().iterator(); iterator.hasNext();) {
						EditPart editPart = iterator.next();
						if (!SenderRequestUtils.isASender(request, editPart)) {
							UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+--> try to Move of " + moveDelta.y + " " + editPart);//$NON-NLS-1$
							ArrayList<EditPart> senderList = SenderRequestUtils.getSenders(request);

							if (editPart instanceof ConnectionEditPart) {
								ConnectionEditPart connectionEditPart = (ConnectionEditPart) editPart;
								// create the request
								UpdateWeakReferenceEditPolicy.moveSourceConnectionEditPart(getHost(), moveDelta, compoundCommand, connectionEditPart, senderList);
								UpdateWeakReferenceEditPolicy.moveTargetConnectionEditPart(getHost(), moveDelta, compoundCommand, connectionEditPart, senderList);

							}
							if (editPart instanceof RoundedCompartmentEditPart) {
								UpdateWeakReferenceEditPolicy.moveRoundedEditPart(getHost(), moveDelta, compoundCommand, editPart, senderList);
							}
						}
					}
				}
			}
		}
		return compoundCommand.isEmpty() ? null : compoundCommand;
	}

	/**
	 * Get the command to update weak references of the execution specification for a deletion.
	 *
	 * @param request
	 *            the delete command wrapped into a {@link EditCommandRequestWrapper}.
	 * @return the command
	 */
	@SuppressWarnings("unchecked")
	private Command getUpdateWeakRefForExecSpecDelete(final EditCommandRequestWrapper request) {
		CompoundCommand command = null;
		AbstractExecutionSpecificationEditPart hostConnectionEditPart = (AbstractExecutionSpecificationEditPart) getHost();

		// compute Delta
		Point moveDelta = new Point(0, -hostConnectionEditPart.getPrimaryShape().getBounds().height);

		if (moveDelta.y < 0) {
			// get the edit policy of references
			if (hostConnectionEditPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE) != null) {
				SequenceReferenceEditPolicy references = (SequenceReferenceEditPolicy) hostConnectionEditPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE);
				if (!SenderRequestUtils.isASender(request, getHost())) {
					CompoundCommand compoundCommand = new CompoundCommand();

					// Gets weak references
					HashMap<EditPart, String> weakReferences = new HashMap<>();
					weakReferences.putAll(references.getWeakReferences());

					// for each weak reference move it
					for (Iterator<EditPart> iterator = weakReferences.keySet().iterator(); iterator.hasNext();) {
						EditPart editPart = iterator.next();
						if (!SenderRequestUtils.isASender(request, editPart)) {// avoid loop
							UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+--> try to Move " + editPart);//$NON-NLS-1$
							ArrayList<EditPart> senderList = SenderRequestUtils.getSenders(request);
							if (editPart instanceof ConnectionEditPart) {
								ConnectionEditPart connectionEditPart = (ConnectionEditPart) editPart;
								// move up, source must be moved before
								UpdateWeakReferenceEditPolicy.moveSourceConnectionEditPart(hostConnectionEditPart, moveDelta, compoundCommand, connectionEditPart, senderList);
								UpdateWeakReferenceEditPolicy.moveTargetConnectionEditPart(hostConnectionEditPart, moveDelta, compoundCommand, connectionEditPart, senderList);
							}
							if (editPart instanceof RoundedCompartmentEditPart) {
								UpdateWeakReferenceEditPolicy.moveRoundedEditPart(hostConnectionEditPart, moveDelta, compoundCommand, editPart, senderList);
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
