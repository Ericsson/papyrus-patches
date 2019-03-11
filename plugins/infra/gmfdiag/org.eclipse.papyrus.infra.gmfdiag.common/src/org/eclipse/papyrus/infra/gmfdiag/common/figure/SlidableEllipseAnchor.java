/*****************************************************************************
 * Copyright (c) 2013 CEA LIST.
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
 *  Patrick Tessier (CEA LIST) Patrick.tessier@cea.fr - Initial API and implementation
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.figure;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.draw2d.ui.geometry.LineSeg;
import org.eclipse.gmf.runtime.gef.ui.figures.SlidableAnchor;


/**
 * this code comes from eclipse.
 * I have change the figure IOvalAnchorableFigure by Ifigure for the constructor
 *
 * @author oboyko
 *
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class SlidableEllipseAnchor extends SlidableAnchor {

	/**
	 * Default constructor
	 */
	public SlidableEllipseAnchor() {
		super();
	}

	/**
	 * Creates default <Code>SlidableOvalAnchor</Code> with a reference points
	 * at the center of the figure
	 *
	 * @param f
	 *            the figure that this anchor will be associated with
	 */
	public SlidableEllipseAnchor(IFigure f) {
		super(f);
	}

	/**
	 * Creates <Code>SlidableOvalAnchor</Code> with a specified reference points
	 *
	 * @param f
	 *            the figure that this anchor will be associated with
	 * @param p
	 *            the <code>PrecisionPoint</code> that the anchor will initially attach to.
	 */
	public SlidableEllipseAnchor(IFigure f, PrecisionPoint p) {
		super(f, p);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gmf.runtime.gef.ui.figures.SlidableAnchor#getIntersectionPoints(org.eclipse.draw2d.geometry.Point, org.eclipse.draw2d.geometry.Point)
	 */
	@Override
	protected PointList getIntersectionPoints(Point ownReference, Point foreignReference) {
		Rectangle ellipseBox = new PrecisionRectangle(getOwner().getBounds());
		getOwner().translateToAbsolute(ellipseBox);
		return (new LineSeg(ownReference, foreignReference)).getLineIntersectionsWithEllipse(ellipseBox);
	}
}
