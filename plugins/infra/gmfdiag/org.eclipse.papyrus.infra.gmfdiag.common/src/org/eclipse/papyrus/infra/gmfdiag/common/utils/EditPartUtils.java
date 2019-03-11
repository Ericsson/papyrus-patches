/*****************************************************************************
 * Copyright (c) 2012 CEA LIST.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.notation.View;

/**
 * @since 3.0
 */
public class EditPartUtils {

	
	public static EditPart findFirstChildEditPartWithId(final EditPart editPart, final String visualId) {
		final List<? extends EditPart> result = findChildEditPartsWithId(editPart, visualId);
		if (result.isEmpty()) {
			return null;
		}
		return result.get(0);
	}

	public static List<? extends EditPart> findChildEditPartsWithId(final EditPart editPart, final String visualId) {
		final List<EditPart> editParts = new ArrayList<EditPart>();
		internalFindChildEditPartsWithId(editPart, visualId, editParts);
		return editParts;
	}

	private static void internalFindChildEditPartsWithId(final EditPart editPart, final String visualId, final List<EditPart> result) {
		final Object model = editPart.getModel();
		if (model instanceof View) {
			final View view = (View) model;
			if( view.getType()!=null) {
				if (view.getType().equals(visualId)) {
					result.add(editPart);
				}
			}
		}
		@SuppressWarnings("unchecked")
		final List<EditPart> children = editPart.getChildren();
		for (final EditPart child : children) {
			internalFindChildEditPartsWithId(child, visualId, result);
		}
	}

	public static EditPart findParentEditPartWithId(final EditPart editPart, final String visualId) {
		EditPart parent = editPart;
		while (parent != null) {
			final Object model = parent.getModel();
			if (model instanceof View) {
				final View parentView = (View) model;
				if (parentView.getType()!=null) {
					if (parentView.getType().equals(visualId)) {
						return parent;
					}
				}
				parent = parent.getParent();
			} else {
				break;
			}
		}
		return null;
	}

	/**
	 * Find the EditPart whose Figure is closest to the given y-coordinate.
	 *
	 * @param ordinate
	 *            the y-coordinate
	 * @param editParts
	 *            the EditParts among which to choose
	 * @return the EditPart closest to the given vertical coordinate
	 */
	public static GraphicalEditPart findEditPartClosestToOrdinate(final int ordinate, final List<? extends GraphicalEditPart> editParts) {
		if (editParts.isEmpty()) {
			return null;
		}

		final TreeMap<Integer, GraphicalEditPart> distanceMap = new TreeMap<Integer, GraphicalEditPart>();
		for (final GraphicalEditPart editPart : editParts) {
			final IFigure figure = editPart.getFigure();
			final Rectangle bounds = new Rectangle(figure.getBounds());
			figure.getParent().translateToAbsolute(bounds);
			final int posY = bounds.y + bounds.height / 2;
			final int distance = Math.abs(posY - ordinate);
			distanceMap.put(Integer.valueOf(distance), editPart);
		}
		return distanceMap.values().iterator().next();
	}


	/** Reveals the given EditPart in its viewer */
	public static void revealEditPart(final EditPart editPart) {
		if (editPart != null && editPart.getViewer() != null) {
			editPart.getViewer().reveal(editPart);
		}
	}
}
