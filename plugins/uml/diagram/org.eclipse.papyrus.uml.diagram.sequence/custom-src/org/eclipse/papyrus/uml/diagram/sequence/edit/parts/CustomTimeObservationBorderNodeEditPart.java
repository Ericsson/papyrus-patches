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

import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.TimeObservation;

/**
 * Custom edit-part for {@code TimeObservation} as a border node.
 */
public class CustomTimeObservationBorderNodeEditPart extends TimeObservationBorderNodeEditPart implements ITimeElementBorderNodeEditPart {

	private final TimeElementEditPartHelper helper;

	/**
	 * Initializes me with my view model.
	 *
	 * @param view
	 *            my view model
	 */
	public CustomTimeObservationBorderNodeEditPart(View view) {
		super(view);

		helper = new TimeElementEditPartHelper(this, this::getMessageEnd);
	}

	@Override
	protected void refreshBounds() {
		if (!helper.refreshBounds(getBorderItemLocator())) {
			super.refreshBounds();
		}
	}

	@Override
	public Optional<MessageEnd> getMessageEnd() {
		return Optional.of(resolveSemanticElement())
				.filter(TimeObservation.class::isInstance)
				.map(TimeObservation.class::cast)
				.map(TimeObservation::getEvent)
				.filter(MessageEnd.class::isInstance)
				.map(MessageEnd.class::cast);
	}

}
