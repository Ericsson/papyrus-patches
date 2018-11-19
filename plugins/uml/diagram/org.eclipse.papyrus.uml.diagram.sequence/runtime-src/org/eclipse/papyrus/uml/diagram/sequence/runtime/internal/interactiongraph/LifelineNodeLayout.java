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
public class LifelineNodeLayout implements InteractionNodeLayout {
	@Override
	public void layout(NodeImpl node) {
		int width = node.getBounds() == null ? ViewUtilities.LIFELINE_DEFAULT_WIDTH : node.getBounds().width;
		int height = ViewUtilities.LIFELINE_HEADER_HEIGHT;

		ColumnImpl column = node.column;
		RowImpl row = node.row;

		Rectangle r = new Rectangle();
		r.y = row.getYPosition() - (height / 2);
		r.x = column.getXPosition() - (width / 2);
		r.width = width;
		r.height = height;
		node.setBounds(r);
	}

}
