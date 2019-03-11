/*****************************************************************************
 * Copyright (c) 2013 CEA
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

import org.eclipse.draw2d.IFigure;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.IPapyrusEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.AppliedStereotypeCommentCreationEditPolicyEx;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.CustomConnectionHandleEditPolicy;
import org.eclipse.papyrus.uml.diagram.stereotype.edition.editpolicies.AppliedStereotypeCommentEditPolicy;


/**
 * Support displaying Stereotype as a Comment Node for TimeConstraint.
 *
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomTimeConstraintLabelEditPart extends TimeConstraintLabelEditPart implements IPapyrusEditPart {

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CustomTimeConstraintLabelEditPart(View view) {
		super(view);
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpart.IPapyrusEditPart#getPrimaryShape()
	 *
	 * @return
	 */

	@Override
	public IFigure getPrimaryShape() {
		return getFigure();
	}

	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(EditPolicyRoles.CONNECTION_HANDLES_ROLE, new CustomConnectionHandleEditPolicy());
		// install a editpolicy to display stereotypes
		installEditPolicy(AppliedStereotypeCommentEditPolicy.APPLIED_STEREOTYPE_COMMENT, new AppliedStereotypeCommentCreationEditPolicyEx());
	}

}
