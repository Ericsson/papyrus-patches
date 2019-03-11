/*****************************************************************************
 * Copyright (c) 2010, 2017 CEA LIST, ALL4TEC and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA List - Initial API and implementation
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 522305
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.RotatableDecoration;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class MessageReply extends MessageFigure {

	/**
	 * Constructor.
	 *
	 */
	public MessageReply() {
		super();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.uml.diagram.sequence.figures.MessageFigure#createTargetDecoration()
	 */
	@Override
	protected RotatableDecoration createTargetDecoration() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.uml.diagram.sequence.figures.MessageFigure#createSourceDecoration()
	 */
	@Override
	protected RotatableDecoration createSourceDecoration() {
		return null;
	}


}
