/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Fanch BONNABESSE (ALL4TEC) fanch.bonnabesse@all4tec.net - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.databinding.custom;

import java.text.NumberFormat;
import java.text.ParseException;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.diagram.ui.internal.properties.WorkspaceViewerProperties;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.UnitsConverterUtils;

/**
 *
 * Implementation for an ObservableValue corresponding to the property "rulergrid.rulerunit".
 * This ObservableValue is associated to a IntValueStyle and a IPreferenceStore.
 * @since 3.0
 *
 */
@SuppressWarnings("restriction")
public class RulersUnitStyleObservableValue extends CustomIntStyleWithStoreObservableValue {

	/**
	 *
	 * Constructor.
	 *
	 * @param source
	 *            The view source
	 * @param domain
	 *            The editing domain
	 * @param preferenceStore
	 *            The preference store
	 */
	public RulersUnitStyleObservableValue(final View source, final EditingDomain domain, final IPreferenceStore preferenceStore) {
		super(source, domain, WorkspaceViewerProperties.RULERUNIT, preferenceStore);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doSetValue(final Object value) {
		if (value instanceof Integer) {
			final Integer oldValue = (Integer) doGetValue();
			final Double oldGridSpacing = this.preferenceStore.getDouble(WorkspaceViewerProperties.GRIDSPACING);
			final String newValue = UnitsConverterUtils.convertUnits(oldValue, (Integer) value, NumberFormat.getInstance().format(oldGridSpacing));

			Number number = null;
			try {
				number = NumberFormat.getInstance().parse(newValue);
			} catch (ParseException e) {
				Activator.log.error(e);
				number = null;
				return;
			}

			IObservableValue observable = new CustomDoubleStyleWithStoreObservableValue(this.source, this.domain, WorkspaceViewerProperties.GRIDSPACING, this.preferenceStore);

			final double newGridSpacing = number.doubleValue();
			observable.setValue(newGridSpacing);
		}

		super.doSetValue(value);
	}

}
