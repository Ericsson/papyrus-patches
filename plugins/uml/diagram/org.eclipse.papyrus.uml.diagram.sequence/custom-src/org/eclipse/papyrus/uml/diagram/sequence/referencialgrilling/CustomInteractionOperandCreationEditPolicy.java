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
 *   Christian W. Damus - bug 533675
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
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
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
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest.ViewAndElementDescriptor;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultCreationEditPolicy;
import org.eclipse.papyrus.infra.services.edit.utils.RequestParameterConstants;
import org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.papyrus.uml.service.types.element.UMLDIElementTypes;
import org.eclipse.papyrus.uml.service.types.utils.ElementUtil;
import org.eclipse.swt.widgets.Display;

/**
 * This allows to define the creation edit policy for the interaction operand.
 */
public class CustomInteractionOperandCreationEditPolicy extends DefaultCreationEditPolicy {

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.CreationEditPolicy#getCommand(org.eclipse.gef.Request)
	 */
	@Override
	public Command getCommand(Request request) {
		if (understandsRequest(request)) {
			EditPart combinedFragmentCompartment = getHost().getParent();
			if (combinedFragmentCompartment != null && REQ_CREATE.equals(request.getType()) && request instanceof CreateUnspecifiedTypeRequest) {
				if (UMLElementTypes.InteractionOperand_Shape.equals(((CreateUnspecifiedTypeRequest) request).getElementTypes().get(0))) {
					Map<? super String, Object> extendedData = request.getExtendedData();
					int hostIndex = combinedFragmentCompartment.getChildren().indexOf(getHost());
					extendedData.put(RequestParameterConstants.INSERT_AT, hostIndex + 1); // Insert after the target
				}
			}
			if (request instanceof CreateUnspecifiedTypeRequest) {
				return getUnspecifiedTypeCreateCommand((CreateUnspecifiedTypeRequest) request);
			}
		}
		return super.getCommand(request);
	}

	@Override
	protected Command getCreateElementAndViewCommand(CreateViewAndElementRequest request) {
		// Used during the drop from the model explorer
		if (request instanceof CreateViewAndElementRequest) {
			CreateViewAndElementRequest req = request;
			ViewAndElementDescriptor descriptor = (req).getViewAndElementDescriptor();
			IElementType elementType = descriptor.getElementAdapter().getAdapter(IElementType.class);
			if (ElementUtil.isTypeOf(elementType, UMLDIElementTypes.COMBINED_FRAGMENT_SHAPE)) {
				Rectangle boundsLifeline = getHostFigure().getBounds();
				Point pointCombinedFragment = req.getLocation();

				pointCombinedFragment.x = pointCombinedFragment.x + boundsLifeline.x;
				pointCombinedFragment.y = pointCombinedFragment.y + boundsLifeline.y;

				req.setLocation(pointCombinedFragment);

				return SequenceUtil.getInteractionCompartment(getHost()).getCommand(req);
			}
		}
		return super.getCreateElementAndViewCommand(request);
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
				EditPart target = getHost().getTargetEditPart(createRequest);
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

	/**
	 * Return the host's figure.
	 * The super calls getFigure(). This is a problem when used with shapecompartments. Instead,
	 * return getContextPane(). In shape comaprtments this will return the correct containing figure.
	 */
	protected IFigure getHostFigure() {
		return ((GraphicalEditPart) getHost()).getContentPane();
	}
}
