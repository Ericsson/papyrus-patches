/*****************************************************************************
 * Copyright (c) 2018 Christian W. Damus and others.
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

package org.eclipse.papyrus.uml.diagram.sequence.validation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.uml.diagram.sequence.util.OccurrenceSpecificationHelper;
import org.eclipse.papyrus.uml.service.validation.internal.UMLDiagnostician;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.OccurrenceSpecification;

/**
 * A diagnostician for validation of an {@link InteractionOperand} sub-tree only.
 * This includes messages that have at least one end within the operand.
 * <b>Note</b> that this diagnostician should never be used in context of
 * general user-driven model validation.
 */
@SuppressWarnings("restriction")
public class OperandDiagnostician extends UMLDiagnostician {

	/**
	 * Initializes me.
	 */
	public OperandDiagnostician() {
		super();
	}

	@Override
	protected boolean doValidateContents(EObject eObject, DiagnosticChain diagnostics, Map<Object, Object> context) {
		boolean result = true;

		// Note that a message end can also be an occurrence specification, so
		// do both of these validations (not an 'else if')
		if (eObject instanceof MessageEnd) {
			// Validate the message, also
			result = validateMessage((MessageEnd) eObject, diagnostics, context);
		}
		if (eObject instanceof OccurrenceSpecification) {
			// Validate the execution specification, also (if any)
			result &= validateExecutionSpecification((OccurrenceSpecification) eObject, diagnostics, context);
		}

		if (result || diagnostics != null) {
			result &= super.doValidateContents(eObject, diagnostics, context);
		}

		return result;
	}

	protected boolean validateMessage(MessageEnd messageEnd, DiagnosticChain diagnostics, Map<Object, Object> context) {
		Message message = messageEnd.getMessage();

		return (message == null) || validate(message, diagnostics, context);
	}

	protected boolean validateExecutionSpecification(OccurrenceSpecification occurrence, DiagnosticChain diagnostics, Map<Object, Object> context) {
		ExecutionSpecification execution = OccurrenceSpecificationHelper.findExecutionWith(occurrence, true);
		if (execution == null) {
			// Maybe it's finished by this occurrence
			execution = OccurrenceSpecificationHelper.findExecutionWith(occurrence, false);
		}

		return (execution == null) || validate(execution, diagnostics, context);
	}

	@Override
	public boolean validate(EObject eObject, DiagnosticChain diagnostics, Map<Object, Object> context) {
		// Avoid redundant validation of things in operands that we might also validate
		// even when they aren't contained by those operands (per the above)
		if (context != null && (eObject instanceof Message || eObject instanceof ExecutionSpecification)) {
			@SuppressWarnings("unchecked")
			Set<EObject> validated = (Set<EObject>) context.get("operand.validated");
			if (validated == null) {
				validated = new HashSet<>();
				context.put("operand.validated", validated);
			}
			if (!validated.add(eObject)) {
				// Already checked it
				return true;
			}
		}

		return super.validate(eObject, diagnostics, context);
	}
}
