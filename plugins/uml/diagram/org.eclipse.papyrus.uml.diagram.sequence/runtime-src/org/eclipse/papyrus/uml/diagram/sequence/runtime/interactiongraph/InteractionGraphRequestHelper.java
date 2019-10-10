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

package org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.uml2.uml.Interaction;

/**
 * @author ETXACAM
 *
 */
public class InteractionGraphRequestHelper {
	private static final String REQ_PROPERTY = "InteractionGraph";

	public static InteractionGraph getInteractionGraph(Request req) {
		return (InteractionGraph) req.getExtendedData().get(REQ_PROPERTY);
	}

	public static InteractionGraph getInteractionGraph(IEditCommandRequest req) {
		return (InteractionGraph) req.getParameter(REQ_PROPERTY);
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

		if (view == null)
			return null;
		
		Diagram dia = (Diagram) view;
		Object el = dia.getElement();
		if (!(el instanceof Interaction)) {
			return null;
		}

		return InteractionGraphFactory.getInstance().createInteractionGraph((Interaction) el, dia, gep.getViewer());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void bound(Request req, InteractionGraph graph) {
		Map extendedData = req.getExtendedData();
		if (!(extendedData instanceof ExtendedData)) {
			extendedData = new ExtendedData(extendedData);
			req.setExtendedData(extendedData);
		}
		req.getExtendedData().put(REQ_PROPERTY, graph);
	}

	public static void bound(IEditCommandRequest req, InteractionGraph graph) {
		req.setParameter(REQ_PROPERTY, graph);
	}

	@SuppressWarnings("rawtypes")
	public static void unbound(Request req, InteractionGraph graph) {
		Map extendedData = req.getExtendedData();
		if (extendedData instanceof ExtendedData) {
			extendedData = ((ExtendedData)extendedData).delegate;
			req.setExtendedData(extendedData);
		}
		req.getExtendedData().remove(REQ_PROPERTY, graph);
	}

	public static void unbound(IEditCommandRequest req, InteractionGraph graph) {
		req.setParameter(REQ_PROPERTY, null);
	}
	
	@SuppressWarnings({"unchecked", "rawtypes", "unused" })
	private static class ExtendedData implements Map {
		private InteractionGraph graph;
		private Map delegate;
		
		public ExtendedData(Map delegate) {
			super();
			this.delegate = delegate;
		}

		@Override
		public int size() {
			checkAndAdd();
			return delegate.size();
		}

		@Override
		public boolean isEmpty() {
			checkAndAdd();
			return delegate.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			checkAndAdd();
			return delegate.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			checkAndAdd();
			return delegate.containsValue(value);
		}

		@Override
		public Object get(Object key) {
			checkAndAdd();
			return delegate.get(key);
		}

		@Override
		public Object put(Object key, Object value) {
			if (key.equals(REQ_PROPERTY)) {
				graph = (InteractionGraph)value;
				if (graph == null)
					delegate.remove(REQ_PROPERTY);
			}
			checkAndAdd();
			return delegate.put(key, value);
		}

		@Override
		public Object remove(Object key) {
			if (key.equals(REQ_PROPERTY)) {
				graph = null;
				delegate.remove(REQ_PROPERTY);
			}
			checkAndAdd();
			return delegate.remove(key);
		}

		@Override
		public void putAll(Map m) {
			checkAndAdd();
			delegate.putAll(m);
		}

		@Override
		public void clear() {
			checkAndAdd();
			delegate.clear();
		}

		@Override
		public Set keySet() {
			checkAndAdd();
			return delegate.keySet();
		}

		@Override
		public Collection values() {
			checkAndAdd();
			return delegate.values();
		}

		@Override
		public Set<Entry> entrySet() {
			checkAndAdd();
			return delegate.entrySet();
		}
		
		private void checkAndAdd() {
			if (graph != null && !delegate.containsKey(REQ_PROPERTY)) {
				delegate.put(REQ_PROPERTY, graph);
			}
		}

	}
}
