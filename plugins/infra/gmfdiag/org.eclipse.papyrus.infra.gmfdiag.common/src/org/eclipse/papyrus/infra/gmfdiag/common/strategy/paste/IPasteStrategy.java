/*****************************************************************************
 * Copyright (c) 2014 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Benoit Maggi (CEA LIST) benoit.maggi@cea.fr - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gef.commands.Command;
import org.eclipse.papyrus.infra.core.clipboard.PapyrusClipboard;
import org.eclipse.papyrus.infra.gmfdiag.common.strategy.IStrategy;
import org.eclipse.swt.graphics.Image;

/**
 * A strategy to be applied when pasting elements
 */
public interface IPasteStrategy extends IStrategy {

	/**
	 * A user-readable label
	 *
	 * @return
	 */
	@Override
	public String getLabel();

	/**
	 * A user-readable description
	 *
	 * @return
	 */
	@Override
	public String getDescription();

	/**
	 * An image to associate to this strategy
	 *
	 * @return
	 */
	@Override
	@Deprecated
	public Image getImage();

	/**
	 * A unique ID for this strategy
	 *
	 * @return
	 */
	@Override
	public String getID();

	/**
	 * The command to be executed when the strategy is applied.
	 * Should return null if the strategy cannot handle the request.
	 *
	 * @param request
	 *            The drop request
	 * @param targetEditPart
	 *            The target edit part
	 * @return
	 *         A command, or null if the strategy cannot handle the request
	 */
	// public Command getCommand(Request request, EditPart targetEditPart);

	/**
	 * The default priority for this strategy. Might be overridden by a user
	 * preference.
	 *
	 * @return
	 * @deprecated The priority mechanism should not be used anymore
	 */
	@Override
	@Deprecated
	public int getPriority();

	/**
	 * Get the command for the semantic strategy pasting
	 *
	 * @param domain
	 * @param targetOwner
	 * @param papyrusClipboard
	 * @return
	 */
	public org.eclipse.emf.common.command.Command getSemanticCommand(EditingDomain domain, EObject targetOwner, PapyrusClipboard<Object> papyrusClipboard);

	/**
	 * Get the command for the graphical and semantic strategy pasting
	 *
	 * @param domain
	 * @param targetOwner
	 * @param papyrusClipboard
	 * @return
	 */
	public Command getGraphicalCommand(EditingDomain domain, org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart targetOwner, PapyrusClipboard<Object> papyrusClipboard);


	/**
	 * Get the required strategy if one
	 *
	 * @return
	 */
	public IPasteStrategy dependsOn();

	/**
	 * Prepare in the clipboard the data required for the pasting strategy
	 *
	 * @param papyrusClipboard
	 * @param list
	 */
	public void prepare(PapyrusClipboard<Object> papyrusClipboard, Collection<EObject> selection);

}
