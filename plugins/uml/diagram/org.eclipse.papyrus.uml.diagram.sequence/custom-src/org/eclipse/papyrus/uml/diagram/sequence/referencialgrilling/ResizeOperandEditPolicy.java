/*****************************************************************************
 * Copyright (c) 2017, 2018 CEA LIST, EclipseSource, Christian W. Damus, and others.
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
 *   EclipseSource - Bug 533770
 *   Christian W. Damus - bug 533676
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.NotationHelper;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetResizeAndLocationCommand;
import org.eclipse.papyrus.uml.diagram.sequence.validation.AsyncValidateCommand;
import org.eclipse.uml2.uml.InteractionOperand;


/**
 * This class is used to allow the resize and adding of children of the combined Fragment.
 * It is applied on the CombinedFragment
 */
public class ResizeOperandEditPolicy extends GraphicalEditPolicy {


	/**
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#activate()
	 *
	 */
	@Override
	public void activate() {
		super.activate();
	}

	/**
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#activate()
	 *
	 */
	@Override
	public void deactivate() {
		super.activate();
	}

	/**
	 * Factors incoming requests into various specific methods.
	 *
	 * @see org.eclipse.gef.EditPolicy#getCommand(Request)
	 */
	@Override
	public Command getCommand(Request request) {
		if (RequestConstants.REQ_RESIZE_CHILDREN.equals(request.getType())) {
			CompositeCommand compositeCommand = new CompositeCommand("Resize Operands");
			ChangeBoundsRequest changeBoundsRequest = (ChangeBoundsRequest) request;
			List<?> editParts = changeBoundsRequest.getEditParts();
			// the user can resize only one InteractionOperand
			if (editParts.size() > 1) {
				return null;
			}

			TransactionalEditingDomain editingDomain = getEditingDomain();

			Object currentEditPart = editParts.get(0);
			updateCurrentChildSize(compositeCommand, changeBoundsRequest, editingDomain, currentEditPart);

			if (!compositeCommand.isEmpty() && (currentEditPart instanceof IGraphicalEditPart)
					&& compositeCommand.canExecute()) {

				EObject object = ((IGraphicalEditPart) currentEditPart).resolveSemanticElement();
				if (object instanceof InteractionOperand) {
					InteractionOperand operand = (InteractionOperand) object;
					// In case the containment of interaction fragments changes, validate
					AsyncValidateCommand.get(operand).ifPresent(compositeCommand::add);
				}
			}
			return new ICommandProxy(compositeCommand);
		}
		return null;
	}

	protected TransactionalEditingDomain getEditingDomain() {
		TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();
		return editingDomain;
	}

	private void updateCurrentChildSize(CompositeCommand compositeCommand, ChangeBoundsRequest changeBoundsRequest, TransactionalEditingDomain editingDomain, Object currentEditPart) {
		IGraphicalEditPart operandPart = (IGraphicalEditPart) currentEditPart;
		View shapeView = NotationHelper.findView(operandPart);
		// Dimension size = operandPart.getFigure().getSize();
		// Point location = operandPart.getFigure().getBounds().getLocation().getCopy();

		Dimension sizeDelta = changeBoundsRequest.getSizeDelta().getCopy();
		Point moveDelta = changeBoundsRequest.getMoveDelta().getCopy();

		// Take zoom into account; the request contains absolute mouse coordinates delta.
		IFigure operandFigure = operandPart.getFigure();
		PrecisionRectangle bounds = new PrecisionRectangle(operandFigure.getBounds());
		operandFigure.translateToAbsolute(bounds);
		bounds.resize(sizeDelta);
		bounds.translate(moveDelta);
		operandFigure.translateToRelative(bounds);

		// Set the new bounds, relative to the parent (CombinedFragment), to make
		// sure the notation is consistent with the visuals. We should get x = 0; y = Sum(sizeOf(previousOperands)); width = width(CF)
		IFigure cfFigure = ((IGraphicalEditPart) operandPart.getParent()).getFigure();
		bounds.translate(cfFigure.getBounds().getTopLeft().getNegated());

		ICommand setBoundsCommand = new SetResizeAndLocationCommand(editingDomain, "Resize Operands", new EObjectAdapter(shapeView), bounds);
		compositeCommand.add(setBoundsCommand);
	}

}
