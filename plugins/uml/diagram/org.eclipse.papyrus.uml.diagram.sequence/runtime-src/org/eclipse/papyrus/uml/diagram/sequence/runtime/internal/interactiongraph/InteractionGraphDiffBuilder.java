/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphDiff;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.UMLPackage;

public class InteractionGraphDiffBuilder {
	/**
	 * Constructor.
	 *
	 * @param interactionGraph
	 */
	public InteractionGraphDiffBuilder(InteractionGraphImpl interactionGraph) {
		super();
		this.interactionGraph = interactionGraph;
	}

	/*
	 * Note: Row repositioning should be done by the "Add / Move / Remove" operations
	 * Note: Deleting nodes, may need to be tracked... In order to remove any reference.
	 * TODO:
	 * 1) Interaction:
	 * - Lifeline feature
	 * - Fragment feature (Order it using the rows filtering out the ones inside a fragment)
	 * - Message feature (Ordered it using the rows, no filtering here)
	 * - Bounds (Based on grid layout)
	 * 2) Lifelines:
	 * - Unset covered attributes for the nodes not having the lifeline as parent
	 * - Bounds (Header + body line)
	 * 3) Message Ocurrence specifications:
	 * - Covered feature
	 * - Location / Bounds / Anchoring
	 * 4) Execution Specifications
	 * - Covered Feature.
	 * - Start feature
	 * - Finish feature
	 * - Bounds / Location.
	 */
	public List<InteractionGraphDiff> calculateDifferences() {
		List<InteractionGraphDiff> diffs = new ArrayList<>();
		calculateLifelinesEditChanges(diffs);
		calculateFragmentsChanges(diffs);
		calculateMessagesChanges(diffs);
		return diffs;
	}

	private void calculateLifelinesEditChanges(List<InteractionGraphDiff> diffs) {
		List<Lifeline> lifelines = interactionGraph.getInteraction().getLifelines();
		List<Lifeline> newLifelines = interactionGraph.getLifelineClusters().stream().map(Node::getElement).map(Lifeline.class::cast).collect(Collectors.toList());

		calculateChangesContainmentCollection(diffs, interactionGraph.getInteraction(), UMLPackage.Literals.INTERACTION__LIFELINE, lifelines, newLifelines);

		// Check fragments covered...
		for (Cluster c : interactionGraph.getLifelineClusters()) {
			Lifeline lf = (Lifeline) c.getElement();
			NodeUtilities.flattenImpl(((ClusterImpl) c)).map(Node::getElement).filter(d -> d instanceof OccurrenceSpecification || d instanceof ExecutionSpecification).map(InteractionFragment.class::cast)
					.forEach(f -> calculateChangesForCollection(diffs, f, UMLPackage.Literals.INTERACTION_FRAGMENT__COVERED,
							f.getCovereds(), Arrays.asList(lf)));
		}
	}

	private void calculateFragmentsChanges(List<InteractionGraphDiff> diffs) {
		List<InteractionFragment> fragments = interactionGraph.getInteraction().getFragments();
		List<InteractionFragment> newFragments = interactionGraph.getRows().stream().flatMap(d -> d.getNodes().stream()).map(Node::getElement).filter(InteractionFragment.class::isInstance).map(InteractionFragment.class::cast).collect(Collectors.toList());

		calculateChangesContainmentCollection(diffs, interactionGraph.getInteraction(), UMLPackage.Literals.INTERACTION__FRAGMENT, fragments, newFragments);
	}

	private void calculateMessagesChanges(List<InteractionGraphDiff> diffs) {
		List<Message> messages = interactionGraph.getInteraction().getMessages();
		List<Message> newMessages = interactionGraph.getRows().stream().flatMap(d -> d.getNodes().stream()).map(Node::getElement).filter(MessageOccurrenceSpecification.class::isInstance).map(d -> ((MessageOccurrenceSpecification) d).getMessage()).distinct()
				.collect(Collectors.toList());

		calculateChangesContainmentCollection(diffs, interactionGraph.getInteraction(), UMLPackage.Literals.INTERACTION__MESSAGE, messages, newMessages);
	}

	static <T extends EObject> void calculateChangesContainmentCollection(List<InteractionGraphDiff> diffs, EObject owner, EStructuralFeature feature, List<T> oldVal, List<T> newVal) {
		if (!calculateChangesForCollection(diffs, owner, feature, oldVal, newVal)) {
			List<T> objToRemove = oldVal.stream().filter(d -> !newVal.contains(d)).collect(Collectors.toList());
			List<T> objToAdd = newVal.stream().filter(d -> !oldVal.contains(d)).collect(Collectors.toList());

			for (T lf : objToAdd) {
				diffs.add(InteractionGraphDiffImpl.create(lf, feature, owner));
			}

			for (T lf : objToRemove) {
				diffs.add(InteractionGraphDiffImpl.delete(lf, feature, owner));
			}
		}
	}

	static <T extends EObject> boolean calculateChangesForCollection(List<InteractionGraphDiff> diffs, EObject owner, EStructuralFeature feature, List<T> oldVal, List<T> newVal) {
		int gcount = oldVal.size();
		int icount = newVal.size();
		if (gcount != icount || !oldVal.equals(newVal)) {
			diffs.add(InteractionGraphDiffImpl.change(owner, feature, oldVal, newVal));
			return false;
		}
		return true;
	}

	private InteractionGraphImpl interactionGraph;
}
