/*****************************************************************************
 * Copyright (c) 2019 CEA LIST and others.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.commands.ConfigureElementCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.ConfigureRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.papyrus.infra.services.edit.service.ElementEditServiceUtils;
import org.eclipse.papyrus.infra.services.edit.service.IElementEditService;
import org.eclipse.papyrus.uml.service.types.element.UMLElementTypes;
import org.eclipse.papyrus.uml.service.types.utils.ElementUtil;
import org.eclipse.papyrus.uml.service.types.utils.SequenceRequestConstant;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.UMLFactory;

/**
 * @author ETXACAM
 *
 */
public class ConfigureMessageEventCommand_UndoBugWorkAround extends ConfigureElementCommand {

	/**
	 * The created message ends (needed for the undo).
	 */
	private List<MessageOccurrenceSpecification> createdMessageEnds;

	/**
	 * The created messages ends covering lifelines (needed for the undo).
	 */
	private Map<Lifeline, List<MessageOccurrenceSpecification>> coveredLifelines;

	/**
	 * The initial message before its modification (needed for the undo).
	 */
	private Message oldMessage;

	/**
	 * The possible element to replace by the message sent (needed for the undo).
	 */
	private ExecutionOccurrenceSpecification oldToReplacebyMessageSent;

	/**
	 * The possible element to replace by the message receive (needed for the undo).
	 */
	private ExecutionOccurrenceSpecification oldToReplacebyMessageReceive;

	/**
	 * The list of element destroyed by commands (needed for the undo).
	 */
	private List<ICommand> destroyedElementsCommands;

	/**
	 * The initial request.
	 */
	private ConfigureRequest request;


	/**
	 * Constructor.
	 *
	 * @param request
	 *            The initial request.
	 */
	public ConfigureMessageEventCommand_UndoBugWorkAround(final ConfigureRequest request) {
		super(request);
		this.request = request;
	}

	/**
	 * Create a MessageEnd
	 *
	 * @param message
	 *            The message that reference the message end always !=null.
	 * @param lifeline
	 *            The lifeLine where is set the message end always !=null.
	 * @param previous
	 *            The element to detect where add the covering of lifeline.
	 * @since 3.0
	 */
	public MessageOccurrenceSpecification createMessageEnd(Message message, Lifeline lifeline, final MessageEnd previous) {
		final MessageOccurrenceSpecification messageOccurrenceSpecification = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
		if (previous == null) {
			messageOccurrenceSpecification.setCovered(lifeline);			
		} else {
			final int index = lifeline.getCoveredBys().indexOf(previous) + 1;
			lifeline.getCoveredBys().add(index, messageOccurrenceSpecification);
			
		}

		// Store the added covered to manage the undo if needed
		if (coveredLifelines == null) {
			coveredLifelines = new HashMap<>();
		}
		
		List<MessageOccurrenceSpecification> newCoveredBy = coveredLifelines.get(lifeline);
		if (newCoveredBy == null) {
			newCoveredBy = new ArrayList<>();
			coveredLifelines.put(lifeline, newCoveredBy);
		}
		
		newCoveredBy.add(messageOccurrenceSpecification);
		messageOccurrenceSpecification.setMessage(message);
		((Interaction) message.getOwner()).getFragments().add(messageOccurrenceSpecification);
		return messageOccurrenceSpecification;
	}

	/**
	 * Create a MessageEnd
	 *
	 * @param message
	 *            The message that reference the message end always !=null.
	 * @param lifeline
	 *            The lifeLine where is set the message end ,always !=null.
	 * @since 3.0
	 */
	public DestructionOccurrenceSpecification createDestroyMessageEnd(final Message message, final Lifeline lifeline) {
		final DestructionOccurrenceSpecification messageOccurrenceSpecification = UMLFactory.eINSTANCE.createDestructionOccurrenceSpecification();
		messageOccurrenceSpecification.setCovered(lifeline);
		messageOccurrenceSpecification.setMessage(message);
		((Interaction) message.getOwner()).getFragments().add(messageOccurrenceSpecification);
		return messageOccurrenceSpecification;
	}

	/**
	 * This method provides the source type provided as {@link ConfigureRequest} parameter.
	 *
	 * @param req
	 *            The configure request.
	 * @return The target role.
	 * @since 3.0
	 */
	protected Element getSource(final ConfigureRequest req) {
		Element result = null;
		final Object paramObject = req.getParameter(CreateRelationshipRequest.SOURCE);
		if (paramObject instanceof Element) {
			result = (Element) paramObject;
		}

		return result;
	}

	/**
	 * This method provides the target type provided as {@link ConfigureRequest} parameter.
	 *
	 * @param req
	 *            The configure request.
	 * @return The target role.
	 * @since 3.0
	 */
	protected Element getTarget(final ConfigureRequest req) {
		Element result = null;
		final Object paramObject = req.getParameter(CreateRelationshipRequest.TARGET);
		if (paramObject instanceof Element) {
			result = (Element) paramObject;
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand#doExecuteWithResult(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	protected CommandResult doExecuteWithResult(final IProgressMonitor progressMonitor, final IAdaptable info) throws ExecutionException {
		final Message message = (Message) request.getElementToConfigure();

		// copy the message for the undo data
		oldMessage = EcoreUtil.copy(message);

		final Element source = getSource(request);
		final Element target = getTarget(request);
		final MessageEnd previousSentEvent = (MessageEnd) request.getParameters().get(SequenceRequestConstant.PREVIOUS_EVENT);
		final MessageEnd previousReceiveEvent = (MessageEnd) request.getParameters().get(SequenceRequestConstant.SECOND_PREVIOUS_EVENT);
		final ExecutionOccurrenceSpecification toReplacebyMessageSent = (ExecutionOccurrenceSpecification) request.getParameters().get(SequenceRequestConstant.MESSAGE_SENTEVENT_REPLACE_EXECUTIONEVENT);
		final ExecutionOccurrenceSpecification toReplacebyMessageReceive = (ExecutionOccurrenceSpecification) request.getParameters().get(SequenceRequestConstant.MESSAGE_RECEIVEEVENT_REPLACE_EXECUTIONEVENT);
		oldToReplacebyMessageSent = toReplacebyMessageSent;
		oldToReplacebyMessageReceive = toReplacebyMessageReceive;

		// Initialise the created message ends list
		createdMessageEnds = new ArrayList<>(2);

		IElementType elementType = request.getTypeToConfigure();
		if (ElementUtil.isTypeOf(elementType, UMLElementTypes.COMPLETE_ASYNCH_CALL)) {
			createdMessageEnds.add(createSendEvent(message, source, previousSentEvent));
			createdMessageEnds.add(createReceiveEvent(message, target, previousReceiveEvent));
			message.setMessageSort(MessageSort.ASYNCH_CALL_LITERAL);
		} else if (ElementUtil.isTypeOf(elementType, UMLElementTypes.COMPLETE_ASYNCH_SIGNAL)) {
			createdMessageEnds.add(createSendEvent(message, source, previousSentEvent));
			createdMessageEnds.add(createReceiveEvent(message, target, previousReceiveEvent));
			message.setMessageSort(MessageSort.ASYNCH_SIGNAL_LITERAL);
		} else if (ElementUtil.isTypeOf(elementType, UMLElementTypes.COMPLETE_CREATE_MESSAGE)) {
			createdMessageEnds.add(createSendEvent(message, source, previousSentEvent));
			createdMessageEnds.add(createReceiveEvent(message, target, previousReceiveEvent));
			message.setMessageSort(MessageSort.CREATE_MESSAGE_LITERAL);
		} else if (ElementUtil.isTypeOf(elementType, UMLElementTypes.COMPLETE_DELETE_MESSAGE)) {
			createdMessageEnds.add(createSendEvent(message, source, previousSentEvent));
			createdMessageEnds.add(createDestroyReceiveEvent(message, target));
			message.setMessageSort(MessageSort.DELETE_MESSAGE_LITERAL);
		} else if (ElementUtil.isTypeOf(elementType, UMLElementTypes.COMPLETE_REPLY)) {
			createdMessageEnds.add(createSendEvent(message, source, previousSentEvent));
			createdMessageEnds.add(createReceiveEvent(message, target, previousReceiveEvent));
			message.setMessageSort(MessageSort.REPLY_LITERAL);
		} else if (ElementUtil.isTypeOf(elementType, UMLElementTypes.COMPLETE_SYNCH_CALL)) {
			createdMessageEnds.add(createSendEvent(message, source, previousSentEvent));
			createdMessageEnds.add(createReceiveEvent(message, target, previousReceiveEvent));
			message.setMessageSort(MessageSort.SYNCH_CALL_LITERAL);
		} else if (ElementUtil.isTypeOf(elementType, UMLElementTypes.FOUND_ASYNCH_CALL)) {
			createdMessageEnds.add(createReceiveEvent(message, target, previousReceiveEvent));
			message.setMessageSort(MessageSort.ASYNCH_CALL_LITERAL);
		} else if (ElementUtil.isTypeOf(elementType, UMLElementTypes.FOUND_ASYNCH_SIGNAL)) {
			createdMessageEnds.add(createReceiveEvent(message, target, previousReceiveEvent));
			message.setMessageSort(MessageSort.ASYNCH_SIGNAL_LITERAL);
		} else if (ElementUtil.isTypeOf(elementType, UMLElementTypes.FOUND_CREATE_MESSAGE)) {
			createdMessageEnds.add(createReceiveEvent(message, target, previousReceiveEvent));
			message.setMessageSort(MessageSort.CREATE_MESSAGE_LITERAL);
		} else if (ElementUtil.isTypeOf(elementType, UMLElementTypes.FOUND_DELETE_MESSAGE)) {
			createdMessageEnds.add(createDestroyReceiveEvent(message, target));
			message.setMessageSort(MessageSort.DELETE_MESSAGE_LITERAL);
		} else if (ElementUtil.isTypeOf(elementType, UMLElementTypes.FOUND_REPLY)) {
			createdMessageEnds.add(createReceiveEvent(message, target, previousReceiveEvent));
			message.setMessageSort(MessageSort.REPLY_LITERAL);
		} else if (ElementUtil.isTypeOf(elementType, UMLElementTypes.LOST_ASYNCH_CALL)) {
			createdMessageEnds.add(createSendEvent(message, source, previousSentEvent));
			message.setMessageSort(MessageSort.ASYNCH_CALL_LITERAL);
		} else if (ElementUtil.isTypeOf(elementType, UMLElementTypes.LOST_ASYNCH_SIGNAL)) {
			createdMessageEnds.add(createSendEvent(message, source, previousSentEvent));
			message.setMessageSort(MessageSort.ASYNCH_SIGNAL_LITERAL);
		} else if (ElementUtil.isTypeOf(elementType, UMLElementTypes.LOST_CREATE_MESSAGE)) {
			createdMessageEnds.add(createSendEvent(message, source, previousSentEvent));
			message.setMessageSort(MessageSort.CREATE_MESSAGE_LITERAL);
		} else if (ElementUtil.isTypeOf(elementType, UMLElementTypes.LOST_DELETE_MESSAGE)) {
			createdMessageEnds.add(createSendEvent(message, source, previousSentEvent));
			message.setMessageSort(MessageSort.DELETE_MESSAGE_LITERAL);
		} else if (ElementUtil.isTypeOf(elementType, UMLElementTypes.LOST_REPLY)) {
			createdMessageEnds.add(createSendEvent(message, source, previousSentEvent));
			message.setMessageSort(MessageSort.REPLY_LITERAL);
		}

		// an occurence spec must replaced?
		if (toReplacebyMessageSent != null) {
			if (toReplacebyMessageSent.getExecution() != null) {
				// by the sent event of the message?
				// this is the start?
				if (toReplacebyMessageSent.getExecution().getStart().equals(toReplacebyMessageSent)) {
					toReplacebyMessageSent.getExecution().setStart((OccurrenceSpecification) message.getSendEvent());
				} else {
					// this is the finish
					toReplacebyMessageSent.getExecution().setFinish((OccurrenceSpecification) message.getSendEvent());
				}
			}
			// the occurennce spec must disapear!
			if (toReplacebyMessageSent.getOwner() != null) {
				final IElementEditService provider = ElementEditServiceUtils.getCommandProvider(toReplacebyMessageSent);
				if (provider != null) {
					final DestroyElementRequest destroyRequest = new DestroyElementRequest(toReplacebyMessageSent, false);
					final ICommand destroyCommand = provider.getEditCommand(destroyRequest);
					destroyCommand.execute(new NullProgressMonitor(), null);

					// Save the destroyed commands
					if (null == destroyedElementsCommands) {
						destroyedElementsCommands = new ArrayList<>();
					}
					destroyedElementsCommands.add(destroyCommand);
				}
			}
		}
		if (toReplacebyMessageReceive != null) {
			// replace by the receive message
			if (toReplacebyMessageReceive.getExecution() != null) {
				// this is the start?
				if (toReplacebyMessageReceive.getExecution().getStart().equals(toReplacebyMessageReceive)) {
					toReplacebyMessageReceive.getExecution().setStart((OccurrenceSpecification) message.getReceiveEvent());
				} else {
					// this is the finish
					toReplacebyMessageReceive.getExecution().setFinish((OccurrenceSpecification) message.getReceiveEvent());
				}
			}
			// the occurence spec must be deleted
			if (toReplacebyMessageReceive.getOwner() != null) {
				final IElementEditService provider = ElementEditServiceUtils.getCommandProvider(toReplacebyMessageReceive);
				if (provider != null) {
					final DestroyElementRequest destroyRequest = new DestroyElementRequest(toReplacebyMessageReceive, false);
					final ICommand destroyCommand = provider.getEditCommand(destroyRequest);
					destroyCommand.execute(new NullProgressMonitor(), null);

					// Save the destroyed commands
					if (null == destroyedElementsCommands) {
						destroyedElementsCommands = new ArrayList<>();
					}
					destroyedElementsCommands.add(destroyCommand);
				}
			}
		}
		return CommandResult.newOKCommandResult(message);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand#doUndo(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 *//*
	@Override
	protected IStatus doUndo(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {
		final Message message = (Message) request.getElementToConfigure();
		final ExecutionOccurrenceSpecification toReplacebyMessageSent = (ExecutionOccurrenceSpecification) request.getParameters().get(SequenceRequestConstant.MESSAGE_SENTEVENT_REPLACE_EXECUTIONEVENT);
		final ExecutionOccurrenceSpecification toReplacebyMessageReceive = (ExecutionOccurrenceSpecification) request.getParameters().get(SequenceRequestConstant.MESSAGE_RECEIVEEVENT_REPLACE_EXECUTIONEVENT);

		// Reset the message modified with the old message stored
		message.setMessageSort(oldMessage.getMessageSort());
		message.setSendEvent(oldMessage.getSendEvent());
		message.setReceiveEvent(oldMessage.getReceiveEvent());

		// Remove the created message ends
		if (null != createdMessageEnds && !createdMessageEnds.isEmpty()) {
			for (final MessageEnd createdMessage : createdMessageEnds) {
				if (createdMessage == null)
					continue;
				final EObject container = createdMessage.eContainer();
				if (container instanceof Interaction) {
					((Interaction) container).getFragments().remove(createdMessage);
				} else if (container instanceof InteractionOperand) {
					((InteractionOperand) container).getFragments().remove(createdMessage);
				}
			}
		}

		// Manage the covered life lines if needed
		if (null != coveredLifelines && !coveredLifelines.isEmpty()) {
			for (final Entry<Lifeline, List<MessageOccurrenceSpecification>> entry : coveredLifelines.entrySet()) {
				for (final MessageOccurrenceSpecification addedCoveredBy : entry.getValue()) {
					entry.getKey().getCoveredBys().remove(addedCoveredBy);
				}
			}
		}

		// an occurence spec must replaced?
		if (oldToReplacebyMessageSent != null) {
			if (oldToReplacebyMessageSent.getExecution() != null) {
				// by the sent event of the message?
				// this is the start?
				if (oldToReplacebyMessageSent.getExecution().getStart().equals(oldToReplacebyMessageSent)) {
					toReplacebyMessageSent.getExecution().setStart(oldToReplacebyMessageSent.getExecution().getStart());
				} else {
					// this is the finish
					toReplacebyMessageSent.getExecution().setFinish(oldToReplacebyMessageSent.getExecution().getFinish());
				}
			}
		}
		if (oldToReplacebyMessageReceive != null) {
			// replace by the receive message
			if (oldToReplacebyMessageReceive.getExecution() != null) {
				// this is the start?
				if (oldToReplacebyMessageReceive.getExecution().getStart().equals(oldToReplacebyMessageReceive)) {
					toReplacebyMessageReceive.getExecution().setStart(oldToReplacebyMessageReceive.getExecution().getStart());
				} else {
					// this is the finish
					toReplacebyMessageReceive.getExecution().setFinish(oldToReplacebyMessageReceive.getExecution().getFinish());
				}
			}
		}

		// Undo the destroy elements if there was destroyed elements
		if (null != destroyedElementsCommands && !destroyedElementsCommands.isEmpty()) {
			for (final ICommand destroyCommand : destroyedElementsCommands) {
				destroyCommand.undo(new NullProgressMonitor(), null);
			}
		}

		// Clear the needed data, because this will be re-fill with the redo action
		oldMessage = null;
		oldToReplacebyMessageReceive = null;
		oldToReplacebyMessageSent = null;
		createdMessageEnds = null;
		coveredLifelines = null;
		destroyedElementsCommands = null;

		setResult(new CommandResult(Status.OK_STATUS));
		return Status.OK_STATUS;
	}
*/
	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand#doRedo(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	/*
	@Override
	protected IStatus doRedo(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {
		// Only re-execute the initial process
		final CommandResult result = doExecuteWithResult(monitor, info);

		setResult(result);
		return result.getStatus();
	}
*/
	/**
	 * This allows to create the destroy receive event.
	 *
	 * @param message
	 *            The message.
	 * @param source
	 *            The source lifeline.
	 * @return
	 */
	private DestructionOccurrenceSpecification createDestroyReceiveEvent(final Message message, final Element source) {
		// Create source and target ends
		final DestructionOccurrenceSpecification sendEvent = createDestroyMessageEnd(message, (Lifeline) source);
		sendEvent.setName(message.getName() + "ReceiveDestroyEvent"); //$NON-NLS-1$
		message.setReceiveEvent(sendEvent);
		return sendEvent;
	}


	/**
	 * This allows to create the send event.
	 *
	 * @param message
	 *            The message.
	 * @param source
	 *            The source lifeline, gate or execution specification.
	 * @param previous
	 *            The previous message end.
	 * @return The create message occurrence specification (can be <code>null</code>).
	 */
	private MessageOccurrenceSpecification createSendEvent(final Message message, final Element source, final MessageEnd previous) {
		if (source instanceof Gate) {
			message.setSendEvent((Gate) source);
		} else if (source instanceof ExecutionSpecification) {
			if (((ExecutionSpecification) source).getCovereds().size() > 0) {
				final Lifeline lifeline = ((ExecutionSpecification) source).getCovereds().get(0);
				final MessageOccurrenceSpecification sendEvent = createMessageEnd(message, lifeline, previous);
				sendEvent.setName(message.getName() + "SendEvent"); //$NON-NLS-1$
				message.setSendEvent(sendEvent);
				return sendEvent;
			}
		} else if (source instanceof Lifeline) {
			// Create source and target ends
			final MessageOccurrenceSpecification sendEvent = createMessageEnd(message, (Lifeline) source, previous);
			sendEvent.setName(message.getName() + "SendEvent"); //$NON-NLS-1$
			message.setSendEvent(sendEvent);
			return sendEvent;
		}

		return null;
	}

	/**
	 * This allows to create the receive event.
	 *
	 * @param message
	 *            The message.
	 * @param source
	 *            The source lifeline, gate or execution specification.
	 * @param previous
	 *            The previous message end.
	 * @return The create message occurrence specification (can be <code>null</code>).
	 */
	private MessageOccurrenceSpecification createReceiveEvent(final Message message, final Element target, final MessageEnd previous) {
		if (target instanceof Gate) {
			message.setReceiveEvent((Gate) target);

		} else if (target instanceof ExecutionSpecification) {
			if (((ExecutionSpecification) target).getCovereds().size() > 0) {
				final Lifeline lifeline = ((ExecutionSpecification) target).getCovereds().get(0);
				final MessageOccurrenceSpecification receiveEvent = createMessageEnd(message, lifeline, previous);
				receiveEvent.setName(message.getName() + "ReceiveEvent"); //$NON-NLS-1$
				message.setReceiveEvent(receiveEvent);
				return receiveEvent;
			}
		} else if (target instanceof Lifeline) {
			final MessageOccurrenceSpecification receiveEvent = createMessageEnd(message, (Lifeline) target, previous);
			receiveEvent.setName(message.getName() + "ReceiveEvent"); //$NON-NLS-1$
			message.setReceiveEvent(receiveEvent);
			return receiveEvent;
		}

		return null;
	}
}
