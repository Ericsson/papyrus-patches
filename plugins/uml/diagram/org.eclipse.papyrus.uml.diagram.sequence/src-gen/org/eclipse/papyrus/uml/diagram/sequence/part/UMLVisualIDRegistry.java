/*****************************************************************************
 * Copyright (c) 2009, 2018 Atos Origin, Christian W. Damus, CEA LIST, and others.
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
 *   Atos Origin - Initial API and implementation
 *   Christian W. Damus - bug 536486
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.part;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.structure.DiagramStructure;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ActionExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.BehaviorExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentCombinedFragmentCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CommentBodyEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CommentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ConsiderIgnoreFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.Constraint2EditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ConstraintContextAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ConstraintEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ConstraintNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ContextLinkEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ContinuationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ContinuationNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DestructionOccurrenceSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationConstraintLinkAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationConstraintLinkEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationConstraintLinkNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationObservationLinkAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationObservationLinkEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationObservationLinkNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.GateEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.GateNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.GeneralOrderingAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.GeneralOrderingEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionOperandEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionUseEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionUseName2EditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionUseNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageAsyncAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageAsyncEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageAsyncNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageDeleteAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageDeleteEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageDeleteNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageFoundAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageFoundEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageFoundNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageLostAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageLostEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageLostNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageReplyAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageReplyEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageReplyNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageSyncAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageSyncEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageSyncNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.SequenceDiagramEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.StateInvariantEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.StateInvariantLabelEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.StateInvariantNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeConstraintAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeConstraintBorderNodeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeConstraintNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeObservationAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeObservationBorderNodeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeObservationNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.expressions.UMLOCLFactory;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * This registry is used to determine which type of visual object should be created for the
 * corresponding Diagram, Node, ChildNode or Link represented by a domain model object.
 *
 * @generated
 */
public class UMLVisualIDRegistry {

	/**
	 * @generated
	 */
	private static final String DEBUG_KEY = "org.eclipse.papyrus.uml.diagram.sequence/debug/visualID"; //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static String getVisualID(View view) {
		if (view instanceof Diagram) {
			if (SequenceDiagramEditPart.MODEL_ID.equals(view.getType())) {
				return SequenceDiagramEditPart.VISUAL_ID;
			} else {
				return "";
			}
		}
		return org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry.getVisualID(view.getType());
	}

	/**
	 * @generated
	 */
	public static String getModelID(View view) {
		View diagram = view.getDiagram();
		while (view != diagram) {
			EAnnotation annotation = view.getEAnnotation("Shortcut"); //$NON-NLS-1$
			if (annotation != null) {
				return annotation.getDetails().get("modelID"); //$NON-NLS-1$
			}
			view = (View) view.eContainer();
		}
		return diagram != null ? diagram.getType() : null;
	}

	/**
	 * @generated
	 */
	public static String getVisualID(String type) {
		return type;
	}

	/**
	 * @generated
	 */
	public static String getType(String visualID) {
		return visualID;
	}

	/**
	 * @generated
	 */
	public static String getDiagramVisualID(EObject domainElement) {
		if (domainElement == null) {
			return "";
		}
		return SequenceDiagramEditPart.VISUAL_ID;
	}

	/**
	 * Generated not for add lifelines on lifeline
	 * installEditPolicy(EditPolicyRoles.DRAG_DROP_ROLE, new CustomDiagramDragDropEditPolicy());
	 *
	 * @generated
	 */
	public static String getNodeVisualID(View containerView, EObject domainElement) {
		if (domainElement == null) {
			return "";
		}
		String containerModelID = org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry
				.getModelID(containerView);
		if (!SequenceDiagramEditPart.MODEL_ID.equals(containerModelID)) {
			return "";
		}
		String containerVisualID;
		if (SequenceDiagramEditPart.MODEL_ID.equals(containerModelID)) {
			containerVisualID = org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry
					.getVisualID(containerView);
		} else {
			if (containerView instanceof Diagram) {
				containerVisualID = SequenceDiagramEditPart.VISUAL_ID;
			} else {
				return "";
			}
		}
		if (containerVisualID != null) {
			switch (containerVisualID) {
			case SequenceDiagramEditPart.VISUAL_ID:
				if (UMLPackage.eINSTANCE.getInteraction().isSuperTypeOf(domainElement.eClass())) {
					return InteractionEditPart.VISUAL_ID;
				}
				break;
			case InteractionEditPart.VISUAL_ID:
				if (UMLPackage.eINSTANCE.getGate().isSuperTypeOf(domainElement.eClass())) {
					return GateEditPart.VISUAL_ID;
				}
				break;
			case CombinedFragmentEditPart.VISUAL_ID:
				if (UMLPackage.eINSTANCE.getGate().isSuperTypeOf(domainElement.eClass())) {
					return GateEditPart.VISUAL_ID;
				}
				break;
			case InteractionOperandEditPart.VISUAL_ID:
				if (UMLPackage.eINSTANCE.getInteractionUse().isSuperTypeOf(domainElement.eClass())) {
					return InteractionUseEditPart.VISUAL_ID;
				}
				if (UMLPackage.eINSTANCE.getConsiderIgnoreFragment().isSuperTypeOf(domainElement.eClass())) {
					return ConsiderIgnoreFragmentEditPart.VISUAL_ID;
				}
				if (UMLPackage.eINSTANCE.getCombinedFragment().isSuperTypeOf(domainElement.eClass())) {
					return CombinedFragmentEditPart.VISUAL_ID;
				}
				if (UMLPackage.eINSTANCE.getContinuation().isSuperTypeOf(domainElement.eClass())) {
					return ContinuationEditPart.VISUAL_ID;
				}
				break;
			case InteractionUseEditPart.VISUAL_ID:
				if (UMLPackage.eINSTANCE.getGate().isSuperTypeOf(domainElement.eClass())) {
					return GateEditPart.VISUAL_ID;
				}
				break;
			case LifelineEditPart.VISUAL_ID:
				if (UMLPackage.eINSTANCE.getActionExecutionSpecification().isSuperTypeOf(domainElement.eClass())) {
					return ActionExecutionSpecificationEditPart.VISUAL_ID;
				}
				if (UMLPackage.eINSTANCE.getBehaviorExecutionSpecification().isSuperTypeOf(domainElement.eClass())) {
					return BehaviorExecutionSpecificationEditPart.VISUAL_ID;
				}
				if (UMLPackage.eINSTANCE.getStateInvariant().isSuperTypeOf(domainElement.eClass())) {
					return StateInvariantEditPart.VISUAL_ID;
				}
				if (UMLPackage.eINSTANCE.getDestructionOccurrenceSpecification()
						.isSuperTypeOf(domainElement.eClass())) {
					return DestructionOccurrenceSpecificationEditPart.VISUAL_ID;
				}
				if (UMLPackage.eINSTANCE.getTimeConstraint().isSuperTypeOf(domainElement.eClass())) {
					return TimeConstraintBorderNodeEditPart.VISUAL_ID;
				}
				if (UMLPackage.eINSTANCE.getTimeObservation().isSuperTypeOf(domainElement.eClass())) {
					return TimeObservationBorderNodeEditPart.VISUAL_ID;
				}
				break;
			case ActionExecutionSpecificationEditPart.VISUAL_ID:
				if (UMLPackage.eINSTANCE.getTimeConstraint().isSuperTypeOf(domainElement.eClass())) {
					return TimeConstraintBorderNodeEditPart.VISUAL_ID;
				}
				if (UMLPackage.eINSTANCE.getTimeObservation().isSuperTypeOf(domainElement.eClass())) {
					return TimeObservationBorderNodeEditPart.VISUAL_ID;
				}
				break;
			case BehaviorExecutionSpecificationEditPart.VISUAL_ID:
				if (UMLPackage.eINSTANCE.getTimeConstraint().isSuperTypeOf(domainElement.eClass())) {
					return TimeConstraintBorderNodeEditPart.VISUAL_ID;
				}
				if (UMLPackage.eINSTANCE.getTimeObservation().isSuperTypeOf(domainElement.eClass())) {
					return TimeObservationBorderNodeEditPart.VISUAL_ID;
				}
				break;
			case DestructionOccurrenceSpecificationEditPart.VISUAL_ID:
				if (UMLPackage.eINSTANCE.getTimeConstraint().isSuperTypeOf(domainElement.eClass())) {
					return TimeConstraintBorderNodeEditPart.VISUAL_ID;
				}
				if (UMLPackage.eINSTANCE.getTimeObservation().isSuperTypeOf(domainElement.eClass())) {
					return TimeObservationBorderNodeEditPart.VISUAL_ID;
				}
				break;
			case InteractionInteractionCompartmentEditPart.VISUAL_ID:
				if (UMLPackage.eINSTANCE.getConsiderIgnoreFragment().isSuperTypeOf(domainElement.eClass())) {
					return ConsiderIgnoreFragmentEditPart.VISUAL_ID;
				}
				if (UMLPackage.eINSTANCE.getCombinedFragment().isSuperTypeOf(domainElement.eClass())) {
					return CombinedFragmentEditPart.VISUAL_ID;
				}
				if (UMLPackage.eINSTANCE.getLifeline().isSuperTypeOf(domainElement.eClass())) {
					return LifelineEditPart.VISUAL_ID;
				}
				if (UMLPackage.eINSTANCE.getInteractionUse().isSuperTypeOf(domainElement.eClass())) {
					return InteractionUseEditPart.VISUAL_ID;
				}
				if (UMLPackage.eINSTANCE.getConstraint().isSuperTypeOf(domainElement.eClass())) {
					return ConstraintEditPart.VISUAL_ID;
				}
				if (UMLPackage.eINSTANCE.getComment().isSuperTypeOf(domainElement.eClass())) {
					return CommentEditPart.VISUAL_ID;
				}
				break;
			case CombinedFragmentCombinedFragmentCompartmentEditPart.VISUAL_ID:
				if (UMLPackage.eINSTANCE.getInteractionOperand().isSuperTypeOf(domainElement.eClass())) {
					return InteractionOperandEditPart.VISUAL_ID;
				}
				break;
			}
		}
		return "";
	}

	/**
	 * @generated
	 */
	public static boolean canCreateNode(View containerView, String nodeVisualID) {
		String containerModelID = org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry
				.getModelID(containerView);
		if (!SequenceDiagramEditPart.MODEL_ID.equals(containerModelID)) {
			return false;
		}
		String containerVisualID;
		if (SequenceDiagramEditPart.MODEL_ID.equals(containerModelID)) {
			containerVisualID = org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry
					.getVisualID(containerView);
		} else {
			if (containerView instanceof Diagram) {
				containerVisualID = SequenceDiagramEditPart.VISUAL_ID;
			} else {
				return false;
			}
		}
		if (containerVisualID != null) {
			switch (containerVisualID) {
			case SequenceDiagramEditPart.VISUAL_ID:
				if (InteractionEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case InteractionEditPart.VISUAL_ID:
				if (InteractionNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (InteractionInteractionCompartmentEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (GateEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case CombinedFragmentEditPart.VISUAL_ID:
				if (CombinedFragmentCombinedFragmentCompartmentEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (GateEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case InteractionOperandEditPart.VISUAL_ID:
				if (InteractionUseEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (ConsiderIgnoreFragmentEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (CombinedFragmentEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (ContinuationEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case InteractionUseEditPart.VISUAL_ID:
				if (InteractionUseNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (InteractionUseName2EditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (GateEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case ContinuationEditPart.VISUAL_ID:
				if (ContinuationNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case LifelineEditPart.VISUAL_ID:
				if (LifelineNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (ActionExecutionSpecificationEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (BehaviorExecutionSpecificationEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (StateInvariantEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (DestructionOccurrenceSpecificationEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (TimeConstraintBorderNodeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (TimeObservationBorderNodeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case ActionExecutionSpecificationEditPart.VISUAL_ID:
				if (TimeConstraintBorderNodeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (TimeObservationBorderNodeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case BehaviorExecutionSpecificationEditPart.VISUAL_ID:
				if (TimeConstraintBorderNodeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (TimeObservationBorderNodeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case StateInvariantEditPart.VISUAL_ID:
				if (StateInvariantNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (StateInvariantLabelEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case DestructionOccurrenceSpecificationEditPart.VISUAL_ID:
				if (TimeConstraintBorderNodeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (TimeObservationBorderNodeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case ConstraintEditPart.VISUAL_ID:
				if (ConstraintNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (Constraint2EditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case CommentEditPart.VISUAL_ID:
				if (CommentBodyEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case GateEditPart.VISUAL_ID:
				if (GateNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case TimeConstraintBorderNodeEditPart.VISUAL_ID:
				if (TimeConstraintNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (TimeConstraintAppliedStereotypeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case TimeObservationBorderNodeEditPart.VISUAL_ID:
				if (TimeObservationNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (TimeObservationAppliedStereotypeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case InteractionInteractionCompartmentEditPart.VISUAL_ID:
				if (ConsiderIgnoreFragmentEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (CombinedFragmentEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (LifelineEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (InteractionUseEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (ConstraintEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (CommentEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case CombinedFragmentCombinedFragmentCompartmentEditPart.VISUAL_ID:
				if (InteractionOperandEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case MessageSyncEditPart.VISUAL_ID:
				if (MessageSyncNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (MessageSyncAppliedStereotypeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case MessageAsyncEditPart.VISUAL_ID:
				if (MessageAsyncNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (MessageAsyncAppliedStereotypeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case MessageReplyEditPart.VISUAL_ID:
				if (MessageReplyNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (MessageReplyAppliedStereotypeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case MessageCreateEditPart.VISUAL_ID:
				if (MessageCreateNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (MessageCreateAppliedStereotypeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case MessageDeleteEditPart.VISUAL_ID:
				if (MessageDeleteNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (MessageDeleteAppliedStereotypeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case MessageLostEditPart.VISUAL_ID:
				if (MessageLostNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (MessageLostAppliedStereotypeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case MessageFoundEditPart.VISUAL_ID:
				if (MessageFoundNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (MessageFoundAppliedStereotypeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case GeneralOrderingEditPart.VISUAL_ID:
				if (GeneralOrderingAppliedStereotypeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case ContextLinkEditPart.VISUAL_ID:
				if (ConstraintContextAppliedStereotypeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case DurationConstraintLinkEditPart.VISUAL_ID:
				if (DurationConstraintLinkNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (DurationConstraintLinkAppliedStereotypeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			case DurationObservationLinkEditPart.VISUAL_ID:
				if (DurationObservationLinkNameEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				if (DurationObservationLinkAppliedStereotypeEditPart.VISUAL_ID.equals(nodeVisualID)) {
					return true;
				}
				break;
			}
		}
		return false;
	}

	/**
	 * @generated
	 */
	public static String getLinkWithClassVisualID(EObject domainElement) {
		if (domainElement == null) {
			return "";
		}
		if (UMLPackage.eINSTANCE.getMessage().isSuperTypeOf(domainElement.eClass())
				&& isMessage_SynchEdge((Message) domainElement)) {
			return MessageSyncEditPart.VISUAL_ID;
		}
		if (UMLPackage.eINSTANCE.getMessage().isSuperTypeOf(domainElement.eClass())
				&& isMessage_AsynchEdge((Message) domainElement)) {
			return MessageAsyncEditPart.VISUAL_ID;
		}
		if (UMLPackage.eINSTANCE.getMessage().isSuperTypeOf(domainElement.eClass())
				&& isMessage_ReplyEdge((Message) domainElement)) {
			return MessageReplyEditPart.VISUAL_ID;
		}
		if (UMLPackage.eINSTANCE.getMessage().isSuperTypeOf(domainElement.eClass())
				&& isMessage_CreateEdge((Message) domainElement)) {
			return MessageCreateEditPart.VISUAL_ID;
		}
		if (UMLPackage.eINSTANCE.getMessage().isSuperTypeOf(domainElement.eClass())
				&& isMessage_DeleteEdge((Message) domainElement)) {
			return MessageDeleteEditPart.VISUAL_ID;
		}
		if (UMLPackage.eINSTANCE.getMessage().isSuperTypeOf(domainElement.eClass())
				&& isMessage_LostEdge((Message) domainElement)) {
			return MessageLostEditPart.VISUAL_ID;
		}
		if (UMLPackage.eINSTANCE.getMessage().isSuperTypeOf(domainElement.eClass())
				&& isMessage_FoundEdge((Message) domainElement)) {
			return MessageFoundEditPart.VISUAL_ID;
		}
		if (UMLPackage.eINSTANCE.getGeneralOrdering().isSuperTypeOf(domainElement.eClass())) {
			return GeneralOrderingEditPart.VISUAL_ID;
		}
		if (UMLPackage.eINSTANCE.getDurationConstraint().isSuperTypeOf(domainElement.eClass())) {
			return DurationConstraintLinkEditPart.VISUAL_ID;
		}
		if (UMLPackage.eINSTANCE.getDurationObservation().isSuperTypeOf(domainElement.eClass())) {
			return DurationObservationLinkEditPart.VISUAL_ID;
		}
		return "";
	}

	/**
	 * User can change implementation of this method to handle some specific situations not covered
	 * by default logic.
	 *
	 * @generated
	 */
	private static boolean isDiagram(Package element) {
		return true;
	}

	/**
	 * @generated
	 */
	private static boolean isMessage_SynchEdge(Message domainElement) {
		Object result = UMLOCLFactory.getExpression(1, UMLPackage.eINSTANCE.getMessage(), null).evaluate(domainElement);
		return result instanceof Boolean && ((Boolean) result).booleanValue();
	}

	/**
	 * @generated
	 */
	private static boolean isMessage_AsynchEdge(Message domainElement) {
		Object result = UMLOCLFactory.getExpression(4, UMLPackage.eINSTANCE.getMessage(), null).evaluate(domainElement);
		return result instanceof Boolean && ((Boolean) result).booleanValue();
	}

	/**
	 * @generated
	 */
	private static boolean isMessage_ReplyEdge(Message domainElement) {
		Object result = UMLOCLFactory.getExpression(7, UMLPackage.eINSTANCE.getMessage(), null).evaluate(domainElement);
		return result instanceof Boolean && ((Boolean) result).booleanValue();
	}

	/**
	 * @generated
	 */
	private static boolean isMessage_CreateEdge(Message domainElement) {
		Object result = UMLOCLFactory.getExpression(10, UMLPackage.eINSTANCE.getMessage(), null)
				.evaluate(domainElement);
		return result instanceof Boolean && ((Boolean) result).booleanValue();
	}

	/**
	 * @generated
	 */
	private static boolean isMessage_DeleteEdge(Message domainElement) {
		Object result = UMLOCLFactory.getExpression(13, UMLPackage.eINSTANCE.getMessage(), null)
				.evaluate(domainElement);
		return result instanceof Boolean && ((Boolean) result).booleanValue();
	}

	/**
	 * @generated
	 */
	private static boolean isMessage_LostEdge(Message domainElement) {
		Object result = UMLOCLFactory.getExpression(16, UMLPackage.eINSTANCE.getMessage(), null)
				.evaluate(domainElement);
		return result instanceof Boolean && ((Boolean) result).booleanValue();
	}

	/**
	 * @generated
	 */
	private static boolean isMessage_FoundEdge(Message domainElement) {
		Object result = UMLOCLFactory.getExpression(19, UMLPackage.eINSTANCE.getMessage(), null)
				.evaluate(domainElement);
		return result instanceof Boolean && ((Boolean) result).booleanValue();
	}

	/**
	 * @generated
	 */
	public static boolean checkNodeVisualID(View containerView, EObject domainElement, String candidate) {
		if (candidate == null) {
			// unrecognized id is always bad
			return false;
		}
		String basic = getNodeVisualID(containerView, domainElement);
		return candidate.equals(basic);
	}

	/**
	 * @generated
	 */
	public static boolean isCompartmentVisualID(String visualID) {
		if (visualID != null) {
			switch (visualID) {
			case InteractionInteractionCompartmentEditPart.VISUAL_ID:
			case CombinedFragmentCombinedFragmentCompartmentEditPart.VISUAL_ID:
				return true;
			}
		}
		return false;
	}

	/**
	 * @generated
	 */
	public static boolean isSemanticLeafVisualID(String visualID) {
		if (visualID != null) {
			switch (visualID) {
			case SequenceDiagramEditPart.VISUAL_ID:
				return false;
			case ConsiderIgnoreFragmentEditPart.VISUAL_ID:
			case ConstraintEditPart.VISUAL_ID:
			case CommentEditPart.VISUAL_ID:
			case ContinuationEditPart.VISUAL_ID:
			case StateInvariantEditPart.VISUAL_ID:
			case TimeConstraintBorderNodeEditPart.VISUAL_ID:
			case TimeObservationBorderNodeEditPart.VISUAL_ID:
			case GateEditPart.VISUAL_ID:
				return true;
			}
		}
		return false;
	}

	/**
	 * @generated
	 */
	public static final DiagramStructure TYPED_INSTANCE = new DiagramStructure() {
		/**
		 * @generated
		 */
		@Override
		public String getVisualID(View view) {
			return org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry.getVisualID(view);
		}

		/**
		 * @generated
		 */
		@Override
		public String getModelID(View view) {
			return org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry.getModelID(view);
		}

		/**
		 * @generated
		 */
		@Override
		public String getNodeVisualID(View containerView, EObject domainElement) {
			return org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry.getNodeVisualID(containerView,
					domainElement);
		}

		/**
		 * @generated
		 */
		@Override
		public boolean checkNodeVisualID(View containerView, EObject domainElement, String candidate) {
			return org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry.checkNodeVisualID(containerView,
					domainElement, candidate);
		}

		/**
		 * @generated
		 */
		@Override
		public boolean isCompartmentVisualID(String visualID) {
			return org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry.isCompartmentVisualID(visualID);
		}

		/**
		 * @generated
		 */
		@Override
		public boolean isSemanticLeafVisualID(String visualID) {
			return org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry.isSemanticLeafVisualID(visualID);
		}
	};
}
