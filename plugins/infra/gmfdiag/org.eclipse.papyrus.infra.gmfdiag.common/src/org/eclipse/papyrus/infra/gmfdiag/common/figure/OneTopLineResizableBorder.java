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
package org.eclipse.papyrus.infra.gmfdiag.common.figure;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.draw2d.ui.figures.OneLineBorder;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeUtil;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.SVGNodePlateFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.SlidableRoundedRectangleAnchor;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.FigureUtils;
import org.eclipse.swt.graphics.Color;

/**
 * The Class OneLineResizableBorder. A border set on the top which can be resize and positioned.
 */
public class OneTopLineResizableBorder extends OneLineBorder {

	/** The forced length. */
	private int forcedLength = -1;

	/** The length ratio. */
	private float lengthRatio = (float) 1.0;

	/** The line position. */
	private int linePosition = PositionConstants.CENTER;

	/**
	 * Sets the line position.
	 *
	 * @param linePosition
	 *            the linePosition to set
	 */
	public void setLinePosition(int linePosition) {
		this.linePosition = linePosition;
	}

	/**
	 * Sets the length ratio.
	 *
	 * @param lengthRatio
	 *            the lengthRatio to set
	 */
	public void setLengthRatio(float lengthRatio) {
		this.lengthRatio = lengthRatio;
	}

	/**
	 * Constructor.
	 *
	 */
	public OneTopLineResizableBorder() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param color
	 *            the color
	 */
	public OneTopLineResizableBorder(Color color) {
		super(color);
	}

	/**
	 * Constructor.
	 *
	 * @param width
	 *            the width
	 */
	public OneTopLineResizableBorder(int width) {
		super(width, PositionConstants.TOP);
	}

	/**
	 * Constructor.
	 *
	 * @param length
	 *            the length
	 * @param width
	 *            the width
	 */
	public OneTopLineResizableBorder(int length, int width) {
		super(width, PositionConstants.TOP);
		this.forcedLength = length;
	}

	/**
	 * Paints the oneTopBorder based on the inputs given.
	 *
	 * @param figure
	 *            <code>IFigure</code> for which this is the border.
	 * @param graphics
	 *            <code>Graphics</code> handle for drawing the border.
	 * @param insets
	 *            Space to be taken up by this border.
	 */
	@Override
	public void paint(IFigure figure, Graphics graphics, Insets insets) {

		tempRect.setBounds(getPaintRectangle(figure, insets));

		// get the length of the line according to the corner
		int length = getLineLength(figure);

		tempRect.width = (length);

		// if the length is forced or a ratio is applied
		if (forcedLength != -1) {
			tempRect.width = forcedLength;
		}
		tempRect.width = (int) (tempRect.width * lengthRatio);


		// Position the line
		switch (linePosition) {
		case PositionConstants.CENTER:
			tempRect.x += (length - tempRect.width) / 2;
			break;
		case PositionConstants.LEFT:
			break;
		case PositionConstants.RIGHT:
			tempRect.x += (length - tempRect.width);
			break;
		default:
			tempRect.x += (length - tempRect.width) / 2;
			break;
		}

		int one = MapModeUtil.getMapMode(figure).DPtoLP(1);
		int widthInDP = getWidth() / one;

		int halfWidthInLP = MapModeUtil.getMapMode(figure).DPtoLP(widthInDP / 2);

		graphics.setLineWidth(getWidth());
		graphics.setLineStyle(getStyle());

		if (getColor() != null) {
			graphics.setForegroundColor(getColor());
		}
		tempRect.y += halfWidthInLP;
		tempRect.height -= getWidth();

		// Applied same transparency as the parent figure
		IRoundedRectangleFigure parentFigureRoundedRectangle = FigureUtils.findParentFigureInstance(figure, IRoundedRectangleFigure.class);
		if (parentFigureRoundedRectangle != null) {
			graphics.setAlpha(255 - ((NodeFigure) parentFigureRoundedRectangle).getTransparency() * 255 / 100);
		}
		graphics.drawLine(tempRect.getTopLeft(), tempRect.getTopRight());
	}

	/**
	 * Gets the line length.
	 *
	 * @param figure
	 *            the figure
	 * @return the line length
	 */
	private int getLineLength(IFigure figure) {
		int length = tempRect.width;

		SVGNodePlateFigure mainFigure = FigureUtils.findParentFigureInstance(figure, SVGNodePlateFigure.class);
		// Get the connection anchor
		if (mainFigure != null) {
			ConnectionAnchor connectionAnchor = mainFigure.getConnectionAnchor("");
			if (connectionAnchor instanceof SlidableRoundedRectangleAnchor && !(mainFigure.getBounds().equals(new Rectangle()))) {
				// Calculate the length off the border thanks the SlidableRoundedRectangleAnchor
				Rectangle rect = figure.getBounds().getCopy();
				figure.translateToAbsolute(rect);

				Point locationLeft = ((SlidableRoundedRectangleAnchor) connectionAnchor).getLocation(rect.getTopLeft().translate(rect.width / 2, 0), rect.getTopLeft());
				Point locationRight = ((SlidableRoundedRectangleAnchor) connectionAnchor).getLocation(rect.getTopLeft().translate(rect.width / 2, 0), rect.getTopRight());

				if (locationLeft != null && locationRight != null) {

					rect.width = locationRight.x - locationLeft.x;
					// translate the length according to the zoom
					figure.translateToRelative(rect);
					length = rect.width;

					// set the position at the figure bounds.x to the position on x of the left location
					// TODO: don't do it on a getLenght method.
					figure.translateToRelative(locationLeft);
					tempRect.x = locationLeft.x;
				}
			}
		}
		return (length);
	}

	/**
	 * Sets the length.
	 *
	 * @param length
	 *            the new length
	 */
	public void setLength(int length) {
		this.forcedLength = length;
	}
}
