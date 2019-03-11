/*****************************************************************************
 * Copyright (c) 2014 CEA LIST.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Ansgar Radermacher (CEA LIST) - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.editpart;

import org.eclipse.gmf.runtime.common.ui.services.parser.IParser;


/**
 * Direct editors install their own parser. The objective of this interface is
 * to reset a parser within an edit part, once a direct editor is stopped.
 * This will restore the original parser
 */
public interface IControlParserForDirectEdit {
	/**
	 * install a new parser
	 *
	 * @param parser
	 */
	public void setParser(IParser parser);
}
