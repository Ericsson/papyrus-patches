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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.IPapyrusEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.AppliedStereotypeCommentCreationEditPolicyEx;
import org.eclipse.papyrus.uml.diagram.stereotype.edition.editpolicies.AppliedStereotypeCommentEditPolicy;

/**
 * Add implementing interface IPapyrusEditPart to displaying Stereotypes.
 *
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomDestructionOccurrenceSpecificationEditPart extends DestructionOccurrenceSpecificationEditPart implements IPapyrusEditPart {

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CustomDestructionOccurrenceSpecificationEditPart(View view) {
		super(view);
	}

	@Override
	protected NodeFigure createNodePlate() {
		NodeFigure result = super.createNodePlate();
		// FIXME: workaround for #154536
		result.getBounds().setSize(result.getPreferredSize());
		return result;
	}

	/**
	 * @Override
	 */
	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		// install a editpolicy to display stereotypes
		installEditPolicy(AppliedStereotypeCommentEditPolicy.APPLIED_STEREOTYPE_COMMENT, new AppliedStereotypeCommentCreationEditPolicyEx());
	}

	@Override
	protected void refreshBounds() {
		if (getBorderItemLocator() != null) {
			int x = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE.getLocation_X())).intValue();
			int y = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE.getLocation_Y())).intValue();
			Point loc = new Point(x, y);
			int width = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE.getSize_Width())).intValue();
			int height = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE.getSize_Height())).intValue();
			Dimension size = new Dimension(width, height);
			if (width != -1 && height != -1) {
				getFigure().setBounds(new Rectangle(loc, size));
			}
			getBorderItemLocator().setConstraint(new Rectangle(loc, size));
		} else {
			super.refreshBounds();
		}
	}

	@Override
	protected void handleNotificationEvent(Notification notification) {
		super.handleNotificationEvent(notification);
		Object feature = notification.getFeature();
		if ((getModel() != null) && (getModel() == notification.getNotifier())) {
			if (NotationPackage.eINSTANCE.getLineStyle_LineWidth().equals(feature)) {
				refreshLineWidth();
			}
		}
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		refreshLineWidth();
	}
}
