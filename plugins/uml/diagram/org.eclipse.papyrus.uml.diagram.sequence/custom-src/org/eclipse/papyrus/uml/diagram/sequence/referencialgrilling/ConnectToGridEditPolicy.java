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

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;



import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.gef.ui.internal.editpolicies.GraphicalEditPolicyEx;
import org.eclipse.gmf.runtime.notation.DecorationNode;
import org.eclipse.gmf.runtime.notation.Location;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.AutomaticNotationEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetLocationCommand;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.util.LogOptions;

/**
 * This class is  a class that contains method to set position to the grid
 *
 */
public abstract class ConnectToGridEditPolicy extends GraphicalEditPolicyEx implements AutomaticNotationEditPolicy, IGrillingEditpolicy {

	protected int displayImprecision=2;

	/**
	 * update an axis of the grid from coordinate X or Y
	 * @param axis the axis to update
	 * @param x the coordinate x
	 * @param y the coordinate y
	 */
	protected void updatePositionGridAxis(DecorationNode axis, int x, int y) {
		Location currentBounds=(Location)	axis.getLayoutConstraint();
		if(x<currentBounds.getX()-displayImprecision||x>currentBounds.getX()+displayImprecision){
			UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "+---->ACTION: modifiy AXIS FINISH to  x=" + x);//$NON-NLS-1$
			execute( new SetLocationCommand(getDiagramEditPart(getHost()).getEditingDomain(), "update Column", new EObjectAdapter(axis), new Point(x,y)));

		}
		if(y<currentBounds.getY()-displayImprecision||y>currentBounds.getY()+displayImprecision){
			UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "+---->ACTION: modifiy AXIS FINISH to  y=" + y);//$NON-NLS-1$
			execute( new SetLocationCommand(getDiagramEditPart(getHost()).getEditingDomain(), "update row", new EObjectAdapter(axis), new Point(x,y)));
		}
	}

}