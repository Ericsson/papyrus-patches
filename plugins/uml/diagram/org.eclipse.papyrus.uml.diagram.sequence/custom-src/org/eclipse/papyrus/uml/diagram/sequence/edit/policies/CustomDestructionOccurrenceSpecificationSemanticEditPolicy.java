/*****************************************************************************
 * Copyright (c) 2018 Christian W. Damus, CEA LIST, and others.
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
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import static java.util.Arrays.asList;
import static org.eclipse.papyrus.uml.service.types.utils.SequenceRequestConstant.NEAREST_OCCURRENCE_SPECIFICATION;

import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultSemanticEditPolicy;
import org.eclipse.papyrus.uml.service.types.element.UMLElementTypes;
import org.eclipse.papyrus.uml.service.types.utils.ElementUtil;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.TimeConstraint;
import org.eclipse.uml2.uml.TimeObservation;

/**
 * Custom semantic edit policy for configuration of the constrained or observed element in a
 * {@link TimeConstraint} or {@link TimeObservation}, respectively.
 */
public class CustomDestructionOccurrenceSpecificationSemanticEditPolicy extends DefaultSemanticEditPolicy {

	@Override
	protected Command getCreateCommand(CreateElementRequest req) {
		if (ElementUtil.isTypeOf(req.getElementType(), UMLElementTypes.TIME_CONSTRAINT)
				|| ElementUtil.isTypeOf(req.getElementType(), UMLElementTypes.TIME_OBSERVATION)) {

			if (!(getHost() instanceof IGraphicalEditPart) || !(req.getContainer() instanceof DestructionOccurrenceSpecification)) {
				return super.getCreateCommand(req);
			}

			req.setParameter(NEAREST_OCCURRENCE_SPECIFICATION, asList(req.getContainer()));
		}
		return super.getCreateCommand(req);
	}


}
