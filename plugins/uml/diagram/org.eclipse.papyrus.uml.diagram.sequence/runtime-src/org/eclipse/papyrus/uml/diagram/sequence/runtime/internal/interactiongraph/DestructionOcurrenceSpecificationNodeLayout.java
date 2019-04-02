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


/**
 * @author ETXACAM
 *
 */
public class DestructionOcurrenceSpecificationNodeLayout implements InteractionNodeLayout {
	@Override
	public void layout(NodeImpl node) {
		ColumnImpl column = node.column;
		RowImpl row = node.row;

		Rectangle r = new Rectangle();
		r.x = column.getXPosition();
		r.y = row.getYPosition();
		r.width = 0;
		r.height = 0;
		node.setBounds(r);
	}

	@Override
	public Rectangle getConstraints(NodeImpl node) {
		Rectangle r = node.getBounds().getCopy();
		r.translate(-20,-20);
		r.setSize(40, 40);
		return r;
	}

}