/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.util.EditPartUtil;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphRequestHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.InteractionGraphCommand;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Lifeline;

/**
 * @author ETXACAM
 *
 */
public class InteractionContainerLayoutEditPolicy extends XYLayoutEditPolicy {
	public InteractionContainerLayoutEditPolicy() {
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

			InteractionGraphCommand cmd = new InteractionGraphCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), "createLifeline", graph, null);
			if (request.getMoveDelta().x != 0 || request.getMoveDelta().y != 0) {
				cmd.nudgeLifeline((Lifeline) element, request.getMoveDelta());
			}

			if (request.getSizeDelta().width != 0 || request.getSizeDelta().height != 0) {
				cmd.resizeLifeline((Lifeline) element, request.getSizeDelta());
			}

			return new ICommandProxy(cmd);
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

	@Override
	public void showSourceFeedback(Request req) {
		super.showSourceFeedback(req);
	}

	@Override
	public void showTargetFeedback(Request req) {
		// TODO: Moveit to a Lifeline policy so we can restrict the target type based on the Node Layouts --> Moving Area Constraint --> Constraint the request ???
		super.showTargetFeedback(req);
		if (req instanceof ChangeBoundsRequest) {
			ChangeBoundsRequest request = (ChangeBoundsRequest) req;
			List<?> editParts = request.getEditParts();
			EditPart ep = editParts.stream().map(IGraphicalEditPart.class::cast).filter(d -> EditPartUtil.getSemanticEClassName(d).equals("uml.Lifeline")).findFirst().orElse(null);
			if (ep != null && request.getMoveDelta() != null) {
				request.getMoveDelta().y = 0;
			}
		}
	}
}
