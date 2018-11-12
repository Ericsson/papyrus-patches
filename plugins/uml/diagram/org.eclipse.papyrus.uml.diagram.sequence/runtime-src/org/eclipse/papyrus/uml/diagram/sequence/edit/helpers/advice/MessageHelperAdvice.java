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

package org.eclipse.papyrus.uml.diagram.sequence.edit.helpers.advice;

import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.type.core.edithelper.AbstractEditHelperAdvice;
import org.eclipse.gmf.runtime.emf.type.core.requests.ConfigureRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;
import org.eclipse.uml2.uml.Message;

public class MessageHelperAdvice extends AbstractEditHelperAdvice {
	@Override
	protected ICommand getBeforeConfigureCommand(final ConfigureRequest request) {
		@SuppressWarnings("unused")
		Message msg = (Message) request.getElementToConfigure();
		// InteractionGraph graph = InteractionGraphFactory.getInstance().createInteractionGraph(interaction, diagram, null);
		// graph.createNodeFor(msg);
		msg = null;
		return null;// super.getBeforeConfigureCommand(request);

	}

	@Override
	public void configureRequest(IEditCommandRequest request) {
		if (request instanceof ConfigureRequest || request instanceof CreateRelationshipRequest) {
			request.setParameter(IEditCommandRequest.REPLACE_DEFAULT_COMMAND, Boolean.TRUE);
		}
		super.configureRequest(request);

	}

	@Override
	public boolean approveRequest(IEditCommandRequest request) {

		if (request instanceof ConfigureRequest || request instanceof CreateRequest) {
			if (request.getElementsToEdit().size() == 0) {
				return false;
			}

			if (!(request.getElementsToEdit().get(0) instanceof Message)) {
				return false;
			}
		}

		return super.approveRequest(request);
	}

}
