/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.LinkLFSVGNodePlateFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.RoundedRectangleNodePlateFigure;
import org.eclipse.papyrus.uml.diagram.sequence.anchors.AnchorConstants;
import org.eclipse.papyrus.uml.diagram.sequence.anchors.CenterAnchor;

/**
 * <p>
 * Custom {@link LinkLFSVGNodePlateFigure} to support custom Anchors for the Destruction
 * Figure.
 * </p>
 *
 * @see CenterAnchor
 * @see AnchorConstants#CENTER_TERMINAL
 */
public class DestructionEventNodePlate extends RoundedRectangleNodePlateFigure {

	public DestructionEventNodePlate(Dimension defSize) {
		super(defSize);
	}

	public DestructionEventNodePlate(int width, int height) {
		super(width, height);
	}

	@Override
	public ConnectionAnchor getConnectionAnchor(String terminal) {
		if (AnchorConstants.CENTER_TERMINAL.equals(terminal)) {
			return new CenterAnchor(this);
		}
		return super.getConnectionAnchor(terminal);
	}

	@Override
	public String getConnectionAnchorTerminal(ConnectionAnchor c) {
		if (c instanceof CenterAnchor) {
			return AnchorConstants.CENTER_TERMINAL;
		}
		return super.getConnectionAnchorTerminal(c);
	}

}
