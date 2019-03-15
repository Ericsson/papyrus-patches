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

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;

/**
 * @author etxacam
 *
 */
public class SetNodeViewZOrderCommand extends AbstractTransactionalCommand {
	public SetNodeViewZOrderCommand(TransactionalEditingDomain editingDomain, InteractionGraph graph,  Node node, int index ) {
		super(editingDomain, "Set ZOrder", null);
		this.node = node;
		this.index = index;
	}	

	
	@Override
	public boolean canExecute() {
		return true;
	}

	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		View view = node.getView();
		ViewUtil.repositionChildAt((View)view.eContainer(), view, index);
		return CommandResult.newOKCommandResult();
	}
	
	private Node node;
	private int index;
}
