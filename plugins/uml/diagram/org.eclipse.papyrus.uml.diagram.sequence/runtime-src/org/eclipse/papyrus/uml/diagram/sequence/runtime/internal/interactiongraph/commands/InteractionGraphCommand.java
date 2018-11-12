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

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.core.command.ICompositeCommand;
import org.eclipse.gmf.runtime.diagram.core.commands.DeleteCommand;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest.ViewDescriptor;
import org.eclipse.gmf.runtime.emf.commands.core.command.CompositeTransactionalCommand;
import org.eclipse.gmf.runtime.emf.type.core.commands.DestroyElementCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.CreateViewCommand;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.SemanticElementAdapter;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.InteractionGraphImpl;
import org.eclipse.uml2.uml.Lifeline;

public class InteractionGraphCommand {
	/**
	 * Constructor.
	 *
	 * @param interactionGraph
	 */
	public InteractionGraphCommand(InteractionGraphImpl interactionGraph) {
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
	public ICommand build(TransactionalEditingDomain editingDomain, String label) {
		ICompositeCommand command = new CompositeTransactionalCommand(editingDomain, label);
		calculateLifelinesEditingCommand(editingDomain, command);
		/*
		 * calculateFragmentsChanges(diffs);
		 * calculateMessagesChanges(diffs);
		 */
		return command;
	}

	private void calculateLifelinesEditingCommand(TransactionalEditingDomain editingDomain, ICompositeCommand command) {
		List<Lifeline> lifelines = interactionGraph.getInteraction().getLifelines();
		List<Lifeline> graphLifelines = interactionGraph.getLifelineClusters().stream().map(Node::getElement).map(Lifeline.class::cast).collect(Collectors.toList());

		// TODO: index of created element
		Function<Lifeline, ICommand> addCommandFunction = (d) -> createLifelinesEditingCommand(editingDomain, d);
		Function<Lifeline, ICommand> removeCommandFunction = (d) -> deleteLifelinesEditingCommand(editingDomain, d);
		createCommandsForCollectionChanges(command, lifelines, graphLifelines, addCommandFunction, removeCommandFunction);
		// Check fragments covered...
		/*
		 * for (Cluster c : interactionGraph.getLifelineClusters()) {
		 * Lifeline lf = (Lifeline)c.getElement();
		 * NodeUtilities.flattenImpl(((ClusterImpl)c)).
		 * map(Node::getElement).
		 * filter(d -> d instanceof OccurrenceSpecification || d instanceof ExecutionSpecification).
		 * map(InteractionFragment.class::cast).
		 * forEach(f -> calculateChangesForCollection(diffs, f, UMLPackage.Literals.INTERACTION_FRAGMENT__COVERED,
		 * f.getCovereds(), Arrays.asList(lf)));
		 * }
		 */
	}

	private ICommand createLifelinesEditingCommand(TransactionalEditingDomain editingDomain, Lifeline lifeline) {
		ICompositeCommand cmd = new CompositeCommand("Create Lifeline");
		final CreateElementRequest createReq = new CreateElementRequest(editingDomain, interactionGraph.getInteraction(), UMLElementTypes.Lifeline_Shape);
		cmd.add(new CreateProvidedElementCommand(createReq, lifeline));
		cmd.add(new CreateViewCommand(editingDomain, new ViewDescriptor(
				new SemanticElementAdapter(lifeline, UMLElementTypes.getElementType(LifelineEditPart.VISUAL_ID)), org.eclipse.gmf.runtime.notation.Node.class, LifelineEditPart.VISUAL_ID,
				((GraphicalEditPart) interactionGraph.getEditPart()).getDiagramPreferencesHint()),
				ViewUtil.getChildBySemanticHint(interactionGraph.getInteractionView(), InteractionInteractionCompartmentEditPart.VISUAL_ID)));
		return cmd;
	}

	private ICommand deleteLifelinesEditingCommand(TransactionalEditingDomain editingDomain, Lifeline lifeline) {
		ICompositeCommand cmd = new CompositeCommand("Delete Lifeline");
		cmd.add(new DeleteCommand(editingDomain, interactionGraph.getLifeline(lifeline).getView()));
		cmd.add(new DestroyElementCommand(new DestroyElementRequest(editingDomain, lifeline, false)));
		return cmd;
	}

	private <T> void createCommandsForCollectionChanges(ICompositeCommand cmd, List<T> oldValues, List<T> newValues, Function<T, ICommand> addCommand, Function<T, ICommand> removeCommand) {
		List<T> objToRemove = oldValues.stream().filter(d -> !newValues.contains(d)).collect(Collectors.toList());
		List<T> objToAdd = newValues.stream().filter(d -> !oldValues.contains(d)).collect(Collectors.toList());

		for (T lf : objToAdd) {
			cmd.add(addCommand.apply(lf));
		}

		for (T lf : objToRemove) {
			cmd.add(removeCommand.apply(lf));
		}

		// ReorderCommands
		int index = 0;
		for (T obj : newValues) {
			if (oldValues.indexOf(obj) != index) {

			}
			index++;
		}
	}

	/*
	 * private void calculateFragmentsChanges(List<InteractionGraphDiff> diffs) {
	 * List<InteractionFragment> fragments = interactionGraph.getInteraction().getFragments();
	 * List<InteractionFragment> newFragments =
	 * interactionGraph.getRows().stream().
	 * flatMap(d -> d.getNodes().stream()).
	 * map(Node::getElement).
	 * filter(InteractionFragment.class::isInstance).
	 * map(InteractionFragment.class::cast).
	 * collect(Collectors.toList());
	 *
	 * calculateChangesContainmentCollection(diffs, interactionGraph.getInteraction(), UMLPackage.Literals.INTERACTION__FRAGMENT, fragments, newFragments);
	 * }
	 *
	 * private void calculateMessagesChanges(List<InteractionGraphDiff> diffs) {
	 * List<Message> messages = interactionGraph.getInteraction().getMessages();
	 * List<Message> newMessages =
	 * interactionGraph.getRows().stream().
	 * flatMap(d -> d.getNodes().stream()).
	 * map(Node::getElement).
	 * filter(MessageOccurrenceSpecification.class::isInstance).
	 * map(d -> ((MessageOccurrenceSpecification)d).getMessage()).
	 * distinct().
	 * collect(Collectors.toList());
	 *
	 * calculateChangesContainmentCollection(diffs, interactionGraph.getInteraction(), UMLPackage.Literals.INTERACTION__MESSAGE, messages, newMessages);
	 * }
	 *
	 * private <T extends EObject> void calculateChangesContainmentCollection(List<InteractionGraphDiff> diffs, EObject owner, EStructuralFeature feature, List<T> oldVal, List<T> newVal) {
	 * if (!calculateChangesForCollection(diffs, owner, feature, oldVal, newVal)) {
	 * List<T> objToRemove = oldVal.stream().filter(d -> !newVal.contains(d)).collect(Collectors.toList());
	 * List<T> objToAdd = newVal.stream().filter(d -> !oldVal.contains(d)).collect(Collectors.toList());
	 *
	 * for (T lf : objToAdd) {
	 * diffs.add(InteractionGraphDiffImpl.create(lf, feature, owner));
	 * }
	 *
	 * for (T lf : objToRemove) {
	 * diffs.add(InteractionGraphDiffImpl.delete(lf, feature, owner));
	 * }
	 * }
	 * }
	 *
	 * private <T extends EObject> boolean calculateChangesForCollection(List<InteractionGraphDiff> diffs, EObject owner, EStructuralFeature feature, List<T> oldVal, List<T> newVal) {
	 * int gcount = oldVal.size();
	 * int icount = newVal.size();
	 * if (gcount != icount || !oldVal.equals(newVal)) {
	 * diffs.add(InteractionGraphDiffImpl.change(owner, feature, oldVal, newVal));
	 * return false;
	 * }
	 * return true;
	 * }
	 */
	private InteractionGraphImpl interactionGraph;

}
