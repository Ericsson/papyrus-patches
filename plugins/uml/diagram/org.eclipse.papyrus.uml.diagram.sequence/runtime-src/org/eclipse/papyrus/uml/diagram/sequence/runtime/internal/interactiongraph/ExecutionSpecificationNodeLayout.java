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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.uml2.uml.ExecutionSpecification;


/**
 * @author ETXACAM
 *
 */
public class ExecutionSpecificationNodeLayout implements InteractionNodeLayout {
	@Override
	public void layout(NodeImpl node) {
		if (node instanceof Cluster) {
			Node lifeline = NodeUtilities.getLifelineNode(node);
			int x = lifeline.getBounds().getCenter().x;
			x = x - (ViewUtilities.EXECUTION_SPECIFICATION_WIDTH / 2);
			Node parent = node.getParent();
			while (parent != lifeline) {
				if (parent.getElement() instanceof ExecutionSpecification) {
					x += (ViewUtilities.EXECUTION_SPECIFICATION_WIDTH / 2);
				}
				parent = parent.getParent();
			}
			Rectangle r = ((ClusterImpl)node).getChildrenBounds();
			r.x = x;
			r.width = ViewUtilities.EXECUTION_SPECIFICATION_WIDTH;
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
			return new Dimension(20,40);
		}
		return new Dimension(0,0);
	}

}
