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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.SemanticElementAdapter;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Link;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ClusterImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ColumnImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.InteractionGraphImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.NodeImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.NodeUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.RowImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.UMLFactory;
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
			List affectedFiles) {
		super(domain, label, affectedFiles);
		this.interactionGraph = (InteractionGraphImpl) interactionGraph;
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
			Map options, List affectedFiles) {
		super(domain, label, options, affectedFiles);
		this.interactionGraph = (InteractionGraphImpl) interactionGraph;
	}

	public void addLifeline(CreateElementRequestAdapter elementAdapter, ViewDescriptor descriptor, Rectangle rect) {
		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
				if (!result.getStatus().isOK()) {
					return;
				}
				elementAdapter.setNewElement(cluster.getElement());
				descriptor.setView(cluster.getView());
			}

			@Override
			public boolean apply(InteractionGraph graph) {
				int x = rect.x;
				Cluster nextLifeline = graph.getLifelineClusters().stream()
						.filter(d -> ((ClusterImpl) d).getBounds().x >= x).findFirst().orElse(null);
				Lifeline lifeline = UMLFactory.eINSTANCE.createLifeline();
				cluster = graph.addLifeline(lifeline, nextLifeline);
				Rectangle r = ((ClusterImpl) cluster).getBounds();
				if (ViewUtilities.isSnapToGrid(graph.getEditPartViewer(), graph.getDiagram()))
					ViewUtilities.snapToGrid(graph.getEditPartViewer(), graph.getDiagram(), rect);
				r.x = x;				
				int offset = r.width;
				((ClusterImpl) cluster).setBounds(r);
				graph.getColumns().stream().filter(d -> d.getXPosition() > x).map(ColumnImpl.class::cast)
						.forEach(d -> d.nudge(offset));
				graph.layout();
				return true;
			}

			Cluster cluster;
		});
	}

	public void moveLifeline(Lifeline lifeline, Point moveDelta) {
		Node lifelineNode = interactionGraph.getNodeFor(lifeline);
		int index = interactionGraph.getLifelineClusters().indexOf(lifelineNode);

		Rectangle clientAreaBounds = ViewUtilities.getClientAreaBounds(interactionGraph.getEditPartViewer(),
				lifelineNode.getView());
		final Rectangle newClientAreaBounds = clientAreaBounds.getCopy();
		newClientAreaBounds.translate(moveDelta.x, 0);

		Cluster nextLifeline = interactionGraph.getLifelineClusters().stream()
				.filter(l -> l.getBounds().x > newClientAreaBounds.x).findFirst().orElse(null);
		int newindex = nextLifeline == null ? interactionGraph.getLifelineClusters().size()
				: interactionGraph.getLifelineClusters().indexOf(nextLifeline);
		if (newindex < 0) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}

		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}

			@Override
			public boolean apply(InteractionGraph graph) {
				// nudge all after by - size
				// graph.getColumns().stream().filter(d -> d.getIndex() >
				// lifelineNode.getColumn().getIndex()).map(ColumnImpl.class::cast).forEach(d ->
				// d.nudge(-clientAreaBounds.width));
				interactionGraph.moveLifeline(lifeline,
						(Lifeline) (nextLifeline == null ? null : nextLifeline.getElement()));
				// graph.getColumns().stream().filter(d -> d.getIndex() >
				// lifelineNode.getColumn().getIndex()).map(ColumnImpl.class::cast).forEach(d ->
				// d.nudge(clientAreaBounds.width));
				lifelineNode.getBounds().x += moveDelta.x;
				graph.layout();
				return true;
			}
		});
	}

	/**
	 * @param moveDelta
	 */
	public void nudgeLifeline(Lifeline lifeline, Point moveDelta) {
		Node lifelineNode = interactionGraph.getNodeFor(lifeline);
		int index = interactionGraph.getLifelineClusters().indexOf(lifelineNode);
		Rectangle clientAreaBounds = ViewUtilities.getClientAreaBounds(interactionGraph.getEditPartViewer(),
				(View) lifelineNode.getView().eContainer());
		int minX = clientAreaBounds.x;
		if (index > 0) {
			Rectangle prevBounds = interactionGraph.getLifelineClusters().get(index - 1).getBounds();
			minX = Math.max(prevBounds.x + prevBounds.width, minX);
		}

		Rectangle bounds = lifelineNode.getBounds();
		if (bounds.x + moveDelta.x < minX) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}

		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}

			@Override
			public boolean apply(InteractionGraph graph) {
				lifelineNode.getBounds().x += moveDelta.x;
				graph.getColumns().stream().filter(d -> d.getIndex() > lifelineNode.getColumn().getIndex())
						.map(ColumnImpl.class::cast).forEach(d -> d.nudge(moveDelta.x));
				graph.layout();
				return true;
			}
		});

	}

	public void resizeLifeline(Lifeline lifeline, Dimension sizeDelta) {
		Node lifelineNode = interactionGraph.getNodeFor(lifeline);
		int index = interactionGraph.getLifelineClusters().indexOf(lifelineNode);
		Rectangle clientAreaBounds = ViewUtilities.getClientAreaBounds(interactionGraph.getEditPartViewer(),
				(View) lifelineNode.getView().eContainer());

		Rectangle bounds = lifelineNode.getBounds();
		// TODO: Check size of label???
		if (bounds.width + sizeDelta.width <= 1) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}

		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}

			@Override
			public boolean apply(InteractionGraph graph) {
				lifelineNode.getBounds().width += sizeDelta.width;
				graph.getColumns().stream().filter(d -> d.getIndex() > lifelineNode.getColumn().getIndex())
						.map(ColumnImpl.class::cast).forEach(d -> d.nudge(sizeDelta.width));
				graph.layout();
				return true;
			}
		});

	}

	public void addMessage(int msgSort, CreateElementRequestAdapter elementAdapter, ViewDescriptor descriptor, 
			Element source, Point srcAnchor, Element target, Point trgAnchor) {

		if (ViewUtilities.isSnapToGrid(interactionGraph.getEditPartViewer(), interactionGraph.getDiagram())) {
			ViewUtilities.snapToGrid(interactionGraph.getEditPartViewer(), interactionGraph.getDiagram(), srcAnchor);
			ViewUtilities.snapToGrid(interactionGraph.getEditPartViewer(), interactionGraph.getDiagram(), trgAnchor);
		}

		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
				if (!result.getStatus().isOK()) {
					return;
				}
				elementAdapter.setNewElement(message.getElement());
				descriptor.setView(message.getView());
			}

			@Override
			public boolean apply(InteractionGraph graph) {			
				Message msg = UMLFactory.eINSTANCE.createMessage();
				msg.setMessageSort(MessageSort.get(msgSort));
				// TODO generate message name.
				int nCount = graph.getMessageLinks().stream().map(d -> Message.class.cast(d.getElement()).getName()).
						filter(d -> d.matches("Message[0-9]*")).map(d-> d.equals("Message") ? 1 : Integer.parseInt(d.replaceFirst("Message", ""))).						
						sorted(Comparator.reverseOrder()).findFirst().orElse(0)+1;
				if (nCount > 1)
					msg.setName("Message"+nCount);
				else
					msg.setName("Message");
				
				MessageOccurrenceSpecification mosSrc = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
				MessageOccurrenceSpecification mosTrg= UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
				msg.setSendEvent(mosSrc);
				mosSrc.setMessage(msg);
				msg.setReceiveEvent(mosTrg);				
				mosTrg.setMessage(msg);
				
				ClusterImpl sourceCluster = (ClusterImpl)graph.getLifeline((Lifeline)source);
				Node srcBeforeFrag = NodeUtilities.flatten(sourceCluster).stream().
					filter(n -> n.getBounds() != null && srcAnchor.y < n.getBounds().y).findFirst().orElse(null);
				ClusterImpl targetCluster = (ClusterImpl)graph.getLifeline((Lifeline)target);
				Node trgBeforeFrag = NodeUtilities.flatten(targetCluster).stream().
						filter(n -> n.getBounds() != null && trgAnchor.y < n.getBounds().y).findFirst().orElse(null);
				
				NodeImpl srcNode = (NodeImpl)graph.addMessageOccurrenceSpecification((Lifeline)source, mosSrc, srcBeforeFrag);
				srcNode.setBounds(new Rectangle(srcAnchor,new Dimension(1,1)));
				
				NodeImpl trgNode = (NodeImpl)graph.addMessageOccurrenceSpecification((Lifeline)target, mosTrg, trgBeforeFrag);				
				trgNode.setBounds(new Rectangle(trgAnchor,new Dimension(1,1)));
				
				message = graph.connectMessageOcurrenceSpecification(mosSrc, mosTrg);
				return true;
			}

			Link message;
		});
		
	}

	public void nudgeMessage(Message msg, Point delta) {
		// TODO: @etxacam Need to handle Lost & Found messages, messages with gates and create message. 
		Link link = interactionGraph.getLinkFor(msg);

		Lifeline srcLifeline = ((MessageOccurrenceSpecification)msg.getSendEvent()).getCovered();
		Cluster srcLifelineNode = interactionGraph.getLifeline(srcLifeline);
		List<Node> srcLifelineNodes = srcLifelineNode.getAllNodes();  
		Node source = link.getSource();
		int srcIndex = srcLifelineNodes.indexOf(source);
		Node srcPrev = srcIndex > 0 ? srcLifelineNodes.get(srcIndex-1) : null;
		Node srcNext = srcIndex < srcLifelineNodes.size()-1 ? srcLifelineNodes.get(srcIndex +1) : null;
				
		Lifeline targetLifeline = ((MessageOccurrenceSpecification)msg.getSendEvent()).getCovered();
		Cluster trgLifelineNode = interactionGraph.getLifeline(targetLifeline);
		List<Node> trgLifelineNodes = trgLifelineNode.getAllNodes();  
		Node target = link.getTarget();
		int trgIndex = trgLifelineNodes.indexOf(target);
		Node trgPrev = trgIndex > 0 ? trgLifelineNodes.get(trgIndex-1) : null;
		Node trgNext = trgIndex < trgLifelineNodes.size()-1 ? trgLifelineNodes.get(trgIndex +1) : null;
		
		Rectangle validArea = getEmptyArea(null, Arrays.asList(srcPrev, trgPrev),null,null);
		Rectangle msgArea = link.getBounds();
		Rectangle newMsgArea = link.getBounds().getCopy().translate(delta);
		if (!validArea.contains(newMsgArea)) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}
		
		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}

			@Override
			public boolean apply(InteractionGraph graph) {
				List<Node> allNodes = interactionGraph.getAllNodes();		
				graph.getRows().stream().filter(d -> (d.getIndex() >= source.getRow().getIndex() 
						|| d.getIndex() >= target.getRow().getIndex()))
						.map(RowImpl.class::cast).forEach(d -> d.nudge(delta.y));
				graph.layout();
				return true;
			}
		});
	}

	public void moveMessage(Message msg, Point moveDelta) {
		Link link = interactionGraph.getLinkFor(msg);

		Lifeline srcLifeline = ((MessageOccurrenceSpecification)msg.getSendEvent()).getCovered();
		Cluster srcLifelineNode = interactionGraph.getLifeline(srcLifeline);
		List<Node> srcLifelineNodes = srcLifelineNode.getAllNodes();  
		Node source = link.getSource();
		int newSrcY = link.getSourceLocation().y + moveDelta.y;
		Node srcMoveBeforeNode = srcLifelineNodes.stream().filter(d -> d.getBounds().getCenter().y > newSrcY).findFirst().orElse(null);

		Lifeline trgLifeline = ((MessageOccurrenceSpecification)msg.getReceiveEvent()).getCovered();
		Cluster trgLifelineNode = interactionGraph.getLifeline(trgLifeline);
		List<Node> trgLifelineNodes = trgLifelineNode.getAllNodes();  
		Node target = link.getTarget();
		int newTrgY = link.getTargetLocation().y + moveDelta.y;
		Node trgMoveBeforeNode = trgLifelineNodes.stream().filter(d -> d.getBounds().getCenter().y > newTrgY).findFirst().orElse(null);
		
		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}

			@Override
			public boolean apply(InteractionGraph graph) {
				source.getBounds().y = newSrcY;
				target.getBounds().y = newTrgY;
				
				interactionGraph.moveMessageOccurrenceSpecification(srcLifeline, (MessageOccurrenceSpecification)source.getElement(), 
						srcLifeline, (InteractionFragment)srcMoveBeforeNode.getElement());				
				
				interactionGraph.moveMessageOccurrenceSpecification(trgLifeline, (MessageOccurrenceSpecification)target.getElement(), 
						trgLifeline, (InteractionFragment)trgMoveBeforeNode.getElement());
				
				// TODO @etxacam Provide a real getOrderedNodes() or something like that....
				
				List<Node> allNodes = ((InteractionGraphImpl)interactionGraph).getLayoutNodes();
				int index = allNodes.indexOf(source);
				allNodes = allNodes.subList(index+1,allNodes.size());
				Element inserBefore = allNodes.stream().filter(d -> ((NodeImpl)d).getConnectedByLink() != null 
						&& ((NodeImpl)d).getConnectedByLink() != link).map(d -> ((NodeImpl)d).getConnectedByLink().getElement()).
						findFirst().orElse(null);				
				interactionGraph.moveMessage(msg, (Message)inserBefore);
				return true;
			}
		});
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

		for (InteractionGraphEditAction action : actions) {
			action.handleResult(cmd.getCommandResult());
		}

		return cmd.getCommandResult();
	}

	protected ICommand buildDelegateCommands(TransactionalEditingDomain editingDomain, String label) {
		ICompositeCommand command = new CompositeTransactionalCommand(editingDomain, label);
		calculateLifelinesEditingCommand(editingDomain, command);
		calculateMessagesChanges(editingDomain, command);
		calculateFragmentsChanges(editingDomain, command);

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
				removeCommandFunction, true);


		// Update lifelines occurrences covered. 
		for (Cluster c : interactionGraph.getLifelineClusters()) {
			Lifeline lf = (Lifeline) c.getElement();
			List<InteractionFragment> fragments = new ArrayList<>();
			for (Node n : NodeUtilities.flatten((ClusterImpl)c)) {
				Element el = n.getElement();
				if (el instanceof OccurrenceSpecification || el instanceof ExecutionSpecification) {
					fragments.add((InteractionFragment)el);
				}
			}
			createCommandsForCollectionChanges(editingDomain, command, lf,
					UMLPackage.Literals.LIFELINE__COVERED_BY, lf.getCoveredBys(), fragments, false);
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
		cmd.add(new DeleteCommand(editingDomain, interactionGraph.getLifeline(lifeline).getView()));
		cmd.add(new DestroyElementCommand(new DestroyElementRequest(editingDomain, lifeline, false)));
		return cmd;
	}

	private void calculateFragmentsChanges(TransactionalEditingDomain editingDomain, ICompositeCommand command) {
		List<InteractionFragment> fragments = interactionGraph.getInteraction().getFragments();
		List<InteractionFragment> graphFragments = interactionGraph.getRows().stream().flatMap(d -> d.getNodes().stream())
				.map(Node::getElement).filter(InteractionFragment.class::isInstance)
				.map(InteractionFragment.class::cast).collect(Collectors.toList());
		
		Function<InteractionFragment, IUndoableOperation> addCommandFunction = (d) -> new EMFCommandOperation(editingDomain,
				AddCommand.create(editingDomain, interactionGraph.getInteraction(), UMLPackage.Literals.INTERACTION__FRAGMENT, d));
		Function<InteractionFragment, IUndoableOperation> removeCommandFunction = (d) -> new EMFCommandOperation(editingDomain,
				RemoveCommand.create(editingDomain, interactionGraph.getInteraction(), UMLPackage.Literals.INTERACTION__FRAGMENT, d));

		createCommandsForCollectionChanges(command, interactionGraph.getInteraction(),
				UMLPackage.Literals.INTERACTION__FRAGMENT, fragments, graphFragments, addCommandFunction,
				removeCommandFunction, false);
	}

	private void calculateMessagesChanges(TransactionalEditingDomain editingDomain, ICompositeCommand command) {
		List<Message> messages = interactionGraph.getInteraction().getMessages();
		List<Message> graphMessages = interactionGraph.getMessageLinks().stream().map(Link::getElement)
				.map(Message.class::cast).collect(Collectors.toList());
		
		Function<Message, IUndoableOperation> addCommandFunction = (d) -> createMessageEditingCommand(editingDomain, d);
		Function<Message, IUndoableOperation> removeCommandFunction = (d) -> deleteMessageEditingCommand(editingDomain, d);

		createCommandsForCollectionChanges(command, interactionGraph.getInteraction(),
				UMLPackage.Literals.INTERACTION__MESSAGE, messages, graphMessages, addCommandFunction,
				removeCommandFunction, false);		
		
		EditPartViewer viewer = interactionGraph.getEditPartViewer();
		for (Link lk : interactionGraph.getMessageLinks()) {
			Message msg = (Message)lk.getElement();
			if (msg.getSendEvent() != lk.getSource().getElement()) {
				command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
						editingDomain, msg, UMLPackage.Literals.MESSAGE__SEND_EVENT, lk.getSource().getElement())));
			}

			if (msg.getReceiveEvent() != lk.getTarget().getElement()) {
				command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
						editingDomain, msg, UMLPackage.Literals.MESSAGE__RECEIVE_EVENT, lk.getTarget().getElement())));
			}
			Edge edge = lk.getEdge();
			if (edge != null) {
				if (lk.getSource() != null) {
					Point p = lk.getSource().getBounds().getTopLeft();
					View endView = lk.getSource().getParent().getView();
					if (edge.getSource() != endView) {
						command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
							editingDomain, lk.getEdge(), NotationPackage.Literals.EDGE__SOURCE, endView)));
					}
					
					String anchorId = ViewUtilities.formatAnchorId(viewer, lk.getSource().getParent().getView(), p);
					IdentityAnchor anchor = NotationFactory.eINSTANCE.createIdentityAnchor();
					anchor.setId(anchorId);
					command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
							editingDomain, lk.getEdge(), NotationPackage.Literals.EDGE__SOURCE_ANCHOR,anchor)));					
				} else {
					command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
							editingDomain, lk.getEdge(), NotationPackage.Literals.EDGE__SOURCE, null)));					
					command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
							editingDomain, lk.getEdge(), NotationPackage.Literals.EDGE__SOURCE_ANCHOR, null)));					
				}
			
				if (lk.getTarget() != null) {
					Point p = lk.getTarget().getBounds().getTopLeft();
					View endView = lk.getSource().getParent().getView();
					if (edge.getSource() != endView) {
						command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
							editingDomain, lk.getEdge(), NotationPackage.Literals.EDGE__TARGET, endView)));
					}
					
					String anchorId = ViewUtilities.formatAnchorId(viewer, lk.getTarget().getParent().getView(), p);
					IdentityAnchor anchor = NotationFactory.eINSTANCE.createIdentityAnchor();
					anchor.setId(anchorId);
					command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
							editingDomain, lk.getEdge(), NotationPackage.Literals.EDGE__TARGET_ANCHOR,anchor)));					
				} else {
					command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
							editingDomain, lk.getEdge(), NotationPackage.Literals.EDGE__TARGET, null)));					
					command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
							editingDomain, lk.getEdge(), NotationPackage.Literals.EDGE__TARGET_ANCHOR, null)));					
				}
			}
		}
	}
	
	private ICommand createMessageEditingCommand(TransactionalEditingDomain editingDomain, Message message) {
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
		
		String visualId = ((IHintedType)type).getSemanticHint();
		
		final CreateElementRequest createReq = new CreateElementRequest(editingDomain,
				interactionGraph.getInteraction(), type);
		cmd.add(new CreateNodeElementCommand(createReq, message));
		cmd.add(new CreateEdgeViewCommand(editingDomain, interactionGraph.getLinkFor(message),
				new ViewDescriptor(
						new SemanticElementAdapter(message,
								UMLElementTypes.getElementType(visualId)),
						org.eclipse.gmf.runtime.notation.Edge.class, visualId,
						((GraphicalEditPart) interactionGraph.getEditPart()).getDiagramPreferencesHint()),
					interactionGraph.getDiagram(), 
					link.getSource().getParent(), link.getSourceLocation(), link.getTarget().getParent(), link.getTargetLocation()));
		return cmd;
	}

	private ICommand deleteMessageEditingCommand(TransactionalEditingDomain editingDomain, Message message) {
		ICompositeCommand cmd = new CompositeTransactionalCommand(editingDomain, "Delete Message");
		cmd.add(new DeleteCommand(editingDomain, interactionGraph.getMessage(message).getView()));
		cmd.add(new DestroyElementCommand(new DestroyElementRequest(editingDomain, message, false)));
		// TODO: @etxacam Check when to destroy mos
		return cmd;
	}

	private <T extends EObject> void createCommandsForCollectionChanges(TransactionalEditingDomain editingDomain, 
			ICompositeCommand cmd, EObject container, EStructuralFeature feature, List<T> oldValues, List<T> newValues, 
			boolean updateBounds) {
		Function<T, IUndoableOperation> addCommandFunction = (d) -> new EMFCommandOperation(editingDomain, 
				AddCommand.create(editingDomain, container, feature, d));
		Function<T, IUndoableOperation> removeCommandFunction = (d) -> new EMFCommandOperation(editingDomain, 
				RemoveCommand.create(editingDomain, container, feature, d));
		
		createCommandsForCollectionChanges(cmd, container, feature, oldValues, newValues, 
				addCommandFunction, removeCommandFunction, updateBounds);
	}
	
	private <T extends EObject> void createCommandsForCollectionChanges(ICompositeCommand cmd, EObject container,
			EStructuralFeature feature, List<T> oldList, List<T> newList, Function<T, IUndoableOperation> addCommand,
			Function<T, IUndoableOperation> removeCommand, boolean updateBounds) {
		// We copy so the execution / undo do not interference with index calculation
		final List<T> oldValues = new ArrayList<>(oldList);
		final List<T> newValues = new ArrayList<>(newList);
		List<T> objToRemove = oldValues.stream().filter(d -> !newValues.contains(d)).collect(Collectors.toList());
		List<T> objToAdd = newValues.stream().filter(d -> !oldValues.contains(d)).collect(Collectors.toList());

		for (T obj : objToRemove)
			cmd.add(removeCommand.apply(obj));

		for (T obj : objToAdd)
			cmd.add(addCommand.apply(obj));

		// ReorderCommands		'
		int index = 0;
		for (T obj : newValues) {
			cmd.add(new TransactionalCommandProxy(getEditingDomain(),
					new MoveCommand(getEditingDomain(), container, feature, obj, index) {
						@Override
						public boolean doCanExecute() {
							return true;
						}
					}, "Rearrange collection", Collections.EMPTY_LIST));
			if (updateBounds) {
				NodeImpl node = interactionGraph.getNodeFor((Element) obj);
				cmd.add(new SetNodeViewBoundsCommand(getEditingDomain(), node, node.getBounds(), "Set location",
						Collections.emptyList()));
			}
			index++;
		}
	}

	public Rectangle getEmptyArea(List<Node> leftNodes, List<Node> topNodes, List<Node> rightNodes, List<Node> bottomNodes) {
		View interactionView = interactionGraph.getInteractionView();
		Rectangle rect =  ViewUtilities.getClientAreaBounds(interactionGraph.getEditPartViewer(), interactionView);
		int left=rect.x, right=rect.x+rect.width, top = rect.y, bottom = rect.y+rect.height;
		
		if (leftNodes != null) {
			for (Node n : leftNodes) {
				if (n == null)
					continue;
				Rectangle r = n.getBounds();
				left = Math.max(left, r.x+r.width);
			}
		}
		
		if (rightNodes != null) {
			for (Node n : rightNodes) {
				if (n == null)
					continue;
				Rectangle r = n.getBounds();
				right = Math.min(right, r.x);
			}
		}
		
		if (topNodes != null) {
			for (Node n : topNodes) {
				if (n == null)
					continue;
				Rectangle r = n.getBounds();
				top = Math.max(top, r.y+r.height);
			}
		}
		
		if (bottomNodes != null) {
			for (Node n : bottomNodes) {
				if (n == null)
					continue;
				Rectangle r = n.getBounds();
				bottom= Math.min(bottom, r.y);
			}
		}
		
		return new Rectangle(left,top, Math.max(0,right-left), Math.max(0,bottom-top));
	}
	

	/*
	private void calculateFragmentsChanges(List<InteractionGraphDiff> diffs) {
		List<InteractionFragment> fragments = interactionGraph.getInteraction().getFragments();
		List<InteractionFragment> newFragments = interactionGraph.getRows().stream().flatMap(d -> d.getNodes().stream())
				.map(Node::getElement).filter(InteractionFragment.class::isInstance)
				.map(InteractionFragment.class::cast).collect(Collectors.toList());

		calculateChangesContainmentCollection(diffs, interactionGraph.getInteraction(),
				UMLPackage.Literals.INTERACTION__FRAGMENT, fragments, newFragments);
	}

	private void calculateMessagesChanges(List<InteractionGraphDiff> diffs) {
		List<Message> messages = interactionGraph.getInteraction().getMessages();
		List<Message> newMessages = interactionGraph.getRows().stream().flatMap(d -> d.getNodes().stream())
				.map(Node::getElement).filter(MessageOccurrenceSpecification.class::isInstance)
				.map(d -> ((MessageOccurrenceSpecification) d).getMessage()).distinct().collect(Collectors.toList());

		calculateChangesContainmentCollection(diffs, interactionGraph.getInteraction(),
				UMLPackage.Literals.INTERACTION__MESSAGE, messages, newMessages);
	}

	private <T extends EObject> void calculateChangesContainmentCollection(List<InteractionGraphDiff> diffs,
			EObject owner, EStructuralFeature feature, List<T> oldVal, List<T> newVal) {
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

	private <T extends EObject> boolean calculateChangesForCollection(List<InteractionGraphDiff> diffs, EObject owner,
			EStructuralFeature feature, List<T> oldVal, List<T> newVal) {
		int gcount = oldVal.size();
		int icount = newVal.size();
		if (gcount != icount || !oldVal.equals(newVal)) {
			diffs.add(InteractionGraphDiffImpl.change(owner, feature, oldVal, newVal));
			return false;
		}
		return true;
	}
*/
	private InteractionGraphImpl interactionGraph;
	private List<InteractionGraphEditAction> actions = new ArrayList<>();
}
