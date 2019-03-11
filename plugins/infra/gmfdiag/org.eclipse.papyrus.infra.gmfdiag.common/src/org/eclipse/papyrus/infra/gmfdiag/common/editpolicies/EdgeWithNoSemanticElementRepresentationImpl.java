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
 *
 *		CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 *
 * @author Vincent Lorenzo
 *         An eobject class used to represent a link with no semantic element
 */
public class EdgeWithNoSemanticElementRepresentationImpl extends EObjectImpl {

	/**
	 * the semantic hint for the represented element
	 */
	protected String semanticHint;

	/**
	 * the source of the link
	 */
	protected final EObject source;

	/**
	 * the target of the link
	 */
	protected final EObject target;

	/**
	 *
	 * Constructor.
	 *
	 * @param source
	 *            the source of the link
	 * @param target
	 *            the target of the link
	 * @param semanticHint
	 *            the semantic hint for the represented element
	 */
	public EdgeWithNoSemanticElementRepresentationImpl(final EObject source, final EObject target, final String semanticHint) {
		this.source = source;
		this.target = target;
		this.semanticHint = semanticHint;
	}

	/**
	 *
	 * @return
	 * 		the source of the represented link
	 */
	public EObject getSource() {
		return source;
	}

	/**
	 *
	 * @return
	 * 		the target of the represented link
	 */
	public EObject getTarget() {
		return target;
	}

	/**
	 *
	 * @return
	 * 		a view for this link
	 */
	public String getSemanticHint() {
		return semanticHint;
	}

	/**
	 * Calculate the hashcode in order to allows to have the same hashcode for 2 {@link EdgeWithNoSemanticElementRepresentationImpl} with the same
	 * field values.
	 *
	 * @see java.lang.Object#hashCode()
	 *
	 * @return
	 * 		the hashcode
	 */
	@Override
	public int hashCode() {
		int result = 1;
		result = result + ((this.target == null) ? 0 : this.target.hashCode());
		result = result + 7 * ((this.source == null) ? 0 : this.source.hashCode());
		result = result + 11 * ((this.semanticHint == null) ? 0 : this.semanticHint.hashCode());
		return result;
	}

	/**
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 *
	 * @param obj
	 *            an object
	 * @return
	 * 		<code>true</code> if this object is the same than the other one
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof EdgeWithNoSemanticElementRepresentationImpl) {
			final EdgeWithNoSemanticElementRepresentationImpl otherLink = (EdgeWithNoSemanticElementRepresentationImpl) obj;
			return this.source == otherLink.getSource() && this.target == otherLink.getTarget() && this.semanticHint.equals(otherLink.getSemanticHint());
		}
		return false;
	}
}