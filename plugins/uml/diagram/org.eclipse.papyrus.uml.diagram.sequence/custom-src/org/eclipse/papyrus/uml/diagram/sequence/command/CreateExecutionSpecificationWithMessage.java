/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.command;

import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.commands.SemanticCreateCommand;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest.ConnectionViewAndElementDescriptor;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequestFactory;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.emf.type.core.IHintedType;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.papyrus.commands.DestroyElementPapyrusCommand;
import org.eclipse.papyrus.infra.emf.gmf.command.GMFtoEMFCommandWrapper;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.NodeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomActionExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.messages.Messages;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.preferences.CustomDiagramGeneralPreferencePage;
import org.eclipse.papyrus.uml.service.types.element.UMLDIElementTypes;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * this class is used to automatically create execution specifications at target
 * from the request in charge of creating a message between lifelines
 * according to the preferences for this message sort
 */
public class CreateExecutionSpecificationWithMessage extends AbstractTransactionalCommand {

	protected CreateConnectionViewAndElementRequest request;
	protected EditPart graphicalContainer;

	protected String preference;
	protected IHintedType type;
	protected boolean createReply;

	/**
	 * @param domain
	 * @param request
	 *            the request that is in charge of creating the message
	 * @param graphicalContainer
	 *            the lifeline that will contain the event representation
	 */
	public CreateExecutionSpecificationWithMessage(TransactionalEditingDomain domain, CreateConnectionViewAndElementRequest request, EditPart graphicalContainer) {
		super(domain, Messages.Commands_CreateExecutionSpecification_Label, null);
		this.request = request;
		this.graphicalContainer = graphicalContainer;

		this.createReply = false;
	}

	/**
	 * @see org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand#doExecuteWithResult(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 *
	 * @param monitor
	 * @param info
	 * @return
	 * @throws ExecutionException
	 */
	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		// 1. look for the message triggering the creation of the execution specification
		Message message = getMessage();
		if (message == null) {
			throw new ExecutionException("null message"); //$NON-NLS-1$
		}
		// 2. retrieve preferences to apply
		// according to the message sort
		retrievePreferences();
		if (type == null || CustomDiagramGeneralPreferencePage.CHOICE_NONE.equals(preference)) {
			throw new ExecutionException("undefined preference"); //$NON-NLS-1$
		}
		// 3. create execution specification at target
		createExecutionSpecification();
		return CommandResult.newOKCommandResult();
	}



	/**
	 * creates an execution specification on the target lifeline
	 * creation location is computed from the request
	 */
	private void createExecutionSpecification() {
		LifelineEditPart lifelineEditPart = (LifelineEditPart) graphicalContainer;
		CreateViewRequest requestcreation = CreateViewRequestFactory.getCreateShapeRequest(type, lifelineEditPart.getDiagramPreferencesHint());
		Point point = request.getLocation().getCopy();
		requestcreation.setLocation(point);
		Command command = lifelineEditPart.getCommand(requestcreation);
		command.execute();

		// case where a reply message must also be created
		if (createReply) {
			// Gets the created execution specification
			ExecutionSpecification executionSpecification = getCreatedElement(command, ExecutionSpecification.class);
			if (null != executionSpecification) {
				Point replysourcepoint = point.getCopy();
				replysourcepoint.setY(replysourcepoint.y + CustomActionExecutionSpecificationEditPart.DEFAULT_HEIGHT);
				// source of the reply message is the end of the execution specification
				createReplyMessage(lifelineEditPart, executionSpecification, replysourcepoint);
			}
		}
	}

	/**
	 * creates a reply message originating from lifelineEditPart at replysourcepoint.
	 */
	private void createReplyMessage(LifelineEditPart lifelineEditPart, ExecutionSpecification executionSpecification, Point replysourcepoint) {
		CreateConnectionViewRequest requestreplycreation = CreateViewRequestFactory.getCreateConnectionRequest(UMLDIElementTypes.MESSAGE_REPLY_EDGE, lifelineEditPart.getDiagramPreferencesHint());
		requestreplycreation.setLocation(replysourcepoint);
		requestreplycreation.setSourceEditPart(null);
		requestreplycreation.setTargetEditPart(lifelineEditPart);
		requestreplycreation.setType(RequestConstants.REQ_CONNECTION_START);
		Command replycommand = lifelineEditPart.getCommand(requestreplycreation);
		// setup the request in preparation to get the connection end command
		requestreplycreation.setSourceEditPart(lifelineEditPart);
		NodeEditPart target = (NodeEditPart) request.getSourceEditPart();

		while (target instanceof AbstractExecutionSpecificationEditPart) {
			target = (NodeEditPart) target.getParent();
		}

		requestreplycreation.setTargetEditPart(target);
		requestreplycreation.setType(RequestConstants.REQ_CONNECTION_END);

		IFigure f = target.getPrimaryShape();
		Rectangle b = f.getBounds().getCopy();
		f.translateToAbsolute(b);
		Point c = b.getCenter().getCopy();

		Point replytargetpoint = replysourcepoint.getCopy();
		replytargetpoint.setX(c.x);
		requestreplycreation.setLocation(replytargetpoint);
		replycommand = target.getCommand(requestreplycreation);
		replycommand.execute();

		// replace execution Specification finish event by the message reply send event.
		Message messageReply = getCreatedElement(replycommand, Message.class);
		if (null != messageReply) {
			MessageEnd sendEvent = messageReply.getSendEvent();
			OccurrenceSpecification finish = executionSpecification.getFinish();
			SetCommand setSendEventCommand = new SetCommand(getEditingDomain(), executionSpecification, UMLPackage.eINSTANCE.getExecutionSpecification_Finish(), sendEvent);
			setSendEventCommand.execute();

			// delete the old finish os.
			DestroyElementPapyrusCommand destroyElementPapyrusCommand = new DestroyElementPapyrusCommand(new DestroyElementRequest(finish, false));
			if (destroyElementPapyrusCommand != null && destroyElementPapyrusCommand.canExecute()) {
				getEditingDomain().getCommandStack().execute(new GMFtoEMFCommandWrapper(destroyElementPapyrusCommand));
			}
		}
	}

	/**
	 * @return the message from the given request, can return null
	 */
	private Message getMessage() {
		Message message = null;
		ConnectionViewAndElementDescriptor connectionViewAndElementDescriptor = request.getConnectionViewAndElementDescriptor();
		if (connectionViewAndElementDescriptor != null) {
			CreateElementRequestAdapter createElementRequestAdapter = connectionViewAndElementDescriptor.getCreateElementRequestAdapter();
			message = (Message) createElementRequestAdapter.getAdapter(Message.class);
		}
		return message;
	}

	/**
	 * retrieve preferences concerned with automatic creation of execution specifications
	 */
	private void retrievePreferences() {
		this.type = null;
		IPreferenceStore store = UMLDiagramEditorPlugin.getInstance().getPreferenceStore();
		if (request.getConnectionViewAndElementDescriptor().getSemanticHint().equals(UMLDIElementTypes.MESSAGE_ASYNCH_EDGE.getSemanticHint())) {
			// for asynchronous messages
			this.preference = store.getString(CustomDiagramGeneralPreferencePage.PREF_EXECUTION_SPECIFICATION_ASYNC_MSG);
		}
		if (request.getConnectionViewAndElementDescriptor().getSemanticHint().equals(UMLDIElementTypes.MESSAGE_SYNCH_EDGE.getSemanticHint())) {
			// for synchronous messages
			this.preference = store.getString(CustomDiagramGeneralPreferencePage.PREF_EXECUTION_SPECIFICATION_SYNC_MSG);
		}
		// case where a behavior execution specification must be created at target
		if (CustomDiagramGeneralPreferencePage.CHOICE_BEHAVIOR.equals(preference) || CustomDiagramGeneralPreferencePage.CHOICE_BEHAVIOR_AND_REPLY.equals(preference)) {
			this.type = UMLDIElementTypes.BEHAVIOR_EXECUTION_SPECIFICATION_SHAPE;
		}
		// case where an action execution specification must be created at target
		if (CustomDiagramGeneralPreferencePage.CHOICE_ACTION.equals(preference) || CustomDiagramGeneralPreferencePage.CHOICE_ACTION_AND_REPLY.equals(preference)) {
			this.type = UMLDIElementTypes.ACTION_EXECUTION_SPECIFICATION_SHAPE;
		}
		// case where a message reply must also be created
		if (CustomDiagramGeneralPreferencePage.CHOICE_BEHAVIOR_AND_REPLY.equals(preference) || CustomDiagramGeneralPreferencePage.CHOICE_ACTION_AND_REPLY.equals(preference)) {
			this.createReply = true;
		}
	}


	/**
	 * Get the list of all commands contained into {@link CompoundCommand}, {@link CompositeCommand} or {@link ICommandProxy}.
	 *
	 * @param parent
	 *            the command to looking for
	 * @return the list of all commands
	 */
	@SuppressWarnings("unchecked")
	private Stream<Object> getAllCommands(final Object parent) {
		Object command = null;
		if (parent instanceof ICommandProxy) {
			// get the inner command in case of proxy
			command = ((ICommandProxy) parent).getICommand();
		} else {
			command = parent;
		}

		if (command instanceof CompoundCommand) {
			return ((CompoundCommand) command).getCommands().stream()
					.flatMap(childNode -> getAllCommands(childNode));
		} else if (command instanceof CompositeCommand) {
			return StreamSupport.stream(Spliterators.spliteratorUnknownSize(((CompositeCommand) command).iterator(), Spliterator.ORDERED), false)
					.flatMap(childNode -> getAllCommands(childNode));
		} else {
			return Stream.of(command);
		}
	}

	/**
	 * Get the created semantic element.
	 *
	 * @param <T>
	 *            the type of expected element
	 * @param command
	 *            the command to looking for the element
	 * @return the created semantic element
	 */
	@SuppressWarnings("unchecked")
	private <T> T getCreatedElement(final Command command, final Class<T> type) {
		T element = null;
		try {
			// extract the semantic create command from compound command
			SemanticCreateCommand semanticCreateCommand = getAllCommands(command)
					.filter(SemanticCreateCommand.class::isInstance)
					.map(SemanticCreateCommand.class::cast).findFirst().get();
			// get the return value of the command
			CommandResult commandResult = semanticCreateCommand.getCommandResult();
			if (null != commandResult && commandResult.getReturnValue() instanceof CreateElementRequestAdapter) {
				// Get the created element
				element = (T) ((CreateElementRequestAdapter) commandResult.getReturnValue()).getAdapter(type);
			}
		} catch (NoSuchElementException e) {
			// Do nothing null is return
		}

		return element;
	}
}
