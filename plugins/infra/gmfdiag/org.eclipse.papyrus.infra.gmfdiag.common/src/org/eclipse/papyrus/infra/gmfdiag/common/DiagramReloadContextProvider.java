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
package org.eclipse.papyrus.infra.gmfdiag.common;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.views.palette.PalettePage;
import org.eclipse.papyrus.infra.core.utils.AdapterUtils;
import org.eclipse.papyrus.infra.ui.editor.reload.EMFSelectionContext;
import org.eclipse.papyrus.infra.ui.editor.reload.EditorReloadEvent;
import org.eclipse.papyrus.infra.ui.editor.reload.IDisposableReloadContext;
import org.eclipse.papyrus.infra.ui.editor.reload.IReloadContextProvider;

import com.google.common.collect.Iterables;


/**
 * An {@linkplain EditorReloadEvent editor reload} context provider for nested diagram editors, providing
 * a reload context that captures and restores the edit-part selection (tracked by URI of the selected
 * edit parts' notation views) and palette state (delegated to a {@link PaletteViewerReloadContextProvider}).
 */
class DiagramReloadContextProvider implements IReloadContextProvider {

	private final SynchronizableGmfDiagramEditor editor;

	public DiagramReloadContextProvider(SynchronizableGmfDiagramEditor editor) {
		this.editor = editor;
	}

	@Override
	public Object createReloadContext() {
		return new DiagramSelectionContext(editor);
	}

	@Override
	public void restore(Object reloadContext) {
		((DiagramSelectionContext) reloadContext).restore(editor);
	}

	//
	// Nested types
	//

	private static class DiagramSelectionContext extends EMFSelectionContext implements IDisposableReloadContext {

		private SynchronizableGmfDiagramEditor editor;

		private Object embeddedPaletteContext;

		private Object palettePageContext;

		DiagramSelectionContext(SynchronizableGmfDiagramEditor editor) {
			super(editor.getDiagramGraphicalViewer());

			PaletteViewer embedded = getEmbeddedPalette(editor);
			if (embedded != null) {
				embeddedPaletteContext = PaletteViewerReloadContextProvider.getInstance(embedded).createReloadContext();
			}

			PalettePage page = Iterables.getFirst(editor.getPalettePages(), null);
			if (page != null) {
				// Get one context for all the pages (there should be only one Palette view!)
				IReloadContextProvider pageProvider = AdapterUtils.adapt(page, IReloadContextProvider.class, null);
				if (pageProvider != null) {
					palettePageContext = pageProvider.createReloadContext();
				}
			}
		}

		@Override
		public void dispose() {
			editor = null;
			embeddedPaletteContext = null;
			palettePageContext = null;
		}

		void restore(SynchronizableGmfDiagramEditor editor) {
			this.editor = editor;

			restore(editor.getDiagramGraphicalViewer());

			if (embeddedPaletteContext != null) {
				PaletteViewer palette = getEmbeddedPalette(editor);
				if (palette != null) {
					PaletteViewerReloadContextProvider.getInstance(palette).restore(embeddedPaletteContext);
				}
			}

			if (palettePageContext != null) {
				Collection<? extends PalettePage> pages = editor.getPalettePages();
				if (pages.isEmpty()) {
					// Defer until the page is created (which we assume it eventually will be, since evidently it
					// was there when we captured the reload context)
					editor.setDeferredPalettePageReloadContext(palettePageContext);
				} else {
					for (PalettePage page : pages) {
						IReloadContextProvider pageProvider = AdapterUtils.adapt(page, IReloadContextProvider.class, null);
						if (pageProvider != null) {
							pageProvider.restore(palettePageContext);
						}
					}
				}
			}
		}

		@Override
		protected Object deresolveSelectableElement(Object selectableElement) {
			Object result = null;

			if (selectableElement instanceof EditPart) {
				Object model = ((EditPart) selectableElement).getModel();
				if (model instanceof EObject) {
					result = model;
				}
			}

			return result;
		}

		@Override
		protected Object resolveSelectableElement(Object deresolved) {
			return editor.getDiagramGraphicalViewer().getEditPartRegistry().get(deresolved);
		}

		PaletteViewer getEmbeddedPalette(SynchronizableGmfDiagramEditor editor) {
			PaletteViewer result = null;

			if (editor.getDiagramEditDomain() instanceof EditDomain) {
				result = ((EditDomain) editor.getDiagramEditDomain()).getPaletteViewer();
			}

			return result;
		}
	}
}
