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
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Column;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.DiagramLayoutPreferences;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphDiff;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Row;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.InteractionGraphCommand;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.OccurrenceSpecification;

public class InteractionGraphImpl extends FragmentClusterImpl implements InteractionGraph {
	public InteractionGraphImpl(Interaction interaction, Diagram diagram, GraphicalEditPart editPart) {
		super(interaction);
		super.setView(diagram);
		super.setEditPart(editPart);
		viewer = editPart == null ? null : editPart.getViewer();
	}

	@Override
	public DiagramLayoutPreferences getLayoutPreferences() {
		return diagramLayoutPrefs;
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

	public EditPartViewer getEditPartViewer() {
		return viewer;
	}

	@Override
	void setView(View vw) {
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

	void addLifeline(ClusterImpl lifeline) {
		lifelineClusters.add(lifeline);
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
	public List<Row> getRows() {
		return Collections.unmodifiableList(rows);
	}

	@Override
	public List<Column> getColumns() {
		return Collections.unmodifiableList(columns);
	}

	@Override
	public NodeImpl getNodeFor(Element element) {
		return builder.nodeCache.get(element);
	}

	public void layoutGrid() {
		rows.clear();
		columns.clear();

		// 1-. Layout Lifelines -> Row(0)
		RowImpl row = new RowImpl();
		rows.add(row);
		row.addNodes(lifelineClusters);

		NodeOrderResolver orderResolver = new NodeOrderResolver(this);

		RowImpl prevRow = null;
		NodeImpl prevNode = null;
		List<NodeImpl> orderedNodes = orderResolver.getOrderedNodes();
		boolean isNewRow = true;
		for (int i = 0; i < orderedNodes.size(); i++) {
			NodeImpl node = orderedNodes.get(i);
			if (prevNode != null) {
				isNewRow = !NodeUtilities.areNodesHorizontallyConnected(prevNode, node) ||
						!NodeUtilities.isNodeConnectedTo(prevNode, node);
				if (prevNode.getConnectedNode() == node &&
						NodeUtilities.getLifelineNode(prevNode) == NodeUtilities.getLifelineNode(node)) {
					// Self Message
					isNewRow = true;
				}
			}

			if (isNewRow) {
				prevRow = new RowImpl();
				rows.add(prevRow);
			}

			prevRow.addNode(node);
			prevNode = node;
		}

		// Layout Columns
		int index = 0;
		for (ClusterImpl lfCluster : lifelineClusters) {
			ColumnImpl column = new ColumnImpl();
			columns.add(column);
			column.addNode(lfCluster);

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
					leftGatesColumn = new ColumnImpl();
				}
				leftGatesColumn.addNode((NodeImpl) formalGate);
				continue;
			} else {
				if (rightGatesColumn == null) {
					rightGatesColumn = new ColumnImpl();
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

		// TODO: Columns & Rows for floating Nodes

		rows.stream().forEach(d -> d.setIndex(rows.indexOf(d)));
		columns.stream().forEach(d -> d.setIndex(columns.indexOf(d)));

		// TODO: Layout Y Positions.


		// TODO: Layout X Positions.
	}

	@Override
	public ClusterImpl getLifeline(Lifeline lifeline) {
		return lifelineClusters.stream().filter(d -> d.getElement() == lifeline).findFirst().orElse(null);
	}

	@Override
	public Cluster addLifeline(Lifeline lifeline) {
		ClusterImpl cluster = new ClusterImpl(lifeline);
		lifelineClusters.add(cluster);
		builder.nodeCache.put(lifeline, cluster);
		if (rows.size() == 0) {
			rows.add(new RowImpl());
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
		lifelineClusters.remove(lfToMove);
		if (beforeLf != null) {
			lifelineClusters.add(lifelineClusters.indexOf(beforeLf), lfToMove);
		} else {
			lifelineClusters.add(lfToMove);
		}

		layoutGrid();
	}

	@Override
	public NodeImpl getMessageOccurrenceSpecification(Lifeline lifeline, MessageOccurrenceSpecification mos) {
		ClusterImpl lifelineCluster = getLifeline(lifeline);
		return NodeUtilities.flattenImpl(lifelineCluster).filter(d -> d.getElement() == mos).findFirst().orElse(null);
	}

	@Override
	public Node addMessageOccurrenceSpecification(Lifeline lifeline, MessageOccurrenceSpecification mos) {
		NodeImpl mosNode = getMessageOccurrenceSpecification(lifeline, mos);
		if (mosNode != null) {
			return mosNode;
		}
		if (getNodeFor(mos) != null) {
			return null;
		}

		ClusterImpl lifelineCluster = getLifeline(lifeline);
		NodeImpl node = new NodeImpl(mos);
		lifelineCluster.addNode(node);
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
		NodeImpl n = builder.nodeCache.get(lifeline);
		if (n == null || !(n instanceof Cluster)) {
			return false;
		}
		ClusterImpl fromLifelineNode = (ClusterImpl) n;

		n = builder.nodeCache.get(toLifeline);
		if (n == null || !(n instanceof Cluster)) {
			return false;
		}
		ClusterImpl toLifelineNode = (ClusterImpl) n;

		NodeImpl mosNode = builder.nodeCache.get(mosToMove);
		if (mosNode == null) {
			return false;
		}
		n = builder.nodeCache.get(fragmentBefore);

		// TODO: Check if it is allowed to move the mos:
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
	public boolean connectMessageOcurrenceSpecification(MessageOccurrenceSpecification send, MessageOccurrenceSpecification recv) {
		NodeImpl sendNode = builder.nodeCache.get(send);
		NodeImpl recvNode = builder.nodeCache.get(recv);

		if (sendNode == null || recvNode == null) {
			return false;
		}

		sendNode.connectNode(recvNode);
		layoutGrid();
		return true;
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
		parentCluster.addNode(execNode);

		for (NodeImpl d : nodesToEncloseIn) {
			if (d != null) {
				parentCluster.removeNode(d);
			}
			execNode.addNode(d);
		}
		;
		execNode.addNode(1, new NodeImpl(exec));


		if (startNodeImpl.getConnectedByNode() != null) {
			startNodeImpl.getConnectedByNode().connectNode(execNode);
		} else if (startNodeImpl.getConnectedNode() != null) {
			execNode.connectNode(startNodeImpl.getConnectedNode());
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

		NodeImpl beforeNode = builder.nodeCache.get(fragmentBefore);

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

	@Override
	public List<InteractionGraphDiff> getEditChanges() {
		return new InteractionGraphDiffBuilder(this).calculateDifferences();
	}

	@Override
	public ICommand getEditCommand(TransactionalEditingDomain editingDomain, String label) {
		return new InteractionGraphCommand(this).build(editingDomain, label);
	}

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

	private InteractionGraphBuilder builder;
	private EditPartViewer viewer;
	private List<ClusterImpl> lifelineClusters = new ArrayList<>();
	private List<RowImpl> rows = new ArrayList<>();
	private List<ColumnImpl> columns = new ArrayList<>();
	private DiagramLayoutPreferencesImpl diagramLayoutPrefs = new DiagramLayoutPreferencesImpl();
}
