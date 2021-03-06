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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Link;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Row;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.MessageEnd;

public class RowImpl extends SlotImpl implements Row {

	private int ypos;

	public RowImpl(InteractionGraphImpl interactionGraph) {
		super(interactionGraph);
	}

	@Override
	protected void attachNode(NodeImpl node) {
		if (node.getRow() != this && node.getRow() != null) {
			node.getRow().removeNode(node);
		}
		node.setRow(this);
	}

	@Override
	protected void detachNode(NodeImpl node) {
		if (node.getRow() == this) {
			node.setRow(null);
		}
	}

	@Override
	public int getYPosition() {
		return ypos;
	}

	void setYPosition(int y) {
		this.ypos = y;
	}

	@Override
	public void sortNodes() {
		List<Node> mos = nodes.stream().filter(d -> d.getElement() instanceof MessageEnd).collect(Collectors.toList());
		if (mos.size() > 0) {
			Collections.sort(nodes, RowImpl.MESSAGE_END_NODE_COMPARATOR);
		} else {
			// throw new UnsupportedOperationException();
		}
	}

	@Override
	public String toString() {
		return String.format("Row[%d][y: %d]", index, ypos);
	}

	public static final NodeFragmentComparator NODE_FRAGMENT_COMPARATOR = new NodeFragmentComparator();
	public static final NodeVPositionComparator NODE_VPOSITION_COMPARATOR = new NodeVPositionComparator();
	public static final MessageEndNodeComparator MESSAGE_END_NODE_COMPARATOR = new MessageEndNodeComparator();

	static class MessageEndNodeComparator implements Comparator<Node> {
		@Override
		public int compare(Node o1, Node o2) {
			return Integer.compare(rank(o1), rank(o2));
		}

		private int rank(Node n) {
			if (n.getElement() instanceof MessageEnd) {
				MessageEnd me = (MessageEnd) n.getElement();
				if (me.getMessage() != null) {
					if (me.getMessage().getSendEvent() == me) {
						return -1;
					} else if (me.getMessage().getReceiveEvent() == me) {
						return 1;
					}
				}
				Link lk = n.getConnectedByLink();
				if (lk != null) {  
					if (lk.getSource() == n) {
						return -1;
					} else if (lk.getTarget() == n) {
						return 1;
					}
				}
			} else if (n.getElement() instanceof ExecutionSpecification) {
				return 2;
			}

			return 0;
		}
	}

	static class NodeVPositionComparator implements Comparator<Node> {
		@Override
		public int compare(Node o1, Node o2) {
			Rectangle r1 = (Rectangle)o1.getBounds();
			Rectangle r2 = (Rectangle)o2.getBounds();
			if (r1 == null || r2 == null)
				return 0;
			int res = Integer.compare(r1.y,r2.y );
			if (res == 0) {
				res = Integer.compare(r1.x, r2.x);
				if (res == 0) {					
					return Integer.compare(o1.getParent().getNodes().indexOf(o1), o2.getParent().getNodes().indexOf(o2));
				}				
			}
			return res;
		}
	}

	static class NodeFragmentComparator implements Comparator<Node> {
		@Override
		public int compare(Node o1, Node o2) {
			Rectangle r1 = (Rectangle)o1.getBounds();
			Rectangle r2 = (Rectangle)o2.getBounds();
			if (r1 == null || r2 == null)
				return 0;
			int res = r1.y - r2.y;
			if (res < -1 || res > 1)
				return res;
			
			res = MESSAGE_END_NODE_COMPARATOR.compare(o1, o2);			
			if (res == 0)
				res = Integer.compare(r1.x, r2.x);				
			if (res == 0) 
				res = Integer.compare(r1.y, r2.y );
			if (res == 0)
				res = Integer.compare(o1.getParent().getNodes().indexOf(o1), o2.getParent().getNodes().indexOf(o2));
			return res;
		}
	}
}
