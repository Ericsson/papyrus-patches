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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.infra.services.validation.IValidationFilter;
import org.eclipse.uml2.uml.InteractionOperand;

/**
 * A validation filter that matches {@link InteractionOperand}s.
 */
public class OperandFilter implements IValidationFilter {

	/**
	 * Initializes me.
	 */
	public OperandFilter() {
		super();
	}

	@Override
	public boolean isApplicable(EObject element) {
		return element instanceof InteractionOperand;
	}

}
