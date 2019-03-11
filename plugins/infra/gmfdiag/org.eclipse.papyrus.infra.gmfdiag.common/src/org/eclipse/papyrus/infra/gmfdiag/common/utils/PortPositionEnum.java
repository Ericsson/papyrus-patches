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


/**
 * The Enum PortPositionEnum for the position of ports on border.
 *
 * @author Mickael ADAM
 */
public enum PortPositionEnum {

	/** The inside. */
	INSIDE("inside"), //$NON-NLS-1$

	/** The outside. */
	OUTSIDE("outside"), //$NON-NLS-1$

	/** The online. */
	ONLINE("onLine"); //$NON-NLS-1$

	/** The literal. */
	private String literal;

	/**
	 * Instantiates a new port position enum.
	 *
	 * @param literal
	 *            the literal
	 */
	private PortPositionEnum(String literal) {
		this.literal = literal;
	}

	/**
	 * Gets the literal.
	 *
	 * @return the literal
	 */
	public String getLiteral() {
		return literal;
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
