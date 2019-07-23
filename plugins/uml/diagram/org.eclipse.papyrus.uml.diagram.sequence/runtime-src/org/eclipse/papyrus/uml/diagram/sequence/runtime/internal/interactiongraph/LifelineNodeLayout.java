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

import org.eclipse.draw2d.geometry.Point;
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
		
		View containerContentPane = ViewUtilities.getViewWithType(node.getInteractionGraph().getInteractionView(), CInteractionInteractionCompartmentEditPart.VISUAL_ID);
		if (containerContentPane != null) {
			Rectangle area = ViewUtilities.getClientAreaBounds(node.getInteractionGraph().getEditPartViewer(), containerContentPane);
			Rectangle childrenArea = ((ClusterImpl)node).getChildrenBounds();
			if (childrenArea != null) {
				height = (childrenArea.height + (childrenArea.y - area.y) + 10) - (r != null ? r.y - area.y : 10 ) /*- 14*/ +
						node.getInteractionGraph().getGridSpacing(20);   
			} else {
				height = area.y;
			}
			
		}

		
		ColumnImpl column = node.column;
		RowImpl row = node.row;

		Point origin = new Point((width / 2), (ViewUtilities.LIFELINE_HEADER_HEIGHT / 2));
		r = new Rectangle();
		r.y = row.getYPosition() - origin.y;
		r.x = column.getXPosition() - origin.x;
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
