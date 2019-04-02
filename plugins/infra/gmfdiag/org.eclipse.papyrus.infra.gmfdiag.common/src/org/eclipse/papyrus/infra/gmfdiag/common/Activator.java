/*****************************************************************************
 * Copyright (c) 2008, 2016 LIFL, CEA LIST, Christian W. Damus, and others.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Cedric Dumoulin  Cedric.dumoulin@lifl.fr - Initial API and implementation
 *  Christian W. Damus (CEA) - bugs 410346, 425270
 *  Christian W. Damus - bug 485220
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.papyrus.infra.core.log.LogHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.spi.GraphicalDeletionHelperService;
import org.eclipse.papyrus.infra.gmfdiag.common.spi.IGraphicalDeletionHelper;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	public static final String ID = "org.eclipse.papyrus.infra.gmfdiag.common"; //$NON-NLS-1$

	public static final PreferencesHint DIAGRAM_PREFERENCES_HINT = new PreferencesHint(ID);
	public static final String EXPANSION_TRACE = "expansion";

	private static Activator instance;

	private ComposedAdapterFactory adapterFactory;

	private GraphicalDeletionHelperService deletionHelper;

	public Activator() {
	}

	/** Logging helper */
	public static LogHelper log;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		// register the login helper
		log = new LogHelper(this);
		adapterFactory = createAdapterFactory();
		deletionHelper = new GraphicalDeletionHelperService(context);
		PreferencesHint.registerPreferenceStore(DIAGRAM_PREFERENCES_HINT, getPreferenceStore());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		deletionHelper.dispose();
		adapterFactory.dispose();
		adapterFactory = null;
		log = null;
		instance = null;
		super.stop(context);
	}

	public static Activator getInstance() {
		return instance;
	}

	protected ComposedAdapterFactory createAdapterFactory() {
		return new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
	}

	public AdapterFactory getItemProvidersAdapterFactory() {
		return adapterFactory;
	}

	public ImageDescriptor getItemImageDescriptor(Object item) {
		IItemLabelProvider labelProvider = (IItemLabelProvider) getItemProvidersAdapterFactory().adapt(item, IItemLabelProvider.class);
		if (labelProvider != null) {
			return ExtendedImageRegistry.getInstance().getImageDescriptor(labelProvider.getImage(item));
		}
		return null;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getBundledImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(ID, path);
	}

	/**
	 * Respects images residing in any plug-in. If path is relative, then this bundle is looked up
	 * for the image, otherwise, for absolute path, first segment is taken as id of plug-in with
	 * image
	 *
	 * @param path
	 *            the path to image, either absolute (with plug-in id as first segment), or relative
	 *            for bundled images
	 * @return the image descriptor
	 */
	public static ImageDescriptor findImageDescriptor(String path) {
		final IPath p = new Path(path);
		if (p.isAbsolute() && p.segmentCount() > 1) {
			return AbstractUIPlugin.imageDescriptorFromPlugin(p.segment(0), p.removeFirstSegments(1).makeAbsolute().toString());
		} else {
			return getBundledImageDescriptor(p.makeAbsolute().toString());
		}
	}

	/**
	 * Returns string from plug-in's resource bundle
	 */
	public static String getString(String key) {
		return Platform.getResourceString(getInstance().getBundle(), "%" + key); //$NON-NLS-1$
	}

	/**
	 * Returns an image for the image file at the given plug-in relative path. Client do not need to
	 * dispose this image. Images will be disposed automatically.
	 *
	 * @param path
	 *            the path
	 * @return image instance
	 */
	public Image getBundledImage(String path) {
		Image image = getImageRegistry().get(path);
		if (image == null) {
			getImageRegistry().put(path, getBundledImageDescriptor(path));
			image = getImageRegistry().get(path);
		}
		return image;
	}

	public void logError(String error) {
		logError(error, null);
	}

	/**
	 * @param throwable
	 *            actual error or null could be passed
	 */
	public void logError(String error, Throwable throwable) {
		if (error == null && throwable != null) {
			error = throwable.getMessage();
		}
		getLog().log(new Status(IStatus.ERROR, Activator.ID, IStatus.OK, error, throwable));
		debug(error, throwable);
	}

	public void logInfo(String message) {
		logInfo(message, null);
	}

	/**
	 * @param throwable
	 *            actual error or null could be passed
	 */
	public void logInfo(String message, Throwable throwable) {
		if (message == null && throwable != null) {
			message = throwable.getMessage();
		}
		getLog().log(new Status(IStatus.INFO, Activator.ID, IStatus.OK, message, throwable));
		debug(message, throwable);
	}

	private void debug(String message, Throwable throwable) {
		if (!isDebugging()) {
			return;
		}
		if (message != null) {
			System.err.println(message);
		}
		if (throwable != null) {
			throwable.printStackTrace();
		}
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 *
	 * @param path
	 *            the path
	 *
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(ID, path);
	}

	public IGraphicalDeletionHelper getGraphicalDeletionHelper() {
		return deletionHelper;
	}
}
