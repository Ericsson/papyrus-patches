/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
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
		if (Math.abs(this.getBounds().x + this.getBounds().width / 2 - x) < 20) {
			return super.containsPoint(x, y); // check also the other bounds
		}
		return false;
	}

	@Override
	public IFigure findFigureAt(int x, int y, TreeSearch search) {
		if (search.prune(this)) {
			return null;
		}
		IFigure result = getBorderItemContainer().findFigureAt(x, y, search);
		if (result != null) {
			return result;
		}
		return getMainFigure().findFigureAt(x, y, search);
	}

}