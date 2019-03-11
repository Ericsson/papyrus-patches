/***************************************************************************************************
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
 *  Celine Janssens (ALL4TEC) celine.janssens@all4tec.net - Add the prepareElementInClipboard Method
 ***************************************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.strategy.copy;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.papyrus.infra.gmfdiag.common.strategy.IStrategy;
import org.eclipse.swt.graphics.Image;

/**
 * A strategy to be applied when copying elements
 */
public interface ICopyStrategy extends IStrategy {

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
	 * 		A command, or null if the strategy cannot handle the request
	 */
	public Command getCommand(Request request, EditPart targetEditPart);

	/**
	 * The default priority for this strategy. Might be overridden by a user
	 * preference.
	 *
	 * @return
	 * @deprecated The priority mechanism isn't used anymore
	 */
	@Override
	@Deprecated
	public int getPriority();

	/**
	 * This method allows to modify the elements list in the ClipBoard before being Paste
	 * 
	 * @param elementsInClipboard
	 *            The list of Objects in the clipBoard to be modified, same as selectedElements by default
	 * @param selectedElements
	 *            The selected Elements
	 */
	public void prepareElementsInClipboard(List<EObject> elementsInClipboard, Collection<EObject> selectedElements);



}
