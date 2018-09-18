/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.helpers.advice;

import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationConstraintLinkEditPart;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * <p>
 * A sequence diagram advice to clear {@link DurationConstraintLinkEditPart DurationConstraint links} when the
 * constraint's constrained elements are changed.
 * </p>
 */
public class DurationConstraintLinkAdvice extends AbstractDurationLinkAdvice {

	public DurationConstraintLinkAdvice() {
		super(UMLPackage.Literals.DURATION_CONSTRAINT, UMLPackage.Literals.CONSTRAINT__CONSTRAINED_ELEMENT, DurationConstraintLinkEditPart.VISUAL_ID);
	}

}
