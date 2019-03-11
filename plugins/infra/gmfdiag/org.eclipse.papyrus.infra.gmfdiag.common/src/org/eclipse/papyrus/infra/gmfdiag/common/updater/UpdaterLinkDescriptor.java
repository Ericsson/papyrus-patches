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
 *   Mickaël ADAM (ALL4TEC) - mickael.adam@all4tec.net - Bug 517679
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.updater;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.EdgeWithNoSemanticElementRepresentationImpl;

/**
 * @since 2.0
 */
public class UpdaterLinkDescriptor extends UpdaterNodeDescriptor {

	private EObject mySource;

	private EObject myDestination;

	private IAdaptable mySemanticAdapter;

	private UpdaterLinkDescriptor(EObject source, EObject destination, EObject linkElement, String linkVID) {
		super(linkElement, linkVID);
		mySource = source;
		myDestination = destination;
	}

	public UpdaterLinkDescriptor(EObject source, EObject destination, final IElementType elementType, String linkVID) {
		this(source, destination, (EObject) null, elementType, linkVID);
	}

	public UpdaterLinkDescriptor(EObject source, EObject destination, EObject linkElement, final IElementType elementType, String linkVID) {
		this(source, destination, linkElement, linkVID);

		if (null != linkElement && !(linkElement instanceof EdgeWithNoSemanticElementRepresentationImpl)) {
			mySemanticAdapter = new EObjectAdapter(linkElement) {

				public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
					if (IElementType.class.equals(adapter)) {
						return elementType;
					}
					return super.getAdapter(adapter);
				}
			};
		} else {
			mySemanticAdapter = new IAdaptable() {

				public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
					if (IElementType.class.equals(adapter)) {
						return elementType;
					} else if (UpdaterLinkDescriptor.class.equals(adapter)) {
						return UpdaterLinkDescriptor.this;
					}
					return null;
				}
			};
		}
	}

	public EObject getSource() {
		return mySource;
	}

	public EObject getDestination() {
		return myDestination;
	}

	public IAdaptable getSemanticAdapter() {
		return mySemanticAdapter;
	}

}
