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

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import java.util.ArrayList;

import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.notation.DecorationNode;
import org.eclipse.uml2.uml.Interaction;

/**
 * the goal of theses classes is to compute elements owned by Interaction operands or by interaction
 * @since 3.0
 */
public interface IComputeOwnerHelper {

	/**
	 *
	 * @param domain the domain to execute commands
	 * @param rows the list of rows from the grid
	 * @param columns the list of columns from the grid
	 * @param interaction the interaction where is created the diagram
	 * @param grid the grid.
	 */
	public void updateOwnedByInteractionOperand(EditingDomain domain, ArrayList<DecorationNode> rows, ArrayList<DecorationNode> columns, Interaction interaction, GridManagementEditPolicy grid);

}