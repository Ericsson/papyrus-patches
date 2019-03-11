/*****************************************************************************
 * Copyright (c) 2014 CEA LIST.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Benoit Maggi (CEA LIST) benoit.maggi@cea.fr - Initial API and implementation
 *  Mickaël ADAM (ALL4TEC) - mickael.adam@all4tec.net - Bug 517679
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.notation.BasicCompartment;
import org.eclipse.gmf.runtime.notation.Connector;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.EdgeWithNoSemanticElementRepresentationImpl;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.StereotypePropertyReferenceEdgeRepresentation;


/**
 * The Class Domain2Notation used for mapping between EObject and views
 */
public class Domain2Notation extends HashMap<EObject, Set<View>> {


	/**
	 * Maps view.
	 *
	 * @param view
	 *            the view from which are mapped all subviews
	 */
	public void mapModel(View view) {
		if ((view instanceof Connector || view instanceof Shape) && !(view instanceof BasicCompartment)) {
			putView(view);
		}
		@SuppressWarnings("unchecked")
		List<View> children = view.getChildren();
		for (View child : children) {
			mapModel(child);
		}
		@SuppressWarnings("unchecked")
		List<View> sourceEdges = view.getSourceEdges();
		for (View edge : sourceEdges) {
			mapModel(edge);
		}

	}


	/**
	 * Put view.
	 *
	 * @param view
	 *            the view
	 */
	public void putView(View view) {
		EObject element = view.getElement();
		if (element == null && view instanceof Connector) {
			final EObject source = ((Connector) view).getSource().getElement();
			final EObject target = ((Connector) view).getTarget().getElement();
			if (view.getType().equals("StereotypePropertyReferenceEdge")) {//$NON-NLS-1$
				EAnnotation eAnnotation = view.getEAnnotation("StereotypePropertyReferenceEdge");//$NON-NLS-1$
				if (null != eAnnotation) {
					String stereotypeQualifyName = eAnnotation.getDetails().get("stereotypeQualifiedName");//$NON-NLS-1$
					String featureToSet = eAnnotation.getDetails().get("featureToSet");//$NON-NLS-1$
					String linkLabel = eAnnotation.getDetails().get("edgeLabel");//$NON-NLS-1$
					element = new StereotypePropertyReferenceEdgeRepresentation(source, target, stereotypeQualifyName, featureToSet, linkLabel);
				}
			} else {
				element = new EdgeWithNoSemanticElementRepresentationImpl(source, target, view.getType());
			}
		} else if (element == null) {
			return;
		}
		Set<View> set = this.get(element);
		if (set != null) {
			set.add(view);
		} else {
			Set<View> hashSet = new HashSet<View>();
			hashSet.add(view);
			put(element, hashSet);
		}
	}

	/**
	 * Put view.
	 *
	 * @param element
	 *            the element
	 * @param view
	 *            the view
	 */
	public void putView(EObject element, View view) {
		Set<View> set = this.get(element);
		if (set != null) {
			set.add(view);
		} else {
			Set<View> hashSet = new HashSet<View>();
			hashSet.add(view);
			put(element, hashSet);
		}
	}

	/**
	 * Gets the first view, prefer the get(Object key) method to get all the views
	 *
	 * @param key
	 *            the key
	 * @return the first view
	 */
	@Deprecated
	public View getFirstView(Object key) {
		Set<View> set = get(key);
		if (set != null) {
			return set.iterator().next();
		}
		return null;
	}
}
