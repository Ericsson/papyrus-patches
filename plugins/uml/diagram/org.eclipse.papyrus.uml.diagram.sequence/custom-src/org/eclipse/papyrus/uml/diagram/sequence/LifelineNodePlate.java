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
 *   CEA LIST - Initial API and implementation
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Bug 531596
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.draw2d.ui.figures.BaseSlidableAnchor;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.LinkLFSVGNodePlateFigure;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.BoundForEditPart;

/**
 * This figure is used in order to allow a link to follow the shape of the lifeLine
 * see getPolygonPoints()
 *
 * @since 3.0
 *
 */
public class LifelineNodePlate extends LinkLFSVGNodePlateFigure {

	/**
	 * Constructor.
	 *
	 * @param hostEP
	 * @param width
	 * @param height
	 * @param lifelineEditPart
	 *            TODO
	 */
	public LifelineNodePlate(org.eclipse.gef.GraphicalEditPart hostEP, int width, int height) {
		super(hostEP, width, height);
		withLinkLFEnabled();
		followSVGPapyrusPath = true;

	}

	/**
	 * @see org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure#getPolygonPoints()
	 */
	@Override
	public PointList getPolygonPoints() {
		return ((NodeFigure) this.getChildren().get(0)).getPolygonPoints();
	}

	@Override
	protected ConnectionAnchor createAnchor(PrecisionPoint p) {
		p.setPreciseX(0.5);// a changer
		return super.createAnchor(p);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ConnectionAnchor createConnectionAnchor(Point p) {
		if (p == null) {
			return getConnectionAnchor(szAnchor);
		} else {
			Point temp = p.getCopy();
			translateToRelative(temp);

			// This allows to calculate the bounds corresponding to the node instead of the figure bounds
			final Bounds bounds = BoundForEditPart.getBounds((Node) getGraphicalEditPart().getModel());
			final Rectangle rectangle = new Rectangle(new Point(bounds.getX(), bounds.getY()), new Dimension(bounds.getWidth(), bounds.getHeight()));

			PrecisionPoint pt = BaseSlidableAnchor.getAnchorRelativeLocation(temp, rectangle);
			return createAnchor(pt);
		}
	}

	/**
	 * @see org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure#isDefaultAnchorArea(org.eclipse.draw2d.geometry.PrecisionPoint)
	 */
	@Override
	protected boolean isDefaultAnchorArea(PrecisionPoint p) {
		return false;
	}

	@Override
	public boolean containsPoint(int x, int y) {
		if (Math.abs(this.getBounds().x + this.getBounds().width / 2 - x) < 20) {
			return super.containsPoint(x, y);
		}
		return false;
	}

}