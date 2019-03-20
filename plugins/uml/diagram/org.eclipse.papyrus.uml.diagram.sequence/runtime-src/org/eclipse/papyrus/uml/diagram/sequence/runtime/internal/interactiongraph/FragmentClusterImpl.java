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
import java.util.List;

import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.uml2.uml.Element;

public class FragmentClusterImpl extends ClusterImpl implements FragmentCluster {

	public FragmentClusterImpl(Element element) {
		super(element);
	}

	@Override
	public List<Cluster> getClusters() {
		return Collections.unmodifiableList(clusters);
	}

	public void addCluster(ClusterImpl cluster) {
		clusters.add(cluster);
		cluster.setFragmentCluster(this);
	}

	public void removeCluster(Cluster cluster) {
		clusters.remove(cluster);
		((ClusterImpl)cluster).setFragmentCluster(null);
	}

	@Override
	public List<FragmentCluster> getOwnedFragmentClusters() {
		return Collections.unmodifiableList(fragmentClusters);
	}

	public void addFragmentCluster(FragmentClusterImpl cluster) {
		fragmentClusters.add(cluster);
		cluster.setParent(this);
	}
	
	public void addFragmentCluster(FragmentClusterImpl cluster, FragmentClusterImpl before) {
		int index = before != null ? fragmentClusters.indexOf(before) : -1;
		if (index == -1)
			fragmentClusters.add(cluster);
		else
			fragmentClusters.add(index, cluster);
		cluster.setParent(this);
	}

	public void removeFragmentCluster(FragmentCluster cluster) {
		fragmentClusters.remove(cluster);
		((FragmentClusterImpl)cluster).setParent(null);
	}

	@Override
	public List<Node> getAllGates() {
		List<Node> gates = new ArrayList<Node>(innerGates);
		gates.addAll(outerGates);
		return Collections.unmodifiableList(gates);
	}

	@Override
	public List<Node> getInnerGates() {
		return Collections.unmodifiableList(innerGates);
	}

	public List<Node> getOuterGates() {
		return Collections.unmodifiableList(outerGates);
	}

	public void addInnerGate(NodeImpl node) {
		addInnerGate(node,null);
	}
	
	public void addInnerGate(NodeImpl node, NodeImpl beforeNode) {
		int index = beforeNode == null ? -1 : innerGates.indexOf(beforeNode);
		if (index != -1)
			innerGates.add(index,node);
		else
			innerGates.add(node);
		node.setParent(this);
	}

	public boolean removeInnerGate(NodeImpl node) {
		return innerGates.remove(node);
	}
	
	public void addOuterGate(NodeImpl node) {
		addOuterGate(node,null);
	}

	public void addOuterGate(NodeImpl node, NodeImpl beforeNode) {
		int index = beforeNode == null ? -1 : outerGates.indexOf(beforeNode);
		if (index != -1)
			outerGates.add(index,node);
		else
			outerGates.add(node);
		node.setParent(this);
	}

	public boolean removeOuterGate(NodeImpl node) {
		return outerGates.remove(node);
	}

	@Override
	public List<Node> getFloatingNodes() {
		return Collections.unmodifiableList(floatingNodes);
	}

	private List<ClusterImpl> clusters = new ArrayList<ClusterImpl>();
	private List<FragmentClusterImpl> fragmentClusters = new ArrayList<FragmentClusterImpl>();
	private List<NodeImpl> innerGates = new ArrayList<NodeImpl>();
	private List<NodeImpl> outerGates = new ArrayList<NodeImpl>();
	private List<NodeImpl> floatingNodes = new ArrayList<NodeImpl>();;
}
