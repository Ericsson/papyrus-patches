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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.papyrus.infra.sync.EObjectMasterSlaveSyncBucket;
import org.eclipse.papyrus.infra.sync.SyncItem;

/**
 * A synchronization bucket for synchronization of the {@link EditPart}s visualizing model elements in GMF diagrams.
 */
public class EditPartMasterSlaveSyncBucket<M extends EObject, T extends EditPart> extends EObjectMasterSlaveSyncBucket<M, T, Notification> {

	public EditPartMasterSlaveSyncBucket(M model, T master) {
		super(model, master);
	}

	@Override
	protected SyncItem<M, T> encapsulate(T element) {
		return new EditPartSyncItem<M, T>(element);
	}

}
