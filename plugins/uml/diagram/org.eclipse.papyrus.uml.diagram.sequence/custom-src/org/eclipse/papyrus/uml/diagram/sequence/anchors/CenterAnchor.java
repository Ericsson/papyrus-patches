/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.anchors;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;

/**
 * An anchor to the center of an element (Typically a {@link DestructionOccurrenceSpecification})
 */
public class CenterAnchor extends AbstractConnectionAnchor {

	public CenterAnchor(IFigure anchorage) {
		super(anchorage);
	}

	@Override
	public Point getLocation(Point reference) {
		Point center = getOwner().getBounds().getCenter().getCopy();
		getOwner().translateToAbsolute(center);
		return center;
	}

}
