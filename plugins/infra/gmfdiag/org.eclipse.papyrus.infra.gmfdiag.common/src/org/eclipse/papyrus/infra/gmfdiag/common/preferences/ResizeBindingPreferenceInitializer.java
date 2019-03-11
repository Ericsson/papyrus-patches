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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;

/**
 * Initializer for the resize binding preference page.
 * @since 3.0
 */
public class ResizeBindingPreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getInstance().getPreferenceStore();
		// By default, enable the invert resize binding
		store.setDefault(PreferencesConstantsHelper.getPapyrusEditorConstant(PreferencesConstantsHelper.INVERT_BINDING_FOR_DEFAULT_RESIZE_AND_CONSTRAINED_RESIZE), Boolean.TRUE);
	}

}
