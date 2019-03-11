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
 *  Micka�l ADAM (ALL4TEC) mickael.adam@all4tec.net - bug 512343
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.service.palette;

import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gmf.runtime.gef.ui.palette.customize.PaletteEntryState;
import org.eclipse.ui.IMemento;

/**
 * Extended palette state, that also manages parent modification, etc.
 * @since 3.0
 */
public class PapyrusPaletteEntryState extends PaletteEntryState {

	/** key for the parent field */
	public static final String PARENT_ID_KEY = "parent";

	/** value of the parent ID field */
	protected String parentID;

	/**
	 * Creates a new PapyrusPaletteEntryState.
	 *
	 * @param entry
	 *            the palette entry to manage
	 */
	public PapyrusPaletteEntryState(PaletteEntry entry) {
		super(entry);
	}

	@Override
	public void applyChangesFromMemento(IMemento entryMemento) {
		super.applyChangesFromMemento(entryMemento);
		PaletteEntry entry = getPaletteEntry();

		String sValue = entryMemento.getString(PARENT_ID_KEY);
		if (sValue != null) {
			if (!entry.getParent().getId().equals(sValue)) {
				// adds to the new container, but does nto remove from old one,
				// because of
				// iterator...
				PaletteContainer container = PaletteUtil.getContainerByID(entry, sValue);
				container.add(entry);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rollback() {
		super.rollback();
		PaletteEntry entry = getPaletteEntry();
		entry.setParent(PaletteUtil.getContainerByID(entry, parentID));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storeChangesInMemento(IMemento memento) {
		super.storeChangesInMemento(memento);

		PaletteEntry entry = getPaletteEntry();

		// stores the parent id name
		if (parentID != null && !parentID.equals(entry.getParent().getId()) || (parentID == null && entry.getParent() != null)) {
			memento.putString(PARENT_ID_KEY, entry.getParent().getId());
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storeState() {
		super.storeState();
		PaletteEntry entry = getPaletteEntry();
		parentID = entry.getParent().getId();
	}

}
