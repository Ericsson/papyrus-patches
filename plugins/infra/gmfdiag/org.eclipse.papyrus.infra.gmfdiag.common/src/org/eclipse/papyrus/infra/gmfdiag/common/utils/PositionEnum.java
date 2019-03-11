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
 * The Enum PositionEnum.
 *
 * @author Mickael ADAM
 */
public enum PositionEnum {

	/** The north. */
	NORTH("NORTH", "north"), //$NON-NLS-1$ //$NON-NLS-2$

	/** The south. */
	SOUTH("SOUTH", "south"), //$NON-NLS-1$ //$NON-NLS-2$

	/** The east. */
	EAST("EAST", "east"), //$NON-NLS-1$ //$NON-NLS-2$

	/** The west. */
	WEST("WEST", "west"), //$NON-NLS-1$ //$NON-NLS-2$

	/** The none. */
	NONE("NONE", "none"), //$NON-NLS-1$ //$NON-NLS-2$

	/** The auto. */
	AUTO("AUTO", "AUTO"), //$NON-NLS-1$ //$NON-NLS-2$

	/** The left. */
	LEFT("LEFT", "left"), //$NON-NLS-1$ //$NON-NLS-2$

	/** The right. */
	RIGHT("RIGHT", "right"), //$NON-NLS-1$ //$NON-NLS-2$

	/** The center. */
	CENTER("CENTER", "center"); //$NON-NLS-1$ //$NON-NLS-2$

	/** The name. */
	private String name;

	/** The literal. */
	private String literal;

	/**
	 * Instantiates a new position enum.
	 *
	 * @param name
	 *            the name
	 * @param literal
	 *            the literal
	 */
	private PositionEnum(String name, String literal) {
		this.name = name;
		this.literal = literal;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
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
