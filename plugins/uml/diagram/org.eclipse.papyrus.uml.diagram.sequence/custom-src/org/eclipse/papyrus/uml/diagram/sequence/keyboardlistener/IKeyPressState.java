/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.keyboardlistener;

/**
 * This interface is used to set the status of a key pressed see also {@link KeyboardListener}
 *
 */
public interface IKeyPressState {

	/**
	 *
	 * @param isPressed
	 *            true if the key has bee pressed
	 */
	public void setKeyPressState(Boolean isPressed);

}
