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
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.IPapyrusEditPart;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomDurationConstraintInMessageAppliedStereotypeEditPart extends DurationConstraintInMessageAppliedStereotypeEditPart implements IPapyrusEditPart {

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CustomDurationConstraintInMessageAppliedStereotypeEditPart(View view) {
		super(view);
	}

	/**
	 * @Override
	 */
	@Override
	public IFigure getPrimaryShape() {
		return getFigure();
	}
}
