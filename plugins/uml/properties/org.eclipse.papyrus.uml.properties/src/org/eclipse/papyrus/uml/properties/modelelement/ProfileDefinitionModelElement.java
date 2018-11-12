/*****************************************************************************
 * Copyright (c) 2013, 2014, 2018 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Camille Letavernier (CEA LIST) camille.letavernier@cea.fr - Initial API and implementation
 *  Christian W. Damus (CEA) - bug 323802
 *  Gabriel Pascual (ALL4TEC) gabriel.pascual@all4tec.net - Bug 447665
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.properties.modelelement;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.RollbackException;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.utils.TransactionHelper;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.emf.utils.ServiceUtilsForEObject;
import org.eclipse.papyrus.infra.properties.ui.modelelement.AbstractModelElement;
import org.eclipse.papyrus.infra.services.labelprovider.service.LabelProviderService;
import org.eclipse.papyrus.infra.widgets.creation.ReferenceValueFactory;
import org.eclipse.papyrus.uml.tools.Activator;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.util.UMLUtil;

/**
 * Model Element for Profile Definitions.
 */
public class ProfileDefinitionModelElement extends AbstractModelElement {

	/** The Constant RETRIEVE_EANNOTATION_ERROR. */
	private static final String RETRIEVE_EANNOTATION_ERROR = "Failed to retrieve Profile Defintion root annotation."; //$NON-NLS-1$

	/**
	 * The Profile::definitions property. Contains the list of definitions (EPackage) of this profile
	 */
	public static final String DEFINITIONS = "definitions"; //$NON-NLS-1$

	protected Profile profile;

	public ProfileDefinitionModelElement(Profile profile) {
		this.profile = profile;
	}

	@Override
	protected IObservable doGetObservable(String propertyPath) {
		if (DEFINITIONS.equals(propertyPath)) {
			if (profile != null) {

				EditingDomain domain = EMFHelper.resolveEditingDomain(profile);
				EAnnotation definitions = getProfileDefinitionsRootAnnotation((TransactionalEditingDomain) domain);
				return new ProfileDefinitionObservableValue(profile, EMFObservables.observeList(definitions, EcorePackage.eINSTANCE.getEAnnotation_Contents()), domain, definitions, EcorePackage.eINSTANCE.getEAnnotation_Contents());

			}
		}

		return Observables.emptyObservableList();
	}

	/**
	 * Gets the profile definitions root annotation.
	 *
	 * @param domain
	 *            the domain
	 * @return the profile definitions root annotation
	 */
	private EAnnotation getProfileDefinitionsRootAnnotation(final TransactionalEditingDomain domain) {

		try {
			TransactionHelper.run(domain, new Runnable() {
				@Override
				public void run() {
					UMLUtil.getEAnnotation(profile, UMLUtil.UML2_UML_PACKAGE_2_0_NS_URI, true);
				}
			});

		} catch (InterruptedException e) {
			Activator.log.error(RETRIEVE_EANNOTATION_ERROR, e);
		} catch (RollbackException e) {
			Activator.log.error(RETRIEVE_EANNOTATION_ERROR, e);
		}

		return profile.getEAnnotation(UMLUtil.UML2_UML_PACKAGE_2_0_NS_URI);

	}

	@Override
	public ILabelProvider getLabelProvider(String propertyPath) {
		try {
			return ServiceUtilsForEObject.getInstance().getService(LabelProviderService.class, profile).getLabelProvider();
		} catch (ServiceException ex) {
			return new LabelProvider();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.views.properties.modelelement.AbstractModelElement#getValueFactory(java.lang.String)
	 */
	@Override
	public ReferenceValueFactory getValueFactory(final String propertyPath) {
		ReferenceValueFactory valueFactory = null;
		if (DEFINITIONS.equals(propertyPath)) {
			valueFactory = new ProfileDefinitionValueFactory(profile, EcorePackage.eINSTANCE.getEAnnotation_Contents());
		} else {
			// Try with inherited implementation
			valueFactory = super.getValueFactory(propertyPath);
		}

		return valueFactory;
	}

	@Override
	public boolean isOrdered(String propertyPath) {
		return false;
	}

	@Override
	public boolean getDirectCreation(String propertyPath) {
		return true; // Cannot browse other definitions
	}

	@Override
	public boolean isEditable(String propertyPath) {
		return !EMFHelper.isReadOnly(profile);
	}
}
