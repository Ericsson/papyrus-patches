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

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.gef.ui.figures.SlidableAnchor;

/**
 * Internal class to manage anchors snappable to grid
 */
public class ExecutionSpecificationSlidableAnchor extends SlidableAnchor {

	private EditPart editPart;

	/**
	 * Constructor.
	 *
	 * @param f
	 * @param p
	 */
	public ExecutionSpecificationSlidableAnchor(NodeFigure f, PrecisionPoint p) {
		super(f, p);
	}

	@Override
	public Point getLocation(Point refParent, Point refPort) {
		Rectangle r = super.getOwner().getBounds().getCopy();
		getOwner().translateToAbsolute(r);
		Point loc = refParent.getCopy();
		loc.x = r.x;
		if (refParent.x < refPort.x) {
			loc.x += r.width;					
		}			
		return loc;
	}			

	public void setEditPart(EditPart editPart) {
		this.editPart = editPart;
	}

	public EditPart getEditPart() {
		return this.editPart;
	}
}
