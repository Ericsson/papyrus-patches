/*****************************************************************************
 * Copyright (c) 2017, 2018 CEA LIST, Christian W. Damus, and others.
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
 *   Christian W. Damus - bug 533679
 *   Vincent LORENZO - bug 541313 - [UML][CDO] UML calls to the method getCacheAdapter(EObject) must be replaced
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetResizeAndLocationCommand;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.BoundForEditPart;
import org.eclipse.uml2.common.util.CacheAdapter;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * This call allows to define needed methods for the exeuction specification objects.
 */
public class ExecutionSpecificationUtil {

	/** The default spacing used between Execution Specification */
	public static final int TOP_SPACING_HEIGHT = 5;

	/**
	 * Private constructor to avoid initialization.
	 */
	private ExecutionSpecificationUtil() {
		// Do nothing
	}

	/**
	 * This allows to calculate the correct location of the execution specification.
	 *
	 * @param lifeLineEditPart
	 *            The current life line edit part.
	 * @param initialRectangle
	 *            The initial rectangle of the execution specification.
	 * @param editPartToSkip
	 *            The edit part to don't modify.
	 * @return The rectangle of the execution specification
	 */
	public static Rectangle calculateExecutionSpecificationCorrectLocation(final LifelineEditPart lifeLineEditPart, final Rectangle initialRectangle, final EditPart editPartToSkip) {
		final Map<AbstractExecutionSpecificationEditPart, Rectangle> executionSpecificationRectangles = getRectangles(lifeLineEditPart);
		return calculateExecutionSpecificationCorrectLocation(lifeLineEditPart, executionSpecificationRectangles, initialRectangle, editPartToSkip);
	}

	/**
	 * This allows to calculate the correct location of the execution specification with the list of rectangles of the execution specification for the current life line.
	 *
	 * @param lifeLineEditPart
	 *            The current life line edit part.
	 * @param executionSpecificationRectangles
	 *            The list of rectangles of the execution specification for the current life line.
	 * @param initialRectangle
	 *            The initial rectangle of the execution specification.
	 * @param editPartToSkip
	 *            The edit part to don't modify.
	 * @returnThe rectangle of the execution specification
	 */
	public static Rectangle calculateExecutionSpecificationCorrectLocation(final LifelineEditPart lifeLineEditPart, final Map<AbstractExecutionSpecificationEditPart, Rectangle> executionSpecificationRectangles, final Rectangle initialRectangle,
			final EditPart editPartToSkip) {
		return calculateExecutionSpecificationCorrectLocation(lifeLineEditPart, executionSpecificationRectangles, initialRectangle, Collections.singleton(editPartToSkip));
	}

	/**
	 * This allows to calculate the correct location of the execution specification with the list of rectangles of the execution specification for the current life line.
	 *
	 * @param lifeLineEditPart
	 *            The current life line edit part.
	 * @param executionSpecificationRectangles
	 *            The list of rectangles of the execution specification for the current life line.
	 * @param initialRectangle
	 *            The initial rectangle of the execution specification.
	 * @param editPartsToSkip
	 *            The edit parts to don't modify.
	 * @returnThe rectangle of the execution specification
	 */
	public static Rectangle calculateExecutionSpecificationCorrectLocation(final LifelineEditPart lifeLineEditPart, final Map<AbstractExecutionSpecificationEditPart, Rectangle> executionSpecificationRectangles, final Rectangle initialRectangle,
			final Collection<EditPart> editPartsToSkip) {
		final Rectangle result = new Rectangle(getInitialXForExecutionSpecification(lifeLineEditPart, initialRectangle), initialRectangle.y, initialRectangle.width, initialRectangle.height);

		for (final Entry<AbstractExecutionSpecificationEditPart, Rectangle> entry : executionSpecificationRectangles.entrySet()) {
			final Rectangle currentRectangle = entry.getValue();
			if (editPartsToSkip == null || !editPartsToSkip.contains(entry.getKey())) {
				if (result.y > currentRectangle.y && result.y < (currentRectangle.y + currentRectangle.height)) {
					// The top spacing between execution specification must be respected
					if (result.y < currentRectangle.y + TOP_SPACING_HEIGHT) {
						result.y = currentRectangle.y + TOP_SPACING_HEIGHT;
					}

					// If the created execution specification is a sub execution specification, we need to represent it in the diagram
					// by moving it to the right place
					if (result.x < currentRectangle.x + (currentRectangle.width / 2)) {
						result.x = currentRectangle.x + (currentRectangle.width / 2);
					}
				}
			}
		}

		return result;
	}

	/**
	 * This allows to get a command to move needed execution specification when an execution specification is moved or deleted.
	 *
	 * @param lifeLineEditPart
	 *            The current life line edit part.
	 * @param initialRectangle
	 *            The initial rectangle of the execution specification.
	 * @param modifiedRectangle
	 *            The modified rectangle of the execution specification.
	 * @param editPartToSkip
	 *            The edit part to don't modify.
	 * @return The compound command containing the commands to move the others execution specifications that are needed.
	 */
	public static CompoundCommand getExecutionSpecificationToMove(final LifelineEditPart lifeLineEditPart, final Rectangle initialRectangle, final Rectangle modifiedRectangle, final EditPart editPartToSkip) {
		final Map<AbstractExecutionSpecificationEditPart, Rectangle> executionSpecificationRectangles = getRectangles(lifeLineEditPart);
		return getExecutionSpecificationToMove(lifeLineEditPart, executionSpecificationRectangles, initialRectangle, modifiedRectangle, editPartToSkip);
	}

	/**
	 * This allows to get a command to move needed execution specification with the list of rectangles of the execution specification for the current life line when an execution specification is moved or deleted.
	 *
	 * @param lifeLineEditPart
	 *            The current life line edit part.
	 * @param executionSpecificationRectangles
	 *            The list of rectangles of the execution specification for the current life line.
	 * @param initialRectangle
	 *            The initial rectangle of the execution specification.
	 * @param modifiedRectangle
	 *            The modified rectangle of the execution specification (it can be null for deleted element).
	 * @param editPartToSkip
	 *            The edit part to don't modify.
	 * @return The compound command containing the commands to move the others execution specifications that are needed.
	 */
	public static CompoundCommand getExecutionSpecificationToMove(final LifelineEditPart lifeLineEditPart, final Map<AbstractExecutionSpecificationEditPart, Rectangle> executionSpecificationRectangles, final Rectangle initialRectangle,
			final Rectangle modifiedRectangle, final EditPart editPartToSkip) {
		final CompoundCommand compoundCommand = new CompoundCommand("Edit Execution Specification positions"); //$NON-NLS-1$

		// Just copy the rectangle to allow comparison on the end of the process
		final Map<AbstractExecutionSpecificationEditPart, Rectangle> initialRectangles = copyRectangles(executionSpecificationRectangles);

		// Initialize the y start and end position modification and the minimal x to check
		int yBeginPosition = null != modifiedRectangle && initialRectangle.y > modifiedRectangle.y ? modifiedRectangle.y : initialRectangle.y;
		int yEndPosition = null != modifiedRectangle && (initialRectangle.y + initialRectangle.height) < (modifiedRectangle.y + modifiedRectangle.height) ? modifiedRectangle.y + modifiedRectangle.height : initialRectangle.y + initialRectangle.height;
		int xPosition = null != modifiedRectangle && initialRectangle.x > modifiedRectangle.x ? modifiedRectangle.x : initialRectangle.x;

		// For the modified edit part, modify the rectangle
		if (null != executionSpecificationRectangles.get(editPartToSkip)) {
			executionSpecificationRectangles.remove(editPartToSkip);
			if (null != modifiedRectangle) {
				executionSpecificationRectangles.put((AbstractExecutionSpecificationEditPart) editPartToSkip, modifiedRectangle);
			}
		}

		// Loop until a modification is done.
		boolean hasChange = true;
		while (hasChange) {
			hasChange = false;

			// Loop on execution specification rectangles
			final Iterator<Entry<AbstractExecutionSpecificationEditPart, Rectangle>> entries = executionSpecificationRectangles.entrySet().iterator();
			while (entries.hasNext() && !hasChange) {
				final Entry<AbstractExecutionSpecificationEditPart, Rectangle> currentEntry = entries.next();

				// Check this is not the modified edit part
				if (currentEntry.getKey() != editPartToSkip) {
					final Rectangle currentRectangle = currentEntry.getValue();

					// Check that the current rectangle is at the right of the modified rectangle and is between the y start and end position
					if (currentRectangle.x >= xPosition && currentRectangle.y >= yBeginPosition && currentRectangle.y <= yEndPosition) {

						final Rectangle calculatedRectangle = ExecutionSpecificationUtil.calculateExecutionSpecificationCorrectLocation(lifeLineEditPart, executionSpecificationRectangles, currentRectangle, currentEntry.getKey());
						if (!currentRectangle.equals(calculatedRectangle)) {
							// Need to modify rectangle of the current edit part
							currentRectangle.setBounds(calculatedRectangle);

							// If the current rectangle is moved, just re-actualize the possible y end (x and y start cannot be modified)
							if (yEndPosition < (calculatedRectangle.y + calculatedRectangle.height)) {
								yEndPosition = calculatedRectangle.y + calculatedRectangle.height;
							}

							hasChange = true;
						}
					}
				}
			}
		}

		// Loop on rectangles to determinate the modified rectangles
		final Iterator<Entry<AbstractExecutionSpecificationEditPart, Rectangle>> modifiedEditPartsEntries = executionSpecificationRectangles.entrySet().iterator();
		while (modifiedEditPartsEntries.hasNext()) {
			final Entry<AbstractExecutionSpecificationEditPart, Rectangle> modifiedEditPartsEntry = modifiedEditPartsEntries.next();

			if (modifiedEditPartsEntry.getKey() != editPartToSkip) {

				final Rectangle initialCurrentRectangle = initialRectangles.get(modifiedEditPartsEntry.getKey());
				final Rectangle modifiedCurrentRectangle = modifiedEditPartsEntry.getValue();

				// If the rectangle is modified, add the command to modify its bound
				if (!initialCurrentRectangle.equals(modifiedCurrentRectangle)) {
					compoundCommand.add(new ICommandProxy(new SetResizeAndLocationCommand(lifeLineEditPart.getEditingDomain(), "Change Execution Specification bounds", modifiedEditPartsEntry.getKey(), modifiedCurrentRectangle))); //$NON-NLS-1$
				}
			}
		}

		return compoundCommand;
	}

	/**
	 * This allows to get initial rectangles by execution specification available in the current life line.
	 *
	 * @param lifeLineEditPart
	 *            The current life line edit part.
	 * @return The map with the rectangle by execution specification edit part.
	 */
	public static Map<AbstractExecutionSpecificationEditPart, Rectangle> getRectangles(final LifelineEditPart lifeLineEditPart) {
		return getRectangles(lifeLineEditPart, null);
	}

	/**
	 * This allows to get initial rectangles by execution specification available in the current life line.
	 *
	 * @param lifeLineEditPart
	 *            The current life line edit part.
	 * @param editPartsToSkip
	 *            The collection of edit parts to skip during the calculation.
	 * @return The map with the rectangle by execution specification edit part.
	 */
	public static Map<AbstractExecutionSpecificationEditPart, Rectangle> getRectangles(final LifelineEditPart lifeLineEditPart, final Collection<EditPart> editPartsToSkip) {
		final Map<AbstractExecutionSpecificationEditPart, Rectangle> executionSpecificationRectangles = new HashMap<>();

		final Iterator<?> editPartChildren = lifeLineEditPart.getChildren().iterator();
		while (editPartChildren.hasNext()) {
			final Object childEditPart = editPartChildren.next();
			if (childEditPart instanceof AbstractExecutionSpecificationEditPart && (editPartsToSkip == null || !editPartsToSkip.contains(childEditPart))) {
				final Object view = ((AbstractExecutionSpecificationEditPart) childEditPart).getModel();
				if (view instanceof Node) {
					final Bounds bounds = BoundForEditPart.getBounds((Node) view);
					executionSpecificationRectangles.put((AbstractExecutionSpecificationEditPart) childEditPart, new Rectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));
				}
			}
		}

		return executionSpecificationRectangles;
	}

	/**
	 * This allows to copy the map of the rectangle with creation (to avoid rectangle modification by instance).
	 *
	 * @param rectangles
	 *            The initial map of rectangle by edit part.
	 * @return The copy of the initial map of rectangle by edit part with new rectangles.
	 */
	private static Map<AbstractExecutionSpecificationEditPart, Rectangle> copyRectangles(final Map<AbstractExecutionSpecificationEditPart, Rectangle> rectangles) {
		final Map<AbstractExecutionSpecificationEditPart, Rectangle> copiedRectangles = new HashMap<>();

		for (final Entry<AbstractExecutionSpecificationEditPart, Rectangle> entry : rectangles.entrySet()) {
			copiedRectangles.put(entry.getKey(), new Rectangle(entry.getValue()));
		}

		return copiedRectangles;
	}

	/**
	 * This allows to calculate the initial x of the execution specification for a life line.
	 *
	 * @param lifeLineEditPart
	 *            The life line edit part.
	 * @param executionSpecificationRectangle
	 *            The execution specification rectangle.
	 * @return The initial x of the execution specification for a life line.
	 */
	public static int getInitialXForExecutionSpecification(final LifelineEditPart lifeLineEditPart, final Rectangle executionSpecificationRectangle) {
		int result = 0;

		final View lifeLineView = (View) lifeLineEditPart.getModel();
		if (lifeLineView instanceof Node) {
			final int lifeLineWidth = BoundForEditPart.getWidthFromView((Node) lifeLineView);
			final int executionSpecificationWidth = null != executionSpecificationRectangle && -1 != executionSpecificationRectangle.width ? executionSpecificationRectangle.width : AbstractExecutionSpecificationEditPart.DEFAUT_WIDTH;
			result = (lifeLineWidth / 2) - (executionSpecificationWidth / 2);
		}

		return result;
	}

	/**
	 * Query the execution, if any, that is started by an {@code occurrence}.
	 *
	 * @param occurrence
	 *            an occurrence specification
	 * @return the execution specification that it starts
	 * @since 5.0
	 */
	public static Optional<ExecutionSpecification> getStartedExecution(OccurrenceSpecification occurrence) {
		CacheAdapter cache = CacheAdapter.getInstance();
		Predicate<EStructuralFeature.Setting> settingFilter = setting -> setting.getEStructuralFeature() == UMLPackage.Literals.EXECUTION_SPECIFICATION__START;

		return cache.getInverseReferences(occurrence).stream()
				.filter(settingFilter).findFirst()
				.map(EStructuralFeature.Setting::getEObject)
				.map(ExecutionSpecification.class::cast);
	}
}
