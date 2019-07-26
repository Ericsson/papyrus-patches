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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IBorderItemEditPart;
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
import org.eclipse.uml2.uml.Gate;
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

	public static boolean areNodesHorizontallyConnected(Node n1, Node n2) {
		return Math.abs(getYPos((NodeImpl)n1) - getYPos((NodeImpl)n2)) < THRESHOLD_HORIZONTAL_DEPS; 
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
	
	public static Cluster getTopLevelCluster(Node impl) {
		while (impl.getParent() != null) {
			impl = impl.getParent();
		}
		
		return (Cluster)impl;
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
	
	public static List<Node> flattenKeepClusters(Node node) {
		return flattenKeepClusters(Arrays.asList(node));
	}
	
	public static List<Node> flattenKeepClusters(List<? extends Node> nodes) {		
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

	public static List<Node> flatten(List<? extends Node> nodes) {
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
			List<FragmentClusterImpl> l = new ArrayList<>();
			l.add(cluster);
			l.addAll(cluster.getOwnedFragmentClusters().stream().map(FragmentClusterImpl.class::cast).
					flatMap(NodeUtilities::flattenImpl).collect(Collectors.toList()));
			return l.stream();
		} else {
			return Collections.singleton(cluster).stream();
		}
	}

	public static final <N extends Node> List<N> removeDuplicated(List<N> l) {
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
			if (c instanceof FragmentCluster) {
				FragmentCluster fc = (FragmentCluster)c;
				nodes.addAll(fc.getAllGates());
				for (Cluster nn : fc.getClusters()) {
					getBlock(nn, nodes);
				}				
				nodes.remove(fc);				
			} else {
				if (c.getFragmentCluster() != null)
					getBlock(c.getFragmentCluster(), nodes);
				for (Node nn : c.getNodes()) {
					getBlock(nn, nodes);
				}
				
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

	public static final List<List<Link>> getBlocksMessageLinks(List<Node> sources) {
		List<List<Node>> blocks = getBlocks(sources);
		return blocks.stream().map(d->NodeUtilities.getMessageLinks(d)).collect(Collectors.toList());
	}

	public static final List<Link> getBlockMessageLinks(Node node) {
		List<Node> block = getBlock(node);
		return getMessageLinks(block);
	}

	public static final List<Link> getMessageLinks(List<Node> nodes) {
		return new ArrayList<Link>(NodeUtilities.flattenKeepClusters(nodes).stream().map(Node::getConnectedByLink).
				filter(Predicate.isEqual(null).negate()).collect(Collectors.toSet()));		
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
		Cluster lf1 = NodeUtilities.getLifelineNode(lnk.getSource());
		Cluster lf2 = NodeUtilities.getLifelineNode(lnk.getTarget());
		return  lf1 == lf2 && lf1 != null;		
	}
	
	public static Link getStartLink(Link finishLink) {
		Node n = finishLink.getSource();
		if (n != getFinishNode(n.getParent()))
				return null;
		
		return getStartLink(n.getParent()); 
	}
	
	public static boolean isStartNode(Node node) {
		Cluster lf = NodeUtilities.getLifelineNode(node);
		return getStartNode(node.getParent()) == node &&  lf != node.getParent() && lf != null;
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
		Cluster lifelineNode = NodeUtilities.getLifelineNode(node);
		return getFinishNode(node.getParent()) == node && lifelineNode != node.getParent() & lifelineNode != null;
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
	
	public static void deleteNodes(InteractionGraph interactionGraph, List<? extends Node> nodes) {
		((InteractionGraphImpl)interactionGraph).disableLayout();
		nodes.forEach(d -> deleteNode(interactionGraph,d));
		((InteractionGraphImpl)interactionGraph).enableLayout();
		interactionGraph.layout();
	}

	public static void deleteNode(InteractionGraph interactionGraph, Node node) {
		// Remove additional references.
		NodeImpl n = (NodeImpl)node;
		removeNode(interactionGraph, n);
		n.disconnectNode();
		
		if (n instanceof Cluster) {
			ClusterImpl c = (ClusterImpl)n;
			FragmentClusterImpl frgCluster = c.getFragmentCluster(); 
			if (frgCluster != null) {
				frgCluster.removeCluster(c);
				if (frgCluster.getClusters().isEmpty()) {
					((InteractionGraphImpl)interactionGraph).removeFragmentCluster(frgCluster);
				}
			}
		}
	}

	public static void addNode(InteractionGraph  interactionGraph, Cluster parent, Node child, Node beforeNode) {
		if (parent instanceof FragmentCluster) {
			FragmentClusterImpl fc = (FragmentClusterImpl)parent;
			if (child instanceof FragmentCluster) {
				fc.addFragmentCluster((FragmentClusterImpl)child, (FragmentClusterImpl)beforeNode);
			} else if (child.getElement() instanceof Gate) {
				Gate gate = (Gate)child.getElement();
				if (fc.getFragmentCluster() != null || fc == interactionGraph) {
					fc.addInnerGate((NodeImpl)child, (NodeImpl)beforeNode);
				} else {
					fc.addOuterGate((NodeImpl)child, (NodeImpl)beforeNode);
				}
			} else if (fc == interactionGraph && child.getElement() instanceof Lifeline) {
				if (interactionGraph.getLifelineClusters().contains(child)) {
					((InteractionGraphImpl)interactionGraph).addLifelineCluster((ClusterImpl)child, (ClusterImpl)beforeNode);					
				}
			}
		} else if (parent != null) {
			((ClusterImpl)parent).addNode((NodeImpl)child, beforeNode);
		}		
	}

	public static void removeNodes(InteractionGraph interactionGraph, List<? extends Node> nodes) {
		((InteractionGraphImpl)interactionGraph).disableLayout();
		nodes.forEach(d -> removeNode(interactionGraph,d));
		((InteractionGraphImpl)interactionGraph).enableLayout();
		interactionGraph.layout();
	}
	
	public static void removeNode(InteractionGraph  interactionGraph, Node n) {
		if (n.getParent() instanceof FragmentCluster) {
			FragmentClusterImpl fc = (FragmentClusterImpl)n.getParent();
			if (fc.getOwnedFragmentClusters().contains(n)) {
				fc.removeFragmentCluster((FragmentCluster)n);
			} else if (fc.getInnerGates().contains(n)) {
				fc.removeInnerGate((NodeImpl)n);
			} else if (fc.getOuterGates().contains(n)) {
				fc.removeOuterGate((NodeImpl)n);
			}
		} else if (n.getParent() != null) {
			((ClusterImpl)n.getParent()).removeNode((NodeImpl)n);
		} else {
			if (interactionGraph.getLifelineClusters().contains(n)) {
				((InteractionGraphImpl)interactionGraph).removeLifelineCluster((Cluster)n);					
			}			
		}
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

	public static List<Node> preserveFragmentClusterBlocksFromRemoval(InteractionGraph interactionGraph, List<Cluster> fragments, List<Node> allNodesBeingRemoved) {
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

	public static void moveNodeBlock(InteractionGraph interactionGraph, List<Node> nodes, int yPos) {
		moveNodeBlock(interactionGraph, nodes, yPos, Collections.emptyMap());
	}
	
	public static void moveNodeBlock(InteractionGraph interactionGraph, List<Node> nodes, int yPos, Map<Node, Cluster> toCluters) {
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
		removeNodeBlockImpl(interactionGraph, nodes,otherNodes,0);		
		
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
		
		addNodeBlock(interactionGraph, nodesByLifeline, newYPos, postInsertionNudge);
	}
	
	public static void removeNodeBlocks(InteractionGraph interactionGraph, List<List<Node>> blocks) {
		for (List<Node> block : blocks) {
			removeNodeBlock(interactionGraph, block);
		}
	}
	
	public static void removeNodeBlock(InteractionGraph interactionGraph, List<Node> nodes) {
		Rectangle r = NodeUtilities.getArea(nodes);
		Node nodeAfter = NodeUtilities.getNodeAfterVerticalPos(interactionGraph, r.bottom());		
		Row nextRow = nodeAfter == null ? null : nodeAfter.getRow();
		// TODO: @etxacam Check if we need to do it always... (Remove space to the next row.)
		int nudge = nextRow == null ? 0 : nextRow.getYPosition() - r.bottom();
		
		List<Node> allNodes = NodeUtilities.removeDuplicated(NodeUtilities.flattenKeepClusters(nodes));
		List<Node> others = NodeUtilities.getBlockOtherNodes(nodes);

		// Preserve Fragment clusters blocks
		List<Cluster> fragments = allNodes.stream().filter(Cluster.class::isInstance).map(Cluster.class::cast).filter(d->d.getFragmentCluster() != null).
				collect(Collectors.toList());
		removeNodeBlockImpl(interactionGraph, nodes, others, (others != null && !others.isEmpty()) ? 0 : nudge);
		allNodes.removeAll(NodeUtilities.flattenKeepClusters(fragments));
		NodeUtilities.deleteNodes(interactionGraph, allNodes); // Delete all references
	}
	
	private static void removeNodeBlockImpl(InteractionGraph interactionGraph, List<Node> nodes, List<Node> otherNodes, int extraNudge) {
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
		
		((InteractionGraphImpl)interactionGraph).disableLayout();
		try {
			NodeUtilities.removeNodes(interactionGraph, nodes);			
			List<Node> nodesAfter = interactionGraph.getLayoutNodes().stream().filter(d->!allNodes.contains(d) && d.getBounds().y >= blockEndPoint).collect(Collectors.toList());
			Rectangle nodesAfterArea = NodeUtilities.getArea(nodesAfter);
			
			// Check how much can we nudge up...
			Rectangle nudgeArea = NodeUtilities.getNudgeArea((InteractionGraphImpl)interactionGraph, otherNodes, false, true, allNodes);
			int maxNudgePrev = prev;
			int gridSpacing = interactionGraph.getGridSpacing();
			if (nudgeArea != null && otherArea != null)
				maxNudgePrev = (otherArea.y - nudgeArea.y) / gridSpacing * gridSpacing;
			
			nudgeArea = NodeUtilities.getNudgeArea((InteractionGraphImpl)interactionGraph, nodesAfter, false, true, allNodes);
			int maxNudgeAfter = nudge; 
			if (nudgeArea != null && nodesAfterArea != null)
				maxNudgeAfter = (nodesAfterArea.y - nudgeArea.y) / gridSpacing * gridSpacing;

			int nudgeUpOthers = otherArea == null ? 0 : Math.max(0, Math.min(prev, maxNudgePrev));
			nudgeUpOthers = -NodeUtilities.nudgeNodes(otherNodes, 0, -nudgeUpOthers).height;			
			NodeUtilities.nudgeNodes(nodesAfter, 0, -Math.max(0, Math.min(nudge, maxNudgeAfter) + nudgeUpOthers ));
		} finally {
			((InteractionGraphImpl)interactionGraph).enableLayout();
			interactionGraph.layout();
		}
	}
	
	public void addNodeBlock(InteractionGraph interactionGraph, Map<Cluster,List<Node>> nodesByLifelines, int yPos) {
		addNodeBlock(interactionGraph, nodesByLifelines, yPos, 0);
	}
	
	// TODO: @etxacam generalize lifelineXXXX variables to parentClusterXXXX => Apply to all blocks functions
	public static void addNodeBlock(InteractionGraph interactionGraph, Map<Cluster,List<Node>> nodesByTopClusters, int yPos, int extraNudge) {
		List<Node> nodes = nodesByTopClusters.values().stream().flatMap(d->d.stream()).collect(Collectors.toList());
		//Map<Cluster,List<Node>> allNodes = NodeUtilities.flattenKeepClusters(nodes);
		int gridSpacing = interactionGraph.getGridSpacing(); 
		Rectangle totalArea = NodeUtilities.getArea(nodes);
		List<Node> firstNodes = nodes.stream().filter(d->d.getBounds() != null && d.getBounds().y == totalArea.y).collect(Collectors.toList());		
		Row row = interactionGraph.getRows().stream().filter(d->Math.abs(yPos - d.getYPosition()) <= 3).findFirst().orElse(null);
		if (row != null) {
			boolean nudgeOverlap = true;
			for (Node n : firstNodes) {
				if (NodeUtilities.flatten(NodeUtilities.getBlock(n)).stream().anyMatch(row.getNodes()::contains)) {
					nudgeOverlap = false;
					break;
				}
//				NodeUtilities.nudgeNodes(Arrays.asList(n), 0,gridSpacing);				
			}

			if (nudgeOverlap) {				
				NodeUtilities.nudgeRows(interactionGraph.getRows().subList(row.getIndex(), interactionGraph.getRows().size()), gridSpacing);
			}
		}

		// Make place for the block.
		List<Node> nodesAfter = interactionGraph.getLayoutNodes().stream().filter(d->d.getBounds().y > yPos).collect(Collectors.toList());
		((InteractionGraphImpl)interactionGraph).disableLayout();
		try {
			NodeUtilities.nudgeNodes(nodesAfter, 0, totalArea.height);
			NodeUtilities.nudgeNodes(nodesAfter, 0, extraNudge);
		} finally {
			((InteractionGraphImpl)interactionGraph).enableLayout();
			interactionGraph.layout();
		}
		
		((InteractionGraphImpl)interactionGraph).disableLayout();
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
		
					NodeUtilities.insertNodes(interactionGraph, ns, target, insertBefore, yPos + (nodesArea.y - totalArea.y));					
					
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
		
					NodeUtilities.insertNodes(interactionGraph, ns, target, insertBefore, yPos + (nodesArea.y - totalArea.y));					
				}
			}
		} finally {
			((InteractionGraphImpl)interactionGraph).enableLayout();
			interactionGraph.layout();
		}	
	}

	public static void reArrangeSelfMessages(InteractionGraph interactionGraph, List<Link> previousSelfLinks, List<Link> newSelfLinks) {
		List<Link> selfLinksToStraight = new ArrayList<>(previousSelfLinks);
		selfLinksToStraight.removeAll(newSelfLinks);
		
		List<Link> linksToSelfLinks = new ArrayList<>(newSelfLinks);
		linksToSelfLinks.removeAll(previousSelfLinks);
		
		// Nudge Target Links for new Self messages.
		List<Node> nodesToNudge = linksToSelfLinks.stream().filter(d -> NodeUtilities.getLinkSlope(d) < 3).map(Link::getTarget).
			map(d-> d instanceof Cluster ? ((Cluster)d).getNodes().get(0) : d).sorted(RowImpl.NODE_VPOSITION_COMPARATOR).
			collect(Collectors.toList());
		int gridSpacing = interactionGraph.getGridSpacing();
		if (!nodesToNudge.isEmpty()) {
			int nudge = 0;
			Iterator<Node> it = nodesToNudge.iterator();
			Node nextNode = it.next();
			for (Node n : interactionGraph.getOrderedNodes()) {
				if (nudge == 0)
				if (n == nextNode) {
					nudge += gridSpacing;
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
			for (Node n : interactionGraph.getOrderedNodes()) {
				if (n == nextNode) {
					nudge -= gridSpacing;
					nextNode = !it.hasNext() ? null : it.next();
				} 
				NodeUtilities.nudgeNodes(n, 0, nudge);
			}			
		}
	}

	public static void insertNodes(InteractionGraph interactionGraph, List<Node> nodes, Cluster targetCluster, Node insertBefore, int yPos) {
		Rectangle area = getArea(nodes);
		
		int orgPosY = area.y;
		for (Node n : nodes) {			
			if (n != insertBefore) {
				addNode(interactionGraph, targetCluster, n, insertBefore);
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
		nudgeNodes(Collections.singletonList(n), xDelta, yDelta);
	}
	
	public static Dimension nudgeNodes(List<? extends Node> nodes, int xDelta, int yDelta) {
		if (nodes.isEmpty())
			return new Dimension(xDelta,yDelta);
		InteractionGraphImpl graph = ((InteractionGraphImpl)nodes.get(0).getInteractionGraph());
		
		int newYDelta = yDelta;
		Set<Cluster> parents = nodes.stream().map(Node::getParent).collect(Collectors.toSet());
		// Check how much we can nudge without break parents minimum size.
		for (Cluster c : parents) {
			Dimension minSize = graph.getLayoutManager().getMinimumSize((ClusterImpl)c);
			Dimension maxSize = graph.getLayoutManager().getMaximumSize((ClusterImpl)c);
			
			Node startNode = getStartNode(c);
			Node endNode = getFinishNode(c);
			if (startNode == null || endNode == null)
				continue;
			
			if (nodes.contains(startNode) && nodes.contains(endNode))
				continue;
			// We do not use the parent bounds as it may not be recalculated.
			int height = endNode.getBounds().y - startNode.getBounds().y;
			if (nodes.contains(startNode)) {
				if (height - yDelta < minSize.height) {
					newYDelta = Math.min(newYDelta, Math.max(0, height - minSize.height));
				}

				if (height - yDelta > maxSize.height) {
					newYDelta = Math.max(newYDelta, Math.max(0, minSize.height - height));
				}
			}
			
			if (nodes.contains(endNode)) {
				if (height + yDelta < minSize.height) {
					newYDelta = Math.max(newYDelta, - Math.max(0, height - minSize.height));
				}				

				if (height + yDelta > maxSize.height) {
					newYDelta = Math.min(newYDelta, Math.max(0, minSize.height - height));
				}
			}
		}
		
		yDelta = newYDelta;
		for (Node n : nodes) {
			n.getBounds().x += xDelta;
			n.getBounds().y += yDelta;
		}		

		graph.layout();
		
		return new Dimension(xDelta, yDelta);
	}

	public static void nudgeRows(List<? extends Row> rows, int yDelta) {
		if (rows.isEmpty())
			return;
		InteractionGraphImpl graph = ((RowImpl)rows.get(0)).getInteractionGraph();
		
		((InteractionGraphImpl)graph).disableLayout();
		List<Node> nodes = rows.stream().flatMap(d->d.getNodes().stream()).collect(Collectors.toList());
		nudgeNodes(nodes, 0, yDelta);
		((InteractionGraphImpl)graph).enableLayout();
	}

	public static Rectangle getEmptyAreaAround(InteractionGraphImpl interactionGraph, List<? extends Node> nodes) {
		List<Node> allNodes = NodeUtilities.flatten(nodes);
		int minRow = Integer.MAX_VALUE;
		int maxRow = Integer.MIN_VALUE;
		int minCol = Integer.MAX_VALUE;
		int maxCol = Integer.MIN_VALUE;		
		
		for (Node n : allNodes) {
			Row row = n.getRow();
			if (row != null) {
				minRow = Math.min(minRow, row.getIndex());
				maxRow = Math.max(maxRow, row.getIndex());
			}			

			Column col = n.getColumn();
			if (col != null) {				
				minCol = Math.min(minCol, col.getIndex());
				maxCol = Math.max(maxCol, col.getIndex());				
			}
		}
		
		int leftCol = Integer.MIN_VALUE;
		int rightCol = Integer.MAX_VALUE;		
		Column leftColumn = null;
		Column rightColumn = null;
		for (int r=minRow; r<=maxRow; r++) {
			Row row = interactionGraph.getRows().get(r);
			List<Node> otherNodes = row.getNodes().stream().filter(d->!allNodes.contains(d)).collect(Collectors.toList());
			for (Node n : otherNodes) {
				Column col = n.getColumn();
				if (col != null) {
					if (col.getIndex() < minCol && col.getIndex() > leftCol) {
						leftCol = col.getIndex();
						leftColumn = col;
					}
					if (col.getIndex() > maxCol && col.getIndex() < rightCol) {
						rightCol = col.getIndex();
						rightColumn = col;
					}
				}
			}
		}
		
		View interactionView = interactionGraph.getInteractionView();
		Rectangle interactionRect =  ViewUtilities.getClientAreaBounds(interactionGraph.getEditPartViewer(), interactionView);
		Row topRow = interactionGraph.getRows().get(minRow);
		if (allNodes.containsAll(topRow.getNodes())) {
			if (topRow.getIndex() == 0)
				topRow = null;
			else
				topRow = interactionGraph.getRows().get(minRow -1);
		}
		int minY = topRow != null ? topRow.getYPosition() : interactionRect.y;
		
		int nRows = interactionGraph.getRows().size();
		Row bottomRow = interactionGraph.getRows().get(maxRow);
		if (allNodes.containsAll(bottomRow.getNodes())) {
			if (bottomRow.getIndex() == nRows-1)
				bottomRow = null;
			else if (maxRow+1 >= interactionGraph.getRows().size())
				bottomRow = null;
			else 
				bottomRow = interactionGraph.getRows().get(maxRow +1);
		}
		int maxY = bottomRow != null ? bottomRow.getYPosition() : (interactionRect.y + interactionRect.height);
		
		int minX = leftColumn != null ? leftColumn.getXPosition() : interactionRect.x;
		int maxX = rightColumn != null ? rightColumn.getXPosition() : (interactionRect.x + interactionRect.width);
		return new Rectangle(minX,minY,maxX-minX,maxY-minY);
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
	
	public static Rectangle getArea(List<? extends Node> nodes) {
		Rectangle r = null;
		for (Node n : nodes) {
			Rectangle b = n.getBounds();
			if (r == null)
				r = new Rectangle(b.x,b.y,b.width,b.height);
			else
				Draw2dUtils.union(r,b);
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
		return n.getBounds() != null && Draw2dUtils.contains(n.getBounds(),p);
		
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

	public static Node getPrevNode(Cluster cluster, Node n) {
		Node prev = null;
		for (Node child : cluster.getAllNodes()) {
			if (child == n)
				return prev;
			prev = child;
		}		
		return null;
	}

	public static Node getNextNode(Cluster cluster, Node n) {
		Node prev = null;
		for (Node child : cluster.getAllNodes()) {
			if (prev == n)
				return child;
			prev = child;
		}		
		return null;
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

	public static final Node getNodeAfterVerticalPos(InteractionGraph graph, int y) {
		List<Node> afters = getNodesAfterVerticalPos(graph, y);
		if (afters.isEmpty())
			return null;
		return afters.get(0);
	}

	public static Rectangle getNudgeArea(InteractionGraphImpl graph, List<? extends Node> nodesToNudge, boolean horizontal, boolean vertical) {
		return getNudgeArea(graph, nodesToNudge, horizontal, vertical, null); 
	}
	
	public static Rectangle getNudgeArea(InteractionGraphImpl graph, List<? extends Node> nodesToNudge, boolean horizontal, boolean vertical, List<Node> excludeNodes) {
		if (nodesToNudge == null || nodesToNudge.isEmpty())
			return null;
		
		Set<Node> vLimitNodes = null;
		Set<Node> hLimitNodes = null;
		
		if (vertical) {
			List<Node> verticalExcludingNodes = new ArrayList<Node>();
			verticalExcludingNodes.addAll(nodesToNudge);
			if (excludeNodes != null)
				verticalExcludingNodes.addAll(excludeNodes);
			
			List<Node> execSpecs = nodesToNudge.stream().map(Node::getConnectedNode).filter(Predicate.isEqual(null).negate()).
					filter(d->d.getElement() instanceof ExecutionSpecification).
					filter(Cluster.class::isInstance).map(Cluster.class::cast).flatMap(d->d.getNodes().stream()). 
					filter(d->d.getElement() instanceof ExecutionSpecification).collect(Collectors.toList());
			verticalExcludingNodes.addAll(execSpecs);
			
			vLimitNodes = new HashSet<Node>();
			Node node = nodesToNudge.stream().sorted(RowImpl.NODE_VPOSITION_COMPARATOR).findFirst().orElse(null);
			Row row = node.getRow();
			vLimitNodes.addAll(row.getNodes());			
			vLimitNodes.removeAll(verticalExcludingNodes);
			
			while (vLimitNodes.isEmpty() && row.getIndex() > 0) {
				Row prevRow = graph.getRows().get(row.getIndex()-1);
				vLimitNodes.addAll(prevRow.getNodes());
				vLimitNodes.removeAll(verticalExcludingNodes);
				row = prevRow;				
			}
		}
		
		if (horizontal) {
			List<Node> horizontalExcludingNodes = new ArrayList<Node>();
			horizontalExcludingNodes.addAll(nodesToNudge);
			if (excludeNodes != null)
				horizontalExcludingNodes.addAll(excludeNodes);

			hLimitNodes = new HashSet<Node>();
			Node node = nodesToNudge.stream().sorted(ColumnImpl.NODE_HPOSITION_COMPARATOR).findFirst().orElse(null);
			Column col = node.getColumn();
			hLimitNodes.addAll(col.getNodes());
			hLimitNodes.removeAll(horizontalExcludingNodes);
			while (hLimitNodes.isEmpty() && col.getIndex() > 0) {
				Column prevCol = graph.getColumns().get(col.getIndex()-1);
				hLimitNodes.addAll(prevCol.getNodes());
				hLimitNodes.removeAll(horizontalExcludingNodes);
				col = prevCol;				
			}
		}			
			
		int minY = Integer.MIN_VALUE; 
		if (vLimitNodes != null) {
			List<Node> lifelines = vLimitNodes.stream().filter(d->d.getElement() instanceof Lifeline).collect(Collectors.toList());		
			List<Node> createLifelines = nodesToNudge.stream().filter(NodeUtilities::isCreateOcurrenceSpecification)
					.map(NodeUtilities::getLifelineNode).collect(Collectors.toList());
			vLimitNodes.removeAll(lifelines);
			for (Node n : lifelines) {
				Rectangle clientArea = ViewUtilities.getClientAreaBounds(graph.getEditPartViewer(),n.getView());
				Rectangle headArea = n.getBounds().getCopy();
				headArea.height = clientArea.y - headArea.y;
				if (!createLifelines.contains(n)) {
					Node coveredNode = nodesToNudge.stream().filter(d->NodeUtilities.getLifelineNode(d) == n).findFirst().orElse(null);
					if (coveredNode != null)
						minY = Math.max(minY, clientArea.y);
					else
						minY = Math.max(minY, headArea.getCenter().y);
				}
			}
		}
				
		Rectangle validArea = getEmptyArea(graph, horizontal && hLimitNodes != null ? new ArrayList<>(hLimitNodes) : null, 
					vertical && vLimitNodes != null ? new ArrayList<>(vLimitNodes) : null, null, null);
			
		if (vLimitNodes != null) {
			validArea.y = Math.max(minY, validArea.y);
		}
		Draw2dUtils.insideRectangle(validArea, horizontal, vertical);
		return validArea;
	}
	
	public static List<Cluster> getIntersectingLifelineLines(InteractionGraph graph, Rectangle rect) {
		List<Cluster> intersectingLifelines = new ArrayList<Cluster>();
		for (Cluster lifelineCluster : graph.getLifelineClusters()) {
			Rectangle lifelineLineRect = ViewUtilities.getClientAreaBounds(graph.getEditPartViewer(), lifelineCluster.getView()).getCopy();
			lifelineLineRect.x = lifelineLineRect.getCenter().x - 1;
			lifelineLineRect.width = 1;
			if (rect.intersects(lifelineLineRect)) {
				intersectingLifelines.add(lifelineCluster);
			}
		}
		return intersectingLifelines;
	}
	
	public static boolean isBorderNode(Node node) {
		if (node.getEditPart() != null) {
			return node.getEditPart() instanceof IBorderItemEditPart;
		} else {
			Element el = node.getElement();
			return el instanceof Gate;
		}
	}
	
	public static boolean isLifelineNode(Node node) {
		return getLifelineNode(node) == node && node != null;		
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
		if (lifeline == null)
			return false; 

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
		if (lifeline == null)
			return false; 
		
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
