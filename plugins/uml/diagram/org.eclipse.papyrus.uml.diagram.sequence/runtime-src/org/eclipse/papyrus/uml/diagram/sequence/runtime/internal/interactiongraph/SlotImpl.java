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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;

public abstract class SlotImpl {
	protected InteractionGraphImpl interactionGraph;
	protected int index;
	protected List<NodeImpl> nodes = new ArrayList<>();

	public SlotImpl(InteractionGraphImpl interactionGraph) {
		this.interactionGraph = interactionGraph;
	}

	public int getIndex() {
		return index;
	}

	void setIndex(int index) {
		this.index = index;
	}

	void addNode(NodeImpl node) {
		nodes.add(node);
		attachNode(node);
	}

	void addNodes(List<? extends NodeImpl> nodes) {
		nodes.stream().forEach(SlotImpl.this::addNode);
	}

	void addNode(int pos, NodeImpl node) {
		nodes.add(pos, node);
		attachNode(node);
	}

	//protected abstract void nudge(int delta);

	protected abstract void attachNode(NodeImpl n);

	protected abstract void detachNode(NodeImpl n);

	void removeNode(NodeImpl node) {
		nodes.remove(node);
		detachNode(node);
	}

	public List<Node> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	public InteractionGraphImpl getInteractionGraph() {
		return interactionGraph;
	}

	Rectangle getBounds() {
		Rectangle r = NodeUtilities.getArea((List)nodes);
		return r;
	}

	Point getLocation() {
		return getBounds().getLocation();
	}

	Dimension getSize() {
		return getBounds().getSize();
	}

	public List<Point> getAnchorPoints() {
		// TODO: Not know if still needed????
		List<Point> points = new ArrayList<>();
		for (NodeImpl n : nodes) {
			Rectangle r = n.getBounds();
			if (r == null) {
				continue;
			}
			points.add(r.getCenter());
		}
		return points;
	}

	public abstract void sortNodes();
}
