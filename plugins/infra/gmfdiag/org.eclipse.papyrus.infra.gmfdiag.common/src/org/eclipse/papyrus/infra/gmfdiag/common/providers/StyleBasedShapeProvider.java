/*****************************************************************************
 * Copyright (c) 2014 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		CEA LIST - Initial API and implementation
 *		Fanch BONNABESSE (ALL4TEC) - fanch.bonnabesse@all4tec.net - Bug 502531
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.providers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.core.listener.DiagramEventBroker;
import org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener;
import org.eclipse.gmf.runtime.draw2d.ui.render.RenderedImage;
import org.eclipse.gmf.runtime.draw2d.ui.render.factory.RenderedImageFactory;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.StringValueStyle;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationUtils;
import org.eclipse.papyrus.infra.gmfdiag.common.service.shape.AbstractShapeProvider;
import org.eclipse.papyrus.infra.gmfdiag.common.service.shape.ProviderNotificationManager;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.NamedStyleProperties;
import org.w3c.dom.svg.SVGDocument;

/**
 * Shape provider based on the applied style
 *
 * @author Laurent Wouters
 */
public class StyleBasedShapeProvider extends AbstractShapeProvider {

	protected static final String STYLE_PROPERTY = "svgFile"; //$NON-NLS-1$

	/**
	 * @since 3.0
	 */
	protected static final String IMAGE_PATH_PROPERTY = "imagePath"; //$NON-NLS-1$

	private ProviderNotificationManager manager;

	private List<SVGDocument> listEmptySVG;
	private List<RenderedImage> listEmptyRendered;
	private List<SVGDocument> listSingletonSVG;
	private List<RenderedImage> listRenderedImages;

	public StyleBasedShapeProvider() {
		listEmptySVG = new ArrayList<SVGDocument>(0);
		listEmptyRendered = new ArrayList<RenderedImage>(0);
		listSingletonSVG = new ArrayList<SVGDocument>(1);
		listSingletonSVG.add(null);
		listRenderedImages = new ArrayList<RenderedImage>();
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.service.shape.IShapeProvider#getShapes(org.eclipse.emf.ecore.EObject)
	 */
	@Override
	public List<RenderedImage> getShapes(EObject view) {
		if (!(view instanceof View)) {
			return listEmptyRendered;
		}
		View v = (View) view;

		if (!isShapeStyleEnable(v)) {
			return listEmptyRendered;
		}

		return doGetShapes(v);
	}

	protected List<RenderedImage> doGetShapes(final View view) {
		if (null != listRenderedImages) {
			listRenderedImages.clear();
		}

		// Check the 'svg' document with the property 'svgFile'
		List<SVGDocument> documents = getSVGDocument(view);
		if ((null != documents) && (!documents.isEmpty())) {
			for (SVGDocument document : documents) {
				if (null == document) {
					continue;
				}
				try {
					RenderedImage renderSVGDocument = renderSVGDocument(view, document);
					if (null != renderSVGDocument) {
						listRenderedImages.add(renderSVGDocument);
					}
				} catch (IOException ex) {
					Activator.log.error(ex);
				}
			}
		}

		// Check the other image file with the property 'imagePath'
		String path = NotationUtils.getStringValue(view, IMAGE_PATH_PROPERTY, null);
		if ((null != path) && (0 < path.length())) {
			URL url;
			try {
				url = new URL(path);
				listRenderedImages.add(RenderedImageFactory.getInstance(url));
			} catch (MalformedURLException e) {
				URI typeResourceURI = view.eResource().getURI();
				if (null != typeResourceURI) {
					String workspaceRelativeFolderPath = typeResourceURI.trimSegments(1).toPlatformString(true);
					try {
						url = new URL("platform:/resource/" + workspaceRelativeFolderPath + File.separatorChar + path); //$NON-NLS-1$
						listRenderedImages.add(RenderedImageFactory.getInstance(url));
					} catch (MalformedURLException e1) {
						Activator.log.error(e1);
					}
				}
			}
		}

		return listRenderedImages;
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.service.shape.IShapeProvider#getShapesForDecoration(org.eclipse.emf.ecore.EObject)
	 *
	 * @param view
	 * @return
	 */
	@Override
	public List<RenderedImage> getShapesForDecoration(EObject view) {
		if (!(view instanceof View)) {
			return listEmptyRendered;
		}
		View v = (View) view;

		if (!isShapeDecorationStyleEnable(v)) {
			return listEmptyRendered;
		}

		return doGetShapesForDecoration(v);
	}

	protected List<RenderedImage> doGetShapesForDecoration(View view) {
		return doGetShapes(view);
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.service.shape.IShapeProvider#getSVGDocument(org.eclipse.emf.ecore.EObject)
	 */
	@Override
	public List<SVGDocument> getSVGDocument(EObject view) {
		if (!(view instanceof View)) {
			return listEmptySVG;
		}
		View v = (View) view;

		if (!isShapeStyleEnable(v)) {
			return listEmptySVG;
		}

		String svgFile = extract((StringValueStyle) v.getNamedStyle(NotationPackage.eINSTANCE.getStringValueStyle(), STYLE_PROPERTY));
		if (svgFile == null) {
			return listEmptySVG;
		}
		SVGDocument svg = getSVGDocument(view, svgFile);
		listSingletonSVG.set(0, svg);
		return listSingletonSVG;
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.service.shape.IShapeProvider#providesShapes(org.eclipse.emf.ecore.EObject)
	 */
	@Override
	public boolean providesShapes(EObject view) {
		if (!(view instanceof View)) {
			return false;
		}
		View v = (View) view;

		if (!isShapeStyleEnable(v)) {
			return false;
		}

		String svgFile = extract((StringValueStyle) v.getNamedStyle(NotationPackage.eINSTANCE.getStringValueStyle(), STYLE_PROPERTY));
		String imagePath = NotationUtils.getStringValue(v, IMAGE_PATH_PROPERTY, null);

		return (svgFile != null || imagePath != null);
	}

	/**
	 * Returns <code>false</code> if the given view specifically removes the support for css-defined shapes.
	 * 
	 * @param view
	 *            the view to check style
	 * @return <code>false</code> if the given view specifically removes the support for css-defined shapes, otherwise <code>true</code>.
	 */
	private boolean isShapeStyleEnable(View view) {
		return NotationUtils.getBooleanValue(view, NamedStyleProperties.SHAPE_STYLE_PROPERTY, true);
	}

	/**
	 * Returns <code>false</code> if the given view specifically removes the support for css-defined shapes.
	 * 
	 * @param view
	 *            the view to check style
	 * @return <code>false</code> if the given view specifically removes the support for css-defined shapes, otherwise <code>true</code>.
	 */
	private boolean isShapeDecorationStyleEnable(View view) {
		return NotationUtils.getBooleanValue(view, NamedStyleProperties.SHAPE_DECORATION_STYLE_PROPERTY, true);
	}

	/**
	 * Extracts the primitive value from the given style
	 *
	 * @param style
	 *            The style
	 * @return The primitive value
	 */
	private String extract(StringValueStyle style) {
		if (style == null || style.getStringValue() == null || style.getStringValue().isEmpty()) {
			return null;
		}
		return style.getStringValue();
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.service.shape.IShapeProvider#createProviderNotificationManager(org.eclipse.gmf.runtime.diagram.core.listener.DiagramEventBroker, org.eclipse.emf.ecore.EObject,
	 *      org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener)
	 */
	@Override
	public ProviderNotificationManager createProviderNotificationManager(DiagramEventBroker diagramEventBroker, EObject view, NotificationListener notificationListener) {
		if (manager != null) {
			return manager;
		}
		manager = new ProviderNotificationManager(diagramEventBroker, view, notificationListener) {
			@Override
			protected void registerListeners() {

			}
		};
		return manager;
	}

}
