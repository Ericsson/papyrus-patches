/*****************************************************************************
 * Copyright (c) 2017 CEA LIST, ALL4TEC and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 519408
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.locator;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CLifeLineEditPart;

/**
 * A specific anchor to attach "create message" to the middle of the rectangle
 * representing the header of the lifeline
 *
 */
public class MessageCreateLifelineAnchor extends AbstractConnectionAnchor {

	private CLifeLineEditPart cLifeLineEditPart;

	public MessageCreateLifelineAnchor(IFigure owner, CLifeLineEditPart cLifeLineEditPart) {
		super(owner);
		this.cLifeLineEditPart = cLifeLineEditPart;
	}

	/**
	 * @see org.eclipse.draw2d.ConnectionAnchor#getLocation(org.eclipse.draw2d.geometry.Point)
	 */
	@Override
	public Point getLocation(Point reference) {

		if (getOwner() == null) {
			return null;
		}

		Rectangle r = getOwner().getBounds().getCopy();
		r.setHeight(cLifeLineEditPart.getStickerHeight());

		Point p = new Point();

		p.y = r.y + r.getSize().height / 2;
		// By default x is set to the left side of the figure
		p.x = r.getLeft().x;

		if (reference != null) {
			// If the reference point is located at the right of the figure
			// means the point should be attached to the right side.
			if (reference.x > r.x) {
				p.x = r.getRight().x;
			}
		}

		// Translate the point to absolute
		getOwner().translateToAbsolute(p);

		return p;
	}

	/**
	 * Overrides to disable the reference point
	 *
	 * @see org.eclipse.draw2d.AbstractConnectionAnchor#getReferencePoint()
	 */
	@Override
	public Point getReferencePoint() {
		return getLocation(null);
	}

}
