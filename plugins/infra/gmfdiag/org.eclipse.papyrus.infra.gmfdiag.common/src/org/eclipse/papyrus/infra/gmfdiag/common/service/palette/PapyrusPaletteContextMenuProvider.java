/*****************************************************************************
 * Copyright (c) 2009 CEA LIST.
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
 *  Micka�l ADAM (ALL4TEC) mickael.adam@all4tec.net - Move from oep.uml.diagram.common, see bug 512343.
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.service.palette;

import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.palette.PaletteContextMenuProvider;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.jface.action.IMenuManager;

/**
 * Specific context menu provider for Papyrus diagrams
 * @since 3.0
 */
public class PapyrusPaletteContextMenuProvider extends PaletteContextMenuProvider {

	/**
	 * Creates a new PaletteContextMenuProvider
	 *
	 * @param palette
	 *            the palette for which the context menu has to be buuild
	 */
	public PapyrusPaletteContextMenuProvider(PaletteViewer palette) {
		super(palette);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void buildContextMenu(IMenuManager menu) {
		super.buildContextMenu(menu);

		// adds a new action with sub-menu to display which providers must be
		// displayed and which
		// ones should be hidden
		menu.appendToGroup(GEFActionConstants.GROUP_REST, new PaletteMenuAction(getPaletteViewer()));
	}

}
