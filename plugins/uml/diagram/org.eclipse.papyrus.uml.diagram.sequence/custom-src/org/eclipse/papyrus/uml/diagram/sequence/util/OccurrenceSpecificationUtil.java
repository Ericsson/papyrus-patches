/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
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

package org.eclipse.papyrus.uml.diagram.sequence.util;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.gmf.runtime.notation.Anchor;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.uml.diagram.sequence.anchors.AnchorConstants;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.OccurrenceSpecification;

/**
 * <p>
 * Util class related to the manipulation of Links targetting {@link OccurrenceSpecification}s,
 * typically used by {@link GraphicalNodeEditPolicy} or {@link EditPart}.
 * </p>
 * <p>
 * Most methods are meant to be used with Edges connecting some {@link OccurrenceSpecification}
 * that are not necessarily displayed on the Diagram.
 * </p>
 * <p>
 * To identify the right {@link OccurrenceSpecification}, the methods may rely either on the
 * {@link IFigure} that is under the mouse cursor, by determining if the mouse is closer to
 * the start or finish (Or source/target for edges) point representing an {@link OccurrenceSpecification}.
 * </p>
 * <p>
 * When only the {@link Edge} view is available, the methods will rely on the current {@link Edge#getSourceAnchor()}
 * and {@link Edge#getTargetAnchor()}. Anchors are expected to match one of the values defined in {@link AnchorConstants}.
 * </p>
 *
 * @see DurationLinkUtil
 * @see GeneralOrderingUtil
 * @see AnchorConstants#START_TERMINAL
 * @see AnchorConstants#END_TERMINAL
 */
public class OccurrenceSpecificationUtil {


	/**
	 * Retrieve the semantic {@link CreateRelationshipRequest} from the given GEF {@link CreateConnectionRequest},
	 * or <code>null</code>.
	 *
	 * @param request
	 * @return
	 */
	public static CreateRelationshipRequest getCreateRelationshipRequest(CreateConnectionRequest request) {
		if (false == request instanceof CreateConnectionViewAndElementRequest) {
			return null;
		}
		CreateElementRequestAdapter requestAdapter = ((CreateConnectionViewAndElementRequest) request).getConnectionViewAndElementDescriptor().getCreateElementRequestAdapter();
		if (requestAdapter == null) {
			return null;
		}
		CreateRelationshipRequest createElementRequest = (CreateRelationshipRequest) requestAdapter.getAdapter(CreateRelationshipRequest.class);
		return createElementRequest;
	}

	/**
	 * Test whether the given request is closer to the start (top) or to the finish (bottom) point of the execution specification
	 *
	 * @param createRequest
	 *            The create request
	 * @return
	 * 		<code>true</code> if the given request is closer to the top of the figure; false if it is closer to the bottom
	 */
	public static boolean isStart(IFigure targetFigure, CreateRequest createRequest) {
		return isStart(targetFigure, createRequest.getLocation());
	}

	/**
	 * Test whether the given request is closer to the start (top) or to the finish (bottom) point of the execution specification
	 *
	 * @param Point
	 *            The current request location
	 * @return
	 * 		<code>true</code> if the given request is closer to the top of the figure; false if it is closer to the bottom
	 */
	public static boolean isStart(IFigure targetFigure, Point requestLocation) {
		Rectangle bounds = targetFigure.getBounds().getCopy();
		targetFigure.translateToAbsolute(bounds);

		double distanceToTop = requestLocation.getDistance(bounds.getTop());
		double distanceToBottom = requestLocation.getDistance(bounds.getBottom());
		return distanceToTop < distanceToBottom;
	}

	/**
	 * Test whether the given request is closer to the source or to the target point of the message
	 *
	 * @param targetFigure
	 *            The connection figure representing the message
	 * @param createRequest
	 *            The create request
	 * @return
	 * 		<code>true</code> if the given request is closer to the source of the connection; false if it is closer to the target
	 */
	public static boolean isSource(IFigure targetFigure, CreateRequest createRequest) {
		return isSource(targetFigure, createRequest.getLocation());
	}

	/**
	 * Test whether the given request is closer to the source or to the target point of the message
	 *
	 * @param targetFigure
	 *            The connection figure representing the message
	 * @param requestLocation
	 *            The mouse location for the current {@link Request}, in Viewer coordinates
	 * @return
	 * 		<code>true</code> if the given request is closer to the source of the connection; false if it is closer to the target
	 */
	public static boolean isSource(IFigure targetFigure, Point requestLocation) {
		requestLocation = requestLocation.getCopy();
		IFigure connection = targetFigure;
		if (connection instanceof Connection) {
			PointList points = ((Connection) connection).getPoints();
			connection.translateToRelative(requestLocation);
			if (points.size() >= 2) {
				Point source = points.getFirstPoint();
				Point target = points.getLastPoint();
				double distanceToSource = requestLocation.getDistance(source);
				double distanceToTarget = requestLocation.getDistance(target);
				return distanceToSource < distanceToTarget;
			}
		}

		// Default; shouldn't happen, unless the Message figure is invalid,
		// in which case we can't determine the source/target).
		return true;
	}


	/**
	 * Find the semantic {@link OccurrenceSpecification} represented by the given <code>connectorEnd</code>.
	 * The connector should be the source or target of a connector (e.g. DurationLink or GeneralOrdering).
	 *
	 * @param connectorEnd
	 *            the source or target of a connector
	 * @param anchorTerminal
	 *            The connection anchor corresponding to the given connector end.
	 * @return
	 * 		The semantic occurrence specification represented by the given connector end (View), or null
	 *         if the view doesn't represent a valid {@link OccurrenceSpecification}.
	 */
	public static OccurrenceSpecification findSemanticOccurrence(View connectorEnd, String anchorTerminal) {
		EObject semantic = connectorEnd.getElement();
		if (semantic instanceof OccurrenceSpecification) {
			return (OccurrenceSpecification) semantic;
		} else if (semantic instanceof ExecutionSpecification) {
			switch (anchorTerminal) {
			case AnchorConstants.START_TERMINAL:
				return ((ExecutionSpecification) semantic).getStart();
			case AnchorConstants.END_TERMINAL:
				return ((ExecutionSpecification) semantic).getFinish();
			default:
				return null;
			}
		} else if (semantic instanceof Message) {
			switch (anchorTerminal) {
			case AnchorConstants.START_TERMINAL:
				MessageEnd sendEvent = ((Message) semantic).getSendEvent();
				return sendEvent instanceof OccurrenceSpecification ? (OccurrenceSpecification) sendEvent : null;
			case AnchorConstants.END_TERMINAL:
				MessageEnd receiveEvent = ((Message) semantic).getReceiveEvent();
				return receiveEvent instanceof OccurrenceSpecification ? (OccurrenceSpecification) receiveEvent : null;
			default:
				return null;
			}
		}
		return null;
	}

	/**
	 * <p>
	 * Return the target {@link OccurrenceSpecification} represented by this {@link ReconnectRequest}.
	 * </p>
	 * <p>
	 * If there is no {@link OccurrenceSpecification} at this request's location, the default EObject
	 * represented by {@link ReconnectRequest#getTarget()} is returned.
	 * </p>
	 *
	 * @param request
	 * @return
	 */
	public static EObject getOccurrence(ReconnectRequest request) {
		EObject element = EMFHelper.getEObject(request.getTarget());
		IFigure targetFigure = ((IGraphicalEditPart) request.getTarget()).getFigure();
		if (element instanceof Message) {
			if (OccurrenceSpecificationUtil.isSource(targetFigure, request.getLocation())) {
				return ((Message) element).getSendEvent();
			} else {
				return ((Message) element).getReceiveEvent();
			}
		} else if (element instanceof ExecutionSpecification) {
			if (OccurrenceSpecificationUtil.isStart(targetFigure, request.getLocation())) {
				return ((ExecutionSpecification) element).getStart();
			} else {
				return ((ExecutionSpecification) element).getFinish();
			}
		}
		return element;
	}

	/**
	 * <p>
	 * Return the {@link OccurrenceSpecification} that is the {@link Edge#getSource() Source}
	 * of this Edge.
	 * </p>
	 *
	 * @param connection
	 * @return
	 */
	public static EObject getSourceOccurrence(Edge connection) {
		Anchor sourceAnchor = connection.getSourceAnchor();
		if (sourceAnchor instanceof IdentityAnchor) {
			return findSemanticOccurrence(connection.getSource(), ((IdentityAnchor) sourceAnchor).getId());
		}
		return connection.getSource().getElement();
	}

	/**
	 * <p>
	 * Return the {@link OccurrenceSpecification} that is the {@link Edge#getTarget() Target}
	 * of this Edge.
	 * </p>
	 *
	 * @param connection
	 * @return
	 */
	public static EObject getTargetOccurrence(Edge connection) {
		Anchor targetAnchor = connection.getTargetAnchor();
		if (targetAnchor instanceof IdentityAnchor) {
			return findSemanticOccurrence(connection.getTarget(), ((IdentityAnchor) targetAnchor).getId());
		}
		return connection.getTarget().getElement();
	}

}
