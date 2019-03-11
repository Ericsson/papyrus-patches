/*****************************************************************************
 * Copyright (c) 2017 CEA LIST, ALL4TEC and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and implementation
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import org.eclipse.emf.ecore.EObject;

/**
 * Representation for stereotype property reference edge.
 * 
 * @author Mickael ADAM
 * @since 3.1
 */
public class StereotypePropertyReferenceEdgeRepresentation extends EdgeWithNoSemanticElementRepresentationImpl {

	/** The hint for stereotype property reference edge< */
	private static final String REFERENCE_LINK_HINT = "StereotypePropertyReferenceEdge";//$NON-NLS-1$

	/** The stereotype Qualified name of the source. */
	private String stereotypeQualifiedName;

	/** The feature set into the source's stereotype application. */
	private String featureToSet;

	/** The edge label. */
	private String edgeLabel;

	/**
	 * Constructor.
	 *
	 * @param source
	 *            The source of the edge.
	 * @param target
	 *            The target of the edge.
	 * @param stereotype
	 *            QualifiedName The stereotype qualified name of the source.
	 * @param featureToSet
	 *            The feature set into the source's stereotype application.
	 */
	public StereotypePropertyReferenceEdgeRepresentation(final EObject source, final EObject target, final String stereotypeQualifiedName, final String featureToSet, final String edgeLabel) {
		super(source, target, REFERENCE_LINK_HINT);
		this.stereotypeQualifiedName = stereotypeQualifiedName;
		this.featureToSet = featureToSet;
		this.edgeLabel = edgeLabel;
	}

	/**
	 * @return the stereotypeToSet
	 */
	public String getStereotypeQualifiedName() {
		return stereotypeQualifiedName;
	}


	/**
	 * @return the feature
	 */
	public String getFeatureToSet() {
		return featureToSet;
	}


	/**
	 * Calculate the hashcode in order to allows to have the same hashcode for 2 {@link StereotypePropertyReferenceEdgeRepresentation} with the same
	 * field values.
	 *
	 * @see java.lang.Object#hashCode()
	 *
	 * @return
	 * 		the hashcode
	 */
	@Override
	public int hashCode() {
		int result = 17;
		int constant = 37;
		result = result + ((this.target == null) ? 0 : this.target.hashCode());
		result = result * constant + ((this.source == null) ? 0 : this.source.hashCode());
		result = result * constant + ((this.stereotypeQualifiedName == null) ? 0 : this.stereotypeQualifiedName.hashCode());
		result = result * constant + ((this.featureToSet == null) ? 0 : this.featureToSet.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof StereotypePropertyReferenceEdgeRepresentation) {
			final StereotypePropertyReferenceEdgeRepresentation otherLink = (StereotypePropertyReferenceEdgeRepresentation) obj;
			return this.source == otherLink.getSource()
					&& this.target == otherLink.getTarget()
					&& this.stereotypeQualifiedName.equals(otherLink.getStereotypeQualifiedName())
					&& this.featureToSet.equals(otherLink.getFeatureToSet())
					&& this.semanticHint.equals(otherLink.getSemanticHint());
		}
		return false;
	}

	/**
	 * @return the edge label.
	 */
	public String getEdgeLabel() {
		return edgeLabel;
	}

}
