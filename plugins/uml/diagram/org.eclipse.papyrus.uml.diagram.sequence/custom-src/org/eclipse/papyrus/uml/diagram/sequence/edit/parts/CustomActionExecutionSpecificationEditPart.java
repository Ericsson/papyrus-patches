/*****************************************************************************
 * Copyright (c) 2010 CEA
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
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.ExecutionGraphicalNodeEditPolicy;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomActionExecutionSpecificationEditPart extends CCombinedCompartmentEditPart {

	public static int DEFAULT_HEIGHT=100;
	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CustomActionExecutionSpecificationEditPart(View view) {
		super(view);
	}

	/**
	 * @Override
	 */
	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		// Fixed bug about reconnect message when the ends of execution were MessageOccurrenceSpecification.
		removeEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE);
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ExecutionGraphicalNodeEditPolicy());
	}



	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeEditPart#refreshBounds()
	 *
	 */
	@Override
	protected void refreshBounds() {

		super.refreshBounds();
	}
	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart#refreshVisuals()
	 *
	 */
	@Override
	protected void refreshVisuals() {

		super.refreshVisuals();
	}
	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CCombinedCompartmentEditPart#handleNotificationEvent(org.eclipse.emf.common.notify.Notification)
	 *
	 * @param event
	 */
	@Override
	protected void handleNotificationEvent(Notification event) {

		super.handleNotificationEvent(event);
	}
}
