/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   Christian W. Damus - bug 539373
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.SelectableBorderedNodeFigure;

/**
 * Specific figure to handle selection for lifelines.
 */
public class LifelineNodeFigure extends SelectableBorderedNodeFigure {

	/**
	 * Constructor.
	 *
	 * @param mainFigure
	 *            the node plate figure
	 */
	public LifelineNodeFigure(IFigure mainFigure) {
		super(mainFigure);
	}

	@Override
	public boolean containsPoint(int x, int y) {
		// Hit test the lifeline itself with execution specifications
		boolean result = getMainFigure().containsPoint(x, y);
		if (!result) {
			// Try the border items (e.g., time observations)
			result = FigureHitTestUtil.INSTANCE.anyChildContainsPoint(
					getBorderItemContainer(), x, y);
		}
		return result;
	}

	@Override
	public IFigure findFigureAt(int x, int y, TreeSearch search) {
		if (search.prune(this)) {
			return null;
		}
		IFigure result = FigureHitTestUtil.INSTANCE.findChildAt(getBorderItemContainer(), x, y, search);
		if (result != null) {
			return result;
		}
		return getMainFigure().findFigureAt(x, y, search);
	}

}
