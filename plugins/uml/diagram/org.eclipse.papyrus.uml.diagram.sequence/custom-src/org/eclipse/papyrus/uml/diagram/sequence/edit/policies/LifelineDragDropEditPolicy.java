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

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.DragDropEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.requests.DropObjectsRequest;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphRequestHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.InteractionGraphImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.InteractionGraphCommand;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.KeyboardHandler;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Lifeline;

/**
 * @author ETXACAM
 *
 */
public class LifelineDragDropEditPolicy extends DragDropEditPolicy {

	public void activate() {
		super.activate();
		KeyboardHandler.getKeyboardHandler(); // Force the keyboard handler to be active
	}


	@Override
	protected Command getDropCommand(ChangeBoundsRequest request) {
		GraphicalEditPart ep = (GraphicalEditPart)request.getEditParts().get(0);
		if (ep instanceof AbstractExecutionSpecificationEditPart) {
			if (!KeyboardHandler.getKeyboardHandler().isAnyPressed())
				return UnexecutableCommand.INSTANCE;
			
			InteractionGraphImpl graph = (InteractionGraphImpl)InteractionGraphRequestHelper.getOrCreateInteractionGraph(
					request, (org.eclipse.gef.GraphicalEditPart) getHost());
			if (graph == null)
				return null;
			
			View view = (View)ep.getModel();
			ExecutionSpecification exec = (ExecutionSpecification)view.getElement();

			InteractionGraphCommand cmd = new InteractionGraphCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), 
					"Move Execution Specification", graph, null);
			Cluster execNode = graph.getClusterFor(exec);
			Point pt = execNode.getBounds().getTopLeft().getCopy();
			pt.translate(request.getMoveDelta());
			cmd.moveExecutionSpecification(exec, (Lifeline)((View)getHost().getModel()).getElement(), pt);
			return new ICommandProxy(cmd);		
		} 
		return super.getDropCommand(request);
	}

	@Override
	protected Command getDropElementCommand(EObject element, DropObjectsRequest request) {
		return null;
	}

	@Override
	protected Command getDropObjectsCommand(DropObjectsRequest request) {
		return null;
	}

}
