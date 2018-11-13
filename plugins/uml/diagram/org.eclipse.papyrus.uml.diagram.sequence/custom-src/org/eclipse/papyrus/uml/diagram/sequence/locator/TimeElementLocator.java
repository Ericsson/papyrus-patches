/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, Christian W. Damus, and others.
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
 *   Christian W. Damus - bug 536486
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.locator;

import java.util.Optional;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.figures.IBorderItemLocator;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.papyrus.uml.diagram.sequence.figures.DestructionEventNodePlate;
import org.eclipse.papyrus.uml.diagram.sequence.figures.LifelineFigure;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.TimeConstraint;
import org.eclipse.uml2.uml.TimeObservation;

/**
 * Specific locator for {@link TimeObservation} or {@link TimeConstraint} on exec spec.
 */
public class TimeElementLocator implements IBorderItemLocator {

	private final IBorderItemLocator centeringDelegate;

	private IFigure parentFigure;
	private Rectangle constraint;
	private ToIntFunction<? super Rectangle> sideFunction;

	/**
	 * Initializes me with my parent figure and a function that computes the side on
	 * which to place the time element based on a proposed locating rectangle.
	 *
	 * @param parentFigure
	 *            the parent figure
	 * @param sideFunction
	 *            the proposed rectangle to side function. Valid outputs of the
	 *            side function are the NSEW and {@link PositionConstants#CENTER CENTER}
	 *            values of the {@link PositionConstants}
	 */
	public TimeElementLocator(IFigure parentFigure, ToIntFunction<? super Rectangle> sideFunction) {
		super();

		this.setParentFigure(parentFigure);
		this.sideFunction = sideFunction;

		this.centeringDelegate = new CenterLocator(parentFigure, PositionConstants.NONE);
	}

	@Override
	public void setConstraint(Rectangle constraint) {
		this.constraint = constraint;
	}

	private Rectangle getConstraint() {
		return constraint;
	}

	@Override
	public Rectangle getValidLocation(Rectangle proposedLocation, IFigure borderItem) {
		Rectangle realLocation = new Rectangle(proposedLocation);
		Point newTopLeft = locateOnBorder(realLocation, borderItem);
		realLocation.setLocation(newTopLeft);
		return realLocation;
	}

	@Override
	public int getCurrentSideOfParent() {
		if (getConstraint().y() >= 10) {
			return PositionConstants.SOUTH;
		}
		return PositionConstants.NORTH;
	}

	@Override
	public void relocate(IFigure borderItem) {
		Dimension size = getSize(borderItem);
		Rectangle rectSuggested = new Rectangle(
				getPreferredLocation(borderItem), size);

		// Point ptNewLocation = locateOnBorder(rectSuggested, borderItem);
		borderItem.setBounds(rectSuggested);
	}

	protected Point locateOnBorder(Rectangle rectSuggested, IFigure borderItem) {
		Point relativeItem = rectSuggested.getTopLeft();
		Rectangle parentBounds = getParentFigure().getBounds();
		Rectangle itemBounds = borderItem.getBounds();

		// On which side?
		int side = sideFunction.applyAsInt(rectSuggested);
		switch (side) {
		case PositionConstants.WEST: // Applied on the lifeline head
			relativeItem.setLocation(-(itemBounds.width() / 2),
					getLifelineHead().height() / 2);
			break;
		case PositionConstants.EAST: // Applied on the lifeline head
			relativeItem.setLocation(parentBounds.width() - (itemBounds.width() / 2),
					getLifelineHead().height() / 2);
			break;
		case PositionConstants.NORTH: // Applied on an execution specification
			relativeItem.setLocation((parentBounds.width() - rectSuggested.width()) / 2, 0);
			break;
		case PositionConstants.SOUTH: // Applied on an execution specification
			relativeItem.setLocation((parentBounds.width() - rectSuggested.width()) / 2,
					parentBounds.height());
			break;
		case PositionConstants.CENTER: // Applied on a destruction occurrence and lifeline
			if (isOnDestructionOccurrence()) {
				// Center vertically, also
				relativeItem.setLocation((parentBounds.width() - itemBounds.width()) / 2,
						parentBounds.height() / 2);
			} else {
				return centeringDelegate.getValidLocation(rectSuggested, borderItem)
						.getLocation();
			}
			break;
		default:
			throw new IllegalArgumentException("unsupported side: " + side);
		}

		Point result = getAbsoluteToBorder(relativeItem);
		return result;
	}

	protected boolean isOnDestructionOccurrence() {
		return getParentFigure() instanceof DestructionEventNodePlate;
	}

	protected Rectangle getLifelineHead() {
		// The first (and only) child of the lifeline node plate is the lifeline figure
		LifelineFigure lifeline = (LifelineFigure) getParentFigure().getChildren().get(0);
		return lifeline.getHeaderFigure().getBounds();
	}

	protected IFigure getParentFigure() {
		return parentFigure;
	}

	protected void setParentFigure(IFigure parentFigure) {
		this.parentFigure = parentFigure;
	}

	/**
	 * Gets the size of the border item figure.
	 *
	 * @param borderItem
	 *            the figure on border
	 * @return the size of the border item figure.
	 */
	protected final Dimension getSize(IFigure borderItem) {
		Dimension size = getConstraint().getSize();
		if (size.isEmpty()) {
			size = borderItem.getPreferredSize();
		}
		return size;
	}

	protected Point getPreferredLocation(IFigure borderItem) {
		Point constraintLocation = locateOnBorder(getConstraint(), borderItem);
		return constraintLocation;
	}


	/**
	 * Convert the relative coords in the model to ones that are Relative to the
	 * container (absolute in respect to the main figure)
	 *
	 * @param ptRelativeOffset
	 * @return point
	 */
	protected Point getAbsoluteToBorder(Point ptRelativeOffset) {
		Point parentOrigin = getParentBorder().getTopLeft();
		return parentOrigin.translate(ptRelativeOffset);
	}

	/**
	 * Utility to calculate the parent bounds with consideration for the handle
	 * bounds inset.
	 *
	 * @return <code>Rectangle</code> that is the bounds of the parent.
	 */
	protected Rectangle getParentBorder() {
		Rectangle bounds = getParentFigure().getBounds().getCopy();
		if (getParentFigure() instanceof NodeFigure) {
			bounds = ((NodeFigure) getParentFigure()).getHandleBounds()
					.getCopy();
		}
		return bounds;
	}

	public static <T extends NamedElement> Optional<T> getTimedElement(EditPart timeElementEP, Class<T> type) {
		return Optional.ofNullable(timeElementEP.getAdapter(EObject.class))
				.filter(NamedElement.class::isInstance).map(NamedElement.class::cast)
				.flatMap(named -> getTimedElement(named, type));
	}

	public static <T extends NamedElement> Optional<T> getTimedElement(NamedElement timeElement, Class<T> type) {
		Stream<T> timed = Stream.empty();

		if (timeElement instanceof TimeConstraint) {
			TimeConstraint constraint = (TimeConstraint) timeElement;
			timed = constraint.getConstrainedElements().stream().filter(type::isInstance).map(type::cast);
		} else if (timeElement instanceof TimeObservation) {
			TimeObservation observation = (TimeObservation) timeElement;
			timed = Stream.of(observation.getEvent()).filter(type::isInstance).map(type::cast);
		}

		return timed.findFirst();
	}

}
