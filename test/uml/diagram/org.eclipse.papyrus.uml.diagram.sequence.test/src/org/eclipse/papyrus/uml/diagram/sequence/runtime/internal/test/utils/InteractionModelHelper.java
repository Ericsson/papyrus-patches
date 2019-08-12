/*****************************************************************************
 * (c) Copyright 2018 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) antonio.campesino.robles@ericsson.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils;

import java.util.Arrays;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.RollbackException;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain.Factory;
import org.eclipse.emf.transaction.impl.TransactionImpl;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.diagram.core.listener.DiagramEventBroker;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.emf.edit.domain.PapyrusTransactionalEditingDomain;
import org.eclipse.uml2.uml.ActionExecutionSpecification;
import org.eclipse.uml2.uml.BehaviorExecutionSpecification;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;

public class InteractionModelHelper {
	private static TransactionImpl TRANSACTION = null;
	private static int TRANSACTION_COUNT = 0;
	
	public static ModelSet createModelSet() {
		ModelSet set = new ModelSet();
		OfflineDiagramHelper.init(set);
		Resource modelResource = set.createResource(URI.createURI("model.uml"));
		Resource notationResource = set.createResource(URI.createURI("model.notation"));
		
		Interaction interaction = UMLFactory.eINSTANCE.createInteraction();
		Diagram diagram =  InteractionNotationHelper.createSequenceDiagram(interaction);			

		modelResource.getContents().add(interaction);
		notationResource.getContents().add(diagram);

		if (TransactionUtil.getEditingDomain(set) == null) {
			Factory factory = PapyrusTransactionalEditingDomain.Factory.INSTANCE;
			TransactionalEditingDomain ed = factory.createEditingDomain(set);
			DiagramEventBroker.getInstance(ed);
		}

		return set;
	}
	
	public static void startTransaction(EObject obj) {
		if (obj.eResource() == null || obj.eResource().getResourceSet() == null)
			return;
		startTransaction(obj.eResource().getResourceSet());
	}
	
	public static void startTransaction(ResourceSet set) {
		if (TRANSACTION != null) {
			TRANSACTION_COUNT ++;
			return;
		}
		
		TransactionalEditingDomain ed = TransactionUtil.getEditingDomain(set);
		TRANSACTION = new TransactionImpl(ed, false);
		try {
			TRANSACTION.start();
		} catch (InterruptedException e) {
			TRANSACTION = null;		
		}		
	}
	
	public static void endTransaction() {
		if (TRANSACTION == null)
			return;
		
		if (TRANSACTION_COUNT > 0) {
			TRANSACTION_COUNT--;
			return;
		}
		
		TransactionImpl impl = TRANSACTION;
		TransactionalEditingDomain ed = impl.getEditingDomain();
		TRANSACTION = null;
		try {			
			impl.commit();
		} catch (RollbackException e) {
			throw new RuntimeException(e);
		} finally {
		}
	}

	public static void clearTransactionStates() {
		if (TRANSACTION != null) {
			TRANSACTION.rollback();
			DiagramEventBroker.stopListening(TRANSACTION.getEditingDomain());
			TRANSACTION = null;
			TRANSACTION_COUNT = 0;
		}
	}

	
	public static Interaction getInteraction(ModelSet set) {
		Resource modelResource = set.getResource(URI.createURI("model.uml"), false);
		return (Interaction)modelResource.getContents().get(0);
	}	

	public static Message createMessage(Interaction interaction, String name, Lifeline from, Lifeline to, MessageSort sort) {
		startTransaction(interaction);
		try {
			MessageOccurrenceSpecification sendMessage = (MessageOccurrenceSpecification)interaction.
					createFragment("send"+name, UMLPackage.Literals.MESSAGE_OCCURRENCE_SPECIFICATION);
			sendMessage.setCovered(from);
			MessageOccurrenceSpecification recvMessage = (MessageOccurrenceSpecification)interaction.
					createFragment("recv"+name, UMLPackage.Literals.MESSAGE_OCCURRENCE_SPECIFICATION);
			recvMessage.setCovered(to);
	
			return createMessage(interaction, name, sendMessage, recvMessage, sort);
		} finally {
			endTransaction();
		}			
	}

	public static Message createMessage(Interaction interaction, String name, Lifeline from, Gate to, MessageSort sort) {
		startTransaction(interaction);
		try {
			MessageOccurrenceSpecification sendMessage = (MessageOccurrenceSpecification)interaction.
					createFragment("send"+name, UMLPackage.Literals.MESSAGE_OCCURRENCE_SPECIFICATION);
			sendMessage.setCovered(from);
	
			return createMessage(interaction, name, sendMessage, to, sort);
		} finally {
			endTransaction();
		}			
	}

	public static Message createMessage(Interaction interaction, String name, Gate from, Lifeline to, MessageSort sort) {
		startTransaction(interaction);
		try {
			MessageOccurrenceSpecification recvMessage = (MessageOccurrenceSpecification)interaction.
					createFragment("recv"+name, UMLPackage.Literals.MESSAGE_OCCURRENCE_SPECIFICATION);
			recvMessage.setCovered(to);
			return createMessage(interaction, name, from, recvMessage, sort);
		} finally {
			endTransaction();
		}			
	}

	public static Message createMessage(Interaction interaction, String name, MessageEnd from, MessageEnd to, MessageSort sort) {
		startTransaction(interaction);
		try {
			Message message = interaction.createMessage(name);
			message.setReceiveEvent(to);
			to.setMessage(message);
			message.setSendEvent(from);
			from.setMessage(message);
			message.setMessageSort(sort);
			
			if (sort == MessageSort.SYNCH_CALL_LITERAL) {
				ActionExecutionSpecification execSpec = (ActionExecutionSpecification)interaction.
						createFragment("execSpec"+name, UMLPackage.Literals.ACTION_EXECUTION_SPECIFICATION);
				execSpec.getCovereds().add(((MessageOccurrenceSpecification)to).getCovered());
				execSpec.setStart((MessageOccurrenceSpecification)to);			
			}
			
			return message;
		} finally {
			endTransaction();
		}			
	}
	
	public static Message createReturnMessage(Interaction interaction, String name, Lifeline from, Lifeline to, Message replayingMessage) {
		startTransaction(interaction);
		try {
			Message relayMsg = createMessage(interaction, name, from, to, MessageSort.REPLY_LITERAL);
			
			ExecutionSpecification execSpec = from.getCoveredBys().stream()
					.filter(ExecutionSpecification.class::isInstance)
					.map(ExecutionSpecification.class::cast)
					.filter(f -> ((ExecutionSpecification) f).getStart() == replayingMessage.getReceiveEvent())
					.findFirst().get();
			if (execSpec != null)
				execSpec.setFinish((MessageOccurrenceSpecification)relayMsg.getSendEvent());
			
			return relayMsg;
		} finally {
			endTransaction();
		}			
	}

	public static ActionExecutionSpecification startActionExecutionSpecification(Interaction interaction, String name, Lifeline lifeline) {
		return (ActionExecutionSpecification)startExecutionSpecification(
				interaction, name, UMLPackage.Literals.ACTION_EXECUTION_SPECIFICATION, lifeline, null);
	}
	
	public static ActionExecutionSpecification startActionExecutionSpecification(Interaction interaction, String name, Lifeline lifeline, MessageOccurrenceSpecification mos) {
		return (ActionExecutionSpecification)startExecutionSpecification(
				interaction, name, UMLPackage.Literals.ACTION_EXECUTION_SPECIFICATION, lifeline, mos);
	}
	
	public static BehaviorExecutionSpecification startBehaviorExecutionSpecification(Interaction interaction, String name, Lifeline lifeline) {
		return (BehaviorExecutionSpecification)startExecutionSpecification(
				interaction, name, UMLPackage.Literals.ACTION_EXECUTION_SPECIFICATION, lifeline, null);
	}

	public static BehaviorExecutionSpecification startBehaviorExecutionSpecification(Interaction interaction, String name, Lifeline lifeline, MessageOccurrenceSpecification mos) {
		return (BehaviorExecutionSpecification)startExecutionSpecification(
				interaction, name, UMLPackage.Literals.ACTION_EXECUTION_SPECIFICATION, lifeline, mos);
	}

	private static ExecutionSpecification startExecutionSpecification(Interaction interaction, String name, EClass cls, Lifeline lifeline, OccurrenceSpecification start) {
		startTransaction(interaction);
		try {
			OccurrenceSpecification startActionExecSpec = start;
			if (startActionExecSpec == null) {
				startActionExecSpec  = (ExecutionOccurrenceSpecification)interaction.
					createFragment("start"+name, UMLPackage.Literals.EXECUTION_OCCURRENCE_SPECIFICATION);
				startActionExecSpec.setCovered(lifeline);
			}

			ActionExecutionSpecification actionExecSpec = (ActionExecutionSpecification )interaction.createFragment(name, cls);
			if (startActionExecSpec instanceof ExecutionOccurrenceSpecification)
				((ExecutionOccurrenceSpecification)startActionExecSpec).setExecution(actionExecSpec);

			actionExecSpec.getCovereds().add(lifeline);
			actionExecSpec.setStart(startActionExecSpec);			
			return actionExecSpec;
		} finally {
			endTransaction();
		}			
	}

	public static ExecutionSpecification endExecutionSpecification(ExecutionSpecification executionSpecification) {
		return endExecutionSpecification(executionSpecification, null);
	}
		
	public static ExecutionSpecification endExecutionSpecification(ExecutionSpecification executionSpecification, OccurrenceSpecification end) {
		startTransaction(executionSpecification);
		try {
			OccurrenceSpecification start = executionSpecification.getStart();
			if (end == null) {
				ExecutionOccurrenceSpecification finish = null;
				if (start.getOwner() instanceof Interaction) {
					finish = (ExecutionOccurrenceSpecification)((Interaction)start.getOwner()).
							createFragment("end"+executionSpecification.getName(), UMLPackage.Literals.EXECUTION_OCCURRENCE_SPECIFICATION);
				} else if (start.getOwner() instanceof InteractionOperand) {
					finish = (ExecutionOccurrenceSpecification)((InteractionOperand)start.getOwner()).
							createFragment("end"+executionSpecification.getName(), UMLPackage.Literals.EXECUTION_OCCURRENCE_SPECIFICATION);
				}
				finish.setExecution(executionSpecification);
				finish.setCovered(executionSpecification.getCovereds().get(0));
				end = finish;
			}
			
			executionSpecification.setFinish(end);
			return executionSpecification;
		} finally {
			endTransaction();
		}			
	}

	public static InteractionUse createInteractionUse(Interaction interaction, String name, Lifeline... lifelines) {
		startTransaction(interaction);
		try {
			InteractionUse intUse = (InteractionUse)interaction.createFragment(name, UMLPackage.Literals.INTERACTION_USE);
			intUse.getCovereds().addAll(Arrays.asList(lifelines));
			return intUse;
		} finally {
			endTransaction();
		}			
	}
}
