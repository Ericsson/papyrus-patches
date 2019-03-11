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
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.papyrus.uml.diagram.common.figure.node.RoundedCompartmentFigure;
import org.eclipse.papyrus.uml.diagram.sequence.figures.ILifelineInternalFigure;

public class ExecutionSpecificationRectangleFigure extends RoundedCompartmentFigure implements ILifelineInternalFigure {
	/**
	 */

	public ExecutionSpecificationRectangleFigure() {

	}

	@Override
	public IFigure findMouseEventTargetAt(int x, int y) {
		// check children first instead of self
		IFigure f = findMouseEventTargetInDescendantsAt(x, y);
		if (f != null) {
			return f;
		}
		if (!containsPoint(x, y)) {
			return null;
		}
		if (isMouseEventTarget()) {
			return this;
		}
		return null;
	}

	@Override
	public IFigure findFigureAt(int x, int y, TreeSearch search) {
		if (search.prune(this)) {
			return null;
		}
		IFigure child = findDescendantAtExcluding(x, y, search);
		if (child != null) {
			return child;
		}
		if (!containsPoint(x, y)) {
			return null;
		}
		if (search.accept(this)) {
			return this;
		}
		return null;
	}
}