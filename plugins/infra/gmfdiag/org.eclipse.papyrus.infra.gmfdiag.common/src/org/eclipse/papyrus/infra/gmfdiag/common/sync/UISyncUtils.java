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

import org.eclipse.papyrus.infra.sync.ISyncObject;
import org.eclipse.papyrus.infra.sync.service.ISyncService;
import org.eclipse.papyrus.infra.sync.service.SyncServiceRunnable;
import org.eclipse.swt.widgets.Display;

import com.google.common.util.concurrent.CheckedFuture;

/**
 * Sync framework utilities for a UI context.
 */
public class UISyncUtils {

	/**
	 * Not instantiable by clients.
	 */
	private UISyncUtils() {
		super();
	}

	/**
	 * Runs an operation in the context of the {@link ISyncService} that owns a {@link syncObject},
	 * asynchronously on the UI thread.
	 * The {@link SyncServiceRunnable.Safe Safe} variant does not throw a checked exception.
	 * 
	 * @param syncObject
	 *            a sync object
	 * @param operation
	 *            a sync-service operation
	 * @return the future result of the {@code operation}
	 * 
	 * @see SyncServiceRunnable.Safe
	 */
	public static <V, X extends Exception> CheckedFuture<V, X> asyncExec(final ISyncObject syncObject, final SyncServiceRunnable<V, X> operation) {
		return syncObject.runAsync(operation);
	}

	/**
	 * Runs an operation in the context of the {@link ISyncService} that owns a {@link syncObject},
	 * synchronously on the UI thread.
	 * The {@link SyncServiceRunnable.Safe Safe} variant does not throw a checked exception.
	 * 
	 * @param syncObject
	 *            a sync object
	 * @param operation
	 *            a sync-service operation
	 * @return the result of the {@code operation}, if it completes without throwing
	 * 
	 * @throws X
	 *             a checked exception that the {@code operation} may optionally declare
	 * 
	 * @see SyncServiceRunnable.Safe
	 */
	public static <V, X extends Exception> V syncExec(final ISyncObject syncObject, final SyncServiceRunnable<V, X> operation) throws X {
		CheckedFuture<V, X> result = operation.asFuture(syncObject);

		Display.getDefault().syncExec((Runnable) result);

		return result.checkedGet();
	}
}
