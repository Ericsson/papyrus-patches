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

import org.eclipse.draw2d.ArrowLocator;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.RotatableDecoration;
import org.eclipse.draw2d.geometry.Dimension;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class MessageLost extends MessageFigure {

	/**
	 * Constructor.
	 */
	public MessageLost() {
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.figures.MessageFigure#createTargetDecoration()
	 *
	 * @return
	 */
	@Override
	protected RotatableDecoration createTargetDecoration() {
		EllipseDecoration df = new EllipseDecoration();
		df.setAlwaysFill(true);
		df.setPreferredSize(new Dimension(10, 10));
		add(df, new ArrowLocator(this, ConnectionLocator.TARGET)); // child figure
		return null;
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.figures.MessageFigure#createSourceDecoration()
	 *
	 * @return
	 */
	@Override
	protected RotatableDecoration createSourceDecoration() {
		return null;
	}
}
