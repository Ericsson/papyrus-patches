/*****************************************************************************
 * Copyright (c) 2010 CEA
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Soyatec - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.locator;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.common.locator.AdvancedBorderItemLocator;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class GateLocator extends AdvancedBorderItemLocator {

	private Integer alignment;

	/**
	 * Constructor.
	 *
	 * @param parentFigure
	 */
	public GateLocator(IFigure parentFigure) {
		super(parentFigure);
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.locator.AdvancedBorderItemLocator#getValidLocation(org.eclipse.draw2d.geometry.Rectangle, org.eclipse.draw2d.IFigure)
	 *
	 * @param proposedLocation
	 * @param borderItem
	 * @return
	 */
	@Override
	public Rectangle getValidLocation(Rectangle proposedLocation, IFigure borderItem) {
		Rectangle validLocation = proposedLocation.getCopy();
		Rectangle bounds = getParentBorder();
		// Enable to locate the Gate on top/bottom.
		int alignment = getAlignment(proposedLocation);
		if (PositionConstants.LEFT == alignment) {
			validLocation.x = bounds.x - proposedLocation.width / 2;
			if (validLocation.y < (bounds.y - proposedLocation.height / 2)) {
				validLocation.y = (bounds.y - proposedLocation.height / 2);
			} else if (validLocation.y > (bounds.bottom() - proposedLocation.height / 2)) {
				validLocation.y = (bounds.bottom() - proposedLocation.height / 2);
			}
		} else if (PositionConstants.RIGHT == alignment) {
			validLocation.x = bounds.right() - proposedLocation.width / 2;
			if (validLocation.y < (bounds.y - proposedLocation.height / 2)) {
				validLocation.y = (bounds.y - proposedLocation.height / 2);
			} else if (validLocation.y > (bounds.bottom() - proposedLocation.height / 2)) {
				validLocation.y = (bounds.bottom() - proposedLocation.height / 2);
			}
		} else if (PositionConstants.TOP == alignment) {
			validLocation.y = (bounds.y - proposedLocation.height / 2);
			if (validLocation.x < (bounds.x - proposedLocation.width / 2)) {
				validLocation.x = (bounds.x - proposedLocation.width / 2);
			} else if (validLocation.x > (bounds.right() - proposedLocation.width / 2)) {
				validLocation.x = (bounds.right() - proposedLocation.width / 2);
			}
		} else {
			validLocation.y = (bounds.bottom() - proposedLocation.height / 2);
			if (validLocation.x < (bounds.x - proposedLocation.width / 2)) {
				validLocation.x = (bounds.x - proposedLocation.width / 2);
			} else if (validLocation.x > (bounds.right() - proposedLocation.width / 2)) {
				validLocation.x = (bounds.right() - proposedLocation.width / 2);
			}
		}
		return validLocation;
	}

	public int getAlignment() {
		return alignment == null ? PositionConstants.NONE : alignment.intValue();
	}

	public int getAlignment(Rectangle newConstraint) {
		if (alignment != null) {
			Rectangle modelConstraint = getModelConstraint();
			if (modelConstraint != null && modelConstraint.equals(newConstraint)) {
				return alignment.intValue();
			}
		}
		Rectangle parentBounds = getParentBorder();
		if (parentBounds.isEmpty()) {
			return PositionConstants.NONE;
		}
		if (parentBounds.touches(newConstraint)) {
			Point center = newConstraint.getCenter();
			int leftOffset = Math.abs(center.x - parentBounds.x);
			int rightOffset = Math.abs(center.x - parentBounds.right());
			int topOffset = Math.abs(center.y - parentBounds.y);
			int bottomOffset = Math.abs(center.y - parentBounds.bottom());
			int minOffset = Math.min(Math.min(leftOffset, rightOffset), Math.min(topOffset, bottomOffset));
			if (minOffset == leftOffset) {
				alignment = PositionConstants.LEFT;
			} else if (minOffset == rightOffset) {
				alignment = PositionConstants.RIGHT;
			} else if (minOffset == topOffset) {
				alignment = PositionConstants.TOP;
			} else {
				alignment = PositionConstants.BOTTOM;
			}
		} else {
			if (newConstraint.x > parentBounds.x && newConstraint.bottom() < parentBounds.y && newConstraint.right() < parentBounds.right()) {
				alignment = PositionConstants.TOP;
			} else if (newConstraint.x > parentBounds.x && newConstraint.y > parentBounds.bottom() && newConstraint.right() < parentBounds.right()) {
				alignment = PositionConstants.BOTTOM;
			} else if (newConstraint.right() < parentBounds.x) {
				alignment = PositionConstants.LEFT;
			} else {
				alignment = PositionConstants.RIGHT;
			}
		}
		return alignment.intValue();
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.locator.AdvancedBorderItemLocator#relocate(org.eclipse.draw2d.IFigure)
	 *
	 * @param borderItem
	 */
	@Override
	public void relocate(IFigure borderItem) {
		Dimension size = getSize(borderItem);
		Rectangle rectSuggested = getConstraint();
		rectSuggested.setSize(size);
		Rectangle validLocation = getValidLocation(rectSuggested, borderItem);
		borderItem.setBounds(validLocation);
	}

	/**
	 * @return
	 */
	protected Rectangle getModelConstraint() {
		// This is an old method that assumes the gateEditPart is non-null; but this edit part no longer exists
		// That method shouldn't be called, but it is still referenced from getAlignment without any check
		throw new IllegalStateException();
	}
}
