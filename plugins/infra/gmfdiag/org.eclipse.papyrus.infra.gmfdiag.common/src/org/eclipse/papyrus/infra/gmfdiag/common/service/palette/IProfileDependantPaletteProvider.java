/*****************************************************************************
 * Copyright (c) 2017 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Remi Schnekenburger (CEA LIST) remi.schnekenburger@cea.fr - Initial API and implementation
 *  Micka�l ADAM (ALL4TEC) mickael.adam@all4tec.net - bug 512343
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.service.palette;

import java.util.Collection;

import org.eclipse.gmf.runtime.diagram.ui.services.palette.IPaletteProvider;
import org.eclipse.papyrus.infra.filters.Filter;

/**
 * Interface implemented by all palette providers that requires some profiles
 * applied to be shown
 * 
 * @since 3.0
 * 
 * @deprecated Since 3.1 - Was never supported. Use {@link Filter Filters} in Palette Configuration elements instead.
 */
@Deprecated
public interface IProfileDependantPaletteProvider extends IPaletteProvider {

	/**
	 * Returns the list of required profiles for the palette to be shown
	 *
	 * @return the list of required profiles for the palette to be shown
	 */
	public Collection<String> getRequiredProfiles();

}
