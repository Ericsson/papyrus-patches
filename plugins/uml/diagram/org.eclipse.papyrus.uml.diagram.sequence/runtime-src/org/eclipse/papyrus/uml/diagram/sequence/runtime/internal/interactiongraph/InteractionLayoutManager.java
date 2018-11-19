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
		interactionGraph.getLifelineClusters().stream().forEach(d -> layout((NodeImpl) d));
	}

	@Override
	public void layout(NodeImpl node) {
		InteractionNodeLayout layout = getNodeLayoutFor(node);
		if (layout != null) {
			layout.layout(node);
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
