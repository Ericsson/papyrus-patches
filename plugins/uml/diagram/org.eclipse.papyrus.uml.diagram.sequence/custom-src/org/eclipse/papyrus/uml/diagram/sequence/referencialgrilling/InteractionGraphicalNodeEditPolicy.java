/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import java.util.Map;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.FeedbackHelper;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeConnectionRequest;
import org.eclipse.gmf.runtime.draw2d.ui.figures.BaseSlidableAnchor;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.NodeEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramEditPartsUtil;
import org.eclipse.papyrus.infra.services.edit.utils.RequestParameterConstants;
import org.eclipse.papyrus.uml.diagram.sequence.edit.helpers.AnchorHelper;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.CustomGraphicalNodeEditPolicy;

/**
 *This class overload all creation of link between lifelines
 */
public class InteractionGraphicalNodeEditPolicy extends CustomGraphicalNodeEditPolicy implements IGrillingEditpolicy{


	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.GraphicalNodeEditPolicy#getConnectionCreateCommand(org.eclipse.gef.requests.CreateConnectionRequest)
	 *
	 * @param request
	 * @return
	 */
	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		ConnectionAnchor anchor = ((NodeEditPart)request.getTargetEditPart()).getSourceConnectionAnchor(request);
		if(DiagramEditPartsUtil.isSnapToGridActive(getHost())){
			//This part is very peculiar for lost and found message because the anchor is not standard.
			if( anchor instanceof AnchorHelper.InnerPointAnchor){
				PrecisionPoint pt = BaseSlidableAnchor.parseTerminalString(((AnchorHelper.InnerPointAnchor)anchor).getTerminal());
				PrecisionRectangle ptOnScreen=new PrecisionRectangle( pt.x,  pt.y,0,0);
				SimpleSnapHelper.snapAPoint(ptOnScreen, getHost().getRoot());
				computeSourcePosition(request, new PrecisionPoint(ptOnScreen.x, ptOnScreen.y));
				Map<Object, Object> parameters = request.getExtendedData();
				parameters.put(RequestParameterConstants.EDGE_SOURCE_POINT, request.getLocation().getCopy());
			}
		}
		return super.getConnectionCreateCommand(request);
	}


	/**
	 * This method update the request in order to make the point at the correctposition on the grill.
	 * @param request the request
	 * @param wanted the position has we want in the serialization
	 */
	protected  void computeSourcePosition(CreateConnectionRequest request, PrecisionPoint wanted){

		ConnectionAnchor anchor = ((NodeEditPart)request.getTargetEditPart()).getSourceConnectionAnchor(request);
		if(DiagramEditPartsUtil.isSnapToGridActive(getHost())){
			if( anchor instanceof AnchorHelper.InnerPointAnchor){
				PrecisionPoint resultedPoint = BaseSlidableAnchor.parseTerminalString(((AnchorHelper.InnerPointAnchor)anchor).getTerminal());
				while (resultedPoint.getDistance(wanted)>5 ){
					Point original= request.getLocation().getCopy();
					PrecisionPoint diff=new PrecisionPoint(original.x-resultedPoint.x,original.y- resultedPoint.y);
					PrecisionRectangle ptOnScreen=new PrecisionRectangle( resultedPoint.x,  resultedPoint.y,0,0);
					SimpleSnapHelper.snapAPoint(ptOnScreen, getHost().getRoot());
					PrecisionPoint Result=new PrecisionPoint(ptOnScreen.x+diff.x, ptOnScreen.y+diff.y);
					request.setLocation(Result);
					anchor = ((NodeEditPart)request.getTargetEditPart()).getSourceConnectionAnchor(request);
					resultedPoint = BaseSlidableAnchor.parseTerminalString(((AnchorHelper.InnerPointAnchor)anchor).getTerminal());
				}
			}
		}
	}
	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultGraphicalNodeEditPolicy#getConnectionAndRelationshipCompleteCommand(org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest)
	 *
	 * @param request
	 * @return
	 */
	@Override
	protected Command getConnectionAndRelationshipCompleteCommand(CreateConnectionViewAndElementRequest request) {
		Command cmd= super.getConnectionAndRelationshipCompleteCommand(request);
		return cmd;
	}
	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.GraphicalNodeEditPolicy#createDummyConnection(org.eclipse.gef.Request)
	 *
	 * @param req
	 * @return
	 */
	@Override
	protected Connection createDummyConnection(Request req) {
		//	if(req.isSnapToEnabled()){
		if(req instanceof  CreateUnspecifiedTypeConnectionRequest){
			CreateUnspecifiedTypeConnectionRequest request2= (CreateUnspecifiedTypeConnectionRequest)req;
			if(DiagramEditPartsUtil.isSnapToGridActive(getHost())){
				ConnectionAnchor anchor = ((NodeEditPart)request2.getTargetEditPart()).getSourceConnectionAnchor(request2);
				//This part is very peculiar for lost and found message because the anchor is not standard.
				if( anchor instanceof AnchorHelper.InnerPointAnchor){
					PrecisionPoint pt = BaseSlidableAnchor.parseTerminalString(((AnchorHelper.InnerPointAnchor)anchor).getTerminal());
					PrecisionRectangle ptOnScreen=new PrecisionRectangle( pt.x,  pt.y,0,0);
					SimpleSnapHelper.snapAPoint(ptOnScreen, getHost().getRoot());
					computeSourcePosition(request2, new PrecisionPoint(ptOnScreen.x, ptOnScreen.y));

				}
			}
		}
		//	}
		return super.createDummyConnection(req);
	}
	/**
	 * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#showSourceFeedback(org.eclipse.gef.Request)
	 *
	 * @param request
	 */
	@Override
	public void showSourceFeedback(Request request) {

		super.showSourceFeedback(request);
	}
	/**
	 * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getFeedbackHelper(org.eclipse.gef.requests.CreateConnectionRequest)
	 *
	 * @param request
	 * @return
	 */
	@Override
	protected FeedbackHelper getFeedbackHelper(CreateConnectionRequest request) {
		return super.getFeedbackHelper(request);
	}



}
