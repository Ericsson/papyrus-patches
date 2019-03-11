/*****************************************************************************
 * Copyright (c) 2010-2017 CEA
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Soyatec - Initial API and implementation
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 521312, 522305
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.RotatableDecoration;
import org.eclipse.draw2d.geometry.Dimension;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class MessageFound extends MessageFigure {

	/**
	 * Constructor.
	 *
	 */
	public MessageFound() {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.uml.diagram.sequence.figures.MessageFigure#createTargetDecoration()
	 */
	@Override
	protected RotatableDecoration createSourceDecoration() {
		EllipseDecoration df = new EllipseDecoration();
		df.setPreferredSize(new Dimension(10, 10));
		df.setAlwaysFill(true);
		return df;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.uml.diagram.sequence.figures.MessageFigure#createTargetDecoration()
	 *
	 * @return
	 */
	@Override
	protected RotatableDecoration createTargetDecoration() {
		return null;
	}
}
