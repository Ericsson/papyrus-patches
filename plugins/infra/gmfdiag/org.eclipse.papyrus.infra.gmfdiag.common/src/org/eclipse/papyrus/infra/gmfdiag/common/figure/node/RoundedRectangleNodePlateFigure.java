/*****************************************************************************
 * Copyright (c) 2010, 2014 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and Implementation
 * 
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.figure.node;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;

/**
 * this figure is used to display at the good position anchor on a
 * Rounded Rectangle Figure.
 */
public class RoundedRectangleNodePlateFigure extends DefaultSizeNodeFigure implements IRoundedRectangleFigure, ICustomNodePlate {

	/** The svg node plate figure. */
	protected SVGNodePlateFigure svgNodePlateFigure = null;

	// @unused
	/**
	 * Instantiates a new rounded rectangle node plate figure.
	 *
	 * @param defSize
	 *            the def size
	 */
	public RoundedRectangleNodePlateFigure(Dimension defSize) {
		super(defSize);
	}


	/**
	 * Instantiates a new rounded rectangle node plate figure.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public RoundedRectangleNodePlateFigure(int width, int height) {
		super(width, height);
	}

	/**
	 * Gets the Rounded Compartment figure, it's a child of RoundedCompartmentNodePlateFigure.
	 *
	 * @return the RoundedCompartment figure
	 */
	public IRoundedRectangleFigure getRoundedCompartmentFigure() {
		if (getChildren().size() > 0 && getChildren().get(0) instanceof IRoundedRectangleFigure) {
			return (IRoundedRectangleFigure) getChildren().get(0);
		}
		if (svgNodePlateFigure != null && svgNodePlateFigure.getChildren().size() > 0 && svgNodePlateFigure.getChildren().get(0) instanceof IRoundedRectangleFigure) {
			return (IRoundedRectangleFigure) svgNodePlateFigure.getChildren().get(0);
		}
		return null;
	}

	/**
	 * Gets the corner dimensions.
	 *
	 * @return the corner dimensions
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#getCornerDimensions()
	 */
	@Override
	public Dimension getCornerDimensions() {
		return getRoundedCompartmentFigure().getCornerDimensions();
	}

	/**
	 * Gets the rounded rectangle bounds.
	 *
	 * @return the rounded rectangle bounds
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#getRoundedRectangleBounds()
	 */
	@Override
	public Rectangle getRoundedRectangleBounds() {
		return getRoundedCompartmentFigure().getBounds();
	}

	/**
	 * Sets the SVG node plate container.
	 *
	 * @param svgNodePlateFigure
	 *            the new SVG node plate container
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.ICustomNodePlate#setSVGNodePlateContainer(SVGNodePlateFigure)
	 */
	@Override
	public void setSVGNodePlateContainer(SVGNodePlateFigure svgNodePlateFigure) {
		this.svgNodePlateFigure = svgNodePlateFigure;
	}

	/**
	 * Gets the svg node plate container.
	 *
	 * @return the svg node plate container
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.ICustomNodePlate#getSvgNodePlateContainer()
	 */
	@Override
	public SVGNodePlateFigure getSvgNodePlateContainer() {
		return this.svgNodePlateFigure;
	}

	/**
	 * Creates a slidable anchor at the specified point (from the ratio of the
	 * reference's coordinates and bounds of the figure.
	 *
	 * @param p
	 *            - relative reference for the <Code>SlidableAnchor</Code>
	 * @return a <code>SlidableAnchor</code> for this figure with relative reference at p
	 */
	@Override
	protected ConnectionAnchor createAnchor(PrecisionPoint p) {
		if (p == null)
			// If the old terminal for the connection anchor cannot be resolved (by SlidableAnchor) a null
			// PrecisionPoint will passed in - this is handled here
			return createDefaultAnchor();
		return new SlidableRoundedRectangleAnchor(this, p);
	}

	/**
	 * Creates the default Slidable anchor with a reference point at the center
	 * of the figure's bounds.
	 *
	 * @return - default SlidableAnchor, relative reference the center of the figure
	 */
	@Override
	protected ConnectionAnchor createDefaultAnchor() {
		return new SlidableRoundedRectangleAnchor(this);
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#setCornerDimensions(org.eclipse.draw2d.geometry.Dimension)
	 *
	 * @param dimension
	 */
	@Override
	// unused
	public void setCornerDimensions(Dimension dimension) {
		getRoundedCompartmentFigure().setCornerDimensions(dimension);
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#setOval(boolean)
	 *
	 * @param booleanValue
	 */
	@Override
	// unused
	public void setOval(boolean booleanValue) {
		getRoundedCompartmentFigure().setOval(booleanValue);
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#isOval()
	 *
	 * @return
	 */
	@Override
	// unused
	public boolean isOval() {
		return getRoundedCompartmentFigure().isOval();
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#setFloatingNameConstrained(boolean)
	 *
	 * @param booleanValue
	 */
	@Override
	// unused
	public void setFloatingNameConstrained(boolean booleanValue) {
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#isFloatingNameConstrained()
	 *
	 * @return
	 */
	@Override
	// unused
	public boolean isFloatingNameConstrained() {
		return false;
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#setFloatingNameOffset(org.eclipse.draw2d.geometry.Dimension)
	 *
	 * @param offset
	 */
	@Override
	// unused
	public void setFloatingNameOffset(Dimension offset) {

	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#getFloatingNameOffset()
	 *
	 * @return
	 */
	@Override
	// unused
	public Dimension getFloatingNameOffset() {
		return null;
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#setBorderStyle(int)
	 *
	 * @param borderStyle
	 */
	@Override
	// unused
	public void setBorderStyle(int borderStyle) {
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#setCustomDash(int[])
	 *
	 * @param dash
	 */
	@Override
	// unused
	public void setCustomDash(int[] dash) {

	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#setHasHeader(boolean)
	 *
	 * @param hasHeader
	 */
	@Override
	public void setHasHeader(boolean hasHeader) {

	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#hasHeader()
	 *
	 * @return
	 */
	@Override
	public boolean hasHeader() {
		return false;
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#getPackageHeader()
	 *
	 * @return
	 */
	@Override
	public Rectangle getPackageHeader() {
		return null;
	}


	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#setShadowWidth(int)
	 *
	 * @param shadowWidth
	 */
	@Override
	public void setShadowWidth(int shadowWidth) {

	}


	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#setIsPackage(boolean)
	 *
	 * @param isPackage
	 */
	@Override
	public void setIsPackage(boolean isPackage) {

	}


	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#getShadowColor()
	 *
	 * @return
	 */
	@Override
	public String getShadowColor() {
		return null;
	}


	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#setShadowColor(java.lang.String)
	 *
	 * @param shadowColor
	 */
	@Override
	public void setShadowColor(String shadowColor) {
	}


	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#getNameBackgroundColor()
	 *
	 * @return
	 */
	@Override
	public String getNameBackgroundColor() {
		return null;
	}


	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure#setNameBackgroundColor(java.lang.String)
	 *
	 * @param labelBackgroundColor
	 */
	@Override
	public void setNameBackgroundColor(final String labelBackgroundColor) {
		// Do nothing
	}

}
