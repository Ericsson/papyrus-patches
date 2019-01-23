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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Link;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Row;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ClusterImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ColumnImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.InteractionGraphImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.NodeImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.NodeOrderResolver;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.NodeUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.RowImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.uml2.uml.ActionExecutionSpecification;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
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
				((InteractionGraphImpl)graph).disableLayout();
				try {
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
					
					ClusterImpl sourceCluster = (ClusterImpl)graph.getClusterFor(source);
					Node srcBeforeFrag = NodeUtilities.flatten(sourceCluster).stream().
						filter(n -> n.getBounds() != null && srcAnchor.y < n.getBounds().y).findFirst().orElse(null);
					ClusterImpl targetCluster = (ClusterImpl)graph.getClusterFor(target);
					Node trgBeforeFrag = NodeUtilities.flatten(targetCluster).stream().
							filter(n -> n.getBounds() != null && trgAnchor.y < n.getBounds().y).findFirst().orElse(null);
					
					Lifeline srcLifeline = (Lifeline)NodeUtilities.getLifelineNode(sourceCluster).getElement();
					NodeImpl srcNode = (NodeImpl)graph.addMessageOccurrenceSpecification(srcLifeline, mosSrc, srcBeforeFrag);
					srcNode.setBounds(new Rectangle(srcAnchor,new Dimension(1, 1)));
					
					Lifeline trgLifeline = (Lifeline)NodeUtilities.getLifelineNode(targetCluster).getElement();
					NodeImpl trgNode = (NodeImpl)graph.addMessageOccurrenceSpecification(trgLifeline, mosTrg, trgBeforeFrag);				
					trgNode.setBounds(new Rectangle(trgAnchor,new Dimension(1, 1)));
					
					message = graph.connectMessageOcurrenceSpecification(mosSrc, mosTrg);
					((InteractionGraphImpl)graph).enableLayout();
					graph.layout();
					
					if (msg.getMessageSort() == MessageSort.SYNCH_CALL_LITERAL) {
						// TODO: @etxacam Check preferences for auto create of behavior spc...
						// TODO: @etxacam Show menu to create action / behavior exec spec.
						
						// TODO @etxacam if horizontal message, nudge there are more nodes in the same row (inclusive message 
						//   if there are in previous columns)  
						graph.getRows().stream().filter(d -> (d.getIndex() > srcNode.getRow().getIndex() 
								|| d.getIndex() > trgNode.getRow().getIndex()))
								.map(RowImpl.class::cast).forEach(d -> d.nudge(40));
						graph.layout();
						
						((InteractionGraphImpl)graph).disableLayout();

						// Create reply message
						Message replyMsg = UMLFactory.eINSTANCE.createMessage();
						replyMsg.setMessageSort(MessageSort.REPLY_LITERAL);
						replyMsg.setName("Reply " + msg.getName());
	
						MessageOccurrenceSpecification repMosSrc = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
						MessageOccurrenceSpecification repMosTrg= UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
						replyMsg.setSendEvent(repMosSrc);
						repMosSrc.setMessage(replyMsg);
						replyMsg.setReceiveEvent(repMosTrg);				
						repMosTrg.setMessage(replyMsg);
						
						NodeImpl repSrcNode = (NodeImpl)graph.addMessageOccurrenceSpecification(trgLifeline, repMosSrc, trgBeforeFrag);
						Point repSrcAnchor = new Point(trgAnchor.x, trgAnchor.y + 60);
						repSrcNode.setBounds(new Rectangle(repSrcAnchor,new Dimension(1, 1)));
						
						NodeImpl repTrgNode = (NodeImpl)graph.addMessageOccurrenceSpecification(srcLifeline, repMosTrg, srcBeforeFrag);				
						Point repTrgAnchor = new Point(srcAnchor.x, repSrcAnchor.y);
						repTrgNode.setBounds(new Rectangle(repTrgAnchor,new Dimension(1, 1)));
	
						message = graph.connectMessageOcurrenceSpecification(repMosSrc, repMosTrg);
	
						ExecutionSpecification execSpec = UMLFactory.eINSTANCE.createBehaviorExecutionSpecification();
						execSpec.setStart(mosTrg);
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

	public void nudgeMessage(Message msg, Point delta) {
		// TODO: @etxacam Need to handle Lost & Found messages, messages with gates and create message. 
		Link link = interactionGraph.getLinkFor(msg);
		
		Node source = link.getSource();
		Set<Node> limitNodes = new HashSet<Node>();
		Row row = source.getRow();
		// Add row nodes except ExecSpecs
		limitNodes.addAll(row.getNodes());
		
		
		if (row.getIndex() > 1)
			limitNodes.addAll(interactionGraph.getRows().get(row.getIndex()-1).getNodes());
		
		Node target = link.getTarget();
		row = target.getRow();
		limitNodes.addAll(row.getNodes());
		if (row.getIndex() > 1)
			limitNodes.addAll(interactionGraph.getRows().get(row.getIndex()-1).getNodes());
		
		limitNodes.remove(source);
		limitNodes.remove(target);
		limitNodes.removeAll(limitNodes.stream().filter(d -> d.getElement() instanceof ExecutionSpecification).
				collect(Collectors.toList()));
		Rectangle validArea = NodeUtilities.getEmptyArea(interactionGraph, null, new ArrayList<>(limitNodes), null, null);
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
				graph.getRows().stream().filter(d -> (d.getIndex() >= source.getRow().getIndex() 
						|| d.getIndex() >= target.getRow().getIndex()))
						.map(RowImpl.class::cast).forEach(d -> d.nudge(delta.y));
				graph.layout();
				return true;
			}
		});
	}

	public void nudgeMessageEnd(MessageEnd msgEnd, Point delta) {
		if (!(msgEnd instanceof MessageOccurrenceSpecification)) {
			// TODO @etxacam We need to handle Gates
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}
		
		boolean isRecvEvent = msgEnd.getMessage().getReceiveEvent() == msgEnd; 
		Link link = interactionGraph.getLinkFor(msgEnd.getMessage());
		Node msgEndNode = interactionGraph.getNodeFor(msgEnd);		
		
		Set<Node> limitNodes = new HashSet<Node>();
		Row row = msgEndNode.getRow();
		limitNodes.addAll(row.getNodes());
		if (row.getIndex() > 1)
			limitNodes.addAll(interactionGraph.getRows().get(row.getIndex()-1).getNodes());

		Node target = link.getTarget();
		Node source = link.getSource();
		limitNodes.remove(source);
		limitNodes.remove(target);

		Rectangle validArea = NodeUtilities.getEmptyArea(interactionGraph, null,new ArrayList<>(limitNodes),null,null);
		validArea.x = msgEndNode.getParent().getBounds().x;
		validArea.width = msgEndNode.getParent().getBounds().width;
		Rectangle newMsgEndPos = msgEndNode.getBounds().getCopy().translate(delta);
		if (!validArea.contains(newMsgEndPos)) {
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}

		if (isRecvEvent) {
			if (newMsgEndPos.y < link.getSource().getBounds().y) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;
			}				
		} else {
			if (newMsgEndPos.y > link.getTarget().getBounds().y) {
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
				if (isRecvEvent) {
					graph.getRows().stream().filter(d -> (d.getIndex() > msgEndNode.getRow().getIndex()))
							.map(RowImpl.class::cast).forEach(d -> d.nudge(delta.y));
					graph.layout();
				}
				
				Rectangle r = msgEndNode.getBounds();
				r.y = newMsgEndPos.y;
				graph.layout();
				return true;
			}
		});
	}
	
	// TODO: @etxacam Need to make to resize the interaction size so we make place for new messages and fragments.  
	// TODO: @etxacam Need to move the message when no changing order. Why is not working???  
	// TODO: @etxacam It is not working to move messages in exec specs
	// TODO: @etxacam Check there is no bucles.
	public void moveMessage(Message msg, Point moveDelta) {
		Link link = interactionGraph.getLinkFor(msg);
		Node source = link.getSource();
		List<Node> nodes = NodeUtilities.getBlock(source);
		Rectangle totalArea = NodeUtilities.getArea(nodes);
		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}
			
			// TODO: @etxacam When moveing to top, the dist should be from lifeline row bottom.
			// TODO: @etxacam Finish Sync Node (Reply msg) can not dettach from exec spec and include or remove blocks from 
			@Override
			public boolean apply(InteractionGraph graph) {
				((InteractionGraphImpl)graph).moveNodeBlock(nodes, totalArea.y+moveDelta.y);
				return true;
			}
		});
	}
	
	public void moveMessage1(Message msg, Point moveDelta) {
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
				interactionGraph.disableLayout();
				try {
					source.getBounds().y = newSrcY;
					interactionGraph.moveMessageOccurrenceSpecification(srcLifeline, (MessageOccurrenceSpecification)source.getElement(), 
							srcLifeline, srcMoveBeforeNode == null ? null : (InteractionFragment)srcMoveBeforeNode.getElement());				
					
					target.getBounds().y = newTrgY;
	
					interactionGraph.moveMessageOccurrenceSpecification(trgLifeline, (MessageOccurrenceSpecification)target.getElement(), 
							trgLifeline, trgMoveBeforeNode == null ? null : (InteractionFragment)trgMoveBeforeNode.getElement());
				} finally {
					interactionGraph.enableLayout();
				}
				
				interactionGraph.layout();
				
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
	
	public void moveMessageEnd(MessageEnd msgEnd, Lifeline toLifeline, Point newLocation) {		
		if (!(msgEnd instanceof MessageOccurrenceSpecification)) {
			// TODO @etxacam We need to handle Gates
			actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
			return;
		}
		
		boolean isRecvEvent = msgEnd.getMessage().getReceiveEvent() == msgEnd; 
		Link link = interactionGraph.getLinkFor(msgEnd.getMessage());
		Node msgEndNode = interactionGraph.getNodeFor(msgEnd);		
		Lifeline currLifeline = ((MessageOccurrenceSpecification)msgEnd).getCovered();
		
		Node target = link.getTarget();
		Node source = link.getSource();

		if (isRecvEvent) {
			if (newLocation.y < source.getBounds().y) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;
			}				
		} else {
			if (newLocation.y > target.getBounds().y) {
				actions.add(AbstractInteractionGraphEditAction.UNEXECUTABLE_ACTION);
				return;
			}				
			
		}

		Cluster toLifelineNode = interactionGraph.getLifeline(toLifeline);
		List<Node> toLifelineNodes = toLifelineNode.getAllNodes();  
		Node toMoveBeforeNode = toLifelineNodes.stream().filter(d -> d.getBounds().getCenter().y > newLocation.y).findFirst().orElse(null);

		actions.add(new AbstractInteractionGraphEditAction(interactionGraph) {
			@Override
			public void handleResult(CommandResult result) {
			}

			@Override
			public boolean apply(InteractionGraph graph) {
				msgEndNode.getBounds().y = newLocation.y;
				msgEndNode.getBounds().x = toLifelineNode.getBounds().getCenter().x;

				interactionGraph.moveMessageOccurrenceSpecification(currLifeline, (MessageOccurrenceSpecification)msgEndNode.getElement(), 
						toLifeline, toMoveBeforeNode == null ? null: (InteractionFragment)toMoveBeforeNode.getElement());
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
		calculateExecutionSpecificationEditingCommand(editingDomain, command);
		calculateMessagesChanges(editingDomain, command);
		calculateFragmentsChanges(editingDomain, command);

		updateBoundsChanges(command, Collections.singletonList(interactionGraph), false);
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
		cmd.add(new DeleteCommand(editingDomain, interactionGraph.getLifeline(lifeline).getView()));
		cmd.add(new DestroyElementCommand(new DestroyElementRequest(editingDomain, lifeline, false)));
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
				UMLPackage.Literals.INTERACTION__FRAGMENT, executionSpecifications, graphExecutionSpecifications, addCommandFunction,
				removeCommandFunction, false, false);
		
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
		List<InteractionFragment> graphFragments = orderResolver.getOrderedNodes().stream()
				.map(Node::getElement).filter(InteractionFragment.class::isInstance)
				.map(InteractionFragment.class::cast).collect(Collectors.toList());
				
		Function<InteractionFragment, IUndoableOperation> addCommandFunction = (d) -> new EMFCommandOperation(editingDomain,
				AddCommand.create(editingDomain, interactionGraph.getInteraction(), UMLPackage.Literals.INTERACTION__FRAGMENT, d));
		Function<InteractionFragment, IUndoableOperation> removeCommandFunction = (d) -> new EMFCommandOperation(editingDomain,
				RemoveCommand.create(editingDomain, interactionGraph.getInteraction(), UMLPackage.Literals.INTERACTION__FRAGMENT, d));

		createCommandsForCollectionChanges(command, interactionGraph.getInteraction(),
				UMLPackage.Literals.INTERACTION__FRAGMENT, fragments, graphFragments, addCommandFunction,
				removeCommandFunction, false, false);
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
					command.add(new SetLinkViewAnchorCommand(editingDomain, lk, SetLinkViewAnchorCommand.Anchor.SOURCE, 
							endView, p, "Set Source Link Anchor", null));
				} else {
					command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
							editingDomain, lk.getEdge(), NotationPackage.Literals.EDGE__SOURCE, null)));					
					command.add(new EMFCommandOperation(editingDomain, SetCommand.create(
							editingDomain, lk.getEdge(), NotationPackage.Literals.EDGE__SOURCE_ANCHOR, null)));					
				}
			
				if (lk.getTarget() != null) {
					Point p = lk.getTarget().getBounds().getTopLeft();
					View endView = lk.getTarget().getParent().getView();
					command.add(new SetLinkViewAnchorCommand(editingDomain, lk, SetLinkViewAnchorCommand.Anchor.TARGET, 
							endView, p, "Set Target Link Anchor", null));
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
				cmd.add(new SetNodeViewBoundsCommand(getEditingDomain(), node, node.getBounds(), updateParts, "Set location",
						Collections.emptyList()));
			}
			index++;
		}
	}

	private void updateBoundsChanges(ICompositeCommand cmd, List<Node> nodes, boolean updateParts) {
		for (Node node : nodes) {
			cmd.add(new SetNodeViewBoundsCommand(getEditingDomain(), node, node.getBounds(), updateParts, "Set location", Collections.emptyList()));
		}
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
