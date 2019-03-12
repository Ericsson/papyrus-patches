/*****************************************************************************
 * (c) Copyright 2018 Telefonaktiebolaget LM Ericsson
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) antonio.campesino.robles@ericsson.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Column;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.GraphItem;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Link;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.MarkNode.Kind;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Row;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.OccurrenceSpecification;

public class InteractionGraphImpl extends FragmentClusterImpl implements InteractionGraph {
	public InteractionGraphImpl(Interaction interaction, Diagram diagram, GraphicalEditPart editPart) {
		super(interaction);
		super.setView(diagram);
		super.setEditPart(editPart);

		viewer = editPart == null ? null : editPart.getViewer();
	}

	@Override
	public InteractionGraphImpl getInteractionGraph() {
		return this;
	}

	InteractionGraphBuilder getBuilder() {
		return builder;
	}

	void setBuilder(InteractionGraphBuilder builder) {
		this.builder = builder;
	}

	@Override
	public Diagram getDiagram() {
		return (Diagram) getView();
	}
	
	@Override
	public EditPartViewer getEditPartViewer() {
		return viewer;
	}

	@Override
	public void setView(View vw) {
	}

	@Override
	Rectangle extractBounds() {
		return ViewUtilities.getBounds(getViewer(), ViewUtilities.getViewWithType(getView(), InteractionEditPart.VISUAL_ID)).getCopy();
	}

	@Override
	public Interaction getInteraction() {
		return (Interaction) super.getElement();
	}

	public View getInteractionView() {
		return ViewUtilities.getViewForElement(getView(), super.getElement());
	}

	@Override
	public List<Cluster> getLifelineClusters() {
		return Collections.unmodifiableList(lifelineClusters);
	}

	public void removeLifelineCluster(ClusterImpl lifeline) {
		lifelineClusters.remove(lifeline);
		lifeline.setParent(null);		
	}
	
	public void addLifelineCluster(ClusterImpl lifeline, ClusterImpl insertBeforeCluster) {
		int indexNext = insertBeforeCluster == null ? -1 : lifelineClusters.indexOf(insertBeforeCluster);
		if (indexNext == -1) {
			lifelineClusters.add(lifeline);
		} else {
			lifelineClusters.add(indexNext, lifeline);
		}
		lifeline.setParent(this);
	}

	@Override
	public List<FragmentCluster> getFragmentClusters() {
		return super.getOwnedFragmentClusters();
	}

	@Override
	public List<Node> getFormalGates() {
		return getInnerGates();
	}

	public void addFormalGate(NodeImpl formalGate) {
		super.addInnerGate(formalGate);
	}

	@Override
	public List<Link> getMessageLinks() {
		return Collections.unmodifiableList(messageLinks);
	}

	@Override
	public List<Node> getOrderedNodes() {
		return rows.stream().flatMap(d->d.getNodes().stream().filter(e->!(e.getElement() instanceof Lifeline))).
				filter(Node.class::isInstance).collect(Collectors.toList());
	}

	void addMessage(LinkImpl message, LinkImpl insertBeforeMessage) {
		int indexNext = insertBeforeMessage == null ? -1 : messageLinks.indexOf(insertBeforeMessage);
		if (indexNext == -1) {
			messageLinks.add(message);
		} else {
			messageLinks.add(indexNext, message);
		}
		message.setInteractionGraph(this);
	}
	
	void moveMessage(LinkImpl message, LinkImpl insertBeforeMessage) {
		messageLinks.remove(message);
		addMessage(message,insertBeforeMessage);
	}
	
	void removeMessage(LinkImpl message) {
		messageLinks.remove(message);
	}

	@Override
	public List<Row> getRows() {
		return Collections.unmodifiableList(rows);
	}

	@Override
	public List<Column> getColumns() {
		return Collections.unmodifiableList(columns);
	}

	@Override
	public NodeImpl getNodeFor(Element element) {
		return builder.getCacheNode(element);
	}

	@Override
	public ClusterImpl getClusterFor(Element element) {
		NodeImpl node = builder.getCacheNode(element);
		if (node == null)
			return null;
		
		if (node instanceof Cluster)
			return (ClusterImpl)node;
			
		node = node.getParent();
		if (node == null)
			return null;
		if (node.getElement() == element)
			return (ClusterImpl)node;
		return null;
	}

	@Override
	public LinkImpl getLinkFor(Element element) {
		return builder.getCacheLink(element);
	}

	@Override
	public GraphItem getItemFor(Element element) {
		GraphItem item = getNodeFor(element);
		if (item != null)
			return item;
		return getLinkFor(element);
	}

	InteractionLayoutManager getLayoutManager() {
		return layoutManager;
	}

	@Override
	public List<Node> getLayoutNodes() {
		return rows.stream().flatMap(r -> r.getNodes().stream()).collect(Collectors.toList());
	}
	
	@Override
	public void layout() {
		layoutGrid();
	}

	public void disableLayout() {
		disabledLayout ++;
	}

	public void enableLayout() {
		disabledLayout --;
		disabledLayout = Math.max(0, disabledLayout);
	}

	// TODO: @etxacam Use the node blocks to split or merge rows in the same Y pos (or under threshold)
	// TODO: @etxacam Remove the Layout manager and get the nodes provide it: So we can get the Origin of the node and recalculate 
	//                size and the pos based on rows and cols.
	@SuppressWarnings("unchecked")
	private void layoutGrid() {
		if (disabledLayout > 0)
			return;
		rows.clear();
		columns.clear();

		// 1-. Layout Lifelines -> Row(0)
		RowImpl row = new RowImpl(this);
		rows.add(row);
		row.addNodes(lifelineClusters);

		View lifelineContainer = ViewUtilities.getViewWithType(getInteractionView(), InteractionInteractionCompartmentEditPart.VISUAL_ID);
		Rectangle compRect = ViewUtilities.getClientAreaBounds(viewer, lifelineContainer);

		int y = compRect.y + ViewUtilities.ROW_PADDING + (ViewUtilities.LIFELINE_HEADER_HEIGHT / 2);
		row.setYPosition(y);

		List<NodeImpl> orderedNodes = getLifelineClusters().stream().flatMap(d -> NodeUtilities.flatten((ClusterImpl)d).stream()).
				map(NodeImpl.class::cast).sorted(RowImpl.NODE_FRAGMENT_COMPARATOR).collect(Collectors.toList());
		//NodeOrderResolver orderResolver = new NodeOrderResolver(this);
		
		RowImpl prevRow = null;
		NodeImpl prevNode = null;
		//List<NodeImpl> orderedNodes = orderResolver.getOrderedNodes();
		boolean isNewRow = true;
		for (int i = 0; i < orderedNodes.size(); i++) {
			NodeImpl node = orderedNodes.get(i);
			if (prevNode != null) {
				if (node.getBounds() != null && prevNode.getBounds() != null) {
					isNewRow =  (Math.abs(node.getBounds().y - prevNode.getBounds().y) > 3);
				} else {
					isNewRow = !NodeUtilities.areNodesHorizontallyConnected(prevNode, node) ||
							   !isNodeConnectedTo(node, prevRow.getNodes());
					if (prevNode.getConnectedNode() == node &&
							NodeUtilities.getLifelineNode(prevNode) == NodeUtilities.getLifelineNode(node)) {
						// Self Message
						isNewRow = true;
					}
				}
			}

			if (isNewRow) {
				prevRow = new RowImpl(this);
				rows.add(prevRow);
				if (node.getBounds() != null) {
					y = node.getBounds().getCenter().y;
					prevRow.setYPosition(y);
				}
				else
					prevRow.setYPosition(-1);
			}

			prevRow.addNode(node);			
			prevNode = node;
		}

		int prevX = Integer.MIN_VALUE;
		// Layout Columns
		for (ClusterImpl lfCluster : lifelineClusters) {
			ColumnImpl column = new ColumnImpl(this);
			columns.add(column);
			column.addNode(lfCluster);
			// Calculate Columns sizes....
			Rectangle r = lfCluster.getBounds();
			if (r != null) {
				int nudgeX = 0;
				if (prevX >= r.x && prevX <= r.x + r.width) {
					nudgeX += prevX - r.x + ViewUtilities.COL_PADDING;
				}
				int colX = r.getCenter().x + nudgeX;

				column.setXPosition(colX);
				prevX = r.x + r.width + nudgeX;
			}
			column.addNodes((List) lfCluster.getAllNodes());
		}

		ColumnImpl leftGatesColumn = null;
		ColumnImpl rightGatesColumn = null;

		Rectangle interactionBounds = ViewUtilities.getBounds(viewer, getInteractionView());

		// Actual Gates
		for (Node formalGate : getFormalGates()) {
			Node opposite = formalGate.getConnectedByNode();
			if (opposite == null) {
				opposite = formalGate.getConnectedNode();
			}

			int colIndex = opposite == null ? -1 : columns.indexOf(opposite.getColumn());
			View gateView = formalGate.getView();
			if (gateView != null && ViewUtilities.hasLayoutConstraints(formalGate.getView())) {
				Rectangle r = ViewUtilities.getBounds(viewer, gateView);
				if (Math.abs(r.getCenter().x - interactionBounds.getLeft().x) <= Math.abs(r.getCenter().x - interactionBounds.getRight().x)) {
					// left side
					colIndex = 0;
				} else {
					colIndex = columns.size();
				}
			}

			if (colIndex < ((columns.size() + 1) / 2)) {
				if (leftGatesColumn == null) {
					leftGatesColumn = new ColumnImpl(this);
				}
				leftGatesColumn.addNode((NodeImpl) formalGate);
				continue;
			} else {
				if (rightGatesColumn == null) {
					rightGatesColumn = new ColumnImpl(this);
				}
				rightGatesColumn.addNode((NodeImpl) formalGate);
			}

		}

		// Fragment Gates
		List<FragmentCluster> fragmentClusters = NodeUtilities.flatten(this);
		for (FragmentCluster cluster : fragmentClusters) {
			if (cluster == this) {
				continue;
			}
			List<Node> coveredLifelines = cluster.getClusters().stream().map(NodeImpl.class::cast).map(NodeUtilities::getLifelineNode).collect(Collectors.toList());
			Rectangle fragmentBounds = ViewUtilities.getBounds(viewer, cluster.getView());
			int min = coveredLifelines.stream().map(d -> columns.indexOf(d.getColumn())).min(Comparator.comparing(Integer::valueOf)).orElse(-1);
			int max = coveredLifelines.stream().map(d -> columns.indexOf(d.getColumn())).max(Comparator.comparing(Integer::valueOf)).orElse(-1);
			if (min == -1 || max == -1) {
				min = 0;
				max = 0;
			}
			for (Node gate : cluster.getAllGates()) {
				// TODO: Correlate in & out gates
				Node opposite = gate.getConnectedByNode();
				if (opposite == null) {
					opposite = gate.getConnectedNode();
				}

				int colIndex = opposite == null ? -1 : columns.indexOf(opposite.getColumn());
				View gateView = gate.getView();
				if (gateView != null && ViewUtilities.hasLayoutConstraints(gate.getView())) {
					Rectangle r = ViewUtilities.getBounds(viewer, gateView);
					if (Math.abs(r.getCenter().x - fragmentBounds.getLeft().x) <= Math.abs(r.getCenter().x - fragmentBounds.getRight().x)) {
						// left side
						colIndex = min;
					} else {
						colIndex = max;
					}
				}

				if (Math.abs(colIndex - min) <= Math.abs(colIndex - max)) {
					columns.get(min).addNode((NodeImpl) gate);
				} else {
					columns.get(max).addNode((NodeImpl) gate);
				}
			}
		}

		if (leftGatesColumn != null) {
			columns.add(0, leftGatesColumn);
		}

		if (rightGatesColumn != null) {
			columns.add(rightGatesColumn);
		}

		// Create Message:
		for (Link lnk : getMessageLinks()) {
			Message msg = (Message)lnk.getElement();
			if (msg == null)
				continue;
			
			if (msg.getMessageSort() == MessageSort.CREATE_MESSAGE_LITERAL) {
				NodeImpl trg = (NodeImpl)lnk.getTarget();
				ClusterImpl lifeline = (ClusterImpl)NodeUtilities.getLifelineNode(trg);
				if (lifeline != null) {
					lifeline.getRow().removeNode(lifeline);
					lifeline.setRow(trg.getRow());
					trg.getRow().addNode(lifeline);
				}
			}
		}
		
		// TODO: Columns & Rows for floating Nodes

		rows.stream().forEach(d -> d.setIndex(rows.indexOf(d)));
		columns.stream().forEach(d -> d.setIndex(columns.indexOf(d)));

		// TODO: Layout Y Positions.
		for (RowImpl r : rows) {
			// Order nodes in each row.
			r.nodes.sort(RowImpl.MESSAGE_END_NODE_COMPARATOR);
		}


		// TODO: Layout X Positions.
		for (ColumnImpl col : columns) {
			// int x = col.getXPosition();
			// col.nodes.stream().forEach(d -> d.horizontalLayout(x));
		}

		layoutManager.layout();
		
		int lastY = Math.max(300,rows.get(rows.size()-1).getYPosition()+40);
		// Set lifelines size equal.
		for (Cluster lifeline : lifelineClusters) {
			boolean destroyed = lifeline.getAllNodes().stream().filter(d -> (d.getElement() instanceof DestructionOccurrenceSpecification)).
				findFirst().orElse(null) != null; 
			if (!destroyed) {
				Rectangle r = lifeline.getBounds();
				r.height = lastY - r.y;
				r.setBounds(r);
			}
		}
		int lastX = 0;
		Rectangle allLifelinesRect = NodeUtilities.getArea((List)lifelineClusters);
		if (allLifelinesRect != null)
			lastX = allLifelinesRect.getRight().x;
		Rectangle allClusterRect = NodeUtilities.getArea((List)getFragmentClusters());
		if (allClusterRect != null)
			lastX = Math.max(allClusterRect.getRight().x, lastX);
		
		lastX = Math.max(lastX,300);
		
		Rectangle r = ViewUtilities.getBounds(getViewer(), ViewUtilities.getViewWithType(getView(), InteractionEditPart.VISUAL_ID)).getCopy();		
		r.height = lastY - r.y + 60;
		r.width = lastX - r.x + 60;		
		setBounds(r);
	}

	private boolean isNodeConnectedTo(NodeImpl node, List<Node> row) {
		for (Node prevNode : row) {
			if (NodeUtilities.isNodeConnectedTo((NodeImpl)prevNode, node) || 
				NodeUtilities.isNodeConnectedTo(node, (NodeImpl)prevNode)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public ClusterImpl getLifeline(Lifeline lifeline) {
		return lifelineClusters.stream().filter(d -> d.getElement() == lifeline).findFirst().orElse(null);
	}

	@Override
	public Cluster addLifeline(Lifeline lifeline) {
		return addLifeline(lifeline, null);
	}

	@Override
	public Cluster addLifeline(Lifeline lifeline, Cluster insertBefore) {
		ClusterImpl cluster = new ClusterImpl(lifeline);
		addLifelineCluster(cluster, (ClusterImpl) insertBefore);
		builder.nodeCache.put(lifeline, cluster);
		if (rows.size() == 0) {
			rows.add(new RowImpl(this));
		}
		layoutGrid();
		return cluster;
	}

	@Override
	public Cluster removeLifeline(Lifeline lifeline) {
		ClusterImpl cluster = getLifeline(lifeline);
		if (cluster != null) {
			lifelineClusters.remove(cluster);
			builder.nodeCache.remove(lifeline);
			layoutGrid();
		}

		return cluster;
	}

	@Override
	public void moveLifeline(Lifeline lifelineToMove, Lifeline beforeLifeline) {
		ClusterImpl lfToMove = getLifeline(lifelineToMove);
		ClusterImpl beforeLf = getLifeline(beforeLifeline);
		if (lfToMove == beforeLf) {
			return;
		}
		lifelineClusters.remove(lfToMove);
		if (beforeLf != null) {
			lifelineClusters.add(lifelineClusters.indexOf(beforeLf), lfToMove);
		} else {
			lifelineClusters.add(lfToMove);
		}

		layoutGrid();
	}

	@Override
	public LinkImpl getMessage(Message message) {
		return messageLinks.stream().filter(d -> d.getElement() == message).findFirst().orElse(null);
	}

	@Override
	public LinkImpl addMessage(Message message) {
		return addMessage(message, null);
	}

	@Override
	public LinkImpl addMessage(Message message, Link insertBefore) {
		LinkImpl link = new LinkImpl(message);
		addMessage(link, (LinkImpl) insertBefore);
		builder.nodeCache.put(message, link);
		layoutGrid();
		return link;
	}
	
	public LinkImpl removeMessage(Message message) {
		LinkImpl msgLink = getMessage(message);
		if (msgLink != null) {
			disableLayout();
			removeMessage(msgLink);
			removeNodeImpl(msgLink.getSource());
			removeNodeImpl(msgLink.getTarget());
			enableLayout();
			layout();
		}
		return msgLink;
	}

	// TODO: @etxacam Check if remove
	@Override
	public void moveMessage(Message message, Message insertBefore) {
		LinkImpl msgToMove = getMessage(message);
		LinkImpl beforeMsg = getMessage(insertBefore);
		if (msgToMove == beforeMsg) {
			return;
		}
		moveMessage(msgToMove, beforeMsg);
		layoutGrid();
	}

	
	// TODO: @etxacam Review what can be done by message API
	@Override
	public NodeImpl getMessageOccurrenceSpecification(Lifeline lifeline, MessageOccurrenceSpecification mos) {
		ClusterImpl lifelineCluster = getLifeline(lifeline);
		return NodeUtilities.flattenImpl(lifelineCluster).filter(d -> d.getElement() == mos).findFirst().orElse(null);
	}

	@Override
	public NodeImpl addMessageOccurrenceSpecification(Lifeline lifeline, MessageOccurrenceSpecification mos) {
		return addMessageOccurrenceSpecification(lifeline, mos, null);
	}
	
	@Override
	public NodeImpl addMessageOccurrenceSpecification(Lifeline lifeline, MessageOccurrenceSpecification mos, Node insertBefore) {
		NodeImpl mosNode = getMessageOccurrenceSpecification(lifeline, mos);
		if (mosNode != null) {
			return mosNode;
		}
		if (getNodeFor(mos) != null) {
			return null;
		}

		ClusterImpl lifelineCluster = getLifeline(lifeline);
		while (insertBefore != null && insertBefore.getParent() != lifelineCluster) {
			if (insertBefore.getParent().getNodes().indexOf(insertBefore) == 0) {
				insertBefore = insertBefore.getParent();
			} else {
				break;
			}
		}
		
		if (insertBefore != null)
			lifelineCluster = (ClusterImpl)insertBefore.getParent();
		
		NodeImpl node = new NodeImpl(mos);
		lifelineCluster.addNode(node, insertBefore);
		builder.nodeCache.put(mos, node);
		layoutGrid();
		return node;
	}

	@Override
	public Node removeMessageOccurrenceSpecification(Lifeline lifeline, MessageOccurrenceSpecification mos) {
		NodeImpl mosNode = getMessageOccurrenceSpecification(lifeline, mos);
		if (mosNode == null || mosNode.getParent() == null) {
			return null;
		}

		if (!removeNodeImpl(mosNode)) {
			return null;
		}
		layoutGrid();
		return mosNode;
	}

	@Override
	public boolean moveMessageOccurrenceSpecification(Lifeline lifeline, MessageOccurrenceSpecification mosToMove,
			Lifeline toLifeline, InteractionFragment fragmentBefore) {
		NodeImpl n = builder.getCacheNode(lifeline);
		if (n == null || !(n instanceof Cluster)) {
			return false;
		}
		ClusterImpl fromLifelineNode = (ClusterImpl) n;

		n = builder.getCacheNode(toLifeline);
		if (n == null || !(n instanceof Cluster)) {
			return false;
		}
		ClusterImpl toLifelineNode = (ClusterImpl) n;

		NodeImpl mosNode = builder.getCacheNode(mosToMove);
		if (mosNode == null) {
			return false;
		}
		n = builder.getCacheNode(fragmentBefore);

		// TODO: @etxacam Check if it is allowed to move the mos:
		// 1) SendEvent before receiveEvent
		// 2) Ends inside the same fragment.
		// 3) Special case may trigger change of gate kind (Inner <--> Outer).

		if (!moveNodeImpl(fromLifelineNode, mosNode, toLifelineNode, n)) {
			return false;
		}

		layoutGrid();
		return true;
	}

	@Override
	public Link connectMessageOcurrenceSpecification(MessageOccurrenceSpecification send, MessageOccurrenceSpecification recv) {
		NodeImpl sendNode = builder.getCacheNode(send);
		NodeImpl recvNode = builder.getCacheNode(recv);

		if (sendNode == null || recvNode == null) {
			return null;
		}

		boolean msgSend = true;
		LinkImpl link = builder.getCacheLink(send.getMessage());
		if (link == null) {
			msgSend = false;
			link = builder.getCacheLink(recv.getMessage());
		}
		
		if (link == null) {
			Message msg = msgSend ? send.getMessage() : recv.getMessage();
			link = new LinkImpl(msg);
			link.setSource(sendNode);
			link.setTarget(recvNode);		
			link.setEdge((Edge)ViewUtilities.getViewForElement(getDiagram(), msg));
			link.setInteractionGraph(this);
			addMessage(link, null);
			builder.nodeCache.put(msg, link);			
		}
		
		sendNode.connectNode(recvNode,link);
		layoutGrid();
		return link;
	}

	@Override
	public ClusterImpl getExecutionSpecification(Lifeline lifeline, ExecutionSpecification exec) {
		ClusterImpl lifelineCluster = getLifeline(lifeline);
		return NodeUtilities.flattenImpl(lifelineCluster).filter(d -> d.getElement() == exec).map(NodeImpl::getParent).findFirst().orElse(null);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ClusterImpl addExecutionSpecification(Lifeline lifeline, ExecutionSpecification exec) {
		ClusterImpl lifelineNode = getLifeline(lifeline);
		if (lifelineNode == null) {
			return null;
		}

		ClusterImpl execNode = getExecutionSpecification(lifeline, exec);
		if (execNode != null) {
			return execNode;
		}

		OccurrenceSpecification start = exec.getStart();
		OccurrenceSpecification finish = exec.getFinish();
		if (start == null || finish == null) {
			return null;
		}

		NodeImpl startNodeImpl = getNodeFor(start);
		NodeImpl finishNodeImpl = getNodeFor(finish);

		ClusterImpl parentCluster = lifelineNode;
		if (startNodeImpl != null && finishNodeImpl != null) {
			if (startNodeImpl.getParent() != finishNodeImpl.getParent()) {
				return null; // start and finish in different clusters.
			}

			parentCluster = startNodeImpl.getParent();
		}

		List<Node> clusterNodes = parentCluster.getNodes();
		int startNodeIndex = -1;
		if (startNodeImpl != null) {
			startNodeIndex = clusterNodes.indexOf(startNodeImpl);
			if (startNodeIndex == -1) {
				return null; // Occurrence in other lifeline
			}
		}

		int finishNodeIndex = -1;
		if (finishNodeImpl != null) {
			finishNodeIndex = clusterNodes.indexOf(finishNodeImpl);
			if (finishNodeIndex == -1) {
				return null; // Occurrence in other lifeline
			}
		}

		if (startNodeIndex > 0 && finishNodeIndex > 0 && finishNodeIndex < startNodeIndex) {
			return null; // finish located before start
		}

		List<NodeImpl> nodesToEncloseIn = new ArrayList<>();
		if (startNodeIndex > 0 && finishNodeIndex > 0) {
			nodesToEncloseIn.addAll((List) clusterNodes.subList(startNodeIndex, finishNodeIndex + 1));
		} else {
			if (startNodeImpl == null) {
				startNodeImpl = new NodeImpl(start);
				builder.nodeCache.put(start, startNodeImpl);
			}
			nodesToEncloseIn.add(startNodeImpl);

			if (finishNodeImpl == null) {
				finishNodeImpl = new NodeImpl(finish);
				builder.nodeCache.put(finish, finishNodeImpl);
			}
			nodesToEncloseIn.add(finishNodeImpl);
		}

		execNode = new ClusterImpl(exec);
		builder.nodeCache.put(exec, execNode);
		parentCluster.addNode(startNodeIndex, execNode);

		for (NodeImpl d : nodesToEncloseIn) {
			if (d != null) {
				parentCluster.removeNode(d);
			}
			execNode.addNode(d);
		}
		
		NodeImpl execMarkNode = new NodeImpl(exec);  
		execNode.addNode(1, execMarkNode);
		if (startNodeImpl.getBounds() != null)
			execMarkNode.setBounds(startNodeImpl.getBounds().getCopy());

		if (startNodeImpl.getConnectedByNode() != null) {
			startNodeImpl.getConnectedByNode().connectNode(execNode, null);
		} else if (startNodeImpl.getConnectedNode() != null) {
			execNode.connectNode(startNodeImpl.getConnectedNode(), null);
		}

		layoutGrid();

		return execNode;
	}

	@Override
	public ClusterImpl removeExecutionSpecification(Lifeline lifeline, ExecutionSpecification exec) {
		ClusterImpl lifelineNode = getLifeline(lifeline);
		if (lifelineNode == null) {
			return null;
		}

		ClusterImpl execNode = getExecutionSpecification(lifeline, exec);
		if (execNode == null) {
			return null;
		}

		if (!removeNodeImpl(execNode)) {
			return null;
		}
		layoutGrid();
		return execNode;
	}

	@Override
	public boolean moveExecutionSpecification(Lifeline lifeline, ExecutionSpecification execToMove,
			Lifeline toLifeline, InteractionFragment fragmentBefore) {
		ClusterImpl lifelineNode = getLifeline(lifeline);
		ClusterImpl execNode = getExecutionSpecification(lifeline, execToMove);
		ClusterImpl toLifelineNode = getLifeline(toLifeline);
		if (lifelineNode == null || toLifelineNode == null || execNode == null) {
			return false;
		}

		NodeImpl beforeNode = builder.getCacheNode(fragmentBefore);

		if (execNode.getConnectedByNode() != null) {
			// TODO: Check it is not going to a previous position of the triggering point
		}

		// TODO: check that it is not going between two nodes in the connected groups...
		// Can we use the grid???
		// Should we calculate connected nodes???

		if (!moveNodeImpl(lifelineNode, execNode, toLifelineNode, beforeNode)) {
			return false;
		}

		layoutGrid();
		return true;
	}

	@Override
	public boolean replaceExecutionSpecificationStart(ExecutionSpecification exec, OccurrenceSpecification ocurrSpec) {
		throw new UnsupportedOperationException();
	};

	@Override
	public boolean moveExecutionSpecificationStart(ExecutionSpecification ocurrSpec, InteractionFragment beforeFragment) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean replaceExecutionSpecificationFinish(ExecutionSpecification exec, OccurrenceSpecification ocurrSpec) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean moveExecutionSpecificationFinish(ExecutionSpecification exec, InteractionFragment beforeFragment) {
		throw new UnsupportedOperationException();
	}

	public FragmentClusterImpl addInteractionUse(InteractionUse interactionUse, List<Lifeline> lifelines, InteractionFragment beforeFragment) {
		NodeImpl beforeFragmentNode = getNodeFor(beforeFragment);
		List<Node> orderedNodes = getOrderedNodes();
		int index = orderedNodes.indexOf(beforeFragmentNode);
		orderedNodes = orderedNodes.subList(index, orderedNodes.size());
		FragmentClusterImpl fragmentCluster = new FragmentClusterImpl(interactionUse); 
		for (Lifeline lf : lifelines) {
			ClusterImpl lifelineCluster = getClusterFor(lf);
			NodeImpl insertBeforeNode = (NodeImpl)orderedNodes.stream().filter(d -> NodeUtilities.getLifelineNode(d) == lifelineCluster).
					findFirst().orElse(null);
			ClusterImpl insertBeforeNodeParent = insertBeforeNode == null ? lifelineCluster : insertBeforeNode.getParent();
			while (insertBeforeNodeParent != lifelineCluster && NodeUtilities.getStartNode(insertBeforeNodeParent) == insertBeforeNode) {
				insertBeforeNode = insertBeforeNodeParent; 
				insertBeforeNodeParent = insertBeforeNodeParent.getParent();
			}
			ClusterImpl intUseLfCluster = new ClusterImpl(interactionUse);
			intUseLfCluster.addNode(new MarkNodeImpl(Kind.start, interactionUse));
			intUseLfCluster.addNode(new MarkNodeImpl(Kind.end, interactionUse));
			
			insertBeforeNodeParent.addNode(intUseLfCluster,insertBeforeNode);
			fragmentCluster.addCluster(intUseLfCluster);
		}
		builder.nodeCache.put(interactionUse, fragmentCluster);
		addFragmentCluster(fragmentCluster);
		return fragmentCluster;
	}
	
	//////////////////////
	// END CHECK USE OF //
	//////////////////////	
	
	public void moveNodeBlock(List<Node> nodes, int yPos) {
		moveNodeBlock(nodes, yPos, Collections.emptyMap());
	}
	
	public void moveNodeBlock(List<Node> nodes, int yPos, Map<Node, Cluster> toCluters) {
		List<Node> allNodes = NodeUtilities.removeDuplicated(NodeUtilities.flattenKeepClusters(nodes));
		// 1) Calculate graph nodes in block area that are not part of the block
		List<Node> otherNodes = NodeUtilities.getBlockOtherNodes(nodes);
		
		// 2) Calculate insertion point after remove block and post insertion nudge ammount.
		Rectangle blockArea = NodeUtilities.getArea(nodes);
		Rectangle otherArea = NodeUtilities.getArea(otherNodes);
		
		int blockStartPoint = blockArea.y;
		int blockEndPoint = blockStartPoint + blockArea.height;
		int otherStartPoint = blockStartPoint;
		int otherEndPoint = blockEndPoint;
		if (otherArea != null) {
			otherStartPoint = otherArea.height != -1 ? otherArea.y : blockStartPoint;
			otherEndPoint = otherArea.height != -1 ?  (otherArea.y + otherArea.height) : blockEndPoint;
		}
		int height = blockEndPoint - blockStartPoint;
		int prev = otherStartPoint - blockStartPoint;
		int after = blockEndPoint - otherEndPoint; 
		
		int postInsertionNudge = 0;
		int newYPos = yPos;
		if (yPos > blockEndPoint) {
			// Insertion point after block
			if (otherArea != null) {
				newYPos -= prev;
				newYPos -= after;
			} else {
				newYPos -= height;
			}
		} else if (otherArea != null && yPos > otherEndPoint) {
			// Insertion point inside block after the other content
			newYPos = otherEndPoint - prev;
			postInsertionNudge = yPos - otherEndPoint;
		} else if (otherArea != null && yPos > otherStartPoint) {
			// Insertion point inside block inside the other content
			newYPos -= prev;
		} else if (yPos > blockStartPoint) {
			// Insertion point inside block before the other content
			newYPos = yPos;
			postInsertionNudge = yPos - blockStartPoint;
		}		
		
		Map<Cluster, List<Node>> nodesByLifeline = nodes.stream().collect(Collectors.groupingBy(
				d -> (toCluters.containsKey(d) ? toCluters.get(d) : NodeUtilities.getLifelineNode(d))));
		removeNodeBlockImpl(nodes,otherNodes,0);		
		addNodeBlock(nodesByLifeline, newYPos, postInsertionNudge);
	}
	
	public void removeNodeBlocks(List<List<Node>> blocks) {
		for (List<Node> block : blocks) {
			removeNodeBlock(block);
		}
	}
	
	public void removeNodeBlock(List<Node> nodes) {
		Rectangle r = NodeUtilities.getArea(nodes);
		Row row = NodeUtilities.getRowAt(this, r.y);
		Row prevRow = (row == null || row.getIndex() == 0) ? null : getRows().get(row.getIndex()-1);
		int nudge = prevRow == null ? 0 : row.getYPosition() - prevRow.getYPosition();
		List<Node> others = NodeUtilities.getBlockOtherNodes(nodes);
		removeNodeBlockImpl(nodes, others, (others != null && !others.isEmpty()) ? 0 : nudge);
		
		// TODO: @etxacam Remove all references from: Links, lifelines, clusters, etc... 
	}
	
	private void removeNodeBlockImpl(List<Node> nodes, List<Node> otherNodes, int extraNudge) {
		Rectangle blockArea = NodeUtilities.getArea(nodes);
		Rectangle otherArea = NodeUtilities.getArea(otherNodes);
		
		int blockStartPoint = blockArea.y;
		int blockEndPoint = blockStartPoint + blockArea.height;
		int otherStartPoint = blockStartPoint;
		int otherEndPoint = blockEndPoint;
		int prev = blockArea.height; 
		int after = 0;
		if (otherArea != null) {
			otherStartPoint = otherArea.height != -1 ? otherArea.y : blockStartPoint;
			otherEndPoint = otherArea.height != -1 ?  (otherArea.y + otherArea.height) : blockEndPoint;
			prev = otherStartPoint - blockStartPoint;
			after = blockEndPoint - otherEndPoint; 
		}
		List<Node> allNodes = NodeUtilities.removeDuplicated(NodeUtilities.flattenKeepClusters(nodes));
		int nudge = after + prev; 
		if (nudge <= 1) {
			nudge = 0;
		}
		nudge += extraNudge;
		
		disableLayout();
		try {
			NodeUtilities.removeNodes(this, nodes);			
			List<Node> nodesAfter = getLayoutNodes().stream().filter(d->!allNodes.contains(d) && d.getBounds().y >= blockEndPoint).collect(Collectors.toList());
			Rectangle nodesAfterArea = NodeUtilities.getArea(nodesAfter);
			
			// Check how much can we nudge up...
			Rectangle nudgeArea = NodeUtilities.getNudgeArea(this, otherNodes, false, true, allNodes);
			int maxNudgePrev = prev;
			if (nudgeArea != null && otherArea != null)
				maxNudgePrev = (otherArea.y - nudgeArea.y) / 20 * 20;
			
			nudgeArea = NodeUtilities.getNudgeArea(this, nodesAfter, false, true, allNodes);
			int maxNudgeAfter = nudge; 
			if (nudgeArea != null && otherArea != null)
				maxNudgeAfter = (nodesAfterArea.y - nudgeArea.y) / 20 * 20;

			NodeUtilities.nudgeNodes(otherNodes, 0, -Math.max(0, Math.min(prev, maxNudgePrev)));			
			NodeUtilities.nudgeNodes(nodesAfter, 0, -Math.max(0, Math.min(nudge, maxNudgeAfter)));
		} finally {
			enableLayout();
			layout();
		}
	}
	
	public void addNodeBlock(Map<Cluster,List<Node>> nodesByLifelines, int yPos) {
		addNodeBlock(nodesByLifelines, yPos, 0);
	}
	
	public void addNodeBlock(Map<Cluster,List<Node>> nodesByLifelines, int yPos, int extraNudge) {
		List<Node> nodes = nodesByLifelines.values().stream().flatMap(d->d.stream()).collect(Collectors.toList());
		//Map<Cluster,List<Node>> allNodes = NodeUtilities.flattenKeepClusters(nodes);
		Rectangle totalArea = NodeUtilities.getArea(nodes);
		List<Node> firstNodes = nodes.stream().filter(d->d.getBounds() != null && d.getBounds().y == totalArea.y).collect(Collectors.toList());		
		Row row = rows.stream().filter(d->Math.abs(yPos - d.getYPosition()) <= 3).findFirst().orElse(null);
		if (row != null) {
			boolean nudgeOverlap = true;
			for (Node n : firstNodes) {
				if (NodeUtilities.flatten(NodeUtilities.getBlock(n)).stream().anyMatch(row.getNodes()::contains)) {
					nudgeOverlap = false;
					break;
				}
//				NodeUtilities.nudgeNodes(Arrays.asList(n), 0,20);				
			}

			for (int r = row.getIndex(); nudgeOverlap && r<rows.size(); r++) {
				rows.get(r).nudge(20);
			}
		}

		// Make place for the block.
		List<Node> nodesAfter = getLayoutNodes().stream().filter(d->d.getBounds().y > yPos).collect(Collectors.toList());
		disableLayout();
		try {
			NodeUtilities.nudgeNodes(nodesAfter, 0, totalArea.height);
			NodeUtilities.nudgeNodes(nodesAfter, 0, extraNudge);
		} finally {
			enableLayout();
			layout();
		}
		
		disableLayout();
		try {
			for (Map.Entry<Cluster,List<Node>> lifelineEntry : nodesByLifelines.entrySet()) {
				Cluster lifelineCluster = lifelineEntry.getKey();
				List<Node> ns = lifelineEntry.getValue();
				List<Node> ans = NodeUtilities.removeDuplicated(NodeUtilities.flattenKeepClusters(ns)); 
				Rectangle lifelineNodesArea = NodeUtilities.getArea(ans);
				Node newPrevNode = lifelineCluster.getAllNodes().stream().filter(d->(d.getBounds().y < yPos)).
						filter(d->!ans.contains(d)).sorted(Collections.reverseOrder(RowImpl.NODE_VPOSITION_COMPARATOR)).findFirst().orElse(null);
				Cluster prevParent = newPrevNode != null ? newPrevNode.getParent() : null;
				if (prevParent != null && prevParent != lifelineCluster && prevParent.getNodes().indexOf(newPrevNode) == prevParent.getNodes().size() -1)
					newPrevNode = prevParent;
				
				Node insertBefore = lifelineCluster.getAllNodes().stream().filter(d->!ans.contains(d) && d.getBounds().y >= yPos).
						findFirst().orElse(null);
				Cluster insertBeforeParent = insertBefore != null ? insertBefore.getParent() : null;
				if (insertBeforeParent != null && insertBeforeParent != lifelineCluster && insertBeforeParent.getNodes().indexOf(insertBefore) == 0)
					insertBefore = insertBeforeParent;
	
				Cluster target = lifelineCluster;
				if (insertBefore != null) {
					target = insertBefore.getParent();
				}
	
				NodeUtilities.insertNodes(this, ns, target, insertBefore, yPos + (lifelineNodesArea.y - totalArea.y));					
			}
		} finally {
			enableLayout();
			layout();
		}	
	}

	public void reArrangeSelfMessages(List<Link> previousSelfLinks, List<Link> newSelfLinks) {
		List<Link> selfLinksToStraight = new ArrayList<>(previousSelfLinks);
		selfLinksToStraight.removeAll(newSelfLinks);
		
		List<Link> linksToSelfLinks = new ArrayList<>(newSelfLinks);
		linksToSelfLinks.removeAll(previousSelfLinks);
		
		// Nudge Target Links for new Self messages.
		List<Node> nodesToNudge = linksToSelfLinks.stream().filter(d -> NodeUtilities.getLinkSlope(d) < 3).map(Link::getTarget).
			map(d-> d instanceof Cluster ? ((Cluster)d).getNodes().get(0) : d).sorted(RowImpl.NODE_VPOSITION_COMPARATOR).
			collect(Collectors.toList());
		if (!nodesToNudge.isEmpty()) {
			int nudge = 0;
			Iterator<Node> it = nodesToNudge.iterator();
			Node nextNode = it.next();
			for (Node n : getOrderedNodes()) {
				if (nudge == 0)
				if (n == nextNode) {
					nudge += 20;
					nextNode = !it.hasNext() ? null : it.next();
				} 
				NodeUtilities.nudgeNodes(n, 0, nudge);
			}
		}
		
		List<Node> nodesToUnnudge = selfLinksToStraight.stream().filter(d -> NodeUtilities.getLinkSlope(d) >= 17).map(Link::getTarget).
				map(d-> d instanceof Cluster ? ((Cluster)d).getNodes().get(0) : d).sorted(RowImpl.NODE_VPOSITION_COMPARATOR).
				collect(Collectors.toList());
		if (!nodesToUnnudge.isEmpty()) {
			int nudge = 0;
			Iterator<Node> it = nodesToUnnudge.iterator();
			Node nextNode = it.next();
			for (Node n : getOrderedNodes()) {
				if (n == nextNode) {
					nudge -= 20;
					nextNode = !it.hasNext() ? null : it.next();
				} 
				NodeUtilities.nudgeNodes(n, 0, nudge);
			}			
		}
	}
	
	// TODO: @etxacam Check if remove
	private boolean moveNodeImpl(ClusterImpl fromCluster, NodeImpl node, ClusterImpl toCluster, NodeImpl before) {
		if (fromCluster == null || toCluster == null || node == null) {
			return false;
		}

		int index = fromCluster.getNodes().indexOf(node);
		if (index == -1) {
			return false;
		}

		if (before != null && toCluster.getNodes().indexOf(before) == -1) {
			return false;
		}

		// TODO: Handle before start of exec spec / fragment start mark
		if (node == before)
			return true;
		
		fromCluster.removeNode(index);

		index = before == null ? -1 : toCluster.getNodes().indexOf(before);
		if (index == -1) {
			toCluster.addNode(node);
		} else {
			toCluster.addNode(node, before);
		}

		return true;

	}


	private boolean removeNodeImpl(NodeImpl nodeImpl) {
		if (nodeImpl == null || nodeImpl.getParent() == null) {
			return true;
		}

		ClusterImpl parent = nodeImpl.getParent();
		parent.removeNode(nodeImpl);
		builder.nodeCache.remove(nodeImpl.getElement());

		if (nodeImpl instanceof Cluster) {
			new ArrayList<>(((Cluster) nodeImpl).getNodes()).stream().forEach(d -> removeNodeImpl((NodeImpl) d));
		}

		if (nodeImpl.getConnectedNode() != null) {
			removeNodeImpl(nodeImpl.getConnectedNode());
		}

		if (nodeImpl.getElement() instanceof MessageEnd && nodeImpl.getConnectedByNode() != null) {
			removeNodeImpl(nodeImpl.getConnectedByNode());
		}

		if (nodeImpl.getElement() instanceof ExecutionOccurrenceSpecification ||
				nodeImpl.getElement() instanceof ExecutionSpecification) {
			removeNodeImpl(parent);
		}

		return true;
	}

	private int disabledLayout = 0; 
	private InteractionGraphBuilder builder;
	private EditPartViewer viewer;
	private List<ClusterImpl> lifelineClusters = new ArrayList<>();
	private List<LinkImpl> messageLinks = new ArrayList<>();
	private List<RowImpl> rows = new ArrayList<>();
	private List<ColumnImpl> columns = new ArrayList<>();
	private InteractionLayoutManager layoutManager = new InteractionLayoutManager(this);
}
