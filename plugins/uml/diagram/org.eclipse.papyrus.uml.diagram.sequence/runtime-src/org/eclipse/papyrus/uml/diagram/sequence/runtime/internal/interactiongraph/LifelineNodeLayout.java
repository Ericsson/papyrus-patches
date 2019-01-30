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
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;


/**
 * @author ETXACAM
 *
 */
public class LifelineNodeLayout implements InteractionNodeLayout {
	@Override
	public void layout(NodeImpl node) {
		Rectangle r = node.getBounds();
		int width = r == null || r.width == -1 ? ViewUtilities.LIFELINE_DEFAULT_WIDTH : r.width;
		int height = ViewUtilities.LIFELINE_HEADER_HEIGHT;
		
		Rectangle childrenArea = ((ClusterImpl)node).getChildrenBounds();

		View containerContentPane = ViewUtilities.getViewWithType(node.getInteractionGraph().getInteractionView(), CInteractionInteractionCompartmentEditPart.VISUAL_ID);
		if (containerContentPane != null) {
			Rectangle area = ViewUtilities.getClientAreaBounds(node.getInteractionGraph().getEditPartViewer(), containerContentPane);
			height = (childrenArea.height + (childrenArea.y - area.y) + 10) - (r != null ? r.y - area.y : 10 ) /*- 14*/ + 20;   
		}

		
		ColumnImpl column = node.column;
		RowImpl row = node.row;

		r = new Rectangle();
		r.y = row.getYPosition() - (ViewUtilities.LIFELINE_HEADER_HEIGHT / 2);
		r.x = column.getXPosition() - (width / 2);
		r.width = width;
		r.height = height;
		Node dosNode = ((Cluster)node).getNodes().stream().filter(
				d->d.getElement() instanceof DestructionOccurrenceSpecification).
			findFirst().orElse(null);
		if (dosNode != null) {
			r.height = dosNode.getBounds().getCenter().y - r.y;
		}
		node.setBounds(r);		
	}

}
