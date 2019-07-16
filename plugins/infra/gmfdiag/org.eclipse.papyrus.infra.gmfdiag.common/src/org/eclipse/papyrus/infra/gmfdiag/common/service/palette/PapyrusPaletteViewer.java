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
 *  Remi Schnekenburger (CEA LIST) remi.schnekenburger@cea.fr - Initial API and implementation
 *  Micka�l ADAM (ALL4TEC) mickael.adam@all4tec.net - bug 512343.
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.service.palette;

import java.lang.reflect.Constructor;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.palette.PaletteCustomizer;
import org.eclipse.gef.ui.palette.customize.PaletteCustomizerDialog;
import org.eclipse.gmf.runtime.gef.ui.palette.customize.PaletteCustomizerDialogEx;
import org.eclipse.gmf.runtime.gef.ui.palette.customize.PaletteViewerEx;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.tools.util.ClassLoaderHelper;
import org.eclipse.swt.widgets.Shell;

/**
 * Extended Palette Viewer, to have a new customize dialog
 * 
 * @since 3.0
 */
public class PapyrusPaletteViewer extends PaletteViewerEx {

	public static final String EXTENSION_ID = Activator.ID + ".paletteCustomization"; //$NON-NLS-1$

	public static final String CUSTOMIZER_ATTRIBUTE = "customizerDialog"; //$NON-NLS-1$

	/** cached dialog for the customization */
	private PaletteCustomizerDialog customizerDialog = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PaletteCustomizerDialog getCustomizerDialog() {
		if (customizerDialog == null) {
			IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_ID);

			// Load from extension point
			for (IConfigurationElement e : config) {
				String customizerClassName = e.getAttribute(CUSTOMIZER_ATTRIBUTE);
				try {
					Class<? extends PaletteCustomizerDialog> advancedCustomizerDialogClass = ClassLoaderHelper.loadClass(customizerClassName, PaletteCustomizerDialog.class, e.getContributor().getName());
					if (advancedCustomizerDialogClass != null) {
						Constructor<? extends PaletteCustomizerDialog> constructor = advancedCustomizerDialogClass.getConstructor(Shell.class, PaletteCustomizer.class, PaletteRoot.class);
						if (constructor != null) {
							customizerDialog = constructor.newInstance(getControl().getShell(), getCustomizer(), getPaletteRoot());
							break;
						}
					}
				} catch (Exception ex) {
					Activator.log.error(ex);
					continue;
				}
			}

			if (customizerDialog == null) {
				// be sure it is not null
				customizerDialog = new PaletteCustomizerDialogEx(getControl().getShell(), getCustomizer(), getPaletteRoot());
			}
		}
		return customizerDialog;
	}
}
