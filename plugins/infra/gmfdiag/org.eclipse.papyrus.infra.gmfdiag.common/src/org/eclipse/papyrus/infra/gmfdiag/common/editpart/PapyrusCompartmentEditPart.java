/*****************************************************************************
 * Copyright (c) 2012 - 2018 CEA LIST, EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Camille Letavernier (CEA LIST) camille.letavernier@cea.fr - Initial API and implementation
 *  EclipseSource - Bug 535519
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpart;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.CompartmentEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.BorderDisplayEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.PapyrusWrappingLabel;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationUtils;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.NamedStyleProperties;


public abstract class PapyrusCompartmentEditPart extends CompartmentEditPart {

	public PapyrusCompartmentEditPart(EObject model) {
		super(model);
	}

	@Override
	public boolean isSelectable() {
		return false;
	}

	/**
	 * Provide access to isSelectable operation from super class, allows sub-classes to restore original behavior
	 *
	 * @return original method of superclass
	 */
	public boolean isSelectableOrig() {
		return super.isSelectable();
	}

	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(BorderDisplayEditPolicy.BORDER_DISPLAY_EDITPOLICY, new BorderDisplayEditPolicy());

	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		refreshLabelSize();
	}


	/**
	 * @since 3.101
	 */
	// Bug 535519
	protected void refreshLabelSize() {
		if (getFigure() instanceof PapyrusWrappingLabel) {
			int labelWidth = NotationUtils.getIntValue(getNotationView(), NamedStyleProperties.LABEL_WIDTH, PapyrusWrappingLabel.AUTO_SIZE);
			int labelHeight = NotationUtils.getIntValue(getNotationView(), NamedStyleProperties.LABEL_HEIGHT, PapyrusWrappingLabel.AUTO_SIZE);
			((PapyrusWrappingLabel)getFigure() ).setLabelSize(labelWidth, labelHeight);
		}
	}

}
