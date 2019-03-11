/*****************************************************************************
 * Copyright (c) 2016 CEA LIST.
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
 *  Mickael ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and Implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.figure.node;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.draw2d.ui.geometry.LineSeg;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.gef.ui.figures.SlidableAnchor;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.RoundedRectangleUtil;

/**
 * A slideable anchor for rounded rectangle figures.
 */
public class SlidableRoundedRectangleAnchor extends SlidableAnchor {

	/** the offset applied to the size of the rectangle */
	private final Dimension offset = new Dimension();

	/**
	 * Constructs a SlidableRoundedRectangleAnchor without a desired anchor
	 * point.
	 * 
	 * @param figure
	 *            the anchorable figure
	 */
	public SlidableRoundedRectangleAnchor(final NodeFigure figure) {
		super(figure);
	}

	/**
	 * Constructs a SlidableRoundedRectangleAnchor with a desired anchor
	 * point.
	 *
	 * @param figure
	 *            the anchorable figure
	 * @param p
	 *            the anchor precision point
	 */
	public SlidableRoundedRectangleAnchor(final NodeFigure figure, final PrecisionPoint p) {
		super(figure, p);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Rectangle getBox() {
		PrecisionRectangle rBox = null;
		Object figure = getFigure();
		if (figure instanceof IRoundedRectangleFigure) {
			rBox = new PrecisionRectangle(((IRoundedRectangleFigure) ((IFigure) figure)).getRoundedRectangleBounds());
		} else if (figure instanceof IFigure) {
			rBox = new PrecisionRectangle(getOwner().getBounds());
		}

		if (null != rBox) {
			((IFigure) figure).translateToAbsolute(rBox);
			rBox.expand(offset.width, offset.height);
		}

		return rBox;
	}

	/**
	 * Gets the figure.
	 *
	 * @return the figure
	 */
	private Object getFigure() {
		Object result = null;
		if (getOwner().getChildren().size() > 0) {
			result = getOwner().getChildren().get(0);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PointList getPolygonPoints() {
		// The returned points
		PointList points = null;

		if (getFigure() instanceof IRoundedRectangleFigure) {
			// return the polygon in the case of package shape

			IRoundedRectangleFigure packageFigure = ((IRoundedRectangleFigure) ((IFigure) getFigure()));

			Rectangle packageHeader = packageFigure.getPackageHeader().getCopy();

			packageFigure.translateToAbsolute(packageHeader);
			if (!packageHeader.isEmpty()) {

				points = new PointList(5);
				Rectangle anchorableRectangle = getBox();

				points.addPoint(anchorableRectangle.x, anchorableRectangle.y);

				// take in account the header of the package
				points.addPoint(anchorableRectangle.x + packageHeader.width, anchorableRectangle.y);
				points.addPoint(anchorableRectangle.x + packageHeader.width, anchorableRectangle.y + packageHeader.height);
				
				//drawing is from 0 to n-1 for width and height, same principle as array
				points.addPoint(anchorableRectangle.x + anchorableRectangle.width -1, anchorableRectangle.y + packageHeader.height);

				points.addPoint(anchorableRectangle.x + anchorableRectangle.width -1, anchorableRectangle.y + anchorableRectangle.height-1);
				points.addPoint(anchorableRectangle.x, anchorableRectangle.y + anchorableRectangle.height-1);
				points.addPoint(anchorableRectangle.x, anchorableRectangle.y);
			}
		}

		return null != points ? points : super.getPolygonPoints();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PointList getIntersectionPoints(final Point ownReference, final Point foreignReference) {

		PointList pointList = null;
		Object figure = getFigure();

		if (figure instanceof IRoundedRectangleFigure) {

			// case of package
			if (!((IRoundedRectangleFigure) figure).getPackageHeader().isEmpty()) {
				final PointList polygon = getPolygonPoints();
				pointList = (new LineSeg(ownReference, foreignReference)).getLineIntersectionsWithLineSegs(polygon);

			} else {
				// Case of RoundedRectangle
				Rectangle rect = getBox();
				Dimension dimension = null;
				// Get the dimension of the owner figure
				if (figure instanceof IRoundedRectangleFigure) {
					// Force the Refresh of the Corner Dimension in case of resize(figure.paintFigure called after)
					((IRoundedRectangleFigure) figure).setOval(((IRoundedRectangleFigure) figure).isOval());
					// Get the Dimension of the figure
					dimension = ((IRoundedRectangleFigure) figure).getCornerDimensions().getCopy();
				} else {
					dimension = new Dimension();
				}
				// Adapt dimension according to the rectangle
				if (rect.height < dimension.height)
					dimension.height = rect.height;
				if (rect.width < dimension.width)
					dimension.width = rect.width;
				PrecisionRectangle corner = new PrecisionRectangle(new Rectangle(0, 0, dimension.width, dimension.height));
				((IFigure) figure).translateToAbsolute(corner);

				pointList = RoundedRectangleUtil.getLineIntersectionsWithRoundedRectangle(new LineSeg(ownReference, foreignReference), rect.x, rect.y, rect.width, rect.height, corner.width, corner.height);
			}
		} else {
			pointList = super.getIntersectionPoints(ownReference, foreignReference);
		}
		return pointList;
	}

	/**
	 * Get the location on the border with a specific ownReference point. Used for the PortEditPart.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public Point getLocation(final Point refParent, final Point refPort) {
		return super.getLocation(refParent, refPort);
	}

	/**
	 * Set the offset
	 * 
	 * @param portOffset
	 */
	public void setOffset(final Dimension portOffset) {
		offset.height = portOffset.height;
		offset.width = portOffset.width;
	}
}
