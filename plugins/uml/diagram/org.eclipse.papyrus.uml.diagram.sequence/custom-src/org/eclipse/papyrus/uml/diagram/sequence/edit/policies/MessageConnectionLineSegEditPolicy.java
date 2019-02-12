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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.GhostImageFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.RelativeBendpoint;
import org.eclipse.draw2d.XYAnchor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.uml2.uml.Message;

/**
 * This bendpoint edit policy is used to allow drag of horizontal messages and forbid drag otherwise.
 *
 * @author mvelten
 *
 */
@SuppressWarnings("restriction")
public class MessageConnectionLineSegEditPolicy extends ConnectionBendpointEditPolicy {
	private static final String CLICK_LOCATION_KEY = "clickLocation";
	
	protected Command getBendpointsChangedCommand(BendpointRequest request) {
		Point loc = SequenceUtil.getSnappedLocation(request.getSource(),request.getLocation().getCopy());
		if ((getHost().getViewer() instanceof ScrollingGraphicalViewer) &&
			(getHost().getViewer().getControl() instanceof FigureCanvas)){
			SelectInDiagramHelper.exposeLocation((FigureCanvas)getHost().getViewer().getControl(),loc);
		}
	
		Point srcLoc = (Point)request.getExtendedData().get(CLICK_LOCATION_KEY); 
		if (srcLoc == null) {
			srcLoc = loc.getCopy();
			srcLoc = ViewUtilities.controlToViewer(getHost().getViewer(), new Point(srcLoc.x, srcLoc.y));
			srcLoc = SequenceUtil.getSnappedLocation(request.getSource(),srcLoc);
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
		p = SequenceUtil.getSnappedLocation(request.getSource(),p);
		
		if (KeyboardHandler.getKeyboardHandler().isAnyPressed() ) {
			cmd.moveMessage(message, new Point(p.x - srcLoc.x, p.y - srcLoc.y));
		} else {
			cmd.nudgeMessage(message, new Point(p.x - srcLoc.x, p.y - srcLoc.y));
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
		Point loc = request.getLocation();		
		Point srcLoc = (Point)request.getExtendedData().get(CLICK_LOCATION_KEY); 
		if (srcLoc == null) {
			srcLoc = loc.getCopy();
			srcLoc = ViewUtilities.controlToViewer(getHost().getViewer(), new Point(srcLoc.x, srcLoc.y));
			srcLoc = SequenceUtil.getSnappedLocation(request.getSource(),srcLoc);
			request.getExtendedData().put(CLICK_LOCATION_KEY, srcLoc);
		}
		
		Point p = ViewUtilities.controlToViewer(getHost().getViewer(), new Point(loc.x, loc.y));				
		p = SequenceUtil.getSnappedLocation(request.getSource(),p);
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

		int deltaY = p.y - srcLoc.y;		
//		Rectangle r = feedbackFigure.getBounds();		
//		feedbackFigure.setLocation(new Point(originalFeedbackLoc.x,originalFeedbackLoc.y + deltaY));
		
		Point a1 = originalSourceAnchor.getReferencePoint();
		Point a2 = originalTargetAnchor.getReferencePoint();
		con.setSourceAnchor(new XYAnchor(new Point(a1.x,a1.y+deltaY)));
		con.setTargetAnchor(new XYAnchor(new Point(a2.x,a2.y+deltaY)));
		
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

	private GhostImageFigure feedbackFigure;
	public ConnectionAnchor originalSourceAnchor;
	public ConnectionAnchor originalTargetAnchor;
	public Point originalFeedbackLoc;
}
