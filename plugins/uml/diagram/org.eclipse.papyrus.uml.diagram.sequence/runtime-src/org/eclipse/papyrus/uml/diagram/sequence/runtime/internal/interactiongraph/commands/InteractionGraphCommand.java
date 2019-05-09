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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.validation.internal.service.GetBatchConstraintsOperation;
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
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Column;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Link;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.MarkNode;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.MarkNode.Kind;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Row;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ClusterImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ColumnImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.Draw2dUtils;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.FragmentClusterImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.InteractionGraphImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.MarkNodeImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.NodeImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.NodeOrderResolver;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.NodeUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.uml2.uml.ActionExecutionSpecification;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperand;
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
				Lifeline lifeline = SemanticElementsService.createElement(
						getEditingDomain(), interactionGraph.getInteraction(), 
						UMLElementTypes.Lifeline_Shape);
				lifeline.setName(NodeUtilities.getNewElementName(graph, lifeline));
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

		Rectangle clientAreaBounds = ViewUtilities.getBounds(interactionGraph.getEditPartViewer(),
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

	public void deleteLifeline(Lifeline lifeline) {
		Cluster lifelineNode = (Cluster)interactionGraph.getNodeFor(lifeline);
		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}

			@Override
			public boolean apply(InteractionGraph graph) {
				List<List<Node>> blocksToDelete = NodeUtilities.getBlocks(lifelineNode.getNodes());
				List<Node> flatBlocksToDelete = blocksToDelete.stream().flatMap(d->d.stream()).collect(Collectors.toList()); 
				List<Link> linksToDelete = new ArrayList<Link>(NodeUtilities.flattenKeepClusters(flatBlocksToDelete).stream().map(Node::getConnectedByLink).
						filter(Predicate.isEqual(null).negate()).collect(Collectors.toSet()));
				try {
					// Remove nodes & Messages
					interactionGraph.disableLayout();
					NodeUtilities.removeMessageLinks(graph, linksToDelete);
				} finally {
					interactionGraph.enableLayout();
				}
				
				try {
					((InteractionGraphImpl)graph).removeNodeBlocks(blocksToDelete);			
					Column col = lifelineNode.getColumn();
					Column prev = col.getIndex() == 0 ? null : interactionGraph.getColumns().get(col.getIndex()-1);
					int prevSize = prev == null ? 0 : (lifelineNode.getBounds().x  - NodeUtilities.getArea(prev.getNodes()).right());
					int horzNudge = lifelineNode.getBounds().width + prevSize;
					interactionGraph.disableLayout();
					NodeUtilities.removeNodes(graph, Collections.singletonList(lifelineNode));
					graph.getColumns().stream().filter(d->d.getIndex() > col.getIndex()).forEach(d -> ((ColumnImpl)d).nudge(-horzNudge));
				} finally {
					interactionGraph.enableLayout();
					interactionGraph.layout();					
				}
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

	// TODO: @etxacam Reply messages
	// TODO: @etxacam Self messages
	
	public void addMessage(MessageSort msgSort, CreateElementRequestAdapter elementAdapter, ViewDescriptor descriptor, 
			Element source, Point srcAnchor, Element target, Point trgAnchor) {

		if (ViewUtilities.isSnapToGrid(interactionGraph.getEditPartViewer(), interactionGraph.getDiagram())) {
			ViewUtilities.snapToGrid(interactionGraph.getEditPartViewer(), interactionGraph.getDiagram(), srcAnchor);
			ViewUtilities.snapToGrid(interactionGraph.getEditPartViewer(), interactionGraph.getDiagram(), trgAnchor);
		}
		
		if (trgAnchor.y < srcAnchor.y) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;			
		}
		
		Cluster	targetCluster = interactionGraph.getClusterFor(target);		
		if (msgSort == MessageSort.CREATE_MESSAGE_LITERAL) {
			if (!(target instanceof Lifeline) || NodeUtilities.isNodeLifelineStartByCreateMessage(targetCluster)) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;
			}	
			
			List<Node> nodes = targetCluster.getNodes();
			if (!nodes.isEmpty() && nodes.get(0).getBounds().y <= trgAnchor.y) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;
			}
							
		} else if (msgSort == MessageSort.DELETE_MESSAGE_LITERAL) {
			if (!(target instanceof Lifeline) || NodeUtilities.isNodeLifelineEndsWithDestroyOcurrenceSpecification(targetCluster)) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;
			}			
			List<Node> nodes = targetCluster.getNodes();
			if (!nodes.isEmpty() && nodes.get(nodes.size()-1).getBounds().y >= trgAnchor.y) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;
			}
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
				interactionGraph.disableLayout();
				try {

					Message msg = SemanticElementsService.createRelationship(getEditingDomain(), interactionGraph.getInteraction(), source, target, 
							(IElementType)elementAdapter.getAdapter(IElementType.class)); 
					msg.setName(NodeUtilities.getNewElementName(graph, msg));
					
					MessageEnd msgEndSrc = msg.getSendEvent();
					if (msgEndSrc instanceof MessageOccurrenceSpecification) {
						MessageOccurrenceSpecification mosSrc = (MessageOccurrenceSpecification)msgEndSrc;
						mosSrc.setName("Send "+msg.getName());						
					} 

					MessageEnd msgEndTrg = msg.getReceiveEvent();
					if (msgEndTrg instanceof MessageOccurrenceSpecification) {
						MessageOccurrenceSpecification mosTrg = (MessageOccurrenceSpecification)msgEndTrg;
						mosTrg.setName("Receive "+msg.getName());						
					} 

					Row r = NodeUtilities.getRowAt(graph, srcAnchor.y);
					if (r != null) {
						NodeUtilities.nudgeRows(graph.getRows().subList(r.getIndex(),graph.getRows().size()), 20);
					}

					r = NodeUtilities.getRowAt(graph, trgAnchor.y);
					if (r != null) {
						NodeUtilities.nudgeRows(graph.getRows().subList(r.getIndex(),graph.getRows().size()), 20);
					}

					ClusterImpl sourceCluster = (ClusterImpl)getMessageEndOwnerCluster(msgEndSrc, source);
					Node srcBeforeFrag = NodeUtilities.flatten(sourceCluster).stream().
						filter(n -> n.getBounds() != null && srcAnchor.y < n.getBounds().y).findFirst().orElse(null);

					ClusterImpl targetCluster = (ClusterImpl)getMessageEndOwnerCluster(msgEndTrg, target);
					Node trgBeforeFrag = NodeUtilities.flatten(targetCluster).stream().
							filter(n -> n.getBounds() != null && trgAnchor.y < n.getBounds().y).findFirst().orElse(null);
					
					NodeImpl srcNode = null;
					if (msgEndSrc instanceof Gate) {					
						if (source instanceof Interaction) {
							srcNode = interactionGraph.addGate((Interaction)source, (Gate)msgEndSrc, srcBeforeFrag);
						} else if (source instanceof InteractionUse) {
							srcNode = interactionGraph.addGate((InteractionUse)source, (Gate)msgEndSrc, srcBeforeFrag);
						}
					} else {
						Lifeline srcLifeline = (Lifeline)NodeUtilities.getLifelineNode(sourceCluster).getElement();
						srcNode = (NodeImpl)graph.addMessageOccurrenceSpecification(srcLifeline, (MessageOccurrenceSpecification)msgEndSrc, srcBeforeFrag);
					}
					srcNode.setBounds(new Rectangle(srcAnchor,new Dimension(0, 0)));						
					
					NodeImpl trgNode = null;
					if (msgEndTrg instanceof Gate) {					
						if (target instanceof Interaction) {
							trgNode = interactionGraph.addGate((Interaction)target, (Gate)msgEndTrg, trgBeforeFrag);
						} else if (target instanceof InteractionUse) {
							trgNode = interactionGraph.addGate((InteractionUse)target, (Gate)msgEndTrg, trgBeforeFrag);							
						}
					} else {
						Lifeline trgLifeline = (Lifeline)NodeUtilities.getLifelineNode(targetCluster).getElement();
						trgNode = (NodeImpl)graph.addMessageOccurrenceSpecification(trgLifeline, (MessageOccurrenceSpecification)msgEndTrg, trgBeforeFrag);				
					}
					trgNode.setBounds(new Rectangle(trgAnchor,new Dimension(0, 0)));
					
					message = graph.connectMessageEnds(msgEndSrc, msgEndTrg);
					interactionGraph.enableLayout();
					interactionGraph.layout();
					
					// TODO: @etxacam What about when there are gates involved??? Should create Reply message and / or reply gate??
					if (msg.getMessageSort() == MessageSort.SYNCH_CALL_LITERAL 
							&& msgEndSrc instanceof MessageOccurrenceSpecification 
							&& msgEndTrg instanceof MessageOccurrenceSpecification) {
						
						int nudgeFrom = trgNode.getBounds().y;
						final Node _srcNode = srcNode;
						final Node _trgNode = trgNode;
						List<Node> nodesAfter = interactionGraph.getLayoutNodes().stream().filter(
								d-> d != _srcNode && d != _trgNode && d.getBounds().y >= nudgeFrom).collect(Collectors.toList());
						boolean isSelf = NodeUtilities.isSelfLink(message);
						interactionGraph.disableLayout();
						try {
							NodeUtilities.nudgeNodes(nodesAfter, 0, !isSelf ? 40 : 60);
						} finally {
							interactionGraph.enableLayout();
							interactionGraph.layout();
						}
						
						interactionGraph.disableLayout();

						// Create reply message
						Message replyMsg = SemanticElementsService.createRelationship(getEditingDomain(), interactionGraph.getInteraction(), 
								source, target, UMLElementTypes.Message_ReplyEdge);
						replyMsg.setName("Reply " + msg.getName());
	
						MessageOccurrenceSpecification repMosSrc = (MessageOccurrenceSpecification)replyMsg.getSendEvent();
						repMosSrc.setName("Send "+replyMsg.getName());
						MessageOccurrenceSpecification repMosTrg= (MessageOccurrenceSpecification)replyMsg.getReceiveEvent();
						repMosTrg.setName("Receive "+replyMsg.getName());
						
						Lifeline trgLifeline = (Lifeline)NodeUtilities.getLifelineNode(targetCluster).getElement();
						NodeImpl repSrcNode = (NodeImpl)graph.addMessageOccurrenceSpecification(trgLifeline, repMosSrc, trgBeforeFrag);
						Point repSrcAnchor = new Point(trgAnchor.x, trgAnchor.y + 40);
						repSrcNode.setBounds(new Rectangle(repSrcAnchor,new Dimension(0, 0)));
						
						Lifeline srcLifeline = (Lifeline)NodeUtilities.getLifelineNode(sourceCluster).getElement();
						NodeImpl repTrgNode = (NodeImpl)graph.addMessageOccurrenceSpecification(srcLifeline, repMosTrg, srcBeforeFrag);				
						Point repTrgAnchor = new Point(srcAnchor.x, repSrcAnchor.y + (isSelf ? 20 : 0));
						repTrgNode.setBounds(new Rectangle(repTrgAnchor,new Dimension(0, 0)));
	
						message = graph.connectMessageEnds(repMosSrc, repMosTrg);
						
						IElementType execElemType = descriptor.getElementAdapter().getAdapter(IElementType.class);
						if (execElemType.getId().equals("org.eclipse.papyrus.uml.diagram.sequence.umldi.Message_SynchActionEdge"))
							execElemType = UMLElementTypes.ActionExecutionSpecification_Shape;
						else
							execElemType = UMLElementTypes.BehaviorExecutionSpecification_Shape;
						
						ExecutionSpecification execSpec = SemanticElementsService.createElement(
								getEditingDomain(), interactionGraph.getInteraction(), execElemType);
						execSpec.setName(NodeUtilities.getNewElementName(graph, UMLPackage.Literals.EXECUTION_SPECIFICATION));
						execSpec.setStart((MessageOccurrenceSpecification)msgEndTrg);
						execSpec.setFinish(repMosSrc);
						ClusterImpl execSpecCluster = (ClusterImpl)graph.addExecutionSpecification(trgLifeline, execSpec);
						execSpecCluster.getBounds();
						((InteractionGraphImpl)graph).enableLayout();
						graph.layout();						
					}
					return true;
				} finally {
					((InteractionGraphImpl)graph).enableLayout();					
				}
			}

			Link message;
		});
		
	}
	
	private Cluster getMessageEndOwnerCluster(MessageEnd msgEnd, Element owner) {
		if (msgEnd instanceof Gate) {
			if (owner instanceof Interaction) {
				return interactionGraph;
			} else if (owner instanceof InteractionUse) {
				return interactionGraph.getClusterFor(owner);
			}
		} 
		return interactionGraph.getClusterFor(owner);
	}

	public void deleteMessage(Message msg) {
		// TODO: @etxacam Need to handle Lost & Found messages, messages with gates and create message.
		
		if (msg.getMessageSort() == MessageSort.REPLY_LITERAL) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}			
		
		Link link = interactionGraph.getLinkFor(msg);		
		Node source = link.getSource();
		
		List<Node> nodes = NodeUtilities.getBlock(source);
		List<Link> linksToDelete = new ArrayList<Link>(NodeUtilities.flattenKeepClusters(nodes).stream().map(Node::getConnectedByLink).
				filter(Predicate.isEqual(null).negate()).collect(Collectors.toSet()));

		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}
			
			@Override
			public boolean apply(InteractionGraph graph) {
				((InteractionGraphImpl)graph).removeNodeBlock(nodes);
				NodeUtilities.removeMessageLinks(graph, linksToDelete);
				return true;
			}
		});
	}

	public void nudgeMessage(Message msg, Point delta) {
		// TODO: @etxacam Need to handle Lost & Found messages, messages with gates and create message. 
		Link link = interactionGraph.getLinkFor(msg);		
		Node source = link.getSource();
		Node target = link.getTarget();

		Rectangle validArea = NodeUtilities.getNudgeArea(interactionGraph, Arrays.asList(source,target), false, true);		
		Rectangle newMsgArea = link.getBounds().getCopy().translate(delta);
		if (!Draw2dUtils.contains(validArea,newMsgArea)) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}
		
		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}

			@Override
			public boolean apply(InteractionGraph graph) {
				List<Row> nudgeRows = graph.getRows().stream().filter(
						d -> (d.getIndex() >= source.getRow().getIndex() || 
						d.getIndex() >= target.getRow().getIndex())).
						collect(Collectors.toList());
				NodeUtilities.nudgeRows(nudgeRows, delta.y);
				graph.layout();
				return true;
			}
		});
	}

	/*
	 * Notes on MessageEnd (source) nudge:
	 * - The source part does not nudge normally, 2 possibilities:
	 * 		a) It does not nudge at all => Verify against the Empty area around it (Implemented)
	 * 		b) Nudge opposite => delta < 0 -> Nudge everything down and / or delta > 0 -> Nudge allt under up 
	 */
	public void nudgeMessageEnd(MessageEnd msgEnd, Point location) {
		if (msgEnd.getMessage() == null) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}

		boolean isRecvEvent = msgEnd.getMessage().getReceiveEvent() == msgEnd; 
		Link link = interactionGraph.getLinkFor(msgEnd.getMessage());
		Node msgEndNode = interactionGraph.getNodeFor(msgEnd);		
		
		Node source = link.getSource();
		Node target = link.getTarget();		
		Rectangle validArea = null;
		if (msgEndNode == source) {
			validArea = NodeUtilities.getEmptyAreaAround(interactionGraph, 
					NodeUtilities.areNodesHorizontallyConnected(source, target) ?  
							Arrays.asList(source,target) :
							Collections.singletonList(msgEndNode));			
			Insets ins = new Insets(Draw2dUtils.SHRINK_SIZE);
			if (Draw2dUtils.contains(validArea,target.getBounds())) {
				ins.bottom = 0;
			}
			validArea.shrink(ins);
		} else {		
			validArea = NodeUtilities.getNudgeArea(interactionGraph, 
				NodeUtilities.areNodesHorizontallyConnected(source, target) ?  
						Arrays.asList(source,target) :
						Collections.singletonList(msgEndNode), false, true);
			if (Draw2dUtils.contains(Draw2dUtils.outsideRectangle(validArea.getCopy()),source.getLocation())) {
				validArea.y -= Draw2dUtils.SHRINK_SIZE; validArea.height += Draw2dUtils.SHRINK_SIZE; 
			}
		}

		Rectangle newMsgEndPos = msgEndNode.getBounds().getCopy().setLocation(location);
		Dimension delta = newMsgEndPos.getLocation().getDifference(msgEndNode.getBounds().getLocation());
		if (NodeUtilities.isBorderNode(msgEndNode))
			validArea.expand(10, 0);
		
		if (!Draw2dUtils.contains(validArea,newMsgEndPos)) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}

		int selfMsgSpace = 0;
		if (NodeUtilities.isSelfLink(link))
			selfMsgSpace = 20;

		if (isRecvEvent) {
			if (newMsgEndPos.y < (link.getSource().getBounds().y + selfMsgSpace)) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;
			}				
		} else {
			if (newMsgEndPos.y > (link.getTarget().getBounds().y - selfMsgSpace)) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;
			}				
			
		}

		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}

			@Override
			public boolean apply(InteractionGraph graph) {
				boolean isGate = NodeUtilities.isBorderNode(msgEndNode);
				if (isRecvEvent) {
					List<Row> nudgeRows = graph.getRows().stream().filter(d -> (d.getIndex() > msgEndNode.getRow().getIndex()))
							.collect(Collectors.toList());
					NodeUtilities.nudgeRows(nudgeRows, delta.height);
					
					List<Node> ownRowNodes = msgEndNode.getRow().getNodes().stream().filter(d-> (d!=source && d!=target)).
							collect(Collectors.toList());
					NodeUtilities.nudgeNodes(ownRowNodes, isGate ? delta.width : 0, delta.height);
					
					graph.layout();
				} else if (delta.height > 0){
					
				}
				
				Rectangle r = msgEndNode.getBounds();
				r.y = newMsgEndPos.y;
				if (isGate)
					r.x = newMsgEndPos.x;
				graph.layout();
				return true;
			}
		});
	}
	

	// TODO: @etxacam Check self messages
	public void moveMessage(Message msg, Point moveDelta) {
		Link link = interactionGraph.getLinkFor(msg);
		Node source = link.getSource();
		Node target = link.getTarget();
		
		int minY = Integer.MIN_VALUE;
		int maxY = Integer.MAX_VALUE;
		Cluster sourceLifelineCluster = NodeUtilities.getLifelineNode(source);
		Cluster targetLifelineCluster = NodeUtilities.getLifelineNode(source);
		if (sourceLifelineCluster != null) {
			Rectangle lifelineArea = ViewUtilities.getClientAreaBounds(interactionGraph.getEditPartViewer(), 
					sourceLifelineCluster.getView());
			minY = lifelineArea.y;
			if (targetLifelineCluster != null && msg.getMessageSort() != MessageSort.CREATE_MESSAGE_LITERAL) {
				minY = Math.max(minY, ViewUtilities.getClientAreaBounds(interactionGraph.getEditPartViewer(), 
						targetLifelineCluster.getView()).y);
			}

			if (NodeUtilities.isNodeLifelineEndsWithDestroyOcurrenceSpecification(source)) 
				maxY = lifelineArea.y + lifelineArea.height - 1;
		} else {
			Rectangle gateOwnerBounds = source.getParent().getBounds();
			minY = gateOwnerBounds.y;
			maxY = gateOwnerBounds.bottom();
		}

		if (targetLifelineCluster != null) {
			if (NodeUtilities.isNodeLifelineEndsWithDestroyOcurrenceSpecification(target)) {
				if (msg.getMessageSort() != MessageSort.DELETE_MESSAGE_LITERAL)	{			
					Rectangle lifelineArea = ViewUtilities.getClientAreaBounds(interactionGraph.getEditPartViewer(), 
							targetLifelineCluster.getView());
					maxY = Math.min(maxY, lifelineArea.y + lifelineArea.height - 1);
				}
			}
		} else {
			Rectangle gateOwnerBounds = source.getParent().getBounds();
			minY = Math.max(gateOwnerBounds.y,minY);
			maxY = Math.max(gateOwnerBounds.bottom(), maxY);			
		}
		
		List<Node> nodes = NodeUtilities.getBlock(source);
		Rectangle totalArea = NodeUtilities.getArea(nodes);
		totalArea.translate(moveDelta);
		if (totalArea.y <= minY || totalArea.y >= maxY) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}
				
		if (msg.getMessageSort() == MessageSort.REPLY_LITERAL) {
			if (moveReplyMessage(msg, moveDelta))
				return;
		}
		

		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}
			
			@Override
			public boolean apply(InteractionGraph graph) {
				Point p = moveDelta;
				((InteractionGraphImpl)graph).moveNodeBlock(nodes, totalArea.y);
				return true;
			}
		});
	}
	
	private boolean moveReplyMessage(Message msg, Point moveDelta) {
		Link link = interactionGraph.getLinkFor(msg);
		Node source = link.getSource();		

		Cluster parentSrc = source.getParent();
		if (parentSrc == null || !(parentSrc.getElement() instanceof ExecutionSpecification))
			return false;
		
		int newY = source.getBounds().y + moveDelta.y;
		if (parentSrc.getBounds().y >= newY) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return true;
		}

		// Resize the exec Spec to include or remove nodes.		
		// Check new pos is inside the same parent's parent => Before a node inside parent's parent.
		Point newSrcPt = source.getBounds().getCenter().getCopy();
		newSrcPt.translate(moveDelta);
		
		Cluster srcParent = source.getParent();
		Cluster srcLifelineNode = NodeUtilities.getLifelineNode(srcParent);
		Cluster newSrcParent = NodeUtilities.getClusterAtVerticalPos(srcLifelineNode, newSrcPt.y);					
		if (newSrcParent != srcParent.getParent() && moveDelta.y > 0) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return true;
		}
		
		if (newSrcParent != srcParent && moveDelta.y <= 0) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return true;
		}
		
		Node target = link.getTarget();
		Point newTrgPt = target.getBounds().getCenter().getCopy();
		newTrgPt.translate(moveDelta);
		Cluster trgParent = target.getParent();
		Cluster trgLifelineNode = NodeUtilities.getLifelineNode(trgParent);
		Cluster newTrgParent = NodeUtilities.getClusterAtVerticalPos(trgLifelineNode, newTrgPt.y);
		if (newTrgParent != trgParent) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return true;
		}			
		
		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}
			
			@Override
			public boolean apply(InteractionGraph graph) {							
				try {
					// Check for padding overlapping
					
					interactionGraph.disableLayout();
					
					// Default nudge if required.							
					List<Node> nodesAfter= NodeUtilities.getNodesAfterVerticalPos(graph, newTrgPt.y-3);
					nodesAfter.remove(link.getSource());
					nodesAfter.remove(link.getTarget());
					if (!nodesAfter.isEmpty()) {
						Node nextNode = nodesAfter.get(0);
						if (nextNode.getBounds().y - newTrgPt.y < 3)
							NodeUtilities.nudgeNodes(nodesAfter, 0, 20);
					}
					
					// Move nodes [ Parent's next node...insert After node] into the parent.
					// TODO: @etxacam Nudge if link src overlapp and get overlapp in cluster
					if (moveDelta.y > 0) {
						expandCluster(srcParent,moveDelta.y);
					} else if (moveDelta.y < 0){
						shrinkCluster(srcParent, -moveDelta.y);
					}

					//Move target node
					Node insertBeforeTrgNode = NodeUtilities.getNextVerticalNode(trgParent,  newTrgPt.y);
					while (insertBeforeTrgNode != null && NodeUtilities.isStartNode(insertBeforeTrgNode)) {						
						insertBeforeTrgNode = insertBeforeTrgNode.getParent();
					}
					NodeUtilities.moveNodes(graph, Collections.singletonList(target), trgParent, insertBeforeTrgNode, newTrgPt.y);
											
				} finally {
					interactionGraph.enableLayout();
					interactionGraph.layout();
				}
				return true;
			}
		});				
		return true;
	}
	
	public void nudgeGate(Gate gate, Point location) {		
		nudgeMessageEnd(gate, location);
	}
	
	public void moveGate(Gate gate, InteractionFragment intFragment, Point location) {
		if (!CombinedFragment.class.isInstance(intFragment) && !InteractionOperand.class.isInstance(intFragment) &&
			!Interaction.class.isInstance(intFragment) && !InteractionUse.class.isInstance(intFragment)) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}
		
		Message msg = gate.getMessage();
		boolean isRecvEvent = msg.getReceiveEvent() == gate; 
		Link link = interactionGraph.getLinkFor(msg);
		Node msgEndNode = interactionGraph.getNodeFor(gate);
		Cluster toGateOwnerNode = interactionGraph.getClusterFor(intFragment);
		boolean isChangingOwner = msgEndNode.getParent() != toGateOwnerNode;
				
		Node target = link.getTarget();
		Node source = link.getSource();

		int selfMsgSpace = 0;
		// TODO: @etxacam Handle Self messages to and from clusyter fragments.
		if (NodeUtilities.isSelfLink(link) && !isChangingOwner)
			selfMsgSpace = 20;

		Point newLoc = location.getCopy();
		if (isRecvEvent) {
			if (newLoc.y < (source.getBounds().y + selfMsgSpace)) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;
			}				
		} else {
			if (newLoc.y > (target.getBounds().y - selfMsgSpace)) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;
			}							
		}

		if (isChangingOwner) {
			if (!(toGateOwnerNode instanceof FragmentCluster)) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;				
			}			
		}		
		
		Rectangle lifelineArea = toGateOwnerNode.getBounds();
		int minY = lifelineArea.y;
		int maxY = lifelineArea.y + lifelineArea.height - 1;
		
		List<Node> nodes = isRecvEvent ? NodeUtilities.getBlock(source) : Arrays.asList(source);
		if (isRecvEvent) {
			nodes.remove(source);
		} 
		
		Link l = NodeUtilities.getStartLink(link);
		if ( l != null && l != link) {
			minY = l.getTarget().getBounds().y;
		}
		l = NodeUtilities.getFinishLink(link);
		if ( l != null && l != link) {
			maxY = l.getSource().getBounds().y;
		}

		Rectangle totalArea = NodeUtilities.getArea(Arrays.asList(msgEndNode));
		totalArea.setLocation(newLoc);
		if (totalArea.y <= minY || totalArea.y >= maxY) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}
		
		moveMessageEndImpl(gate, toGateOwnerNode, location);
		
		Rectangle r = msgEndNode.getBounds();
		if (r.x != newLoc.x) {
			actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {				
				@Override
				public boolean apply(InteractionGraph graph) {
					msgEndNode.getBounds().x = newLoc.x;
					graph.layout();
					return true;
				}
			});
		}
	}

	public void nudgeExecutionSpecification(ExecutionSpecification execSpec, int delta) {
		Cluster execSpecNode = interactionGraph.getClusterFor(execSpec);
		Node occurrenceNode = execSpecNode.getNodes().get(0);
		Element occSpec = occurrenceNode.getElement();
		if (occSpec instanceof MessageEnd) {
			nudgeMessage(((MessageEnd) occSpec).getMessage(), new Point(0,delta));
		} else {
			throw new UnsupportedOperationException("Need to implement Nudge for ExecSpecOcurrence");
		}		
	}
	
	public void resizeExecutionSpecification(ExecutionSpecification execSpec, boolean topSide, int delta) {
		Cluster execSpecNode = interactionGraph.getClusterFor(execSpec);
		Node occurrenceNode = topSide ? execSpecNode.getNodes().get(0) : execSpecNode.getNodes().get(execSpecNode.getNodes().size()-1);
		Element occSpec = occurrenceNode.getElement();
		if (occSpec instanceof MessageEnd) {
			nudgeMessage(((MessageEnd) occSpec).getMessage(), new Point(0,delta));
		} else {
			throw new UnsupportedOperationException("Need to implement Nudge for ExecSpecOcurrence");
		}					

		if (topSide) {
			// Nudge all efter
			Node after = NodeUtilities.getNodesAfter(interactionGraph,Collections.singletonList(occurrenceNode)).
					stream().filter(d->d.getElement() != execSpec).findFirst().orElse(null);
			if (after == null || occurrenceNode.getBounds().y+delta >= after.getBounds().y) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;
			}
			
			actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
				@Override
				public void handleResult(CommandResult result) {
				}

				@Override
				public boolean apply(InteractionGraph graph) {
					List<Row> rows = graph.getRows().stream().filter(d -> (d.getIndex() > occurrenceNode.getRow().getIndex()))
							.collect(Collectors.toList());
					NodeUtilities.nudgeRows(rows, -delta);
					graph.layout();
					return true;
				}
			});
		}
	}

	public void moveExecutionSpecification(ExecutionSpecification execSpec, Lifeline lifeline, Point point) {
		Cluster execSpecNode = interactionGraph.getClusterFor(execSpec);
		Node occurrenceNode = execSpecNode.getNodes().get(0);
		Element occSpec = occurrenceNode.getElement();
		if (occSpec instanceof MessageEnd) {
			if (lifeline == NodeUtilities.getLifelineNode(execSpecNode).getElement()) {
				Dimension delta = point.getDifference(occurrenceNode.getBounds().getTopLeft());
				moveMessage(((MessageEnd) occSpec).getMessage(), new Point(0,delta.height));
			} else {
				moveMessageEnd((MessageEnd)occSpec, lifeline, point);
			}
		} else {
			throw new UnsupportedOperationException("Need to implement Nudge for ExecSpecOcurrence");
		}				
	}

	public void moveExecutionSpecificationOccurrence(ExecutionSpecification execSpec, OccurrenceSpecification occurrenceSpec, Point point) {
		if (execSpec.getStart() != occurrenceSpec && execSpec.getFinish() != occurrenceSpec) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;		
		}	
		
		// Check limit to move to the parent / lifeline bounds 
		Node occurSpecNode = interactionGraph.getNodeFor(occurrenceSpec);
		Cluster execSpecNode = interactionGraph.getClusterFor(execSpec);
		Node execSpecParentNode = execSpecNode.getParent();
		Rectangle parentLimits = execSpecParentNode.getBounds();
		if (point.y <= parentLimits.y || point.y >= parentLimits.y + parentLimits.height) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;		
		}			
		
		boolean startOccur = execSpec.getStart() == occurrenceSpec;
		OccurrenceSpecification other = startOccur ? execSpec.getFinish() : execSpec.getStart();
		Node otherNode = interactionGraph.getNodeFor(other);
		Rectangle otherBounds = otherNode.getBounds();
		if ((startOccur && point.y >= otherBounds.y) || (!startOccur && point.y <= otherBounds.y)) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;		
		}			
		// Start & End will have same parent
		Cluster lifelineNode = NodeUtilities.getLifelineNode(occurSpecNode);
		Cluster newParent = NodeUtilities.getClusterAtVerticalPos(lifelineNode, point.y);
		if (newParent != execSpecNode.getParent() && newParent != execSpecNode) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;		
		}

		
		Rectangle lifelineArea = ViewUtilities.getClientAreaBounds(interactionGraph.getEditPartViewer(), 
				NodeUtilities.getLifelineNode(lifelineNode).getView());
		if (!Draw2dUtils.contains(lifelineArea,point)) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;		
		}

		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}

			@Override
			public boolean apply(InteractionGraph graph) {
				NodeImpl node = (NodeImpl)occurSpecNode; 
				List<Node> nodesAfter= NodeUtilities.getNodesAfterVerticalPos(graph, point.y-3);
				if (!nodesAfter.isEmpty()) {
					Node nextNode = nodesAfter.get(0);
					if (nextNode.getBounds().y - point.y < 3)
						NodeUtilities.nudgeNodes(nodesAfter, 0, 20);
				}
				
				Rectangle r = node.getBounds();
				if (startOccur) {
					resizeCluster((ClusterImpl)execSpecNode, r.y - point.y, 0);
				} else {
					resizeCluster((ClusterImpl)execSpecNode, 0, point.y - (r.y + r.height));
				}
				return true;
			}
		});
		
		if (occurrenceSpec instanceof MessageEnd) {
			Node sendMessageNode = startOccur ? occurSpecNode.getParent().getConnectedByLink().getSource() : occurSpecNode.getConnectedByLink().getTarget();
			MessageEnd msgEnd = (MessageEnd)sendMessageNode.getElement();
			Lifeline lifeline = msgEnd instanceof MessageOccurrenceSpecification ? 
					((MessageOccurrenceSpecification)msgEnd).getCovered() : null;
			Point newPos = sendMessageNode.getLocation().getCopy();
			newPos.y = point.y;
			// TODO: @ etxacam Needs to nudge on overlap
			moveMessageEndImpl(msgEnd, NodeUtilities.getLifelineNode(sendMessageNode), newPos);
		}

	}

	public void deleteExecutionSpecification(ExecutionSpecification execSpec) {
		Cluster execSpecNode = interactionGraph.getClusterFor(execSpec);
		Node occurrenceNode = execSpecNode.getNodes().get(0);
		Element occSpec = occurrenceNode.getElement();
		if (occSpec instanceof MessageEnd) {
			deleteMessage(((MessageEnd) occSpec).getMessage());
		} else {
			throw new UnsupportedOperationException("Need to implement delete of ExecSpec when is not attached to a message");
		}			
	}
	
	public void addInteractionUse(CreateElementRequestAdapter elementAdapter, ViewDescriptor descriptor, Rectangle rect) {
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
				InteractionUse interactionUse = SemanticElementsService.createElement(
						getEditingDomain(), interactionGraph.getInteraction(), 
						UMLElementTypes.InteractionUse_Shape);
				
				int posY = rect.y;				
				List<Lifeline> lifelines = new ArrayList<>(); 
				int minX = Integer.MAX_VALUE;
				int maxX = Integer.MIN_VALUE;
				for (Cluster lifelineCluster : graph.getLifelineClusters()) {
					Rectangle r = lifelineCluster.getBounds();
					if (rect.intersects(r)) {
						minX = Math.min(minX, r.getLeft().x);
						maxX = Math.max(maxX, r.getRight().x);
						lifelines.add((Lifeline)lifelineCluster.getElement());
					}				
				}
				
				Rectangle newRect = rect.getCopy();
				newRect.x = minX;
				newRect.width = maxX - minX;
				newRect.height = 40;
				
				Node nodeInsertBefore = NodeUtilities.getNodeAfterVerticalPos(graph, posY-3);
				
				FragmentClusterImpl interactionUseCluster = interactionGraph.addInteractionUse(
						interactionUse, lifelines, nodeInsertBefore == null ? null : (InteractionFragment)nodeInsertBefore.getElement());
				interactionUseCluster.setBounds(newRect);
				for (Cluster subCluster : interactionUseCluster.getClusters()) {
					ClusterImpl c = (ClusterImpl)subCluster; 
					ClusterImpl parent = c.getParent();
					Rectangle parentRect = parent.getBounds();
					((NodeImpl)c.getNodes().get(0)).setBounds(new Rectangle(parentRect.x , newRect.y, 0, 0));;
					((NodeImpl)c.getNodes().get(1)).setBounds(new Rectangle(parentRect.x , newRect.y+newRect.height, 0, 0));
				}
				graph.layout();
				cluster = interactionUseCluster;
				return true;
			}

			Cluster cluster;
		});
	}

	public void nudgeInteractionUse(InteractionUse intUse, Point delta) {
		FragmentCluster fragmentCluster = (FragmentCluster)interactionGraph.getClusterFor(intUse);		
		List<Node> startNodes = fragmentCluster.getClusters().stream().map(d->d.getNodes().get(0)).collect(Collectors.toList());
		Rectangle validArea = NodeUtilities.getNudgeArea(interactionGraph, startNodes, false, true);		
		Rectangle newMsgArea = fragmentCluster.getBounds().getCopy().translate(delta);
		if (!Draw2dUtils.contains(validArea,newMsgArea)) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}
		
		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}

			@Override
			public boolean apply(InteractionGraph graph) {
				Row r = startNodes.get(0).getRow();
				List<Row> nudgeRows = graph.getRows().stream().filter(d -> (d.getIndex() >= r.getIndex()))
					.collect(Collectors.toList());
				NodeUtilities.nudgeRows(nudgeRows, delta.y);
				graph.layout();
				return true;
			}
		});
	}

	public void nudgeResizeInteractionUse(InteractionUse intUse, Rectangle rect) {
		FragmentClusterImpl fragmentCluster = (FragmentClusterImpl)interactionGraph.getClusterFor(intUse);
		Dimension minSize = interactionGraph.getLayoutManager().getMinimumSize(fragmentCluster);
		if (minSize.width > rect.width || minSize.height > rect.height) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return; 
		}

		List<Node> allNodes = NodeUtilities.flatten(fragmentCluster.getClusters()); 
		allNodes.addAll(fragmentCluster.getAllGates());
		Rectangle validArea = NodeUtilities.getEmptyAreaAround(interactionGraph, allNodes).shrink(2, 2);		
		if (rect.x < validArea.x || rect.right() > validArea.right()) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;			
		}

		Rectangle currentRect = fragmentCluster.getBounds();
		if (currentRect.y != rect.y) {
			Rectangle nudgeArea = NodeUtilities.getEmptyAreaAround(interactionGraph, fragmentCluster.getStartMarkNodes());
			// Check top borders
			if (nudgeArea.y >= rect.y || rect.y >= nudgeArea.bottom()) {  
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;
			}				
		}
		
		if (currentRect.bottom() != rect.bottom()) {
			Rectangle nudgeArea = NodeUtilities.getNudgeArea(interactionGraph, fragmentCluster.getEndMarkNodes(), false, true);
			// Check top borders
			if (nudgeArea.y >= rect.bottom() || rect.bottom() >= nudgeArea.bottom()) {  
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;
			}							
		}
		
		List<Cluster> lifelines = NodeUtilities.getIntersectingLifelineLines(interactionGraph,rect);
		if (lifelines.size() == 0) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;			
		}
		
		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}

			@Override
			public boolean apply(InteractionGraph graph) {
				List<Cluster> curLifelines = fragmentCluster.getClusters().stream().map(NodeUtilities::getLifelineNode).
						collect(Collectors.toList());
				
				int newBottom = rect.bottom();
				int bottom = fragmentCluster.getBounds().bottom(); 
				if ( bottom != newBottom) {
					// Nudge nodes if bottom position change.
					List<Node> nudgeNodes = NodeUtilities.getNodesAfterVerticalPos(graph, bottom);
					nudgeNodes.addAll(fragmentCluster.getEndMarkNodes());
					NodeUtilities.nudgeNodes(nudgeNodes, 0, newBottom - bottom);
				}
				
				
				// Remove old marks
				List<Cluster> clusterToDelete = fragmentCluster.getClusters().stream().
						filter(d->!lifelines.contains(NodeUtilities.getLifelineNode(d))).collect(Collectors.toList());
				NodeUtilities.removeNodes((InteractionGraphImpl)graph, clusterToDelete);
				clusterToDelete.forEach(fragmentCluster::removeCluster);
				lifelines.removeAll(curLifelines);
				int newPosY = rect.y; //fragmentCluster.getBounds().y;
				int posY = fragmentCluster.getBounds().y;				
				int height = rect.height;//fragmentCluster.getBounds().height;
				
				if (newPosY != posY) {
					List<MarkNode> nudgeNodes = fragmentCluster.getStartMarkNodes();
					NodeUtilities.nudgeNodes(nudgeNodes, 0, newPosY - posY);				
				}
				
				for (Cluster lifeline : lifelines) {
					Cluster parent = NodeUtilities.getClusterAtVerticalPos(lifeline, newPosY); 
					Rectangle parentRect = parent.getBounds(); 
					Node insertBefore = NodeUtilities.getNextVerticalNode(lifeline,newPosY);
					while (insertBefore != null && NodeUtilities.isStartNode(insertBefore)) {
						insertBefore = insertBefore.getParent();
					}
					ClusterImpl intUseLfCluster = new ClusterImpl(intUse);
					intUseLfCluster.addNode(new MarkNodeImpl(Kind.start, intUse));
					intUseLfCluster.addNode(new MarkNodeImpl(Kind.end, intUse));

					((NodeImpl)intUseLfCluster.getNodes().get(0)).setBounds(new Rectangle(parentRect.getCenter().x , newPosY, 0, 0));
					((NodeImpl)intUseLfCluster.getNodes().get(1)).setBounds(new Rectangle(parentRect.getCenter().x , newPosY + height, 0, 0));

					((ClusterImpl)parent).addNode(intUseLfCluster, insertBefore);
					((FragmentClusterImpl)fragmentCluster).addCluster(intUseLfCluster); // TODO: @etxacam reorder them...
				}
				
				graph.layout();
				return true;
			}
		});

	}
	
	public void resizeInteractionUse(InteractionUse intUse, Rectangle rect) {
		FragmentClusterImpl fragmentCluster = (FragmentClusterImpl)interactionGraph.getClusterFor(intUse);		

		Dimension minSize = interactionGraph.getLayoutManager().getMinimumSize(fragmentCluster);
		if (minSize.width > rect.width || minSize.height > rect.height) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return; 
		}

		List<Node> allNodes = NodeUtilities.flatten(fragmentCluster.getClusters()); 
		allNodes.addAll(fragmentCluster.getAllGates());

		List<Cluster> lifelines = NodeUtilities.getIntersectingLifelineLines(interactionGraph,rect);
		if (lifelines.size() == 0) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;			
		}

		for (Cluster lf : lifelines) {
			Node newStartParent = NodeUtilities.getClusterAtVerticalPos(lf, rect.y());
			if (fragmentCluster.getClusters().contains(newStartParent))
				newStartParent = newStartParent.getParent();
			Node newEndParent = NodeUtilities.getClusterAtVerticalPos(lf, rect.bottom());			
			if (fragmentCluster.getClusters().contains(newEndParent))
				newEndParent = newEndParent.getParent();
			
			if (newStartParent != newEndParent) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return; 
			}
		}
		
		List<Node> nodes = NodeUtilities.getNodesAfterVerticalPos(interactionGraph, rect.y-2);
		nodes.removeAll(allNodes);
		nodes.removeAll(NodeUtilities.getNodesAfterVerticalPos(interactionGraph, rect.bottom()));
		if (!nodes.isEmpty()) {
			Node overlappingNode = nodes.stream().filter(d->lifelines.contains(NodeUtilities.getLifelineNode(d))).findFirst().orElse(null);
			if (overlappingNode != null) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return; 
			}
		}		

		List<Node> gates = fragmentCluster.getAllGates();
		int maxY = gates.stream().map(d->d.getBounds().getCenter().y + 10).max(Integer::compareTo).orElse(minSize.height + fragmentCluster.getBounds().y);
		int minY = gates.stream().map(d->d.getBounds().getCenter().y - 10).min(Integer::compareTo).orElse(fragmentCluster.getBounds().y);
		if (rect.y > minY || rect.bottom() < maxY) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return; 
		}
				
		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}

			@Override
			public boolean apply(InteractionGraph graph) {
				List<Cluster> curLifelines = fragmentCluster.getClusters().stream().map(NodeUtilities::getLifelineNode).
						collect(Collectors.toList());
				
				int newBottom = rect.bottom();
				int bottom = fragmentCluster.getBounds().bottom(); 
				if ( bottom != newBottom) {
					// Resize botton border
					fragmentCluster.getClusters().forEach(d->resizeCluster((ClusterImpl)d, 0, newBottom-bottom));
				}
				
				int newTop = rect.y();
				int top = fragmentCluster.getBounds().y(); 
				if ( newTop != top) {
					// Resize top border
					fragmentCluster.getClusters().forEach(d->resizeCluster((ClusterImpl)d, top - newTop, 0));
				}
				
				// Remove old marks
				List<Cluster> clusterToDelete = fragmentCluster.getClusters().stream().
						filter(d->!lifelines.contains(NodeUtilities.getLifelineNode(d))).collect(Collectors.toList());
				NodeUtilities.removeNodes((InteractionGraphImpl)graph, clusterToDelete);
				clusterToDelete.forEach(fragmentCluster::removeCluster);
				lifelines.removeAll(curLifelines);
				int newPosY = rect.y; //fragmentCluster.getBounds().y;
				int posY = fragmentCluster.getBounds().y;				
				int height = rect.height;//fragmentCluster.getBounds().height;
				
				if (newPosY != posY) {
					List<MarkNode> nudgeNodes = fragmentCluster.getStartMarkNodes();
					NodeUtilities.nudgeNodes(nudgeNodes, 0, newPosY - posY);				
				}
				
				for (Cluster lifeline : lifelines) {
					Cluster parent = NodeUtilities.getClusterAtVerticalPos(lifeline, newPosY); 
					Rectangle parentRect = parent.getBounds(); 
					Node insertBefore = NodeUtilities.getNextVerticalNode(lifeline,newPosY);
					while (insertBefore != null && NodeUtilities.isStartNode(insertBefore)) {
						insertBefore = insertBefore.getParent();
					}
					ClusterImpl intUseLfCluster = new ClusterImpl(intUse);
					intUseLfCluster.addNode(new MarkNodeImpl(Kind.start, intUse));
					intUseLfCluster.addNode(new MarkNodeImpl(Kind.end, intUse));

					((NodeImpl)intUseLfCluster.getNodes().get(0)).setBounds(new Rectangle(parentRect.getCenter().x , newPosY, 0, 0));
					((NodeImpl)intUseLfCluster.getNodes().get(1)).setBounds(new Rectangle(parentRect.getCenter().x , newPosY + height, 0, 0));

					((ClusterImpl)parent).addNode(intUseLfCluster, insertBefore);
					((FragmentClusterImpl)fragmentCluster).addCluster(intUseLfCluster); // TODO: @etxacam reorder them...
				}
				
				graph.layout();
				return true;
			}
		});

	}

	public void moveInteractionUse(InteractionUse intUse, Rectangle rect) {
		FragmentCluster fragmentCluster = (FragmentCluster)interactionGraph.getClusterFor(intUse);		
		Rectangle currRect = fragmentCluster.getBounds();
		Dimension moveDelta = rect.getTopLeft().getDifference(currRect.getTopLeft());
		
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		for (Cluster n : fragmentCluster.getClusters()) {
			Rectangle lifelineArea = ViewUtilities.getClientAreaBounds(interactionGraph.getEditPartViewer(), 
					NodeUtilities.getLifelineNode(n).getView());
			minY = Math.min(minY,lifelineArea.y);
			maxY = Math.max(maxY,lifelineArea.y+lifelineArea.height);
		}
		
		List<Node> nodes = NodeUtilities.getBlock(fragmentCluster);
		Rectangle totalArea = NodeUtilities.getArea(nodes);
		totalArea.translate(moveDelta.width, moveDelta.height);
		if (totalArea.y <= minY || totalArea.y >= maxY) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}
				
		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}
			
			@Override
			public boolean apply(InteractionGraph graph) {
				((InteractionGraphImpl)graph).moveNodeBlock(nodes, totalArea.y); // We moved to the 
				return true;
			}
		});
		
	}

	public void deleteInteractionUse(InteractionUse intUse) {
		FragmentCluster intUseNode = (FragmentCluster)interactionGraph.getClusterFor(intUse);
		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}

			@Override
			public boolean apply(InteractionGraph graph) {
				// TODO: It needs to handle gates.
				List<Node> blockToDelete = NodeUtilities.getBlock(intUseNode);
				List<Link> linksToDelete = new ArrayList<Link>(NodeUtilities.flattenKeepClusters(blockToDelete).stream().map(Node::getConnectedByLink).
						filter(Predicate.isEqual(null).negate()).collect(Collectors.toSet()));
				try {
					// Remove nodes & Messages
					interactionGraph.disableLayout();
					NodeUtilities.removeMessageLinks(graph, linksToDelete);
				} finally {
					interactionGraph.enableLayout();
				}
				
				try {
					((InteractionGraphImpl)graph).removeNodeBlock(blockToDelete);		
					((InteractionGraphImpl)graph).removeFragmentCluster(intUseNode);
					interactionGraph.disableLayout();
					NodeUtilities.removeNodes(graph, intUseNode.getClusters());
				} finally {
					interactionGraph.enableLayout();
					interactionGraph.layout();					
				}
				return true;
				
			}
		});
	}
		
	private void resizeCluster(ClusterImpl cluster, int topAmmount, int bottomAmmount) {
		Rectangle clusterBounds = cluster.getBounds(); 
		ClusterImpl parent = (ClusterImpl)cluster.getParent();
		Node startNode = cluster.getNodes().get(0);
		Node endNode = cluster.getNodes().get(cluster.getNodes().size()-1);
		
		int startPos = clusterBounds.y - topAmmount;
		int endPos = clusterBounds.y + clusterBounds.height + bottomAmmount;
		
		// TODO: @etxacam nudge if overlapp

		
		// Add nodes in parent between [top.y + topAmmount ... top.y)		
		// Add nodes in parent between (bottomAmmount ... bottom.y + bottomAmmount]
		int off = 0;
		if (cluster.getNodes().size() > 2 && cluster.getNodes().get(1).getElement() == cluster.getElement())
			off = 1;
		for (Node n : new ArrayList<>(parent.getNodes())) {
			Rectangle b = n.getBounds();
			if (b.y >= startPos && b.y < clusterBounds.y) {
				parent.removeNode((NodeImpl)n);
				cluster.addNode(1+off, (NodeImpl)n);
				off++;
			} else if (b.y > clusterBounds.y && b.y <= endPos) {
				parent.removeNode((NodeImpl)n);
				cluster.addNode(cluster.getNodes().size()-1, (NodeImpl)n);				
			}
		}
		
		// Remove nodes in cluster between [top.y ... top.y + topAmmount]
		// Remove nodes in cluster between [bottom.y + bottomAmmount ... bottom.y]
		for (Node n : new ArrayList<>(cluster.getNodes().subList(1, cluster.getNodes().size()-1))) {
			if (n.getElement() == cluster.getElement())
				continue;
			Rectangle b = n.getBounds();
			if (b.y < startPos) {
				cluster.removeNode((NodeImpl)n);
				parent.addNode((NodeImpl)n,cluster);
			} else if (b.y > endPos) {
				cluster.removeNode((NodeImpl)n);
				parent.addNode(parent.getNodes().indexOf(cluster)+1,(NodeImpl)n);
			}
		}
		
		startNode.getBounds().y-=topAmmount;
		endNode.getBounds().y+=bottomAmmount;		
		
		cluster.getBounds().y = startNode.getBounds().y;
		cluster.getBounds().height = endNode.getBounds().y - startNode.getBounds().y;
		
		if (cluster.getNodes().size() > 2 && cluster.getNodes().get(1).getElement() == cluster.getElement()) {
			cluster.getNodes().get(1).getBounds().y = cluster.getBounds().y; 
		}
	}
	
	private void shrinkCluster(Cluster cluster, int ammount) {
		Node lastNode = cluster.getNodes().get(cluster.getNodes().size()-1);

		Cluster parent = cluster.getParent();
		Rectangle bounds = cluster.getBounds();
		int newPosY = bounds.y + bounds.height - ammount;

		Node fromNode = NodeUtilities.getNextVerticalNode(cluster, newPosY);
		while (fromNode != null && fromNode.getParent() != cluster)
			fromNode = fromNode.getParent();
		List<Node> nodesToMoveIn = cluster.getNodes().subList(cluster.getNodes().indexOf(fromNode), cluster.getNodes().size()-1);														
		if (!nodesToMoveIn.isEmpty()) {
			int idx = parent.getNodes().indexOf(cluster)+1;
			Node insertBefore = null; 
			if (idx < parent.getNodes().size())
				insertBefore = parent.getNodes().get(idx);						
			NodeUtilities.moveNodes(interactionGraph, nodesToMoveIn, parent, insertBefore, NodeUtilities.getArea(nodesToMoveIn).y);
		}
		lastNode.getBounds().y -= ammount;
		cluster.getBounds().height -= ammount;

	}

	private void expandCluster(Cluster cluster, int ammount) {
		Node lastNode = cluster.getNodes().get(cluster.getNodes().size()-1);
		
		Cluster parent = cluster.getParent();
		Rectangle bounds = cluster.getBounds();
		int newPosY = bounds.y + bounds.height + ammount;
		Node untilNode = NodeUtilities.getNextVerticalNode(parent,  newPosY);
		while (untilNode != null && untilNode.getParent() != parent)
			untilNode = untilNode.getParent();

		int curIndx = parent.getNodes().indexOf(cluster);
		int lastIdx = parent.getNodes().indexOf(untilNode);
		List<Node> nodesToMoveIn = parent.getNodes().subList(curIndx+1, lastIdx == -1 ? parent.getNodes().size() : lastIdx);
		if (!nodesToMoveIn.isEmpty()) {
			NodeUtilities.moveNodes(interactionGraph, nodesToMoveIn, cluster, lastNode, NodeUtilities.getArea(nodesToMoveIn).y);
		}		
		
		lastNode.getBounds().y += ammount;
		cluster.getBounds().height += ammount;
	}

	public void moveMessageEnd(MessageEnd msgEnd, Lifeline toLifeline, Point location) {		
		if (!(msgEnd instanceof MessageOccurrenceSpecification)) {
			// TODO @etxacam We need to handle Gates
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}
		Message msg = msgEnd.getMessage();
		boolean isRecvEvent = msg.getReceiveEvent() == msgEnd; 
		Link link = interactionGraph.getLinkFor(msg);
		Node msgEndNode = interactionGraph.getNodeFor(msgEnd);
		Cluster toLifelineNode = interactionGraph.getLifeline(toLifeline);
		boolean isChangingLifeline = NodeUtilities.getLifelineNode(msgEndNode) != toLifelineNode;
				
		Node target = link.getTarget();
		Node source = link.getSource();

		int selfMsgSpace = 0;
		if (NodeUtilities.isSelfLink(link) && !isChangingLifeline)
			selfMsgSpace = 20;

		Point newLoc = location.getCopy();
		if (isRecvEvent) {
			if (newLoc.y < (source.getBounds().y + selfMsgSpace)) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;
			}				
		} else {
			if (newLoc.y > (target.getBounds().y - selfMsgSpace)) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;
			}							
		}

		if (isRecvEvent && isChangingLifeline) {
			if (NodeUtilities.isNodeLifelineStartByCreateMessage(toLifelineNode) && NodeUtilities.isCreateOcurrenceSpecification(msgEndNode)) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;				
			}
			
			if (NodeUtilities.isNodeLifelineEndsWithDestroyOcurrenceSpecification(toLifelineNode) && NodeUtilities.isDestroyOcurrenceSpecification(msgEndNode)) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;				
			}			
		}		
		
		Rectangle lifelineArea = ViewUtilities.getClientAreaBounds(interactionGraph.getEditPartViewer(), 
				NodeUtilities.getLifelineNode(toLifelineNode).getView());
		int minY = lifelineArea.y;
		int maxY = lifelineArea.y + lifelineArea.height - 1;
		
		List<Node> nodes = isRecvEvent ? NodeUtilities.getBlock(source) : Arrays.asList(source);
		if (isRecvEvent) {
			nodes.remove(source);
		} 
		
		Link l = NodeUtilities.getStartLink(link);
		if ( l != null && l != link) {
			minY = l.getTarget().getBounds().y;
		}
		l = NodeUtilities.getFinishLink(link);
		if ( l != null && l != link) {
			maxY = l.getSource().getBounds().y;
		}

		Rectangle totalArea = NodeUtilities.getArea(Arrays.asList(msgEndNode));
		totalArea.setLocation(newLoc);
		if (totalArea.y <= minY || totalArea.y >= maxY) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}
		
		moveMessageEndImpl(msgEnd, toLifelineNode, location);
	}
	
	private void moveMessageEndImpl(MessageEnd msgEnd, Cluster toMsgEndOwnerCluster, Point location) {		
		Message msg = msgEnd.getMessage();
		boolean isRecvEvent = msg.getReceiveEvent() == msgEnd; 
		Link link = interactionGraph.getLinkFor(msg);
		Node msgEndNode = interactionGraph.getNodeFor(msgEnd);
		Cluster lifelineCluster = NodeUtilities.getLifelineNode(msgEndNode);
		boolean isChangingOwner = lifelineCluster != null && lifelineCluster != toMsgEndOwnerCluster;
				
		Node target = link.getTarget();
		Node source = link.getSource();

		int selfMsgSpace = 0;
		if (NodeUtilities.isSelfLink(link) && !isChangingOwner)
			selfMsgSpace = 20;

		Point newLoc = location.getCopy();
		List<Node> nodes = isRecvEvent ? NodeUtilities.getBlock(source) : Arrays.asList(source);
		if (isRecvEvent) {
			nodes.remove(source);
		} 
		
		Rectangle totalArea = NodeUtilities.getArea(Arrays.asList(msgEndNode));
		totalArea.setLocation(newLoc);
		Link otherLink = isRecvEvent ? NodeUtilities.getStartLink(link) : NodeUtilities.getFinishLink(link);		
		

		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}

			@Override
			public boolean apply(InteractionGraph graph) {
				Map<Node, Cluster> moveToLifelines = new HashMap<>();
				if (isChangingOwner) {
					moveToLifelines.put(msgEndNode, toMsgEndOwnerCluster);
					// Flatting ExecSpec in any case. Only relevant when isRecEvent and changing lifeline.
					if (source.getConnectedNode() != null && source.getConnectedNode().getElement() instanceof ExecutionSpecification) {
						Cluster c = (Cluster)source.getConnectedNode();
						moveToLifelines.put(c, toMsgEndOwnerCluster);
						c.getAllNodes().forEach(d -> moveToLifelines.put(d, toMsgEndOwnerCluster));
					}
				}
				
				List<Link> previousSelfLinks = new ArrayList<>(nodes.stream().map(Node::getConnectedByLink).filter(Predicate.isEqual(null).negate()).
					filter(NodeUtilities::isSelfLink).collect(Collectors.toSet()));
				
				ClusterImpl finishingBlock = null;
				if (!isRecvEvent && NodeUtilities.isFinishNode(msgEndNode)){
					finishingBlock = (ClusterImpl)msgEndNode.getParent();
				}
				
				((InteractionGraphImpl)graph).moveNodeBlock(nodes, totalArea.y, moveToLifelines);

				if (finishingBlock != null) {
					if (finishingBlock != msgEndNode.getParent() && finishingBlock.getParent() != msgEndNode.getParent()) {
						// TODO: @etxacam Make a it a constrain when providing command instead...
						// The finish mark end up in a different parent OR IMPLEMENT IT!				
						return false;
					}
					
					if (finishingBlock.getParent() == msgEndNode.getParent()) {
						// Re-add node to the block
						ClusterImpl parent = (ClusterImpl)finishingBlock.getParent();
						int clIndex = parent.getNodes().indexOf(finishingBlock);
						int index = parent.getNodes().indexOf(msgEndNode);
						
						for (int i= clIndex+1; i<=index; i++) {
							NodeImpl n = parent.removeNode(clIndex+1);
							finishingBlock.addNode(n);
						}
					}
					graph.layout();
				}
				
				// Re-target reply msg.
				if (otherLink != null) {
					Node otherEndNode = isRecvEvent ? otherLink.getSource() : otherLink.getTarget();
					List<Node> otherNodes = Arrays.asList(otherEndNode);
					Rectangle otherTotalArea = NodeUtilities.getArea(otherNodes);
					Map<Node, Cluster> otherMoveToLifelines = new HashMap<>();
					otherMoveToLifelines.put(otherEndNode, toMsgEndOwnerCluster);					
					boolean selfMsg = NodeUtilities.getLifelineNode(isRecvEvent ? otherLink.getTarget() : otherLink.getSource()) == toMsgEndOwnerCluster;
					((InteractionGraphImpl)graph).moveNodeBlock(otherNodes, otherTotalArea.y + (selfMsg ? 20 : 0), otherMoveToLifelines);		
					
					// Check if both ends has the same parent???
					if (msgEndNode.getParent() != otherEndNode.getParent()) {
						// TODO: @etxacam Make a it a constrain when providing command instead...
						// We undo as one of the ends has a different parent.
						return false;
					}
					
					if (previousSelfLinks.contains(link))
						previousSelfLinks.add(otherLink);
					
				}
				
				// Nudge & "unnudge" self messages ends.
				List<Link> selfLinks = new ArrayList<>(nodes.stream().map(Node::getConnectedByLink).filter(Predicate.isEqual(null).negate()).
						filter(NodeUtilities::isSelfLink).collect(Collectors.toSet()));
				((InteractionGraphImpl)graph).disableLayout();
				try {
					((InteractionGraphImpl)graph).reArrangeSelfMessages(previousSelfLinks, selfLinks);
				} finally {
					((InteractionGraphImpl)graph).enableLayout();					
					((InteractionGraphImpl)graph).layout();
				}
				
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
					
					if (isSelfLink && (trgAnchorPoint.y - srcAnchorPoint.y) < 20)
						trgAnchorPoint.y = srcAnchorPoint.y + 20;
					
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
	private List<InteractionGraphEditAction> actions = new ArrayList<>();
}
