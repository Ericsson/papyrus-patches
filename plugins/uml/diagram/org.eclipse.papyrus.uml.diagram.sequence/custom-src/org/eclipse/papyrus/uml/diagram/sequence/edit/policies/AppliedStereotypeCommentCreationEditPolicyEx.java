/*****************************************************************************
 * Copyright (c) 2013, 2014 Soyatec, CEA, and others.
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
 *   Soyatec - Initial API and implementation
 *   Christian W. Damus (CEA) - bug 323802
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.diagram.ui.editparts.BorderedBorderItemEditPart;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.LayoutConstraint;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.emf.gmf.util.GMFUnsafe;
import org.eclipse.papyrus.uml.diagram.common.Activator;
import org.eclipse.papyrus.uml.diagram.sequence.command.CreateAppliedStereotypeCommentViewCommandEx;
import org.eclipse.papyrus.uml.diagram.stereotype.edition.editpolicies.AppliedStereotypeCommentEditPolicy;
import org.eclipse.swt.widgets.Display;


/**
 * Rewrite the CreateAppliedStereotypeCommentViewCommand to create all Comment View on the Interaction, but not the container of current node.
 *
 * Because we can not add any Node on some container view such as Lifeline.
 *
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class AppliedStereotypeCommentCreationEditPolicyEx extends AppliedStereotypeCommentEditPolicy {


	@Override
	protected void executeAppliedStereotypeCommentCreation(final EObject semanticElement) {

		final TransactionalEditingDomain domain = hostEditPart.getEditingDomain();
		Display.getCurrent().syncExec(new Runnable() {

			@Override
			public void run() {
				int x = 200;
				int y = 100;
				if (hostEditPart.getModel() instanceof Node) {
					LayoutConstraint constraint = ((Node) hostEditPart.getModel()).getLayoutConstraint();
					if (constraint instanceof Bounds) {
						x = x + ((Bounds) constraint).getX();
						y = ((Bounds) constraint).getY();
					}

				}
				if (hostEditPart.getModel() instanceof Edge && ((((Edge) hostEditPart.getModel()).getSource()) instanceof Node)) {

					LayoutConstraint constraint = ((Node) ((Edge) hostEditPart.getModel()).getSource()).getLayoutConstraint();
					if (constraint instanceof Bounds) {
						x = x + ((Bounds) constraint).getX();
						y = ((Bounds) constraint).getY() - 100;
					}

				}
				boolean isBorderElement = false;
				if (hostEditPart instanceof BorderedBorderItemEditPart) {
					isBorderElement = true;
				}
				if (helper.getStereotypeComment((View) getHost().getModel()) == null) {
					CreateAppliedStereotypeCommentViewCommandEx command = new CreateAppliedStereotypeCommentViewCommandEx(domain, (View) hostEditPart.getModel(), x, y, semanticElement, isBorderElement);
					// use to avoid to put it in the command stack
					try {
						GMFUnsafe.write(domain, command);
					} catch (Exception e) {
						Activator.log.error(e);
					}
				}
			}

		});
	}

}
