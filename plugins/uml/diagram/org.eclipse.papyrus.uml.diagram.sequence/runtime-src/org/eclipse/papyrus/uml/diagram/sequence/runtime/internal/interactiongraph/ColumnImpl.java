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

import java.util.Comparator;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Column;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;

public class ColumnImpl extends SlotImpl implements Column {
	public static final NodeHPositionComparator NODE_HPOSITION_COMPARATOR = new NodeHPositionComparator();
	static class NodeHPositionComparator implements Comparator<Node> {
		@Override
		public int compare(Node o1, Node o2) {
			Rectangle r1 = (Rectangle)o1.getBounds();
			Rectangle r2 = (Rectangle)o2.getBounds();
			if (r1 == null || r2 == null)
				return 0;
			int res = Integer.compare(r1.x, r2.x);
			if (res == 0) {
				res = Integer.compare(r1.y, r2.y);
				if (res == 0) {					
					return Integer.compare(o1.getParent().getNodes().indexOf(o1), o2.getParent().getNodes().indexOf(o2));
				}				
			}
			return res;
		}
	}


	// TODO: Keep left padding, so we can nudge when name has changed???
	private int xpos;

	public ColumnImpl(InteractionGraphImpl interactionGraph) {
		super(interactionGraph);
	}


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
		/*
		 * List<Node> mos = nodes.stream().
		 * filter(d -> d.getElement() instanceof MessageEnd).
		 * collect(Collectors.toList());
		 * if (mos.size() > 0) {
		 * Collections.sort(nodes, ColumnImpl.MESSAGE_END_NODE_COMPARATORS);
		 * } else {
		 * //throw new UnsupportedOperationException();
		 * }
		 */
	}

	@Override
	public void nudge(int delta) {
		this.xpos += delta;
		getNodes().stream().map(NodeImpl.class::cast).forEach(interactionGraph.getLayoutManager()::layout);
	}

	@Override
	public String toString() {
		return String.format("Col[%d][x: %d]", index, xpos);
	}
}

