package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.PolylineShape;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;

public class SequencePolylineShape extends PolylineShape {

	public IMapMode getMapModel() {
		return SequenceMapModeUtil.getMapModel(this);
	}
}
