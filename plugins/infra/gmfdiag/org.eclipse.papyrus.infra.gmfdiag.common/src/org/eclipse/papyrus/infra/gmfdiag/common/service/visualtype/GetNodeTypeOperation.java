/*****************************************************************************
 * Copyright (c) 2015 Christian W. Damus and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.service.visualtype;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.core.service.IProvider;
import org.eclipse.gmf.runtime.notation.View;

/**
 * The {@link IVisualTypeProvider#getNodeType(View, EObject)} operation.
 */
public class GetNodeTypeOperation implements IOperation, IVisualTypeOperation {

	private View parentView;
	private EObject element;

	public GetNodeTypeOperation(View parentView, EObject element) {
		super();

		this.parentView = parentView;
		this.element = element;
	}

	@Override
	public Object execute(IProvider provider) {
		String result = null;

		if (provider instanceof IVisualTypeProvider) {
			result = ((IVisualTypeProvider) provider).getNodeType(parentView, element);
		}

		return result;
	}

	@Override
	public String getDiagramType() {
		return parentView.getDiagram().getType();
	}

}
