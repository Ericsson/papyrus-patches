/*****************************************************************************
 * Copyright (c) 2010, 2019 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Atos Origin - Initial API and implementation
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Bug 542802
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.util;

import java.util.Arrays;
import java.util.List;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.core.commands.DeleteCommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionNodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IBorderItemEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.EditCommandRequestWrapper;
import org.eclipse.gmf.runtime.emf.commands.core.command.CompositeTransactionalCommand;
import org.eclipse.gmf.runtime.emf.type.core.commands.DestroyElementCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramEditPartsUtil;
import org.eclipse.papyrus.infra.services.edit.service.ElementEditServiceUtils;
import org.eclipse.papyrus.infra.services.edit.service.IElementEditService;
import org.eclipse.papyrus.uml.diagram.sequence.RestoreExecutionEndAdvice;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.OLDLifelineXYLayoutEditPolicy;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.OccurrenceSpecification;

/**
 * An Helper to get deleting command for the sequence diagram elements.
 */
public class SequenceDeleteHelper {

	/**
	 * Complete an ICommand which destroys an ExecutionSpecification element to also destroy dependent finish and start events and time/duration
	 * constraint/observation linked with these ends
	 *
	 * @param deleteViewsCmd
	 *            the command to complete
	 * @param editingDomain
	 *            the editing domain
	 * @param executionPart
	 *            the execution specification edit part on which the request is called
	 * @return the deletion command deleteViewsCmd for convenience
	 */
	public static CompoundCommand completeDeleteMessageViewCommand(CompoundCommand deleteViewsCmd, TransactionalEditingDomain editingDomain, EditPart messagePart) {
		if (messagePart instanceof IGraphicalEditPart) {
			EObject obj = ((IGraphicalEditPart) messagePart).resolveSemanticElement();
			if (obj instanceof Message) {
				Message message = (Message) obj;
				LifelineEditPart srcLifelinePart = SequenceUtil.getParentLifelinePart(((ConnectionNodeEditPart) messagePart).getSource());
				MessageEnd send = message.getSendEvent();
				addDeleteRelatedTimePartsToCommand(deleteViewsCmd, editingDomain, srcLifelinePart, send);
				LifelineEditPart tgtLifelinePart = SequenceUtil.getParentLifelinePart(((ConnectionNodeEditPart) messagePart).getTarget());
				MessageEnd receive = message.getReceiveEvent();
				addDeleteRelatedTimePartsToCommand(deleteViewsCmd, editingDomain, tgtLifelinePart, receive);
				// also delete time observation links which are related to message end
			}
		}
		return deleteViewsCmd;
	}

	private static void addDeleteRelatedTimePartsToCommand(CompoundCommand deleteViewsCmd, TransactionalEditingDomain editingDomain, LifelineEditPart lifelineEP, MessageEnd messageEnd) {
		if (lifelineEP != null && messageEnd instanceof OccurrenceSpecification) {
			for (Object lifelineChild : lifelineEP.getChildren()) {
				if (lifelineChild instanceof IBorderItemEditPart) {
					final IBorderItemEditPart timePart = (IBorderItemEditPart) lifelineChild;
					int positionForEvent = SequenceUtil.positionWhereEventIsLinkedToPart((OccurrenceSpecification) messageEnd, timePart);
					if (positionForEvent != PositionConstants.NONE) {
						// time part is linked, delete the view
						Command deleteTimeViewCommand = new ICommandProxy(new DeleteCommand(editingDomain, (View) timePart.getModel()));
						deleteViewsCmd.add(deleteTimeViewCommand);
					}
				}
			}
		}
	}

	/**
	 * Complete an ICommand which destroys an ExecutionSpecification element to also destroy dependent finish and start events and time/duration
	 * constraint/observation linked with these ends
	 *
	 * @param deleteViewsCmd
	 *            the command to complete
	 * @param editingDomain
	 *            the editing domain
	 * @param executionPart
	 *            the execution specification edit part on which the request is called
	 * @return the deletion command deleteViewsCmd for convenience
	 */
	public static CompoundCommand completeDeleteExecutionSpecificationViewCommand(CompoundCommand deleteViewsCmd, TransactionalEditingDomain editingDomain, EditPart executionPart) {
		if (executionPart instanceof IGraphicalEditPart) {
			EObject obj = ((IGraphicalEditPart) executionPart).resolveSemanticElement();
			if (obj instanceof ExecutionSpecification) {
				ExecutionSpecification execution = (ExecutionSpecification) obj;
				LifelineEditPart lifelinePart = SequenceUtil.getParentLifelinePart(executionPart);
				if (lifelinePart != null) {
					for (Object lifelineChild : lifelinePart.getChildren()) {
						if (lifelineChild instanceof IBorderItemEditPart) {
							final IBorderItemEditPart timePart = (IBorderItemEditPart) lifelineChild;
							OccurrenceSpecification start = execution.getStart();
							OccurrenceSpecification finish = execution.getFinish();
							int positionForStart = SequenceUtil.positionWhereEventIsLinkedToPart(start, timePart);
							int positionForFinish = SequenceUtil.positionWhereEventIsLinkedToPart(finish, timePart);
							if (positionForStart != PositionConstants.NONE || positionForFinish != PositionConstants.NONE) {
								// time part is linked, delete the view
								Command deleteTimeViewCommand = new ICommandProxy(new DeleteCommand(editingDomain, (View) timePart.getModel()));
								deleteViewsCmd.add(deleteTimeViewCommand);
							}
						}
					}
				}
			}
		}
		return deleteViewsCmd;
	}

	/**
	 * Delete the views associated with a list of elements.
	 *
	 * @param cmd
	 *            the CompositeTransactionalCommand
	 * @param element
	 *            the list of model elements
	 * @param editingDomain
	 *            the editing domain to use.
	 */
	public static void deleteView(CompositeTransactionalCommand cmd, List<Element> elements, TransactionalEditingDomain editingDomain) {
		for (Element element : elements) {
			deleteView(cmd, element, editingDomain);
		}
	}

	/**
	 * Delete the views associated with an element.
	 *
	 * @param cmd
	 *            the CompositeTransactionalCommand
	 * @param element
	 *            the model element referenced by the views
	 * @param editingDomain
	 *            the editing domain to use.
	 */
	public static void deleteView(CompositeTransactionalCommand cmd, Element element, TransactionalEditingDomain editingDomain) {
		// Destroy its views
		@SuppressWarnings("rawtypes")
		List views = DiagramEditPartsUtil.getEObjectViews(element);
		for (Object object : views) {
			if (object instanceof View) {
				cmd.add(new DeleteCommand(editingDomain, (View) object));
			}
		}
	}

	/**
	 * Add complete delete message command
	 *
	 * @param req
	 * @param editPart
	 * @return Command
	 */
	public static Command completeDeleteMessageCommand(DestroyElementRequest req, EditPart editPart) {
		EObject selectedEObject = req.getElementToDestroy();
		IElementEditService provider = ElementEditServiceUtils.getCommandProvider(selectedEObject);
		if (provider != null) {
			// Retrieve delete command from the Element Edit service
			ICommand deleteCommand = provider.getEditCommand(req);
			if (deleteCommand != null) {
				CompoundCommand command = new CompoundCommand(deleteCommand.getLabel());
				command.add(new ICommandProxy(deleteCommand));
				// return completeDeleteMessageCommand(command, (ConnectionEditPart)editPart, req.getEditingDomain());
				destroyMessageEvents(command, Arrays.asList(editPart), req.getEditingDomain());
				return command;
			}
		}
		return UnexecutableCommand.INSTANCE;
	}

	public static void destroyExecutionOccurrenceSpecification(DestroyElementRequest req, CompoundCommand deleteElementsCommand, ShapeNodeEditPart host, ExecutionSpecification es) {
		DestroyElementRequest delStart = new DestroyElementRequest(req.getEditingDomain(), es.getStart(), false);
		deleteElementsCommand.add(new ICommandProxy(new DestroyElementCommand(delStart)));
		DestroyElementRequest delEnd = new DestroyElementRequest(req.getEditingDomain(), es.getFinish(), false);
		deleteElementsCommand.add(new ICommandProxy(new DestroyElementCommand(delEnd)));
		destroyMessageEvents(deleteElementsCommand, host, req.getEditingDomain());
		SequenceDeleteHelper.addDestroyExecutionSpecificationChildrenCommand(deleteElementsCommand, req.getEditingDomain(), host);
	}

	public static void destroyMessageEvents(CompoundCommand deleteElementsCommand, ShapeNodeEditPart host, TransactionalEditingDomain transactionalEditingDomain) {
		destroyMessageEvents(deleteElementsCommand, host.getSourceConnections(), transactionalEditingDomain);
		destroyMessageEvents(deleteElementsCommand, host.getTargetConnections(), transactionalEditingDomain);
	}

	static void destroyMessageEvents(CompoundCommand deleteElementsCommand, List<?> list, TransactionalEditingDomain transactionalEditingDomain) {
		for (Object o : list) {
			if (o instanceof ConnectionEditPart) {
				ConnectionEditPart connectionEP = (ConnectionEditPart) o;
				EObject model = ((ConnectionEditPart) o).resolveSemanticElement();
				if (model instanceof Message) {
					Message message = (Message) model;
					MessageEnd receiveEvent = message.getReceiveEvent();
					MessageEnd sendEvent = message.getSendEvent();
					destroyMessageEvent(deleteElementsCommand, sendEvent, transactionalEditingDomain);
					if (false == receiveEvent instanceof DestructionOccurrenceSpecification) {
						destroyMessageEvent(deleteElementsCommand, receiveEvent, transactionalEditingDomain);
					}
				}
			}
		}
	}

	/**
	 * This allows to destroy a message event.
	 *
	 * @param deleteElementsCommand
	 *            The compound command to fill.
	 * @param event
	 *            The event to delete.
	 * @param transactionalEditingDomain
	 *            The editing domain.
	 * @since 5.2
	 */
	public static void destroyMessageEvent(final CompoundCommand deleteElementsCommand, final MessageEnd event, final TransactionalEditingDomain transactionalEditingDomain) {
		if (event != null) {
			final DestroyElementRequest myReq = new DestroyElementRequest(transactionalEditingDomain, event, false);
			// Sometimes, the message end is also the end of a execution.
			final RestoreExecutionEndAdvice provider = new RestoreExecutionEndAdvice();
			if (provider != null) {
				final ICommand editCommand = provider.getAfterEditCommand(myReq);
				if (editCommand != null && editCommand.canExecute()) {
					deleteElementsCommand.add(new ICommandProxy(editCommand));
				}
			}
			deleteElementsCommand.add(new ICommandProxy(new DestroyElementCommand(myReq)));
		}
	}

	static void addDestroyExecutionSpecificationChildrenCommand(CompoundCommand deleteElementsCommand, TransactionalEditingDomain editingDomain, ShapeNodeEditPart part) {
		List<ShapeNodeEditPart> list = OLDLifelineXYLayoutEditPolicy.getAffixedExecutionSpecificationEditParts(part);
		for (ShapeNodeEditPart p : list) {
			Request request = new EditCommandRequestWrapper(new DestroyElementRequest(p.resolveSemanticElement(), false));
			deleteElementsCommand.add(p.getCommand(request));
			destroyMessageEvents(deleteElementsCommand, p.getSourceConnections(), editingDomain);
			destroyMessageEvents(deleteElementsCommand, p.getTargetConnections(), editingDomain);
		}
	}
}
