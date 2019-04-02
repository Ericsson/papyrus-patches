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
package org.eclipse.papyrus.uml.diagram.sequence.providers;

import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.core.service.IProviderChangeListener;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.CreateEditPoliciesOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.IEditPolicyProvider;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractMessageEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CDestructionOccurrenceSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomContinuationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomStateInvariantEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.GeneralOrderingEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.SequenceDiagramEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.AppliedStereotypeCommentCreationEditPolicyEx;
import org.eclipse.papyrus.uml.diagram.stereotype.edition.editpolicies.AppliedStereotypeCommentEditPolicy;


/**
 * Highest priority EditPolicyProvider, which can ensure the installed EditPolicies correctly.
 *
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class PostEditPolicyProvider implements IEditPolicyProvider {

	/**
	 * @see org.eclipse.gmf.runtime.common.core.service.IProvider#addProviderChangeListener(org.eclipse.gmf.runtime.common.core.service.IProviderChangeListener)
	 *
	 * @param listener
	 */

	@Override
	public void addProviderChangeListener(IProviderChangeListener listener) {

	}

	/**
	 * @see org.eclipse.gmf.runtime.common.core.service.IProvider#provides(org.eclipse.gmf.runtime.common.core.service.IOperation)
	 *
	 * @param operation
	 * @return
	 */

	@Override
	public boolean provides(IOperation operation) {
		CreateEditPoliciesOperation epOperation = (CreateEditPoliciesOperation) operation;
		if (!(epOperation.getEditPart() instanceof GraphicalEditPart) && !(epOperation.getEditPart() instanceof ConnectionEditPart)) {
			return false;
		}
		EditPart gep = epOperation.getEditPart();
		String diagramType = ((View) gep.getModel()).getDiagram().getType();
		if (SequenceDiagramEditPart.MODEL_ID.equals(diagramType)) {
			return true;
		}
		return false;
	}

	/**
	 * @see org.eclipse.gmf.runtime.common.core.service.IProvider#removeProviderChangeListener(org.eclipse.gmf.runtime.common.core.service.IProviderChangeListener)
	 *
	 * @param listener
	 */

	@Override
	public void removeProviderChangeListener(IProviderChangeListener listener) {

	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.IEditPolicyProvider#createEditPolicies(org.eclipse.gef.EditPart)
	 *
	 * @param editPart
	 */

	@Override
	public void createEditPolicies(EditPart editPart) {
		// Replace AppliedStereotypeCommentCreationEditPolicy to a custom one.
		if (editPart instanceof AbstractExecutionSpecificationEditPart || editPart instanceof CDestructionOccurrenceSpecificationEditPart
				|| editPart instanceof CustomStateInvariantEditPart || editPart instanceof AbstractMessageEditPart
				|| editPart instanceof GeneralOrderingEditPart || editPart instanceof CustomContinuationEditPart) {
			editPart.installEditPolicy(AppliedStereotypeCommentEditPolicy.APPLIED_STEREOTYPE_COMMENT, new AppliedStereotypeCommentCreationEditPolicyEx());
		}
	}

}
