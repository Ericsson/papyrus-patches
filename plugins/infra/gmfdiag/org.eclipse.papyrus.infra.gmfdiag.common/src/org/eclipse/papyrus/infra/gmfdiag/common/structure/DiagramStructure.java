/*****************************************************************************
 * Copyright (c) 2015 CEA LIST and others.
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

package org.eclipse.papyrus.infra.gmfdiag.common.structure;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.notation.View;

public abstract class DiagramStructure {

	public abstract String getVisualID(View view);

	public abstract String getModelID(View view);

	public abstract String getNodeVisualID(View containerView, EObject domainElement);

	public abstract boolean checkNodeVisualID(View containerView, EObject domainElement, String candidate);

	public abstract boolean isCompartmentVisualID(String visualID);

	public abstract boolean isSemanticLeafVisualID(String visualID);

}
