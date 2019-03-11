/*****************************************************************************
 * Copyright (c) 2018 Christian W. Damus and others.
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

package org.eclipse.papyrus.uml.diagram.sequence.validation;

import static org.eclipse.papyrus.uml.diagram.sequence.util.OccurrenceSpecificationHelper.findExecutionWith;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.papyrus.infra.core.utils.OneShotExecutor;
import org.eclipse.papyrus.infra.emf.gmf.command.INonDirtying;
import org.eclipse.papyrus.infra.services.validation.ValidationTool;
import org.eclipse.papyrus.infra.services.validation.commands.ValidateSubtreeCommand;
import org.eclipse.papyrus.uml.diagram.sequence.command.AsynchronousCommand;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.util.UMLSwitch;

import com.google.common.collect.MapMaker;

/**
 * An asynchronous validation command.
 */
public class AsyncValidateCommand extends AsynchronousCommand implements INonDirtying {
	private static Map<EObject, OneShotExecutor> executors = new MapMaker().weakKeys().makeMap();

	/**
	 * Initializes me with the {@code object to validate}.
	 *
	 * @param object
	 *            the object to validate later
	 */
	public AsyncValidateCommand(EObject object) {
		super("Validate", TransactionUtil.getEditingDomain(object), () -> validate(object),
				// Ensure that only one async validation of this object can be pending at any time
				// and that subsequent requests just supersede any previous pending requests
				executors.computeIfAbsent(object, __ -> new OneShotExecutor(Display.getDefault()::asyncExec)));
	}

	private static ICommand validate(EObject object) {
		ValidateSubtreeCommand cmd = new UMLSwitch<ValidateSubtreeCommand>() {

			@Override
			public ValidateSubtreeCommand caseCombinedFragment(CombinedFragment cfrag) {
				return new ValidateCombinedFragmentCommand(cfrag,
						() -> Stream.concat(messages(cfrag), nonOwnedExecutions(cfrag)));
			}

			@Override
			public ValidateSubtreeCommand caseInteractionOperand(InteractionOperand operand) {
				return new ValidateCombinedFragmentCommand(operand,
						() -> Stream.concat(messages(operand), nonOwnedExecutions(operand)));
			}

			@Override
			public ValidateSubtreeCommand defaultCase(EObject object) {
				return new ValidateSubtreeCommand(object);
			}
		}.doSwitch(object);

		cmd.disableUIFeedback();

		return cmd;
	}

	/**
	 * Returns a new {@link AsyncValidateCommand} if preference to validate after edition is set to <code>true</code>.
	 * 
	 * @return a {@link Optional} {@link AsyncValidateCommand} or an empty one if the preference is set to no validation.
	 */
	public static Optional<AsyncValidateCommand> get(EObject object) {
		IPreferenceStore store = UMLDiagramEditorPlugin.getInstance().getPreferenceStore();
		Boolean triggerValidation = store.getBoolean(org.eclipse.papyrus.uml.diagram.sequence.preferences.CustomDiagramGeneralPreferencePage.PREF_TRIGGER_ASYNC_VALIDATION);
		return Optional.ofNullable(triggerValidation ? new AsyncValidateCommand(object) : null);
	}

	static Stream<Message> messages(InteractionOperand operand) {
		return operand.getFragments().stream()
				.filter(MessageEnd.class::isInstance).map(MessageEnd.class::cast)
				.map(MessageEnd::getMessage)
				.filter(Objects::nonNull).distinct();
	}

	static Stream<ExecutionSpecification> nonOwnedExecutions(InteractionOperand operand) {
		return operand.getFragments().stream()
				.filter(OccurrenceSpecification.class::isInstance).map(OccurrenceSpecification.class::cast)
				.map(AsyncValidateCommand::getExecution)
				.filter(Objects::nonNull)
				.filter(exec -> exec.getOwner() != operand)
				.distinct();
	}

	static ExecutionSpecification getExecution(OccurrenceSpecification occurrence) {
		return Optional.ofNullable(findExecutionWith(occurrence, true))
				.orElseGet(() -> findExecutionWith(occurrence, false));
	}

	static Stream<Message> messages(CombinedFragment cfrag) {
		return cfrag.getOperands().stream().flatMap(AsyncValidateCommand::messages)
				.distinct();
	}

	static Stream<ExecutionSpecification> nonOwnedExecutions(CombinedFragment cfrag) {
		return cfrag.getOperands().stream().flatMap(AsyncValidateCommand::nonOwnedExecutions)
				.distinct();
	}

	//
	// Nested types
	//

	private static class ValidateCombinedFragmentCommand extends ValidateSubtreeCommand {
		private final Supplier<Stream<? extends EObject>> dependenciesSupplier;

		ValidateCombinedFragmentCommand(EObject root, Supplier<Stream<? extends EObject>> dependenciesSupplier) {
			super(root, new OperandDiagnostician());

			this.dependenciesSupplier = dependenciesSupplier;
		}

		@Override
		protected void handleDiagnostic(IProgressMonitor monitor, Diagnostic diagnostic, EObject validateElement, Shell shell) {
			// Do not show a dialog, as in the original version since the user sees the result directly
			// in the model explorer
			Resource resource = getValidationResource();
			// final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			if (resource != null) {
				if (validateElement != null) {
					// In a combined fragment or an interaction operand, we validate also the
					// messages and execution specifications (provided generically as "dependencies")
					// that are at least partially within some operand in scope. We have to be
					// careful about also removing existing markers for these related elements
					// because the Diagnostician isn't responsible for that
					List<? extends EObject> others = dependenciesSupplier.get().collect(Collectors.toList());
					int markersToCreate = diagnostic.getChildren().size();

					SubMonitor sub = SubMonitor.convert(monitor, 1 + others.size() + markersToCreate);

					ValidationTool vt = new ValidationTool(validateElement, resource);

					// Delete existing markers
					vt.deleteSubMarkers(sub.newChild(1));
					others.forEach(el -> new ValidationTool(el, resource).deleteSubMarkers(sub.newChild(1)));

					// Create new markers
					vt.createMarkers(diagnostic, sub.newChild(markersToCreate));

					sub.done();
				}
			}
		}
	}
}
