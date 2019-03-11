/*
 * Copyright (c) 2014 CEA and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus (CEA) - Initial API and implementation
 *
 */
package org.eclipse.papyrus.infra.gmfdiag.common.adapter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.ui.editor.reload.IReloadContextProvider;


/**
 * A {@linkplain IReloadContextProvider reload context provider} for nested diagram outline pages that records and restores the selection of
 * which presentation mode (tree outline or thumbnail overview) is active for each outline page. This implementation uses reflection to access
 * internals (non API) of the outline page. On any problem in initializing this reflective access, the adapter basically disables itself and
 * never participates in editor re-load, so that the system continues normally but with reduced functionality.
 */
class DiagramOutlineReloadContextProvider implements IReloadContextProvider {

	private static final int ID_OUTLINE = 0;

	private static final int ID_OVERVIEW = 1;

	private static final Class<?> diagramOutlinePage;

	private static final Method showPage;

	private static final Field showOutlineAction;

	static {
		Class<?> diagramOutlinePageClass = null;
		Method showPageMethod = null;
		Field showOutlineActionField = null;

		for (Class<?> next : DiagramEditor.class.getDeclaredClasses()) {
			if ("DiagramOutlinePage".equals(next.getSimpleName())) { //$NON-NLS-1$
				diagramOutlinePageClass = next;

				try {
					showPageMethod = diagramOutlinePageClass.getDeclaredMethod("showPage", int.class); //$NON-NLS-1$
					showPageMethod.setAccessible(true);

					showOutlineActionField = diagramOutlinePageClass.getDeclaredField("showOutlineAction"); //$NON-NLS-1$
					showOutlineActionField.setAccessible(true);
				} catch (Exception e) {
					// Can't reflect? Then abandon all hope
					Activator.log.error(e);
					diagramOutlinePageClass = null;
				}

				break;
			}
		}

		diagramOutlinePage = diagramOutlinePageClass;
		showPage = showPageMethod;
		showOutlineAction = showOutlineActionField;
	}

	private final Object diagramOutline;

	public DiagramOutlineReloadContextProvider(Object diagramOutline) {
		super();

		this.diagramOutline = diagramOutline;
	}

	@Override
	public Object createReloadContext() {
		return new ReloadContext(diagramOutline);
	}

	@Override
	public void restore(Object reloadContext) {
		((ReloadContext) reloadContext).restore(diagramOutline);
	}

	static boolean isDiagramOutline(Object o) {
		// If we couldn't reflect on DiagramOutline class, then we will never detect a diagram outline
		// instance, so we will never try to create an adapter, and all is safe. The only consequence
		// will be that we don't get diagram outline state properly restored. That's fine
		return (diagramOutlinePage != null) && diagramOutlinePage.isInstance(o);
	}

	//
	// Nested types
	//

	private static class ReloadContext {

		private final int pageID;

		public ReloadContext(Object diagramOutline) {
			super();

			try {
				this.pageID = ((IAction) showOutlineAction.get(diagramOutline)).isChecked() ? ID_OUTLINE : ID_OVERVIEW;
			} catch (IllegalAccessException e) {
				// We wouldn't be here if we couldn't make it accessible. Something is very wrong
				throw new Error(e);
			}
		}

		void restore(Object diagramOutline) {
			try {
				showPage.invoke(diagramOutline, pageID);
			} catch (IllegalAccessException e) {
				// We wouldn't be here if we couldn't make it accessible. Something is very wrong
				throw new Error(e);
			} catch (InvocationTargetException e) {
				Throwable target = e.getTargetException();
				if (target instanceof RuntimeException) {
					throw (RuntimeException) target;
				} else if (target instanceof Error) {
					throw (Error) target;
				} else {
					Activator.log.error(target);
				}
			}
		}
	}
}
