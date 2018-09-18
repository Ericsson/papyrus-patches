/*****************************************************************************
 * Copyright (c) 2010, 2018 CEA List, EclipseSource, Christian W. Damus, and others
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
 *   EclipseSource - Bug 536641
 *   Christian W. Damus - bug 536486
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.providers;

import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.SilentEditpart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.BehaviorExecutionSpecificationBehaviorEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CCombinedFragmentCombinedFragmentCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CCombinedFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CDestructionOccurrenceSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CInteractionEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CInteractionInteractionCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CInteractionOperandEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CLifeLineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentCombinedFragmentCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ConsiderIgnoreFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomDurationConstraintLinkEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomDurationObservationLinkEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomGeneralOrderingEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomMessageName2EditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomMessageName3EditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomMessageName4EditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomMessageName5EditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomMessageName6EditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomMessageName7EditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomMessageNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomStateInvariantEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomStateInvariantLabelEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomTimeConstraintBorderNodeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomTimeObservationBorderNodeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DestructionOccurrenceSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationConstraintLinkEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationObservationLinkEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.GeneralOrderingEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionOperandEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionOperandGuardEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageAsyncNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageDeleteNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageFoundNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageLostNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageReplyNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageSyncNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.StateInvariantEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.StateInvariantLabelEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeConstraintBorderNodeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeObservationBorderNodeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.GrillingEditpart;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomEditPartProvider extends UMLEditPartProvider {

	@Override
	protected IGraphicalEditPart createEditPart(View view) {
		IGraphicalEditPart customEditPart = createCustomEditPart(view);
		if (customEditPart != null) {
			return customEditPart;
		}
		IGraphicalEditPart graphicalEditPart = super.createEditPart(view);
		if (graphicalEditPart == null) {
			return new SilentEditpart(view);
		}
		return graphicalEditPart;
	}

	protected IGraphicalEditPart createCustomEditPart(View view) {
		if (InteractionOperandGuardEditPart.GUARD_TYPE.equals(view.getType())) {
			return new InteractionOperandGuardEditPart(view);
		} else if (BehaviorExecutionSpecificationBehaviorEditPart.BEHAVIOR_TYPE.equals(view.getType())) {
			return new BehaviorExecutionSpecificationBehaviorEditPart(view);
		}
		switch (UMLVisualIDRegistry.getVisualID(view)) {
		// case SequenceDiagramEditPart.VISUAL_ID:
		// return new OLDPackageEditPart(view);
		case InteractionEditPart.VISUAL_ID:
			return new CInteractionEditPart(view);
		case GrillingEditpart.VISUAL_ID:
			return new GrillingEditpart(view);
		// case InteractionNameEditPart.VISUAL_ID:
		// return new CustomInteractionNameEditPart(view);
		case ConsiderIgnoreFragmentEditPart.VISUAL_ID:
			return new CCombinedFragmentEditPart(view);
		case CombinedFragmentEditPart.VISUAL_ID:
			return new CCombinedFragmentEditPart(view);
		case CombinedFragmentCombinedFragmentCompartmentEditPart.VISUAL_ID:
			return new CCombinedFragmentCombinedFragmentCompartmentEditPart(view);

		case InteractionOperandEditPart.VISUAL_ID:
			return new CInteractionOperandEditPart(view);
		// case InteractionUseEditPart.VISUAL_ID:
		// return new CustomInteractionUseEditPart(view);
		// case InteractionUseNameEditPart.VISUAL_ID:
		// return new CustomInteractionUseNameEditPart(view);
		// case InteractionUseName2EditPart.VISUAL_ID:
		// return new CustomInteractionUseName2EditPart(view);
		// case ContinuationEditPart.VISUAL_ID:
		// return new CustomContinuationEditPart(view);
		case LifelineEditPart.VISUAL_ID:
			return new CLifeLineEditPart(view);
		// case LifelineNameEditPart.VISUAL_ID:
		// return new CustomLifelineNameEditPart(view);
		// case ActionExecutionSpecificationEditPart.VISUAL_ID:
		// return new CustomActionExecutionSpecificationEditPart(view);
		// case BehaviorExecutionSpecificationEditPart.VISUAL_ID:
		// return new CustomBehaviorExecutionSpecificationEditPart(view);
		case StateInvariantEditPart.VISUAL_ID:
			return new CustomStateInvariantEditPart(view);
		// case CombinedFragment2EditPart.VISUAL_ID:
		// return new CustomCombinedFragment2EditPart(view);
		case TimeConstraintBorderNodeEditPart.VISUAL_ID:
			return new CustomTimeConstraintBorderNodeEditPart(view);
		// case TimeConstraintAppliedStereotypeEditPart.VISUAL_ID:
		// return new CustomTimeConstraintAppliedStereotypeEditPart(view);
		// case TimeConstraintLabelEditPart.VISUAL_ID:
		// return new CustomTimeConstraintLabelEditPart(view);
		case TimeObservationBorderNodeEditPart.VISUAL_ID:
			return new CustomTimeObservationBorderNodeEditPart(view);
		// case TimeObservationLabelEditPart.VISUAL_ID:
		// return new CustomTimeObservationLabelEditPart(view);
		// case TimeObservationAppliedStereotypeEditPart.VISUAL_ID:
		// return new CustomTimeObservationAppliedStereotypeEditPart(view);
		// case DurationConstraintAppliedStereotypeEditPart.VISUAL_ID:
		// return new CustomDurationConstraintAppliedStereotypeEditPart(view);
		case DestructionOccurrenceSpecificationEditPart.VISUAL_ID:
			return new CDestructionOccurrenceSpecificationEditPart(view);
		// case ConstraintEditPart.VISUAL_ID:
		// return new CustomConstraintEditPart(view);
		// case Constraint2EditPart.VISUAL_ID:
		// return new CustomConstraint2EditPart(view);
		// case CommentEditPart.VISUAL_ID:
		// return new CustomCommentEditPart(view);
		// case CommentBodyEditPart.VISUAL_ID:
		// return new CustomCommentBodyEditPart(view);
		// case DurationConstraintInMessageEditPart.VISUAL_ID:
		// return new CustomDurationConstraintInMessageEditPart(view);
		// case DurationConstraintInMessageAppliedStereotypeEditPart.VISUAL_ID:
		// return new CustomDurationConstraintInMessageAppliedStereotypeEditPart(view);
		case InteractionInteractionCompartmentEditPart.VISUAL_ID:
			return new CInteractionInteractionCompartmentEditPart(view);
		// case CombinedFragmentCombinedFragmentCompartmentEditPart.VISUAL_ID:
		// return new CustomCombinedFragmentCombinedFragmentCompartmentEditPart(view);
		// case MessageSyncEditPart.VISUAL_ID:
		// return new CustomMessageSyncEditPart(view);
		case MessageSyncNameEditPart.VISUAL_ID:
			return new CustomMessageNameEditPart(view);
		// case MessageAsyncEditPart.VISUAL_ID:
		// return new CustomMessageAsyncEditPart(view);
		case MessageAsyncNameEditPart.VISUAL_ID:
			return new CustomMessageName2EditPart(view);
		// case MessageReplyEditPart.VISUAL_ID:
		// return new CustomMessageReplyEditPart(view);
		case MessageReplyNameEditPart.VISUAL_ID:
			return new CustomMessageName3EditPart(view);
		// case MessageCreateEditPart.VISUAL_ID:
		// return new CustomMessageCreateEditPart(view);
		case MessageCreateNameEditPart.VISUAL_ID:
			return new CustomMessageName4EditPart(view);
		// case MessageDeleteEditPart.VISUAL_ID:
		// return new CustomMessageDeleteEditPart(view);
		case MessageDeleteNameEditPart.VISUAL_ID:
			return new CustomMessageName5EditPart(view);
		// case MessageLostEditPart.VISUAL_ID:
		// return new CustomMessageLostEditPart(view);
		case MessageLostNameEditPart.VISUAL_ID:
			return new CustomMessageName6EditPart(view);
		// case MessageFoundEditPart.VISUAL_ID:
		// return new CustomMessageFoundEditPart(view);
		case MessageFoundNameEditPart.VISUAL_ID:
			return new CustomMessageName7EditPart(view);
		// case CommentAnnotatedElementEditPart.VISUAL_ID:
		// return new CustomCommentAnnotatedElementEditPart(view);
		case GeneralOrderingEditPart.VISUAL_ID:
			return new CustomGeneralOrderingEditPart(view);
		// case ExecutionSpecificationEndEditPart.VISUAL_ID:
		// return new ExecutionSpecificationEndEditPart(view);
		// case MessageEndEditPart.VISUAL_ID:
		// return new MessageEndEditPart(view);
		case StateInvariantLabelEditPart.VISUAL_ID:
			return new CustomStateInvariantLabelEditPart(view);
		case DurationConstraintLinkEditPart.VISUAL_ID:
			return new CustomDurationConstraintLinkEditPart(view);
		case DurationObservationLinkEditPart.VISUAL_ID:
			return new CustomDurationObservationLinkEditPart(view);
		}
		return null;
	}
}
