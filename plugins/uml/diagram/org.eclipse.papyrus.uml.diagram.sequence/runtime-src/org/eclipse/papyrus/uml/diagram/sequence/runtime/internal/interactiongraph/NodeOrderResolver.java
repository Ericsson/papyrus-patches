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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.MarkNode.Kind;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.Lifeline;

/**
 * @author etxacam
 *
 */
// TODO: @etxacam Need to implement order based on visual ordering...
//TODO: @etxacam Sloped crossing messages don't work good with current ordering algorithm -> Need to be simplified.
public class NodeOrderResolver {
	public NodeOrderResolver(InteractionGraphImpl graph) {
		super();
		this.graph = graph;
	}

	public List<Node> getOrderedNodes() {
		List<Cluster> lifelineClusters = graph.getLifelineClusters();
		List<Node> nodes = new ArrayList<Node>();
		List<Node> branch = new ArrayList<Node>();
		for (Cluster lfCluster : lifelineClusters) {
			expandNode(lfCluster, branch, nodes);
			nodes.addAll(branch);
			branch.clear();
		}
		
		nodes.addAll(branch);
		
		fixGates(nodes);
		return nodes;
	}
	
	private boolean expandNode(Node n, List<Node> branch, List<Node> nodes) {
		if (branch.contains(n))
			return false;
		
		int index = nodes.indexOf(n);
		if (index != -1) {
			// TODO: @etxacam Find insertion point by links backwards
			// TODO: @etxacam Tune it, by using positions. 
			Node conNode = n.getConnectedByNode(); 
			if (conNode != null && nodes.indexOf(conNode) != -1)
				index = nodes.indexOf(conNode);
			nodes.addAll(index, branch);
			branch.clear();
			return true;
		}
		
		if (n instanceof Cluster) {
			Cluster c = (Cluster)n;
			for (Node cn : c.getNodes()) {
				expandNode(cn,branch,nodes);
			}
		} else {		
			branch.add(n);
		}
		Node next = n.getConnectedNode();
		if (next != null) {
			return expandNode(next,branch,nodes);
		}
		return false;
	}
	
	
	public List<Node> getOrderedNodesPrevConnectedBySort() {
		List<Cluster> lifelineClusters = graph.getLifelineClusters();
		List<List<Node>> lifelinesNodes = 
				lifelineClusters.stream().
				map(ClusterImpl.class::cast).
				map(d->d.getAllNodes()).collect(Collectors.toList());
		
		//boolean changes = false;
		Map<Node,Set<Node>> previousNodes = new HashMap<>();
		Map<Node,Set<Node>> triggeringNodes = new HashMap<>();
		for (Cluster lfCluster : lifelineClusters) {
			List<Node> lfCumm = new ArrayList<>();
			for (Node n : lfCluster.getAllNodes()) {				
				//changes = true;
				Set<Node> previous = new HashSet<>();
				// Add previous in the lifeline
				previous.addAll(lfCumm);
				lfCumm.add(n);
				
				/*
				// Add triggered by
				Node con = n.getConnectedByNode();
				if (con != null) {
					previous.add(con);					
				}
				Cluster parent = n.getParent();
				if (parent != lfCluster && parent.getNodes().indexOf(n) == 0) {
					// First node in the parent
					con = parent.getConnectedByNode();
					if (con != null)
						previous.add(con);
				}*/
				previousNodes.put(n, previous);

				Set<Node> triggering = new HashSet<>();
				Node con = getConnectedBy(n);
				while (con != null) {
					triggering.add(con);
					
					Cluster parent = con.getParent();
					if (parent == null || !(parent.getElement() instanceof ExecutionSpecification)) 
						break;
					con = getConnectedBy(parent);
				}
				triggeringNodes.put(n, triggering);
				

			}
		}
/*		
		while (changes) {
			changes = false;
			for (Map.Entry<Node, Set<Node>> e : previousNodes.entrySet()) {
				List<Node> ls = new ArrayList<>(e.getValue());
				for (Node n : ls) {
					if (e.getValue().addAll(previousNodes.get(n)))
						changes = true;
				}
			}
		}*/
		
		List<Node> nodes = lifelinesNodes.stream().flatMap(d -> d.stream()).
				sorted(new Comparator<Node>() {
					@Override
					public int compare(Node o1, Node o2) {
						Set<Node> previous1 = previousNodes.get(o1);
						Set<Node> previous2 = previousNodes.get(o2);
						if (previous2.contains(o1))
							return -1;
						if (previous1.contains(o2))
							return 1;
						
						Set<Node> trigg1 = triggeringNodes.get(o1);
						Set<Node> trigg2 = triggeringNodes.get(o2);
						if (previous2.stream().filter(d->trigg1.contains(d)).findFirst().isPresent())
							return -1;
						if (previous1.stream().filter(d->trigg2.contains(d)).findFirst().isPresent())
							return 1;
						return 0;
					}
					
				}).collect(Collectors.toList());
		
		fixGates(nodes);
		
		return nodes;
	}	
	
	private Node getConnectedBy(Node n) {
		Node con = n.getConnectedByNode();
		if (con != null) {
			return con;					
		}
		
		Cluster parent = n.getParent();
		if (parent.getElement() instanceof ExecutionSpecification && parent.getNodes().indexOf(n) == 0) {
			// First node in the parent
			con = parent.getConnectedByNode();			
		}
		
		return con;
	}
	
	private void fixGates(List<Node> nodes) {
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
					Node nextNode = nodes.stream().filter(d -> d.getBounds().y > _impl.bounds.y).findFirst().get();
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
					Node nextNode = nodes.stream().filter(d -> d.getBounds().y > _impl.bounds.y).findFirst().get();
					if (nextNode == null)
						nodes.add(impl);
				} else {
					nodes.add(0,impl);
				}
			}			
		}
		
	}
	
	
	
	
	
	public List<NodeImpl> getOrderedNodesA() {
		List<Cluster> lifelineClusters = graph.getLifelineClusters();
		List<List<Node>> lifelinesNodes = 
				lifelineClusters.stream().
				map(ClusterImpl.class::cast).
				map(d->(new ArrayList<Node>(d.getAllNodes().stream().
							/*sorted(RowImpl.NODE_VPOSITION_COMPARATORS).*/collect(Collectors.toList()))))
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
