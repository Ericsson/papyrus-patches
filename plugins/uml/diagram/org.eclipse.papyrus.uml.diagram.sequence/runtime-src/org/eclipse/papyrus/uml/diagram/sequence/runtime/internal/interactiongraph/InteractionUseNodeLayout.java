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

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;


/**
 * @author ETXACAM
 *
 */
public class InteractionUseNodeLayout implements InteractionNodeLayout {
	@SuppressWarnings("unchecked")
	@Override
	public void layout(NodeImpl node) {
		if (node instanceof FragmentCluster) {
			Rectangle r = node.getBounds();
			List<Cluster> clusters = ((FragmentCluster)node).getClusters();
			if (!clusters.isEmpty()) {				
				Rectangle childRect = NodeUtilities.getArea(clusters);
				r = childRect.getCopy();
				r.height = 40;
				List<Cluster> lifelines = clusters.stream().map(NodeUtilities::getLifelineNode).filter(Predicate.isEqual(null).negate()).collect(Collectors.toList());
				if (!lifelines.isEmpty()) {
					int leftSide = lifelines.stream().map(Node::getBounds).map(Rectangle::x).collect(Collectors.minBy(Integer::compare)).get();
					int rightSide = lifelines.stream().map(Node::getBounds).map(Rectangle::right).collect(Collectors.maxBy(Integer::compare)).get();
					r.x = Math.min(r.x, leftSide);
					int right = Math.max(r.getRight().x, rightSide);
					r.width = right - r.x;
				}
				node.setBounds(r);
			}
		} else if (node instanceof Cluster) {
			Cluster cluster = (Cluster) node;
			FragmentCluster fragCluster = cluster.getFragmentCluster();
			if (fragCluster == null)
				return;
			
			Rectangle fragClusterRect = fragCluster.getBounds();
			Rectangle bounds = cluster.getParent().getBounds().getCopy().intersect(fragClusterRect);			
			Rectangle r = NodeUtilities.getArea(cluster.getNodes());
			bounds.y = r.y;
			bounds.height = r.height;
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

}
