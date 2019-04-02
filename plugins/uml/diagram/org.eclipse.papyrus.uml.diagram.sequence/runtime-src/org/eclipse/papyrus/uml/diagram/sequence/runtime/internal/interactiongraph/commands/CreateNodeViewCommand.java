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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest.ViewDescriptor;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.CreateViewCommand;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.GraphItem;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.GraphItemImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.InteractionGraphImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.NodeImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;

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
		this.interactionGraphNode = (NodeImpl)interactionGraphNode;
	}

	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		CommandResult res = super.doExecuteWithResult(monitor, info);
		if (res.getStatus().isOK()) {
			InteractionGraphImpl graph = interactionGraphNode.getInteractionGraph();
			View view = (View) getViewDescriptor().getAdapter(View.class);
			Rectangle constraints = ViewUtilities.toRelativeForLayoutConstraints(graph.getEditPartViewer(), (View) view.eContainer(), 
				interactionGraphNode.getBounds());		
			((Shape) view).setLayoutConstraint(ViewUtilities.toBounds(constraints));

			GraphicalEditPart ep = (GraphicalEditPart)graph.getEditPartViewer().getEditPartRegistry().get(containerView);
			if (ep != null) {
				ep.refresh();
				ep.getContentPane().invalidateTree();
				ep.getContentPane().validate();				
			}
			
			((GraphItemImpl) interactionGraphNode).setView(view);
		}
		return res;
	}

	private NodeImpl interactionGraphNode;
}
