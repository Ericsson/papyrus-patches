/*****************************************************************************
 * Copyright (c) 2009 CEA
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
 *   CEA LIST - Initial API and implementation
 *   Alex Paperno - bug 395248
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.util;

import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionOperandEditPart;

public class CombinedFragmentMoveHelper {

	protected final static int CF_PADDING = 10;

	/**
	 * Calculate combined rect
	 *
	 */
	public static Rectangle calcCombinedRect(ChangeBoundsRequest request) {
		Rectangle rectangleDroppedCombined = new Rectangle();

		List<?> editParts = request.getEditParts();

		if (editParts != null) {
			for (Object part : editParts) {
				CombinedFragmentEditPart combinedFragmentEP = (CombinedFragmentEditPart) part;
				Rectangle rectangleDropped = combinedFragmentEP.getFigure().getBounds().getCopy();
				combinedFragmentEP.getFigure().translateToAbsolute(rectangleDropped);

				if (!rectangleDroppedCombined.isEmpty()) {
					rectangleDroppedCombined = new Rectangle(rectangleDropped.getUnion(rectangleDroppedCombined));
				} else {
					rectangleDroppedCombined = rectangleDropped;
				}
			}
		}

		rectangleDroppedCombined.translate(request.getMoveDelta());
		rectangleDroppedCombined.expand(CF_PADDING, CF_PADDING);
		return rectangleDroppedCombined;
	}

	/**
	 * Shift inner CFs so that they don't change absolute coords
	 *
	 */
	@Deprecated
	public static Command getShiftEnclosedCFsCommand(InteractionOperandEditPart editPart, Point offset) {
		return null;
	}

}
