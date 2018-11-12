/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Gabriel Pascual (ALL4TEC) gabriel.pascual@all4tec.net - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.properties.modelelement;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.papyrus.infra.properties.ui.creation.EcorePropertyEditorFactory;
import org.eclipse.papyrus.infra.widgets.creation.ReferenceValueFactory;
import org.eclipse.papyrus.uml.profile.ui.dialogs.ProfileDefinitionDialog;
import org.eclipse.papyrus.uml.tools.util.PapyrusProfileDefinition;
import org.eclipse.swt.widgets.Control;
import org.eclipse.uml2.uml.Profile;

/**
 * Property Editor Factory for Profile Definition.
 */
public class ProfileDefinitionValueFactory extends EcorePropertyEditorFactory implements ReferenceValueFactory {

	/** The Constant EDIT_DIALOG_TITLE. */
	private static final String EDIT_DIALOG_TITLE = "Edit Profile Definition"; //$NON-NLS-1$

	/** The profile to define. */
	private final Profile profileToDefine;

	/**
	 * Instantiates a new profile definition value factory.
	 *
	 * @param profile
	 *            The profile.
	 * @param referenceIn
	 *            The reference.
	 */
	public ProfileDefinitionValueFactory(final Profile profile, final EReference referenceIn) {
		super(referenceIn);
		profileToDefine = profile;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.views.properties.creation.EcorePropertyEditorFactory#doCreateObject(org.eclipse.swt.widgets.Control, java.lang.Object)
	 */
	@Override
	protected Object doCreateObject(final Control widget, final Object context) {
		Object createdObject = null;
		EObject rootProfile = EcoreUtil.getRootContainer(profileToDefine);
		if (rootProfile instanceof Profile) {
			ProfileDefinitionDialog dialog = new ProfileDefinitionDialog(widget.getShell(), (Profile) rootProfile);
			int returnDialog = dialog.open();
			if (Dialog.OK == returnDialog) {
				createdObject = new PapyrusProfileDefinition(dialog.getPapyrusDefinitionAnnotation(), dialog.saveConstraintInDefinition());
			}
		}

		return createdObject;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.views.properties.creation.EcorePropertyEditorFactory#getEditionDialogTitle(java.lang.Object)
	 */
	@Override
	public String getEditionDialogTitle(final Object objectToEdit) {
		String dialogTitle = null;
		if (objectToEdit instanceof EPackage) {
			dialogTitle = EDIT_DIALOG_TITLE;
		} else {
			dialogTitle = super.getEditionDialogTitle(objectToEdit);
		}

		return dialogTitle;
	}
}
