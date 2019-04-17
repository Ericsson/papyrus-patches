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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
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
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Gate;
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
	public InteractionGraphImpl(Interaction interaction, Diagram diagram, DiagramEditPart editPart) {
		super(interaction);
		this.diagram = diagram; 
		this.diagramEditPart = editPart;
		viewer = editPart == null ? null : editPart.getViewer();
		
		setView(ViewUtilities.getViewForElement(diagram, interaction));		
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
		return diagram;
	}
	
	@Override
	public DiagramEditPart getDiagramEditPart() {
		return diagramEditPart;
	}

	@Override
	public EditPartViewer getEditPartViewer() {
		return viewer;
	}

	@Override
	Rectangle extractBounds() {
		return super.extractBounds();
	}

	@Override
	public Interaction getInteraction() {
		return (Interaction) super.getElement();
	}

	public View getInteractionView() {
		return getView();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<NodeImpl> getAllGraphNodes() {
		List<NodeImpl> l = new ArrayList<>();
		l .addAll((List)NodeUtilities.flatten(getLifelineClusters()));
		// Gates
		l .addAll((List)NodeUtilities.flatten(this).stream().map(FragmentCluster::getAllGates).
				flatMap(Collection::stream).collect(Collectors.toList()));
		return l;
	}
	
	@Override
	public List<Cluster> getLifelineClusters() {
		return Collections.unmodifiableList(lifelineClusters);
	}

	public void removeLifelineCluster(Cluster lifeline) {
		lifelineClusters.remove(lifeline);
		((ClusterImpl)lifeline).setParent(null);		
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

	public void addFormalGate(NodeImpl formalGate, NodeImpl beforeNode) {
		super.addInnerGate(formalGate,beforeNode);
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

		List<NodeImpl> allNodes = getAllGraphNodes();
		List<NodeImpl> orderedNodes = allNodes.stream().sorted(RowImpl.NODE_FRAGMENT_COMPARATOR).collect(Collectors.toList());
		
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
					Cluster lfPrev = NodeUtilities.getLifelineNode(prevNode);
					Cluster lf = NodeUtilities.getLifelineNode(node);
					if (prevNode.getConnectedNode() == node && lfPrev == lf && lf != null) {
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
		// Layout Lifeline Columns
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
			Rectangle r = formalGate.getBounds();
			if (r != null) {
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
			List<Node> coveredLifelines = cluster.getClusters().stream().map(NodeImpl.class::cast).map(NodeUtilities::getLifelineNode).
					filter(Predicate.isEqual(null).negate()).collect(Collectors.toList());
			Rectangle fragmentBounds = ViewUtilities.getBounds(viewer, cluster.getView());
			Column leftLifelineColumn = coveredLifelines.stream().map(Node::getColumn).min(Comparator.comparing(d->columns.indexOf(d))).orElse(null);
			Column rightLifelineColumn = coveredLifelines.stream().map(Node::getColumn).max(Comparator.comparing(d->columns.indexOf(d))).orElse(null);
			if (leftLifelineColumn == null || rightLifelineColumn == null)
				continue;
			for (Node gate : cluster.getAllGates()) {
				// TODO: Correlate in & out gates
				Node opposite = gate.getConnectedByNode();
				if (opposite == null) {
					opposite = gate.getConnectedNode();
				}

				Rectangle r = opposite.getBounds();
				// Force gate to fall in left or right side				
				if (r != null) {
					ColumnImpl col;
					if (Math.abs(r.getCenter().x - fragmentBounds.getLeft().x) <= Math.abs(r.getCenter().x - fragmentBounds.getRight().x)) {
						int x = fragmentBounds.x();
						col = columns.stream().filter(d->d.getXPosition() == x).findFirst().orElse(null);
						if (col == null) {
							col = new ColumnImpl(this);
							col.setXPosition(x);
							columns.add(columns.indexOf(leftLifelineColumn), col);							
						}
					} else {
						int x = fragmentBounds.right();
						col = columns.stream().filter(d->d.getXPosition() == x).findFirst().orElse(null);
						if (col == null) {
							col = new ColumnImpl(this);
							col.setXPosition(x);
							int index = columns.indexOf(rightLifelineColumn) +1;
							if (index >= columns.size())
								columns.add(col);
							else
								columns.add(index, col);							
						}
					}
					col.addNode((NodeImpl)gate);						
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
		
		Rectangle r = ViewUtilities.getBounds(getViewer(), getView()).getCopy();		
		r.height = lastY - r.y + 60;
		r.width = lastX - r.x + 60;		
		setBounds(r);
		
		if (rightGatesColumn != null) {
			rightGatesColumn.setXPosition(r.right());
			rightGatesColumn.getNodes().stream().map(NodeImpl.class::cast).forEach(layoutManager::layout);
		}
		
		if (leftGatesColumn != null) {
			leftGatesColumn.setXPosition(r.x());
			leftGatesColumn.getNodes().stream().map(NodeImpl.class::cast).forEach(layoutManager::layout);
		}

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

	public NodeImpl getGate(Gate gate) {
		return (NodeImpl)NodeUtilities.flatten(this).stream().filter(d -> d.getElement() == gate).findFirst().orElse(null);
	}
	
	@Override
	public NodeImpl getGate(Interaction interaction, Gate gate) {
		if (interaction != getInteraction())
			return null;
		return (NodeImpl)this.getFormalGates().stream().filter(d -> d.getElement() == gate).findFirst().orElse(null);
	}

	@Override
	public NodeImpl getGate(InteractionUse intUse, Gate gate) {
		FragmentClusterImpl intUseCluster = getInteractionUse(intUse);
		if (intUseCluster == null)
			return null;
		return (NodeImpl)intUseCluster.getOuterGates().stream().filter(d -> d.getElement() == gate).findFirst().orElse(null);
	}

	@Override
	public NodeImpl addGate(Interaction interaction, Gate gate, Node insertBefore) {
		if (interaction != getInteraction())
			return null;
		
		NodeImpl gateNode = getGate(interaction, gate);
		if (gateNode != null) {
			return gateNode;
		}
		
		if (getNodeFor(gate) != null) {
			return null;
		}

		NodeImpl node = new NodeImpl(gate);
		addFormalGate(node, (NodeImpl)insertBefore);
		builder.nodeCache.put(gate, node);
		layoutGrid();
		return node;
	}

	@Override
	public NodeImpl addGate(InteractionUse intUse, Gate gate, Node insertBefore) {
		FragmentClusterImpl intUseCluster = getInteractionUse(intUse);
		if (intUseCluster == null)
			return null;
		
		NodeImpl gateNode = getGate(intUse, gate);
		if (gateNode != null) {
			return gateNode;
		}
		
		if (getNodeFor(gate) != null) {
			return null;
		}

		
		NodeImpl node = new NodeImpl(gate);
		intUseCluster.addOuterGate(node, (NodeImpl)insertBefore);
		builder.nodeCache.put(gate, node);
		layoutGrid();
		return node;
	}

	@Override
	public Link connectMessageEnds(MessageEnd send, MessageEnd recv) {
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

	public FragmentClusterImpl getInteractionUse(InteractionUse interactionUse) {
		return (FragmentClusterImpl)NodeUtilities.flatten(this).stream().filter(d->d.getElement() == interactionUse).findFirst().orElse(null);
	}

	public FragmentClusterImpl addInteractionUse(InteractionUse interactionUse, List<Lifeline> lifelines, InteractionFragment beforeFragment) {
		List<Node> orderedNodes = getOrderedNodes();
		if (beforeFragment != null) {
			NodeImpl beforeFragmentNode = getNodeFor(beforeFragment);
			int index = orderedNodes.indexOf(beforeFragmentNode);
			orderedNodes = orderedNodes.subList(index, orderedNodes.size());
		} else {
			orderedNodes = Collections.emptyList();
		}
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

	
	@Override
	public FragmentCluster addInteractionUseToLifeline(InteractionUse interactionUse, Lifeline lifeline) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FragmentCluster removeInteractionUseFromLifeline(InteractionUse interactionUse, Lifeline lifeline) {
		// TODO Auto-generated method stub
		return null;
	}

	//////////////////////
	// END CHECK USE OF //
	//////////////////////	
	// TODO: @etxacam: It can be used to preserve cluster fragments to be moved with a block by removing the returned list from the block and 
	// adding it to the other nodes list 
	public List<Node> preserveFragmentClusterBlocksFromRemoval(List<Cluster> fragments, List<Node> allNodesBeingRemoved) {
		List<Node> nodesToRemove = new ArrayList<>();
		List<FragmentCluster> frgClusters = NodeUtilities.removeDuplicated(fragments.stream().map(Cluster::getFragmentCluster).collect(Collectors.toList()));
		for (FragmentCluster fc : frgClusters) {
			for (Cluster c : fc.getClusters()) {
				if (!fragments.contains(c))
					continue;
				Cluster newParent = c.getParent();
				Node insertPoint = null;
				while (newParent != null && allNodesBeingRemoved.contains(newParent)) {
					insertPoint = newParent; 
					newParent = newParent.getParent();
				}
				
				if (insertPoint != null && newParent != null) {
					((ClusterImpl)c.getParent()).removeNode(c);
					((ClusterImpl)newParent).addNode((ClusterImpl)c, insertPoint);
					nodesToRemove.addAll(NodeUtilities.flattenKeepClusters(c));
				}
			}
		}		
		return nodesToRemove;
	}

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
/*		if (yPos > blockEndPoint) {
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
*/		
				
		Map<Cluster, List<Node>> nodesByLifeline = nodes.stream().collect(Collectors.groupingBy(
				d -> (toCluters.containsKey(d) ? toCluters.get(d) : NodeUtilities.getTopLevelCluster(d))));

		List<Cluster> fragments = allNodes.stream().filter(Cluster.class::isInstance).map(Cluster.class::cast).filter(d->d.getFragmentCluster() != null).
				collect(Collectors.toList());
		removeNodeBlockImpl(nodes,otherNodes,0);		
		
		Rectangle newOtherArea = NodeUtilities.getArea(otherNodes);
		
		int newYPos = yPos;
		if (yPos > blockEndPoint) {
			// Insertion point after block
			if (otherArea != null) {
				// TODO: @etxacam Need to calculate the newYPos based on the next row (if any otherwise -prev-after)
				newYPos -= prev; 
				newYPos -= after;
			} else {
				newYPos -= height;
			}
		} else if (otherArea != null && yPos > otherEndPoint) {
			// Insertion point inside block after the other content
			newYPos = newOtherArea.bottom() + yPos - otherArea.bottom();
			postInsertionNudge = blockEndPoint - yPos ;
		} else if (otherArea != null && yPos > otherStartPoint) {
			// Insertion point inside block inside the other content
			newYPos -= prev; // TODO: @etxacam Need to calculate the newYPos based other block start (if any otherwise -prev-after)
		} else if (yPos > blockStartPoint) {
			// Insertion point inside block before the other content
			newYPos = yPos;
			postInsertionNudge = yPos - blockStartPoint;
		}				
		
		addNodeBlock(nodesByLifeline, newYPos, postInsertionNudge);
	}
	
	public void removeNodeBlocks(List<List<Node>> blocks) {
		for (List<Node> block : blocks) {
			removeNodeBlock(block);
		}
	}
	
	public void removeNodeBlock(List<Node> nodes) {
		Rectangle r = NodeUtilities.getArea(nodes);
		Node nodeAfter = NodeUtilities.getNodeAfterVerticalPos(this, r.bottom());		
		Row nextRow = nodeAfter == null ? null : nodeAfter.getRow();
		// TODO: @etxacam Check if we need to do it always... (Remove space to the next row.)
		int nudge = nextRow == null ? 0 : nextRow.getYPosition() - r.bottom();
		
		List<Node> allNodes = NodeUtilities.removeDuplicated(NodeUtilities.flattenKeepClusters(nodes));
		List<Node> others = NodeUtilities.getBlockOtherNodes(nodes);

		// Preserve Fragment clusters blocks
		List<Cluster> fragments = allNodes.stream().filter(Cluster.class::isInstance).map(Cluster.class::cast).filter(d->d.getFragmentCluster() != null).
				collect(Collectors.toList());
		removeNodeBlockImpl(nodes, others, (others != null && !others.isEmpty()) ? 0 : nudge);
		allNodes.removeAll(NodeUtilities.flattenKeepClusters(fragments));
		NodeUtilities.deleteNodes(this, allNodes); // Delete all references
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
			if (nudgeArea != null && nodesAfterArea != null)
				maxNudgeAfter = (nodesAfterArea.y - nudgeArea.y) / 20 * 20;

			int nudgeUpOthers = otherArea == null ? 0 : Math.max(0, Math.min(prev, maxNudgePrev));
			nudgeUpOthers = -NodeUtilities.nudgeNodes(otherNodes, 0, -nudgeUpOthers).height;			
			NodeUtilities.nudgeNodes(nodesAfter, 0, -Math.max(0, Math.min(nudge, maxNudgeAfter) + nudgeUpOthers ));
		} finally {
			enableLayout();
			layout();
		}
	}
	
	public void addNodeBlock(Map<Cluster,List<Node>> nodesByLifelines, int yPos) {
		addNodeBlock(nodesByLifelines, yPos, 0);
	}
	
	// TODO: @etxacam generalize lifelineXXXX variables to parentClusterXXXX => Apply to all blocks functions
	public void addNodeBlock(Map<Cluster,List<Node>> nodesByTopClusters, int yPos, int extraNudge) {
		List<Node> nodes = nodesByTopClusters.values().stream().flatMap(d->d.stream()).collect(Collectors.toList());
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

			if (nudgeOverlap) {				
				NodeUtilities.nudgeRows(rows.subList(row.getIndex(), rows.size()), 20);
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
			for (Map.Entry<Cluster,List<Node>> entry : nodesByTopClusters.entrySet()) {
				Cluster toCluster = entry.getKey();
				Rectangle nodesArea;
				if (toCluster instanceof FragmentCluster) {
					FragmentClusterImpl fc = (FragmentClusterImpl)toCluster;
					// Handle Gates and floating nodes....
					List<Node> allGates = fc.getAllGates();
					List<Node> ns = entry.getValue();					
					nodesArea = NodeUtilities.getArea(ns);
					Node insertBefore = allGates.stream().filter(d->!ns.contains(d) && d.getBounds().y >= yPos).
							findFirst().orElse(null);
		
					Cluster target = toCluster;
					if (insertBefore != null) {
						target = insertBefore.getParent();
					}
		
					NodeUtilities.insertNodes(this, ns, target, insertBefore, yPos + (nodesArea.y - totalArea.y));					
					
				} else {
					List<Node> ns = entry.getValue();
					List<Node> ans = NodeUtilities.removeDuplicated(NodeUtilities.flattenKeepClusters(ns)); 
					nodesArea = NodeUtilities.getArea(ns);
					Node newPrevNode = toCluster.getAllNodes().stream().filter(d->(d.getBounds().y < yPos)).
							filter(d->!ans.contains(d)).sorted(Collections.reverseOrder(RowImpl.NODE_VPOSITION_COMPARATOR)).findFirst().orElse(null);
					Cluster prevParent = newPrevNode != null ? newPrevNode.getParent() : null;
					if (prevParent != null && prevParent != toCluster && prevParent.getNodes().indexOf(newPrevNode) == prevParent.getNodes().size() -1)
						newPrevNode = prevParent;
					
					Node insertBefore = toCluster.getAllNodes().stream().filter(d->!ans.contains(d) && d.getBounds().y >= yPos).
							findFirst().orElse(null);
					Cluster insertBeforeParent = insertBefore != null ? insertBefore.getParent() : null;
					if (insertBeforeParent != null && insertBeforeParent != toCluster && insertBeforeParent.getNodes().indexOf(insertBefore) == 0)
						insertBefore = insertBeforeParent;
		
					Cluster target = toCluster;
					if (insertBefore != null) {
						target = insertBefore.getParent();
					}
		
					NodeUtilities.insertNodes(this, ns, target, insertBefore, yPos + (nodesArea.y - totalArea.y));					
				}
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

	private Diagram diagram;
	private DiagramEditPart diagramEditPart;
	private int disabledLayout = 0; 
	private InteractionGraphBuilder builder;
	private EditPartViewer viewer;
	private List<ClusterImpl> lifelineClusters = new ArrayList<>();
	private List<LinkImpl> messageLinks = new ArrayList<>();
	private List<RowImpl> rows = new ArrayList<>();
	private List<ColumnImpl> columns = new ArrayList<>();
	private InteractionLayoutManager layoutManager = new InteractionLayoutManager(this);
}
