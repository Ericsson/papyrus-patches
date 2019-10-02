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
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CLifeLineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Column;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.GraphItem;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Link;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.MarkNode;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.MarkNode.Kind;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Row;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
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

import com.google.common.collect.Comparators;

public class InteractionGraphImpl extends FragmentClusterImpl implements InteractionGraph {
	public InteractionGraphImpl(Interaction interaction, Diagram diagram, DiagramEditPart editPart) {
		super(interaction);
		this.diagram = diagram; 
		this.diagramEditPart = editPart;
		viewer = editPart == null ? null : editPart.getViewer();		
		this.gridEnabled = ViewUtilities.isSnapToGrid(viewer, diagram);
		if (!gridEnabled)
			this.gridSpacing = 20;
		else
			this.gridSpacing = (int)ViewUtilities.getGridSpacing(viewer, diagram);
		
		if (this.gridSpacing < 20) {
			this.gridSpacing = 20;
		}

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
	public int getGridSpacing() {
		return gridSpacing;
	}
	
	@Override
	public int getGridSpacing(int size) {
		return ViewUtilities.getClosestGrid(gridSpacing,size);
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
		l.addAll(layoutMarks);
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

	public InteractionLayoutManager getLayoutManager() {
		return layoutManager;
	}

	@Override
	public List<Node> getLayoutNodes() {
		return rows.stream().flatMap(r -> r.getNodes().stream()).collect(Collectors.toList());
	}
	
	public MarkNode setlayoutMark(Point pt) {
		MarkNodeImpl mark = new MarkNodeImpl(MarkNode.Kind.layout);
		mark.setBounds(new Rectangle(pt.x,pt.y,0,0));
		mark.setParent(this);
		layoutMarks.add(mark);
		if (disabledLayout > 0) {
			RowImpl row = (RowImpl)NodeUtilities.getRowAt(this, pt.y);
			if (row == null) {
				Row ra = NodeUtilities.getRowAfter(this, pt.x);
				int index = ra != null ? rows.indexOf(ra) : rows.size();
				row = new RowImpl(this);
				row.setYPosition(pt.y);
				row.setIndex(index);
				rows.add(index,row);
				rows.stream().filter(r->r.getIndex()>index).forEach(r->r.setIndex(r.getIndex()+1));
			}
			row.addNode(mark);
			
			ColumnImpl col = (ColumnImpl)NodeUtilities.getColumnAt(this, pt.x);
			if (col == null) {
				Column ca = NodeUtilities.getColumnAfter(this, pt.x);
				int index = ca != null ? columns.indexOf(ca) : columns.size();
				col = new ColumnImpl(this);
				col.setXPosition(pt.x);
				col.setIndex(index);
				columns.add(index,col);
				columns.stream().filter(c->c.getIndex()>index).forEach(c->c.setIndex(c.getIndex()+1));
			}			
			col.addNode(mark);
			
		} else {			
			layout();
		}
		return mark;
	}
	
	public boolean removeLayoutMark(MarkNode mark) {
		if (!layoutMarks.remove(mark))
			return false;
		
		RowImpl row = (RowImpl)mark.getRow();
		ColumnImpl col = (ColumnImpl)mark.getColumn();
		
		row.removeNode((MarkNodeImpl)mark);
		col.removeNode((MarkNodeImpl)mark);

		if (row.getNodes().isEmpty()) {				
			int index = rows.indexOf(row);
			rows.remove(row);
			rows.stream().filter(r->r.getIndex()>index).forEach(r->r.setIndex(r.getIndex()-1));
		}
		if (col.getNodes().isEmpty()) {				
			int index = columns.indexOf(col);
			columns.remove(col);
			columns.stream().filter(c->c.getIndex()>index).forEach(c->c.setIndex(c.getIndex()-1));
		}
		return true;
	}

	public void clearLayoutMarks() {
		List<MarkNodeImpl> l = new ArrayList<>(layoutMarks);
		l.forEach(this::removeLayoutMark);
	}
	
	public List<MarkNode> getLayoutMarks() {
		return Collections.unmodifiableList(layoutMarks);
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

	/* The grid layout algorithm should do:
	 *  Current issues with implementation. It should be:
	 *  1) Layout all the leafs in rows and calculate the provisional order.
	 *  2) Assign columns to Lifelines and leafs nodes.   
	 *  3) Layout cluster (Bounds may be invalid as lifeline may have changed)
	 *  5) Layout Interaction container (Bounds may be invalid as lifeline may have changed)
	 *  6) Layout Create Message (Lifeline head to right row)  
	 *  4) Interaction Gates (left & right sides)
	 *  5) Invalidate Fragment clusters (Bounds may be invalid as lifeline may have changed or covered list has changed)
	 *  6) Fragment cluster gates
	 *  7) Fragment cluster inner fragments (recursively)
	 *  8) Align rows to grid and merge too close rows (~ duplicated).
	 *  9) Index rows
	 *  10) Index Columns
	 *  11) Resolve column overlapping ???
	 *  12) Apply rows and cols positions to nodes (By layout them)
	 *  13) Align lifeline bottom positions
	 *  14) Extend / contract Interaction frame size  
	 */
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
		int headerHeight = lifelineClusters.stream().filter(CLifeLineEditPart.class::isInstance).
				map(d->((CLifeLineEditPart)d.getEditPart()).getStickerHeight()).
				max(Integer::compare).orElse(ViewUtilities.LIFELINE_HEADER_HEIGHT); 
		int y = compRect.y + SequenceUtil.LIFELINE_VERTICAL_OFFSET + (headerHeight / 2); // getGridSpacing(20) ;
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

		Rectangle prevBounds = null;
		// Layout Lifeline Columns
		for (ClusterImpl lfCluster : lifelineClusters) {
			ColumnImpl column = new ColumnImpl(this);
			columns.add(column);
			column.addNode(lfCluster);
			
			// TODO: It maybe not good to resolve overlaps in Lifeline columns here, it may layout gates and 
			// floating element wrongly... Needs to be done at the end. 
			// See function comment.
			Rectangle r = lfCluster.getBounds();
			if (r != null) {
				int nudgeX = 0;
				if (prevBounds != null && Draw2dUtils.intersects(r,prevBounds)) {
					nudgeX += prevBounds.right() + getGridSpacing(20) - r.x;
				}
				int colX = r.getCenter().x + nudgeX;

				column.setXPosition(colX);
				prevBounds = r;
			}
			column.addNodes((List) lfCluster.getAllNodes());
			
			// Layout Clusters
			List<ClusterImpl> subClusters = (List)lfCluster.getAllClusters();
			Collections.reverse(subClusters);
			subClusters.stream().forEach(layoutManager::layout);
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

		// Fragment Cluster & Fragment Cluster Gates
		List<FragmentCluster> fragmentClusters = NodeUtilities.flatten(this);
		for (FragmentCluster cluster : fragmentClusters) {
			if (cluster == this) {
				continue;
			}
			
			// Needs to layout in order to layout gates on the right side. 
			getLayoutManager().layout((FragmentClusterImpl)cluster);

			List<Node> coveredLifelines = cluster.getClusters().stream().map(NodeImpl.class::cast).map(NodeUtilities::getLifelineNode).
					filter(Predicate.isEqual(null).negate()).collect(Collectors.toList());
			Rectangle fragmentBounds = cluster.getBounds();//ViewUtilities.getBounds(viewer, cluster.getView());
			Column leftLifelineColumn = coveredLifelines.stream().map(Node::getColumn).min(Comparator.comparing(d->columns.indexOf(d))).orElse(null);
			Column rightLifelineColumn = coveredLifelines.stream().map(Node::getColumn).max(Comparator.comparing(d->columns.indexOf(d))).orElse(null);
			if (leftLifelineColumn == null || rightLifelineColumn == null)
				continue;
			for (Node gate : cluster.getAllGates()) {
				// TODO: Correlate in & out gates
				Rectangle r = gate.getBounds();
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

		if (leftGatesColumn != null) {
			columns.add(0, leftGatesColumn);
		}

		if (rightGatesColumn != null) {
			columns.add(rightGatesColumn);
		}

		// Layout Mark Columns
		for (MarkNodeImpl m : layoutMarks) {
			ColumnImpl c = (ColumnImpl)NodeUtilities.getColumnAt(this, m.getBounds().x);
			if (c == null) {
				c = new ColumnImpl(this);
				c.setXPosition(m.getBounds().x);
				Column ca = NodeUtilities.getColumnAfter(this, c.getXPosition());
				int index = ca == null ? columns.size() : columns.indexOf(ca);
				columns.add(index,c);
			}			
			c.addNode(m);
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
		// TODO: Layout Y Positions.
		double grid = -1.0;
		if (ViewUtilities.isSnapToGrid(viewer, diagram))
			grid = ViewUtilities.getGridSpacing(viewer, diagram);
		if (grid <= 5.0)
			grid = -1.0;
		
		List<RowImpl> rowsToDelete = new ArrayList<RowImpl>();
		prevRow = null;
		for (RowImpl r : rows) {
			int closestGrid = (int)(Math.round(((double)r.getYPosition() / (double)grid)) * (double)grid);
			int diff = Math.abs(r.getYPosition() - closestGrid);
			if (diff > 0 && diff <= 2) {
				r.setYPosition(closestGrid);
			}

		}
		
		for (RowImpl r : rows) {			// Order nodes in each row.
			r.nodes.sort(RowImpl.MESSAGE_END_NODE_COMPARATOR);
			if (prevRow != null && r.getYPosition() - prevRow.getYPosition() <= 2) {
				r.addNodes(new ArrayList(prevRow.getNodes()));
				rowsToDelete.add(r);
			}
			prevRow = r;
			
		}
		
		rows.removeAll(rowsToDelete);		
		rows.stream().forEach(d -> d.setIndex(rows.indexOf(d)));
		rows.stream().forEach(d -> d.setIndex(rows.indexOf(d)));
		columns.stream().forEach(d -> d.setIndex(columns.indexOf(d)));



		// TODO: Layout X Positions.
		for (ColumnImpl col : columns) {
			// int x = col.getXPosition();
			// col.nodes.stream().forEach(d -> d.horizontalLayout(x));
		}

		layoutManager.layout();
		
		int lastY = Math.max(300,rows.get(rows.size()-1).getYPosition()+getGridSpacing(40));
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
		r.height = lastY - r.y + getGridSpacing(60);
		r.width = lastX - r.x + getGridSpacing(60);		
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

	@Override
	public NodeImpl getGate(InteractionFragment interaction, Gate gate) {
		if (interaction instanceof Interaction) {
			return getGate((Interaction)interaction, gate);
		} else if (interaction instanceof InteractionUse) {
			return getGate((InteractionUse)interaction, gate);
		}
		throw new IllegalArgumentException("InteractionFragment type cannot have gates.");
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
	public NodeImpl addGate(InteractionFragment interaction, Gate gate, Node insertBefore) {
		if (interaction instanceof Interaction) {
			return addGate((Interaction)interaction, gate, insertBefore);
		} else if (interaction instanceof InteractionUse) {
			return addGate((InteractionUse)interaction, gate, insertBefore);
		}
		throw new IllegalArgumentException("InteractionFragment type cannot have gates.");
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
		} else {
			link.setSource(sendNode);
			link.setTarget(recvNode);					
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

	private Diagram diagram;
	private DiagramEditPart diagramEditPart;
	private int disabledLayout = 0; 
	private boolean gridEnabled = true; 
	private int gridSpacing = 20; 
	private InteractionGraphBuilder builder;
	private EditPartViewer viewer;
	private List<ClusterImpl> lifelineClusters = new ArrayList<>();
	private List<LinkImpl> messageLinks = new ArrayList<>();
	private List<RowImpl> rows = new ArrayList<>();
	private List<ColumnImpl> columns = new ArrayList<>();
	private InteractionLayoutManager layoutManager = new InteractionLayoutManager(this);
	private List<MarkNodeImpl> layoutMarks = new ArrayList<>();
}
