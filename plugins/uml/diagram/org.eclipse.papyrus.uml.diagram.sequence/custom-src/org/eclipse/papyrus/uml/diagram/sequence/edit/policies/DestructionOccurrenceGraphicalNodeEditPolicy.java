/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultGraphicalNodeEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.figures.DurationLinkFigure;
import org.eclipse.papyrus.uml.diagram.sequence.util.DurationLinkUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.GeneralOrderingUtil;

/**
 * An extended {@link DefaultGraphicalNodeEditPolicy} which supports creation of DurationLinks
 */
public class DestructionOccurrenceGraphicalNodeEditPolicy extends DefaultGraphicalNodeEditPolicy {

	@Override
	protected ICommand getAfterConnectionCompleteCommand(CreateConnectionViewAndElementRequest request, final TransactionalEditingDomain editingDomain) {
		if (DurationLinkUtil.isCreateDurationLink(request) || GeneralOrderingUtil.isCreateGeneralOrderingLink(request)) {
			return null; // Prevent the superclass from "Fixing" the anchors
		}
		return super.getAfterConnectionCompleteCommand(request, editingDomain);
	}

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		// if (DurationLinkUtil.isDurationLink(request)) {
		// // Bug 536639: Forbid reconnect on Duration edit parts
		// return UnexecutableCommand.INSTANCE;
		// }
		return super.getReconnectSourceCommand(request);
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		// if (DurationLinkUtil.isDurationLink(request)) {
		// // Bug 536639: Forbid reconnect on Duration edit parts
		// return UnexecutableCommand.INSTANCE;
		// }
		return super.getReconnectTargetCommand(request);
	}

	@Override
	protected Connection createDummyConnection(Request req) {
		if (req instanceof CreateConnectionRequest && DurationLinkUtil.isCreateDurationLink((CreateConnectionRequest) req)) {
			return new DurationLinkFigure();
		}
		return new PolylineConnection();
	}


}
