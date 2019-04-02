/*****************************************************************************
 * (c) Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) - Initial API and implementation
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
	static final int REORDER_KEY = SWT.MOD1;
	private static final KeyboardHandler INSTANCE = new KeyboardHandler(REORDER_KEY);
	
	public static KeyboardHandler getKeyboardHandler() {
		if (!INSTANCE.activated)
			INSTANCE.activate();
		INSTANCE.activated = true;
		return INSTANCE;
	}

	/*
	 * static {
	 * if (Platform.OS_MACOSX.equals(Platform.getOS())) {
	 * REORDER_KEY = SWT.CTRL;
	 * } else {
	 * REORDER_KEY = SWT.ALT;
	 * }
	 * }
	 */

	private KeyboardHandler() {
		this(REORDER_KEY);
	}


	private KeyboardHandler(int... keyCodeMasks) {
		for (int kcm : keyCodeMasks) {
			keyPressState.put(kcm, false);
		}
	}

	private void activate() {
		PlatformUI.getWorkbench().getDisplay().addFilter(SWT.KeyDown, this);
		PlatformUI.getWorkbench().getDisplay().addFilter(SWT.KeyUp, this);
		keyPressState.entrySet().forEach(e -> e.setValue(false));
	}


	private void deactivate() {
		PlatformUI.getWorkbench().getDisplay().removeFilter(SWT.KeyDown, this);
		PlatformUI.getWorkbench().getDisplay().removeFilter(SWT.KeyUp, this);
		keyPressState.entrySet().forEach(e -> e.setValue(false));
	}

	@Override
	public void handleEvent(Event event) {
		if (keyPressState.containsKey(event.keyCode)) {
			if (event.type == 1) {
				keyPressState.put(event.keyCode, Boolean.TRUE);
			} else if (event.type == 2) {
				keyPressState.put(event.keyCode, Boolean.FALSE);
			}	
		}
	}

	public boolean isPressed(int key) {
		return keyPressState.get(key);
	}

	public boolean isAnyPressed() {
		return keyPressState.values().stream().filter(d -> d.booleanValue()).findFirst().orElse(false);
	}

	protected HashMap<Integer, Boolean> keyPressState = new HashMap<>();
	protected boolean activated = false; 
}
