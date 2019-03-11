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

import org.eclipse.draw2d.Connection;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.IPapyrusEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.figures.CustomGeneralOrderingDescriptor;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomGeneralOrderingEditPart extends GeneralOrderingEditPart implements IPapyrusEditPart {

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CustomGeneralOrderingEditPart(View view) {
		super(view);
	}

	@Override
	protected void handleNotificationEvent(Notification notification) {
		super.handleNotificationEvent(notification);
		Object feature = notification.getFeature();
		if (NotationPackage.eINSTANCE.getLineStyle_LineWidth().equals(feature)) {
			refreshLineWidth();
		}
	}

	@Override
	protected Connection createConnectionFigure() {
		return new CustomGeneralOrderingDescriptor(getMapMode());
	}

	/**
	 * @since 3.0
	 */
	@Override
	public CustomGeneralOrderingDescriptor getPrimaryShape() {

		return (CustomGeneralOrderingDescriptor) getFigure();
	}

	@Override
	protected void setLineWidth(int width) {
		getPrimaryShape().setLineWidth(width < 0 ? 1 : 0);
	}

	@Override
	public EditPart getTargetEditPart(Request request) {
		EditPart ep = super.getTargetEditPart(request);
		if (ep != null && ep instanceof org.eclipse.gef.ConnectionEditPart) {
			if (request instanceof ReconnectRequest) {
				ReconnectRequest rRequest = (ReconnectRequest) request;

				// If source anchor is moved, the connection's source edit part
				// should not be taken into account for a cyclic dependency
				// check so as to avoid false checks. Same goes for the target
				// anchor. See bugzilla# 417373 -- we do not want to target a
				// connection that is already connected to us so that we do not
				// introduce a cyclic connection
				if (isCyclicConnectionRequest((org.eclipse.gef.ConnectionEditPart) ep, rRequest.getConnectionEditPart())) {
					return null;
				}
			}
		}

		return ep;
	}

	/**
	 * Fixed bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=417373
	 *
	 * The ends of GeneralOrderingEditPart are MessageEndEditParts which parent are Message*EditParts, once we move the ends of the messages, we
	 * should IGNORE to move current GeneralOrdering, otherwise cyclic dependency occur.
	 *
	 */
	private boolean isCyclicConnectionRequest(ConnectionEditPart currentConn, ConnectionEditPart reqConn) {
		if (currentConn == null || reqConn == null) {
			return false;
		}
		EditPart source = currentConn.getSource();
		EditPart target = currentConn.getTarget();
		if (reqConn == source || reqConn == target) {
			return true;
		}
		if (reqConn == source.getParent() || reqConn == target.getParent()) {
			return true;
		}
		return false;
	}

}
