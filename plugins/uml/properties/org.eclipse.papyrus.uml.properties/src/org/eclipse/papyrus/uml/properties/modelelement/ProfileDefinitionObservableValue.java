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

import java.util.List;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.papyrus.uml.tools.commands.DefineProfileCommand;
import org.eclipse.papyrus.uml.tools.commands.UndefineProfileCommand;
import org.eclipse.papyrus.uml.tools.databinding.PapyrusObservableList;
import org.eclipse.papyrus.uml.tools.profile.definition.IPapyrusVersionConstants;
import org.eclipse.papyrus.uml.tools.profile.definition.PapyrusDefinitionAnnotation;
import org.eclipse.papyrus.uml.tools.util.IPapyrusProfileDefinition;
import org.eclipse.uml2.uml.Profile;

/**
 * Observable for Profile Definition list of a profile.
 */
public class ProfileDefinitionObservableValue extends PapyrusObservableList implements IObservable {

	/** The profile which contains definitions. */
	private Profile profile = null;

	/**
	 * Instantiates a new profile definition observable value.
	 *
	 * @param profile
	 *            the profile
	 * @param wrappedList
	 *            the wrapped list
	 * @param domain
	 *            the domain
	 * @param source
	 *            the source
	 * @param feature
	 *            the feature
	 */
	public ProfileDefinitionObservableValue(final Profile profile, final List<?> wrappedList, final EditingDomain domain, final EObject source, final EStructuralFeature feature) {
		super(wrappedList, domain, source, feature);
		this.profile = profile;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.uml.tools.databinding.PapyrusObservableList#getAddCommand(java.lang.Object)
	 *
	 * @param value
	 * @return
	 */
	@Override
	public Command getAddCommand(final Object value) {
		Command addCommand = null;
		if (value instanceof IPapyrusProfileDefinition) {
			EObject rootProfile = EcoreUtil.getRootContainer(profile);
			if (rootProfile instanceof Profile) {
				addCommand = new DefineProfileCommand((TransactionalEditingDomain) editingDomain, (IPapyrusProfileDefinition) value, (Profile) rootProfile);
			}
		} else {
			// Get command from parent
			addCommand = super.getAddCommand(value);
		}

		return addCommand;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.uml.tools.databinding.PapyrusObservableList#getRemoveCommand(java.lang.Object)
	 */
	@Override
	public Command getRemoveCommand(final Object value) {
		Command removeCommand = null;
		if (value instanceof EPackage) {
			EObject rootProfile = EcoreUtil.getRootContainer(profile);
			PapyrusDefinitionAnnotation papyrusAnnotation = PapyrusDefinitionAnnotation.parseEAnnotation(((EPackage) value).getEAnnotation(IPapyrusVersionConstants.PAPYRUS_EANNOTATION_SOURCE));
			if (rootProfile instanceof Profile) {
				removeCommand = new UndefineProfileCommand((TransactionalEditingDomain) editingDomain, papyrusAnnotation, (Profile) rootProfile);
			}
		} else {
			removeCommand = super.getRemoveCommand(value);
		}
		return removeCommand;
	}
}
