/**
 * Copyright (c) 2016 CEA LIST.
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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.service.visualtype.AbstractVisualTypeProvider;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry;

/**
 * @generated
 */
public class UMLVisualTypeProvider extends AbstractVisualTypeProvider {

	/**
	 * @generated
	 */
	public UMLVisualTypeProvider() {
		super();
	}

	/**
	 * @generated
	 */
	@Override
	public IElementType getElementType(Diagram diagram, String viewType) {
		IElementType result = null;

		try {
			result = UMLElementTypes.getElementType(viewType);
		} catch (NumberFormatException e) {
			// Not supported by this diagram
		}

		return result;
	}

	/**
	 * @generated
	 */
	@Override
	public String getNodeType(View parentView, EObject element) {
		return UMLVisualIDRegistry.getNodeVisualID(parentView, element);
	}

	/**
	 * @generated
	 */
	@Override
	public String getLinkType(Diagram diagram, EObject element) {
		return UMLVisualIDRegistry.getLinkWithClassVisualID(element);
	}
}
