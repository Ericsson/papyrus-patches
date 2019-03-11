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


package org.eclipse.papyrus.infra.gmfdiag.common.utils;

import org.eclipse.draw2d.Graphics;


/**
 * Enumeration to define line style as String related to the Open Declaration org.eclipse.draw2d.Graphics line styles
 * 
 * @author Mickael ADAM
 *
 */
public enum LineStyleEnum {

	/** The dash. */
	DASH(Graphics.LINE_DASH, "dash"), //$NON-NLS-1$

	/** The dash dot. */
	DASH_DOT(Graphics.LINE_DASHDOT, "dashDot"), //$NON-NLS-1$

	/** The dash dot dot. */
	DASH_DOT_DOT(Graphics.LINE_DASHDOTDOT, "dashDotDot"), //$NON-NLS-1$

	/** The dot. */
	DOT(Graphics.LINE_DOT, "dot"), //$NON-NLS-1$

	/** The solid. */
	SOLID(Graphics.LINE_SOLID, "solid"), //$NON-NLS-1$

	/** The custom. */
	CUSTOM(Graphics.LINE_CUSTOM, "custom"); //$NON-NLS-1$

	/** The line style. */
	private int lineStyle;

	/** The literal. */
	private String literal;

	/**
	 * Instantiates a new line style enum.
	 *
	 * @param lineStyle
	 *            the line style
	 * @param literal
	 *            the literal
	 */
	private LineStyleEnum(int lineStyle, String literal) {
		this.lineStyle = lineStyle;
		this.literal = literal;
	}

	/**
	 * Gets the line style.
	 *
	 * @return the line style
	 */
	public int getLineStyle() {
		return lineStyle;
	}

	/**
	 * Gets the literal.
	 *
	 * @return the literal
	 */
	public String getLiteral() {
		return literal;
	}

	/** The Constant LINE_STYLE_ARRAY. */
	private static final LineStyleEnum[] LINE_STYLE_ARRAY = new LineStyleEnum[] { DASH, DASH_DOT, DASH_DOT_DOT, DOT, SOLID, CUSTOM, };

	/**
	 * Gets the by literal.
	 *
	 * @param literal
	 *            the literal
	 * @return the by literal
	 */
	public static LineStyleEnum getByLiteral(String literal) {
		for (int i = 0; i < LINE_STYLE_ARRAY.length; ++i) {
			LineStyleEnum result = LINE_STYLE_ARRAY[i];
			if (result.getLiteral().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the literal value of the enumerator, which is its string representation.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return literal;
	}
}
