/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
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

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.AllowResizeAffixedNodeAlignmentEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.StateInvariantEditPart;

/**
 * @since 3.0
 *
 */
public class LifeLineResizeAffixedNodeEditPolicy extends AllowResizeAffixedNodeAlignmentEditPolicy {

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.editpolicies.ConstrainedItemBorderLayoutEditPolicy#createChildEditPolicy(org.eclipse.gef.EditPart)
	 *
	 * @param child
	 * @return
	 */
	@Override
	protected EditPolicy createChildEditPolicy(EditPart child) {
		if (child instanceof StateInvariantEditPart) {
			return new StateInvariantResizableEditPolicy();
		}
		return super.createChildEditPolicy(child);

	}

}
