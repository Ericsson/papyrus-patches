/*****************************************************************************
 * Copyright (c) 2009 CEA
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Atos Origin - Initial API and implementation
 *   Celine Janssens (ALL4TEC) celine.janssens@all4tec.net - Bug 440230 : Label Margin
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
import org.eclipse.papyrus.uml.diagram.common.figure.node.RectangularShadowBorder;

/**
 * Change super type to support displaying stereotypes, modified by [Jin Liu(jin.liu@soyatec.com)]
 */
public class CombinedFragmentFigure extends StereotypeInteractionFigure {


	/**
	 * Height of the Bracket in case of CoRegion in Pixel
	 */
	private static final int BRACKET_HEIGHT = 30;
	private WrappingLabel titleLabel;
	private RectangleFigure header;
	private boolean coRegion;

	public WrappingLabel getTitleLabel() {
		return titleLabel;
	}

	public IFigure getHeaderContainer() {
		return header;
	}

	@Override
	public void setShadow(boolean shadow) {
		final int BORDER_WIDTH = 3;

		if (!shadow) {
			super.setShadow(shadow);
		} else {
			RectangularShadowBorder b = new RectangularShadowBorder(BORDER_WIDTH, getForegroundColor()) {
				@Override
				public Insets getInsets(IFigure figure) {
					return new Insets(1, 1, 1, 1);
				}
			};
			setBorder(b);
		}

		Rectangle figureRect = new Rectangle(getBounds()).expand(new Insets(0, 0, BORDER_WIDTH, BORDER_WIDTH));
		IFigure parent = getParent();
		while (parent != null) {
			if (parent.getBounds().contains(figureRect)) {
				parent.revalidate();
				parent.repaint();
				break;
			}
			parent = parent.getParent();
		}
	}


	/**
	 * @see org.eclipse.draw2d.Figure#paint(org.eclipse.draw2d.Graphics)
	 *
	 * @param graphics
	 */
	@Override
	public void paint(Graphics graphics) {
		if (isCoregion()) {
			Rectangle CBbounds = this.getBounds();

			graphics.pushState();

			Rectangle clipRectangle = new Rectangle();
			graphics.getClip(clipRectangle);
			graphics.setClip(clipRectangle.expand(2, 2));

			graphics.setLineWidth(getLineWidth());
			graphics.setLineStyle(getLineStyle());
			graphics.setForegroundColor(getForegroundColor());
			graphics.setBackgroundColor(getBackgroundColor());



			// Top Bracket Creation
			PointList list = new PointList();
			Point topLeft = CBbounds.getTopLeft().getCopy().getTranslated(0, BRACKET_HEIGHT);
			Point topRight = CBbounds.getTopRight().getCopy().getTranslated(0, BRACKET_HEIGHT);

			list.addPoint(topRight);
			list.addPoint(CBbounds.getTopRight().getCopy());
			list.addPoint(CBbounds.getTopLeft().getCopy());
			list.addPoint(topLeft);

			graphics.drawPolyline(list);

			// Bottom Bracket Creation
			list = new PointList();

			Point bottomLeft = CBbounds.getBottomLeft().getCopy().getTranslated(0, -BRACKET_HEIGHT);
			Point bottomRight = CBbounds.getBottomRight().getCopy().getTranslated(0, -BRACKET_HEIGHT);

			list.addPoint(bottomRight);
			list.addPoint(CBbounds.getBottomRight().getCopy());
			list.addPoint(CBbounds.getBottomLeft().getCopy());
			list.addPoint(bottomLeft);

			graphics.drawPolyline(list);

			this.setPreferredSize(new Dimension(40, 100));
			graphics.popState();
		} else {
			super.paint(graphics);
		}
	}


	/**
	 * @return
	 */
	public boolean isCoregion() {

		return this.coRegion;

	}

	/**
	 * @return
	 */
	public void setCoregion(boolean coregion) {
		this.coRegion = coregion;

	}

}
