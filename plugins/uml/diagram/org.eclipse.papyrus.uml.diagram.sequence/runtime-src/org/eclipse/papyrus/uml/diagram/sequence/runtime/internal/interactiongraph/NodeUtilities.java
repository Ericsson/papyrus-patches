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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Column;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.MarkNode;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Row;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
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
		// Remove children and order by YPos
		List<Node> res = nodes.stream().filter(d -> !nodes.contains(d.getParent())).
				sorted(RowImpl.NODE_FRAGMENT_COMPARATOR).collect(Collectors.toList());
		return res;
	}
	
	static List<Node> getBlock(Node source, List<Node> nodes) {
		if (nodes.contains(source))
			return nodes;
		
		nodes.add(source);
		Node rn = source.getConnectedByNode();
		if (rn != null)
			getBlock(rn,nodes);
		Node cn = source.getConnectedNode();
		if (cn != null)
			getBlock(cn,nodes);
		
		if (source instanceof Cluster) {
			Cluster c = (Cluster) source;
			for (Node nn : c.getNodes()) {
				getBlock(nn, nodes);
			}
			
			Node ret = c.getNodes().get(c.getNodes().size()-1);
			Node retTrg = ret.getConnectedNode();
			if (rn != null && retTrg != null && rn.getParent() == retTrg.getParent() && nodes.contains(rn.getParent())) {
				// Add to the block the nodes between the triggering one and the return
				List<Node> parentNodes = rn.getParent().getNodes();
				for (int i=parentNodes.indexOf(rn); i<=parentNodes.indexOf(retTrg); i++) {
					getBlock(parentNodes.get(i),nodes);
				}
			}
		}
		
		return nodes;
	}	

	public static List<Node> getBlockOtherNodes(List<Node> blockNodes) {
		List<Node> allNodes = NodeUtilities.flattenKeepClusters(blockNodes);
		List<Node> otherNodes = new ArrayList<>();
		InteractionGraph interactionGraph = blockNodes.stream().map(Node::getInteractionGraph).findFirst().orElse(null);
		if (interactionGraph == null)
			return otherNodes;
		
		int firstRow = allNodes.stream().filter(d->d.getRow() != null).map(d->d.getRow().getIndex()).min(Integer::compareTo).orElse(-1); 
		int lastRow = allNodes.stream().filter(d->d.getRow() != null).map(d->d.getRow().getIndex()).min(Comparator.reverseOrder()).orElse(-1); 
		int firstRowWithOtherContent = lastRow;
		int lastRowWithOtherContent = firstRow;
		for (int i=firstRow; i<=lastRow; i++) {
			otherNodes.addAll(interactionGraph.getRows().get(i).getNodes());
			if (!allNodes.containsAll(interactionGraph.getRows().get(i).getNodes())) {
				firstRowWithOtherContent = Math.min(firstRowWithOtherContent, i);
				lastRowWithOtherContent = Math.max(lastRowWithOtherContent, i);
			}
		}
		otherNodes.removeAll(allNodes);
		otherNodes.removeIf(d->d.getElement() instanceof Lifeline);
		return otherNodes;
	}
	
	public static void removeNodes(InteractionGraph interactionGraph, List<Node> nodes) {
		for (Node n : nodes) {
			if (n.getParent() != null)
				((ClusterImpl)n.getParent()).removeNode((NodeImpl)n);
			//((NodeImpl)n).disconnectNode();
		}		
		interactionGraph.layout();
	}
	
	public static void insertNodes(InteractionGraph interactionGraph, List<Node> nodes, Cluster targetCluster, Node insertBefore, int yPos) {
		Rectangle area = getArea(nodes);
		
		int orgPosY = area.y;
		for (Node n : nodes) {			
			if (n != insertBefore) {
				((ClusterImpl)targetCluster).addNode((NodeImpl)n, insertBefore);
			}
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

	public static void moveNodes(InteractionGraph interactionGraph, List<Node> nodes, Cluster targetCluster, Node insertBefore, int yPos) {
		Rectangle area = getArea(nodes);
		
		int orgPosY = area.y;
		for (Node n : new ArrayList<>(nodes)) {			
			if (n != insertBefore) {
				((ClusterImpl)n.getParent()).removeNode((NodeImpl)n);
				((ClusterImpl)targetCluster).addNode((NodeImpl)n, insertBefore);
			}
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
			return null;//	new Rectangle(0,0,-1,-1);
		
		r.width = Math.max(1,r.width);
		r.height = Math.max(1,r.height);			

		return r;
	}
	
	public static Cluster getClusterAt(Cluster cluster, Point p) {
		Node n = getNodeAt(cluster, p);
		if (n == null)
			return null;
		return n instanceof Cluster ? (Cluster)n : n.getParent(); 
	}
	
	public static Node getNodeAt(Cluster cluster, Point p) {
		if (!isNodeAt(cluster, p))
			return null;
		Node n = cluster;
		while (n instanceof Cluster) {
			Node child = ((Cluster)n).getNodes().stream().filter(d->isNodeAt(d, p)).findAny().orElse(null);
			if (child == null || child == n)
				break;
			n = child;
		}
		return n;
	}

	public static Cluster getClusterAtVerticalPos(Cluster cluster, int y) {
		Node n = getNodeAtVerticalPos(cluster, y);
		if (n == null)
			return null;
		return n instanceof Cluster ? (Cluster)n : n.getParent(); 
	}
	

	public static Node getNodeAtVerticalPos(Cluster cluster, int y) {
		if (!isNodeAtVerticalPos(cluster,y))
			return null;
		Node n = cluster;
		while (n instanceof Cluster) {
			Node child = ((Cluster)n).getNodes().stream().filter(d->isNodeAtVerticalPos(d,y)).findAny().orElse(null);
			if (child == null || child == n)
				break;
			n = child;
		}
		return n;
	}
	
	public static Cluster getClusterAtHorizontalPos(Cluster cluster, int x) {
		Node n = getNodeAtHorizontalPos(cluster, x);
		if (n == null)
			return null;
		return n instanceof Cluster ? (Cluster)n : n.getParent(); 
	}
	

	public static Node getNodeAtHorizontalPos(Cluster cluster, int x) {
		if (!isNodeAtHorizontalPos(cluster,x))
			return null;
		Node n = cluster;
		while (n instanceof Cluster) {
			Node child = ((Cluster)n).getNodes().stream().filter(d->isNodeAtHorizontalPos(d,x)).findAny().orElse(null);
			if (child == null || child == n)
				break;
			n = child;
		}
		return n;
	}

	public static boolean isNodeAt(Node n, Point p) {
		return n.getBounds() != null && n.getBounds().contains(p);
		
	}
	
	public static boolean isNodeAtVerticalPos(Node n, int y) {
		return n.getBounds() != null && n.getBounds().y <= y && y <= n.getBounds().getBottom().y;
	}

	public static boolean isNodeAtHorizontalPos(Node n, int x) {
		return n.getBounds() != null && n.getBounds().x <= x && x <= n.getBounds().getBottom().x;
	}
	
	public static Node getPreviousVerticalNode(Cluster cluster, int y) {
		Node n = null;
		for (Node child : cluster.getAllNodes()) {
			if (child.getBounds() == null || child.getBounds().getBottom().y >= y)
				break;
			n = child;
			
		}
		return n;
	}

	public static Node getNextVerticalNode(Cluster cluster, int y) {
		Node n = null;
		for (Node child : cluster.getAllNodes()) {
			if (child.getBounds() != null && child.getBounds().y < y)
				continue;
			n = child;
			break;			
		}
		return n;
	}

	public static Rectangle getNudgeArea(InteractionGraphImpl graph, List<Node> nodesToNudge, boolean horizontal, boolean vertical) {
		Set<Node> vLimitNodes = null;
		Set<Node> hLimitNodes = null;
		for (Node n : nodesToNudge) {
			if (vertical) {
				if (vLimitNodes == null)
					vLimitNodes = new HashSet<Node>();
				Row row = n.getRow();
				vLimitNodes.addAll(row.getNodes());
							
				if (row.getIndex() > 0)
					vLimitNodes.addAll(graph.getRows().get(row.getIndex()-1).getNodes());
			}
			
			if (horizontal) {
				if (hLimitNodes == null)
					hLimitNodes = new HashSet<Node>();
				Column col = n.getColumn();
				hLimitNodes.addAll(col.getNodes());
							
				if (col.getIndex() > 0)
					hLimitNodes.addAll(graph.getColumns().get(col.getIndex()-1).getNodes());				
			}			
		}
		
		if (vertical)
			vLimitNodes.removeAll(nodesToNudge);
		if (horizontal)
			hLimitNodes.removeAll(nodesToNudge);
		
		int minY = Integer.MIN_VALUE; 
		if (vLimitNodes != null) {
			List<Node> createLifelines = nodesToNudge.stream().filter(NodeUtilities::isCreateOcurrenceSpecification)
					.map(NodeUtilities::getLifelineNode).collect(Collectors.toList());
			List<Node> lifelines = vLimitNodes.stream().filter(d->d.getElement() instanceof Lifeline).collect(Collectors.toList());
			List<Node> execSpecs = vLimitNodes.stream().filter(d->d.getElement() instanceof ExecutionSpecification).
					filter(d->nodesToNudge.contains(d.getParent().getConnectedByNode())).collect(Collectors.toList());
			
			vLimitNodes.removeAll(lifelines);
			vLimitNodes.removeAll(execSpecs);
			for (Node n : lifelines) {
				Rectangle clientArea = ViewUtilities.getClientAreaBounds(graph.getEditPartViewer(),n.getView());
				if (!createLifelines.contains(n))
				minY = Math.max(minY, clientArea.y);
			}
		}
				
		Rectangle validArea = getEmptyArea(graph, horizontal ? new ArrayList<>(hLimitNodes) : null, 
					vertical ? new ArrayList<>(vLimitNodes) : null, null, null);
			
		if (vLimitNodes != null) {
			validArea.y = Math.max(minY, validArea.y);
		}
		validArea.shrink(3, 3);
		return validArea;
	}
	
	public static boolean isCreateOcurrenceSpecification(Node node) {
		Element el = node.getElement();
		if (!MessageOccurrenceSpecification.class.isInstance(el))
			return false;
		Message msg = ((MessageOccurrenceSpecification)el).getMessage();
		return msg.getReceiveEvent() == el && msg.getMessageSort() == MessageSort.CREATE_MESSAGE_LITERAL;		
	}	

	public static boolean isDestroyOcurrenceSpecification(Node node) {
		Element el = node.getElement();
		return (el instanceof DestructionOccurrenceSpecification);
	}	

	public static boolean isNodeLifelineEndsWithDestroyOcurrenceSpecification(Node node) {
		Cluster lifeline = getLifelineNode(node);
		List<Node> nodes = lifeline.getNodes();
		if (nodes.isEmpty())
			return false;
		return isDestroyOcurrenceSpecification(nodes.get(nodes.size()-1));
	}	
}
