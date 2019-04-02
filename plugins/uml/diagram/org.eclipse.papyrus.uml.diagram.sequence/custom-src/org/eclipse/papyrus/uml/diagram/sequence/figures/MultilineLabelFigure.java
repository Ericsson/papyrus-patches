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
package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.PapyrusWrappingLabel;

public class MultilineLabelFigure extends PapyrusWrappingLabel {

	/**
	 * @generated
	 */
	public MultilineLabelFigure() {
		this.setTextAlignment(PositionConstants.CENTER);
		this.setAlignment(PositionConstants.CENTER);
		this.setBackgroundColor(getBackgroundColor());
		this.setTextWrap(true);
		this.setTextJustification(PositionConstants.CENTER);
	}
}