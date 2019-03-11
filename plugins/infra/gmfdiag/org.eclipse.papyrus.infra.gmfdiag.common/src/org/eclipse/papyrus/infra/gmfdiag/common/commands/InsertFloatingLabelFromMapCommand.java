/*****************************************************************************
 * Copyright (c) 2010, 2015 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and Implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.commands;

import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.command.AbstractCommand;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.notation.DecorationNode;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Location;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.View;

/**
 * The Class InsertFloatingLabelFromMapCommand. Used with reconciler to insert to views's diagram corresponding to the key a child corresponding to the value.
 * If a child having the same type already exist, it is not added.
 */
public class InsertFloatingLabelFromMapCommand extends AbstractCommand {


	/** The diagram. */
	private Diagram diagram;

	/** The floating labels map. */
	private Map<String, String> floatingLabelsMap;

	/**
	 * Instantiates a new InsertFloatingLabelFromMapCommand command.
	 *
	 * @param diagram
	 *            the diagram
	 * @param floatingLabelMap
	 *            the floating label map
	 */
	public InsertFloatingLabelFromMapCommand(Diagram diagram, Map<String, String> floatingLabelMap) {
		super("Migrate Class Diagram");
		this.diagram = diagram;
		this.floatingLabelsMap = floatingLabelMap;
	}

	/**
	 * Do execute with result.
	 *
	 * @param progressMonitor
	 *            the progress monitor
	 * @param info
	 *            the info
	 * @return the command result
	 * @throws ExecutionException
	 *             the execution exception
	 * @see org.eclipse.gmf.runtime.common.core.command.AbstractCommand#doExecuteWithResult(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
		// EObject element = diagram.getElement();

		final TreeIterator<EObject> children = diagram.eAllContents();

		while (children.hasNext()) {
			EObject object = (EObject) children.next();
			// for (Object child : children) {
			if (object instanceof View) {
				View element = (View) object;

				// foatingLabel have to be adds to the Element
				if (floatingLabelsMap.containsKey(element.getType())) {

					String floatingLabelVisualId = floatingLabelsMap.get(element.getType());

					// If it doesn't exist yet
					boolean haveFloatingLabel = false;
					for (Object child : element.getChildren()) {
						if (child instanceof View) {
							if (floatingLabelVisualId.equals(((View) child).getType())) {
								haveFloatingLabel = true;
							}
						}
					}
					if (!haveFloatingLabel) {
						// Create the decoration node corresponding to the floating label
						DecorationNode floatingLabel = NotationFactory.eINSTANCE.createDecorationNode();
						floatingLabel.setType(floatingLabelVisualId);
						final Location location = NotationFactory.eINSTANCE.createLocation();
						location.setY(5);
						floatingLabel.setLayoutConstraint(location);
						element.insertChild(floatingLabel);
					}
				}
			}
		}

		return CommandResult.newOKCommandResult();
	}


	/**
	 * Can undo.
	 *
	 * @return true, if successful
	 * @see org.eclipse.core.commands.operations.AbstractOperation#canUndo()
	 */
	@Override
	public boolean canUndo() {
		return false;
	}

	/**
	 * Can redo.
	 *
	 * @return true, if successful
	 * @see org.eclipse.core.commands.operations.AbstractOperation#canRedo()
	 */
	@Override
	public boolean canRedo() {
		return false;
	}

	/**
	 * Do redo with result.
	 *
	 * @param progressMonitor
	 *            the progress monitor
	 * @param info
	 *            the info
	 * @return the command result
	 * @throws ExecutionException
	 *             the execution exception
	 * @see org.eclipse.gmf.runtime.common.core.command.AbstractCommand#doRedoWithResult(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	protected CommandResult doRedoWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
		throw new ExecutionException("Should not be called, canRedo false");
	}

	/**
	 * Do undo with result.
	 *
	 * @param progressMonitor
	 *            the progress monitor
	 * @param info
	 *            the info
	 * @return the command result
	 * @throws ExecutionException
	 *             the execution exception
	 * @see org.eclipse.gmf.runtime.common.core.command.AbstractCommand#doUndoWithResult(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	protected CommandResult doUndoWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
		throw new ExecutionException("Should not be called, canUndo false");
	}
}