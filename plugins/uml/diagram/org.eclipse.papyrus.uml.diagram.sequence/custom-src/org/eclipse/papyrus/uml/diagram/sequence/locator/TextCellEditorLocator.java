/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
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
package org.eclipse.papyrus.uml.diagram.sequence.locator;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

public class TextCellEditorLocator implements CellEditorLocator {

	/**
	 * @generated
	 */
	private WrappingLabel wrapLabel;

	/**
	 * @generated
	 */
	public TextCellEditorLocator(WrappingLabel wrapLabel) {
		this.wrapLabel = wrapLabel;
	}

	/**
	 * @generated
	 */
	public WrappingLabel getWrapLabel() {
		return wrapLabel;
	}

	/**
	 * @generated
	 */
	@Override
	public void relocate(CellEditor celleditor) {
		Text text = (Text) celleditor.getControl();
		Rectangle rect = getWrapLabel().getTextBounds().getCopy();
		getWrapLabel().translateToAbsolute(rect);
		if (!text.getFont().isDisposed()) {
			if (getWrapLabel().isTextWrapOn() && getWrapLabel().getText().length() > 0) {
				rect.setSize(new Dimension(text.computeSize(rect.width, SWT.DEFAULT)));
			} else {
				int avr = FigureUtilities.getFontMetrics(text.getFont()).getAverageCharWidth();
				rect.setSize(new Dimension(text.computeSize(SWT.DEFAULT, SWT.DEFAULT)).expand(avr * 2, 0));
			}
		}
		if (!rect.equals(new Rectangle(text.getBounds()))) {
			text.setBounds(rect.x, rect.y, rect.width, rect.height);
		}
	}
}
