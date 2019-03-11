/*****************************************************************************
 * Copyright (c) 2010, 2015 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	 Florian Noyrit  (CEA) florian.noyrit@cea.fr - Initial API and Implementation
 *   Mickael ADAM (ALL4TEC) mickael.adam@all4tec.net - reconciler to add floating label
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.migration;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.command.AbstractCommand;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.reconciler.DiagramReconciler;

/**
 * Sequence Diagram Reconciler from 1.1.0 to 1.2.0
 */
public class SequenceReconciler_1_2_0 extends DiagramReconciler {

	@Override
	public ICommand getReconcileCommand(Diagram diagram) {
		CompositeCommand cc = new CompositeCommand("Migrate diagram from 1.1.0 to 1.2.0");
		cc.add(new ChangeVisualIDsCommand(diagram));
		return cc;
	}

	protected class ChangeVisualIDsCommand extends AbstractCommand {

		protected final Diagram diagram;

		public ChangeVisualIDsCommand(Diagram diagram) {
			super("Change the diagram's visual ids from 1.1.0 to 1.2.0");
			this.diagram = diagram;
		}

		@Override
		protected CommandResult doExecuteWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
			TreeIterator<EObject> allContentIterator = diagram.eAllContents();

			while (allContentIterator.hasNext()) {
				EObject eObject = allContentIterator.next();
				if (eObject instanceof View) {
					View view = (View) eObject;
					view.setType(getNewVisualID(view.getType()));
				}
			}

			return CommandResult.newOKCommandResult();
		}

		@Override
		public boolean canUndo() {
			return false;
		}

		@Override
		public boolean canRedo() {
			return false;
		}

		@Override
		protected CommandResult doRedoWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
			throw new ExecutionException("Should not be called, canRedo false");
		}

		@Override
		protected CommandResult doUndoWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
			throw new ExecutionException("Should not be called, canUndo false");
		}
	}

	public static String getNewVisualID(String oldVisualID) {
		switch (oldVisualID) {
		case "1000":
			return "Package_SequenceDiagram";
		case "3007":
			return "ConsiderIgnoreFragment_Shape";
		case "3004":
			return "CombinedFragment_Shape";
		case "3005":
			return "InteractionOperand_Shape";
		case "3002":
			return "InteractionUse_Shape";
		case "5003":
			return "InteractionUse_NameLabel";
		case "5004":
			return "InteractionUse_TypeLabel";
		case "3016":
			return "Continuation_Shape";
		case "5007":
			return "Continuation_NameLabel";
		case "3001":
			return "Lifeline_Shape";
		case "5002":
			return "Lifeline_NameLabel";
		case "3006":
			return "ActionExecutionSpecification_Shape";
		case "3003":
			return "BehaviorExecutionSpecification_Shape";
		case "3017":
			return "StateInvariant_Shape";
		case "5008":
			return "StateInvariant_NameLabel";
		case "5023":
			return "StateInvariant_ConstraintLabel";
		case "3018":
			return "CombinedFragment_CoRegionShape";
		case "3019":
			return "TimeConstraint_Shape";
		case "5009":
			return "TimeConstraint_ConstraintLabel";
		case "5013":
			return "TimeConstraint_StereotypeLabel";
		case "3020":
			return "TimeObservation_Shape";
		case "5010":
			return "TimeObservation_NameLabel";
		case "5014":
			return "TimeObservation_StereotypeLabel";
		case "3021":
			return "DurationConstraint_Shape";
		case "5011":
			return "DurationConstraint_BodyLabel";
		case "5015":
			return "DurationConstraint_StereotypeLabel";
		case "3022":
			return "DestructionOccurrenceSpecification_Shape";
		case "3008":
			return "Constraint_Shape";
		case "5005":
			return "Constraint_NameLabel";
		case "5012":
			return "Constraint_BodyLabel";
		case "3009":
			return "Comment_Shape";
		case "5006":
			return "Comment_BodyLabel";
		case "3023":
			return "DurationConstraint_Shape_CN";
		case "5018":
			return "DurationConstraint_BodyLabel_CN";
		case "5019":
			return "DurationConstraint_StereotypeLabel_CN";
		case "3024":
			return "DurationObservation_Shape";
		case "5016":
			return "DurationObservation_NameLabel";
		case "5017":
			return "DurationObservation_StereotypeLabel";
		case "2001":
			return "Interaction_Shape";
		case "5001":
			return "Interaction_NameLabel";
		case "4003":
			return "Message_SynchEdge";
		case "6001":
			return "Message_SynchNameLabel";
		case "6008":
			return "Message_SynchStereotypeLabel";
		case "4004":
			return "Message_AsynchEdge";
		case "6002":
			return "Message_AsynchNameLabel";
		case "6009":
			return "Message_AsynchStereotypeLabel";
		case "4005":
			return "Message_ReplyEdge";
		case "6003":
			return "Message_ReplyNameLabel";
		case "6010":
			return "Message_ReplyStereotypeLabel";
		case "4006":
			return "Message_CreateEdge";
		case "6004":
			return "Message_CreateNameLabel";
		case "6011":
			return "Message_CreateStereotypeLabel";
		case "4007":
			return "Message_DeleteEdge";
		case "6005":
			return "Message_DeleteNameLabel";
		case "6012":
			return "Message_DeleteStereotypeLabel";
		case "4008":
			return "Message_LostEdge";
		case "6006":
			return "Message_LostNameLabel";
		case "6013":
			return "Message_LostStereotypeLabel";
		case "4009":
			return "Message_FoundEdge";
		case "6007":
			return "Message_FoundNameLabel";
		case "6014":
			return "Message_FoundStereotypeLabel";
		case "4010":
			return "Comment_AnnotatedElementEdge";
		case "4011":
			return "Constraint_ConstrainedElementEdge";
		case "4012":
			return "GeneralOrdering_Edge";
		case "6015":
			return "GeneralOrdering_StereotypeLabel";
		case "8500":
			return "Constraint_ContextEdge";
		case "8501":
			return "Constraint_KeywordLabel";
		case "7001":
			return "Interaction_SubfragmentCompartment";
		case "7004":
			return "CombinedFragment_SubfragmentCompartment";
		default:
			return defaultGetNewVisualID(oldVisualID);
		}
	}

	private static String defaultGetNewVisualID(String oldVisualID) {
		switch (oldVisualID) {
		case "999999":
			return "MessageEnd_Shape";
		case "999998":
			return "ExecutionSpecification_Shape";
		default:
			return oldVisualID;
		}
	}
}
