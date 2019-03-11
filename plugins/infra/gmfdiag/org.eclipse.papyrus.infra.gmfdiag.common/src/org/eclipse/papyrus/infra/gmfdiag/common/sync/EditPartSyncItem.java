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

package org.eclipse.papyrus.infra.gmfdiag.common.sync;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.sync.SyncItem;
import org.eclipse.swt.widgets.Control;

/**
 * A specialized {@link SyncItem} that synchronizes objects with {@link EditPart}s in GMF diagrams.
 */
public class EditPartSyncItem<M, T extends EditPart> extends SyncItem<M, T> {
	private Reference<M> lastKnownModel;

	public EditPartSyncItem(T backend) {
		super(backend);

		// Initialize the last-known-model cache
		getModel();
	}

	@SuppressWarnings("unchecked")
	@Override
	public M getModel() {
		T backend = getBackend();
		View view = (backend == null) ? null : (View) backend.getModel();
		M result = (view == null) ? null : (M) view.getElement();

		if (result == null) {
			if (lastKnownModel != null) {
				result = lastKnownModel.get();
			}
		} else if ((lastKnownModel == null) || (lastKnownModel.get() != result)) {
			// Refresh the cache
			lastKnownModel = new WeakReference<M>(result);
		}

		return result;
	}

	@Override
	public boolean isActive() {
		boolean result = super.isActive();

		if (result) {
			EditPart editPart = getBackend();

			// Detached edit-parts do not synchronize
			EditPartViewer viewer = (editPart == null) ? null : editPart.getViewer();
			Control control = (viewer == null) ? null : viewer.getControl();
			result = (control != null) && !control.isDisposed();
		}

		return result;
	}
}
