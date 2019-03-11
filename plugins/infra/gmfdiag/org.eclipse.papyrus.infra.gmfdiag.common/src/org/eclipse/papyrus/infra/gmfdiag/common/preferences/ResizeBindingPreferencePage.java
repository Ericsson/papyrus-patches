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
 *      Thanh Liem PHAN (ALL4TEC) thanhliem.phan@all4tec.net - Bug 513580
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.messages.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The preference page for resize binding.
 */
public class ResizeBindingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Constructor.
	 */
	public ResizeBindingPreferencePage() {
		super(Messages.ResizeBindingPreferencePage_PageTitle, org.eclipse.papyrus.infra.widgets.Activator.getDefault().getImageDescriptor("/icons/papyrus.png"), FLAT); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(final IWorkbench workbench) {
		setPreferenceStore(Activator.getInstance().getPreferenceStore());
		setDescription(Messages.ResizeBindingPreferencePage_PageDescription);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createFieldEditors() {

		addField(new BooleanFieldEditor(PreferencesConstantsHelper.getPapyrusEditorConstant(PreferencesConstantsHelper.INVERT_BINDING_FOR_DEFAULT_RESIZE_AND_CONSTRAINED_RESIZE),
				Messages.ResizeBindingPreferencePage_HoldingShiftToRecalculateAnchorPositionWhenResizing, getFieldEditorParent()));
	}
}
