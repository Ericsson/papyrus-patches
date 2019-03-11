/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.util.StringStatics;
import org.eclipse.gmf.runtime.diagram.core.commands.SetConnectionAnchorsCommand;
import org.eclipse.gmf.runtime.diagram.core.commands.SetConnectionEndsCommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.INodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.Routing;
import org.eclipse.gmf.runtime.notation.RoutingStyle;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.command.CustomSetConnectionAnchorsCommand;

/**
 * This class allows to redefine the reconnect source and target commands to use the {@link CustomSetConnectionAnchorsCommand} instead of {@link SetConnectionAnchorsCommand} because the custom manage the calculation during the execution and not in the
 * initialisation.
 * @since 5.0
 */
public class CustomGraphicalNodeEditPolicy extends GraphicalNodeEditPolicy {

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.GraphicalNodeEditPolicy#getReconnectSourceCommand(org.eclipse.gef.requests.ReconnectRequest)
	 */
	@Override
	protected Command getReconnectSourceCommand(final ReconnectRequest request) {
		final INodeEditPart node = getConnectableEditPart();
		if (node == null) {
			return null;
		}

		final TransactionalEditingDomain editingDomain = getEditingDomain();

		final SetConnectionEndsCommand sceCommand = new SetConnectionEndsCommand(editingDomain, StringStatics.BLANK);
		sceCommand.setEdgeAdaptor(new EObjectAdapter((View) request.getConnectionEditPart().getModel()));
		sceCommand.setNewSourceAdaptor(new EObjectAdapter((View) node.getModel()));
		final CustomSetConnectionAnchorsCommand scaCommand = new CustomSetConnectionAnchorsCommand(editingDomain, StringStatics.BLANK);
		scaCommand.setEdgeAdaptor(new EObjectAdapter((View) request.getConnectionEditPart().getModel()));
		scaCommand.setNode(node);
		scaCommand.setSourceReconnectRequest(request);
		scaCommand.setSourceSet(true);
		final CompositeCommand cc = new CompositeCommand(DiagramUIMessages.Commands_SetConnectionEndsCommand_Source);
		cc.compose(sceCommand);
		cc.compose(scaCommand);
		return new ICommandProxy(cc);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.GraphicalNodeEditPolicy#getReconnectTargetCommand(org.eclipse.gef.requests.ReconnectRequest)
	 */
	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		final INodeEditPart node = getConnectableEditPart();
		if (node == null) {
			return null;
		}

		final TransactionalEditingDomain editingDomain = getEditingDomain();

		final INodeEditPart targetEP = getConnectionCompleteEditPart(request);
		if (targetEP == null) {
			return null;
		}
		final SetConnectionEndsCommand sceCommand = new SetConnectionEndsCommand(editingDomain, StringStatics.BLANK);
		sceCommand.setEdgeAdaptor(new EObjectAdapter((EObject) request.getConnectionEditPart().getModel()));
		sceCommand.setNewTargetAdaptor(targetEP);
		final CustomSetConnectionAnchorsCommand scaCommand = new CustomSetConnectionAnchorsCommand(editingDomain, StringStatics.BLANK);
		scaCommand.setEdgeAdaptor(new EObjectAdapter((EObject) request.getConnectionEditPart().getModel()));
		scaCommand.setNode(targetEP);
		scaCommand.setTargetReconnectRequest(request);
		scaCommand.setTargetSet(true);
		final CompositeCommand cc = new CompositeCommand(DiagramUIMessages.Commands_SetConnectionEndsCommand_Target);
		cc.compose(sceCommand);
		cc.compose(scaCommand);
		Command cmd = new ICommandProxy(cc);
		EditPart cep = request.getConnectionEditPart();
		RoutingStyle style = (RoutingStyle) ((View) cep.getModel()).getStyle(NotationPackage.eINSTANCE.getRoutingStyle());
		Routing currentRouter = Routing.MANUAL_LITERAL;
		if (style != null) {
			currentRouter = style.getRouting();
		}
		Command cmdRouter = getRoutingAdjustment(request.getConnectionEditPart(), getSemanticHint(request), currentRouter, request.getTarget());
		if (cmdRouter != null) {
			cmd = cmd == null ? cmdRouter : cmd.chain(cmdRouter);
			// reset the bendpoints

			final CustomSetConnectionBendpointsCommand csbbCommand = new CustomSetConnectionBendpointsCommand(editingDomain);
			csbbCommand.setNode(node);
			csbbCommand.setEdgeAdaptor(request.getConnectionEditPart());
			csbbCommand.setRequest(request);
			final Command cmdBP = new ICommandProxy(csbbCommand);
			if (cmdBP != null) {
				cmd = cmd == null ? cmdBP : cmd.chain(cmdBP);
			}
		}
		return cmd;
	}

	/**
	 * This allows to get the editing domain.
	 *
	 * @return The editing domain.
	 */
	private TransactionalEditingDomain getEditingDomain() {
		return ((IGraphicalEditPart) getHost()).getEditingDomain();
	}

	/**
	 * Add a command of reconnection of the given connection editpart at the location.
	 *
	 * @param hostEditpart
	 *            the current editpart that is the origin of this impact
	 * @param connectionEditPart
	 *            the given editpart to move
	 * @param location
	 *            the next location of the anchor
	 * @param senderList
	 *            the list of editpart that are origin of this request
	 * @param reconnectType
	 *            the type of the reconnection see {@link RequestConstants}
	 * @return return a request of reconnection
	 */
	protected static ReconnectRequest copyReconnectRequest(final ReconnectRequest initialRequest) {
		final ReconnectRequest reconnectRequest = new ReconnectRequest();
		reconnectRequest.setConnectionEditPart(initialRequest.getConnectionEditPart());
		reconnectRequest.setLocation(initialRequest.getLocation().getCopy());
		reconnectRequest.setType(initialRequest.getType());
		reconnectRequest.setTargetEditPart(initialRequest.getTarget());
		return reconnectRequest;
	}

}
