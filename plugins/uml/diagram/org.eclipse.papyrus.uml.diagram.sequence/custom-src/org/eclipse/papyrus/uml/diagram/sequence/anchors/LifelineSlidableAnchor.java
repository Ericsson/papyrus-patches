/*****************************************************************************
 * (c) Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.anchors;

import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.gef.ui.figures.SlidableAnchor;

/**
 * Internal class to manage anchors snappable to grid
 */
public class LifelineSlidableAnchor extends SlidableAnchor {

	private EditPart editPart;

	/**
	 * Constructor.
	 *
	 * @param f
	 * @param p
	 */
	public LifelineSlidableAnchor(NodeFigure f, PrecisionPoint p) {
		super(f, p);
	}

	public void setEditPart(EditPart editPart) {
		this.editPart = editPart;
	}

	public EditPart getEditPart() {
		return this.editPart;
	}
}
