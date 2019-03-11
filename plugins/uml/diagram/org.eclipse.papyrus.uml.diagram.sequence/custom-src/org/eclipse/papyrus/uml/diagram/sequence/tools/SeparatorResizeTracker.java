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
 *   EclipseSource - Initial API and implementation (Bug 533770, 537001)
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.tools;

import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.tools.ResizeTracker;
import org.eclipse.papyrus.uml.diagram.sequence.requests.MoveSeparatorRequest;

/**
 * A special resize tracker for separators in a Swimlanes compartment,
 * that can be used to resize elements on both sides of the separator.
 */
public class SeparatorResizeTracker extends ResizeTracker {

	private int separatorIndex;

	/**
	 * Constructor.
	 *
	 * @param owner
	 * @param direction
	 */
	public SeparatorResizeTracker(GraphicalEditPart owner, int direction, int separatorIndex) {
		super(owner, direction);
		this.separatorIndex = separatorIndex;
	}

	@Override
	protected List<EditPart> createOperationSet() {
		// The request only applies to the owner of the handle. We don't support multi-selection
		return Collections.singletonList(getOwner());
	}

	/**
	 * @see org.eclipse.gef.tools.ResizeTracker#updateSourceRequest()
	 *
	 */
	@Override
	protected void updateSourceRequest() {
		MoveSeparatorRequest request = (MoveSeparatorRequest) getSourceRequest();
		Dimension d = getDragMoveDelta();
		Point location = new Point(getLocation());
		Point moveDelta = new Point(0, 0);

		moveDelta.y += d.height;

		request.setMoveDelta(moveDelta);
		request.setLocation(location);
		request.setEditParts(getOperationSet());
		request.getExtendedData().clear();
	}

	/**
	 * @see org.eclipse.gef.tools.SimpleDragTracker#getSourceRequest()
	 *
	 * @return
	 */
	@Override
	protected Request createSourceRequest() {
		return new MoveSeparatorRequest(separatorIndex);
	}

}
