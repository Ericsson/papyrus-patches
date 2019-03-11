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

package org.eclipse.papyrus.infra.gmfdiag.common.locator;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.figures.IBorderItemLocator;
import org.eclipse.gmf.runtime.notation.View;

/**
 * The Interface IPapyrusBorderItemLocator.
 *
 */
public interface IPapyrusBorderItemLocator extends IBorderItemLocator {


	/**
	 * Gets the position.
	 *
	 * @return the position
	 */
	public int getPosition();

	/**
	 * Sets the position.
	 *
	 * @param position
	 *            the position to set
	 */
	public void setPosition(int position);

	/**
	 * Gets the offset.
	 *
	 * @return the offset
	 */
	public Dimension getOffset();

	/**
	 * Sets the offset.
	 *
	 * @param offset
	 *            the offset to set
	 */
	public void setOffset(Dimension offset);

	/**
	 * Checks if is constrained.
	 *
	 * @return the constrained
	 */
	public boolean isConstrained();

	/**
	 * Sets the constrained.
	 *
	 * @param constrained
	 *            the constrained to set
	 */
	public void setConstrained(boolean constrained);

	/**
	 * Sets the text alignment.
	 *
	 * @param textAlignment
	 *            the new text alignment
	 */
	public void setTextAlignment(int textAlignment);

	/**
	 * Checks if is initialize.
	 *
	 * @return true, if is initialize
	 */
	public boolean isInitialize();

	/**
	 * Sets the view.
	 *
	 * @param model
	 *            the new view
	 */
	public void setView(View model);

	/**
	 * Sets the editpart.
	 *
	 * @param editpart
	 *            the new editpart
	 */
	public void setEditpart(EditPart editpart);

	/**
	 * Sets the margin.
	 *
	 * @param margin
	 *            the new margin
	 */
	public void setMargin(Point margin);
}
