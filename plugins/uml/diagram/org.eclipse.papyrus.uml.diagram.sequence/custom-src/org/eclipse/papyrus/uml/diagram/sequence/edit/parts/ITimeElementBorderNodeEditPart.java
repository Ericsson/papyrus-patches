/*****************************************************************************
 * Copyright (c) 2018 Christian W. Damus, CEA LIST, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import java.util.Optional;

import org.eclipse.gmf.runtime.diagram.ui.editparts.IBorderItemEditPart;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.TimeConstraint;
import org.eclipse.uml2.uml.TimeObservation;

/**
 * Protocol for the border-item edit parts controlling the presentation of
 * {@link TimeObservation}s and {@link TimeConstraint}s.
 */
public interface ITimeElementBorderNodeEditPart extends IBorderItemEditPart {

	/**
	 * Obtain the message end that is my time event, if any.
	 *
	 * @return my message end, or {@code null} if I do not observe or constrain a message end
	 */
	Optional<MessageEnd> getMessageEnd();
}
