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
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;

/**
 * @author ETXACAM
 *
 */
public class TransactionalCommandProxy extends AbstractTransactionalCommand {
	public TransactionalCommandProxy(TransactionalEditingDomain domain, Command emfCommand, String label, List affectedFiles) {
		super(domain, label, affectedFiles);
		this.emfCommand = emfCommand;
	}

	public TransactionalCommandProxy(TransactionalEditingDomain domain, Command emfCommand, String label, Map options, List affectedFiles) {
		super(domain, label, options, affectedFiles);
		this.emfCommand = emfCommand;
	}

	@Override
	public boolean canExecute() {
		return emfCommand.canExecute();
	}

	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		try {
			emfCommand.execute();
		} catch (Throwable th) {
			return CommandResult.newErrorCommandResult(th);
		}
		return CommandResult.newOKCommandResult();
	}

	private Command emfCommand;
}
