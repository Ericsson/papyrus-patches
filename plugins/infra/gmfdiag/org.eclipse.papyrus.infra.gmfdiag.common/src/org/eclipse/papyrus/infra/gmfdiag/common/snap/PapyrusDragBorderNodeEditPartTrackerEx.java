/*****************************************************************************
 * Copyright (c) 2013 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *		CEA LIST - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.snap;

import org.eclipse.gef.EditPart;

/**
 *
 * @author Vincent Lorenzo
 *
 */
public class PapyrusDragBorderNodeEditPartTrackerEx extends PapyrusDragEditPartsTrackerEx {

	/**
	 *
	 * Constructor.
	 *
	 * @param sourceEditPart
	 */
	public PapyrusDragBorderNodeEditPartTrackerEx(EditPart sourceEditPart) {
		this(sourceEditPart, false, false, true);
	}

	/**
	 *
	 * Constructor.
	 *
	 * @param sourceEditPart
	 * @param snapOnCorners
	 * @param snapOnMiddles
	 * @param snapOnCenter
	 */
	public PapyrusDragBorderNodeEditPartTrackerEx(EditPart sourceEditPart, boolean snapOnCorners, boolean snapOnMiddles, boolean snapOnCenter) {
		super(sourceEditPart, snapOnCorners, snapOnMiddles, snapOnCenter);
	}

	/**
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.snap.copy.CustomDragEditPartsTracker#isMove()
	 *
	 * @return
	 */
	@Override
	protected boolean isMove() {
		return true;// see org.eclipse.gmf.runtime.diagram.ui.editparts.AbstractBorderItemEditPart.getEditPartTracker
	}

}
