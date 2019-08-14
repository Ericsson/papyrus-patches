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

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest.ViewDescriptor;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.providers.SequenceDiagramElementTypes;
import org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Column;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Link;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.MarkNode;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.MarkNode.Kind;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Row;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.SemanticElementsService;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Element;
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

/**
 * @author ETXACAM
 *
 */
public class InteractionGraphService {
	public InteractionGraphService(InteractionGraphImpl interactionGraph, TransactionalEditingDomain editingDomain) {
		this.interactionGraph = interactionGraph;
		this.editingDomain = editingDomain;
	}
	
	public boolean canAddLifeline(CreateElementRequestAdapter elementAdapter, Rectangle rect) {
		// TODO: Check with ElementType Services?? Just now, we assume it is possible. 
		return true;
	}
	
	public Cluster addLifeline(Rectangle rect) {
		int x = rect.x;
		Cluster nextLifeline = interactionGraph.getLifelineClusters().stream()
				.filter(d -> ((ClusterImpl) d).getBounds().x >= x).findFirst().orElse(null);
		Lifeline lifeline = SemanticElementsService.createElement(
				editingDomain, interactionGraph.getInteraction(), 
				UMLElementTypes.Lifeline_Shape);
		lifeline.setName(NodeUtilities.getNewElementName(interactionGraph, lifeline));
		Cluster cluster = interactionGraph.addLifeline(lifeline, nextLifeline);
		Rectangle r = ((ClusterImpl) cluster).getBounds();
		if (ViewUtilities.isSnapToGrid(interactionGraph.getEditPartViewer(), interactionGraph.getDiagram()))
			ViewUtilities.snapToGrid(interactionGraph.getEditPartViewer(), interactionGraph.getDiagram(), rect);
		r.x = x;				
		int offset = r.width;
		((ClusterImpl) cluster).setBounds(r);
		interactionGraph.getColumns().stream().filter(d -> d.getXPosition() > x).map(ColumnImpl.class::cast)
				.forEach(d -> d.nudge(offset));
		interactionGraph.layout();
		return cluster;
	}

	public boolean canMoveLifeline(Lifeline lifeline, Point moveDelta) {
		Node lifelineNode = interactionGraph.getNodeFor(lifeline);
		
		Rectangle clientAreaBounds = ViewUtilities.getBounds(interactionGraph.getEditPartViewer(),
				lifelineNode.getView());
		final Rectangle newClientAreaBounds = clientAreaBounds.getCopy();
		newClientAreaBounds.translate(moveDelta.x, 0);

		Cluster nextLifeline = interactionGraph.getLifelineClusters().stream()
				.filter(l -> l.getBounds().x > newClientAreaBounds.x).findFirst().orElse(null);
		int newindex = nextLifeline == null ? interactionGraph.getLifelineClusters().size()
				: interactionGraph.getLifelineClusters().indexOf(nextLifeline);
		if (newindex < 0) {
			return false;
		}
		return true;
	}

	public void moveLifeline(Lifeline lifeline, Point moveDelta) {
		Node lifelineNode = interactionGraph.getNodeFor(lifeline);
		int newX = lifelineNode.getBounds().x + moveDelta.x;
		Cluster nextLifeline = interactionGraph.getLifelineClusters().stream()
				.filter(l -> l.getBounds().x > newX).findFirst().orElse(null);

		interactionGraph.moveLifeline(lifeline,
				(Lifeline) (nextLifeline == null ? null : nextLifeline.getElement()));
		lifelineNode.getBounds().x = newX;
		interactionGraph.layout();
	}

	public boolean canDeleteLifeline(Lifeline lifeline) {
		return true;
	}
	
	public void deleteLifeline(Lifeline lifeline) {
		Cluster lifelineNode = (Cluster)interactionGraph.getNodeFor(lifeline);
		List<List<Node>> blocksToDelete = NodeUtilities.getBlocks(lifelineNode.getNodes());
		List<Node> flatBlocksToDelete = blocksToDelete.stream().flatMap(d->d.stream()).collect(Collectors.toList()); 
		List<Link> linksToDelete = new ArrayList<Link>(NodeUtilities.flattenKeepClusters(flatBlocksToDelete).stream().map(Node::getConnectedByLink).
				filter(Predicate.isEqual(null).negate()).collect(Collectors.toSet()));
		try {
			// Remove nodes & Messages
			interactionGraph.disableLayout();
			NodeUtilities.removeMessageLinks(interactionGraph, linksToDelete);
		} finally {
			interactionGraph.enableLayout();
		}
		
		try {
			NodeUtilities.removeNodeBlocks(interactionGraph,blocksToDelete);			
			Column col = lifelineNode.getColumn();
			Column prev = col.getIndex() == 0 ? null : interactionGraph.getColumns().get(col.getIndex()-1);
			int prevSize = prev == null ? 0 : (lifelineNode.getBounds().x  - NodeUtilities.getArea(prev.getNodes()).right());
			int horzNudge = lifelineNode.getBounds().width + prevSize;
			interactionGraph.disableLayout();
			NodeUtilities.removeNodes(interactionGraph, Collections.singletonList(lifelineNode));
			interactionGraph.getColumns().stream().filter(d->d.getIndex() > col.getIndex()).forEach(d -> ((ColumnImpl)d).nudge(-horzNudge));
		} finally {
			interactionGraph.enableLayout();
			interactionGraph.layout();					
		}
	}

	public boolean canNudgeLifeline(Lifeline lifeline, Point moveDelta) {
		
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
			return false;
		}
		
		return true;
	}
	
	public void nudgeLifeline(Lifeline lifeline, Point moveDelta) {
		Node lifelineNode = interactionGraph.getNodeFor(lifeline);
		// We need to nudge the left colums associated with the fragments which the lifeline is the lefter most lifeline.
		List<FragmentCluster> fragmentClusters = lifeline.getCoveredBys().stream().
				filter(d->d instanceof InteractionUse || d instanceof CombinedFragment).
				map(interactionGraph::getClusterFor).map(d-> d instanceof FragmentCluster ? d : d.getFragmentCluster()).
				map(FragmentCluster.class::cast).collect(Collectors.toList());
		int firstColIndex = lifelineNode.getColumn().getIndex();
		for (FragmentCluster c : fragmentClusters) {
			List<Cluster> lifelines = c.getClusters().stream().map(NodeUtilities::getLifelineNode).filter(Predicate.isEqual(null).negate()).collect(Collectors.toList());
			int leftSideIndex = lifelines.stream().map(Node::getColumn).map(Column::getIndex).collect(Collectors.minBy(Integer::compare)).get();
			if (leftSideIndex == lifelineNode.getColumn().getIndex()) {
				firstColIndex = Math.min(firstColIndex,c.getAllGates().stream().
						map(Node::getColumn).map(Column::getIndex).collect(Collectors.minBy(Integer::compare)).get());
			}
				
		}

		int _firstColIndex = firstColIndex; 
		lifelineNode.getBounds().x += moveDelta.x;
		interactionGraph.getColumns().stream().filter(d -> d.getIndex() >= _firstColIndex)
				.map(ColumnImpl.class::cast).forEach(d -> d.nudge(moveDelta.x));
		
		
		
		interactionGraph.layout();
	}

	public boolean canResizeLifeline(Lifeline lifeline, Dimension sizeDelta) {
		Node lifelineNode = interactionGraph.getNodeFor(lifeline);
		Rectangle bounds = lifelineNode.getBounds();
		// TODO: Check size of label???
		if (bounds.width + sizeDelta.width <= 1) {
			return false;
		}
		return true;
	}
	
	public void resizeLifeline(Lifeline lifeline, Dimension sizeDelta) {
		Node lifelineNode = interactionGraph.getNodeFor(lifeline);
		lifelineNode.getBounds().width += sizeDelta.width;
		interactionGraph.getColumns().stream().filter(d -> d.getIndex() > lifelineNode.getColumn().getIndex())
				.map(ColumnImpl.class::cast).forEach(d -> d.nudge(sizeDelta.width));
		interactionGraph.layout();
	}
	
	public boolean canAddMessage(MessageSort msgSort, CreateElementRequestAdapter elementAdapter, ViewDescriptor descriptor, 
			Element source, Point srcAnchor, Element target, Point trgAnchor) {
		if (ViewUtilities.isSnapToGrid(interactionGraph.getEditPartViewer(), interactionGraph.getDiagram())) {
			ViewUtilities.snapToGrid(interactionGraph.getEditPartViewer(), interactionGraph.getDiagram(), srcAnchor);
			ViewUtilities.snapToGrid(interactionGraph.getEditPartViewer(), interactionGraph.getDiagram(), trgAnchor);
		}
		
		if (trgAnchor.y < srcAnchor.y) {
			return false;			
		}
		
		Cluster	targetCluster = interactionGraph.getClusterFor(target);		
		if (msgSort == MessageSort.CREATE_MESSAGE_LITERAL) {
			if (!(target instanceof Lifeline) || NodeUtilities.isNodeLifelineStartByCreateMessage(targetCluster)) {
				return false;
			}	
			
			List<Node> nodes = targetCluster.getNodes();
			if (!nodes.isEmpty() && nodes.get(0).getBounds().y <= trgAnchor.y) {
				return false;
			}
							
		} else if (msgSort == MessageSort.DELETE_MESSAGE_LITERAL) {
			if (!(target instanceof Lifeline) || NodeUtilities.isNodeLifelineEndsWithDestroyOcurrenceSpecification(targetCluster)) {
				return false;
			}			
			List<Node> nodes = targetCluster.getNodes();
			if (!nodes.isEmpty() && nodes.get(nodes.size()-1).getBounds().y >= trgAnchor.y) {
				return false;
			}
		}
		return true;
	}
	
	public Link addMessage(MessageSort msgSort, CreateElementRequestAdapter elementAdapter, ViewDescriptor descriptor, 
			Element source, Point srcAnchor, Element target, Point trgAnchor) {
		interactionGraph.disableLayout();
		try {
	
			Message msg = SemanticElementsService.createRelationship(editingDomain, interactionGraph.getInteraction(), source, target, 
					(IElementType)elementAdapter.getAdapter(IElementType.class)); 
			msg.setName(NodeUtilities.getNewElementName(interactionGraph, msg));
			
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
			
			int gridSpacing = interactionGraph.getGridSpacing();
			Row r = NodeUtilities.getRowAt(interactionGraph, srcAnchor.y);
			if (r != null) {
				NodeUtilities.nudgeRows(interactionGraph.getRows().subList(r.getIndex(),interactionGraph.getRows().size()), gridSpacing);
			}
	
			r = NodeUtilities.getRowAt(interactionGraph, trgAnchor.y);
			if (r != null) {
				NodeUtilities.nudgeRows(interactionGraph.getRows().subList(r.getIndex(),interactionGraph.getRows().size()), gridSpacing);
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
				srcNode = (NodeImpl)interactionGraph.addMessageOccurrenceSpecification(srcLifeline, (MessageOccurrenceSpecification)msgEndSrc, srcBeforeFrag);
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
				trgNode = (NodeImpl)interactionGraph.addMessageOccurrenceSpecification(trgLifeline, (MessageOccurrenceSpecification)msgEndTrg, trgBeforeFrag);				
			}
			trgNode.setBounds(new Rectangle(trgAnchor,new Dimension(0, 0)));
			
			Link message = interactionGraph.connectMessageEnds(msgEndSrc, msgEndTrg);
			IElementType execElemType = descriptor.getElementAdapter().getAdapter(IElementType.class);
			if (execElemType == SequenceDiagramElementTypes.Message_SynchActionEdge) {
				message.setProperty(LinkImpl.SYNCH_TYPE_PROPERTY, LinkImpl.SYNCH_TYPE_ACTION);
			} else if (execElemType == SequenceDiagramElementTypes.Message_SynchActionEdge) { 
				message.setProperty(LinkImpl.SYNCH_TYPE_PROPERTY, LinkImpl.SYNCH_TYPE_BEHAVIOR);
			}
			
			interactionGraph.enableLayout();
			interactionGraph.layout();
			
			// TODO: @etxacam What about when there are gates involved??? Should create Reply message and / or reply gate??
			if (msg.getMessageSort() == MessageSort.SYNCH_CALL_LITERAL 
					&& msgEndSrc instanceof MessageOccurrenceSpecification 
					&& msgEndTrg instanceof MessageOccurrenceSpecification) {
				if (execElemType == SequenceDiagramElementTypes.Message_SynchActionEdge) {
					execElemType = UMLElementTypes.ActionExecutionSpecification_Shape;
				} else {
					execElemType = UMLElementTypes.BehaviorExecutionSpecification_Shape;
				}
				
				target = ((MessageOccurrenceSpecification)msg.getReceiveEvent()).getCovered();
				addExecutionSpecification(execElemType, (Lifeline)target, (MessageOccurrenceSpecification)msg.getReceiveEvent(), true);
			} 
			return message;
		} finally {
			interactionGraph.enableLayout();					
		}
	}	

	private Cluster addActionExecutionSpecification(Lifeline lifeline, OccurrenceSpecification start, boolean createReplyMessage) {		
		return addExecutionSpecification(UMLElementTypes.ActionExecutionSpecification_Shape, lifeline, start, createReplyMessage);
	}
	
	private Cluster addBehaviorExecutionSpecification(Lifeline lifeline, OccurrenceSpecification start, boolean createReplyMessage) {		
		return addExecutionSpecification(UMLElementTypes.BehaviorExecutionSpecification_Shape, lifeline, start, createReplyMessage);
	}

	private Cluster addExecutionSpecification(IElementType execElemType, Lifeline lifeline, OccurrenceSpecification start, boolean createReplyMessage) {		
		NodeImpl startNode = interactionGraph.getNodeFor(start);	
		int nudgeFrom = startNode.getBounds().y;
		final Node _srcNode = startNode.getConnectedByNode();
		final Node _trgNode = startNode;
		List<Node> nodesAfter = interactionGraph.getLayoutNodes().stream().filter(
				d-> d != _srcNode && d != _trgNode && d.getBounds().y >= nudgeFrom).collect(Collectors.toList());
		Link link = startNode.getConnectedByLink();
		boolean isSelf =  link == null ? false : NodeUtilities.isSelfLink(link);
		interactionGraph.disableLayout();
		try {
			NodeUtilities.nudgeNodes(nodesAfter, 0, !isSelf ? 
					interactionGraph.getGridSpacing(40) : interactionGraph.getGridSpacing(60));
		} finally {
			interactionGraph.enableLayout();
			interactionGraph.layout();
		}
		
		interactionGraph.disableLayout();

		Node finishNode = null;
		OccurrenceSpecification finish = null;
		if (createReplyMessage && start instanceof MessageOccurrenceSpecification) {			
			Link message = addReplyMessage(startNode.getConnectedByLink().getElement());
			finishNode = message.getSource();
			finish = (OccurrenceSpecification)finishNode.getElement();
		} else {
			// TODO: Create the finish occurrence
		}
		
		
		ExecutionSpecification execSpec = SemanticElementsService.createElement(
				editingDomain, interactionGraph.getInteraction(), execElemType);
		execSpec.setName(NodeUtilities.getNewElementName(interactionGraph, UMLPackage.Literals.EXECUTION_SPECIFICATION));
		execSpec.setStart(start);
		execSpec.setFinish(finish);
		ClusterImpl execSpecCluster = (ClusterImpl)interactionGraph.addExecutionSpecification(lifeline, execSpec);
		execSpecCluster.getBounds();
		((InteractionGraphImpl)interactionGraph).enableLayout();
		interactionGraph.layout();					
		return execSpecCluster;
	}

	private Link addReplyMessage(Message msg) {
		// Create reply message
		Link link = interactionGraph.getLinkFor(msg);
		Node srcNode = link.getSource();
		Node trgNode = link.getTarget();
		Lifeline srcLifeline = (Lifeline)NodeUtilities.getLifelineNode(srcNode).getElement();
		Lifeline trgLifeline = (Lifeline)NodeUtilities.getLifelineNode(trgNode).getElement();
		boolean isSelf = NodeUtilities.isSelfLink(link);
		Message replyMsg = SemanticElementsService.createRelationship(editingDomain, interactionGraph.getInteraction(), 
				srcLifeline, trgLifeline, UMLElementTypes.Message_ReplyEdge);
		replyMsg.setName("Reply " + msg.getName());

		MessageOccurrenceSpecification repMosSrc = (MessageOccurrenceSpecification)replyMsg.getSendEvent();
		repMosSrc.setName("Send "+replyMsg.getName());
		MessageOccurrenceSpecification repMosTrg= (MessageOccurrenceSpecification)replyMsg.getReceiveEvent();
		repMosTrg.setName("Receive "+replyMsg.getName());
		
		Cluster trgLifelineNode =  NodeUtilities.getLifelineNode(link.getTargetAnchoringNode());
		NodeImpl repSrcNode = (NodeImpl)interactionGraph.addMessageOccurrenceSpecification(trgLifeline, repMosSrc, 
				NodeUtilities.getNextNode(trgLifelineNode, link.getTarget()));				
		Point trgAnchor = link.getTargetLocation();
		Point repSrcAnchor = new Point(trgAnchor.x, trgAnchor.y + interactionGraph.getGridSpacing(40));
		repSrcNode.setBounds(new Rectangle(repSrcAnchor,new Dimension(0, 0)));
		
		Cluster srcLifelineNode = NodeUtilities.getLifelineNode(link.getSourceAnchoringNode());
		NodeImpl repTrgNode = (NodeImpl)interactionGraph.addMessageOccurrenceSpecification(srcLifeline, repMosTrg, 
				NodeUtilities.getNextNode(srcLifelineNode, link.getSource()));				
		Point srcAnchor = link.getSourceLocation();
		Point repTrgAnchor = new Point(srcAnchor.x, repSrcAnchor.y + (isSelf ? interactionGraph.getGridSpacing(): 0));
		repTrgNode.setBounds(new Rectangle(repTrgAnchor,new Dimension(0, 0)));

		return interactionGraph.connectMessageEnds(repMosSrc, repMosTrg);
	}
	
	public boolean canDeleteMessage(Message msg) {
		// TODO: @etxacam Need to handle Lost & Found messages, messages with gates and create message.
		if (msg.getMessageSort() == MessageSort.REPLY_LITERAL) {
			return false;
		}			
		return true;
	}
	
	public void deleteMessage(Message msg) {
		Link link = interactionGraph.getLinkFor(msg);		
		Node source = link.getSource();

		List<Node> nodes = NodeUtilities.getBlock(source);
		List<Link> linksToDelete = new ArrayList<Link>(NodeUtilities.flattenKeepClusters(nodes).stream().map(Node::getConnectedByLink).
				filter(Predicate.isEqual(null).negate()).collect(Collectors.toSet()));

		NodeUtilities.removeNodeBlock(interactionGraph,nodes);
		NodeUtilities.removeMessageLinks(interactionGraph, linksToDelete);
	}

	public boolean canNudgeMessage(Message msg, Point delta) {
		Link link = interactionGraph.getLinkFor(msg);		
		Node source = link.getSource();
		Node target = link.getTarget();

		Rectangle validArea = NodeUtilities.getNudgeArea(interactionGraph, Arrays.asList(source,target), false, true);		
		Rectangle newMsgArea = link.getBounds().getCopy().translate(delta);
		if (!Draw2dUtils.contains(validArea,newMsgArea)) {
			return false;
		}		
		return true;
	}
	
	public void nudgeMessage(Message msg, Point delta) {
		// TODO: @etxacam Need to handle Lost & Found messages, messages with gates and create message. 
		Link link = interactionGraph.getLinkFor(msg);		
		Node source = link.getSource();
		Node target = link.getTarget();

		List<Row> nudgeRows = interactionGraph.getRows().stream().filter(
				d -> (d.getIndex() >= source.getRow().getIndex() || 
				d.getIndex() >= target.getRow().getIndex())).
				collect(Collectors.toList());
		NodeUtilities.nudgeRows(nudgeRows, delta.y);
		interactionGraph.layout();
	}

	// TODO: Make it generic for (gates & MOS) and provide two delegate methods, for MOS & Gates 
	public boolean canNudgeMessageEnd(MessageEnd msgEnd, Point location) {
		if (msgEnd.getMessage() == null) {
			return false;
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
						Collections.singletonList(msgEndNode), false, true,
						Collections.singletonList(target));
			if (Draw2dUtils.contains(Draw2dUtils.outsideRectangle(validArea.getCopy()),source.getLocation())) {
				validArea.y -= Draw2dUtils.SHRINK_SIZE; validArea.height += Draw2dUtils.SHRINK_SIZE; 
			}
		}

		Rectangle newMsgEndPos = msgEndNode.getBounds().getCopy().setLocation(location);
		if (NodeUtilities.isBorderNode(msgEndNode))
			validArea.expand(10, 0);
		
		if (!Draw2dUtils.contains(validArea,newMsgEndPos)) {
			return false;
		}

		int selfMsgSpace = 0;
		if (NodeUtilities.isSelfLink(link))
			selfMsgSpace = interactionGraph.getGridSpacing();

		if (isRecvEvent) {
			if (newMsgEndPos.y < (link.getSource().getBounds().y + selfMsgSpace)) {
				return false;
			}				
		} else {
			if (newMsgEndPos.y > (link.getTarget().getBounds().y - selfMsgSpace)) {
				return false;
			}				
			
		}
		
		return true;
	}
	
	/*
	 * Notes on MessageEnd (source) nudge:
	 * - The source part does not nudge normally, 2 possibilities:
	 * 		a) It does not nudge at all => Verify against the Empty area around it (Implemented)
	 * 		b) Nudge opposite => delta < 0 -> Nudge everything down and / or delta > 0 -> Nudge allt under up 
	 */
	// TODO: Make it generic for (gates & MOS) and provide two delegate methods, for MOS & Gates 
	public void nudgeMessageEnd(MessageEnd msgEnd, Point location) {
		Link link = interactionGraph.getLinkFor(msgEnd.getMessage());
		Node source = link.getSource();
		Node target = link.getTarget();

		boolean isRecvEvent = msgEnd.getMessage().getReceiveEvent() == msgEnd; 
		Node msgEndNode = interactionGraph.getNodeFor(msgEnd);		
		
		Rectangle newMsgEndPos = msgEndNode.getBounds().getCopy().setLocation(location);
		Dimension delta = newMsgEndPos.getLocation().getDifference(msgEndNode.getBounds().getLocation());

		boolean isGate = NodeUtilities.isBorderNode(msgEndNode);
		if (isRecvEvent) {
			List<Row> nudgeRows = interactionGraph.getRows().stream().filter(d -> (d.getIndex() > msgEndNode.getRow().getIndex()))
					.collect(Collectors.toList());
			NodeUtilities.nudgeRows(nudgeRows, delta.height);
			
			List<Node> ownRowNodes = msgEndNode.getRow().getNodes().stream().filter(d-> (d!=source && d!=target)).
					collect(Collectors.toList());
			NodeUtilities.nudgeNodes(ownRowNodes, isGate ? delta.width : 0, delta.height);
			
			interactionGraph.layout();
		} else if (delta.height > 0){
			
		}
		
		Rectangle r = msgEndNode.getBounds();
		r.y = newMsgEndPos.y;
		if (isGate)
			r.x = newMsgEndPos.x;
		interactionGraph.layout();
	}

	public boolean canMoveMessageEnd(MessageEnd msgEnd, Lifeline toLifeline, Point location) {
		if (msgEnd instanceof Gate) {
			return canMoveGate((Gate)msgEnd, toLifeline, location);
		} else {
			return canMoveMessageOccurrenceSpecification((MessageOccurrenceSpecification)msgEnd, toLifeline, location);
		}
	}

	public boolean moveMessageEnd(MessageEnd msgEnd, Lifeline toLifeline, Point location) {
		if (msgEnd instanceof Gate) {
			return moveGate((Gate)msgEnd, toLifeline, location);
		} else {
			return moveMessageOccurrenceSpecification((MessageOccurrenceSpecification)msgEnd, toLifeline, location);
		}				
	}

	public boolean canMoveMessageEnd(MessageEnd msgEnd, InteractionFragment toFragment, Point location) {
		if (msgEnd instanceof Gate) {
			return canMoveGate((Gate)msgEnd, toFragment, location);
		} else {
			return canMoveMessageOccurrenceSpecification((MessageOccurrenceSpecification)msgEnd, toFragment, location);
		}		
	}

	public boolean moveMessageEnd(MessageEnd msgEnd, InteractionFragment toFragment, Point location) {
		if (msgEnd instanceof Gate) {
			return moveGate((Gate)msgEnd, toFragment, location);
		} else {
			return moveMessageOccurrenceSpecification((MessageOccurrenceSpecification)msgEnd, toFragment, location);
		}				
	}

	public boolean canMoveMessageOccurrenceSpecification(MessageOccurrenceSpecification msgEnd, Lifeline toLifeline, Point location) {		
		if (!(msgEnd instanceof MessageOccurrenceSpecification)) {
			return false;
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
			selfMsgSpace = interactionGraph.getGridSpacing();

		Point newLoc = location.getCopy();
		if (isRecvEvent) {
			if (newLoc.y < (source.getBounds().y + selfMsgSpace)) {
				return false;
			}				
		} else {
			if (newLoc.y > (target.getBounds().y - selfMsgSpace)) {
				return false;
			}							
		}

		if (isRecvEvent && isChangingLifeline) {
			if (NodeUtilities.isNodeLifelineStartByCreateMessage(toLifelineNode) && NodeUtilities.isCreateOcurrenceSpecification(msgEndNode)) {
				return false;				
			}
			
			if (NodeUtilities.isNodeLifelineEndsWithDestroyOcurrenceSpecification(toLifelineNode) && NodeUtilities.isDestroyOcurrenceSpecification(msgEndNode)) {
				return false;				
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
			Node n = l.getSource().getParent();
			if (n.getParent() != null && NodeUtilities.getStartLink(n.getParent()) != null) {				
				maxY = n.getParent().getBounds().y;
			}
		}

		Rectangle totalArea = NodeUtilities.getArea(Arrays.asList(msgEndNode));
		totalArea.setLocation(newLoc);
		if (totalArea.y <= minY || totalArea.y >= maxY) {
			return false;
		}
		
		return canMoveMessageEndImpl(msgEnd, toLifelineNode, location);
	}

	public boolean moveMessageOccurrenceSpecification(MessageOccurrenceSpecification msgEnd, Lifeline toLifeline, Point location) {		
		Cluster toLifelineNode = interactionGraph.getLifeline(toLifeline);
		return moveMessageEndImpl(msgEnd,toLifelineNode, location);
	}
	
	public boolean canMoveGate(Gate gate, Lifeline toLifeline, Point location) {		
		Message msg = gate.getMessage();
		boolean isRecvEvent = msg.getReceiveEvent() == gate; 
		Link link = interactionGraph.getLinkFor(msg);
		Node msgEndNode = interactionGraph.getNodeFor(gate);
		Cluster toLifelineNode = interactionGraph.getLifeline(toLifeline);
				
		Node target = link.getTarget();
		Node source = link.getSource();

		Point newLoc = location.getCopy();
		if (isRecvEvent) {
			if (newLoc.y < source.getBounds().y) {
				return false;
			}				
		} else {
			if (newLoc.y > target.getBounds().y) {
				return false;
			}							
		}

		if (isRecvEvent) {
			if (NodeUtilities.isNodeLifelineStartByCreateMessage(toLifelineNode) && NodeUtilities.isCreateOcurrenceSpecification(msgEndNode)) {
				return false;				
			}
			
			if (NodeUtilities.isNodeLifelineEndsWithDestroyOcurrenceSpecification(toLifelineNode) && NodeUtilities.isDestroyOcurrenceSpecification(msgEndNode)) {
				return false;				
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
		
		Rectangle totalArea = NodeUtilities.getArea(Arrays.asList(msgEndNode));
		totalArea.setLocation(newLoc);
		if (totalArea.y <= minY || totalArea.y >= maxY) {
			return false;
		}
		
		return canMoveMessageEndImpl(gate, toLifelineNode, location);
	}
	
	public boolean moveGate(Gate gate, Lifeline toLifeline, Point location) {		
		Message msg = gate.getMessage();
		Link link = interactionGraph.getLinkFor(msg);
		boolean isRecEvt = link.getTarget() == gate;
		Cluster toLifelineNode = interactionGraph.getLifeline(toLifeline);
		MessageOccurrenceSpecification mos = SemanticElementsService.createElement(
				editingDomain, interactionGraph.getInteraction(), 
				org.eclipse.papyrus.uml.service.types.element.UMLElementTypes.MESSAGE_OCCURRENCE_SPECIFICATION);

		interactionGraph.disableLayout();		
		ClusterImpl lifelineCluster = (ClusterImpl)getMessageEndOwnerCluster(mos, interactionGraph.getInteraction());
		Node srcBeforeFrag = NodeUtilities.flatten(lifelineCluster).stream().
				filter(n -> n.getBounds() != null && location.y < n.getBounds().y).findFirst().orElse(null);
		
		NodeImpl mosNode = (NodeImpl)interactionGraph.addMessageOccurrenceSpecification(toLifeline, (MessageOccurrenceSpecification)mos, srcBeforeFrag);
		mosNode.setBounds(new Rectangle(location,new Dimension(0, 0)));						

		Node gateNode = interactionGraph.getNodeFor(gate);
		NodeUtilities.deleteNode(interactionGraph, gateNode);
		
		if (isRecEvt) {			
			interactionGraph.connectMessageEnds(msg.getSendEvent(), mos);
		} else {
			interactionGraph.connectMessageEnds(msg.getSendEvent(), mos);
		}
			
		interactionGraph.enableLayout();
		interactionGraph.layout();
		
		if (!moveMessageEndImpl(gate,toLifelineNode, location))
			return false;
		
		if (msg.getMessageSort() == MessageSort.SYNCH_CALL_LITERAL) {
			
			addExecutionSpecification(
					(LinkImpl.SYNCH_TYPE_ACTION.equals(link.getProperty(LinkImpl.SYNCH_TYPE_PROPERTY)) ? 
						UMLElementTypes.ActionExecutionSpecification_Shape :
						UMLElementTypes.BehaviorExecutionSpecification_Shape), 
						toLifeline, mos, true);
		}
		return true;
	}
	
	public boolean canMoveMessageOccurrenceSpecification(MessageOccurrenceSpecification msgEnd, InteractionFragment intFragment, Point location) {
		if (!CombinedFragment.class.isInstance(intFragment) && !InteractionOperand.class.isInstance(intFragment) &&
			!Interaction.class.isInstance(intFragment) && !InteractionUse.class.isInstance(intFragment)) {
			return false;
		}
		
		Message msg = msgEnd.getMessage();
		boolean isRecvEvent = msg.getReceiveEvent() == msgEnd; 
		Link link = interactionGraph.getLinkFor(msg);
		Node msgEndNode = interactionGraph.getNodeFor(msgEnd);
		Cluster toClusterOwnerNode = interactionGraph.getClusterFor(intFragment);
				
		Node target = link.getTarget();
		Node source = link.getSource();

		int selfMsgSpace = 0;
		// TODO: @etxacam Handle Self messages to and from clusyter fragments.
		if (NodeUtilities.isSelfLink(link))
			selfMsgSpace = interactionGraph.getGridSpacing();

		Point newLoc = location.getCopy();
		if (isRecvEvent) {
			if (newLoc.y < (source.getBounds().y + selfMsgSpace)) {
				return false;
			}				
		} else {
			if (newLoc.y > (target.getBounds().y - selfMsgSpace)) {
				return false;
			}							
		}

		Rectangle ownerArea = toClusterOwnerNode.getBounds();
		int minY = ownerArea.y;
		int maxY = ownerArea.y + ownerArea.height - 1;
		
		List<Node> nodes = isRecvEvent ? NodeUtilities.getBlock(source) : Arrays.asList(source);
		if (isRecvEvent) {
			nodes.remove(source);
		} 
		
		Rectangle totalArea = NodeUtilities.getArea(Arrays.asList(msgEndNode));
		totalArea.setLocation(newLoc);
		if (totalArea.y <= minY || totalArea.y >= maxY) {
			return false;
		}
		
		return canMoveMessageEndImpl(msgEnd, toClusterOwnerNode, location);
	}

	public boolean moveMessageOccurrenceSpecification(MessageOccurrenceSpecification msgEnd, InteractionFragment intFragment, Point location) {		
		Message msg = msgEnd.getMessage();
		Cluster toGateOwnerNode = interactionGraph.getClusterFor(intFragment);
		Point newLoc = location.getCopy();
		Node msgEndNode = interactionGraph.getNodeFor(msgEnd);
		FragmentCluster toMsgEndOwnerCluster = (FragmentCluster)interactionGraph.getClusterFor(intFragment);

		MarkNode mk = interactionGraph.setlayoutMark(location);
		interactionGraph.disableLayout();
		Gate gate = SemanticElementsService.createElement(editingDomain, intFragment,UMLElementTypes.Gate_Shape);
		
		if (NodeUtilities.isStartNode(msgEndNode)){
			Link l = NodeUtilities.getStartLink(msgEndNode.getParent());
			List<Node> nodes = NodeUtilities.getBlock(msgEndNode.getParent());
			List<Link> links = NodeUtilities.getBlockMessageLinks(msgEndNode.getParent());
			nodes.remove(l.getSource());
			NodeUtilities.removeNodeBlock(interactionGraph, nodes);
			links.remove(l);
			NodeUtilities.removeMessageLinks(interactionGraph, links);
		} else {
			NodeUtilities.deleteNode(interactionGraph, msgEndNode);
		}

		// TODO: Nugde if collision???		
		interactionGraph.enableLayout();
		interactionGraph.layout();					
		newLoc = mk.getLocation(); 
		interactionGraph.clearLayoutMarks();
		
		interactionGraph.disableLayout();
		Node insertBefore = ((FragmentClusterImpl)toMsgEndOwnerCluster).getAllGates().stream().
				filter(d->d.getBounds().y > location.y).findFirst().orElse(null);
		NodeImpl gateNode = interactionGraph.addGate(intFragment, gate, insertBefore);
		gateNode.setBounds(new Rectangle(newLoc,new Dimension(0,0)));
		if (msg.getReceiveEvent() == msgEnd)
			interactionGraph.connectMessageEnds(msg.getSendEvent(), gate);
		else
			interactionGraph.connectMessageEnds(gate, msg.getReceiveEvent());
		interactionGraph.enableLayout();
		interactionGraph.layout();					
		
		if (!moveMessageEndImpl(msgEnd, toGateOwnerNode, newLoc))
			return false;
		
		Rectangle r = msgEndNode.getBounds();
		if (r.x != newLoc.x) {
			msgEndNode.getBounds().x = newLoc.x;
			interactionGraph.layout();
		}
		return true;
	}

	private boolean canMoveMessageEndImpl(MessageEnd msgEnd, Cluster toMsgEndOwnerCluster, Point location) {
		// TODO: Move constraints from moveMessageEndImpl here.
		return true;
	}
	
	private boolean moveMessageEndImpl(MessageEnd msgEnd, Cluster toMsgEndOwnerCluster, Point location) {		
		Node msgEndNode = interactionGraph.getNodeFor(msgEnd);
		Message msg = msgEnd.getMessage();
		boolean isRecvEvent = msg.getReceiveEvent() == msgEnd; 
		Link link = interactionGraph.getLinkFor(msg);
		Cluster msgEndCluster = NodeUtilities.getLifelineNode(msgEndNode);
		if (msgEndCluster == null) {
			msgEndCluster = msgEndNode.getParent();
		}
		boolean isChangingOwner = msgEndCluster != null && msgEndCluster != toMsgEndOwnerCluster;
				
		Node target = link.getTarget();
		Node source = link.getSource();

		int selfMsgSpace = 0;
		if (NodeUtilities.isSelfLink(link) && !isChangingOwner)
			selfMsgSpace = interactionGraph.getGridSpacing();

		Point newLoc = location.getCopy();
		List<Node> nodes = isRecvEvent ? NodeUtilities.getBlock(source) : Arrays.asList(source);
		if (isRecvEvent) {
			nodes.remove(source);
		} 
		
		Rectangle totalArea = NodeUtilities.getArea(Arrays.asList(msgEndNode));
		totalArea.setLocation(newLoc);
		Link otherLink = isRecvEvent ? NodeUtilities.getStartLink(link) : NodeUtilities.getFinishLink(link);
		
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
		
		NodeUtilities.moveNodeBlock(interactionGraph, nodes, totalArea.y, moveToLifelines);

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
			interactionGraph.layout();
		}
		
		// Re-target reply msg.
		if (otherLink != null) {
			Node otherEndNode = isRecvEvent ? otherLink.getSource() : otherLink.getTarget();
			List<Node> otherNodes = Arrays.asList(otherEndNode);
			Rectangle otherTotalArea = NodeUtilities.getArea(otherNodes);
			Map<Node, Cluster> otherMoveToLifelines = new HashMap<>();
			otherMoveToLifelines.put(otherEndNode, toMsgEndOwnerCluster);					
			boolean selfMsg = NodeUtilities.getLifelineNode(isRecvEvent ? otherLink.getTarget() : otherLink.getSource()) == toMsgEndOwnerCluster;
			NodeUtilities.moveNodeBlock(interactionGraph,otherNodes, otherTotalArea.y + 
					(selfMsg ? interactionGraph.getGridSpacing() : 0), otherMoveToLifelines);		
			
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
		interactionGraph.disableLayout();
		try {
			NodeUtilities.reArrangeSelfMessages(interactionGraph, previousSelfLinks, selfLinks);
		} finally {
			interactionGraph.enableLayout();					
			interactionGraph.layout();
		}
		
		return true;
	}
	
	public boolean canMoveMessage(Message msg, Point moveDelta) {
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
			return false;
		}
				
		if (msg.getMessageSort() == MessageSort.REPLY_LITERAL) {
			if (!canMoveReplyMessage(msg, moveDelta))
				return false;
		}
		return true;
	}
	
	public void moveMessage(Message msg, Point moveDelta) {
		if (msg.getMessageSort() == MessageSort.REPLY_LITERAL) {
			if (moveReplyMessage(msg, moveDelta))
				return;
		}
		
		Link link = interactionGraph.getLinkFor(msg);
		Node source = link.getSource();
		List<Node> nodes = NodeUtilities.getBlock(source);
		Rectangle r = NodeUtilities.getArea(nodes);		
		NodeUtilities.moveNodeBlock(interactionGraph, nodes, r.y + moveDelta.y);
	}
	
	
	private boolean canMoveReplyMessage(Message msg, Point moveDelta) {
		Link link = interactionGraph.getLinkFor(msg);
		Node source = link.getSource();		

		Cluster parentSrc = source.getParent();
		if (parentSrc == null || !(parentSrc.getElement() instanceof ExecutionSpecification))
			return false;
		
		int newY = source.getBounds().y + moveDelta.y;
		if (parentSrc.getBounds().y >= newY) {
			return false;
		}

		// Resize the exec Spec to include or remove nodes.		
		// Check new pos is inside the same parent's parent => Before a node inside parent's parent.
		Point newSrcPt = source.getBounds().getCenter().getCopy();
		newSrcPt.translate(moveDelta);
		
		Cluster srcParent = source.getParent();
		Cluster srcLifelineNode = NodeUtilities.getLifelineNode(srcParent);
		Cluster newSrcParent = NodeUtilities.getClusterAtVerticalPos(srcLifelineNode, newSrcPt.y);					
		if (newSrcParent != srcParent.getParent() && moveDelta.y > 0) {
			return false;
		}
		
		if (newSrcParent != srcParent && moveDelta.y <= 0) {
			return false;
		}
		
		Node target = link.getTarget();
		Point newTrgPt = target.getBounds().getCenter().getCopy();
		newTrgPt.translate(moveDelta);
		Cluster trgParent = target.getParent();
		Cluster trgLifelineNode = NodeUtilities.getLifelineNode(trgParent);
		Cluster newTrgParent = NodeUtilities.getClusterAtVerticalPos(trgLifelineNode, newTrgPt.y);
		if (newTrgParent != trgParent) {
			return false;
		}			
		return true;
	}
	
	private boolean moveReplyMessage(Message msg, Point moveDelta) {
		Link link = interactionGraph.getLinkFor(msg);
		Node source = link.getSource();		
		Node target = link.getSource();		

		Cluster trgParent = target.getParent();
		Cluster srcParent = source.getParent();
		Point newTrgPt = target.getBounds().getCenter().getCopy();
		
		newTrgPt.translate(moveDelta);

		int gridSpacing = interactionGraph.getGridSpacing();
		try {
			// Check for padding overlapping			
			interactionGraph.disableLayout();
			
			// Default nudge if required.							
			List<Node> nodesAfter= NodeUtilities.getNodesAfterVerticalPos(interactionGraph, newTrgPt.y-3);
			nodesAfter.remove(link.getSource());
			nodesAfter.remove(link.getTarget());
			if (!nodesAfter.isEmpty()) {
				Node nextNode = nodesAfter.get(0);
				if (nextNode.getBounds().y - newTrgPt.y < 3)
					NodeUtilities.nudgeNodes(nodesAfter, 0, gridSpacing);
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
			NodeUtilities.moveNodes(interactionGraph, Collections.singletonList(target), trgParent, insertBeforeTrgNode, newTrgPt.y);
									
		} finally {
			interactionGraph.enableLayout();
			interactionGraph.layout();
		}
		return true;
	}
	
	public boolean canNudgeGate(Gate gate, Point location) {		
		return canNudgeMessageEnd(gate, location);
	}

	public void nudgeGate(Gate gate, Point location) {		
		nudgeMessageEnd(gate, location);
	}
	
	public boolean canMoveGate(Gate gate, InteractionFragment intFragment, Point location) {
		if (!CombinedFragment.class.isInstance(intFragment) && !InteractionOperand.class.isInstance(intFragment) &&
			!Interaction.class.isInstance(intFragment) && !InteractionUse.class.isInstance(intFragment)) {
			return false;
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
			selfMsgSpace = interactionGraph.getGridSpacing();

		Point newLoc = location.getCopy();
		if (isRecvEvent) {
			if (newLoc.y < (source.getBounds().y + selfMsgSpace)) {
				return false;
			}				
		} else {
			if (newLoc.y > (target.getBounds().y - selfMsgSpace)) {
				return false;
			}							
		}

		if (isChangingOwner) {
			if (!(toGateOwnerNode instanceof FragmentCluster)) {
				return false;
			}			
		}		
		
		Rectangle ownerArea = toGateOwnerNode.getBounds();
		int minY = ownerArea.y;
		int maxY = ownerArea.y + ownerArea.height - 1;
		
		List<Node> nodes = isRecvEvent ? NodeUtilities.getBlock(source) : Arrays.asList(source);
		if (isRecvEvent) {
			nodes.remove(source);
		} 

		Rectangle totalArea = NodeUtilities.getArea(Arrays.asList(msgEndNode));
		totalArea.setLocation(newLoc);
		if (totalArea.y <= minY || totalArea.y >= maxY) {
			return false;
		}
		
		return canMoveMessageEndImpl(gate, toGateOwnerNode, location);
	}

	
	public boolean moveGate(Gate gate, InteractionFragment intFragment, Point location) {		
		Cluster toGateOwnerNode = interactionGraph.getClusterFor(intFragment);
		Point newLoc = location.getCopy();
		Node msgEndNode = interactionGraph.getNodeFor(gate);

		if (!moveMessageEndImpl(gate, toGateOwnerNode, newLoc))
			return false;
		
		Rectangle r = msgEndNode.getBounds();
		if (r.x != newLoc.x) {
			msgEndNode.getBounds().x = newLoc.x;
			interactionGraph.layout();
		}
		return true;
	}


	public boolean canNudgeExecutionSpecification(ExecutionSpecification execSpec, int delta) {
		Cluster execSpecNode = interactionGraph.getClusterFor(execSpec);
		Node occurrenceNode = execSpecNode.getNodes().get(0);
		Element occSpec = occurrenceNode.getElement();
		if (!(occSpec instanceof MessageEnd)) 
			return false;
		
		return canNudgeMessage(((MessageEnd) occSpec).getMessage(), new Point(0,delta));
	}
	
	public void nudgeExecutionSpecification(ExecutionSpecification execSpec, int delta) {
		Cluster execSpecNode = interactionGraph.getClusterFor(execSpec);
		Node occurrenceNode = execSpecNode.getNodes().get(0);
		Element occSpec = occurrenceNode.getElement();
		nudgeMessage(((MessageEnd) occSpec).getMessage(), new Point(0,delta));
	}

	public boolean canResizeExecutionSpecification(ExecutionSpecification execSpec, boolean topSide, int delta) {
		Cluster execSpecNode = interactionGraph.getClusterFor(execSpec);
		Node occurrenceNode = topSide ? execSpecNode.getNodes().get(0) : execSpecNode.getNodes().get(execSpecNode.getNodes().size()-1);
		Element occSpec = occurrenceNode.getElement();
		if (!(occSpec instanceof MessageEnd))
			return false;

		if (topSide) {
			Node after = NodeUtilities.getNodesAfter(interactionGraph,Collections.singletonList(occurrenceNode)).
					stream().filter(d->d.getElement() != execSpec).findFirst().orElse(null);
			if (after == null || occurrenceNode.getBounds().y+delta >= after.getBounds().y) {
				return false;
			}			
		}

		return true;
	}

	public void resizeExecutionSpecification(ExecutionSpecification execSpec, boolean topSide, int delta) {
		Cluster execSpecNode = interactionGraph.getClusterFor(execSpec);
		Node occurrenceNode = topSide ? execSpecNode.getNodes().get(0) : execSpecNode.getNodes().get(execSpecNode.getNodes().size()-1);
		Element occSpec = occurrenceNode.getElement();
		nudgeMessage(((MessageEnd) occSpec).getMessage(), new Point(0,delta));

		if (topSide) {
			List<Row> rows = interactionGraph.getRows().stream().filter(d -> (d.getIndex() > occurrenceNode.getRow().getIndex()))
					.collect(Collectors.toList());
			NodeUtilities.nudgeRows(rows, -delta);
			interactionGraph.layout();
		}
	}

	public boolean canMoveExecutionSpecification(ExecutionSpecification execSpec, Lifeline lifeline, Point point) {
		Cluster execSpecNode = interactionGraph.getClusterFor(execSpec);
		Node occurrenceNode = execSpecNode.getNodes().get(0);
		Element occSpec = occurrenceNode.getElement();
		if (occSpec instanceof MessageEnd) {
			if (lifeline == NodeUtilities.getLifelineNode(execSpecNode).getElement()) {
				Dimension delta = point.getDifference(occurrenceNode.getBounds().getTopLeft());
				return canMoveMessage(((MessageEnd) occSpec).getMessage(), new Point(0,delta.height));
			} else {
				return canMoveMessageOccurrenceSpecification((MessageOccurrenceSpecification)occSpec, lifeline, point);
			}
		} else {
			return false;
		}				
	}
	
	public boolean moveExecutionSpecification(ExecutionSpecification execSpec, Lifeline lifeline, Point point) {
		Cluster execSpecNode = interactionGraph.getClusterFor(execSpec);
		Node occurrenceNode = execSpecNode.getNodes().get(0);
		Element occSpec = occurrenceNode.getElement();
		if (occSpec instanceof MessageEnd) {
			if (lifeline == NodeUtilities.getLifelineNode(execSpecNode).getElement()) {
				Dimension delta = point.getDifference(occurrenceNode.getBounds().getTopLeft());
				moveMessage(((MessageEnd) occSpec).getMessage(), new Point(0,delta.height));
				return true;
			} else {
				return moveMessageOccurrenceSpecification((MessageOccurrenceSpecification)occSpec, lifeline, point);
			}
		} else {
			throw new UnsupportedOperationException("Need to implement Nudge for ExecSpecOcurrence");
		}				
	}

	public boolean canMoveExecutionSpecificationOccurrence(ExecutionSpecification execSpec, OccurrenceSpecification occurrenceSpec, Point point) {
		if (execSpec.getStart() != occurrenceSpec && execSpec.getFinish() != occurrenceSpec) {
			return false;		
		}	
		
		// Check limit to move to the parent / lifeline bounds 
		Node occurSpecNode = interactionGraph.getNodeFor(occurrenceSpec);
		Cluster execSpecNode = interactionGraph.getClusterFor(execSpec);
		Node execSpecParentNode = execSpecNode.getParent();
		Rectangle parentLimits = execSpecParentNode.getBounds();
		if (point.y <= parentLimits.y || point.y >= parentLimits.y + parentLimits.height) {
			return false;		
		}			
		
		boolean startOccur = execSpec.getStart() == occurrenceSpec;
		OccurrenceSpecification other = startOccur ? execSpec.getFinish() : execSpec.getStart();
		Node otherNode = interactionGraph.getNodeFor(other);
		Rectangle otherBounds = otherNode.getBounds();
		if ((startOccur && point.y >= otherBounds.y) || (!startOccur && point.y <= otherBounds.y)) {
			return false;		
		}			
		// Start & End will have same parent
		Cluster lifelineNode = NodeUtilities.getLifelineNode(occurSpecNode);
		Cluster newParent = NodeUtilities.getClusterAtVerticalPos(lifelineNode, point.y);
		if (newParent != execSpecNode.getParent() && newParent != execSpecNode) {
			return false;		
		}

		
		Rectangle lifelineArea = ViewUtilities.getClientAreaBounds(interactionGraph.getEditPartViewer(), 
				NodeUtilities.getLifelineNode(lifelineNode).getView());
		if (!Draw2dUtils.contains(lifelineArea,point)) {
			return false;		
		}

		if (occurrenceSpec instanceof MessageEnd) {
			Node sendMessageNode = startOccur ? occurSpecNode.getParent().getConnectedByLink().getSource() : occurSpecNode.getConnectedByLink().getTarget();
			MessageEnd msgEnd = (MessageEnd)sendMessageNode.getElement();
			Point newPos = sendMessageNode.getLocation().getCopy();
			newPos.y = point.y;
			if (!canMoveMessageEndImpl(msgEnd, NodeUtilities.getLifelineNode(sendMessageNode), newPos))
				return false;
		}
		
		return true;
	}

	public boolean moveExecutionSpecificationOccurrence(ExecutionSpecification execSpec, OccurrenceSpecification occurrenceSpec, Point point) {
		Node occurSpecNode = interactionGraph.getNodeFor(occurrenceSpec);
		Cluster execSpecNode = interactionGraph.getClusterFor(execSpec);
		boolean startOccur = execSpec.getStart() == occurrenceSpec;

		NodeImpl node = (NodeImpl)occurSpecNode; 
		List<Node> nodesAfter= NodeUtilities.getNodesAfterVerticalPos(interactionGraph, point.y-3);
		if (!nodesAfter.isEmpty()) {
			Node nextNode = nodesAfter.get(0);
			if (nextNode.getBounds().y - point.y < 3)
				NodeUtilities.nudgeNodes(nodesAfter, 0, interactionGraph.getGridSpacing());
		}
		
		Rectangle r = node.getBounds();
		if (startOccur) {
			resizeCluster((ClusterImpl)execSpecNode, r.y - point.y, 0);
		} else {
			resizeCluster((ClusterImpl)execSpecNode, 0, point.y - (r.y + r.height));
		}

		if (occurrenceSpec instanceof MessageEnd) {
			Node sendMessageNode = startOccur ? occurSpecNode.getParent().getConnectedByLink().getSource() : occurSpecNode.getConnectedByLink().getTarget();
			MessageEnd msgEnd = (MessageEnd)sendMessageNode.getElement();
			Point newPos = sendMessageNode.getLocation().getCopy();
			newPos.y = point.y;

			// TODO: @ etxacam Needs to nudge on overlap
			return moveMessageEndImpl(msgEnd, NodeUtilities.getLifelineNode(sendMessageNode), newPos);
		}

		return true;
	}

	public boolean canDeleteExecutionSpecification(ExecutionSpecification execSpec) {
		Cluster execSpecNode = interactionGraph.getClusterFor(execSpec);
		Node occurrenceNode = execSpecNode.getNodes().get(0);
		Element occSpec = occurrenceNode.getElement();
		if (!(occSpec instanceof MessageEnd)) {
			return false;
		} 		
		return true;
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
	
	public boolean canAddInteractionUse(CreateElementRequestAdapter elementAdapter, ViewDescriptor descriptor, Rectangle rect) {
		return true;
	}

	public Cluster addInteractionUse(CreateElementRequestAdapter elementAdapter, ViewDescriptor descriptor, Rectangle rect) {
		InteractionUse interactionUse = SemanticElementsService.createElement(
				editingDomain, interactionGraph.getInteraction(), 
				UMLElementTypes.InteractionUse_Shape);
		
		int posY = rect.y;				
		List<Lifeline> lifelines = new ArrayList<>(); 
		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		for (Cluster lifelineCluster : interactionGraph.getLifelineClusters()) {
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
		newRect.height = interactionGraph.getGridSpacing(40);
		
		Node nodeInsertBefore = NodeUtilities.getNodeAfterVerticalPos(interactionGraph, posY-3);
		
		// Make place for it...
		if (nodeInsertBefore != null) {
			Row r = nodeInsertBefore.getRow();
			List<Row> nudgeRows = interactionGraph.getRows().stream().filter(d -> (d.getIndex() >= r.getIndex()))
				.collect(Collectors.toList());
			NodeUtilities.nudgeRows(nudgeRows, newRect.height);
			interactionGraph.layout();
		}

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
		
		interactionGraph.layout();
		return interactionUseCluster;
	}

	public boolean canNudgeInteractionUse(InteractionUse intUse, Point delta) {
		FragmentCluster fragmentCluster = (FragmentCluster)interactionGraph.getClusterFor(intUse);		
		List<Node> startNodes = fragmentCluster.getClusters().stream().map(d->d.getNodes().get(0)).collect(Collectors.toList());
		Rectangle validArea = NodeUtilities.getNudgeArea(interactionGraph, startNodes, false, true);		
		Rectangle newMsgArea = fragmentCluster.getBounds().getCopy().translate(delta);
		if (!Draw2dUtils.contains(validArea,newMsgArea)) {
			return false;
		}
		return true;
	}
	
	public void nudgeInteractionUse(InteractionUse intUse, Point delta) {
		FragmentCluster fragmentCluster = (FragmentCluster)interactionGraph.getClusterFor(intUse);		
		List<Node> startNodes = fragmentCluster.getClusters().stream().map(d->d.getNodes().get(0)).collect(Collectors.toList());
		Row r = startNodes.get(0).getRow();
		List<Row> nudgeRows = interactionGraph.getRows().stream().filter(d -> (d.getIndex() >= r.getIndex()))
			.collect(Collectors.toList());
		NodeUtilities.nudgeRows(nudgeRows, delta.y);
		interactionGraph.layout();
	}


	public boolean canNudgeResizeInteractionUse(InteractionUse intUse, Rectangle rect) {
		FragmentClusterImpl fragmentCluster = (FragmentClusterImpl)interactionGraph.getClusterFor(intUse);
		Dimension minSize = interactionGraph.getLayoutManager().getMinimumSize(fragmentCluster);
		if (minSize.width > rect.width || minSize.height > rect.height) {
			return false; 
		}

		List<Node> allNodes = NodeUtilities.flatten(fragmentCluster.getClusters()); 
		allNodes.addAll(fragmentCluster.getAllGates());
		Rectangle validArea = NodeUtilities.getEmptyAreaAround(interactionGraph, allNodes).shrink(2, 2);		
		if (rect.x < validArea.x || rect.right() > validArea.right()) {
			return false; 
		}

		Rectangle currentRect = fragmentCluster.getBounds();
		if (currentRect.y != rect.y) {
			Rectangle nudgeArea = NodeUtilities.getEmptyAreaAround(interactionGraph, fragmentCluster.getStartMarkNodes());
			// Check top borders
			if (nudgeArea.y >= rect.y || rect.y >= nudgeArea.bottom()) {  
				return false; 
			}				
		}
		
		if (currentRect.bottom() != rect.bottom()) {
			Rectangle nudgeArea = NodeUtilities.getNudgeArea(interactionGraph, fragmentCluster.getEndMarkNodes(), false, true);
			// Check top borders
			if (nudgeArea.y >= rect.bottom() || rect.bottom() >= nudgeArea.bottom()) {  
				return false; 
			}							
		}
		
		List<Cluster> lifelines = NodeUtilities.getIntersectingLifelineLines(interactionGraph,rect);
		if (lifelines.size() == 0) {
			return false; 
		}
		
		return true;
	}

	public void nudgeResizeInteractionUse(InteractionUse intUse, Rectangle rect) {
		FragmentClusterImpl fragmentCluster = (FragmentClusterImpl)interactionGraph.getClusterFor(intUse);
		List<Cluster> lifelines = NodeUtilities.getIntersectingLifelineLines(interactionGraph,rect);
		List<Cluster> curLifelines = fragmentCluster.getClusters().stream().map(NodeUtilities::getLifelineNode).
				collect(Collectors.toList());
		
		int newBottom = rect.bottom();
		int bottom = fragmentCluster.getBounds().bottom(); 
		if ( bottom != newBottom) {
			// Nudge nodes if bottom position change.
			List<Node> nudgeNodes = NodeUtilities.getNodesAfterVerticalPos(interactionGraph, bottom);
			nudgeNodes.addAll(fragmentCluster.getEndMarkNodes());
			NodeUtilities.nudgeNodes(nudgeNodes, 0, newBottom - bottom);
		}
		
		
		// Remove old marks
		List<Cluster> clusterToDelete = fragmentCluster.getClusters().stream().
				filter(d->!lifelines.contains(NodeUtilities.getLifelineNode(d))).collect(Collectors.toList());
		NodeUtilities.removeNodes((InteractionGraphImpl)interactionGraph, clusterToDelete);
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
		
		interactionGraph.layout();
	}

	public boolean canResizeInteractionUse(InteractionUse intUse, Rectangle rect) {
		FragmentClusterImpl fragmentCluster = (FragmentClusterImpl)interactionGraph.getClusterFor(intUse);		

		Dimension minSize = interactionGraph.getLayoutManager().getMinimumSize(fragmentCluster);
		if (minSize.width > rect.width || minSize.height > rect.height) {
			return false;
		}

		List<Node> allNodes = NodeUtilities.flatten(fragmentCluster.getClusters()); 
		allNodes.addAll(fragmentCluster.getAllGates());

		List<Cluster> lifelines = NodeUtilities.getIntersectingLifelineLines(interactionGraph,rect);
		if (lifelines.size() == 0) {
			return false;
		}

		for (Cluster lf : lifelines) {
			Node newStartParent = NodeUtilities.getClusterAtVerticalPos(lf, rect.y());
			if (fragmentCluster.getClusters().contains(newStartParent))
				newStartParent = newStartParent.getParent();
			Node newEndParent = NodeUtilities.getClusterAtVerticalPos(lf, rect.bottom());			
			if (fragmentCluster.getClusters().contains(newEndParent))
				newEndParent = newEndParent.getParent();
			
			if (newStartParent != newEndParent) {
				return false;
			}
		}
		
		List<Node> nodes = NodeUtilities.getNodesAfterVerticalPos(interactionGraph, rect.y-2);
		nodes.removeAll(allNodes);
		nodes.removeAll(NodeUtilities.getNodesAfterVerticalPos(interactionGraph, rect.bottom()));
		if (!nodes.isEmpty()) {
			Node overlappingNode = nodes.stream().filter(d->lifelines.contains(NodeUtilities.getLifelineNode(d))).findFirst().orElse(null);
			if (overlappingNode != null) {
				return false;
			}
		}		

		List<Node> gates = fragmentCluster.getAllGates();
		int maxY = gates.stream().map(d->d.getBounds().getCenter().y + 10).max(Integer::compareTo).orElse(minSize.height + fragmentCluster.getBounds().y);
		int minY = gates.stream().map(d->d.getBounds().getCenter().y - 10).min(Integer::compareTo).orElse(fragmentCluster.getBounds().y);
		if (rect.y > minY || rect.bottom() < maxY) {
			return false;
		}
		return true;
	}

	public void resizeInteractionUse(InteractionUse intUse, Rectangle rect) {
		FragmentClusterImpl fragmentCluster = (FragmentClusterImpl)interactionGraph.getClusterFor(intUse);		
		List<Cluster> lifelines = NodeUtilities.getIntersectingLifelineLines(interactionGraph,rect);

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
		NodeUtilities.removeNodes((InteractionGraphImpl)interactionGraph, clusterToDelete);
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
		
		interactionGraph.layout();
	}

	public boolean canMoveInteractionUse(InteractionUse intUse, Rectangle rect) {
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
			return false;
		}
		return true;
	}

	public void moveInteractionUse(InteractionUse intUse, Rectangle rect) {
		FragmentCluster fragmentCluster = (FragmentCluster)interactionGraph.getClusterFor(intUse);		
		List<Node> nodes = NodeUtilities.getBlock(fragmentCluster);
		Rectangle totalArea = NodeUtilities.getArea(nodes);
		NodeUtilities.moveNodeBlock(interactionGraph,nodes, totalArea.y); // We moved to the 
	}

	public boolean canDeleteInteractionUse(InteractionUse intUse) {
		return true;
	}
		
	public void deleteInteractionUse(InteractionUse intUse) {
		FragmentCluster intUseNode = (FragmentCluster)interactionGraph.getClusterFor(intUse);
		List<Node> blockToDelete = NodeUtilities.getBlock(intUseNode);
		List<Link> linksToDelete = new ArrayList<Link>(NodeUtilities.flattenKeepClusters(blockToDelete).stream().map(Node::getConnectedByLink).
				filter(Predicate.isEqual(null).negate()).collect(Collectors.toSet()));
		try {
			// Remove nodes & Messages
			interactionGraph.disableLayout();
			NodeUtilities.removeMessageLinks(interactionGraph, linksToDelete);
		} finally {
			interactionGraph.enableLayout();
		}
		
		try {
			NodeUtilities.removeNodeBlock(interactionGraph,blockToDelete);		
			((InteractionGraphImpl)interactionGraph).removeFragmentCluster(intUseNode);
			interactionGraph.disableLayout();
			NodeUtilities.removeNodes(interactionGraph, intUseNode.getClusters());
		} finally {
			interactionGraph.enableLayout();
			interactionGraph.layout();					
		}
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
	
	public Cluster getMessageEndOwnerCluster(MessageEnd msgEnd, Element owner) {
		if (msgEnd instanceof Gate) {
			if (owner instanceof Interaction) {
				return interactionGraph;
			} else if (owner instanceof InteractionUse) {
				return interactionGraph.getClusterFor(owner);
			}
		} 
		return interactionGraph.getClusterFor(owner);
	}
	
	private InteractionGraphImpl interactionGraph;
	private TransactionalEditingDomain editingDomain;
}
