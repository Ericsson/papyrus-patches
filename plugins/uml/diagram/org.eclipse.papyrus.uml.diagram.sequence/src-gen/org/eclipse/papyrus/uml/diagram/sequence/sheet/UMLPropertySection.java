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
package org.eclipse.papyrus.uml.diagram.sequence.sheet;

import org.eclipse.papyrus.infra.gmfdiag.tooling.runtime.sheet.DefaultPropertySection;
import org.eclipse.ui.views.properties.IPropertySourceProvider;

/**
 * @generated
 */
public class UMLPropertySection extends DefaultPropertySection implements IPropertySourceProvider {

	/**
	 * Modify/unwrap selection.
	 *
	 * @generated
	 */
	@Override
	protected Object transformSelection(Object selected) {
		selected = /* super. */transformSelectionToDomain(selected);
		return selected;
	}

}
