/*****************************************************************************
 * Copyright (c) 2011 CEA LIST.
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
package org.eclipse.papyrus.infra.gmfdiag.common.utils;

import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.BorderedBorderItemEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.CompartmentEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;


/**
 * Class that contains utililty methods on top of GMF constructs.
 */
public class Util {

	/**
	 * Test if an EditPart is an Affixed Child Node or not
	 *
	 * @param ep
	 *            the editpart to test
	 * @return <ul>
	 *         <li> <code>true</code> if the editpart is an Affixed Child Node</li>
	 *         <li> <code>false</code>if not</li>
	 *         </ul>
	 */
	public static boolean isAffixedChildNode(EditPart ep) {
		if (ep instanceof BorderedBorderItemEditPart) {
			if (ep.getParent() instanceof CompartmentEditPart) {
				return false;
			} else if (ep.getParent() instanceof DiagramEditPart) {
				return false;
			}
			return true;
		}
		return false;
	}
}
