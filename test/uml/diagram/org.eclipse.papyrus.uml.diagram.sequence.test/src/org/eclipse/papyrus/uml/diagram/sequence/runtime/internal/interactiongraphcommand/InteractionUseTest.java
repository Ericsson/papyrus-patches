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
package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraphcommand;

import java.util.Arrays;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import javax.lang.model.element.Element;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.GateEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionUseEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageAsyncEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.CollectionMatchers;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.InteractionGraphCommandHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.InteractionModelHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.InteractionNotationHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.ViewAssert;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.ViewConstants;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.WorkspaceAndPapyrusEditor;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.NamedElement;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;


public class InteractionUseTest {
	
	@BeforeClass
	public static void init() {
	}
	
	@After
	public void cleanUp() {
		InteractionModelHelper.clearTransactionStates();
		editor.closeDiagrams();		
	}
	
	/**
	 *   +---+            +---+
	 *   | A |            | B |
	 *   +---+            +---+
	 *     |                |
	 *     |                |
	 *     |                |
	 *     |                |
	 *     |                |
	 */
	@Test
	public void testSelfMessageBendpoint1() throws ExecutionException {
		ModelSet modelSet = editor.getResourceSet();
		InteractionGraphCommandHelper helper = new InteractionGraphCommandHelper(modelSet);
		
		InteractionModelHelper.startTransaction(modelSet);		
		Diagram diagram = helper.createSequenceDiagram("testSelfMessageBendpoint1");
		editor.initDiagramAndModel((Interaction)diagram.getElement(), diagram);				
		InteractionModelHelper.endTransaction();
		
		editor.openDiagram(diagram);

		try {
			Rectangle lifeline1_rect = new Rectangle(20, 10, 100, -1);
			Lifeline lifeline1 = helper.addLifeline("Lf_1", lifeline1_rect);
			Assert.assertNotEquals(null, lifeline1);		
			editor.flushDisplayEvents();
					
			Rectangle lifeline2_rect = new Rectangle(220, 10, 100, -1);
			Lifeline lifeline2 = helper.addLifeline("Lf_2", lifeline2_rect);
			Assert.assertNotEquals(null, lifeline2);		
			editor.flushDisplayEvents();
			
			Rectangle intUse1_rect = new Rectangle(20, 120, 300, 60);
			InteractionUse intUse1 = helper.addInteractionUse("IU_1", intUse1_rect);
			Assert.assertNotEquals(null, intUse1);		
			editor.flushDisplayEvents();
			MatcherAssert.assertThat("'IU_1' covereds:", intUse1.getCovereds(), 
					CollectionMatchers.inAnyOrder(lifeline1, lifeline2));
	
			Rectangle gate1_rect = new Rectangle(14,134,12,12);
			Point msg1_send = new Point(70, 80);
			Message msg1 = helper.addMessage("Message1", MessageSort.ASYNCH_SIGNAL_LITERAL, lifeline1, msg1_send, intUse1, gate1_rect.getCenter());
			Assert.assertNotEquals(null, msg1);		
			editor.flushDisplayEvents();
			
			Rectangle gate2_rect = new Rectangle(314,154,12,12);
			Point msg2_send = new Point(270, 100);
			Message msg2 = helper.addMessage("Message2", MessageSort.ASYNCH_SIGNAL_LITERAL, lifeline2, msg2_send, intUse1, gate2_rect.getCenter());
			Assert.assertNotEquals(null, msg2);		
			editor.flushDisplayEvents();
			
			lifeline1_rect = ViewConstants.DEFAULT_LIFELINE_YPOS(helper.getViewer(), lifeline1, lifeline1_rect);
			ViewAssert.assertView(diagram, lifeline1, 
					LifelineEditPart.VISUAL_ID, 
					ViewUtilities.getViewWithType(diagram, InteractionInteractionCompartmentEditPart.VISUAL_ID),
					lifeline1_rect);
			
			lifeline2_rect = ViewConstants.DEFAULT_LIFELINE_YPOS(helper.getViewer(), lifeline2, lifeline2_rect);
			ViewAssert.assertView(diagram, lifeline2, 
					LifelineEditPart.VISUAL_ID, 
					ViewUtilities.getViewWithType(diagram, InteractionInteractionCompartmentEditPart.VISUAL_ID),
					lifeline2_rect);
	
			ViewAssert.assertView(diagram, intUse1, 
					InteractionUseEditPart.VISUAL_ID, 
					ViewUtilities.getViewWithType(diagram, InteractionInteractionCompartmentEditPart.VISUAL_ID),
					intUse1_rect);
	
			
			ViewAssert.assertEdge(diagram, msg1, 
					MessageAsyncEditPart.VISUAL_ID,
					lifeline1,
					msg1_send,
					msg1.getReceiveEvent(),
					gate1_rect.getCenter());
	
			ViewAssert.assertView(diagram, msg1.getReceiveEvent(), 
					GateEditPart.VISUAL_ID, 
					ViewUtilities.getViewForElement(diagram, intUse1),
					gate1_rect);
			
			ViewAssert.assertEdge(diagram, msg2, 
					MessageAsyncEditPart.VISUAL_ID,
					lifeline2,
					msg2_send,
					msg2.getReceiveEvent(),
					gate2_rect.getCenter());
	
			ViewAssert.assertView(diagram, msg2.getReceiveEvent(), 
					GateEditPart.VISUAL_ID, 
					ViewUtilities.getViewForElement(diagram, intUse1),
					gate2_rect);
	
			
			List<SimpleEntry<NamedElement,Rectangle>> elements = 
					Arrays.asList(new SimpleEntry<>(lifeline1, lifeline1_rect), 
								  new SimpleEntry<>(lifeline2, lifeline2_rect),
								  new SimpleEntry<>(intUse1, intUse1_rect),
								  new SimpleEntry<>(msg1.getReceiveEvent(), gate1_rect),
								  new SimpleEntry<>(msg2.getReceiveEvent(), gate2_rect));
	
			List<SimpleEntry<Message,Point[]>> messages = 
					Arrays.asList(new SimpleEntry<>(msg1, new Point[] {msg1_send, gate1_rect.getCenter()}), 
								  new SimpleEntry<>(msg2, new Point[] {msg2_send, gate2_rect.getCenter()}));
	
			helper.nudgeLifeline(lifeline1, 200);
			elements.forEach(d->d.getValue().x+=200);
			messages.forEach(d->Arrays.asList(d.getValue()).forEach(p->p.x+=200));
			editor.flushDisplayEvents();			
			ViewAssert.assertViewsBounds(diagram, (List)elements);
			ViewAssert.assertAnchorsLocations(diagram, messages);
	
			helper.nudgeLifeline(lifeline1, -200);
			elements.forEach(d->d.getValue().x-=200);
			messages.forEach(d->Arrays.asList(d.getValue()).forEach(p->p.x-=200));
			editor.flushDisplayEvents();
			ViewAssert.assertViewsBounds(diagram, (List)elements);
			ViewAssert.assertAnchorsLocations(diagram, messages);
			
			editor.waitForClose(diagram);
		} catch (Throwable e) {
			editor.waitForClose(diagram);
			throw e;
		}
	}
	

	@Rule
	public WorkspaceAndPapyrusEditor editor = new WorkspaceAndPapyrusEditor();
}
