/*****************************************************************************
 * Copyright (c) 2012 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Camille Letavernier (CEA LIST) camille.letavernier@cea.fr - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.databinding.custom;

import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;

public class CustomBooleanStyleObservableList extends CustomStyleObservableList {

	public CustomBooleanStyleObservableList(View view, EditingDomain domain, String styleName) {
		super(view, styleName, domain, NotationPackage.eINSTANCE.getBooleanListValueStyle(), NotationPackage.eINSTANCE.getBooleanListValueStyle_BooleanListValue());
	}

}
