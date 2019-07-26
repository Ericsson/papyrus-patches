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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.GraphItem;

public abstract class GraphItemImpl implements GraphItem {

	public GraphItemImpl() {
	}

	public abstract void setView(View view);

	@SuppressWarnings("unchecked")
	public Map<String,Object> getProperties() {
		if (properties == null)
			return Collections.EMPTY_MAP;
		return Collections.unmodifiableMap(properties);
	}
	
	public Object getProperty(String name) {
		if (properties == null)
			return null;
		return properties.get(name);
	}
	
	public void setProperty(String name, Object value) {
		if (properties == null)
			properties = new HashMap<String, Object>();
		
		properties.put(name,value);		
	}
	
	private Map<String,Object> properties;
}
