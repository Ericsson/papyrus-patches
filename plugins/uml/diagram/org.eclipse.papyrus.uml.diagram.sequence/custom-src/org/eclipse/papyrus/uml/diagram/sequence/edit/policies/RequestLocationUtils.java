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

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.LocationRequest;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.util.SelectInDiagramHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;

/**
 * @author Antonio Campesino
 *
 */
public class RequestLocationUtils {
	public static final String CLICK_LOCATION_KEY = "clickLocation";
	public static final String ORIG_LOCATION_KEY = "origLocation";

	public static Point calculateRequestDragDelta(Request request, Point point, GraphicalEditPart ep, Point figureDragPoint) {
		EditPartViewer viewer = ep.getViewer();
		Point loc = point.getCopy();
		loc = ViewUtilities.controlToViewer(viewer, loc);
		if ((viewer instanceof ScrollingGraphicalViewer) && (viewer.getControl() instanceof FigureCanvas)){
			SelectInDiagramHelper.exposeLocation((FigureCanvas)viewer.getControl(),loc);
		}
	
		Point srcLoc = (Point)request.getExtendedData().get(CLICK_LOCATION_KEY);		
		if (srcLoc == null) {
			srcLoc = loc.getCopy();
			request.getExtendedData().put(CLICK_LOCATION_KEY, srcLoc);
		}

		Point orig = (Point)request.getExtendedData().get(ORIG_LOCATION_KEY);
		if (orig == null) {
			orig = figureDragPoint.getCopy();
			request.getExtendedData().put(ORIG_LOCATION_KEY, figureDragPoint);
		}
		
		loc = SequenceUtil.getSnappedLocation(ep,loc);
		Point delta = new Point(loc.x - srcLoc.x + orig.x, loc.y - srcLoc.y + orig.y);
		delta = SequenceUtil.getSnappedLocation(ep,delta);
		delta.setLocation(delta.x - orig.x, delta.y - orig.y);
		return delta;				
	}

	public static Point calculateRequestDragDelta(LocationRequest request, GraphicalEditPart ep, Point figureDragPoint) {
		return calculateRequestDragDelta(request, request.getLocation(), ep, figureDragPoint);
	}
}
