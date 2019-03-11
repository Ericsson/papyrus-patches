/*****************************************************************************
 * Copyright (c) 2010 Atos Origin, CEA LIST, EclipseSource and others.
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
 *   Arthur Daussy - Bug 354622 - [ActivityDiagram] Object Flows selection prevent selecting other close elements.
 *   Céline Janssens (ALL4TEC) celine.janssens@all4tec.net - Bug 440230 - Margin Label
 *   EclipseSource - Bug 535519
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.figure.node;

import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.IPapyrusWrappingLabel;
import org.eclipse.swt.graphics.Image;

/**
 * This correct the bug where invisible label can be selected
 *
 * @author arthur daussy
 */
public class PapyrusWrappingLabel extends WrappingLabel implements IPapyrusWrappingLabel {

	/**
	 * A constant used for {@link #setLabelSize(int, int)}, {@link #setLabelHeight(int)}, {@link #setLabelWidth(int)}
	 * to indicate that the label size should be automatically derived from the current font size.
	 *
	 * @since 3.101
	 */
	public static final int AUTO_SIZE = -1;

	private int labelWidth = AUTO_SIZE;

	private int labelHeight = AUTO_SIZE;

	/**
	 * Constructor.
	 *
	 * @param image
	 */
	public PapyrusWrappingLabel(Image image) {
		super(image);
	}

	/**
	 * Constructor.
	 *
	 * @param text
	 */
	public PapyrusWrappingLabel(String text) {
		super(text);
	}

	/**
	 * Constructor.
	 *
	 */
	public PapyrusWrappingLabel() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param text
	 * @param image
	 */
	public PapyrusWrappingLabel(String text, Image image) {
		super(text, image);
	}

	/**
	 * Bug 354622 - [ActivityDiagram] Object Flows selection prevent selecting other close elements.
	 * On this bug bug come from that invisible label return true containsPoint(int, int) even if there invisible
	 *
	 * This is a temporary fix until the real issue described in Bug 363362
	 * (https://bugs.eclipse.org/bugs/show_bug.cgi?id=363362) is fixed by GMF.
	 *
	 * @see org.eclipse.draw2d.Figure#containsPoint(int, int)
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	@Override
	public boolean containsPoint(int x, int y) {
		if (isVisible()) {
			return super.containsPoint(x, y);
		}
		return false;
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.IPapyrusWrappingLabel#setMarginLabel(int, int)
	 *
	 * @param xMargin Vertical margin
	 * @param yMargin Horizontal margin
	 */
	@Override
	public void setMarginLabel(int xMargin, int yMargin) {

		this.setMarginLabel(xMargin, yMargin , xMargin, yMargin);

	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.IPapyrusWrappingLabel#setMarginLabel(int, int, int, int)
	 *
	 * @param leftMargin
	 * @param topMargin
	 * @param rightMargin
	 * @param bottomMargin
	 */
	@Override
	public void setMarginLabel(int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
		MarginBorder mb = new MarginBorder(topMargin, leftMargin, bottomMargin, rightMargin);
		this.setBorder(mb);
		repaint();
		revalidate();

	}

	/**
	 * Set the height of this Label, in pixels. Use {@linksetText #AUTO_SIZE} to
	 * compute the best size based on the current font size.
	 *
	 * @param labelHeight
	 * @since 3.101
	 */
	public void setLabelHeight(int labelHeight) {
		this.labelHeight = labelHeight;
	}

	/**
	 * Set the width of this Label, in pixels. Use {@link #AUTO_SIZE} to
	 * compute the best size based on the current font size.
	 *
	 * @param labelWidthgetStringExtents
	 * @since 3.101
	 */
	public void setLabelWidth(int labelWidth) {
		this.labelWidth = labelWidth;
	}

	/**
	 * Set the size (Width, Height) of this label, in pixels. Use {@link #AUTO_SIZE} to
	 * compute the best size based on the current font size.
	 *
	 * @param height
	 * @param width
	 * @since 3.101
	 */
	public void setLabelSize(int width, int height) {
		setLabelWidth(width);
		setLabelHeight(height);
	}

	/**
	 * @see org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel#getPreferredSize(int, int)
	 *
	 * @param wHint
	 * @param hHint
	 * @return
	 */
	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {
		//TODO Compute the best font size to fit the height, and truncate on width
		Dimension prefSize = super.getPreferredSize(wHint, hHint);
		if (labelHeight > 0) {
			prefSize.height = labelHeight;
		}
		if (labelWidth > 0) {
			prefSize.width = labelWidth;
		}
		return prefSize;
	}

}
