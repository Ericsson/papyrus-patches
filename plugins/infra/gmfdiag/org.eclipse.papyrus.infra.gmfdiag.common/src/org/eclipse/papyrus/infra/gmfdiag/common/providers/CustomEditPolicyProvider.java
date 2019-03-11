/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Francois Le Fevre (CEA LIST) francois.le-fevre@cea.fr - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.providers;

import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.core.service.IProviderChangeListener;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.CreateEditPoliciesOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.IEditPolicyProvider;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.HighlightEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.service.ProviderServiceUtil;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.ServiceUtilsForEditPart;

/**
 * this is an editpolicy provider in charge to install a policy to navigate between diagrams and elements
 *
 */
public class CustomEditPolicyProvider implements IEditPolicyProvider {

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void addProviderChangeListener(IProviderChangeListener listener) {
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void createEditPolicies(final EditPart editPart) {
		installEditPolicy(editPart, new HighlightEditPolicy(), HighlightEditPolicy.HIGHLIGHT_ROLE);
	}

	/**
	 * Safely install a EditPolicy, if the editpolicy with given role is existed in editpart, ignore it.
	 *
	 * @param editPart
	 * @param editPolicy
	 * @param role
	 */
	private void installEditPolicy(EditPart editPart, EditPolicy editPolicy, String role) {
		if (editPart == null || editPolicy == null) {
			return;
		}
		EditPolicy myEditPolicy = editPart.getEditPolicy(role);
		if (myEditPolicy == null) {
			editPart.installEditPolicy(role, editPolicy);
		}
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public boolean provides(IOperation operation) {
		CreateEditPoliciesOperation epOperation = (CreateEditPoliciesOperation) operation;
		if (!ProviderServiceUtil.isEnabled(this, epOperation.getEditPart())) {
			return false;
		}
		try {
			EditPart editPart = epOperation.getEditPart();
			if (!(editPart instanceof GraphicalEditPart)) {
				return false;
			}

			EditingDomain domain = EMFHelper.resolveEditingDomain(editPart);
			if (domain == null) {
				return false;
			}

			ServicesRegistry registry = ServiceUtilsForEditPart.getInstance().getServiceRegistry(epOperation.getEditPart());
			return registry != null;
		} catch (ServiceException e) {
			return false;
		}

	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void removeProviderChangeListener(IProviderChangeListener listener) {
	}
}
