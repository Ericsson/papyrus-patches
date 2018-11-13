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
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;


/**
 * Anchors a Connection to the target of a {@link PolylineConnection} (Typically the receiveEvent of a Message)
 */
public class ConnectionTargetAnchor extends AbstractConnectionAnchor {

	private final PolylineConnection anchorage;

	public ConnectionTargetAnchor(PolylineConnection anchorage) {
		super(anchorage);
		this.anchorage = anchorage;
	}

	@Override
	public Point getLocation(Point reference) {
		Point target = anchorage.getEnd().getCopy();
		anchorage.translateToAbsolute(target);
		return target;
	}

}
