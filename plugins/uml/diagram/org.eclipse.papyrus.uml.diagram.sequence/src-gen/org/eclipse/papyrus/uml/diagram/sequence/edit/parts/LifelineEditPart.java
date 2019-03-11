
/**
 * Copyright (c) 2016 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
  *  CEA LIST - Initial API and implementation
 */
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IBorderItemEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.DragDropEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.diagram.ui.figures.IBorderItemLocator;
import org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultCreationEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultGraphicalNodeEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultSemanticEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IPapyrusNodeFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.RoundedRectangleNodePlateFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.SelectableBorderedNodeFigure;
import org.eclipse.papyrus.uml.diagram.common.editparts.RoundedCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.AffixedNodeAlignmentEditPolicy;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.AppliedStereotypeLabelDisplayEditPolicy;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.AppliedStereotypeNodeLabelDisplayEditPolicy;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.BorderItemResizableEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.CustomDiagramDragDropEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.LifeLineResizeAffixedNodeEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.figures.LifelineFigure;
import org.eclipse.papyrus.uml.diagram.sequence.locator.CenterLocator;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectRectangleToGridEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.GrillingBasedResizableShapeEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.LifeLineGraphicalNodeEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.LifeLineXYLayoutEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.LifelineCreationEditPolicy;
import org.eclipse.swt.graphics.Color;

/**
 * @generated
 */
public class LifelineEditPart extends RoundedCompartmentEditPart {

	/**
	 * @generated
	 */
	public static final String VISUAL_ID = "Lifeline_Shape";

	/**
	 * @generated
	 */
	protected IFigure contentPane;

	/**
	 * @generated
	 */
	protected IFigure primaryShape;

	/**
	 * @generated
	 */
	public LifelineEditPart(View view) {
		super(view);
	}

	/**
	 * @generated
	 */
	@Override
	protected void createDefaultEditPolicies() {
		installEditPolicy(EditPolicyRoles.CREATION_ROLE, new DefaultCreationEditPolicy());
		super.createDefaultEditPolicies();
		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new DefaultSemanticEditPolicy());

		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new DefaultGraphicalNodeEditPolicy());

		installEditPolicy(EditPolicyRoles.DRAG_DROP_ROLE, new DragDropEditPolicy());
		// in Papyrus diagrams are not strongly synchronised
		// installEditPolicy(org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles.CANONICAL_ROLE, new org.eclipse.papyrus.uml.diagram.sequence.edit.policies.LifelineCanonicalEditPolicy());

		installEditPolicy(EditPolicy.LAYOUT_ROLE, createLayoutEditPolicy());
		installEditPolicy(ConnectRectangleToGridEditPolicy.CONNECT_TO_GRILLING_MANAGEMENT, new ConnectRectangleToGridEditPolicy());
		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new GrillingBasedResizableShapeEditPolicy());
		installEditPolicy(EditPolicyRoles.DRAG_DROP_ROLE, new CustomDiagramDragDropEditPolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new LifeLineGraphicalNodeEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new LifeLineXYLayoutEditPolicy());
		installEditPolicy(EditPolicyRoles.CREATION_ROLE, new LifelineCreationEditPolicy());
		installEditPolicy(AffixedNodeAlignmentEditPolicy.AFFIXED_CHILD_ALIGNMENT_ROLE, new LifeLineResizeAffixedNodeEditPolicy());
		installEditPolicy(AppliedStereotypeLabelDisplayEditPolicy.STEREOTYPE_LABEL_POLICY, new AppliedStereotypeNodeLabelDisplayEditPolicy());
		// XXX need an SCR to runtime to have another abstract superclass that would let children add reasonable editpolicies
		// removeEditPolicy(org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles.CONNECTION_HANDLES_ROLE);
	}

	/**
	 * @generated
	 */
	protected LayoutEditPolicy createLayoutEditPolicy() {
		org.eclipse.gmf.runtime.diagram.ui.editpolicies.LayoutEditPolicy lep = new org.eclipse.gmf.runtime.diagram.ui.editpolicies.LayoutEditPolicy() {

			@Override
			protected EditPolicy createChildEditPolicy(EditPart child) {
				View childView = (View) child.getModel();
				String vid = UMLVisualIDRegistry.getVisualID(childView);
				if (vid != null) {
					switch (vid) {
					case StateInvariantEditPart.VISUAL_ID:
					case TimeConstraintEditPart.VISUAL_ID:
					case TimeObservationEditPart.VISUAL_ID:
					case DurationConstraintEditPart.VISUAL_ID:
					case DestructionOccurrenceSpecificationEditPart.VISUAL_ID:
						return new BorderItemResizableEditPolicy();
					}
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
	 * Papyrus codeGen
	 *
	 * @generated
	 **/
	@Override
	protected void handleNotificationEvent(Notification event) {
		super.handleNotificationEvent(event);

	}

	/**
	 * @generated
	 */
	@Override
	protected IFigure createNodeShape() {
		return primaryShape = new LifelineFigure();
	}

	/**
	 * org.eclipse.papyrus.uml.diagram.sequence.figures.LifelineFigure
	 *
	 * @generated
	 */
	@Override
	public LifelineFigure getPrimaryShape() {
		return (LifelineFigure) primaryShape;
	}

	/**
	 * @generated
	 */
	protected boolean addFixedChild(EditPart childEditPart) {
		if (childEditPart instanceof LifelineNameEditPart) {
			((LifelineNameEditPart) childEditPart).setLabel(getPrimaryShape().getFigureLifelineLabelFigure());
			return true;
		}




		// Papyrus Gencode :Specific locator for the itemBorder of the lifeline.
		if (childEditPart instanceof StateInvariantEditPart) {
			IBorderItemLocator locator = new CenterLocator(getMainFigure(), PositionConstants.NONE);
			getBorderedFigure().getBorderItemContainer().add(((StateInvariantEditPart) childEditPart).getFigure(), locator);
			return true;
		}




		// Papyrus Gencode :Affixed locator for Lifelines to place element with a time bar
		if (childEditPart instanceof TimeConstraintEditPart) {
			IBorderItemLocator locator = new CenterLocator(getMainFigure(), PositionConstants.NONE);
			getBorderedFigure().getBorderItemContainer().add(((TimeConstraintEditPart) childEditPart).getFigure(), locator);
			return true;
		}




		// Papyrus Gencode :Affixed locator for Lifelines to place element with a time bar
		if (childEditPart instanceof TimeObservationEditPart) {
			IBorderItemLocator locator = new CenterLocator(getMainFigure(), PositionConstants.NONE);
			getBorderedFigure().getBorderItemContainer().add(((TimeObservationEditPart) childEditPart).getFigure(), locator);
			return true;
		}




		// Papyrus Gencode :Affixed locator for Lifelines to place element with a time bar
		if (childEditPart instanceof DurationConstraintEditPart) {
			IBorderItemLocator locator = new CenterLocator(getMainFigure(), PositionConstants.NONE);
			getBorderedFigure().getBorderItemContainer().add(((DurationConstraintEditPart) childEditPart).getFigure(), locator);
			return true;
		}




		// Papyrus Gencode :Specific locator for the itemBorder of the lifeline.
		if (childEditPart instanceof DestructionOccurrenceSpecificationEditPart) {
			IBorderItemLocator locator = new CenterLocator(getMainFigure(), PositionConstants.SOUTH);
			getBorderedFigure().getBorderItemContainer().add(((DestructionOccurrenceSpecificationEditPart) childEditPart).getFigure(), locator);
			return true;
		}


		return false;
	}

	/**
	 * @generated
	 */
	protected boolean removeFixedChild(EditPart childEditPart) {
		if (childEditPart instanceof LifelineNameEditPart) {
			return true;
		}
		if (childEditPart instanceof StateInvariantEditPart) {
			getBorderedFigure().getBorderItemContainer().remove(((StateInvariantEditPart) childEditPart).getFigure());
			return true;
		}
		if (childEditPart instanceof TimeConstraintEditPart) {
			getBorderedFigure().getBorderItemContainer().remove(((TimeConstraintEditPart) childEditPart).getFigure());
			return true;
		}
		if (childEditPart instanceof TimeObservationEditPart) {
			getBorderedFigure().getBorderItemContainer().remove(((TimeObservationEditPart) childEditPart).getFigure());
			return true;
		}
		if (childEditPart instanceof DurationConstraintEditPart) {
			getBorderedFigure().getBorderItemContainer().remove(((DurationConstraintEditPart) childEditPart).getFigure());
			return true;
		}
		if (childEditPart instanceof DestructionOccurrenceSpecificationEditPart) {
			getBorderedFigure().getBorderItemContainer().remove(((DestructionOccurrenceSpecificationEditPart) childEditPart).getFigure());
			return true;
		}
		return false;
	}

	/**
	 * @generated
	 */
	@Override
	protected void addChildVisual(EditPart childEditPart, int index) {
		if (addFixedChild(childEditPart)) {
			return;
		}
		super.addChildVisual(childEditPart, -1);
	}

	/**
	 * @generated
	 */
	@Override
	protected void removeChildVisual(EditPart childEditPart) {
		if (removeFixedChild(childEditPart)) {
			return;
		}
		super.removeChildVisual(childEditPart);
	}

	/**
	 * @generated
	 */
	@Override
	protected IFigure getContentPaneFor(IGraphicalEditPart editPart) {
		if (editPart instanceof IBorderItemEditPart) {
			return getBorderedFigure().getBorderItemContainer();
		}
		return getContentPane();
	}

	/**
	 * @generated
	 */
	@Override
	protected NodeFigure createNodePlate() {
		RoundedRectangleNodePlateFigure result = new RoundedRectangleNodePlateFigure(100, 700);
		return result;
	}

	/**
	 * Creates figure for this edit part.
	 *
	 * Body of this method does not depend on settings in generation model
	 * so you may safely remove <i>generated</i> tag and modify it.
	 *
	 * @generated
	 */
	@Override
	protected NodeFigure createMainFigure() {
		return new SelectableBorderedNodeFigure(createMainFigureWithSVG());

	}

	/**
	 * Default implementation treats passed figure as content pane.
	 * Respects layout one may have set for generated figure.
	 *
	 * @param nodeShape
	 *            instance of generated figure class
	 * @generated
	 */
	@Override
	protected IFigure setupContentPane(IFigure nodeShape) {
		if (nodeShape.getLayoutManager() == null) {
			ConstrainedToolbarLayout layout = new ConstrainedToolbarLayout();
			layout.setSpacing(5);
			nodeShape.setLayoutManager(layout);
		}
		return nodeShape; // use nodeShape itself as contentPane
	}

	/**
	 * @generated
	 */
	@Override
	public IFigure getContentPane() {
		if (contentPane != null) {
			return contentPane;
		}
		return super.getContentPane();
	}

	/**
	 * @generated
	 */
	@Override
	protected void setForegroundColor(Color color) {
		if (primaryShape != null) {
			primaryShape.setForegroundColor(color);
		}
	}

	/**
	 * @generated
	 */
	@Override
	protected void setLineWidth(int width) {
		super.setLineWidth(width);
	}

	/**
	 * @generated
	 */
	@Override
	protected void setLineType(int style) {
		if (primaryShape instanceof IPapyrusNodeFigure) {
			((IPapyrusNodeFigure) primaryShape).setLineStyle(style);
		}
	}

	/**
	 * @generated
	 */
	@Override
	public EditPart getPrimaryChildEditPart() {
		return getChildBySemanticHint(UMLVisualIDRegistry.getType(LifelineNameEditPart.VISUAL_ID));
	}

}
