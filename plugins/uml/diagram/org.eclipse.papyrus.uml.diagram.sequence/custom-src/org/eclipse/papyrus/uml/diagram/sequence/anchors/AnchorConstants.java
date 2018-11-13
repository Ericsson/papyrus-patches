/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.anchors;

import org.eclipse.draw2d.IFigure;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Message;

/**
 * Constants class for Anchors
 */
public final class AnchorConstants {
	private AnchorConstants() {
		// Constants class; no instance
	}

	/**
	 * <p>
	 * The Anchor Terminal for an Anchor at the start/beginning of an element.
	 * </p>
	 *
	 * <p>
	 * This anchor represents a single point (Which depends on the anchorage {@link IFigure}), without any parameter.
	 * </p>
	 *
	 * <p>
	 * This anchor may represent, for example, the start of an {@link ExecutionSpecification}, or the source Send Event of a {@link Message}
	 * </p>
	 */
	public static final String START_TERMINAL = "start";

	/**
	 * <p>
	 * The Anchor Terminal for an Anchor at the finish/end of an element.
	 * </p>
	 *
	 * <p>
	 * This anchor represents a single point (Which depends on the anchorage {@link IFigure}), without any parameter.
	 * </p>
	 *
	 * <p>
	 * This anchor may represent, for example, the finish of an {@link ExecutionSpecification}, or the target Receive Event of a {@link Message}
	 * </p>
	 */
	public static final String END_TERMINAL = "end";

	/**
	 * <p>
	 * The Anchor Terminal for an Anchor at the center of an element.
	 * </p>
	 *
	 * <p>
	 * This anchor represents a single point (Which depends on the anchorage {@link IFigure}), without any parameter.
	 * </p>
	 *
	 * <p>
	 * This anchor typically represents the center of the X in a {@link DestructionOccurrenceSpecification}
	 * </p>
	 */
	public static final String CENTER_TERMINAL = "center";
}
