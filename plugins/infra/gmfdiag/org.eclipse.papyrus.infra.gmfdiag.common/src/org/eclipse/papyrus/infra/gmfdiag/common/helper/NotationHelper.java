/*****************************************************************************
 * Copyright (c) 2012, 2014 CEA LIST and others.
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
 *  Christian W. Damus (CEA) - bug 386118
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.helper;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;

/**
 * A Helper class related to the GMF Notation metamodel.
 *
 * @author Camille Letavernier
 */
public class NotationHelper {

	/**
	 * Retrieves the GMF View associated to the source object
	 *
	 * @param source
	 *            the source
	 * @return the resolved view, or null if it cannot be found
	 */
	public static View findView(Object source) {
		if (source instanceof View) {
			return (View) source;
		}

		if (source instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) source;
			Object adapter = adaptable.getAdapter(View.class);
			if (adapter instanceof View) {
				return (View) adapter;
			}
		}

		if (source != null) {
			return Platform.getAdapterManager().getAdapter(source, View.class);
		}

		EObject obj = EMFHelper.getEObject(source);
		return (obj instanceof View) ? (View) obj : null;
	}

	/**
	 * Tests whether the given View is a reference to an external element.
	 * A view is an external reference if its graphical container is different from its semantic
	 * container (i.e. self.element.eContainer() != self.primaryView.eContainer().element)
	 *
	 * @param diagramElement
	 * @return
	 */
	public static boolean isExternalRef(View diagramElement) {
		if (diagramElement == null) {
			return false;
		}

		View primaryView = SemanticElementHelper.findTopView(diagramElement);
		if (primaryView == null) {
			return false;
		}

		EObject semanticElement = primaryView.getElement();

		if (semanticElement == null) {
			return false;
		}

		EObject parentView = primaryView.eContainer();
		if (!(parentView instanceof View)) {
			return false;
		}

		EObject parentSemanticElement = ((View) parentView).getElement();
		if (parentSemanticElement == null) {
			return false;
		}

		// Relax the constraints for elements displayed on themselves (e.g. Frame in Composite Structure Diagram)
		return parentSemanticElement != semanticElement.eContainer() && parentSemanticElement != semanticElement;
	}

	/**
	 * get the absolute position form the notation
	 * 
	 * @param node
	 *            the current node
	 * @return
	 * @since 3.0
	 */
	public static PrecisionRectangle getAbsoluteBounds(Node node) {
		if (node.getLayoutConstraint() instanceof Bounds) {
			PrecisionRectangle bounds = new PrecisionRectangle(((Bounds) node.getLayoutConstraint()).getX(), ((Bounds) node.getLayoutConstraint()).getY(), ((Bounds) node.getLayoutConstraint()).getWidth(), ((Bounds) node.getLayoutConstraint()).getHeight());
			EObject currentView = (EObject) node.eContainer();
			while (currentView != null) {

				if (currentView instanceof Node) {
					Point ptCurrenview = new Point(((Bounds) ((Node) currentView).getLayoutConstraint()).getX(), ((Bounds) ((Node) currentView).getLayoutConstraint()).getY());
					bounds.translate(ptCurrenview);
				}
				currentView = currentView.eContainer();
			}
			return bounds;
		} else
			return null;
	}
}
