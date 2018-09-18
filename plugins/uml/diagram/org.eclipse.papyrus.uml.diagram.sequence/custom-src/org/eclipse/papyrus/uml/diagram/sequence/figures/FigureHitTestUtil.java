/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.figures;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Hit-test utilities for {@code IFigure figure}s.
 */
public class FigureHitTestUtil {
	public static final FigureHitTestUtil INSTANCE = new FigureHitTestUtil();

	private static final int FUZZ_FACTOR = 2;

	// A slightly fuzzy point for hit-testing on a polyline
	private final Rectangle fuzzyPoint = new Rectangle(0, 0,
			FUZZ_FACTOR * 2 + 1, FUZZ_FACTOR * 2 + 1);

	/**
	 * Not instantiable by clients.
	 */
	private FigureHitTestUtil() {
		super();
	}

	/**
	 * Obtain the children of a figure as a typed list.
	 *
	 * @param figure
	 *            a figure
	 * @return its children
	 */
	@SuppressWarnings("unchecked")
	public final List<? extends IFigure> getChildren(IFigure figure) {
		return figure.getChildren();
	}

	/**
	 * Fuzzily test whether a {@code polygon} contains some point. Effectively,
	 * test whether the {@code polygon} overlaps a tiny square around the point.
	 *
	 * @param polygon
	 *            a polygon
	 * @param x
	 *            the X coördinate at which to search
	 * @param y
	 *            the Y coördinate at which to search
	 * @return whether the point fuzzily is in the {@code polygon}
	 */
	public boolean fuzzyHitTest(PointList polygon, int x, int y) {
		boolean result = polygon.polygonContainsPoint(x, y);
		if (!result) {
			// If the center of our rectangle isn't in the polygon but
			// any of it does overlap the polygon, then it must intersect
			// the polygon boundary
			fuzzyPoint.setLocation(x - FUZZ_FACTOR, y - FUZZ_FACTOR);
			result = polygon.intersects(fuzzyPoint);
		}
		return result;
	}

	/**
	 * Query whether any child of a {@code figure} contains a point.
	 *
	 * @param figure
	 *            a figure
	 * @param x
	 *            the X coördinate at which to search
	 * @param y
	 *            the Y coördinate at which to search
	 * @return whether any child of the {@code figure} contains the point
	 */
	public boolean anyChildContainsPoint(IFigure figure, int x, int y) {
		boolean result = false;

		final List<? extends IFigure> children = getChildren(figure);

		for (int i = children.size() - 1; i >= 0; i--) {
			IFigure next = children.get(i);
			if (next.isVisible() && next.containsPoint(x, y)) {
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * Find a child figure (recursively) at the given location, excluding
	 * the {@code figure}, itself.
	 *
	 * @param figure
	 *            a figure in which to search for some child
	 * @param x
	 *            the X coördinate at which to search
	 * @param y
	 *            the Y coördinate at which to search
	 * @param search
	 *            a tree search filter
	 *
	 * @return the child, or {@code null} if there is no child at this location
	 */
	public IFigure findChildAt(IFigure figure, int x, int y, TreeSearch search) {
		IFigure result = null;

		final List<? extends IFigure> children = getChildren(figure);

		for (int i = children.size() - 1; i >= 0; i--) {
			IFigure next = children.get(i);
			if (next.isVisible()) {
				next = next.findFigureAt(x, y, search);
				if (next != null) {
					result = next;
					break;
				}
			}
		}

		return result;
	}
}
