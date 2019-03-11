/*****************************************************************************
 * Copyright (c) 2010 Atos Origin.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 *  Atos Origin - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.infra.ui.extension.commands.PerspectiveContextDependence;

/**
 * SequenceDiagramCreationCondition class allows to check if a Sequence diagram can be added to the
 * selected element.
 */
public class SequenceDiagramCreationCondition extends PerspectiveContextDependence {

	/**
	 * @return whether the diagram can be created.
	 */
	@Override
	public boolean create(EObject selectedElement) {
		return false;
	}
}
