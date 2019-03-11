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
 *   CEA LIST - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common;

import org.eclipse.gef.ui.parts.GraphicalEditor;


/**
 * An interface for helper objects that support the functioning of GMF {@link GraphicalEditor}s
 */
public interface IGraphicalEditorSupport {
	void initialize(GraphicalEditor editor);
}
