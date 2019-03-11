/*****************************************************************************
 * Copyright (c) 2010, 2016 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Patrick Tessier (CEA LIST) Patrick.tessier@cea.fr - Initial API and implementation
 *  Christian W. Damus - bug 485220
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.service;

import java.awt.datatransfer.Clipboard;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.core.command.UnexecutableCommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.providers.IPasteCommandProvider;

/**
 * this singleton looks for in extension point to find a pasteCommandProvider
 * and can provide a paste command
 */


public class PasteCommandService {

	private static final String PASTECOMMANDPROVIDER_EXTENSION_ID = Activator.ID + ".pasteCommandProvider"; //$NON-NLS-1$

	private static final String REALIZATION = "realization"; //$NON-NLS-1$

	private static final String PRIORITY = "priority"; //$NON-NLS-1$

	private static PasteCommandService instance = null;

	public static PasteCommandService getInstance() {
		if (instance == null) {
			instance = new PasteCommandService();
		}
		return instance;
	}


	/**
	 * Load an instance of a class
	 *
	 * @param element
	 *            the extension point
	 * @param classAttribute
	 *            the name of the class to load
	 * @return the loaded Class
	 * @throws Exception
	 *             if the class is not loaded
	 */
	protected static Object createExtension(final IConfigurationElement element, final String classAttribute) throws Exception {
		return element.createExecutableExtension(classAttribute);
	}

	protected Map<String, IPasteCommandProvider> pasteCommandProviderMap;


	private PasteCommandService() {
		// Reading data from plugins
		IConfigurationElement[] configElements = Platform.getExtensionRegistry().getConfigurationElementsFor(PASTECOMMANDPROVIDER_EXTENSION_ID);
		pasteCommandProviderMap = new HashMap<String, IPasteCommandProvider>();
		for (int i = 0; i < configElements.length; i++) {
			getPasteCommandProvider(configElements[i]);
		}

	}

	/**
	 * Load one rule
	 *
	 * @param element
	 *            the extension point
	 */
	protected void getPasteCommandProvider(IConfigurationElement element) {
		IPasteCommandProvider pasteCommandProvider = null;
		try {
			pasteCommandProvider = (IPasteCommandProvider) createExtension(element, REALIZATION);
			String priority = element.getAttribute(PRIORITY);
			pasteCommandProviderMap.put(priority, pasteCommandProvider);

		} catch (Exception e) {
			Activator.log.error("- " + pasteCommandProvider + " can not be loaded: " + e.getLocalizedMessage(), e); //$NON-NLS-1$
		}
	}

	protected IPasteCommandProvider lookForProvider() {
		String[] priority = { "Highest", "High", "Medium", "Low", "Lowest" };
		IPasteCommandProvider selectedProvider = null;
		int i = 0;
		while (selectedProvider == null && i < priority.length) {
			selectedProvider = pasteCommandProviderMap.get(priority[i]);
			i++;
		}
		return selectedProvider;
	}

	/**
	 * return the paste command to execute by taking account parameter
	 *
	 * @param targetEditPart
	 *            the target where object will be paste
	 * @param systemClipboard
	 *            contains info form the system copy paste
	 * @param papyrusCliboard
	 *            the list of views to paste
	 * @return a command
	 */
	public ICommand getPasteViewCommand(GraphicalEditPart targetEditPart, Clipboard systemClipboard, Collection<Object> papyrusCliboard) {
		IPasteCommandProvider selectedProvider = lookForProvider();
		if (selectedProvider == null) {
			return UnexecutableCommand.INSTANCE;
		}
		return selectedProvider.getPasteViewCommand(targetEditPart, systemClipboard, papyrusCliboard);
	}

	/**
	 * return the paste command to execute by taking account parameter. It copy also element of the semantic model
	 *
	 * @param targetEditPart
	 *            the target where object will be paste
	 * @param systemClipboard
	 *            contains info form the system copy paste
	 * @param papyrusCliboard
	 *            the list of views to paste
	 * @return a command
	 */
	public ICommand getPasteWithModelCommand(GraphicalEditPart targetEditPart, Clipboard systemClipboard, Collection<Object> papyrusCliboard) {
		IPasteCommandProvider selectedProvider = lookForProvider();
		if (selectedProvider == null) {
			return UnexecutableCommand.INSTANCE;
		}
		return selectedProvider.getPasteWithModelCommand(targetEditPart, systemClipboard, papyrusCliboard);
	}
}
