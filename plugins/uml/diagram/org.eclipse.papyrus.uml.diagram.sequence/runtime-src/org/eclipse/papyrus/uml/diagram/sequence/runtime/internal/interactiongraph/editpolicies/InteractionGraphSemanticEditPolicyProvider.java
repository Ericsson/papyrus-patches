/*****************************************************************************
 * (c) Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.editpolicies;

import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.CreateEditPoliciesOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.IEditPolicyProvider;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractMessageEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CLifeLineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.GateNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionUseEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageAsyncAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageDeleteAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageFoundAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageLostAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageSyncAppliedStereotypeEditPart;
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
public class InteractionGraphSemanticEditPolicyProvider extends AbstractProvider implements IEditPolicyProvider {
	private static final Class<?> ADD_POLICY_CLASSES[] = {
		   AbstractMessageEditPart.class, CLifeLineEditPart.class,  AbstractExecutionSpecificationEditPart.class,
		   InteractionUseEditPart.class, InteractionEditPart.class
	};		

	private static final Class<?> REMOVE_POLICY_CLASSES[] = {
		   MessageAsyncAppliedStereotypeEditPart.class, 
		   MessageSyncAppliedStereotypeEditPart.class,
		   MessageCreateAppliedStereotypeEditPart.class,
		   MessageDeleteAppliedStereotypeEditPart.class,
		   MessageLostAppliedStereotypeEditPart.class,
		   MessageFoundAppliedStereotypeEditPart.class,
		   GateNameEditPart.class
	};		

	@Override
	public boolean provides(IOperation operation) {
		if (!(operation instanceof CreateEditPoliciesOperation)) {
			return false;
		}

		CreateEditPoliciesOperation op = (CreateEditPoliciesOperation) operation;
		EditPart editPart = op.getEditPart();

		for (Class<?> c : ADD_POLICY_CLASSES) {
			if (c.isInstance(editPart))
				return true;
		}

		for (Class<?> c : REMOVE_POLICY_CLASSES) {
			if (c.isInstance(editPart))
				return true;
		}
		
		return false; 
	}

	@Override
	public void createEditPolicies(EditPart editPart) {
		boolean add = false;
		for (Class<?> c : ADD_POLICY_CLASSES) {
			if (c.isInstance(editPart)) {
				add = true;
				break;
			}
		}

		boolean remove = false;
		for (Class<?> c : REMOVE_POLICY_CLASSES) {
			if (c.isInstance(editPart)) {
				remove = true;
				break;
			}
		}

		if (remove)
			editPart.removeEditPolicy(EditPolicyRoles.SEMANTIC_ROLE);
		if (add)
			editPart.installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new InteractionGraphSemanticEditPolicy());
	
	}

}
