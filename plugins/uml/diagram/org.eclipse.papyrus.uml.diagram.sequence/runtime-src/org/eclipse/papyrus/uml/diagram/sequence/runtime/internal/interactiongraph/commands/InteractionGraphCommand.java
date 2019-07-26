/*****************************************************************************
 * (c) Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.EMFCommandOperation;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.core.command.ICompositeCommand;
import org.eclipse.gmf.runtime.diagram.core.commands.DeleteCommand;
import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest.ViewDescriptor;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.emf.commands.core.command.CompositeTransactionalCommand;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.IHintedType;
import org.eclipse.gmf.runtime.emf.type.core.commands.DestroyElementCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.SemanticElementAdapter;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ActionExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.BehaviorExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DestructionOccurrenceSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.GateEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionUseEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Link;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ClusterImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.InteractionGraphImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.InteractionGraphService;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.NodeImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.NodeOrderResolver;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.NodeUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.uml2.uml.ActionExecutionSpecification;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.UMLPackage;

public class InteractionGraphCommand extends AbstractTransactionalCommand {
	/**
	 * Constructor.
	 *
	 * @param domain
	 * @param label
	 * @param affectedFiles
	 */
	public InteractionGraphCommand(TransactionalEditingDomain domain, String label, InteractionGraph interactionGraph,
			List<?> affectedFiles) {
		super(domain, label, affectedFiles);
		init((InteractionGraphImpl) interactionGraph);
	}

	/**
	 * Constructor.
	 *
	 * @param domain
	 * @param label
	 * @param options
	 * @param affectedFiles
	 */
	public InteractionGraphCommand(TransactionalEditingDomain domain, String label, InteractionGraph interactionGraph,
			Map<?,?> options, List<?> affectedFiles) {
		super(domain, label, options, affectedFiles);		
		init((InteractionGraphImpl) interactionGraph);
	}

	private void init(InteractionGraphImpl interactionGraph) {
		this.interactionGraph = (InteractionGraphImpl) interactionGraph;
		this.gridSpacing = this.interactionGraph.getGridSpacing();
		this.interactionGraphService = new InteractionGraphService(this.interactionGraph);
	}
	
	public InteractionGraphEditActionBuilder addAction() {
		InteractionGraphEditActionBuilder builder = new InteractionGraphEditActionBuilder(interactionGraph);
		actions.add(builder.action());
		return builder;
	}
	
	public void addLifeline(CreateElementRequestAdapter elementAdapter, ViewDescriptor descriptor, Rectangle rect) {
		addAction()
			.prepare(()->interactionGraphService.canAddLifeline(elementAdapter, rect))
			.apply(()->{return interactionGraphService.addLifeline(getEditingDomain(), rect);})
			.handleResult(
				(CommandResult r,Cluster cluster) -> {
					elementAdapter.setNewElement(cluster.getElement());
					descriptor.setView(cluster.getView());
				});		
	}

	public void moveLifeline(Lifeline lifeline, Point moveDelta) {
		addAction()
			.prepare(()->interactionGraphService.canMoveLifeline(lifeline, moveDelta))
			.apply(() -> interactionGraphService.moveLifeline(lifeline, moveDelta));
	}

	public void deleteLifeline(Lifeline lifeline) {
		addAction()
			.prepare(()->interactionGraphService.canDeleteLifeline(lifeline))
			.apply(() -> interactionGraphService.deleteLifeline(lifeline));
	}
	
	public void nudgeLifeline(Lifeline lifeline, Point moveDelta) {
		addAction().
			prepare(() -> interactionGraphService.canNudgeLifeline(lifeline, moveDelta)).
			apply(	() -> interactionGraphService.nudgeLifeline(lifeline, moveDelta));
	}

	public void resizeLifeline(Lifeline lifeline, Dimension sizeDelta) {
		addAction().
			prepare(() -> interactionGraphService.canResizeLifeline(lifeline, sizeDelta)).
			apply(() -> interactionGraphService.resizeLifeline(lifeline, sizeDelta));
	}

	// TODO: @etxacam Reply messages
	// TODO: @etxacam Self messages
	
	public void addMessage(MessageSort msgSort, CreateElementRequestAdapter elementAdapter, ViewDescriptor descriptor, 
			Element source, Point srcAnchor, Element target, Point trgAnchor) {

		addAction().
			prepare(() -> interactionGraphService.canAddMessage(getEditingDomain(), msgSort, elementAdapter, 
					descriptor, source, srcAnchor, target, trgAnchor)).		
			apply(()->interactionGraphService.addMessage(getEditingDomain(), msgSort, 
						elementAdapter, descriptor, source, srcAnchor, target, trgAnchor)).
			handleResult(
				(CommandResult r, Link message) -> {				
					elementAdapter.setNewElement(message.getElement());
					descriptor.setView(message.getView());
			});
	}
	
	public void deleteMessage(Message msg) {				
		addAction().
			prepare(()->interactionGraphService.canDeleteMessage(msg)).
			apply(()->interactionGraphService.deleteMessage(msg));
	}

	public void nudgeMessage(Message msg, Point delta) {
		// TODO: @etxacam Need to handle Lost & Found messages, messages with gates and create message.		
		addAction().
			prepare(()->interactionGraphService.canNudgeMessage(msg, delta)).
			apply(()->interactionGraphService.nudgeMessage(msg, delta));
	}

	public void nudgeMessageEnd(MessageEnd msgEnd, Point location) {
		addAction().
			prepare(()->interactionGraphService.canNudgeMessageEnd(msgEnd, location)).
			apply(()->interactionGraphService.nudgeMessageEnd(msgEnd, location));
	}
	

	// TODO: @etxacam Check self messages
	public void moveMessage(Message msg, Point moveDelta) {
		addAction().
			prepare(() -> interactionGraphService.canMoveMessage(msg, moveDelta)).
			apply(() -> interactionGraphService.moveMessage(msg, moveDelta));
	}
	
	public void moveMessageEnd(MessageEnd msgEnd, Lifeline toLifeline, Point location) {		
		addAction().
			prepare(()->interactionGraphService.canMoveMessageEnd(msgEnd, toLifeline, location)).
			apply(()->interactionGraphService.moveMessageEnd(msgEnd, toLifeline, location));
	}
	
	
	public void nudgeGate(Gate gate, Point location) {		
		addAction().
			prepare(() -> interactionGraphService.canNudgeGate(gate, location)).
			apply(() -> interactionGraphService.nudgeGate(gate, location));
	}
	
	public void moveGate(Gate gate, InteractionFragment intFragment, Point location) {
		addAction().
			prepare(() -> interactionGraphService.canMoveGate(gate, intFragment, location)).
			apply(() -> interactionGraphService.moveGate(gate, intFragment, location));
	}

	public void nudgeExecutionSpecification(ExecutionSpecification execSpec, int delta) {
		addAction().
			prepare(() -> interactionGraphService.canNudgeExecutionSpecification(execSpec, delta)).
			apply(() -> interactionGraphService.nudgeExecutionSpecification(execSpec, delta));
	}
	
	public void resizeExecutionSpecification(ExecutionSpecification execSpec, boolean topSide, int delta) {
		addAction().
			prepare(() -> interactionGraphService.canResizeExecutionSpecification(execSpec, topSide, delta)).
			apply(() -> interactionGraphService.resizeExecutionSpecification(execSpec, topSide, delta));

	}

	public void moveExecutionSpecification(ExecutionSpecification execSpec, Lifeline lifeline, Point point) {
		addAction().
			prepare(() -> interactionGraphService.canMoveExecutionSpecification(execSpec, lifeline, point)).
			apply(() -> interactionGraphService.moveExecutionSpecification(execSpec, lifeline, point));
	}

	public void moveExecutionSpecificationOccurrence(ExecutionSpecification execSpec, OccurrenceSpecification occurrenceSpec, Point point) {
		addAction().
			prepare(() -> interactionGraphService.canMoveExecutionSpecificationOccurrence(execSpec, occurrenceSpec, point)).
			apply(() -> interactionGraphService.moveExecutionSpecificationOccurrence(execSpec, occurrenceSpec, point));
	}

	public void deleteExecutionSpecification(ExecutionSpecification execSpec) {
		addAction().
			prepare(()->interactionGraphService.canDeleteExecutionSpecification(execSpec)).
			apply(() -> interactionGraphService.deleteExecutionSpecification(execSpec));
	}
	
	public void addInteractionUse(CreateElementRequestAdapter elementAdapter, ViewDescriptor descriptor, Rectangle rect) {
		addAction().
			prepare(() -> interactionGraphService.canAddInteractionUse(elementAdapter, descriptor, rect)).
			apply(() -> interactionGraphService.addInteractionUse(getEditingDomain(), elementAdapter, descriptor, rect)).
			handleResult((ClusterImpl cluster) -> {
				elementAdapter.setNewElement(cluster.getElement());
				descriptor.setView(cluster.getView());
			});
	}

	public void nudgeInteractionUse(InteractionUse intUse, Point delta) {
		addAction().
			prepare(()->interactionGraphService.canNudgeInteractionUse(intUse,delta)).
			apply(()->interactionGraphService.nudgeInteractionUse(intUse,delta));
	}

	public void nudgeResizeInteractionUse(InteractionUse intUse, Rectangle rect) {
		addAction().
			prepare(()->interactionGraphService.canNudgeResizeInteractionUse(intUse,rect)).
			apply(()->interactionGraphService.nudgeResizeInteractionUse(intUse,rect));
	}
	
	public void resizeInteractionUse(InteractionUse intUse, Rectangle rect) {
		addAction().
			prepare(()->interactionGraphService.canResizeInteractionUse(intUse,rect)).
			apply(()->interactionGraphService.resizeInteractionUse(intUse,rect));
	}

	public void moveInteractionUse(InteractionUse intUse, Rectangle rect) {
		addAction().
			prepare(()->interactionGraphService.canMoveInteractionUse(intUse,rect)).
			apply(()->interactionGraphService.moveInteractionUse(intUse,rect));		
	}

	public void deleteInteractionUse(InteractionUse intUse) {
		addAction().
			prepare(()->interactionGraphService.canDeleteInteractionUse(intUse)).
			apply(()->interactionGraphService.deleteInteractionUse(intUse));		
	}
		
	@Override
	public boolean canExecute() {
		for (InteractionGraphEditAction action : actions) {
			if (!action.prepare()) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		if (!canExecute()) {
			return CommandResult.newCancelledCommandResult();
		}

		CommandResult res;
		try {
			for (InteractionGraphEditAction action : actions) {
				if (!action.apply(interactionGraph)) {
					return CommandResult.newErrorCommandResult("Could not apply action.");
				}
			}

			ICommand cmd = buildDelegateCommands(getEditingDomain(), getLabel());
			if (cmd == null || !cmd.canExecute()) {
				return CommandResult.newCancelledCommandResult();
			}

			cmd.execute(monitor, info);
			res = cmd.getCommandResult(); 
			if (!res.getStatus().isOK()) {
				if (res.getStatus().getException() != null)
					throw new ExecutionException("Can not apply changes.", res.getStatus().getException());	
				UMLDiagramEditorPlugin.log.log(res.getStatus());
				return res;
			}
			for (InteractionGraphEditAction action : actions) {
				action.handleResult(cmd.getCommandResult());
			}
		} catch (Exception e) {
			UMLDiagramEditorPlugin.log.error(e.getMessage(), e);;
			throw e;
		}
		
		return res;
	}

	protected ICommand buildDelegateCommands(TransactionalEditingDomain editingDomain, String label) {
		ICompositeCommand command = new CompositeTransactionalCommand(editingDomain, label);
		calculateLifelinesEditingCommand(editingDomain, command);
		calculateInteractionUseEditingCommand(editingDomain, command);
		calculateExecutionSpecificationEditingCommand(editingDomain, command);
		calculateMessagesChanges(editingDomain, command);
		calculateFragmentsChanges(editingDomain, command);
		calculateGateChanges(editingDomain, command);

		// TODO: @etxacam Why is not resizing when creating new Lifelines????
		updateBoundsChanges(command, Collections.singletonList(interactionGraph), false);
		
		// Reorder views inside Lifelines
		for (Cluster c : interactionGraph.getLifelineClusters()) {
			List<Node> newValues = NodeUtilities.flattenKeepClusters(c.getNodes()).stream().filter(Cluster.class::isInstance).
					filter(d->d.getView()!=null).collect(Collectors.toList());
			if (c.getView() != null) {
				List<View> oldValues = c.getView().getChildren();			
				updateZOrder(command, c.getView(), oldValues, newValues);
			}
		}

		// Reorder InteractionUse, Combined Fragments and Lifelines
		View containerView = ViewUtilities.getViewWithType(interactionGraph.getInteractionView(), InteractionInteractionCompartmentEditPart.VISUAL_ID); 
		List<View> oldValues = containerView.getChildren();
		List<Node> newValues = new ArrayList<>();
		newValues.addAll(interactionGraph.getLifelineClusters().stream().collect(Collectors.toList()));
		newValues.addAll(interactionGraph.getFragmentClusters().stream().collect(Collectors.toList()));
		updateZOrder(command, containerView, oldValues, newValues);
		
		// TODO: Refresh diagram ??
		
		
		return command;
	}

	private void calculateLifelinesEditingCommand(TransactionalEditingDomain editingDomain, ICompositeCommand command) {
		List<Lifeline> lifelines = interactionGraph.getInteraction().getLifelines();
		List<Lifeline> graphLifelines = interactionGraph.getLifelineClusters().stream().map(Node::getElement)
				.map(Lifeline.class::cast).collect(Collectors.toList());

		// TODO: index of created element
		Function<Lifeline, IUndoableOperation> addCommandFunction = (d) -> createLifelinesEditingCommand(editingDomain, d);
		Function<Lifeline, IUndoableOperation> removeCommandFunction = (d) -> deleteLifelinesEditingCommand(editingDomain, d);
		createCommandsForCollectionChanges(command, interactionGraph.getInteraction(),
				UMLPackage.Literals.INTERACTION__LIFELINE, lifelines, graphLifelines, addCommandFunction,
				removeCommandFunction, true, true);


		for (Cluster c : interactionGraph.getLifelineClusters()) {
			Lifeline lf = (Lifeline) c.getElement();
			List<InteractionFragment> fragments = new ArrayList<>();
			for (Node n : NodeUtilities.flatten((ClusterImpl)c)) {
				Element el = n.getElement();
				if (el instanceof OccurrenceSpecification || el instanceof ExecutionSpecification) {
					fragments.add((InteractionFragment)el);
				} else if (el instanceof InteractionUse && !fragments.contains(el)) {
					fragments.add((InteractionFragment)el);
				}
			}			
			createCommandsForCollectionChanges(editingDomain, command, lf,
					UMLPackage.Literals.LIFELINE__COVERED_BY, lf.getCoveredBys(), fragments, false, false);
		}						
	}

	private ICommand createLifelinesEditingCommand(TransactionalEditingDomain editingDomain, Lifeline lifeline) {
		ICompositeCommand cmd = new CompositeTransactionalCommand(editingDomain, "Create Lifeline");
		final CreateElementRequest createReq = new CreateElementRequest(editingDomain,
				interactionGraph.getInteraction(), UMLElementTypes.Lifeline_Shape);
		cmd.add(new CreateNodeElementCommand(createReq, lifeline));
		cmd.add(new CreateNodeViewCommand(editingDomain, interactionGraph.getNodeFor(lifeline),
				new ViewDescriptor(
						new SemanticElementAdapter(lifeline,
								UMLElementTypes.getElementType(LifelineEditPart.VISUAL_ID)),
						org.eclipse.gmf.runtime.notation.Node.class, LifelineEditPart.VISUAL_ID,
						((GraphicalEditPart) interactionGraph.getEditPart()).getDiagramPreferencesHint()),
				ViewUtil.getChildBySemanticHint(interactionGraph.getInteractionView(),
						InteractionInteractionCompartmentEditPart.VISUAL_ID)));
		return cmd;
	}

	private ICommand deleteLifelinesEditingCommand(TransactionalEditingDomain editingDomain, Lifeline lifeline) {
		ICompositeCommand cmd = new CompositeTransactionalCommand(editingDomain, "Delete Lifeline");
		cmd.add(new DeleteCommand(editingDomain, ViewUtilities.getViewForElement(interactionGraph.getDiagram(), lifeline)));
		cmd.add(new DestroyElementCommand(new DestroyElementRequest(editingDomain, lifeline, false)));
		return cmd;
	}

	private void calculateInteractionUseEditingCommand(TransactionalEditingDomain editingDomain, ICompositeCommand command) {
		List<InteractionUse> interactionUses  = interactionGraph.getInteraction().getFragments().stream().
				filter(InteractionUse.class::isInstance).
				map(InteractionUse.class::cast).collect(Collectors.toList());
		List<InteractionUse> graphInteractionUses = interactionGraph.getFragmentClusters().stream().
				map(Node::getElement).filter(InteractionUse.class::isInstance).
				map(InteractionUse.class::cast).collect(Collectors.toList());
		
		Function<InteractionUse, IUndoableOperation> addCommandFunction = 
				(d) -> createInteractionUseEditingCommand(editingDomain, d);
		Function<InteractionUse, IUndoableOperation> removeCommandFunction = 
				(d) -> deleteInteractionUseEditingCommand(editingDomain, d);
		
		createCommandsForCollectionChanges(command, interactionGraph.getInteraction(),
				UMLPackage.Literals.INTERACTION__FRAGMENT, interactionUses, graphInteractionUses, addCommandFunction, removeCommandFunction);
		
		updateBoundsChanges(command, interactionGraph.getFragmentClusters().stream().
				filter(d -> d.getElement() instanceof InteractionUse).collect(Collectors.toList()), true);		
	}
	
	private ICommand createInteractionUseEditingCommand(TransactionalEditingDomain editingDomain, InteractionUse interactionUse) {
		ICompositeCommand cmd = new CompositeTransactionalCommand(editingDomain, "Create ExecutionSpecification");
		final CreateElementRequest createReq = new CreateElementRequest(editingDomain, interactionGraph.getInteraction(), 
				UMLElementTypes.InteractionUse_Shape);
		cmd.add(new CreateNodeElementCommand(createReq, interactionUse));
		
		//Node interactionUseNode = interactionGraph.getClusterFor(interactionUse);		
		cmd.add(new CreateNodeViewCommand(editingDomain, interactionGraph.getClusterFor(interactionUse),
				new ViewDescriptor(
						new SemanticElementAdapter(interactionUse,
								UMLElementTypes.InteractionUse_Shape),
						org.eclipse.gmf.runtime.notation.Node.class, InteractionUseEditPart.VISUAL_ID,
						((GraphicalEditPart) interactionGraph.getEditPart()).getDiagramPreferencesHint()),
				ViewUtil.getChildBySemanticHint(interactionGraph.getInteractionView(),
						InteractionInteractionCompartmentEditPart.VISUAL_ID)));
		
		return cmd;
	}

	private ICommand deleteInteractionUseEditingCommand(TransactionalEditingDomain editingDomain, InteractionUse interactionUse) {
		ICompositeCommand cmd = new CompositeTransactionalCommand(editingDomain, "Delete Lifeline");
		cmd.add(new DeleteCommand(editingDomain, interactionGraph.getClusterFor(interactionUse).getView()));
		cmd.add(new DestroyElementCommand(new DestroyElementRequest(editingDomain, interactionUse, false)));
		return cmd;
	}

	private void calculateExecutionSpecificationEditingCommand(TransactionalEditingDomain editingDomain, ICompositeCommand command) {
		List<ExecutionSpecification> executionSpecifications  = interactionGraph.getInteraction().getFragments().stream().
				filter(ExecutionSpecification.class::isInstance).
				map(ExecutionSpecification.class::cast).collect(Collectors.toList());
		List<ExecutionSpecification> graphExecutionSpecifications = interactionGraph.getLifelineClusters().stream().
				flatMap(d->d.getAllNodes().stream()).map(Node::getElement).filter(ExecutionSpecification.class::isInstance).
				map(ExecutionSpecification.class::cast).collect(Collectors.toList());

		// TODO: index of created element
		Function<ExecutionSpecification, IUndoableOperation> addCommandFunction = 
				(d) -> createExecutionSpecificationEditingCommand(editingDomain, d);
		Function<ExecutionSpecification, IUndoableOperation> removeCommandFunction = 
				(d) -> deleteExecutionSpecificationEditingCommand(editingDomain, d);
		createCommandsForCollectionChanges(command, interactionGraph.getInteraction(),
				UMLPackage.Literals.INTERACTION__FRAGMENT, executionSpecifications, graphExecutionSpecifications, addCommandFunction, removeCommandFunction);
		
		// Update and / or create Star and Finish occurrences
		for (ExecutionSpecification execSpec : graphExecutionSpecifications) {
			Cluster execSpecCluster = interactionGraph.getClusterFor(execSpec);
			List<Node> nodes= execSpecCluster.getNodes();
			Node startNode = NodeUtilities.getStartNode(execSpecCluster);
			Node finishNode = NodeUtilities.getFinishNode(execSpecCluster);
			
			if (startNode.getElement() != execSpec.getStart()) {
				OccurrenceSpecification ocurr = (OccurrenceSpecification)startNode.getElement(); 
				if (startNode.getElement().eResource() == null && ocurr instanceof ExecutionOccurrenceSpecification) {
					command.add(new CreateNodeElementCommand(new CreateElementRequest(editingDomain, interactionGraph.getInteraction(), 
							org.eclipse.papyrus.uml.service.types.element.UMLElementTypes.EXECUTION_OCCURRENCE_SPECIFICATION), 
							ocurr));
				}
				
				command.add(new EMFCommandOperation(getEditingDomain(), new SetCommand(
						getEditingDomain(), execSpec, UMLPackage.Literals.EXECUTION_SPECIFICATION__START, ocurr)));
			}
			
			if (finishNode.getElement() != execSpec.getFinish()) {
				OccurrenceSpecification ocurr = (OccurrenceSpecification)finishNode.getElement(); 
				if (finishNode.getElement().eResource() == null && ocurr instanceof ExecutionOccurrenceSpecification) {
					command.add(new CreateNodeElementCommand(new CreateElementRequest(editingDomain, interactionGraph.getInteraction(), 
							org.eclipse.papyrus.uml.service.types.element.UMLElementTypes.EXECUTION_OCCURRENCE_SPECIFICATION), 
							ocurr));
				}

				command.add(new EMFCommandOperation(getEditingDomain(), new SetCommand(
						getEditingDomain(), execSpec, UMLPackage.Literals.EXECUTION_SPECIFICATION__FINISH, ocurr)));				
			}
		}
		
		// Update Parents
		interactionGraph.getLifelineClusters().stream().flatMap(d->d.getAllNodes().stream()).
			filter(d->d.getElement() instanceof ExecutionSpecification).
			filter(d->d.getView() != null && d.getView().eContainer() != NodeUtilities.getLifelineNode(d).getView()).
			forEach(d-> {
				command.add(new EMFCommandOperation(getEditingDomain(), RemoveCommand.create(
						getEditingDomain(), d.getView())));
				command.add(new EMFCommandOperation(getEditingDomain(), AddCommand.create(
						getEditingDomain(), NodeUtilities.getLifelineNode(d).getView(), 
						NotationPackage.Literals.VIEW__PERSISTED_CHILDREN, d.getView())));
			});

		updateBoundsChanges(command, interactionGraph.getLifelineClusters().stream().
				flatMap(d->d.getAllNodes().stream()).filter(d -> d.getElement() instanceof ExecutionSpecification).
				map(Node::getParent).collect(Collectors.toList()), true);
		
	}

	private ICommand createExecutionSpecificationEditingCommand(TransactionalEditingDomain editingDomain, ExecutionSpecification executionSpecification) {
		ICompositeCommand cmd = new CompositeTransactionalCommand(editingDomain, "Create ExecutionSpecification");
		boolean isActionExecSpec = executionSpecification instanceof ActionExecutionSpecification; 
		final CreateElementRequest createReq = new CreateElementRequest(editingDomain, interactionGraph.getInteraction(), 
				isActionExecSpec ? UMLElementTypes.ActionExecutionSpecification_Shape : 
								   UMLElementTypes.BehaviorExecutionSpecification_Shape);
		cmd.add(new CreateNodeElementCommand(createReq, executionSpecification));
		
		Node execSpecNode = interactionGraph.getNodeFor(executionSpecification);
		Cluster lfCluster = execSpecNode.getParent();
		while (lfCluster != null && !(lfCluster.getElement() instanceof Lifeline)) {
			lfCluster = lfCluster.getParent();
		}
		
		
		String visualId = isActionExecSpec ? ActionExecutionSpecificationEditPart.VISUAL_ID :
											 BehaviorExecutionSpecificationEditPart.VISUAL_ID;
		cmd.add(new CreateNodeViewCommand(editingDomain, interactionGraph.getNodeFor(executionSpecification),
				new ViewDescriptor(
						new SemanticElementAdapter(executionSpecification,
								UMLElementTypes.getElementType(visualId)),
						org.eclipse.gmf.runtime.notation.Node.class, visualId,
						((GraphicalEditPart) interactionGraph.getEditPart()).getDiagramPreferencesHint()),
				lfCluster.getView()));
		
		return cmd;
	}

	private ICommand deleteExecutionSpecificationEditingCommand(TransactionalEditingDomain editingDomain, ExecutionSpecification executionSpecification) {
		ICompositeCommand cmd = new CompositeTransactionalCommand(editingDomain, "Delete Lifeline");
		cmd.add(new DeleteCommand(editingDomain, interactionGraph.getNodeFor(executionSpecification).getView()));
		cmd.add(new DestroyElementCommand(new DestroyElementRequest(editingDomain, executionSpecification, false)));
		return cmd;
	}

	private void calculateFragmentsChanges(TransactionalEditingDomain editingDomain, ICompositeCommand command) {
		List<InteractionFragment> fragments = interactionGraph.getInteraction().getFragments();
		/*List<InteractionFragment> graphFragments = interactionGraph.getRows().stream().flatMap(d -> d.getNodes().stream())
				.map(Node::getElement).filter(InteractionFragment.class::isInstance)
				.map(InteractionFragment.class::cast).collect(Collectors.toList());*/
		
		NodeOrderResolver orderResolver = new NodeOrderResolver(interactionGraph);
		List<NodeImpl> fragmentNodes = orderResolver.getOrderedNodes();
		List<InteractionFragment> graphFragments = fragmentNodes.stream()
				.map(Node::getElement).filter(InteractionFragment.class::isInstance)
				.map(InteractionFragment.class::cast).collect(Collectors.toList());
		graphFragments = new ArrayList<>(new LinkedHashSet<>(graphFragments));
		// Here we can only handle the fragment order.
		// Elements are created in the Actions and added here.
		// Behavior specs delete and add itself to fragments.

		// Reorder fragments
		createCommandsForCollectionChanges(command, interactionGraph.getInteraction(),
				UMLPackage.Literals.INTERACTION__FRAGMENT, fragments, graphFragments, null, null, false, false);
		
		// Update Fragments with views
		List<Node> fragmentsToUpdateBounds = fragmentNodes.stream().filter(d->d.getView() != null).filter(d->!(d.getElement() instanceof ExecutionSpecification)).
			collect(Collectors.toList());
		updateBoundsChanges(command, fragmentsToUpdateBounds, false);
	}

	private void calculateMessagesChanges(TransactionalEditingDomain editingDomain, ICompositeCommand command) {
		List<Message> messages = interactionGraph.getInteraction().getMessages();
		List<Message> graphMessages = interactionGraph.getMessageLinks().stream().map(Link::getElement)
				.map(Message.class::cast).collect(Collectors.toList());
		
		Function<Message, IUndoableOperation> addCommandFunction = (d) -> createMessageEditingCommand(editingDomain, d);
		Function<Message, IUndoableOperation> removeCommandFunction = (d) -> deleteMessageEditingCommand(editingDomain, d);

		createCommandsForCollectionChanges(command, interactionGraph.getInteraction(),
				UMLPackage.Literals.INTERACTION__MESSAGE, messages, graphMessages, addCommandFunction,
				removeCommandFunction, false, false);		
		
		EditPartViewer viewer = interactionGraph.getEditPartViewer();
		for (Link lk : interactionGraph.getMessageLinks()) {
			Message msg = (Message)lk.getElement();
			if (lk.getSource() == null) {
				command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
						editingDomain, msg, UMLPackage.Literals.MESSAGE__SEND_EVENT, SetCommand.UNSET_VALUE)));				
			} else if (msg.getSendEvent() != lk.getSource().getElement()) {
				command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
						editingDomain, msg, UMLPackage.Literals.MESSAGE__SEND_EVENT, lk.getSource().getElement())));
			}

			if (lk.getTarget() == null) {
				command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
						editingDomain, msg, UMLPackage.Literals.MESSAGE__RECEIVE_EVENT, SetCommand.UNSET_VALUE)));
			} else if (msg.getReceiveEvent() != lk.getTarget().getElement()) {
				command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
						editingDomain, msg, UMLPackage.Literals.MESSAGE__RECEIVE_EVENT, lk.getTarget().getElement())));
			}
			
			Edge edge = lk.getEdge();
			if (edge != null) {
				View srcView = null;
				Point srcAnchorPoint = null;
				if (lk.getSource() != null) {
					srcAnchorPoint = lk.getSource().getBounds().getCenter();
					srcView = lk.getSourceAnchoringNode().getView();
					command.add(new SetLinkViewAnchorCommand(editingDomain, lk, SetLinkViewAnchorCommand.Anchor.SOURCE, 
							srcView, srcAnchorPoint, "Set Source Link Anchor", null));
				} else {
					command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
							editingDomain, lk.getEdge(), NotationPackage.Literals.EDGE__SOURCE, SetCommand.UNSET_VALUE)));					
					command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
							editingDomain, lk.getEdge(), NotationPackage.Literals.EDGE__SOURCE_ANCHOR, SetCommand.UNSET_VALUE)));					
				}
			
				View trgView = null;
				Point trgAnchorPoint = null;
				boolean isSelfLink = NodeUtilities.isSelfLink(lk);
				if (lk.getTarget() != null) {
					trgAnchorPoint = lk.getTarget().getBounds().getCenter();
					trgView = lk.getTargetAnchoringNode().getView();
					
					if (isSelfLink && (trgAnchorPoint.y - srcAnchorPoint.y) < gridSpacing)
						trgAnchorPoint.y = srcAnchorPoint.y + gridSpacing;
					
					command.add(new SetLinkViewAnchorCommand(editingDomain, lk, SetLinkViewAnchorCommand.Anchor.TARGET, 
							trgView, trgAnchorPoint, "Set Target Link Anchor", null));
				} else {
					command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
							editingDomain, lk.getEdge(), NotationPackage.Literals.EDGE__TARGET, SetCommand.UNSET_VALUE)));					
					command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
							editingDomain, lk.getEdge(), NotationPackage.Literals.EDGE__TARGET_ANCHOR, SetCommand.UNSET_VALUE)));					
				}
				/*
				// TODO: Layout SelfMessage using columns....
				// TODO: Fix the MessageRouter to align the feedback 
				// Handling Bendpoints for self message
				if (isSelfLink) {
					RelativeBendpoints bendpoints = (RelativeBendpoints)edge.getBendpoints();
					List<RelativeBendpoint> l = new ArrayList<>();
					l.add(new RelativeBendpoint(0, 0, srcAnchorPoint.x - trgAnchorPoint.x, srcAnchorPoint.y - trgAnchorPoint.y));
					l.add(new RelativeBendpoint(50, 0, srcAnchorPoint.x + 50 - trgAnchorPoint.x, srcAnchorPoint.y - trgAnchorPoint.y));
					l.add(new RelativeBendpoint(50, trgAnchorPoint.y - srcAnchorPoint.y, srcAnchorPoint.x + 50 - trgAnchorPoint.x, 0));
					l.add(new RelativeBendpoint(trgAnchorPoint.x - srcAnchorPoint.x, trgAnchorPoint.y - srcAnchorPoint.y, 0, 0));
					command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
							editingDomain, bendpoints, NotationPackage.Literals.RELATIVE_BENDPOINTS__POINTS, l)));
					// command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
					//	 	editingDomain, edge, NotationPackage.Literals.EDGE__BENDPOINTS, bendpoints)));
				}*/
			}
			
			// Handling Destroy Ocurrences
			if (NodeUtilities.isDestroyOcurrenceSpecification(lk.getTarget()) && edge != null) {
				View lifelineView = (View)edge.getTarget().eContainer(); 
				Node lifelineNode = NodeUtilities.getLifelineNode(lk.getTarget());
				if (lifelineView != null && lifelineView.getElement() != lifelineNode.getElement()) {
					command.add(new EMFCommandOperation(getEditingDomain(), RemoveCommand.create(
							getEditingDomain(), edge.getTarget())));
					command.add(new EMFCommandOperation(getEditingDomain(), AddCommand.create(
							getEditingDomain(), lifelineNode.getView(), 
							NotationPackage.Literals.VIEW__PERSISTED_CHILDREN, lk.getTarget().getView())));
				}
			}
		}
	}	
	
	private ICommand createMessageEditingCommand(TransactionalEditingDomain editingDomain, Message message) {
		// MessageOcurrenceSpecifications are created by the MessageEditHelper
		Link link = interactionGraph.getLinkFor(message);
		ICompositeCommand cmd = new CompositeTransactionalCommand(editingDomain, "Create Message");
		// TODO: @etxacam Check the message to get the Element Type 
		IElementType type = UMLElementTypes.Message_AsynchEdge;
		switch (message.getMessageSort()) {
			case ASYNCH_CALL_LITERAL:
			case ASYNCH_SIGNAL_LITERAL:
				type = UMLElementTypes.Message_AsynchEdge; break;
			case SYNCH_CALL_LITERAL:
				type = UMLElementTypes.Message_SynchEdge; break;
			case CREATE_MESSAGE_LITERAL:
				type = UMLElementTypes.Message_CreateEdge; break;
			case DELETE_MESSAGE_LITERAL:
				type = UMLElementTypes.Message_DeleteEdge; break;
			case REPLY_LITERAL:
				type = UMLElementTypes.Message_ReplyEdge; break;
		}

		MessageEnd sendEvent = message.getSendEvent();
		if (sendEvent instanceof MessageOccurrenceSpecification) {
			cmd.add(new CreateNodeElementCommand(new CreateElementRequest(editingDomain, interactionGraph.getInteraction(), 
					org.eclipse.papyrus.uml.service.types.element.UMLElementTypes.MESSAGE_OCCURRENCE_SPECIFICATION), 
					sendEvent));
		} else if (sendEvent instanceof Gate) {
			Element gateOwner = link.getSource().getParent().getElement();
			cmd.add(new CreateNodeElementCommand(new CreateElementRequest(editingDomain, gateOwner, 
					org.eclipse.papyrus.uml.service.types.element.UMLElementTypes.GATE), 
					sendEvent));		
			cmd.add(new CreateNodeViewCommand(editingDomain, link.getSource(),
					new ViewDescriptor(
							new SemanticElementAdapter(sendEvent,UMLElementTypes.getElementType(GateEditPart.VISUAL_ID)),
							org.eclipse.gmf.runtime.notation.Node.class, GateEditPart.VISUAL_ID,
							((GraphicalEditPart) interactionGraph.getEditPart()).getDiagramPreferencesHint()),
							link.getSource().getParent().getView()));			
		}
		
		MessageEnd receiveEvent = message.getReceiveEvent();
		if (receiveEvent instanceof MessageOccurrenceSpecification) {
			cmd.add(new CreateNodeElementCommand(new CreateElementRequest(editingDomain, interactionGraph.getInteraction(), 
					receiveEvent instanceof DestructionOccurrenceSpecification ? 
						UMLElementTypes.DestructionOccurrenceSpecification_Shape : 	
						org.eclipse.papyrus.uml.service.types.element.UMLElementTypes.MESSAGE_OCCURRENCE_SPECIFICATION), 
					receiveEvent));
		} else if (receiveEvent instanceof Gate) {
			Element gateOwner = link.getTarget().getParent().getElement();
			cmd.add(new CreateNodeElementCommand(new CreateElementRequest(editingDomain, gateOwner, 
					org.eclipse.papyrus.uml.service.types.element.UMLElementTypes.GATE), 
					receiveEvent));	
			cmd.add(new CreateNodeViewCommand(editingDomain, link.getTarget(),
					new ViewDescriptor(
							new SemanticElementAdapter(receiveEvent,UMLElementTypes.getElementType(GateEditPart.VISUAL_ID)),
							org.eclipse.gmf.runtime.notation.Node.class, GateEditPart.VISUAL_ID,
							((GraphicalEditPart) interactionGraph.getEditPart()).getDiagramPreferencesHint()),
							link.getTarget().getParent().getView()));
		}
		
		final CreateElementRequest createReq = new CreateElementRequest(editingDomain,
				interactionGraph.getInteraction(), type);
		String visualId = ((IHintedType)type).getSemanticHint();
		cmd.add(new CreateNodeElementCommand(createReq, message));
		
		boolean isDestroyMessage = (receiveEvent instanceof DestructionOccurrenceSpecification);
		if (isDestroyMessage) {
			String hint = DestructionOccurrenceSpecificationEditPart.VISUAL_ID;
			DestructionOccurrenceSpecification dos = (DestructionOccurrenceSpecification)receiveEvent;
			Cluster lifelineCluster = NodeUtilities.getLifelineNode(link.getTarget());
			cmd.add(new CreateNodeViewCommand(editingDomain, link.getTarget(),
					new ViewDescriptor(
							new SemanticElementAdapter(dos,UMLElementTypes.getElementType(hint)),
							org.eclipse.gmf.runtime.notation.Node.class, hint,
							((GraphicalEditPart) interactionGraph.getEditPart()).getDiagramPreferencesHint()),
						lifelineCluster.getView()));
		}

		cmd.add(new CreateEdgeViewCommand(editingDomain, interactionGraph.getLinkFor(message),
					new ViewDescriptor(
							new SemanticElementAdapter(message,
									UMLElementTypes.getElementType(visualId)),
							org.eclipse.gmf.runtime.notation.Edge.class, visualId,
							((GraphicalEditPart) interactionGraph.getEditPart()).getDiagramPreferencesHint()),
						interactionGraph.getDiagram(), 
						link.getSourceAnchoringNode(), link.getSourceLocation(), 
						link.getTargetAnchoringNode(), link.getTargetLocation()));
		return cmd;
	}

	private void calculateGateChanges(TransactionalEditingDomain editingDomain, ICompositeCommand command) {
		// Interaction Gates
		List<Node> allGates = NodeUtilities.flatten(interactionGraph).stream().flatMap(d->d.getAllGates().stream()).
				collect(Collectors.toList());
		
		updateBoundsChanges(command, allGates, false);
		
	}
	
	private ICommand deleteMessageEditingCommand(TransactionalEditingDomain editingDomain, Message message) {
		ICompositeCommand cmd = new CompositeTransactionalCommand(editingDomain, "Delete Message");		
		cmd.add(new DeleteCommand(editingDomain, ViewUtilities.getViewForElement(interactionGraph.getDiagram(), message)));
		if (message.getSendEvent() instanceof Gate) {
			cmd.add(new DeleteCommand(editingDomain, ViewUtilities.getViewForElement(interactionGraph.getDiagram(), message.getSendEvent())));			
		}

		if (message.getReceiveEvent() instanceof DestructionOccurrenceSpecification || message.getReceiveEvent() instanceof Gate) {
			cmd.add(new DeleteCommand(editingDomain, ViewUtilities.getViewForElement(interactionGraph.getDiagram(), message.getReceiveEvent())));			
		}
		
		cmd.add(new DestroyElementCommand(new DestroyElementRequest(editingDomain, message.getSendEvent(), false)));
		cmd.add(new DestroyElementCommand(new DestroyElementRequest(editingDomain, message.getReceiveEvent(), false)));
		cmd.add(new DestroyElementCommand(new DestroyElementRequest(editingDomain, message, false)));

		return cmd;
	}

	private <T extends EObject> void createCommandsForCollectionChanges(TransactionalEditingDomain editingDomain, 
			ICompositeCommand cmd, EObject container, EStructuralFeature feature, List<T> oldValues, List<T> newValues, 
			boolean updateBounds, boolean updateParts) {
		Function<T, IUndoableOperation> addCommandFunction = (d) -> new EMFCommandOperation(editingDomain, 
				AddCommand.create(editingDomain, container, feature, d));
		Function<T, IUndoableOperation> removeCommandFunction = (d) -> new EMFCommandOperation(editingDomain, 
				RemoveCommand.create(editingDomain, container, feature, d));
		
		createCommandsForCollectionChanges(cmd, container, feature, oldValues, newValues, 
				addCommandFunction, removeCommandFunction, updateBounds, updateParts);
	}
	
	private <T extends EObject> void createCommandsForCollectionChanges(ICompositeCommand cmd, EObject container,
			EStructuralFeature feature, List<T> oldList, List<T> newList, Function<T, IUndoableOperation> addCommand,
			Function<T, IUndoableOperation> removeCommand, boolean updateBounds, boolean updateParts) {

		final List<T> newValues = new ArrayList<>(newList);
		createCommandsForCollectionChanges(cmd, container, feature, oldList, newList, addCommand, removeCommand);
		
		// ReorderCommands		'
		int index = 0;
		for (T obj : newValues) {
			if (feature != null) {
				cmd.add(new TransactionalCommandProxy(getEditingDomain(),
						new MoveCommand(getEditingDomain(), container, feature, obj, index) {
							@Override
							public boolean doCanExecute() {
								return true;
							}
						}, "Rearrange collection", Collections.EMPTY_LIST));
			}
			
			if (updateBounds) {
				NodeImpl node = interactionGraph.getNodeFor((Element) obj);
				cmd.add(new SetNodeViewBoundsCommand(getEditingDomain(), node, node.getConstraints(), updateParts, "Set location",
						Collections.emptyList()));
			}
			index++;
		}
	}

	private <T extends EObject> void createCommandsForCollectionChanges(ICompositeCommand cmd, EObject container,
			EStructuralFeature feature, List<T> oldList, List<T> newList, Function<T, IUndoableOperation> addCommand,
			Function<T, IUndoableOperation> removeCommand) {
		// We copy so the execution / undo do not interference with index calculation
		final List<T> oldValues = new ArrayList<>(oldList);
		final List<T> newValues = new ArrayList<>(newList);
		List<T> objToRemove = oldValues.stream().filter(d -> !newValues.contains(d)).collect(Collectors.toList());
		List<T> objToAdd = newValues.stream().filter(d -> !oldValues.contains(d)).collect(Collectors.toList());

		if (removeCommand != null) {
			for (int i=objToRemove.size()-1; i>=0; i--)
				cmd.add(removeCommand.apply(objToRemove.get(i)));
		}
		
		if (addCommand != null) {
			for (T obj : objToAdd)
				cmd.add(addCommand.apply(obj));
		}
	}

	private void updateZOrder(ICompositeCommand cmd, View container, List<View> oldList, List<? extends Node> newList) {
		final List<View> oldValues = new ArrayList<>(oldList);
		final List<Node> newValues = new ArrayList<>(newList);
		int index = 0;
		for (View v: oldValues) {
			if (v.getElement() == container.getElement())
				index++;
			else 
				break;
		}
			
		for (Node n : newValues) {
			cmd.add(new SetNodeViewZOrderCommand(getEditingDomain(), interactionGraph, n, index));
			index++;
		}
	}

	private void updateBoundsChanges(ICompositeCommand cmd, List<Node> nodes, boolean updateParts) {
		for (Node node : nodes) {
			cmd.add(new SetNodeViewBoundsCommand(getEditingDomain(), node, node.getConstraints(), updateParts, "Set location", Collections.emptyList()));
		}
	}
	
	private InteractionGraphImpl interactionGraph;
	private InteractionGraphService interactionGraphService; 
	private int gridSpacing;
	private List<InteractionGraphEditAction> actions = new ArrayList<>();
}
