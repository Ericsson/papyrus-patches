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

package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.LinkLFSVGNodePlateFigure;
import org.eclipse.papyrus.uml.diagram.sequence.edit.helpers.AnchorHelper;

/**
 * @since 3.0
 *
 */
public class ExecutionSpecificationNodePlate extends LinkLFSVGNodePlateFigure implements ILifelineInternalFigure {
	/**
	 * Constructor.
	 *
	 * @param hostEP
	 * @param width
	 * @param height
	 */
	public ExecutionSpecificationNodePlate(GraphicalEditPart hostEP, int width, int height) {
		super(hostEP, width, height);
	}

	/**
	 * @see org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure#isDefaultAnchorArea(org.eclipse.draw2d.geometry.PrecisionPoint)
	 */
	@Override
	protected boolean isDefaultAnchorArea(PrecisionPoint p) {
		return false;
	}

	@Override
	public ConnectionAnchor getConnectionAnchor(String terminal) {
		// Use FixedAnchorEx for MessageSync, this will be invoked by mapConnectionAnchor(termial) operation.
		if (terminal != null && terminal.indexOf("{") != -1 && terminal.indexOf("}") != -1) {
			int position = AnchorHelper.FixedAnchorEx.parsePosition(terminal);
			if (PositionConstants.TOP == position || PositionConstants.BOTTOM == position) {
				return new AnchorHelper.FixedAnchorEx(this, position);
			}
		}
		return super.getConnectionAnchor(terminal);
	}
}
