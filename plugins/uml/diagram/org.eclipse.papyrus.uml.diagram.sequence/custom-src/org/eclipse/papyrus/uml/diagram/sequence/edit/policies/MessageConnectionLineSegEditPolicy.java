/*****************************************************************************
 * Copyright (c) 2010-2017 CEA
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Atos Origin - Initial API and implementation
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 519408, 525372, 526628
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Bug 531596
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.BendpointRequest;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ConnectionBendpointEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.requests.SetAllBendpointRequest;
import org.eclipse.gmf.runtime.diagram.ui.util.SelectInDiagramHelper;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphRequestHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.InteractionGraphCommand;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.KeyboardHandler;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.uml2.uml.Message;

/**
 * This bendpoint edit policy is used to allow drag of horizontal messages and forbid drag otherwise.
 *
 * @author mvelten
 *
 */
@SuppressWarnings("restriction")
public class MessageConnectionLineSegEditPolicy extends ConnectionBendpointEditPolicy {
	protected KeyboardHandler keyHandler = new KeyboardHandler();
	private static final String CLICK_LOCATION_KEY = "clickLocation";
	
	@Override
	public void activate() {
		super.activate();
		keyHandler.activate();
	}

	@Override
	public void deactivate() {
		super.deactivate();
		keyHandler.deactivate();
	}

	protected Command getBendpointsChangedCommand(BendpointRequest request) {
		Point loc = SequenceUtil.getSnappedLocation(request.getSource(),request.getLocation().getCopy());
		if ((getHost().getViewer() instanceof ScrollingGraphicalViewer) &&
			(getHost().getViewer().getControl() instanceof FigureCanvas)){
			SelectInDiagramHelper.exposeLocation((FigureCanvas)getHost().getViewer().getControl(),loc);
		}
	
		Point srcLoc = (Point)request.getExtendedData().get(CLICK_LOCATION_KEY); 
		if (srcLoc == null) {
			srcLoc = loc.getCopy();
			request.getExtendedData().put(CLICK_LOCATION_KEY, srcLoc);
		}
		
		
		Connection connection = getConnection();
		Edge connectionView = (Edge) request.getSource().getModel();
		if (!(connectionView.getElement() instanceof Message))
			return null;
		Message message = (Message)connectionView.getElement(); 
		InteractionGraph graph = InteractionGraphRequestHelper.getOrCreateInteractionGraph(request, (org.eclipse.gef.GraphicalEditPart) getHost());
		if (graph == null)
			return null;
		
		InteractionGraphCommand cmd = new InteractionGraphCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), 
				"Move Message", graph, null);
		Point p = ViewUtilities.controlToViewer(graph.getEditPartViewer(), new Point(loc.x, loc.y));				
		Point p1 = ViewUtilities.controlToViewer(graph.getEditPartViewer(), new Point(srcLoc.x, srcLoc.y));				

		if (keyHandler.isAnyPressed() ) {
			cmd.moveMessage(message, new Point(p.x - p1.x, p.y - p1.y));
		} else {
			cmd.nudgeMessage(message, new Point(p.x - p1.x, p.y - p1.y));
		}
		return new ICommandProxy(cmd);
	}
	
	@Override
	protected Command getSetBendpointCommand(SetAllBendpointRequest request) {
		return super.getSetBendpointCommand(request);
	}

}
