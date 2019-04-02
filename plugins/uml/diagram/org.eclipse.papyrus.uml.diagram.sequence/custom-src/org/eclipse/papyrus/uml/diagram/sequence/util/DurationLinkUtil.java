/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.util;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.SetRequest;
import org.eclipse.gmf.runtime.notation.Connector;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.service.palette.AspectUnspecifiedTypeConnectionTool.CreateAspectUnspecifiedTypeConnectionRequest;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationConstraintLinkEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationObservationLinkEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes;
import org.eclipse.uml2.uml.DurationConstraint;
import org.eclipse.uml2.uml.DurationObservation;

/**
 * <p>
 * Util class related to the manipulation of DurationLinks, typically used by
 * {@link GraphicalNodeEditPolicy} or {@link EditPart}
 * </p>
 */
public class DurationLinkUtil extends OccurrenceSpecificationUtil {

	/**
	 * Test if the given {@link CreateConnectionRequest} is creating a DurationLink
	 *
	 * @param request
	 * @return
	 */
	public static boolean isCreateDurationLink(CreateConnectionRequest request) {
		CreateRelationshipRequest createElementRequest = getCreateRelationshipRequest(request);
		if (createElementRequest == null) {
			if (request instanceof CreateAspectUnspecifiedTypeConnectionRequest) {
				CreateAspectUnspecifiedTypeConnectionRequest createRequest = (CreateAspectUnspecifiedTypeConnectionRequest) request;
				List<?> types = createRequest.getElementTypes();
				if (types.stream().allMatch(
						type -> type == UMLElementTypes.DurationConstraint_Edge ||
								type == UMLElementTypes.DurationObservation_Edge)) {
					return true;
				}
			}
		} else {
			IElementType type = createElementRequest.getElementType();
			return type == UMLElementTypes.DurationConstraint_Edge || type == UMLElementTypes.DurationObservation_Edge;
		}
		return false;
	}


	/**
	 * Test if the given {@link CreateConnectionViewRequest} is creating a DurationLink
	 *
	 * @param createRequest
	 * @return
	 */
	public static boolean isDurationLink(CreateConnectionViewRequest createRequest) {
		String semanticHint = createRequest.getConnectionViewDescriptor().getSemanticHint();
		switch (semanticHint) {
		case DurationConstraintLinkEditPart.VISUAL_ID:
		case DurationObservationLinkEditPart.VISUAL_ID:
			return true;
		}
		return false;
	}


	/**
	 * <p>
	 * Test if this request is trying to reconnect a DurationLink edit part
	 * </p>
	 *
	 * @param request
	 * @return
	 *
	 * @see DurationConstraintLinkEditPart
	 * @see DurationObservationLinkEditPart
	 */
	public static boolean isDurationLink(ReconnectRequest request) {
		return request.getConnectionEditPart() instanceof DurationConstraintLinkEditPart ||
				request.getConnectionEditPart() instanceof DurationObservationLinkEditPart;
	}

	/**
	 * <p>
	 * Test if the connector view is consistent with a new value. If the new value is not a List,
	 * this method always returns true. Otherwise, the list items will be compared with the
	 * semantic source/target of the given connector.
	 * </p>
	 *
	 * @param connector
	 *            A connector representing a DurationLink (Constraint or Observation) in the Sequence Diagram
	 * @param setRequest
	 *            A {@link SetRequest} modifying a duration link source/target (for {@link DurationConstraint#getConstrainedElements()}
	 *            or {@link DurationObservation#getEvents()},
	 * @return
	 * 		<code>true</code> if the Connector is consistent with the new proposed value, <code>false</code> if the connector
	 *         is no longer consistent. If the result is <code>false</code>, actions should be taken to preserve the diagram
	 *         consistency.
	 */
	public static boolean isConsistent(Connector connector, SetRequest setRequest) {
		Object newValue = setRequest.getValue();
		if (false == newValue instanceof List) {
			// Not supported; do nothing. Probably shouldn't happen anyway.
			return true;
		}

		List<?> values = (List<?>) newValue;
		if (values.isEmpty()) {
			// FIXME Workaround for the Properties View. When using the multi-reference editor's dialog,
			// the editor will first send a clear() request, then a addAll() request; so we'd always
			// destroy the connector, even if it's actually still valid. To be safe, we ignore this case
			// Keeping an invalid connector in the diagram is better than destroying a valid one.
			return true;
		}

		View sourceView = connector.getSource();
		String sourceAnchor = connector.getSourceAnchor() instanceof IdentityAnchor ? ((IdentityAnchor) connector.getSourceAnchor()).getId() : "";

		View targetView = connector.getTarget();
		String targetAnchor = connector.getSourceAnchor() instanceof IdentityAnchor ? ((IdentityAnchor) connector.getTargetAnchor()).getId() : "";

		if (sourceView == null || targetView == null) {
			return false;
		}

		if (values.isEmpty()) {
			return false;
		}

		Object sourceEvent = values.get(0);
		if (sourceEvent != DurationLinkUtil.findSemanticOccurrence(sourceView, sourceAnchor)) {
			return false;
		}

		if (values.size() > 1) { // source != target
			Object targetEvent = values.get(1);
			return targetEvent == DurationLinkUtil.findSemanticOccurrence(targetView, targetAnchor);
		} else { // source == target
			return sourceEvent == DurationLinkUtil.findSemanticOccurrence(targetView, targetAnchor);
		}
	}

}
