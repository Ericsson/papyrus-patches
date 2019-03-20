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

import java.util.List;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.util.SelectInDiagramHelper;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.RoundedRectangleNodePlateFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.snap.PapyrusDragEditPartsTrackerEx;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.BorderItemResizableEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphRequestHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.InteractionGraphCommand;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.KeyboardHandler;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.InteractionFragment;

/**
 * @author etxacam
 *
 */
public class CustomGateEditPart extends GateEditPart implements IGraphicalEditPart {

	public static final int GATE_SIZE = 12;
	
	public CustomGateEditPart(View view) {
		super(view);
	}

	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		BorderItemResizableEditPolicy resizableEditPolicy = new BorderItemResizableEditPolicy() {
			@Override
			protected void createResizeHandle(List handles, int direction) {
				return;
			}

			@Override
			protected Command getMoveCommand(ChangeBoundsRequest request) {
				Point loc = request.getLocation();		
				if ((getHost().getViewer() instanceof ScrollingGraphicalViewer) &&
						(getHost().getViewer().getControl() instanceof FigureCanvas)) {
						SelectInDiagramHelper.exposeLocation((FigureCanvas)getHost().getViewer().getControl(),loc);
				}

				InteractionGraph graph = InteractionGraphRequestHelper.getOrCreateInteractionGraph(request, (org.eclipse.gef.GraphicalEditPart) getHost());
				if (graph == null)
					return null;
				
				Gate gate = (Gate)resolveSemanticElement();
				InteractionGraphCommand cmd = new InteractionGraphCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), 
						"Move Message", graph, null);

				Rectangle bounds = ViewUtilities.getBounds(getViewer(), ((IGraphicalEditPart)getHost()).getNotationView()).getCopy();
				bounds = request.getTransformedRectangle(bounds);	
				Rectangle beforeSnap = bounds.getCopy();
				Point p = SequenceUtil.getSnappedLocation(getHost(),bounds.getCenter());
				bounds.x += (p.x - bounds.getCenter().x);
				bounds.y += (p.y - bounds.getCenter().y);
				if (KeyboardHandler.getKeyboardHandler().isAnyPressed() ) {
					cmd.moveGate(gate, (InteractionFragment)gate.getOwner(), bounds.getCenter());		
				} else {
					cmd.nudgeGate(gate, bounds.getCenter()); 
				}
				return new ICommandProxy(cmd);
			}

		};
		resizableEditPolicy.setResizeDirections(0);
		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, resizableEditPolicy);
	}

	@Override
	protected NodeFigure createNodePlate() {
		RoundedRectangleNodePlateFigure result = new RoundedRectangleNodePlateFigure(GATE_SIZE, GATE_SIZE);
		return result;
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.AbstractBorderItemEditPart#refreshBounds()
	 *
	 */
	@Override
	protected void refreshBounds() {
		super.refreshBounds();
		getBorderItemLocator().relocate(getFigure());
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.editparts.BorderNodeEditPart#getDragTracker(org.eclipse.gef.Request)
	 *
	 * @param request
	 * @return
	 */
	@Override
	public DragTracker getDragTracker(Request request) {
		return new PapyrusDragEditPartsTrackerEx(this, false, true, false) {
			@Override
			protected void setCloneActive(boolean cloneActive) {
				super.setCloneActive(false); // Disable cloning
			}			
			
			protected boolean isMove() {
				EditPart part = getSourceEditPart();
				while (part != getTargetEditPart() && part != null) {
					if (part.getParent() == getTargetEditPart()) {
						return true;
					}
					part = part.getParent();
				}
				return false;
			}
		};
	}
	
	
}
