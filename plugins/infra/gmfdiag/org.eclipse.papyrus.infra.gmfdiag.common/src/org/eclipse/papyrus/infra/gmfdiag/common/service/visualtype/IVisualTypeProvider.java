/*****************************************************************************
 * Copyright (c) 2015 Christian W. Damus and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.service.visualtype;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.service.IProvider;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;

/**
 * A GMF service provider for visual element-type information specific to a diagram.
 * 
 * @noimplement Providers should extend the {@link AbstractVisualTypeProvider} class.
 */
public interface IVisualTypeProvider extends IProvider {
	/**
	 * Obtains the element-type indicated by the specified {@code visualID}.
	 * 
	 * @param diagram
	 *            the diagram context in which the visualID is encountered
	 * @param viewType
	 *            the {@linkplain View#getType() view type} for an element visualized in the diagram
	 * 
	 * @return the corresponding element type, or {@code null} if the {@code visualID} is
	 *         not recognized by this provider
	 */
	IElementType getElementType(Diagram diagram, String viewType);

	/**
	 * Obtains the unique view type for the visualization of an {@code element} in a parent view.
	 * 
	 * @param parentView
	 *            the view within which the {@code element} is to be visualized
	 * @param element
	 *            a model element to visualize
	 * 
	 * @return the {@linkplain View#getType() view type} of the node that would present the {@code element} in the parent view,
	 *         or {@code null} if the {@code element} is not supported by this provider in the given parent view
	 */
	String getNodeType(View parentView, EObject element);

	/**
	 * Obtains the unique view type for the visualization of an {@code element} as an edge in the
	 * diagram. Note that, unlike the case of {@linkplain #getNodeType(View, EObject) nodes},
	 * there is no meaningful container-view context to qualify this query, as all edges are
	 * children of the root {@link Diagram}.
	 * 
	 * @param diagram
	 *            the diagram in which the {@code element} is to be visualized as an edge
	 * @param element
	 *            a model element to visualize as an edge
	 * 
	 * @return the {@linkplain View#getType() view type} of the edge that would present the {@code element} in the diagram,
	 *         or {@code null} if the {@code element} is not supported by this provider in its diagram
	 */
	String getLinkType(Diagram diagram, EObject element);

}
