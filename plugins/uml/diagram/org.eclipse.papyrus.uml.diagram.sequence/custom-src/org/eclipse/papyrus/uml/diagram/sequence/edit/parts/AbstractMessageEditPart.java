/*****************************************************************************
 * Copyright (c) 2017, 2018 CEA LIST, EclipseSource and others.
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
 *   EclipseSource - Bug 536631
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.diagram.ui.editparts.LabelEditPart;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramColorRegistry;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest;
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
import org.eclipse.papyrus.uml.diagram.common.service.ApplyStereotypeRequest;
import org.eclipse.papyrus.uml.diagram.sequence.anchors.ConnectionSourceAnchor;
import org.eclipse.papyrus.uml.diagram.sequence.anchors.ConnectionTargetAnchor;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.MessageGraphicalNodeEditPolicy;
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
import org.eclipse.papyrus.uml.diagram.sequence.util.DurationLinkUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.GeneralOrderingUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.OccurrenceSpecificationUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.SelectMessagesEditPartTracker;
import org.eclipse.papyrus.uml.diagram.sequence.util.SelfMessageHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.ui.PlatformUI;

public abstract class AbstractMessageEditPart extends UMLConnectionNodeEditPart implements IKeyPressState {

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
		List<?> lineSegments = PointListUtilities.getLineSegments(points);
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
		List<?> list = getModelChildren();
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
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new MessageGraphicalNodeEditPolicy());
	}

	@Override
	public EditPart getTargetEditPart(Request request) {
		if (request instanceof CreateUnspecifiedTypeConnectionRequest) {
			List<?> types = ((CreateUnspecifiedTypeConnectionRequest) request).getElementTypes();
			if (types.contains(UMLElementTypes.Message_FoundEdge) || types.contains(UMLElementTypes.Message_LostEdge)) {
				return null;
			}
		} else if (request instanceof ReconnectRequest) {
			ConnectionEditPart con = ((ReconnectRequest) request).getConnectionEditPart();
			if (con instanceof MessageLostEditPart || con instanceof MessageFoundEditPart) {
				return null;
			}
			// Workaround for Bug 537724: GMF does not support reconnection of links if link.source == link.target and
			// the source/target is a link.
			// We need to copy all inherited implementations, except the problematic GMF one... To be safe, only do
			// that for DurationLinks and GeneralOrderings, since that's the case we want to support
			ReconnectRequest reconnectRequest = (ReconnectRequest) request;
			if (DurationLinkUtil.isDurationLink(reconnectRequest) || GeneralOrderingUtil.isGeneralOrderingLink(reconnectRequest)) {
				return doGetTargetEditPart(reconnectRequest);
			}
		}
		return super.getTargetEditPart(request);
	}

	/**
	 * Workaround for Bug 537724: GMF's implementation of cyclic dependency is incorrect,
	 * and we need to bypass it. Unfortunately, that means we need to copy all inherited
	 * implementations.
	 *
	 * @param reconnectRequest
	 * @return
	 */
	protected EditPart doGetTargetEditPart(ReconnectRequest reconnectRequest) {
		// From UMLConnectionNodeEditPart
		if (ApplyStereotypeRequest.APPLY_STEREOTYPE_REQUEST.equals(reconnectRequest.getType())) {
			return this;
		}

		// From GEF's AbstractEditPart
		EditPolicyIterator i = getEditPolicyIterator();
		EditPart targetEditPart = null;
		while (i.hasNext()) {
			targetEditPart = i.next().getTargetEditPart(reconnectRequest);
			if (targetEditPart != null) {
				break;
			}
		}

		// From GMF's ConnectionNodeEditPart (The buggy part)

		if (reconnectRequest.isMovingStartAnchor()) {
			if (reconnectRequest.getConnectionEditPart().getSource() == targetEditPart) {
				return targetEditPart;
			}
		} else if (reconnectRequest.getConnectionEditPart().getTarget() == targetEditPart) {
			return targetEditPart;
		}

		// If source anchor is moved, the connection's source edit part
		// should not be taken into account for a cyclic dependency
		// check so as to avoid false checks. Same goes for the target
		// anchor. See bugzilla# 155243 -- we do not want to target a
		// connection that is already connected to us so that we do not
		// introduce a cyclic connection
		if (isCyclicConnectionRequest((org.eclipse.gef.ConnectionEditPart) targetEditPart,
				reconnectRequest.getConnectionEditPart(), false, reconnectRequest.isMovingStartAnchor())) {
			return null;
		}

		return targetEditPart;
	}

	// Custom implementation of the parent method, which is buggy
	// This is a workaround for Bug 537724
	// TODO This implementation should be properly tested... It allows more cases than
	// the parent one, and may potentially allow cycles
	private boolean isCyclicConnectionRequest(org.eclipse.gef.ConnectionEditPart targetCEP,
			org.eclipse.gef.ConnectionEditPart sourceCEP,
			boolean checkSourceAndTargetEditParts, boolean doNotCheckSourceEditPart) {
		if (targetCEP == null || sourceCEP == null) {
			return false;
		}

		// first, do a cyclic check on source and target connections
		// of the source connection itself.
		// (as every connection is also a node).

		Set<IFigure> set = new HashSet<>();
		getSourceAndTargetConnections(set, sourceCEP);
		if (set.contains(targetCEP.getFigure())) {
			return true;
		}

		// now do the cyclic check on the source and target of the source connection...
		EditPart sourceEP = sourceCEP.getSource(),
				targetEP = sourceCEP.getTarget();

		if (!checkSourceAndTargetEditParts && doNotCheckSourceEditPart) {
			// .
		} else if (sourceEP instanceof org.eclipse.gef.ConnectionEditPart &&
				isCyclicConnectionRequest(targetCEP,
						(org.eclipse.gef.ConnectionEditPart) sourceEP,
						true, doNotCheckSourceEditPart)) {
			return true;
		}

		if (!checkSourceAndTargetEditParts && !doNotCheckSourceEditPart) {
			// .
		} else if (targetEP instanceof org.eclipse.gef.ConnectionEditPart &&
				isCyclicConnectionRequest(targetCEP,
						(org.eclipse.gef.ConnectionEditPart) targetEP,
						true, doNotCheckSourceEditPart)) {
			return true;
		}

		return false;
	}

	private void getSourceAndTargetConnections(Set<IFigure> set,
			org.eclipse.gef.ConnectionEditPart connectionEditPart) {

		if (connectionEditPart == null || set == null) {
			return;
		}

		for (Iterator<?> i = connectionEditPart.getSourceConnections().iterator(); i.hasNext();) {
			org.eclipse.gef.ConnectionEditPart next = (org.eclipse.gef.ConnectionEditPart) i.next();
			Connection sourceConnection = (Connection) next.getFigure();
			set.add(sourceConnection);
			getSourceAndTargetConnections(set, next);
		}

		for (Iterator<?> i = connectionEditPart.getTargetConnections().iterator(); i.hasNext();) {
			org.eclipse.gef.ConnectionEditPart next = (org.eclipse.gef.ConnectionEditPart) i.next();
			Connection targetConnection = (Connection) next.getFigure();
			set.add(targetConnection);
			getSourceAndTargetConnections(set, next);
		}
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

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		if (request instanceof CreateUnspecifiedTypeConnectionRequest) {
			CreateUnspecifiedTypeConnectionRequest createRequest = (CreateUnspecifiedTypeConnectionRequest) request;
			List<?> relationshipTypes = createRequest.getElementTypes();
			for (Object type : relationshipTypes) {
				if (UMLElementTypes.DurationConstraint_Edge.equals(type) || UMLElementTypes.DurationObservation_Edge.equals(type) || UMLElementTypes.GeneralOrdering_Edge.equals(type)) {
					return OccurrenceSpecificationUtil.isSource(getConnectionFigure(), createRequest.getLocation()) ? new ConnectionSourceAnchor(getPrimaryShape()) : new ConnectionTargetAnchor(getPrimaryShape());
				}
			}
		} else if (request instanceof CreateConnectionViewRequest) {
			CreateConnectionViewRequest createRequest = (CreateConnectionViewRequest) request;
			if (DurationLinkUtil.isDurationLink(createRequest) || GeneralOrderingUtil.isGeneralOrderingLink(createRequest)) {
				return OccurrenceSpecificationUtil.isSource(getConnectionFigure(), createRequest.getLocation()) ? new ConnectionSourceAnchor(getPrimaryShape()) : new ConnectionTargetAnchor(getPrimaryShape());
			}
		} else if (request instanceof ReconnectRequest) {
			ReconnectRequest reconnectRequest = (ReconnectRequest) request;
			if (DurationLinkUtil.isDurationLink(reconnectRequest) || GeneralOrderingUtil.isGeneralOrderingLink(reconnectRequest)) {
				return OccurrenceSpecificationUtil.isSource(getConnectionFigure(), reconnectRequest.getLocation()) ? new ConnectionSourceAnchor(getPrimaryShape()) : new ConnectionTargetAnchor(getPrimaryShape());
			}
		}
		return super.getSourceConnectionAnchor(request);
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		if (request instanceof CreateUnspecifiedTypeConnectionRequest) {
			CreateUnspecifiedTypeConnectionRequest createRequest = (CreateUnspecifiedTypeConnectionRequest) request;
			List<?> relationshipTypes = createRequest.getElementTypes();
			for (Object type : relationshipTypes) {
				if (UMLElementTypes.DurationConstraint_Edge.equals(type) || UMLElementTypes.DurationObservation_Edge.equals(type) || UMLElementTypes.GeneralOrdering_Edge.equals(type)) {
					return OccurrenceSpecificationUtil.isSource(getConnectionFigure(), createRequest.getLocation()) ? new ConnectionSourceAnchor(getPrimaryShape()) : new ConnectionTargetAnchor(getPrimaryShape());
				}
			}
		} else if (request instanceof CreateConnectionViewRequest) {
			CreateConnectionViewRequest createRequest = (CreateConnectionViewRequest) request;
			if (DurationLinkUtil.isDurationLink(createRequest) || GeneralOrderingUtil.isGeneralOrderingLink(createRequest)) {
				return OccurrenceSpecificationUtil.isSource(getConnectionFigure(), createRequest.getLocation()) ? new ConnectionSourceAnchor(getPrimaryShape()) : new ConnectionTargetAnchor(getPrimaryShape());
			}
		} else if (request instanceof ReconnectRequest) {
			ReconnectRequest reconnectRequest = (ReconnectRequest) request;
			if (DurationLinkUtil.isDurationLink(reconnectRequest) || GeneralOrderingUtil.isGeneralOrderingLink(reconnectRequest)) {
				return OccurrenceSpecificationUtil.isSource(getConnectionFigure(), reconnectRequest.getLocation()) ? new ConnectionSourceAnchor(getPrimaryShape()) : new ConnectionTargetAnchor(getPrimaryShape());
			}
		}
		return super.getTargetConnectionAnchor(request);
	}


}
