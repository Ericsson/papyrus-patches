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

package org.eclipse.papyrus.infra.gmfdiag.internal.common.model;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.ResourceSetListener;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.papyrus.infra.core.resource.EMFLogicalModel;
import org.eclipse.papyrus.infra.core.resource.IModel;
import org.eclipse.papyrus.infra.core.resource.IModelSnippet;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.resource.ResourceAdapter;
import org.eclipse.papyrus.infra.core.sashwindows.di.service.IPageManager;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.emf.gmf.util.GMFUnsafe;
import org.eclipse.papyrus.infra.emf.utils.ServiceUtilsForResourceSet;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.ui.util.TransactionUIHelper;

/**
 * A snippet on the notation {@link IModel} that ensures
 * closure of editor pages when notation views (diagrams, tables)
 * are removed by sneaky means (such as undo).
 */
public class NotationPageSnippet implements IModelSnippet {

	private EMFLogicalModel model;
	private TransactionalEditingDomain domain;
	private ResourceSetListener listener;
	private IPageManager pageManager;

	/**
	 * Initializes me.
	 */
	public NotationPageSnippet() {
		super();
	}

	/**
	 * If the {@link startingModel} has a resource, attach
	 * a listener to the editing domain that cleans up editor pages
	 * for notations that are deleted.
	 */
	@Override
	public void start(IModel startingModel) {
		if (startingModel instanceof EMFLogicalModel) {
			model = (EMFLogicalModel) startingModel;
			Resource resource = model.getResource();
			if (resource != null) {
				// Of course it's a model-set. I am a model snippet
				ModelSet modelSet = (ModelSet) resource.getResourceSet();
				domain = modelSet.getTransactionalEditingDomain();
				if (domain != null) {
					listener = createNotationDeletionListener();
					domain.addResourceSetListener(listener);
				}
			}
		}
	}

	@Override
	public void dispose(IModel stoppingModel) {
		if (stoppingModel == model) {
			try {
				if ((domain != null) && (listener != null)) {
					domain.removeResourceSetListener(listener);
				}
			} finally {
				pageManager = null;
				listener = null;
				domain = null;
				model = null;
			}
		}
	}

	private ResourceSetListener createNotationDeletionListener() {
		return new ResourceAdapter.Transactional() {
			@Override
			protected void handleRootRemoved(Resource resource, EObject root) {
				if (model.getResources().contains(resource)) {
					if (pageManager == null) {
						try {
							pageManager = ServiceUtilsForResourceSet.getInstance().getIPageManager(domain.getResourceSet());
						} catch (ServiceException e) {
							// No page manager? Then we have nothing to do
							domain.removeResourceSetListener(this);
							return;
						}
					}

					TransactionUIHelper.getExecutor(domain).execute(() -> {
						if (pageManager.isOpen(root)) {
							try {
								// Need an unsafe write in this context but that
								// is okay because we never record edits in the
								// sash model for undo/redo anyways
								GMFUnsafe.write(domain, () -> {
									// Because the diagram/table/whatever is deleted
									// from the resource, any and all pages that were
									// showing it must be closed.  Usually a notation
									// is only opened in at most one page in the editor,
									// but the API allows for more than one
									pageManager.closeAllOpenedPages(root);
								});
							} catch (Exception e) {
								Activator.log.error(e);
							}
						}
					});
				}
			}
		};
	}
}
