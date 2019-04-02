/*****************************************************************************
 * Copyright (c) 2010 CEA
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Soyatec - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ITreeBranchEditPart;
import org.eclipse.gmf.runtime.draw2d.ui.internal.figures.ConnectionLayerEx;
import org.eclipse.gmf.runtime.draw2d.ui.internal.routers.FanRouter;
import org.eclipse.gmf.runtime.gef.ui.internal.tools.SelectConnectionEditPartTracker;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.RoutingStyle;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.draw2d.routers.DurationConstraintAutomaticRouter;
import org.eclipse.papyrus.uml.diagram.sequence.util.LinkRouteModelElementFactory;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomCommentAnnotatedElementEditPart extends CommentAnnotatedElementEditPart implements ITreeBranchEditPart {

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CustomCommentAnnotatedElementEditPart(View view) {
		super(view);
	}


	@Override
	protected void handleNotificationEvent(Notification event) {
		super.handleNotificationEvent(event);
		if (LinkRouteModelElementFactory.isRoutingNotification(event)) {
			installRouter();
		}
	}

	private FanRouter customRouter;

	@Override
	protected void installRouter() {
		if (this.getSource() instanceof CustomDurationConstraintEditPart) {
			ConnectionLayer cLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
			RoutingStyle style = (RoutingStyle) ((View) getModel()).getStyle(NotationPackage.Literals.ROUTING_STYLE);
			if (style != null && cLayer instanceof ConnectionLayerEx) {
				ConnectionLayerEx cLayerEx = (ConnectionLayerEx) cLayer;
				if (LinkRouteModelElementFactory.isAutomaticRouting(this.getNotationView())) {
					CustomDurationConstraintEditPart customDurationConstraintEditPart = (CustomDurationConstraintEditPart) this.getSource();
					if (customRouter == null) {
						FanRouter router = new FanRouter();
						router.setNextRouter(new DurationConstraintAutomaticRouter(this.getNotationView()));
						customRouter = router;
					}
					getConnectionFigure().setConnectionRouter(customRouter);
				} else {
					getConnectionFigure().setConnectionRouter(cLayerEx.getObliqueRouter());
				}
			}
			refreshRouterChange();
		} else {
			super.installRouter();
		}
	}

	@Override
	public void setSource(EditPart editPart) {
		super.setSource(editPart);
		// Fixed bug about duration constraint links' automatic router.
		if (editPart instanceof CustomDurationConstraintEditPart) {
			installRouter();
		}
	}

	@Override
	public DragTracker getDragTracker(final Request req) {
		return new SelectConnectionEditPartTrackerEx(this);
	}

	class SelectConnectionEditPartTrackerEx extends SelectConnectionEditPartTracker {

		public SelectConnectionEditPartTrackerEx(CommentAnnotatedElementEditPart owner) {
			super(owner);
		}

		@Override
		protected boolean handleDragInProgress() {
			if (isInState(STATE_DRAG_IN_PROGRESS) && shouldAllowDrag()) {
				LinkRouteModelElementFactory.switchToManualRouting(getEdge());
				super.handleDragInProgress();
			}
			return true;
		}
	};
}
