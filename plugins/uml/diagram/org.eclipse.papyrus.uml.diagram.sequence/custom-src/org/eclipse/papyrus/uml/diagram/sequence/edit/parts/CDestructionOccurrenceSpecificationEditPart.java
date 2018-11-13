/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, EclipseSource, Christian W. Damus, and others.
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
 *   Christian W. Damus - bug 536486
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IBorderItemEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeConnectionRequest;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.anchors.CenterAnchor;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.DestructionOccurrenceGraphicalNodeEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.figures.DestructionEventNodePlate;
import org.eclipse.papyrus.uml.diagram.sequence.locator.TimeElementLocator;
import org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes;
import org.eclipse.papyrus.uml.diagram.sequence.util.DurationLinkUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.GeneralOrderingUtil;

public class CDestructionOccurrenceSpecificationEditPart extends DestructionOccurrenceSpecificationEditPart {

	public CDestructionOccurrenceSpecificationEditPart(View view) {
		super(view);
	}

	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new DestructionOccurrenceGraphicalNodeEditPolicy());
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		if (request instanceof CreateUnspecifiedTypeConnectionRequest) {
			CreateUnspecifiedTypeConnectionRequest createRequest = (CreateUnspecifiedTypeConnectionRequest) request;
			List<?> relationshipTypes = createRequest.getElementTypes();
			for (Object type : relationshipTypes) {
				if (UMLElementTypes.DurationConstraint_Edge.equals(type) || UMLElementTypes.DurationObservation_Edge.equals(type) || UMLElementTypes.GeneralOrdering_Edge.equals(type)) {
					return new CenterAnchor(getFigure());
				}
			}
		} else if (request instanceof CreateConnectionViewRequest) {
			CreateConnectionViewRequest createRequest = (CreateConnectionViewRequest) request;
			if (DurationLinkUtil.isDurationLink(createRequest) || GeneralOrderingUtil.isGeneralOrderingLink(createRequest)) {
				return new CenterAnchor(getFigure());
			}
		} else if (request instanceof ReconnectRequest) {
			ReconnectRequest reconnectRequest = (ReconnectRequest) request;
			if (DurationLinkUtil.isDurationLink(reconnectRequest) || GeneralOrderingUtil.isGeneralOrderingLink(reconnectRequest)) {
				return new CenterAnchor(getFigure());
			}
		}
		return super.getTargetConnectionAnchor(request);
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		if (request instanceof CreateUnspecifiedTypeConnectionRequest) {
			CreateUnspecifiedTypeConnectionRequest createRequest = (CreateUnspecifiedTypeConnectionRequest) request;
			List<?> relationshipTypes = createRequest.getElementTypes();
			for (Object type : relationshipTypes) {
				if (UMLElementTypes.DurationConstraint_Edge.equals(type) || UMLElementTypes.DurationObservation_Edge.equals(type) || UMLElementTypes.GeneralOrdering_Edge.equals(type)) {
					return new CenterAnchor(getFigure());
				}
			}
		} else if (request instanceof CreateConnectionViewRequest) {
			CreateConnectionViewRequest createRequest = (CreateConnectionViewRequest) request;
			if (DurationLinkUtil.isDurationLink(createRequest) || GeneralOrderingUtil.isGeneralOrderingLink(createRequest)) {
				return new CenterAnchor(getFigure());
			}
		}
		return super.getSourceConnectionAnchor(request);
	}

	@Override
	protected NodeFigure createNodePlate() {
		// Use a custom NodePlate to support the Destruction's CenterAnchor
		DestructionEventNodePlate nodePlate = new DestructionEventNodePlate(40, 40);
		return nodePlate;
	}

	@Override
	protected void addBorderItem(IFigure borderItemContainer, IBorderItemEditPart borderItemEditPart) {
		if (borderItemEditPart instanceof TimeConstraintBorderNodeEditPart
				|| borderItemEditPart instanceof TimeObservationBorderNodeEditPart) {
			borderItemContainer.add(borderItemEditPart.getFigure(), new TimeElementLocator(getMainFigure(),
					__ -> PositionConstants.CENTER));
		} else {
			super.addBorderItem(borderItemContainer, borderItemEditPart);
		}
	}

}
