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
 *  Antonio Campesino (Ericsson) antonio.campesino.robles@ericsson.com - Initial API and implementation
 *
 *****************************************************************************/
package com.ericsson.papyrus.devtools.views;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;
import org.eclipse.ui.part.EditorPart;

import com.rits.cloning.Cloner;
import com.rits.cloning.ICloningStrategy;

public class RequestsLogService {
	private static final RequestsLogService INSTANCE = new RequestsLogService();
	private List<RequestLogEntry> requests = new LinkedList<RequestLogEntry>();
	private List<RequestsLogListener> listeners = new LinkedList<RequestsLogListener>();
	private Cloner cloner; 
	private boolean enabled = false;
	
	public static RequestsLogService getInstance() {
		return INSTANCE;
	}
	
	private RequestsLogService() {
		cloner = Cloner.standard();
		cloner.dontCloneInstanceOf(EObject.class, EditorPart.class, EditPolicy.class);
		cloner.registerCloningStrategy(new ICloningStrategy() {
			@Override
			public Strategy strategyFor(Object toBeCloned, Field field) {
				Class<?> c = field.getType();//toBeCloned.getClass();
				if (c.isPrimitive() || c.isArray() || toBeCloned instanceof Request)
					return Strategy.IGNORE;
				Package p = c.getPackage();
				if (p != null && p.getName().startsWith("org.eclipse.draw2d.geometry"))
					return Strategy.IGNORE;
				if (IEditCommandRequest.class.isAssignableFrom(c) || 
					Request.class.isAssignableFrom(c))
					return Strategy.IGNORE;
				return Strategy.SAME_INSTANCE_INSTEAD_OF_CLONE;
			}
			
		});
	}
	
	public List<RequestLogEntry> getLoggedRequests() {
		return requests;
	}
	
	public void log(EditPart part, Request request, Command command) {
		if (!enabled)
			return;
		
		Request req = cloner.deepClone(request);
		Command cmd = cloner.deepClone(command);
		requests.add(new RequestLogEntry(part, req, cmd));
		for (RequestsLogListener l : listeners)
			l.requestLogged(req);
	}
	
	public void clear() {
		requests.clear();
	}

	public void addListener(RequestsLogListener l) {
		listeners.add(l);
	}
	
	public void removeListener(RequestsLogListener l) {
		listeners.remove(l);
	}

	public static interface RequestsLogListener {
		public void requestLogged(Request r);
	}

	public void enable() {
		enabled = true;
	}
	public void disable() {
		enabled = false;
	}

	public boolean isEnabled() {
		return enabled;
	}
}
