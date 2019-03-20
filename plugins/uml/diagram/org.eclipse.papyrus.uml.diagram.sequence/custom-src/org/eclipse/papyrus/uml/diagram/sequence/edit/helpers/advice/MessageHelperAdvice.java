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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.type.core.commands.CreateElementCommand;
import org.eclipse.gmf.runtime.emf.type.core.edithelper.AbstractEditHelperAdvice;
import org.eclipse.gmf.runtime.emf.type.core.requests.ConfigureRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.SetRequest;
import org.eclipse.papyrus.infra.services.edit.service.ElementEditServiceUtils;
import org.eclipse.papyrus.infra.services.edit.service.IElementEditService;
import org.eclipse.papyrus.uml.service.types.element.UMLElementTypes;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.UMLPackage;

public class MessageHelperAdvice extends AbstractEditHelperAdvice {
	@Override
	protected ICommand getBeforeConfigureCommand(final ConfigureRequest request) {
		final Element source = getSource(request);
		final Element target = getTarget(request);
		final Message message = (Message)request.getElementToConfigure();
		
		ICommand cmd = getCreateGateMessageEndCommand(message, true, source);
		ICommand trgCmd = getCreateGateMessageEndCommand(message, false, target);
		if (cmd != null) {
			if (trgCmd != null)
				return cmd.compose(trgCmd);
			return cmd;
		}
		
		return trgCmd;
	}

	protected ICommand getCreateGateMessageEndCommand(Message msg, boolean source, Element owner) {
		if (owner instanceof Interaction) {
			return new CreateGateMessageEndCommand(msg, source, new CreateElementRequest(owner,UMLElementTypes.GATE,UMLPackage.Literals.INTERACTION__FORMAL_GATE));
		} else if (owner instanceof InteractionUse) {
			return new CreateGateMessageEndCommand(msg, source, new CreateElementRequest(owner,UMLElementTypes.GATE,UMLPackage.Literals.INTERACTION_USE__ACTUAL_GATE));
		}
		return null;
	}
	
	protected Element getSource(ConfigureRequest req) {
		Element result = null;
		Object paramObject = req.getParameter(CreateRelationshipRequest.SOURCE);
		if (paramObject instanceof Element) {
			result = (Element) paramObject;
		}

		return result;
	}

	protected Element getTarget(ConfigureRequest req) {
		Element result = null;
		Object paramObject = req.getParameter(CreateRelationshipRequest.TARGET);
		if (paramObject instanceof Element) {
			result = (Element) paramObject;
		}

		return result;
	}

	private static class CreateGateMessageEndCommand extends CreateElementCommand {
		public CreateGateMessageEndCommand(Message msg, boolean source, CreateElementRequest request) {
			super(request);
			this.source = source;
			this.message = msg;
		}

		@Override
		protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			CommandResult res = super.doExecuteWithResult(monitor, info);
			if (res.getReturnValue() instanceof Gate) {
				Gate gate = (Gate) res.getReturnValue();
				getSetMessageEndCommand(gate, message).execute(monitor,info);
				if (source)
					message.setSendEvent(gate);
				else
					message.setReceiveEvent(gate);
				
				getSetMessageEndCommand(gate, message).execute(monitor,info);
			}
			return res;
		}
		
		protected ICommand getSetMessageEndCommand(MessageEnd msgEnd, Message msg) {
			ICommand semanticCommand = null;

			IElementEditService commandService = ElementEditServiceUtils.getCommandProvider(msgEnd);
			SetRequest setMsgEndRequest = new SetRequest(msgEnd, UMLPackage.eINSTANCE.getMessageEnd_Message(), message);

			if (commandService != null) {
				semanticCommand = commandService.getEditCommand(setMsgEndRequest);
			}

			return semanticCommand;
		}

		private Message message;
		private boolean source;
	}

}
