/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
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
package org.eclipse.papyrus.uml.diagram.sequence.figures;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.transaction.RollbackException;
import org.eclipse.emf.transaction.Transaction;
import org.eclipse.emf.transaction.TransactionalCommandStack;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IBorderItemEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.figures.IBorderItemLocator;
import org.eclipse.gmf.runtime.notation.LayoutConstraint;
import org.eclipse.gmf.runtime.notation.Location;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.papyrus.infra.core.Activator;
import org.eclipse.papyrus.uml.diagram.common.locator.ExternalLabelPositionLocator;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry;

public class TimeMarkElementFigure extends SequencePolylineShape {

	/**
	 * The length of the time mark
	 */
	public static final int TIME_MARK_LENGTH = 20;

	/**
	 * The side where the figure currently is
	 */
	private int sideOfFigure = PositionConstants.NONE;

	public TimeMarkElementFigure() {
		this.addPoint(new Point(getMapModel().DPtoLP(0), getMapModel().DPtoLP(0)));
		this.addPoint(new Point(getMapModel().DPtoLP(20), getMapModel().DPtoLP(0)));
		this.setLocation(new Point(getMapModel().DPtoLP(0), getMapModel().DPtoLP(0)));
	}

	/**
	 * Update the side of the lifeline where the figure lies
	 *
	 * @param side
	 *            side where the figure must be
	 * @param newLocation
	 *            the new location rectangle
	 */
	public void setCurrentSideOfFigure(int side, Rectangle newLocation, EditPart host, String labelVisualId) {
		if (host == null || false == host instanceof IGraphicalEditPart) {
			return;
		}
		// no effect if side has not changed
		if (sideOfFigure != side && !(PositionConstants.NONE == sideOfFigure && side == PositionConstants.EAST)) {
			// mirror the label too
			IGraphicalEditPart labelChild = ((IGraphicalEditPart) host).getChildBySemanticHint(UMLVisualIDRegistry.getType(labelVisualId));
			if (labelChild instanceof IBorderItemEditPart) {
				IBorderItemEditPart label = (IBorderItemEditPart) labelChild;
				int labelWidth = label.getFigure().getMinimumSize().width;
				if (label.getNotationView() instanceof Node) {
					LayoutConstraint constraint = ((Node) label.getNotationView()).getLayoutConstraint();
					// update model location constraint for persisting the mirror effect
					if (constraint instanceof Location) {
						int xLocation = ((Location) constraint).getX();
						int mirroredLocation = -xLocation - labelWidth;
						TransactionalEditingDomain dom = ((IGraphicalEditPart) host).getEditingDomain();
						org.eclipse.emf.common.command.Command setCmd = SetCommand.create(dom, constraint, NotationPackage.eINSTANCE.getLocation_X(), mirroredLocation);
						TransactionalCommandStack stack = (TransactionalCommandStack) dom.getCommandStack();
						Map<String, Boolean> options = new HashMap<>();
						options.put(Transaction.OPTION_NO_NOTIFICATIONS, true);
						options.put(Transaction.OPTION_NO_UNDO, true);
						options.put(Transaction.OPTION_UNPROTECTED, true);
						try {
							stack.execute(setCmd, options);
							// then, update graphically for short time effect
							IBorderItemLocator locator = label.getBorderItemLocator();
							Rectangle constrRect = ((ExternalLabelPositionLocator) locator).getConstraint();
							constrRect.x = mirroredLocation;
							locator.relocate(label.getFigure());
						} catch (InterruptedException e) {
							// log and skip update
							Activator.log.error(e);
						} catch (RollbackException e) {
							// log and skip update
							Activator.log.error(e);
						}
					}
				}
			}
		}
		sideOfFigure = side;
	}
}