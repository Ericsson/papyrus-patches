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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * An interface for figures which have a rounded rectangle shape and need to
 * provide a connection anchor and a floating Name.
 * 
 */
public interface IRoundedRectangleFigure extends IFigure {

	/**
	 * Returns the rounded rectangles corner dimensions.
	 * 
	 * @return the corner dimensions
	 */
	Dimension getCornerDimensions();

	/**
	 * Sets the rounded rectangles corner dimensions.
	 *
	 * @param CornerDimension
	 *            the new corner dimensions
	 */
	void setCornerDimensions(Dimension CornerDimension);

	/**
	 * Returns the rounded rectangles bounds.
	 * 
	 * @return the bounds
	 */
	Rectangle getRoundedRectangleBounds();

	/**
	 * Sets if the shape must be oval.
	 *
	 * @param isOval
	 *            the new oval
	 */
	void setOval(boolean isOval);

	/**
	 * Returns true if the shape is forced to be oval.
	 *
	 * @return isOval
	 */
	boolean isOval();

	/**
	 * Sets true if the floating name must be constrained.
	 *
	 * @param isConstrained
	 *            the new floating name constrained
	 */
	void setFloatingNameConstrained(boolean isConstrained);

	/**
	 * Returns true if the floating name is constrained.
	 *
	 * @return isAttached
	 */
	boolean isFloatingNameConstrained();

	/**
	 * Sets the floating Name offset for not constrained case.
	 *
	 * @param offset
	 *            the new floating name offset
	 */
	void setFloatingNameOffset(Dimension offset);


	/**
	 * Returns the offset of the floating Name offset for not constrained case.
	 *
	 * @return the offset
	 */
	Dimension getFloatingNameOffset();

	/**
	 * Sets the border style.
	 *
	 * @param borderStyle
	 *            the new border style
	 */
	public void setBorderStyle(int borderStyle);

	/** Set custom dash values */
	public void setCustomDash(int[] dash);

	/**
	 * Sets the checks for header.
	 *
	 * @param hasHeader
	 *            the new checks for header
	 */
	public void setHasHeader(boolean hasHeader);

	/**
	 * Checks for header.
	 *
	 * @return true, if successful
	 */
	public boolean hasHeader();

	/**
	 * Gets the Package Header
	 * 
	 * @return
	 */
	public Rectangle getPackageHeader();

	/**
	 * Sets the shadow width.
	 *
	 * @param shadowWidth
	 *            the new shadow width
	 */
	public void setShadowWidth(int shadowWidth);


	/**
	 * Sets the checks if is package.
	 *
	 * @param isPackage
	 *            the new checks if is package
	 */
	public void setIsPackage(boolean isPackage);

	/**
	 * Gets the shadow color.
	 *
	 * @return the shadowColor
	 */
	public String getShadowColor();

	/**
	 * Sets the shadow color.
	 *
	 * @param shadowColor
	 *            the shadowColor to set
	 */
	public void setShadowColor(String shadowColor);


	/**
	 * Gets the Name Background color.
	 *
	 * @return the Label BackgroundColor
	 */
	public String getNameBackgroundColor();

	/**
	 * Sets the Label Background color.
	 *
	 * @param shadowColor
	 *            the LabelBackgroundColor to set
	 */
	public void setNameBackgroundColor(String nameBackgroundColor);

}
