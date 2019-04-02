/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, EclipseSource and others.
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

package org.eclipse.papyrus.uml.diagram.sequence.figures;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.geometry.Geometry;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.PapyrusWrappingLabel;
import org.eclipse.papyrus.uml.diagram.common.figure.edge.UMLEdgeFigure;
import org.eclipse.swt.SWT;
import org.eclipse.uml2.uml.DurationConstraint;
import org.eclipse.uml2.uml.DurationObservation;

/**
 * <p>
 * A Figure for Durations ({@link DurationObservation Observation} or {@link DurationConstraint Constraint})
 * represented as an arrow between two events. The figure consists of two horizontal dashed lines,
 * with a vertical arrow between them:
 * </p>
 * <img src="./doc-files/DurationLinkFigure.png" />
 * <p>
 * The horizontal lines are <code>anchored</code> to the source/target of the link. By default, the vertical line will
 * be placed in the middle of the bounds formed by these two anchors, although the figure supports a horizontal delta,
 * to position the arrow closer to one or the other anchor.
 * </p>
 *
 * <p>
 * The figure can also be rotated 90° (i.e. vertical dashed lines and horizontal arrow),
 * when the source and target points are on the same Y coordinate (Typically for horizontal messages)
 * </p>
 */
public class DurationLinkFigure extends UMLEdgeFigure {

	/**
	 * The orientation of the figure changes from the default value {@link Orientation#VERTICAL} to {@link Orientation#HORIZONTAL}
	 * if the difference in pixels between end and start points are no more than this amount of pixels.
	 */
	private static final int ORIENTATION_SWITCH_DIFFERENCE = 30;

	/**
	 * When the arrow is in an {@link Orientation#HORIZONTAL} position, the start line is drawn as a 90° bent line, with
	 * an horizontal segment connected to the start point and a vertical segment. This offset determines the length of
	 * the horizontal segment, in pixels.
	 */
	private static final int HORIZOTAL_ARROW_START_LINE_OFFSET = 15;

	/**
	 * When the arrow is in an {@link Orientation#HORIZONTAL} position, the end line is drawn as a 90° bent line, with
	 * an horizontal segment connected to the end point and a vertical segment. This offset determines the length of
	 * the horizontal segment, in pixels.
	 */
	private static final int HORIZOTAL_ARROW_END_LINE_OFFSET = 15;

	/**
	 * The connecting end and start dashed lines will be drawn slightly further
	 * than the arrow, by this amount of pixels.
	 */
	private static final int ARROW_PADDING = 15;
	private Orientation arrowOrientation = Orientation.VERTICAL;
	private int arrowPositionDelta = 0;
	private PapyrusWrappingLabel durationLabel;

	/**
	 * Thin lines may be difficult to select, so we add a tolerance area around it
	 * to make selection easier.
	 *
	 * @see #containsPoint(int, int)
	 */
	private static final int SELECTION_TOLERANCE = 3;

	public static final String DELTA_VIEW_STYLE = "delta"; //$NON-NLS-1$

	@Override
	protected void outlineShape(Graphics graphics) {
		// Skip super; we're not drawing a polyline connection
		arrowOrientation = computeOptimalOrientation();

		paintStartLine(graphics);
		paintEndLine(graphics);
		paintArrow(graphics);
	}

	/**
	 * Paint the line from this figure to the start point/event (Typically a horizontal line)
	 *
	 * @param graphics
	 */
	protected void paintStartLine(Graphics graphics) {
		graphics.pushState();
		graphics.setLineStyle(SWT.LINE_DASH);
		try {
			PointList startLinePoints = getStartLinePoints();
			graphics.drawPolyline(startLinePoints);
		} finally {
			graphics.popState();
		}
	}

	/** Returns the points for the start line - the line connecting the start point to the arrow. */
	public PointList getStartLinePoints() {
		if (arrowOrientation == Orientation.HORIZONTAL) {
			return getStartLinePointsHorizontal();
		}
		// Orientation.VERTICAL and default case
		return getStartLinePointsVertical();
	}

	private PointList getStartLinePointsHorizontal() {
		PointList points = new PointList(3);

		points.addPoint(getStart());

		Point startOffsetEnd = getStart().getCopy();
		startOffsetEnd.setX(startOffsetEnd.x() + HORIZOTAL_ARROW_START_LINE_OFFSET);
		points.addPoint(startOffsetEnd);

		int arrowYCoordinate = getArrowLineHorizontalY();

		// the vertical segment
		Point startLineEnd = startOffsetEnd.getCopy();
		if (arrowYCoordinate > startOffsetEnd.y) {
			startLineEnd.setY(arrowYCoordinate + ARROW_PADDING);
		} else {
			startLineEnd.setY(arrowYCoordinate - ARROW_PADDING);
		}
		points.addPoint(startLineEnd);
		return points;
	}

	private PointList getStartLinePointsVertical() {
		PointList points = new PointList(2);

		// start
		points.addPoint(getStart());

		// end
		int arrowLinePosition = getArrowLineVerticalX();
		Point startLineEnd = getStart().getCopy();
		if (arrowLinePosition > getStart().x()) {
			startLineEnd.setX(arrowLinePosition + ARROW_PADDING);
		} else {
			startLineEnd.setX(arrowLinePosition - ARROW_PADDING);
		}
		points.addPoint(startLineEnd);
		return points;
	}

	private int getArrowLineVerticalX() {
		if (super.getPoints().size() < 2) {
			// The connection is not configured yet
			return 0;
		}
		return (getStart().x() + getEnd().x()) / 2 + arrowPositionDelta;
	}

	private int getArrowLineVerticalY() {
		return getEnd().y();
	}

	/**
	 * Paint the line from this figure to the end point/event (Typically a horizontal line)
	 *
	 * @param graphics
	 */
	protected void paintEndLine(Graphics graphics) {
		graphics.pushState();
		graphics.setLineStyle(SWT.LINE_DASH);
		try {
			PointList endLinePoints = getEndLinePoints();
			graphics.drawPolyline(endLinePoints);
		} finally {
			graphics.popState();
		}
	}

	/** Returns the points for the end line - the line connecting the end point to the arrow. */
	public PointList getEndLinePoints() {
		if (arrowOrientation == Orientation.HORIZONTAL) {
			return getEndLinePointsHorizontal();
		}
		// Orientation.VERTICAL and default case
		return getEndLinePointsVertical();
	}

	private PointList getEndLinePointsHorizontal() {
		PointList points = new PointList(2);

		points.addPoint(getEnd());
		Point endOffsetEnd = getEnd().getCopy();
		endOffsetEnd.setX(endOffsetEnd.x() - HORIZOTAL_ARROW_END_LINE_OFFSET);
		points.addPoint(endOffsetEnd);
		int arrowYCoordinate = getArrowLineHorizontalY();
		// paint the end line
		Point endLineEnd = endOffsetEnd.getCopy();
		if (arrowYCoordinate < getEnd().y) {
			endLineEnd.setY(arrowYCoordinate - HORIZOTAL_ARROW_END_LINE_OFFSET);
		} else {
			endLineEnd.setY(arrowYCoordinate + HORIZOTAL_ARROW_END_LINE_OFFSET);
		}
		points.addPoint(endLineEnd);

		return points;
	}

	private PointList getEndLinePointsVertical() {
		PointList points = new PointList(2);

		// start
		points.addPoint(getEnd());

		// end
		int arrowLinePosition = getArrowLineVerticalX();
		Point endLineEnd = getEnd().getCopy();
		if (arrowLinePosition < getEnd().x()) {
			endLineEnd.setX(arrowLinePosition - ARROW_PADDING);
		} else {
			endLineEnd.setX(arrowLinePosition + ARROW_PADDING);
		}
		points.addPoint(endLineEnd);
		return points;
	}

	/**
	 * Paint the arrow between the start line and end line (Typically a vertical arrow)
	 *
	 * @param graphics
	 */
	protected void paintArrow(Graphics graphics) {
		PolylineConnection arrowLine = new PolylineConnection();
		arrowLine.setForegroundColor(getForegroundColor());
		arrowLine.setBackgroundColor(getBackgroundColor());
		arrowLine.setLineStyle(getLineStyle());
		arrowLine.setLineWidth(getLineWidth());

		PointList arrowPoints = getArrowLinePoints();
		Point arrowStart = arrowPoints.getFirstPoint();
		Point arrowEnd = arrowPoints.getLastPoint();

		arrowLine.setStart(arrowStart);
		arrowLine.setEnd(arrowEnd);

		decorateArrowLine(arrowLine, arrowStart, arrowEnd);
		arrowLine.paint(graphics);
	}

	/** Returns the points for the arrow line drawn between the and and start lines. */
	public PointList getArrowLinePoints() {
		PointList points = new PointList(2);
		Point arrowStart = null, arrowEnd = null;
		if (arrowOrientation == Orientation.HORIZONTAL) {
			arrowStart = getStart().getCopy().setX(getStart().x() + ARROW_PADDING).setY(getArrowLineHorizontalY());
			arrowEnd = getEnd().getCopy().setX(getEnd().x() - ARROW_PADDING).setY(getArrowLineHorizontalY());
		} else {
			arrowStart = getStart().getCopy().setX(getArrowLineVerticalX());
			arrowEnd = arrowStart.getCopy().setY(getArrowLineVerticalY());
		}
		points.addPoint(arrowStart);
		points.addPoint(arrowEnd);
		return points;
	}

	private int getArrowLineHorizontalY() {
		if (super.getPoints().size() < 2) {
			// The connection is not configured yet
			return 0;
		}
		return (getStart().y() + getEnd().y()) / 2 + arrowPositionDelta;
	}

	/** Adds decorations(e.g. arrow triangles) to the arrow line. */
	protected void decorateArrowLine(PolylineConnection arrowLine, Point arrowStart, Point arrowEnd) {
		// source
		PolylineDecoration source = new PolylineDecoration();
		source.setScale(7 * getLineWidth(), 3 * getLineWidth());
		source.setLineWidth(getLineWidth());
		source.setLocation(arrowStart);
		source.setReferencePoint(arrowEnd);
		arrowLine.setSourceDecoration(source);

		// target
		PolylineDecoration target = new PolylineDecoration();
		target.setScale(7 * getLineWidth(), 3 * getLineWidth());
		target.setLineWidth(getLineWidth());
		target.setLocation(arrowEnd);
		target.setReferencePoint(arrowStart);
		arrowLine.setTargetDecoration(target);
	}


	private Orientation computeOptimalOrientation() {
		if (Math.abs(getStart().y - getEnd().y) < ORIENTATION_SWITCH_DIFFERENCE) {
			return Orientation.HORIZONTAL;
		}
		return Orientation.VERTICAL;
	}


	/**
	 * {@inheritDoc}
	 * <p>
	 * Override containsPoint to handle clicks on any of the 3 lines (start, end and arrow line)
	 * </p>
	 */
	@Override
	public boolean containsPoint(int x, int y) {
		// START LINE
		PointList startLinePoints = getStartLinePoints();
		if (Geometry.polylineContainsPoint(startLinePoints, x, y, SELECTION_TOLERANCE)) {
			return true;
		}

		// END LINE
		PointList endLinePoints = getEndLinePoints();
		if (Geometry.polylineContainsPoint(endLinePoints, x, y, SELECTION_TOLERANCE)) {
			return true;
		}

		// ARROW
		PointList arrowPoints = getArrowLinePoints();
		if (Geometry.polylineContainsPoint(arrowPoints, x, y, SELECTION_TOLERANCE)) {
			return true;
		}

		// Child labels
		@SuppressWarnings("unchecked")
		List<IFigure> children = getChildren();
		return children.stream().anyMatch(child -> child.containsPoint(x, y));
	}


	/**
	 * <p>
	 * By default, the arrow is centered between its start and end point (delta = 0). The position
	 * delta can be used to move it to the right (delta > 0) or to the left (delta < 0).
	 * </p>
	 *
	 * @param delta
	 */
	public void setArrowPositionDelta(int delta) {
		if (delta != this.arrowPositionDelta) {
			this.arrowPositionDelta = delta;
			revalidate();
		}
	}

	/** Returns the arrow position delta.
	 * <p>
	 * By default, the arrow is centered between its start and end point (delta = 0). The position
	 * delta can be used to move it to the right (delta > 0) or to the left (delta < 0).
	 * </p>*/
	public int getArrowPositionDelta() {
		return arrowPositionDelta;
	}

	@Override
	public Rectangle getBounds() {
		Rectangle bounds = super.getBounds();

		// The arrow may be moved outside of the bounds defined by (start, end).
		// In that case, we need to update the bounds, to make sure we can draw
		// everything
		if (getPoints().size() >= 2) {
			PointList allPoints = new PointList();
			allPoints.addAll(getStartLinePoints());
			allPoints.addAll(getEndLinePoints());
			allPoints.addAll(getArrowLinePoints());
			bounds.union(allPoints.getBounds());
		}
		return bounds;
	}

	@Override
	public void setConnectionRouter(ConnectionRouter cr) {
		// Skip; this figure doesn't support routers/bendpoints
	}

	/**
	 * @return the arrowOrientation
	 */
	public Orientation getArrowOrientation() {
		return arrowOrientation;
	}

	public static enum Orientation {
		VERTICAL, HORIZONTAL;
	}

	public WrappingLabel getDurationLabelFigure() {
		return this.durationLabel;
	}


	@Override
	protected void createContents() {
		super.createContents();
		this.durationLabel = new PapyrusWrappingLabel();
		this.durationLabel.setText(""); //$NON-NLS-1$
		add(this.durationLabel);
	}

	@Override
	public Object getRoutingConstraint() {
		// Bendpoints should at least contain the start and end points; otherwise the
		// bendpoint policy will crash. We don't support bendpoints, so just return
		// a new list everytime.
		List<Bendpoint> list = new ArrayList<>();
		list.add(this::getStart);
		list.add(this::getEnd);
		return list;
	}
}
