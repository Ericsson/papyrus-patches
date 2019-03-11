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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.core.services.view.CreateNodeViewOperation;
import org.eclipse.gmf.runtime.diagram.core.services.view.CreateViewForKindOperation;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.DecorationNode;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.Location;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.RelativeBendpoints;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.gmf.runtime.notation.datatype.RelativeBendpoint;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.papyrus.uml.diagram.common.helper.PreferenceInitializerForElementHelper;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.BehaviorExecutionSpecificationBehaviorEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentCombinedFragmentCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CommentAnnotatedElementEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ConsiderIgnoreFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationConstraintAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationConstraintEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationConstraintInMessageAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationConstraintInMessageEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationConstraintInMessageLabelEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationConstraintLabelEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationObservationAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationObservationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationObservationLabelEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionOperandGuardEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeObservationAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeObservationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeObservationLabelEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.Lifeline;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomViewProvider extends UMLViewProvider {

	@Override
	protected boolean provides(CreateNodeViewOperation op) {
		if (op.getContainerView() == null) {
			return false;
		}
		String visualID = UMLVisualIDRegistry.getVisualID(op.getSemanticHint());
		if (DurationConstraintEditPart.VISUAL_ID.equals(visualID)) {
			// avoid to
			// modify
			// UMLVisualIDRegistry.getNodeVisualID(View,
			// EObject)
			if (InteractionInteractionCompartmentEditPart.VISUAL_ID.equals(UMLVisualIDRegistry
					.getVisualID(op.getContainerView()))) {
				return true;
			}
		}
		return super.provides(op);
	}

	@Override
	protected boolean provides(CreateViewForKindOperation op) {
		if (op.getContainerView() == null) {
			return false;
		}
		String visualID = UMLVisualIDRegistry.getVisualID(op.getSemanticHint());
		if (DurationConstraintEditPart.VISUAL_ID.equals(visualID)) { // avoid to
			// modify
			// UMLVisualIDRegistry.getNodeVisualID(View,
			// EObject)
			if (InteractionInteractionCompartmentEditPart.VISUAL_ID.equals(UMLVisualIDRegistry
					.getVisualID(op.getContainerView()))) {
				return true;
			}
		}
		return super.provides(op);
	}

	@Override
	public Edge createEdge(IAdaptable semanticAdapter, View containerView,
			String semanticHint, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Edge edge = super.createEdge(semanticAdapter, containerView,
				semanticHint, index, persisted, preferencesHint);
		if (edge != null) {
			edge.getStyles().add(NotationFactory.eINSTANCE.createLineStyle());
			final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint
					.getPreferenceStore();
			PreferenceInitializerForElementHelper.initForegroundFromPrefs(edge,
					prefStore, "Message");
		}
		return edge;
	}

	@Override
	public Node createTimeObservation_Shape(EObject domainElement,
			View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry
				.getType(TimeObservationEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint
				.getPreferenceStore();
		PreferenceInitializerForElementHelper.initForegroundFromPrefs(node,
				prefStore, "TimeObservation");
		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node,
				prefStore, "TimeObservation");
		PreferenceInitializerForElementHelper.initBackgroundFromPrefs(node,
				prefStore, "TimeObservation");
		Node timeObservation_NameLabel = createLabel(node,
				UMLVisualIDRegistry
						.getType(TimeObservationLabelEditPart.VISUAL_ID),
				true);
		timeObservation_NameLabel.setLayoutConstraint(NotationFactory.eINSTANCE
				.createLocation());
		Location timeObservation_NameLabel_Location = (Location) timeObservation_NameLabel.getLayoutConstraint();
		timeObservation_NameLabel_Location.setX(0);
		timeObservation_NameLabel_Location.setY(0);
		Node timeObservation_StereotypeLabel = createLabel(
				node,
				UMLVisualIDRegistry
						.getType(TimeObservationAppliedStereotypeEditPart.VISUAL_ID),
				true);
		timeObservation_StereotypeLabel.setLayoutConstraint(NotationFactory.eINSTANCE
				.createLocation());
		Location timeObservation_StereotypeLabel_Location = (Location) timeObservation_StereotypeLabel.getLayoutConstraint();
		timeObservation_StereotypeLabel_Location.setX(0);
		timeObservation_StereotypeLabel_Location.setY(0);
		return node;
	}

	protected Node createLabel(View owner, String hint,
			boolean isTimeObservationLable) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(hint);
		ViewUtil.insertChildView(owner, node, ViewUtil.APPEND, true);
		return node;
	}

	@Override
	public Node createDurationConstraint_Shape(EObject domainElement,
			View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry
				.getType(DurationConstraintEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint
				.getPreferenceStore();
		PreferenceInitializerForElementHelper.initForegroundFromPrefs(node,
				prefStore, "DurationConstraint");
		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node,
				prefStore, "DurationConstraint");
		PreferenceInitializerForElementHelper.initBackgroundFromPrefs(node,
				prefStore, "DurationConstraint");
		Node durationConstraint_BodyLabel = createLabel(node,
				UMLVisualIDRegistry
						.getType(DurationConstraintLabelEditPart.VISUAL_ID));
		durationConstraint_BodyLabel.setLayoutConstraint(NotationFactory.eINSTANCE
				.createLocation());
		Location durationConstraint_BodyLabel_Location = (Location) durationConstraint_BodyLabel.getLayoutConstraint();
		durationConstraint_BodyLabel_Location.setX(25);
		durationConstraint_BodyLabel_Location.setY(0);
		Node durationConstraint_StereotypeLabel = createLabel(
				node,
				UMLVisualIDRegistry
						.getType(DurationConstraintAppliedStereotypeEditPart.VISUAL_ID));
		durationConstraint_StereotypeLabel.setLayoutConstraint(NotationFactory.eINSTANCE
				.createLocation());
		Location durationConstraint_StereotypeLabel_Location = (Location) durationConstraint_StereotypeLabel.getLayoutConstraint();
		durationConstraint_StereotypeLabel_Location.setX(0);
		durationConstraint_StereotypeLabel_Location.setY(-22);
		return node;
	}

	/**
	 * This class has bee overloaded in order to set the combined fragment under the lifelines
	 *
	 * @see org.eclipse.papyrus.uml.diagram.sequence.providers.UMLViewProvider#createCombinedFragment_Shape(org.eclipse.emf.ecore.EObject, org.eclipse.gmf.runtime.notation.View, int, boolean, org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint)
	 */
	@Override
	public Node createCombinedFragment_Shape(EObject domainElement, View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
		int position = LifelinePosition(containerView);
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry.getType(CombinedFragmentEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, position, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint
				.getPreferenceStore();
		PreferenceInitializerForElementHelper.initForegroundFromPrefs(node, prefStore, "CombinedFragment");
		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node, prefStore, "CombinedFragment");
		PreferenceInitializerForElementHelper.initBackgroundFromPrefs(node, prefStore, "CombinedFragment");
		Node compartment = createCompartment(node, UMLVisualIDRegistry.getType(CombinedFragmentCombinedFragmentCompartmentEditPart.VISUAL_ID),
				false, false, true, true);
		// Add by default InteractionOperand
		for (InteractionOperand interactionOperand : ((CombinedFragment) domainElement).getOperands()) {
			createInteractionOperand_Shape(interactionOperand, compartment, -1, true, UMLDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
		}
		// initialization for the compartment visibility
		PreferenceInitializerForElementHelper.initCompartmentsStatusFromPrefs(
				node, prefStore, "CombinedFragment");
		return node;
	}

	/**
	 * @param containerView
	 *            the view that can contains lifeline representation
	 * @return the position of the first lifeline in the notation
	 * @since 3.0
	 *
	 */
	protected int LifelinePosition(View containerView) {
		@SuppressWarnings("unchecked")
		List<Object> children = containerView.getChildren();
		int i = 0;
		while (i < children.size()) {
			if (children.get(i) instanceof View) {
				if (((View) children.get(i)).getElement() instanceof Lifeline) {
					return i;
				}
			}
			i++;
		}
		return i;
	}


	@Override
	public Node createDurationConstraint_Shape_CN(EObject domainElement,
			View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Node node = NotationFactory.eINSTANCE.createShape();
		node.getStyles()
				.add(NotationFactory.eINSTANCE.createDescriptionStyle());
		node.getStyles().add(NotationFactory.eINSTANCE.createFillStyle());
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry
				.getType(DurationConstraintInMessageEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// custom layout for location
		Location durationConstraint_Shape_CN_Location = (Location) node.getLayoutConstraint();
		durationConstraint_Shape_CN_Location.setX(0);
		durationConstraint_Shape_CN_Location.setY(20);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint
				.getPreferenceStore();
		PreferenceInitializerForElementHelper.initBackgroundFromPrefs(node,
				prefStore, "DurationConstraint");
		Node durationConstraint_BodyLabel_CN = createLabel(
				node,
				UMLVisualIDRegistry
						.getType(DurationConstraintInMessageLabelEditPart.VISUAL_ID));
		Node durationConstraint_StereotypeLabel_CN = createLabel(
				node,
				UMLVisualIDRegistry
						.getType(DurationConstraintInMessageAppliedStereotypeEditPart.VISUAL_ID));
		durationConstraint_StereotypeLabel_CN.setLayoutConstraint(NotationFactory.eINSTANCE
				.createLocation());
		Location durationConstraint_StereotypeLabel_CN_Location = (Location) durationConstraint_StereotypeLabel_CN.getLayoutConstraint();
		durationConstraint_StereotypeLabel_CN_Location.setX(0);
		durationConstraint_StereotypeLabel_CN_Location.setY(-22);
		return node;
	}

	@Override
	public Node createDurationObservation_Shape(EObject domainElement,
			View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Node node = NotationFactory.eINSTANCE.createShape();
		node.getStyles()
				.add(NotationFactory.eINSTANCE.createDescriptionStyle());
		node.getStyles().add(NotationFactory.eINSTANCE.createFillStyle());
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry
				.getType(DurationObservationEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// custom layout for location
		Location durationObservation_Shape_Location = (Location) node.getLayoutConstraint();
		durationObservation_Shape_Location.setX(0);
		durationObservation_Shape_Location.setY(0);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint
				.getPreferenceStore();
		PreferenceInitializerForElementHelper.initBackgroundFromPrefs(node,
				prefStore, "DurationObservation");
		Node durationObservation_NameLabel = createLabel(node,
				UMLVisualIDRegistry
						.getType(DurationObservationLabelEditPart.VISUAL_ID));
		Node durationObservation_StereotypeLabel = createLabel(
				node,
				UMLVisualIDRegistry
						.getType(DurationObservationAppliedStereotypeEditPart.VISUAL_ID));
		durationObservation_StereotypeLabel.setLayoutConstraint(NotationFactory.eINSTANCE
				.createLocation());
		Location durationObservation_StereotypeLabel_Location = (Location) durationObservation_StereotypeLabel.getLayoutConstraint();
		durationObservation_StereotypeLabel_Location.setX(0);
		durationObservation_StereotypeLabel_Location.setY(0);
		return node;
	}

	@Override
	public Node createConsiderIgnoreFragment_Shape(EObject domainElement,
			View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Shape node = NotationFactory.eINSTANCE.createShape();
		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		node.setType(UMLVisualIDRegistry
				.getType(ConsiderIgnoreFragmentEditPart.VISUAL_ID));
		ViewUtil.insertChildView(containerView, node, index, persisted);
		node.setElement(domainElement);
		// initializeFromPreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint
				.getPreferenceStore();
		PreferenceInitializerForElementHelper.initForegroundFromPrefs(node,
				prefStore, "ConsiderIgnoreFragment");
		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(node,
				prefStore, "ConsiderIgnoreFragment");
		PreferenceInitializerForElementHelper.initBackgroundFromPrefs(node,
				prefStore, "ConsiderIgnoreFragment");
		Node compartment = createCompartment(
				node,
				UMLVisualIDRegistry
						.getType(CombinedFragmentCombinedFragmentCompartmentEditPart.VISUAL_ID),
				false, false, true, true);
		// Add by default InteractionOperand
		for (InteractionOperand interactionOperand : ((CombinedFragment) domainElement)
				.getOperands()) {
			createInteractionOperand_Shape(interactionOperand, compartment, -1,
					true, UMLDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
		}
		// initialization for the compartment visibility
		PreferenceInitializerForElementHelper.initCompartmentsStatusFromPrefs(
				node, prefStore, "ConsiderIgnoreFragment");
		return node;
	}

	@Override
	public Edge createComment_AnnotatedElementEdge(View containerView,
			int index, boolean persisted, PreferencesHint preferencesHint) {
		Edge edge = NotationFactory.eINSTANCE.createEdge(); // override
															// Connector
		edge.getStyles().add(NotationFactory.eINSTANCE.createRoutingStyle());
		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE
				.createRelativeBendpoints();
		ArrayList<RelativeBendpoint> points = new ArrayList<>(
				2);
		points.add(new RelativeBendpoint());
		points.add(new RelativeBendpoint());
		bendpoints.setPoints(points);
		edge.setBendpoints(bendpoints);
		ViewUtil.insertChildView(containerView, edge, index, persisted);
		edge.setType(UMLVisualIDRegistry
				.getType(CommentAnnotatedElementEditPart.VISUAL_ID));
		edge.setElement(null);
		// initializePreferences
		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint
				.getPreferenceStore();
		PreferenceInitializerForElementHelper.initForegroundFromPrefs(edge,
				prefStore, "Undefined");
		PreferenceInitializerForElementHelper.initFontStyleFromPrefs(edge,
				prefStore, "Undefined");
		PreferenceInitializerForElementHelper.initRountingFromPrefs(edge,
				prefStore, "Undefined");
		return edge;
	}

	// Add Guard label support.
	@Override
	public Node createInteractionOperand_Shape(EObject domainElement,
			View containerView, int index, boolean persisted,
			PreferencesHint preferencesHint) {
		Node node = super.createInteractionOperand_Shape(domainElement,
				containerView, index, persisted, preferencesHint);
		DecorationNode guardNode = NotationFactory.eINSTANCE
				.createDecorationNode();
		Bounds b = NotationFactory.eINSTANCE.createBounds();
		b.setX(5);
		b.setY(5);
		guardNode.setLayoutConstraint(b);
		guardNode.setType(InteractionOperandGuardEditPart.GUARD_TYPE);
		guardNode.setElement(((InteractionOperand) domainElement).getGuard());
		ViewUtil.insertChildView(node, guardNode, ViewUtil.APPEND, true);
		return node;
	}

	@Override
	public Node createBehaviorExecutionSpecification_Shape(
			EObject domainElement, View containerView, int index,
			boolean persisted, PreferencesHint preferencesHint) {
		Node node = super.createBehaviorExecutionSpecification_Shape(domainElement,
				containerView, index, persisted, preferencesHint);
		// Add a label for Behavior.
		Node label = createLabel(node,
				BehaviorExecutionSpecificationBehaviorEditPart.BEHAVIOR_TYPE);
		Location location = NotationFactory.eINSTANCE.createLocation();
		location.setX(18);
		location.setY(18);
		label.setLayoutConstraint(location);
		return node;
	}



}
