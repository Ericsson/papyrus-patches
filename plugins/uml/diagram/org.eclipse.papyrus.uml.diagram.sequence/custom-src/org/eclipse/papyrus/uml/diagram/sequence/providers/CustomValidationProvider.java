/*****************************************************************************
 * Copyright (c) 2010 CEA
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Soyatec - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.providers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.AbstractModelConstraint;
import org.eclipse.emf.validation.IValidationContext;
import org.eclipse.emf.validation.model.IClientSelector;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.BehaviorExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CCombinedCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionOperandEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageAsyncEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageDeleteEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageFoundEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageLostEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageReplyEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageSyncEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.SequenceDiagramEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry;
import org.eclipse.papyrus.uml.diagram.sequence.validation.UMLValidationHelper;

public class CustomValidationProvider extends UMLValidationProvider {

	public CustomValidationProvider() {
	}

	public static class Ctx_3001 implements IClientSelector {

		/**
		 * select all moved edit parts which are linked to an occurrence specification
		 */
		@Override
		public boolean selects(Object object) {
			if (object instanceof Bounds) {
				// validate on resize or move
				object = ((Bounds) object).eContainer();
			}
			if (object instanceof View && SequenceDiagramEditPart.MODEL_ID.equals(UMLVisualIDRegistry.getModelID((View) object))) {
				final String id = UMLVisualIDRegistry.getVisualID((View) object);
				boolean result = false;
				// Lifeline
				result = result || LifelineEditPart.VISUAL_ID.equals(id);
				// ES
				result = result || CCombinedCompartmentEditPart.VISUAL_ID.equals(id);
				result = result || BehaviorExecutionSpecificationEditPart.VISUAL_ID.equals(id);
				// CF and Interaction operands
				result = result || CombinedFragmentEditPart.VISUAL_ID.equals(id);
				result = result || InteractionOperandEditPart.VISUAL_ID.equals(id);
				// Time related : do nothing, the real event support will be also moved
				// result = result || TimeConstraintEditPart.VISUAL_ID.equals(id);
				// result = result || TimeObservationEditPart.VISUAL_ID.equals(id);
				// result = result || DurationConstraintEditPart.VISUAL_ID.equals(id);
				// result = result || DurationObservationEditPart.VISUAL_ID.equals(id);
				// Messages
				result = result || MessageSyncEditPart.VISUAL_ID.equals(id);
				result = result || MessageAsyncEditPart.VISUAL_ID.equals(id);
				result = result || MessageReplyEditPart.VISUAL_ID.equals(id);
				result = result || MessageCreateEditPart.VISUAL_ID.equals(id);
				result = result || MessageDeleteEditPart.VISUAL_ID.equals(id);
				result = result || MessageLostEditPart.VISUAL_ID.equals(id);
				result = result || MessageFoundEditPart.VISUAL_ID.equals(id);
				// General Ordering : do nothing, the real event support will be also moved
				// result = result || GeneralOrderingEditPart.VISUAL_ID.equals(id);
				return result;
			}
			return false;
		}
	}

	public static class Adapter3 extends AbstractModelConstraint {

		/**
		 * do not presume on target type
		 */
		@Override
		public IStatus validate(IValidationContext ctx) {
			// do not presume on target type
			EObject target = ctx.getTarget();
			return UMLValidationHelper.validateFragmentsOrder(target, ctx);
		}
	}
}
