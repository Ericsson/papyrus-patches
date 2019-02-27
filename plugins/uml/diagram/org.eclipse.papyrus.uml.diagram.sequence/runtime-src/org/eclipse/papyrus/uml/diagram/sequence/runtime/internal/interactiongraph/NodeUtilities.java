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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Column;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Link;
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
import org.eclipse.uml2.uml.NamedElement;
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
		List<Node> flatNodes = nodes.stream().map(NodeImpl.class::cast).
				flatMap(d -> { 
					List<Node> l = new ArrayList<Node>();
					l.add(d);
					if (d instanceof Cluster)
						l.addAll(flattenKeepClusters(((Cluster)d).getNodes()));
					return l.stream();
				}).collect(Collectors.toList());
		
		return flatNodes;
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

	public static final List<Node> removeDuplicated(List<Node> l) {
		return new ArrayList<>(new LinkedHashSet<>(l));
	}
	
	public static List<List<Node>> getBlocks(List<Node> sources) {
		Set<Node> flat = new HashSet<>();
		List<List<Node>> blocks = new ArrayList<>();
		
		for (Node n : sources) {
			List<Node> block = getBlock(n);
			if (!flat.containsAll(block)) {
				blocks.add(block);
				flat.addAll(NodeUtilities.flattenKeepClusters(block));
			}
		}
		
		return blocks;
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
			
			if (!c.getNodes().isEmpty()) {
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
			List<Node> rowNodes = interactionGraph.getRows().get(i).getNodes().stream().
					filter(d->!NodeUtilities.isLifelineNode(d)).collect(Collectors.toList());
			otherNodes.addAll(rowNodes);
			// Check if the nodes in the row are previous the ones in the block. Use the comparator.
			if (!allNodes.containsAll(rowNodes)) {
				firstRowWithOtherContent = Math.min(firstRowWithOtherContent, i);
				lastRowWithOtherContent = Math.max(lastRowWithOtherContent, i);
			}
		}
		otherNodes.removeAll(allNodes);
		otherNodes.removeIf(d->d.getElement() instanceof Lifeline);
		
		// Remove Nodes that are before or after the nodes.
		List<Node> orderedNodes = interactionGraph.getOrderedNodes();
		int min = allNodes.stream().map(orderedNodes::indexOf).filter(d->d>=0).min(Integer::compare).orElse(0);
		int max = allNodes.stream().map(orderedNodes::indexOf).filter(d->d>=0).max(Integer::compare).orElse(Integer.MAX_VALUE);
		otherNodes.removeIf(d->{ int i= orderedNodes.indexOf(d); return (i< min || i > max);});
		return otherNodes;
	}
	
	public static int getLinkSlope(Link lnk) {
		return getLinkSlope(lnk.getSource(), lnk.getTarget());
	}
	
	public static int getLinkSlope(Node srcNode, Node trgNode) {
		Rectangle b1 = srcNode == null ? null : srcNode.getBounds();
		Rectangle b2 = trgNode == null ? null : trgNode.getBounds();
		if (b1 == null || b2 == null)
			return 0;
		
		return b2.y - b1.y;
	}
	
	public static boolean isSelfLink(Link lnk) {
		return NodeUtilities.getLifelineNode(lnk.getSource()) == NodeUtilities.getLifelineNode(lnk.getTarget());		
	}
	
	public static Link getStartLink(Link finishLink) {
		Node n = finishLink.getSource();
		if (n != getFinishNode(n.getParent()))
				return null;
		
		return getStartLink(n.getParent()); 
	}
	
	public static Node getStartNode(Cluster cluster) {
		List<Node> nodes = cluster.getAllNodes();
		if (nodes.isEmpty())
			return null;
		return nodes.get(0);				
	}
	
	public static Link getStartLink(Cluster cluster) {
		Link lnk = cluster.getConnectedByLink();
		if (lnk == null || lnk.getSource().getConnectedNode() != cluster)
			return null;
		return lnk;
		
	}
	
	public static Link getFinishLink(Link startLink) {
		Node n = startLink.getTarget();
		if (n != getStartNode(n.getParent()))
			return null;
		
		return getFinishLink(n.getParent()); 
	}

	public static boolean isFinishNode(Node node) {
		return getFinishNode(node.getParent()) == node && NodeUtilities.getLifelineNode(node) != node.getParent();
	}
	
	public static Node getFinishNode(Cluster cluster) {
		List<Node> nodes = cluster.getNodes();
		if (nodes.isEmpty())
			return null;
		return nodes.get(nodes.size()-1);				
	}
	
	public static Link getFinishLink(Cluster cluster) {
		Node n = getFinishNode(cluster);
		if (n == null)
			return null;
		Link lnk = n.getConnectedByLink();
		if (lnk == null || lnk.getSource() != n)
			return null;
		return lnk;
	}
	
	public static void removeNodes(InteractionGraph interactionGraph, List<Node> nodes) {
		for (Node n : nodes) {
			if (n.getParent() != null)
				((ClusterImpl)n.getParent()).removeNode((NodeImpl)n);
			else if (interactionGraph.getLifelineClusters().contains(n)) {
				((InteractionGraphImpl)interactionGraph).removeLifelineCluster((ClusterImpl)n);
			}
		}		
		interactionGraph.layout();
	}
	
	public static void removeMessageLinks(InteractionGraph interactionGraph, List<Link> links) {
		for (Link ln : links) {
			LinkImpl impl = (LinkImpl)ln;
			((InteractionGraphImpl)interactionGraph).removeMessage(impl);
			impl.getSource().connectingLink = null;
			impl.getTarget().connectingLink = null;
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

	public static void nudgeNodes(Node n, int xDelta, int yDelta) {
		n.getBounds().x += xDelta;
		n.getBounds().y += yDelta;
	}
	
	public static void nudgeNodes(List<Node> nodes, int xDelta, int yDelta) {
		for (Node n : nodes) {
			n.getBounds().x += xDelta;
			n.getBounds().y += yDelta;
			((InteractionGraphImpl)nodes.get(0).getInteractionGraph()).layout();
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
		
		//r.width = Math.max(1,r.width);
		//r.height = Math.max(1,r.height);			

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

	public static Column getColumnAt(InteractionGraph graph, int x) {
		return graph.getColumns().stream().filter(d->d.getXPosition() == x).findFirst().orElse(null);
	}

	public static Row getRowAt(InteractionGraph graph, int y) {
		return graph.getRows().stream().filter(d->d.getYPosition() == y).findFirst().orElse(null);
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

	public static final List<Node> getNodesAfter(InteractionGraph graph, List<Node> nodes) {
		List<Node> orderedNodes = graph.getOrderedNodes();
		int max = nodes.stream().map(orderedNodes::indexOf).filter(d->d>=0).max(Integer::compare).orElse(Integer.MAX_VALUE);
		orderedNodes.removeIf(d->{ int i= orderedNodes.indexOf(d); return (i <= max);});
		return orderedNodes;
	}
	
	public static final List<Node> getNodesAfterVerticalPos(InteractionGraph graph, int y) {
		List<Node> orderedNodes = graph.getOrderedNodes();
		orderedNodes.removeIf(d->d.getBounds().y <= y);
		return orderedNodes;
	}

	public static Rectangle getNudgeArea(InteractionGraphImpl graph, List<Node> nodesToNudge, boolean horizontal, boolean vertical) {
		return getNudgeArea(graph, nodesToNudge, horizontal, vertical, null); 
	}
	
	public static Rectangle getNudgeArea(InteractionGraphImpl graph, List<Node> nodesToNudge, boolean horizontal, boolean vertical, List<Node> excludeNodes) {
		Set<Node> vLimitNodes = null;
		Set<Node> hLimitNodes = null;
		for (Node n : nodesToNudge) {
			if (vertical) {
				if (vLimitNodes == null)
					vLimitNodes = new HashSet<Node>();
				Row row = n.getRow();
				vLimitNodes.addAll(row.getNodes());
							
				while (row.getIndex() > 0) {
					Row prevRow = graph.getRows().get(row.getIndex()-1);
					List<Node> rowNodes = prevRow.getNodes();
					if (excludeNodes == null || !excludeNodes.containsAll(rowNodes)) {
						vLimitNodes.addAll(rowNodes);
						break;
					}
					row = prevRow;
				}
				
				if (excludeNodes != null)
					vLimitNodes.removeAll(excludeNodes);
			}
			
			if (horizontal) {
				if (hLimitNodes == null)
					hLimitNodes = new HashSet<Node>();
				Column col = n.getColumn();
				hLimitNodes.addAll(col.getNodes());
							
				while (col.getIndex() > 0) {
					Column prevCol = graph.getColumns().get(col.getIndex()-1);
					List<Node> colNodes = prevCol.getNodes();
					if (excludeNodes == null || !excludeNodes.containsAll(colNodes)) {
						hLimitNodes.addAll(colNodes);
						break;
					}
					col = prevCol;
				}
				
				if (excludeNodes != null)
					hLimitNodes.removeAll(excludeNodes);
			}			
		}
		
		if (vertical && vLimitNodes != null)
			vLimitNodes.removeAll(nodesToNudge);
		if (horizontal && hLimitNodes != null)
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
				
		Rectangle validArea = getEmptyArea(graph, horizontal && hLimitNodes != null ? new ArrayList<>(hLimitNodes) : null, 
					vertical && vLimitNodes != null ? new ArrayList<>(vLimitNodes) : null, null, null);
			
		if (vLimitNodes != null) {
			validArea.y = Math.max(minY, validArea.y);
		}
		validArea.shrink(3, 3);
		return validArea;
	}
	
	public static boolean isLifelineNode(Node node) {
		return getLifelineNode(node) == node;		
	}	

	public static boolean isCreateOcurrenceSpecification(Node node) {
		Element el = node.getElement();
		if (!MessageOccurrenceSpecification.class.isInstance(el))
			return false;
		Message msg = ((MessageOccurrenceSpecification)el).getMessage();
		return msg.getReceiveEvent() == el && msg.getMessageSort() == MessageSort.CREATE_MESSAGE_LITERAL;		
	}	

	public static boolean isNodeLifelineStartByCreateMessage(Node node) {
		Cluster lifeline = getLifelineNode(node);
		List<Node> nodes = lifeline.getNodes();
		if (nodes.isEmpty())
			return false;
		return isCreateOcurrenceSpecification(nodes.get(0));
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
	
	public static String getNewElementName(InteractionGraph graph, Element element) {
		EClass eClass = element.eClass(); 
		return getNewElementName(graph, eClass);
	}
	
	public static String getNewElementName(InteractionGraph graph, EClass eClass) {
		String prefix = eClass.getName();
		return getNewElementName(graph, eClass, prefix);
	}

	public static String getNewElementName(InteractionGraph graph, EClass eClass, String prefix) {
		int nCount = Stream.concat(graph.getMessageLinks().stream().map(Link::getElement), 
								   graph.getLayoutNodes().stream().map(Node::getElement)).		
				filter(d -> eClass.isInstance(d)).
				filter(NamedElement.class::isInstance).map(d -> NamedElement.class.cast(d).getName()).
				filter(d -> d.matches(prefix+"[0-9]*")).
				map(d-> d.equals(prefix) ? 1 : Integer.parseInt(d.replaceFirst(prefix, ""))).						
				sorted(Comparator.reverseOrder()).findFirst().orElse(0)+1;
		if (nCount > 1)
			return prefix+nCount;
		else
			return prefix;
	}
}
