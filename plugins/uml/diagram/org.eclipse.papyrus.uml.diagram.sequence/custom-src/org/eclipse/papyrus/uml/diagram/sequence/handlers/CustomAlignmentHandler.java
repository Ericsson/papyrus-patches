/*****************************************************************************
 * Copyright (c) 2010 CEA
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
 *   Soyatec - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.gef.commands.Command;
import org.eclipse.papyrus.uml.diagram.menu.actions.handlers.AlignementHandler;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomAlignmentHandler extends AlignementHandler {

	/**
	 * Constructor.
	 *
	 */
	public CustomAlignmentHandler() {
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.menu.actions.handlers.AlignementHandler#getCommand()
	 *
	 * @return
	 * @throws ExecutionException
	 */
	@Override
	protected Command getCommand() {
		return super.getCommand();
	}
}
