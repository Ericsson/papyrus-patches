/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick TESSIER (CEA LIST) patrick.tessier@cea.fr - Initial API and implementation
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Bug 531596
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CCombinedFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CLifeLineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionOperandEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;

/**
 * This allows to define util methods for bounds in edit parts.
 */
public class BoundForEditPart {

	/**
	 * This allows to get bounds for a node in parameter.
	 *
	 * @param node
	 *            The node which one to get bounds.
	 * @return The bounds.
	 */
	public static Bounds getBounds(final Node node) {
		final Bounds initialBounds = EcoreUtil.copy((Bounds) node.getLayoutConstraint());

		if (null != initialBounds) {
			if (initialBounds.getWidth() == -1) {
				initialBounds.setWidth(getDefaultWidthFromView(node));
			}
			if (initialBounds.getHeight() == -1) {
				initialBounds.setHeight(getDefaultHeightFromView(node));
			}
		}

		return initialBounds;
	}

	/**
	 * This allows to get height from view node.
	 *
	 * @param node
	 *            The node which one to get height.
	 * @return The height.
	 */
	public static int getHeightFromView(final Node node) {
		final Bounds bounds = BoundForEditPart.getBounds(node);
		if (bounds != null && bounds.getHeight() != -1) {
			return bounds.getHeight();
		} else {
			return getDefaultHeightFromView(node);
		}
	}

	/**
	 * This allows to get the default height of a view node.
	 *
	 * @param node
	 *            The node which one to get the default height.
	 * @return The default height.
	 */
	public static int getDefaultHeightFromView(final Node node) {
		if (node.getType().equals(CombinedFragmentEditPart.VISUAL_ID)) {
			return CCombinedFragmentEditPart.DEFAULT_HEIGHT;
		}
		if (node.getType().equals(InteractionOperandEditPart.VISUAL_ID)) {
			return 40;
		}
		if (node.getType().equals(LifelineEditPart.VISUAL_ID)) {
			return CLifeLineEditPart.DEFAUT_HEIGHT;
		}
		return 100;
	}

	/**
	 * This allows to get width from view node.
	 *
	 * @param node
	 *            The node which one to get width.
	 * @return The width.
	 */
	public static int getWidthFromView(final Node node) {
		final Bounds bounds = BoundForEditPart.getBounds(node);
		if (bounds != null && bounds.getWidth() != -1) {
			return bounds.getWidth();
		} else {
			return getDefaultWidthFromView(node);
		}
	}

	/**
	 * This allows to get the default width of a view node.
	 *
	 * @param node
	 *            The node which one to get the default width.
	 * @return The default width.
	 */
	public static int getDefaultWidthFromView(final Node node) {
		if (node.getType().equals(CombinedFragmentEditPart.VISUAL_ID)) {
			return CCombinedFragmentEditPart.DEFAULT_HEIGHT;
		}
		if (node.getType().equals(InteractionOperandEditPart.VISUAL_ID)) {
			return 100;
		}
		if (node.getType().equals(LifelineEditPart.VISUAL_ID)) {
			return CLifeLineEditPart.DEFAUT_WIDTH;
		}
		return 100;
	}

}
