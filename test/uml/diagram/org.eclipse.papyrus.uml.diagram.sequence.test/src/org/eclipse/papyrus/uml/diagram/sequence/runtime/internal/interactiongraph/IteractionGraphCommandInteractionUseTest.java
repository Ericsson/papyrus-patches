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
package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.InteractionGraphCommandHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.InteractionModelHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.WorkspaceAndPapyrusEditor;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageSort;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;


public class IteractionGraphCommandInteractionUseTest {
	@BeforeClass
	public static void init() {
		WorkspaceAndPapyrusEditor.DEBUG = true;
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

		Lifeline lifeline1 = helper.addLifeline("Lf_1", new Rectangle(20, 10, 100, -1));
		Assert.assertNotEquals(null, lifeline1);		
		editor.flushDisplayEvents();
				
		Lifeline lifeline2 = helper.addLifeline("Lf_2", new Rectangle(220, 10, 100, -1));
		Assert.assertNotEquals(null, lifeline2);		
		editor.flushDisplayEvents();

		InteractionUse intUse1 = helper.addInteractionUse("IU_1", new Rectangle(20, 120, 320, 60));
		Assert.assertNotEquals(null, intUse1);		
		editor.flushDisplayEvents();

		Message msg1 = helper.addMessage("Message1", MessageSort.ASYNCH_SIGNAL_LITERAL, lifeline1, new Point(70, 80), intUse1, new Point(20, 140));
		Assert.assertNotEquals(null, msg1);		
		editor.flushDisplayEvents();
		
		Message msg2 = helper.addMessage("Message1", MessageSort.ASYNCH_SIGNAL_LITERAL, lifeline2, new Point(270, 100), intUse1, new Point(320, 160));
		Assert.assertNotEquals(null, msg2);		
		editor.flushDisplayEvents();

		helper.nudgeLifeline(lifeline1, 200);
		editor.flushDisplayEvents();
		
		helper.nudgeLifeline(lifeline1, -200);
		editor.flushDisplayEvents();

		editor.waitForClose(diagram);
	}
	

	@Rule
	public WorkspaceAndPapyrusEditor editor = new WorkspaceAndPapyrusEditor();
}
