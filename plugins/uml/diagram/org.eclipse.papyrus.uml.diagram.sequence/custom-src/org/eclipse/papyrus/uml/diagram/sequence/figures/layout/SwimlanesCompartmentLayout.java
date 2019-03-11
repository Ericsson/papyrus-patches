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
 *   EclipseSource - Initial API and implementation: Bug 533770
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.figures.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout;

/**
 * <p>
 * A {@link LayoutManager} which places children vertically. Children
 * are expected to have an integer height constraint, specified in pixels.
 * </p>
 * <p>
 * The children will get their requested size if possible, and will always take all available width.
 * Otherwise, the following will happen:
 * <ul>
 * <li>If the total height of children is higher than the height of the compartment,
 * the last child(ren) will be smaller than requested (And may ultimately get a size of 0).</li>
 * <li>If the total height of children is smaller than the height of the compartment,
 * the last child will be expanded</li>
 * </ul>
 * </p>
 */
// Note: this compartment can be generalized; but for now it has only been tested/applied
// on CombinedFragments for InteractionOperands, in the SequenceDiagram. If you want to use
// it in other contexts, you may move that class to a more general bundle; but don't forget
// to test/implement missing cases (Typically, horizontal layout is not properly supported)
public class SwimlanesCompartmentLayout extends ConstrainedToolbarLayout {

	private Map<IFigure, Integer> constraints;

	// The implementation mostly comes from the parent class, with some small variations:
	// - The layout constraint is an Integer (Height value, in pixels) rather than a Ratio
	// - Last elements are either expanded or shrank, if the size doesn't perfectly match
	// the compartment. Other elements get exactly their requested size.
	@Override
	public void layout(IFigure parent) {
		if (!parent.isVisible()) {
			return;
		}
		List<IFigure> children = getChildren(parent);
		int numChildren = children.size();
		// FIXME: When the CF is small (< ~120px), the ClientArea is bigger than what's really available
		// for the compartment. This means that Scrollbars immediately appear.
		Rectangle clientArea = transposer.t(parent.getClientArea());
		int x = clientArea.x;
		int y = clientArea.y;
		int availableHeight = clientArea.height;

		Dimension prefSizes[] = new Dimension[numChildren];
		Dimension minSizes[] = new Dimension[numChildren];
		Dimension maxSizes[] = new Dimension[numChildren];

		// Calculate the width and height hints. If it's a vertical ToolBarLayout,
		// then ignore the height hint (set it to -1); otherwise, ignore the
		// width hint. These hints will be passed to the children of the parent
		// figure when getting their preferred size.
		int wHint = -1;
		int hHint = -1;
		if (isHorizontal()) {
			hHint = parent.getClientArea(Rectangle.SINGLETON).height;
		} else {
			wHint = parent.getClientArea(Rectangle.SINGLETON).width;
		}

		/*
		 * Calculate sum of preferred heights of all children(totalHeight).
		 * Calculate sum of minimum heights of all children(minHeight).
		 * Cache Preferred Sizes and Minimum Sizes of all children.
		 *
		 * totalHeight is the sum of the preferred heights of all children
		 * totalMinHeight is the sum of the minimum heights of all children
		 * prefMinSumHeight is the sum of the difference between all children's
		 * preferred heights and minimum heights. (This is used as a ratio to
		 * calculate how much each child will shrink).
		 */
		IFigure child;
		int totalHeight = 0;
		int totalMinHeight = 0;
		double totalMaxHeight = 0;
		int prefMinSumHeight = 0;
		double prefMaxSumHeight = 0;

		for (int i = 0; i < numChildren; i++) {
			child = children.get(i);

			prefSizes[i] = transposer.t(child.getPreferredSize(wHint, hHint));
			minSizes[i] = transposer.t(new Dimension(0, 0));
			maxSizes[i] = transposer.t(child.getMaximumSize());

			if (getConstraint(child) != null) {
				int prefHeight = getConstraint(child);
				prefHeight = Math.max(prefHeight, minSizes[i].height);
				prefHeight = Math.min(prefHeight, maxSizes[i].height);
				prefSizes[i].height = prefHeight;
			}

			totalHeight += prefSizes[i].height;
			totalMinHeight += minSizes[i].height;
			totalMaxHeight += maxSizes[i].height;
		}

		int spacing = getSpacing();

		totalHeight += (numChildren - 1) * spacing;
		totalMinHeight += (numChildren - 1) * spacing;
		totalMaxHeight += (numChildren - 1) * spacing;
		prefMinSumHeight = totalHeight - totalMinHeight;
		prefMaxSumHeight = totalMaxHeight - totalHeight;

		/*
		 * The total amount that the children must be shrunk is the
		 * sum of the preferred Heights of the children minus
		 * Max(the available area and the sum of the minimum heights of the children).
		 *
		 * amntShrinkHeight is the combined amount that the children must shrink
		 * amntShrinkCurrentHeight is the amount each child will shrink respectively
		 */
		int amntShrinkHeight = totalHeight - Math.max(availableHeight, totalMinHeight);

		int maxY = y + availableHeight;

		for (int i = 0; i < numChildren; i++) {
			int amntShrinkCurrentHeight = 0;
			int prefHeight = prefSizes[i].height;
			int minHeight = minSizes[i].height;
			int maxHeight = maxSizes[i].height;
			int prefWidth = prefSizes[i].width;
			int minWidth = minSizes[i].width;
			int maxWidth = maxSizes[i].width;
			Rectangle newBounds = new Rectangle(x, y, prefWidth, prefHeight);

			child = children.get(i);
			if (getStretchMajorAxis()) {
				if (amntShrinkHeight > 0 && prefMinSumHeight != 0) {
					amntShrinkCurrentHeight = (int) ((long) (prefHeight - minHeight)
							* amntShrinkHeight / (prefMinSumHeight));
				} else if (amntShrinkHeight < 0 && totalHeight != 0) {
					amntShrinkCurrentHeight = (int) (((maxHeight - prefHeight) / prefMaxSumHeight)
							* amntShrinkHeight);
				}
			}

			int width = Math.min(prefWidth, maxWidth);
			if (isStretchMinorAxis()) {
				width = maxWidth;
			}
			width = Math.max(minWidth, Math.min(clientArea.width, width));
			// FIXME For InteractionOperand, the width is 2 pixels too big. Why?
			// It seems that the clientArea includes the border (Even though the border should be on the outside)
			newBounds.width = width;

			int adjust = clientArea.width - width;
			switch (getMinorAlignment()) {
			case ALIGN_TOPLEFT:
				adjust = 0;
				break;
			case ALIGN_CENTER:
				adjust /= 2;
				break;
			case ALIGN_BOTTOMRIGHT:
				break;
			}
			newBounds.x += adjust;
			if (newBounds.height - amntShrinkCurrentHeight > maxHeight) {
				amntShrinkCurrentHeight = newBounds.height - maxHeight;
			}
			if (amntShrinkHeight < 0 && i == numChildren - 1) {
				amntShrinkCurrentHeight = amntShrinkHeight;
			} else if (amntShrinkHeight > 0 && y > (maxY - newBounds.height)) {
				amntShrinkCurrentHeight = newBounds.height - (maxY - y);
			}
			newBounds.height -= amntShrinkCurrentHeight;
			child.setBounds(transposer.t(newBounds));

			amntShrinkHeight -= amntShrinkCurrentHeight;
			prefMinSumHeight -= (prefHeight - minHeight);
			prefMaxSumHeight -= (maxHeight - prefHeight);
			totalHeight -= prefHeight;
			y += newBounds.height + spacing;
		}
	}

	@Override
	protected Dimension getChildPreferredSize(IFigure child, int wHint, int hHint) {
		return super.getChildPreferredSize(child, wHint, hHint);
	}

	@Override
	protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
		// Return the smallest non-empty value, so that we get exactly the
		// height we were assigned, and no scrollbars
		return new Dimension(1, 1);
	}

	/**
	 * Sets the constraint for the given figure.
	 *
	 * @param child
	 *                       the child
	 * @param constraint
	 *                       the child's new constraint. Supported values are <code>null</code> (remove),
	 *                       {@link Integer} (height in pixels) or {@link Rectangle} (Height in pixels; position and width will be ignored).
	 *                       Other values are silently ignored.
	 */
	@Override
	public void setConstraint(IFigure child, Object constraint) {
		Integer height;
		if (constraint instanceof Integer) {
			height = (Integer) constraint; // Set new constraint
		} else if (constraint instanceof Rectangle) {
			height = ((Rectangle) constraint).getSize().height; // Set new constraint
		} else if (constraint == null) {
			height = null; // Remove the constraint
		} else {
			return; // Not supported, don't change anything
		}
		if (constraints == null) {
			constraints = new HashMap<IFigure, Integer>();
		}
		if (height == null || height < 0) {
			constraints.remove(child);
		} else {
			constraints.put(child, height);
		}

		invalidate();
	}

	/**
	 * @see org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout#getConstraint(org.eclipse.draw2d.IFigure)
	 *
	 * @param child
	 * @return
	 */
	@Override
	public Integer getConstraint(IFigure child) {
		return constraints.containsKey(child) ? constraints.get(child) : 0;
	}

	protected List<IFigure> getChildren(IFigure parent) {
		@SuppressWarnings("unchecked")
		List<IFigure> children = new ArrayList<>(parent.getChildren());
		if (getIgnoreInvisibleChildren()) {
			Iterator<IFigure> iter = children.iterator();
			while (iter.hasNext()) {
				IFigure f = iter.next();
				if (!f.isVisible()) {
					iter.remove();
				}
			}
		}
		if (isReversed()) {
			Collections.reverse(children);
		}
		return children;
	}

	@Override
	public void setHorizontal(boolean flag) {
		if (flag) {
			// This is not implemented yet; so just make sure we don't accidentally use it...
			throw new UnsupportedOperationException("Horizontal layout is not supported");
		}
		super.setHorizontal(flag);
	}

}
