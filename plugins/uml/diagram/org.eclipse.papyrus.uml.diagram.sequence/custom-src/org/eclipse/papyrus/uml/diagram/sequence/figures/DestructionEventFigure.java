/*****************************************************************************
 * Copyright (c) 2005 AIRBUS FRANCE. 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: 
 * David Sciamma (Anyware Technologies), 
 * Mathieu Garcia (Anyware Technologies),
 * Jacques Lescot (Anyware Technologies), 
 * Thomas Friol (Anyware Technologies),
 * Nicolas Lalevee (Anyware Technologies) - initial API and implementation
 *
 ****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;

/**
 * <!-- begin-user-doc --> <!-- end-user-doc -->
 *
 * @generated
 */
public class DestructionEventFigure extends org.eclipse.draw2d.Figure {

	/**
	 * Constructor <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	public DestructionEventFigure() {
		super();
	}
	/**
	 * @return a <code>Dimension</code> that represents the minimum or default size of
	 * this figure.
	 * @since 3.0
	 */
	public Dimension getDefaultSize() {
		return new Dimension(40,40);
	}
	/**
	 * The stop is a cross
	 *
	 * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
	 */
	@Override
	protected void paintFigure(Graphics graphics) {
		super.paintFigure(graphics);
		graphics.pushState();
		graphics.setLineWidth(2);
		graphics.drawLine(bounds.x, bounds.y, bounds.x + bounds.width,  bounds.y + bounds.height);
		graphics.drawLine(bounds.x, bounds.y+ bounds.height, bounds.x + bounds.width,  bounds.y);
		graphics.popState();
	}

	public void setLineWidth(int w) {
		if ((lineWidth == w) || (w < 0)) {
			return;
		}
		lineWidth = w;
		repaint();
	}

	private int lineWidth = 1;
}
