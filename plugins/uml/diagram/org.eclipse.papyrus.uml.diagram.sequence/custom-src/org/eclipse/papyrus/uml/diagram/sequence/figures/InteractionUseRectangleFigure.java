/*****************************************************************************
 * Copyright (c) 2009 Atos Origin.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Atos Origin - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.PapyrusWrappingLabel;
import org.eclipse.papyrus.uml.diagram.common.figure.node.RoundedCompartmentFigure;

/**
 * The figure of the InteractionUse
 *
 * Change super type to support displaying stereotypes, modified by [Jin Liu(jin.liu@soyatec.com)]
 */
public class InteractionUseRectangleFigure extends RoundedCompartmentFigure {

	/** The centered label */
	protected PapyrusWrappingLabel centerLabel;


	/**
	 * Constructor.
	 *
	 */
	public InteractionUseRectangleFigure() {


		centerLabel = new PapyrusWrappingLabel();
		this.add(centerLabel);

		this.setLayoutManager(new AbstractLayout() {

			@Override
			public void layout(IFigure container) {
				// Set the Bounds of the header Label
				Rectangle containerBounds = container.getBounds();
				WrappingLabel header = getNameLabel();
				header.setBounds(new Rectangle(containerBounds.x(), containerBounds.y(), header.getPreferredSize().width, header.getPreferredSize().height));

				// Set Bounds of the Center Label
				Dimension centerSize = centerLabel.getPreferredSize();
				int centerWidth = centerSize.width;
				int centerHeight = centerSize.height;
				// Centered the label
				int centerX = containerBounds.getCenter().x() - centerWidth / 2;
				int centerY = containerBounds.getCenter().y() - centerHeight / 2;

				centerLabel.setBounds(new Rectangle(centerX, centerY, centerWidth, centerHeight));

			}

			@Override
			protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
				return new Dimension(-1, -1);
			}

		});

	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.RoundedCompartmentFigure#getLabelsDimension()
	 *
	 * @return
	 */
	@Override
	public Dimension getLabelsDimension() {
		if (null != nameLabel) {
			// Header decoration should have the header dimension
			return new Dimension(nameLabel.getBounds().width, nameLabel.getBounds().height);
		}
		return super.getLabelsDimension();
	}

	/**
	 * Getter of the centerLabel
	 *
	 * @return centerLabel (creates it if null)
	 */
	public PapyrusWrappingLabel getCenterLabel() {

		if (null == centerLabel) {
			centerLabel = new PapyrusWrappingLabel();
		}

		return centerLabel;
	}


}
