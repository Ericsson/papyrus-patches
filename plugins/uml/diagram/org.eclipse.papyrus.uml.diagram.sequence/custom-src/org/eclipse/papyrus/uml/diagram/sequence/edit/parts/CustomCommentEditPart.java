/*****************************************************************************
 * Copyright (c) 2010 CEA
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
 *   Soyatec - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.notation.FillStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomCommentEditPart extends CommentEditPart {

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CustomCommentEditPart(View view) {
		super(view);
	}


	@Override
	protected void setLineWidth(int width) {
		if (primaryShape instanceof NodeFigure) {
			((NodeFigure) primaryShape).setLineWidth(width);
		}
		super.setLineWidth(width);
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		refreshTransparency();
	}

	@Override
	protected void refreshTransparency() {
		FillStyle style = (FillStyle) getPrimaryView().getStyle(NotationPackage.Literals.FILL_STYLE);
		if (style != null) {
			setTransparency(style.getTransparency());
		}
	}
}
