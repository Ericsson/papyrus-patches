/*****************************************************************************
 * Copyright (c) 2015 CEA LIST and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Celine JANSSENS (ALL4TEC) celine.janssens@all4tec.net  - Initial API and implementation
 *   Celine JANSSENS (ALL4TEC) celine.janssens@all4tec.net - Bug 455311 Stereotype Display
 *   
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.css.helper;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.notation.DecorationNode;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.papyrus.infra.gmfdiag.css.helper.CSSDOMSemanticElementHelper;
import org.eclipse.papyrus.uml.diagram.common.stereotype.display.helper.StereotypeDisplayUtil;

/**
 * This class is a helper for retrieving view and semantic element from a compatible object related to UML.
 * 
 * @author Céline JANSSENS
 *
 */
public class CSSDOMUMLSemanticElementHelper extends CSSDOMSemanticElementHelper {


	/**
	 * singleton instance
	 */
	private static CSSDOMUMLSemanticElementHelper elementHelper;

	/** Private Constructor. */
	protected CSSDOMUMLSemanticElementHelper() {
		super();
	}

	/**
	 * Returns the singleton instance of this class
	 *
	 * @return the singleton instance.
	 */
	public static CSSDOMUMLSemanticElementHelper getInstance() {
		if (elementHelper == null) {
			elementHelper = new CSSDOMUMLSemanticElementHelper();
		}
		return elementHelper;
	}



	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.css.helper.CSSDOMSemanticElementHelper#findSemanticElement(org.eclipse.emf.ecore.EObject)
	 *
	 * @param notationElement
	 * @return
	 */
	@Override
	public EObject findSemanticElement(EObject notationElement) {


		StereotypeDisplayUtil stereotypeHelper = StereotypeDisplayUtil.getInstance();

		// Add Stereotype Comment
		if (notationElement instanceof Shape) {
			if (stereotypeHelper.isStereotypeComment(notationElement)) {
				return notationElement;
			}
		}

		// Add Stereotype Label
		if (notationElement instanceof DecorationNode) {
			if (stereotypeHelper.isStereotypeLabel(notationElement)) {
				return notationElement;
			}
		}



		// Add StereotypeProperty to the DOM model
		if (notationElement instanceof DecorationNode) {
			if (stereotypeHelper.isStereotypeProperty(notationElement)
					|| stereotypeHelper.isStereotypeBraceProperty(notationElement)) {
				return notationElement;
			}

		}

		return super.findSemanticElement(notationElement);
	}

}
