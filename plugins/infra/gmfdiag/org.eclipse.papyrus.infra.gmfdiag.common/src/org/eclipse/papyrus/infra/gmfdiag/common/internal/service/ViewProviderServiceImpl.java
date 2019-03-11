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

import org.eclipse.gmf.runtime.diagram.core.providers.IViewProvider;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.service.ViewProviderService;
import org.eclipse.papyrus.infra.gmfdiag.common.service.ViewProviderTester;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class ViewProviderServiceImpl implements ViewProviderService {

	private Set<ViewProviderTester> testers = new HashSet<>();

	@Override
	public boolean isEnabled(IViewProvider provider, View view) {
		return testers.stream().map(tester -> tester.isEnabled(provider, view)).reduce(true, Boolean::logicalAnd);
	}

	@Reference
	public void registerTester(ViewProviderTester tester) {
		testers.add(tester);
	}

}
