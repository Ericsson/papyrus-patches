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
 *  Boutheina Bannour (boutheina.bannour@cea.fr) - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.providers;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IPrimaryEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.CreateEditPoliciesOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.IEditPolicyProvider;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.emf.utils.ServiceUtilsForEObject;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Namespace;

/**
 * Edit Policy provider to manage custom creation command for Context links
 *
 */
public class CustomNamespaceEditPolicyProvider extends AbstractProvider implements IEditPolicyProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean provides(IOperation operation) {
		CreateEditPoliciesOperation epOperation = (CreateEditPoliciesOperation) operation;
		EditPart editPart = epOperation.getEditPart();
		if (editPart == null) {
			return false;
		}
		EObject eElement = EMFHelper.getEObject(editPart);
		if (!(eElement instanceof Namespace) && !(eElement instanceof Constraint)) {
			return false;
		}
		try {
			// check whether the element is a papyrus element.
			ServicesRegistry registry = ServiceUtilsForEObject.getInstance().getServiceRegistry(eElement);
			if (registry == null) {
				return false;
			}
		} catch (Exception ex) {
			return false;
		}
		// primary edit part is the toplevel (main) editpart
		return (editPart instanceof IPrimaryEditPart);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createEditPolicies(EditPart editPart) {
	}
}
