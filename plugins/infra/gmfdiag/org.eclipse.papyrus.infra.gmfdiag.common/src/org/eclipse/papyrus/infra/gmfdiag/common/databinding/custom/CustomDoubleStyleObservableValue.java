/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and Others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	Fanch BONNABESSE (ALL4TEC) fanch.bonnabesse@all4tec.net - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.databinding.custom;

import java.math.BigDecimal;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;

/**
 *
 * Implementation for an ObservableValue associated to a GMF custom style (DoubleValueStyle).
 *
 * @since 3.0
 *
 */
public class CustomDoubleStyleObservableValue extends AbstractCustomStyleObservableValue {

	/**
	 * Default double value.
	 */
	private final static double DEFAULT_VALUE = 0.0;

	/**
	 *
	 * Constructor.
	 *
	 * @param source
	 *            the view source
	 * @param domain
	 *            the editing domain
	 * @param styleName
	 *            the style name
	 */
	public CustomDoubleStyleObservableValue(final View source, final EditingDomain domain, final String styleName) {
		super(source, domain, styleName, NotationPackage.eINSTANCE.getDoubleValueStyle(), NotationPackage.eINSTANCE.getDoubleValueStyle_DoubleValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValueType() {
		return EcorePackage.eINSTANCE.getEFloat();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object getDefaultValue() {
		return DEFAULT_VALUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object doGetValue() {
		return super.doGetValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Command getCommand(final Object value) {
		if (value instanceof Double) {
			return super.getCommand(value);
		} else if (value instanceof Float) {
			BigDecimal number = new BigDecimal((Float) value);
			return super.getCommand(number.doubleValue());
		}
		throw new IllegalArgumentException(String.format("The value %s is not a valid Double Value", value == null ? "null" : value.toString())); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
