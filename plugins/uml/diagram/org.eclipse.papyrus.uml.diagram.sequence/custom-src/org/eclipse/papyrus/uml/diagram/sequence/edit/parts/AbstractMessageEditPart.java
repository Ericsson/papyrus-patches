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
 * CEA LIST - Initial API and implementation
 * Celine Janssens (ALL4TEC) - Bug 507348
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import java.util.List;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.diagram.ui.editparts.LabelEditPart;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramColorRegistry;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeConnectionRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeRequest;
import org.eclipse.gmf.runtime.draw2d.ui.geometry.LineSeg;
import org.eclipse.gmf.runtime.draw2d.ui.geometry.PointListUtilities;
import org.eclipse.gmf.runtime.notation.FontStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.IMaskManagedLabelEditPolicy;
import org.eclipse.papyrus.uml.diagram.common.editparts.UMLConnectionNodeEditPart;
import org.eclipse.papyrus.uml.diagram.common.figure.edge.UMLEdgeFigure;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.MessageLabelEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.SequenceReferenceEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.figures.MessageDelete;
import org.eclipse.papyrus.uml.diagram.sequence.figures.MessageFigure;
import org.eclipse.papyrus.uml.diagram.sequence.keyboardlistener.IKeyPressState;
import org.eclipse.papyrus.uml.diagram.sequence.keyboardlistener.KeyboardListener;
import org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectMessageToGridEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectRectangleToGridEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.LifeLineGraphicalNodeEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.util.SelectMessagesEditPartTracker;
import org.eclipse.papyrus.uml.diagram.sequence.util.SelfMessageHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.ui.PlatformUI;

public abstract class AbstractMessageEditPart extends UMLConnectionNodeEditPart implements IKeyPressState {

	private List messageEventParts;

	private boolean reorderMessages = false;

	/**
	 * The shift down key.
	 *
	 * @since 5.1
	 */
	protected KeyboardListener SHIFTDown = new KeyboardListener(this, SWT.SHIFT, true);

	/**
	 * The shift up key.
	 *
	 * @since 5.1
	 */
	protected KeyboardListener SHIFTUp = new KeyboardListener(this, SWT.SHIFT, false);

	/**
	 * Handle mouse move event to update cursors.
	 */
	private MouseMoveListener mouseMoveListener;

	private Cursor myCursor;

	private Cursor defaultCursor;

	public AbstractMessageEditPart(View view) {
		super(view);
	}

	/**
	 * {{@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.uml.diagram.sequence.keyboardlistener.IKeyPressState#setKeyPressState(java.lang.Boolean)
	 *
	 * @param isPressed
	 * @since 5.1
	 */
	@Override
	public void setKeyPressState(Boolean isPressed) {
		reorderMessages = isPressed;
	}

	/**
	 * Get the value of reorderMessages property. This is defined by the Shift pressed Key.
	 *
	 * @return true if the messages should be reordered in the Interaction and false if the move of a message move also the other message without reorder it.
	 * @since 5.1
	 */
	public boolean mustReorderMessage() {
		if (getFigure() instanceof MessageDelete) {
			return false;
		}
		return reorderMessages;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		// activate listeners
		PlatformUI.getWorkbench().getDisplay().addFilter(SWT.KeyDown, SHIFTDown);
		PlatformUI.getWorkbench().getDisplay().addFilter(SWT.KeyUp, SHIFTUp);
		if (getTarget() == null || getSource() == null) {
			getViewer().getControl().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					hookGraphicalViewer();
				}
			});
		} else {
			hookGraphicalViewer();
		}
	}


	/**
	 * This method has been added in order to satisfy the requirement Diagram.UML.Sequence.REQ_004:
	 * <I>"It should be possible to select and move several messages at the same time."</I>
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart#getDragTracker(org.eclipse.gef.Request)
	 *
	 *
	 * @param req
	 * @return the drag tracker
	 *
	 */
	@Override
	public DragTracker getDragTracker(Request req) {
		return new SelectMessagesEditPartTracker(this);
	}


	private void hookGraphicalViewer() {
		if (SelfMessageHelper.isSelfLink(this)) {
			getViewer().getControl().addMouseMoveListener(mouseMoveListener = new MouseMoveListener() {

				@Override
				public void mouseMove(org.eclipse.swt.events.MouseEvent e) {
					handleMouseMoved(e.x, e.y);
				}
			});
		}
	}

	/**
	 * Update cursor for self message.
	 */
	protected void handleMouseMoved(int x, int y) {
		myCursor = null;
		if (defaultCursor != null) {
			getViewer().setCursor(Cursors.ARROW);
			defaultCursor = null;
		}
		if (!SelfMessageHelper.isSelfLink(this)) {
			return;
		}
		UMLEdgeFigure primaryShape = getPrimaryShape();
		Point p = new Point(x, y);
		primaryShape.translateToRelative(p);
		if (!primaryShape.containsPoint(p.x, p.y)) {
			return;
		}
		PointList points = primaryShape.getPoints();
		if (points.size() <= 1) {
			return;
		}
		List lineSegments = PointListUtilities.getLineSegments(points);
		LineSeg nearestSegment = PointListUtilities.getNearestSegment(lineSegments, p.x, p.y);
		if (points.size() > 3 && (p.getDistance(points.getPoint(1)) < 5 || p.getDistance(points.getPoint(2)) < 5)) {
			myCursor = Cursors.SIZEALL;
		} else if (nearestSegment.isHorizontal()) {
			myCursor = Cursors.SIZENS;
		} else {
			myCursor = Cursors.SIZEWE;
		}
		defaultCursor = getViewer().getControl().getCursor();
		getViewer().setCursor(myCursor);
		if (SelfMessageHelper.isSelfLink(this)) {
			getPrimaryShape().setCustomCursor(myCursor);
		} else {
			getPrimaryShape().setCustomCursor(null);
		}
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.editparts.UMLConnectionNodeEditPart#getPrimaryShape()
	 *
	 * @return
	 */
	@Override
	public abstract MessageFigure getPrimaryShape();

	@Override
	public void deactivate() {
		if (mouseMoveListener != null) {
			getViewer().getControl().removeMouseMoveListener(mouseMoveListener);
		}
		PlatformUI.getWorkbench().getDisplay().removeFilter(SWT.KeyDown, SHIFTDown);
		PlatformUI.getWorkbench().getDisplay().removeFilter(SWT.KeyUp, SHIFTUp);
		super.deactivate();
	}

	private Cursor getCustomCursor() {
		if (!SelfMessageHelper.isSelfLink(this)) {
			return null;
		}
		return myCursor;
	}

	@Override
	protected int getLineWidth() {
		int lineWidth = super.getLineWidth();
		return lineWidth < 0 ? 1 : lineWidth;
	}

	@Override
	protected void fireSelectionChanged() {
		super.fireSelectionChanged();
		UMLEdgeFigure primaryShape = getPrimaryShape();
		if (primaryShape instanceof MessageFigure) {
			((MessageFigure) primaryShape).setSelection(getSelected() != SELECTED_NONE);
		}
	}

	public View findChildByModel(EObject model) {
		List list = getModelChildren();
		if (list != null && list.size() > 0) {
			for (Object o : list) {
				if (!(o instanceof View)) {
					continue;
				}
				View view = (View) o;
				if (view.getElement() == model) {
					return view;
				}
			}
		}
		return null;
	}


	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(IMaskManagedLabelEditPolicy.MASK_MANAGED_LABEL_EDIT_POLICY, new MessageLabelEditPolicy());
		// Ordering Message Occurrence Specification. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=403233
		installEditPolicy(ConnectRectangleToGridEditPolicy.CONNECT_TO_GRILLING_MANAGEMENT, new ConnectMessageToGridEditPolicy());
		installEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE, new SequenceReferenceEditPolicy());
	}

	@Override
	public EditPart getTargetEditPart(Request request) {
		if (request instanceof CreateUnspecifiedTypeConnectionRequest) {
			List types = ((CreateUnspecifiedTypeConnectionRequest) request).getElementTypes();
			if (types.contains(UMLElementTypes.Message_FoundEdge) || types.contains(UMLElementTypes.Message_LostEdge)) {
				return null;
			}
		} else if (request instanceof ReconnectRequest) {
			ConnectionEditPart con = ((ReconnectRequest) request).getConnectionEditPart();
			if (con instanceof MessageLostEditPart || con instanceof MessageFoundEditPart) {
				return null;
			}
		}
		return super.getTargetEditPart(request);
	}

	@Override
	protected void handleNotificationEvent(Notification notification) {
		super.handleNotificationEvent(notification);
		Object feature = notification.getFeature();
		MessageLabelEditPart labelPart = getMessageLabelEditPart();
		if (labelPart == null) {
			return;
		}
		if (NotationPackage.eINSTANCE.getFontStyle_FontColor().equals(feature)) {
			labelPart.refreshFontColor();
		} else if (NotationPackage.eINSTANCE.getFontStyle_FontHeight().equals(feature) || NotationPackage.eINSTANCE.getFontStyle_FontName().equals(feature) || NotationPackage.eINSTANCE.getFontStyle_Bold().equals(feature)
				|| NotationPackage.eINSTANCE.getFontStyle_Italic().equals(feature)) {
			labelPart.refreshFont();
		}
	}

	public MessageLabelEditPart getMessageLabelEditPart() {
		for (Object c : this.getChildren()) {
			if (c instanceof MessageLabelEditPart) {
				return (MessageLabelEditPart) c;
			}
		}
		return null;
	}

	// public abstract IFigure getPrimaryShape() ;
	@Override
	public void setLineWidth(int width) {
		if (getPrimaryShape() instanceof MessageFigure) {
			MessageFigure edge = getPrimaryShape();
			edge.setLineWidth(width < 0 ? 1 : width);
		}
	}

	@Override
	public void showSourceFeedback(Request request) {
		if (request instanceof CreateUnspecifiedTypeRequest) {
			getSource().showSourceFeedback(request);
			getTarget().showSourceFeedback(request);
		}
		super.showSourceFeedback(request);
	}

	/**
	 * @generated NOT Override for redirecting creation request to the lifeline
	 */
	@Override
	public void eraseSourceFeedback(Request request) {
		if (request instanceof CreateUnspecifiedTypeRequest) {
			getSource().eraseSourceFeedback(request);
			getTarget().eraseSourceFeedback(request);
		}
		super.eraseSourceFeedback(request);
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart#installRouter()
	 *
	 */
	@Override
	protected void installRouter() {
		getConnectionFigure().setConnectionRouter(LifeLineGraphicalNodeEditPolicy.messageRouter);
		getConnectionFigure().setCursor(org.eclipse.gmf.runtime.gef.ui.internal.l10n.Cursors.CURSOR_SEG_MOVE);
		refreshBendpoints();
	}

	/**
	 * Ignore routing style since we are using a custom router and a custom ConnectionBendpointEditPolicy.
	 */
	@Override
	protected void refreshRoutingStyles() {
		// Do nothing
	}

	static abstract class MessageLabelEditPart extends LabelEditPart {

		public MessageLabelEditPart(View view) {
			super(view);
		}

		@Override
		protected void handleNotificationEvent(Notification notification) {
			Object feature = notification.getFeature();
			if (NotationPackage.eINSTANCE.getLineStyle_LineColor().equals(feature)) {
				refreshFontColor();
			} else {
				super.handleNotificationEvent(notification);
			}
		}

		@Override
		public void refreshFontColor() {
			FontStyle style = (FontStyle) ((org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart) getParent()).getPrimaryView().getStyle(NotationPackage.Literals.FONT_STYLE);
			if (style != null) {
				setFontColor(DiagramColorRegistry.getInstance().getColor(Integer.valueOf(style.getFontColor())));
			}
		}

		@Override
		protected void refreshFont() {
			super.refreshFont();
		}
	}
}
