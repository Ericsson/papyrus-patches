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
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.handles.ResizableHandleKit;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.tools.ResizeTracker;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IBorderItemEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.figures.IBorderItemLocator;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.FigureUtils;
import org.eclipse.papyrus.uml.diagram.common.editparts.IFloatingLabelEditPart;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.AllowResizeAffixedNodeAlignmentEditPolicy;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.BorderItemResizableEditPolicy;
import org.eclipse.uml2.uml.TimeConstraint;
import org.eclipse.uml2.uml.TimeObservation;

/**
 * Specific execution specification policy to allow specific resize of {@link TimeConstraint} & {@link TimeObservation}.
 */
public class ExecutionSpecificationAffixedChildAlignmentPolicy extends AllowResizeAffixedNodeAlignmentEditPolicy {

	public ExecutionSpecificationAffixedChildAlignmentPolicy() {
		super();
	}

	@Override
	protected EditPolicy createChildEditPolicy(EditPart child) {
		if ((child instanceof IBorderItemEditPart) && !(child instanceof IFloatingLabelEditPart)) {
			BorderItemResizableEditPolicy policy = new BorderItemResizableEditPolicy() {
				@Override
				protected void createResizeHandle(List handles, int direction) {
					if ((getResizeDirections() & direction) == direction) {
						ResizableHandleKit.addHandle((GraphicalEditPart) getHost(),
								handles, direction, getResizeTracker(direction), Cursors
										.getDirectionalCursor(direction, getHostFigure()
												.isMirrored()));
					}
					// no else, otherwise there is some overrride
				}

				@Override
				protected ResizeTracker getResizeTracker(int direction) {
					return new TimeElementResizeTracker((GraphicalEditPart) getHost(), direction);
				}

				@Override
				protected Command getAutoSizeCommand(Request request) {
					return null;
				}

				@Override
				protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
					IBorderItemEditPart borderItemEP = (IBorderItemEditPart) getHost();
					IBorderItemLocator borderItemLocator = borderItemEP.getBorderItemLocator();

					if (borderItemLocator != null) {
						IFigure feedback = getDragSourceFeedbackFigure();
						PrecisionRectangle rect = new PrecisionRectangle(getInitialFeedbackBounds().getCopy());
						EditPart part = borderItemEP.getParent();
						// position is relative to parent
						Point p = ((IGraphicalEditPart) part).getFigure().getBounds().getLocation().getNegated();
						rect.translate(p);
						// scaling not taken into account for feedback
						double scale = FigureUtils.getScale(getHostFigure());
						Point moveDelta = request.getMoveDelta().getCopy();
						moveDelta.scale(1 / scale);
						rect.translate(moveDelta);
						Dimension sizeDelta = request.getSizeDelta().getCopy();
						sizeDelta.scale(1 / scale);
						rect.resize(sizeDelta);
						IFigure borderItemfigure = borderItemEP.getFigure();
						Rectangle realLocation = borderItemLocator.getValidLocation(rect.getCopy(), borderItemfigure);
						getHostFigure().translateToAbsolute(realLocation);
						feedback.translateToRelative(realLocation);
						feedback.setBounds(realLocation);
					}
				}
			};
			policy.setResizeDirections(PositionConstants.EAST | PositionConstants.WEST);
			policy.setDragAllowed(false);
			return policy;
		}
		EditPolicy result = child.getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
		if (result == null) {
			result = new NonResizableEditPolicy();
		}
		return result;

	}

	public class TimeElementResizeTracker extends ResizeTracker {

		public TimeElementResizeTracker(GraphicalEditPart owner, int direction) {
			super(owner, direction);
		}

		@Override
		protected List<EditPart> createOperationSet() {
			// no multi-selection
			return Collections.singletonList(getOwner());
		}

		@Override
		protected Request createSourceRequest() {
			ChangeBoundsRequest request = new ChangeBoundsRequest(REQ_RESIZE);
			request.setConstrainedResize(false);
			request.setResizeDirection(getResizeDirection());
			return request;
		}

		@Override
		protected Command getCommand() {
			return super.getCommand();
		}

		/**
		 * @see org.eclipse.gef.tools.ResizeTracker#updateSourceRequest()
		 *
		 */
		@Override
		protected void updateSourceRequest() {
			super.updateSourceRequest();
		}

		@Override
		protected Dimension getMinimumSizeFor(ChangeBoundsRequest request) {
			return new Dimension(20, 1);
		}

		/**
		 * @see org.eclipse.gef.tools.SimpleDragTracker#showSourceFeedback()
		 *
		 */
		@Override
		protected void showSourceFeedback() {
			super.showSourceFeedback();
		}

	}

}
