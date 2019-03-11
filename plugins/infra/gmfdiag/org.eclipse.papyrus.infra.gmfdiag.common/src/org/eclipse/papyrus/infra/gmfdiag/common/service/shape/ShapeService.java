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
 *      Benoit Maggi (CEA) benoit.maggi@cea.fr - Bug 498881
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.service.shape;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.service.ExecutionStrategy;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.core.service.IProvider;
import org.eclipse.gmf.runtime.common.core.service.Service;
import org.eclipse.gmf.runtime.common.ui.services.util.ActivityFilterProviderDescriptor;
import org.eclipse.gmf.runtime.diagram.core.listener.DiagramEventBroker;
import org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener;
import org.eclipse.gmf.runtime.draw2d.ui.render.RenderedImage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationUtils;
import org.w3c.dom.svg.SVGDocument;

/**
 * Service that manages shape.
 */
public class ShapeService extends org.eclipse.gmf.runtime.common.core.service.Service {

	/**
	 * 
	 */
	private static final String MAX_NUMBER_OF_SYMBOL = "maxNumberOfSymbol";//$NON-NLS-1$

	/**
	 * 
	 */
	private static final String MAX_NUMBER_OF_SYMBOL_DECORATION = "maxNumberOfSymbolDecoration";//$NON-NLS-1$

	/** singleton instance */
	private static ShapeService instance;

	/**
	 * Constructor (hidden: singleton instance).
	 */
	protected ShapeService() {

	}

	/**
	 * Checks if the given element should display a shape.
	 *
	 * @return <code>true</code> if a shape should be displayed
	 */
	public boolean hasShapeToDisplay(EObject view) {
		@SuppressWarnings("unchecked")
		List<List<RenderedImage>> images = execute(ExecutionStrategy.REVERSE, new GetShapesForViewOperation(view));
		Iterator<List<RenderedImage>> iterator = images.iterator();
		boolean hasRealImage = false;
		while (!hasRealImage && iterator.hasNext()) { // strategy gives a lot of null && empty list check if there is a real one
			List<RenderedImage> next = iterator.next();
			hasRealImage = next!=null && !next.isEmpty() ;
		} 
		return hasRealImage;
	}	

	/**
	 * Checks if the given element can display shapes as decoration.
	 *
	 * @return <code>true</code> if one or several decorations should be displayed
	 */
	public boolean hasShapeDecorationToDisplay(EObject view) {
		@SuppressWarnings("unchecked")
		List<List<RenderedImage>> images = execute(ExecutionStrategy.REVERSE, new GetShapeDecorationsForViewOperation(view));
		Iterator<List<RenderedImage>> iterator = images.iterator();
		boolean hasRealImage = false;
		while (!hasRealImage && iterator.hasNext()) { // strategy gives a lot of null && empty list check if there is a real one
			List<RenderedImage> next = iterator.next();
			hasRealImage = next!=null && !next.isEmpty() ;
		} 
		return hasRealImage;
	}

	/**
	 * Returns the shape to be displayed
	 *
	 * @param view
	 *            the EObject for which the shape is computed
	 * @return the shape to be displayed
	 */
	public List<RenderedImage> getShapesToDisplay(EObject view) {
		@SuppressWarnings("unchecked")
		List<List<RenderedImage>> listOfListOfImages = execute(ExecutionStrategy.FORWARD, new GetShapesForViewOperation(view));
		List<RenderedImage> images = new ArrayList<RenderedImage>();
		for (List<RenderedImage> listOfImages : listOfListOfImages) {
			if (listOfImages != null && !listOfImages.isEmpty()) {
				images.addAll(listOfImages);
			}
		}
		// Get the number of images to display
		int nbImagesToDisplay = NotationUtils.getIntValue((View) view, MAX_NUMBER_OF_SYMBOL, getDefaultMaxNumberOfSymbol());

		return images.subList(0, Math.min(nbImagesToDisplay, images.size()));
	}
	
	/**
	 * Returns the shape to be displayed
	 *
	 * @param view
	 *            the EObject for which the shape is computed
	 * @return the shape to be displayed
	 */
	public List<RenderedImage> getShapeDecorationsToDisplay(EObject view) {
		@SuppressWarnings("unchecked")
		List<List<RenderedImage>> listOfListOfImages = execute(ExecutionStrategy.FORWARD, new GetShapeDecorationsForViewOperation(view));
		List<RenderedImage> images = new ArrayList<RenderedImage>();
		for (List<RenderedImage> listOfImages : listOfListOfImages) {
			if (listOfImages != null && !listOfImages.isEmpty()) {
				images.addAll(listOfImages);
			}
		}
		// Get the number of images to display
		int nbImagesToDisplay = NotationUtils.getIntValue((View) view, MAX_NUMBER_OF_SYMBOL_DECORATION, getDefaultMaxNumberOfSymbolDecoration());

		return images.subList(0, Math.min(nbImagesToDisplay, images.size()));
	}

	/**
	 * Returns the shape to be displayed
	 *
	 * @param view
	 *            the EObject for which the shape is computed
	 * @return the shape to be displayed
	 */
	public List<SVGDocument> getSVGDocumentToDisplay(EObject view) {
		@SuppressWarnings("unchecked")
		List<List<SVGDocument>> listOfListOfImages = execute(ExecutionStrategy.FORWARD, new GetSVGDocumentForViewOperation(view));
		// lists of images are sort by priority from the highest to the lowest
		List<SVGDocument> images = new ArrayList<SVGDocument>();
		for (List<SVGDocument> listOfImages : listOfListOfImages) {
			if (listOfImages != null && !listOfImages.isEmpty()) {
				images.addAll(listOfImages);
			}
		}
		// Get the number of images to display
		int nbImagesToDisplay = NotationUtils.getIntValue((View) view, MAX_NUMBER_OF_SYMBOL, getDefaultMaxNumberOfSymbol());

		return images.subList(0, Math.min(nbImagesToDisplay, images.size()));
	}

	/**
	 * @return
	 */
	private int getDefaultMaxNumberOfSymbol() {
		return 10;
	}

	/**
	 * @return
	 */
	private int getDefaultMaxNumberOfSymbolDecoration() {
		return 10;
	}


	/**
	 * Ask all the shape providers to add their required notification listeners to the diagram event broker.
	 *
	 * @param diagramEventBroker
	 *            the diagram event broker used to manage notifications
	 * @param view
	 *            view on which required listened elements are retrieved
	 * @param notificationListener
	 *            notification listener that should be notified when there are modifications susceptible to change the shapes
	 */
	protected List<ProviderNotificationManager> createProviderNotificationManagers(DiagramEventBroker diagramEventBroker, EObject view, NotificationListener notificationListener) {
		@SuppressWarnings("unchecked")
		List<ProviderNotificationManager> providerNotificationManagers = execute(ExecutionStrategy.REVERSE, new CreateProviderNotificationManagersOperation(diagramEventBroker, view, notificationListener));
		return providerNotificationManagers;
	}

	/**
	 * gets the singleton instance
	 *
	 * @return <code>PaletteService</code>
	 */
	public static synchronized ShapeService getInstance() {
		if (instance == null) {
			instance = new ShapeService();
			configureProviders();
		}
		return instance;
	}

	/**
	 * configure the extensions for this service.
	 */
	private static void configureProviders() {
		getInstance().configureProviders(Activator.ID, "shapeProvider"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Service.ProviderDescriptor newProviderDescriptor(IConfigurationElement element) {
		return new ProviderDescriptor(element);
	}

	/**
	 * @{inheritDoc
	 */
	public List<IShapeProvider> getProviders() {
		List<IShapeProvider> providers = new ArrayList<IShapeProvider>();
		execute(ExecutionStrategy.REVERSE, new GetAllShapeProvidersOperation(providers));
		return providers;
	}

	/**
	 * @{inheritDoc
	 */
	public IShapeProvider getProvider(String id) {
		@SuppressWarnings("unchecked")
		List<IShapeProvider> providers = execute(ExecutionStrategy.REVERSE, new GetShapeProviderByIdentifierOperation(id));
		if (providers == null) {
			return null;
		}
		Iterator<IShapeProvider> it = providers.iterator();
		while (it.hasNext()) {
			IShapeProvider aspectActionProvider = it.next();
			if (aspectActionProvider != null) {
				return aspectActionProvider;
			}
		}
		return null;
	}

	/**
	 * A descriptor for aspect tool providers defined by a configuration
	 * element.
	 */
	protected static class ProviderDescriptor extends ActivityFilterProviderDescriptor {

		/** the provider configuration parsed from XML */
		protected ShapeProviderConfiguration providerConfiguration;

		/**
		 * Constructs a <code>ISemanticProvider</code> descriptor for the
		 * specified configuration element.
		 *
		 * @param element
		 *            The configuration element describing the provider.
		 */
		public ProviderDescriptor(IConfigurationElement element) {
			super(element);
			this.providerConfiguration = ShapeProviderConfiguration.parse(element);
			Assert.isNotNull(providerConfiguration);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean provides(IOperation operation) {
			if (!super.provides(operation)) {
				return false;
			}

			if (!(operation instanceof IShapeProviderOperation)) {
				return false;
			}

			if (operation instanceof GetShapeProviderByIdentifierOperation) {
				return providerConfiguration.getId().equals(((GetShapeProviderByIdentifierOperation) operation).getIdentifier());
			}
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public IProvider getProvider() {
			if (provider == null) {
				IProvider newProvider = super.getProvider();
				if (provider instanceof IShapeProvider) {
					IShapeProvider defaultProvider = (IShapeProvider) newProvider;
					defaultProvider.setConfiguration(getElement());
				}
				return newProvider;
			}
			return super.getProvider();
		}
	}

	/**
	 * Creates the notification manager, initializes it with all managers provided by the service providers, and returns it for the given view
	 *
	 * @param diagramEventBroker
	 *            event broker on which provider managers register themselves.
	 * @param view
	 *            the view in charge of the display of the shapes
	 * @param notificationListener
	 *            the listener notified when the event broker fires a notification
	 * @return the created notification provider
	 */
	public NotificationManager createNotificationManager(DiagramEventBroker diagramEventBroker, EObject view, NotificationListener notificationListener) {
		NotificationManager manager = new NotificationManager(view);
		List<ProviderNotificationManager> providerNotificationManagers = createProviderNotificationManagers(diagramEventBroker, view, notificationListener);
		manager.getProviderNotificationManagers().addAll(providerNotificationManagers);
		return manager;
	}

}
