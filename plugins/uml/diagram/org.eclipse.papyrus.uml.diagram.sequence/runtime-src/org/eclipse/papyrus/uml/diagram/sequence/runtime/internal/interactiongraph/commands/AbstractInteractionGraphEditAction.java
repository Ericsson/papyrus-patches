/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
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

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands;

import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;

public abstract class AbstractInteractionGraphEditAction implements InteractionGraphEditAction {
	public static final InteractionGraphEditAction UNEXECUTABLE_ACTION = new AbstractInteractionGraphEditAction(null) {
		@Override
		public boolean prepare() {
			return false;
		};

		@Override
		public boolean apply(InteractionGraph graph) {
			return false;
		}

	};

	public AbstractInteractionGraphEditAction(InteractionGraph interactionGraph) {
		this.interactionGraph = interactionGraph;
	}

	protected InteractionGraph getInteractionGraph() {
		return interactionGraph;
	}

	@Override
	public boolean prepare() {
		return true;
	};


	@Override
	public void handleResult(CommandResult result) {
	}

	private InteractionGraph interactionGraph;
}
