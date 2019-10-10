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
 *  Antonio Campesino (Ericsson) antonio.campesino.robles@ericsson.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.runtime.test.debug;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.MeasurementUnit;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Column;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphFactory;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Row;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.uml2.uml.Interaction;

public class DebugDiagramRootEditPart extends DiagramRootEditPart {
	private static final boolean GRID_ENABLED = isGridEnabled(); 
	
	private static boolean isGridEnabled() {
		String val = System.getProperty("org.eclipse.papyrus.uml.diagram.sequence.debug.GridEnable","false");
		if (val.isEmpty())
			val = "true";
		return Boolean.parseBoolean(val);
	}
	
	public DebugDiagramRootEditPart() {
		super();
	}

	public DebugDiagramRootEditPart(MeasurementUnit mu) {
		super(mu);
	}

	@Override
	protected ScalableFreeformLayeredPane createScaledLayers() {
		interactionLayoutFigure = new InteractionLayoutFigure();
		ScalableFreeformLayeredPane  pane = super.createScaledLayers();
		//pane.add(interactionLayoutFigure, "InteractionLayoutDebug", 0);
		pane.add(interactionLayoutFigure, "InteractionLayoutDebug2");
		return pane;
	}
	
	public InteractionGraph getInteractionGraph() {
		return graph;
	}
	
	private IFigure interactionLayoutFigure;
	private InteractionGraph graph;
	
	private class InteractionLayoutFigure extends FreeformLayer {
		
		public Dimension getPreferredSize(int wHint, int hHint) {
			return new Dimension();
		}

		/**
		 * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
		 */
		protected void paintFigure(Graphics graphics) {
			super.paintFigure(graphics);
			if (GRID_ENABLED)
				paintGrid(graphics);
		}

		/**
		 * Paints the grid. Sub-classes can override to customize the grid's look.
		 * If this layer is being used with SnapToGrid, this method will only be
		 * invoked when the {@link SnapToGrid#PROPERTY_GRID_VISIBLE visibility}
		 * property is set to true.
		 * 
		 * @param g
		 *            The Graphics object to be used to do the painting
		 * @see FigureUtilities#paintGrid(Graphics, IFigure, Point, int, int)
		 */
		protected void paintGrid(Graphics g) {
			if (getContents() == null)
				return;
			Diagram dia = (Diagram)getContents().getModel();
			Interaction interaction = (Interaction)dia.getElement();
			if (graph == null)
				graph = InteractionGraphFactory.getInstance().createInteractionGraph(interaction, dia, getViewer());
			else
				graph.reset();

			Rectangle bounds = getBounds();
			Color oldFc = g.getForegroundColor();
			Color oldBg = g.getBackgroundColor();
			g.setForegroundColor(ColorConstants.red);
			g.setBackgroundColor(TRANSPARENT);
			g.setLineStyle(SWT.LINE_DASH);
			try {
				for (Column col : graph.getColumns()) {
					int x = col.getXPosition();
					g.drawLine(x, bounds.y, x, bounds.y + bounds.height);
				}
				
				for (Row row : graph.getRows()) {
					int y = row.getYPosition();
					g.drawLine(bounds.x, y, bounds.x + bounds.width, y);
				}	
			} catch (Throwable th) {} 
			g.setLineStyle(SWT.LINE_SOLID);
			g.setForegroundColor(oldFc);
			g.setBackgroundColor(oldBg);
		}
		
	}	
	private static final Color TRANSPARENT = new Color(Display.getDefault(),0,0,0,0);  
}


