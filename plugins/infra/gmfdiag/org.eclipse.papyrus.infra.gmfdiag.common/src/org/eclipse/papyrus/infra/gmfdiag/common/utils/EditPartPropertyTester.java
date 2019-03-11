/*****************************************************************************
 * Copyright (c) 2015 Christian W. Damus and others.
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

package org.eclipse.papyrus.infra.gmfdiag.common.utils;

import static org.eclipse.papyrus.infra.tools.util.TypeUtils.as;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.gef.EditPart;

/**
 * A tester for core-expressions {@link EditPart} properties.
 */
public class EditPartPropertyTester extends PropertyTester {
	public static final String PROPERTY_HAS_EDIT_POLICY = "hasEditPolicy"; //$NON-NLS-1$

	public EditPartPropertyTester() {
		super();
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		boolean result = false;

		if (PROPERTY_HAS_EDIT_POLICY.equals(property)) {
			result = hasEditPolicy(as(receiver, EditPart.class), as(args, 0, String.class)) == defaultTrue(expectedValue);
		}

		return result;
	}

	private static boolean defaultTrue(Object value) {
		return as(value, true);
	}

	static boolean hasEditPolicy(EditPart editPart, String role) {
		return (editPart != null) && (role != null) && (editPart.getEditPolicy(role) != null);
	}
}
