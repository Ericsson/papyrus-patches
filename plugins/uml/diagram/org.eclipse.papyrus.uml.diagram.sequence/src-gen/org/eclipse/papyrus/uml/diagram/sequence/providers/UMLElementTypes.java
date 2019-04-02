
/**
 * Copyright (c) 2016 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
  *  CEA LIST - Initial API and implementation
 */
package org.eclipse.papyrus.uml.diagram.sequence.providers;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.gmf.runtime.emf.type.core.ElementTypeRegistry;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.papyrus.infra.gmfdiag.common.providers.DiagramElementTypes;
import org.eclipse.papyrus.infra.gmfdiag.tooling.runtime.providers.DiagramElementTypeImages;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ActionExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.BehaviorExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CommentAnnotatedElementEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CommentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ConsiderIgnoreFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ConstraintConstrainedElementEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ConstraintEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ContextLinkEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ContinuationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DestructionOccurrenceSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationConstraintLinkEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationObservationLinkEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.GateEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.GeneralOrderingEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionOperandEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionUseEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageAsyncEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageDeleteEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageFoundEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageLostEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageReplyEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageSyncEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.SequenceDiagramEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.StateInvariantEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeConstraintBorderNodeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeObservationBorderNodeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * @generated
 */
public class UMLElementTypes {

	/**
	 * @generated
	 */
	private UMLElementTypes() {
	}

	/**
	 * @generated
	 */
	private static Map<IElementType, ENamedElement> elements;

	/**
	 * @generated
	 */
	private static DiagramElementTypeImages elementTypeImages = new DiagramElementTypeImages(
			UMLDiagramEditorPlugin.getInstance().getItemProvidersAdapterFactory());

	/**
	 * @generated
	 */
	private static Set<IElementType> KNOWN_ELEMENT_TYPES;

	/**
	 * @generated
	 */
	public static final IElementType Package_SequenceDiagram = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.Package_SequenceDiagram"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType Interaction_Shape = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.Interaction_Shape"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType ConsiderIgnoreFragment_Shape = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.ConsiderIgnoreFragment_Shape"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType CombinedFragment_Shape = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.CombinedFragment_Shape"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType InteractionOperand_Shape = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.InteractionOperand_Shape"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType InteractionUse_Shape = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.InteractionUse_Shape"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType Continuation_Shape = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.Continuation_Shape"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType Lifeline_Shape = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.Lifeline_Shape"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType ActionExecutionSpecification_Shape = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.ActionExecutionSpecification_Shape"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType BehaviorExecutionSpecification_Shape = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.BehaviorExecutionSpecification_Shape"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType StateInvariant_Shape = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.StateInvariant_Shape"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType DestructionOccurrenceSpecification_Shape = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.DestructionOccurrenceSpecification_Shape"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType Constraint_Shape = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.Constraint_Shape"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType Comment_Shape = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.Comment_Shape"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType Gate_Shape = getElementTypeByUniqueId("org.eclipse.papyrus.umldi.Gate_Shape"); //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static final IElementType TimeConstraint_Shape = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.TimeConstraint_Shape"); //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static final IElementType TimeObservation_Shape = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.TimeObservation_Shape"); //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static final IElementType Message_SynchEdge = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.Message_SynchEdge"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType Message_AsynchEdge = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.Message_AsynchEdge"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType Message_ReplyEdge = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.Message_ReplyEdge"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType Message_CreateEdge = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.Message_CreateEdge"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType Message_DeleteEdge = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.Message_DeleteEdge"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType Message_LostEdge = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.Message_LostEdge"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType Message_FoundEdge = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.Message_FoundEdge"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType Comment_AnnotatedElementEdge = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.Comment_AnnotatedElementEdge"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType Constraint_ConstrainedElementEdge = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.Constraint_ConstrainedElementEdge"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType GeneralOrdering_Edge = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.GeneralOrdering_Edge"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType Constraint_ContextEdge = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.Constraint_ContextEdge"); //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static final IElementType DurationConstraint_Edge = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.DurationConstraint_Edge"); //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static final IElementType DurationObservation_Edge = getElementTypeByUniqueId(
			"org.eclipse.papyrus.umldi.DurationObservation_Edge"); //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static ImageDescriptor getImageDescriptor(ENamedElement element) {
		return elementTypeImages.getImageDescriptor(element);
	}

	/**
	 * @generated
	 */
	public static Image getImage(ENamedElement element) {
		return elementTypeImages.getImage(element);
	}

	/**
	 * @generated
	 */
	public static ImageDescriptor getImageDescriptor(IAdaptable hint) {
		return getImageDescriptor(getElement(hint));
	}

	/**
	 * @generated
	 */
	public static Image getImage(IAdaptable hint) {
		return getImage(getElement(hint));
	}

	/**
	 * Returns 'type' of the ecore object associated with the hint.
	 *
	 * @generated
	 */
	public static synchronized ENamedElement getElement(IAdaptable hint) {
		Object type = hint.getAdapter(IElementType.class);
		if (elements == null) {
			elements = new IdentityHashMap<>();

			elements.put(Package_SequenceDiagram, UMLPackage.eINSTANCE.getPackage());

			elements.put(Interaction_Shape, UMLPackage.eINSTANCE.getInteraction());

			elements.put(ConsiderIgnoreFragment_Shape, UMLPackage.eINSTANCE.getConsiderIgnoreFragment());

			elements.put(CombinedFragment_Shape, UMLPackage.eINSTANCE.getCombinedFragment());

			elements.put(InteractionOperand_Shape, UMLPackage.eINSTANCE.getInteractionOperand());

			elements.put(InteractionUse_Shape, UMLPackage.eINSTANCE.getInteractionUse());

			elements.put(Continuation_Shape, UMLPackage.eINSTANCE.getContinuation());

			elements.put(Lifeline_Shape, UMLPackage.eINSTANCE.getLifeline());

			elements.put(ActionExecutionSpecification_Shape, UMLPackage.eINSTANCE.getActionExecutionSpecification());

			elements.put(BehaviorExecutionSpecification_Shape,
					UMLPackage.eINSTANCE.getBehaviorExecutionSpecification());

			elements.put(StateInvariant_Shape, UMLPackage.eINSTANCE.getStateInvariant());

			elements.put(DestructionOccurrenceSpecification_Shape,
					UMLPackage.eINSTANCE.getDestructionOccurrenceSpecification());

			elements.put(Constraint_Shape, UMLPackage.eINSTANCE.getConstraint());

			elements.put(Comment_Shape, UMLPackage.eINSTANCE.getComment());

			elements.put(Gate_Shape, UMLPackage.eINSTANCE.getGate());

			elements.put(TimeConstraint_Shape, UMLPackage.eINSTANCE.getTimeConstraint());

			elements.put(TimeObservation_Shape, UMLPackage.eINSTANCE.getTimeObservation());

			elements.put(Message_SynchEdge, UMLPackage.eINSTANCE.getMessage());

			elements.put(Message_AsynchEdge, UMLPackage.eINSTANCE.getMessage());

			elements.put(Message_ReplyEdge, UMLPackage.eINSTANCE.getMessage());

			elements.put(Message_CreateEdge, UMLPackage.eINSTANCE.getMessage());

			elements.put(Message_DeleteEdge, UMLPackage.eINSTANCE.getMessage());

			elements.put(Message_LostEdge, UMLPackage.eINSTANCE.getMessage());

			elements.put(Message_FoundEdge, UMLPackage.eINSTANCE.getMessage());

			elements.put(Comment_AnnotatedElementEdge, UMLPackage.eINSTANCE.getComment_AnnotatedElement());

			elements.put(Constraint_ConstrainedElementEdge, UMLPackage.eINSTANCE.getConstraint_ConstrainedElement());

			elements.put(GeneralOrdering_Edge, UMLPackage.eINSTANCE.getGeneralOrdering());

			elements.put(Constraint_ContextEdge, UMLPackage.eINSTANCE.getConstraint_Context());

			elements.put(DurationConstraint_Edge, UMLPackage.eINSTANCE.getDurationConstraint());

			elements.put(DurationObservation_Edge, UMLPackage.eINSTANCE.getDurationObservation());
		}
		return elements.get(type);
	}

	/**
	 * @generated
	 */
	private static IElementType getElementTypeByUniqueId(String id) {
		return ElementTypeRegistry.getInstance().getType(id);
	}

	/**
	 * @generated
	 */
	public static synchronized boolean isKnownElementType(IElementType elementType) {
		if (KNOWN_ELEMENT_TYPES == null) {
			KNOWN_ELEMENT_TYPES = new HashSet<>();
			KNOWN_ELEMENT_TYPES.add(Package_SequenceDiagram);
			KNOWN_ELEMENT_TYPES.add(Interaction_Shape);
			KNOWN_ELEMENT_TYPES.add(ConsiderIgnoreFragment_Shape);
			KNOWN_ELEMENT_TYPES.add(CombinedFragment_Shape);
			KNOWN_ELEMENT_TYPES.add(InteractionOperand_Shape);
			KNOWN_ELEMENT_TYPES.add(InteractionUse_Shape);
			KNOWN_ELEMENT_TYPES.add(Continuation_Shape);
			KNOWN_ELEMENT_TYPES.add(Lifeline_Shape);
			KNOWN_ELEMENT_TYPES.add(ActionExecutionSpecification_Shape);
			KNOWN_ELEMENT_TYPES.add(BehaviorExecutionSpecification_Shape);
			KNOWN_ELEMENT_TYPES.add(StateInvariant_Shape);
			KNOWN_ELEMENT_TYPES.add(DestructionOccurrenceSpecification_Shape);
			KNOWN_ELEMENT_TYPES.add(Constraint_Shape);
			KNOWN_ELEMENT_TYPES.add(Comment_Shape);
			KNOWN_ELEMENT_TYPES.add(Gate_Shape);
			KNOWN_ELEMENT_TYPES.add(TimeConstraint_Shape);
			KNOWN_ELEMENT_TYPES.add(TimeObservation_Shape);
			KNOWN_ELEMENT_TYPES.add(Message_SynchEdge);
			KNOWN_ELEMENT_TYPES.add(Message_AsynchEdge);
			KNOWN_ELEMENT_TYPES.add(Message_ReplyEdge);
			KNOWN_ELEMENT_TYPES.add(Message_CreateEdge);
			KNOWN_ELEMENT_TYPES.add(Message_DeleteEdge);
			KNOWN_ELEMENT_TYPES.add(Message_LostEdge);
			KNOWN_ELEMENT_TYPES.add(Message_FoundEdge);
			KNOWN_ELEMENT_TYPES.add(Comment_AnnotatedElementEdge);
			KNOWN_ELEMENT_TYPES.add(Constraint_ConstrainedElementEdge);
			KNOWN_ELEMENT_TYPES.add(GeneralOrdering_Edge);
			KNOWN_ELEMENT_TYPES.add(Constraint_ContextEdge);
			KNOWN_ELEMENT_TYPES.add(DurationConstraint_Edge);
			KNOWN_ELEMENT_TYPES.add(DurationObservation_Edge);
		}

		boolean result = KNOWN_ELEMENT_TYPES.contains(elementType);

		if (!result) {
			IElementType[] supertypes = elementType.getAllSuperTypes();
			for (int i = 0; !result && (i < supertypes.length); i++) {
				result = KNOWN_ELEMENT_TYPES.contains(supertypes[i]);
			}
		}

		return result;
	}

	/**
	 * @generated
	 */
	public static IElementType getElementType(String visualID) {
		if (visualID != null) {
			switch (visualID) {
			case SequenceDiagramEditPart.VISUAL_ID:
				return Package_SequenceDiagram;
			case InteractionEditPart.VISUAL_ID:
				return Interaction_Shape;
			case ConsiderIgnoreFragmentEditPart.VISUAL_ID:
				return ConsiderIgnoreFragment_Shape;
			case CombinedFragmentEditPart.VISUAL_ID:
				return CombinedFragment_Shape;
			case InteractionOperandEditPart.VISUAL_ID:
				return InteractionOperand_Shape;
			case InteractionUseEditPart.VISUAL_ID:
				return InteractionUse_Shape;
			case ContinuationEditPart.VISUAL_ID:
				return Continuation_Shape;
			case LifelineEditPart.VISUAL_ID:
				return Lifeline_Shape;
			case ActionExecutionSpecificationEditPart.VISUAL_ID:
				return ActionExecutionSpecification_Shape;
			case BehaviorExecutionSpecificationEditPart.VISUAL_ID:
				return BehaviorExecutionSpecification_Shape;
			case StateInvariantEditPart.VISUAL_ID:
				return StateInvariant_Shape;
			case DestructionOccurrenceSpecificationEditPart.VISUAL_ID:
				return DestructionOccurrenceSpecification_Shape;
			case ConstraintEditPart.VISUAL_ID:
				return Constraint_Shape;
			case CommentEditPart.VISUAL_ID:
				return Comment_Shape;
			case GateEditPart.VISUAL_ID:
				return Gate_Shape;
			case TimeConstraintBorderNodeEditPart.VISUAL_ID:
				return TimeConstraint_Shape;
			case TimeObservationBorderNodeEditPart.VISUAL_ID:
				return TimeObservation_Shape;
			case MessageSyncEditPart.VISUAL_ID:
				return Message_SynchEdge;
			case MessageAsyncEditPart.VISUAL_ID:
				return Message_AsynchEdge;
			case MessageReplyEditPart.VISUAL_ID:
				return Message_ReplyEdge;
			case MessageCreateEditPart.VISUAL_ID:
				return Message_CreateEdge;
			case MessageDeleteEditPart.VISUAL_ID:
				return Message_DeleteEdge;
			case MessageLostEditPart.VISUAL_ID:
				return Message_LostEdge;
			case MessageFoundEditPart.VISUAL_ID:
				return Message_FoundEdge;
			case CommentAnnotatedElementEditPart.VISUAL_ID:
				return Comment_AnnotatedElementEdge;
			case ConstraintConstrainedElementEditPart.VISUAL_ID:
				return Constraint_ConstrainedElementEdge;
			case GeneralOrderingEditPart.VISUAL_ID:
				return GeneralOrdering_Edge;
			case ContextLinkEditPart.VISUAL_ID:
				return Constraint_ContextEdge;
			case DurationConstraintLinkEditPart.VISUAL_ID:
				return DurationConstraint_Edge;
			case DurationObservationLinkEditPart.VISUAL_ID:
				return DurationObservation_Edge;
			}
		}
		return null;
	}

	/**
	 * @generated
	 */
	public static final DiagramElementTypes TYPED_INSTANCE = new DiagramElementTypes(elementTypeImages) {

		/**
		 * @generated
		 */
		@Override
		public boolean isKnownElementType(IElementType elementType) {
			return org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes.isKnownElementType(elementType);
		}

		/**
		 * @generated
		 */
		@Override
		public IElementType getElementTypeForVisualId(String visualID) {
			return org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes.getElementType(visualID);
		}

		/**
		 * @generated
		 */
		@Override
		public ENamedElement getDefiningNamedElement(IAdaptable elementTypeAdapter) {
			return org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes.getElement(elementTypeAdapter);
		}
	};

	/**
	 * @generated
	 */
	public static boolean isKindOf(IElementType subtype, IElementType supertype) {
		boolean result = subtype == supertype;

		if (!result) {
			IElementType[] supertypes = subtype.getAllSuperTypes();
			for (int i = 0; !result && (i < supertypes.length); i++) {
				result = supertype == supertypes[i];
			}
		}

		return result;
	}
}
