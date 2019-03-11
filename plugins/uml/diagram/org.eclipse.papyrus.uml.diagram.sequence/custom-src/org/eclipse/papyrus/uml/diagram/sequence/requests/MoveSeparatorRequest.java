/*****************************************************************************
 * Copyright (c) 2018 EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - Initial API and implementation (Bug 533770)
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.requests;

import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;

/**
 * <p>
 * A request to move the separator between two Operands in a CombinedFragment.
 * This should expand one of the Operands, and shrink the other one of the same
 * amount.
 * </p>
 * 
 * @since 5.0
 */
public class MoveSeparatorRequest extends Request {

	public static final String REQ_MOVE_SEPARATOR = "MoveSeparatorRequest";
	private final int separatorIndex;
	private Point moveDelta;
	private Point location;
	private List<? extends EditPart> editParts;

	public MoveSeparatorRequest(int separatorIndex) {
		this.separatorIndex = separatorIndex;
	}

	@Override
	public Object getType() {
		return REQ_MOVE_SEPARATOR;
	}

	/**
	 * @return
	 * 		The index of the separator being moved
	 */
	public int getSeparatorIndex() {
		return separatorIndex;
	}

	/**
	 * @param moveDelta
	 */
	public void setMoveDelta(Point moveDelta) {
		this.moveDelta = moveDelta;
	}

	public Point getMoveDelta() {
		return moveDelta;
	}

	/**
	 * @param location
	 */
	public void setLocation(Point location) {
		this.location = location;
	}

	/**
	 * @return the location
	 */
	public Point getLocation() {
		return location;
	}

	/**
	 * @param editParts
	 */
	public void setEditParts(List<? extends EditPart> editParts) {
		this.editParts = editParts;
	}

	/**
	 * @return the editParts
	 */
	public List<? extends EditPart> getEditParts() {
		return editParts;
	}
}
