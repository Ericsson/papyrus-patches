/*****************************************************************************
 * Copyright (c) 2016 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Mickael ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and Implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.service.palette;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gmf.runtime.diagram.ui.services.palette.IPaletteProvider;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.messages.Messages;
import org.eclipse.papyrus.infra.gmfdiag.paletteconfiguration.PaletteConfiguration;
import org.eclipse.papyrus.infra.gmfdiag.paletteconfiguration.PaletteconfigurationPackage;
import org.eclipse.papyrus.infra.types.ElementTypeSetConfiguration;
import org.eclipse.papyrus.infra.types.core.registries.ElementTypeSetConfigurationRegistry;

/**
 * Palette provider to be used for palettes defined by models paletteConfiguration, when they are locally defined
 */
public class LocalExtendedPaletteProvider extends ExtendedPluginPaletteProvider implements IPaletteProvider {

	/** if an exception is logged */
	protected boolean loadResourceExceptionLogged = false;

	/**
	 * locally defines palette
	 *
	 * @param description
	 *            the description of the palette to build
	 */
	public void setContributions(final IPaletteDescription description) {
		ResourceSet resourceSet = new ResourceSetImpl();
		Object paletteContributions = description.getContributions();
		contributions = Collections.emptyList();
		if (paletteContributions instanceof String) {

			Resource resource = null;
			IPath resourcePath = Activator.getInstance().getStateLocation().append((String) description.getContributions());
			URI uri = URI.createFileURI(resourcePath.toOSString());
			if (uri != null && uri.isFile()) {
				resource = resourceSet.createResource(uri);
			}

			if (null != resource) {
				try {
					resource.load(Collections.emptyMap());
					if (!resource.getContents().isEmpty()) {
						// Deploy local element type model if exist
						deployLocalElementTypeModels(description.getPaletteID());
						contributions = new ArrayList<PaletteConfiguration>(EcoreUtil.<PaletteConfiguration> getObjectsByType(resource.getContents(), PaletteconfigurationPackage.eINSTANCE.getPaletteConfiguration()));
					}
				} catch (IOException e) {
					if (!loadResourceExceptionLogged) {
						Activator.log.debug(Messages.LocalExtendedPaletteProvider_Error_ImpossibleToLoadRessource + description);
						loadResourceExceptionLogged = true;
					}
					contributions = Collections.emptyList();
				}
			}
		}
	}

	/**
	 * Deploy associated local element type model if exists(same name).
	 * 
	 * @param description
	 *            the {@link IPaletteDescription}
	 * @param resourceSet
	 */
	private void deployLocalElementTypeModels(final String paletteID) {

		ResourceSet resourceSet = new ResourceSetImpl();

		StringBuilder stringBuilderUI = new StringBuilder();
		stringBuilderUI.append(paletteID);
		stringBuilderUI.append("_UI.elementtypesconfigurations");//$NON-NLS-1$
		File fileUI = Activator.getInstance().getStateLocation().append(stringBuilderUI.toString()).toFile();// $NON-NLS-1$

		StringBuilder stringBuilderSem = new StringBuilder();
		stringBuilderSem.append(paletteID);
		stringBuilderSem.append("_Semantic.elementtypesconfigurations");//$NON-NLS-1$
		File fileSem = Activator.getInstance().getStateLocation().append(stringBuilderSem.toString()).toFile();

		// If files exist
		if (fileSem.canRead() && fileUI.canRead()) {

			URI elementTypeUIURI = URI.createFileURI(fileUI.getAbsolutePath());
			URI elementTypeSemURI = URI.createFileURI(fileSem.getAbsolutePath());

			if (null != elementTypeUIURI && null != elementTypeSemURI) {
				// Create resource
				Resource elementTypeUIResource = resourceSet.createResource(elementTypeUIURI);
				Resource elementTypeSemResource = resourceSet.createResource(elementTypeSemURI);

				// Try to load files
				try {
					elementTypeUIResource.load(Collections.emptyMap());
					elementTypeSemResource.load(Collections.emptyMap());
				} catch (IOException e) {
					Activator.log.error(e);
				}
				if (!elementTypeUIResource.getContents().isEmpty() && !elementTypeSemResource.getContents().isEmpty()) {

					// deploy element types configuration files
					String clientContext = "org.eclipse.papyrus.infra.services.edit.TypeContext";//$NON-NLS-1$

					ElementTypeSetConfigurationRegistry.getInstance().unload(clientContext, ((ElementTypeSetConfiguration) elementTypeSemResource.getContents().get(0)).getIdentifier());
					ElementTypeSetConfigurationRegistry.getInstance().unload(clientContext, ((ElementTypeSetConfiguration) elementTypeUIResource.getContents().get(0)).getIdentifier());

					ElementTypeSetConfigurationRegistry.getInstance().loadElementTypeSetConfiguration(clientContext, ((ElementTypeSetConfiguration) elementTypeSemResource.getContents().get(0)));
					ElementTypeSetConfigurationRegistry.getInstance().loadElementTypeSetConfiguration(clientContext, ((ElementTypeSetConfiguration) elementTypeUIResource.getContents().get(0)));
				}
			}
		}
	}

}
