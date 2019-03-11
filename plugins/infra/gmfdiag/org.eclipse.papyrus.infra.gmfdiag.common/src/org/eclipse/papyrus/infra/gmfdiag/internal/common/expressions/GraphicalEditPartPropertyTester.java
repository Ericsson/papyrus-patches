/*****************************************************************************
 * Copyright (c) 2014, 2016 CEA LIST, Christian W. Damus, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Gabriel Pascual (ALL4TEC) gabriel.pascual@all4tec.net - Initial API and implementation
 *   Christian W. Damus - bug 485220
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.internal.common.expressions;

import static com.google.common.base.Objects.equal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramEditPartsUtil;

/**
 * Property tester to enable different actions in diagrams.
 * 
 * @author Gabriel Pascual
 *
 */
public class GraphicalEditPartPropertyTester extends PropertyTester {

	private static final String SEMANTIC_DELETION_PROPERTY = "isSemanticDeletion"; //$NON-NLS-1$

	private static final String CAN_DELETE_PROPERTY = "canDelete"; //$NON-NLS-1$

	public GraphicalEditPartPropertyTester() {
		super();
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		boolean result = false;

		switch (property) {
		case SEMANTIC_DELETION_PROPERTY:
			result = equal(expectedValue, DiagramEditPartsUtil.isSemanticDeletion((IGraphicalEditPart) receiver));
			break;
		case CAN_DELETE_PROPERTY:
			boolean canDelete = Activator.getInstance().getGraphicalDeletionHelper().canDelete((IGraphicalEditPart) receiver);
			result = equal(expectedValue, canDelete);
			break;
		}

		return result;
	}
}
