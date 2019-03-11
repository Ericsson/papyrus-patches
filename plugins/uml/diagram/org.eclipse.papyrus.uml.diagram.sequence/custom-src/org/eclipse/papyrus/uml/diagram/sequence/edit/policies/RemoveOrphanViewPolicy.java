/*****************************************************************************
 * Copyright (c) 2010, 2017 CEA LIST and Others.
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
 *   Fanch BONNABESSE (ALL4TEC) fanch.bonnabesse@all4tec.net - Bug 528499
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import org.eclipse.gmf.runtime.notation.BasicCompartment;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.OrphanViewPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineNameEditPart;

/**
 * this policy is used to suppress orphan node view in GMF view the policy to remove orphan
 * connection is more complex. It is dependent of the diagram. see remove OrphanConnectionView
 * policy.
 *
 * @deprecated since 5.0. Useless. Helper Advices remove views.
 *
 */
@Deprecated
public class RemoveOrphanViewPolicy extends OrphanViewPolicy {

	public String[] notOrphanNode = { LifelineNameEditPart.VISUAL_ID };

	public RemoveOrphanViewPolicy() {
		super();
		init(notOrphanNode);
	}

	@Override
	protected boolean isOrphaned(View view) {
		// Since added support of ShapeCompartment for NamedElement(See ShapeCompartmentEditPolicy.CreateShapeCompartmentViewCommand, the element is not set for ShapeCompartment),
		// There's a bug about removing orphaned views. Some ShapeCompartments unrelated to current context would be removed, this will block the undo/redo actions.
		if (view instanceof BasicCompartment) {
			return ((BasicCompartment) view).getElement() == null;
		}
		return super.isOrphaned(view);
	}
}
