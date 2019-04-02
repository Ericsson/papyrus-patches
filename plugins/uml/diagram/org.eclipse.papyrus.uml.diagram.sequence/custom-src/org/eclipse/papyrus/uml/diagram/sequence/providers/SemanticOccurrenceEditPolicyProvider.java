/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.providers;

import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.CreateEditPoliciesOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.IEditPolicyProvider;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractMessageEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.OccurenceSemanticEditPolicy;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.OccurrenceSpecification;

/**
 * <p>
 * An {@link IEditPolicyProvider} installing an {@link OccurenceSemanticEditPolicy} on
 * {@link Message} and {@link ExecutionSpecification} edit parts.
 * </p>
 * <p>
 * Note: it doesn't have to be installed on {@link DestructionOccurrenceSpecification}, because that
 * edit part already directly represents a single {@link OccurrenceSpecification}.
 * </p>
 */
public class SemanticOccurrenceEditPolicyProvider extends AbstractProvider implements IEditPolicyProvider {

	@Override
	public boolean provides(IOperation operation) {
		if (false == operation instanceof CreateEditPoliciesOperation) {
			return false;
		}

		CreateEditPoliciesOperation op = (CreateEditPoliciesOperation) operation;
		EditPart editPart = op.getEditPart();

		// Only install this on Message EditParts. We also need that policy for ExecSpecs,
		// but CustomExecutionSpecificationEditPolicyProvider already takes care of that
		return editPart instanceof AbstractMessageEditPart;
	}

	@Override
	public void createEditPolicies(EditPart editPart) {
		editPart.installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new OccurenceSemanticEditPolicy());
	}

}
