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
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.UMLPackage;

public class MessageHelperAdvice extends org.eclipse.papyrus.uml.service.types.helper.advice.MessageHelperAdvice {
	@Override
	protected ICommand getBeforeConfigureCommand(final ConfigureRequest request) {
		final Element source = getSource(request);
		final Element target = getTarget(request);
		final Message message = (Message)request.getElementToConfigure();
		ICommand cmd = super.getBeforeConfigureCommand(request);
		ICommand srcCmd = getCreateGateMessageEndCommand(message, true, source);
		if (srcCmd != null)
			cmd = cmd.compose(srcCmd);
		ICommand trgCmd = getCreateGateMessageEndCommand(message, false, target);		
		if (trgCmd != null)
			cmd = cmd.compose(trgCmd);
		
		return cmd;
	}

	protected ICommand getConfigureCommand(ConfigureRequest request) {
		return new ConfigureMessageEventCommand_UndoBugWorkAround(request);
	}


	protected ICommand getCreateGateMessageEndCommand(Message msg, boolean source, Element owner) {
		if (owner instanceof Interaction) {
			return new CreateGateMessageEndCommand(msg, source, new CreateElementRequest(owner,UMLElementTypes.GATE,UMLPackage.Literals.INTERACTION__FORMAL_GATE));
		} else if (owner instanceof InteractionUse) {
			return new CreateGateMessageEndCommand(msg, source, new CreateElementRequest(owner,UMLElementTypes.GATE,UMLPackage.Literals.INTERACTION_USE__ACTUAL_GATE));
		} 
		return null;
	}
	
	protected boolean isValidConfigureRequest(ConfigureRequest request) {
		ConfigureRequest req = request;
		boolean valid = true;
		if ((getSource(req) == null) || (getTarget(req) == null)) {
			valid = false;
		} else if ((!(getSource(req) instanceof Lifeline)) && (!(getSource(req) instanceof Interaction)) && (!(getSource(req) instanceof Gate)) && 
				   (!(getSource(req) instanceof ExecutionSpecification)) && (!(getSource(req) instanceof InteractionUse))) {
			valid = false;
		} else if ((!(getTarget(req) instanceof Lifeline)) && (!(getTarget(req) instanceof Interaction)) && (!(getTarget(req) instanceof Gate)) && 
				   (!(getTarget(req) instanceof ExecutionSpecification)) && (!(getTarget(req) instanceof InteractionUse))) {
			valid = false;
		}

		return valid;
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
