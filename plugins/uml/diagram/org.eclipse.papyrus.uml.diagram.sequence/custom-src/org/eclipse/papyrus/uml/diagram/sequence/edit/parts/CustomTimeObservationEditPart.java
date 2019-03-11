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

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IBorderItemEditPart;
import org.eclipse.gmf.runtime.diagram.ui.figures.IBorderItemLocator;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeRequest;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.common.locator.ExternalLabelPositionLocator;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.CustomExternalLabelPrimaryDragRoleEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.DeleteTimeElementWithoutEventPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.TimeRelatedSelectionEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.figures.TimeObservationFigure;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomTimeObservationEditPart extends TimeObservationEditPart {

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CustomTimeObservationEditPart(View view) {
		super(view);
	}

	/**
	 * @Override
	 */
	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		removeEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
		removeEditPolicy(DeleteTimeElementWithoutEventPolicy.KEY);
	}

	/**
	 * This method creates a specific edit policy for time realted elements
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.AbstractBorderItemEditPart#getPrimaryDragEditPolicy()
	 *
	 * @return <code>EditPolicy</code>
	 * @Override
	 */
	@Override
	public EditPolicy getPrimaryDragEditPolicy() {
		return new TimeRelatedSelectionEditPolicy();
	}

	/**
	 * @Override use ExternalLabelPrimaryDragRoleEditPolicy
	 */
	@Override
	protected LayoutEditPolicy createLayoutEditPolicy() {
		org.eclipse.gmf.runtime.diagram.ui.editpolicies.LayoutEditPolicy lep = new org.eclipse.gmf.runtime.diagram.ui.editpolicies.LayoutEditPolicy() {

			@Override
			protected EditPolicy createChildEditPolicy(EditPart child) {
				View childView = (View) child.getModel();
				switch (UMLVisualIDRegistry.getVisualID(childView)) {
				case TimeObservationLabelEditPart.VISUAL_ID:
				case TimeObservationAppliedStereotypeEditPart.VISUAL_ID:
					// use ExternalLabelPrimaryDragRoleEditPolicy
					return new CustomExternalLabelPrimaryDragRoleEditPolicy();
				}
				EditPolicy result = child.getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
				if (result == null) {
					result = new NonResizableEditPolicy();
				}
				return result;
			}

			@Override
			protected Command getMoveChildrenCommand(Request request) {
				return null;
			}

			@Override
			protected Command getCreateCommand(CreateRequest request) {
				return null;
			}
		};
		return lep;
	}

	/**
	 * @Override use ExternalLabelPositionLocator
	 */
	@Override
	protected void addBorderItem(IFigure borderItemContainer, IBorderItemEditPart borderItemEditPart) {
		if (borderItemEditPart instanceof TimeObservationLabelEditPart || borderItemEditPart instanceof TimeObservationAppliedStereotypeEditPart) {
			// use ExternalLabelPositionLocator
			IBorderItemLocator locator = new ExternalLabelPositionLocator(getMainFigure());
			borderItemContainer.add(borderItemEditPart.getFigure(), locator);
		} else {
			super.addBorderItem(borderItemContainer, borderItemEditPart);
		}
	}

	/**
	 * @Override use correct dimensions
	 */
	@Override
	protected NodeFigure createNodePlate() {
		// use correct dimensions
		/*
		 * Bypass the preference mechanism which finally returns an incoherent constant hard written in NodePreferencePage.xpt templates.
		 * Instead, we shall use the correct default size.
		 */
		// DefaultSizeNodeFigure result = new DefaultSizeNodeFigure(TimeMarkElementFigure.TIME_MARK_LENGTH, 1);
		// String prefElementId = "TimeObservation";
		// IPreferenceStore store = UMLDiagramEditorPlugin.getInstance().getPreferenceStore();
		// String preferenceConstantWitdh = PreferenceInitializerForElementHelper.getpreferenceKey(getNotationView(), prefElementId, PreferencesConstantsHelper.WIDTH);
		// String preferenceConstantHeight = PreferenceInitializerForElementHelper.getpreferenceKey(getNotationView(), prefElementId, PreferencesConstantsHelper.HEIGHT);
		// DefaultSizeNodeFigure result = new DefaultSizeNodeFigure(store.getInt(preferenceConstantWitdh), store.getInt(preferenceConstantHeight));
		// FIXME: workaround for #154536
		// result.getBounds().setSize(result.getPreferredSize());
		NodeFigure result = new NodeFigure();
		return result;
	}

	/**
	 * @Override Override for redirecting creation request to the lifeline
	 */
	@Override
	public Command getCommand(Request request) {
		if (request instanceof CreateUnspecifiedTypeRequest) {
			return getParent().getCommand(request);
		}
		return super.getCommand(request);
	}

	/**
	 * @Override Override for redirecting creation request to the lifeline
	 */
	@Override
	public void showSourceFeedback(Request request) {
		if (request instanceof CreateUnspecifiedTypeRequest) {
			getParent().showSourceFeedback(request);
		}
		super.showSourceFeedback(request);
	}

	/**
	 * @Override Override for redirecting creation request to the lifeline
	 */
	@Override
	public void eraseSourceFeedback(Request request) {
		if (request instanceof CreateUnspecifiedTypeRequest) {
			getParent().eraseSourceFeedback(request);
		}
		super.eraseSourceFeedback(request);
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeObservationEditPart#createNodeShape()
	 *
	 * @return
	 */
	@Override
	protected IFigure createNodeShape() {
		return primaryShape = new TimeObservationFigure();
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeObservationEditPart#getPrimaryShape()
	 *
	 * @return
	 */
	@Override
	public TimeObservationFigure getPrimaryShape() {
		return (TimeObservationFigure) primaryShape;
	}
}
