/*****************************************************************************
 * Copyright (c) 2014 CEA LIST.
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
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.messages.Messages;
import org.eclipse.papyrus.infra.gmfdiag.common.strategy.StrategyEditor;
import org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.PasteStrategyManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Allow to select strategies for Papyrus paste
 */
public class PastePreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Keep the references
	 */
	public static final String KEEP_EXTERNAL_REFERENCES = "KEEP_EXTERNAL_REFERENCES"; //$NON-NLS-1$

	/**
	 * Editor for managing paste strategies
	 */
	private StrategyEditor editor;

	public PastePreferencesPage() {
		super(Messages.PastePreferencesPage_PageTitle, org.eclipse.papyrus.infra.widgets.Activator.getDefault().getImageDescriptor("/icons/papyrus.png"), FLAT); //$NON-NLS-1$
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getInstance().getPreferenceStore());
		setDescription(Messages.PastePreferencesPage_PageDescription);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control self = super.createContents(parent);
		editor = new StrategyEditor((Composite) self, SWT.NONE, PasteStrategyManager.getInstance(), Messages.PastePreferencesPage_StrategiesDescription);
		editor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return self;
	}

	@Override
	protected void performDefaults() {
		if (editor != null && !editor.isDisposed()) {
			PasteStrategyManager.getInstance().restoreDefaults();
			editor.refresh();
		}
		super.performDefaults();
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(KEEP_EXTERNAL_REFERENCES, Messages.PastePreferencesPage_KeepReferenceDescription, getFieldEditorParent()));
	}

}
