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
 *   Atos Origin - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.locator;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.diagram.ui.figures.BorderItemLocator;

/**
 * A specific locator for Continuation.
 * This locator is in charge of positioning the continuation at the top or the bottom of its container.
 *
 */
public class ContinuationLocator extends BorderItemLocator {

	private static final int MARGIN = 2;

	/**
	 * Constructor
	 *
	 * @param parentFigure
	 *            the parent figure
	 * @param location
	 *            ContinuationLocator.TOP or ContinuationLocator.BOTTOM
	 */
	public ContinuationLocator(IFigure parentFigure, int location) {
		super(parentFigure, location);
	}

	/**
	 * Locate the figure on its parent
	 *
	 * @param suggestedLocation
	 * @param suggestedSide
	 * @return point
	 */
	@Override
	protected Point locateOnParent(Point suggestedLocation, int suggestedSide, IFigure borderItem) {
		Rectangle bounds = getParentBorder();
		Dimension borderItemSize = getSize(borderItem);
		if (getPreferredSideOfParent() == PositionConstants.SOUTH) {
			return new Point(bounds.x + MARGIN, bounds.y + bounds.height - borderItemSize.height - MARGIN);
		}
		return new Point(bounds.x + MARGIN, bounds.y + MARGIN);
	}

	/**
	 * Overrides :
	 * - the width of the borderItem is equivalent to its parentBorder width - MARGIN * 2
	 * - the CurrentSide and PreferredSide are no more needed.
	 *
	 * @see org.eclipse.draw2d.Locator#relocate(org.eclipse.draw2d.IFigure)
	 */
	@Override
	public void relocate(IFigure borderItem) {
		// Determines the size of the figure
		Dimension size = getParentBorder().getSize().getCopy();
		size.width = size.width - MARGIN * 2;
		size.height = getSize(borderItem).height;
		// Set figure bounds
		Point ptNewLocation = locateOnBorder(getPreferredLocation(borderItem), getPreferredSideOfParent(), 0, borderItem);
		borderItem.setBounds(new Rectangle(ptNewLocation, size));
	}
}
