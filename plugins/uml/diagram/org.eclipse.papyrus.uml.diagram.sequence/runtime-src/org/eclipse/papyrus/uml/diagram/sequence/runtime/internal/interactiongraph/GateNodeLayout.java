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

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomGateEditPart;


/**
 * @author ETXACAM
 *
 */
public class GateNodeLayout implements InteractionNodeLayout {
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
		r.translate(-CustomGateEditPart.GATE_SIZE/2,-CustomGateEditPart.GATE_SIZE/2);
		r.setSize(CustomGateEditPart.GATE_SIZE, CustomGateEditPart.GATE_SIZE);
		return r;
	}

}
