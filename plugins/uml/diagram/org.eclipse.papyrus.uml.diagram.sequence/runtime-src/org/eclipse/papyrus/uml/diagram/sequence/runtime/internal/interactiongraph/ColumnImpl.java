/*****************************************************************************
 * (c) Copyright 2018 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) antonio.campesino.robles@ericsson.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph;

import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Column;

public class ColumnImpl extends SlotImpl implements Column {
	
	private int xpos;

	@Override
	protected void attachNode(NodeImpl node) {
		if (node.getColumn() != this && node.getColumn() != null) {
			node.getColumn().removeNode(node);
		}
		node.setColumn(this);
	}

	@Override
	protected void detachNode(NodeImpl node) {
		if (node.getColumn() == this) {
			node.setColumn(null);
		}
	}

	@Override
	public int getXPosition() {
		return xpos;
	}

	void setXPosition(int x) {
		this.xpos = x;
	}
	
	@Override
	public void sortNodes() {
/*		List<Node> mos = nodes.stream().
				filter(d -> d.getElement() instanceof MessageEnd).
				collect(Collectors.toList());
		if (mos.size() > 0) {			
			Collections.sort(nodes, ColumnImpl.MESSAGE_END_NODE_COMPARATORS);
		} else {
			//throw new UnsupportedOperationException(); 
		}*/
	}
		
	public String toString() {
		return String.format("Col[%d][x: %d]", index, xpos);
	}
}
