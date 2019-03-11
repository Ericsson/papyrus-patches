/*****************************************************************************
 * Copyright (c) 2010 CEA
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Soyatec - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.preferences;

import org.eclipse.gmf.runtime.diagram.ui.preferences.DiagramsPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomDiagramPreferenceInitializer extends DiagramPreferenceInitializer {

	/**
	 * Add the diagram preference page to the initialization
	 *
	 * @Override (update at each gmf change) diagram preference page
	 */
	@Override
	public void initializeDefaultPreferences() {
		super.initializeDefaultPreferences();
		IPreferenceStore store = getPreferenceStore();

		// diagram preference page
		DiagramsPreferencePage.initDefaults(store);


		// Custom preference pages.
		CustomCombinedFragmentPreferencePage.initDefaults(store);
		CustomConsiderIgnoreFragmentPreferencePage.initDefaults(store);
		CustomDestructionOccurrenceSpecificationPreferencePage.initDefaults(store);
		CustomInteractionOperandPreferencePage.initDefaults(store);
		CustomDiagramGeneralPreferencePage.initSpecificDefaults(store);
	}
}
