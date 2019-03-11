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
 *   Celine janssens (ALL4TEC) celine.janssens@all4tec.net - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.command;

import java.util.ArrayList;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Connector;
import org.eclipse.gmf.runtime.notation.EObjectValueStyle;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.RelativeBendpoints;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.gmf.runtime.notation.TitleStyle;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.gmf.runtime.notation.datatype.RelativeBendpoint;
import org.eclipse.papyrus.uml.diagram.common.stereotype.display.command.CreateAppliedStereotypeCommentViewCommand;
import org.eclipse.papyrus.uml.diagram.common.stereotype.display.helper.StereotypeDisplayConstant;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.Lifeline;

/**
 * @author Céline JANSSENS
 *
 */
public class CreateAppliedStereotypeCommentViewCommandEx extends CreateAppliedStereotypeCommentViewCommand {

	/**
	 * Constructor.
	 *
	 * @param domain
	 * @param owner
	 * @param x
	 * @param y
	 * @param base_Element
	 * @param isABordererElement
	 */
	public CreateAppliedStereotypeCommentViewCommandEx(TransactionalEditingDomain domain, View owner, int x, int y, EObject base_Element, boolean isABordererElement) {
		super(domain, owner, x, y, base_Element, isABordererElement);
	}

	@Override
	public void doExecute() {

		// create the node
		Node node = NotationFactory.eINSTANCE.createShape();
		Bounds bounds = NotationFactory.eINSTANCE.createBounds();
		bounds.setX(x);
		bounds.setY(y);
		node.setLayoutConstraint(bounds);
		TitleStyle ts = NotationFactory.eINSTANCE.createTitleStyle();
		ts.setShowTitle(true);
		node.getStyles().add(ts);
		node.setElement(null);
		node.setType(StereotypeDisplayConstant.STEREOTYPE_COMMENT_TYPE);

		connectCommentNode(owner, node);

		EObjectValueStyle eObjectValueStyle = (EObjectValueStyle) node.createStyle(NotationPackage.eINSTANCE.getEObjectValueStyle());
		eObjectValueStyle.setEObjectValue(base_element);
		eObjectValueStyle.setName(StereotypeDisplayConstant.STEREOTYPE_COMMENT_RELATION_NAME);

		// create the link
		Connector edge = NotationFactory.eINSTANCE.createConnector();
		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
		ArrayList<RelativeBendpoint> points = new ArrayList<>(2);
		points.add(new RelativeBendpoint());
		points.add(new RelativeBendpoint());
		bendpoints.setPoints(points);
		edge.setBendpoints(bendpoints);
		ViewUtil.insertChildView(owner.getDiagram(), edge, -1, StereotypeDisplayConstant.PERSISTENT);
		edge.setType(StereotypeDisplayConstant.STEREOTYPE_COMMENT_LINK_TYPE);
		edge.setElement(base_element);
		IdentityAnchor anchor = NotationFactory.eINSTANCE.createIdentityAnchor();
		edge.setSourceAnchor(anchor);
		anchor = NotationFactory.eINSTANCE.createIdentityAnchor();
		edge.setTargetAnchor(anchor);
		View source = owner;
		while (source != null && !(source instanceof Shape || source instanceof Edge)) {
			source = (View) source.eContainer();
		}
		edge.setSource(source);
		edge.setTarget(node);
		edge.setElement(null);
		eObjectValueStyle = (EObjectValueStyle) edge.createStyle(NotationPackage.eINSTANCE.getEObjectValueStyle());
		eObjectValueStyle.setEObjectValue(base_element);
		eObjectValueStyle.setName(StereotypeDisplayConstant.STEREOTYPE_COMMENT_RELATION_NAME);


	}

	/**
	 * add the comment node form the owner
	 *
	 * @param owner
	 *            the view from which we want to display a comment stereotype, cannot be null
	 * @param commentNode
	 *            node that represent the comment , cannot be null
	 */
	private void connectCommentNode(View owner, Node commentNode) {


		View econtainer = (View) owner.eContainer();
		if (owner instanceof Edge) {
			econtainer = (View) ((Edge) owner).getSource().eContainer();
			((Bounds) commentNode.getLayoutConstraint()).setX(100);
			((Bounds) commentNode.getLayoutConstraint()).setY(100);
			while (econtainer instanceof Edge) {
				econtainer = (View) ((Edge) econtainer).getSource().eContainer();
			}
		}
		// for the case of a port
		if (isBorderedElement) {
			if (econtainer.eContainer() != null) {
				econtainer = (View) econtainer.eContainer();
			}
		}
		// Ignore to create on self container.
		if (ViewUtil.resolveSemanticElement(owner) == ViewUtil.resolveSemanticElement(econtainer)) {
			econtainer = (View) econtainer.eContainer();
		}
		// We should NOT add any child to Lifeline Directly.
		while (econtainer != null && (ViewUtil.resolveSemanticElement(econtainer) instanceof Lifeline || ViewUtil.resolveSemanticElement(econtainer) instanceof CombinedFragment || ViewUtil.resolveSemanticElement(econtainer) instanceof InteractionOperand)) {
			econtainer = (View) econtainer.eContainer();
		}
		ViewUtil.insertChildView(econtainer, commentNode, ViewUtil.APPEND, StereotypeDisplayConstant.PERSISTENT);

	}



}
