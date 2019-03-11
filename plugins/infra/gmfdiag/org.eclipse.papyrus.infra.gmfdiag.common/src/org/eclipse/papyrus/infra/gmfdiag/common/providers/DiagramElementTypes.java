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
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.papyrus.infra.gmfdiag.tooling.runtime.providers.DiagramElementTypeImages;

/**
 * @author melaasar
 *
 */
public abstract class DiagramElementTypes {

	private DiagramElementTypeImages myImages;

	public DiagramElementTypes(AdapterFactory adapterFactory) {
		this(new DiagramElementTypeImages(adapterFactory));
	}

	/**
	 * @since 3.0
	 */
	public DiagramElementTypes(DiagramElementTypeImages images) {
		myImages = images;
	}

	public abstract IElementType getElementTypeForVisualId(String visualID);

	public abstract boolean isKnownElementType(IElementType elementType);

	public abstract ENamedElement getDefiningNamedElement(IAdaptable elementTypeAdapter);

	/**
	 * @since 3.0
	 */
	public DiagramElementTypeImages getElementTypeImages() {
		return myImages;
	}

}