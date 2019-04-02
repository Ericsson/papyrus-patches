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

package org.eclipse.papyrus.uml.diagram.sequence.providers;

import java.util.OptionalInt;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.diagram.ui.figures.IBorderItemLocator;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.CreateEditPoliciesOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.IEditPolicyProvider;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CLifeLineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.CustomLifelineSemanticEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.TimeElementCreationFeedbackEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.locator.TimeElementLocator;

/**
 * Provider of custom edit policies for lifelines.
 */
public class CustomLifelineEditPolicyProvider extends AbstractProvider implements IEditPolicyProvider {

	@Override
	public boolean provides(final IOperation operation) {

		if (operation instanceof CreateEditPoliciesOperation) {
			final EditPart editPart = ((CreateEditPoliciesOperation) operation).getEditPart();
			if (editPart instanceof CLifeLineEditPart) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void createEditPolicies(final EditPart editPart) {
		CLifeLineEditPart lifeline = (CLifeLineEditPart) editPart;

		editPart.installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new CustomLifelineSemanticEditPolicy());
		editPart.installEditPolicy(TimeElementCreationFeedbackEditPolicy.ROLE, new TimeElementCreationFeedbackEditPolicy(
				parentFigure -> getTimeElementLocator(lifeline, parentFigure)));
	}

	private IBorderItemLocator getTimeElementLocator(CLifeLineEditPart lifeline, IFigure parentFigure) {
		return new TimeElementLocator(parentFigure, proposedConstraints -> {
			OptionalInt side = lifeline.getCreateMessageIncomingSide(proposedConstraints.getTopLeft());

			return side.isPresent()
					? PositionConstants.EAST_WEST ^ side.getAsInt()
					: PositionConstants.CENTER;
		});
	}
}
