/**
 * Copyright (c) 2017 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
  *  CEA LIST - Initial API and implementation
 */
package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PolylineShape;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.common.draw2d.CenterLayout;
import org.eclipse.papyrus.uml.diagram.common.draw2d.LinesBorder;
import org.eclipse.swt.SWT;

public class DurationConstraintFigure extends RectangleFigure {



	/**
	 * The delta number of pixel to paint the Arrow.
	 */
	private static final int ARROW_SIZE = 10;

	/**
	 *
	 * Constructor.
	 *
	 */
	public DurationConstraintFigure() {
		CenterLayout layoutThis = new CenterLayout();
		this.setLayoutManager(layoutThis);

	}

	/**
	 * Create and display the top and bottom line of the figure.
	 *
	 * @return the created Border
	 */
	private Border createBorder() {
		LinesBorder result = new LinesBorder();
		result.setSides(PositionConstants.TOP | PositionConstants.BOTTOM);
		result.setStyle(SWT.BORDER_DASH);
		return result;
	}


	/**
	 * @see org.eclipse.draw2d.Figure#paint(org.eclipse.draw2d.Graphics)
	 *
	 * @param graphics
	 */
	@Override
	public void paint(Graphics graphics) {
		super.paint(graphics);
		Rectangle rect = this.getBounds();
		graphics.pushState();
		graphics.setForegroundColor(getForegroundColor());
		Point top = new Point(rect.getTop());
		Point bottom = new Point(rect.getBottom());
		graphics.drawLine(top, bottom);
		// draw arrows

		// Top Arrow
		Point left = new Point(top);
		left = left.getTranslated(-ARROW_SIZE, ARROW_SIZE);
		Point right = new Point(top);
		right = right.getTranslated(ARROW_SIZE, ARROW_SIZE);
		// Create list of point
		PointList list = new PointList();
		list.addPoint(right);
		list.addPoint(top);
		list.addPoint(left);
		graphics.drawPolyline(list);

		// Bottom Arrow
		left = new Point(bottom);
		left = left.getTranslated(-ARROW_SIZE, -ARROW_SIZE);
		right = new Point(bottom);
		right = right.getTranslated(ARROW_SIZE, -ARROW_SIZE);
		// Create list of point
		list = new PointList();
		list.addPoint(right);
		list.addPoint(bottom);
		list.addPoint(left);
		graphics.drawPolyline(list);

		this.setFill(false);
		this.setOutline(false);
		this.setBorder(createBorder());
		graphics.popState();

	}

	/**
	 * Not used anymore (present for compilation purpose of CustomDurationConstraintFigure that is no more called )
	 *
	 * @return null
	 * @deprecated Use paint instead.
	 *
	 */
	@Deprecated
	protected PolylineShape getDurationArrow() {
		return null;
	}
}