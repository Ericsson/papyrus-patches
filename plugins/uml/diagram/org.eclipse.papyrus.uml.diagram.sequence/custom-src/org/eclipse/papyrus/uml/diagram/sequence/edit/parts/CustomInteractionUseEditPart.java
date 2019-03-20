/*****************************************************************************
 * Copyright (c) 2019 CEA LIST and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.PapyrusResizableShapeEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.snap.PapyrusDragEditPartsTrackerEx;
import org.eclipse.papyrus.uml.diagram.sequence.figures.InteractionUseRectangleFigure;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;

/**
 * @author etxacam
 *
 */
public class CustomInteractionUseEditPart extends InteractionUseEditPart implements IGraphicalEditPart {

	public CustomInteractionUseEditPart(View view) {
		super(view);
	}

	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		PapyrusResizableShapeEditPolicy resizableEditPolicy = new PapyrusResizableShapeEditPolicy() {
			@Override
			protected void createResizeHandle(List handles, int direction) {
				if (direction != PositionConstants.EAST &&  direction != PositionConstants.WEST)
					return;
				super.createResizeHandle(handles, direction);
			}			
		};
		resizableEditPolicy.setResizeDirections(PositionConstants.EAST |PositionConstants.WEST);
		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, resizableEditPolicy);
	}

	@Override
	public DragTracker getDragTracker(Request req) {
		return new PapyrusDragEditPartsTrackerEx(this, true, false, false) {
			@Override
			protected void setCloneActive(boolean cloneActive) {
				super.setCloneActive(false); // Disable cloning
			}			
		};
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#registerVisuals()
	 *
	 */
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		refreshCoveredLifelines();
	}

	protected void refreshCoveredLifelines() {
		InteractionUse intUse = (InteractionUse)resolveSemanticElement();
		Interaction interaction = intUse.getEnclosingInteraction();
		List<Lifeline> allLifelines = interaction.getLifelines();
		List<Lifeline> coveredLifelines = intUse.getCovereds();
		List<IFigure> figures = new ArrayList<IFigure>();
		Rectangle newBounds = null;
		for (Lifeline lf : allLifelines) {
			View vw = ViewUtilities.getViewForElement(getDiagramView(),lf);
			if (coveredLifelines.contains(lf)) {
				Rectangle r = ViewUtilities.absoluteLayoutConstraint(getViewer(), vw);
				if (newBounds == null)
					newBounds = r.getCopy();
				else
					newBounds.union(r);
				continue;
			}
			GraphicalEditPart ep = (GraphicalEditPart)getViewer().getEditPartRegistry().get(vw);
			figures.add(ep.getFigure());
		}

		if (newBounds != null) {
			Rectangle intUseRect = ViewUtilities.absoluteLayoutConstraint(getViewer(),(View)getModel());
			if ((intUseRect.x != newBounds.x || intUseRect.width != newBounds.width) && newBounds.width > 0) {
				intUseRect.x = newBounds.x;
				intUseRect.width = newBounds.width; 
				intUseRect = ViewUtilities.toRelativeForLayoutConstraints(getViewer(), (View)((View)getModel()).eContainer(), intUseRect);
				((GraphicalEditPart) getParent()).setLayoutConstraint(
						this,
						getFigure(),
						intUseRect);
			}
		}
		if (primaryShape instanceof InteractionUseRectangleFigure)
			((InteractionUseRectangleFigure)primaryShape).setNonCoveredLifelinesFigures(figures);
	}
}
