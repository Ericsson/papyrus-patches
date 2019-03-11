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
 *  Vincent Lorenzo (CEA LIST) vincent.lorenzo@cea.fr - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import org.eclipse.papyrus.infra.gmfdiag.common.structure.DiagramStructure;
import org.eclipse.papyrus.infra.gmfdiag.common.updater.DiagramUpdater;

/**
 *
 * The EditPolicy used to get informations about the diagram
 *
 */
public interface IDiagramInformationProviderEditPolicy {


	/**
	 *
	 * @return
	 *         the diagram updater
	 */
	public DiagramUpdater getDiagramUpdater();

	/**
	 *
	 * @return
	 *         the diagram structure
	 */
	public DiagramStructure getDiagramStructure();
}
