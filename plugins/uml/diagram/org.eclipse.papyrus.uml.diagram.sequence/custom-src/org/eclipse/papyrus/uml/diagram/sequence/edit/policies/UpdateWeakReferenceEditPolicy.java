/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
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
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 526191, 526628
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;



import java.util.ArrayList;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CLifeLineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.keyboardlistener.IKeyPressState;
import org.eclipse.papyrus.uml.diagram.sequence.keyboardlistener.KeyboardListener;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.preferences.CustomDiagramGeneralPreferencePage;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;

/**
 * the goal of this editpat is to propose a set of method and action by SHIFT key button to move messages and execution spec under the current editpart
 *
 * @since 4.0
 *
 */
public abstract class UpdateWeakReferenceEditPolicy extends GraphicalEditPolicy implements IKeyPressState {

	protected KeyboardListener SHIFTDown = new KeyboardListener(this, SWT.SHIFT, true);
	protected KeyboardListener SHIFTUp = new KeyboardListener(this, SWT.SHIFT, false);
	protected boolean mustMove = true;

	/**
	 * The instance of listener of move message preference property change.
	 */
	private final MoveMessagePropertyChangeListener moveMessageListener = new MoveMessagePropertyChangeListener();

	/**
	 * The must move preference boolean. Set to true if messages below the current message must move down at the same time.
	 */
	protected boolean mustMoveBelowAtMovingDown;

	/**
	 * Minimum space that must be below a message at creation.
	 */
	protected static int deltaMoveAtCreationAndDeletion = CustomDiagramGeneralPreferencePage.PREF_MOVE_BELOW_ELEMENTS_AT_MESSAGE_CREATION_VALUE;

	/**
	 * Constructor.
	 *
	 */
	public UpdateWeakReferenceEditPolicy() {
		super();
	}

	@Override
	public void activate() {
		super.activate();
		// activate listeners
		PlatformUI.getWorkbench().getDisplay().addFilter(SWT.KeyDown, SHIFTDown);
		PlatformUI.getWorkbench().getDisplay().addFilter(SWT.KeyUp, SHIFTUp);
		mustMoveBelowAtMovingDown = UMLDiagramEditorPlugin.getInstance().getPreferenceStore().getBoolean(CustomDiagramGeneralPreferencePage.PREF_MOVE_BELOW_ELEMENTS_AT_MESSAGE_DOWN);
		deltaMoveAtCreationAndDeletion = UMLDiagramEditorPlugin.getInstance().getPreferenceStore().getInt(CustomDiagramGeneralPreferencePage.PREF_MOVE_BELOW_ELEMENTS_AT_MESSAGE_CREATION);
		UMLDiagramEditorPlugin.getInstance().getPreferenceStore().addPropertyChangeListener(moveMessageListener);
	}

	@Override
	public void deactivate() {
		PlatformUI.getWorkbench().getDisplay().removeFilter(SWT.KeyDown, SHIFTDown);
		PlatformUI.getWorkbench().getDisplay().removeFilter(SWT.KeyUp, SHIFTUp);
		UMLDiagramEditorPlugin.getInstance().getPreferenceStore().removePropertyChangeListener(moveMessageListener);
		super.deactivate();
	}


	/**
	 * Add a command of reconnection of the given connection editpart at the location.
	 *
	 * @param hostEditpart
	 *            the current editpart that is the origin of this impact
	 * @param connectionEditPart
	 *            the given editpart to move
	 * @param location
	 *            the next location of the anchor
	 * @param senderList
	 *            the list of editpart that are origin of this request
	 * @param reconnectType
	 *            the type of the reconnection see {@link RequestConstants}
	 * @return return a request of reconnection
	 */
	protected static ReconnectRequest createReconnectRequest(EditPart hostEditpart, ConnectionEditPart connectionEditPart, Rectangle location, ArrayList<EditPart> senderList, String reconnectType) {
		ReconnectRequest reconnectRequest = new ReconnectRequest();
		reconnectRequest.setConnectionEditPart(connectionEditPart);
		SenderRequestUtils.addRequestSenders(reconnectRequest, senderList);
		if (null != hostEditpart) {
			SenderRequestUtils.addRequestSender(reconnectRequest, hostEditpart);
		}
		reconnectRequest.setLocation(location.getLocation().getCopy());
		reconnectRequest.setType(reconnectType);
		if (RequestConstants.REQ_RECONNECT_TARGET.equals(reconnectType)) {
			reconnectRequest.setTargetEditPart(connectionEditPart.getTarget());
		} else {
			reconnectRequest.setTargetEditPart(connectionEditPart.getSource());
		}
		return reconnectRequest;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.uml.diagram.sequence.keyboardlistener.IKeyPressState#setKeyPressState(java.lang.Boolean)
	 */
	@Override
	public void setKeyPressState(Boolean isPressed) {
		mustMove = !isPressed;
	}

	/**
	 * Move the editPart to move at this location withe the move delta
	 *
	 * @param moveDelta
	 *            the move delta (very important)
	 * @param compoundCommand
	 *            the command that will contain the result of the request
	 * @param editPartToMove
	 *            the editpart to move
	 */
	protected static void moveRoundedEditPart(EditPart hostEditPart, Point moveDelta, CompoundCommand compoundCommand, EditPart editPartToMove, ArrayList<EditPart> senderList) {
		ChangeBoundsRequest changeBoundsRequest = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
		SenderRequestUtils.addRequestSenders(changeBoundsRequest, senderList);
		if (null != hostEditPart) {
			SenderRequestUtils.addRequestSender(changeBoundsRequest, hostEditPart);
		}
		GraphicalEditPart gEditPart = (GraphicalEditPart) editPartToMove;
		Point newLocation = new Point(gEditPart.getFigure().getBounds().getTopLeft().x, gEditPart.getFigure().getBounds().getTopLeft().y + moveDelta.y());

		if (editPartToMove.getParent() instanceof CLifeLineEditPart) {
			// Translate to relative
			int stickerHeight = ((CLifeLineEditPart) editPartToMove.getParent()).getStickerHeight();
			if (newLocation.y >= stickerHeight) {
				changeBoundsRequest.setLocation(newLocation);
				changeBoundsRequest.setEditParts(editPartToMove);
				changeBoundsRequest.setMoveDelta(moveDelta);
				changeBoundsRequest.setSizeDelta(new Dimension(0, 0));
				compoundCommand.add(editPartToMove.getCommand(changeBoundsRequest));
			} else {
				compoundCommand.add(UnexecutableCommand.INSTANCE);
			}
		}
	}

	/**
	 * move the target anchor of the connection editpartPart
	 *
	 * @param hostEditPart
	 *            the editpart that impacts the connection editpart
	 * @param moveDelta
	 *            the delta to move the anchors
	 * @param compoundCommand
	 *            the compound command that can have the result of the request
	 * @param connectionEditPart
	 *            the editpart to move
	 * @param senderList
	 *            the list of editpart that are origin of this move
	 */
	protected static void moveTargetConnectionEditPart(EditPart hostEditPart, Point moveDelta, CompoundCommand compoundCommand, ConnectionEditPart connectionEditPart, ArrayList<EditPart> senderList) {
		PolylineConnectionEx polyline = (PolylineConnectionEx) connectionEditPart.getFigure();
		Rectangle newAnchorPositionOnScreen;
		Point positiononScreen = polyline.getTargetAnchor().getReferencePoint();
		newAnchorPositionOnScreen = new Rectangle(positiononScreen.x, positiononScreen.y + moveDelta.y, 0, 0);
		ReconnectRequest reconnectTargetRequest = createReconnectRequest(hostEditPart, connectionEditPart, newAnchorPositionOnScreen, senderList, RequestConstants.REQ_RECONNECT_TARGET);
		reconnectTargetRequest.getExtendedData().put(SequenceUtil.DO_NOT_CHECK_HORIZONTALITY, true);
		compoundCommand.add(connectionEditPart.getTarget().getCommand(reconnectTargetRequest));
	}

	protected static void moveSourceConnectionEditPart(EditPart hostEditPart, Point moveDelta, CompoundCommand compoundCommand, ConnectionEditPart connectionEditPart, ArrayList<EditPart> senderList) {
		PolylineConnectionEx polyline = (PolylineConnectionEx) connectionEditPart.getFigure();
		Point anchorPositionOnScreen = polyline.getSourceAnchor().getReferencePoint();
		Rectangle newAnchorPositionOnScreen = new Rectangle(anchorPositionOnScreen.x, anchorPositionOnScreen.y + moveDelta.y, 0, 0);
		ReconnectRequest reconnectSourceRequest = createReconnectRequest(hostEditPart, connectionEditPart, newAnchorPositionOnScreen, senderList, RequestConstants.REQ_RECONNECT_SOURCE);
		reconnectSourceRequest.getExtendedData().put(SequenceUtil.DO_NOT_CHECK_HORIZONTALITY, true);
		compoundCommand.add(connectionEditPart.getSource().getCommand(reconnectSourceRequest));
	}

	/**
	 * Listener of move message preference property change.
	 */
	private final class MoveMessagePropertyChangeListener implements IPropertyChangeListener {
		/**
		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			String property = event.getProperty();
			switch (property) {
			case CustomDiagramGeneralPreferencePage.PREF_MOVE_BELOW_ELEMENTS_AT_MESSAGE_DOWN:
				if (mustMoveBelowAtMovingDown != (boolean) event.getNewValue()) {
					mustMoveBelowAtMovingDown = (boolean) event.getNewValue();
				}
				break;
			case CustomDiagramGeneralPreferencePage.PREF_MOVE_BELOW_ELEMENTS_AT_MESSAGE_CREATION:
				if (deltaMoveAtCreationAndDeletion != (int) event.getNewValue()) {
					deltaMoveAtCreationAndDeletion = (int) event.getNewValue();
				}
				break;
			}
		}
	}

}