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

package org.eclipse.papyrus.infra.gmfdiag.common.internal.sync;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.papyrus.infra.core.services.IService;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.core.utils.IExecutorPolicy;
import org.eclipse.papyrus.infra.core.utils.TransactionHelper;
import org.eclipse.papyrus.infra.sync.service.ISyncService;
import org.eclipse.papyrus.infra.ui.util.UIUtil;
import org.eclipse.swt.widgets.Display;

/**
 * A service that provides UI-specific initialization of the {@link ISyncService}.
 * In particular, it installs an asynchronous executor that runs operations on the UI thread.
 */
public class UISyncServiceInitializer implements IService {

	private ServicesRegistry registry;

	public UISyncServiceInitializer() {
		super();
	}

	@Override
	public void init(ServicesRegistry servicesRegistry) throws ServiceException {
		this.registry = servicesRegistry;
	}

	@Override
	public void startService() throws ServiceException {
		ISyncService syncService = registry.getService(ISyncService.class);

		syncService.setAsyncExecutor(new UISyncServiceExecutorService(syncService.getEditingDomain()));
	}

	@Override
	public void disposeService() throws ServiceException {
		// Nothing to dispose
	}

	private static class UISyncServiceExecutorService extends AbstractExecutorService {

		private final ExecutorService uiExecutor;
		private final Executor transactionExecutor;

		UISyncServiceExecutorService(TransactionalEditingDomain domain) {
			super();

			this.uiExecutor = UIUtil.createUIExecutor(Display.getDefault());
			this.transactionExecutor = TransactionHelper.createTransactionExecutor(domain, uiExecutor, new IExecutorPolicy() {
				@Override
				public Ranking rank(Runnable task, Executor executor) {
					if (executor == uiExecutor) {
						// Always OK to fall back to the UI-thread executor
						return Ranking.ACCEPTABLE;
					} else {
						// The case of the transaction executor
						return (Display.getCurrent() == null) ? Ranking.DEPRECATED : Ranking.PREFERRED;
					}
				}
			});
		}

		@Override
		public void execute(Runnable command) {
			transactionExecutor.execute(command);
		}

		@Override
		public void shutdown() {
			// Only the UI executor is an executor service
			uiExecutor.shutdown();
		}

		@Override
		public List<Runnable> shutdownNow() {
			// Only the UI executor is an executor service
			return uiExecutor.shutdownNow();
		}

		@Override
		public boolean isShutdown() {
			// Only the UI executor is an executor service
			return uiExecutor.isShutdown();
		}

		@Override
		public boolean isTerminated() {
			// Only the UI executor is an executor service
			return uiExecutor.isTerminated();
		}

		@Override
		public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
			// Only the UI executor is an executor service
			return uiExecutor.awaitTermination(timeout, unit);
		}

	}
}
