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

import org.eclipse.draw2d.PolylineShape;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;

public class SequencePolylineShape extends PolylineShape {

	public IMapMode getMapModel() {
		return SequenceMapModeUtil.getMapModel(this);
	}
}
