/*****************************************************************************
 * Copyright (c) 2012, 2016 CEA LIST, Christian W. Damus, and others.
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
 *  Christian W. Damus - bugs 433206, 461629, 466997, 478556, 485220, 501946
 *  
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.helper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.papyrus.infra.core.sasheditor.editor.ISashWindowsContainer;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.core.utils.ServiceUtils;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.IPapyrusCanonicalEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramEditPartsUtil;
import org.eclipse.papyrus.infra.ui.editor.IMultiDiagramEditor;
import org.eclipse.papyrus.infra.ui.util.EditorUtils;
import org.eclipse.papyrus.infra.ui.util.TransactionUIHelper;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;


public class DiagramHelper {
	private static final AtomicBoolean refreshPending = new AtomicBoolean();

	private static final ConcurrentMap<DiagramEditPart, Boolean> pendingDiagramRefresh = new MapMaker().concurrencyLevel(4).weakKeys().makeMap();

	public static void refresh(EditPart editPart, boolean recursive) {
		editPart.refresh();

		// CSS styles may have altered canonical synchronization properties
		refreshCanonical(editPart);

		if (recursive) {
			@SuppressWarnings("unchecked")
			List<? extends EditPart> children = editPart.getChildren();
			for (EditPart childEditPart : children) {
				refresh(childEditPart, true);
			}
		}
	}

	private static void refreshCanonical(EditPart editPart) {
		EditPolicy editPolicy = editPart.getEditPolicy(EditPolicyRoles.CANONICAL_ROLE);
		if (editPolicy instanceof IPapyrusCanonicalEditPolicy) {
			((IPapyrusCanonicalEditPolicy) editPolicy).refreshActive();
		}
	}

	public static void refresh(DiagramEditPart diagramEditPart, boolean recursive) {
		setNeedsRefresh(diagramEditPart, false);
		diagramEditPart.refresh();
		if (recursive) {
			// Defensive copy
			@SuppressWarnings("unchecked")
			List<EditPart> childrenToRefresh = ImmutableList.copyOf((List<? extends EditPart>) diagramEditPart.getChildren());
			for (EditPart childEditPart : childrenToRefresh) {
				refresh(childEditPart, true);
			}
			// Defensive copy
			@SuppressWarnings("unchecked")
			List<EditPart> connectionsToRefresh = ImmutableList.copyOf((List<? extends EditPart>) diagramEditPart.getConnections());
			for (EditPart childEditPart : connectionsToRefresh) {
				refresh(childEditPart, true);
			}
		}
	}

	/**
	 * Refreshes all diagrams in this IEditorPart (Including nested editors when necessary)
	 *
	 * @param editorPart
	 */
	public static void refresh(IEditorPart editorPart) {
		List<IEditorPart> visibleEditorParts = null;
		if (editorPart instanceof IMultiDiagramEditor) {
			ServicesRegistry servicesRegistry = editorPart.getAdapter(ServicesRegistry.class);
			if (servicesRegistry != null) {
				try {
					ISashWindowsContainer container = ServiceUtils.getInstance().getService(ISashWindowsContainer.class, servicesRegistry);
					visibleEditorParts = container.getVisibleIEditorParts();
				} catch (ServiceException e) {
					Activator.log.error(e);
				}
			}
		} else {
			visibleEditorParts = Collections.singletonList(editorPart);
		}

		if (visibleEditorParts != null) {
			for (IEditorPart visiblePart : visibleEditorParts) {
				if (visiblePart instanceof DiagramEditor) {
					DiagramEditor diagramEditor = (DiagramEditor) visiblePart;
					DiagramEditPart topEditPart = diagramEditor.getDiagramEditPart();
					if (topEditPart != null) {
						DiagramHelper.refresh(topEditPart, true);
					}
				}
			}
		}
	}

	/**
	 * Posts an asynchronous refresh that will aggregate all subsequent refresh requests until the
	 * refresh is actually performed.
	 * 
	 * @see #forceRefresh(DiagramEditPart)
	 * @see #scheduleRefresh()
	 * 
	 * @since 1.1
	 */
	public static void scheduleRefresh(final DiagramEditPart diagram) {
		if (setNeedsRefresh(diagram, true)) {
			asyncExec(diagram, new Runnable() {

				@Override
				public void run() {
					// Don't refresh a diagram that has been deleted (is no longer
					// attached to a resoure)
					if (setNeedsRefresh(diagram, false) && diagram.isActive()
							&& (diagram.getViewer() != null) && isAttached(diagram.getModel())) {

						refresh(diagram, true);
					}
				}
			});
		}
	}

	private static boolean isAttached(Object diagramView) {
		return (diagramView instanceof EObject) && (((EObject) diagramView).eResource() != null);
	}

	private static boolean setNeedsRefresh(DiagramEditPart diagram, boolean refresh) {
		if (refresh) {
			return pendingDiagramRefresh.putIfAbsent(diagram, Boolean.TRUE) == null;
		} else {
			return pendingDiagramRefresh.remove(diagram, Boolean.TRUE);
		}
	}

	private static Executor getExecutor(DiagramEditPart diagram) {
		TransactionalEditingDomain domain = (diagram == null) ? null : diagram.getEditingDomain();
		// There is a default executor for the null domain
		return TransactionUIHelper.getExecutor(domain);
	}

	/**
	 * Forces a asynchronous refresh of a {@code diagram}. Generally, an {@linkplain #scheduleRefresh(DiagramEditPart) asynchronous refresh} is preferred.
	 * 
	 * @see #scheduleRefresh(DiagramEditPart)
	 * 
	 * @since 1.1
	 */
	public static void forceRefresh(DiagramEditPart diagram) {
		setNeedsRefresh(diagram, true);
		refresh(diagram, true);
	}

	/**
	 * Refreshes all opened diagrams, if we currently {@link #setNeedsRefresh() need to}.
	 * 
	 * @deprecated Use the {@link #scheduleRefresh()} or {@link #forceRefresh()} API, instead
	 * 
	 * @see #setNeedsRefresh()
	 * @see #scheduleRefresh()
	 */
	@Deprecated
	public static void refreshDiagrams() {
		if (refreshPending.compareAndSet(true, false)) {
			IMultiDiagramEditor[] editors = EditorUtils.getMultiDiagramEditors();
			if (editors == null || editors.length < 1) {
				return;
			}
			for (IMultiDiagramEditor activeMultiEditor : editors) {
				refresh(activeMultiEditor);
			}
		}
	}

	/**
	 * Indicates that diagrams are in need of refresh, to be performed later (perhaps asynchronously).
	 * Has no effect if the needs-refresh state is already set.
	 * 
	 * @return whether the refresh state of the diagrams was changed by this call. A {@code false} result
	 *         indicates that refresh was already pending
	 * 
	 * @deprecated Use the {@link #scheduleRefresh()} or {@link #forceRefresh()} API, instead
	 * 
	 * @see #scheduleRefresh()
	 * @see #refreshDiagrams()
	 */
	@Deprecated
	public static boolean setNeedsRefresh() {
		return refreshPending.compareAndSet(false, true);
	}

	/**
	 * Posts an asynchronous refresh that will aggregate all subsequent refresh requests until the
	 * refresh is actually performed.
	 * 
	 * @see #setNeedsRefresh()
	 * @see #refreshDiagrams()
	 * 
	 * @since 1.1
	 */
	public static void scheduleRefresh() {
		if (setNeedsRefresh()) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					refreshDiagrams();
				}
			});
		}
	}

	/**
	 * Forces a asynchronous refresh of all open diagrams. Generally, an {@linkplain #scheduleRefresh() asynchronous refresh} is preferred.
	 * 
	 * @see #scheduleRefresh()
	 * 
	 * @since 1.1
	 */
	public static void forceRefresh() {
		setNeedsRefresh();
		refreshDiagrams();
	}

	/**
	 * Schedules a {@code task} for asynchronous execution in the {@code context} of a particular diagram.
	 * 
	 * @param context
	 *            the diagram context. Must not be {@code null}
	 * @param task
	 *            the task to execute asynchronously
	 */
	public static void asyncExec(DiagramEditPart context, Runnable task) {
		getExecutor(context).execute(task);
	}

	/**
	 * Schedules a {@code task} for asynchronous execution in the {@code context} of an edit-part in a diagram.
	 * 
	 * @param context
	 *            the edit-part context. Must not be {@code null}
	 * @param task
	 *            the task to execute asynchronously
	 */
	public static void asyncExec(EditPart context, Runnable task) {
		DiagramEditPart diagram = DiagramEditPartsUtil.getDiagramEditPart(context);
		getExecutor(diagram).execute(task);
	}

	/**
	 * Schedules a {@code task} for asynchronous execution in the {@code context} of a particular diagram.
	 * 
	 * @param context
	 *            the diagram context. Must not be {@code null}
	 * @param task
	 *            the task to execute asynchronously
	 */
	public static <V> ListenableFuture<V> submit(DiagramEditPart context, Callable<V> task) {
		ListenableFutureTask<V> result = ListenableFutureTask.create(task);
		getExecutor(context).execute(result);
		return result;
	}

	/**
	 * Schedules a {@code task} for asynchronous execution in the {@code context} of an edit-part in a diagram.
	 * 
	 * @param context
	 *            the edit-part context. Must not be {@code null}
	 * @param task
	 *            the task to execute asynchronously
	 */
	public static <V> ListenableFuture<V> submit(EditPart context, Callable<V> task) {
		ListenableFuture<V> result;
		DiagramEditPart diagram = DiagramEditPartsUtil.getDiagramEditPart(context);

		if (diagram != null) {
			result = submit(diagram, task);
		} else {
			ListenableFutureTask<V> runnable = ListenableFutureTask.create(task);
			// Yes, 'diagram' is null and that is okay
			getExecutor(diagram).execute(runnable);
			result = runnable;
		}

		return result;
	}
}
