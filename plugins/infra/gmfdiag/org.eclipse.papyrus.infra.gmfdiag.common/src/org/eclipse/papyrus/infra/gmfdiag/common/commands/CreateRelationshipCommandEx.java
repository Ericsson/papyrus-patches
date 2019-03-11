/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.commands;

import org.eclipse.gmf.runtime.emf.type.core.commands.CreateRelationshipCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.ConfigureRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;

/**
 * Specific {@link CreateRelationshipCommand} that does not set the source and target in the configure request if it is already set.
 * <p>
 * This allows to override the source and/or the target in the advices. Basic GMF implementation reset the {@link CreateRelationshipRequest#SOURCE} & {@link CreateRelationshipRequest#TARGET} when the configureRequest is created, with original values.
 * </p>
 */
public class CreateRelationshipCommandEx extends CreateRelationshipCommand {

	/**
	 * Constructor.
	 *
	 * @param request
	 *            the element creation request
	 */
	public CreateRelationshipCommandEx(CreateRelationshipRequest request) {
		super(request);
	}

	@Override
	protected ConfigureRequest createConfigureRequest() {
		// let the super() create the requests as usual
		ConfigureRequest result = super.createConfigureRequest();
		
		// retrieve those from create request
		Object requestSource = getRequest().getParameter(CreateRelationshipRequest.SOURCE); 
		Object requestTarget = getRequest().getParameter(CreateRelationshipRequest.TARGET);
		
		// overrides parameters only if necessary, e.g. set and different
		if (requestSource != null && !requestSource.equals(result.getParameter(CreateRelationshipRequest.SOURCE))) {
			result.setParameter(CreateRelationshipRequest.SOURCE, requestSource);
		}
		if (requestTarget != null && !requestTarget.equals(result.getParameter(CreateRelationshipRequest.TARGET))) {
			result.setParameter(CreateRelationshipRequest.TARGET, requestTarget);
		}
		return result;
	}

}
