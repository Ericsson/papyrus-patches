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
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.figures.DurationLinkFigure;
import org.eclipse.papyrus.uml.diagram.sequence.util.DurationLinkUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.GeneralOrderingUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.OccurrenceSpecificationUtil;
import org.eclipse.papyrus.uml.service.types.utils.SequenceRequestConstant;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.GeneralOrdering;
import org.eclipse.uml2.uml.OccurrenceSpecification;

/**
 * <p>
 * A specialized {@link GraphicalNodeEditPolicy} for {@link ExecutionSpecification ExecutionSpecifications}, to handle
 * connection of DurationLinks or {@link GeneralOrdering} links to the Start/Finish Occurrences of the {@link ExecutionSpecification}
 * </p>
 */
public class ExecutionSpecificationGraphicalNodeEditPolicy extends ElementCreationWithMessageEditPolicy {

	// Source (First half of the request)
	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		if (DurationLinkUtil.isCreateDurationLink(request) || GeneralOrderingUtil.isCreateGeneralOrderingLink(request)) {
			CreateRelationshipRequest createRequest = DurationLinkUtil.getCreateRelationshipRequest(request);
			if (createRequest != null) {
				OccurrenceSpecification sourceOccurrence;
				ExecutionSpecification execSpec = getExecutionSpecification();
				if (execSpec != null) {
					if (OccurrenceSpecificationUtil.isStart(getHostFigure(), request.getLocation())) {
						sourceOccurrence = execSpec.getStart();
					} else {
						sourceOccurrence = execSpec.getFinish();
					}
					@SuppressWarnings("unchecked")
					Map<Object, Object> extendedData = request.getExtendedData();
					extendedData.put(SequenceRequestConstant.SOURCE_OCCURRENCE, sourceOccurrence);
					createRequest.setParameter(SequenceRequestConstant.SOURCE_OCCURRENCE, sourceOccurrence);
				}
			}
		}
		return super.getConnectionCreateCommand(request);
	}

	private ExecutionSpecification getExecutionSpecification() {
		Object model = getHost().getModel();
		if (model instanceof View && ((View) model).getElement() instanceof ExecutionSpecification) {
			return (ExecutionSpecification) ((View) model).getElement();
		}
		return null;
	}

	// Target (Second half of the request)
	@Override
	protected Command getConnectionAndRelationshipCompleteCommand(CreateConnectionViewAndElementRequest request) {
		if (DurationLinkUtil.isCreateDurationLink(request) || GeneralOrderingUtil.isCreateGeneralOrderingLink(request)) {
			CreateRelationshipRequest createRequest = OccurrenceSpecificationUtil.getCreateRelationshipRequest(request);
			if (createRequest != null) {
				OccurrenceSpecification targetOccurrence;
				ExecutionSpecification execSpec = getExecutionSpecification();
				if (execSpec != null) {
					if (OccurrenceSpecificationUtil.isStart(getHostFigure(), request.getLocation())) {
						targetOccurrence = execSpec.getStart();
					} else {
						targetOccurrence = execSpec.getFinish();
					}
					@SuppressWarnings("unchecked")
					Map<Object, Object> extendedData = request.getExtendedData();
					extendedData.put(SequenceRequestConstant.TARGET_OCCURRENCE, targetOccurrence);
					createRequest.setParameter(SequenceRequestConstant.TARGET_OCCURRENCE, targetOccurrence);
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

	@Override
	protected Connection createDummyConnection(Request req) {
		if (req instanceof CreateConnectionRequest && DurationLinkUtil.isCreateDurationLink((CreateConnectionRequest) req)) {
			return new DurationLinkFigure();
		}
		return new PolylineConnection();
	}

}
