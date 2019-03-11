/*****************************************************************************
 * Copyright (c) 2012, 2016 CEA LIST, Christian W. Damus, and others.
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
 *  Christian W. Damus - bug 474467
 *  
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.providers;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ResizableCompartmentEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.editpart.EditPartService;
import org.eclipse.gmf.runtime.notation.BasicCompartment;
import org.eclipse.gmf.runtime.notation.DecorationNode;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.types.NotationTypesMap;
import org.eclipse.papyrus.infra.viewpoints.policy.ViewPrototypeLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * A Label Provider for GMF Notation model
 */
public class NotationLabelProvider extends ViewPrototypeLabelProvider {

	/** icon for a compartment */
	public static final String ICON_COMPARTMENT = "/icons/none_comp_vis.gif"; //$NON-NLS-1$

	@Override
	protected Image getImage(EObject element) {
		// if the element is a compartment
		if (element instanceof BasicCompartment || element instanceof DecorationNode) {
			return org.eclipse.papyrus.infra.widgets.Activator.getDefault().getImage(Activator.ID, ICON_COMPARTMENT);
		}

		return super.getImage(element);
	}

	@Override
	protected String getText(EObject element) {
		String result = null;

		if (element instanceof Diagram) {
			result = super.getText(element);
		} else if (element instanceof View) { // maybe it is a view of a compartment
			String humanType = NotationTypesMap.instance.getHumanReadableType((View) element);
			if (humanType != null) {
				result = humanType;
			} else {
				EditPart dummyEP = EditPartService.getInstance().createGraphicEditPart((View) element);
				if (dummyEP instanceof ResizableCompartmentEditPart) {
					result = ((ResizableCompartmentEditPart) dummyEP).getCompartmentName();
				}
			}
		} else {
			result = super.getText(element);
		}

		return result;
	}
}
