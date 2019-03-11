/*****************************************************************************
 * Copyright (c) 2018 EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - Initial API and implementation (Bug 533701)
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.service;

import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.core.providers.IViewProvider;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.IEditPolicyProvider;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.emf.utils.ServiceUtilsForEObject;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.ServiceUtilsForEditPart;

/**
 * <p>
 * Utility methods to use the {@link ViewProviderService} and {@link EditPolicyProviderService}
 * </p>
 *
 * @since 3.100
 */
public class ProviderServiceUtil {

	/**
	 * <p>
	 * Tests if the given edit part is a Papyrus Edit Part, by testing if a Papyrus {@link ServicesRegistry}
	 * is present.
	 * </p>
	 *
	 * @param editPart
	 *                     The edit part to test
	 * @return
	 * 		<code>true</code> if this edit part is part of a Papyrus environment (Using a Papyrus {@link ServicesRegistry}), <code>false</code> otherwise
	 */
	public static boolean isPapyrusPart(EditPart editPart) {
		try {
			return ServiceUtilsForEditPart.getInstance().getServiceRegistry(editPart) != null;
		} catch (Exception ex) {
			// Ignore & return
			return false;
		}
	}

	/**
	 * <p>
	 * Tests if the given View is a Papyrus View, by testing if a Papyrus {@link ServicesRegistry}
	 * is present.
	 * </p>
	 *
	 * @param view
	 *                 The view to test
	 * @return
	 * 		<code>true</code> if this view is part of a Papyrus environment (Using a Papyrus {@link ServicesRegistry}), <code>false</code> otherwise
	 */
	public static boolean isPapyrusView(View view) {
		try {
			return ServiceUtilsForEObject.getInstance().getServiceRegistry(view) != null;
		} catch (Exception ex) {
			// Ignore & return
			return false;
		}
	}

	/**
	 * <p>
	 * Helper method to use the {@link ViewProviderService}.
	 * </p>
	 * <p>
	 * This methods verifies that the edit part is a Papyrus Edit Part (Via {@link #isPapyrusPart(EditPart)}),
	 * and then calls {@link ViewProviderService#isEnabled(IViewProvider, EditPart)}
	 * </p>
	 *
	 * @param provider
	 * @param editPart
	 * @return
	 *
	 * @see ViewProviderService#isEnabled(IViewProvider, EditPart)
	 */
	public static boolean isEnabled(IViewProvider provider, View view) {
		if (isPapyrusView(view)) {
			try {
				ViewProviderService service = ServiceUtilsForEObject.getInstance().getService(ViewProviderService.class, view);
				return service.isEnabled(provider, view);
			} catch (Exception ex) {
				// Ignore & return
				return false;
			}
		}

		return false;
	}

	/**
	 * <p>
	 * Helper method to use the {@link EditPolicyProviderService}.
	 * </p>
	 * <p>
	 * This methods verifies that the edit part is a Papyrus Edit Part (Via {@link #isPapyrusPart(EditPart)}),
	 * and then calls {@link EditPolicyProviderService#isEnabled(IEditPolicyProvider, EditPart)}
	 * </p>
	 *
	 * @param provider
	 * @param editPart
	 * @return
	 *
	 * @see {@link EditPolicyProviderService#isEnabled(IEditPolicyProvider, EditPart)}
	 */
	public static boolean isEnabled(IEditPolicyProvider provider, EditPart editPart) {
		if (isPapyrusPart(editPart)) {
			try {
				EditPolicyProviderService service = ServiceUtilsForEditPart.getInstance().getService(EditPolicyProviderService.class, editPart);
				return service.isEnabled(provider, editPart);
			} catch (Exception ex) {
				// Ignore & return
				return false;
			}
		}

		return false;
	}

}
