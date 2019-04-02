/*****************************************************************************
 * Copyright (c) 2013 CEA LIST.
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
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpart;

import org.eclipse.gef.EditPart;


/**
 * A filter that detects whether an {@link EditPart} has model changes that
 * are in conflict with changes made by another editor. Conflicting edit parts
 * are not removed from the diagram when it is refreshed.
 */
public interface IConflictingEditPartFilter {

	/**
	 * The default filter, which never detects a conflict.
	 */
	IConflictingEditPartFilter DEFAULT = new IConflictingEditPartFilter() {

		@Override
		public boolean isConflicting(EditPart editPart) {
			return false;
		}
	};

	boolean isConflicting(EditPart editPart);
}
