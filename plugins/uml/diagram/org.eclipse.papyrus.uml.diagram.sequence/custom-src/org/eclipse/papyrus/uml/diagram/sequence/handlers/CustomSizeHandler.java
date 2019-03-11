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
package org.eclipse.papyrus.uml.diagram.sequence.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.papyrus.uml.diagram.menu.actions.SizeAction;
import org.eclipse.papyrus.uml.diagram.menu.actions.handlers.SizeHandler;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomSizeHandler extends SizeHandler {

	/**
	 * @see org.eclipse.papyrus.uml.diagram.menu.actions.handlers.SizeHandler#getCommand()
	 *
	 * @return
	 * @throws ExecutionException
	 */
	@Override
	protected Command getCommand() {
		return new CustomSizeAction(this.parameter, getSelectedElements()).getCommand();
	}

	protected static Rectangle getLifelineConstraint(List<IGraphicalEditPart> selectedElements) {
		Rectangle constraint = new Rectangle();
		for (int i = 0; i < selectedElements.size(); i++) {
			LifelineEditPart lifelineEditPart = (LifelineEditPart) selectedElements.get(i);
			Rectangle rect = SequenceUtil.getAbsoluteBounds(lifelineEditPart);
			constraint.union(rect);
		}
		return constraint;
	}

	protected boolean isLifelines(List<IGraphicalEditPart> selectedElements) {
		if (selectedElements.isEmpty()) {
			return true;
		}
		boolean isLifelines = true;
		for (int i = 0; i < selectedElements.size(); i++) {
			isLifelines &= selectedElements.get(i) instanceof LifelineEditPart;
		}
		return isLifelines;
	}

	private static Dimension correctDelta(IGraphicalEditPart toResize, Dimension delta, Rectangle constraint) {
		// Align all Lifelines at bottom.
		Rectangle previousRect = SequenceUtil.getAbsoluteBounds(toResize);
		return new Dimension(delta.width, constraint.bottom() - previousRect.bottom());
	}

	public class CustomSizeAction extends SizeAction {

		/**
		 * Constructor.
		 *
		 * @param parameter
		 * @param selectedElements
		 */
		public CustomSizeAction(String parameter, List<IGraphicalEditPart> selectedElements) {
			super(parameter, selectedElements);
		}

		@Override
		protected Command getBothCommand() {
			if (isLifelines(selectedElements)) {
				return new LifeLinedSameHeightAction(selectedElements).getCommand();
			}
			return super.getBothCommand();
		}

		@Override
		protected Command getHeightCommand() {
			if (isLifelines(selectedElements)) {
				return new LifeLinedSameHeightAction(selectedElements).getCommand();
			}
			return super.getHeightCommand();
		}

		protected class LifeLinedSameBothSizeAction extends SameBothSizeAction {

			private final Rectangle lifelinesContext;

			public LifeLinedSameBothSizeAction(List<IGraphicalEditPart> selectedElements) {
				super(selectedElements);
				lifelinesContext = getLifelineConstraint(selectedElements);
			}

			@Override
			protected Dimension calculateDelta(IGraphicalEditPart toResize, Dimension previousSize) {
				Dimension delta = super.calculateDelta(toResize, previousSize);

				return correctDelta(toResize, delta, lifelinesContext);
			}
		}

		protected class LifeLinedSameHeightAction extends SameHeightSizeAction {

			private final Rectangle lifelinesContext;

			public LifeLinedSameHeightAction(List<IGraphicalEditPart> selectedElements) {
				super(selectedElements);
				lifelinesContext = getLifelineConstraint(selectedElements);
			}

			@Override
			protected Dimension calculateDelta(IGraphicalEditPart toResize, Dimension newSize) {
				Dimension delta = super.calculateDelta(toResize, newSize);

				return correctDelta(toResize, delta, lifelinesContext);
			}
		}
	}
}
