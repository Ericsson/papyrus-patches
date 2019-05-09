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

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphRequestHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.InteractionGraphCommand;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.KeyboardHandler;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;

/**
 * @author ETXACAM
 *
 */
public class InteractionContainerLayoutEditPolicy extends XYLayoutEditPolicy {
	public InteractionContainerLayoutEditPolicy() {
	}

	@Override
	public void activate() {
		super.activate();
		KeyboardHandler.getKeyboardHandler(); // Force the keyboard handler to be active
	}

	@Override
	protected Command getDeleteDependantCommand(Request request) {
		return null;
	}

	@Override
	protected Command getAddCommand(Request request) {
		return null;
	}

	@Override
	protected Command getOrphanChildrenCommand(Request request) {
		return null;
	}

	@Override
	protected Command getMoveChildrenCommand(Request request) {
		if (!(request instanceof ChangeBoundsRequest)) {
			return null;
		}

		return getChangeConstraintCommand((ChangeBoundsRequest) request);
	}

	@Override
	protected Command getResizeChildrenCommand(ChangeBoundsRequest request) {
		return getChangeConstraintCommand(request);
	}

	@Override
	protected Command getChangeConstraintCommand(ChangeBoundsRequest request) {
		// TODO: @etxacam Need to handle multiselection properly
		EditPart editPart = (EditPart) request.getEditParts().stream().findFirst().orElse(null);
		if (editPart == null || !(editPart.getModel() instanceof View)) {
			return null;
		}

		View view = (View) editPart.getModel();
		Element element = (Element) ViewUtil.resolveSemanticElement(view);
		if (element instanceof Lifeline) {

			InteractionGraph graph = InteractionGraphRequestHelper.getOrCreateInteractionGraph(request, (org.eclipse.gef.GraphicalEditPart) getHost());
			if (graph == null) {
				return null;
			}
			Point orig = ViewUtilities.controlToViewer(graph.getEditPartViewer(), new Point(0,0));
			Point moveDelta = ViewUtilities.controlToViewer(graph.getEditPartViewer(), request.getMoveDelta().getCopy());
			moveDelta.x -= orig.x;
			moveDelta.y -= orig.y;
			
			Dimension sizeDelta = ViewUtilities.controlToViewer(graph.getEditPartViewer(), request.getSizeDelta().getCopy());
			
			if (!KeyboardHandler.getKeyboardHandler().isAnyPressed() || request.getSizeDelta().width != 0 || request.getSizeDelta().height != 0) {
				// No reordering. Resizing is never reordering
				InteractionGraphCommand cmd = new InteractionGraphCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), "move Lifeline", graph, null);
				if (moveDelta.x != 0 || moveDelta.y != 0) {					
					cmd.nudgeLifeline((Lifeline) element, moveDelta);
				}

				if (sizeDelta.width != 0 || sizeDelta.height != 0) {
					cmd.resizeLifeline((Lifeline) element, sizeDelta);
				}
				return new ICommandProxy(cmd);
			} else {
				// Reordering case.
				InteractionGraphCommand cmd = new InteractionGraphCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), "move Lifeline", graph, null);
				if (moveDelta.x != 0 || moveDelta.y != 0) {
					cmd.moveLifeline((Lifeline) element, moveDelta);
				}
				return new ICommandProxy(cmd);
			}
		} else if (element instanceof InteractionUse) {
			InteractionGraph graph = InteractionGraphRequestHelper.getOrCreateInteractionGraph(request, (org.eclipse.gef.GraphicalEditPart) getHost());
			if (graph == null) {
				return null;
			}
			
			Rectangle rect = ViewUtilities.getBounds(graph.getEditPartViewer(), (View)((GraphicalEditPart)request.getEditParts().get(0)).getModel());
			rect = request.getTransformedRectangle(rect);
			Point orig = ViewUtilities.controlToViewer(graph.getEditPartViewer(), new Point(0,0));
			Point moveDelta = ViewUtilities.controlToViewer(graph.getEditPartViewer(), request.getMoveDelta().getCopy());
			moveDelta.x -= orig.x;
			moveDelta.y -= orig.y;
			
			Dimension sizeDelta = request.getSizeDelta();
			if (!KeyboardHandler.getKeyboardHandler().isAnyPressed()) {
				// No reordering. Resizing is never reordering
				InteractionGraphCommand cmd = new InteractionGraphCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), "move Lifeline", graph, null);
				if (sizeDelta.width != 0 || sizeDelta.height != 0) {
					cmd.nudgeResizeInteractionUse((InteractionUse) element, rect);
				} else if (moveDelta.x != 0 || moveDelta.y != 0) {					
					cmd.nudgeInteractionUse((InteractionUse) element, moveDelta);
				}

				return new ICommandProxy(cmd);
			} else {
				// Reordering case.
				InteractionGraphCommand cmd = new InteractionGraphCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), "move Lifeline", graph, null);
				if (request.getSizeDelta().width != 0 || request.getSizeDelta().height != 0) {
					cmd.resizeInteractionUse((InteractionUse) element, rect);					
				} else if (moveDelta.x != 0 || moveDelta.y != 0) {
					cmd.moveInteractionUse((InteractionUse) element, rect);
				}
				return new ICommandProxy(cmd);
			}

		}
		return null;
	}

	@Override
	protected Command getCloneCommand(ChangeBoundsRequest request) {
		return null;
	}

	@Override
	protected Command getCreateCommand(CreateRequest req) {
		return null;
	}

	private Rectangle getCreationRectangle(CreateRequest request) {
		Point location = request.getLocation();
		Dimension size = request.getSize();

		if (size == null) {
			return new Rectangle(location.x(), location.y(), 1, 1);
		}

		return new Rectangle(location, size);
	}
}
