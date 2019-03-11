/*****************************************************************************
 * Copyright (c) 2010, 2014 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and Implementation
 * 
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.figure.node;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeUtil;
import org.eclipse.swt.graphics.Color;

/**
 * Rounded shadow border, extends RectangularShadowBorder otherwise problem in
 * hierarchy of figures Works with borderwidth of 3.
 */
public class RoundedRectangleShadowBorder extends RectangularShadowBorder {

	/** The borderwidth. */
	int borderwidth;

	/** The corner dimension. */
	Dimension cornerDimension;

	/**
	 * Instantiates a new rounded rectangle shadow border.
	 *
	 * @param color
	 *            the color
	 * @param cornerDimension
	 *            the corner dimension
	 */
	public RoundedRectangleShadowBorder(Color color, Dimension cornerDimension) {
		super(3, color);
		this.borderwidth = 3;
		this.cornerDimension = cornerDimension;
		setWidth(1);
	}

	/**
	 * Method for determining the inset the border will take up on the shape.
	 * 
	 * @param figure
	 *            Figure that will be inset from the border
	 * @return Insets the Insets for the border on the given figure.
	 */
	@Override
	public Insets getInsets(IFigure figure) {
		Insets insetsNew = super.getInsets(figure);
		insetsNew.top = 0;
		insetsNew.left = 0;
		insetsNew.bottom = MapModeUtil.getMapMode(figure).DPtoLP(insetsNew.bottom + borderwidth);
		insetsNew.right = MapModeUtil.getMapMode(figure).DPtoLP(insetsNew.right + borderwidth);
		return insetsNew;
	}

	/**
	 * Paint the figure on the graphics.
	 *
	 * @param figure
	 *            the figure
	 * @param graphics
	 *            the graphics
	 * @param insets
	 *            the insets
	 * @see org.eclipse.draw2d.Border#paint(IFigure, Graphics, Insets)
	 */
	@Override
	public void paint(IFigure figure, Graphics graphics, Insets insets) {
		graphics.pushState();

		// draw the normal line border
		tempRect.setBounds(getPaintRectangle(figure, insets));
		// Take into account the line width
		tempRect.x += getWidth() / 2;
		tempRect.y += getWidth() / 2;
		tempRect.width -= getWidth();
		tempRect.height -= getWidth();
		// Set the arc dimension from the corner dimension of the figure
		if (figure instanceof IRoundedRectangleFigure) {
			cornerDimension.width = ((IRoundedRectangleFigure) figure).getCornerDimensions().width;
			cornerDimension.height = ((IRoundedRectangleFigure) figure).getCornerDimensions().height;
		}
		graphics.setLineWidth(getWidth());
		// Set the color of the shadow
		if (getColor() != null) {
			graphics.setBackgroundColor(getColor());
		} else {
			graphics.setBackgroundColor(ColorConstants.black);
		}
		graphics.setLineStyle(getStyle());
		graphics.drawRoundRectangle(tempRect, cornerDimension.width, cornerDimension.height);
		graphics.setLineStyle(Graphics.LINE_SOLID);
		// draw the shadow
		// first expand the clip rectangle
		Rectangle newRect = new Rectangle(tempRect);
		newRect.width = newRect.width + borderwidth;
		newRect.height = newRect.height + borderwidth;
		newRect.x += borderwidth;
		newRect.y += borderwidth;
		graphics.setClip(newRect);
		if (cornerDimension.width != 0 || cornerDimension.height != 0) {
			// Adapt arc width
			if (cornerDimension.width > tempRect.width)
				cornerDimension.width = tempRect.width;
			// Adapt arc height
			if (cornerDimension.height > tempRect.height)
				cornerDimension.height = tempRect.height;
			// Right Top corner
			graphics.drawArc(tempRect.x + tempRect.width - cornerDimension.width + borderwidth, tempRect.y, cornerDimension.width - 1, cornerDimension.height, -5, 45); // -5,70);
			graphics.drawArc(tempRect.x + tempRect.width - cornerDimension.width + borderwidth, tempRect.y, cornerDimension.width - 2, cornerDimension.height, -5, 45); // -5,70);
			graphics.drawArc(tempRect.x + tempRect.width - cornerDimension.width + borderwidth, tempRect.y, cornerDimension.width - 3, cornerDimension.height, -5, 45); // -5,70);
			// Left Bottom corner
			graphics.drawArc(tempRect.x, tempRect.y + borderwidth + tempRect.height - cornerDimension.height, cornerDimension.width, cornerDimension.height - 1, 210, 65); // 210, 65);
			graphics.drawArc(tempRect.x, tempRect.y + borderwidth + tempRect.height - cornerDimension.height, cornerDimension.width, cornerDimension.height - 2, 210, 65); // 210, 65);
			graphics.drawArc(tempRect.x, tempRect.y + borderwidth + tempRect.height - cornerDimension.height, cornerDimension.width, cornerDimension.height - 3, 210, 65); // 210, 65);
			// Right bottom corner
			int tmpWidth = getWidth();
			graphics.setLineWidthFloat((float) (borderwidth - 0.5));
			// graphics.drawArc(tempRect.x + tempRect.width - arc.width + borderwidth, tempRect.y + borderwidth + tempRect.height - arc.height, arc.width - 1, arc.height - 1, 270, 90);
			graphics.drawArc(tempRect.x + tempRect.width - cornerDimension.width + borderwidth, tempRect.y + borderwidth + tempRect.height - cornerDimension.height + getWidth() / 2, cornerDimension.width - 2 + getWidth() / 2, cornerDimension.height - 2, 270,
					95);
			graphics.setLineWidth(tmpWidth);
			graphics.drawArc(tempRect.x + tempRect.width - cornerDimension.width + borderwidth, tempRect.y + borderwidth + tempRect.height - cornerDimension.height + getWidth() / 2, cornerDimension.width - 3 + getWidth() / 2, cornerDimension.height - 3, 270,
					95);
			// Right rectangle shadow
			if ((tempRect.height - cornerDimension.height) > 0)
				graphics.fillRectangle(tempRect.x + tempRect.width + getWidth() / 2, tempRect.y + cornerDimension.height / 2, borderwidth, tempRect.height - cornerDimension.height + borderwidth);
			// Bottom rectangle shadow
			if ((tempRect.width - cornerDimension.width) > 0)
				graphics.fillRectangle(tempRect.x + cornerDimension.width / 2, tempRect.y + tempRect.height + getWidth() / 2, tempRect.width - cornerDimension.width + borderwidth, borderwidth);
		} else {
			// If there is no rounded corner
			PointList plt = new PointList();
			plt.addPoint(tempRect.x + tempRect.width, tempRect.y + borderwidth);
			plt.addPoint(tempRect.x + tempRect.width, tempRect.y + tempRect.height);
			plt.addPoint(tempRect.x + borderwidth, tempRect.y + tempRect.height);
			plt.addPoint(tempRect.x + borderwidth, tempRect.y + tempRect.height + borderwidth);
			plt.addPoint(tempRect.x + tempRect.width + borderwidth, tempRect.y + tempRect.height + borderwidth);
			plt.addPoint(tempRect.x + tempRect.width + borderwidth, tempRect.y + borderwidth);
			plt.addPoint(tempRect.x + tempRect.width, tempRect.y + borderwidth);
			graphics.fillPolygon(plt);
		}
		graphics.popState();
	}
}
