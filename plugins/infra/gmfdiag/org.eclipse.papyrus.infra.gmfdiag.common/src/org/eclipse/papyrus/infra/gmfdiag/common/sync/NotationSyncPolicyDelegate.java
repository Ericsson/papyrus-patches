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

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.sync.EMFDispatch;
import org.eclipse.papyrus.infra.sync.SyncItem;
import org.eclipse.papyrus.infra.sync.policy.SyncPolicyDelegate;
import org.eclipse.papyrus.infra.tools.util.TypeUtils;

/**
 * A synchronization policy delegate that looks for feature exclusions in the notation style dedicated to that purpose.
 * 
 * @see SyncStyles#isSynchronized(View, String)
 */
public abstract class NotationSyncPolicyDelegate<M extends EObject, T extends EditPart> extends SyncPolicyDelegate<M, T> {

	private final String feature;

	/**
	 * Initializes me with the name of the feature for which I determine synchronization.
	 *
	 * @param feature
	 *            my feature
	 */
	public NotationSyncPolicyDelegate(String feature) {
		super();

		this.feature = feature;
	}

	@Override
	public boolean shouldSynchronize(SyncItem<M, T> from, SyncItem<M, T> to) {
		T backend = to.getBackend();
		View view = (backend == null) ? null : TypeUtils.as(backend.getModel(), View.class);

		return (view != null) && SyncStyles.isSynchronized(view, feature);
	}

	/**
	 * Obtains a command that will stop synchronizing the given edit-part.
	 * 
	 * @param editPart
	 *            an edit-part that should stop having my feature synchronized
	 * @return a command to override synchronization of my feature for the edit-part, or {@code null}
	 *         if the edit-part already has synchronization overridden
	 */
	protected Command stopSynchronzing(T editPart) {
		Command result = null;
		View view = (editPart == null) ? null : TypeUtils.as(editPart.getModel(), View.class);

		if (view != null) {
			result = SyncStyles.createSetExcludedCommand(view, feature, true);
			if (!result.canExecute()) {
				// If it can't be executed then that's because synchronizing is already stopped
				result = null;
			}
		}

		return result;
	}

	/**
	 * Handles the response to a synchronization override event that was received by a {@code receiver}
	 * dispatcher for the given {@code syncItem}.
	 * 
	 * @param receiver
	 *            a dispatcher that received an event on the feature that is synchronized to the {@code syncItem}
	 * @param syncItem
	 *            the sync-item on which the synchronization override occurred
	 */
	protected void overrideOccurred(EMFDispatch receiver, SyncItem<M, T> syncItem) {
		Command reaction = stopSynchronzing(syncItem.getBackend());
		if (reaction != null) {
			receiver.react(reaction);
		}

		// And we no longer need to listen to it
		unobserve(syncItem);
	}
}
