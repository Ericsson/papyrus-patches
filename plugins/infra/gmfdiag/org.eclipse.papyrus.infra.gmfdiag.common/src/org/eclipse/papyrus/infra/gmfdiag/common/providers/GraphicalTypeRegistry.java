/*****************************************************************************
 * Copyright (c) 2011 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *		CEA LIST - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.providers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.IHintedType;

/**
 * <pre>
 * This class provides graphical type id (used as View type) for
 * domain element according to their actual or expected graphical
 * container type.
 * </pre>
 */
public class GraphicalTypeRegistry implements IGraphicalTypeRegistry {

	/** A Set containing all known node graphical types */
	protected Set<String> knownNodes = new HashSet<String>();

	/** A Set containing all known edge graphical types */
	protected Set<String> knownEdges = new HashSet<String>();

	/** Default constructor */
	public GraphicalTypeRegistry() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEdgeGraphicalType(EObject domainElement) {
		String graphicalType = UNDEFINED_TYPE;
		if (domainElement == null) {
			return UNDEFINED_TYPE;
		}
		return graphicalType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEdgeGraphicalType(IElementType elementType) {
		if (elementType instanceof IHintedType) {
			String semanticHint = ((IHintedType) elementType).getSemanticHint();
			return getEdgeGraphicalType(semanticHint);
		}
		return UNDEFINED_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEdgeGraphicalType(String proposedType) {
		if (isKnownEdgeType(proposedType)) {
			return proposedType;
		}
		return UNDEFINED_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getNodeGraphicalType(EObject domainElement, String containerType) {
		String graphicalType = UNDEFINED_TYPE;
		if ((containerType == null) || (domainElement == null)) {
			return UNDEFINED_TYPE;
		}

		return graphicalType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getNodeGraphicalType(IElementType elementType, String containerType) {
		if (elementType instanceof IHintedType) {
			String semanticHint = ((IHintedType) elementType).getSemanticHint();
			return getNodeGraphicalType(semanticHint, containerType);
		}

		return UNDEFINED_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getNodeGraphicalType(String proposedType, String containerType) {
		if (isKnownNodeType(proposedType)) {
			return proposedType;
		}

		return UNDEFINED_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isKnownEdgeType(String type) {
		return knownEdges.contains(type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isKnownNodeType(String type) {
		return knownNodes.contains(type);
	}
}
