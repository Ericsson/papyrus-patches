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
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.uml2.uml.StateInvariant;

public class CustomStateInvariantLabelEditPart extends StateInvariantLabelEditPart {

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CustomStateInvariantLabelEditPart(View view) {
		super(view);
	}

	@Override
	protected EObject getParserElement() {
		EObject element = resolveSemanticElement();
		if (element instanceof StateInvariant) {
			return ((StateInvariant) element).getInvariant();
		}
		return element;
	}
}
