/*****************************************************************************
 * Copyright (c) 2016 - 2017 CEA LIST and others.
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
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 519408
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.figures;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.diagram.ui.figures.BorderedNodeFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.ScalableCompartmentFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.SelectableBorderedNodeFigure;
import org.eclipse.papyrus.uml.diagram.common.figure.node.AutomaticCompartmentLayoutManager;
import org.eclipse.papyrus.uml.diagram.sequence.figures.LifelineFigure.LifelineHeaderFigure;

/**
 * @since 3.0
 *        This class manage as {@link AutomaticCompartmentLayoutManager} but layout figure as XY layout if there are {@link ILifelineInternalFigure}
 */
public class LifeLineLayoutManager extends AutomaticCompartmentLayoutManager {
	private int bottomHeaderY = 0;
	/** The layout contraints */
	protected Map<IFigure, Rectangle> constraints = new HashMap<>();

	public int getBottomHeader() {
		return bottomHeaderY;
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.AutomaticCompartmentLayoutManager#layout(org.eclipse.draw2d.IFigure)
	 *
	 * @param container
	 */
	@Override
	public void layout(IFigure container) {
		super.layout(container);
		layoutXYFigure(container);
	}

	/*
	 * Fills the given bound data as a non-compartment child
	 *
	 * @param container
	 * The container to layout
	 *
	 * @param bound
	 * The bound to fill
	 *
	 * @param previous
	 * The previously filled bound
	 */
	@Override
	protected void fillBoundsForOther(IFigure container, Rectangle bound, Rectangle previous) {
		bound.x = container.getBounds().x + 1; // +1, see bug 490318, restore +1 to fix shift from Papyrus Luna to Papyrus Mars
		bound.width = container.getBounds().width;
		if (previous == null) {
			bound.y = container.getBounds().y + 3;
		} else {
			bound.y = previous.getBottomLeft().y + 1;
		}
	}

	/**
	 * @see org.eclipse.draw2d.AbstractLayout#setConstraint(org.eclipse.draw2d.IFigure, java.lang.Object)
	 *
	 * @param child
	 * @param constraint
	 */
	@Override
	public void setConstraint(IFigure child, Object constraint) {
		if (child instanceof SelectableBorderedNodeFigure) {
			if (((SelectableBorderedNodeFigure) child).getMainFigure() instanceof ILifelineInternalFigure) {
				if (constraint instanceof Rectangle) {
					constraints.put(child, (Rectangle) constraint);
				}
			}
		}
		super.setConstraint(child, constraint);
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.AutomaticCompartmentLayoutManager#fillBoundsForCompartment(org.eclipse.draw2d.IFigure, org.eclipse.draw2d.geometry.Rectangle, org.eclipse.draw2d.geometry.Rectangle, double)
	 *
	 * @param container
	 * @param bound
	 * @param previous
	 * @param ratio
	 */
	@Override
	protected void fillBoundsForCompartment(IFigure container, Rectangle bound, Rectangle previous, double ratio) {
		fillBoundsForOther(container, bound, previous);
		// bound.height = (int) (bound.height / ratio);
		if (previous == null) {
			bound.y = container.getBounds().y;
		}
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.AutomaticCompartmentLayoutManager#layoutDefault(org.eclipse.draw2d.IFigure)
	 *
	 * @param container
	 */
	@Override
	protected void layoutDefault(IFigure container) {
		ScalableCompartmentFigure symbolFigure = null;
		int i = 0;
		while (symbolFigure == null && i < container.getChildren().size()) {
			if (container.getChildren().get(i) instanceof ScalableCompartmentFigure) {
				symbolFigure = (ScalableCompartmentFigure) container.getChildren().get(i);
			}
			i++;
		}
		super.layoutDefault(container);

		// change coordinate to set the symbol at the top
		if (symbolFigure != null) {
			symbolFigure.getBounds().setY(container.getBounds().getTop().y);
		}
		Rectangle containerBounds = container.getBounds();
		IFigure previous = null;
		for (IFigure child : visibleOthers) {
			Rectangle bound = new Rectangle();
			if (previous != null) {
				if (child.equals(symbolFigure)) {
					bound.y = containerBounds.y + 3;
				} else {
					bound.y = previous.getBounds().getBottomLeft().y + 1;
					bound.x = containerBounds.x + 1;
					bound.width = containerBounds.width;
					bound.height = child.getBounds().height;
					bottomHeaderY = bound.y + bound.height;
				}
			} else {
				bound.x = containerBounds.x + 1;
				// here the symbo may be present
				if (symbolFigure != null) {
					bound.y = containerBounds.y + 3 + symbolFigure.getBounds().height;
				} else {
					bound.y = containerBounds.y + 3;
				}

				bound.width = containerBounds.width;
				bound.height = child.getBounds().height;
			}
			child.setBounds(bound);
			previous = child;
		}


	}


	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.AutomaticCompartmentLayoutManager#layoutOthers(org.eclipse.draw2d.geometry.Rectangle)
	 *
	 * @param container
	 */
	@Override
	protected void layoutOthers(Rectangle container) {
		super.layoutOthers(container);

		IFigure previous = null;
		for (IFigure child : visibleOthers) {
			Rectangle bound = new Rectangle();
			if (previous != null) {
				bound.y = previous.getBounds().getBottomLeft().y + 1;
				bound.x = container.x + 1;
				bound.width = container.width;
				bound.height = child.getBounds().height;
				bottomHeaderY = bound.y + bound.height;
			} else {
				bound.x = container.x + 1;
				// in the case where the content is grater than the container
				// it is forbidden to change the y coordinate
				bound.y = container.y + 3;
				bound.width = container.width;
				bound.height = child.getBounds().height;
				bottomHeaderY = bound.y + bound.height;
			}
			child.setBounds(bound);
			previous = child;
		}
	}


	/**
	 * all {@link ILifelineInternalFigure} must be managed as XY layout by respecting their position and size
	 *
	 * @param container
	 */
	protected void layoutXYFigure(IFigure container) {
		for (Object child : container.getChildren()) {
			if (child instanceof BorderedNodeFigure) {
				if (((BorderedNodeFigure) child).getMainFigure() instanceof ILifelineInternalFigure) {
					Rectangle theConstraint = constraints.get(child).getCopy();
					if (theConstraint != null) {
						theConstraint.translate(container.getBounds().getTopLeft());
						((BorderedNodeFigure) child).setBounds(theConstraint);
					}
				}
			} else if (child instanceof LifelineHeaderFigure) {
				((LifelineHeaderFigure) child).setBounds(container.getBounds().getCopy().setHeight(bottomHeaderY - container.getBounds().y + 1));
			}
		}
	}
}