/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Celine Janssens (celine.janssens@all4tec.net) - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.locator;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.common.locator.AdvancedBorderItemLocator;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;

/**
 * @author Celine JANSSENS
 *         This class is used for Duration Constraint (@see {@link LifelineEditPart}) in order to locate it just next to the middle of it's parent (the Lifeline) on the X axe.
 *         The Y Axe is free except if the top is upper than its Parent.
 */
public class DurationConstraintLocator extends AdvancedBorderItemLocator {

	/**
	 * Constructor.
	 *
	 * @param borderItem
	 * @param parentFigure
	 * @param constraint
	 */
	public DurationConstraintLocator(IFigure borderItem, IFigure parentFigure, Rectangle constraint) {
		super(borderItem, parentFigure, constraint);

	}

	/**
	 * Constructor.
	 *
	 */
	public DurationConstraintLocator(IFigure parentFigure) {
		super(parentFigure);
	}


	/**
	 * Constructor.
	 *
	 */
	public DurationConstraintLocator(IFigure parentFigure, int position) {
		super(parentFigure, position);
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.locator.AdvancedBorderItemLocator#relocate(org.eclipse.draw2d.IFigure)
	 *
	 * @param borderItem
	 */
	@Override
	public void relocate(IFigure borderItem) {
		Dimension size = getSize(borderItem);
		// The returned constraint is relative to the parent
		Rectangle rectSuggested = getConstraint();
		rectSuggested.setSize(size);
		// transform it to absolute
		Rectangle suggestedRectIndiagram = rectSuggested.getCopy();
		suggestedRectIndiagram.x = suggestedRectIndiagram.x + getParentFigure().getBounds().x;
		suggestedRectIndiagram.y = suggestedRectIndiagram.y + getParentFigure().getBounds().y;
		// get the valid Location in Absolute coordinates
		suggestedRectIndiagram = getValidLocation(suggestedRectIndiagram, borderItem);

		// transform it back in relative coordinate to its parent.
		borderItem.setBounds(suggestedRectIndiagram.getCopy());
		suggestedRectIndiagram.x = suggestedRectIndiagram.x - getParentFigure().getBounds().x;
		suggestedRectIndiagram.y = suggestedRectIndiagram.y - getParentFigure().getBounds().y;

		// Set the new Constraint in Relative.
		setConstraint(suggestedRectIndiagram);

	}

	/**
	 *
	 * The Valid location for a Duration Constraint is just next to the center of its parent (the lifeline).
	 *
	 * @see org.eclipse.papyrus.uml.diagram.common.locator.AdvancedBorderItemLocator#getValidLocation(org.eclipse.draw2d.geometry.Rectangle, org.eclipse.draw2d.IFigure)
	 *
	 * @param proposedLocation
	 * @param borderItem
	 * @return
	 */
	@Override
	public Rectangle getValidLocation(Rectangle proposedLocation, IFigure borderItem) {
		// the offset required to take the Lifeline Header height into account
		int headerYOffset = 20;

		int parentMiddleX = getParentFigure().getBounds().x + (getParentFigure().getBounds().width / 2);

		// Place the DurationConstraint Just next to the lifeline (right or left depends on the proposedLocation )
		if (proposedLocation.x < parentMiddleX) {
			proposedLocation.setX(parentMiddleX - proposedLocation.width());
		} else {
			proposedLocation.setX(parentMiddleX);

		}

		// If the proposed Location is upper than the Parent Figure (The lifeline) , then set the location as close as possible from the top of the parent.
		if (proposedLocation.y < getParentFigure().getBounds().y + headerYOffset) {
			proposedLocation.setY(getParentFigure().getBounds().y + headerYOffset);
		}

		// If the DurationConstraint is out of the Lifeline bottom then relocate it into the Lifeline
		if (proposedLocation.y + proposedLocation.height > getParentFigure().getBounds().bottom()) {
			proposedLocation.setY(getParentFigure().getBounds().bottom() - proposedLocation.height);
		}


		return super.getValidLocation(proposedLocation, borderItem);
	}

}
