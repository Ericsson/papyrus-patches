/*****************************************************************************
 * Copyright (c) 2019 CEA LIST and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.providers;

import org.eclipse.gmf.runtime.emf.type.core.ElementTypeRegistry;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;

/**
 * @author ETXACAM
 *
 */
public class SequenceDiagramElementTypes {
	public static final IElementType Message_SynchActionEdge = getElementTypeByUniqueId(
			 "org.eclipse.papyrus.uml.diagram.sequence.umldi.Message_SynchActionEdge"); //$NON-NLS-1$

	public static final IElementType Message_SynchBehaviorEdge = getElementTypeByUniqueId(
			 "org.eclipse.papyrus.uml.diagram.sequence.umldi.Message_SynchBehaviorEdge"); //$NON-NLS-1$

	private static IElementType getElementTypeByUniqueId(String id) {
		return ElementTypeRegistry.getInstance().getType(id);
	}
}
