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
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;


public class ConnectionToolPreferences {

	public static final String DRAW_CONNECTION_MODE = "drawConnectionMode";

	public static final String SINGLE_CLICK = "singleClick";

	public static final String TWO_CLICKS = "twoClicks";

	private ConnectionToolPreferences() {
	}

	public boolean isInSingleClickMode() {
		return SINGLE_CLICK.equals(preferences.getString(DRAW_CONNECTION_MODE));
	}

	private IPreferenceStore preferences = Activator.getInstance().getPreferenceStore();

	public static final ConnectionToolPreferences instance = new ConnectionToolPreferences();
}
