/*****************************************************************************
 * (c) Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) - Initial API and implementation
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
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionEditPart;
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
	public SetNodeViewBoundsCommand(TransactionalEditingDomain domain, Node interactionGraphNode, Rectangle r, boolean refreshParts, String label, List affectedFiles) {
		super(domain, label, affectedFiles);
		this.interactionGraphNode = (NodeImpl) interactionGraphNode;
		this.rect = r;
		this.refreshEditParts = refreshParts;
		
	}

	public SetNodeViewBoundsCommand(TransactionalEditingDomain domain, Node interactionGraphNode, Rectangle r, boolean refreshParts, String label, Map options, List affectedFiles) {
		super(domain, label, options, affectedFiles);
		this.interactionGraphNode = (NodeImpl) interactionGraphNode;
		this.rect = r;
		this.refreshEditParts = refreshParts;
	}

	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		InteractionGraphImpl graph = interactionGraphNode.getInteractionGraph();
		View v = interactionGraphNode.getView();
		if (v.getType().equals(InteractionEditPart.VISUAL_ID)) {
			((Shape) v).setLayoutConstraint(ViewUtilities.toBounds(rect));
		} else {
			Rectangle constraints = ViewUtilities.toRelativeForLayoutConstraints(graph.getEditPartViewer(), (View) v.eContainer(), rect);
			((Shape) v).setLayoutConstraint(ViewUtilities.toBounds(constraints));
		}
		
		if (refreshEditParts) {
			GraphicalEditPart ep = (GraphicalEditPart)graph.getEditPartViewer().getEditPartRegistry().get(v);
			if (ep != null) {
				ep.refresh();
				((GraphicalEditPart )ep.getParent()).getContentPane().invalidateTree();
				((GraphicalEditPart )ep.getParent()).getContentPane().validate();				
			}
		}		
		return CommandResult.newOKCommandResult();
	}

	private NodeImpl interactionGraphNode;
	private Rectangle rect;
	private boolean refreshEditParts;
}
