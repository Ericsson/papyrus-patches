/*****************************************************************************
 * Copyright (c) 2013 CEA LIST.
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
 *  Boutheina Bannour (CEA LIST) boutheina.bannour@cea.fr - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.AppliedStereotypeLinkLabelDisplayEditPolicy;
import org.eclipse.papyrus.uml.diagram.common.figure.node.IPapyrusUMLElementFigure;

/**
 * Edit Policy for Applied Stereotype Label for {@link ContextLink}.
 */
public class CustomAppliedStereotypeContextLinkLabelDisplayEditPolicy extends AppliedStereotypeLinkLabelDisplayEditPolicy {

	/**
	 * Creates the EditPolicy, with the correct tag.
	 */
	public CustomAppliedStereotypeContextLinkLabelDisplayEditPolicy() {
		super("context"); //$NON-NLS-1$
	}

	@Override
	public void activate() {
		// retrieve the view
		View view = getView();
		if (view == null) {
			return;
		}
		// call the refresh overridden in this class
		refreshDisplay();
	}

	/**
	 * Refreshes the tag display
	 */
	@Override
	protected void refreshStereotypeDisplay() {
		IFigure figure = ((GraphicalEditPart) getHost()).getFigure();
		// the tag displayed here is <code>&laquo context &raquo</code> see the class constructor
		if (figure instanceof IPapyrusUMLElementFigure) {
			((IPapyrusUMLElementFigure) figure).setStereotypeDisplay(tag, null);
		}
	}
}
