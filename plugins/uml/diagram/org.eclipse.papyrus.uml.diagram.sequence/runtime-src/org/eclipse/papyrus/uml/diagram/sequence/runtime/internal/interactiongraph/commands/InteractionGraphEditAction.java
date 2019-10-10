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

public interface InteractionGraphEditAction {
	public static final InteractionGraphEditAction UNEXECUTABLE_ACTION = new AbstractInteractionGraphEditAction() {
		@Override
		public boolean isApplicable(InteractionGraph interactionGraph) {
			return true;
		};

		@Override
		public boolean prepare(InteractionGraph interactionGraph) {
			return false;
		};

		@Override
		public boolean apply(InteractionGraph graph) {
			return false;
		}

		@Override
		public void postApply(InteractionGraph graph) {}

	};


	public boolean isApplicable(InteractionGraph interactionGraph);
	public boolean prepare(InteractionGraph interactionGraph);
	public boolean apply(InteractionGraph graph);
	public void postApply(InteractionGraph graph);
	public void handleResult(CommandResult result);
}
