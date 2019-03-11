/*****************************************************************************
 * Copyright (c) 2017 CEA LIST, ALL4TEC and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and implementation
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.decoration;

import org.eclipse.draw2d.PolygonDecoration;

/**
 * The <i>"solid_arrow_filled</i> connection decoration.
 * 
 * @author Mickaël ADAM
 * @since 3.1
 */
public class SolidArrowFilledConnectionDecoration extends PolygonDecoration {

	/** Default X scale value. */
	private static final int DEFAULT_SCALE_X = 15;

	/** Default Y scale value. */
	private static final int DEFAULT_SCALE_Y = 5;


	/**
	 * Constructor.
	 */
	public SolidArrowFilledConnectionDecoration() {
		init();
	}

	/**
	 * Initialize the decoration.
	 */
	protected void init() {
		setTemplate(PolygonDecoration.TRIANGLE_TIP);
		setScale(DEFAULT_SCALE_X, DEFAULT_SCALE_Y);
		setFill(true);
	}

	/**
	 * <pre>
	 * Overridden to take into account of the line with in the scale (better representation).
	 * </pre>
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.draw2d.Polyline#setLineWidth(int)
	 */
	@Override
	public void setLineWidth(final int w) {
		setScale(DEFAULT_SCALE_X + w, DEFAULT_SCALE_Y + w);
		super.setLineWidth(w);
	}

}
