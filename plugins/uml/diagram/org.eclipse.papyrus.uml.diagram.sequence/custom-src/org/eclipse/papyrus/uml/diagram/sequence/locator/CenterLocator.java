/*****************************************************************************
 * Copyright (c) 2010, 2018 CEA, Christian W. Damus, and others
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
 *   Christian W. Damus - bug 536486
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.locator;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.diagram.ui.figures.BorderedNodeFigure;
import org.eclipse.gmf.runtime.diagram.ui.internal.figures.BorderItemContainerFigure;
import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;
import org.eclipse.papyrus.uml.diagram.common.locator.AdvancedBorderItemLocator;
import org.eclipse.papyrus.uml.diagram.sequence.figures.DestructionEventFigure;

/**
 * This class is used to place all BorderItem node in the middle of the figure for the X but let the position Y
 *
 */
public class CenterLocator extends AdvancedBorderItemLocator {


	/**
	 * Constructor
	 *
	 * @param parentFigure
	 *            the parent figure
	 * @param location
	 *            ContinuationLocator.TOP or ContinuationLocator.BOTTOM
	 */
	public CenterLocator(IFigure parentFigure, int location) {
		super(parentFigure, location);
	}

	/**
	 * The DestructionEventFigure.
	 *
	 * It must be access through the method {@link #getDestructionEventFigure()}
	 */
	private DestructionEventFigure destructionEventFigure = null;

	/**
	 * The BorderItemContainerFigure. It must be access through the method {@link #getBorderItemContainerFigure()}
	 */
	private BorderItemContainerFigure borderItemContainerFigure = null;

	/**
	 * Get the DestructionEventFigure of the lifeline, if it is drawn.
	 *
	 * @return the DestructionEventFigure or null
	 */
	private DestructionEventFigure getDestructionEventFigure() {
		if (destructionEventFigure == null) {
			BorderItemContainerFigure borderItemContainerFigure = getBorderItemContainerFigure();
			if (borderItemContainerFigure != null) {
				for (Object child : borderItemContainerFigure.getChildren()) {
					if (child instanceof BorderedNodeFigure) {
						// Unwrap it (the destruction occurrence, itself, can have border items)
						child = ((BorderedNodeFigure) child).getMainFigure();
					}
					if (child instanceof DefaultSizeNodeFigure) {
						for (Object figure : ((DefaultSizeNodeFigure) child).getChildren()) {
							if (figure instanceof DestructionEventFigure) {
								destructionEventFigure = (DestructionEventFigure) figure;
								return destructionEventFigure;
							}
						}
					}
				}
			}
		}
		return destructionEventFigure;
	}

	/**
	 * Get the BorderItemContainerFigure
	 *
	 * @return the borderItemContainerFigure or null
	 */
	private BorderItemContainerFigure getBorderItemContainerFigure() {
		if (borderItemContainerFigure == null) {
			IFigure figure = getParentFigure().getParent();
			for (Object object : figure.getChildren()) {
				if (object instanceof BorderItemContainerFigure) {
					borderItemContainerFigure = (BorderItemContainerFigure) object;
					return borderItemContainerFigure;
				}
			}
		}
		return borderItemContainerFigure;
	}

	/**
	 * Overridden :
	 * - the destructionEventFigure is always drawn at the end of the figure
	 *
	 * @see org.eclipse.papyrus.uml.diagram.common.locator.AdvancedBorderItemLocator#getValidLocation(org.eclipse.draw2d.geometry.Rectangle, org.eclipse.draw2d.IFigure)
	 */
	@Override
	public Rectangle getValidLocation(Rectangle proposedLocation, IFigure borderItem) {
		// The valid position for destruction event is always the bottom
		if (getDestructionEventFigure() != null) {
			// The destruction event supports border items, itself, so there are two levels of parent
			if (borderItem.equals(getDestructionEventFigure().getParent().getParent())) {
				Rectangle realLocation = new Rectangle(proposedLocation);
				Point point = new Point(getParentBorder().getCenter().x - realLocation.getSize().width / 2, getParentBorder().y + getParentBorder().height - realLocation.height / 2);
				realLocation.setLocation(point);
				return realLocation;
			}
		}
		proposedLocation.setX(getParentFigure().getBounds().x + getParentFigure().getBounds().width / 2 - borderItem.getBounds().width() / 2);

		if (proposedLocation.y - proposedLocation.height / 2 <= getParentFigure().getBounds().y) {
			proposedLocation.setY(getParentFigure().getBounds().y);
		}
		if (proposedLocation.y - proposedLocation.height / 2 >= getParentFigure().getBounds().getBottomLeft().y) {
			proposedLocation.setY(getParentFigure().getBounds().getBottomLeft().y);
		}
		return super.getValidLocation(proposedLocation, borderItem);
	}


}
