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
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.LayoutEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.requests.EditCommandRequestWrapper;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyDependentsRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.util.ExecutionSpecificationUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;

/**
 * The XY Layout edit policy for the execution specification.
 */
public class CustomExecutionSpecificationXYLayoutEditPolicy extends LayoutEditPolicy {

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCommand(org.eclipse.gef.Request)
	 */
	@Override
	public Command getCommand(final Request request) {
		CompoundCommand subDeleteCommand = null;
		// Check if this is an edit command based on an execution specification edit part
		if (request instanceof EditCommandRequestWrapper && getHost() instanceof AbstractExecutionSpecificationEditPart) {
			final AbstractExecutionSpecificationEditPart hostEditPart = (AbstractExecutionSpecificationEditPart) getHost();

			// Check that this is a delete command, in this case, we have to recalculate the other execution specification positions
			final IEditCommandRequest editCommandRequest = ((EditCommandRequestWrapper) request).getEditCommandRequest();
			if (editCommandRequest instanceof DestroyElementRequest) {

				// Get the destroy dependents request parameter to get the edit parts modified
				final Object parameter = editCommandRequest.getParameter(DestroyElementRequest.DESTROY_DEPENDENTS_REQUEST_PARAMETER);
				if (null != parameter && parameter instanceof DestroyDependentsRequest) {
					final DestroyDependentsRequest destroyDependantRequestParameter = (DestroyDependentsRequest) parameter;

					// Get the modified edit parts
					for (final Object object : destroyDependantRequestParameter.getDependentElementsToDestroy()) {
						if (object == hostEditPart.getModel()) {
							final LifelineEditPart parentLifeLineEditPart = SequenceUtil.getParentLifelinePart(hostEditPart);
							if (null != parentLifeLineEditPart) {

								final Object view = hostEditPart.getModel();
								if (view instanceof Node) {
									final Bounds bounds = BoundForEditPart.getBounds((Node) view);
									final Rectangle initialRectangle = new Rectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());

									// This allows to check the graphical 'sub' execution specifications and move their bounds if necessary
									subDeleteCommand = ExecutionSpecificationUtil.getExecutionSpecificationToMove(parentLifeLineEditPart, initialRectangle, null, hostEditPart);
								}
							}
						}
					}
				}
			}
		}

		Command result = null;
		if (null != subDeleteCommand && !subDeleteCommand.isEmpty()) {
			final CompoundCommand cc = new CompoundCommand("Delete Execution Specification"); //$NON-NLS-1$
			cc.add(super.getCommand(request));
			cc.add(subDeleteCommand);
			result = cc;
		} else {
			result = super.getCommand(request);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#createChildEditPolicy(org.eclipse.gef.EditPart)
	 */
	@Override
	protected EditPolicy createChildEditPolicy(final EditPart child) {
		EditPolicy result = child.getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
		if (result == null) {
			result = new NonResizableEditPolicy();
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getMoveChildrenCommand(org.eclipse.gef.Request)
	 */
	@Override
	protected Command getMoveChildrenCommand(final Request request) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
	 */
	@Override
	protected Command getCreateCommand(final CreateRequest request) {
		return null;
	}
}
