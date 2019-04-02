/*****************************************************************************
 * Copyright (c) 2018 Christian W. Damus, CEA LIST, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.SetBoundsCommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest.ViewDescriptor;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultCreationEditPolicy;
import org.eclipse.papyrus.uml.service.types.element.UMLDIElementTypes;

/**
 * Custom creation edit policy for destruction occurrences, supporting time
 * constraints and time observations.
 */
public class CustomDestructionOccurrenceSpecificationCreationEditPolicy extends DefaultCreationEditPolicy {

	@Override
	protected ICommand getSetBoundsCommand(CreateViewRequest request, ViewDescriptor descriptor) {
		if (UMLDIElementTypes.TIME_CONSTRAINT_SHAPE.getSemanticHint().equals(descriptor.getSemanticHint())
				|| UMLDIElementTypes.TIME_OBSERVATION_SHAPE.getSemanticHint().equals(descriptor.getSemanticHint())) {

			// The visualization of the time constraint cannot be moved
			Dimension size = new Dimension(40, 1);
			return new SetBoundsCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), DiagramUIMessages.Commands_MoveElement, descriptor, size);
		}
		return super.getSetBoundsCommand(request, descriptor);
	}
}
