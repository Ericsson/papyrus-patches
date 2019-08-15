package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.uml2.uml.Lifeline;

public interface ViewConstants {
	static final int DEFAULT_GATE_SIZE = 12;
	
	public static Rectangle DEFAULT_LIFELINE_YPOS(EditPartViewer viewer, Lifeline lf, Rectangle rect) {
		View vw = ViewUtilities.getViewWithType((View)viewer.getRootEditPart().getContents().getModel(), InteractionInteractionCompartmentEditPart.VISUAL_ID);
		Rectangle r = ViewUtilities.getBounds(viewer, ViewUtilities.getViewForElement(vw, lf));
		rect.y = r.y; 
		rect.height = r.height;
		return rect;
	}
}
