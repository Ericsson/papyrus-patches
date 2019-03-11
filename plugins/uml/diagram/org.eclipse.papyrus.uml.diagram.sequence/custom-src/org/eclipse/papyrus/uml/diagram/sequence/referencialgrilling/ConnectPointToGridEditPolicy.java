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

import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.gmf.runtime.notation.DecorationNode;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.uml2.uml.Element;

/**
 * This editPolicy is specific to register the position top left of the rectangle
 *
 */
public class ConnectPointToGridEditPolicy extends ConnectRectangleToGridEditPolicy {

	protected GrillingEditpart grillingCompartment=null;


	protected DecorationNode rowStart=null;
	protected DecorationNode columnStart=null;

	/**
	 * Constructor.
	 *
	 */
	public ConnectPointToGridEditPolicy() {
	}


	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectNodeToGridEditPolicy#initListeningColumnFinish(org.eclipse.gmf.runtime.notation.Node, org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.GrillingManagementEditPolicy, org.eclipse.draw2d.geometry.PrecisionRectangle, org.eclipse.uml2.uml.Element, org.eclipse.draw2d.geometry.PrecisionRectangle)
	 *
	 * @param node
	 * @param grilling
	 * @param originPosition
	 * @param element
	 * @param p
	 * @throws NoGrillElementFound
	 */
	@Override
	protected void initListeningColumnFinish(Node node, GridManagementEditPolicy grilling,  Element element, PrecisionRectangle p) throws NoGrillElementFound {
		//the ColumnFinish is not listen
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectNodeToGridEditPolicy#initListeningRowFinish(org.eclipse.gmf.runtime.notation.Node, org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.GrillingManagementEditPolicy, org.eclipse.draw2d.geometry.PrecisionRectangle, org.eclipse.uml2.uml.Element, org.eclipse.draw2d.geometry.PrecisionRectangle)
	 *
	 * @param node
	 * @param grilling
	 * @param originPosition
	 * @param element
	 * @param p
	 * @throws NoGrillElementFound
	 */
	@Override
	protected void initListeningRowFinish(Node node, GridManagementEditPolicy grilling, Element element, PrecisionRectangle p) throws NoGrillElementFound {
		//the RowFinish is not listen
	}




	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectNodeToGridEditPolicy#updateColumFinishFromWitdhNotification(org.eclipse.draw2d.geometry.PrecisionRectangle)
	 *
	 * @param notationBound
	 */
	@Override
	protected void updateColumFinishFromWitdhNotification(PrecisionRectangle notationBound) {
		//the ColumnFinish is not listen
	}
	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectNodeToGridEditPolicy#updateRowFinishStartFromHeightNotification(org.eclipse.draw2d.geometry.PrecisionRectangle)
	 *
	 * @param p
	 */
	@Override
	protected void updateRowFinishFromHeightNotification(PrecisionRectangle p) {
		//the RowFinish is not listen
	}

}
