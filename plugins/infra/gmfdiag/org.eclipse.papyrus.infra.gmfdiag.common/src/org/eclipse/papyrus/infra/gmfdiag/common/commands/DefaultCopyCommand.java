/*****************************************************************************
 * Copyright (c) 2014, 2016 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Benoit Maggi (CEA LIST) benoit.maggi@cea.fr - Initial API and implementation
 *  Christian W. Damus - bugs 502461, 508404
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.commands;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.common.command.AbstractCommand.NonDirtying;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AbstractOverrideableCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.papyrus.infra.core.clipboard.ICopierFactory;
import org.eclipse.papyrus.infra.core.clipboard.PapyrusClipboard;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.preferences.PastePreferencesPage;

/**
 * Command that puts a list of object in the clipboard, and that copy them.
 */
public class DefaultCopyCommand extends AbstractOverrideableCommand implements NonDirtying {

	/** list of objects to put in the clipboard */
	private final Collection<Object> objectsToPutInClipboard;

	public Collection<Object> getObjectsToPutInClipboard() {
		return objectsToPutInClipboard;
	}

	/** old list of the clipboard, for undo */
	private Collection<Object> oldClipboardContent;

	/**
	 * Creates a new Command that set the new content of the clipboard
	 *
	 * @param domain
	 *            editing domain for which the clipboard is set.
	 */
	public DefaultCopyCommand(EditingDomain domain, PapyrusClipboard papyrusClipboard, Collection<EObject> pObjectsToPutInClipboard) {
		super(domain);
		objectsToPutInClipboard = new ArrayList<Object>();
		boolean keepReferences = Activator.getInstance().getPreferenceStore().getBoolean(PastePreferencesPage.KEEP_EXTERNAL_REFERENCES);
		EcoreUtil.Copier copier = ICopierFactory.getInstance(domain.getResourceSet(), keepReferences).get();
		copier.copyAll(pObjectsToPutInClipboard);
		copier.copyReferences();
		papyrusClipboard.addAllInternalCopyInClipboard(copier);
		objectsToPutInClipboard.add(copier.values());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doExecute() {
		oldClipboardContent = domain.getClipboard();
		domain.setClipboard(objectsToPutInClipboard);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doUndo() {
		domain.setClipboard(oldClipboardContent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doRedo() {
		domain.setClipboard(objectsToPutInClipboard);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean prepare() {
		return domain != null;
	}

}
