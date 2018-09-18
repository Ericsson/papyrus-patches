/*****************************************************************************
 * Copyright (c) 2014, 2018 CEA LIST, EclipseSource and others.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Vincent Lorenzo (CEA LIST) vincent.lorenzo@cea.fr - Initial API and implementation
 *  EclipseSource - Bug 536638
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.helper;

import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.gmf.runtime.draw2d.ui.figures.BaseSlidableAnchor;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;

/**
 *
 * Helper used for identity anchor
 *
 */
public class IdentityAnchorHelper {

	/**
	 * the char starting an id of {@link IdentityAnchor}
	 */
	public static final char START_ID = '(';

	/**
	 * the char ending an id of {@link IdentityAnchor}
	 */

	public static final char END_ID = ')';

	/**
	 * the char separating percentage in ids of {@link IdentityAnchor}
	 */
	public static final char X_Y_SEPARATOR = ',';

	/**
	 * the char separating percentage as string in ids of {@link IdentityAnchor}
	 */
	public static final String X_Y_SEPARATOR_AS_STRING = Character.toString(X_Y_SEPARATOR);

	/**
	 *
	 * Constructor.
	 *
	 */
	private IdentityAnchorHelper() {
		// to prevent instanciation
	}

	/**
	 *
	 * @param anchor
	 *            an {@link IdentityAnchor} representing a {@link BaseSlidableAnchor}
	 * @return
	 * 		the value of x percentage
	 * @deprecated
	 * 			This method only supports {@link IdentityAnchor IdentityAnchors} representing a {@link BaseSlidableAnchor}. Other
	 *             anchors would cause an exception. Use {@link BaseSlidableAnchor#parseTerminalString(String)} instead; and check if the
	 *             resulting point is != null (If null, then the {@link IdentityAnchor} doesn't represent a {@link BaseSlidableAnchor})
	 */
	@Deprecated
	public static final double getXPercentage(final IdentityAnchor anchor) {
		PrecisionPoint point = BaseSlidableAnchor.parseTerminalString(anchor.getId());
		if (point == null) {
			throw new IllegalArgumentException("Anchor " + anchor.getId() + " is not a valid BaseSlidableAnchor");
		}
		return point.preciseX();
	}

	/**
	 *
	 * @param anchor
	 *            an anchor
	 * @return
	 * 		the value of y percentage
	 * @deprecated
	 * 			This method only supports {@link IdentityAnchor IdentityAnchors} representing a {@link BaseSlidableAnchor}. Other
	 *             anchors would cause an exception. Use {@link BaseSlidableAnchor#parseTerminalString(String)} instead; and check if the
	 *             resulting point is != null (If null, then the {@link IdentityAnchor} doesn't represent a {@link BaseSlidableAnchor})
	 */
	@Deprecated
	public static final double getYPercentage(final IdentityAnchor anchor) {
		PrecisionPoint point = BaseSlidableAnchor.parseTerminalString(anchor.getId());
		if (point == null) {
			throw new IllegalArgumentException("Anchor " + anchor.getId() + " is not a valid BaseSlidableAnchor");
		}
		return point.preciseY();
	}


	/**
	 *
	 * @param percentageOnX
	 *            the percentage on x
	 * @param percentageOnY
	 *            the percentage on y
	 * @return
	 * 		the string representing the new id for an anchor
	 */
	public static final String createNewAnchorIdValue(final double percentageOnX, final double percentageOnY) {
		final StringBuilder builder = new StringBuilder();
		builder.append(START_ID);
		builder.append(Double.toString(percentageOnX));
		builder.append(X_Y_SEPARATOR_AS_STRING);
		builder.append(Double.toString(percentageOnY));
		builder.append(END_ID);
		return builder.toString();
	}
}
