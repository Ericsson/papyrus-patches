/*****************************************************************************
 * Copyright (c) 2009, 2018 Atos Origin, EclipseSource and others.
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
 *   Atos Origin - Initial API and implementation
 *   EclipseSource - Bug 533770
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.DragDropEditPolicy;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.type.core.commands.SetValueCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.SetRequest;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.LayoutConstraint;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetLocationCommand;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionOperandEditPart;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * The custom DragDropEditPolicy for InteractionOperandEditPart.
 */
public class InteractionOperandDragDropEditPolicy extends DragDropEditPolicy {

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.DragDropEditPolicy#getCommand(org.eclipse.gef.Request)
	 *
	 * @param request
	 * @return
	 */
	@Override
	public Command getCommand(Request request) {
		return super.getCommand(request);
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.DragDropEditPolicy#getDropCommand(org.eclipse.gef.requests.ChangeBoundsRequest)
	 *
	 * @param request
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked") // GMF is Java 1.4
	protected Command getDropCommand(ChangeBoundsRequest request) {
		List<EditPart> editParts = request.getEditParts();

		if (editParts.size() == 1 && editParts.get(0) instanceof InteractionOperandEditPart) {
			InteractionOperandEditPart partToMove = (InteractionOperandEditPart) editParts.get(0);
			InteractionOperand operandToReorder = getOperand(partToMove);

			EditPart fragmentCptPart = getHost().getParent();
			CombinedFragment fragment = getCombinedFragment();
			if (fragment == null || false == fragmentCptPart instanceof IGraphicalEditPart) {
				return UnexecutableCommand.INSTANCE;
			}
			IGraphicalEditPart fragmentGEP = (IGraphicalEditPart)fragmentCptPart;
			CompositeCommand command = new CompositeCommand("Reorder operands");
			List<InteractionOperand> operands = new ArrayList<>(fragment.getOperands());
			if (! operands.contains(operandToReorder)) {
				return UnexecutableCommand.INSTANCE;
			}

			// Reorder semantic elements
			int newIndex = operands.indexOf(getHostOperand());
			operands.remove(operandToReorder);
			operands.add(newIndex, operandToReorder);
			SetRequest semanticReorder = new SetRequest(fragment, UMLPackage.Literals.COMBINED_FRAGMENT__OPERAND, operands);
			command.add(new SetValueCommand(semanticReorder));

			// Reorder notation elements
			List<View> operandViews = new ArrayList<>(fragmentGEP.getNotationView().getChildren());
			View operandViewToReorder = partToMove.getNotationView();
			operandViews.remove(operandViewToReorder);
			operandViews.add(newIndex, operandViewToReorder);
			SetRequest graphicalReorder = new SetRequest(fragmentGEP.getNotationView(), NotationPackage.Literals.VIEW__PERSISTED_CHILDREN, operandViews);
			command.add(new SetValueCommand(graphicalReorder));

			// Compute the new bounds of each view, based on the new order
			// The height of each operand didn't change, so we just have to compute the sum of heights for each previous view
			int y = 0;
			CompositeCommand updateBounds = new CompositeCommand("Update operands bounds");
			TransactionalEditingDomain editingDomain = ((IGraphicalEditPart)getHost()).getEditingDomain();
			for (View view : operandViews) {
				if (view instanceof Node) {
					Node node = (Node)view;
					LayoutConstraint layoutConstraint = node.getLayoutConstraint();
					if (layoutConstraint instanceof Bounds) {
						Bounds currentBounds = (Bounds)layoutConstraint;
						Point newPos = new Point(0, y);
						updateBounds.add(new SetLocationCommand(editingDomain, "Update bounds", new EObjectAdapter(node), newPos));
						y += currentBounds.getHeight();
					}
				}
			}
			command.add(updateBounds);

			return new ICommandProxy(command);
		}

		return null;
	}

	private InteractionOperand getOperand(InteractionOperandEditPart editPart) {
		EObject element = editPart.getNotationView().getElement();
		return element instanceof InteractionOperand ? (InteractionOperand)element : null;
	}

	private CombinedFragment getCombinedFragment() {
		InteractionOperand operand = getHostOperand();
		Element parent = operand == null ? null : operand.getOwner();
		return parent instanceof CombinedFragment ? (CombinedFragment) parent : null;
	}

	private InteractionOperand getHostOperand() {
		EObject hostSemantic = getHostObject();
		return hostSemantic instanceof InteractionOperand ? (InteractionOperand)hostSemantic : null;
	}

}
