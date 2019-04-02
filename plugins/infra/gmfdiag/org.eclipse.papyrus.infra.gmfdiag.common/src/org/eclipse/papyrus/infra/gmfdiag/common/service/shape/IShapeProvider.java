/*****************************************************************************
 * Copyright (c) 2011 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *		CEA LIST - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.service.shape;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.service.IProvider;
import org.eclipse.gmf.runtime.diagram.core.listener.DiagramEventBroker;
import org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener;
import org.eclipse.gmf.runtime.draw2d.ui.render.RenderedImage;
import org.w3c.dom.svg.SVGDocument;

/**
 * Interface implemented by all providers to the {@link ShapeService}
 */
public interface IShapeProvider extends IProvider {

	/**
	 * Sets the configuration for this provider, given the configuration element
	 *
	 * @param element
	 *            configuration element coming from plugin.xml file
	 */
	void setConfiguration(IConfigurationElement element);

	/**
	 * Returns the unique identifier of this provider
	 *
	 * @return the unique identifier of this provider
	 */
	String getId();

	/**
	 * Returns the list of shapes proposed by this provider
	 *
	 * @param view
	 *            the view for which shapes are looked for
	 * @return the list of shapes or <code>null</code> if no shapes have to be displayed by this provider
	 */
	List<RenderedImage> getShapes(EObject view);

	/**
	 * Returns the list of shapes proposed by this provider specific for decoration
	 *
	 * @param view
	 *            the view for which shapes are looked for
	 * @return the list of shapes displayed as decoration or <code>null</code> if no shapes have to be displayed by this provider
	 */
	List<RenderedImage> getShapesForDecoration(EObject view);


	/**
	 * Returns the list of SVG Documents proposed by this provider
	 *
	 * @param view
	 *            the view for which shapes are looked for
	 * @return the list of SVG DOCUMENT or <code>null</code> if no shapes have to be displayed by this provider
	 */
	List<SVGDocument> getSVGDocument(EObject view);

	/**
	 * Returns <code>true</code> if the provider can display shapes.
	 * This methods allows to compute if shapes can be displayed instead of computing the whole list of shapes to be displayed.
	 *
	 * @param view
	 *            the view that can display shapes
	 * @return <code>true</code> if this provider can provide shapes for this view.
	 */
	boolean providesShapes(EObject view);

	/**
	 * Adds notification listeners to the diagram event broker for the given view
	 *
	 * @param diagramEventBroker
	 *            diagram event broker that dispatches the notifications to interested elements
	 * @param view
	 *            view from which objects to listen are retrieved
	 * @param notificationListener
	 *            notification listener that will react to the modification of shapes
	 * @return the result of the creation
	 */
	ProviderNotificationManager createProviderNotificationManager(DiagramEventBroker diagramEventBroker, EObject view, NotificationListener notificationListener);

}
