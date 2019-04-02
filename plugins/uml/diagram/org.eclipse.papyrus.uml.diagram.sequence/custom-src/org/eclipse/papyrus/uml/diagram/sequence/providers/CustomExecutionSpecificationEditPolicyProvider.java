/*****************************************************************************
 * Copyright (c) 2017, 2018 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Initial API and implementation
 *   Christian W. Damus - bug 536486
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.providers;

import static org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart.findNearestSide;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.CreateEditPoliciesOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.IEditPolicyProvider;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.CustomExecutionSpecificationSemanticEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.TimeElementCreationFeedbackEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.locator.TimeElementLocator;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.CustomExecutionSpecificationCreationEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.CustomExecutionSpecificationXYLayoutEditPolicy;

/**
 * The edit policy provider for the execution specifications.
 */
public class CustomExecutionSpecificationEditPolicyProvider extends AbstractProvider implements IEditPolicyProvider {

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.common.core.service.IProvider#provides(org.eclipse.gmf.runtime.common.core.service.IOperation)
	 */
	@Override
	public boolean provides(final IOperation operation) {

		if (operation instanceof CreateEditPoliciesOperation) {
			final EditPart editPart = ((CreateEditPoliciesOperation) operation).getEditPart();
			if (editPart instanceof AbstractExecutionSpecificationEditPart) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void createEditPolicies(final EditPart editPart) {
		editPart.installEditPolicy(EditPolicyRoles.CREATION_ROLE, new CustomExecutionSpecificationCreationEditPolicy());
		editPart.installEditPolicy(EditPolicy.LAYOUT_ROLE, new CustomExecutionSpecificationXYLayoutEditPolicy());
		editPart.installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new CustomExecutionSpecificationSemanticEditPolicy());

		TimeElementCreationFeedbackEditPolicy tecfep = new TimeElementCreationFeedbackEditPolicy(
				parentFigure -> new TimeElementLocator(parentFigure,
						constraint -> findNearestSide(parentFigure, constraint)));
		editPart.installEditPolicy(TimeElementCreationFeedbackEditPolicy.ROLE, tecfep);
	}

}
