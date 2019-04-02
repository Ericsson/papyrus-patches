/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource France - Initial API and implementation
 *   Christian W. Damus - bug 536486
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.Arrays;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.papyrus.uml.diagram.sequence.util.DurationLinkUtil;
import org.eclipse.papyrus.uml.service.types.element.UMLElementTypes;
import org.eclipse.papyrus.uml.service.types.utils.ElementUtil;
import org.eclipse.papyrus.uml.service.types.utils.SequenceRequestConstant;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.TimeConstraint;

/**
 * Specific policy to set the contrained element for {@link TimeConstraint}.
 */
public class CustomExecutionSpecificationSemanticEditPolicy extends OccurenceSemanticEditPolicy {

	@Override
	protected Command getCreateCommand(CreateElementRequest req) {
		if (ElementUtil.isTypeOf(req.getElementType(), UMLElementTypes.TIME_CONSTRAINT)
				|| ElementUtil.isTypeOf(req.getElementType(), UMLElementTypes.TIME_OBSERVATION)) {

			Object loc = req.getParameter("initialMouseLocationForCreation");
			// evaluate parameters
			if (!Point.class.isInstance(loc)
					|| !IGraphicalEditPart.class.isInstance(getHost())
					|| !ExecutionSpecification.class.isInstance(req.getContainer())) {
				return super.getCreateCommand(req);
			}

			boolean isStart = DurationLinkUtil.isStart(((IGraphicalEditPart) getHost()).getFigure(), Point.class.cast(loc));
			if (isStart) {
				req.setParameter(SequenceRequestConstant.NEAREST_OCCURRENCE_SPECIFICATION, Arrays.asList(((ExecutionSpecification) req.getContainer()).getStart()));
			} else {
				req.setParameter(SequenceRequestConstant.NEAREST_OCCURRENCE_SPECIFICATION, Arrays.asList(((ExecutionSpecification) req.getContainer()).getFinish()));
			}

		}
		return super.getCreateCommand(req);
	}


}
