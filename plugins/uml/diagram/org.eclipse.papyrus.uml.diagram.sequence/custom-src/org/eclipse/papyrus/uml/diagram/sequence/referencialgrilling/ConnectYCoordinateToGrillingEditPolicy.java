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
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.uml2.uml.Element;

/**
 * This editPolicy is specific to register the Y Top and the T bottom of the node
 *
 */
public class ConnectYCoordinateToGrillingEditPolicy extends ConnectRectangleToGridEditPolicy {

	protected GrillingEditpart grillingCompartment=null;

	public static String CONNECT_TO_GRILLING_MANAGEMENT="CONNECT_TO_GRILLING_MANAGEMENT";

	protected View rowStart=null;
	protected View rowFinish=null;

	/**
	 * Constructor.
	 *
	 */
	public ConnectYCoordinateToGrillingEditPolicy() {
	}
	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectRectangleToGridEditPolicy#initListeningColumnFinish(org.eclipse.gmf.runtime.notation.Node, org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.GridManagementEditPolicy, org.eclipse.uml2.uml.Element, org.eclipse.draw2d.geometry.PrecisionRectangle)
	 *
	 * @param node
	 * @param grilling
	 * @param element
	 * @param bounds
	 * @throws NoGrillElementFound
	 */
	@Override
	protected void initListeningColumnFinish(Node node, GridManagementEditPolicy grilling, Element element, PrecisionRectangle bounds) throws NoGrillElementFound {
		//do nothing
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectRectangleToGridEditPolicy#initListeningColumnStart(org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.GridManagementEditPolicy, org.eclipse.uml2.uml.Element, org.eclipse.draw2d.geometry.PrecisionRectangle)
	 *
	 * @param grilling
	 * @param element
	 * @param bounds
	 * @throws NoGrillElementFound
	 */
	@Override
	protected void initListeningColumnStart(GridManagementEditPolicy grilling, Element element, PrecisionRectangle bounds) throws NoGrillElementFound {
		//do nothing
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectRectangleToGridEditPolicy#updateColumFinishFromWitdhNotification(org.eclipse.draw2d.geometry.PrecisionRectangle)
	 *
	 * @param notationBound
	 */
	@Override
	protected void updateColumFinishFromWitdhNotification(PrecisionRectangle notationBound) {
		//do nothing
	}
	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectRectangleToGridEditPolicy#updateColumnStartFromXNotification(org.eclipse.draw2d.geometry.PrecisionRectangle)
	 *
	 * @param bounds
	 */
	@Override
	protected void updateColumnStartFromXNotification(PrecisionRectangle bounds) {
		//do nothing
	}

}
