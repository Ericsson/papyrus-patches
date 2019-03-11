/*****************************************************************************
 * Copyright (c) 2013 CEA
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
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.ExposeHelper;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.papyrus.commands.wrappers.GEFtoEMFCommandWrapper;
import org.eclipse.papyrus.infra.emf.gmf.command.GMFtoEMFCommandWrapper;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetResizeAndLocationCommand;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentCombinedFragmentCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionOperandEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.figures.StereotypeInteractionFigure;
import org.eclipse.papyrus.uml.diagram.sequence.util.CommandHelper;
import org.eclipse.papyrus.uml.diagram.sequence.util.OperandBoundsComputeHelper;


/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CombinedFragmentHeadImpactLayoutEditPolicy extends AbstractHeadImpactLayoutEditPolicy {

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.policies.AbstractHeadImpactLayoutEditPolicy#getHeadHeight()
	 *
	 * @return
	 */

	@Override
	protected int getHeadHeight() {
		IFigure primaryShape = getPrimaryShape();
		if (primaryShape instanceof StereotypeInteractionFigure) {
			IFigure headContainer = ((StereotypeInteractionFigure) primaryShape).getNameLabel().getParent();
			Rectangle boundsRect = getBoundsRect();
			return headContainer.getPreferredSize(boundsRect.width, -1).height;
		}
		return 0;
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.policies.AbstractHeadImpactLayoutEditPolicy#doImpactLayout(int)
	 *
	 * @param resizeDelta
	 */

	@Override
	protected void doImpactLayout(int resizeDelta) {
		CompoundCommand commands = new CompoundCommand();
		CombinedFragmentEditPart host = (CombinedFragmentEditPart) getHost();
		// 1. resize the first operand.
		CombinedFragmentCombinedFragmentCompartmentEditPart compartment = (CombinedFragmentCombinedFragmentCompartmentEditPart) host.getPrimaryChildEditPart();
		List children = compartment.getChildren();
		if (!children.isEmpty()) {
			Object child = children.get(0);
			if (child instanceof InteractionOperandEditPart) {
				InteractionOperandEditPart operand = (InteractionOperandEditPart) child;
				Node shape = (Node) operand.getNotationView();
				Bounds bounds = (Bounds) shape.getLayoutConstraint();
				Dimension size = new Dimension(bounds.getWidth(), bounds.getHeight()).expand(0, -resizeDelta);
				Rectangle newBounds = new Rectangle(new Point(bounds.getX(), bounds.getY() + resizeDelta), size);
				commands.appendIfCanExecute(new GMFtoEMFCommandWrapper(new SetResizeAndLocationCommand(getEditingDomain(), "", operand, newBounds)));
				Command cmd = OperandBoundsComputeHelper.getShiftEnclosedFragmentsCommand(operand, newBounds, resizeDelta);
				if (cmd != null) {
					commands.appendIfCanExecute(new GEFtoEMFCommandWrapper(cmd));
				}
			}
		}
		if (!commands.isEmpty() && commands.canExecute()) {
			CommandHelper.executeCommandWithoutHistory(getEditingDomain(), commands, true);
		}
		for (Object object : children) {
			EditPart part = (EditPart) object;
			EditPart current = part.getParent();
			while (current != null) {
				ExposeHelper helper = current
						.getAdapter(ExposeHelper.class);
				if (helper != null) {
					helper.exposeDescendant(part);
				}
				current = current.getParent();
			}
		}
	}
}
