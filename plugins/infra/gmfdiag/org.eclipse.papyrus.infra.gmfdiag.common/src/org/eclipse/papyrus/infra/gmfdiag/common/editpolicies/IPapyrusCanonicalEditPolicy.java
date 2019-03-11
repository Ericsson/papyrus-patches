/*****************************************************************************
 * Copyright (c) 2015 Christian W. Damus and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import org.eclipse.gef.EditPolicy;

/**
 * A mix-in interface for Papyrus implementations of the canonical edit policy that support conditional activation.
 */
public interface IPapyrusCanonicalEditPolicy {

	/**
	 * Queries whether I am currently active.
	 * 
	 * @return whether I am active
	 * 
	 * @see EditPolicy#activate()
	 */
	boolean isActive();

	/**
	 * Refreshes my activation state, checking perhaps whether I need to be activated or deactivated according to
	 * my activation condition.
	 * 
	 * @see #isActive()
	 * @see EditPolicy#activate()
	 * @see EditPolicy#deactivate()
	 */
	void refreshActive();
}
