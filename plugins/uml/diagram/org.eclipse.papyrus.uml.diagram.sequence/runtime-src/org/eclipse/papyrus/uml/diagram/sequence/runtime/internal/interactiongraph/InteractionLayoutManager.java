/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.uml2.uml.ActionExecutionSpecification;
import org.eclipse.uml2.uml.BehaviorExecutionSpecification;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;

/**
 * @author ETXACAM
 *
 */
public class InteractionLayoutManager implements InteractionNodeLayout {
	private static Map<Class<? extends Element>, InteractionNodeLayout> nodeLayouts = initializeNodeLayouts();
	private static Map<Class<? extends Element>, InteractionNodeLayout> clusterLayouts = initializeClusterLayouts();

	public InteractionLayoutManager(InteractionGraphImpl graph) {
		this.interactionGraph = graph;
	}

	public void layout() {
		interactionGraph.getLifelineClusters().stream().forEach(d -> layout((NodeImpl) d));		
		interactionGraph.getFragmentClusters().stream().forEach(d -> layout((NodeImpl) d));
		List<Node> gates = NodeUtilities.flatten(interactionGraph).stream().
				map(FragmentCluster::getAllGates).flatMap(Collection::stream).
			collect(Collectors.toList());
		gates.stream().forEach(d -> layout((NodeImpl) d));
	}
	
	@Override
	public Rectangle getConstraints(NodeImpl node) {
		if (node instanceof ClusterImpl) {
			InteractionNodeLayout layout = getClusterLayoutFor((ClusterImpl)node);
			if (layout != null)
				return layout.getConstraints(node);
		} 

		InteractionNodeLayout layout = getNodeLayoutFor(node);
		if (layout != null)
			return layout.getConstraints(node);
		
		return node.getBounds().getCopy();
	}
	
	@Override
	public void layout(NodeImpl node) {
		if (node instanceof ClusterImpl) {
			ClusterImpl cluster = (ClusterImpl)node;
			for (Node n : cluster.getNodes()) {
				layout((NodeImpl)n);
			}
			InteractionNodeLayout layout = getClusterLayoutFor(cluster);
			layoutImp(node, layout);
		} else {
			InteractionNodeLayout layout = getNodeLayoutFor(node);
			layoutImp(node, layout);
		}
	}

	@Override
	public Dimension getMinimumSize(NodeImpl node) {
		if (node instanceof ClusterImpl) {
			InteractionNodeLayout layout = getClusterLayoutFor((ClusterImpl)node);
			if (layout != null)
				return layout.getMinimumSize(node);
		} 

		InteractionNodeLayout layout = getNodeLayoutFor(node);
		if (layout != null)
			return layout.getMinimumSize(node);
		
		return new Dimension(0,0);
	}

	private void layoutImp(NodeImpl node, InteractionNodeLayout layout) {
		if (layout != null) {
			layout.layout(node);
		} else {
			Rectangle r = node.getBounds();
			if (r != null) {				
				r.x = node.getColumn().getXPosition() - (r.width / 2);
				r.y = node.getRow().getYPosition() - (r.height / 2);
			}				
		}
	}
	
	InteractionNodeLayout getNodeLayoutFor(NodeImpl node) {
		if (node.getElement() == null)
			return null;
		Class key = node.getElement().eClass().getInstanceClass();
		return nodeLayouts.get(key);
	}

	InteractionNodeLayout getClusterLayoutFor(ClusterImpl node) {
		Class key = node.getElement().eClass().getInstanceClass();
		return clusterLayouts.get(key);
	}

	private static Map<Class<? extends Element>, InteractionNodeLayout> initializeNodeLayouts() {
		Map<Class<? extends Element>, InteractionNodeLayout> map = new HashMap<>();
		map.put(Gate.class, new GateNodeLayout());
		map.put(DestructionOccurrenceSpecification.class, new DestructionOcurrenceSpecificationNodeLayout());
		map.put(ActionExecutionSpecification.class, new ExecutionSpecificationNodeLayout());
		map.put(BehaviorExecutionSpecification.class, new ExecutionSpecificationNodeLayout());
		return map;
	}

	private static Map<Class<? extends Element>, InteractionNodeLayout> initializeClusterLayouts() {
		Map<Class<? extends Element>, InteractionNodeLayout> map = new HashMap<>();
		map.put(Lifeline.class, new LifelineNodeLayout());
		map.put(ActionExecutionSpecification.class, new ExecutionSpecificationNodeLayout());
		map.put(BehaviorExecutionSpecification.class, new ExecutionSpecificationNodeLayout());
		map.put(InteractionUse.class, new InteractionUseNodeLayout());
		return map;
	}

	private InteractionGraphImpl interactionGraph;
}
