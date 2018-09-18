/*****************************************************************************
 * Copyright (c) 2010, 2018 CEA
 *
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
 *   Vincent LORENZO (CEA LIST) - vincent.lorenzo@cea.fr - bug Bug 531520
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.diagram.core.commands.SetConnectionEndsCommand;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest.ConnectionViewDescriptor;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeConnectionRequest;
import org.eclipse.gmf.runtime.emf.type.core.IHintedType;
import org.eclipse.gmf.runtime.notation.Connector;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.command.OLDCreateGateViewCommand;
import org.eclipse.papyrus.uml.diagram.sequence.draw2d.routers.MessageRouter;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractMessageEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageDeleteEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.OLDLifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes;
import org.eclipse.papyrus.uml.diagram.sequence.util.GateHelper;
import org.eclipse.papyrus.uml.diagram.sequence.util.LifelineMessageCreateHelper;
import org.eclipse.papyrus.uml.diagram.sequence.util.OccurrenceSpecificationMoveHelper;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceRequestConstant;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;

/**
 * A specific policy to handle :
 * - Message aspects inherited from {@link OLDSequenceGraphicalNodeEditPolicy}.
 * - Time/duration move when a message end or an execution is moved.
 * - Duration constraint/observation creation feedback.
 * - Creation of general ordering links.
 * This edit policy is intended to be installed on parts which represent a lifeline or which are contained within a lifeline part.
 */
public class LifelineChildGraphicalNodeEditPolicy extends OLDSequenceGraphicalNodeEditPolicy {

	/** the router to use for messages */
	public static ConnectionRouter messageRouter = new MessageRouter();

	@Override
	public Command getCommand(Request request) {
		/*
		 * 1. we check if the command must be redirected to the LifelineEditPart:
		 * We redirect when we are creating a message or reconnecting a message
		 * Bug 531520 : we decided to delegate the message creation directly to the Lifeline
		 */
		boolean delegateToLifeline = false;

		if (request instanceof CreateConnectionViewAndElementRequest) {
			CreateConnectionViewAndElementRequest r = (CreateConnectionViewAndElementRequest) request;
			delegateToLifeline = isMessageHint(r.getConnectionViewDescriptor().getSemanticHint());
		}

		if (request instanceof CreateUnspecifiedTypeConnectionRequest) {
			final List<?> coll = ((CreateUnspecifiedTypeConnectionRequest) request).getElementTypes();
			final Iterator<?> iter = coll.iterator();
			while (iter.hasNext() && false == delegateToLifeline) {
				final Object current = iter.next();
				if (current instanceof IHintedType) {
					if (current instanceof IHintedType) {
						final IHintedType eltType = (IHintedType) current;
						delegateToLifeline = isMessageHint(eltType.getSemanticHint());
					}
				}
			}
		}

		if (request instanceof ReconnectRequest) {
			final ConnectionEditPart cep = ((ReconnectRequest) request).getConnectionEditPart();
			final Object model = cep.getModel();
			if (model instanceof Connector) {
				if (((Connector) model).getElement() instanceof Message) {
					((ReconnectRequest) request).setTargetEditPart(getHost().getParent());
					delegateToLifeline = true;
				}
			}
		}

		if (delegateToLifeline && getHost() instanceof AbstractExecutionSpecificationEditPart && getHost().getParent() instanceof LifelineEditPart) {
			return getHost().getParent().getCommand(request);
		}

		/*
		 * 2. we are in other cases:
		 * 2.1: it is a comment link or a constraint link or a context link -> seems works fine removing this code
		 * 2.2: it is a General Ordering, probably not supported
		 *
		 * TODO : this code and more in this class should probably be deleted
		 */
		if (org.eclipse.gef.RequestConstants.REQ_CONNECTION_START.equals(request.getType())) {
			if (request instanceof CreateConnectionViewAndElementRequest) {
				return getConnectionAndRelationshipCreateCommand((CreateConnectionViewAndElementRequest) request);
			} else if (request instanceof CreateUnspecifiedTypeConnectionRequest) {
				return getUnspecifiedConnectionCreateCommand((CreateUnspecifiedTypeConnectionRequest) request);
			}
		} else if (org.eclipse.gef.RequestConstants.REQ_CONNECTION_END.equals(request.getType())) {
			if (request instanceof CreateConnectionViewAndElementRequest) {
				return getConnectionAndRelationshipCompleteCommand((CreateConnectionViewAndElementRequest) request);
			} else if (request instanceof CreateUnspecifiedTypeConnectionRequest) {
				return getUnspecifiedConnectionCompleteCommand((CreateUnspecifiedTypeConnectionRequest) request);
			}
		}
		return super.getCommand(request);
	}

	/**
	 * Gets the command to start the creation of a new connection and
	 * relationship (if applicable) for a unspecified type request. This will
	 * update all the individual requests appropriately.
	 *
	 * @param request
	 *            the unspecified type request
	 * @return the command
	 */
	private Command getUnspecifiedConnectionCreateCommand(final CreateUnspecifiedTypeConnectionRequest request) {
		if (request.isDirectionReversed()) {
			return new Command() {

				/**
				 * All we know is the target and the possible relationship
				 * types. At this point, there is no way to validate the
				 * commands for this scenario.
				 */
				@Override
				public boolean canExecute() {
					return true;
				}
			};
		} else {
			// Get the start command for each individual request, this will
			// update each request as required.
			final List<Command> commands = new ArrayList<>();
			for (Iterator iter = request.getAllRequests().iterator(); iter.hasNext();) {
				Request individualRequest = (Request) iter.next();
				Command cmd = null;
				if (individualRequest instanceof CreateConnectionViewAndElementRequest) {
					cmd = getConnectionAndRelationshipCreateCommand((CreateConnectionViewAndElementRequest) individualRequest);
				} else if (individualRequest instanceof CreateConnectionViewRequest) {
					cmd = getConnectionCreateCommand((CreateConnectionViewRequest) individualRequest);
				}
				if (cmd != null && cmd.canExecute()) {
					commands.add(cmd);
				}
			}
			if (commands.isEmpty()) {
				// GEF's AbstractConnectionCreationTool expects a null command
				// when the gesture should be disabled.
				return null;
			}
			// return an executable command that does nothing
			return new Command() {/* do nothing */
			};
		}
	}

	/**
	 * Get the command to reconnect the source and move associated time/duration constraints/observation.
	 *
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.policies.OLDSequenceGraphicalNodeEditPolicy#getReconnectSourceCommand(org.eclipse.gef.requests.ReconnectRequest)
	 *
	 * @param request
	 *            the reconnection request
	 * @return the command
	 */
	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		ReconnectRequest reconnectRequest = request;
		ConnectionEditPart linkEditPart = reconnectRequest.getConnectionEditPart();
		if (linkEditPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE) != null) {
			SequenceReferenceEditPolicy references = (SequenceReferenceEditPolicy) linkEditPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE);
			for (Iterator<EditPart> iterator = references.getStrongReferences().keySet().iterator(); iterator.hasNext();) {
				EditPart editPart = iterator.next();
				if (editPart.equals(getHost())) {
					reconnectRequest.setTargetEditPart(getHost().getParent());
					return getHost().getParent().getCommand(reconnectRequest);
				}
			}
		}
		Command command = super.getReconnectSourceCommand(request);
		if (command != null && request.getConnectionEditPart() instanceof AbstractMessageEditPart) {
			command = OccurrenceSpecificationMoveHelper.completeReconnectConnectionCommand(command, request, getConnectableEditPart());
			if (request.getConnectionEditPart() instanceof MessageCreateEditPart && request.getTarget() instanceof LifelineEditPart) {
				LifelineEditPart newSource = (LifelineEditPart) request.getTarget();
				OLDLifelineEditPart target = (OLDLifelineEditPart) request.getConnectionEditPart().getTarget();
				command = LifelineMessageCreateHelper.moveLifelineDown(command, target, newSource.getFigure().getBounds().getLocation().getCopy());
			}
		}
		return command;
	}

	/**
	 * Get the command to reconnect the target and move associated time/duration constraints/observation.
	 *
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.policies.OLDSequenceGraphicalNodeEditPolicy#getReconnectTargetCommand(org.eclipse.gef.requests.ReconnectRequest)
	 *
	 * @param request
	 *            the reconnection request
	 * @return the command
	 */
	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		ReconnectRequest reconnectRequest = request;
		ConnectionEditPart linkEditPart = reconnectRequest.getConnectionEditPart();
		if (linkEditPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE) != null) {
			SequenceReferenceEditPolicy references = (SequenceReferenceEditPolicy) linkEditPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE);
			for (Iterator<EditPart> iterator = references.getStrongReferences().keySet().iterator(); iterator.hasNext();) {
				EditPart editPart = iterator.next();
				if (editPart.equals(getHost())) {
					reconnectRequest.setTargetEditPart(getHost().getParent());
					return getHost().getParent().getCommand(reconnectRequest);
				}
			}
		}

		Command command = super.getReconnectTargetCommand(request);
		if (command != null && request.getConnectionEditPart() instanceof AbstractMessageEditPart) {
			command = OccurrenceSpecificationMoveHelper.completeReconnectConnectionCommand(command, request, getConnectableEditPart());
			if (request.getConnectionEditPart() instanceof MessageCreateEditPart && request.getTarget() instanceof LifelineEditPart) {
				command = LifelineMessageCreateHelper.reconnectMessageCreateTarget(request, command);
			}
			if (request.getConnectionEditPart() instanceof MessageDeleteEditPart && request.getTarget() instanceof LifelineEditPart) {
				// command = LifelineMessageDeleteHelper.getReconnectMessageDeleteTargetCommand(request, command);
			}
		}
		return command;
	}

	/**
	 * Get the replacing connection router for routing messages correctly
	 *
	 * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getDummyConnectionRouter(org.eclipse.gef.requests.CreateConnectionRequest)
	 */
	@Override
	protected ConnectionRouter getDummyConnectionRouter(CreateConnectionRequest req) {
		return messageRouter;
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.policies.OLDSequenceGraphicalNodeEditPolicy#getConnectionCompleteCommand(org.eclipse.gef.requests.CreateConnectionRequest)
	 *
	 * @param request
	 * @return
	 */
	@Override
	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		Command command = super.getConnectionCompleteCommand(request);
		if (request instanceof CreateConnectionViewAndElementRequest) {
			CreateConnectionViewAndElementRequest viewRequest = (CreateConnectionViewAndElementRequest) request;
			EditPart sourceEP = viewRequest.getSourceEditPart();
			EObject source = ViewUtil.resolveSemanticElement((View) sourceEP.getModel());
			/*
			 * Create Graphical Gate if needed, See: https://bugs.eclipse.org/bugs/show_bug.cgi?id=389531
			 */
			String semanticHint = viewRequest.getConnectionViewAndElementDescriptor().getSemanticHint();
			// Ignore CoRegion.
			if ((((IHintedType) (UMLElementTypes.Message_AsynchEdge)).getSemanticHint().equals(semanticHint) || ((IHintedType) (UMLElementTypes.Message_ReplyEdge)).getSemanticHint().equals(semanticHint))) {
				if (source instanceof CombinedFragment || source instanceof Interaction || source instanceof InteractionUse) {
					CompoundCommand cc = new CompoundCommand("Redirect to Gate");
					Point location = null;
					IGraphicalEditPart adapter = sourceEP.getAdapter(IGraphicalEditPart.class);
					if (adapter != null) {
						Point sourceLocation = request.getLocation();
						Object object = request.getExtendedData().get(SequenceRequestConstant.SOURCE_LOCATION_DATA);
						if (object instanceof Point) {
							sourceLocation = (Point) object;
						}
						location = GateHelper.computeGateLocation(sourceLocation, adapter.getFigure(), null);
					}
					ConnectionViewDescriptor edgeAdapter = viewRequest.getConnectionViewDescriptor();
					final IAdaptable elementAdapter = edgeAdapter.getElementAdapter();
					if (elementAdapter != null) {
						IAdaptable gateAdapter = new IAdaptable() {

							@Override
							public <T> T getAdapter(Class<T> adapter) {
								if (Gate.class == adapter) {
									Message message = elementAdapter.getAdapter(Message.class);
									MessageEnd sendEvent = message.getSendEvent();
									if (sendEvent instanceof Gate) {
										return adapter.cast(sendEvent);
									}
								}
								return null;
							}
						};
						TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();
						OLDCreateGateViewCommand createGateCommand = new OLDCreateGateViewCommand(editingDomain, sourceEP, location, gateAdapter);
						cc.add(new ICommandProxy(createGateCommand));
						SetConnectionEndsCommand resetSourceCommand = new SetConnectionEndsCommand(editingDomain, null);
						resetSourceCommand.setEdgeAdaptor(edgeAdapter);
						resetSourceCommand.setNewSourceAdaptor(createGateCommand.getResult());
						cc.add(new ICommandProxy(resetSourceCommand));
						if (cc.canExecute()) {
							command = command.chain(cc);
						}
					}
				}
			}
		}
		return command;
	}
}
