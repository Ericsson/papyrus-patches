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
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultCreationEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphRequestHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.InteractionGraphCommand;
import org.eclipse.uml2.uml.InteractionFragment;

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
		InteractionGraph graph = InteractionGraphRequestHelper.getOrCreateInteractionGraph(request, (org.eclipse.gef.GraphicalEditPart) getHost());
		if (graph == null) {
			return null;
		}

		// TODO: @etxacam Handle multiple creation for paste???
		if (request.getViewDescriptors().get(0).getSemanticHint().equals(LifelineEditPart.VISUAL_ID)) {
			
			Rectangle rectangle = ViewUtilities.controlToViewer(graph.getEditPartViewer(), getCreationRectangle(request).getCopy());
			InteractionGraphCommand cmd = new InteractionGraphCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), "createLifeline", graph, null);
			cmd.addLifeline(request.getViewAndElementDescriptor().getCreateElementRequestAdapter(),
					request.getViewAndElementDescriptor(), rectangle);
			return new ICommandProxy(cmd);
		}
		return super.getCreateElementAndViewCommand(request);
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
