/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.locator;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

/** ConnectionLocator that provides the location of the intersection point of two PointLists. */
public class IntersectionPointSelectionLocator extends ConnectionLocator {

	private PointList pointList1;
	private PointList pointList2;

	/**
	 * Constructor.
	 *
	 * @param connection
	 */
	public IntersectionPointSelectionLocator(Connection connection, PointList pointList1, PointList pointList2) {
		super(connection);
		this.pointList1 = pointList1;
		this.pointList2 = pointList2;
	}

	@Override
	protected Point getLocation(PointList points) {
		if (pointList1 == null || pointList2 == null) {
			return super.getLocation(points);
		}

		Rectangle intersect = pointList1.getBounds().intersect(pointList2.getBounds());
		if (!intersect.isEmpty()) {
			return intersect.getLocation();
		}
		// fallback to default (middle point)
		return super.getLocation(points);
	}
}