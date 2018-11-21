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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest.ViewDescriptor;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.CreateViewCommand;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.NodeImpl;

/**
 * @author ETXACAM
 *
 */
public class CreateNodeViewCommand extends CreateViewCommand {

	/**
	 * Constructor.
	 *
	 * @param editingDomain
	 * @param viewDescriptor
	 * @param containerView
	 */
	public CreateNodeViewCommand(TransactionalEditingDomain editingDomain, Node interactionGraphNode, ViewDescriptor viewDescriptor, View containerView) {
		super(editingDomain, viewDescriptor, containerView);
		this.interactionGraphNode = interactionGraphNode;
	}

	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		CommandResult res = super.doExecuteWithResult(monitor, info);
		if (res.getStatus().isOK()) {
			((NodeImpl) interactionGraphNode).setView((View) getViewDescriptor().getAdapter(View.class));
		}
		return res;
	}

	private Node interactionGraphNode;
}