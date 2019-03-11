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

package org.eclipse.papyrus.uml.diagram.sequence.command;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.gef.commands.Command;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.GridManagementEditPolicy;

/**
 *this command is used to explain all line must move at the same time or not
 *
 */
public class SetMoveAllLineAtSamePositionCommand extends  Command {

	protected boolean setMoveAllLineAtSamePosition;

	protected GridManagementEditPolicy grid=null;
	/**
	 * Constructor.
	 *
	 * @param domain
	 * @param label
	 * @param affectedFiles
	 */
	public SetMoveAllLineAtSamePositionCommand(GridManagementEditPolicy grid,boolean setMoveAllLineAtSamePosition) {
		super( "SetMoveAllLineAtSamePosition to "+setMoveAllLineAtSamePosition);
		this.setMoveAllLineAtSamePosition=setMoveAllLineAtSamePosition;
		this.grid=grid;
	}

	/**
	 * @see org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand#doExecuteWithResult(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 *
	 * @param monitor
	 * @param info
	 * @return
	 * @throws ExecutionException
	 */
	@Override
	public void execute() {
		grid.setMoveAllLinesAtSamePosition(setMoveAllLineAtSamePosition);
	}
	/**
	 * @see org.eclipse.gef.commands.Command#undo()
	 *
	 */
	@Override
	public void undo() {
		grid.setMoveAllLinesAtSamePosition(!setMoveAllLineAtSamePosition);
	}


}
