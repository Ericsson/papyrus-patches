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

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.uml2.uml.Element;

public class InteractionGraphEditActionBuilder {
	public InteractionGraphEditActionBuilder(String name) {
		this.action = new Action(name);
	}
	
	public InteractionGraphEditActionBuilder isApplicableIfExist(Element... els) {
		this.action.applicable = (InteractionGraph g)->Arrays.asList(els).stream().allMatch(				
				d->(g.getNodeFor(d) != null || g.getLinkFor(d) != null) && d.eResource() != null);
		return this;
	}

	public InteractionGraphEditActionBuilder isApplicable(Function<InteractionGraph, Boolean> applicable) {
		this.action.applicable = applicable;
		return this;
	}

	public InteractionGraphEditActionBuilder prepare(Function<InteractionGraph, Boolean> prepare) {
		this.action.prepare = prepare;
		return this;
	}
	
	public InteractionGraphEditActionBuilder prepare(Supplier<Boolean> prepare) {		
		this.action.prepare = (InteractionGraph g)->prepare.get();
		return this;
	}

	public <T> InteractionGraphEditActionBuilder apply(Function<InteractionGraph, T> apply) {
		this.action.apply = apply;
		return this;
	}

	public InteractionGraphEditActionBuilder apply(Runnable apply) {
		this.action.apply = (InteractionGraph g)->{apply.run(); return true;};
		return this;
	}

	public <T> InteractionGraphEditActionBuilder apply(Supplier<T> apply) {
		this.action.apply = (InteractionGraph g)->  {return apply.get();};
		return this;
	}

	public <T> InteractionGraphEditActionBuilder handleResult(BiConsumer<CommandResult, T> handleResult) {
		this.action.handleResult = handleResult;
		return this;
	}

	public <T> InteractionGraphEditActionBuilder handleResult(Consumer<T> handleResult) {
		this.action.handleResult = (BiConsumer<CommandResult, T>)(CommandResult r, T item) -> handleResult.accept(item);
		return this;
	}

	public <T> InteractionGraphEditActionBuilder postApply(BiConsumer<InteractionGraph, T> handleResult) {
		this.action.postApply = handleResult;
		return this;
	}

	public <T> InteractionGraphEditActionBuilder postApply(Consumer<T> handleResult) {
		this.action.postApply = (BiConsumer<InteractionGraph , T>)(InteractionGraph g, T item) -> handleResult.accept(item);
		return this;
	}

	InteractionGraphEditAction action() {
		return action;
	}
	
	private Action action;
	
	private static class Action implements InteractionGraphEditAction {
		public Action(String name) {
			this.name = name;
		}
		
		@Override
		public boolean isApplicable(InteractionGraph interactionGraph) {
			if (applicable == null)
				return true;
			return applicable.apply(interactionGraph);
		};

		@Override
		public boolean prepare(InteractionGraph interactionGraph) {
			if (prepare != null)
				return prepare.apply(interactionGraph);
			return true;
		};
		
		@Override
		public boolean apply(InteractionGraph graph) {
			if (apply == null)
				return true;
			item = apply.apply(graph);
			if (item instanceof Boolean) {
				return (Boolean)item;
			} else {
				return item != null;
			}
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public void postApply(InteractionGraph graph) {
			if (postApply != null) {
				((BiConsumer)postApply).accept(graph, item);
			}
		}


		@SuppressWarnings("unchecked")
		@Override
		public void handleResult(CommandResult result) {
			if (handleResult != null) {
				if (!result.getStatus().isOK())
					return;
				((BiConsumer)handleResult).accept(result, item);
			}
		}

		private String name;
		private Function<InteractionGraph, Boolean> applicable;
		private Function<InteractionGraph, Boolean> prepare;
		private Function<InteractionGraph, ?> apply;
		private BiConsumer<InteractionGraph, ?> postApply;
		private BiConsumer<CommandResult, ?> handleResult;
		private Object item;
	}
}
