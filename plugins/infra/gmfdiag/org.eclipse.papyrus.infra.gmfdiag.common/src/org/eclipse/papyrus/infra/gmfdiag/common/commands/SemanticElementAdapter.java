/*****************************************************************************
 * Copyright (c) 2011 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *		CEA LIST - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.commands;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.emf.core.util.PackageUtil;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.IHintedType;
import org.eclipse.gmf.runtime.notation.View;

/**
 * An adapter for {@link IElementType}, {@link EObject} or {@link View}.
 */
public class SemanticElementAdapter extends SemanticAdapter implements ISemanticHintAdapter {

	/** The elementType. */
	private Object elementType;

	/** Constructor from EObject */
	public SemanticElementAdapter(EObject element) {
		super(element, null);
		this.elementType = null;
	}

	/** Constructor from IElementType */
	public SemanticElementAdapter(IElementType elementType) {
		super(null, null);
		this.elementType = elementType;
	}

	/** Constructor from EObject and IElementType */
	public SemanticElementAdapter(EObject element, IElementType elementType) {
		super(element, null);
		this.elementType = elementType;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IHintedType.class)) {
			if (elementType instanceof IHintedType) {
				return elementType;
			}
		}

		if (adapter.equals(IElementType.class)) {
			return elementType;
		}
		return super.getAdapter(adapter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getProxyClassID() {
		if (elementType != null) {
			return PackageUtil.getID(((IElementType) elementType).getEClass());
		}
		return super.getProxyClassID();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSemanticHint() {
		if (elementType instanceof IHintedType) {
			return ((IHintedType) elementType).getSemanticHint();
		}
		return null;
	}

}
