/*****************************************************************************
 * Copyright (c) 2015, 2016 Christian W. Damus and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.canonical;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.Switch;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.tools.util.TypeUtils;
import org.eclipse.papyrus.uml.diagram.common.canonical.DefaultUMLSemanticChildrenStrategy;
import org.eclipse.uml2.uml.Continuation;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.StateInvariant;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.util.UMLSwitch;

import com.google.common.collect.Lists;

/**
 * Custom semantic-children strategy for lifelines in sequence diagrams.
 */
public class LifelineSemanticChildrenStrategy extends DefaultUMLSemanticChildrenStrategy {

	private final Switch<Boolean> visualizedInteractionFragmentSwitch = new UMLSwitch<Boolean>() {
		@Override
		public Boolean caseInteractionFragment(InteractionFragment object) {
			return false;
		}

		@Override
		public Boolean caseContinuation(Continuation object) {
			return true;
		}

		@Override
		public Boolean caseDestructionOccurrenceSpecification(DestructionOccurrenceSpecification object) {
			return true;
		}

		@Override
		public Boolean caseExecutionSpecification(ExecutionSpecification object) {
			return true;
		}

		@Override
		public Boolean caseInteractionUse(InteractionUse object) {
			return true;
		}

		@Override
		public Boolean caseStateInvariant(StateInvariant object) {
			return true;
		}

		@Override
		public Boolean defaultCase(EObject object) {
			return false;
		}
	};

	public LifelineSemanticChildrenStrategy() {
		super();
	}

	@Override
	public List<? extends EObject> getCanonicalSemanticChildren(EObject semanticFromEditPart, View viewFromEditPart) {
		List<? extends EObject> _result;

		if (semanticFromEditPart instanceof Lifeline) {
			// Get the execution specifications covering it
			Lifeline lifeline = (Lifeline) semanticFromEditPart;

			List<EObject> result = Lists.newArrayList();
			_result = result;

			collectCoveringExecutions(lifeline, result);

			lifeline.getCoveredBys().stream()
					.filter(this::isVisualizedNonExecution)
					.forEach(result::add);
		} else if (semanticFromEditPart instanceof InteractionOperand) {
			// Everything is driven by the lifelines' coverage
			_result = null;
		} else {
			_result = super.getCanonicalSemanticChildren(semanticFromEditPart, viewFromEditPart);
		}

		return _result;
	}

	/**
	 * Collects execution specifications implicitly covering a lifeline. They typically don't actually cover
	 * a lifeline: their start/end occurrence specifications do in the semantics, but the diagram has views
	 * only for the executions, not for the occurrences.
	 *
	 * @param lifeline
	 *            a lifeline visualized in the sequence diagram
	 * @param result
	 *            collects its covering executions
	 */
	protected void collectCoveringExecutions(Lifeline lifeline, Collection<? super ExecutionSpecification> result) {
		lifeline.getCoveredBys().stream()
				.filter(OccurrenceSpecification.class::isInstance).map(OccurrenceSpecification.class::cast)
				.flatMap(this::getExecutions)
				.filter(Objects::nonNull)
				.distinct()
				.forEach(result::add);
	}

	protected boolean isVisualizedNonExecution(InteractionFragment fragment) {
		return !(fragment instanceof ExecutionSpecification)
				&& visualizedInteractionFragmentSwitch.doSwitch(fragment);
	}

	/**
	 * Gets/finds the execution(s) started or finished by an {@code occurrence}, which
	 * may just be a message-end and so not have a reference to the execution(s).
	 *
	 * @param occurrence
	 *            an occurrence that may or may not start or finish an execution
	 * @return the execution(s) started or finished by the {@code occurrence}, or
	 *         an empty stream if none
	 */
	private Stream<ExecutionSpecification> getExecutions(OccurrenceSpecification occurrence) {
		Stream<ExecutionSpecification> result;

		if (occurrence instanceof ExecutionOccurrenceSpecification) {
			result = Stream.of(((ExecutionOccurrenceSpecification) occurrence).getExecution());
		} else {
			// Do it the hard way
			result = EMFHelper.getUsages(occurrence).stream()
					.filter(s -> (s.getEStructuralFeature() == UMLPackage.Literals.EXECUTION_SPECIFICATION__START)
							|| (s.getEStructuralFeature() == UMLPackage.Literals.EXECUTION_SPECIFICATION__FINISH))
					.map(EStructuralFeature.Setting::getEObject)
					.map(ExecutionSpecification.class::cast);
		}

		return result;
	}

	@Override
	public List<? extends EObject> getCanonicalSemanticConnections(EObject semanticFromEditPart, View viewFromEditPart) {
		// Lifelines and execution-specifications are responsible for the messages
		// connected to them via message-ends
		List<? extends EObject> result = null;

		Stream<? extends InteractionFragment> fromLifeline;
		Lifeline lifeline = TypeUtils.as(semanticFromEditPart, Lifeline.class);
		if (lifeline == null) {
			fromLifeline = Stream.empty();
		} else {
			fromLifeline = lifeline.getCoveredBys().stream();
		}

		Stream<? extends InteractionFragment> fromExec;
		ExecutionSpecification exec = TypeUtils.as(semanticFromEditPart, ExecutionSpecification.class);
		if (exec == null) {
			fromExec = Stream.empty();
		} else {
			fromExec = Stream.of(exec.getStart(), exec.getFinish());
		}

		result = Stream.concat(fromLifeline, fromExec)
				.filter(MessageEnd.class::isInstance).map(MessageEnd.class::cast)
				.map(MessageEnd::getMessage)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());

		return result;
	}

	@Override
	public Object getSource(EObject connectionElement) {
		Object result;

		if (connectionElement instanceof Message) {
			MessageEnd end = ((Message) connectionElement).getSendEvent();
			result = end;

			// But, in this diagram, messages usually connect lifelines or
			// execution specifications, not message-ends (except for gates
			// and destruction occurrence-specs, which are explicitly visualized)
			result = resolveMessageEnd(end);
		} else {
			result = super.getSource(connectionElement);
		}

		return result;
	}

	/**
	 * Obtains the execution-specification or lifeline to which a message {@code end}
	 * connects a message, if the {@code end} is not explicitly visualized in the
	 * sequence diagram as a gate or destruction is.
	 *
	 * @param end
	 *            a message end
	 *
	 * @return the element that is actually visualized in the diagram at that {@code end}
	 */
	private Object resolveMessageEnd(MessageEnd end) {
		Object result = end;

		if (end instanceof DestructionOccurrenceSpecification) {
			// The destruction-occurrence is a properly visualized message-end,
			// but nonetheless (unlike a gate, for example), the diagram does not
			// connect destroy messages to this occurrence. Instead, it connects
			// them to the lifeline that is destroyed
			result = ((DestructionOccurrenceSpecification) end).getCovered();
		} else if (end instanceof OccurrenceSpecification) {

			Optional<Object> exec = getExecutions((OccurrenceSpecification) end)
					.findFirst().map(Object.class::cast);

			result = exec.orElseGet(() -> {
				Optional<Object> lifeline = getCovered(end).map(Object.class::cast);
				return lifeline.orElse(end);
			});
		}

		return result;
	}

	/**
	 * If a message end is a message occurrence specification (not a gate), get the lifeline
	 * that it covers.
	 *
	 * @param messageEnd
	 *            a message end
	 * @return its covered lifeline
	 */
	protected Optional<Lifeline> getCovered(MessageEnd messageEnd) {
		Lifeline result = null;

		if (messageEnd instanceof OccurrenceSpecification) {
			result = ((MessageOccurrenceSpecification) messageEnd).getCovered();
		}

		return Optional.ofNullable(result);
	}

	@Override
	public Object getTarget(EObject connectionElement) {
		Object result;

		if (connectionElement instanceof Message) {
			MessageEnd end = ((Message) connectionElement).getReceiveEvent();
			result = end;

			// But, in this diagram, messages usually connect lifelines or
			// execution specifications, not message-ends (except for gates
			// and destruction occurrence-specs, which are explicitly visualized)
			result = resolveMessageEnd(end);
		} else {
			result = super.getTarget(connectionElement);
		}

		return result;
	}
}
