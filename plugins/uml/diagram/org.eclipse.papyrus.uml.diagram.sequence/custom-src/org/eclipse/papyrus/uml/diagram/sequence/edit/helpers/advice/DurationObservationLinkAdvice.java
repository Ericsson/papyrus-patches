/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.helpers.advice;

import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationObservationLinkEditPart;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * <p>
 * A sequence diagram advice to clear {@link DurationObservationLinkEditPart DurationObservation links} when the
 * observation's events are changed.
 * </p>
 */
public class DurationObservationLinkAdvice extends AbstractDurationLinkAdvice {

	public DurationObservationLinkAdvice() {
		super(UMLPackage.Literals.DURATION_OBSERVATION, UMLPackage.Literals.DURATION_OBSERVATION__EVENT, DurationObservationLinkEditPart.VISUAL_ID);
	}

}
