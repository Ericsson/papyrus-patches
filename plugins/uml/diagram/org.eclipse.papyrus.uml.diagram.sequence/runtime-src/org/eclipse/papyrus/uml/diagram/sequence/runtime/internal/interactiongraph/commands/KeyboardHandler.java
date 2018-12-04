/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;

/**
 * @author etxacam
 *
 */
public class KeyboardHandler implements Listener {
	static final int REORDER_KEY = SWT.SHIFT;

	/*
	 * static {
	 * if (Platform.OS_MACOSX.equals(Platform.getOS())) {
	 * REORDER_KEY = SWT.CTRL;
	 * } else {
	 * REORDER_KEY = SWT.ALT;
	 * }
	 * }
	 */

	public KeyboardHandler() {
		this(REORDER_KEY);
	}


	public KeyboardHandler(int... keyCodeMasks) {
		for (int kcm : keyCodeMasks) {
			keyPressState.put(kcm, false);
		}
	}

	public void activate() {
		PlatformUI.getWorkbench().getDisplay().addFilter(SWT.KeyDown, this);
		PlatformUI.getWorkbench().getDisplay().addFilter(SWT.KeyUp, this);
		keyPressState.entrySet().forEach(e -> e.setValue(false));
	}


	public void deactivate() {
		PlatformUI.getWorkbench().getDisplay().removeFilter(SWT.KeyDown, this);
		PlatformUI.getWorkbench().getDisplay().removeFilter(SWT.KeyUp, this);
		keyPressState.entrySet().forEach(e -> e.setValue(false));
	}

	@Override
	public void handleEvent(Event event) {
		if (keyPressState.containsKey(event.keyCode)) {
			if (event.type == 1) {
				keyPressState.put(event.keyCode, true);
			} else if (event.type == 2) {
				keyPressState.put(event.keyCode, false);
			}

		}
	}

	public boolean isPressed(int key) {
		return keyPressState.get(key);
	}

	public boolean isAnyPressed() {
		return keyPressState.values().stream().filter(d -> d).findFirst().orElse(false);
	}

	protected HashMap<Integer, Boolean> keyPressState = new HashMap<>();
}
