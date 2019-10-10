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

package org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.BendpointRequest;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.LocationRequest;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.commands.wrappers.GMFtoGEFCommandWrapper;
import org.eclipse.papyrus.uml.diagram.common.util.ViewUtils;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.InteractionGraphCommand;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.KeyboardHandler;
import org.eclipse.uml2.uml.Element;

/**
 * @author etxacam
 *
 */
public class InteractionGraphSelectionCommandProxy {
	public static final Object START_LOCATION =  "startLocation";
	public static final Object TRANSLATED_REQUEST =  "translatedRequest";

	public InteractionGraphSelectionCommandProxy() {		
	}
	
	// TODO: Needs to update the extended data and the updated fields in the original request.
	public void setRequest(Request request, EditPart editPart) {
		if (request != this.request || editPart != this.editPart) {
			this.request = request;
			this.proxiedRequest = null;
			this.graph = null;
			this.editPart = editPart;
		}
			
	}
	
	public Command getMultiSelectionCommand() {		
		graph = InteractionGraphRequestHelper.getOrCreateInteractionGraph(request, (GraphicalEditPart)editPart);
		List<GraphicalEditPart> selectedParts = graph.getEditPartViewer().getSelectedEditParts();
		if (selectedParts.size() <= 1)
			return null;
		
		if (request instanceof LocationRequest) {
			LocationRequest locRequest = (LocationRequest)request;
			Point p = locRequest.getLocation().getCopy();
			Command c = getMultiSelectionCommandImpl(p);
			locRequest.setLocation(p);
			return c;
		} else if (request instanceof ChangeBoundsRequest) {
			ChangeBoundsRequest req = (ChangeBoundsRequest)request;
			Rectangle r = getProxyItem().getBounds().getCopy();
			r.translate(req.getMoveDelta());
			return getMultiSelectionCommandImpl(r.getTopLeft());
		}
		return null;
	}
	
	protected Command getMultiSelectionCommandImpl(Point location) {
		if (!KeyboardHandler.getKeyboardHandler().isAnyPressed()) {
			GraphItem firstItem = getProxyItem();		
			GraphicalEditPart firstEditPart = firstItem.getEditPart();
			
			Request newReq = translateRequest(request,editPart,firstEditPart, (Point)request.getExtendedData().get(START_LOCATION));
			Command c = firstEditPart.getCommand(newReq);
			if (request != newReq)
				request.getExtendedData().putAll(newReq.getExtendedData());
			return c;
		} else {
			InteractionGraphCommand cmd = new InteractionGraphCommand(((IGraphicalEditPart) editPart).getEditingDomain(), "move selection", graph, null);
			List<GraphicalEditPart> selectedParts = graph.getEditPartViewer().getSelectedEditParts();			
			cmd.moveSelection(selectedParts.stream().map(d->(Element)ViewUtil.resolveSemanticElement((View)d.getModel())).
				filter(Predicate.isEqual(null).negate()).collect(Collectors.toList()), location);
			return GMFtoGEFCommandWrapper.wrap(cmd);
		}
	}
	
	private GraphItem getProxyItem() {
		InteractionGraph graph = InteractionGraphRequestHelper.getOrCreateInteractionGraph(request, (GraphicalEditPart)editPart);
		List<GraphicalEditPart> selectedParts = graph.getEditPartViewer().getSelectedEditParts();
		Set<GraphItem> nodes = selectedParts.stream().map(d->(Element)((View)d.getModel()).getElement()).
				filter(Predicate.isEqual(null).negate()).				
				map(d->graph.getItemFor(d)).
				filter(Predicate.isEqual(null).negate()).collect(Collectors.toSet());
		
		return nodes.stream().sorted((GraphItem e1, GraphItem e2)->Integer.compare(e1.getBounds().y, e2.getBounds().y)).
			findFirst().orElse(null);				
	}
	
	private Request translateRequest(Request req, EditPart source, EditPart target, Point startLocation) {
		if (source == target)
			return req;

		if (proxiedRequest == null)
			proxiedRequest = createRequest(req, source, target);
		
		
		InteractionGraph graph = InteractionGraphRequestHelper.getInteractionGraph(req);
		if (graph == null)
			return req;
		
		Element srcElem = (Element)((View)target.getModel()).getElement();
		GraphItem srcItem = graph.getItemFor(srcElem);
		Element trgElem = (Element)((View)target.getModel()).getElement();
		GraphItem trgItem = graph.getItemFor(trgElem);

		if (source instanceof ConnectionEditPart && !(target instanceof ConnectionEditPart)) {
			if (req instanceof BendpointRequest) {
				ChangeBoundsRequest request = (ChangeBoundsRequest)proxiedRequest;
				Node trgNode = graph.getNodeFor(trgElem); 
				if (trgNode == null)
					return req;
				Dimension diff = ((BendpointRequest) req).getLocation().getDifference(startLocation);
				request.setMoveDelta(new Point(diff.width, diff.height));
				request.getExtendedData().putAll(req.getExtendedData());
				return request;
			}
		}
		
		if (!(source instanceof ConnectionEditPart) && target instanceof ConnectionEditPart) {
			if (req instanceof ChangeBoundsRequest) {
				BendpointRequest request = (BendpointRequest)proxiedRequest;
				Point p = ((ChangeBoundsRequest) req).getMoveDelta().getCopy();
				p.translate(trgItem.getBounds().getCenter());
				setLocation(request, target, p);
				return request;
			}
		}

		if (req instanceof LocationRequest) {
			LocationRequest request = (LocationRequest)req;
			Rectangle srcRect = srcItem.getBounds();
			Rectangle trgRect = trgItem.getBounds();			
			Point p = request.getLocation();
			p.translate(-srcRect.x,-srcRect.y);
			p.translate(trgRect.x,trgRect.y);
			setLocation(request, srcItem.getEditPart(), p);
		}
		
		return req;
	}

	private Request createRequest(Request req, EditPart source, EditPart target) {
		if (source instanceof ConnectionEditPart && !(target instanceof ConnectionEditPart)) {
			// Translate from Connection to shapes command
			if (req instanceof BendpointRequest) {
				ChangeBoundsRequest request = new ChangeBoundsRequest();
				return request;
			}
		}
		
		if (!(source instanceof ConnectionEditPart) && target instanceof ConnectionEditPart) {
			if (req instanceof ChangeBoundsRequest) {
				BendpointRequest request = new BendpointRequest();
				request.setType(RequestConstants.REQ_MOVE_BENDPOINT);
				return request;
			}
		}
		
		return req;		
	}
	
	private void setLocation(LocationRequest req, EditPart source, Point p) {
		req.setLocation(p);
		if (req instanceof BendpointRequest) {
			((BendpointRequest) req).setSource((ConnectionEditPart)source);
		}
	}

	private InteractionGraph graph;
	private EditPart editPart;
	private Request request;
	private Request proxiedRequest;
}
