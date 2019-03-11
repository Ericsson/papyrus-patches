/*****************************************************************************
 * Copyright (c) 2010, 2017 CEA LIST.
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
 *  Nicolas FAUVERGUE (ALL4TEC) nicolas.fauvergue@all4tec.net - bug 453445, 515650
 *  Fanch BONNABESSE (ALL4TEC) fanch.bonnabesse@all4tec.net - Bug 502533
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.properties.modelelement;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.papyrus.infra.emf.gmf.command.GMFtoEMFCommandWrapper;
import org.eclipse.papyrus.infra.properties.ui.modelelement.EMFModelElement;
import org.eclipse.papyrus.infra.properties.ui.modelelement.EObjectStructuredValueFactory;
import org.eclipse.papyrus.infra.properties.ui.modelelement.ILabeledModelElement;
import org.eclipse.papyrus.infra.widgets.creation.ReferenceValueFactory;
import org.eclipse.papyrus.infra.widgets.providers.IStaticContentProvider;
import org.eclipse.papyrus.uml.properties.Activator;
import org.eclipse.papyrus.uml.properties.datatype.DataTypeProvider;
import org.eclipse.papyrus.uml.properties.datatype.StructuredDataTypeObservableValue;
import org.eclipse.papyrus.uml.tools.databinding.PapyrusObservableList;
import org.eclipse.papyrus.uml.tools.databinding.PapyrusObservableValue;
import org.eclipse.papyrus.uml.tools.providers.UMLContentProvider;
import org.eclipse.papyrus.uml.tools.utils.DataTypeUtil;
import org.eclipse.papyrus.uml.tools.utils.StereotypeUtil;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Stereotype;

/**
 * A Model Element for manipulating Stereotype properties
 *
 * @author Camille Letavernier
 */
public class StereotypeModelElement extends EMFModelElement implements ILabeledModelElement {

	/**
	 * The stereotype handled by this ModelElement
	 */
	protected Stereotype stereotype;

	/**
	 * Constructor.
	 *
	 * @param stereotypeApplication
	 *            The StereotypeApplication being edited
	 * @param stereotype
	 *            The Stereotype element
	 * @param domain
	 *            The Editing domain on which the commands will be called
	 *
	 */
	public StereotypeModelElement(EObject stereotypeApplication, Stereotype stereotype, EditingDomain domain) {
		super(stereotypeApplication, domain);
		this.stereotype = stereotype;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IObservable doGetObservable(String propertyPath) {
		FeaturePath featurePath = getFeaturePath(propertyPath);
		EStructuralFeature feature = getFeature(featurePath);
		if (feature == null) {
			return super.doGetObservable(propertyPath);
		}

		if (feature.getEType() instanceof EDataType && !(feature.getEType() instanceof EEnum)) {
			if (feature.getUpperBound() == 1) {
				// Single-valued DataType
				if (DataTypeProvider.instance.canHandle((EDataType) feature.getEType())) {
					return new StructuredDataTypeObservableValue(source, feature, domain, (EDataType) feature.getEType());
				}
				// TODO : Multi-valued DataTypes
			}
		}

		if (feature.getEType() instanceof EClass) {
			if (DataTypeUtil.isDataTypeDefinition((EClass) feature.getEType(), getSource(featurePath))) {
				return new org.eclipse.papyrus.infra.services.edit.ui.databinding.PapyrusObservableValue(getSource(featurePath), feature, domain, GMFtoEMFCommandWrapper::wrap);
			}
		}


		if (feature.getUpperBound() != 1) {
			return new PapyrusObservableList(EMFProperties.list(featurePath).observe(source), domain, getSource(featurePath), feature);
		}

		return new PapyrusObservableValue(getSource(featurePath), feature, domain);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FeaturePath getFeaturePath(String propertyPath) {
		String[] featureNames = propertyPath.split("\\."); //$NON-NLS-1$
		EStructuralFeature[] features = new EStructuralFeature[featureNames.length];

		int i = 0;
		EClass currentClass = source.eClass();
		for (String featureName : featureNames) {
			// Bug 453445 : Manage the special character for the property path
			featureName = UML2Util.getValidJavaIdentifier(featureName);
			EStructuralFeature feature = currentClass.getEStructuralFeature(featureName);
			features[i++] = feature;
			if (i < featureNames.length) {
				if (feature instanceof EReference) {
					EReference reference = (EReference) feature;
					EClassifier type = reference.getEType();
					if (type instanceof EClass) {
						currentClass = (EClass) type;
						continue;
					}
				}

				final StringBuilder warningMessage = new StringBuilder("Cannot find feature path "); //$NON-NLS-1$
				warningMessage.append(propertyPath);
				warningMessage.append(" for EClass "); //$NON-NLS-1$
				warningMessage.append(source.eClass());
				Activator.log.warn(warningMessage.toString());
				return null;
			}
		}

		return FeaturePath.fromList(features);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IStaticContentProvider getContentProvider(String propertyPath) {
		EStructuralFeature feature = getFeature(propertyPath);

		if (feature == null) {
			return super.getContentProvider(propertyPath);
		}

		return new UMLContentProvider(source, feature, stereotype);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.papyrus.infra.properties.ui.modelelement.EMFModelElement#getValueFactory(java.lang.String)
	 */
	@Override
	public ReferenceValueFactory getValueFactory(final String propertyPath) {
		EStructuralFeature feature = getFeature(propertyPath);
		if (feature != null) {
			if (feature instanceof EReference) {
				EReference reference = (EReference) feature;
				if (reference.isContainment()) {
					EClassifier featureType = feature.getEType();
					if (featureType instanceof EClass && DataTypeUtil.isDataTypeDefinition((EClass) featureType, getSource())) {
						return new EObjectStructuredValueFactory(reference);
					}
				}
			}
		}

		return super.getValueFactory(propertyPath);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.papyrus.infra.properties.ui.modelelement.ILabeledModelElement#getLabel(java.lang.String)
	 */
	@Override
	public String getLabel(final String propertyPath) {
		String result = null;

		final FeaturePath featurePath = getFeaturePath(propertyPath);
		final EStructuralFeature feature = getFeature(featurePath);

		if (null != feature) {
			final EObject property = StereotypeUtil.getPropertyByName(stereotype, feature.getName());
			if (property instanceof NamedElement) {
				final NamedElement namedElement = (NamedElement) property;
				final String name = namedElement.getName();
				final String label = namedElement.getLabel();

				if (!label.equals(name)) {
					result = label;
				}
			}
		}

		return result;
	}
}
