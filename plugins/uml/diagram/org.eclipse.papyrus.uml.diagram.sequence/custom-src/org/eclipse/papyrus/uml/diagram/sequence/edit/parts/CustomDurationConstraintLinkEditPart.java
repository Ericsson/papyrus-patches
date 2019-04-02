/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gmf.runtime.notation.Connector;
import org.eclipse.gmf.runtime.notation.IntValueStyle;
import org.eclipse.gmf.runtime.notation.NamedStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.DurationLinkSelectionHandlesEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.figures.DurationLinkFigure;

public class CustomDurationConstraintLinkEditPart extends DurationConstraintLinkEditPart {

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CustomDurationConstraintLinkEditPart(View view) {
		super(view);
	}

	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new DurationLinkSelectionHandlesEditPolicy(this, getEditingDomain()));
	}

	@Override
	protected void refreshVisuals() {
		refreshArrowDelta();
		super.refreshVisuals();
	}

	protected void refreshArrowDelta() {
		Connector connector = (Connector) getNotationView();
		NamedStyle namedStyle = connector.getNamedStyle(NotationPackage.Literals.INT_VALUE_STYLE, DurationLinkFigure.DELTA_VIEW_STYLE);
		if (namedStyle instanceof IntValueStyle) {
			int delta = ((IntValueStyle) namedStyle).getIntValue();
			getPrimaryShape().setArrowPositionDelta(delta);
		} else {
			// no style - reset value
			getPrimaryShape().setArrowPositionDelta(0);
		}
	}

	@Override
	protected void handleNotificationEvent(Notification event) {
		if (isDeltaIntValueStyle(event.getNotifier()) ||
				(event.getNotifier() == getNotationView()
						&& event.getFeature() == NotationPackage.Literals.VIEW__STYLES &&
						(isDeltaIntValueStyle(event.getNewValue()) ||
								(event.getNewValue() == null && isDeltaIntValueStyle(event.getOldValue()))))) {
			refreshArrowDelta();
		}
		super.handleNotificationEvent(event);
	}

	private boolean isDeltaIntValueStyle(Object object) {
		return object instanceof IntValueStyle && DurationLinkFigure.DELTA_VIEW_STYLE.equals(((IntValueStyle) object).getName());
	}
}
