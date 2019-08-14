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

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;


/**
 * @author ETXACAM
 *
 */
public class InteractionUseNodeLayout implements InteractionNodeLayout {
	@Override
	public void layout(NodeImpl node) {
		if (node instanceof FragmentCluster) {
			FragmentCluster cluster = (FragmentCluster)node;
			Rectangle fcr = node.getBounds();
			List<Cluster> clusters = cluster.getClusters();
			if (!clusters.isEmpty()) {				
				Rectangle childRect = NodeUtilities.getArea(clusters);
				Rectangle r = childRect.getCopy();
				//r.height = 60;
				List<Node> leftGates = cluster.getAllGates().stream().filter(d->d.getBounds().x <= fcr.x).collect(Collectors.toList());
				List<Node> rightGates = cluster.getAllGates().stream().filter(d->d.getBounds().x >= fcr.right()).collect(Collectors.toList());
				List<Cluster> lifelines = clusters.stream().map(NodeUtilities::getLifelineNode).filter(Predicate.isEqual(null).negate()).collect(Collectors.toList());
				
				if (!lifelines.isEmpty()) {
					int leftSide = lifelines.stream().map(Node::getBounds).map(Rectangle::x).collect(Collectors.minBy(Integer::compare)).get();
					int rightSide = lifelines.stream().map(Node::getBounds).map(Rectangle::right).collect(Collectors.maxBy(Integer::compare)).get();
					r.x = Math.min(r.x, leftSide);
					leftGates.forEach(d->d.getBounds().x = r.x);
					int right = Math.max(r.getRight().x, rightSide);
					r.width = right - r.x;
					rightGates.forEach(d->d.getBounds().x = right);
				}
				node.setBounds(r);
			}
		} else if (node instanceof Cluster) {
			Cluster cluster = (Cluster) node;
			FragmentCluster fragCluster = cluster.getFragmentCluster();
			if (fragCluster == null)
				return;
			
			Rectangle r = NodeUtilities.getArea(cluster.getNodes());
			node.setBounds(r);			
		} else {
			ColumnImpl column = node.column;
			RowImpl row = node.row;

			Rectangle r = new Rectangle();
			r.x = column.getXPosition();
			r.y = row.getYPosition();
			r.width = 0;
			r.height = 0;
			node.setBounds(r);
		}
	}

	@Override
	public Dimension getMinimumSize(NodeImpl node) {
		if (node instanceof Cluster) {
			return new Dimension(40,60);
		}
		return new Dimension(0,0);
	}

}
