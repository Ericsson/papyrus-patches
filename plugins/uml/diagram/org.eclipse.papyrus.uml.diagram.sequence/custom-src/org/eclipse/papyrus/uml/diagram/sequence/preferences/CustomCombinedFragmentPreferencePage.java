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

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.gmf.runtime.common.ui.preferences.CheckBoxFieldEditor;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.papyrus.infra.gmfdiag.common.preferences.PreferencesConstantsHelper;
import org.eclipse.papyrus.infra.gmfdiag.preferences.pages.AbstractPapyrusNodePreferencePage;
import org.eclipse.papyrus.infra.gmfdiag.preferences.ui.AbstractGroup;
import org.eclipse.papyrus.infra.gmfdiag.preferences.ui.BackgroundColor;
import org.eclipse.papyrus.infra.gmfdiag.preferences.ui.DecorationGroup;
import org.eclipse.papyrus.infra.gmfdiag.preferences.ui.LabelGroup;
import org.eclipse.papyrus.infra.gmfdiag.preferences.ui.NodeColorGroup;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.SequenceDiagramEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.part.Messages;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomCombinedFragmentPreferencePage extends AbstractPapyrusNodePreferencePage {

	public static final String compartments[] = { Messages.CombinedFragmentCombinedFragmentCompartmentEditPart_title };

	/**
	 * Constructor.
	 *
	 */
	public CustomCombinedFragmentPreferencePage() {
		super();
	}

	/**
	 * @Override
	 */
	public static void initDefaults(IPreferenceStore store) {
		String key = SequenceDiagramEditPart.MODEL_ID + "_CombinedFragment";
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

	static class NodeCompartmentGroupEx extends AbstractGroup {

		/** the list owning the compartment names for the UML element */
		private final List<String> myCompartments;

		private final IPreferenceStore myPreferenceStore;

		private final Set<String> compartmentsWithTitle;

		/**
		 *
		 * Constructor.
		 *
		 * @param parent
		 * @param title
		 * @param dialogPage
		 * @param compartmentsName
		 */
		public NodeCompartmentGroupEx(Composite parent, String title, DialogPage dialogPage, List<String> compartmentsName, Set<String> compartmentsWithTitle, IPreferenceStore store) {
			super(parent, title, dialogPage);
			this.myCompartments = compartmentsName;
			this.compartmentsWithTitle = compartmentsWithTitle;
			myPreferenceStore = store;
			createContent(parent);
		}

		/**
		 * create the content.
		 *
		 * @param parent
		 *            : the parent composite
		 */
		protected void createContent(Composite parent) {
			for (String compartment : myCompartments) {
				addCompartmentVisibilityGroup(parent, compartment);
			}
		}

		protected void addCompartmentVisibilityGroup(Composite parent, String compartment) {
			// show Compartment Visibility and CompartmentName Visibility items in the same row
			Group group = new Group(parent, SWT.NONE);
			group.setLayout(new GridLayout(2, true));
			group.setText(compartment);
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 2;
			group.setLayoutData(gridData);
			String compartmentVisibilityPreference = PreferencesConstantsHelper.getCompartmentElementConstant(getKey(), compartment, PreferencesConstantsHelper.COMPARTMENT_VISIBILITY);
			String compartmentVisibilityLabel = "Show compartment";
			Button showCompartmentButton = addCheckboxField(group, compartmentVisibilityPreference, compartmentVisibilityLabel);
			if (this.compartmentsWithTitle.contains(compartment)) {
				String compartmentNameVisibilityPreference = PreferencesConstantsHelper.getCompartmentElementConstant(getKey(), compartment, PreferencesConstantsHelper.COMPARTMENT_NAME_VISIBILITY);
				String compartmentNameVisibilityLabel = "Show name";
				Button showNameButton = addCheckboxField(group, compartmentNameVisibilityPreference, compartmentNameVisibilityLabel);
				boolean showCompartmentIsNotChecked = !myPreferenceStore.getBoolean(compartmentVisibilityPreference);
				if (showCompartmentIsNotChecked) {
					showNameButton.setEnabled(false);
				}
				createDependency(showCompartmentButton, new Control[] { showNameButton });
			}
		}

		protected Button addCheckboxField(Composite parent, String preferenceKey, String label) {
			// show Compartment Visibility and CompartmentName Visibility items in the same row
			// as CheckBoxFieldEditor resets layout data to fit the grid we create this stub plate
			// @see #doFillIntoGrid
			Composite plate = new Composite(parent, SWT.NONE);
			plate.setLayoutData(new GridData());
			CheckBoxFieldEditor compartmentVisibilityBooleanFieldEditor = new CheckBoxFieldEditor(preferenceKey, label, plate);
			Button checkbox = compartmentVisibilityBooleanFieldEditor.getCheckbox();
			compartmentVisibilityBooleanFieldEditor.setPage(getDialogPage());
			addFieldEditor(compartmentVisibilityBooleanFieldEditor);
			return checkbox;
		}

		protected void createDependency(final Button master, final Control[] slaves) {
			SelectionListener dependencyListener = new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean state = master.getSelection();
					for (int i = 0; i < slaves.length; i++) {
						slaves[i].setEnabled(state);
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			};
			master.addSelectionListener(dependencyListener);
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
