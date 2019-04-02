/*****************************************************************************
 * Copyright (c) 2005, 2018 AIRBUS FRANCE, CEA LIST, EclipseSource and others. 
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
 * EclipseSource - Bug 536638, Bug 536641
 ****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Graphics;
import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;
import org.eclipse.papyrus.uml.diagram.sequence.anchors.AnchorConstants;
import org.eclipse.papyrus.uml.diagram.sequence.anchors.CenterAnchor;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;

/**
 * Figure for a {@link DestructionOccurrenceSpecification}. It is drawn as an X centered over a Lifeline body
 */
public class DestructionEventFigure extends DefaultSizeNodeFigure {

	private int lineWidth = 1;

	/**
	 * Constructor <!-- begin-user-doc --> <!-- end-user-doc -->
	 */
	public DestructionEventFigure() {
		super(40, 40);
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
		graphics.drawLine(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
		graphics.drawLine(bounds.x, bounds.y + bounds.height, bounds.x + bounds.width, bounds.y);
		graphics.popState();
	}

	@Override
	public void setLineWidth(int w) {
		if ((lineWidth == w) || (w < 0)) {
			return;
		}
		lineWidth = w;
		repaint();
	}

	@Override
	public ConnectionAnchor getConnectionAnchor(String terminal) {
		if (AnchorConstants.CENTER_TERMINAL.equals(terminal)) {
			return new CenterAnchor(this);
		}
		return super.getConnectionAnchor(terminal);
	}

	@Override
	public String getConnectionAnchorTerminal(ConnectionAnchor c) {
		if (c instanceof CenterAnchor) {
			return AnchorConstants.CENTER_TERMINAL;
		}
		return super.getConnectionAnchorTerminal(c);
	}

}
