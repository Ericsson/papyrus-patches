/*****************************************************************************
 * Copyright (c) 2013, 2016 CEA LIST, Christian W. Damus, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   Christian W. Damus - bug 485220
 *   
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.css;

import org.eclipse.papyrus.infra.core.log.LogHelper;
import org.eclipse.papyrus.infra.gmfdiag.css.spi.IStylingProvider;
import org.eclipse.papyrus.uml.diagram.css.helper.UMLStylingProvider;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.papyrus.uml.diagram.css"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	public static LogHelper log;

	private ServiceRegistration<IStylingProvider> stylingProviderReg;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		log = new LogHelper(this);

		stylingProviderReg = context.registerService(IStylingProvider.class, new UMLStylingProvider(), null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (stylingProviderReg != null) {
			stylingProviderReg.unregister();
			stylingProviderReg = null;
		}

		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
