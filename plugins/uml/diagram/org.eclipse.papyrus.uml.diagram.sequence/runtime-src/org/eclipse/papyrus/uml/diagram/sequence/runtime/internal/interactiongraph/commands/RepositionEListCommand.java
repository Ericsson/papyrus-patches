/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
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

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;

@SuppressWarnings("rawtypes")
public class RepositionEListCommand extends AbstractTransactionalCommand {

	/**
	 * the amount to move element by relative to its position
	 */
	private List newPositions;

	/**
	 * The list of elements in which reposition will take place.
	 */
	private EList elements;

	/**
	 * Constructs a runtime instance of <code>RepositionEObjectCommand</code>.
	 *
	 * @param editingDomain
	 *            the editing domain through which model changes are made
	 * @param label
	 *            label for command
	 * @param elements
	 *            the list of elements in which reposition will take place
	 * @param element
	 *            target element
	 * @param displacement
	 *            amount of movement
	 */
	public RepositionEListCommand(TransactionalEditingDomain editingDomain, String label, EList elements, List newPositions) {
		super(editingDomain, label, null);
		this.newPositions = newPositions;
		this.elements = elements;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected CommandResult doExecuteWithResult(
			IProgressMonitor progressMonitor, IAdaptable info)
			throws ExecutionException {
		CommandResult commandResult = null;
		try {
			int index = 0;
			for (Object obj : newPositions) {
				elements.move(index++, obj);
			}
		} catch (RuntimeException exp) {
			commandResult = CommandResult.newErrorCommandResult(exp);
		}
		return (commandResult == null) ? CommandResult.newOKCommandResult()
				: commandResult;
	}

}