/*****************************************************************************
 * Copyright (c) 2003, 2014 IBM Corporation, Christian W. Damus, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   IBM Corporation - Initial API and implementation
 *   Christian W. Damus - Adapted to the Papyrus environment
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.Tool;
import org.eclipse.gef.tools.SelectionTool;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ConnectionHandleEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramWorkbenchPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Papyrus adaptation of the GMF connection handle diagram assistant.
 * 
 * @author cmahoney
 */
public class PapyrusConnectionHandleEditPolicy extends ConnectionHandleEditPolicy {

	public PapyrusConnectionHandleEditPolicy() {
		super();
	}

	/*
	 * Replaces DiagramAssistantEditPolicy::shouldShowDiagramAssistant() method that depends on private methods of that class.
	 */
	protected boolean basicShouldShowDiagramAssistant() {
		return getHost().isActive() && isPreferenceOn() && isHostEditable()
				&& isHostResolvable() && isDiagramPartActive();
	}

	/*
	 * Replaces ConnectionHandleEditPolicy::shouldShowDiagramAssistant() method that depends on private methods of that class.
	 */
	@Override
	protected boolean shouldShowDiagramAssistant() {
		if (!basicShouldShowDiagramAssistant()) {
			return false;
		}

		if (isDiagramAssistantShowing() || !isSelectionToolActive()) {
			return false;
		}
		return true;
	}

	/*
	 * Replaces ConnectionHandleEditPolicy::isSelectionToolActive() method that is private.
	 */
	protected boolean isSelectionToolActive() {
		// getViewer calls getParent so check for null
		if (getHost().getParent() != null) {
			Tool theTool = getHost().getViewer().getEditDomain().getActiveTool();
			if ((theTool != null) && theTool instanceof SelectionTool) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Replaces DiagramAssistantEditPolicy::isHostEditable() method that is private.
	 */
	protected boolean isHostEditable() {
		if (getHost() instanceof GraphicalEditPart) {
			return ((GraphicalEditPart) getHost()).isEditModeEnabled();
		}
		return true;
	}

	/*
	 * Replaces DiagramAssistantEditPolicy::isHostResolvable() method that is private.
	 */
	protected boolean isHostResolvable() {
		final View view = (View) getHost().getModel();
		EObject element = view.getElement();
		if (element != null) {
			return !element.eIsProxy();
		}
		return true;
	}


	/*
	 * Replaces DiagramAssistantEditPolicy::isDiagramPartActive() method that is private and
	 * does not account for the fact that Papyrus nests its diagrams in a multi-editor.
	 */
	protected boolean isDiagramPartActive() {
		boolean result = false;

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IWorkbenchPart activePart = page.getActivePart();
				IDiagramWorkbenchPart editorPart = null;

				if (activePart instanceof IDiagramWorkbenchPart) {
					editorPart = (IDiagramWorkbenchPart) activePart;
				} else if (activePart instanceof IAdaptable) {
					editorPart = (IDiagramWorkbenchPart) ((IAdaptable) activePart).getAdapter(IDiagramWorkbenchPart.class);
				}

				if (editorPart != null) {
					result = editorPart.getDiagramEditPart().getRoot().equals(((IGraphicalEditPart) getHost()).getRoot());
				}
			}
		}

		return result;
	}

}
