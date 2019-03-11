/*****************************************************************************
 * Copyright (c) 2009, 2014 Atos Origin, CEA, and others.
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
 *   Atos Origin - Initial API and implementation
 *   Christian W. Damus (CEA) - bug 410346
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.part;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.papyrus.infra.core.log.LogHelper;
import org.eclipse.papyrus.infra.gmfdiag.preferences.Activator;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.UMLBaseItemSemanticEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.expressions.UMLOCLFactory;
import org.eclipse.papyrus.uml.diagram.sequence.preferences.CustomDiagramPreferenceInitializer;
import org.eclipse.papyrus.uml.diagram.sequence.providers.ElementInitializers;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @generated
 */
public class UMLDiagramEditorPlugin extends AbstractUIPlugin {

	/** Logging helper */
	public static LogHelper log;

	/**
	 * @generated
	 */
	public static final String ID = "org.eclipse.papyrus.uml.diagram.sequence"; //$NON-NLS-1$

	/**
	 * @generated
	 */
	private LogHelper myLogHelper;

	/**
	 * @generated
	 */
	public static final PreferencesHint DIAGRAM_PREFERENCES_HINT = new PreferencesHint(ID);

	/**
	 * @generated
	 */
	private static UMLDiagramEditorPlugin instance;

	/**
	 * @generated
	 */
	private AdapterFactory adapterFactory;

	/**
	 * @generated
	 */
	private UMLDocumentProvider documentProvider;

	/**
	 * @generated
	 */
	private UMLBaseItemSemanticEditPolicy.LinkConstraints linkConstraints;

	/**
	 * @generated
	 */
	private ElementInitializers initializers;

	/**
	 * @generated
	 */
	private UMLOCLFactory oclFactory;

	/**
	 * @generated
	 */
	public UMLDiagramEditorPlugin() {
	}

	/**
	 * It is no more generated to initialize the log.
	 *
	 * @generated NOT
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		PreferencesHint.registerPreferenceStore(DIAGRAM_PREFERENCES_HINT, getPreferenceStore());
		adapterFactory = org.eclipse.papyrus.infra.gmfdiag.common.Activator.getInstance()
				.getItemProvidersAdapterFactory();
		log = new LogHelper(instance);
		CustomDiagramPreferenceInitializer diagramPreferenceInitializer = new CustomDiagramPreferenceInitializer();
		diagramPreferenceInitializer.initializeDefaultPreferences();
	}

	/**
	 * @generated
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		adapterFactory = null;
		linkConstraints = null;
		initializers = null;
		oclFactory = null;
		instance = null;
		super.stop(context);
	}

	/**
	 * @generated
	 */
	public static UMLDiagramEditorPlugin getInstance() {
		return instance;
	}

	/**
	 * @generated
	 */
	@Override
	public IPreferenceStore getPreferenceStore() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store;
	}

	/**
	 * @generated
	 */
	public AdapterFactory getItemProvidersAdapterFactory() {
		return adapterFactory;
	}

	/**
	 * @generated
	 */
	public ImageDescriptor getItemImageDescriptor(Object item) {
		IItemLabelProvider labelProvider = (IItemLabelProvider) adapterFactory.adapt(item, IItemLabelProvider.class);
		if (labelProvider != null) {
			return ExtendedImageRegistry.getInstance().getImageDescriptor(labelProvider.getImage(item));
		}
		return null;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 *
	 * @generated
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
	 * @generated
	 * @param path
	 *            the path to image, either absolute (with plug-in id as first segment), or relative
	 *            for bundled images
	 * @return the image descriptor
	 */
	public static ImageDescriptor findImageDescriptor(String path) {
		final IPath p = new Path(path);
		if (p.isAbsolute() && p.segmentCount() > 1) {
			return AbstractUIPlugin.imageDescriptorFromPlugin(p.segment(0),
					p.removeFirstSegments(1).makeAbsolute().toString());
		} else {
			return getBundledImageDescriptor(p.makeAbsolute().toString());
		}
	}

	/**
	 * Returns an image for the image file at the given plug-in relative path. Client do not need to
	 * dispose this image. Images will be disposed automatically.
	 *
	 * @generated
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

	/**
	 * Returns string from plug-in's resource bundle
	 *
	 * @generated
	 */
	public static String getString(String key) {
		return Platform.getResourceString(getInstance().getBundle(), "%" + key); //$NON-NLS-1$
	}

	/**
	 * @generated
	 */
	public UMLDocumentProvider getDocumentProvider() {
		if (documentProvider == null) {
			documentProvider = new UMLDocumentProvider();
		}
		return documentProvider;
	}

	/**
	 * @generated
	 */
	public UMLBaseItemSemanticEditPolicy.LinkConstraints getLinkConstraints() {
		return linkConstraints;
	}

	/**
	 * @generated
	 */
	public void setLinkConstraints(UMLBaseItemSemanticEditPolicy.LinkConstraints lc) {
		this.linkConstraints = lc;
	}

	/**
	 * @generated
	 */
	public ElementInitializers getElementInitializers() {
		return initializers;
	}

	/**
	 * @generated
	 */
	public void setElementInitializers(ElementInitializers i) {
		this.initializers = i;
	}

	/**
	 * @generated
	 */
	public UMLOCLFactory getUMLOCLFactory() {
		return oclFactory;
	}

	/**
	 * @generated
	 */
	public void setUMLOCLFactory(UMLOCLFactory f) {
		this.oclFactory = f;
	}

	/**
	 * @generated
	 */
	public void logError(String error) {
		getLogHelper().warn(error);
	}

	/**
	 * @generated
	 */
	public void logError(String error, Throwable throwable) {
		getLogHelper().error(error, throwable);
	}

	/**
	 * @generated
	 */
	public void logInfo(String message) {
		getLogHelper().info(message);
	}

	/**
	 * @generated
	 */
	public void logInfo(String message, Throwable throwable) {
		getLogHelper().error(message, throwable);
	}

	/**
	 * @generated
	 */
	public LogHelper getLogHelper() {
		return myLogHelper;
	}
}
