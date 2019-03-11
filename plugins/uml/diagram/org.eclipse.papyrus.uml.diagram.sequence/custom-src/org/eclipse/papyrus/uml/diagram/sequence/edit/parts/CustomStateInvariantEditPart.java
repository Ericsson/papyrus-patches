/*****************************************************************************
 * Copyright (c) 2010 CEA
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
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gmf.runtime.notation.DecorationNode;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.IPapyrusEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.IPapyrusWrappingLabel;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationUtils;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.FigureUtils;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.AppliedStereotypeLabelDisplayEditPolicy;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.AppliedStereotypeNodeLabelDisplayEditPolicy;
import org.eclipse.papyrus.uml.diagram.common.providers.UIAdapterImpl;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.AppliedStereotypeCommentCreationEditPolicyEx;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.StateInvariantResizableEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.figures.StateInvariantFigure;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry;
import org.eclipse.papyrus.uml.diagram.sequence.util.NotificationHelper;
import org.eclipse.papyrus.uml.diagram.stereotype.edition.editpolicies.AppliedStereotypeCommentEditPolicy;
import org.eclipse.swt.graphics.Color;
import org.eclipse.uml2.uml.StateInvariant;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomStateInvariantEditPart extends StateInvariantEditPart implements IPapyrusEditPart {

	/**
	 * Default Margin when not present in CSS
	 */
	public static final int DEFAULT_MARGIN = 0;

	/**
	 * CSS Integer property to define the horizontal Label Margin
	 */
	public static final String TOP_MARGIN_PROPERTY = "TopMarginLabel"; // $NON-NLS$

	/**
	 * CSS Integer property to define the vertical Label Margin
	 */
	public static final String LEFT_MARGIN_PROPERTY = "LeftMarginLabel"; // $NON-NLS$

	/**
	 * CSS Integer property to define the horizontal Label Margin
	 */
	public static final String BOTTOM_MARGIN_PROPERTY = "BottomMarginLabel"; // $NON-NLS$

	/**
	 * CSS Integer property to define the vertical Label Margin
	 */
	public static final String RIGHT_MARGIN_PROPERTY = "RightMarginLabel"; // $NON-NLS$

	/**
	 * Notfier for listen and unlistend model element.
	 */
	private NotificationHelper notifierHelper = new NotificationHelper(new UIAdapterImpl() {

		@Override
		protected void safeNotifyChanged(Notification msg) {
			handleNotificationEvent(msg);
		}
	});

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CustomStateInvariantEditPart(View view) {
		super(view);
	}

	@Override
	public void activate() {
		super.activate();
		EObject element = resolveSemanticElement();
		if (element instanceof StateInvariant && ((StateInvariant) element).getInvariant() != null) {
			notifierHelper.listenObject(((StateInvariant) element).getInvariant());
		}
	}

	@Override
	public void deactivate() {
		EObject element = resolveSemanticElement();
		if (element instanceof StateInvariant && ((StateInvariant) element).getInvariant() != null) {
			notifierHelper.unlistenObject(((StateInvariant) element).getInvariant());
		}
		super.deactivate();
	}

	/**
	 * @Override
	 */
	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		// install a editpolicy to display stereotypes
		installEditPolicy(AppliedStereotypeCommentEditPolicy.APPLIED_STEREOTYPE_COMMENT, new AppliedStereotypeCommentCreationEditPolicyEx());
		installEditPolicy(AppliedStereotypeLabelDisplayEditPolicy.STEREOTYPE_LABEL_POLICY, new AppliedStereotypeNodeLabelDisplayEditPolicy());

	}

	@Override
	protected void handleNotificationEvent(Notification notification) {
		super.handleNotificationEvent(notification);
		Object feature = notification.getFeature();
		Object notifier = notification.getNotifier();
		if ((getModel() != null) && (getModel() == notifier)) {
			if (NotationPackage.eINSTANCE.getLineStyle_LineWidth().equals(feature)) {
				refreshLineWidth();
			}
		} else if (UMLPackage.eINSTANCE.getStateInvariant_Invariant() == feature) {
			if (Notification.SET == notification.getEventType() || Notification.UNSET == notification.getEventType()) {
				notifierHelper.unlistenObject((Notifier) notification.getOldValue());
				notifierHelper.listenObject((Notifier) notification.getNewValue());
			}
			refreshLabels();
		} else if (notifier != null) {
			EObject element = resolveSemanticElement();
			if (element instanceof StateInvariant && notifier == ((StateInvariant) element).getInvariant()) {
				refreshLabels();
			}
		}
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.AbstractBorderItemEditPart#getPrimaryDragEditPolicy()
	 *
	 * @return
	 */
	@Override
	public EditPolicy getPrimaryDragEditPolicy() {

		return new StateInvariantResizableEditPolicy();
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#getModelChildren()
	 *
	 * @return
	 */
	@Override
	protected List getModelChildren() {
		List<Object> modelChildren = new ArrayList<Object>(super.getModelChildren());
		boolean hasInvariantView = false;
		for (Object object : modelChildren) {
			if (object instanceof View && UMLVisualIDRegistry.getType(StateInvariantLabelEditPart.VISUAL_ID).equals(((View) object).getType())) {
				hasInvariantView = true;
				break;
			}
		}
		if (!hasInvariantView) {
			final View view = getNotationView();
			final DecorationNode guardNode = NotationFactory.eINSTANCE.createDecorationNode();
			guardNode.setType(UMLVisualIDRegistry.getType(StateInvariantLabelEditPart.VISUAL_ID));

			modelChildren.add(guardNode);
		}
		return modelChildren;
	}

	@Override
	protected void setFontColor(Color color) {
		super.setFontColor(color);
		StateInvariantFigure primaryShape = getPrimaryShape();
		if (primaryShape != null && primaryShape.getConstraintContentContainer() != null) {
			primaryShape.getConstraintContentContainer().setForegroundColor(color);
		}
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		refreshLineWidth();
		refreshTransparency();
		refreshLabels();
		refreshLabelMargin();
	}

	/**
	 * Refresh margin of named element children labels
	 * <ul>
	 * <li>Get Css values</li>
	 * <li>Get all the children figure</li>
	 * <li>If the child is a label then apply the margin</li>
	 * </ul>
	 */
	private void refreshLabelMargin() {
		IFigure figure = null;

		int leftMargin = DEFAULT_MARGIN;
		int rightMargin = DEFAULT_MARGIN;
		int topMargin = DEFAULT_MARGIN;
		int bottomMargin = DEFAULT_MARGIN;

		Object model = this.getModel();



		if (model instanceof View) {
			leftMargin = NotationUtils.getIntValue((View) model, LEFT_MARGIN_PROPERTY, DEFAULT_MARGIN);
			rightMargin = NotationUtils.getIntValue((View) model, RIGHT_MARGIN_PROPERTY, DEFAULT_MARGIN);
			topMargin = NotationUtils.getIntValue((View) model, TOP_MARGIN_PROPERTY, DEFAULT_MARGIN);
			bottomMargin = NotationUtils.getIntValue((View) model, BOTTOM_MARGIN_PROPERTY, DEFAULT_MARGIN);
		}

		// Get all children figures of the Edit Part and set margin according to the retrieve values
		if (this instanceof IPapyrusEditPart) {
			figure = ((IPapyrusEditPart) this).getPrimaryShape();
			List<IPapyrusWrappingLabel> labelChildFigureList = FigureUtils.findChildFigureInstances(figure, IPapyrusWrappingLabel.class);

			for (IPapyrusWrappingLabel label : labelChildFigureList) {
				if (label != null) {
					label.setMarginLabel(leftMargin, topMargin, rightMargin, bottomMargin);
				}
			}
		}


	}

	/**
	 * Refresh Invariant.
	 */
	protected void refreshLabels() {
		List parts = getChildren();
		for (Object p : parts) {
			if (p instanceof CustomStateInvariantLabelEditPart) {
				((CustomStateInvariantLabelEditPart) p).refreshLabel();
			}
		}
	}

	@Override
	protected void setTransparency(int transp) {
		getPrimaryShape().setTransparency(transp);
	}

	@Override
	protected void refreshBounds() {
		if (getBorderItemLocator() != null) {
			int x = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE.getLocation_X())).intValue();
			int y = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE.getLocation_Y())).intValue();
			Point loc = new Point(x, y);
			int width = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE.getSize_Width())).intValue();
			int height = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE.getSize_Height())).intValue();
			Dimension size = new Dimension(width, height);
			// fix size
			getFigure().setBounds(new Rectangle(loc, size));
			getBorderItemLocator().setConstraint(new Rectangle(loc, size));
		} else {
			super.refreshBounds();
		}
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.parts.StateInvariantEditPart#getPrimaryShape()
	 *
	 * @return
	 * @since 3.0
	 */
	@Override
	public StateInvariantFigure getPrimaryShape() {
		return (StateInvariantFigure) primaryShape;
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.parts.StateInvariantEditPart#addFixedChild(org.eclipse.gef.EditPart)
	 *
	 * @param childEditPart
	 * @return
	 */
	@Override
	protected boolean addFixedChild(EditPart childEditPart) {
		if (childEditPart instanceof CustomStateInvariantLabelEditPart) {
			((CustomStateInvariantLabelEditPart) childEditPart).setLabel(getPrimaryShape().getInvariantFigure());
			return true;
		}
		return super.addFixedChild(childEditPart);
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.parts.StateInvariantEditPart#removeFixedChild(org.eclipse.gef.EditPart)
	 *
	 * @param childEditPart
	 * @return
	 */
	@Override
	protected boolean removeFixedChild(EditPart childEditPart) {
		if (childEditPart instanceof CustomStateInvariantLabelEditPart) {
			return true;
		}
		return super.removeFixedChild(childEditPart);
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.parts.StateInvariantEditPart#createNodeShape()
	 *
	 * @return
	 */
	@Override
	protected IFigure createNodeShape() {
		return primaryShape = new StateInvariantFigure(getMapMode());
	}



}
