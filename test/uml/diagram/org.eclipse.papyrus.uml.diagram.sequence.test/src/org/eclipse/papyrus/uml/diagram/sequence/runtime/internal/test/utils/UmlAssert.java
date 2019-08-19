package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils;

import static org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.CollectionMatchers.sameInOrder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageKind;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;

public class UmlAssert {

	public static void assertMessageGroup(Message msg, MessageSort msgSort, MessageKind kind, 
			Lifeline srcLifeline, Lifeline trgLifeline,  
			ExecutionSpecification execSpec, Message msgReply) {
		
		assertThat("Message Send Event Lifeline", ((MessageOccurrenceSpecification)msg.getSendEvent()).getCovered(), sameInstance(srcLifeline));
		assertThat("Message Receive Event Lifeline", ((MessageOccurrenceSpecification)msg.getReceiveEvent()).getCovered(), sameInstance(trgLifeline));
		
		assertThat("Reply Message", msgReply.getName(), is("Reply "+msg.getName()));
		assertThat("Reply Message Send Event Lifeline", 
				((MessageOccurrenceSpecification)msgReply.getSendEvent()).getCovered(), 
				sameInstance(((MessageOccurrenceSpecification)msg.getReceiveEvent()).getCovered()));
		assertThat("Reply Message Receive Event Lifeline", 
				((MessageOccurrenceSpecification)msgReply.getReceiveEvent()).getCovered(), 
				sameInstance(((MessageOccurrenceSpecification)msg.getSendEvent()).getCovered()));

		assertThat("ExecutionSpecification Lifeline", execSpec.getCovereds(), sameInOrder(trgLifeline));
		assertThat("ExecutionSpecification Start", execSpec.getStart(), sameInstance(msg.getReceiveEvent()));
		assertThat("ExecutionSpecification Finish", execSpec.getFinish(), sameInstance(msgReply.getSendEvent()));
	}
	
	public static void assertFragmentOrder(Interaction interaction, InteractionFragment[] fragments, 
			Lifeline[] lifelines, InteractionFragment[][] fragmentsByLifeline) {
		
		assertThat("Interaction Fragments", interaction.getFragments(),sameInOrder(fragments));

		for (int i=0; i<lifelines.length; i++) {
			assertThat(lifelines[i].getName() + " Covered", lifelines[i].getCoveredBys(), 
					sameInOrder(fragmentsByLifeline[i]));
		}
	}
}
