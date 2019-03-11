/*****************************************************************************
 * Copyright (c) 2016 Christian W. Damus and others.
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

package org.eclipse.papyrus.infra.gmfdiag.common.spi;

import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.papyrus.infra.tools.util.CompositeServiceTracker;
import org.osgi.framework.BundleContext;

/**
 * A delegating deletion helper that consults registered deletion-helper services.
 */
public class GraphicalDeletionHelperService implements IGraphicalDeletionHelper {

	private final CompositeServiceTracker<IGraphicalDeletionHelper> tracker;

	public GraphicalDeletionHelperService(BundleContext context) {
		super();

		tracker = new CompositeServiceTracker<>(context,
				IGraphicalDeletionHelper.class,
				IGraphicalDeletionHelper.DEFAULT,
				IGraphicalDeletionHelper::compose);
		tracker.open();
	}

	public void dispose() {
		tracker.close();
	}

	@Override
	public boolean canDelete(IGraphicalEditPart editPart) {
		return tracker.getService().canDelete(editPart);
	}

}
