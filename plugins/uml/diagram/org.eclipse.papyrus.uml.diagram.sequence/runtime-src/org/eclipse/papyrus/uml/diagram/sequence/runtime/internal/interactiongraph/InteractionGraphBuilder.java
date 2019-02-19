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

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.GraphItem;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.MarkNode.Kind;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Continuation;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.util.UMLSwitch;

class InteractionGraphBuilder extends UMLSwitch<Node> {
	InteractionGraphImpl graph;
	EditPartViewer viewer;
	Map<OccurrenceSpecification, ExecutionSpecification> startExecutionSpecification;
	Map<OccurrenceSpecification, ExecutionSpecification> endExecutionSpecification;
	Map<Lifeline, ClusterImpl> activeLifelineGroups;
	Map<EObject, GraphItem> nodeCache = new HashMap<>(); // TODO: -> Move to the GraphImpl

	InteractionGraphBuilder(Interaction interaction, Diagram diagram, EditPartViewer viewer) {
		GraphicalEditPart editPart = viewer == null ? null : (GraphicalEditPart) viewer.getEditPartRegistry().get(diagram);
		graph = new InteractionGraphImpl(interaction, diagram, editPart);
		graph.setBuilder(this);
		this.viewer = viewer;
	}

	public InteractionGraph build() {
		startExecutionSpecification = graph.getInteraction().getLifelines().stream()
				.map(Lifeline::getCoveredBys).flatMap(List::stream)
				.filter(ExecutionSpecification.class::isInstance)
				.map(ExecutionSpecification.class::cast)
				.collect(Collectors.toMap(ExecutionSpecification::getStart, Function.identity()));

		endExecutionSpecification = graph.getInteraction().getLifelines().stream()
				.map(Lifeline::getCoveredBys).flatMap(List::stream)
				.filter(ExecutionSpecification.class::isInstance)
				.map(ExecutionSpecification.class::cast)
				.collect(Collectors.toMap(ExecutionSpecification::getFinish, Function.identity()));

		activeLifelineGroups = new HashMap<>();

		// Create Lifeline Nodes
		graph.getInteraction().getLifelines().forEach(lifeline -> doSwitch(lifeline));

		// Create Gate Nodes
		graph.getInteraction().getFormalGates().forEach(gate -> doSwitch(gate));

		// Create Fragments
		graph.getInteraction().getFragments().forEach(fragment -> doSwitch(fragment));
		// Synch diagram positions
		graph.getLifelineClusters().forEach(cluster -> ((ClusterImpl)cluster).
				updateNodes(RowImpl.NODE_VPOSITION_COMPARATOR));

		
		// Link with messages
		graph.getInteraction().getMessages().forEach(message -> doSwitch(message));

		// Layout Grid
		graph.layout();

		return graph;
	}

	@Override
	public NodeImpl caseLifeline(Lifeline element) {
		ClusterImpl cluster = new ClusterImpl(element);
		graph.addLifelineCluster(cluster, null);
		cluster.setView(ViewUtilities.getViewForElement(graph.getDiagram(), element));
		activeLifelineGroups.put(element, cluster);
		cache(element, cluster);

		return cluster;
	}

	@Override
	public NodeImpl caseDestructionOccurrenceSpecification(DestructionOccurrenceSpecification element) {
		return null;
	}

	@Override
	public NodeImpl caseMessageOccurrenceSpecification(MessageOccurrenceSpecification element) {
		NodeImpl node = caseOccurrenceSpecification(element);
		Message msg = element.getMessage();
		Edge msgView = (Edge)ViewUtilities.getViewForElement(graph.getDiagram(), msg);
		
		if (msg.getSendEvent() == element) {
			Point p = ViewUtilities.getAnchorLocationForView(viewer, msgView, msgView.getSource());
			if (ViewUtilities.isSnapToGrid(graph.getEditPartViewer(), graph.getDiagram()))
				p = ViewUtilities.snapToGrid(graph.getEditPartViewer(), graph.getDiagram(), p);
			node.setBounds(new Rectangle(p,new Dimension(0, 0)));
		} else if (msg.getReceiveEvent() == element) {
			Point p = ViewUtilities.getAnchorLocationForView(viewer, msgView, msgView.getTarget());
			if (ViewUtilities.isSnapToGrid(graph.getEditPartViewer(), graph.getDiagram()))
				p = ViewUtilities.snapToGrid(graph.getEditPartViewer(), graph.getDiagram(), p);

			if (p != null) {
				node.setBounds(new Rectangle(p,new Dimension(0, 0)));
			}
		}
		return node;
	}

	@Override
	public NodeImpl caseExecutionOccurrenceSpecification(ExecutionOccurrenceSpecification element) {
		NodeImpl node = caseOccurrenceSpecification(element);
		Point top = node.getParent().getBounds().getTop();
		if (ViewUtilities.isSnapToGrid(graph.getEditPartViewer(), graph.getDiagram()))
			top = ViewUtilities.snapToGrid(graph.getEditPartViewer(), graph.getDiagram(), top);
		node.setBounds(new Rectangle(top, new Dimension(0, 0)));
		return node;
	}

	@Override
	public NodeImpl caseOccurrenceSpecification(OccurrenceSpecification element) {
		boolean isStartExecutionSpecification = startExecutionSpecification.containsKey(element);
		boolean isFinishExecutionSpecification = endExecutionSpecification.containsKey(element);
		Lifeline lifeline = element.getCovered();

		if (isStartExecutionSpecification) {
			ClusterImpl parent = activeLifelineGroups.get(lifeline);
			ClusterImpl execSpecGroup = new ClusterImpl(startExecutionSpecification.get(element));
			parent.addNode(execSpecGroup);
			execSpecGroup.setView(ViewUtilities.getViewForElement(graph.getDiagram(), execSpecGroup.getElement()));
			activeLifelineGroups.put(lifeline, execSpecGroup);
		}

		ClusterImpl parent = activeLifelineGroups.get(lifeline);
		NodeImpl node = new NodeImpl(element);		
		parent.addNode(node);
		node.setView(ViewUtilities.getViewForElement(graph.getDiagram(), element));
		if (isStartExecutionSpecification) {
			Point p = node.getParent().getBounds().getTop();
			if (ViewUtilities.isSnapToGrid(graph.getEditPartViewer(), graph.getDiagram()))
				p = ViewUtilities.snapToGrid(graph.getEditPartViewer(), graph.getDiagram(), p);			
			node.setBounds(new Rectangle(p, new Dimension(0, 0)));
		} else if (isFinishExecutionSpecification) {
			Point p = node.getParent().getBounds().getBottom();
			if (ViewUtilities.isSnapToGrid(graph.getEditPartViewer(), graph.getDiagram()))
				p = ViewUtilities.snapToGrid(graph.getEditPartViewer(), graph.getDiagram(), p);
			node.setBounds(new Rectangle(p, new Dimension(0, 0)));
		}
		cache(element, node);

		if (isFinishExecutionSpecification) {
			if (parent.getParent() != null)
				activeLifelineGroups.put(lifeline, parent.getParent());
	}

		return node;
	}

	@Override
	public NodeImpl caseExecutionSpecification(ExecutionSpecification element) {
		OccurrenceSpecification start = element.getStart();

		NodeImpl startNode = getCacheNode(start);
		ClusterImpl execSpecCluster = startNode.getParent();

		NodeImpl node = new NodeImpl(element);
		execSpecCluster.addNode(node);
		node.setView(ViewUtilities.getViewForElement(graph.getDiagram(), element));
		cache(element, node);

		return node;
	}

	@Override
	public NodeImpl caseInteractionUse(InteractionUse element) {
		// TODO: Need To handle Gates messages before the end row...
		FragmentClusterImpl intUseCluster = new FragmentClusterImpl(element);
		cache(element, intUseCluster);
		graph.addFragmentCluster(intUseCluster);
		intUseCluster.setView(ViewUtilities.getViewForElement(graph.getDiagram(), element));

		for (Lifeline lifeline : element.getCovereds()) {
			ClusterImpl activeCluster = activeLifelineGroups.get(lifeline);
			ClusterImpl cluster = new ClusterImpl(element);
			activeCluster.addNode(cluster);
			cluster.setView(ViewUtilities.getViewForElement(graph.getDiagram(), element));
			intUseCluster.addCluster(cluster);

			// Create start
			MarkNodeImpl start = new MarkNodeImpl(Kind.start);
			cluster.addNode(start);

			// Create End mark
			MarkNodeImpl end = new MarkNodeImpl(Kind.end);
			cluster.addNode(end);
		}

		// Actual gates.
		element.getActualGates().stream().forEach(InteractionGraphBuilder.this::doSwitch);

		return intUseCluster;
	}

	@Override
	public NodeImpl caseCombinedFragment(CombinedFragment element) {
		return null;
	}

	@Override
	public NodeImpl caseInteractionOperand(InteractionOperand element) {
		return null;
	}

	@Override
	public NodeImpl caseGate(Gate element) {
		NodeImpl node = new NodeImpl(element);
		if (element.getOwner() instanceof Interaction) {
			graph.addFormalGate(node);
		} else if (element.getOwner() instanceof InteractionUse) {
			FragmentClusterImpl intUseCluster = (FragmentClusterImpl) getCacheNode(element.getOwner());
			intUseCluster.addOuterGate(node);
		}
		node.setView(ViewUtilities.getViewForElement(graph.getDiagram(), element));
		cache(element, node);
		return node;
	}

	@Override
	public Node caseMessage(Message element) {
		MessageEnd sendEvent = element.getSendEvent();
		MessageEnd recvEvent = element.getReceiveEvent();
		boolean isExecutionSpecification = recvEvent instanceof OccurrenceSpecification &&
				startExecutionSpecification.containsKey(recvEvent);

		NodeImpl sendNode = getCacheNode(sendEvent);
		NodeImpl recvNode = getCacheNode(recvEvent);

		LinkImpl link = new LinkImpl(element);
		link.setSource(sendNode);
		link.setTarget(recvNode);		
		graph.addMessage(link, null);
		link.setEdge((Edge)ViewUtilities.getViewForElement(graph.getDiagram(), element));		
		sendNode.connectNode(isExecutionSpecification ? recvNode.getParent() : recvNode, link);
		nodeCache.put(element, link);
		return sendNode; // return not null to avoid continue case processing
	}

	@Override
	public Node caseContinuation(Continuation element) {
		return null;
	}

	NodeImpl getCacheNode(EObject obj) {		
		GraphItem res = nodeCache.get(obj);
		if (!(res instanceof NodeImpl))
			return null;
		return (NodeImpl)res;
	}
	
	LinkImpl getCacheLink(EObject obj) {		
		GraphItem res = nodeCache.get(obj);
		if (!(res instanceof LinkImpl))
			return null;
		return (LinkImpl)res;
	}

	private void cache(EObject obj, GraphItem n) {
		nodeCache.put(obj, n);
	}

	private RowImpl getRowFor(NodeImpl node) {
		return node != null ? node.getRow() : null;
	}
}