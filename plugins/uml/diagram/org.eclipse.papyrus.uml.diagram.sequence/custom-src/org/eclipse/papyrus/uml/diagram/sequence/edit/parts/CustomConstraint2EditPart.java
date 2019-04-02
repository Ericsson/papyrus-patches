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

import org.eclipse.draw2d.IFigure;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.common.figure.node.ConstraintFigure;
import org.eclipse.papyrus.uml.diagram.sequence.util.ElementIconUtil;
import org.eclipse.swt.graphics.Image;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomConstraint2EditPart extends Constraint2EditPart {

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CustomConstraint2EditPart(View view) {
		super(view);
	}

	@Override
	protected void setLabelIconHelper(IFigure figure, Image icon) {
		if (figure instanceof ConstraintFigure) {
			((ConstraintFigure) figure).setAppliedStereotypeIcon(icon);
		}
		super.setLabelIconHelper(figure, icon);
	}

	@Override
	protected Image getLabelIcon() {
		return ElementIconUtil.getLabelIcon(this);
	}
}
