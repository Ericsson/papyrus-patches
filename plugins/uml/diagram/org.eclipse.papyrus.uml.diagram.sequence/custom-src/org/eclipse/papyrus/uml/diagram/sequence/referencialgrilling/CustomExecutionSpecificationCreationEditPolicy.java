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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.FileModificationValidator;
import org.eclipse.gmf.runtime.diagram.ui.commands.CommandUtilities;
import org.eclipse.gmf.runtime.diagram.ui.commands.CreateOrSelectElementCommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeRequest;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultCreationEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.swt.widgets.Display;

/**
 * This allows to define the creation edit policy for the execution specification.
 */
public class CustomExecutionSpecificationCreationEditPolicy extends DefaultCreationEditPolicy {

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.CreationEditPolicy#getCommand(org.eclipse.gef.Request)
	 */
	@Override
	public Command getCommand(Request request) {
		if (understandsRequest(request)) {
			if (request instanceof CreateUnspecifiedTypeRequest) {
				return getUnspecifiedTypeCreateCommand((CreateUnspecifiedTypeRequest) request);
			}
		}
		return super.getCommand(request);
	}

	/**
	 * When this is a {@link CreateUnspecifiedTypeRequest}, we need to check if the position needed by the user is on an ExecutionSpecification because this is not allowed by the UML Norm
	 * but this will be possible graphically.
	 *
	 * @param request
	 *            The unspecified type request.
	 * @return The command.
	 */
	@SuppressWarnings("unchecked")
	private Command getUnspecifiedTypeCreateCommand(
			final CreateUnspecifiedTypeRequest request) {

		final Map<IElementType, Command> createCmds = new HashMap<>();
		List<IElementType> validTypes = new ArrayList<>();
		for (Iterator<IElementType> iter = request.getElementTypes().iterator(); iter
				.hasNext();) {
			IElementType elementType = iter.next();
			Request createRequest = request.getRequestForType(elementType);
			if (createRequest != null) {
				EditPart target = SequenceUtil.getParentLifelinePart(getHost().getTargetEditPart(createRequest));
				if (target == null) {
					continue;
				}
				Command individualCmd = target.getCommand(createRequest);

				if (individualCmd != null && individualCmd.canExecute()) {
					createCmds.put(elementType, individualCmd);
					validTypes.add(elementType);
				}
			}
		}

		if (createCmds.isEmpty()) {
			return null;
		} else if (createCmds.size() == 1) {
			return (Command) createCmds.values().toArray()[0];
		} else {
			CreateOrSelectElementCommand selectAndCreateViewCmd = new CreateOrSelectElementCommand(
					DiagramUIMessages.CreateCommand_Label, Display.getCurrent()
							.getActiveShell(),
					validTypes) {

				private Command _createCmd;

				/**
				 * Execute the command that prompts the user with the popup
				 * menu, then executes the command prepared for the element
				 * type that the user selected.
				 */
				@Override
				protected CommandResult doExecuteWithResult(
						IProgressMonitor progressMonitor, IAdaptable info)
						throws ExecutionException {

					CommandResult cmdResult = super.doExecuteWithResult(progressMonitor, info);
					if (!cmdResult.getStatus().isOK()) {
						return cmdResult;
					}

					IElementType type = (IElementType) cmdResult
							.getReturnValue();

					_createCmd = createCmds.get(type);
					Assert.isTrue(_createCmd != null && _createCmd.canExecute());

					// validate the affected files
					IStatus status = validateAffectedFiles(_createCmd);
					if (!status.isOK()) {
						return new CommandResult(status);
					}

					_createCmd.execute();

					// Set the result in the unspecified type request.
					CreateRequest createRequest = request
							.getRequestForType(type);

					Collection<?> newObject = ((Collection<?>) createRequest
							.getNewObject());
					request.setNewObject(newObject);

					return CommandResult.newOKCommandResult(newObject);
				}

				@Override
				protected CommandResult doUndoWithResult(
						IProgressMonitor progressMonitor, IAdaptable info)
						throws ExecutionException {

					if (_createCmd != null && _createCmd.canUndo()) {
						// validate the affected files
						IStatus status = validateAffectedFiles(_createCmd);
						if (!status.isOK()) {
							return new CommandResult(status);
						}
						_createCmd.undo();
					}
					return super.doUndoWithResult(progressMonitor, info);
				}

				@Override
				protected CommandResult doRedoWithResult(
						IProgressMonitor progressMonitor, IAdaptable info)
						throws ExecutionException {

					if (_createCmd != null && CommandUtilities.canRedo(_createCmd)) {
						// validate the affected files
						IStatus status = validateAffectedFiles(_createCmd);
						if (!status.isOK()) {
							return new CommandResult(status);
						}
						_createCmd.redo();
					}
					return super.doRedoWithResult(progressMonitor, info);
				}

				private IStatus validateAffectedFiles(Command command) {
					Collection<?> affectedFiles = CommandUtilities
							.getAffectedFiles(command);
					int fileCount = affectedFiles.size();
					if (fileCount > 0) {
						return FileModificationValidator
								.approveFileModification(affectedFiles
										.toArray(new IFile[fileCount]));
					}
					return Status.OK_STATUS;
				}
			};

			return new ICommandProxy(selectAndCreateViewCmd);
		}
	}
}
