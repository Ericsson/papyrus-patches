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

	void addCluster(ClusterImpl cluster) {
		clusters.add(cluster);
		cluster.setFragmentCluster(this);
	}

	@Override
	public List<FragmentCluster> getOwnedFragmentClusters() {
		return Collections.unmodifiableList(fragmentClusters);
	}

	void addFragmentCluster(FragmentClusterImpl cluster) {
		fragmentClusters.add(cluster);
		cluster.setParent(this);
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

	void addInnerGate(NodeImpl node) {
		innerGates.add(node);
		node.setParent(this);
	}
	
	void addOuterGate(NodeImpl node) {
		outerGates.add(node);
		node.setParent(this);
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
