/*****************************************************************************
 * Copyright (c) 2011 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *		CEA LIST - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.core.util.StringStatics;
import org.eclipse.gmf.runtime.diagram.core.commands.SetConnectionAnchorsCommand;
import org.eclipse.gmf.runtime.diagram.core.commands.SetConnectionEndsCommand;
import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
import org.eclipse.gmf.runtime.diagram.ui.commands.CreateCommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.internal.commands.SetConnectionBendpointsCommand;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.CreateViewCommand;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.FixEdgeAnchorAfterCreationCommand;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.FixEdgeAnchorsDeferredCommand;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.ServiceUtilsForEditPart;
import org.eclipse.papyrus.infra.services.edit.utils.RequestParameterConstants;


/**
 * Default graphical node edit policy replacement used to replace {@link CreateCommand} by {@link CreateViewCommand},
 * different implementation of the canExecute() method.
 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=346739.
 */
public class DefaultGraphicalNodeEditPolicy extends GraphicalNodeEditPolicy {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Command getConnectionAndRelationshipCompleteCommand(CreateConnectionViewAndElementRequest request) {

		// Add parameter (source and target view to the CreateRelationshipRequest
		CreateElementRequestAdapter requestAdapter = request.getConnectionViewAndElementDescriptor().getCreateElementRequestAdapter();
		CreateRelationshipRequest createElementRequest = (CreateRelationshipRequest) requestAdapter.getAdapter(CreateRelationshipRequest.class);

		View sourceView = (View) request.getSourceEditPart().getModel();
		createElementRequest.setParameter(RequestParameterConstants.EDGE_CREATE_REQUEST_SOURCE_VIEW, sourceView);

		View targetView = (View) request.getTargetEditPart().getModel();
		createElementRequest.setParameter(RequestParameterConstants.EDGE_CREATE_REQUEST_TARGET_VIEW, targetView);
		if (request.getLocation() != null) {
			createElementRequest.setParameter(RequestParameterConstants.EDGE_TARGET_POINT, request.getLocation().getCopy());
		} else {
			createElementRequest.setParameter(RequestParameterConstants.EDGE_TARGET_POINT, null);

		}
		// see bug 430702: [Diagram] Moving source of a link moves the target too, we need to store in the parameters :
		// - the source point
		// - the target point
		// - the source figure
		// - the target figure
		final EditPart sourceEP = request.getSourceEditPart();
		if (sourceEP instanceof GraphicalEditPart) {
			createElementRequest.setParameter(RequestParameterConstants.EDGE_SOURCE_FIGURE, ((GraphicalEditPart) sourceEP).getFigure());
		}
		final EditPart targetEP = request.getTargetEditPart();
		if (targetEP instanceof GraphicalEditPart) {
			createElementRequest.setParameter(RequestParameterConstants.EDGE_TARGET_FIGURE, ((GraphicalEditPart) targetEP).getFigure());
		}

		final TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();
		final Command defaultCommand = super.getConnectionAndRelationshipCompleteCommand(request);
		final ICommand afterConnectionCompleteCommand = getAfterConnectionCompleteCommand(request, editingDomain);

		if (defaultCommand != null && defaultCommand.canExecute()) {
			final CompoundCommand cc = new CompoundCommand("ConnectionAndRelationshipCompleteCommand");//$NON-NLS-1$
			cc.add(defaultCommand);
			if (afterConnectionCompleteCommand != null && afterConnectionCompleteCommand.canExecute()) {
				cc.add(new ICommandProxy(afterConnectionCompleteCommand));
			}
			return cc;
		}
		return defaultCommand;
	}


	/**
	 * Method that allow to add a command that will be executed after the connection creation
	 * Exemple: Fixing the Edge Anchor
	 * 
	 * @param request
	 *            Initial Connection creation Request
	 * @param editingDomain
	 *            The editing Domain
	 * 
	 * @return The Command to be executed after the creation of the connection View and element
	 */
	protected ICommand getAfterConnectionCompleteCommand(CreateConnectionViewAndElementRequest request, final TransactionalEditingDomain editingDomain) {
		final ICommand fixAnchor = new FixEdgeAnchorAfterCreationCommand(editingDomain, request);
		return fixAnchor;

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Command getConnectionAndRelationshipCreateCommand(CreateConnectionViewAndElementRequest request) {
		// Add parameter (source view to the CreateRelationshipRequest)
		CreateElementRequestAdapter requestAdapter = request.getConnectionViewAndElementDescriptor().getCreateElementRequestAdapter();
		if (requestAdapter != null) {
			CreateRelationshipRequest createElementRequest = (CreateRelationshipRequest) requestAdapter.getAdapter(CreateRelationshipRequest.class);

			if (createElementRequest != null && request.getTargetEditPart() != null) {
				View sourceView = (View) request.getTargetEditPart().getModel();
				if (sourceView != null) {
					createElementRequest.setParameter(RequestParameterConstants.EDGE_CREATE_REQUEST_SOURCE_VIEW, sourceView);
				}
			}

		}


		return super.getConnectionAndRelationshipCreateCommand(request);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("restriction")
	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		if (!(request instanceof CreateConnectionViewRequest)) {
			return null;
		}
		CreateConnectionViewRequest req = (CreateConnectionViewRequest) request;


		CompositeCommand cc = new CompositeCommand(DiagramUIMessages.Commands_CreateCommand_Connection_Label);
		Diagram diagramView = ((View) getHost().getModel()).getDiagram();

		// TransactionalEditingDomain editingDomain = getEditingDomain();
		TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();
		CreateCommand createCommand = new CreateViewCommand(editingDomain, req.getConnectionViewDescriptor(), diagramView.getDiagram());
		//
		setViewAdapter((IAdaptable) createCommand.getCommandResult().getReturnValue());

		// see bug 430702: [Diagram] Moving source of a link moves the target too, we need to store the source point to fix this bug.
		@SuppressWarnings("unchecked")
		Map<Object, Object> parameters = req.getExtendedData();
		if (request.getLocation() != null) {
			parameters.put(RequestParameterConstants.EDGE_SOURCE_POINT, request.getLocation().getCopy());
		} else {
			parameters.put(RequestParameterConstants.EDGE_SOURCE_POINT, null);
		}

		SetConnectionEndsCommand sceCommand = new SetConnectionEndsCommand(editingDomain, StringStatics.BLANK);
		sceCommand.setEdgeAdaptor(getViewAdapter());
		sceCommand.setNewSourceAdaptor(new EObjectAdapter(getView()));
		ConnectionAnchor sourceAnchor = getConnectableEditPart().getSourceConnectionAnchor(request);
		SetConnectionAnchorsCommand scaCommand = new SetConnectionAnchorsCommand(editingDomain, StringStatics.BLANK);
		scaCommand.setEdgeAdaptor(getViewAdapter());
		scaCommand.setNewSourceTerminal(getConnectableEditPart().mapConnectionAnchorToTerminal(sourceAnchor));
		SetConnectionBendpointsCommand sbbCommand = new SetConnectionBendpointsCommand(editingDomain);
		sbbCommand.setEdgeAdapter(getViewAdapter());
		cc.compose(createCommand);
		cc.compose(sceCommand);
		cc.compose(scaCommand);
		cc.compose(sbbCommand);
		Command c = new ICommandProxy(cc);
		request.setStartCommand(c);
		return c;
	}

	/**
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.GraphicalNodeEditPolicy#getReconnectSourceCommand(org.eclipse.gef.requests.ReconnectRequest)
	 *
	 * @param request
	 * @return
	 */
	@Override
	protected Command getReconnectSourceCommand(final ReconnectRequest request) {
		final Command reconnectCmd = super.getReconnectSourceCommand(request);

		if (reconnectCmd != null && reconnectCmd.canExecute()) {
			final CompoundCommand cc = new CompoundCommand();
			cc.add(reconnectCmd);
			// see bug 430702: [Diagram] Moving source of a link moves the target too.
			cc.add(new ICommandProxy(new FixEdgeAnchorsDeferredCommand(getEditingDomain(), (IGraphicalEditPart) getHost(), Collections.singleton(request.getConnectionEditPart()))));
			return cc;
		}
		return reconnectCmd;
	}

	/**
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.GraphicalNodeEditPolicy#getReconnectTargetCommand(org.eclipse.gef.requests.ReconnectRequest)
	 *
	 * @param request
	 * @return
	 */
	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		final Command reconnectCmd = super.getReconnectTargetCommand(request);
		if (reconnectCmd != null && reconnectCmd.canExecute()) {
			final CompoundCommand cc = new CompoundCommand();
			cc.add(reconnectCmd);
			// see bug 430702: [Diagram] Moving source of a link moves the target too.
			cc.add(new ICommandProxy(new FixEdgeAnchorsDeferredCommand(getEditingDomain(), (IGraphicalEditPart) getHost(), Collections.singleton(request.getConnectionEditPart()))));
			return cc;
		}
		return reconnectCmd;
	}



	/**
	 *
	 * @return
	 * 		the editing domain to use
	 */
	protected final TransactionalEditingDomain getEditingDomain() {
		try {
			return ServiceUtilsForEditPart.getInstance().getTransactionalEditingDomain(getHost());
		} catch (ServiceException e) {
			Activator.log.error(e);
		}
		return null;

	}
}
