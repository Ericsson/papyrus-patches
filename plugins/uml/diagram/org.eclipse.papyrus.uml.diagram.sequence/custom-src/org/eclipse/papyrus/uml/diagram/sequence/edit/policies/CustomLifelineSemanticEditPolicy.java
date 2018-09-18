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

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.Arrays;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.CustomDefaultSemanticEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.DisplayEvent;
import org.eclipse.papyrus.uml.service.types.element.UMLElementTypes;
import org.eclipse.papyrus.uml.service.types.utils.ElementUtil;
import org.eclipse.papyrus.uml.service.types.utils.SequenceRequestConstant;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.TimeConstraint;

/**
 * Custom semantic edit-policy for lifelines that manages configuration of
 * {@#link TimeObservation} and {@link TimeConstraint} border items.
 */
public class CustomLifelineSemanticEditPolicy extends CustomDefaultSemanticEditPolicy {

	private DisplayEvent displayEvent;

	/**
	 * Initializes me.
	 */
	public CustomLifelineSemanticEditPolicy() {
		super();
	}

	@Override
	public void setHost(EditPart host) {
		super.setHost(host);
		displayEvent = new DisplayEvent(host);
	}

	@Override
	protected Command getCreateCommand(CreateElementRequest req) {
		if (ElementUtil.isTypeOf(req.getElementType(), UMLElementTypes.TIME_CONSTRAINT)
				|| ElementUtil.isTypeOf(req.getElementType(), UMLElementTypes.TIME_OBSERVATION)) {

			Object loc = req.getParameter("initialMouseLocationForCreation");
			if ((loc instanceof Point) && (getHost() instanceof IGraphicalEditPart)) {
				// Is a message end here?
				MessageOccurrenceSpecification messageOcc = displayEvent.getMessageEvent(((IGraphicalEditPart) getHost()).getFigure(), (Point) loc);
				if (messageOcc != null) {
					req.setParameter(SequenceRequestConstant.NEAREST_OCCURRENCE_SPECIFICATION, Arrays.asList(messageOcc));
				} else {
					// Cannot create this on a lifeline without the message end to constrain
					// or observe
					return UnexecutableCommand.INSTANCE;
				}
			}
		}
		return super.getCreateCommand(req);
	}

}
