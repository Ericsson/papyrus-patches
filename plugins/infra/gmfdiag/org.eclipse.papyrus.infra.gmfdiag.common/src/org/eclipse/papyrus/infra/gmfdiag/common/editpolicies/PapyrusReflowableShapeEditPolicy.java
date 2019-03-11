/*****************************************************************************
 * Copyright (c) 2013 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *		CEA LIST - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.tools.ResizeTracker;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeUtil;
import org.eclipse.papyrus.infra.gmfdiag.common.snap.ResizeTrackerWithPreferences;

/**
 * Extends {@link PapyrusResizableShapeEditPolicy} with ability to correctly handle
 * figures that can reflow, that is figures with minimum size depending on the width.
 * <p/>
 * Normally such figures contain {@link FlowPage} inside, with the most used example of bent-corner figure for UML comments. There may be some other
 * examples, e.g, org.eclipse.papyrus.uml.diagram.common.figure.node.IMultilineEditableFigure's are expected to have some re-flowable behavior.
 * <p/>
 * When drawing the Change bounds feedback for such figures we need to take into account the suggested width and compute minimum size using it as a
 * hint.
 */
public class PapyrusReflowableShapeEditPolicy extends PapyrusResizableShapeEditPolicy {

	/**
	 * 
	 * @see org.eclipse.gef.editpolicies.ResizableEditPolicy#getResizeTracker(int)
	 * 
	 * @param direction
	 * @return
	 */
	@Override
	protected ResizeTracker getResizeTracker(int direction) {
		return new ResizeTrackerWithPreferences((GraphicalEditPart)getHost(), direction);
	}

	/**
	 * The copy of super-class method with the computation of the minimum size extracted
	 * to separate method.
	 * 
	 * @see PapyrusReflowableShapeEditPolicy#computeMinimumSizeFor(Dimension)
	 */
	@Override
	protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
		IFigure feedback = getDragSourceFeedbackFigure();

		PrecisionRectangle rect = new PrecisionRectangle(getInitialFeedbackBounds().getCopy());
		getHostFigure().translateToAbsolute(rect);
		rect.translate(request.getMoveDelta());
		rect.resize(request.getSizeDelta());

		IFigure f = getHostFigure();
		// difference to super-class START
		Dimension min = computeMinimumSizeFor(rect.getSize());
		// different to super-class END
		Dimension max = f.getMaximumSize().getCopy();
		IMapMode mmode = MapModeUtil.getMapMode(f);
		min.height = mmode.LPtoDP(min.height);
		min.width = mmode.LPtoDP(min.width);
		max.height = mmode.LPtoDP(max.height);
		max.width = mmode.LPtoDP(max.width);

		if(min.width > rect.width)
			rect.width = min.width;
		else if(max.width < rect.width)
			rect.width = max.width;

		if(min.height > rect.height)
			rect.height = min.height;
		else if(max.height < rect.height)
			rect.height = max.height;

		feedback.translateToRelative(rect);
		feedback.setBounds(rect);

	}

	/**
	 * Instead of respecting static {@link IFigure#getMinimumSize()} computes proposed
	 * width and uses it as a hint for {@link IFigure#getMinimumSize(int, int)}.
	 */
	protected Dimension computeMinimumSizeFor(Dimension suggestedSize) {
		//super class just returns f.getMinimumSize().getCopy() here
		IFigure f = getHostFigure();
		//assuming that width is more important than height for user
		return f.getMinimumSize(suggestedSize.width, -1);
	}

}
