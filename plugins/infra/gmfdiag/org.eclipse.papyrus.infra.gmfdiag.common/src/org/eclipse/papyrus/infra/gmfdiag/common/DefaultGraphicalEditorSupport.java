/*****************************************************************************
 * Copyright (c) 2013 CEA LIST.
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
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common;

import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.papyrus.infra.core.services.IService;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;

/**
 * This is the DefaultGraphicalEditorSupport type. Enjoy.
 */
public class DefaultGraphicalEditorSupport implements IGraphicalEditorSupport, IService {

	public DefaultGraphicalEditorSupport() {
		super();
	}

	@Override
	public void initialize(GraphicalEditor editor) {
		// pass
	}

	@Override
	public void init(ServicesRegistry servicesRegistry) {
		// pass
	}

	@Override
	public void startService() {
		// pass
	}

	@Override
	public void disposeService() {
		// pass
	}

}
