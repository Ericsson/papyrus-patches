/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
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

package org.eclipse.papyrus.uml.diagram.sequence.keyboardlistener;

import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetMoveAllLineAtSamePositionCommand;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.GridManagementEditPolicy;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * This listener listen keyboard and affect the behavior of the grid
 *
 */
public class KeyToSetMoveLinesListener  implements Listener{

	protected GridManagementEditPolicy gridManagementEditPolicy;
	protected boolean moveAllLines;
	protected int keyboard;
	/**
	 * Constructor.
	 *
	 */
	public KeyToSetMoveLinesListener(GridManagementEditPolicy gridManagementEditPolicy,int keyboard,boolean moveAllLines ) {
		this.gridManagementEditPolicy=gridManagementEditPolicy;
		this.moveAllLines=moveAllLines;
		this.keyboard=keyboard;
	}
	@Override
	public void handleEvent(Event event) {
		if (event.keyCode == keyboard) {
			SetMoveAllLineAtSamePositionCommand setMoveAllLineAtSamePositionCommand= new SetMoveAllLineAtSamePositionCommand(gridManagementEditPolicy, moveAllLines);
			((GraphicalEditPart)gridManagementEditPolicy.getHost()).getDiagramEditDomain().getDiagramCommandStack().execute(setMoveAllLineAtSamePositionCommand);
		}
	}
}
