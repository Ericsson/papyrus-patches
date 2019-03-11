/*****************************************************************************
 * Copyright (c) 2016 CEA LIST and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mickael ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and Implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.utils;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.draw2d.ui.geometry.LineSeg;

/**
 * Utility Class for rounded rectangle.
 *
 */
public class RoundedRectangleUtil {

	/**
	 * Computes the intersections of a line segment with a rounded rectangle.
	 * 
	 * @param line
	 *            the line segment
	 * @param rectX
	 *            the x-coordinate of the rounded rectangle
	 * @param rectY
	 *            the y-coordinate of the rounded rectangle
	 * @param rectW
	 *            the width of the rounded rectangle
	 * @param rectH
	 *            the height of the rounded rectangle
	 * @param arcW
	 *            the arc width of the rounded rectangle
	 * @param arcH
	 *            the arc height of the rounded rectangle
	 * @return the intersections
	 */
	public static PointList getLineIntersectionsWithRoundedRectangle(final LineSeg line, final int rectX, final int rectY, final int rectW, final int rectH, final int arcW, final int arcH) {
		PointList intersections = new PointList();
		PointList rect;
		PointList rectIntersections;
		// intersection with top line segment
		rect = new PointList();
		rect.addPoint(new PrecisionPoint(rectX + arcW / 2, rectY));
		rect.addPoint(new PrecisionPoint(rectX + rectW - arcW / 2, rectY));
		rectIntersections = line.getLineIntersectionsWithLineSegs(rect);
		if (rectIntersections.size() > 0) {
			intersections.addPoint(rectIntersections.getFirstPoint());
		}
		// intersection with bottom line segment
		rect = new PointList();
		rect.addPoint(new PrecisionPoint(rectX + arcW / 2, rectY + rectH));
		rect.addPoint(new PrecisionPoint(rectX + rectW - arcW / 2, rectY + rectH));
		rectIntersections = line.getLineIntersectionsWithLineSegs(rect);
		if (rectIntersections.size() > 0) {
			intersections.addPoint(rectIntersections.getFirstPoint());
		}
		// intersection with left line segment
		rect = new PointList();
		rect.addPoint(new PrecisionPoint(rectX, rectY + arcH / 2));
		rect.addPoint(new PrecisionPoint(rectX, rectY + rectH - arcH / 2));
		rectIntersections = line.getLineIntersectionsWithLineSegs(rect);
		if (rectIntersections.size() > 0) {
			intersections.addPoint(rectIntersections.getFirstPoint());
		}
		// intersection with right line segment
		rect = new PointList();
		rect.addPoint(new PrecisionPoint(rectX + rectW, rectY + arcH / 2));
		rect.addPoint(new PrecisionPoint(rectX + rectW, rectY + rectH - arcH / 2));
		rectIntersections = line.getLineIntersectionsWithLineSegs(rect);
		if (rectIntersections.size() > 0) {
			intersections.addPoint(rectIntersections.getFirstPoint());
		}
		PointList ellipseIntersections;
		// intersection with top left ellipse
		ellipseIntersections = line.getLineIntersectionsWithEllipse(new Rectangle(rectX, rectY, arcW, arcH));
		for (int i = 0; i < ellipseIntersections.size(); ++i) {
			Point point = ellipseIntersections.getPoint(i);
			if (point.x <= rectX + arcW / 2 && point.y <= rectY + arcH / 2) {
				intersections.addPoint(point);
			}
		}
		// intersection with top right ellipse
		ellipseIntersections = line.getLineIntersectionsWithEllipse(new Rectangle(rectX + rectW - arcW, rectY, arcW, arcH));
		for (int i = 0; i < ellipseIntersections.size(); ++i) {
			Point point = ellipseIntersections.getPoint(i);
			if (point.x >= rectX + rectW - arcW / 2 && point.y <= rectY + arcH / 2) {
				intersections.addPoint(point);
			}
		}
		// intersection with bottom left ellipse
		ellipseIntersections = line.getLineIntersectionsWithEllipse(new Rectangle(rectX, rectY + rectH - arcH, arcW, arcH));
		for (int i = 0; i < ellipseIntersections.size(); ++i) {
			Point point = ellipseIntersections.getPoint(i);
			if (point.x <= rectX + arcW / 2 && point.y >= rectY + rectH - arcH / 2) {
				intersections.addPoint(point);
			}
		}
		// intersection with bottom right ellipse
		ellipseIntersections = line.getLineIntersectionsWithEllipse(new Rectangle(rectX + rectW - arcW, rectY + rectH - arcH, arcW, arcH));
		for (int i = 0; i < ellipseIntersections.size(); ++i) {
			Point point = ellipseIntersections.getPoint(i);
			if (point.x >= rectX + rectW - arcW / 2 && point.y >= rectY + rectH - arcH / 2) {
				intersections.addPoint(point);
			}
		}
		// this should always be true
		if (intersections.size() == 2) {
			// order the list so the point that is closer to the origin comes
			// first
			Point point1 = intersections.getLastPoint();
			Point point2 = intersections.getFirstPoint();
			int deltaX1 = point1.x - line.getTerminus().x;
			int deltaY1 = point1.y - line.getTerminus().y;
			int deltaX2 = point2.x - line.getTerminus().x;
			int deltaY2 = point2.y - line.getTerminus().y;
			if (deltaX1 * deltaX1 + deltaY1 * deltaY1 < deltaX2 * deltaX2 + deltaY2 * deltaY2) {
				intersections.removePoint(0);
			} else {
				intersections.removePoint(1);
			}
		}
		return intersections;
	}

	/**
	 * Get the position of the location relative to a rounded rectangle.
	 * 
	 * @param rectangle
	 *            The parent rectangle.
	 * @param cornerDimension
	 *            The dimension of rectangle corners
	 * @param location
	 *            The location of the position.
	 * @return The position as a {@link PositionConstants}
	 */
	public static int getPosition(final Rectangle rectangle, final Dimension cornerDimension, final Point location) {
		int position = PositionConstants.NONE;

		// Get the position of the proposed location on the parent figure
		final Rectangle maxRect = rectangle.getCopy();
		while (maxRect.contains(location)) {
			maxRect.shrink(1, 1);
		}

		Rectangle shrinkedParent = new Rectangle(rectangle);
		shrinkedParent.shrink(cornerDimension.width / 2, cornerDimension.height / 2);

		if (location.x < shrinkedParent.getTopLeft().x && location.y < shrinkedParent.getTopLeft().y) {
			position = PositionConstants.NORTH_WEST;
		} else if (location.x > shrinkedParent.getTopRight().x && location.y < shrinkedParent.getTopRight().y) {
			position = PositionConstants.NORTH_EAST;
		} else if (location.x > shrinkedParent.getBottomRight().x && location.y > shrinkedParent.getBottomRight().y) {
			position = PositionConstants.SOUTH_EAST;
		} else if (location.x < shrinkedParent.getBottomLeft().x && location.y > shrinkedParent.getBottomLeft().y) {
			position = PositionConstants.SOUTH_WEST;
		} else {
			// not on a corner
			position = maxRect.getPosition(location);
		}
		return position;
	}

}
