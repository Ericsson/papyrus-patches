package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CLifeLineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.uml2.uml.Lifeline;

public interface ViewConstants {
	static final int DEFAULT_GATE_SIZE = 12;
	
	public static int getLifelineHeaderHeight(EditPartViewer viewer, Lifeline lf) {
		View vw = ViewUtilities.getViewForElement((View)viewer.getRootEditPart().getContents().getModel(), lf);
		CLifeLineEditPart ep = (CLifeLineEditPart)viewer.getEditPartRegistry().get(vw);
		return ep.getStickerHeight();
	}
	
	public static Rectangle DEFAULT_LIFELINE_YPOS(EditPartViewer viewer, Lifeline lf, Rectangle rect) {
		View vw = ViewUtilities.getViewForElement((View)viewer.getRootEditPart().getContents().getModel(), lf);
		Rectangle r = ViewUtilities.getBounds(viewer, vw);
		rect.y = r.y; 
		rect.height = r.height;
		return rect;
	}
}
