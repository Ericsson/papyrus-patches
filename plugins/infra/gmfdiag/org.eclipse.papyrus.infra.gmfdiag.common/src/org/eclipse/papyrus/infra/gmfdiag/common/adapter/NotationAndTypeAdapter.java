/*****************************************************************************
 * Copyright (c) 2016 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.adapter;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.View;

/**
 * @author Camille Letavernier
 *
 */
public class NotationAndTypeAdapter implements IAdaptable {

	private final IElementType type;

	private final View view;

	private final String visualID;

	private final EObject semanticElement;

	/**
	 * Constructor.
	 *
	 * @param element
	 * @param view
	 */
	public NotationAndTypeAdapter(IElementType type, EObject element, View view, String visualID) {
		this.type = type;
		this.view = view;
		this.visualID = visualID;
		this.semanticElement = element;
	}

	/**
	 * Constructor.
	 *
	 * @param element
	 * @param view
	 */
	public NotationAndTypeAdapter(EObject element, View view, String visualID) {
		this(null, element, view, visualID);
	}

	/**
	 * Constructor.
	 *
	 * @param element
	 * @param view
	 */
	public NotationAndTypeAdapter(EObject element, View view) {
		this(null, element, view, view == null ? null : view.getType());
	}

	/**
	 *
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 *
	 * @param adapter
	 * @return
	 */
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == View.class) {
			return adapter.cast(view);
		} else if (adapter == IElementType.class) {
			return adapter.cast(type);
		} else if (adapter == EObject.class) {
			return adapter.cast(semanticElement);
		} else if (adapter == String.class) {
			return adapter.cast(visualID);
		}

		return null;
	}


}
