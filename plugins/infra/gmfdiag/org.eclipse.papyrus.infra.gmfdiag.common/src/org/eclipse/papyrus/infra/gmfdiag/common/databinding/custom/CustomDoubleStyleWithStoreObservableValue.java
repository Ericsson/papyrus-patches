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

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 *
 * Implementation for an ObservableValue associated to a DoubleValueStyle and a IPreferenceStore.
 * @since 3.0
 *
 */
public class CustomDoubleStyleWithStoreObservableValue extends CustomDoubleStyleObservableValue {

	/**
	 * The IPreferenceStore corresponding to the EditPart.
	 */
	protected IPreferenceStore preferenceStore;
	/**
	 * The listener on the preference store used to force refresh.
	 */
	protected IPropertyChangeListener listener = new IPropertyChangeListener() {

		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (styleName.equals(event.getProperty())) {
				ValueDiff diff = new ValueDiff() {

					@Override
					public Object getOldValue() {
						return event.getOldValue();
					}

					@Override
					public Object getNewValue() {
						return event.getNewValue();
					}
				};
				fireValueChange(diff);
			}
		}
	};

	/**
	 *
	 * Constructor.
	 *
	 * @param source
	 *            The view source
	 * @param domain
	 *            The editing domain
	 * @param styleName
	 *            The style name to of the property
	 * @param preferenceStore
	 *            The preference store
	 */
	public CustomDoubleStyleWithStoreObservableValue(final View source, final EditingDomain domain, final String styleName, final IPreferenceStore preferenceStore) {
		super(source, domain, styleName);
		this.preferenceStore = preferenceStore;
		this.preferenceStore.addPropertyChangeListener(this.listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleChange(final ChangeEvent event) {
		super.handleChange(event);
		addToPreferenceStore(doGetValue());
	}

	/**
	 * Set the new value in the corresponding IPreferenceStore.
	 *
	 * @param value
	 *            The new value
	 */
	public void addToPreferenceStore(final Object value) {
		if (value instanceof Float) {
			final double oldValue = (Double) doGetValue();
			this.preferenceStore.setValue(this.styleName, ((Float) value).doubleValue());
			final ValueDiff diff = new ValueDiff() {

				@Override
				public Object getOldValue() {
					return oldValue;
				}

				@Override
				public Object getNewValue() {
					return ((Float) value).doubleValue();
				}
			};
			fireValueChange(diff);

		} else if (value instanceof Double) {
			final double oldValue = (Double) doGetValue();
			this.preferenceStore.setValue(this.styleName, ((Double) value).doubleValue());
			final ValueDiff diff = new ValueDiff() {

				@Override
				public Object getOldValue() {
					return oldValue;
				}

				@Override
				public Object getNewValue() {
					return ((Double) value).doubleValue();
				}
			};
			fireValueChange(diff);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		this.preferenceStore.removePropertyChangeListener(this.listener);
		super.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object getDefaultValue() {
		return this.preferenceStore.getDouble(this.styleName);
	}

}
