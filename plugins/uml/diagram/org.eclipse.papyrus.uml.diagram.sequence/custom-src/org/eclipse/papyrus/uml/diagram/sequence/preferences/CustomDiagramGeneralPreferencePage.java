/**
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
 *  CEA LIST - Initial API and implementation
 *  Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 521829, 526191, 526462
 */
package org.eclipse.papyrus.uml.diagram.sequence.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.papyrus.infra.gmfdiag.preferences.pages.DiagramPreferencePage;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.SequenceDiagramEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.messages.Messages;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;


public class CustomDiagramGeneralPreferencePage extends DiagramPreferencePage {

	/**
	 * preference page editor control for choosing if and which execution specifications should be automatically created with synchronous messages
	 */
	private RadioGroupFieldEditor executionSpecificationWithSyncMsg = null;

	/**
	 * preference page editor control for choosing if and which execution specifications should be automatically created with asynchronous messages
	 */
	private RadioGroupFieldEditor executionSpecificationWithAsyncMsg = null;

	private BooleanFieldEditor triggerValidation;

	/**
	 * preference key for asynchronous messages
	 */
	public static String PREF_EXECUTION_SPECIFICATION_ASYNC_MSG = "PREF_EXECUTION_SPECIFICATION_ASYNC_MSG"; //$NON-NLS-1$

	/**
	 * preference key for synchronous messages
	 */
	public static String PREF_EXECUTION_SPECIFICATION_SYNC_MSG = "PREF_EXECUTION_SPECIFICATION_SYNC_MSG"; //$NON-NLS-1$

	/**
	 * preference key to move down messages in the same time that message down.
	 *
	 * @since 5.0
	 */
	public static final String PREF_MOVE_BELOW_ELEMENTS_AT_MESSAGE_DOWN = "PREF_MOVE_BELOW_ELEMENTS_AT_MESSAGE_DOWN"; //$NON-NLS-1$

	/**
	 * preference key to move down messages in the same time that message is created.
	 *
	 * @since 5.0
	 */
	public static final String PREF_MOVE_BELOW_ELEMENTS_AT_MESSAGE_CREATION = "PREF_MOVE_BELOW_ELEMENTS_AT_MESSAGE_CREATION"; //$NON-NLS-1$

	/**
	 * Value to move down messages in the same time that message down.
	 *
	 * @since 5.0
	 */
	public static final int PREF_MOVE_BELOW_ELEMENTS_AT_MESSAGE_CREATION_VALUE = 40;

	/**
	 * preference key to trigger model validation after edition.
	 *
	 * @since 5.0
	 */
	public static final String PREF_TRIGGER_ASYNC_VALIDATION = "PREF_TRIGGER_ASYNC_VALIDATION"; //$NON-NLS-1$

	/**
	 * possible preference values
	 */
	public static final String CHOICE_BEHAVIOR_AND_REPLY = "CHOICE_BEHAVIOR_AND_REPLY"; //$NON-NLS-1$
	public static final String CHOICE_ACTION_AND_REPLY = "CHOICE_ACTION_AND_REPLY"; //$NON-NLS-1$
	public static final String CHOICE_BEHAVIOR = "CHOICE_BEHAVIOR"; //$NON-NLS-1$
	public static final String CHOICE_ACTION = "CHOICE_ACTION"; //$NON-NLS-1$
	public static final String CHOICE_NONE = "CHOICE_NONE"; //$NON-NLS-1$

	public CustomDiagramGeneralPreferencePage() {
		setPreferenceStore(UMLDiagramEditorPlugin.getInstance().getPreferenceStore());
		setPreferenceKey(SequenceDiagramEditPart.MODEL_ID);
	}

	/**
	 * Create new composite to contain the field editors
	 *
	 * @param parent
	 *            the parent Composite that the field editors will be added to
	 */
	@Override
	protected void addFields(Composite parent) {
		super.addFields(parent);
		Group notificationsGroup = new Group(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		notificationsGroup.setLayout(gridLayout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		notificationsGroup.setLayoutData(gridData);
		notificationsGroup.setText(Messages.DiagramsPreferencePage_notificationGroup_label);
		Composite composite = new Composite(notificationsGroup, SWT.NONE);
		createFieldEditors(composite);
		addField(executionSpecificationWithSyncMsg);
		addField(executionSpecificationWithAsyncMsg);


		Group otherGroup = new Group(parent, SWT.NONE);
		otherGroup.setLayout(new GridLayout(2, false));
		GridData otherGroupGridData = new GridData(GridData.FILL_HORIZONTAL);
		otherGroupGridData.grabExcessHorizontalSpace = true;
		otherGroupGridData.horizontalSpan = 2;
		otherGroup.setLayoutData(otherGroupGridData);
		otherGroup.setText(Messages.CustomDiagramGeneralPreferencePage_othersGroupLabel);
		addField(new BooleanFieldEditor(PREF_MOVE_BELOW_ELEMENTS_AT_MESSAGE_DOWN, Messages.CustomDiagramGeneralPreferencePage_MoveBelowElementsAtMessageDownDescription, otherGroup));
		addField(new IntegerFieldEditor(PREF_MOVE_BELOW_ELEMENTS_AT_MESSAGE_CREATION, Messages.CustomDiagramGeneralPreferencePage_MinimumSpaceBelowMessageAtCreation, otherGroup));
		triggerValidation = new BooleanFieldEditor(PREF_TRIGGER_ASYNC_VALIDATION,
				Messages.DiagramsPreferencePage_triggerValidation_label, otherGroup);
		addField(triggerValidation);
	}

	/**
	 *
	 * @param composite
	 */
	protected void createFieldEditors(Composite composite) {
		// preference for choosing if and which execution specifications should be automatically created with synchronous message
		// choice between behavior execution specification, action execution specification or nothing
		executionSpecificationWithSyncMsg = new RadioGroupFieldEditor(PREF_EXECUTION_SPECIFICATION_SYNC_MSG,
				Messages.DiagramsPreferencePage_executionSpecificationWithSyncMsg_label, 1,
				new String[][] {
						{ Messages.DiagramsPreferencePage_createBehaviorExecutionSpecificationAndReply, CHOICE_BEHAVIOR_AND_REPLY },
						{ Messages.DiagramsPreferencePage_createActionExecutionSpecificationAndReply, CHOICE_ACTION_AND_REPLY },
						{ Messages.DiagramsPreferencePage_createBehaviorExecutionSpecification, CHOICE_BEHAVIOR },
						{ Messages.DiagramsPreferencePage_createActionExecutionSpecification, CHOICE_ACTION },
						{ Messages.DiagramsPreferencePage_createNoExecutionSpecification, CHOICE_NONE }
				}, composite);

		// preference for choosing if and which execution specifications should be automatically created with asynchronous message
		// choice between behavior execution specification, action execution specification or nothing
		executionSpecificationWithAsyncMsg = new RadioGroupFieldEditor(PREF_EXECUTION_SPECIFICATION_ASYNC_MSG,
				Messages.DiagramsPreferencePage_executionSpecificationWithAsyncMsg_label, 1,
				new String[][] {
						{ Messages.DiagramsPreferencePage_createBehaviorExecutionSpecification, CHOICE_BEHAVIOR },
						{ Messages.DiagramsPreferencePage_createActionExecutionSpecification, CHOICE_ACTION },
						{ Messages.DiagramsPreferencePage_createNoExecutionSpecification, CHOICE_NONE }
				}, composite);

	}

	/**
	 * Initializes the default preference values for this preference store.
	 *
	 * @param IPreferenceStore
	 *            preferenceStore
	 */
	public static void initSpecificDefaults(IPreferenceStore preferenceStore) {
		preferenceStore.setDefault(PREF_EXECUTION_SPECIFICATION_SYNC_MSG, CHOICE_BEHAVIOR_AND_REPLY);
		preferenceStore.setDefault(PREF_EXECUTION_SPECIFICATION_ASYNC_MSG, CHOICE_NONE);

		preferenceStore.setDefault(PREF_MOVE_BELOW_ELEMENTS_AT_MESSAGE_DOWN, true);
		preferenceStore.setDefault(PREF_MOVE_BELOW_ELEMENTS_AT_MESSAGE_CREATION, PREF_MOVE_BELOW_ELEMENTS_AT_MESSAGE_CREATION_VALUE);

		preferenceStore.setDefault(PREF_TRIGGER_ASYNC_VALIDATION, false);

	}
}
