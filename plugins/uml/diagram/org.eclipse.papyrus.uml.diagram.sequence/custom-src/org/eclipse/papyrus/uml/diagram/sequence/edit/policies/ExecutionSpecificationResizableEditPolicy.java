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

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.tools.ResizeTracker;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ResizableShapeEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.internal.requests.ChangeBoundsDeferredRequest;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeUtil;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CLifeLineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.BoundForEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphRequestHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.InteractionGraphImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.InteractionGraphCommand;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.KeyboardHandler;
import org.eclipse.papyrus.uml.diagram.sequence.util.CoordinateReferentialUtils;
import org.eclipse.uml2.uml.ExecutionSpecification;

/**
 * @author ETXACAM
 *
 */
public class ExecutionSpecificationResizableEditPolicy extends ResizableShapeEditPolicy {
	private static final Dimension MAX_DIMENSION = new Dimension(20, IFigure.MAX_DIMENSION.height);
	private static final Dimension MIN_DIMENSION = new Dimension(20, 20);
	
	public void activate() {
		super.activate();
		KeyboardHandler.getKeyboardHandler(); // Force the keyboard handler to be active
	}

	public ExecutionSpecificationResizableEditPolicy() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Command getAutoSizeCommand(Request request) {
		return null;
	}

	@Override
	protected Command getMoveDeferredCommand(ChangeBoundsDeferredRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	//TODO: @etxacam: Need to implement detach mode
	@Override
	protected Command getResizeCommand(ChangeBoundsRequest request) {
		InteractionGraphImpl graph = (InteractionGraphImpl)InteractionGraphRequestHelper.getOrCreateInteractionGraph(request, (org.eclipse.gef.GraphicalEditPart) getHost());
		if (graph == null)
			return null;
		GraphicalEditPart ep = (GraphicalEditPart)request.getEditParts().get(0);
		View view = (View)ep.getModel();
		ExecutionSpecification exec = (ExecutionSpecification)view.getElement();
		Cluster execCluster = graph.getClusterFor(exec); 
		boolean resizingTop = request.getSizeDelta().height != 0 && request.getMoveDelta().y != 0;
		int nudging = resizingTop ? request.getMoveDelta().y : request.getSizeDelta().height;  
		if (!KeyboardHandler.getKeyboardHandler().isAnyPressed()) {
			InteractionGraphCommand cmd = new InteractionGraphCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), 
						"Resize Execution Specification", graph, null);
			cmd.resizeExecutionSpecification(exec, resizingTop, nudging);
			return new ICommandProxy(cmd);
		} else {
			InteractionGraphCommand cmd = new InteractionGraphCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), 
					"Resize Execution Specification", graph, null);
			Point point = resizingTop ? execCluster.getBounds().getTop().getCopy() : execCluster.getBounds().getBottom(); 
			point.y += nudging;
			cmd.moveExecutionSpecificationOccurrence(exec, resizingTop ? exec.getStart() : exec.getFinish(), point);
			return new ICommandProxy(cmd);
			
		}
	}

	@Override
	protected Command getMoveCommand(ChangeBoundsRequest request) {
		InteractionGraphImpl graph = (InteractionGraphImpl)InteractionGraphRequestHelper.getOrCreateInteractionGraph(
				request, (org.eclipse.gef.GraphicalEditPart) getHost());
		if (graph == null)
			return null;
		
		GraphicalEditPart ep = (GraphicalEditPart)request.getEditParts().get(0);
		View view = (View)ep.getModel();
		ExecutionSpecification exec = (ExecutionSpecification)view.getElement();
		boolean detaching = KeyboardHandler.getKeyboardHandler().isAnyPressed();		
		int nudging = request.getMoveDelta().y;  
		InteractionGraphCommand cmd = new InteractionGraphCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), 
				"Move Execution Specification", graph, null);
		if (!detaching) {
			cmd.nudgeExecutionSpecification(exec, nudging);
		} else {
			Cluster execNode = graph.getClusterFor(exec);
			Point pt = execNode.getBounds().getTopLeft().getCopy();
			pt.translate(request.getMoveDelta());
			cmd.moveExecutionSpecification(exec, exec.getCovereds().get(0), pt);
		}

		return new ICommandProxy(cmd);		
	}

	@Override
	protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
		request.getMoveDelta().x = 0; // reset offset
		IFigure feedback = getDragSourceFeedbackFigure();
		PrecisionRectangle rect = new PrecisionRectangle(getInitialFeedbackBounds().getCopy());
		getHostFigure().translateToAbsolute(rect);
		IFigure f = getHostFigure();
		Dimension min = f.getMinimumSize().getCopy();
		Dimension max = f.getMaximumSize().getCopy();
		IMapMode mmode = MapModeUtil.getMapMode(f);
		min.height = mmode.LPtoDP(min.height);
		min.width = mmode.LPtoDP(min.width);
		max.height = mmode.LPtoDP(max.height);
		max.width = mmode.LPtoDP(max.width);
		Rectangle originalBounds = rect.getCopy();
		rect.translate(request.getMoveDelta());
		rect.resize(request.getSizeDelta());
		if (min.width > rect.width) {
			rect.width = min.width;
		} else if (max.width < rect.width) {
			rect.width = max.width;
		}
		if (min.height > rect.height) {
			rect.height = min.height;
		} else if (max.height < rect.height) {
			rect.height = max.height;
		}
		if (rect.height == min.height && request.getSizeDelta().height < 0 && request.getMoveDelta().y > 0) { // shrink at north
			Point loc = rect.getLocation();
			loc.y = originalBounds.getBottom().y - min.height;
			rect.setLocation(loc);
			request.getSizeDelta().height = min.height - originalBounds.height;
			request.getMoveDelta().y = loc.y - originalBounds.y;
		}
		if (request.getSizeDelta().height == 0) { // moving
			moveExecutionSpecificationFeedback(request, (AbstractExecutionSpecificationEditPart)getHost(), rect, originalBounds);
		}
		feedback.translateToRelative(rect);
		feedback.setBounds(rect);
	}
	
	/**
	 * @since 5.0
	 */
	protected void moveExecutionSpecificationFeedback(ChangeBoundsRequest request, AbstractExecutionSpecificationEditPart movedPart, PrecisionRectangle rect, Rectangle originalBounds) {

		// If this is a move to the top, the execution specification cannot be moved upper than the life line y position
		if (request.getMoveDelta().y < 0) {
			EditPart parent = movedPart.getParent();
			if (parent instanceof CLifeLineEditPart) {

				Point locationOnDiagram = CoordinateReferentialUtils.transformPointFromScreenToDiagramReferential(originalBounds.getCopy().getLocation(), (GraphicalViewer) movedPart.getViewer());
				Bounds parentBounds = BoundForEditPart.getBounds((org.eclipse.gmf.runtime.notation.Node) ((CLifeLineEditPart) parent).getModel());

				// This magic delta is needed to be at the bottom of the life line name
				if ((locationOnDiagram.y + request.getMoveDelta().y) < (parentBounds.getY() + 50)) {
					Point loc = locationOnDiagram.getCopy();
					loc.y = parentBounds.getY() + 50;
					rect.setLocation(loc);
					request.getMoveDelta().y = parentBounds.getY() + 50 - locationOnDiagram.y;
				}
			}
		}
	}
	
	protected void createResizeHandle(List handles, int direction) {
		if (direction != PositionConstants.NORTH && direction != PositionConstants.SOUTH)
			return;
		super.createResizeHandle(handles, direction);
	}
	
	@Override
	protected ResizeTracker getResizeTracker(int direction) {
		return new ResizeTracker((GraphicalEditPart) getHost(), direction) {
			
			@Override
			protected Dimension getMaximumSizeFor(ChangeBoundsRequest request) {
				return MAX_DIMENSION;
			}

			@Override
			protected Dimension getMinimumSizeFor(ChangeBoundsRequest request) {
				return MIN_DIMENSION;
			}

			@Override
			protected Request createSourceRequest() {
				ChangeBoundsRequest request;
				request = new ChangeBoundsRequest(REQ_RESIZE) {

					@Override
					public void setCenteredResize(boolean value) {
						super.setCenteredResize(false);
					}
					
				};
				request.setResizeDirection(getResizeDirection());
				return request;
			}
			
		};
	}

}
