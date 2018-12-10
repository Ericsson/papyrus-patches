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
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CInteractionInteractionCompartmentEditPart;


/**
 * @author ETXACAM
 *
 */
public class LifelineNodeLayout implements InteractionNodeLayout {
	@Override
	public void layout(NodeImpl node) {
		Rectangle r = node.getBounds();
		int width = r == null ? ViewUtilities.LIFELINE_DEFAULT_WIDTH : r.width;
		int height = ViewUtilities.LIFELINE_HEADER_HEIGHT;
		View containerContentPane = ViewUtilities.getViewWithType(node.getInteractionGraph().getInteractionView(), CInteractionInteractionCompartmentEditPart.VISUAL_ID);
		if (containerContentPane != null) {
			Rectangle area = ViewUtilities.getClientAreaBounds(node.getInteractionGraph().getEditPartViewer(), containerContentPane);
			height = area.height - (r != null ? r.y - area.y : 10 )- 14; // TODO: Find where that come from.
			/*if (r != null && height - r.height < 20)
				height = r.height;*/
		}

		ColumnImpl column = node.column;
		RowImpl row = node.row;

		r = new Rectangle();
		r.y = row.getYPosition() - (ViewUtilities.LIFELINE_HEADER_HEIGHT / 2);
		r.x = column.getXPosition() - (width / 2);
		r.width = width;
		r.height = height;
		node.setBounds(r);		
	}

}
