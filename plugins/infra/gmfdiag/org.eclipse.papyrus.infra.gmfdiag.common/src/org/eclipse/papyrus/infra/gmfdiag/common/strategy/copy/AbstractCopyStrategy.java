/*****************************************************************************
 * Copyright (c) 2015 CEA LIST and others.
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
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.strategy.copy;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.Image;

/**
 * Abstract Class for the Copy Strategy, implement ICopyStrategy
 * 
 * @author Céline JANSSENS
 *
 */
public abstract class AbstractCopyStrategy implements ICopyStrategy {

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.copy.ICopyStrategy#getLabel()
	 *
	 * @return the Label
	 */
	@Override
	public String getLabel() {
		return null;
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.copy.ICopyStrategy#getDescription()
	 *
	 * @return the Description
	 */
	@Override
	public String getDescription() {
		return null;
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.copy.ICopyStrategy#getImage()
	 *
	 * @return the Image
	 */
	@Override
	public Image getImage() {
		return null;
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.copy.ICopyStrategy#getID()
	 *
	 * @return the Id
	 */
	@Override
	public String getID() {
		return null;
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.copy.ICopyStrategy#getCommand(org.eclipse.gef.Request, org.eclipse.gef.EditPart)
	 */
	@Override
	public Command getCommand(Request request, EditPart targetEditPart) {
		return null;
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.copy.ICopyStrategy#getPriority()
	 *
	 * @return
	 * @deprecated
	 */
	@Deprecated
	@Override
	public int getPriority() {
		return 0;
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.copy.ICopyStrategy#prepareElementsInClipboard(java.util.List, java.util.Collection)
	 */
	@Override
	public void prepareElementsInClipboard(List<EObject> elementsInClipboard, Collection<EObject> selectedElements) {
		// By default do nothing

	}

}
