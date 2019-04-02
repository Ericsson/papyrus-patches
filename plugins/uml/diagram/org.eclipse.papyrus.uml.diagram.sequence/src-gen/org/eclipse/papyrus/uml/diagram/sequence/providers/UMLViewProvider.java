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
 *   Vincent Lorenzo - vincent.lorenzo@cea.fr - CEA - LIST
 *   Christian W. Damus - bug 536486
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.core.providers.IViewProvider;
import org.eclipse.gmf.runtime.diagram.core.services.view.CreateChildViewOperation;
import org.eclipse.gmf.runtime.diagram.core.services.view.CreateDiagramViewOperation;
import org.eclipse.gmf.runtime.diagram.core.services.view.CreateEdgeViewOperation;
import org.eclipse.gmf.runtime.diagram.core.services.view.CreateNodeViewOperation;
import org.eclipse.gmf.runtime.diagram.core.services.view.CreateViewForKindOperation;
import org.eclipse.gmf.runtime.diagram.core.services.view.CreateViewOperation;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.draw2d.ui.figures.FigureUtilities;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.IHintedType;
import org.eclipse.gmf.runtime.notation.Connector;
import org.eclipse.gmf.runtime.notation.DecorationNode;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.FillStyle;
import org.eclipse.gmf.runtime.notation.FontStyle;
import org.eclipse.gmf.runtime.notation.Location;
import org.eclipse.gmf.runtime.notation.MeasurementUnit;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.RelativeBendpoints;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.gmf.runtime.notation.TitleStyle;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.gmf.runtime.notation.datatype.RelativeBendpoint;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.papyrus.infra.gmfdiag.common.preferences.PreferencesConstantsHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.reconciler.DiagramVersioningUtils;
import org.eclipse.papyrus.infra.gmfdiag.preferences.utils.GradientPreferenceConverter;
import org.eclipse.papyrus.uml.diagram.common.helper.PreferenceInitializerForElementHelper;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ActionExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.BehaviorExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentCombinedFragmentCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CommentAnnotatedElementEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CommentBodyEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CommentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ConsiderIgnoreFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.Constraint2EditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ConstraintConstrainedElementEditPart;
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
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;

/**
 * @generated
 */
public class UMLViewProvider extends AbstractProvider implements IViewProvider {

	/**
	 * @generated
	 */
	@Override
	public final boolean provides(IOperation operation) {
		if (operation instanceof CreateViewForKindOperation) {
			return provides((CreateViewForKindOperation) operation);
		}
		assert operation instanceof CreateViewOperation;

		/* we check this view provider is the good one for the currently edited diagram */
		if (operation instanceof CreateChildViewOperation) {
			View container = ((CreateChildViewOperation) operation).getContainerView();
			Diagram diagram = container.getDiagram();
			if (!getDiagramProvidedId().equals(diagram.getType())) {
				return false;
			}
		}

		if (operation instanceof CreateDiagramViewOperation) {
			return provides((CreateDiagramViewOperation) operation);
		} else if (operation instanceof CreateEdgeViewOperation) {
			return provides((CreateEdgeViewOperation) operation);
		} else if (operation instanceof CreateNodeViewOperation) {
			return provides((CreateNodeViewOperation) operation);
		}
		return false;
	}

	/**
	 * @generated
	 */
	protected boolean provides(CreateViewForKindOperation op) {
		/*
		 * if (op.getViewKind() == Node.class)
		 * return getNodeViewClass(op.getSemanticAdapter(), op.getContainerView(), op.getSemanticHint()) != null;
		 * if (op.getViewKind() == Edge.class)
		 * return getEdgeViewClass(op.getSemanticAdapter(), op.getContainerView(), op.getSemanticHint()) != null;
		 */

		// check Diagram Type should be the class diagram
		String modelID = UMLVisualIDRegistry.getModelID(op.getContainerView());
		if (!getDiagramProvidedId().equals(modelID)) {
			return false;
		}

		String visualID = UMLVisualIDRegistry.getVisualID(op.getSemanticHint());
		if (Node.class.isAssignableFrom(op.getViewKind())) {
			return UMLVisualIDRegistry.canCreateNode(op.getContainerView(), visualID);
		}

		return true;
	}

	/**
	 * @generated
	 */
	protected String getDiagramProvidedId() {
		/*
		 * Indicates for which diagram this provider works for.
		 * <p>
		 * This method can be overloaded when diagram editor inherits from another one, but should never be <code>null</code>
		 * </p>
		 * 
		 * @return the unique identifier of the diagram for which views are provided.
		 */
		return SequenceDiagramEditPart.MODEL_ID;
	}

	/**
	 * @generated
	 */
	protected boolean provides(CreateDiagramViewOperation op) {
		return SequenceDiagramEditPart.MODEL_ID.equals(op.getSemanticHint())
				&& UMLVisualIDRegistry.getDiagramVisualID(getSemanticElement(op.getSemanticAdapter())) != null
				&& !UMLVisualIDRegistry.getDiagramVisualID(getSemanticElement(op.getSemanticAdapter())).isEmpty();
	}

	/**
	 * @generated
	 */
	protected boolean provides(CreateNodeViewOperation op) {
		if (op.getContainerView() == null) {
			return false;
		}
		IElementType elementType = getSemanticElementType(op.getSemanticAdapter());
		EObject domainElement = getSemanticElement(op.getSemanticAdapter());
		String visualID;
		if (op.getSemanticHint() == null) {
			// Semantic hint is not specified. Can be a result of call from CanonicalEditPolicy.
			// In this situation there should be NO elementType, visualID will be determined
			// by VisualIDRegistry.getNodeVisualID() for domainElement.
			if (elementType != null || domainElement == null) {
				return false;
			}
			visualID = UMLVisualIDRegistry.getNodeVisualID(op.getContainerView(), domainElement);
		} else {
			visualID = UMLVisualIDRegistry.getVisualID(op.getSemanticHint());
			if (elementType != null) {

				if (!UMLElementTypes.isKnownElementType(elementType) || (!(elementType instanceof IHintedType))) {
					return false; // foreign element type
				}

				String elementTypeHint = ((IHintedType) elementType).getSemanticHint();
				if (!op.getSemanticHint().equals(elementTypeHint)) {
					return false; // if semantic hint is specified it should be the same as in element type
				}
				// if (domainElement != null && !visualID.equals(org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry.getNodeVisualID(op.getContainerView(), domainElement))) {
				// return false; // visual id for node EClass should match visual id from element type
				// }
			} else {
				if (!SequenceDiagramEditPart.MODEL_ID.equals(UMLVisualIDRegistry.getModelID(op.getContainerView()))) {
					return false; // foreign diagram
				}
				if (visualID != null) {
					switch (visualID) {
					case InteractionEditPart.VISUAL_ID:
					case ConsiderIgnoreFragmentEditPart.VISUAL_ID:
					case CombinedFragmentEditPart.VISUAL_ID:
					case InteractionOperandEditPart.VISUAL_ID:
					case InteractionUseEditPart.VISUAL_ID:
					case ContinuationEditPart.VISUAL_ID:
					case LifelineEditPart.VISUAL_ID:
					case ActionExecutionSpecificationEditPart.VISUAL_ID:
					case BehaviorExecutionSpecificationEditPart.VISUAL_ID:
					case StateInvariantEditPart.VISUAL_ID:
					case DestructionOccurrenceSpecificationEditPart.VISUAL_ID:
					case ConstraintEditPart.VISUAL_ID:
					case CommentEditPart.VISUAL_ID:
					case GateEditPart.VISUAL_ID:
					case TimeConstraintBorderNodeEditPart.VISUAL_ID:
					case TimeObservationBorderNodeEditPart.VISUAL_ID:
						if (domainElement == null || !visualID
								.equals(UMLVisualIDRegistry.getNodeVisualID(op.getContainerView(), domainElement))) {
							return false; // visual id in semantic hint should match visual id for domain element
						}
						break;
					default:
						return false;
					}
				}
			}
		}

		return UMLVisualIDRegistry.canCreateNode(op.getContainerView(), visualID);
	}

	/**
	 * @generated
	 */
	protected boolean provides(CreateEdgeViewOperation op) {
		IElementType elementType = getSemanticElementType(op.getSemanticAdapter());

		if (!UMLElementTypes.isKnownElementType(elementType) || (!(elementType instanceof IHintedType))) {
			return false; // foreign element type
		}

		String elementTypeHint = ((IHintedType) elementType).getSemanticHint();
		if (elementTypeHint == null
				|| (op.getSemanticHint() != null && !elementTypeHint.equals(op.getSemanticHint()))) {
			return false; // our hint is visual id and must be specified, and it should be the same as in element type
		}
		// String visualID = org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry.getVisualID(elementTypeHint);
		// org.eclipse.emf.ecore.EObject domainElement = getSemanticElement(op.getSemanticAdapter());
		// if (domainElement != null && !visualID.equals(org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry.getLinkWithClassVisualID(domainElement))) {
		// return false; // visual id for link EClass should match visual id from element type
		// }
		return true;
	}

	/**
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Diagram createDiagram(IAdaptable semanticAdapter, String diagramKind, PreferencesHint preferencesHint) {
		Diagram diagram = NotationFactory.eINSTANCE.createDiagram();
		DiagramVersioningUtils.stampCurrentVersion(diagram);
		diagram.getStyles().add(NotationFactory.eINSTANCE.createDiagramStyle());
		diagram.setType(SequenceDiagramEditPart.MODEL_ID);
		diagram.setElement(getSemanticElement(semanticAdapter));
		diagram.setMeasurementUnit(MeasurementUnit.PIXEL_LITERAL);
		return diagram;
	}

	/**
	 * @generated
	 */
	@Override
	public Node createNode(IAdaptable semanticAdapter, View containerView, String semanticHint, int index,
			boolean persisted, PreferencesHint preferencesHint) {
		final EObject domainElement = getSemanticElement(semanticAdapter);
		final String visualID;
		if (semanticHint == null) {
			visualID = UMLVisualIDRegistry.getNodeVisualID(containerView, domainElement);
		} else {
			visualID = UMLVisualIDRegistry.getVisualID(semanticHint);
		}
		if (visualID != null) {
			switch (visualID) {
			case InteractionEditPart.VISUAL_ID:
				return createInteraction_Shape(domainElement, containerView, index, persisted, preferencesHint);
			case ConsiderIgnoreFragmentEditPart.VISUAL_ID:
				return createConsiderIgnoreFragment_Shape(domainElement, containerView, index, persisted,
						preferencesHint);
			case CombinedFragmentEditPart.VISUAL_ID:
				return createCombinedFragment_Shape(domainElement, containerView, index, persisted, preferencesHint);
			case InteractionOperandEditPart.VISUAL_ID:
				return createInteractionOperand_Shape(domainElement, containerView, index, persisted, preferencesHint);
			case InteractionUseEditPart.VISUAL_ID:
				return createInteractionUse_Shape(domainElement, containerView, index, persisted, preferencesHint);
			case ContinuationEditPart.VISUAL_ID:
				return createContinuation_Shape(domainElement, containerView, index, persisted, preferencesHint);
			case LifelineEditPart.VISUAL_ID:
				return createLifeline_Shape(domainElement, containerView, index, persisted, preferencesHint);
			case ActionExecutionSpecificationEditPart.VISUAL_ID:
				return createActionExecutionSpecification_Shape(domainElement, containerView, index, persisted,
						preferencesHint);
			case BehaviorExecutionSpecificationEditPart.VISUAL_ID:
				return createBehaviorExecutionSpecification_Shape(domainElement, containerView, index, persisted,
						preferencesHint);
			case StateInvariantEditPart.VISUAL_ID:
				return createStateInvariant_Shape(domainElement, containerView, index, persisted, preferencesHint);
			case DestructionOccurrenceSpecificationEditPart.VISUAL_ID:
				return createDestructionOccurrenceSpecification_Shape(domainElement, containerView, index, persisted,
						preferencesHint);
			case ConstraintEditPart.VISUAL_ID:
				return createConstraint_Shape(domainElement, containerView, index, persisted, preferencesHint);
			case CommentEditPart.VISUAL_ID:
				return createComment_Shape(domainElement, containerView, index, persisted, preferencesHint);
			case GateEditPart.VISUAL_ID:
				return createGate_Shape(domainElement, containerView, index, persisted, preferencesHint);
			case TimeConstraintBorderNodeEditPart.VISUAL_ID:
				return createTimeConstraint_Shape(domainElement, containerView, index, persisted, preferencesHint);
			case TimeObservationBorderNodeEditPart.VISUAL_ID:
				return createTimeObservation_Shape(domainElement, containerView, index, persisted, preferencesHint);
			}
		}
		// can't happen, provided #provides(CreateNodeViewOperation) is correct
		return null;
	}

	/**
	 * @generated
	 */
	@Override
	public Edge createEdge(IAdaptable semanticAdapter, View containerView, String semanticHint, int index,
			boolean persisted, PreferencesHint preferencesHint) {
		IElementType elementType = getSemanticElementType(semanticAdapter);
		String elementTypeHint = ((IHintedType) elementType).getSemanticHint();
		String vid = UMLVisualIDRegistry.getVisualID(elementTypeHint);
		if (vid != null) {
			switch (vid) {
			case MessageSyncEditPart.VISUAL_ID:
				return createMessage_SynchEdge(getSemanticElement(semanticAdapter), containerView, index, persisted,
						preferencesHint);
			case MessageAsyncEditPart.VISUAL_ID:
				return createMessage_AsynchEdge(getSemanticElement(semanticAdapter), containerView, index, persisted,
						preferencesHint);
			case MessageReplyEditPart.VISUAL_ID:
				return createMessage_ReplyEdge(getSemanticElement(semanticAdapter), containerView, index, persisted,
						preferencesHint);
			case MessageCreateEditPart.VISUAL_ID:
				return createMessage_CreateEdge(getSemanticElement(semanticAdapter), containerView, index, persisted,
						preferencesHint);
			case MessageDeleteEditPart.VISUAL_ID:
				return createMessage_DeleteEdge(getSemanticElement(semanticAdapter), containerView, index, persisted,
						preferencesHint);
			case MessageLostEditPart.VISUAL_ID:
				return createMessage_LostEdge(getSemanticElement(semanticAdapter), containerView, index, persisted,
						preferencesHint);
			case MessageFoundEditPart.VISUAL_ID:
				return createMessage_FoundEdge(getSemanticElement(semanticAdapter), containerView, index, persisted,
						preferencesHint);
			case CommentAnnotatedElementEditPart.VISUAL_ID:
				return createComment_AnnotatedElementEdge(containerView, index, persisted, preferencesHint);
			case ConstraintConstrainedElementEditPart.VISUAL_ID:
				return createConstraint_ConstrainedElementEdge(containerView, index, persisted, preferencesHint);
			case GeneralOrderingEditPart.VISUAL_ID:
				return createGeneralOrdering_Edge(getSemanticElement(semanticAdapter), containerView, index, persisted,
						preferencesHint);
			case ContextLinkEditPart.VISUAL_ID:
				return createConstraint_ContextEdge(containerView, index, persisted, preferencesHint);
			case DurationConstraintLinkEditPart.VISUAL_ID:
				return createDurationConstraint_Edge(getSemanticElement(semanticAdapter), containerView, index,
						persisted, preferencesHint);
			case DurationObservationLinkEditPart.VISUAL_ID:
				return createDurationObservation_Edge(getSemanticElement(semanticAdapter), containerView, index,
						persisted, preferencesHint);
			}
		}
		// can never happen, provided #provides(CreateEdgeViewOperation) is correct
		return null;
	}

	/**
	 * @generated
	 */
	public Node createInteraction_Shape(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry.getType(InteractionEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		stampShortcut(containerView, node);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node, prefStore, "Interaction");
		Node interaction_NameLabel = createLabel(node, UMLVisualIDRegistry.getType(InteractionNameEditPart.VISUAL_ID));
		createCompartment(node, UMLVisualIDRegistry.getType(InteractionInteractionCompartmentEditPart.VISUAL_ID), false,
				false, false, false);
		PreferenceInitializerForElementHelper.initCompartmentsStatusFromPrefs(node, prefStore, "Interaction");
		return node;
	}

	/**
	 * @generated
	 */
	public Node createConsiderIgnoreFragment_Shape(EObject domainElement, View containerView, int index,
			boolean persisted, PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry.getType(ConsiderIgnoreFragmentEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node, prefStore, "ConsiderIgnoreFragment");
		return node;
	}

	/**
	 * @generated
	 */
	public Node createCombinedFragment_Shape(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry.getType(CombinedFragmentEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node, prefStore, "CombinedFragment");
		createCompartment(node,
				UMLVisualIDRegistry.getType(CombinedFragmentCombinedFragmentCompartmentEditPart.VISUAL_ID), false,
				false, true, true);
		PreferenceInitializerForElementHelper.initCompartmentsStatusFromPrefs(node, prefStore, "CombinedFragment");
		return node;
	}

	/**
	 * @generated
	 */
	public Node createInteractionOperand_Shape(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry.getType(InteractionOperandEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node, prefStore, "InteractionOperand");
		return node;
	}

	/**
	 * @generated
	 */
	public Node createInteractionUse_Shape(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.getStyles().add(NotationFactory.eINSTANCE.createHintedDiagramLinkStyle());
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry.getType(InteractionUseEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node, prefStore, "InteractionUse");
		Node interactionUse_NameLabel = createLabel(node,
				UMLVisualIDRegistry.getType(InteractionUseNameEditPart.VISUAL_ID));
		Node interactionUse_TypeLabel = createLabel(node,
				UMLVisualIDRegistry.getType(InteractionUseName2EditPart.VISUAL_ID));
		return node;
	}

	/**
	 * @generated
	 */
	public Node createContinuation_Shape(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry.getType(ContinuationEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node, prefStore, "Continuation");
		Node continuation_NameLabel = createLabel(node,
				UMLVisualIDRegistry.getType(ContinuationNameEditPart.VISUAL_ID));
		return node;
	}

	/**
	 * @generated
	 */
	public Node createLifeline_Shape(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry.getType(LifelineEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node, prefStore, "Lifeline");
		Node lifeline_NameLabel = createLabel(node, UMLVisualIDRegistry.getType(LifelineNameEditPart.VISUAL_ID));
		return node;
	}

	/**
	 * @generated
	 */
	public Node createActionExecutionSpecification_Shape(EObject domainElement, View containerView, int index,
			boolean persisted, PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry.getType(ActionExecutionSpecificationEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node, prefStore, "ActionExecutionSpecification");
		return node;
	}

	/**
	 * @generated
	 */
	public Node createBehaviorExecutionSpecification_Shape(EObject domainElement, View containerView, int index,
			boolean persisted, PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry.getType(BehaviorExecutionSpecificationEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node, prefStore, "BehaviorExecutionSpecification");
		return node;
	}

	/**
	 * @generated
	 */
	public Node createStateInvariant_Shape(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry.getType(StateInvariantEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node, prefStore, "StateInvariant");
		Node stateInvariant_NameLabel = createLabel(node,
				UMLVisualIDRegistry.getType(StateInvariantNameEditPart.VISUAL_ID));
		Node stateInvariant_ConstraintLabel = createLabel(node,
				UMLVisualIDRegistry.getType(StateInvariantLabelEditPart.VISUAL_ID));
		return node;
	}

	/**
	 * @generated
	 */
	public Node createDestructionOccurrenceSpecification_Shape(EObject domainElement, View containerView, int index,
			boolean persisted, PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry.getType(DestructionOccurrenceSpecificationEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node, prefStore,
				"DestructionOccurrenceSpecification");
		return node;
	}

	/**
	 * @generated
	 */
	public Node createConstraint_Shape(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry.getType(ConstraintEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node, prefStore, "Constraint");
		Node constraint_NameLabel = createLabel(node, UMLVisualIDRegistry.getType(ConstraintNameEditPart.VISUAL_ID));
		Node constraint_BodyLabel = createLabel(node, UMLVisualIDRegistry.getType(Constraint2EditPart.VISUAL_ID));
		return node;
	}

	/**
	 * @generated
	 */
	public Node createComment_Shape(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry.getType(CommentEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node, prefStore, "Comment");
		Node comment_BodyLabel = createLabel(node, UMLVisualIDRegistry.getType(CommentBodyEditPart.VISUAL_ID));
		return node;
	}

	/**
	 * @generated
	 */
	public Node createGate_Shape(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry.getType(GateEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node, prefStore, "Gate");
		Node gate_NameLabel = createLabel(node, UMLVisualIDRegistry.getType(GateNameEditPart.VISUAL_ID));
		gate_NameLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location gate_NameLabel_Location = (Location) gate_NameLabel.getLayoutConstraint();
		gate_NameLabel_Location.setX(25);
		gate_NameLabel_Location.setY(3);
		PreferenceInitializerForElementHelper.initLabelVisibilityFromPrefs(node, prefStore, "Gate");
		return node;
	}

	/**
	 * @generated
	 */
	public Node createTimeConstraint_Shape(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry.getType(TimeConstraintBorderNodeEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node, prefStore, "TimeConstraint");
		Node timeConstraint_NameLabel = createLabel(node,
				UMLVisualIDRegistry.getType(TimeConstraintNameEditPart.VISUAL_ID));
		timeConstraint_NameLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location timeConstraint_NameLabel_Location = (Location) timeConstraint_NameLabel.getLayoutConstraint();
		timeConstraint_NameLabel_Location.setX(25);
		timeConstraint_NameLabel_Location.setY(3);
		Node timeConstraint_StereotypeLabel = createLabel(node,
				UMLVisualIDRegistry.getType(TimeConstraintAppliedStereotypeEditPart.VISUAL_ID));
		timeConstraint_StereotypeLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location timeConstraint_StereotypeLabel_Location = (Location) timeConstraint_StereotypeLabel
				.getLayoutConstraint();
		timeConstraint_StereotypeLabel_Location.setX(0);
		timeConstraint_StereotypeLabel_Location.setY(-22);
		return node;
	}

	/**
	 * @generated
	 */
	public Node createTimeObservation_Shape(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry.getType(TimeObservationBorderNodeEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node, prefStore, "TimeObservation");
		Node timeObservation_NameLabel = createLabel(node,
				UMLVisualIDRegistry.getType(TimeObservationNameEditPart.VISUAL_ID));
		timeObservation_NameLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location timeObservation_NameLabel_Location = (Location) timeObservation_NameLabel.getLayoutConstraint();
		timeObservation_NameLabel_Location.setX(25);
		timeObservation_NameLabel_Location.setY(3);
		Node timeObservation_StereotypeLabel = createLabel(node,
				UMLVisualIDRegistry.getType(TimeObservationAppliedStereotypeEditPart.VISUAL_ID));
		timeObservation_StereotypeLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location timeObservation_StereotypeLabel_Location = (Location) timeObservation_StereotypeLabel
				.getLayoutConstraint();
		timeObservation_StereotypeLabel_Location.setX(0);
		timeObservation_StereotypeLabel_Location.setY(-22);
		return node;
	}

	/**
	 * @generated
	 */
	public Edge createMessage_SynchEdge(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Connector edge = NotationFactory.eINSTANCE.createConnector();
		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
		List<RelativeBendpoint> points = new ArrayList<>(2);
		points.add(new RelativeBendpoint());
		points.add(new RelativeBendpoint());
		bendpoints.setPoints(points);
		edge.setBendpoints(bendpoints);
		ViewUtil.insertChildView(containerView, edge, index, persisted);
		edge.setType(UMLVisualIDRegistry.getType(MessageSyncEditPart.VISUAL_ID));
		edge.setElement(domainElement);
		// initializePreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(edge, prefStore, "Message");
		// org.eclipse.gmf.runtime.notation.Routing routing = org.eclipse.gmf.runtime.notation.Routing.get(prefStore.getInt(org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants.PREF_LINE_STYLE));
		// if (routing != null) {
		// org.eclipse.gmf.runtime.diagram.core.util.ViewUtil.setStructuralFeatureValue(edge, org.eclipse.gmf.runtime.notation.NotationPackage.eINSTANCE.getRoutingStyle_Routing(), routing);
		// }
		Node message_SynchNameLabel = createLabel(edge, UMLVisualIDRegistry.getType(MessageSyncNameEditPart.VISUAL_ID));
		message_SynchNameLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location message_SynchNameLabel_Location = (Location) message_SynchNameLabel.getLayoutConstraint();
		message_SynchNameLabel_Location.setX(1);
		message_SynchNameLabel_Location.setY(-13);
		Node message_SynchStereotypeLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(MessageSyncAppliedStereotypeEditPart.VISUAL_ID));
		message_SynchStereotypeLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location message_SynchStereotypeLabel_Location = (Location) message_SynchStereotypeLabel.getLayoutConstraint();
		message_SynchStereotypeLabel_Location.setX(1);
		message_SynchStereotypeLabel_Location.setY(-33);

		PreferenceInitializerForElementHelper.initLabelVisibilityFromPrefs(edge, prefStore, "Message");
		return edge;
	}

	/**
	 * @generated
	 */
	public Edge createMessage_AsynchEdge(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Connector edge = NotationFactory.eINSTANCE.createConnector();
		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
		List<RelativeBendpoint> points = new ArrayList<>(2);
		points.add(new RelativeBendpoint());
		points.add(new RelativeBendpoint());
		bendpoints.setPoints(points);
		edge.setBendpoints(bendpoints);
		ViewUtil.insertChildView(containerView, edge, index, persisted);
		edge.setType(UMLVisualIDRegistry.getType(MessageAsyncEditPart.VISUAL_ID));
		edge.setElement(domainElement);
		// initializePreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(edge, prefStore, "Message");
		// org.eclipse.gmf.runtime.notation.Routing routing = org.eclipse.gmf.runtime.notation.Routing.get(prefStore.getInt(org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants.PREF_LINE_STYLE));
		// if (routing != null) {
		// org.eclipse.gmf.runtime.diagram.core.util.ViewUtil.setStructuralFeatureValue(edge, org.eclipse.gmf.runtime.notation.NotationPackage.eINSTANCE.getRoutingStyle_Routing(), routing);
		// }
		Node message_AsynchNameLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(MessageAsyncNameEditPart.VISUAL_ID));
		message_AsynchNameLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location message_AsynchNameLabel_Location = (Location) message_AsynchNameLabel.getLayoutConstraint();
		message_AsynchNameLabel_Location.setX(1);
		message_AsynchNameLabel_Location.setY(-13);
		Node message_AsynchStereotypeLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(MessageAsyncAppliedStereotypeEditPart.VISUAL_ID));
		message_AsynchStereotypeLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location message_AsynchStereotypeLabel_Location = (Location) message_AsynchStereotypeLabel
				.getLayoutConstraint();
		message_AsynchStereotypeLabel_Location.setX(1);
		message_AsynchStereotypeLabel_Location.setY(-33);

		PreferenceInitializerForElementHelper.initLabelVisibilityFromPrefs(edge, prefStore, "Message");
		return edge;
	}

	/**
	 * @generated
	 */
	public Edge createMessage_ReplyEdge(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Connector edge = NotationFactory.eINSTANCE.createConnector();
		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
		List<RelativeBendpoint> points = new ArrayList<>(2);
		points.add(new RelativeBendpoint());
		points.add(new RelativeBendpoint());
		bendpoints.setPoints(points);
		edge.setBendpoints(bendpoints);
		ViewUtil.insertChildView(containerView, edge, index, persisted);
		edge.setType(UMLVisualIDRegistry.getType(MessageReplyEditPart.VISUAL_ID));
		edge.setElement(domainElement);
		// initializePreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(edge, prefStore, "Message");
		// org.eclipse.gmf.runtime.notation.Routing routing = org.eclipse.gmf.runtime.notation.Routing.get(prefStore.getInt(org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants.PREF_LINE_STYLE));
		// if (routing != null) {
		// org.eclipse.gmf.runtime.diagram.core.util.ViewUtil.setStructuralFeatureValue(edge, org.eclipse.gmf.runtime.notation.NotationPackage.eINSTANCE.getRoutingStyle_Routing(), routing);
		// }
		Node message_ReplyNameLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(MessageReplyNameEditPart.VISUAL_ID));
		message_ReplyNameLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location message_ReplyNameLabel_Location = (Location) message_ReplyNameLabel.getLayoutConstraint();
		message_ReplyNameLabel_Location.setX(1);
		message_ReplyNameLabel_Location.setY(-13);
		Node message_ReplyStereotypeLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(MessageReplyAppliedStereotypeEditPart.VISUAL_ID));
		message_ReplyStereotypeLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location message_ReplyStereotypeLabel_Location = (Location) message_ReplyStereotypeLabel.getLayoutConstraint();
		message_ReplyStereotypeLabel_Location.setX(1);
		message_ReplyStereotypeLabel_Location.setY(-33);

		PreferenceInitializerForElementHelper.initLabelVisibilityFromPrefs(edge, prefStore, "Message");
		return edge;
	}

	/**
	 * @generated
	 */
	public Edge createMessage_CreateEdge(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Connector edge = NotationFactory.eINSTANCE.createConnector();
		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
		List<RelativeBendpoint> points = new ArrayList<>(2);
		points.add(new RelativeBendpoint());
		points.add(new RelativeBendpoint());
		bendpoints.setPoints(points);
		edge.setBendpoints(bendpoints);
		ViewUtil.insertChildView(containerView, edge, index, persisted);
		edge.setType(UMLVisualIDRegistry.getType(MessageCreateEditPart.VISUAL_ID));
		edge.setElement(domainElement);
		// initializePreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(edge, prefStore, "Message");
		// org.eclipse.gmf.runtime.notation.Routing routing = org.eclipse.gmf.runtime.notation.Routing.get(prefStore.getInt(org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants.PREF_LINE_STYLE));
		// if (routing != null) {
		// org.eclipse.gmf.runtime.diagram.core.util.ViewUtil.setStructuralFeatureValue(edge, org.eclipse.gmf.runtime.notation.NotationPackage.eINSTANCE.getRoutingStyle_Routing(), routing);
		// }
		Node message_CreateNameLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(MessageCreateNameEditPart.VISUAL_ID));
		message_CreateNameLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location message_CreateNameLabel_Location = (Location) message_CreateNameLabel.getLayoutConstraint();
		message_CreateNameLabel_Location.setX(1);
		message_CreateNameLabel_Location.setY(-13);
		Node message_CreateStereotypeLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(MessageCreateAppliedStereotypeEditPart.VISUAL_ID));
		message_CreateStereotypeLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location message_CreateStereotypeLabel_Location = (Location) message_CreateStereotypeLabel
				.getLayoutConstraint();
		message_CreateStereotypeLabel_Location.setX(1);
		message_CreateStereotypeLabel_Location.setY(-33);

		PreferenceInitializerForElementHelper.initLabelVisibilityFromPrefs(edge, prefStore, "Message");
		return edge;
	}

	/**
	 * @generated
	 */
	public Edge createMessage_DeleteEdge(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Connector edge = NotationFactory.eINSTANCE.createConnector();
		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
		List<RelativeBendpoint> points = new ArrayList<>(2);
		points.add(new RelativeBendpoint());
		points.add(new RelativeBendpoint());
		bendpoints.setPoints(points);
		edge.setBendpoints(bendpoints);
		ViewUtil.insertChildView(containerView, edge, index, persisted);
		edge.setType(UMLVisualIDRegistry.getType(MessageDeleteEditPart.VISUAL_ID));
		edge.setElement(domainElement);
		// initializePreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(edge, prefStore, "Message");
		// org.eclipse.gmf.runtime.notation.Routing routing = org.eclipse.gmf.runtime.notation.Routing.get(prefStore.getInt(org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants.PREF_LINE_STYLE));
		// if (routing != null) {
		// org.eclipse.gmf.runtime.diagram.core.util.ViewUtil.setStructuralFeatureValue(edge, org.eclipse.gmf.runtime.notation.NotationPackage.eINSTANCE.getRoutingStyle_Routing(), routing);
		// }
		Node message_DeleteNameLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(MessageDeleteNameEditPart.VISUAL_ID));
		message_DeleteNameLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location message_DeleteNameLabel_Location = (Location) message_DeleteNameLabel.getLayoutConstraint();
		message_DeleteNameLabel_Location.setX(1);
		message_DeleteNameLabel_Location.setY(-13);
		Node message_DeleteStereotypeLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(MessageDeleteAppliedStereotypeEditPart.VISUAL_ID));
		message_DeleteStereotypeLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location message_DeleteStereotypeLabel_Location = (Location) message_DeleteStereotypeLabel
				.getLayoutConstraint();
		message_DeleteStereotypeLabel_Location.setX(1);
		message_DeleteStereotypeLabel_Location.setY(-33);

		PreferenceInitializerForElementHelper.initLabelVisibilityFromPrefs(edge, prefStore, "Message");
		return edge;
	}

	/**
	 * @generated
	 */
	public Edge createMessage_LostEdge(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Connector edge = NotationFactory.eINSTANCE.createConnector();
		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
		List<RelativeBendpoint> points = new ArrayList<>(2);
		points.add(new RelativeBendpoint());
		points.add(new RelativeBendpoint());
		bendpoints.setPoints(points);
		edge.setBendpoints(bendpoints);
		ViewUtil.insertChildView(containerView, edge, index, persisted);
		edge.setType(UMLVisualIDRegistry.getType(MessageLostEditPart.VISUAL_ID));
		edge.setElement(domainElement);
		// initializePreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(edge, prefStore, "Message");
		// org.eclipse.gmf.runtime.notation.Routing routing = org.eclipse.gmf.runtime.notation.Routing.get(prefStore.getInt(org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants.PREF_LINE_STYLE));
		// if (routing != null) {
		// org.eclipse.gmf.runtime.diagram.core.util.ViewUtil.setStructuralFeatureValue(edge, org.eclipse.gmf.runtime.notation.NotationPackage.eINSTANCE.getRoutingStyle_Routing(), routing);
		// }
		Node message_LostNameLabel = createLabel(edge, UMLVisualIDRegistry.getType(MessageLostNameEditPart.VISUAL_ID));
		message_LostNameLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location message_LostNameLabel_Location = (Location) message_LostNameLabel.getLayoutConstraint();
		message_LostNameLabel_Location.setX(1);
		message_LostNameLabel_Location.setY(-13);
		Node message_LostStereotypeLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(MessageLostAppliedStereotypeEditPart.VISUAL_ID));
		message_LostStereotypeLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location message_LostStereotypeLabel_Location = (Location) message_LostStereotypeLabel.getLayoutConstraint();
		message_LostStereotypeLabel_Location.setX(1);
		message_LostStereotypeLabel_Location.setY(-33);

		PreferenceInitializerForElementHelper.initLabelVisibilityFromPrefs(edge, prefStore, "Message");
		return edge;
	}

	/**
	 * @generated
	 */
	public Edge createMessage_FoundEdge(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Connector edge = NotationFactory.eINSTANCE.createConnector();
		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
		List<RelativeBendpoint> points = new ArrayList<>(2);
		points.add(new RelativeBendpoint());
		points.add(new RelativeBendpoint());
		bendpoints.setPoints(points);
		edge.setBendpoints(bendpoints);
		ViewUtil.insertChildView(containerView, edge, index, persisted);
		edge.setType(UMLVisualIDRegistry.getType(MessageFoundEditPart.VISUAL_ID));
		edge.setElement(domainElement);
		// initializePreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(edge, prefStore, "Message");
		// org.eclipse.gmf.runtime.notation.Routing routing = org.eclipse.gmf.runtime.notation.Routing.get(prefStore.getInt(org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants.PREF_LINE_STYLE));
		// if (routing != null) {
		// org.eclipse.gmf.runtime.diagram.core.util.ViewUtil.setStructuralFeatureValue(edge, org.eclipse.gmf.runtime.notation.NotationPackage.eINSTANCE.getRoutingStyle_Routing(), routing);
		// }
		Node message_FoundNameLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(MessageFoundNameEditPart.VISUAL_ID));
		message_FoundNameLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location message_FoundNameLabel_Location = (Location) message_FoundNameLabel.getLayoutConstraint();
		message_FoundNameLabel_Location.setX(1);
		message_FoundNameLabel_Location.setY(-13);
		Node message_FoundStereotypeLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(MessageFoundAppliedStereotypeEditPart.VISUAL_ID));
		message_FoundStereotypeLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location message_FoundStereotypeLabel_Location = (Location) message_FoundStereotypeLabel.getLayoutConstraint();
		message_FoundStereotypeLabel_Location.setX(1);
		message_FoundStereotypeLabel_Location.setY(-33);

		PreferenceInitializerForElementHelper.initLabelVisibilityFromPrefs(edge, prefStore, "Message");
		return edge;
	}

	/**
	 * @generated
	 */
	public Edge createComment_AnnotatedElementEdge(View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Connector edge = NotationFactory.eINSTANCE.createConnector();
		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
		List<RelativeBendpoint> points = new ArrayList<>(2);
		points.add(new RelativeBendpoint());
		points.add(new RelativeBendpoint());
		bendpoints.setPoints(points);
		edge.setBendpoints(bendpoints);
		ViewUtil.insertChildView(containerView, edge, index, persisted);
		edge.setType(UMLVisualIDRegistry.getType(CommentAnnotatedElementEditPart.VISUAL_ID));
		edge.setElement(null);
		// initializePreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		// org.eclipse.gmf.runtime.notation.Routing routing = org.eclipse.gmf.runtime.notation.Routing.get(prefStore.getInt(org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants.PREF_LINE_STYLE));
		// if (routing != null) {
		// org.eclipse.gmf.runtime.diagram.core.util.ViewUtil.setStructuralFeatureValue(edge, org.eclipse.gmf.runtime.notation.NotationPackage.eINSTANCE.getRoutingStyle_Routing(), routing);
		// }

		return edge;
	}

	/**
	 * @generated
	 */
	public Edge createConstraint_ConstrainedElementEdge(View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Connector edge = NotationFactory.eINSTANCE.createConnector();
		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
		List<RelativeBendpoint> points = new ArrayList<>(2);
		points.add(new RelativeBendpoint());
		points.add(new RelativeBendpoint());
		bendpoints.setPoints(points);
		edge.setBendpoints(bendpoints);
		ViewUtil.insertChildView(containerView, edge, index, persisted);
		edge.setType(UMLVisualIDRegistry.getType(ConstraintConstrainedElementEditPart.VISUAL_ID));
		edge.setElement(null);
		// initializePreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		// org.eclipse.gmf.runtime.notation.Routing routing = org.eclipse.gmf.runtime.notation.Routing.get(prefStore.getInt(org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants.PREF_LINE_STYLE));
		// if (routing != null) {
		// org.eclipse.gmf.runtime.diagram.core.util.ViewUtil.setStructuralFeatureValue(edge, org.eclipse.gmf.runtime.notation.NotationPackage.eINSTANCE.getRoutingStyle_Routing(), routing);
		// }

		return edge;
	}

	/**
	 * @generated
	 */
	public Edge createGeneralOrdering_Edge(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Connector edge = NotationFactory.eINSTANCE.createConnector();
		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
		List<RelativeBendpoint> points = new ArrayList<>(2);
		points.add(new RelativeBendpoint());
		points.add(new RelativeBendpoint());
		bendpoints.setPoints(points);
		edge.setBendpoints(bendpoints);
		ViewUtil.insertChildView(containerView, edge, index, persisted);
		edge.setType(UMLVisualIDRegistry.getType(GeneralOrderingEditPart.VISUAL_ID));
		edge.setElement(domainElement);
		// initializePreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(edge, prefStore, "GeneralOrdering");
		// org.eclipse.gmf.runtime.notation.Routing routing = org.eclipse.gmf.runtime.notation.Routing.get(prefStore.getInt(org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants.PREF_LINE_STYLE));
		// if (routing != null) {
		// org.eclipse.gmf.runtime.diagram.core.util.ViewUtil.setStructuralFeatureValue(edge, org.eclipse.gmf.runtime.notation.NotationPackage.eINSTANCE.getRoutingStyle_Routing(), routing);
		// }
		Node generalOrdering_StereotypeLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(GeneralOrderingAppliedStereotypeEditPart.VISUAL_ID));
		generalOrdering_StereotypeLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location generalOrdering_StereotypeLabel_Location = (Location) generalOrdering_StereotypeLabel
				.getLayoutConstraint();
		generalOrdering_StereotypeLabel_Location.setX(1);
		generalOrdering_StereotypeLabel_Location.setY(-33);

		PreferenceInitializerForElementHelper.initLabelVisibilityFromPrefs(edge, prefStore, "GeneralOrdering");
		return edge;
	}

	/**
	 * @generated
	 */
	public Edge createConstraint_ContextEdge(View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Connector edge = NotationFactory.eINSTANCE.createConnector();
		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
		List<RelativeBendpoint> points = new ArrayList<>(2);
		points.add(new RelativeBendpoint());
		points.add(new RelativeBendpoint());
		bendpoints.setPoints(points);
		edge.setBendpoints(bendpoints);
		ViewUtil.insertChildView(containerView, edge, index, persisted);
		edge.setType(UMLVisualIDRegistry.getType(ContextLinkEditPart.VISUAL_ID));
		edge.setElement(null);
		// initializePreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		// org.eclipse.gmf.runtime.notation.Routing routing = org.eclipse.gmf.runtime.notation.Routing.get(prefStore.getInt(org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants.PREF_LINE_STYLE));
		// if (routing != null) {
		// org.eclipse.gmf.runtime.diagram.core.util.ViewUtil.setStructuralFeatureValue(edge, org.eclipse.gmf.runtime.notation.NotationPackage.eINSTANCE.getRoutingStyle_Routing(), routing);
		// }
		Node constraint_KeywordLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(ConstraintContextAppliedStereotypeEditPart.VISUAL_ID));
		constraint_KeywordLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location constraint_KeywordLabel_Location = (Location) constraint_KeywordLabel.getLayoutConstraint();
		constraint_KeywordLabel_Location.setX(0);
		constraint_KeywordLabel_Location.setY(60);

		PreferenceInitializerForElementHelper.initLabelVisibilityFromPrefs(edge, prefStore, "Undefined");
		return edge;
	}

	/**
	 * @generated
	 */
	public Edge createDurationConstraint_Edge(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Connector edge = NotationFactory.eINSTANCE.createConnector();
		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
		List<RelativeBendpoint> points = new ArrayList<>(2);
		points.add(new RelativeBendpoint());
		points.add(new RelativeBendpoint());
		bendpoints.setPoints(points);
		edge.setBendpoints(bendpoints);
		ViewUtil.insertChildView(containerView, edge, index, persisted);
		edge.setType(UMLVisualIDRegistry.getType(DurationConstraintLinkEditPart.VISUAL_ID));
		edge.setElement(domainElement);
		// initializePreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(edge, prefStore, "DurationConstraint");
		// org.eclipse.gmf.runtime.notation.Routing routing = org.eclipse.gmf.runtime.notation.Routing.get(prefStore.getInt(org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants.PREF_LINE_STYLE));
		// if (routing != null) {
		// org.eclipse.gmf.runtime.diagram.core.util.ViewUtil.setStructuralFeatureValue(edge, org.eclipse.gmf.runtime.notation.NotationPackage.eINSTANCE.getRoutingStyle_Routing(), routing);
		// }
		Node durationConstraint_NameLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(DurationConstraintLinkNameEditPart.VISUAL_ID));
		durationConstraint_NameLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location durationConstraint_NameLabel_Location = (Location) durationConstraint_NameLabel.getLayoutConstraint();
		durationConstraint_NameLabel_Location.setX(1);
		durationConstraint_NameLabel_Location.setY(-13);
		Node durationConstraint_StereotypeLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(DurationConstraintLinkAppliedStereotypeEditPart.VISUAL_ID));
		durationConstraint_StereotypeLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location durationConstraint_StereotypeLabel_Location = (Location) durationConstraint_StereotypeLabel
				.getLayoutConstraint();
		durationConstraint_StereotypeLabel_Location.setX(1);
		durationConstraint_StereotypeLabel_Location.setY(-33);

		PreferenceInitializerForElementHelper.initLabelVisibilityFromPrefs(edge, prefStore, "DurationConstraint");
		return edge;
	}

	/**
	 * @generated
	 */
	public Edge createDurationObservation_Edge(EObject domainElement, View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Connector edge = NotationFactory.eINSTANCE.createConnector();
		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
		List<RelativeBendpoint> points = new ArrayList<>(2);
		points.add(new RelativeBendpoint());
		points.add(new RelativeBendpoint());
		bendpoints.setPoints(points);
		edge.setBendpoints(bendpoints);
		ViewUtil.insertChildView(containerView, edge, index, persisted);
		edge.setType(UMLVisualIDRegistry.getType(DurationObservationLinkEditPart.VISUAL_ID));
		edge.setElement(domainElement);
		// initializePreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();

		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(edge, prefStore, "DurationObservation");
		// org.eclipse.gmf.runtime.notation.Routing routing = org.eclipse.gmf.runtime.notation.Routing.get(prefStore.getInt(org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants.PREF_LINE_STYLE));
		// if (routing != null) {
		// org.eclipse.gmf.runtime.diagram.core.util.ViewUtil.setStructuralFeatureValue(edge, org.eclipse.gmf.runtime.notation.NotationPackage.eINSTANCE.getRoutingStyle_Routing(), routing);
		// }
		Node durationObservation_NameLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(DurationObservationLinkNameEditPart.VISUAL_ID));
		durationObservation_NameLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location durationObservation_NameLabel_Location = (Location) durationObservation_NameLabel
				.getLayoutConstraint();
		durationObservation_NameLabel_Location.setX(1);
		durationObservation_NameLabel_Location.setY(-13);
		Node durationObservation_StereotypeLabel = createLabel(edge,
				UMLVisualIDRegistry.getType(DurationObservationLinkAppliedStereotypeEditPart.VISUAL_ID));
		durationObservation_StereotypeLabel.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
		Location durationObservation_StereotypeLabel_Location = (Location) durationObservation_StereotypeLabel
				.getLayoutConstraint();
		durationObservation_StereotypeLabel_Location.setX(1);
		durationObservation_StereotypeLabel_Location.setY(-33);

		PreferenceInitializerForElementHelper.initLabelVisibilityFromPrefs(edge, prefStore, "DurationObservation");
		return edge;
	}

	/**
	 * @generated
	 */
	protected void stampShortcut(View containerView, Node target) {
		if (!SequenceDiagramEditPart.MODEL_ID.equals(UMLVisualIDRegistry.getModelID(containerView))) {
			EAnnotation shortcutAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
			shortcutAnnotation.setSource("Shortcut"); //$NON-NLS-1$
			shortcutAnnotation.getDetails().put("modelID", SequenceDiagramEditPart.MODEL_ID); //$NON-NLS-1$
			target.getEAnnotations().add(shortcutAnnotation);
		}
	}

	/**
	 * @generated
	 */
	protected Node createLabel(View owner, String hint) {
		DecorationNode rv = NotationFactory.eINSTANCE.createDecorationNode();
		rv.setType(hint);
		ViewUtil.insertChildView(owner, rv, ViewUtil.APPEND, true);
		return rv;
	}

	/**
	 * @generated
	 */
	protected Node createCompartment(View owner, String hint, boolean canCollapse, boolean hasTitle, boolean canSort,
			boolean canFilter) {
		// SemanticListCompartment rv = NotationFactory.eINSTANCE.createSemanticListCompartment();
		// rv.setShowTitle(showTitle);
		// rv.setCollapsed(isCollapsed);
		Node rv = NotationFactory.eINSTANCE.createBasicCompartment();

		rv.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());

		if (hasTitle) {
			TitleStyle ts = NotationFactory.eINSTANCE.createTitleStyle();
			rv.getStyles().add(ts);
		}
		if (canSort) {
			rv.getStyles().add(NotationFactory.eINSTANCE.createSortingStyle());
		}
		if (canFilter) {
			rv.getStyles().add(NotationFactory.eINSTANCE.createFilteringStyle());
		}
		rv.setType(hint);
		ViewUtil.insertChildView(owner, rv, ViewUtil.APPEND, true);
		return rv;
	}

	/**
	 * @generated
	 */
	protected EObject getSemanticElement(IAdaptable semanticAdapter) {
		if (semanticAdapter == null) {
			return null;
		}
		EObject eObject = semanticAdapter.getAdapter(EObject.class);
		if (eObject != null) {
			return EMFCoreUtil.resolve(TransactionUtil.getEditingDomain(eObject), eObject);
		}
		return null;
	}

	/**
	 * @generated
	 */
	protected IElementType getSemanticElementType(IAdaptable semanticAdapter) {
		if (semanticAdapter == null) {
			return null;
		}
		return semanticAdapter.getAdapter(IElementType.class);
	}

	/**
	 * @generated
	 */
	private void initFontStyleFromPrefs(View view, final IPreferenceStore store, String elementName) {
		String fontConstant = PreferencesConstantsHelper.getElementConstant(elementName,
				PreferencesConstantsHelper.FONT);
		String fontColorConstant = PreferencesConstantsHelper.getElementConstant(elementName,
				PreferencesConstantsHelper.COLOR_FONT);

		FontStyle viewFontStyle = (FontStyle) view.getStyle(NotationPackage.Literals.FONT_STYLE);
		if (viewFontStyle != null) {
			FontData fontData = PreferenceConverter.getFontData(store, fontConstant);
			viewFontStyle.setFontName(fontData.getName());
			viewFontStyle.setFontHeight(fontData.getHeight());
			viewFontStyle.setBold((fontData.getStyle() & SWT.BOLD) != 0);
			viewFontStyle.setItalic((fontData.getStyle() & SWT.ITALIC) != 0);

			org.eclipse.swt.graphics.RGB fontRGB = PreferenceConverter.getColor(store, fontColorConstant);
			viewFontStyle.setFontColor(FigureUtilities.RGBToInteger(fontRGB).intValue());
		}
	}

	/**
	 * @generated
	 */
	private void initForegroundFromPrefs(View view, final IPreferenceStore store, String elementName) {
		String lineColorConstant = PreferencesConstantsHelper.getElementConstant(elementName,
				PreferencesConstantsHelper.COLOR_LINE);
		org.eclipse.swt.graphics.RGB lineRGB = PreferenceConverter.getColor(store, lineColorConstant);
		ViewUtil.setStructuralFeatureValue(view, NotationPackage.eINSTANCE.getLineStyle_LineColor(),
				FigureUtilities.RGBToInteger(lineRGB));
	}

	/**
	 * @generated
	 */
	private void initBackgroundFromPrefs(View view, final IPreferenceStore store, String elementName) {
		String fillColorConstant = PreferencesConstantsHelper.getElementConstant(elementName,
				PreferencesConstantsHelper.COLOR_FILL);
		String gradientColorConstant = PreferencesConstantsHelper.getElementConstant(elementName,
				PreferencesConstantsHelper.COLOR_GRADIENT);
		String gradientPolicyConstant = PreferencesConstantsHelper.getElementConstant(elementName,
				PreferencesConstantsHelper.GRADIENT_POLICY);

		org.eclipse.swt.graphics.RGB fillRGB = PreferenceConverter.getColor(store, fillColorConstant);
		ViewUtil.setStructuralFeatureValue(view, NotationPackage.eINSTANCE.getFillStyle_FillColor(),
				FigureUtilities.RGBToInteger(fillRGB));

		FillStyle fillStyle = (FillStyle) view.getStyle(NotationPackage.Literals.FILL_STYLE);
		fillStyle.setFillColor(FigureUtilities.RGBToInteger(fillRGB).intValue());

		;
		if (store.getBoolean(gradientPolicyConstant)) {
			GradientPreferenceConverter gradientPreferenceConverter = new GradientPreferenceConverter(
					store.getString(gradientColorConstant));
			fillStyle.setGradient(gradientPreferenceConverter.getGradientData());
			fillStyle.setTransparency(gradientPreferenceConverter.getTransparency());
		}
	}
}
