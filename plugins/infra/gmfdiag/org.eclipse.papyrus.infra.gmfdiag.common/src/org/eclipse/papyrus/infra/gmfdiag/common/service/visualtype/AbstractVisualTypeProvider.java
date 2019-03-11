/*****************************************************************************
 * Copyright (c) 2015 Christian W. Damus and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.service.visualtype;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

/**
 * Abstract superclass of all {@link IVisualTypeProvider} extensions.
 */
public abstract class AbstractVisualTypeProvider extends AbstractProvider implements IVisualTypeProvider {

	private String diagramType;

	public AbstractVisualTypeProvider() {
		super();
	}

	@Override
	public boolean provides(IOperation operation) {
		return (operation instanceof IVisualTypeOperation)
				&& Objects.equal(((IVisualTypeOperation) operation).getDiagramType(), diagramType);
	}

	void setConfiguration(IConfigurationElement config) {
		diagramType = config.getAttribute("diagramType"); //$NON-NLS-1$
		if (Strings.isNullOrEmpty(diagramType)) {
			throw new IllegalArgumentException("No diagramType attribute specified by visual type provider extension"); //$NON-NLS-1$
		}
	}
}
