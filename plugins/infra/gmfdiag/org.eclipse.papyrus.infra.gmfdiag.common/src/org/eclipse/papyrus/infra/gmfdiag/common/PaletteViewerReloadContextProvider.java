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

import java.util.List;

import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteStack;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.papyrus.infra.core.utils.AdapterUtils;
import org.eclipse.papyrus.infra.ui.editor.reload.EditorReloadEvent;
import org.eclipse.papyrus.infra.ui.editor.reload.IReloadContextProvider;
import org.eclipse.papyrus.infra.ui.util.UIUtil;
import org.eclipse.ui.IMemento;


/**
 * An {@linkplain EditorReloadEvent editor reload} context provider for palette viewers, providing
 * a reload context that captures and restores the expansion states of drawers and selection states
 * of stacks and tools.
 */
class PaletteViewerReloadContextProvider implements IReloadContextProvider {

	private static final String MEMENTO_ACTIVE_ENTRY = "activeEntry"; //$NON-NLS-1$

	private PaletteViewer palette;

	public PaletteViewerReloadContextProvider(PaletteViewer palette) {
		super();

		this.palette = palette;
	}

	public static IReloadContextProvider getInstance(PaletteViewer palette) {
		IReloadContextProvider result = AdapterUtils.adapt(palette, IReloadContextProvider.class, null);

		if (result == null) {
			result = new PaletteViewerReloadContextProvider(palette);
		}

		return result;
	}

	@Override
	public Object createReloadContext() {
		IMemento result = UIUtil.createLocalMemento();
		palette.saveState(result);
		saveMoreState(palette.getPaletteRoot(), result);
		return result;
	}

	@Override
	public void restore(Object reloadContext) {
		IMemento memento = (IMemento) reloadContext;
		palette.restoreState(memento);
		restoreMoreState(palette.getPaletteRoot(), memento);
	}

	/**
	 * The palette's own memento doesn't record which tool of a stack is the stack's active tool.
	 */
	void saveMoreState(PaletteEntry entry, IMemento memento) {
		if (entry instanceof PaletteStack) {
			PaletteStack stack = (PaletteStack) entry;
			memento.putInteger(MEMENTO_ACTIVE_ENTRY, stack.getChildren().indexOf(stack.getActiveEntry()));
		}

		if (entry instanceof PaletteContainer) {
			PaletteContainer container = (PaletteContainer) entry;
			List<?> children = container.getChildren();
			IMemento[] mementos = memento.getChildren();

			int max = Math.min(children.size(), mementos.length);
			for (int i = 0; i < max; i++) {
				saveMoreState((PaletteEntry) children.get(i), mementos[i]);
			}
		}
	}

	/**
	 * @see #saveMoreState(PaletteEntry, IMemento)
	 */
	void restoreMoreState(PaletteEntry entry, IMemento memento) {
		if (entry instanceof PaletteStack) {
			PaletteStack stack = (PaletteStack) entry;
			List<?> children = stack.getChildren();

			int activeIndex = memento.getInteger(MEMENTO_ACTIVE_ENTRY);
			if ((activeIndex >= 0) && (activeIndex < children.size())) {
				stack.setActiveEntry((PaletteEntry) children.get(activeIndex));
			}
		}

		if (entry instanceof PaletteContainer) {
			PaletteContainer container = (PaletteContainer) entry;
			List<?> children = container.getChildren();
			IMemento[] mementos = memento.getChildren();

			int max = Math.min(children.size(), mementos.length);
			for (int i = 0; i < max; i++) {
				restoreMoreState((PaletteEntry) children.get(i), mementos[i]);
			}
		}
	}

}
