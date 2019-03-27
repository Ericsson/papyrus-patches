/*****************************************************************************
 * Copyright (c) 2019 CEA LIST and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.service;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.papyrus.infra.gmfdiag.common.service.palette.AspectUnspecifiedTypeConnectionTool;
import org.eclipse.swt.graphics.Cursor;

/**
 * @author ETXACAM
 *
 */
public class MessageCreationTool extends AspectUnspecifiedTypeConnectionTool {
	public MessageCreationTool(List<IElementType> elementTypes) {
		super(elementTypes);
	}

	@Override
	protected Cursor calculateCursor() {
	   if (isInState(STATE_CONNECTION_STARTED)) {
			 Command cmd = getCommand();
			 if (cmd != null && !cmd.canExecute())
				 return getDisabledCursor();
		}
		return super.calculateCursor();
	}	
}
