/*****************************************************************************
 * Copyright (c) 2017, 2018 CEA LIST, EclipseSource and others.
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
 *   Celine Janssens (celine.janssens@all4tec.net) - Add Coregion  functionnality
 *   EclipseSource - Bug 533770
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.PapyrusWrappingLabel;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationUtils;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.NamedStyleProperties;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.CombinedFragmentResizeEditPolicy;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.ConsiderIgnoreFragment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.InteractionOperatorKind;

/**
 * @author Patrick Tessier
 * @since 3.0
 *        this class has been customized to prevent the strange feedback of lifeline during the move
 *
 */
public class CCombinedFragmentEditPart extends CombinedFragmentEditPart {
	public static int DEFAULT_HEIGHT = 60;
	public static int DEFAULT_WIDTH = 40;


	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CCombinedFragmentEditPart(View view) {
		super(view);
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentEditPart#handleNotificationEvent(org.eclipse.emf.common.notify.Notification)
	 *
	 * @param event
	 */
	@Override
	protected void handleNotificationEvent(Notification event) {
		super.handleNotificationEvent(event);
		refreshLabel();
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.editparts.RoundedCompartmentEditPart#refreshVisuals()
	 *
	 */
	@Override
	protected void refreshVisuals() {
		refreshLabel();
		super.refreshVisuals();
		refreshLabelSize();
	}

	/**
	 * @since 5.0
	 */
	protected void refreshLabel() {
		CombinedFragment semantic = (CombinedFragment) this.resolveSemanticElement();
		if (semantic != null && semantic.getInteractionOperator() != null) {
			getPrimaryShape().setName(semantic.getInteractionOperator().getLiteral());
		}
	}

	/**
	 * @since 5.1
	 */
	// Bug 535519
	protected void refreshLabelSize() {
		int labelWidth = NotationUtils.getIntValue(getNotationView(), NamedStyleProperties.LABEL_WIDTH, PapyrusWrappingLabel.AUTO_SIZE);
		int labelHeight = NotationUtils.getIntValue(getNotationView(), NamedStyleProperties.LABEL_HEIGHT, PapyrusWrappingLabel.AUTO_SIZE);
		getPrimaryShape().setLabelSize(labelWidth, labelHeight);
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentEditPart#createDefaultEditPolicies()
	 *
	 */
	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new CombinedFragmentResizeEditPolicy());
	}


	/**
	 * Get the Notation Value of the CoRegion boolean
	 *
	 * @return true if the Combined Fragment should be displayed as a CoRegion with Brackets.
	 *         This is the case if the Operator is Parallel and if the combinedFragment covers only 1 lifeline
	 */
	public boolean isCoregion() {

		boolean coregion = false;
		Element umlElement = getUMLElement();

		if ((umlElement instanceof CombinedFragment) && !(umlElement instanceof ConsiderIgnoreFragment)) {

			InteractionOperatorKind interactionOperator = ((CombinedFragment) umlElement).getInteractionOperator();
			if (InteractionOperatorKind.PAR_LITERAL.getLiteral() == interactionOperator.getLiteral()) {
				if (((CombinedFragment) umlElement).getCovereds().size() == 1) {
					coregion = true;
				}
			}
		}

		return coregion;
	}


	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.editparts.NamedElementEditPart#refresh()
	 *
	 *      Refresh the CoRegion Value
	 */
	@Override
	public void refresh() {

		getPrimaryShape().setCoregion(isCoregion());
		super.refresh();

	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#showTargetFeedback(org.eclipse.gef.Request)
	 *
	 * @param request
	 */
	@Override
	public void showTargetFeedback(Request request) {
		if (request instanceof ChangeBoundsRequest) {
			ChangeBoundsRequest changeBoundsRequest = (ChangeBoundsRequest) request;

			if (changeBoundsRequest.getEditParts().get(0) instanceof LifelineEditPart) {
				changeBoundsRequest.setMoveDelta(new Point(changeBoundsRequest.getMoveDelta().x, 0));
			}
		}
		super.showTargetFeedback(request);
	}

}
