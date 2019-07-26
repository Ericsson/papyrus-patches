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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.MarkNode.Kind;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.uml2.uml.Gate;

/**
 * @author etxacam
 *
 */
// TODO: @etxacam Need to implement order based on visual ordering...
//TODO: @etxacam Sloped crossing messages don't work good with current ordering algorithm -> Need to be simplified.
public class NodeOrderResolverByRelations {
	public NodeOrderResolverByRelations(InteractionGraphImpl graph) {
		super();
		this.graph = graph;
	}

	
	public List<NodeImpl> getOrderedNodes() {
		List<Cluster> lifelineClusters = graph.getLifelineClusters();
		List<List<Node>> lifelinesNodes = 
				lifelineClusters.stream().
				map(ClusterImpl.class::cast).
				map(d->(new ArrayList<Node>(d.getAllNodes().stream().
							sorted(RowImpl.NODE_FRAGMENT_COMPARATOR).collect(Collectors.toList()))))
				.collect(Collectors.toList());
		List<NodeImpl> nodes = new ArrayList<NodeImpl>();
		NodeImpl node = null;
		do {
			// 1-. Choose the first head node.
			node = chooseNode(nodes.isEmpty() ? null : nodes.get(nodes.size()-1), lifelinesNodes);
			if (node != null) {
				nodes.add(node);
				final NodeImpl node_ = node;
				lifelinesNodes.stream().forEach(d -> d.remove(node_));
			}
		}  while(node != null);
		
		// Fixing Gates
		List<FragmentClusterImpl> fragments = graph.getFragmentClusters().stream().map(FragmentClusterImpl.class::cast).
				flatMap(NodeUtilities::flattenImpl).collect(Collectors.toList());		

		List<NodeImpl> gates = new ArrayList<NodeImpl>();
		gates.addAll(graph.getFormalGates().stream().map(NodeImpl.class::cast).collect(Collectors.toList()));
		gates.addAll(fragments.stream().flatMap(d -> d.getInnerGates().stream()).
				map(NodeImpl.class::cast).collect(Collectors.toList()));
		gates.addAll(fragments.stream().flatMap(d -> d.getOuterGates().stream()).
				map(NodeImpl.class::cast).collect(Collectors.toList()));
		for (NodeImpl gate : gates) {
			// Sending gate
			NodeImpl impl = gate.getConnectedNode();
			if (impl instanceof Cluster)
				impl = (NodeImpl)((Cluster)impl).getNodes().stream().findFirst().orElse(null);
			if (impl != null) {
				if (NodeUtilities.areNodesHorizontallyConnected(gate, impl)) {
					nodes.add(nodes.indexOf(impl),gate);
				} else if (impl.bounds != null) {
					// TODO consider the x position and the y tolerance
					final NodeImpl _impl = impl; 
					Node nextNode = nodes.stream().filter(d -> d.bounds.y > _impl.bounds.y).findFirst().orElse(null);
					if (nextNode == null)
						nodes.add(impl);
				} else {
					nodes.add(0,impl);
				}
				
				continue;
			}
			
			impl = gate.getConnectedByNode();
			if (impl != null) {
				if (NodeUtilities.areNodesHorizontallyConnected(gate, impl)) {
					nodes.add(nodes.indexOf(impl)+1,gate);
				} else if (impl.bounds != null) {
					// TODO consider the x position and the y tolerance
					final NodeImpl _impl = impl; 
					Node nextNode = nodes.stream().filter(d -> d.bounds.y > _impl.bounds.y).findFirst().orElse(null);
					if (nextNode == null)
						nodes.add(impl);
				} else {
					nodes.add(0,impl);
				}
			}			
		}
		
		
		return nodes;
	}
	
	private NodeImpl chooseNode(NodeImpl previous, List<List<Node>> nodes) {
		List<NodeImpl> heads = nodes.stream().
				map(d -> d.stream().findFirst().orElse(null)).
				map(NodeImpl.class::cast).
				collect(Collectors.toList());
		if (heads.stream().filter(d -> d != null).findFirst().orElse(null) == null)
			return null;

		NodeImpl candidate = null;
		for (NodeImpl node : heads) {
			if (node != null && !isTherePreviousHead(node, heads)) { 
				if (candidate == null) {
					candidate = node;
					continue;
				}
				
				if (NodeUtilities.getYPos(node) < NodeUtilities.getYPos(candidate))
					candidate = node;
				
				if (NodeUtilities.isNodeConnectedTo(previous, node)) {
					if (NodeUtilities.areNodesHorizontallyConnected(node,previous) ||
						NodeUtilities.getYPos(node) == Integer.MIN_VALUE || 
						NodeUtilities.getYPos(previous) == Integer.MIN_VALUE) {
							candidate = node;
					}
				}
			}
		}
		
		if (candidate == null) {
			// Cycle detected
		}
		return candidate;
	}
	
	private boolean isTherePreviousHead(NodeImpl node, List<NodeImpl> heads) {
		Set<NodeImpl> previous = new HashSet<NodeImpl>();
		previous.add(node);
		getPreviousNodes(node, previous);
		for (NodeImpl n : getPreviousNodesInLifeline(node, null)) {
			previous.add(n);
			getPreviousNodes(n, previous);
		}
		
		if (heads.stream().filter(d -> d != null && d != node).anyMatch(d -> previous.contains(d)))
			return true;
		return false;		
	}

	private void getPreviousNodes(NodeImpl node, Set<NodeImpl> previous) {		
		NodeImpl connectedBy = node.getConnectedByNode();
		if (connectedBy != null) {
			if (previous.contains(connectedBy))
				return;
			if (previous.add(connectedBy)) {
				List<NodeImpl> ns = getPreviousNodesInLifeline(connectedBy, null);
				if (previous.addAll(ns)) {
					ns.stream().forEach(d -> getPreviousNodes(d, previous));
				}
			}
		}
		
		NodeImpl connect = node.getConnectedNode();
		if (connect != null) {
			if (NodeUtilities.areNodesHorizontallyConnected(node,connect)) {
				List<NodeImpl> ns = getPreviousNodesInLifeline(connect, null);
				if (!ns.contains(node)) { // Avoid loops				
					if (previous.addAll(ns)) {
						ns.stream().forEach(d -> getPreviousNodes(d, previous));
					}
				}
			}
		}

		if (node instanceof MarkNodeImpl) {
			MarkNodeImpl markImpl = (MarkNodeImpl)node;			
			if (markImpl.getKind() == Kind.end) {
				FragmentClusterImpl fgCluster = markImpl.getParent().getFragmentCluster();
				List<NodeImpl> startMarks = fgCluster.getClusters().stream().map(d -> (NodeImpl)d.getNodes().get(0)).collect(Collectors.toList());
				if (previous.addAll(startMarks)) {
					startMarks.stream().forEach(d -> getPreviousNodes(d, previous));
				}
				
				List<Node> gates = new ArrayList<Node>(fgCluster.getInnerGates());
				if (gates.addAll(fgCluster.getOuterGates())) {
					gates.stream().map(NodeImpl.class::cast).forEach(d -> {
						if (previous.add(d.getConnectedByNode() != null ? d.getConnectedByNode() : d.getConnectedNode())) { 
							getPreviousNodes(d, previous);
						}
					});
				}
			}
		}
				
		ClusterImpl parent = node.getParent(); 
		if (parent != null && parent.getParent() != null) {
			if (previous.add(parent)) {
				getPreviousNodes(parent, previous);
			}
		}
	}

	private List<NodeImpl> getPreviousNodesInLifeline(NodeImpl n, List<NodeImpl> list) {
		if (list == null)
			list = new ArrayList<NodeImpl>();
		ClusterImpl parent = n.getParent();
		if (parent == null)
			return list;
		getPreviousNodesInLifeline(parent,list);
		List<Node> nodes = parent.getNodes();
		int index = nodes.indexOf(n);
		if (index == -1) {
			// It may be a Gate, as gates are floating message ends.
			if (n.getElement() instanceof Gate) {
				FragmentCluster fgCluster = (FragmentCluster)n.getParent();
				list.addAll(fgCluster.getClusters().stream().map(d -> (NodeImpl)d.getNodes().get(0)).collect(Collectors.toList()));
				return list;
			}
			return list;
		}
		nodes = nodes.subList(0, nodes.indexOf(n));
		list.addAll(nodes.stream().
				flatMap(d -> (d instanceof Cluster ? 
						     	((ClusterImpl)d).getAllNodes().stream() : 
						     	Collections.singletonList(d).stream())).
				map(NodeImpl.class::cast).
				collect(Collectors.toList()));
		return list;
	}
	
	private InteractionGraphImpl graph;
}
