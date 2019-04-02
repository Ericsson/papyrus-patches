/*****************************************************************************
 * Copyright (c) 2009, 2018 Atos Origin, CEA, Christian W. Damus, and others.
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
 *   Christian W. Damus (CEA) - bug 410909
 *   Christian W. Damus - bug 536486
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.part;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gmf.runtime.emf.core.util.CrossReferenceAdapter;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.updater.DiagramUpdater;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ActionExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.BehaviorExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CCombinedCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentCombinedFragmentCompartmentEditPart;
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
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;
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
import org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes;
import org.eclipse.uml2.uml.ActionExecutionSpecification;
import org.eclipse.uml2.uml.BehaviorExecutionSpecification;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.ConsiderIgnoreFragment;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Continuation;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.DurationConstraint;
import org.eclipse.uml2.uml.DurationObservation;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.GeneralOrdering;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.StateInvariant;
import org.eclipse.uml2.uml.TimeConstraint;
import org.eclipse.uml2.uml.TimeObservation;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * @generated
 */
public class UMLDiagramUpdater implements DiagramUpdater {

	/**
	 * @generated
	 */
	public static final org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramUpdater INSTANCE = new UMLDiagramUpdater();

	/**
	 * @generated
	 */
	protected UMLDiagramUpdater() {
		// to prevent instantiation allowing the override
	}

	/**
	 * @generated
	 */
	@Override
	public List<UMLNodeDescriptor> getSemanticChildren(View view) {
		String vid = UMLVisualIDRegistry.getVisualID(view);
		if (vid != null) {
			switch (vid) {
			case SequenceDiagramEditPart.VISUAL_ID:
				return getPackage_SequenceDiagram_SemanticChildren(view);
			case InteractionEditPart.VISUAL_ID:
				return getInteraction_Shape_SemanticChildren(view);
			case CombinedFragmentEditPart.VISUAL_ID:
				return getCombinedFragment_Shape_SemanticChildren(view);
			case InteractionOperandEditPart.VISUAL_ID:
				return getInteractionOperand_Shape_SemanticChildren(view);
			case InteractionUseEditPart.VISUAL_ID:
				return getInteractionUse_Shape_SemanticChildren(view);
			case LifelineEditPart.VISUAL_ID:
				return getLifeline_Shape_SemanticChildren(view);
			case ActionExecutionSpecificationEditPart.VISUAL_ID:
				return getActionExecutionSpecification_Shape_SemanticChildren(view);
			case BehaviorExecutionSpecificationEditPart.VISUAL_ID:
				return getBehaviorExecutionSpecification_Shape_SemanticChildren(view);
			case DestructionOccurrenceSpecificationEditPart.VISUAL_ID:
				return getDestructionOccurrenceSpecification_Shape_SemanticChildren(view);
			case InteractionInteractionCompartmentEditPart.VISUAL_ID:
				return getInteraction_SubfragmentCompartment_SemanticChildren(view);
			case CombinedFragmentCombinedFragmentCompartmentEditPart.VISUAL_ID:
				return getCombinedFragment_SubfragmentCompartment_SemanticChildren(view);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public List<UMLNodeDescriptor> getPackage_SequenceDiagram_SemanticChildren(View view) {
		if (!view.isSetElement()) {
			return Collections.emptyList();
		}
		Package modelElement = (Package) view.getElement();
		LinkedList<UMLNodeDescriptor> result = new LinkedList<>();
		for (Iterator<?> it = modelElement.getPackagedElements().iterator(); it.hasNext();) {
			PackageableElement childElement = (PackageableElement) it.next();
			String visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
			if (InteractionEditPart.VISUAL_ID.equals(visualID)) {
				result.add(new UMLNodeDescriptor(childElement, visualID));
				continue;
			}
		}
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLNodeDescriptor> getInteractionOperand_Shape_SemanticChildren(View view) {
		if (!view.isSetElement()) {
			return Collections.emptyList();
		}
		InteractionOperand modelElement = (InteractionOperand) view.getElement();
		LinkedList<UMLNodeDescriptor> result = new LinkedList<>();
		for (Iterator<?> it = modelElement.getFragments().iterator(); it.hasNext();) {
			InteractionFragment childElement = (InteractionFragment) it.next();
			String visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
			if (InteractionUseEditPart.VISUAL_ID.equals(visualID)) {
				result.add(new UMLNodeDescriptor(childElement, visualID));
				continue;
			}
			if (ConsiderIgnoreFragmentEditPart.VISUAL_ID.equals(visualID)) {
				result.add(new UMLNodeDescriptor(childElement, visualID));
				continue;
			}
			if (CombinedFragmentEditPart.VISUAL_ID.equals(visualID)) {
				result.add(new UMLNodeDescriptor(childElement, visualID));
				continue;
			}
		}
		for (Iterator<?> it = modelElement.getFragments().iterator(); it.hasNext();) {
			InteractionFragment childElement = (InteractionFragment) it.next();
			String visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
			if (ContinuationEditPart.VISUAL_ID.equals(visualID)) {
				result.add(new UMLNodeDescriptor(childElement, visualID));
				continue;
			}
		}
		return result;
	}

	/**
	 * @generated NOT problem due to a bad features
	 */
	public List<UMLNodeDescriptor> getInteractionUse_Shape_SemanticChildren(View view) {
		if (!view.isSetElement()) {
			return Collections.emptyList();
		}
		InteractionUse modelElement = (InteractionUse) view.getElement();
		LinkedList<UMLNodeDescriptor> result = new LinkedList<>();
		for (Iterator<?> it = modelElement.getActualGates().iterator(); it.hasNext();) {
			Gate childElement = (Gate) it.next();
			String visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
			if (GateEditPart.VISUAL_ID.equals(visualID)) {
				result.add(new UMLNodeDescriptor(childElement, visualID));
				continue;
			}
		}
		return result;
	}

	/**
	 * @generated NOT add feature packagedElement...
	 */
	public List<UMLNodeDescriptor> getInteraction_SubfragmentCompartment_SemanticChildren(View view) {
		if (false == view.eContainer() instanceof View) {
			return Collections.emptyList();
		}
		View containerView = (View) view.eContainer();
		if (!containerView.isSetElement()) {
			return Collections.emptyList();
		}
		Interaction modelElement = (Interaction) containerView.getElement();
		LinkedList<UMLNodeDescriptor> result = new LinkedList<>();
		for (Iterator<?> it = modelElement.getFragments().iterator(); it.hasNext();) {
			InteractionFragment childElement = (InteractionFragment) it.next();
			String visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
			if (ConsiderIgnoreFragmentEditPart.VISUAL_ID.equals(visualID)) {
				result.add(new UMLNodeDescriptor(childElement, visualID));
				continue;
			}
			if (CombinedFragmentEditPart.VISUAL_ID.equals(visualID)) {
				result.add(new UMLNodeDescriptor(childElement, visualID));
				continue;
			}
			if (InteractionUseEditPart.VISUAL_ID.equals(visualID)) {
				result.add(new UMLNodeDescriptor(childElement, visualID));
				continue;
			}
		}
		for (Iterator<?> it = modelElement.getLifelines().iterator(); it.hasNext();) {
			Lifeline childElement = (Lifeline) it.next();
			String visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
			if (LifelineEditPart.VISUAL_ID.equals(visualID)) {
				result.add(new UMLNodeDescriptor(childElement, visualID));
				continue;
			}
		}
		for (Iterator<?> it = modelElement.getOwnedRules().iterator(); it.hasNext();) {
			Constraint childElement = (Constraint) it.next();
			String visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
			if (ConstraintEditPart.VISUAL_ID.equals(visualID)) {
				result.add(new UMLNodeDescriptor(childElement, visualID));
				continue;
			}
		}
		for (Iterator<?> it = modelElement.getOwnedComments().iterator(); it.hasNext();) {
			Comment childElement = (Comment) it.next();
			String visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
			if (CommentEditPart.VISUAL_ID.equals(visualID)) {
				result.add(new UMLNodeDescriptor(childElement, visualID));
				continue;
			}
		}

		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLNodeDescriptor> getCombinedFragment_SubfragmentCompartment_SemanticChildren(View view) {
		if (false == view.eContainer() instanceof View) {
			return Collections.emptyList();
		}
		View containerView = (View) view.eContainer();
		if (!containerView.isSetElement()) {
			return Collections.emptyList();
		}
		CombinedFragment modelElement = (CombinedFragment) containerView.getElement();
		LinkedList<UMLNodeDescriptor> result = new LinkedList<>();
		for (Iterator<?> it = modelElement.getOperands().iterator(); it.hasNext();) {
			InteractionOperand childElement = (InteractionOperand) it.next();
			String visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
			if (InteractionOperandEditPart.VISUAL_ID.equals(visualID)) {
				result.add(new UMLNodeDescriptor(childElement, visualID));
				continue;
			}
		}
		return result;
	}

	/**
	 * @generated NOT remove fake children for messages (DurationConstraintInMessageEditPart/DurationObservationEditPart)
	 */
	public static List<UMLNodeDescriptor> getInteraction_Shape_SemanticChildren(View view) {
		if (!view.isSetElement()) {
			return Collections.emptyList();
		}
		Interaction modelElement = (Interaction) view.getElement();
		List<UMLNodeDescriptor> result = new LinkedList<>();
		// remove fake children for messages (DurationConstraintInMessageEditPart/DurationObservationEditPart)
		return result;
	}

	/**
	 * @generated NOT bad feature to find gates
	 */
	public List<UMLNodeDescriptor> getCombinedFragment_Shape_SemanticChildren(View view) {
		if (!view.isSetElement()) {
			return Collections.emptyList();
		}
		CombinedFragment modelElement = (CombinedFragment) view.getElement();
		LinkedList<UMLNodeDescriptor> result = new LinkedList<>();
		for (Iterator<?> it = modelElement.getCfragmentGates().iterator(); it.hasNext();) {
			Gate childElement = (Gate) it.next();
			String visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
			if (GateEditPart.VISUAL_ID.equals(visualID)) {
				result.add(new UMLNodeDescriptor(childElement, visualID));
				continue;
			}
		}
		return result;
	}

	/**
	 * @generated NOT (update at each lifeline modification) Added code for manage ExecutionSpecification, handle TimeConstraintEditPart and
	 *            handle TimeObservationEditPart children
	 */
	public static List<UMLNodeDescriptor> getLifeline_Shape_SemanticChildren(View view) {
		if (!view.isSetElement()) {
			return Collections.EMPTY_LIST;
		}
		Lifeline modelElement = (Lifeline) view.getElement();
		List<UMLNodeDescriptor> result = new LinkedList();
		// Added code for manage ExecutionSpecification
		if (modelElement instanceof Lifeline) {
			if ((modelElement).getCoveredBys().size() > 0) {
				Interaction interaction = modelElement.getInteraction();
				for (Iterator values = interaction.getFragments().iterator(); values.hasNext();) {
					EObject nextValue = (EObject) values.next();
					String visualID = UMLVisualIDRegistry.getNodeVisualID(view, nextValue);
					if (BehaviorExecutionSpecificationEditPart.VISUAL_ID.equals(visualID)) {
						BehaviorExecutionSpecification be = (BehaviorExecutionSpecification) nextValue;
						if (be.getCovereds().size() > 0 && be.getCovereds().get(0) == modelElement) {
							// result.add(nextValue);
							result.add(new UMLNodeDescriptor(be, visualID));
						}
					} else if (CCombinedCompartmentEditPart.VISUAL_ID.equals(visualID)) {
						ActionExecutionSpecification ae = (ActionExecutionSpecification) nextValue;
						if (ae.getCovereds().size() > 0 && ae.getCovereds().get(0) == modelElement) {
							result.add(new UMLNodeDescriptor(ae, visualID));
						}
					} else if (StateInvariantEditPart.VISUAL_ID.equals(visualID)) {
						StateInvariant ae = (StateInvariant) nextValue;
						if (ae.getCovereds().size() > 0 && ae.getCovereds().get(0) == modelElement) {
							result.add(new UMLNodeDescriptor(ae, visualID));
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * @generated NOT
	 */
	public List<UMLNodeDescriptor> getActionExecutionSpecification_Shape_SemanticChildren(View view) {
		if (!view.isSetElement()) {
			return Collections.emptyList();
		}
		ActionExecutionSpecification modelElement = (ActionExecutionSpecification) view.getElement();
		LinkedList<UMLNodeDescriptor> result = new LinkedList<>();
		Interaction interaction = modelElement.getEnclosingInteraction();
		if (interaction == null) {
			return Collections.emptyList();
		}
		for (Iterator<?> it = interaction.getOwnedRules().iterator(); it.hasNext();) {
			Constraint childElement = (Constraint) it.next();
			String visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
			if (TimeConstraintBorderNodeEditPart.VISUAL_ID.equals(visualID)) {
				result.add(new UMLNodeDescriptor(childElement, visualID));
				continue;
			}
		}
		return result;
	}

	/**
	 * @generated NOT
	 */
	public List<UMLNodeDescriptor> getBehaviorExecutionSpecification_Shape_SemanticChildren(View view) {
		if (!view.isSetElement()) {
			return Collections.emptyList();
		}
		BehaviorExecutionSpecification modelElement = (BehaviorExecutionSpecification) view.getElement();
		LinkedList<UMLNodeDescriptor> result = new LinkedList<>();
		Interaction interaction = modelElement.getEnclosingInteraction();
		if (interaction == null) {
			return Collections.emptyList();
		}
		for (Iterator<?> it = interaction.getOwnedRules().iterator(); it.hasNext();) {
			Constraint childElement = (Constraint) it.next();
			String visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
			if (TimeConstraintBorderNodeEditPart.VISUAL_ID.equals(visualID)) {
				result.add(new UMLNodeDescriptor(childElement, visualID));
				continue;
			}
		}
		return result;
	}

	/**
	 * @generated NOT
	 */
	public List<UMLNodeDescriptor> getDestructionOccurrenceSpecification_Shape_SemanticChildren(View view) {
		if (!view.isSetElement()) {
			return Collections.emptyList();
		}
		DestructionOccurrenceSpecification modelElement = (DestructionOccurrenceSpecification) view.getElement();
		Interaction interaction = modelElement.getEnclosingInteraction();
		if (interaction == null) {
			return Collections.emptyList();
		}
		LinkedList<UMLNodeDescriptor> result = new LinkedList<>();
		for (Iterator<?> it = interaction.getOwnedRules().iterator(); it.hasNext();) {
			Constraint childElement = (Constraint) it.next();
			// Does it constrain this destruction occurrence?
			if (childElement.getConstrainedElements().contains(modelElement)) {
				String visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
				if (TimeConstraintBorderNodeEditPart.VISUAL_ID.equals(visualID)) {
					result.add(new UMLNodeDescriptor(childElement, visualID));
					continue;
				}
			}
		}
		return result;
	}

	/**
	 * @generated
	 */
	@Override
	public List<UMLLinkDescriptor> getContainedLinks(View view) {
		String vid = UMLVisualIDRegistry.getVisualID(view);
		if (vid != null) {
			switch (vid) {
			case SequenceDiagramEditPart.VISUAL_ID:
				return getPackage_SequenceDiagram_ContainedLinks(view);
			case InteractionEditPart.VISUAL_ID:
				return getInteraction_Shape_ContainedLinks(view);
			case ConsiderIgnoreFragmentEditPart.VISUAL_ID:
				return getConsiderIgnoreFragment_Shape_ContainedLinks(view);
			case CombinedFragmentEditPart.VISUAL_ID:
				return getCombinedFragment_Shape_ContainedLinks(view);
			case InteractionOperandEditPart.VISUAL_ID:
				return getInteractionOperand_Shape_ContainedLinks(view);
			case InteractionUseEditPart.VISUAL_ID:
				return getInteractionUse_Shape_ContainedLinks(view);
			case ContinuationEditPart.VISUAL_ID:
				return getContinuation_Shape_ContainedLinks(view);
			case LifelineEditPart.VISUAL_ID:
				return getLifeline_Shape_ContainedLinks(view);
			case ActionExecutionSpecificationEditPart.VISUAL_ID:
				return getActionExecutionSpecification_Shape_ContainedLinks(view);
			case BehaviorExecutionSpecificationEditPart.VISUAL_ID:
				return getBehaviorExecutionSpecification_Shape_ContainedLinks(view);
			case StateInvariantEditPart.VISUAL_ID:
				return getStateInvariant_Shape_ContainedLinks(view);
			case DestructionOccurrenceSpecificationEditPart.VISUAL_ID:
				return getDestructionOccurrenceSpecification_Shape_ContainedLinks(view);
			case ConstraintEditPart.VISUAL_ID:
				return getConstraint_Shape_ContainedLinks(view);
			case CommentEditPart.VISUAL_ID:
				return getComment_Shape_ContainedLinks(view);
			case GateEditPart.VISUAL_ID:
				return getGate_Shape_ContainedLinks(view);
			case TimeConstraintBorderNodeEditPart.VISUAL_ID:
				return getTimeConstraint_Shape_ContainedLinks(view);
			case TimeObservationBorderNodeEditPart.VISUAL_ID:
				return getTimeObservation_Shape_ContainedLinks(view);
			case MessageSyncEditPart.VISUAL_ID:
				return getMessage_SynchEdge_ContainedLinks(view);
			case MessageAsyncEditPart.VISUAL_ID:
				return getMessage_AsynchEdge_ContainedLinks(view);
			case MessageReplyEditPart.VISUAL_ID:
				return getMessage_ReplyEdge_ContainedLinks(view);
			case MessageCreateEditPart.VISUAL_ID:
				return getMessage_CreateEdge_ContainedLinks(view);
			case MessageDeleteEditPart.VISUAL_ID:
				return getMessage_DeleteEdge_ContainedLinks(view);
			case MessageLostEditPart.VISUAL_ID:
				return getMessage_LostEdge_ContainedLinks(view);
			case MessageFoundEditPart.VISUAL_ID:
				return getMessage_FoundEdge_ContainedLinks(view);
			case GeneralOrderingEditPart.VISUAL_ID:
				return getGeneralOrdering_Edge_ContainedLinks(view);
			case DurationConstraintLinkEditPart.VISUAL_ID:
				return getDurationConstraint_Edge_ContainedLinks(view);
			case DurationObservationLinkEditPart.VISUAL_ID:
				return getDurationObservation_Edge_ContainedLinks(view);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	@Override
	public List<UMLLinkDescriptor> getIncomingLinks(View view) {
		String vid = UMLVisualIDRegistry.getVisualID(view);
		if (vid != null) {
			switch (vid) {
			case InteractionEditPart.VISUAL_ID:
				return getInteraction_Shape_IncomingLinks(view);
			case ConsiderIgnoreFragmentEditPart.VISUAL_ID:
				return getConsiderIgnoreFragment_Shape_IncomingLinks(view);
			case CombinedFragmentEditPart.VISUAL_ID:
				return getCombinedFragment_Shape_IncomingLinks(view);
			case InteractionOperandEditPart.VISUAL_ID:
				return getInteractionOperand_Shape_IncomingLinks(view);
			case InteractionUseEditPart.VISUAL_ID:
				return getInteractionUse_Shape_IncomingLinks(view);
			case ContinuationEditPart.VISUAL_ID:
				return getContinuation_Shape_IncomingLinks(view);
			case LifelineEditPart.VISUAL_ID:
				return getLifeline_Shape_IncomingLinks(view);
			case ActionExecutionSpecificationEditPart.VISUAL_ID:
				return getActionExecutionSpecification_Shape_IncomingLinks(view);
			case BehaviorExecutionSpecificationEditPart.VISUAL_ID:
				return getBehaviorExecutionSpecification_Shape_IncomingLinks(view);
			case StateInvariantEditPart.VISUAL_ID:
				return getStateInvariant_Shape_IncomingLinks(view);
			case DestructionOccurrenceSpecificationEditPart.VISUAL_ID:
				return getDestructionOccurrenceSpecification_Shape_IncomingLinks(view);
			case ConstraintEditPart.VISUAL_ID:
				return getConstraint_Shape_IncomingLinks(view);
			case CommentEditPart.VISUAL_ID:
				return getComment_Shape_IncomingLinks(view);
			case GateEditPart.VISUAL_ID:
				return getGate_Shape_IncomingLinks(view);
			case TimeConstraintBorderNodeEditPart.VISUAL_ID:
				return getTimeConstraint_Shape_IncomingLinks(view);
			case TimeObservationBorderNodeEditPart.VISUAL_ID:
				return getTimeObservation_Shape_IncomingLinks(view);
			case MessageSyncEditPart.VISUAL_ID:
				return getMessage_SynchEdge_IncomingLinks(view);
			case MessageAsyncEditPart.VISUAL_ID:
				return getMessage_AsynchEdge_IncomingLinks(view);
			case MessageReplyEditPart.VISUAL_ID:
				return getMessage_ReplyEdge_IncomingLinks(view);
			case MessageCreateEditPart.VISUAL_ID:
				return getMessage_CreateEdge_IncomingLinks(view);
			case MessageDeleteEditPart.VISUAL_ID:
				return getMessage_DeleteEdge_IncomingLinks(view);
			case MessageLostEditPart.VISUAL_ID:
				return getMessage_LostEdge_IncomingLinks(view);
			case MessageFoundEditPart.VISUAL_ID:
				return getMessage_FoundEdge_IncomingLinks(view);
			case GeneralOrderingEditPart.VISUAL_ID:
				return getGeneralOrdering_Edge_IncomingLinks(view);
			case DurationConstraintLinkEditPart.VISUAL_ID:
				return getDurationConstraint_Edge_IncomingLinks(view);
			case DurationObservationLinkEditPart.VISUAL_ID:
				return getDurationObservation_Edge_IncomingLinks(view);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	@Override
	public List<UMLLinkDescriptor> getOutgoingLinks(View view) {
		String vid = UMLVisualIDRegistry.getVisualID(view);
		if (vid != null) {
			switch (vid) {
			case InteractionEditPart.VISUAL_ID:
				return getInteraction_Shape_OutgoingLinks(view);
			case ConsiderIgnoreFragmentEditPart.VISUAL_ID:
				return getConsiderIgnoreFragment_Shape_OutgoingLinks(view);
			case CombinedFragmentEditPart.VISUAL_ID:
				return getCombinedFragment_Shape_OutgoingLinks(view);
			case InteractionOperandEditPart.VISUAL_ID:
				return getInteractionOperand_Shape_OutgoingLinks(view);
			case InteractionUseEditPart.VISUAL_ID:
				return getInteractionUse_Shape_OutgoingLinks(view);
			case ContinuationEditPart.VISUAL_ID:
				return getContinuation_Shape_OutgoingLinks(view);
			case LifelineEditPart.VISUAL_ID:
				return getLifeline_Shape_OutgoingLinks(view);
			case ActionExecutionSpecificationEditPart.VISUAL_ID:
				return getActionExecutionSpecification_Shape_OutgoingLinks(view);
			case BehaviorExecutionSpecificationEditPart.VISUAL_ID:
				return getBehaviorExecutionSpecification_Shape_OutgoingLinks(view);
			case StateInvariantEditPart.VISUAL_ID:
				return getStateInvariant_Shape_OutgoingLinks(view);
			case DestructionOccurrenceSpecificationEditPart.VISUAL_ID:
				return getDestructionOccurrenceSpecification_Shape_OutgoingLinks(view);
			case ConstraintEditPart.VISUAL_ID:
				return getConstraint_Shape_OutgoingLinks(view);
			case CommentEditPart.VISUAL_ID:
				return getComment_Shape_OutgoingLinks(view);
			case GateEditPart.VISUAL_ID:
				return getGate_Shape_OutgoingLinks(view);
			case TimeConstraintBorderNodeEditPart.VISUAL_ID:
				return getTimeConstraint_Shape_OutgoingLinks(view);
			case TimeObservationBorderNodeEditPart.VISUAL_ID:
				return getTimeObservation_Shape_OutgoingLinks(view);
			case MessageSyncEditPart.VISUAL_ID:
				return getMessage_SynchEdge_OutgoingLinks(view);
			case MessageAsyncEditPart.VISUAL_ID:
				return getMessage_AsynchEdge_OutgoingLinks(view);
			case MessageReplyEditPart.VISUAL_ID:
				return getMessage_ReplyEdge_OutgoingLinks(view);
			case MessageCreateEditPart.VISUAL_ID:
				return getMessage_CreateEdge_OutgoingLinks(view);
			case MessageDeleteEditPart.VISUAL_ID:
				return getMessage_DeleteEdge_OutgoingLinks(view);
			case MessageLostEditPart.VISUAL_ID:
				return getMessage_LostEdge_OutgoingLinks(view);
			case MessageFoundEditPart.VISUAL_ID:
				return getMessage_FoundEdge_OutgoingLinks(view);
			case GeneralOrderingEditPart.VISUAL_ID:
				return getGeneralOrdering_Edge_OutgoingLinks(view);
			case DurationConstraintLinkEditPart.VISUAL_ID:
				return getDurationConstraint_Edge_OutgoingLinks(view);
			case DurationObservationLinkEditPart.VISUAL_ID:
				return getDurationObservation_Edge_OutgoingLinks(view);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getPackage_SequenceDiagram_ContainedLinks(View view) {
		Package modelElement = (Package) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getContainedTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getContainedTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getInteraction_Shape_ContainedLinks(View view) {
		Interaction modelElement = (Interaction) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getContainedTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getContainedTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getContainedTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getContainedTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getContainedTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getContainedTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getContainedTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getContainedTypeModelFacetLinks_GeneralOrdering_Edge(modelElement));
		result.addAll(getContainedTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getConsiderIgnoreFragment_Shape_ContainedLinks(View view) {
		ConsiderIgnoreFragment modelElement = (ConsiderIgnoreFragment) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getContainedTypeModelFacetLinks_GeneralOrdering_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getCombinedFragment_Shape_ContainedLinks(View view) {
		CombinedFragment modelElement = (CombinedFragment) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getContainedTypeModelFacetLinks_GeneralOrdering_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getInteractionOperand_Shape_ContainedLinks(View view) {
		InteractionOperand modelElement = (InteractionOperand) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getContainedTypeModelFacetLinks_GeneralOrdering_Edge(modelElement));
		result.addAll(getContainedTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getInteractionUse_Shape_ContainedLinks(View view) {
		InteractionUse modelElement = (InteractionUse) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getContainedTypeModelFacetLinks_GeneralOrdering_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getContinuation_Shape_ContainedLinks(View view) {
		Continuation modelElement = (Continuation) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getContainedTypeModelFacetLinks_GeneralOrdering_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getLifeline_Shape_ContainedLinks(View view) {
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getActionExecutionSpecification_Shape_ContainedLinks(View view) {
		ActionExecutionSpecification modelElement = (ActionExecutionSpecification) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getContainedTypeModelFacetLinks_GeneralOrdering_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getBehaviorExecutionSpecification_Shape_ContainedLinks(View view) {
		BehaviorExecutionSpecification modelElement = (BehaviorExecutionSpecification) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getContainedTypeModelFacetLinks_GeneralOrdering_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getStateInvariant_Shape_ContainedLinks(View view) {
		StateInvariant modelElement = (StateInvariant) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getContainedTypeModelFacetLinks_GeneralOrdering_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getDestructionOccurrenceSpecification_Shape_ContainedLinks(View view) {
		DestructionOccurrenceSpecification modelElement = (DestructionOccurrenceSpecification) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getContainedTypeModelFacetLinks_GeneralOrdering_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getConstraint_Shape_ContainedLinks(View view) {
		Constraint modelElement = (Constraint) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement));
		result.addAll(getOutgoingFeatureModelFacetLinks_Constraint_ContextEdge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getComment_Shape_ContainedLinks(View view) {
		Comment modelElement = (Comment) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getGate_Shape_ContainedLinks(View view) {
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getTimeConstraint_Shape_ContainedLinks(View view) {
		TimeConstraint modelElement = (TimeConstraint) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement));
		result.addAll(getOutgoingFeatureModelFacetLinks_Constraint_ContextEdge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getTimeObservation_Shape_ContainedLinks(View view) {
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_SynchEdge_ContainedLinks(View view) {
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_AsynchEdge_ContainedLinks(View view) {
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_ReplyEdge_ContainedLinks(View view) {
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_CreateEdge_ContainedLinks(View view) {
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_DeleteEdge_ContainedLinks(View view) {
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_LostEdge_ContainedLinks(View view) {
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_FoundEdge_ContainedLinks(View view) {
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getGeneralOrdering_Edge_ContainedLinks(View view) {
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getDurationConstraint_Edge_ContainedLinks(View view) {
		DurationConstraint modelElement = (DurationConstraint) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement));
		result.addAll(getOutgoingFeatureModelFacetLinks_Constraint_ContextEdge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getDurationObservation_Edge_ContainedLinks(View view) {
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getInteraction_Shape_IncomingLinks(View view) {
		Interaction modelElement = (Interaction) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ContextEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getConsiderIgnoreFragment_Shape_IncomingLinks(View view) {
		ConsiderIgnoreFragment modelElement = (ConsiderIgnoreFragment) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getCombinedFragment_Shape_IncomingLinks(View view) {
		CombinedFragment modelElement = (CombinedFragment) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getInteractionOperand_Shape_IncomingLinks(View view) {
		InteractionOperand modelElement = (InteractionOperand) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ContextEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getInteractionUse_Shape_IncomingLinks(View view) {
		InteractionUse modelElement = (InteractionUse) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getContinuation_Shape_IncomingLinks(View view) {
		Continuation modelElement = (Continuation) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getLifeline_Shape_IncomingLinks(View view) {
		Lifeline modelElement = (Lifeline) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getActionExecutionSpecification_Shape_IncomingLinks(View view) {
		ActionExecutionSpecification modelElement = (ActionExecutionSpecification) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getBehaviorExecutionSpecification_Shape_IncomingLinks(View view) {
		BehaviorExecutionSpecification modelElement = (BehaviorExecutionSpecification) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getStateInvariant_Shape_IncomingLinks(View view) {
		StateInvariant modelElement = (StateInvariant) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getDestructionOccurrenceSpecification_Shape_IncomingLinks(View view) {
		DestructionOccurrenceSpecification modelElement = (DestructionOccurrenceSpecification) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_GeneralOrdering_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getConstraint_Shape_IncomingLinks(View view) {
		Constraint modelElement = (Constraint) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getComment_Shape_IncomingLinks(View view) {
		Comment modelElement = (Comment) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getGate_Shape_IncomingLinks(View view) {
		Gate modelElement = (Gate) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getTimeConstraint_Shape_IncomingLinks(View view) {
		TimeConstraint modelElement = (TimeConstraint) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getTimeObservation_Shape_IncomingLinks(View view) {
		TimeObservation modelElement = (TimeObservation) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_SynchEdge_IncomingLinks(View view) {
		Message modelElement = (Message) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_AsynchEdge_IncomingLinks(View view) {
		Message modelElement = (Message) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_ReplyEdge_IncomingLinks(View view) {
		Message modelElement = (Message) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_CreateEdge_IncomingLinks(View view) {
		Message modelElement = (Message) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_DeleteEdge_IncomingLinks(View view) {
		Message modelElement = (Message) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_LostEdge_IncomingLinks(View view) {
		Message modelElement = (Message) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_FoundEdge_IncomingLinks(View view) {
		Message modelElement = (Message) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getGeneralOrdering_Edge_IncomingLinks(View view) {
		GeneralOrdering modelElement = (GeneralOrdering) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getDurationConstraint_Edge_IncomingLinks(View view) {
		DurationConstraint modelElement = (DurationConstraint) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getDurationObservation_Edge_IncomingLinks(View view) {
		DurationObservation modelElement = (DurationObservation) view.getElement();
		CrossReferenceAdapter crossReferencer = CrossReferenceAdapter
				.getCrossReferenceAdapter(view.eResource().getResourceSet());
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getIncomingTypeModelFacetLinks_Message_SynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_AsynchEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_ReplyEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_CreateEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_DeleteEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_LostEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_Message_FoundEdge(modelElement, crossReferencer));
		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement, crossReferencer));
		result.addAll(
				getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationConstraint_Edge(modelElement, crossReferencer));
		result.addAll(getIncomingTypeModelFacetLinks_DurationObservation_Edge(modelElement, crossReferencer));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getInteraction_Shape_OutgoingLinks(View view) {
		Interaction modelElement = (Interaction) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getConsiderIgnoreFragment_Shape_OutgoingLinks(View view) {
		ConsiderIgnoreFragment modelElement = (ConsiderIgnoreFragment) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getCombinedFragment_Shape_OutgoingLinks(View view) {
		CombinedFragment modelElement = (CombinedFragment) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getInteractionOperand_Shape_OutgoingLinks(View view) {
		InteractionOperand modelElement = (InteractionOperand) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getInteractionUse_Shape_OutgoingLinks(View view) {
		InteractionUse modelElement = (InteractionUse) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getContinuation_Shape_OutgoingLinks(View view) {
		Continuation modelElement = (Continuation) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getLifeline_Shape_OutgoingLinks(View view) {
		Lifeline modelElement = (Lifeline) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getActionExecutionSpecification_Shape_OutgoingLinks(View view) {
		ActionExecutionSpecification modelElement = (ActionExecutionSpecification) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getBehaviorExecutionSpecification_Shape_OutgoingLinks(View view) {
		BehaviorExecutionSpecification modelElement = (BehaviorExecutionSpecification) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getStateInvariant_Shape_OutgoingLinks(View view) {
		StateInvariant modelElement = (StateInvariant) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getDestructionOccurrenceSpecification_Shape_OutgoingLinks(View view) {
		DestructionOccurrenceSpecification modelElement = (DestructionOccurrenceSpecification) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_GeneralOrdering_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getConstraint_Shape_OutgoingLinks(View view) {
		Constraint modelElement = (Constraint) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement));
		result.addAll(getOutgoingFeatureModelFacetLinks_Constraint_ContextEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getComment_Shape_OutgoingLinks(View view) {
		Comment modelElement = (Comment) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getGate_Shape_OutgoingLinks(View view) {
		Gate modelElement = (Gate) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getTimeConstraint_Shape_OutgoingLinks(View view) {
		TimeConstraint modelElement = (TimeConstraint) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement));
		result.addAll(getOutgoingFeatureModelFacetLinks_Constraint_ContextEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getTimeObservation_Shape_OutgoingLinks(View view) {
		TimeObservation modelElement = (TimeObservation) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_SynchEdge_OutgoingLinks(View view) {
		Message modelElement = (Message) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_AsynchEdge_OutgoingLinks(View view) {
		Message modelElement = (Message) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_ReplyEdge_OutgoingLinks(View view) {
		Message modelElement = (Message) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_CreateEdge_OutgoingLinks(View view) {
		Message modelElement = (Message) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_DeleteEdge_OutgoingLinks(View view) {
		Message modelElement = (Message) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_LostEdge_OutgoingLinks(View view) {
		Message modelElement = (Message) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getMessage_FoundEdge_OutgoingLinks(View view) {
		Message modelElement = (Message) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getGeneralOrdering_Edge_OutgoingLinks(View view) {
		GeneralOrdering modelElement = (GeneralOrdering) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getDurationConstraint_Edge_OutgoingLinks(View view) {
		DurationConstraint modelElement = (DurationConstraint) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(modelElement));
		result.addAll(getOutgoingFeatureModelFacetLinks_Constraint_ContextEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public List<UMLLinkDescriptor> getDurationObservation_Edge_OutgoingLinks(View view) {
		DurationObservation modelElement = (DurationObservation) view.getElement();
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		result.addAll(getOutgoingTypeModelFacetLinks_Message_SynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_AsynchEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_ReplyEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_CreateEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_DeleteEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_LostEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_Message_FoundEdge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(modelElement));
		result.addAll(getOutgoingTypeModelFacetLinks_DurationObservation_Edge(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_Message_SynchEdge(Interaction container) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getMessages().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof Message) {
				continue;
			}
			Message link = (Message) linkObject;
			if (!MessageSyncEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getOwnedElements();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof Element) {
				continue;
			}
			Element dst = (Element) theTarget;
			Element src = link.getOwner();
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.Message_SynchEdge,
					MessageSyncEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_Message_AsynchEdge(Interaction container) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getMessages().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof Message) {
				continue;
			}
			Message link = (Message) linkObject;
			if (!MessageAsyncEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getOwnedElements();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof Element) {
				continue;
			}
			Element dst = (Element) theTarget;
			Element src = link.getOwner();
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.Message_AsynchEdge,
					MessageAsyncEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_Message_ReplyEdge(Interaction container) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getMessages().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof Message) {
				continue;
			}
			Message link = (Message) linkObject;
			if (!MessageReplyEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getOwnedElements();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof Element) {
				continue;
			}
			Element dst = (Element) theTarget;
			Element src = link.getOwner();
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.Message_ReplyEdge,
					MessageReplyEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_Message_CreateEdge(Interaction container) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getMessages().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof Message) {
				continue;
			}
			Message link = (Message) linkObject;
			if (!MessageCreateEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getOwnedElements();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof Element) {
				continue;
			}
			Element dst = (Element) theTarget;
			Element src = link.getOwner();
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.Message_CreateEdge,
					MessageCreateEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_Message_DeleteEdge(Interaction container) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getMessages().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof Message) {
				continue;
			}
			Message link = (Message) linkObject;
			if (!MessageDeleteEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getOwnedElements();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof Element) {
				continue;
			}
			Element dst = (Element) theTarget;
			Element src = link.getOwner();
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.Message_DeleteEdge,
					MessageDeleteEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_Message_LostEdge(Interaction container) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getMessages().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof Message) {
				continue;
			}
			Message link = (Message) linkObject;
			if (!MessageLostEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getOwnedElements();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof Element) {
				continue;
			}
			Element dst = (Element) theTarget;
			Element src = link.getOwner();
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.Message_LostEdge,
					MessageLostEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_Message_FoundEdge(Interaction container) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getMessages().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof Message) {
				continue;
			}
			Message link = (Message) linkObject;
			if (!MessageFoundEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getOwnedElements();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof Element) {
				continue;
			}
			Element dst = (Element) theTarget;
			Element src = link.getOwner();
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.Message_FoundEdge,
					MessageFoundEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_GeneralOrdering_Edge(
			InteractionFragment container) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getGeneralOrderings().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof GeneralOrdering) {
				continue;
			}
			GeneralOrdering link = (GeneralOrdering) linkObject;
			if (!GeneralOrderingEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			OccurrenceSpecification dst = link.getAfter();
			OccurrenceSpecification src = link.getBefore();
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.GeneralOrdering_Edge,
					GeneralOrderingEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_DurationConstraint_Edge(
			Namespace container) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getOwnedRules().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof DurationConstraint) {
				continue;
			}
			DurationConstraint link = (DurationConstraint) linkObject;
			if (!DurationConstraintLinkEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getConstrainedElements();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof Element) {
				continue;
			}
			Element dst = (Element) theTarget;
			List<?> sources = link.getConstrainedElements();
			Object theSource = sources.size() == 1 ? sources.get(0) : null;
			if (false == theSource instanceof Element) {
				continue;
			}
			Element src = (Element) theSource;
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.DurationConstraint_Edge,
					DurationConstraintLinkEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_DurationObservation_Edge(
			Package container) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getPackagedElements().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof DurationObservation) {
				continue;
			}
			DurationObservation link = (DurationObservation) linkObject;
			if (!DurationObservationLinkEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getEvents();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof NamedElement) {
				continue;
			}
			NamedElement dst = (NamedElement) theTarget;
			List<?> sources = link.getEvents();
			Object theSource = sources.size() == 1 ? sources.get(0) : null;
			if (false == theSource instanceof NamedElement) {
				continue;
			}
			NamedElement src = (NamedElement) theSource;
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.DurationObservation_Edge,
					DurationObservationLinkEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getIncomingTypeModelFacetLinks_Message_SynchEdge(Element target,
			CrossReferenceAdapter crossReferencer) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		Collection<EStructuralFeature.Setting> settings = crossReferencer.getInverseReferences(target);
		for (EStructuralFeature.Setting setting : settings) {
			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getElement_OwnedElement()
					|| false == setting.getEObject() instanceof Message) {
				continue;
			}
			Message link = (Message) setting.getEObject();
			if (!MessageSyncEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			Element src = link.getOwner();
			result.add(new UMLLinkDescriptor(src, target, link, UMLElementTypes.Message_SynchEdge,
					MessageSyncEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getIncomingTypeModelFacetLinks_Message_AsynchEdge(Element target,
			CrossReferenceAdapter crossReferencer) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		Collection<EStructuralFeature.Setting> settings = crossReferencer.getInverseReferences(target);
		for (EStructuralFeature.Setting setting : settings) {
			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getElement_OwnedElement()
					|| false == setting.getEObject() instanceof Message) {
				continue;
			}
			Message link = (Message) setting.getEObject();
			if (!MessageAsyncEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			Element src = link.getOwner();
			result.add(new UMLLinkDescriptor(src, target, link, UMLElementTypes.Message_AsynchEdge,
					MessageAsyncEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getIncomingTypeModelFacetLinks_Message_ReplyEdge(Element target,
			CrossReferenceAdapter crossReferencer) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		Collection<EStructuralFeature.Setting> settings = crossReferencer.getInverseReferences(target);
		for (EStructuralFeature.Setting setting : settings) {
			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getElement_OwnedElement()
					|| false == setting.getEObject() instanceof Message) {
				continue;
			}
			Message link = (Message) setting.getEObject();
			if (!MessageReplyEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			Element src = link.getOwner();
			result.add(new UMLLinkDescriptor(src, target, link, UMLElementTypes.Message_ReplyEdge,
					MessageReplyEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getIncomingTypeModelFacetLinks_Message_CreateEdge(Element target,
			CrossReferenceAdapter crossReferencer) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		Collection<EStructuralFeature.Setting> settings = crossReferencer.getInverseReferences(target);
		for (EStructuralFeature.Setting setting : settings) {
			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getElement_OwnedElement()
					|| false == setting.getEObject() instanceof Message) {
				continue;
			}
			Message link = (Message) setting.getEObject();
			if (!MessageCreateEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			Element src = link.getOwner();
			result.add(new UMLLinkDescriptor(src, target, link, UMLElementTypes.Message_CreateEdge,
					MessageCreateEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getIncomingTypeModelFacetLinks_Message_DeleteEdge(Element target,
			CrossReferenceAdapter crossReferencer) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		Collection<EStructuralFeature.Setting> settings = crossReferencer.getInverseReferences(target);
		for (EStructuralFeature.Setting setting : settings) {
			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getElement_OwnedElement()
					|| false == setting.getEObject() instanceof Message) {
				continue;
			}
			Message link = (Message) setting.getEObject();
			if (!MessageDeleteEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			Element src = link.getOwner();
			result.add(new UMLLinkDescriptor(src, target, link, UMLElementTypes.Message_DeleteEdge,
					MessageDeleteEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getIncomingTypeModelFacetLinks_Message_LostEdge(Element target,
			CrossReferenceAdapter crossReferencer) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		Collection<EStructuralFeature.Setting> settings = crossReferencer.getInverseReferences(target);
		for (EStructuralFeature.Setting setting : settings) {
			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getElement_OwnedElement()
					|| false == setting.getEObject() instanceof Message) {
				continue;
			}
			Message link = (Message) setting.getEObject();
			if (!MessageLostEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			Element src = link.getOwner();
			result.add(new UMLLinkDescriptor(src, target, link, UMLElementTypes.Message_LostEdge,
					MessageLostEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getIncomingTypeModelFacetLinks_Message_FoundEdge(Element target,
			CrossReferenceAdapter crossReferencer) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		Collection<EStructuralFeature.Setting> settings = crossReferencer.getInverseReferences(target);
		for (EStructuralFeature.Setting setting : settings) {
			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getElement_OwnedElement()
					|| false == setting.getEObject() instanceof Message) {
				continue;
			}
			Message link = (Message) setting.getEObject();
			if (!MessageFoundEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			Element src = link.getOwner();
			result.add(new UMLLinkDescriptor(src, target, link, UMLElementTypes.Message_FoundEdge,
					MessageFoundEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getIncomingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(
			Element target, CrossReferenceAdapter crossReferencer) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		Collection<EStructuralFeature.Setting> settings = crossReferencer.getInverseReferences(target);
		for (EStructuralFeature.Setting setting : settings) {
			if (setting.getEStructuralFeature() == UMLPackage.eINSTANCE.getComment_AnnotatedElement()) {
				result.add(new UMLLinkDescriptor(setting.getEObject(), target,
						UMLElementTypes.Comment_AnnotatedElementEdge, CommentAnnotatedElementEditPart.VISUAL_ID));
			}
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(
			Element target, CrossReferenceAdapter crossReferencer) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		Collection<EStructuralFeature.Setting> settings = crossReferencer.getInverseReferences(target);
		for (EStructuralFeature.Setting setting : settings) {
			if (setting.getEStructuralFeature() == UMLPackage.eINSTANCE.getConstraint_ConstrainedElement()) {
				result.add(new UMLLinkDescriptor(setting.getEObject(), target,
						UMLElementTypes.Constraint_ConstrainedElementEdge,
						ConstraintConstrainedElementEditPart.VISUAL_ID));
			}
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getIncomingTypeModelFacetLinks_GeneralOrdering_Edge(
			OccurrenceSpecification target, CrossReferenceAdapter crossReferencer) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		Collection<EStructuralFeature.Setting> settings = crossReferencer.getInverseReferences(target);
		for (EStructuralFeature.Setting setting : settings) {
			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getGeneralOrdering_After()
					|| false == setting.getEObject() instanceof GeneralOrdering) {
				continue;
			}
			GeneralOrdering link = (GeneralOrdering) setting.getEObject();
			if (!GeneralOrderingEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			OccurrenceSpecification src = link.getBefore();
			result.add(new UMLLinkDescriptor(src, target, link, UMLElementTypes.GeneralOrdering_Edge,
					GeneralOrderingEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getIncomingFeatureModelFacetLinks_Constraint_ContextEdge(Namespace target,
			CrossReferenceAdapter crossReferencer) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		Collection<EStructuralFeature.Setting> settings = crossReferencer.getInverseReferences(target);
		for (EStructuralFeature.Setting setting : settings) {
			if (setting.getEStructuralFeature() == UMLPackage.eINSTANCE.getConstraint_Context()) {
				result.add(new UMLLinkDescriptor(setting.getEObject(), target, UMLElementTypes.Constraint_ContextEdge,
						ContextLinkEditPart.VISUAL_ID));
			}
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getIncomingTypeModelFacetLinks_DurationConstraint_Edge(Element target,
			CrossReferenceAdapter crossReferencer) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		Collection<EStructuralFeature.Setting> settings = crossReferencer.getInverseReferences(target);
		for (EStructuralFeature.Setting setting : settings) {
			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getConstraint_ConstrainedElement()
					|| false == setting.getEObject() instanceof DurationConstraint) {
				continue;
			}
			DurationConstraint link = (DurationConstraint) setting.getEObject();
			if (!DurationConstraintLinkEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> sources = link.getConstrainedElements();
			Object theSource = sources.size() == 1 ? sources.get(0) : null;
			if (false == theSource instanceof Element) {
				continue;
			}
			Element src = (Element) theSource;
			result.add(new UMLLinkDescriptor(src, target, link, UMLElementTypes.DurationConstraint_Edge,
					DurationConstraintLinkEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getIncomingTypeModelFacetLinks_DurationObservation_Edge(NamedElement target,
			CrossReferenceAdapter crossReferencer) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		Collection<EStructuralFeature.Setting> settings = crossReferencer.getInverseReferences(target);
		for (EStructuralFeature.Setting setting : settings) {
			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getDurationObservation_Event()
					|| false == setting.getEObject() instanceof DurationObservation) {
				continue;
			}
			DurationObservation link = (DurationObservation) setting.getEObject();
			if (!DurationObservationLinkEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> sources = link.getEvents();
			Object theSource = sources.size() == 1 ? sources.get(0) : null;
			if (false == theSource instanceof NamedElement) {
				continue;
			}
			NamedElement src = (NamedElement) theSource;
			result.add(new UMLLinkDescriptor(src, target, link, UMLElementTypes.DurationObservation_Edge,
					DurationObservationLinkEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_Message_SynchEdge(Element source) {
		Interaction container = null;
		// Find container element for the link.
		// Climb up by containment hierarchy starting from the source
		// and return the first element that is instance of the container class.
		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
			if (element instanceof Interaction) {
				container = (Interaction) element;
			}
		}
		if (container == null) {
			return Collections.emptyList();
		}
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getMessages().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof Message) {
				continue;
			}
			Message link = (Message) linkObject;
			if (!MessageSyncEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getOwnedElements();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof Element) {
				continue;
			}
			Element dst = (Element) theTarget;
			Element src = link.getOwner();
			if (src != source) {
				continue;
			}
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.Message_SynchEdge,
					MessageSyncEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_Message_AsynchEdge(Element source) {
		Interaction container = null;
		// Find container element for the link.
		// Climb up by containment hierarchy starting from the source
		// and return the first element that is instance of the container class.
		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
			if (element instanceof Interaction) {
				container = (Interaction) element;
			}
		}
		if (container == null) {
			return Collections.emptyList();
		}
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getMessages().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof Message) {
				continue;
			}
			Message link = (Message) linkObject;
			if (!MessageAsyncEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getOwnedElements();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof Element) {
				continue;
			}
			Element dst = (Element) theTarget;
			Element src = link.getOwner();
			if (src != source) {
				continue;
			}
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.Message_AsynchEdge,
					MessageAsyncEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_Message_ReplyEdge(Element source) {
		Interaction container = null;
		// Find container element for the link.
		// Climb up by containment hierarchy starting from the source
		// and return the first element that is instance of the container class.
		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
			if (element instanceof Interaction) {
				container = (Interaction) element;
			}
		}
		if (container == null) {
			return Collections.emptyList();
		}
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getMessages().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof Message) {
				continue;
			}
			Message link = (Message) linkObject;
			if (!MessageReplyEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getOwnedElements();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof Element) {
				continue;
			}
			Element dst = (Element) theTarget;
			Element src = link.getOwner();
			if (src != source) {
				continue;
			}
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.Message_ReplyEdge,
					MessageReplyEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_Message_CreateEdge(Element source) {
		Interaction container = null;
		// Find container element for the link.
		// Climb up by containment hierarchy starting from the source
		// and return the first element that is instance of the container class.
		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
			if (element instanceof Interaction) {
				container = (Interaction) element;
			}
		}
		if (container == null) {
			return Collections.emptyList();
		}
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getMessages().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof Message) {
				continue;
			}
			Message link = (Message) linkObject;
			if (!MessageCreateEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getOwnedElements();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof Element) {
				continue;
			}
			Element dst = (Element) theTarget;
			Element src = link.getOwner();
			if (src != source) {
				continue;
			}
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.Message_CreateEdge,
					MessageCreateEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_Message_DeleteEdge(Element source) {
		Interaction container = null;
		// Find container element for the link.
		// Climb up by containment hierarchy starting from the source
		// and return the first element that is instance of the container class.
		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
			if (element instanceof Interaction) {
				container = (Interaction) element;
			}
		}
		if (container == null) {
			return Collections.emptyList();
		}
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getMessages().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof Message) {
				continue;
			}
			Message link = (Message) linkObject;
			if (!MessageDeleteEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getOwnedElements();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof Element) {
				continue;
			}
			Element dst = (Element) theTarget;
			Element src = link.getOwner();
			if (src != source) {
				continue;
			}
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.Message_DeleteEdge,
					MessageDeleteEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_Message_LostEdge(Element source) {
		Interaction container = null;
		// Find container element for the link.
		// Climb up by containment hierarchy starting from the source
		// and return the first element that is instance of the container class.
		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
			if (element instanceof Interaction) {
				container = (Interaction) element;
			}
		}
		if (container == null) {
			return Collections.emptyList();
		}
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getMessages().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof Message) {
				continue;
			}
			Message link = (Message) linkObject;
			if (!MessageLostEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getOwnedElements();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof Element) {
				continue;
			}
			Element dst = (Element) theTarget;
			Element src = link.getOwner();
			if (src != source) {
				continue;
			}
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.Message_LostEdge,
					MessageLostEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_Message_FoundEdge(Element source) {
		Interaction container = null;
		// Find container element for the link.
		// Climb up by containment hierarchy starting from the source
		// and return the first element that is instance of the container class.
		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
			if (element instanceof Interaction) {
				container = (Interaction) element;
			}
		}
		if (container == null) {
			return Collections.emptyList();
		}
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getMessages().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof Message) {
				continue;
			}
			Message link = (Message) linkObject;
			if (!MessageFoundEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getOwnedElements();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof Element) {
				continue;
			}
			Element dst = (Element) theTarget;
			Element src = link.getOwner();
			if (src != source) {
				continue;
			}
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.Message_FoundEdge,
					MessageFoundEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getOutgoingFeatureModelFacetLinks_Comment_AnnotatedElementEdge(
			Comment source) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> destinations = source.getAnnotatedElements().iterator(); destinations.hasNext();) {
			Element destination = (Element) destinations.next();
			result.add(new UMLLinkDescriptor(source, destination, UMLElementTypes.Comment_AnnotatedElementEdge,
					CommentAnnotatedElementEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getOutgoingFeatureModelFacetLinks_Constraint_ConstrainedElementEdge(
			Constraint source) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> destinations = source.getConstrainedElements().iterator(); destinations.hasNext();) {
			Element destination = (Element) destinations.next();
			result.add(new UMLLinkDescriptor(source, destination, UMLElementTypes.Constraint_ConstrainedElementEdge,
					ConstraintConstrainedElementEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_GeneralOrdering_Edge(
			OccurrenceSpecification source) {
		InteractionFragment container = null;
		// Find container element for the link.
		// Climb up by containment hierarchy starting from the source
		// and return the first element that is instance of the container class.
		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
			if (element instanceof InteractionFragment) {
				container = (InteractionFragment) element;
			}
		}
		if (container == null) {
			return Collections.emptyList();
		}
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getGeneralOrderings().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof GeneralOrdering) {
				continue;
			}
			GeneralOrdering link = (GeneralOrdering) linkObject;
			if (!GeneralOrderingEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			OccurrenceSpecification dst = link.getAfter();
			OccurrenceSpecification src = link.getBefore();
			if (src != source) {
				continue;
			}
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.GeneralOrdering_Edge,
					GeneralOrderingEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getOutgoingFeatureModelFacetLinks_Constraint_ContextEdge(
			Constraint source) {
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		Namespace destination = source.getContext();
		if (destination == null) {
			return result;
		}
		result.add(new UMLLinkDescriptor(source, destination, UMLElementTypes.Constraint_ContextEdge,
				ContextLinkEditPart.VISUAL_ID));
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_DurationConstraint_Edge(Element source) {
		Namespace container = null;
		// Find container element for the link.
		// Climb up by containment hierarchy starting from the source
		// and return the first element that is instance of the container class.
		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
			if (element instanceof Namespace) {
				container = (Namespace) element;
			}
		}
		if (container == null) {
			return Collections.emptyList();
		}
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getOwnedRules().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof DurationConstraint) {
				continue;
			}
			DurationConstraint link = (DurationConstraint) linkObject;
			if (!DurationConstraintLinkEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getConstrainedElements();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof Element) {
				continue;
			}
			Element dst = (Element) theTarget;
			List<?> sources = link.getConstrainedElements();
			Object theSource = sources.size() == 1 ? sources.get(0) : null;
			if (false == theSource instanceof Element) {
				continue;
			}
			Element src = (Element) theSource;
			if (src != source) {
				continue;
			}
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.DurationConstraint_Edge,
					DurationConstraintLinkEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected Collection<UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_DurationObservation_Edge(
			NamedElement source) {
		Package container = null;
		// Find container element for the link.
		// Climb up by containment hierarchy starting from the source
		// and return the first element that is instance of the container class.
		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
			if (element instanceof Package) {
				container = (Package) element;
			}
		}
		if (container == null) {
			return Collections.emptyList();
		}
		LinkedList<UMLLinkDescriptor> result = new LinkedList<>();
		for (Iterator<?> links = container.getPackagedElements().iterator(); links.hasNext();) {
			EObject linkObject = (EObject) links.next();
			if (false == linkObject instanceof DurationObservation) {
				continue;
			}
			DurationObservation link = (DurationObservation) linkObject;
			if (!DurationObservationLinkEditPart.VISUAL_ID.equals(UMLVisualIDRegistry.getLinkWithClassVisualID(link))) {
				continue;
			}
			List<?> targets = link.getEvents();
			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
			if (false == theTarget instanceof NamedElement) {
				continue;
			}
			NamedElement dst = (NamedElement) theTarget;
			List<?> sources = link.getEvents();
			Object theSource = sources.size() == 1 ? sources.get(0) : null;
			if (false == theSource instanceof NamedElement) {
				continue;
			}
			NamedElement src = (NamedElement) theSource;
			if (src != source) {
				continue;
			}
			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.DurationObservation_Edge,
					DurationObservationLinkEditPart.VISUAL_ID));
		}
		return result;
	}

}
