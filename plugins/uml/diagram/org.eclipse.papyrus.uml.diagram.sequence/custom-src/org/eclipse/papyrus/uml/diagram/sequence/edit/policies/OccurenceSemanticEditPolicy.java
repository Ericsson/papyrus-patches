/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.SemanticEditPolicy;
import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientRelationshipRequest;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultSemanticEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.util.DurationLinkUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.GeneralOrderingUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.OccurrenceSpecificationUtil;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.OccurrenceSpecification;

/**
 * A {@link SemanticEditPolicy} that is able to target specific {@link OccurrenceSpecification}s
 * from a request (Typically for {@link Message#getSendEvent()},{@link Message#getReceiveEvent()},
 * {@link ExecutionSpecification#getStart()} and {@link ExecutionSpecification#getFinish()})
 */
public class OccurenceSemanticEditPolicy extends DefaultSemanticEditPolicy {

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Overridden to support {@link ReconnectRequest}, when only the anchor changes, referencing
	 * a different {@link OccurrenceSpecification} on the same edit part.
	 * </p>
	 *
	 * @param request
	 * @return
	 */
	@Override
	public Command getCommand(Request request) {
		if (REQ_RECONNECT_SOURCE.equals(request.getType())
				&& relationshipSourceHasChanged((ReconnectRequest) request)) {
			EditPart connectionEP = ((ReconnectRequest) request)
					.getConnectionEditPart();
			if (ViewUtil.resolveSemanticElement((View) connectionEP.getModel()) == null) {
				return getReorientRefRelationshipSourceCommand((ReconnectRequest) request);
			} else {
				return getReorientRelationshipSourceCommand((ReconnectRequest) request);
			}
		} else if (REQ_RECONNECT_TARGET.equals(request.getType())
				&& relationshipTargetHasChanged((ReconnectRequest) request)) {
			EditPart connectionEP = ((ReconnectRequest) request)
					.getConnectionEditPart();
			if (ViewUtil.resolveSemanticElement((View) connectionEP.getModel()) == null) {
				return getReorientRefRelationshipTargetCommand((ReconnectRequest) request);
			} else {
				return getReorientRelationshipTargetCommand((ReconnectRequest) request);
			}
		}
		return super.getCommand(request);
	}

	protected boolean relationshipSourceHasChanged(ReconnectRequest request) {
		if (!request.getConnectionEditPart().getSource().equals(request.getTarget())) {
			// Connecting different edit parts
			return true;
		} else if (request.getConnectionEditPart().getModel() instanceof Edge) {
			// Connecting different occurrences on the same edit part (Source vs Target, Start vs Finish...)
			Edge edge = (Edge) request.getConnectionEditPart().getModel();
			return OccurrenceSpecificationUtil.getSourceOccurrence(edge) != OccurrenceSpecificationUtil.getOccurrence(request);
		}
		return false;
	}

	protected boolean relationshipTargetHasChanged(ReconnectRequest request) {
		if (!request.getConnectionEditPart().getTarget().equals(request.getTarget())) {
			// Connecting different edit parts
			return true;
		} else if (request.getConnectionEditPart().getModel() instanceof Edge) {
			// Connecting different occurrences on the same edit part (Source vs Target, Start vs Finish...)
			Edge edge = (Edge) request.getConnectionEditPart().getModel();
			return OccurrenceSpecificationUtil.getTargetOccurrence(edge) != OccurrenceSpecificationUtil.getOccurrence(request);
		}
		return false;
	}

	@Override
	protected Command getReorientRelationshipSourceCommand(ReconnectRequest request) {
		if (GeneralOrderingUtil.isGeneralOrderingLink(request) || DurationLinkUtil.isDurationLink(request)) {
			EObject connectionSemElement = ViewUtil.resolveSemanticElement(((View) request.getConnectionEditPart()
					.getModel()));
			EObject targetSemElement = OccurrenceSpecificationUtil.getOccurrence(request);
			EObject oldSemElement = OccurrenceSpecificationUtil.getSourceOccurrence((Edge) request.getConnectionEditPart().getModel());

			TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost())
					.getEditingDomain();
			ReorientRelationshipRequest semRequest = new ReorientRelationshipRequest(
					editingDomain, connectionSemElement, targetSemElement,
					oldSemElement, ReorientRelationshipRequest.REORIENT_SOURCE);

			semRequest.addParameters(request.getExtendedData());

			return getSemanticCommand(semRequest);
		}

		return super.getReorientRefRelationshipSourceCommand(request);
	}

	@Override
	protected Command getReorientRelationshipTargetCommand(ReconnectRequest request) {
		if (GeneralOrderingUtil.isGeneralOrderingLink(request) || DurationLinkUtil.isDurationLink(request)) {
			EObject connectionSemElement = ViewUtil.resolveSemanticElement((View) request.getConnectionEditPart().getModel());
			EObject targetSemElement = OccurrenceSpecificationUtil.getOccurrence(request);
			EObject oldSemElement = OccurrenceSpecificationUtil.getTargetOccurrence((Edge) request.getConnectionEditPart().getModel());

			TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost())
					.getEditingDomain();
			ReorientRelationshipRequest semRequest = new ReorientRelationshipRequest(
					editingDomain, connectionSemElement, targetSemElement,
					oldSemElement, ReorientRelationshipRequest.REORIENT_TARGET);

			semRequest.addParameters(request.getExtendedData());

			return getSemanticCommand(semRequest);
		}

		return super.getReorientRelationshipTargetCommand(request);
	}

}
