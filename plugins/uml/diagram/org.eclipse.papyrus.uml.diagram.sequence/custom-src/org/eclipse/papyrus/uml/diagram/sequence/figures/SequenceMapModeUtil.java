package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeUtil;

public class SequenceMapModeUtil {

	public static IMapMode getMapModel(IFigure figure) {
		IMapMode curMapMode = MapModeUtil.getMapMode(figure);
		return curMapMode == null ? MapModeUtil.getMapMode() : curMapMode;
	}
}
