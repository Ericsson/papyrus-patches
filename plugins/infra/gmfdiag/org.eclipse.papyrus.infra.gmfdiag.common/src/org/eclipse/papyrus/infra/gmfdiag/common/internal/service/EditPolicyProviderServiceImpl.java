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
package org.eclipse.papyrus.infra.gmfdiag.common.internal.service;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.IEditPolicyProvider;
import org.eclipse.papyrus.infra.gmfdiag.common.service.EditPolicyProviderService;
import org.eclipse.papyrus.infra.gmfdiag.common.service.EditPolicyProviderTester;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.common.base.Predicates;

@Component
public class EditPolicyProviderServiceImpl implements EditPolicyProviderService {

	private Set<EditPolicyProviderTester> testers = new HashSet<>();

	@Override
	public boolean isEnabled(IEditPolicyProvider provider, EditPart editPart) {
		return testers.stream().map(tester -> tester.isEnabled(provider, editPart)).noneMatch(Predicates.equalTo(Boolean.FALSE));
	}

	@Reference
	public void registerTester(EditPolicyProviderTester tester) {
		testers.add(tester);
	}
}
