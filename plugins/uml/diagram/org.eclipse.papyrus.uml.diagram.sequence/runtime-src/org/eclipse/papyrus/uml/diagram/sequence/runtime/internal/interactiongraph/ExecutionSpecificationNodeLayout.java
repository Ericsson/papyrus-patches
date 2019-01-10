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

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;


/**
 * @author ETXACAM
 *
 */
public class ExecutionSpecificationNodeLayout implements InteractionNodeLayout {
	@Override
	public void layout(NodeImpl node) {
		if (node instanceof Cluster) {
			Rectangle r = ((ClusterImpl)node).getChildrenBounds();
			r.width = ViewUtilities.EXECUTION_SPECIFICATION_WIDTH;
			r.x = r.x- (ViewUtilities.EXECUTION_SPECIFICATION_WIDTH / 2);		
			node.setBounds(r);			
		} else {
			ColumnImpl column = node.column;
			RowImpl row = node.row;

			Rectangle r = new Rectangle();
			r.x = column.getXPosition();
			r.y = row.getYPosition();
			r.width = 1;
			r.height = 1;
			node.setBounds(r);
		}
	}

}
