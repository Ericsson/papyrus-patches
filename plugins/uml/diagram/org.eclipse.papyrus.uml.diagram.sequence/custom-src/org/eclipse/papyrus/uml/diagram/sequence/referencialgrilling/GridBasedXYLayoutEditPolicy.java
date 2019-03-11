/*****************************************************************************
 * Copyright (c) 2016 - 2017 CEA LIST and others.
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
 *   Celine Janssens (ALL4TEC) celine.janssens@all4tec.net - Bug 520154
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import java.util.Set;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.INodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.XYLayoutWithConstrainedResizedEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.FixAnchorHelper;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetMoveAllLineAtSamePositionCommand;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;

/**
 * This class is over load the creation of element and to avoid to move element directly at creation
 * this class has been customized to prevent the strange feedback of lifeline during the move
 *
 */
public class GridBasedXYLayoutEditPolicy extends XYLayoutWithConstrainedResizedEditPolicy implements IGrillingEditpolicy {
	/**
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.XYLayoutWithConstrainedResizedEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
	 *
	 * @param request
	 * @return
	 */
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		DiagramEditPart diagramEditPart = getDiagramEditPart(getHost());
		GridManagementEditPolicy grid = (GridManagementEditPolicy) diagramEditPart.getEditPolicy(GridManagementEditPolicy.GRID_MANAGEMENT);
		if (grid != null) {
			if (request instanceof CreateViewAndElementRequest) {
				String semanticHint = ((CreateViewAndElementRequest) request).getViewAndElementDescriptor().getSemanticHint();
				// do let the user place where he want the life line at the creation
				if (semanticHint.equals(LifelineEditPart.VISUAL_ID)) {
					request.setLocation(new Point(request.getLocation().x, 40));
				}
			}
			CompoundCommand cmd = new CompoundCommand();
			SetMoveAllLineAtSamePositionCommand setMoveAllLineAtSamePositionCommand = new SetMoveAllLineAtSamePositionCommand(grid, false);
			cmd.add(setMoveAllLineAtSamePositionCommand);
			cmd.add(super.getCreateCommand(request));
			setMoveAllLineAtSamePositionCommand = new SetMoveAllLineAtSamePositionCommand(grid, true);
			cmd.add(setMoveAllLineAtSamePositionCommand);
			return cmd;
		}
		return super.getCreateCommand(request);
	}

	/**
	 * Override to use to deal with causes where the point is UNDERFINED
	 * we will ask the layout helper to find a location for us
	 *
	 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#getConstraintFor(org.eclipse.gef.requests.CreateRequest)
	 */
	@Override
	protected Object getConstraintFor(CreateRequest request) {
		// Used during the creation from the palette
		Object constraint = super.getConstraintFor(request);
		if (constraint instanceof Rectangle) {
			if (request instanceof CreateViewAndElementRequest) {
				String semanticHint = ((CreateViewAndElementRequest) request).getViewAndElementDescriptor().getSemanticHint();
				// do let the user place where he want the life line at the creation
				if (semanticHint.equals(LifelineEditPart.VISUAL_ID)) {
					((Rectangle) constraint).setY(10);
				}
			}
		}

		return constraint;
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.XYLayoutWithConstrainedResizedEditPolicy#getHelper()
	 *
	 * @return
	 */
	@Override
	public FixAnchorHelper getHelper() {
		FixAnchorHelper helper = new FixAnchorHelper(getEditingDomain()) {


			/**
			 *
			 * In the case of Sequence Diagram, the Edge are managed by other mechanisms.
			 * The Command then should do nothing.
			 *
			 * @see org.eclipse.papyrus.infra.gmfdiag.common.helper.FixAnchorHelper#getFixAnchorCommand(org.eclipse.gmf.runtime.diagram.ui.editparts.INodeEditPart, org.eclipse.draw2d.geometry.PrecisionRectangle, org.eclipse.draw2d.geometry.PrecisionRectangle,
			 *      org.eclipse.gef.editparts.AbstractConnectionEditPart, org.eclipse.draw2d.geometry.Point, org.eclipse.draw2d.geometry.Dimension, boolean)
			 */
			@Override
			public Command getFixAnchorCommand(INodeEditPart nodeEditPart, PrecisionRectangle oldNodeBounds, PrecisionRectangle newNodeBounds, AbstractConnectionEditPart targetConnectionEP, Point move, Dimension sizeDelta, boolean fixSource) {

				return null;
			}

			/**
			 * @see org.eclipse.papyrus.infra.gmfdiag.common.helper.FixAnchorHelper#getFixIdentityAnchorCommand(org.eclipse.gmf.runtime.diagram.ui.editparts.INodeEditPart, org.eclipse.draw2d.geometry.Point, org.eclipse.draw2d.geometry.Dimension, int)
			 *
			 *      In the case of Sequence Diagram, the Edge are managed by other mechanisms.
			 *      The Command then should do nothing.
			 */
			@Override
			public Command getFixIdentityAnchorCommand(INodeEditPart node, Point move, Dimension sizeDelta, int moveDirection) {

				return null;
			}
		};

		return helper;
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.XYLayoutWithConstrainedResizedEditPolicy#getMoveChildrenFixEdgeAnchorCommand(java.util.Set)
	 *
	 *      In the case of Sequence Diagram, the Edge are managed by other mechanisms.
	 *      The Command then should do nothing.
	 * @param notBeingMovedConnections
	 * @return null
	 */
	@Override
	protected ICommandProxy getMoveChildrenFixEdgeAnchorCommand(Set<Object> notBeingMovedConnections) {

		return null;
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.XYLayoutWithConstrainedResizedEditPolicy#createChangeConstraintCommand(org.eclipse.gef.requests.ChangeBoundsRequest, org.eclipse.gef.EditPart, java.lang.Object)
	 *
	 * @param request
	 * @param child
	 * @param constraint
	 * @return
	 */
	@Override
	protected Command createChangeConstraintCommand(ChangeBoundsRequest request, EditPart child, Object constraint) {
		if (child instanceof LifelineEditPart && constraint instanceof Rectangle) {
			Rectangle rect = (Rectangle) constraint;
			rect.y = ((Bounds) ((Node) ((LifelineEditPart) child).getNotationView()).getLayoutConstraint()).getY();
			return super.createChangeConstraintCommand(request, child, rect);
		}
		return super.createChangeConstraintCommand(request, child, constraint);
	}

	/**
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#showLayoutTargetFeedback(org.eclipse.gef.Request)
	 *
	 * @param request
	 */
	@Override
	protected void showLayoutTargetFeedback(Request request) {

		if (request instanceof ChangeBoundsRequest) {
			ChangeBoundsRequest changeBoundsRequest = (ChangeBoundsRequest) request;

			if (changeBoundsRequest.getEditParts().get(0) instanceof LifelineEditPart) {
				changeBoundsRequest.setMoveDelta(new Point(changeBoundsRequest.getMoveDelta().x, 0));
			}
		}
		super.showLayoutTargetFeedback(request);
	}
}

