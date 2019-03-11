/*****************************************************************************
 * Copyright (c) 2016 CEA LIST and others.
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

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.notation.Location;
import org.eclipse.papyrus.uml.diagram.sequence.figures.ReferencialGrid;

/**
 * @author Patrick Tessier
 * @since 3.0
 *
 */
public class GrillingEditpart extends GraphicalEditPart {

	/**
	 * Constructor.
	 *
	 * @param model
	 */
	public GrillingEditpart(EObject model) {
		super(model);
	}

	public static final String VISUAL_ID="GRILLING";
	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#setVisibility(boolean)
	 *
	 * @param vis
	 */
	@Override
	protected void setVisibility(boolean vis) {
		super.setVisibility(false);
		//super.setVisibility(true);
	}
	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#createFigure()
	 *
	 * @return
	 */
	@Override
	protected IFigure createFigure() {
		Figure fig= new ReferencialGrid();

		return fig;
	}
	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#refreshVisuals()
	 *
	 */
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		if(getNotationView().isVisible()) {
			getFigure().setBounds(new Rectangle (0,0,50,1000));
			((ReferencialGrid)getFigure()).cleanAllLines();
			DiagramEditPart diagramEditPart=(DiagramEditPart)this.getParent();
			GridManagementEditPolicy grid=(GridManagementEditPolicy)diagramEditPart.getEditPolicy(GridManagementEditPolicy.GRID_MANAGEMENT);
			for (int i=0; i<grid.rows.size();i++) {
				if( grid.rows.get(i)!=null) {
					Location location= (Location)grid.rows.get(i).getLayoutConstraint();
					if( location!=null) {
						((ReferencialGrid)getFigure()).displayLine(location.getY());
					}
				}
			}
		}
	}
}
