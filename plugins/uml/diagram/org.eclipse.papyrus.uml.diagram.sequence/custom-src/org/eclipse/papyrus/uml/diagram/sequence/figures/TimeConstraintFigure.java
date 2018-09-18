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

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IPapyrusNodeFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.uml2.uml.TimeConstraint;
import org.eclipse.uml2.uml.TimeObservation;

/**
 * Specific figure for the {@link TimeConstraint} & {@link TimeObservation}. This may be simplified later as a single line only
 */
public class TimeConstraintFigure extends DefaultSizeNodeFigure implements IPapyrusNodeFigure {

	public TimeConstraintFigure() {
		super(60, 1);
	}

	@Override
	public void paintFigure(Graphics graphics) {
		super.paintFigure(graphics);

		graphics.pushState();

		Rectangle clipRectangle = new Rectangle();
		graphics.getClip(clipRectangle);
		graphics.setClip(clipRectangle.expand(Math.max(0, getLineWidth()), Math.max(0, getLineWidth())));

		graphics.setLineWidth(getLineWidth());
		graphics.drawLine(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y);

		graphics.popState();
	}


	@Override
	public Color getBorderColor() {
		return null;
	}

	@Override
	public boolean isShadow() {
		return false;
	}

	@Override
	public void setBorderColor(Color borderColor) {

	}

	@Override
	public void setShadow(boolean shadow) {

	}

}
