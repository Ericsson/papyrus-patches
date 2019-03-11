/*****************************************************************************
 * Copyright (c) 2012, 2016 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Camille Letavernier (CEA LIST) camille.letavernier@cea.fr - Initial API and implementation
 *  Christian W. Damus (CEA) - support pluggable edit-part conflict detection (CDO)
 *  Celine Janssens (ALL4TEC) - Override getDragTracker with the PapyrusRubberbandDragTracker
 *  Christian W. Damus - bugs 451230, 461629, 501946
 *  Mickael ADAM (ALL4TEC) mickael.adam@all4tec.net - add refresh of SVGPostProcessor - Bug 467569
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpart;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.tools.DeselectAllTracker;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.PapyrusPopupBarEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.XYLayoutWithConstrainedResizedEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.handler.IRefreshHandlerPart;
import org.eclipse.papyrus.infra.gmfdiag.common.selection.PapyrusRubberbandDragTracker;
import org.eclipse.papyrus.infra.gmfdiag.common.service.shape.SVGPostProcessor;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.ServiceUtilsForEditPart;
import org.eclipse.papyrus.infra.ui.editor.IMultiDiagramEditor;
import org.eclipse.ui.IEditorPart;

import com.google.common.collect.Sets;

public class PapyrusDiagramEditPart extends DiagramEditPart {
	// We store weak-referencing sets in the values
	private static final Map<Diagram, Set<PapyrusDiagramEditPart>> diagrams = new WeakHashMap<Diagram, Set<PapyrusDiagramEditPart>>();

	private IConflictingEditPartFilter conflictFilter = IConflictingEditPartFilter.DEFAULT;

	/**
	 * Id of the right clic button
	 */
	public static final int RIGHT_MOUSE_BUTTON = 3;

	public PapyrusDiagramEditPart(View diagramView) {
		super(diagramView);

		register(diagramView, this);
	}

	private static void register(View diagramView, PapyrusDiagramEditPart editPart) {
		if (diagramView instanceof Diagram) {
			Diagram diagram = (Diagram) diagramView;

			synchronized (diagrams) {
				Set<PapyrusDiagramEditPart> diagramEditParts = diagrams.get(diagram);
				if (diagramEditParts == null) {
					// A weak-referencing set
					diagramEditParts = Sets.newSetFromMap(new WeakHashMap<PapyrusDiagramEditPart, Boolean>());
					diagrams.put(diagram, diagramEditParts);
				}
				diagramEditParts.add(editPart);
			}
		}
	}

	@Override
	public void refresh() {
		if (hasNotationView() && (getNotationView().eResource() != null)) {
			if (SVGPostProcessor.instance instanceof IRefreshHandlerPart) {
				IEditorPart activeEditor = null;
				try {
					IMultiDiagramEditor multiDiagramEditor = ServiceUtilsForEditPart.getInstance().getService(IMultiDiagramEditor.class, this);
					activeEditor = multiDiagramEditor.getActiveEditor();
				} catch (ServiceException e) {
					Activator.log.error(e);
				}
				((IRefreshHandlerPart) SVGPostProcessor.instance).refresh(activeEditor);
			}
			super.refresh();
		}
	}

	/**
	 * Obtains the edit-parts currently managing the specified {@code diagram} in editors.
	 * 
	 * @param diagram
	 *            a diagram (may be {@code null})
	 * 
	 * @return the set of edit-parts for the diagram, or an empty set if none (such as when the {@code diagram} is {@code null})
	 */
	public static Set<? extends DiagramEditPart> getDiagramEditPartsFor(Diagram diagram) {
		Set<? extends PapyrusDiagramEditPart> result;

		synchronized (diagrams) {
			result = diagrams.get(diagram);
		}

		if (result == null) {
			result = Collections.emptySet();
		}

		return result;
	}

	@Override
	protected void removeChild(EditPart child) {
		if (!getConflictingEditPartFilter().isConflicting(child)) {
			super.removeChild(child);
		}
	}

	public IConflictingEditPartFilter getConflictingEditPartFilter() {
		return conflictFilter;
	}

	public void setConflictingEditPartFilter(IConflictingEditPartFilter filter) {
		this.conflictFilter = (filter == null) ? IConflictingEditPartFilter.DEFAULT : filter;
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart#createDefaultEditPolicies()
	 */
	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new XYLayoutWithConstrainedResizedEditPolicy());

		// Replace GMF's default popup-bar edit policy
		removeEditPolicy(EditPolicyRoles.POPUPBAR_ROLE);
		installEditPolicy(EditPolicyRoles.POPUPBAR_ROLE, new PapyrusPopupBarEditPolicy());
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart#getDragTracker(org.eclipse.gef.Request)
	 *
	 * @param req
	 * @return a New DragTracker
	 */
	@Override
	public DragTracker getDragTracker(Request req) {

		// In case of new selection and right-click deselect previous trackers
		if (req instanceof SelectionRequest && ((SelectionRequest) req).getLastButtonPressed() == RIGHT_MOUSE_BUTTON) {
			return new DeselectAllTracker(this);
		}

		// This new tracker includes connections, Labels, nodes and different Behaviors
		return new PapyrusRubberbandDragTracker();

	}
}
