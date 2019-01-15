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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.MarkNode;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.OccurrenceSpecification;

/**
 * @author ETXACAM
 *
 */
public class NodeUtilities {
	public static final int THRESHOLD_HORIZONTAL_DEPS = 5; // Move it to LayoutPreferences...

	public static boolean areNodesHorizontallyConnected(NodeImpl n1, NodeImpl n2) {
		return Math.abs(getYPos(n1) - getYPos(n2)) < THRESHOLD_HORIZONTAL_DEPS; 
	}
	
	public static boolean isNodeConnectedTo(NodeImpl n1, NodeImpl n2) {
		if (n1 == null)
			return false;
		
		NodeImpl connected = n1.getConnectedNode();
		if (connected == n2)
			return true;
		
		if (connected instanceof Cluster) {
			Node firstNode = ((Cluster) connected).getNodes().stream().findFirst().orElse(null);
			if (firstNode == n2)
				return true;
		}
		
		if (n2.getElement() instanceof ExecutionSpecification) {
			if (n1.getElement() == ((ExecutionSpecification)n2.getElement()).getStart())
				return true;
		}
		
		// TODO: Consider Start / End fragments marks ???
		if (n2 instanceof MarkNodeImpl && n1 instanceof MarkNodeImpl) {
			MarkNodeImpl nm1 = (MarkNodeImpl)n1;
			MarkNodeImpl nm2 = (MarkNodeImpl)n2;
			
			MarkNode.Kind k = nm1.getKind();
			if (k == nm2.getKind() && (k == MarkNode.Kind.start || k == MarkNode.Kind.end)) {
				if (nm1.getParent().getFragmentCluster() == nm2.getParent().getFragmentCluster()) 
					return true;
			}
		}
		return false;
	}
	
	public static int getYPos(NodeImpl node) {
		Rectangle r = node.getBounds();
		if (r == null)
			return Integer.MIN_VALUE;
		if (node.getElement() instanceof MessageEnd ||
			node.getElement() instanceof OccurrenceSpecification) {
			return r.getCenter().y;
		}
		return r.y;
	}
	
	public static Cluster getLifelineNode(Node impl) {
		while (impl.getParent() != null) {
			impl = impl.getParent();
		}
		
		if (impl.getElement() instanceof Lifeline) {
			return (Cluster)impl;			
		}
		return null;
	}
	
	public static List<Node> flattenKeepClusters(List<Node> nodes) {
		List<Node> res = new ArrayList<Node>();
		List<Node> flatNodes = nodes.stream().map(NodeImpl.class::cast).
				flatMap(NodeUtilities::flattenImpl).collect(Collectors.toList());
		for (Node n : flatNodes) {
			if (n.getParent() != null && n.getParent().getNodes().indexOf(n) == 0) {
				if (n.getParent().getParent() != null && n.getParent().getParent() != n.getInteractionGraph()) 
					res.add(n.getParent());
			}
			res.add(n);
		}
		return res;
	}

	public static List<Node> flatten(List<Node> nodes) {
		return nodes.stream().map(NodeImpl.class::cast).flatMap(NodeUtilities::flattenImpl).collect(Collectors.toList());
	}
	
	
	public static List<Node> flatten(ClusterImpl cluster) {
		return flattenImpl((NodeImpl)cluster).collect(Collectors.toList());
	}
	
	static Stream<NodeImpl> flattenImpl(NodeImpl cluster) {
		if (cluster instanceof ClusterImpl) {
			return ((Cluster)cluster).getNodes().stream().map(NodeImpl.class::cast).flatMap(NodeUtilities::flattenImpl);
		} else {
			return Collections.singleton(cluster).stream();
		}
		
	}

	public static List<FragmentCluster> flatten(FragmentClusterImpl cluster) {
		return flattenImpl(cluster).collect(Collectors.toList());
	}
	
	static Stream<FragmentClusterImpl> flattenImpl(FragmentClusterImpl cluster) {
		if (!cluster.getOwnedFragmentClusters().isEmpty()) {
			return cluster.getOwnedFragmentClusters().stream().map(FragmentClusterImpl.class::cast).flatMap(NodeUtilities::flattenImpl);
		} else {
			return Collections.singleton(cluster).stream();
		}
	}

	public static List<Node> getBlock(Node source) {
		List<Node> nodes = new ArrayList<>();
		getBlock(source, nodes);
		List<Node> res = nodes.stream().filter(d -> !nodes.contains(d.getParent())).collect(Collectors.toList());
		return res;
	}
	
	static List<Node> getBlock(Node source, List<Node> nodes) {
		nodes.add(source);
		Node n = source.getConnectedNode();
		if (n == null)
			return nodes;
		
		nodes.add(n);
		if (n instanceof Cluster) {
			for (Node nn : ((Cluster) n).getNodes()) {
				getBlock(nn, nodes);
			}
		}
		
		return nodes;
	}	
	
	public static void moveNodes(InteractionGraph interactionGraph, List<Node> nodes, Cluster targetCluster, Node insertBefore, int yPos) {
		Rectangle area = getArea(nodes);
		
		int orgPosY = area.y;
		for (Node n : nodes) {			
			((ClusterImpl)n.getParent()).removeNode((NodeImpl)n);
			((ClusterImpl)targetCluster).addNode((NodeImpl)n, insertBefore);
			if (n instanceof Cluster) {
				// Update position in the children 
				for (Node child : flattenKeepClusters(Collections.singletonList((ClusterImpl)n))) {
					child.getBounds().y = yPos + (child.getBounds().y - orgPosY);
				}
			} else {
				n.getBounds().y = yPos + (n.getBounds().y - orgPosY);				
			}
		}
		interactionGraph.layout();
	}

	public static void nudgeNodes(List<Node> nodes, int xDelta, int yDelta) {
		for (Node n : nodes) {
			n.getBounds().x += xDelta;
			n.getBounds().y += yDelta;
		}		
		if (nodes.size() > 0)
			((InteractionGraphImpl)nodes.get(0).getInteractionGraph()).layout();
	}

	public static Rectangle getEmptyArea(InteractionGraphImpl interactionGraph, List<Node> leftNodes, List<Node> topNodes, List<Node> rightNodes, List<Node> bottomNodes) {
		View interactionView = interactionGraph.getInteractionView();
		Rectangle rect =  ViewUtilities.getClientAreaBounds(interactionGraph.getEditPartViewer(), interactionView);
		int left=rect.x, right=rect.x+rect.width, top = rect.y, bottom = rect.y+rect.height;
		
		if (leftNodes != null) {
			for (Node n : leftNodes) {
				if (n == null)
					continue;
				Rectangle r = n.getBounds();
				left = Math.max(left, r.x+r.width);
			}
		}
		
		if (rightNodes != null) {
			for (Node n : rightNodes) {
				if (n == null)
					continue;
				Rectangle r = n.getBounds();
				right = Math.min(right, r.x);
			}
		}
		
		if (topNodes != null) {
			for (Node n : topNodes) {
				if (n == null)
					continue;
				Rectangle r = n.getBounds();
				top = Math.max(top, r.y+r.height);
			}
		}
		
		if (bottomNodes != null) {
			for (Node n : bottomNodes) {
				if (n == null)
					continue;
				Rectangle r = n.getBounds();
				bottom= Math.min(bottom, r.y);
			}
		}
		
		return new Rectangle(left,top, Math.max(0,right-left), Math.max(0,bottom-top));
	}
	
	public static Rectangle getArea(List<Node> nodes) {
		Rectangle r = null;
		for (Node n : nodes) {
			Rectangle b = n.getBounds();
			if (b.width <= 1 && b.height <= 1) {
				if (r == null)
					r = new Rectangle(b.x,b.y,0,0);
				else
					r.union(b.x,b.y);
			} else {
				if (r == null)
					r = b.getCopy();
				else
					r.union(b);				
			}				
		}
		
		if (r == null)
			return new Rectangle(0,0,-1,-1);
		r.width = Math.max(1,r.width);
		r.height = Math.max(1,r.height);			

		return r;
	}
	
	
}
