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

package org.eclipse.papyrus.uml.diagram.sequence.util;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;

/**
 * The purpose of this class is to provide method to transform local coordinates to screen coordinate or absolute coordinate
 *
 * @since 4.0
 */
public class CoordinateReferentialUtils {

	/**
	 * this method transform a position seen on the screen referential to the position with the referential of the diagram
	 *
	 * @param locationOnScreen
	 *            the position on the screen
	 * @param graphicalViewer
	 * @return the position on the diagram
	 */
	public static Point transformPointFromScreenToDiagramReferential(Point locationOnScreen, GraphicalViewer graphicalViewer) {
		FigureCanvas figureCanvas = (FigureCanvas) graphicalViewer.getControl();
		org.eclipse.draw2d.geometry.Point locationScreen = figureCanvas.getViewport().getViewLocation();
		ZoomManager zoomManager = (ZoomManager) graphicalViewer.getProperty(ZoomManager.class.toString());
		Point locationOnDiagram = new Point(locationOnScreen.x + locationScreen.x, locationOnScreen.y + locationScreen.y);
		if (zoomManager != null) {
			locationOnDiagram = locationOnDiagram.getScaled(1 / zoomManager.getZoom());
		}
		return locationOnDiagram;

	}

	/**
	 * this method transform a position on the diagram to the position on the screen
	 *
	 * @param locationOnDiagram
	 *            shall be never null
	 * @param graphicalViewer
	 * @return the position on the screen must be never null
	 */
	public static Point transformPointFromDiagramToScreenReferential(Point locationOnDiagram, GraphicalViewer graphicalViewer) {
		FigureCanvas figureCanvas = (FigureCanvas) graphicalViewer.getControl();
		org.eclipse.draw2d.geometry.Point locationScreen = figureCanvas.getViewport().getViewLocation();
		ZoomManager zoomManager = (ZoomManager) graphicalViewer.getProperty(ZoomManager.class.toString());
		Point locationOnScreen = locationOnDiagram.getCopy();
		if (zoomManager != null) {
			locationOnScreen = locationOnScreen.getScaled(zoomManager.getZoom());
		}
		locationOnScreen = new Point(locationOnScreen.x - locationScreen.x, locationOnScreen.y - locationScreen.y);
		return locationOnScreen;

	}

	/**
	 * return the relative position of the figure according the diagram
	 *
	 * @param figure
	 *            the figure from which we want the position
	 *            never null
	 * @param diagramEditPart
	 *            the editpart of the diagram
	 * @return
	 */
	public static Point getFigurePositionRelativeToDiagramReferential(IFigure figure, DiagramEditPart diagramEditPart) {
		Rectangle bounds = figure.getBounds().getCopy();
		figure.getParent().translateToAbsolute(bounds);
		Point relativeToParent = bounds.getTopLeft().getCopy();
		diagramEditPart.getFigure().translateToRelative(relativeToParent);
		return relativeToParent;
	}
}
