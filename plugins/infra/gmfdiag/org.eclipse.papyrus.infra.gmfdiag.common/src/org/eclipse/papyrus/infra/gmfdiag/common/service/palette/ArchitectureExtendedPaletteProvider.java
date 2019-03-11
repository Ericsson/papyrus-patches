/*****************************************************************************
 * Copyright (c) 2017 CEA LIST, ALL4TEC and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.service.palette;

import java.io.IOException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.gmf.runtime.diagram.ui.services.palette.IPaletteProvider;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;

/**
 * Palette provider to be used for palettes defined by models, when they are defined with an Architecture model.
 */
public class ArchitectureExtendedPaletteProvider extends ExtendedPluginPaletteProvider implements IPaletteProvider {

	/**
	 * Constructor.
	 */
	public ArchitectureExtendedPaletteProvider() {
	}

	/**
	 * Set the contributions.
	 * 
	 * @param descriptor
	 *            The contribution descriptor
	 */
	public void setContributions(final ArchitectureExtendedProviderDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		contributorID = descriptor.getContributionID();

		Resource resource = loadResourceFromPreferences(new ResourceSetImpl());
		if (null != resource) {
			// contribution have been redefined
			try {
				contributions = loadConfigurationModel(resource);
			} catch (IOException e) {
				Activator.log.error(e);
			}
		} else {
			contributions = descriptor.getDiagram().getPalettes();
		}
	}
}
