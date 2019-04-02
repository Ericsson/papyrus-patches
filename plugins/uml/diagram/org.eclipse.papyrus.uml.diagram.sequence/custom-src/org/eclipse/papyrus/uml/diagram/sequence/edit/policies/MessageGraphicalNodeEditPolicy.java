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

import java.util.Map;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.uml.diagram.sequence.figures.DurationLinkFigure;
import org.eclipse.papyrus.uml.diagram.sequence.util.DurationLinkUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.GeneralOrderingUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.OccurrenceSpecificationUtil;
import org.eclipse.papyrus.uml.service.types.utils.SequenceRequestConstant;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.OccurrenceSpecification;

public class MessageGraphicalNodeEditPolicy extends GraphicalNodeEditPolicy {
	// Source (First half of the request)
	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		if (DurationLinkUtil.isCreateDurationLink(request) || GeneralOrderingUtil.isCreateGeneralOrderingLink(request)) {
			CreateRelationshipRequest createRequest = OccurrenceSpecificationUtil.getCreateRelationshipRequest(request);
			if (createRequest != null) {
				MessageEnd sourceOccurrence;
				Message message = getMessage();
				if (message != null) {
					sourceOccurrence = OccurrenceSpecificationUtil.isSource(getHostFigure(), request.getLocation()) ? message.getSendEvent() : message.getReceiveEvent();
					if (sourceOccurrence instanceof OccurrenceSpecification) {
						@SuppressWarnings("unchecked")
						Map<Object, Object> extendedData = request.getExtendedData();
						extendedData.put(SequenceRequestConstant.SOURCE_OCCURRENCE, sourceOccurrence);
						createRequest.setParameter(SequenceRequestConstant.SOURCE_OCCURRENCE, sourceOccurrence);
					}
				}
			}
		}
		return super.getConnectionCreateCommand(request);
	}

	// Target (Second half of the request)
	@Override
	protected Command getConnectionAndRelationshipCompleteCommand(CreateConnectionViewAndElementRequest request) {
		if (DurationLinkUtil.isCreateDurationLink(request) || GeneralOrderingUtil.isCreateGeneralOrderingLink(request)) {
			CreateRelationshipRequest createRequest = OccurrenceSpecificationUtil.getCreateRelationshipRequest(request);
			if (createRequest != null) {
				MessageEnd targetOccurrence;
				Message message = getMessage();
				if (message != null) {
					targetOccurrence = OccurrenceSpecificationUtil.isSource(getHostFigure(), request.getLocation()) ? message.getSendEvent() : message.getReceiveEvent();
					if (targetOccurrence instanceof OccurrenceSpecification) {
						@SuppressWarnings("unchecked")
						Map<Object, Object> extendedData = request.getExtendedData();
						extendedData.put(SequenceRequestConstant.TARGET_OCCURRENCE, targetOccurrence);
						createRequest.setParameter(SequenceRequestConstant.TARGET_OCCURRENCE, targetOccurrence);
					}
				}
			}
		}
		return super.getConnectionAndRelationshipCompleteCommand(request);
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

	private Message getMessage() {
		EObject model = EMFHelper.getEObject(getHost());
		return model instanceof Message ? (Message) model : null;
	}

	@Override
	protected Connection createDummyConnection(Request req) {
		if (req instanceof CreateConnectionRequest && DurationLinkUtil.isCreateDurationLink((CreateConnectionRequest) req)) {
			return new DurationLinkFigure();
		}
		return new PolylineConnection();
	}
}
