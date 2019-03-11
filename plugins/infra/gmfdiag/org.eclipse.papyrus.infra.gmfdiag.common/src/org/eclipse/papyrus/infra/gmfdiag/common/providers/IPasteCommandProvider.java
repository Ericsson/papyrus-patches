/*****************************************************************************
 * Copyright (c) 2010, 2016 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Patrick Tessier (CEA LIST) Patrick.tessier@cea.fr - Initial API and implementation
 *  Christian W. Damus - bug 485220
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.providers;

import java.awt.datatransfer.Clipboard;
import java.util.Collection;

import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;

/**
 *
 * a paste provider has provide operation in order to paste by taking account system clipboard or papyrus clipboard
 *
 */
public interface IPasteCommandProvider {

	/**
	 * return the paste command to execute by taking account parameter
	 *
	 * @param targetEditPart
	 *            the target where object will be paste
	 * @param systemClipboard
	 *            contains info form the system copy paste
	 * @param papyrusCliboard
	 *            the list of views to paste
	 * @return a command
	 */
	public ICommand getPasteViewCommand(GraphicalEditPart targetEditPart, Clipboard systemClipboard, Collection<Object> papyrusCliboard);

	/**
	 * return the paste command to execute by taking account parameter. It copy also element of the semantic model
	 *
	 * @param targetEditPart
	 *            the target where object will be paste
	 * @param systemClipboard
	 *            contains info form the system copy paste
	 * @param papyrusCliboard
	 *            the list of views to paste
	 * @return a command
	 */
	public ICommand getPasteWithModelCommand(GraphicalEditPart targetEditPart, Clipboard systemClipboard, Collection<Object> papyrusCliboard);
}
