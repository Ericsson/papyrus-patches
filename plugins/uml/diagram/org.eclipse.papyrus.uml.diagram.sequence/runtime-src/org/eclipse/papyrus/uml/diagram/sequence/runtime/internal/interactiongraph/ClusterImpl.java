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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;

public class ClusterImpl extends NodeImpl implements Cluster {
	public ClusterImpl(Element element) {
		super(element);
	}

	protected void reset() {
		super.reset();
		fragmentCluster = null;
		nodes = new ArrayList<NodeImpl>();
	}
	
	@Override
	public ClusterImpl getParent() {
		ClusterImpl par = super.getParent();
		if (par instanceof InteractionGraph) {
			return null; // Force the lifeline parent to be null. To stop searching up... 
		}		
		return par;
	}
	
	@Override
	public FragmentClusterImpl getFragmentCluster() {
		return fragmentCluster;
	}

	void setFragmentCluster(FragmentClusterImpl fragmentCluster) {
		this.fragmentCluster = fragmentCluster;
	}

	protected void updateNodes(Comparator<Node> comparator) {
		nodes.sort(comparator);
	}
	
	@Override
	public List<Node> getNodes() {
		return Collections.unmodifiableList(nodes);
	}
	
	public void addNode(NodeImpl node) {
		nodes.add(node);
		node.setParent(this);
	}
	
	public void addNode(NodeImpl node, Node beforeNode) {
		if (beforeNode == null) {
			addNode(node);
		} else {
			int index = nodes.indexOf(beforeNode);
			nodes.add(index,node);
			node.setParent(this);
		}
	}
	
	public void addNode(int index, NodeImpl node) {
		nodes.add(index,node);
		node.setParent(this);
	}

	public boolean removeNode(Node node) {
		boolean res = nodes.remove(node);
		((NodeImpl)node).setParent(null);
		return res;
	}
	
	public NodeImpl removeNode(int index) {
		NodeImpl n = nodes.remove(index);
		n.setParent(null);
		return n;
	}

	public List<Node> getAllNodes() {
		return NodeUtilities.flatten(this);
	}
	
	public List<Cluster> getAllClusters() {
		return NodeUtilities.flattenKeepClusters(this).stream().
				filter(Cluster.class::isInstance).filter(Predicate.isEqual(this).negate()).
				map(Cluster.class::cast).collect(Collectors.toList());
	}

	public Rectangle getBounds() {
		Rectangle r = super.getBounds();
		if (r != null)
			return r;
		
		return getChildrenBounds();
	}
	
	Rectangle getChildrenBounds() {
		Rectangle r = NodeUtilities.getArea(getNodes());
		if (r == null) 
		{
			if (bounds == null) 
				return null;
			r = getBounds().getCopy();
			r.height = 0;
			r.width = 0;
		} 
		return r;
	}
	
	public String toString() {
		if (getElement() == null)
			return "Cluster[--]";
		return String.format("Cluster[%s - %s Col:%s Row:%s]",
				getPrintableString(getElement()),
				bounds == null ? "--" : bounds.toString(),
				column == null ? "-" : ("[" + column.getIndex() +"]("+column.getXPosition()+")"),
				row == null ? "-" : ("[" + row.getIndex() +"]("+row.getYPosition()+")"));

	}

	private String getPrintableString(Element element) {
		return getElement() instanceof NamedElement ? ((NamedElement)getElement()).getName() : "A " + getElement().eClass().getName();
	}
	
	private FragmentClusterImpl fragmentCluster;
	private List<NodeImpl> nodes = new ArrayList<NodeImpl>();
}
