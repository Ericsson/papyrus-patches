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

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.editpolicies;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.requests.EditCommandRequestWrapper;
import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
import org.eclipse.gmf.runtime.emf.type.core.requests.ConfigureRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyReferenceRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DuplicateElementsRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.GetEditContextRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.MoveRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientReferenceRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.SetRequest;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultSemanticEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphRequestHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.InteractionGraphCommand;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;

/**
 * @author ETXACAM
 *
 */
public class InteractionGraphSemanticEditPolicy extends DefaultSemanticEditPolicy {

	public Command getCommand(Request request) {
		if (RequestConstants.REQ_SEMANTIC_WRAPPER.equals(request.getType())) {
			EditCommandRequestWrapper wrapper = (EditCommandRequestWrapper)request;
			InteractionGraph graph = InteractionGraphRequestHelper.getInteractionGraph(wrapper);
			if (graph == null) {
				graph = InteractionGraphRequestHelper.getOrCreateInteractionGraph(wrapper, (GraphicalEditPart)getHost());
				InteractionGraphRequestHelper.bound(wrapper, graph);				
			}

			if (graph != null) {
				InteractionGraphRequestHelper.bound(wrapper.getEditCommandRequest(), graph);
			}  
		}
		return super.getCommand(request);
	}
	@Override
	protected IEditCommandRequest completeRequest(IEditCommandRequest request) {
		InteractionGraph graph = InteractionGraphRequestHelper.getInteractionGraph(request);
		IEditCommandRequest res = super.completeRequest(request);
		InteractionGraphRequestHelper.bound(res, graph);
		return res;
	}
	
	@Override
	protected Command getConfigureCommand(ConfigureRequest req) {
		return null;
	}

	@Override
	protected Command getCreateRelationshipCommand(CreateRelationshipRequest req) {
		return null;
	}

	@Override
	protected Command getCreateCommand(CreateElementRequest req) {
		return null;
	}

	@Override
	protected Command getSetCommand(SetRequest req) {
		return null;
	}

	@Override
	protected Command getDestroyElementCommand(DestroyElementRequest req) {
		EObject obj = req.getElementToDestroy();
		if (obj instanceof Lifeline) {
			InteractionGraph graph = InteractionGraphRequestHelper.getInteractionGraph(req);
			InteractionGraphCommand cmd = new InteractionGraphCommand(getEditingDomain(), "Delete Lifeline", graph, null);
			cmd.deleteLifeline((Lifeline)obj);
			return new ICommandProxy(cmd);
		} else if (obj instanceof Message) {
			InteractionGraph graph = InteractionGraphRequestHelper.getInteractionGraph(req);
			InteractionGraphCommand cmd = new InteractionGraphCommand(getEditingDomain(), "Delete Lifeline", graph, null);
			Message msg = (Message)obj;
			cmd.deleteMessage(msg);
			return new ICommandProxy(cmd);			
		} else if (obj instanceof ExecutionSpecification) {
			InteractionGraph graph = InteractionGraphRequestHelper.getInteractionGraph(req);
			InteractionGraphCommand cmd = new InteractionGraphCommand(getEditingDomain(), "Delete Lifeline", graph, null);
			cmd.deleteExecutionSpecification((ExecutionSpecification)obj);
			return new ICommandProxy(cmd);			
		} else if (obj instanceof InteractionUse) {
			InteractionGraph graph = InteractionGraphRequestHelper.getInteractionGraph(req);
			InteractionGraphCommand cmd = new InteractionGraphCommand(getEditingDomain(), "Delete Lifeline", graph, null);
			cmd.deleteInteractionUse((InteractionUse)obj);
			return new ICommandProxy(cmd);			
		}
		return null;
	}

	@Override
	protected Command getDestroyReferenceCommand(DestroyReferenceRequest req) {
		return null;
	}

	@Override
	protected Command getDestroyReferenceCommand(DestroyReferenceRequest req, Object context) {
		return null;
	}

	@Override
	protected Command getDuplicateCommand(DuplicateElementsRequest req) {
		return null;
	}

	@Override
	protected Command getMoveCommand(MoveRequest req) {
		return null;
	}

	@Override
	protected Command getReorientReferenceRelationshipCommand(ReorientReferenceRelationshipRequest req) {
		return null;
	}

	@Override
	protected Command getReorientRelationshipCommand(ReorientRelationshipRequest req) {
		return null;
	}
	
	protected Command getEditContextCommand(GetEditContextRequest req) {
		return null;
	}
}
