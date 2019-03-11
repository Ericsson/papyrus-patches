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
 *      Vincent Lorenzo - bug 492522
 *      Benoit Maggi (CEA LIST) benoit.maggi@cea.fr - bug 514289
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.SemanticEditPolicy;
import org.eclipse.gmf.runtime.emf.type.core.IClientContext;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.requests.ConfigureRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyReferenceRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DuplicateElementsRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.GetEditContextRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.MoveRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientReferenceRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.SetRequest;
import org.eclipse.gmf.runtime.notation.Connector;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.ConnectionEditPart;
import org.eclipse.papyrus.infra.services.edit.context.TypeContext;
import org.eclipse.papyrus.infra.services.edit.service.ElementEditServiceUtils;
import org.eclipse.papyrus.infra.services.edit.service.IElementEditService;
import org.eclipse.papyrus.infra.services.edit.utils.RequestParameterConstants;

/**
 * Non diagram-specific class replacing UMLBaseItemSemanticEditPolicy generated
 * by GMF Tooling.
 */
public class DefaultSemanticEditPolicy extends SemanticEditPolicy {

	public static final String GRAPHICAL_RECONNECTED_EDGE = "graphical_edge"; //$NON-NLS-1$

	/**
	 * Extended request data key to hold editpart visual id.
	 * Add visual id of edited editpart to extended data of the request
	 * so command switch can decide what kind of diagram element is being edited.
	 * It is done in those cases when it's not possible to deduce diagram
	 * element kind from domain element.
	 *
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Command getCommand(Request request) {
		if (request instanceof ReconnectRequest) {
			Object view = ((ReconnectRequest) request).getConnectionEditPart().getModel();
			if (view instanceof View) {
				request.getExtendedData().put(GRAPHICAL_RECONNECTED_EDGE, view);
			}
		}
		return super.getCommand(request);
	}

	@Override
	protected Command getSemanticCommand(IEditCommandRequest request) {
		IEditCommandRequest completedRequest = completeRequest(request);
		Command semanticCommand = getSemanticCommandSwitch(completedRequest);
		if (completedRequest instanceof DestroyRequest) {
			DestroyRequest destroyRequest = (DestroyRequest) completedRequest;
			return shouldProceed(destroyRequest) ? semanticCommand : null;
		}
		return semanticCommand;
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.SemanticEditPolicy#completeRequest(org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest)
	 *
	 * @param request
	 * @return
	 */
	@Override
	protected IEditCommandRequest completeRequest(IEditCommandRequest request) {
		IEditCommandRequest result = super.completeRequest(request);
		if (result instanceof DestroyReferenceRequest) {
			if (null == ((IGraphicalEditPart) getHost()).resolveSemanticElement()) {
				if (getHost() instanceof ConnectionEditPart) {
					ConnectionEditPart ep = (ConnectionEditPart) getHost();
					if (ep.isSemanticConnection()) {
						Object model = ep.getModel();
						if (model instanceof Connector) {
							String type = ((Connector) model).getType();
							if (type != null) {
								result.setParameter(RequestParameterConstants.VIEW_VISUAL_ID, type);
								result.setParameter(RequestParameterConstants.AFFECTED_VIEW, model);
							}
						}
					}
				}
			}
		}
		return result;
	}

	protected Command getSemanticCommandSwitch(IEditCommandRequest req) {
		if (req instanceof CreateRelationshipRequest) {
			return getCreateRelationshipCommand((CreateRelationshipRequest) req);
		} else if (req instanceof CreateElementRequest) {
			return getCreateCommand((CreateElementRequest) req);
		} else if (req instanceof ConfigureRequest) {
			return getConfigureCommand((ConfigureRequest) req);
		} else if (req instanceof DestroyElementRequest) {
			return getDestroyElementCommand((DestroyElementRequest) req);
		} else if (req instanceof DestroyReferenceRequest) {
			return getDestroyReferenceCommand((DestroyReferenceRequest) req);
		} else if (req instanceof DuplicateElementsRequest) {
			return getDuplicateCommand((DuplicateElementsRequest) req);
		} else if (req instanceof GetEditContextRequest) {
			return getEditContextCommand((GetEditContextRequest) req);
		} else if (req instanceof MoveRequest) {
			return getMoveCommand((MoveRequest) req);
		} else if (req instanceof ReorientReferenceRelationshipRequest) {
			return getReorientReferenceRelationshipCommand((ReorientReferenceRelationshipRequest) req);
		} else if (req instanceof ReorientRelationshipRequest) {
			return getReorientRelationshipCommand((ReorientRelationshipRequest) req);
		} else if (req instanceof SetRequest) {
			return getSetCommand((SetRequest) req);
		}
		return null;
	}


	protected Command getConfigureCommand(ConfigureRequest req) {
		return null;
	}


	protected Command getCreateRelationshipCommand(CreateRelationshipRequest req) {
		return getDefaultSemanticCommand(req, req.getElementType());
	}


	protected Command getCreateCommand(CreateElementRequest req) {
		IElementType elementType = req.getElementType();
		if (elementType != null && elementType.getEClass() == null) {
			return getDefaultSemanticCommand(req, elementType);
		} else {
			return getDefaultSemanticCommand(req);
		}
	}


	protected Command getSetCommand(SetRequest req) {
		return null;
	}


	protected Command getEditContextCommand(GetEditContextRequest req) {
		return null;
	}


	protected Command getDestroyElementCommand(DestroyElementRequest req) {
		return getDefaultSemanticCommand(req);
	}


	protected Command getDestroyReferenceCommand(DestroyReferenceRequest req) {
		return getDestroyReferenceCommand(req, req.getContainer());
	}

	protected Command getDestroyReferenceCommand(DestroyReferenceRequest req, Object context) {
		return getDefaultSemanticCommand(req, context);
	}

	protected Command getDuplicateCommand(DuplicateElementsRequest req) {
		return null;
	}


	protected Command getMoveCommand(MoveRequest req) {
		return UnexecutableCommand.INSTANCE;
	}


	protected Command getReorientReferenceRelationshipCommand(ReorientReferenceRelationshipRequest req) {
		EObject context = req.getReferenceOwner();
		return getDefaultSemanticCommand(req, context);
	}


	protected Command getReorientRelationshipCommand(ReorientRelationshipRequest req) {
		IElementEditService commandService = ElementEditServiceUtils.getCommandProvider(req.getRelationship());
		if (commandService == null) {
			return UnexecutableCommand.INSTANCE;
		}

		// Add new graphical end in request parameters
		View newView = (View) getHost().getModel();
		req.setParameter(RequestParameterConstants.EDGE_REORIENT_REQUEST_END_VIEW, newView);

		ICommand semanticCommand = commandService.getEditCommand(req);

		if ((semanticCommand != null) && (semanticCommand.canExecute())) {
			return getGEFWrapper(semanticCommand);
		}
		return UnexecutableCommand.INSTANCE;
	}


	protected final Command getGEFWrapper(ICommand cmd) {
		return new ICommandProxy(cmd);
	}

	/**
	 * Returns editing domain from the host edit part.
	 *
	 */
	protected TransactionalEditingDomain getEditingDomain() {
		return ((IGraphicalEditPart) getHost()).getEditingDomain();
	}

	private Command getDefaultSemanticCommand(IEditCommandRequest req) {
		return getDefaultSemanticCommand(req, null);
	}

	private Command getDefaultSemanticCommand(IEditCommandRequest req, Object context) {
		try {
			IClientContext clientContext = TypeContext.getContext(getEditingDomain());

			IElementEditService commandService;
			if (context != null) {
				commandService = ElementEditServiceUtils.getCommandProvider(context, clientContext);
			} else {
				commandService = ElementEditServiceUtils.getCommandProvider(((IGraphicalEditPart) getHost()).resolveSemanticElement(), clientContext);
			}

			if (commandService != null) {
				ICommand semanticCommand = commandService.getEditCommand(req);
				if ((semanticCommand != null) && (semanticCommand.canExecute())) {
					return getGEFWrapper(semanticCommand);
				}
			}
		} catch (ServiceException e) {
			Activator.log.error(e);
		}
		return UnexecutableCommand.INSTANCE;
	}
}
