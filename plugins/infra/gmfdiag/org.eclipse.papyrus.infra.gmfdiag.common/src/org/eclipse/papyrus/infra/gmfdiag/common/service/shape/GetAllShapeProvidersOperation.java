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
package org.eclipse.papyrus.infra.gmfdiag.common.service.shape;

import java.util.List;

import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.core.service.IProvider;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;

/**
 * Operation to find all shape providers
 */
public class GetAllShapeProvidersOperation implements IOperation, IShapeProviderOperation {

	/** list of providers to complete */
	protected final List<IShapeProvider> providers;

	/**
	 * Creates a new GetAllAspectToolProvidersOperation.
	 */
	protected GetAllShapeProvidersOperation(List<IShapeProvider> providers) {
		this.providers = providers;
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public Object execute(IProvider provider) {
		if (provider instanceof IShapeProvider) {
			providers.add((IShapeProvider) provider);
		} else {
			Activator.log.warn("Trying to add a non-shape provider to the list of shape providers");
		}
		return providers;
	}
}
