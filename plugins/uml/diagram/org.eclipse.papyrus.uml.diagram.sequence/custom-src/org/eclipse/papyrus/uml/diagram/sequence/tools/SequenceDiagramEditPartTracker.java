/*****************************************************************************
 * Copyright (c) 2019 CEA LIST and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.tools;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.papyrus.infra.gmfdiag.common.snap.copy.SelectEditPartTracker;

/**
 * @author etxacam
 *
 */
public class SequenceDiagramEditPartTracker extends SelectEditPartTracker {

	public SequenceDiagramEditPartTracker(EditPart owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Command getCommand() {
		if (getTargetEditPart() == null) {
			return null;
		}
		return getTargetEditPart().getCommand(getTargetRequest());
	}

}
