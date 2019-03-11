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

package org.eclipse.papyrus.infra.gmfdiag.common.providers;

import java.net.URL;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.ui.services.icon.GetIconOperation;
import org.eclipse.gmf.runtime.common.ui.services.icon.IIconProvider;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.ISpecializationType;
import org.eclipse.papyrus.infra.core.utils.AdapterUtils;
import org.eclipse.swt.graphics.Image;

/**
 * A generic icon provider that provides the icons specified by {@link IElementType}s. This is intended to override the
 * GMF-generated providers, which only provide the base metamodel icons from the {@literal EMF.Edit} providers.
 */
public class ElementTypeIconProvider extends AbstractProvider implements IIconProvider {

	public ElementTypeIconProvider() {
		super();
	}

	@Override
	public boolean provides(IOperation operation) {
		boolean result = operation instanceof GetIconOperation;
		if (result) {
			GetIconOperation getIcon = (GetIconOperation) operation;
			result = getIcon.getHint().getAdapter(IElementType.class) != null;
		}
		return result;
	}

	@Override
	public Image getIcon(IAdaptable hint, int flags) {
		Image result = null;
		IElementType elementType = AdapterUtils.adapt(hint, IElementType.class, null);

		if (elementType != null) {
			URL url = elementType.getIconURL();
			result = (url == null) ? null : getIcon(url);
		}

		return result;
	}

	protected Image getIcon(URL iconURL) {
		return ExtendedImageRegistry.INSTANCE.getImage(iconURL);
	}

	protected URL getIconURL(IElementType elementType) {
		URL result = elementType.getIconURL();

		if ((result == null) && (elementType instanceof ISpecializationType)) {
			ISpecializationType subtype = (ISpecializationType) elementType;
			IElementType[] supertypes = subtype.getSpecializedTypes();
			if (supertypes != null) {
				for (int i = 0; (result == null) && (i < supertypes.length); i++) {
					result = getIconURL(supertypes[i]);
				}
			}
		}

		return result;
	}
}
