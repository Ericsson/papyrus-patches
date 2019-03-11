/*****************************************************************************
 * Copyright (c) 2014, 2016 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Benoit Maggi (CEA LIST) benoit.maggi@cea.fr - Initial API and implementation
 *  Christian W. Damus - bug 508404
 *  Asma Smaoui (CEA LIST) asma.smaoui@cea.fr -Bug 490804
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.DropObjectsRequest;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.MoveRequest;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.core.clipboard.PapyrusClipboard;
import org.eclipse.papyrus.infra.core.internal.clipboard.CopierFactory;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.CopyPasteUtil;
import org.eclipse.papyrus.infra.services.edit.service.ElementEditServiceUtils;
import org.eclipse.papyrus.infra.services.edit.service.IElementEditService;


/**
 * this command is used to wrap a copy command. it allows set a new owner for views.
 */
public class DefaultDiagramPasteCommand extends AbstractTransactionalCommand {



	/** the new container for the shape */
	protected View container = null;

	protected List<EObject> semanticList = new ArrayList<EObject>();

	protected List<EObject> viewList = new ArrayList<EObject>();

	protected ICommand editCommand;

	private CompoundCommand allDropCommand;

	private GraphicalEditPart targetEditPart;

	private List<EObject> objectToDrop;

	private List<EObject> elementsToMove;

	/**
	 * Constructor.
	 *
	 * @param editingDomain
	 * @param label
	 * @param papyrusClipboard
	 * @param subCommand
	 * @param container
	 */
	public DefaultDiagramPasteCommand(TransactionalEditingDomain editingDomain, String label, PapyrusClipboard<Object> papyrusClipboard, GraphicalEditPart targetEditPart) {
		super(editingDomain, label, null);
		this.container = (View) targetEditPart.getModel();
		this.targetEditPart = targetEditPart;

		EcoreUtil.Copier copier = new CopierFactory(editingDomain.getResourceSet()).get();

		List<EObject> rootElementInClipboard = EcoreUtil.filterDescendants(CopyPasteUtil.filterEObject(papyrusClipboard));
		copier.copyAll(rootElementInClipboard);
		copier.copyReferences();
		viewList.addAll(EcoreUtil.filterDescendants(copier.values()));
		for (Object eObject : rootElementInClipboard) {
			if (!(eObject instanceof View)) {
				viewList.remove(copier.get(eObject));
				semanticList.add(copier.get(eObject));
			}
		}

		// Inform the clipboard of the element created (used by strategies)
		Map<Object, EObject> transtypeCopier = CopyPasteUtil.transtypeCopier(copier);
		papyrusClipboard.addAllInternalToTargetCopy(transtypeCopier);
		List<EObject> semanticRootList = EcoreUtil.filterDescendants(semanticList);
		MoveRequest moveRequest = new MoveRequest(container.getElement(), semanticRootList);
		this.elementsToMove = semanticRootList;
		IElementEditService provider = ElementEditServiceUtils.getCommandProvider(container.getElement());
		if (provider != null) {
			editCommand = provider.getEditCommand(moveRequest);
		}
		if (!papyrusClipboard.getContainerType().equals(targetEditPart.getNotationView().getDiagram().getType()) || viewList.isEmpty()) {
			this.objectToDrop = semanticRootList;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {

		// this will execute the Move request command: create the semantic elements to be copied
		// all copied semanitc elements regardless of the targetContainer (if a possible view could be created or not)
		editCommand.execute(progressMonitor, info);

		if (this.objectToDrop != null) {
			constructDropRequest(targetEditPart, this.objectToDrop);

			// if there is a view in the clipboard, create the duplicated view via a drop command to find the convenient editPart
			// this will ensure that no view will be created if the drop disallow the view creation
		} else if (viewList != null && !viewList.isEmpty()) {
			constructViewDropRequest(targetEditPart, viewList);
		}

		// execute the drop command : create all possible views
		if (allDropCommand != null && !allDropCommand.isEmpty()) {
			allDropCommand.execute();
		}

		// remove all moved elements with no possible created views:
		if (elementsToMove != null && !elementsToMove.isEmpty()) {
			for (EObject eObject : elementsToMove) {
				DestroyElementRequest destroyRequest = new DestroyElementRequest(eObject, false);
				if (eObject.eContainer() != null) {
					IElementEditService provider = ElementEditServiceUtils.getCommandProvider(eObject.eContainer());
					if (provider != null) {
						ICommand destroyCommand = provider.getEditCommand(destroyRequest);
						destroyCommand.execute(progressMonitor, info);
					}
				}
			}
		}
		return editCommand.getCommandResult();
	}


	/**
	 * Construct the drop request
	 *
	 * @param targetEditPart
	 * @param objectToDrop
	 */
	protected void constructDropRequest(GraphicalEditPart targetEditPart, List<EObject> objectToDrop) {
		DropObjectsRequest dropObjectsRequest = new DropObjectsRequest();
		if (container instanceof Diagram) {
			Point cursorPosition = CopyPasteUtil.getCursorPosition(targetEditPart);
			allDropCommand = new CompoundCommand("Drop all semantics elements on diagram"); //$NON-NLS-1$
			for (EObject eObject : objectToDrop) {
				dropObjectsRequest.setObjects(Collections.singletonList(eObject));
				dropObjectsRequest.setLocation(cursorPosition);
				Command command = targetEditPart.getCommand(dropObjectsRequest);
				allDropCommand.add(command);
				cursorPosition = CopyPasteUtil.shiftLayout(cursorPosition);
			}
		} else if (!(container instanceof Diagram)) {
			Rectangle bounds = targetEditPart.getFigure().getBounds();
			Point center = bounds.getCenter();
			allDropCommand = new CompoundCommand("Drop all semantics elements on a view"); //$NON-NLS-1$
			dropObjectsRequest.setObjects(objectToDrop);
			dropObjectsRequest.setLocation(center);
			Command command = targetEditPart.getCommand(dropObjectsRequest);
			if (command == null) {
				command = CopyPasteUtil.lookForCommandInSubContainer(targetEditPart, objectToDrop);
			}
			allDropCommand.add(command);
		}
	}


	/**
	 * Construct the drop request to control created views, duplicated views will be created at the cursor position
	 * 
	 * @param targetEditPart
	 * @param viewsToDrop
	 * 
	 * @since 3.0
	 */
	protected void constructViewDropRequest(GraphicalEditPart targetEditPart, List<EObject> viewsToDrop) {

		DropObjectsRequest dropObjectsRequest = new DropObjectsRequest();
		Point newlocation = CopyPasteUtil.getCursorPosition(targetEditPart);
		allDropCommand = new CompoundCommand("Drop all semantics elements"); //$NON-NLS-1$
		for (EObject eObject : viewsToDrop) {
			if (eObject instanceof View && ((View) eObject).getElement() != null) {
				EObject objectToDrop = ((View) eObject).getElement();
				dropObjectsRequest.setObjects(Collections.singletonList(objectToDrop));
				dropObjectsRequest.setLocation(newlocation);
				Command command = targetEditPart.getCommand(dropObjectsRequest);
				if (command == null) {
					command = CopyPasteUtil.lookForCommandInSubContainer(targetEditPart, Collections.singletonList(objectToDrop), newlocation);
				}
				if (command != null && command.canExecute()) {
					// a view will be created for this semantic elements, remove this semantic elements from the list of the elementToMove
					if (elementsToMove.contains(objectToDrop)) {
						elementsToMove.remove(objectToDrop);
					}
					allDropCommand.add(command);
					newlocation = CopyPasteUtil.shiftLayout(newlocation);
				}

			}
		}

	}

}
