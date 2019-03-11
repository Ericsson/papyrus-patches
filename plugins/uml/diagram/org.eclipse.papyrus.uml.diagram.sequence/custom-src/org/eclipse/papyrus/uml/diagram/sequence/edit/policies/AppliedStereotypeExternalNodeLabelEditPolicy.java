/*****************************************************************************
 * Copyright (c) 2013, 2016 CEA, Christian W. Damus, and others
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
 *   Soyatec - Initial API and implementation
 *   Christian W. Damus - bug 492482
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.IPapyrusEditPart;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.AppliedStereotypeLabelDisplayEditPolicy;
import org.eclipse.papyrus.uml.diagram.common.figure.node.IPapyrusNodeUMLElementFigure;
import org.eclipse.papyrus.uml.diagram.common.figure.node.IPapyrusUMLElementFigure;


/**
 * Display both stereotype and stereotype properties in one figure.
 *
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class AppliedStereotypeExternalNodeLabelEditPolicy extends AppliedStereotypeLabelDisplayEditPolicy {



	/**
	 * Constructor.
	 *
	 */
	public AppliedStereotypeExternalNodeLabelEditPolicy() {
		this(null);
	}

	/**
	 * Constructor.
	 *
	 */
	public AppliedStereotypeExternalNodeLabelEditPolicy(View hostView) {
		this.hostView = hostView;
	}

	@Override
	public void activate() {
		// retrieve the view and the element managed by the edit part
		View view = getView();
		if (view == null) {
			return;
		}
		super.activate();
		if (hostView == null) {
			// add a listener for TimeObservationEditPart
			// eContainer = getParent() , but here it's the ECore model
			EObject parent = view.eContainer();
			if (parent instanceof View) {
				hostView = (View) parent;
			}
		}
		subscribe(hostView);

		refreshDisplay();

	}

	@Override
	public void deactivate() {
		if (hostView != null) {
			unsubscribe(hostView);
		}
		super.deactivate();
	}


	/**
	 * Refreshes the stereotypes properties displayed above name of the element
	 * in this edit part.
	 */
	protected void refreshAppliedStereotypesPropertiesInBrace() {
		if (hostView != null) {

			if (getHost() instanceof IPapyrusEditPart) {
				IFigure figure = ((IPapyrusEditPart) getHost()).getPrimaryShape();

				if (figure instanceof IPapyrusNodeUMLElementFigure) {
					IPapyrusNodeUMLElementFigure UMLfigure = ((IPapyrusNodeUMLElementFigure) figure);



					// check if properties have to be displayed in braces.
					String todisplay = helper.getStereotypePropertiesInBrace(hostView);

					if (todisplay != null || !"".equals(todisplay)) {
						UMLfigure.setStereotypePropertiesInBrace(todisplay);
					} else {
						UMLfigure.setStereotypePropertiesInBrace(null);
					}

				}
			}
		}
	}

	/**
	 *
	 */
	private void refreshStereotypeLabel() {
		if (getHost() instanceof IPapyrusEditPart) {
			IFigure figure = ((IPapyrusEditPart) getHost()).getPrimaryShape();

			if (figure instanceof IPapyrusUMLElementFigure) {// calculate text
				// and icon to
				// display
				final String stereotypesToDisplay = helper.getStereotypeTextToDisplay(hostView);
				((IPapyrusUMLElementFigure) figure).setStereotypeDisplay(tag + (stereotypesToDisplay), null);
			}
		}
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.editpolicies.AppliedStereotypeLabelDisplayEditPolicy#refreshStereotypeDisplay()
	 *
	 */

	@Override
	protected void refreshStereotypeDisplay() {
		refreshStereotypeLabel();
		refreshAppliedStereotypesPropertiesInBrace();
	}
}
