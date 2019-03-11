/*****************************************************************************
 * Copyright (c) 2012, 2016 CEA LIST, Christian W. Damus, and others
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
 *   Celine Janssens (ALL4TEC) celine.janssens@all4tec.net - Bug 455311
 *   Christian W. Damus - bug 485220
 *   
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.css.helper;

import java.util.function.Predicate;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.gmfdiag.css.spi.IStylingProvider;
import org.eclipse.papyrus.uml.diagram.common.stereotype.display.helper.StereotypeDisplayConstant;
import org.eclipse.papyrus.uml.diagram.common.stereotype.display.helper.StereotypeDisplayUtil;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * UML-specific CSS styling provider.
 */
public class UMLStylingProvider implements IStylingProvider {

	public UMLStylingProvider() {
		super();
	}

	@Override
	public void resetStyle(View view) {
		// Remove our annotation, if any
		EAnnotation annotation = view.getEAnnotation(StereotypeDisplayConstant.STEREOTYPE_LABEL_DEPTH);
		if (annotation != null) {
			view.getEAnnotations().remove(annotation);
		}

		// Unset persistency, if possible
		StereotypeDisplayUtil helper = StereotypeDisplayUtil.getInstance();

		if (helper.isStereotypeView(view)) {
			TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(view);
			if (domain != null) {
				helper.unsetPersistency(domain, view);
			}
		}
	}

	@Override
	public Iterable<EClass> getSupportedSemanticClasses() {
		return () -> UMLPackage.eINSTANCE.getEClassifiers().stream()
				.filter(EClass.class::isInstance)
				.map(EClass.class::cast)
				.iterator();
	}

	@Override
	public Predicate<EStructuralFeature> getSemanticPropertySupportedPredicate() {
		// All attributes are supported, plus references to named elements of any kind
		return feature -> (feature instanceof EAttribute)
				|| ((feature instanceof EReference)
						&& EMFHelper.isSubclass(((EReference) feature).getEReferenceType(), UMLPackage.Literals.NAMED_ELEMENT));
	}
}
