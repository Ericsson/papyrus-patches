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

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.uml2.uml.ExecutionSpecification;


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
			Rectangle childRect = NodeUtilities.getArea((List)((FragmentCluster)node).getClusters());
			r.y = childRect.y;
			r.height = 40;
			r.x = Math.min(childRect.x, r.x);
			int right = Math.max(childRect.getRight().x, r.getRight().x);
			r.width = right - r.x;
			node.setBounds(r);
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
