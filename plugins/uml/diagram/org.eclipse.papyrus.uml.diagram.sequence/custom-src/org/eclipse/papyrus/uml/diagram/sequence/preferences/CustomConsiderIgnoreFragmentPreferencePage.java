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

import java.util.TreeMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.papyrus.infra.gmfdiag.common.preferences.PreferencesConstantsHelper;
import org.eclipse.papyrus.infra.gmfdiag.preferences.pages.AbstractPapyrusNodePreferencePage;
import org.eclipse.papyrus.infra.gmfdiag.preferences.ui.BackgroundColor;
import org.eclipse.papyrus.infra.gmfdiag.preferences.ui.DecorationGroup;
import org.eclipse.papyrus.infra.gmfdiag.preferences.ui.LabelGroup;
import org.eclipse.papyrus.infra.gmfdiag.preferences.ui.NodeColorGroup;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.SequenceDiagramEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.part.Messages;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.preferences.CustomCombinedFragmentPreferencePage.NodeCompartmentGroupEx;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomConsiderIgnoreFragmentPreferencePage extends AbstractPapyrusNodePreferencePage {

	public static final String compartments[] = { Messages.CombinedFragmentCombinedFragmentCompartmentEditPart_title };


	/**
	 * @Override
	 */
	public CustomConsiderIgnoreFragmentPreferencePage() {
		super();
		setPreferenceKey(SequenceDiagramEditPart.MODEL_ID + "_ConsiderIgnoreFragment");
	}

	/**
	 * @Override
	 */
	public static void initDefaults(IPreferenceStore store) {
		String key = SequenceDiagramEditPart.MODEL_ID + "_ConsiderIgnoreFragment";
		store.setDefault(PreferencesConstantsHelper.getElementConstant(key, PreferencesConstantsHelper.WIDTH), 40);
		store.setDefault(PreferencesConstantsHelper.getElementConstant(key, PreferencesConstantsHelper.HEIGHT), 40);
		for (String name : compartments) {
			String preferenceName = PreferencesConstantsHelper.getCompartmentElementConstant(key, name, PreferencesConstantsHelper.COMPARTMENT_VISIBILITY);
			store.setDefault(preferenceName, true);
		}
	}

	@Override
	protected TreeMap<String, Boolean> getCompartmentTitleVisibilityPreferences() {
		TreeMap<String, Boolean> map = new TreeMap<>();
		for (String name : compartments) {
			map.put(name, Boolean.FALSE);
		}
		return map;
	}

	@Override
	protected void initializeCompartmentsList() {
		for (String name : compartments) {
			this.compartmentsList.add(name);
		}
	}

	@Override
	protected TreeMap<String, String> getLabelRole() {
		return new TreeMap<>();
	}

	@Override
	protected void createPageContents(Composite parent) {
		super.createPageContents(parent);
		NodeColorGroup colorGroupForNodeComposite = new NodeColorGroup(parent, getPreferenceKey(), this);
		addPreferenceGroup(colorGroupForNodeComposite);
		BackgroundColor backgroundColorGroup = new BackgroundColor(parent, getPreferenceKey(), this);
		addPreferenceGroup(backgroundColorGroup);
		DecorationGroup decorationGroup = new DecorationGroup(parent, getPreferenceKey(), this);
		addPreferenceGroup(decorationGroup);
		if (!compartmentsList.isEmpty()) {
			NodeCompartmentGroupEx compartmentGroup = new NodeCompartmentGroupEx(parent, getPreferenceKey(), this, compartmentsList, getCompartmentTitleVisibilityPreferences().keySet(), getPreferenceStore());
			addPreferenceGroup(compartmentGroup);
		}
		// Label role group
		if (!getLabelRole().isEmpty()) {
			LabelGroup compartmentGroup = new LabelGroup(parent, getPreferenceKey(), this, getLabelRole());
			addPreferenceGroup(compartmentGroup);
		}
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
