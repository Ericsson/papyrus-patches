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
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.ui.editparts.INodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest.ViewDescriptor;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.CreateViewCommand;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Link;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.GraphItemImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.LinkImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;

/**
 * @author ETXACAM
 *
 */
public class CreateEdgeViewCommand extends CreateViewCommand {

	/**
	 * Constructor.
	 *
	 * @param editingDomain
	 * @param viewDescriptor
	 * @param containerView
	 */
	public CreateEdgeViewCommand(TransactionalEditingDomain editingDomain, Link interactionGraphLink, ViewDescriptor viewDescriptor, View containerView, 
			Node source, Point sourceAnchor, Node target, Point targetAnchor) {
		super(editingDomain, viewDescriptor, containerView);
		this.interactionGraphNode = (LinkImpl)interactionGraphLink;
		this.source = source;
		this.sourceAnchor = sourceAnchor;
		this.target = target;
		this.targetAnchor = targetAnchor;
	}

	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		CommandResult res = super.doExecuteWithResult(monitor, info);
		if (res.getStatus().isOK()) {
			Edge edge = (Edge) getViewDescriptor().getAdapter(View.class);
			((GraphItemImpl) interactionGraphNode).setView(edge);
			// Create Anchors
			edge.setSource(source.getView());
			edge.setSourceAnchor(createAnchor(source, target, sourceAnchor, true));
			
			edge.setTarget(target.getView());
			edge.setTargetAnchor(createAnchor(source, target, targetAnchor, false));			
		}
		return res;
	}

	private IdentityAnchor createAnchor(Node source, Node target, Point pt, boolean isSource) {
		CreateConnectionRequest loc = new CreateConnectionRequest();
		loc.setSourceEditPart(source.getEditPart());
		loc.setTargetEditPart(target.getEditPart());
		pt = ViewUtilities.viewerToControl(interactionGraphNode.getInteractionGraph().getEditPartViewer(), pt.getCopy()); 
		loc.setLocation(pt);
		loc.setSnapToEnabled(true);
		
		INodeEditPart editPart = isSource ? (INodeEditPart)source.getEditPart() : (INodeEditPart)target.getEditPart();			
		ConnectionAnchor anchor = isSource ? editPart.getSourceConnectionAnchor(loc) : editPart.getTargetConnectionAnchor(loc);
		String terminal = editPart.mapConnectionAnchorToTerminal(anchor);
		IdentityAnchor notAnchor = NotationFactory.eINSTANCE.createIdentityAnchor();
		notAnchor.setId(terminal);
		return notAnchor;
	}
	
	private LinkImpl interactionGraphNode;
	private Node source;
	private Point sourceAnchor;
	private Node target;
	private Point targetAnchor;
}
