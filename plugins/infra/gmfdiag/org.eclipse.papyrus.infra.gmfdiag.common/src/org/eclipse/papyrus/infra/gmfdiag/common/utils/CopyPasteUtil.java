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

package org.eclipse.papyrus.infra.gmfdiag.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IDiagramPreferenceSupport;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.DropObjectsRequest;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Different utility methods prior to paste command.
 * @since 3.0
 */
public class CopyPasteUtil {
	
	// TODO: should be provided configurable in prefs
	public static final int DEFAULT_AVOID_SUPERPOSITION_Y = 10;

	// TODO: should be provided configurable in prefs
	public static final int DEFAULT_AVOID_SUPERPOSITION_X = 10;


		/**
		 * Shift position to avoid overlap
		 *
		 * @param point
		 * @return
		 */
	public static Point shiftLayout(Point point) {
			return new Point(point.x + DEFAULT_AVOID_SUPERPOSITION_X, point.y + DEFAULT_AVOID_SUPERPOSITION_Y);
		}

		/**
		 * @param targetEditPart
		 * @return
		 */
		
		public static Point getCursorPosition(GraphicalEditPart targetEditPart) {
			Display display = Display.getDefault();
			org.eclipse.swt.graphics.Point cursorLocation = display.getCursorLocation();
			EditPartViewer viewer = targetEditPart.getViewer();
			Control control = viewer.getControl();
			org.eclipse.swt.graphics.Point point = control.toControl(cursorLocation);
			FigureCanvas figureCanvas = (FigureCanvas) control;
			Point location = figureCanvas.getViewport().getViewLocation();
			return new Point(point.x + location.x, point.y + location.y);
		}


		/**
		 * @param collection
		 * @return
		 */
	
		public static Collection<EObject> filterEObject(Collection<Object> collection) {
			List<EObject> eobjectList = new ArrayList<EObject>();
			for (Object object : collection) {
				if (object instanceof EObject) {
					eobjectList.add((EObject) object);
				}
			}
			return eobjectList;
		}


		/**
		 * @param copier
		 * @return
		 */
		
		public static Map<Object, EObject> transtypeCopier(EcoreUtil.Copier copier) {
			Map<Object, EObject> map = new HashMap<Object, EObject>();
			Set<Entry<EObject, EObject>> entrySet = copier.entrySet();
			for (Entry<EObject, EObject> entry : entrySet) {
				map.put(entry.getKey(), entry.getValue());
			}
			return map;
		}
		
		/**
		 * Look in the list of the children editParts, the one that accept the create view request
		 * 
		 * @param targetEditPart
		 * @param view
		 * @return
		 */
		public static GraphicalEditPart lookForTargetEditPart(GraphicalEditPart targetEditPart, View view){
			List<?> children = targetEditPart.getChildren();
			PreferencesHint prefs = ((IDiagramPreferenceSupport) targetEditPart.getRoot()).getPreferencesHint();
			CreateViewRequest request = new CreateViewRequest(view.getElement(), prefs);
			for (Object object : children) {
				if (object instanceof GraphicalEditPart) {
					GraphicalEditPart graphicalEditPart = (GraphicalEditPart) object;
					Command command = graphicalEditPart.getCommand(request);
					if (command != null) {
						return graphicalEditPart;
					}
				}
			}
			return null;	
		}
		
		/**
		 * Look in sub container for a dropcommand
		 *
		 * @param targetEditPart
		 * @param objectToDrop
		 * @return
		 */
		public static Command lookForCommandInSubContainer(GraphicalEditPart targetEditPart, List<EObject> objectToDrop) {
			List<?> children = targetEditPart.getChildren();
			DropObjectsRequest dropObjectsRequest = new DropObjectsRequest();
			for (Object object : children) {
				if (object instanceof GraphicalEditPart) {
					GraphicalEditPart graphicalEditPart = (GraphicalEditPart) object;
					Point center = graphicalEditPart.getFigure().getBounds().getCenter();
					dropObjectsRequest.setLocation(center);
					dropObjectsRequest.setObjects(objectToDrop);
					Command command = graphicalEditPart.getCommand(dropObjectsRequest);
					if (command != null) {
						return command;
					}
				}
			}
			return null;
		}

		/**
		 * 
		 *  Look in sub container for a dropcommand
		 *  
		 * @param targetEditPart
		 * @param semanticObjects
		 * @param newLocation
		 * @return
		 */
		public static Command lookForCommandInSubContainer(GraphicalEditPart targetEditPart, List<EObject> semanticObjects, Point newLocation) {
			List<?> children = targetEditPart.getChildren();
			DropObjectsRequest dropObjectsRequest = new DropObjectsRequest();
			for (Object object : children) {
				if (object instanceof GraphicalEditPart) {
					GraphicalEditPart graphicalEditPart = (GraphicalEditPart) object;
					dropObjectsRequest.setLocation(newLocation);
					dropObjectsRequest.setObjects(semanticObjects);
					Command command = graphicalEditPart.getCommand(dropObjectsRequest);
					if (command != null) {
						return command;	
					}
				}
				
			}
			return null;
		}

		
}
