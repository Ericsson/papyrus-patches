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
		if (PositionConstants.RIGHT == alignment) {
			validLocation.x = bounds.right() - proposedLocation.width / 2;
			if (validLocation.y < (bounds.y - proposedLocation.height / 2)) {
				validLocation.y = (bounds.y - proposedLocation.height / 2);
			} else if (validLocation.y > (bounds.bottom() - proposedLocation.height / 2)) {
				validLocation.y = (bounds.bottom() - proposedLocation.height / 2);
			}
		} else /*if (PositionConstants.LEFT == alignment)*/ {
			validLocation.x = bounds.x - proposedLocation.width / 2;
			if (validLocation.y < (bounds.y - proposedLocation.height / 2)) {
				validLocation.y = (bounds.y - proposedLocation.height / 2);
			} else if (validLocation.y > (bounds.bottom() - proposedLocation.height / 2)) {
				validLocation.y = (bounds.bottom() - proposedLocation.height / 2);
			}
		}  
		return validLocation;
	}

	public int getAlignment() {
		return alignment == null ? PositionConstants.NONE : alignment.intValue();
	}

	public int getAlignment(Rectangle newConstraint) {
		Rectangle parentBounds = getParentBorder();
		if (parentBounds.isEmpty()) {
			return PositionConstants.NONE;
		}
		if (parentBounds.touches(newConstraint)) {
			Point center = newConstraint.getCenter();
			int leftOffset = Math.abs(center.x - parentBounds.x);
			int rightOffset = Math.abs(center.x - parentBounds.right());
			if (leftOffset < rightOffset) {
				alignment = PositionConstants.LEFT;
			} else {
				alignment = PositionConstants.RIGHT;
			} 
		} else {
			if (newConstraint.right() < parentBounds.x) {
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

}
