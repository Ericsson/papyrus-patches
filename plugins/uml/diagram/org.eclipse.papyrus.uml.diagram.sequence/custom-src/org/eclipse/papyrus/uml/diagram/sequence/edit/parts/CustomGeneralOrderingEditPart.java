/*****************************************************************************
 * Copyright (c) 2010, 2018 CEA List, EclipseSource and others
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
 *   EclipseSource - Bug 537561
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import org.eclipse.draw2d.Connection;
import org.eclipse.emf.common.notify.Notification;
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
		getPrimaryShape().setLineWidth(width < 0 ? 1 : width);
	}

}
