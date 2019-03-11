/*****************************************************************************
 * Copyright (c) 2013 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Laurent Wouters (CEA LIST) laurent.wouters@cea.fr - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.handler;

import org.eclipse.ui.IEditorPart;

/**
 * Represents a handler of refresh event
 *
 * @author Laurent Wouters
 */
public interface IRefreshHandlerPart {
	/**
	 * Refresh what needs to be, taking into account that the active editor is the given one
	 *
	 * @param editorPart
	 *            The currently active editor
	 */
	void refresh(IEditorPart editorPart);
}
