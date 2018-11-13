/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.uml2.uml.InteractionOperatorKind;

public class InteractionOperatorKindCompatibleMapping {

	/**
	 * Set the true if the kind of InteractionOperator supports multi operand
	 */
	private static final Map<InteractionOperatorKind, Boolean> map = new HashMap<>();
	static {
		map.put(InteractionOperatorKind.SEQ_LITERAL, true);
		map.put(InteractionOperatorKind.ALT_LITERAL, true);
		map.put(InteractionOperatorKind.OPT_LITERAL, false);
		map.put(InteractionOperatorKind.BREAK_LITERAL, false);
		map.put(InteractionOperatorKind.PAR_LITERAL, true);
		map.put(InteractionOperatorKind.STRICT_LITERAL, true);
		map.put(InteractionOperatorKind.LOOP_LITERAL, false);
		map.put(InteractionOperatorKind.CRITICAL_LITERAL, true);
		map.put(InteractionOperatorKind.NEG_LITERAL, false);
		map.put(InteractionOperatorKind.ASSERT_LITERAL, true);
		map.put(InteractionOperatorKind.IGNORE_LITERAL, true);
		map.put(InteractionOperatorKind.CONSIDER_LITERAL, true);
	}

	/**
	 * Check if the InteractionOperator supports multi operand
	 *
	 * @param kind
	 * @return
	 */
	public static boolean supportMultiOperand(InteractionOperatorKind kind) {
		Boolean multiOperandSupport = map.get(kind);
		return multiOperandSupport == null ? false : multiOperandSupport;
	}
}
