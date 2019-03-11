/*****************************************************************************
 * Copyright (c) 2017 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Remi Schnekenburger (CEA LIST) - Initial API and implementation
 *  Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - bug 512343
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.service.palette;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gmf.runtime.diagram.ui.services.palette.IPaletteProvider;
import org.eclipse.papyrus.infra.gmfdiag.common.messages.Messages;
import org.eclipse.papyrus.infra.gmfdiag.paletteconfiguration.Activator;
import org.eclipse.papyrus.infra.gmfdiag.paletteconfiguration.PaletteConfiguration;
import org.eclipse.papyrus.infra.gmfdiag.paletteconfiguration.PaletteconfigurationPackage;

/**
 * Palette provider to be used for palettes defined by models, when they are located in the workspace (deployment at runtime)
 */
public class WorkspaceExtendedPaletteProvider extends ExtendedPluginPaletteProvider implements IPaletteProvider {

	protected boolean loadResourceExceptionLogged = false;

	/**
	 * locally defines palette
	 *
	 * @param description
	 *            the description of the palette to build
	 */
	public void setContributions(IPaletteDescription description) {
		ResourceSet resourceSet = new ResourceSetImpl();
		Object paletteContributions = description.getContributions();
		contributions = Collections.emptyList();
		if (!(paletteContributions instanceof String)) {
			return;
		}
		Resource resource = loadResourceFromWorkspace((String) paletteContributions, resourceSet);
		if (resource != null) {
			try {
				resource.load(Collections.emptyMap());
				if (resource.getContents().size() > 0) {
					contributions = new ArrayList<PaletteConfiguration>(EcoreUtil.<PaletteConfiguration> getObjectsByType(resource.getContents(), PaletteconfigurationPackage.eINSTANCE.getPaletteConfiguration()));
				}
			} catch (IOException e) {
				if (!loadResourceExceptionLogged) {
					Activator.log.debug(Messages.WorkspaceExtendedPaletteProvider_ImpossibleToReadResourcePalette + description);
					loadResourceExceptionLogged = true;
				}
				contributions = Collections.emptyList();
			}
		}
	}
}
