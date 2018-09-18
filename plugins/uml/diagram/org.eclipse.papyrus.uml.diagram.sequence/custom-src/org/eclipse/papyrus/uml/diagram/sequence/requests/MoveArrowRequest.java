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

package org.eclipse.papyrus.uml.diagram.sequence.requests;

import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.papyrus.uml.diagram.sequence.figures.DurationLinkFigure.Orientation;

/**
 * A request for moving the position of the arrow of a DurationLinkFigure.
 */
public class MoveArrowRequest extends ChangeBoundsRequest {
	public static final String REQ_MOVE_ARROW = "MoveArrowRequest";
	private Orientation arrowOrientation;

	@Override
	public Object getType() {
		return REQ_MOVE_ARROW;
	}

	/**
	 * @param arrowOrientation
	 */
	public void setArrowOrientation(Orientation arrowOrientation) {
		this.arrowOrientation = arrowOrientation;
	}

	/**
	 * @return the arrowOrientation
	 */
	public Orientation getArrowOrientation() {
		return arrowOrientation;
	}
}
