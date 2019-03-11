/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
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
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IBorderItemEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.InteractionOperandResizePolicy;
import org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.BoundForEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;

/**
 * This class has been modified
 * because the container is used to manipulate as a list
 * the refresh has been modified to take the size of the operand.
 *
 */
public class CInteractionOperandEditPart extends InteractionOperandEditPart {

	public static int DEFAULT_HEIGHT = 40;
	public static int DEFAULT_WIDHT = 100;
	private EditPart activeCreateFeedbackEditPart;

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CInteractionOperandEditPart(View view) {
		super(view);
	}

	/**
	 * this method has been overloaded in order to set InteractionOperand transparent
	 *
	 * @see org.eclipse.papyrus.uml.diagram.common.editparts.RoundedCompartmentEditPart#refreshVisuals()
	 *
	 */
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		getPrimaryShape().setTransparency(100);
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.editparts.NamedElementEditPart#refresh()
	 *
	 */
	@Override
	public void refresh() {
		if (children != null) {
			for (Object object : children) {
				if (object instanceof GraphicalEditPart) {
					// ((GraphicalEditPart)object).refresh();
				}
			}
		}
		super.refresh();

	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionOperandEditPart#createDefaultEditPolicies()
	 *
	 */
	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(InteractionOperandResizePolicy.class.getSimpleName(), new InteractionOperandResizePolicy());
	}

	/**
	 * this method method has been overloaded because of a mistake in the gmfgen.
	 * so we has to implement addition of sub-figures inside the primary figure...
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.AbstractBorderedShapeEditPart#getContentPaneFor(org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart)
	 *
	 * @param editPart
	 * @return
	 */
	@Override
	protected IFigure getContentPaneFor(IGraphicalEditPart editPart) {
		if (editPart instanceof IBorderItemEditPart) {
			return getBorderedFigure().getBorderItemContainer();
		} else {
			return getPrimaryShape();
		}
	}

	/**
	 * because the container is used to manipulate as a list
	 * the refresh has been modified to take the size of the operand.
	 */
	@Override
	protected void refreshBounds() {
		int width = BoundForEditPart.getWidthFromView((Node) getNotationView());
		int height = BoundForEditPart.getHeightFromView((Node) getNotationView());
		Dimension size = new Dimension(width, height);
		int x = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE.getLocation_X())).intValue();
		int y = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE.getLocation_Y())).intValue();
		Point loc = new Point(x, y);
		((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), new Rectangle(loc, size));
	}

	@Override
	public EditPart getTargetEditPart(Request request) {
		if (request instanceof CreateViewAndElementRequest) {
			CreateViewAndElementRequest req = (CreateViewAndElementRequest) request;

			// If we're creating an operand, it needs to be done by the covering combined fragment
			if (UMLElementTypes.CombinedFragment_Shape.equals(req.getViewAndElementDescriptor().getElementAdapter().getAdapter(IElementType.class))) {
				EditPart interactionEP = SequenceUtil.getInteractionCompartment(this);
				if (interactionEP instanceof InteractionInteractionCompartmentEditPart) {
					return ((InteractionInteractionCompartmentEditPart) interactionEP).getTargetEditPart(request);
				}
			} else {
				return SequenceUtil.getParentCombinedFragmentPart(this).getTargetEditPart(request);
			}
		}

		return super.getTargetEditPart(request);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void showTargetFeedback(Request request) {
		if (request instanceof CreateUnspecifiedTypeRequest) {
			((CreateUnspecifiedTypeRequest) request).getElementTypes().forEach(t -> {
				CreateRequest req = ((CreateUnspecifiedTypeRequest) request).getRequestForType((IElementType) t);
				EditPart targetEP = getTargetEditPart(req);
				// as target EP may vary during time, moving along a lifeline, crossing several elements for example
				// storing the target EP seems interesting.
				if (activeCreateFeedbackEditPart != targetEP) {
					// erase active feedback
					if (activeCreateFeedbackEditPart != null) {
						activeCreateFeedbackEditPart.eraseTargetFeedback(request);
					}
					activeCreateFeedbackEditPart = targetEP;
				}
				if (targetEP != this) {
					targetEP.showTargetFeedback(request);
				} else {
					super.showTargetFeedback(request);
				}
			});
			return;
		}
		super.showTargetFeedback(request);

	}

	@SuppressWarnings("unchecked")
	@Override
	public void eraseTargetFeedback(Request request) {
		if (request instanceof CreateUnspecifiedTypeRequest) {
			((CreateUnspecifiedTypeRequest) request).getElementTypes().forEach(t -> {
				CreateRequest req = ((CreateUnspecifiedTypeRequest) request).getRequestForType((IElementType) t);
				EditPart targetEP = getTargetEditPart(req);
				// as target EP may vary during time, moving along a lifeline, crossing several elements for example
				// storing the target EP seems interesting.
				if (activeCreateFeedbackEditPart != null && activeCreateFeedbackEditPart != this) {
					// erase active feedback if any, and forget the active EP
					activeCreateFeedbackEditPart.eraseTargetFeedback(request);
					activeCreateFeedbackEditPart = null;
				}
				if (targetEP != this) {
					targetEP.eraseTargetFeedback(request);
				} else {
					super.eraseTargetFeedback(request);
				}
			});
			return;
		}
		super.eraseTargetFeedback(request);
	}
}
