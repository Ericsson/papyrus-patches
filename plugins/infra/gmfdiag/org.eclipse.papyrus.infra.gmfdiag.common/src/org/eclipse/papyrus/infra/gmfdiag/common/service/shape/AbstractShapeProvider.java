/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
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

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.util.DOMUtilities;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.editparts.AbstractBorderItemEditPart;
import org.eclipse.gmf.runtime.diagram.ui.figures.IBorderItemLocator;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.draw2d.ui.render.RenderedImage;
import org.eclipse.gmf.runtime.draw2d.ui.render.factory.RenderedImageFactory;
import org.eclipse.papyrus.commands.Activator;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.emf.utils.ServiceUtilsForEObject;
import org.eclipse.papyrus.infra.gmfdiag.common.handler.IRefreshHandlerPart;
import org.eclipse.papyrus.infra.gmfdiag.common.handler.RefreshHandler;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.PositionEnum;
import org.eclipse.papyrus.infra.ui.editor.IMultiDiagramEditor;
import org.eclipse.ui.IEditorPart;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

/**
 * Abstract implementation of the shape provider interface.
 */
public abstract class AbstractShapeProvider extends AbstractProvider implements IShapeProvider, IRefreshHandlerPart {

	/** Prefix for platform strings */
	protected static final String PLATFORM = "platform:/"; //$NON-NLS-1$

	/**
	 * "Magic" key within an SVG filename for orientation dependent files
	 */
	protected static final String POSITION_KEY = "position"; //$NON-NLS-1$

	/** field for name */
	protected static final String NAME = "name"; //$NON-NLS-1$

	/** field for identifier */
	protected static final String ID = "id"; //$NON-NLS-1$

	/** field for description */
	protected static final String DESCRIPTION = "description"; //$NON-NLS-1$

	/** field for Activator ID */
	protected String bundleId;

	/** name for the factory */
	protected String name;

	/** identifier for the factory */
	protected String id;

	/** description of the factory */
	protected String description;

	/**
	 * Maps of URIs for SVG files referred to with relative paths
	 */
	private WeakHashMap<Resource, Map<String, String>> relativePaths;

	/** Cache for the loaded SVG document */
	private Map<String, SVGDocument> cache;


	/**
	 * Initializes this provider
	 */
	public AbstractShapeProvider() {
		RefreshHandler.register(this);
	}

	/**
	 * Returns the bundle identifier for this provider
	 *
	 * @return the bundle identifier for this provider
	 */
	public String getBundleId() {
		return bundleId;
	}


	/**
	 * Returns the name of this provider
	 *
	 * @return the name of this provider
	 */
	public String getName() {
		return name;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return id;
	}


	/**
	 * Returns the description of this provider
	 *
	 * @return the description of this provider
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public boolean provides(IOperation operation) {
		return operation instanceof GetAllShapeProvidersOperation || operation instanceof GetShapeProviderByIdentifierOperation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setConfiguration(IConfigurationElement element) {
		name = element.getAttribute(NAME);
		id = element.getAttribute(ID);
		description = element.getAttribute(DESCRIPTION);
		bundleId = element.getContributor().getName();
	}

	/**
	 * Loads a SVG document from the given location.
	 * This method uses a cache so that any given document is only loaded once.
	 *
	 * @param view
	 *            The view object to retrieve a svg document for
	 * @param location
	 *            The location to load the document from
	 * @return the Document SVG from its location, can return null if this is not a svg
	 */
	protected synchronized SVGDocument getSVGDocument(EObject view, String location) {
		if (relativePaths == null) {
			relativePaths = new WeakHashMap<Resource, Map<String, String>>();
		}
		if (location.contains("/" + POSITION_KEY) || location.contains("." + POSITION_KEY)) {  //$NON-NLS-1$//$NON-NLS-2$
			// load a specific variant of a symbol (with a different orientation) if the file name
			// contains a specific "magic" key. The motivation is that symbols on the border of a parent
			// (e.g. a port	symbol) could always point into the parent shape depending on its position
			try {
				IMultiDiagramEditor editor = ServiceUtilsForEObject.getInstance().getService(IMultiDiagramEditor.class, view);

				IEditorPart part = editor.getActiveEditor();
				PositionEnum positionKey = PositionEnum.SOUTH;
				if (part instanceof DiagramEditor) {
					Map<?, ?> editPartRegistry = ((DiagramEditor) part).getDiagramGraphicalViewer().getEditPartRegistry();
					// get edit part, in order to retrieve a border item locator that is in turn used to obtain the
					// relative position
					EditPart ep = (EditPart) editPartRegistry.get(view);
					if (ep instanceof AbstractBorderItemEditPart) {
						IBorderItemLocator locator = ((AbstractBorderItemEditPart) ep).getBorderItemLocator();
						if (locator != null) {
							int side = locator.getCurrentSideOfParent();
							positionKey = getPositionKey(side);
						}
					}
				}
				location = location.replace(POSITION_KEY, positionKey.getLiteral());
			} catch (ServiceException e) {
				Activator.log.error(e);
			}
		}

		String canonical = getCanonicalURI(view, location);
		return getSVGDocument(canonical);
	}

	/**
	 * Get a position key (enumeration) from the bit-vector representation of the position
	 * @param side the binary encoded position
	 * @return the position key
	 */
	protected PositionEnum getPositionKey(int side) {
		if ((side & PositionConstants.WEST) > 0) {
			return PositionEnum.WEST;
		} else if ((side & PositionConstants.EAST) > 0) {
			return PositionEnum.EAST;
		} else if ((side & PositionConstants.NORTH) > 0) {
			return PositionEnum.NORTH;
		} else {
			// use south as default
			return PositionEnum.SOUTH;
		}
	}
	/**
	 * Loads a SVG document from the given location.
	 * This method uses a cache so that any given document is only loaded once.
	 *
	 * @param location
	 *            The location to load the document from
	 * @return the Document SVG from its location, can return null if this is not a svg
	 */
	protected synchronized SVGDocument getSVGDocument(String location) {
		if (cache == null) {
			cache = new HashMap<String, SVGDocument>();
		}
		if (cache.containsKey(location)) {
			return cache.get(location);
		}
		SVGDocument doc = doGetSVGDocument(location);
		cache.put(location, doc);
		return doc;
	}

	/**
	 * Loads a SVG document from the given location
	 *
	 * @param location
	 *            The location to load the document from
	 * @return the Document SVG from its location, can return null if this is not a svg
	 */
	private SVGDocument doGetSVGDocument(String location) {
		int extensionIndex = location.lastIndexOf('.');
		if (extensionIndex <= 0) {
			return null;
		}
		String fileExtension = location.substring(extensionIndex);
		if (!fileExtension.equalsIgnoreCase(".svg")) { //$NON-NLS-1$
			return null;
		}

		String parser = org.apache.batik.util.XMLResourceDescriptor.getXMLParserClassName();

		try {
			SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);

			Document doc = f.createDocument(location);
			SVGDocument svgDocument = (SVGDocument) doc;
			return svgDocument;

		} catch (Exception e) {
			org.eclipse.papyrus.infra.core.Activator.log.error(e);
		}
		return null;
	}

	/**
	 * Translates the given URI as a string to a canonical Eclipse URI
	 * The URI may be relative to the currently edited EMF resource
	 *
	 * @param model
	 *            The model element used to retrieve the EMF resource that is currently edited
	 * @param uri
	 *            The potentially relative URI of a svg file
	 * @return The canonical URI of the resource
	 */
	private String getCanonicalURI(EObject model, String uri) {
		if (uri.startsWith(PLATFORM)) {
			return uri;
		}

		Map<String, String> resMap = relativePaths.get(model.eResource());
		if (resMap == null) {
			resMap = new HashMap<String, String>();
			relativePaths.put(model.eResource(), resMap);
		}
		String canonical = resMap.get(uri);
		if (canonical != null) {
			return canonical;
		}

		URI resURI = model.eResource().getURI();
		if (!resURI.isPlatform()) {
			return null;
		}
		StringBuilder builder = new StringBuilder(PLATFORM);
		String[] segments = resURI.segments();
		for (int i = 0; i < segments.length - 1; i++) {
			builder.append(segments[i]);
			builder.append("/"); //$NON-NLS-1$
		}
		builder.append(uri);
		canonical = builder.toString();
		resMap.put(uri, canonical);
		return canonical;
	}

	protected RenderedImage renderSVGDocument(EObject view, SVGDocument document) throws IOException {
		postProcess(view, document);
		String svgAsText = toString(document);
		byte[] buffer = svgAsText.getBytes();

		return RenderedImageFactory.getInstance(buffer);
	}

	protected void postProcess(EObject view, SVGDocument document) {
		SVGPostProcessor.instance.postProcess(view, document);
	}

	protected String toString(SVGDocument domDocument) throws IOException {
		StringWriter writer = new StringWriter();

		DOMUtilities.writeDocument(domDocument, writer);

		return writer.toString();
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.handler.IRefreshHandlerPart#refresh(org.eclipse.ui.IEditorPart)
	 */
	@Override
	public synchronized void refresh(IEditorPart editorPart) {
		// Clears the cache of loaded SVG documents
		// This will force their reloading
		if (cache != null) {
			cache.clear();
		}
		if (relativePaths != null) {
			relativePaths.clear();
		}
	}
}
