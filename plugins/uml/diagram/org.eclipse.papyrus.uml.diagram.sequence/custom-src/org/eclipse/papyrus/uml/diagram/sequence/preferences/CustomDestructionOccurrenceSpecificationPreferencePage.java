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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.papyrus.infra.gmfdiag.common.preferences.PreferencesConstantsHelper;
import org.eclipse.papyrus.infra.gmfdiag.preferences.pages.AbstractPapyrusNodePreferencePage;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.SequenceDiagramEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomDestructionOccurrenceSpecificationPreferencePage extends AbstractPapyrusNodePreferencePage {

	/**
	 * @Override
	 */
	public static void initDefaults(IPreferenceStore store) {
		String key = SequenceDiagramEditPart.MODEL_ID + "_DestructionOccurrenceSpecification";
		store.setDefault(PreferencesConstantsHelper.getElementConstant(key, PreferencesConstantsHelper.WIDTH), 20);
		store.setDefault(PreferencesConstantsHelper.getElementConstant(key, PreferencesConstantsHelper.HEIGHT), 20);
	}

	/**
	 * @see org.eclipse.papyrus.infra.ui.preferences.AbstractPapyrusPreferencePage#getBundleId()
	 *
	 * @return
	 */
	@Override
	protected String getBundleId() {
		return UMLDiagramEditorPlugin.ID;
	}
}
