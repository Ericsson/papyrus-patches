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

import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionSpecification;

/**
 * Only event of Execution are connected to the grid
 *
 */
public class ConnectExecutionToGridEditPolicy extends ConnectYCoordinateToGrillingEditPolicy {


	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectRectangleToGridEditPolicy#initListeningRowStart(org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.GridManagementEditPolicy, org.eclipse.uml2.uml.Element, org.eclipse.draw2d.geometry.PrecisionRectangle)
	 *
	 * @param grid
	 * @param element
	 * @param bounds
	 * @throws NoGrillElementFound
	 */
	@Override
	protected void initListeningRowStart(GridManagementEditPolicy grid, Element element, PrecisionRectangle bounds) throws NoGrillElementFound {
		super.initListeningRowStart(grid, ((ExecutionSpecification)element).getStart(), bounds);
	}
	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectRectangleToGridEditPolicy#initListeningRowFinish(org.eclipse.gmf.runtime.notation.Node, org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.GridManagementEditPolicy, org.eclipse.uml2.uml.Element, org.eclipse.draw2d.geometry.PrecisionRectangle)
	 *
	 * @param node
	 * @param grilling
	 * @param element
	 * @param bounds
	 * @throws NoGrillElementFound
	 */
	@Override
	protected void initListeningRowFinish(Node node, GridManagementEditPolicy grilling, Element element, PrecisionRectangle bounds) throws NoGrillElementFound {
		super.initListeningRowFinish(node, grilling, ((ExecutionSpecification)element).getFinish(), bounds);
	}
}
