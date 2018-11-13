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
 * Anchors a Connection to the Top-Center point of a Node (Typically the start of an Execution Specification)
 */
public class NodeTopAnchor extends AbstractConnectionAnchor {

	public NodeTopAnchor(IFigure anchorage) {
		super(anchorage);
	}

	@Override
	public Point getLocation(Point reference) {
		IFigure owner = getOwner();
		Point top = owner.getBounds().getTop().getCopy();
		owner.translateToAbsolute(top);
		return top;
	}

}
