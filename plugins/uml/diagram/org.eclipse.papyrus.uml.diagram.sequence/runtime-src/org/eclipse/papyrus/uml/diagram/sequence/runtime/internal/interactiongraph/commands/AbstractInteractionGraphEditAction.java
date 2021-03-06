/*****************************************************************************
 * (c) Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands;

import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;

public abstract class AbstractInteractionGraphEditAction implements InteractionGraphEditAction {
	public AbstractInteractionGraphEditAction() {
	}

	@Override
	public boolean prepare(InteractionGraph interactionGraph) {
		return true;
	};


	@Override
	public void handleResult(CommandResult result) {
	}

	private InteractionGraph interactionGraph;
}
