/*****************************************************************************
 * Copyright (c) 2017 CEA LIST, ALL4TEC and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and implementation
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.service.palette;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.papyrus.infra.architecture.listeners.IArchitectureDescriptionListener;

/**
 * {@link IArchitectureDescriptionListener} which inform {@link PapyrusPaletteService} that architecture have changed.
 */
public class ArchitectureForPaletteServiceListener implements IArchitectureDescriptionListener {

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.papyrus.infra.architecture.listeners.IArchitectureDescriptionListener#architectureContextChanged(org.eclipse.emf.common.notify.Notification)
	 */
	@Override
	public void architectureContextChanged(final Notification notification) {
		PapyrusPaletteService.getInstance().architectureChanged();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.papyrus.infra.architecture.listeners.IArchitectureDescriptionListener#architectureViewpointsChanged(org.eclipse.emf.common.notify.Notification)
	 */
	@Override
	public void architectureViewpointsChanged(final Notification notification) {
		PapyrusPaletteService.getInstance().architectureChanged();
	}

}
