/*****************************************************************************
 * Copyright (c) 2016-2017 CEA LIST and others.
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
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 521312
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.LocationRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest.ConnectionViewDescriptor;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeConnectionRequest;
import org.eclipse.gmf.runtime.draw2d.ui.figures.BaseSlidableAnchor;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.helpers.AnchorHelper;
import org.eclipse.papyrus.uml.service.types.element.UMLDIElementTypes;




/**
 * @author Patrick Tessier
 * @since 3.0
 *        this class has been customized to prevent the strange feedback of lifeline during the move
 *
 */
public class CInteractionEditPart extends InteractionEditPart {

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CInteractionEditPart(View view) {
		super(view);
	}


	// ***********************************************************************
	// **ALL this code is used to manage LOST and CREATE MESSAGE on Interaction.
	// **************************************************************************
	/**
	 * Handle found message
	 */
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		ConnectionAnchor sourceAnchor = createAnchor(request, UMLDIElementTypes.MESSAGE_FOUND_EDGE, MessageFoundEditPart.VISUAL_ID, MessageFoundEditPart.class);

		if (sourceAnchor == null) {
			sourceAnchor = super.getSourceConnectionAnchor(request);
		}
		return sourceAnchor;
	}

	/**
	 * Handle found message
	 */
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connEditPart) {

		// Read the anchor and load it, it read the absolute position
		if (connEditPart instanceof MessageFoundEditPart) {
			String terminal = AnchorHelper.getAnchorId(getEditingDomain(), connEditPart, true);
			if (terminal.length() > 0) {
				PrecisionPoint pt = BaseSlidableAnchor.parseTerminalString(terminal);
				return new AnchorHelper.InnerPointAnchor(getFigure(), pt);
			}
		}
		ConnectionAnchor sourceConnectionAnchor = super.getSourceConnectionAnchor(connEditPart);
		return sourceConnectionAnchor;
	}

	/**
	 * Handle lost message
	 */
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {

		ConnectionAnchor targetAnchor = createAnchor(request, UMLDIElementTypes.MESSAGE_LOST_EDGE, MessageLostEditPart.VISUAL_ID, MessageLostEditPart.class);
		if (targetAnchor == null) {
			targetAnchor = super.getTargetConnectionAnchor(request);
		}
		return targetAnchor;
	}

	/**
	 * Handle lost message
	 */
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connEditPart) {
		// Enabled to find Anchor for MessageCreate, this would be useful when showing feedbacks.
		// Fixed bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=403134
		if (connEditPart instanceof MessageLostEditPart || connEditPart instanceof MessageCreateEditPart) {
			String terminal = AnchorHelper.getAnchorId(getEditingDomain(), connEditPart, false);
			if (terminal.length() > 0) {
				PrecisionPoint pt = BaseSlidableAnchor.parseTerminalString(terminal);
				return new AnchorHelper.InnerPointAnchor(getFigure(), pt);
			}
		}
		ConnectionAnchor targetConnectionAnchor = super.getTargetConnectionAnchor(connEditPart);
		return targetConnectionAnchor;
	}

	/**
	 * Create Anchor
	 *
	 * @param request
	 *            The request
	 * @param elementType
	 *            The element type of the message
	 * @param visualId
	 *            The visual ID of the message
	 * @param messageType
	 *            The type of the message
	 * @return The connection anchor
	 */
	private ConnectionAnchor createAnchor(Request request, IElementType elementType, String visualId, Class<?> messageType) {
		if (request instanceof CreateUnspecifiedTypeConnectionRequest) {

			CreateUnspecifiedTypeConnectionRequest createRequest = (CreateUnspecifiedTypeConnectionRequest) request;

			List<?> relationshipTypes = createRequest.getElementTypes();
			for (Object obj : relationshipTypes) {
				if (elementType.equals(obj)) {
					return createAnchor(createRequest.getLocation().getCopy());
				}
			}
		} else if (request instanceof CreateConnectionViewRequest) {
			CreateConnectionViewRequest createRequest = (CreateConnectionViewRequest) request;
			ConnectionViewDescriptor connectionViewDescriptor = createRequest.getConnectionViewDescriptor();
			if (connectionViewDescriptor != null) {
				if (visualId.equals(connectionViewDescriptor.getSemanticHint())) {
					return createAnchor(createRequest.getLocation().getCopy());
				}
			}
		} else if (request instanceof ReconnectRequest) {
			ReconnectRequest reconnectRequest = (ReconnectRequest) request;
			ConnectionEditPart connectionEditPart = reconnectRequest.getConnectionEditPart();
			// Fixed bug creating anchors for MessageLost and MessageFound.
			if (messageType.isInstance(connectionEditPart) && request instanceof LocationRequest) {
				return createAnchor(((LocationRequest) request).getLocation().getCopy());
			}
		}
		return null;
	}

	/**
	 * Create Anchor
	 *
	 * @param location
	 *            The location
	 * @return The connection anchor
	 */
	private ConnectionAnchor createAnchor(Point location) {
		return AnchorHelper.InnerPointAnchor.createAnchorAtLocation(getFigure(), new PrecisionPoint(location));
	}

	/**
	 * {@inheritDoc}
	 *
	 * <pre>
	 * Interaction is not selectable.
	 * </pre>
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#isSelectable()
	 */
	@Override
	public boolean isSelectable() {
		return false;
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#showTargetFeedback(org.eclipse.gef.Request)
	 *
	 * @param request
	 */
	@Override
	public void showTargetFeedback(Request request) {
		if (request instanceof ChangeBoundsRequest) {
			ChangeBoundsRequest changeBoundsRequest = (ChangeBoundsRequest) request;

			if (changeBoundsRequest.getEditParts().get(0) instanceof LifelineEditPart) {
				changeBoundsRequest.setMoveDelta(new Point(changeBoundsRequest.getMoveDelta().x, 0));
			}
		}
		super.showTargetFeedback(request);
	}

}
