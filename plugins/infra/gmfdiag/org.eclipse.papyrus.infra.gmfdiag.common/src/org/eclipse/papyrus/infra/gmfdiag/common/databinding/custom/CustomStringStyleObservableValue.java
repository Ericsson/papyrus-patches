/*****************************************************************************
 * Copyright (c) 2012 CEA LIST.
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
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.databinding.custom;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;


public class CustomStringStyleObservableValue extends AbstractCustomStyleObservableValue {

	public CustomStringStyleObservableValue(View source, EditingDomain domain, String styleName) {
		super(source, domain, styleName, NotationPackage.eINSTANCE.getStringValueStyle(), NotationPackage.eINSTANCE.getStringValueStyle_StringValue());
	}

	@Override
	public Object getValueType() {
		return String.class;
	}

	/**
	 * Gets the default value when this style is not set.
	 * Subclasses may override
	 *
	 * @return The default value for this StringStyleValue
	 */
	@Override
	protected String getDefaultValue() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public Command getCommand(Object value) {
		if (value instanceof String) {
			return super.getCommand(value);
		}
		throw new IllegalArgumentException(String.format("The value %s is not a valid String Value", value == null ? "null" : value.toString())); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
