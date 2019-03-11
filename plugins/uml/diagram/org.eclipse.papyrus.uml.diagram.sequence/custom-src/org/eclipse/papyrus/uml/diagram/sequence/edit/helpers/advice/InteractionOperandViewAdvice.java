/*****************************************************************************
 * Copyright (c) 2018 EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.helpers.advice;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.emf.type.core.edithelper.AbstractEditHelperAdvice;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyDependentsRequest;
import org.eclipse.gmf.runtime.notation.LayoutConstraint;
import org.eclipse.gmf.runtime.notation.Location;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.Size;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.adapter.NotationAndTypeAdapter;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetResizeAndLocationCommand;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetResizeCommand;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionOperandEditPart;
import org.eclipse.uml2.uml.InteractionOperand;

/**
 * <p>
 * Advice for {@link InteractionOperand} View (InteractionOperand_Shape), responsible
 * for resize sibling operands during deletion.
 * </p>
 * <p>
 * This advice must be bound before the default GMF's notationDepdendents advice,
 * to make sure we can propagate the size of the deleted view before it is actually
 * deleted.
 * </p>
 */
public class InteractionOperandViewAdvice extends AbstractEditHelperAdvice {

	@Override
	protected ICommand getBeforeDestroyDependentsCommand(DestroyDependentsRequest request) {
		ICommand beforeDestroyDependentsCommand = super.getBeforeDestroyDependentsCommand(request);

		if (request.getElementToDestroy() instanceof InteractionOperand) {
			Set<View> operandViews = findOperandViews((InteractionOperand) request.getElementToDestroy());
			return CompositeCommand.compose(beforeDestroyDependentsCommand,
					operandViews.stream()
							.map(view -> getSizeCommandFor(view, request.getEditingDomain()))
							.reduce(null, CompositeCommand::compose));
		}

		return beforeDestroyDependentsCommand;
	}

	private Set<View> findOperandViews(InteractionOperand operand) {
		return EMFHelper.getUsages(operand).stream()
				.filter(setting -> setting.getEStructuralFeature() == NotationPackage.Literals.VIEW__ELEMENT)
				.map(Setting::getEObject)
				.filter(View.class::isInstance)
				.map(View.class::cast)
				.filter(view -> view.getElement() == operand)
				.filter(view -> InteractionOperandEditPart.VISUAL_ID.equals(view.getType()))
				.collect(Collectors.toSet());
	}

	/**
	 * Return the command used to propagate the size of the given <code>viewToDestroy</code>
	 * to one of the sibling operands (The previous one, or the next one if the destroyed view
	 * if the first in the CF).
	 *
	 * The actual resize commands are created on-the-fly during command execution, so
	 * the commands can be properly chained in case of multi-deletion.
	 *
	 * @param viewToDestroy
	 * @param editingDomain
	 * @return
	 */
	private ICommand getSizeCommandFor(View viewToDestroy, TransactionalEditingDomain editingDomain) {
		// Do the actual work in the command (At execution time), so we can propagate size changes
		// in case of multi-selection (So when deleting A, B and C, A will resize B, then B will resize C, then C will resize D)
		return new AbstractTransactionalCommand(editingDomain, "Resize sibling operand", null) {

			@Override
			protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				Dimension deletedSize = getSize(viewToDestroy);
				if (deletedSize == null) {
					return null;
				}
				View viewBefore = findViewBefore(viewToDestroy);
				int height = deletedSize.height();
				if (viewBefore != null) {
					Dimension sizeOfViewBefore = getSize(viewBefore);
					sizeOfViewBefore.expand(0, height);
					NotationAndTypeAdapter adapter = new NotationAndTypeAdapter(viewBefore.getElement(), viewBefore);
					ICommand result = new SetResizeCommand(editingDomain, "Expand previous operand", adapter, sizeOfViewBefore);
					result.execute(monitor, info);
				} else {
					View viewAfter = findViewAfter(viewToDestroy);
					if (viewAfter != null) {
						Rectangle boundsOfViewAfter = getBounds(viewAfter);
						boundsOfViewAfter.height += height; // Do not use expand, because it would also translate the rectangle
						boundsOfViewAfter.y -= height; // Shift the following operand up (Used by the Grid policy to compute coverage)
						NotationAndTypeAdapter adapter = new NotationAndTypeAdapter(viewAfter.getElement(), viewAfter);
						ICommand result = new SetResizeAndLocationCommand(editingDomain, "Expand previous operand", adapter, boundsOfViewAfter);
						result.execute(monitor, info);
					}
				}
				return CommandResult.newOKCommandResult();
			}
		};

	}

	/**
	 * @param view
	 * @return
	 * 		The sibling view immediately following the given view, or <code>null</code> if there is no such view
	 */
	private static View findViewAfter(View view) {
		if (false == view.eContainer() instanceof View) {
			return null;
		}

		@SuppressWarnings("unchecked") // GMF is Java 1.4
		List<View> viewSiblings = ((View) view.eContainer()).getPersistedChildren();

		int index = viewSiblings.indexOf(view);
		int indexAfter = index + 1;
		if (indexAfter < viewSiblings.size()) {
			return viewSiblings.get(indexAfter);
		}
		return null;
	}

	/**
	 * @param view
	 * @return
	 * 		The sibling view immediately preceding the given view, or <code>null</code> if there is no such view
	 */
	private static View findViewBefore(View view) {
		if (false == view.eContainer() instanceof View) {
			return null;
		}

		@SuppressWarnings("unchecked") // GMF is Java 1.4
		List<View> viewSiblings = ((View) view.eContainer()).getPersistedChildren();

		int index = viewSiblings.indexOf(view) - 1;

		if (index >= 0) {
			return viewSiblings.get(index);
		}
		return null;
	}

	/**
	 * @param view
	 * @return
	 * 		A new {@link Rectangle} instance representing the model bounds of the {@link View},
	 *         or <code>null</code> if the View doesn't have a size.
	 */
	private Rectangle getBounds(View view) {
		if (view instanceof Node) {
			Node node = (Node) view;
			LayoutConstraint constraint = node.getLayoutConstraint();
			Rectangle bounds = new Rectangle();
			if (constraint instanceof Size) {
				Size size = (Size) constraint;
				bounds.setSize(size.getWidth(), size.getHeight());
			}
			if (constraint instanceof Location) {
				Location location = (Location) constraint;
				bounds.setLocation(location.getX(), location.getY());
			}
			return bounds;
		}
		return null;
	}



	private Dimension getSize(View viewToDestroy) {
		Rectangle bounds = getBounds(viewToDestroy);
		return bounds == null ? null : bounds.getSize();
	}
}
