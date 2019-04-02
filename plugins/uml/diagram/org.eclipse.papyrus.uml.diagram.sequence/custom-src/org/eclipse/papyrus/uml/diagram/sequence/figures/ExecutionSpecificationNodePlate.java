/*****************************************************************************
 * Copyright (c) 2017, 2018 CEA LIST, Christian W. Damus, and others.
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
 *   Christian W. Damus - bug 539373
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.figures.BorderedNodeFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.LinkLFSVGNodePlateFigure;
import org.eclipse.papyrus.uml.diagram.sequence.anchors.AnchorConstants;
import org.eclipse.papyrus.uml.diagram.sequence.anchors.NodeBottomAnchor;
import org.eclipse.papyrus.uml.diagram.sequence.anchors.NodeTopAnchor;
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
		if (AnchorConstants.START_TERMINAL.equals(terminal)) {
			return new NodeTopAnchor(this);
		} else if (AnchorConstants.END_TERMINAL.equals(terminal)) {
			return new NodeBottomAnchor(this);
		}

		// Use FixedAnchorEx for MessageSync, this will be invoked by mapConnectionAnchor(termial) operation.
		if (terminal != null && terminal.indexOf("{") != -1 && terminal.indexOf("}") != -1) {
			int position = AnchorHelper.FixedAnchorEx.parsePosition(terminal);
			if (PositionConstants.TOP == position || PositionConstants.BOTTOM == position) {
				return new AnchorHelper.FixedAnchorEx(this, position);
			}
		}
		return super.getConnectionAnchor(terminal);
	}

	@Override
	public String getConnectionAnchorTerminal(ConnectionAnchor c) {
		if (c instanceof NodeTopAnchor) {
			return AnchorConstants.START_TERMINAL;
		} else if (c instanceof NodeBottomAnchor) {
			return AnchorConstants.END_TERMINAL;
		}
		return super.getConnectionAnchorTerminal(c);
	}

	@Override
	public boolean containsPoint(int x, int y) {
		boolean result = super.containsPoint(x, y);
		if (!result) {
			// Hit test my border items
			BorderedNodeFigure parent = (BorderedNodeFigure) getParent();
			result = parent.getBorderItemContainer().containsPoint(x, y);
		}
		return result;
	}

	@Override
	public final IFigure findFigureAt(int x, int y, TreeSearch search) {
		IFigure result = super.findFigureAt(x, y, search);
		if (result == null) {
			// Search my border items
			BorderedNodeFigure parent = (BorderedNodeFigure) getParent();
			result = parent.getBorderItemContainer().findFigureAt(x, y, search);
		}
		return result;
	}
}
