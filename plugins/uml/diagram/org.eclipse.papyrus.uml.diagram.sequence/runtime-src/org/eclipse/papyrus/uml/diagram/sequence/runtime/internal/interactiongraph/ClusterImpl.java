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

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;

public class ClusterImpl extends NodeImpl implements Cluster {
	public ClusterImpl(Element element) {
		super(element);
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
	
	void addNode(NodeImpl node) {
		nodes.add(node);
		node.setParent(this);
	}
	
	void addNode(NodeImpl node, Node beforeNode) {
		if (beforeNode == null) {
			addNode(node);
		} else {
			int index = nodes.indexOf(beforeNode);
			nodes.add(index,node);
			node.setParent(this);
		}
	}
	
	void addNode(int index, NodeImpl node) {
		nodes.add(index,node);
		node.setParent(this);
	}

	boolean removeNode(NodeImpl node) {
		boolean res = nodes.remove(node);
		node.setParent(null);
		return res;
	}
	
	NodeImpl removeNode(int index) {
		NodeImpl n = nodes.remove(index);
		n.setParent(null);
		return n;
	}

	public List<Node> getAllNodes() {
		return NodeUtilities.flatten(this);
	}
	
	public Rectangle getBound() {
		Rectangle r = super.getBounds();
		if (r != null)
			return r;
		
		for (Node c : getNodes()) {
			Rectangle b = c.getBounds();
			if (b == null)
				continue;
			if (r == null)
				r = b.getCopy();
			else
				r.union(b);
		}
		return r;
	}
	
	public String toString() {
		if (getElement() == null)
			return "Cluster[--]";
		return String.format("Cluster[%s]", 
				getElement() instanceof NamedElement ? ((NamedElement)getElement()).getName() : "A " + getElement().eClass().getName());
	}

	private FragmentClusterImpl fragmentCluster;
	private List<NodeImpl> nodes = new ArrayList<NodeImpl>();
}
