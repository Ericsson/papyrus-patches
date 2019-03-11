/*****************************************************************************
 * Copyright (c) 2017 CEA LIST, ALL4TEC and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Micka�l ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 510587
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.tools;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.papyrus.infra.gmfdiag.common.service.palette.AspectUnspecifiedTypeCreationTool;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;

/**
 * Try to create child in Interaction directly.
 *
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class InteractionChildCreationTool extends AspectUnspecifiedTypeCreationTool {

	/**
	 * Constructor.
	 *
	 * @param elementTypes
	 */
	public InteractionChildCreationTool(List<IElementType> elementTypes) {
		super(elementTypes);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gef.tools.TargetingTool#updateTargetUnderMouse()
	 */
	@Override
	protected boolean updateTargetUnderMouse() {
		if (antiScroll) {
			return super.updateTargetUnderMouse();
		}
		if (!isTargetLocked()) {
			EditPart editPart = null;
			if (getCurrentViewer() != null) {
				editPart = getCurrentViewer().findObjectAtExcluding(getLocation(), getExclusionSet(), getTargetingConditional());
			}
			if (editPart != null) {
				editPart = getInteractionEditPart(editPart);
			}
			boolean changed = getTargetEditPart() != editPart;
			setTargetEditPart(editPart);
			return changed;
		} else {
			return false;
		}
	}

	/**
	 * Get the Interaction Edit Part.
	 */
	private EditPart getInteractionEditPart(final EditPart editPart) {
		if (null == editPart) {
			return null;
		}
		if (editPart instanceof InteractionInteractionCompartmentEditPart) {
			return editPart;
		}
		return getInteractionEditPart(editPart.getParent());
	}
}