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

import java.util.Map;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.notation.Location;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.InteractionFragment;

/**
 * @author ETXACAM
 *
 */
public interface GraphItem {
	/**
	 * Returns the {@link InteractionGraph} this node belongs to.
	 *
	 * @return the {@link InteractionGraph}
	 */
	public InteractionGraph getInteractionGraph();

	/**
	 * Returns the {@link Element} that this node represents.
	 *
	 * @return a {@link Element} subclass, an {@link InteractionFragment} or a {@link Gate}.
	 */
	public Element getElement();

	/**
	 * The View in the notation model that hold graphical constraints probably, in the form of
	 * {@link Location}.
	 *
	 * @return a {@link View}
	 */
	public View getView();

	/**
	 * The EditPart associated to the view in the notation model. The edit parts are used to calculate the position
	 * in the diagram editor.
	 *
	 * @return a {@link View}
	 */
	public GraphicalEditPart getEditPart();

	public Object getProperty(String name);
	public void setProperty(String name, Object value);
	public Map<String,Object> getProperties();

}
