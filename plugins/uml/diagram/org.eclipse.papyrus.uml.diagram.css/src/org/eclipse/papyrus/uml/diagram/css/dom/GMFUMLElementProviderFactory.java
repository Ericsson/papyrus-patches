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
 *  Camille Letavernier (CEA LIST) camille.letavernier@cea.fr - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.css.dom;

import org.eclipse.papyrus.infra.gmfdiag.css.engine.ICSSElementProviderFactory;
import org.eclipse.papyrus.infra.gmfdiag.css.notation.CSSDiagram;
import org.eclipse.papyrus.infra.gmfdiag.css.provider.IPapyrusElementProvider;
import org.eclipse.uml2.uml.Element;


/**
 * IElementProvider Factory for Diagrams related to UML Elements
 *
 * @author Camille Letavernier
 *
 */
public class GMFUMLElementProviderFactory implements ICSSElementProviderFactory {

	@Override
	public boolean isProviderFor(CSSDiagram diagram) {
		return diagram.getElement() instanceof Element; // Provider for UML Elements
	}

	@Override
	public IPapyrusElementProvider createProvider(CSSDiagram diagram) {
		return new GMFUMLElementProvider();
	}

}
