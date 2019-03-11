/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	Remi Schnekenburger (CEA) remi.chnekenburger@cea.fr - Initial API and implementation
 *  Gabriel Pascual (ALL4TEC) gabriel.pascual@all4tec.net - Bug 464625
 *****************************************************************************/


package org.eclipse.papyrus.infra.gmfdiag.common.parsers;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.core.command.UnexecutableCommand;
import org.eclipse.gmf.runtime.emf.type.core.commands.SetValueCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.SetRequest;
import org.eclipse.papyrus.infra.gmfdiag.tooling.runtime.parsers.AbstractAttributeParser;
import org.eclipse.papyrus.infra.services.edit.service.ElementEditServiceUtils;
import org.eclipse.papyrus.infra.services.edit.service.IElementEditService;

/**
 * Extended {@link AbstractAttributeParser} to use {@link IElementEditService}.
 *
 */
public abstract class AbstractElementTypeBasedAttributeParser extends AbstractAttributeParser {

	/**
	 * Instantiates a new abstract element type based attribute parser.
	 *
	 * @param features
	 *            the features
	 * @param editableFeatures
	 *            the editable features
	 */
	public AbstractElementTypeBasedAttributeParser(final EAttribute[] features, final EAttribute[] editableFeatures) {
		super(features, editableFeatures);
	}

	/**
	 * Instantiates a new abstract element type based attribute parser.
	 *
	 * @param features
	 *            the features
	 */
	public AbstractElementTypeBasedAttributeParser(final EAttribute[] features) {
		super(features);
	}


	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.gmf.tooling.runtime.parsers.AbstractFeatureParser#getModificationCommand(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EStructuralFeature, java.lang.Object)
	 *
	 * @param element
	 * @param feature
	 * @param value
	 * @return
	 */
	@Override
	protected ICommand getModificationCommand(final EObject element, final EStructuralFeature feature, final Object value) {
		ICommand modificationCommand;
		// Validate the value
		Object validValue = getValidNewValue(feature, value);
		if (validValue instanceof InvalidValue) {
			modificationCommand = UnexecutableCommand.INSTANCE;
		} else {
			SetRequest request = new SetRequest(element, feature, validValue);
			IElementEditService serviceEdit = ElementEditServiceUtils.getCommandProvider(element);
			if(serviceEdit != null) {
				// Ask to Edit Service for the command
				modificationCommand = serviceEdit.getEditCommand(request);
			}else {
				// Return the standard command
				modificationCommand= new SetValueCommand(request);
			}
		}
		return modificationCommand;
	}


}
