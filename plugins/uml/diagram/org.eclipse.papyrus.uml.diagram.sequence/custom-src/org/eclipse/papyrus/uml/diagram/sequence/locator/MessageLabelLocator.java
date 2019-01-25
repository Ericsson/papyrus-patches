package org.eclipse.papyrus.uml.diagram.sequence.locator;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.infra.gmfdiag.common.locator.PapyrusLabelLocator;

public class MessageLabelLocator extends PapyrusLabelLocator {

	public MessageLabelLocator(IFigure parent, Point offSet, int alignment) {
		super(parent, offSet, alignment);
	}

	public MessageLabelLocator(IFigure parent, Rectangle bounds, int alignment) {
		super(parent, bounds, alignment);
	}

	public void relocate(IFigure target) {

		target.setSize(new Dimension(target.getPreferredSize().width, target.getPreferredSize().height));

		// Calculate the position
		Point ref = getReferencePoint();
		
		Point location = ref.getCopy();
		location.translate(getOffset());
		location.translate(-1 * target.getBounds().width / 2, -1 * target.getBounds().height / 2);

		// Translate the position according to the justification
		switch (getTextAlignment()) {
		case PositionConstants.LEFT:
			location.translate(target.getBounds().width / 2, 0);
			break;
		case PositionConstants.RIGHT:
			location.translate(-target.getBounds().width / 2, 0);
			break;
		case PositionConstants.CENTER:
			break;
		default:
			break;
		}

		// Set the location
		target.setLocation(location);
	}
}
