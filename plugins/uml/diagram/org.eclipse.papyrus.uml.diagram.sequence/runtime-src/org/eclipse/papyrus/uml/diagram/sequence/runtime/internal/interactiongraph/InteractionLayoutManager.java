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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Column;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Row;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Lifeline;

/**
 * @author ETXACAM
 *
 */
public class InteractionLayoutManager implements InteractionNodeLayout {
	private static Map<Class<? extends Element>, InteractionNodeLayout> layouts = initializeLayouts();

	public InteractionLayoutManager(InteractionGraphImpl graph) {
		this.interactionGraph = graph;
	}

	public void layout() {
		for (Column column : interactionGraph.getColumns()) {
			for (Node n : column.getNodes()) {
				layout((NodeImpl)n);
			}
		}

		for (Row row : interactionGraph.getRows()) {
			for (Node n : row.getNodes()) {
				layout((NodeImpl)n);
			}
		}

		interactionGraph.getLifelineClusters().stream().forEach(d -> layout((NodeImpl) d));		
	}

	@Override
	public void layout(NodeImpl node) {
		InteractionNodeLayout layout = getNodeLayoutFor(node);
		if (layout != null) {
			layout.layout(node);
		} else {
			Rectangle r = node.getBounds();
			if (r != null) {
				
				r.x = node.getColumn().getXPosition() - (r.width / 2);
				r.y = node.getRow().getYPosition() - (r.height / 2);
			}				
		}
	}

	InteractionNodeLayout getNodeLayoutFor(NodeImpl node) {
		Class key = node.getElement().eClass().getInstanceClass();
		return layouts.get(key);
	}

	private static Map<Class<? extends Element>, InteractionNodeLayout> initializeLayouts() {
		Map<Class<? extends Element>, InteractionNodeLayout> map = new HashMap<>();
		map.put(Lifeline.class, new LifelineNodeLayout());
		return map;
	}

	private InteractionGraphImpl interactionGraph;
}
