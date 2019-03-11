/*****************************************************************************
 * Copyright (c) 2012 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Camille Letavernier (CEA LIST) camille.letavernier@cea.fr - Initial API and implementation
 *  Gabriel Pascual (ALL4TEC) gabriel.pascual@all4tec.net - Bug 372322
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.handler;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.CanonicalEditPolicy;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.DiagramHelper;
import org.eclipse.papyrus.infra.ui.editor.IMultiDiagramEditor;
import org.eclipse.papyrus.infra.ui.util.ServiceUtilsForHandlers;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

/**
 * Handles the refresh command on the Papyrus editor (F5)
 *
 * Refreshes the active diagram
 *
 * @author Camille Letavernier
 */
// TODO: To be refactored. Move to infra/gmfdiag/menu when the plugin exists
public class RefreshHandler extends AbstractHandler {

	/**
	 * The registry of refresh handler parts that are NOT specific to an editor
	 * We use weak references so that we do not block the garbage collection of objects
	 */
	private static Collection<WeakReference<IRefreshHandlerPart>> PARTS = new LinkedList<WeakReference<IRefreshHandlerPart>>();

	/**
	 * Register a refresher part that is not attached to any editor
	 *
	 * @param refresher
	 *            The refresher part
	 */
	public static synchronized void register(IRefreshHandlerPart refresher) {
		PARTS.add(new WeakReference<IRefreshHandlerPart>(refresher));
	}

	/**
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart activeEditor;
		try {
			activeEditor = ServiceUtilsForHandlers.getInstance().getService(IMultiDiagramEditor.class, event);
		} catch (ServiceException ex) {
			Activator.log.error(ex);
			return null;
		}

		updateUMLDiagram();

		// Hooks must be called before the actual refresh, because they typically reset cache
		// Call the relevant hooks
		callRefresherHooks(activeEditor);

		// Refresh the base GMF objects
		DiagramHelper.refresh(activeEditor);

		return null;
	}

	/**
	 * Update UML diagram with canonical policy.
	 * 
	 * <p>
	 * This implementation comes from refactor of Generated code for the Bug 372322. It was generated in an update command to refresh current diagram.
	 * </p>
	 */
	private void updateUMLDiagram() {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.size() != 1) {


				if (structuredSelection.getFirstElement() instanceof EditPart && ((EditPart) structuredSelection.getFirstElement()).getModel() instanceof View) {
					EObject modelElement = ((View) ((EditPart) structuredSelection.getFirstElement()).getModel()).getElement();
					List<CanonicalEditPolicy> editPolicies = CanonicalEditPolicy.getRegisteredEditPolicies(modelElement);
					for (Iterator<CanonicalEditPolicy> it = editPolicies.iterator(); it.hasNext();) {
						CanonicalEditPolicy nextEditPolicy = it.next();
						nextEditPolicy.refresh();
					}
				}
			}
		}
	}

	private synchronized void callRefresherHooks(IEditorPart activeEditor) {
		Iterator<WeakReference<IRefreshHandlerPart>> iterator = PARTS.iterator();
		while (iterator.hasNext()) {
			WeakReference<IRefreshHandlerPart> wr = iterator.next();
			IRefreshHandlerPart part = wr.get();
			if (part != null) {
				part.refresh(activeEditor);
			} else {
				iterator.remove();
			}
		}
	}

}
