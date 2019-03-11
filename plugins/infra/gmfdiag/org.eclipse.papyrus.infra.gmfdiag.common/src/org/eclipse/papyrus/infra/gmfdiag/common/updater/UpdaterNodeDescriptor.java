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

package org.eclipse.papyrus.infra.gmfdiag.common.updater;

import org.eclipse.emf.ecore.EObject;

/**
 * @since 2.0
 */
public class UpdaterNodeDescriptor {

	private final EObject myModelElement;

	private final String myVisualID;

	public UpdaterNodeDescriptor(EObject modelElement, String visualID) {
		myModelElement = modelElement;
		myVisualID = visualID;
	}

	public EObject getModelElement() {
		return myModelElement;
	}

	public String getVisualID() {
		return myVisualID;
	}

}
