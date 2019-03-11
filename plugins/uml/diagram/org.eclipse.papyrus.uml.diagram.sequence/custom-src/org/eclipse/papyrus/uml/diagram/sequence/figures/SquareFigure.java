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
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.gef.ui.figures.SlidableAnchor;
import org.eclipse.papyrus.uml.diagram.common.figure.node.RoundedCompartmentFigure;

/**
 * @author Patrick Tessier
 *
 */
public class SquareFigure extends RoundedCompartmentFigure {
	static Dimension DEFAULT_SIZE=new Dimension(10,10);
	/**
	 * Constructor.
	 *
	 */
	public SquareFigure() {
		setBorder(new LineBorder(ColorConstants.black));
		setPreferredSize(DEFAULT_SIZE);
	}

	@Override
	protected ConnectionAnchor createDefaultAnchor() {
		return new SlidableAnchor(this) {

			@Override
			public Point getLocation(Point reference) {
				Point location = getLocation(new PrecisionPoint(getBox().getCenter()), reference);
				if (location == null) {
					location = getBox().getCenter();
				}
				return location;
			}
		};
	}

	@Override
	protected ConnectionAnchor createAnchor(PrecisionPoint p) {
		if (p == null) {
			return createDefaultAnchor();
		}
		return new SlidableAnchor(this, p) {

			@Override
			public Point getLocation(Point reference) {
				Rectangle box = getBox();
				Point location = getLocation(new PrecisionPoint(box.getCenter()), reference);
				if (location == null) {
					location = getBox().getCenter();
				}
				return location;
			}
		};
	}
}
