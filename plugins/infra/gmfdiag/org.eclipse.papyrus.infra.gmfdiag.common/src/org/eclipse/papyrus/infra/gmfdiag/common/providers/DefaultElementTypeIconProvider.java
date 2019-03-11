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

package org.eclipse.papyrus.infra.gmfdiag.common.providers;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.ui.services.icon.GetIconOperation;
import org.eclipse.gmf.runtime.common.ui.services.icon.IIconProvider;
import org.eclipse.papyrus.infra.gmfdiag.tooling.runtime.providers.DiagramElementTypeImages;
import org.eclipse.swt.graphics.Image;

/**
 *
 * @author melaasar
 *
 */
public class DefaultElementTypeIconProvider extends AbstractProvider implements IIconProvider {

	private final DiagramElementTypes myElementTypes;

	public DefaultElementTypeIconProvider(DiagramElementTypes elementTypes) {
		myElementTypes = elementTypes;
	}

	public final DiagramElementTypes getElementTypes() {
		return myElementTypes;
	}

	/**
	 * @since 3.0
	 */
	public final DiagramElementTypeImages getElementTypeImages() {
		return myElementTypes.getElementTypeImages();
	}

	public Image getIcon(IAdaptable hint, int flags) {
		ENamedElement definingElement = getElementTypes().getDefiningNamedElement(hint);
		return definingElement == null ? null : getElementTypeImages().getImage(definingElement);
	}

	public boolean provides(IOperation operation) {
		if (operation instanceof GetIconOperation) {
			return ((GetIconOperation) operation).execute(this) != null;
		}
		return false;
	}
}
