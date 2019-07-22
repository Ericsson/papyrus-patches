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

import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.GhostImageFigure;
import org.eclipse.draw2d.XYAnchor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.BendpointRequest;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ConnectionBendpointEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.requests.SetAllBendpointRequest;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphRequestHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.InteractionGraphCommand;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.KeyboardHandler;
import org.eclipse.uml2.uml.Message;

/**
 * This bendpoint edit policy is used to allow drag of horizontal messages and forbid drag otherwise.
 *
 * @author mvelten
 *
 */
@SuppressWarnings("restriction")
public class MessageConnectionLineSegEditPolicy extends ConnectionBendpointEditPolicy {
	public void activate() {
		super.activate();
		KeyboardHandler.getKeyboardHandler(); // Force the keyboard handler to be active
	}

	protected Command getBendpointsChangedCommand(BendpointRequest request) {
		
		Connection connection = getConnection();
		Point p = connection.getSourceAnchor().getReferencePoint();
		Point delta = RequestLocationUtils.calculateRequestDragDelta(request, request.getSource(), p);
		delta.x = 0;
		Edge connectionView = (Edge) request.getSource().getModel();
		if (!(connectionView.getElement() instanceof Message))
			return null;

		Message message = (Message)connectionView.getElement(); 
		InteractionGraph graph = InteractionGraphRequestHelper.getOrCreateInteractionGraph(request, (org.eclipse.gef.GraphicalEditPart) getHost());
		if (graph == null)
			return null;
		
		InteractionGraphCommand cmd = new InteractionGraphCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), 
				"Move Message", graph, null);

		if (KeyboardHandler.getKeyboardHandler().isAnyPressed() ) {
			cmd.moveMessage(message, delta);
		} else {
			cmd.nudgeMessage(message, delta);
		}
		return new ICommandProxy(cmd);
	}
	
	@Override
	protected Command getSetBendpointCommand(SetAllBendpointRequest request) {
		return super.getSetBendpointCommand(request);
	}

	@Override
	protected void showCreateBendpointFeedback(BendpointRequest request) {
		Connection con = getConnection();
		Point p = con.getSourceAnchor().getReferencePoint();
		Point delta = RequestLocationUtils.calculateRequestDragDelta(request, request.getSource(), p);
		
		if (originalSourceAnchor == null || originalTargetAnchor == null) {
			originalSourceAnchor = con.getSourceAnchor();
			originalTargetAnchor = con.getTargetAnchor();
			saveOriginalConstraint();
/*			
			feedbackFigure = new GhostImageFigure(con, 128, new RGB(255,255,255));
			getFeedbackLayer().add(feedbackFigure);
			Rectangle r = con.getBounds().getCopy();
			con.getParent().translateToAbsolute(r);
			getFeedbackLayer().translateToRelative(r);			
			feedbackFigure.setBounds(r);
			originalFeedbackLoc = r.getLocation();*/
		} 					

		
		Point a1 = originalSourceAnchor.getReferencePoint();
		Point a2 = originalTargetAnchor.getReferencePoint();
		con.setSourceAnchor(new XYAnchor(new Point(a1.x,a1.y+delta.y)));
		con.setTargetAnchor(new XYAnchor(new Point(a2.x,a2.y+delta.y)));
		
//		super.showCreateBendpointFeedback(request);
	}

	@Override
	protected void eraseConnectionFeedback(BendpointRequest request, boolean removeFeedbackFigure) {
		Connection con = getConnection();
		if (originalSourceAnchor != null) {
			 con.setSourceAnchor(originalSourceAnchor);
			 originalSourceAnchor = null;
		}

		if (originalTargetAnchor != null) {
			 con.setTargetAnchor(originalTargetAnchor);
			 originalTargetAnchor = null;
		}
		
		if (feedbackFigure != null) {
			getFeedbackLayer().remove(feedbackFigure);
			feedbackFigure = null;
			originalFeedbackLoc = null;
		}
		super.eraseConnectionFeedback(request, removeFeedbackFigure);
	}

	@Override
	protected List createManualHandles() {
		return Collections.EMPTY_LIST;
	}

	private GhostImageFigure feedbackFigure;
	public ConnectionAnchor originalSourceAnchor;
	public ConnectionAnchor originalTargetAnchor;
	public Point originalFeedbackLoc;
}
