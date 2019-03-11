/*****************************************************************************
 * Copyright (c) 2011 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *		CEA LIST - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.commands;


/**
 * Interface for an adapter that can give a semantic hint
 */
public interface ISemanticHintAdapter {

	/**
	 * Returns the semantic hint for this adapter
	 *
	 * @return the semantic hint for this adapter
	 */
	public String getSemanticHint();
}
