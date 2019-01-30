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
import org.eclipse.uml2.uml.ExecutionSpecification;


/**
 * @author ETXACAM
 *
 */
public class DestructionOcurrentceSpecificationNodeLayout implements InteractionNodeLayout {
	@Override
	public void layout(NodeImpl node) {
		ColumnImpl column = node.column;
		RowImpl row = node.row;

		Rectangle r = new Rectangle();
		r.x = column.getXPosition()-20;
		r.y = row.getYPosition()-20;
		r.width = 40;
		r.height = 40;
		node.setBounds(r);
	}

}
