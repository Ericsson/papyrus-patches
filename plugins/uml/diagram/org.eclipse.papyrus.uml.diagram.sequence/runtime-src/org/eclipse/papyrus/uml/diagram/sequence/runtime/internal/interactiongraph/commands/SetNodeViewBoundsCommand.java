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
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.core.internal.command.ICommandWithSettableResult;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.InteractionGraphImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.NodeImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;

/**
 * @author ETXACAM
 *
 */
@SuppressWarnings({ "rawtypes", "restriction" })
public class SetNodeViewBoundsCommand extends AbstractTransactionalCommand
		implements ICommand, ICommandWithSettableResult {
	public SetNodeViewBoundsCommand(TransactionalEditingDomain domain, Node interactionGraphNode, String label, List affectedFiles) {
		super(domain, label, affectedFiles);
		this.interactionGraphNode = (NodeImpl) interactionGraphNode;
	}

	public SetNodeViewBoundsCommand(TransactionalEditingDomain domain, Node interactionGraphNode, String label, Map options, List affectedFiles) {
		super(domain, label, options, affectedFiles);
		this.interactionGraphNode = (NodeImpl) interactionGraphNode;
	}

	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		InteractionGraphImpl graph = interactionGraphNode.getInteractionGraph();
		View v = interactionGraphNode.getView();
		Rectangle constraints = ViewUtilities.toRelativeForLayoutConstraints(graph.getEditPartViewer(), (View) v.eContainer(), interactionGraphNode.getBounds());
		((Shape) v).setLayoutConstraint(ViewUtilities.toBounds(constraints));
		return CommandResult.newOKCommandResult();
	}

	private NodeImpl interactionGraphNode;
}
