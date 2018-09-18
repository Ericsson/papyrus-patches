/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
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

package org.eclipse.papyrus.uml.diagram.sequence.util;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EStructuralFeature;
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
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.GeneralOrderingEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes;
import org.eclipse.uml2.uml.GeneralOrdering;
import org.eclipse.uml2.uml.UMLPackage.Literals;

/**
 * <p>
 * Util class related to the manipulation of GeneralOrdering Links, typically used by
 * {@link GraphicalNodeEditPolicy} or {@link EditPart}
 * </p>
 */
public class GeneralOrderingUtil extends OccurrenceSpecificationUtil {

	/**
	 * Test if the given {@link CreateConnectionRequest} is creating a GeneralOrdering link
	 *
	 * @param request
	 * @return
	 */
	public static boolean isCreateGeneralOrderingLink(CreateConnectionRequest request) {
		CreateRelationshipRequest createElementRequest = getCreateRelationshipRequest(request);
		if (createElementRequest == null) {
			if (request instanceof CreateAspectUnspecifiedTypeConnectionRequest) {
				CreateAspectUnspecifiedTypeConnectionRequest createRequest = (CreateAspectUnspecifiedTypeConnectionRequest) request;
				List<?> types = createRequest.getElementTypes();
				if (types.stream().allMatch(Predicate.isEqual(UMLElementTypes.GeneralOrdering_Edge))) {
					return true;
				}
			}
		} else {
			IElementType type = createElementRequest.getElementType();
			return type == UMLElementTypes.GeneralOrdering_Edge;
		}
		return false;
	}


	/**
	 * Test if the given {@link CreateConnectionViewRequest} is creating a GeneralOrdering
	 *
	 * @param createRequest
	 * @return
	 */
	public static boolean isGeneralOrderingLink(CreateConnectionViewRequest createRequest) {
		String semanticHint = createRequest.getConnectionViewDescriptor().getSemanticHint();
		return GeneralOrderingEditPart.VISUAL_ID.equals(semanticHint);
	}

	/**
	 * <p>
	 * Test if this request is trying to reconnect a GeneralOrderingLink edit part
	 * </p>
	 *
	 * @param request
	 * @return
	 *
	 * @see GeneralOrderingEditPart
	 */
	public static boolean isGeneralOrderingLink(ReconnectRequest request) {
		return request.getConnectionEditPart() instanceof GeneralOrderingEditPart;
	}

	/**
	 * <p>
	 * Test if the connector view is consistent with a new value.
	 * </p>
	 *
	 * @param connector
	 *            A connector representing a GeneralOrdering Link in the Sequence Diagram
	 * @param setRequest
	 *            A {@link SetRequest} modifying a GeneralOrdering 'before' or 'after' reference
	 * @return
	 * 		<code>true</code> if the Connector is consistent with the new proposed value, <code>false</code> if the connector
	 *         is no longer consistent. If the result is <code>false</code>, actions should be taken to preserve the diagram
	 *         consistency.
	 *
	 * @see GeneralOrdering#getBefore()
	 * @see GeneralOrdering#getAfter()
	 */
	public static boolean isConsistent(Connector connector, SetRequest setRequest) {
		Object newValue = setRequest.getValue();
		EStructuralFeature feature = setRequest.getFeature();
		if (feature != Literals.GENERAL_ORDERING__BEFORE && feature != Literals.GENERAL_ORDERING__AFTER) {
			return true; // The set request doesn't affect that link; do nothing
		}

		if (newValue == null) { // Before or After was unset; the link is no longer consistent
			return false;
		}

		View sourceView = connector.getSource();
		String sourceAnchor = connector.getSourceAnchor() instanceof IdentityAnchor ? ((IdentityAnchor) connector.getSourceAnchor()).getId() : "";

		View targetView = connector.getTarget();
		String targetAnchor = connector.getSourceAnchor() instanceof IdentityAnchor ? ((IdentityAnchor) connector.getTargetAnchor()).getId() : "";

		if (sourceView == null || targetView == null) {
			return false;
		}

		if (feature == Literals.GENERAL_ORDERING__BEFORE) {
			if (newValue != DurationLinkUtil.findSemanticOccurrence(sourceView, sourceAnchor)) {
				return false;
			}
		} else if (feature == Literals.GENERAL_ORDERING__AFTER) {
			if (newValue != DurationLinkUtil.findSemanticOccurrence(targetView, targetAnchor)) {
				return false;
			}
		}

		return true;
	}


}
