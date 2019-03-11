/*****************************************************************************
 * Copyright (c) 2014 CEA LIST and others.
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
 *   Mickael ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 482586
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Bug 531596
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.figure.node;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.draw2d.ui.figures.BaseSlidableAnchor;
import org.eclipse.papyrus.infra.gmfdiag.tooling.runtime.linklf.LinkLFNodeFigure;

public class LinkLFSVGNodePlateFigure extends SVGNodePlateFigure {
	private static final double AVOID_DEFAULT_ANCHOR_AREA = 1.0;

	public static final String ENABLE_LINKLF = "papyrus.linklf_enable"; //$NON-NLS-1$

	private final GraphicalEditPart myHost;

	private boolean myLinkLFIsEnabled = false;

	/**
	 * {@inheritDoc}
	 */
	public LinkLFSVGNodePlateFigure(GraphicalEditPart hostEP, int width, int height) {
		super(width, height);
		myHost = hostEP;
	}

	/**
	 * {@inheritDoc}
	 */
	public LinkLFSVGNodePlateFigure withLinkLFEnabled() {
		myLinkLFIsEnabled = Boolean.getBoolean(ENABLE_LINKLF);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ConnectionAnchor createAnchor(PrecisionPoint p) {
		if (!myLinkLFIsEnabled) {
			return super.createAnchor(p);
		}
		if (p == null) {
			// If the old terminal for the connection anchor cannot be resolved (by SlidableAnchor) a null
			// PrecisionPoint will passed in - this is handled here
			return createDefaultAnchor();
		}

		PapyrusSlidableSnapToGridAnchor result;
		if (followSVGPapyrusPath) {
			result = new PapyrusSlidableSnapToGridAnchor(this, p);
		} else {
			result = new LinkLFSlidableRoundedRectangleAnchor(this, p);
		}
		result.setEditPart(myHost);

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected double getSlidableAnchorArea() {
		return myLinkLFIsEnabled ? AVOID_DEFAULT_ANCHOR_AREA : super.getSlidableAnchorArea();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ConnectionAnchor createConnectionAnchor(Point p) {
		if (!myLinkLFIsEnabled) {
			return super.createConnectionAnchor(p);
		}
		if (p == null) {
			return getConnectionAnchor(szAnchor);
		} else {
			Point temp = p.getCopy();
			translateToRelative(temp);
			PrecisionPoint pt = BaseSlidableAnchor.getAnchorRelativeLocation(temp, getBounds());
			if (isDefaultAnchorArea(pt)) {
				return getConnectionAnchor(szAnchor);
			}

			LinkLFNodeFigure.forceSideForBorderItemAnchorLocation(myHost, pt);

			return createAnchor(pt);
		}
	}
	
	/**
	 * Get the graphical edit part.
	 * 
	 * @return The myHost.
	 * @since 3.100
	 */
	public GraphicalEditPart getGraphicalEditPart() {
		return myHost;
	}
}
