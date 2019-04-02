/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.handles.ConnectionHandle;
import org.eclipse.gef.handles.SquareHandle;
import org.eclipse.gef.tools.SelectEditPartTracker;
import org.eclipse.gef.tools.SimpleDragTracker;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.Connector;
import org.eclipse.gmf.runtime.notation.IntValueStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.PapyrusConnectionEndEditPolicy;
import org.eclipse.papyrus.uml.diagram.common.editparts.UMLConnectionNodeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationConstraintLinkEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.figures.DurationLinkFigure;
import org.eclipse.papyrus.uml.diagram.sequence.figures.DurationLinkFigure.Orientation;
import org.eclipse.papyrus.uml.diagram.sequence.locator.IntersectionPointSelectionLocator;
import org.eclipse.papyrus.uml.diagram.sequence.requests.MoveArrowRequest;
import org.eclipse.swt.graphics.Cursor;

/**
 * Edit policy for the selection handles of a {@link DurationConstraintLinkEditPart}
 */
public class DurationLinkSelectionHandlesEditPolicy extends PapyrusConnectionEndEditPolicy implements PropertyChangeListener {
	private UMLConnectionNodeEditPart durationLinkEditPart;
	private TransactionalEditingDomain editingDomain;
	private Integer arrowPositionDelta;

	public DurationLinkSelectionHandlesEditPolicy(UMLConnectionNodeEditPart durationLinkEditPart, TransactionalEditingDomain editingDomain) {
		this.durationLinkEditPart = durationLinkEditPart;
		this.editingDomain = editingDomain;
	}

	@Override
	public void setHost(EditPart host) {
		super.setHost(host);
		if (getHost() != null) {
			getHostFigure().addPropertyChangeListener(Connection.PROPERTY_POINTS, this);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<?> createSelectionHandles() {
		List<Handle> handles = new ArrayList<>();
		handles.addAll((Collection<? extends Handle>) super.createSelectionHandles());
		addSelectionHandles(handles);
		addMoveHandles(handles);
		return handles;
	}

	private void addMoveHandles(List<Handle> list) {
		// middle of the arrow line
		DurationLinkFigure figure = ((DurationLinkFigure) getHostFigure());
		Cursor cursor = getCursor(figure);
		Handle moveHandle = new SquareHandle((ConnectionEditPart) getHost(), new CustomMoveHandleLocator(figure), cursor) {
			@Override
			protected DragTracker createDragTracker() {
				return new ArrowLineMoveTracker();
			}
		};

		list.add(moveHandle);
	}

	@Override
	public void showSourceFeedback(Request request) {
		if (MoveArrowRequest.REQ_MOVE_ARROW.equals(request.getType())) {
			showArrowMoveFeedback((MoveArrowRequest) request);
		}
		super.showSourceFeedback(request);
	}

	@Override
	public void eraseSourceFeedback(Request request) {
		if (MoveArrowRequest.REQ_MOVE_ARROW.equals(request.getType())) {
			eraseArrowMoveFeedback((MoveArrowRequest) request);
		}
		super.eraseSourceFeedback(request);
	}

	private void eraseArrowMoveFeedback(MoveArrowRequest request) {
		arrowPositionDelta = null;
		getHost().refresh();
	}

	protected void showArrowMoveFeedback(MoveArrowRequest request) {
		DurationLinkFigure figure = (DurationLinkFigure) durationLinkEditPart.getFigure();
		if (arrowPositionDelta == null) {
			arrowPositionDelta = figure.getArrowPositionDelta();
		}
		PointList arrowLinePoints = figure.getArrowLinePoints();
		Point arrowPoint = arrowLinePoints.getMidpoint().getCopy();

		figure.translateToAbsolute(arrowPoint);
		arrowPoint.translate(request.getMoveDelta());
		figure.translateToRelative(arrowPoint);

		Dimension moveDelta = arrowPoint.getDifference(arrowLinePoints.getMidpoint());

		Orientation arrowOrientation = request.getArrowOrientation();
		if (arrowOrientation == Orientation.VERTICAL) {
			figure.setArrowPositionDelta(arrowPositionDelta + moveDelta.width);
		} else {
			// horizontal
			figure.setArrowPositionDelta(arrowPositionDelta + moveDelta.height);
		}
	}

	private Cursor getCursor(DurationLinkFigure figure) {
		if (figure.getArrowOrientation() == Orientation.VERTICAL) {
			return Cursors.SIZEWE;
		}
		return Cursors.SIZENS;
	}

	private void addSelectionHandles(List<Handle> list) {
		DurationLinkFigure figure = ((DurationLinkFigure) getHostFigure());

		PointList arrowLinePoints = figure.getArrowLinePoints();
		PointList startLinePoints = figure.getStartLinePoints();
		PointList endLinePoints = figure.getEndLinePoints();
		// intersection between start and arrow line
		list.add(new DurationConstraintArrowSelectionHandle(true, durationLinkEditPart, figure, startLinePoints, arrowLinePoints));
		// intersection between end and arrow line
		list.add(new DurationConstraintArrowSelectionHandle(true, durationLinkEditPart, figure, endLinePoints, arrowLinePoints));
	}

	protected SelectEditPartTracker getSelectTracker() {
		return new SelectEditPartTracker(getHost());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// refresh selection handles when the points property changes
		if (getHost().getSelected() != EditPart.SELECTED_NONE) {
			addSelectionHandles();
		}
	}

	class CustomMoveHandleLocator extends ConnectionLocator {

		/**
		 * Constructor.
		 *
		 * @param connection
		 */
		public CustomMoveHandleLocator(Connection connection) {
			super(connection);
		}

		@Override
		protected Point getLocation(PointList points) {
			DurationLinkFigure figure = (DurationLinkFigure) getConnection();
			PointList arrowLinePoints = figure.getArrowLinePoints();
			return arrowLinePoints.getMidpoint();
		}

	}

	class DurationConstraintArrowSelectionHandle extends ConnectionHandle {

		DurationConstraintArrowSelectionHandle(boolean fixed, org.eclipse.gef.GraphicalEditPart owner, Connection connection, PointList pointList1, PointList pointList2) {
			super(fixed);
			setOwner(owner);
			setLocator(new IntersectionPointSelectionLocator(connection, pointList1, pointList2));
		}

		@Override
		protected DragTracker createDragTracker() {
			return new SimpleDragTracker() {
				@Override
				protected String getCommandName() {
					return RequestConstants.REQ_SELECTION;
				}
			};
		}
	}

	class ArrowLineMoveTracker extends SimpleDragTracker {

		@Override
		protected String getCommandName() {
			return MoveArrowRequest.REQ_MOVE_ARROW;
		}

		@Override
		protected void updateSourceRequest() {
			super.updateSourceRequest();
			MoveArrowRequest request = (MoveArrowRequest) getSourceRequest();
			DurationLinkFigure figure = (DurationLinkFigure) durationLinkEditPart.getPrimaryShape();
			request.setArrowOrientation(figure.getArrowOrientation());
			Point location = new Point(getLocation());
			request.setLocation(location);
			Dimension dragMoveDelta = getDragMoveDelta();
			Point moveDelta = new Point(0, 0);
			moveDelta.y += dragMoveDelta.height;
			moveDelta.x += dragMoveDelta.width;
			request.setMoveDelta(moveDelta);
		}

		@Override
		protected Request createSourceRequest() {
			return new MoveArrowRequest();
		}

		@Override
		protected Command getCommand() {
			Request request = getSourceRequest();

			if (request instanceof MoveArrowRequest) {
				ICommand moveArrowCommand = new AbstractTransactionalCommand(editingDomain, "Move arrow", null) {

					@Override
					protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
						Connector connector = (Connector) durationLinkEditPart.getNotationView();

						@SuppressWarnings("unchecked")
						Optional<IntValueStyle> deltaOptional = connector.getStyles().stream().filter(IntValueStyle.class::isInstance).filter(style -> DurationLinkFigure.DELTA_VIEW_STYLE.equals(((IntValueStyle) style).getName())).findFirst();
						IntValueStyle deltaStyle = deltaOptional.orElseGet(() -> {
							IntValueStyle style = (IntValueStyle) connector.createStyle(NotationPackage.eINSTANCE.getIntValueStyle());
							style.setName(DurationLinkFigure.DELTA_VIEW_STYLE);
							return style;
						});

						DurationLinkFigure figure = ((DurationLinkFigure) durationLinkEditPart.getFigure());
						PointList arrowLinePoints = figure.getArrowLinePoints();
						Point arrowPoint = arrowLinePoints.getMidpoint().getCopy();

						figure.translateToAbsolute(arrowPoint);
						arrowPoint.translate(((MoveArrowRequest) request).getMoveDelta());
						figure.translateToRelative(arrowPoint);

						Dimension moveDelta = arrowPoint.getDifference(arrowLinePoints.getMidpoint());

						Orientation arrowOrientation = ((MoveArrowRequest) request).getArrowOrientation();
						if (arrowOrientation == Orientation.VERTICAL) {
							deltaStyle.setIntValue(deltaStyle.getIntValue() + moveDelta.width);
						} else {
							// horizontal
							deltaStyle.setIntValue(deltaStyle.getIntValue() + moveDelta.height);
						}
						return CommandResult.newOKCommandResult();
					}
				};
				return new ICommandProxy(moveArrowCommand);
			}
			return super.getCommand();
		}

	}

}