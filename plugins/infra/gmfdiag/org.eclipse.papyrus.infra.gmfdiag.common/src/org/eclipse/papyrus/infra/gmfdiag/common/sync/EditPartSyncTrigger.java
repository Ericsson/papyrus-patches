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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.RollbackException;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.core.utils.AdapterUtils;
import org.eclipse.papyrus.infra.emf.gmf.util.GMFUnsafe;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.sync.SyncBucket;
import org.eclipse.papyrus.infra.sync.SyncRegistry;
import org.eclipse.papyrus.infra.sync.service.AbstractSyncTrigger;
import org.eclipse.papyrus.infra.sync.service.ISyncAction;
import org.eclipse.papyrus.infra.sync.service.ISyncService;

/**
 * Trigger to engage synchronization on edit parts that have the master/slave synchronization styles set.
 */
public abstract class EditPartSyncTrigger<M extends EObject, T extends EditPart, X> extends AbstractSyncTrigger {

	private final Class<? extends SyncRegistry<M, T, X>> registryType;

	public EditPartSyncTrigger(Class<? extends SyncRegistry<M, T, X>> registryType) {
		super();

		this.registryType = registryType;
	}

	@Override
	public ISyncAction trigger(ISyncService syncService, Object object) {
		ISyncAction result = null;

		if (object instanceof EditPart) {
			EditPart editPart = (EditPart) object;
			Object model = editPart.getModel();
			if (model instanceof View) {
				SyncKind kind = SyncStyles.getSyncKind((View) model);
				result = trigger(kind);
			}
		}

		return result;
	}

	protected ISyncAction trigger(final SyncKind syncKind) {
		return new ISyncAction() {

			@Override
			public IStatus perform(final ISyncService syncService, Object object) {
				final IStatus[] result = { Status.OK_STATUS };
				final EditPart editPart = (EditPart) object;
				final TransactionalEditingDomain domain = TransactionUtil.getEditingDomain((View) editPart.getModel());

				try {
					GMFUnsafe.write(domain, new Runnable() {

						@Override
						public void run() {
							result[0] = doTrigger(syncService, editPart, syncKind);
						}
					});
				} catch (InterruptedException e) {
					result[0] = new Status(IStatus.ERROR, Activator.ID, "Synchronization trigger was interrupted", e);
				} catch (RollbackException e) {
					result[0] = e.getStatus();
				}

				return result[0];
			}
		};
	}

	protected IStatus doTrigger(ISyncService syncService, EditPart editPart, SyncKind syncKind) {
		IStatus result = Status.OK_STATUS;

		SyncRegistry<M, T, X> registry = null;
		M model = null;
		T backend = null;

		try {
			registry = syncService.getSyncRegistry(registryType);
			model = AdapterUtils.adapt(editPart, registry.getModelType(), null);
			backend = registry.getBackendType().cast(editPart);
		} catch (Exception e) {
			result = new Status(IStatus.ERROR, Activator.ID, "Failed to access synchronization registry", e);
		}

		if ((registry != null) && (model != null)) {
			switch (syncKind) {
			case MASTER:
				SyncBucket<M, T, X> bucket = registry.getBucket(model);
				if (bucket == null) {
					bucket = createSyncBucket(model, backend);
					registry.register(bucket);
				}
				break;
			case SLAVE:
				registry.synchronize(backend);
				break;
			default:
				result = new Status(IStatus.ERROR, Activator.ID, "Unsupported synchronization kind: " + syncKind, null);
				break;
			}
		}

		return result;
	}

	protected abstract SyncBucket<M, T, X> createSyncBucket(M model, T editPart);
}
