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

package org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.uml2.uml.Interaction;

/**
 * @author ETXACAM
 *
 */
public class InteractionGraphRequestHelper {
	private static final String REQ_PROPERTY = "InteractionGraph";

	public static InteractionGraph getInteractiongraph(Request req) {
		return (InteractionGraph) req.getExtendedData().get(REQ_PROPERTY);
	}

	public static InteractionGraph getOrCreateInteractionGraph(Request req, GraphicalEditPart gep) {
		InteractionGraph graph = (InteractionGraph) req.getExtendedData().get(REQ_PROPERTY);
		if (graph != null) {
			return graph;
		}

		graph = createInteractionGraph(gep);
		if (graph == null) {
			return null;
		}
		bound(req, graph);
		return graph;
	}

	public static InteractionGraph createInteractionGraph(GraphicalEditPart gep) {
		if (!(gep.getModel() instanceof View)) {
			return null;
		}

		View view = (View) gep.getModel();
		if (view == null) {
			return null;
		}

		if (!(view instanceof Diagram)) {
			view = view.getDiagram();
		}

		Diagram dia = (Diagram) view;
		Object el = dia.getElement();
		if (!(el instanceof Interaction)) {
			return null;
		}

		return InteractionGraphFactory.getInstance().createInteractionGraph((Interaction) el, dia, gep.getViewer());
	}

	@SuppressWarnings("unchecked")
	public static void bound(Request req, InteractionGraph graph) {
		req.getExtendedData().put(REQ_PROPERTY, graph);
	}

	public static void unbound(Request req, InteractionGraph graph) {
		req.getExtendedData().remove(REQ_PROPERTY, graph);
	}
}
