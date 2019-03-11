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
 *   EclipseSource - Bugs 533770, 533678
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.CommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest.ViewDescriptor;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.adapter.NotationAndTypeAdapter;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultCreationEditPolicy;
import org.eclipse.papyrus.infra.services.edit.utils.RequestParameterConstants;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetResizeAndLocationCommand;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetResizeCommand;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionOperandEditPart;

/**
 * @author Patrick Tessier
 * @since 3.0
 *        This class is used to set location and dimension for the InteractionOperand
 *
 */
public class CombinedCreationEditPolicy extends DefaultCreationEditPolicy {

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The {@link CombinedCreationEditPolicy} also takes the {@link RequestParameterConstants#INSERT_AT}
	 * parameter into account, if present, to allow creating new operands in the middle of the CF's operands
	 * list.
	 * </p>
	 *
	 * @param request
	 * @return
	 */
	@Override
	protected Command getCreateCommand(CreateViewRequest request) {
		Command createCommand = super.getCreateCommand(request);
		if (request.getExtendedData().get(RequestParameterConstants.INSERT_AT) instanceof Integer) {
			int insertAt = (Integer) request.getExtendedData().get(RequestParameterConstants.INSERT_AT);
			if (insertAt >= 0) {
				// The view descriptor has an index, but we can't rely on it: the descriptor is created directly
				// by the palette tool, in a very generic layer, and always uses -1 as the index.
				GraphicalEditPart graphicalHost = (GraphicalEditPart) getHost();
				ICommand insertAtCommand = new AbstractTransactionalCommand(graphicalHost.getEditingDomain(), "Insert view at " + insertAt, null) {

					@Override
					protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
						View container = graphicalHost.getNotationView();
						EList<View> children = container.getPersistedChildren();
						List<? extends ViewDescriptor> viewDescriptors = request.getViewDescriptors();
						List<ViewDescriptor> reversedDescriptors = new ArrayList<>(viewDescriptors);
						Collections.reverse(reversedDescriptors);
						for (ViewDescriptor descriptor : reversedDescriptors) {
							children.move(insertAt, (View) descriptor.getAdapter(View.class));
						}
						return CommandResult.newOKCommandResult();
					}
				};
				return new ICommandProxy(createCommand == null ? insertAtCommand : new CommandProxy(createCommand).compose(insertAtCommand).reduce());
			}
		}
		return createCommand;
	}

	/**
	 * <p>
	 * Adds a command to properly update the CF's operand(s) sizes. This policy will
	 * make sure the new Operand is created "under the mouse cursor", shrinking
	 * the existing Operand under the mouse cursor if necessary.
	 * </p>
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultCreationEditPolicy#getSetBoundsCommand(org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest, org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest.ViewDescriptor)
	 *
	 * @param request
	 * @param descriptor
	 * @return
	 */
	@Override
	protected ICommand getSetBoundsCommand(CreateViewRequest request, ViewDescriptor descriptor) {
		TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();
		GraphicalEditPart compartmentEditPart = (GraphicalEditPart) getHost();

		IFigure compartmentFigure = compartmentEditPart.getFigure();
		Point locationToViewer = request.getLocation().getCopy(); // Relative to visible viewer area (Changes when scroll/zoom is applied).

		Point locationToCompartment = locationToViewer.getCopy(); // Transform to diagram coordinates (Relative to the compartment).
		compartmentFigure.translateToRelative(locationToCompartment);
		compartmentFigure.translateFromParent(locationToCompartment);

		final GraphicalEditPart targetOperandPart = findOperandAt(locationToCompartment, compartmentEditPart);

		// TODO Support feedback (From mouse location to the top of the next operand, or to the bottom of the CF)

		if (targetOperandPart != null) {

			final IFigure targetOperandFigure = targetOperandPart.getFigure();
			Rectangle targetOperandBounds = targetOperandFigure.getBounds();

			Point locationToOperand = locationToViewer.getCopy();
			targetOperandFigure.translateToRelative(locationToOperand);
			targetOperandFigure.translateFromParent(locationToOperand);

			// We get the size from the mouse cursor location to the bottom of the existing operand
			int height = targetOperandBounds.getBottom().y() - locationToOperand.y();
			int width = compartmentFigure.getBounds().width();

			int distanceToCompartmentTop = compartmentFigure.getBounds().getTopLeft().getNegated().translate(locationToCompartment).y;
			Rectangle bounds = new Rectangle(0, distanceToCompartmentTop, width, height);
			ICommand setBoundsCommand = new SetResizeAndLocationCommand(editingDomain, "Set dimension", descriptor, bounds);

			// Also reduce the size of the existing operand, to avoid shifting the entire operands stack
			View view = targetOperandPart.getNotationView();

			int siblingHeight = targetOperandPart.getFigure().getBounds().height();

			Dimension siblingDimension = new Dimension(width, siblingHeight - height);
			ICommand reduceSiblingSizeCommand = new SetResizeCommand(editingDomain, "Set dimension", new NotationAndTypeAdapter(view.getElement(), view), siblingDimension);
			return setBoundsCommand.compose(reduceSiblingSizeCommand);
		}

		// Shouldn't happen in a well-formed diagram, since a CF should always have at least one operand.
		// If this happens, simply take all available size
		Rectangle clientArea = compartmentFigure.getClientArea();
		Dimension size = new Dimension(clientArea.getSize());
		ICommand setBoundsCommand = new SetResizeCommand(editingDomain, "Set dimension", descriptor, size);
		return setBoundsCommand;
	}

	private static InteractionOperandEditPart findOperandAt(Point locationToCompartment, GraphicalEditPart compartmentPart) {
		final EditPart targetPart = findEditPartAt(locationToCompartment, compartmentPart);

		final InteractionOperandEditPart operandEditPart;
		if (targetPart instanceof InteractionOperandEditPart) {
			operandEditPart = (InteractionOperandEditPart) targetPart;
		} else if (targetPart != null) {
			operandEditPart = findParentOperandPart(targetPart);
		} else {
			operandEditPart = null;
		}
		return operandEditPart;
	}

	private static EditPart findEditPartAt(Point locationToCompartment, GraphicalEditPart compartmentPart) {
		IFigure targetOperandFigure = compartmentPart.getFigure().findFigureAt(locationToCompartment);
		final EditPart targetPart;
		if (targetOperandFigure == null) {
			targetPart = null;
		} else {
			EditPart partForFigure = null;
			IFigure currentFigure = targetOperandFigure;
			while (currentFigure != null) {
				partForFigure = (EditPart) compartmentPart.getViewer().getVisualPartMap().get(currentFigure);
				if (partForFigure != null) {
					break;
				}
				currentFigure = currentFigure.getParent();
			}
			targetPart = partForFigure;
		}
		return targetPart;
	}

	private static InteractionOperandEditPart findParentOperandPart(final EditPart part) {
		EditPart currentPart = part.getParent();
		while (currentPart != null) {
			if (currentPart instanceof InteractionOperandEditPart) {
				return (InteractionOperandEditPart) currentPart;
			}
			currentPart = currentPart.getParent();
		}
		return null;
	}

}
