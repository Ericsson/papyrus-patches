/*
 * Copyright (c) 2014 CEA and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus (CEA) - Initial API and implementation
 *
 */
package org.eclipse.papyrus.infra.gmfdiag.common.adapter;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.papyrus.infra.ui.editor.reload.IReloadContextProvider;

/**
 * An adapter factory for the outline page contributed by nested diagram editors.
 */
public class DiagramOutlineAdapterFactory implements IAdapterFactory {

	private static final Class<?>[] ADAPTERS = { IReloadContextProvider.class };

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return ADAPTERS;
	}

	@Override
	public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
		if (adapterType == IReloadContextProvider.class) {
			if (DiagramOutlineReloadContextProvider.isDiagramOutline(adaptableObject)) {
				return new DiagramOutlineReloadContextProvider(adaptableObject);
			}
		}
		return null;
	}

}
