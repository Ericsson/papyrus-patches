/*****************************************************************************
 * (c) Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import java.util.List;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
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
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.util.SelectInDiagramHelper;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.RoundedRectangleNodePlateFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.snap.PapyrusDragEditPartsTrackerEx;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.BorderItemResizableEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphRequestHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.InteractionGraphCommand;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.KeyboardHandler;
import org.eclipse.papyrus.uml.diagram.sequence.tools.PapyrusSequenceDragEditPartsTracker;
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
			public void activate() {
				super.activate();
				KeyboardHandler.getKeyboardHandler(); // Force the keyboard handler to be active
			}
			
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
						"Move Gate", graph, null);
				Rectangle bounds = ViewUtilities.getBounds(getViewer(), ((IGraphicalEditPart)getHost()).getNotationView()).getCopy();
				bounds = request.getTransformedRectangle(bounds);	
				Rectangle beforeSnap = bounds.getCopy();
				Point p = SequenceUtil.getSnappedLocation(getHost(),ViewUtilities.viewerToControl(getViewer(), bounds.getCenter()));
				bounds.x += (p.x - bounds.getCenter().x);
				bounds.y += (p.y - bounds.getCenter().y);
				p = ViewUtilities.controlToViewer(getViewer(), bounds.getCenter());
				if (KeyboardHandler.getKeyboardHandler().isAnyPressed() ) {
					cmd.moveGate(gate, (InteractionFragment)gate.getOwner(), p);		
				} else {
					cmd.nudgeGate(gate, p); 
				}
				return new ICommandProxy(cmd);
			}

			@Override
			protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
				Rectangle bounds = ViewUtilities.getBounds(getViewer(), ((IGraphicalEditPart)getHost()).getNotationView());
				bounds = request.getTransformedRectangle(bounds);	
				Rectangle beforeSnap = bounds.getCopy();
				Point p = SequenceUtil.getSnappedLocation(getHost(),ViewUtilities.viewerToControl(getViewer(), bounds.getCenter()));
				p = ViewUtilities.controlToViewer(getViewer(), p);
				request.getMoveDelta().translate(p.getDifference(beforeSnap.getCenter()));
				super.showChangeBoundsFeedback(request);
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
		if (getBorderItemLocator() != null) {
			int x = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE
				.getLocation_X())).intValue();
			int y = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE
				.getLocation_Y())).intValue();
			Point loc = new Point(x, y);
			
			int width = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE.getSize_Width())).intValue();
			int height = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE.getSize_Height())).intValue();
			Dimension size = new Dimension(width, height);
			if (getParent() != null) {
				IFigure parentFigure = ((GraphicalEditPart)getParent()).getFigure();
				if (parentFigure.getParent() != null && parentFigure.getParent().getLayoutManager() != null) {
					Object obj = parentFigure.getParent().getLayoutManager().getConstraint(parentFigure);
					if (obj instanceof Rectangle) {
						Point borderLoc = ((Rectangle)obj).getLocation();
						loc.translate(borderLoc);
						getBorderItemLocator().setConstraint(new Rectangle(
							loc, size));
						getBorderItemLocator().relocate(getFigure());
					}
				}
			}
		} else {
			super.refreshBounds();
		}
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.editparts.BorderNodeEditPart#getDragTracker(org.eclipse.gef.Request)
	 *
	 * @param request
	 * @return
	 */
	@Override
	public DragTracker getDragTracker(Request request) {
		return new PapyrusSequenceDragEditPartsTracker(this, false, true, false) {
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
