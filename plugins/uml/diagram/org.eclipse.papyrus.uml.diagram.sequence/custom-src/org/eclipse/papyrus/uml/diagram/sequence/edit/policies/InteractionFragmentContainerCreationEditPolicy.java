/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultCreationEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphFactory;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.UMLFactory;

/**
 * Custom creation edit policy for containers of {@link InteractionFragment}s, primarily
 * for the creation of such fragments.
 *
 * @since 5.0
 */
public class InteractionFragmentContainerCreationEditPolicy extends DefaultCreationEditPolicy {

	/**
	 * Initializes me.
	 */
	public InteractionFragmentContainerCreationEditPolicy() {
		super();
	}

	@Override
	protected Command getCreateElementAndViewCommand(CreateViewAndElementRequest request) {
		InteractionGraph graph = (InteractionGraph) request.getExtendedData().get("interactionGraph");
		View view = ((GraphicalEditPart) getHost()).getNotationView();
		graph = InteractionGraphFactory.getInstance().createInteractionGraph(
				(Interaction) view.getElement(),
				view.getDiagram(),
				getHost().getViewer());

		////////////////////////////////////////////////
		// TODO: Need to wrap the graph operation in something, so we do not modify until command execution. A command Builder????
		////////////////////////////////////////////////

		Rectangle rectangle = getCreationRectangle(request);
		Cluster cluster = graph.addLifeline(UMLFactory.eINSTANCE.createLifeline());

		// TODO: Needs to set the view and the element in the request so the result is propagated through the command chain??? Or we do not want that???
		// CreateElementRequestAdapter createReq = request.getViewAndElementDescriptor().getElementAdapter().getAdapter(CreateElementRequestAdapter.class);
		// createReq.setNewElement(cluster.getElement());
		// Need a wrapper to set the element in the CreateElementRequestAdapter from the cluster.
		// Need a wrapper to set the view in the request ViewDescriptor from the cluster.

		ICommand command = graph.getEditCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), "createLifeline");
		return new ICommandProxy(command);
		/*
		 * IElementType typeToCreate = request.getViewAndElementDescriptor().getElementAdapter().getAdapter(IElementType.class);
		 *
		 * if (!ElementUtil.isTypeOf(typeToCreate, UMLElementTypes.LIFELINE)) {
		 * IEditCommandRequest semanticCreateRequest = (IEditCommandRequest) request.getViewAndElementDescriptor().getCreateElementRequestAdapter().getAdapter(IEditCommandRequest.class);
		 * if (semanticCreateRequest != null) {
		 * // What are the lifelines covered?
		 * Rectangle rectangle = getCreationRectangle(request);
		 * Set<Lifeline> covered = SequenceUtil.getCoveredLifelines(rectangle, getHost());
		 * RequestParameterUtils.setCoveredLifelines(semanticCreateRequest, covered);
		 * }
		 * }
		 *
		 * return super.getCreateElementAndViewCommand(request);
		 */
	}

	protected Rectangle getCreationRectangle(CreateViewAndElementRequest request) {
		Point location = request.getLocation();
		Dimension size = request.getSize();

		if (size == null) {
			return new Rectangle(location.x(), location.y(), 1, 1);
		}

		return new Rectangle(location, size);
	}
}
