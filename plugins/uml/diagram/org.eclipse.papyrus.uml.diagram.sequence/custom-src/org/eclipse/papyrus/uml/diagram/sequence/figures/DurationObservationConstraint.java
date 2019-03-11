package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class DurationObservationConstraint extends WrappingLabel {

	private static final Font THIS_FONT = new Font(Display.getCurrent(), "SANS", 9, SWT.NORMAL);

	private WrappingLabel fDurationLabel;

	public DurationObservationConstraint() {
		this.setTextWrap(true);
		this.setTextJustification(PositionConstants.CENTER);
		this.setForegroundColor(ColorConstants.black);
		this.setFont(THIS_FONT);
	}

	public WrappingLabel getDurationLabel() {
		return fDurationLabel;
	}
}