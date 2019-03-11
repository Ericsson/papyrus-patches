/*****************************************************************************
 * Copyright (c) 2016 CEA LIST and others.
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

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;

/**
 * @author Patrick Tessier
 *
 */
public interface IGrillingEditpolicy {

	/**
	 * Walks up the editpart hierarchy to find and return the
	 * <code>TopGraphicEditPart</code> instance.
	 */
	public default DiagramEditPart getDiagramEditPart(EditPart editPart) {
		while (editPart instanceof IGraphicalEditPart) {
			if (editPart instanceof DiagramEditPart){
				return (DiagramEditPart) editPart;
			}

			editPart = editPart.getParent();
		}
		if(editPart instanceof DiagramRootEditPart){
			return (DiagramEditPart)((DiagramRootEditPart)editPart).getChildren().get(0);
		}
		return null;
	}
}
