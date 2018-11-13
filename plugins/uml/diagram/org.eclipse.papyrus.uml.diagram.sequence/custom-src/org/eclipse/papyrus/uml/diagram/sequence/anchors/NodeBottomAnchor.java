/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.anchors;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;

/**
 * Anchors a Connection to the Bottom-Center point of a Node (Typically the finish of an Execution Specification)
 */
public class NodeBottomAnchor extends AbstractConnectionAnchor {

	public NodeBottomAnchor(IFigure anchorage) {
		super(anchorage);
	}

	@Override
	public Point getLocation(Point reference) {
		IFigure owner = getOwner();
		Point bottom = owner.getBounds().getBottom().getCopy();
		owner.translateToAbsolute(bottom);
		return bottom;
	}

}
