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

import java.util.stream.Collectors;

import org.apache.commons.jxpath.JXPathContext;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Column;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphFactory;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Link;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Row;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.GraphAssert;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.InteractionModelHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.InteractionNotationHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.WorkspaceAndPapyrusEditor;
import org.eclipse.swt.SWT;
import org.eclipse.uml2.uml.ActionExecutionSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.UMLFactory;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;

@Ignore
public class InteractionGraphTest {
	@BeforeClass
	public static void init() {
		//WorkspaceAndPapyrusEditor.DEBUG = true;
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
	public void testBuildGraph2LifelinesEmpty() {
		ModelSet modelSet = editor.getResourceSet();
		InteractionModelHelper.startTransaction(modelSet);		
		Interaction interaction = UMLFactory.eINSTANCE.createInteraction();
		interaction.setName("test");		
		Diagram diagram = InteractionNotationHelper.createSequenceDiagram(interaction);
		diagram.setName("test");				
		editor.initDiagramAndModel(interaction, diagram);				
		InteractionModelHelper.endTransaction();

		editor.openDiagram(diagram);
		EditPartViewer viewer = InteractionNotationHelper.getEditPartViewer(diagram);
		
		InteractionModelHelper.startTransaction(interaction);
		Lifeline lf1 = interaction.createLifeline("Lifeline1");
		View lifelineView1 = InteractionNotationHelper.createLifeline(viewer, diagram, lf1, new Rectangle(0,10,-1,-1));
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();
		
		InteractionModelHelper.startTransaction(interaction);
		Lifeline lf2 = interaction.createLifeline("Lifeline2");		
		Rectangle r = ViewUtilities.getBoundsForLayoutConstraint(viewer, lifelineView1);
		View lifelineView2 = InteractionNotationHelper.createLifeline(viewer, diagram, lf2, new Rectangle(5 + r.x + r.width,10,-1,-1));
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();
		
		InteractionGraph graph = InteractionGraphFactory.getInstance().createInteractionGraph(interaction, diagram, viewer);
		JXPathContext context = JXPathContext.newContext(graph);
		GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[1]"), null, 
				lf1, lifelineView1, 
				(Row)context.getValue("rows[1]"), 
				(Column)context.getValue("columns[1]"), 0);
		GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[2]"), null, 
				lf2, lifelineView2, 
				(Row)context.getValue("rows[1]"), 
				(Column)context.getValue("columns[2]"), 0);
		
		editor.waitForClose(diagram);
	}
	
	/**
	 *   +---+            +---+
	 *   | A |            | B |
	 *   +---+            +---+
	 *     |                |
	 *     |--------------->|
	 *     |                |
	 *     |<---------------|
	 */
	@Test
	public void testBuildGraph2LifelinesAsynchMessage() {
		ModelSet modelSet = editor.getResourceSet();
		InteractionModelHelper.startTransaction(modelSet);		
		Interaction interaction = UMLFactory.eINSTANCE.createInteraction();
		interaction.setName("test");		
		Diagram diagram = InteractionNotationHelper.createSequenceDiagram(interaction);
		diagram.setName("test");				
		editor.initDiagramAndModel(interaction, diagram);				
		InteractionModelHelper.endTransaction();

		editor.openDiagram(diagram);
		EditPartViewer viewer = InteractionNotationHelper.getEditPartViewer(diagram);
		
		InteractionModelHelper.startTransaction(interaction);
		Lifeline lf1 = interaction.createLifeline("Lifeline1");
		View lifelineView1 = InteractionNotationHelper.createLifeline(viewer, diagram, lf1);
		InteractionNotationHelper.setLayoutPosition((org.eclipse.gmf.runtime.notation.Node)lifelineView1,0,10);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();
		
		InteractionModelHelper.startTransaction(interaction);
		Lifeline lf2 = interaction.createLifeline("Lifeline2");
		View lifelineView2 = InteractionNotationHelper.createLifeline(viewer, diagram, lf2);
		Rectangle r = ViewUtilities.getBoundsForLayoutConstraint(viewer, lifelineView1);
		InteractionNotationHelper.setLayoutPosition((org.eclipse.gmf.runtime.notation.Node)lifelineView2, 5 + r.x + r.width,10);		
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();
		
		InteractionModelHelper.startTransaction(interaction);
		Message msg1 = InteractionModelHelper.createMessage(interaction, "AsynchCall", lf1, lf2, MessageSort.ASYNCH_CALL_LITERAL);
		Edge msgEdge1 = InteractionNotationHelper.createMessage(viewer, diagram, msg1);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();
		
		InteractionModelHelper.startTransaction(interaction);
		Message msg2 = InteractionModelHelper.createMessage(interaction, "AsynchCallResp", lf2, lf1, MessageSort.REPLY_LITERAL);		
		Edge msgEdge2 = InteractionNotationHelper.createMessage(viewer, diagram, msg2);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();
		
		InteractionGraph graph = InteractionGraphFactory.getInstance().createInteractionGraph(interaction, diagram, viewer);
		
		JXPathContext context = JXPathContext.newContext(graph);
		GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[1]"), null, 
				lf1, lifelineView1, 
				(Row)context.getValue("rows[1]"), 
				(Column)context.getValue("columns[1]"), 2);
		GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[2]"), null, 
				lf2, lifelineView2, 
				(Row)context.getValue("rows[1]"), 
				(Column)context.getValue("columns[2]"), 2);
		
		GraphAssert.assertSendMessageNode(
				(Node)context.getValue("lifelineClusters[1]/nodes[1]"),
				lf1, 
				msg1.getSendEvent(), null,
				(Node)context.getValue("lifelineClusters[2]/nodes[1]"), 
				(Row)context.getValue("rows[2]"), 
				(Column)context.getValue("columns[1]"));
		GraphAssert.assertReceivingMessageNode(
				(Node)context.getValue("lifelineClusters[2]/nodes[1]"),
				lf2, 
				msg1.getReceiveEvent(), null,
				(Node)context.getValue("lifelineClusters[1]/nodes[1]"), 
				(Row)context.getValue("rows[2]"), 
				(Column)context.getValue("columns[2]"));

		GraphAssert.assertMessageLink(
				(Link)context.getValue("messageLinks[1]"), 
				msg1, msgEdge1,
				(Node)context.getValue("lifelineClusters[1]/nodes[1]"),
				(Node)context.getValue("lifelineClusters[2]/nodes[1]"));

		GraphAssert.assertSendMessageNode(
				(Node)context.getValue("lifelineClusters[2]/nodes[2]"),
				lf2, 
				msg2.getSendEvent(), null,
				(Node)context.getValue("lifelineClusters[1]/nodes[2]"), 
				(Row)context.getValue("rows[3]"), 
				(Column)context.getValue("columns[2]"));
		GraphAssert.assertReceivingMessageNode(
				(Node)context.getValue("lifelineClusters[1]/nodes[2]"),
				lf1, 
				msg2.getReceiveEvent(), null,//(View)msgEdge2.getTransientChildren().get(1),
				(Node)context.getValue("lifelineClusters[2]/nodes[2]"), 
				(Row)context.getValue("rows[3]"), 
				(Column)context.getValue("columns[1]"));

		GraphAssert.assertMessageLink(
				(Link)context.getValue("messageLinks[2]"), 
				msg2, msgEdge2,
				(Node)context.getValue("lifelineClusters[2]/nodes[2]"),
				(Node)context.getValue("lifelineClusters[1]/nodes[2]"));

		editor.waitForClose(diagram);
	}

	/**
	 *   +---+            +---+
	 *   | A |            | B |
	 *   +---+            +---+
	 *     |                |
	 *     |-------------|>+++
	 *     |               | |
	 *     |               | |
	 *     |<..............+++
	 *     |                |
	 */
	@Test
	public void testBuildGraph2LifelinesSynchMessage() {
		ModelSet modelSet = editor.getResourceSet();
		InteractionModelHelper.startTransaction(modelSet);		
		Interaction interaction = UMLFactory.eINSTANCE.createInteraction();
		interaction.setName("test");		
		Diagram diagram = InteractionNotationHelper.createSequenceDiagram(interaction);
		diagram.setName("test");				
		editor.initDiagramAndModel(interaction, diagram);				
		InteractionModelHelper.endTransaction();

		editor.openDiagram(diagram);
		EditPartViewer viewer = InteractionNotationHelper.getEditPartViewer(diagram);

		InteractionModelHelper.startTransaction(interaction);
		Lifeline lf1 = interaction.createLifeline("Lifeline1");		
		View lifelineView1 = InteractionNotationHelper.createLifeline(viewer, diagram, lf1);
		InteractionNotationHelper.setLayoutPosition((org.eclipse.gmf.runtime.notation.Node)lifelineView1,0,10);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Lifeline lf2 = interaction.createLifeline("Lifeline2");
		View lifelineView2 = InteractionNotationHelper.createLifeline(viewer, diagram, lf2);
		Rectangle r = ViewUtilities.getBoundsForLayoutConstraint(viewer, lifelineView1);
		InteractionNotationHelper.setLayoutPosition((org.eclipse.gmf.runtime.notation.Node)lifelineView2, 5 + r.x + r.width,10);		
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Message msg1 = InteractionModelHelper.createMessage(interaction, "Call", lf1, lf2, MessageSort.SYNCH_CALL_LITERAL);
		ExecutionSpecification execSpec = (ExecutionSpecification)interaction.getFragments().stream().
				filter(ExecutionSpecification.class::isInstance).findFirst().orElse(null);
		Edge msgEdge1 = InteractionNotationHelper.createMessage(viewer, diagram, msg1);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();
		
		InteractionModelHelper.startTransaction(interaction);
		Message msg2 = InteractionModelHelper.createReturnMessage(interaction, "return Call", lf2, lf1, msg1);		
		Edge msgEdge2 = InteractionNotationHelper.createMessage(viewer, diagram, msg2);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionGraph graph = InteractionGraphFactory.getInstance().createInteractionGraph(interaction, diagram, viewer);
		JXPathContext context = JXPathContext.newContext(graph);
		GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[1]"), null, 
				lf1, lifelineView1, 
				(Row)context.getValue("rows[1]"), 
				(Column)context.getValue("columns[1]"),2);
		GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[2]"), null, 
				lf2, lifelineView2, 
				(Row)context.getValue("rows[1]"), 
				(Column)context.getValue("columns[2]"),1);
		
		GraphAssert.assertSendMessageNode(
				(Node)context.getValue("lifelineClusters[1]/nodes[1]"),
				lf1, 
				msg1.getSendEvent(), null,//(View)msgEdge1.getTransientChildren().get(0),
				(Node)context.getValue("lifelineClusters[2]/nodes[1]"), 
				(Row)context.getValue("rows[2]"), 
				(Column)context.getValue("columns[1]"));
		GraphAssert.assertReceivingClusterNode(
				(Cluster)context.getValue("lifelineClusters[2]/nodes[1]"),
				lf2, 
				execSpec, msgEdge1.getTarget(),
				(Node)context.getValue("lifelineClusters[1]/nodes[1]"), 
				3);
		

		GraphAssert.assertNode((Node)context.getValue("lifelineClusters[2]/nodes[1]/nodes[1]"),
				execSpec, 
				msg1.getReceiveEvent(), null,//(View)execSpecView.getTransientChildren().get(0), 
				(Row)context.getValue("rows[2]"), 
				(Column)context.getValue("columns[2]"));
		GraphAssert.assertNode((Node)context.getValue("lifelineClusters[2]/nodes[1]/nodes[2]"),
				execSpec, 
				execSpec, msgEdge1.getTarget(), 
				(Row)context.getValue("rows[2]"), 
				(Column)context.getValue("columns[2]"));
		
		
		GraphAssert.assertSendMessageNode(
				(Node)context.getValue("lifelineClusters[2]/nodes[1]/nodes[3]"),
				execSpec, 
				msg2.getSendEvent(), null,//(View)execSpecView.getTransientChildren().get(1),
				(Node)context.getValue("lifelineClusters[1]/nodes[2]"), 
				(Row)context.getValue("rows[3]"), 
				(Column)context.getValue("columns[2]"));
		GraphAssert.assertReceivingMessageNode(
				(Node)context.getValue("lifelineClusters[1]/nodes[2]"),
				lf1, 
				msg2.getReceiveEvent(), null,//(View)msgEdge2.getTransientChildren().get(1),
				(Node)context.getValue("lifelineClusters[2]/nodes[1]/nodes[3]"), 
				(Row)context.getValue("rows[3]"), 
				(Column)context.getValue("columns[1]"));

		editor.waitForClose(diagram);
	}

	/**
	 *   +---+            +---+
	 *   | A |            | B |
	 *   +---+            +---+
	 *     |                |
	 *     |               +++
	 *     |               | |
	 *     |-------------->| |
	 *     |               | |
	 *     |               | |
	 *     |<--------------| |
	 *     |               | |
	 *     |               +++
	 *     |                |
	 */
	@Test
	public void testBuildGraph2LifelinesAsynchMessageInExecSpec() {
		ModelSet modelSet = editor.getResourceSet();
		InteractionModelHelper.startTransaction(modelSet);		
		Interaction interaction = UMLFactory.eINSTANCE.createInteraction();
		interaction.setName("test");		
		Diagram diagram = InteractionNotationHelper.createSequenceDiagram(interaction);
		diagram.setName("test");				
		editor.initDiagramAndModel(interaction, diagram);				
		InteractionModelHelper.endTransaction();

		editor.openDiagram(diagram);
		EditPartViewer viewer = InteractionNotationHelper.getEditPartViewer(diagram);

		InteractionModelHelper.startTransaction(interaction);
		Lifeline lf1 = interaction.createLifeline("Lifeline1");		
		View lifelineView1 = InteractionNotationHelper.createLifeline(viewer, diagram, lf1);
		InteractionNotationHelper.setLayoutPosition((org.eclipse.gmf.runtime.notation.Node)lifelineView1,0,10);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Lifeline lf2 = interaction.createLifeline("Lifeline2");		
		View lifelineView2 = InteractionNotationHelper.createLifeline(viewer, diagram, lf2);
		Rectangle r = ViewUtilities.getBoundsForLayoutConstraint(viewer, lifelineView1);
		InteractionNotationHelper.setLayoutPosition((org.eclipse.gmf.runtime.notation.Node)lifelineView2, 5 + r.x + r.width,10);		
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();
		
		InteractionModelHelper.startTransaction(interaction);
		ActionExecutionSpecification execSpec = InteractionModelHelper.startActionExecutionSpecification(interaction, "ExecSpec1", lf2);		
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();
		
		InteractionModelHelper.startTransaction(interaction);
		Message msg1 = InteractionModelHelper.createMessage(interaction, "AsynchCall", lf1, lf2, MessageSort.ASYNCH_CALL_LITERAL);
		Edge msgEdge1 = InteractionNotationHelper.createMessage(viewer, diagram, msg1);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Message msg2 = InteractionModelHelper.createMessage(interaction, "AsynchCallResp", lf2, lf1, MessageSort.ASYNCH_CALL_LITERAL);		
		Edge msgEdge2 = InteractionNotationHelper.createMessage(viewer, diagram, msg2);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();
		
		InteractionModelHelper.startTransaction(interaction);
		InteractionModelHelper.endExecutionSpecification(execSpec);
		View execSpecView = InteractionNotationHelper.createExecutionSpecification(viewer, diagram, execSpec);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionGraph graph = InteractionGraphFactory.getInstance().createInteractionGraph(interaction, diagram, viewer);
		JXPathContext context = JXPathContext.newContext(graph);
		GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[1]"), null, 
				lf1, lifelineView1, 
				(Row)context.getValue("rows[1]"), 
				(Column)context.getValue("columns[1]"),2);
		GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[2]"), null, 
				lf2, lifelineView2, 
				(Row)context.getValue("rows[1]"), 
				(Column)context.getValue("columns[2]"),1);
		
		GraphAssert.assertExecuteSpecificationGroup(
				(Cluster)context.getValue("lifelineClusters[2]/nodes[1]"),
				lf2, 	
				execSpec,(View)execSpecView,
				execSpec.getStart(), null, //(View)execSpecView.getTransientChildren().get(0),
				execSpec.getFinish(), null, //(View)execSpecView.getTransientChildren().get(1),
				(Row)context.getValue("rows[2]"),(Row)context.getValue("rows[5]"), 
				(Column)context.getValue("columns[2]"), 5);

		GraphAssert.assertSendMessageNode(
				(Node)context.getValue("lifelineClusters[1]/nodes[1]"),
				lf1, 
				msg1.getSendEvent(), null, //(View)msgEdge1.getTransientChildren().get(0),
				(Node)context.getValue("lifelineClusters[2]/nodes[1]/nodes[3]"), 
				(Row)context.getValue("rows[3]"), 
				(Column)context.getValue("columns[1]"));
		GraphAssert.assertReceivingMessageNode(
				(Node)context.getValue("lifelineClusters[2]/nodes[1]/nodes[3]"),
				execSpec, 
				msg1.getReceiveEvent(), null, //(View)msgEdge1.getTransientChildren().get(1),
				(Node)context.getValue("lifelineClusters[1]/nodes[1]"), 
				(Row)context.getValue("rows[3]"), 
				(Column)context.getValue("columns[2]"));

		GraphAssert.assertSendMessageNode(
				(Node)context.getValue("lifelineClusters[2]/nodes[1]/nodes[4]"),
				execSpec, 
				msg2.getSendEvent(), null, //(View)msgEdge2.getTransientChildren().get(0),
				(Node)context.getValue("lifelineClusters[1]/nodes[2]"), 
				(Row)context.getValue("rows[4]"), 
				(Column)context.getValue("columns[2]"));
		GraphAssert.assertReceivingMessageNode(
				(Node)context.getValue("lifelineClusters[1]/nodes[2]"),
				lf1, 
				msg2.getReceiveEvent(), null, //(View)msgEdge2.getTransientChildren().get(1),
				(Node)context.getValue("lifelineClusters[2]/nodes[1]/nodes[4]"), 
				(Row)context.getValue("rows[4]"), 
				(Column)context.getValue("columns[1]"));
		
		editor.waitForClose(diagram);
	}

	/**
	 *   +---+            +---+
	 *   | A |            | B |
	 *   +---+            +---+
	 *     |                |
	 * G-->|                |
	 *     |--------------->|
	 *     |                |
	 *     |<---------------|
	 * G<--|                |
	 *     |                |
	 */
	@Test
	public void testBuildGraph2LifelinesAsynchMessageWithGates() {
		ModelSet modelSet = editor.getResourceSet();
		InteractionModelHelper.startTransaction(modelSet);		
		Interaction interaction = UMLFactory.eINSTANCE.createInteraction();
		interaction.setName("test");		
		Diagram diagram = InteractionNotationHelper.createSequenceDiagram(interaction);
		diagram.setName("test");				
		editor.initDiagramAndModel(interaction, diagram);				
		InteractionModelHelper.endTransaction();

		editor.openDiagram(diagram);
		EditPartViewer viewer = InteractionNotationHelper.getEditPartViewer(diagram);
		
		InteractionModelHelper.startTransaction(interaction);
		Lifeline lf1 = interaction.createLifeline("Lifeline1");		
		View lifelineView1 = InteractionNotationHelper.createLifeline(viewer, diagram, lf1);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Lifeline lf2 = interaction.createLifeline("Lifeline2");
		View lifelineView2 = InteractionNotationHelper.createLifeline(viewer, diagram, lf2);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Gate gate1 = interaction.createFormalGate("Gate1");
		View gateView1 = InteractionNotationHelper.createGate(viewer, diagram, gate1, SWT.LEFT);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Message msg1 = InteractionModelHelper.createMessage(interaction, "G-AsynchCall", gate1, lf1, MessageSort.ASYNCH_CALL_LITERAL);
		Edge msgEdge1 = InteractionNotationHelper.createMessage(viewer, diagram, msg1);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Message msg2 = InteractionModelHelper.createMessage(interaction, "AsynchCall", lf1, lf2, MessageSort.ASYNCH_CALL_LITERAL);
		Edge msgEdge2 = InteractionNotationHelper.createMessage(viewer, diagram, msg2);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();
		
		InteractionModelHelper.startTransaction(interaction);
		Message msg3 = InteractionModelHelper.createMessage(interaction, "AsynchCallResp", lf2, lf1, MessageSort.ASYNCH_CALL_LITERAL);		
		Edge msgEdge3 = InteractionNotationHelper.createMessage(viewer, diagram, msg3);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Gate gate2 = interaction.createFormalGate("Gate2");
		View gateView2 = InteractionNotationHelper.createGate(viewer, diagram, gate2, SWT.LEFT);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();
		
		InteractionModelHelper.startTransaction(interaction);
		Message msg4 = InteractionModelHelper.createMessage(interaction, "G-AsynchCallResp", lf1, gate2, MessageSort.ASYNCH_CALL_LITERAL);		
		Edge msgEdge4 = InteractionNotationHelper.createMessage(viewer, diagram, msg4);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionGraph graph = InteractionGraphFactory.getInstance().createInteractionGraph(interaction, diagram, viewer);
		JXPathContext context = JXPathContext.newContext(graph);
		try {
			GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[1]"), null, 
					lf1, lifelineView1, 
					(Row)context.getValue("rows[1]"), 
					(Column)context.getValue("columns[2]"),4);
			GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[2]"), null, 
					lf2, lifelineView2, 
					(Row)context.getValue("rows[1]"), 
					(Column)context.getValue("columns[3]"),2);
			
			GraphAssert.assertSendMessageNode(
					(Node)context.getValue("formalGates[1]"),
					interaction,
					msg1.getSendEvent(), gateView1,
					(Node)context.getValue("lifelineClusters[1]/nodes[1]"),
					(Row)context.getValue("rows[2]"), 
					(Column)context.getValue("columns[1]"));
	
			GraphAssert.assertReceivingMessageNode(
					(Node)context.getValue("lifelineClusters[1]/nodes[1]"),
					lf1, 
					msg1.getReceiveEvent(), null, //(View)msgEdge1.getTransientChildren().get(0),
					(Node)context.getValue("formalGates[1]"), 
					(Row)context.getValue("rows[2]"), 
					(Column)context.getValue("columns[2]"));
	
			GraphAssert.assertSendMessageNode(
					(Node)context.getValue("lifelineClusters[1]/nodes[2]"),
					lf1, 
					msg2.getSendEvent(), null, //(View)msgEdge2.getTransientChildren().get(0),
					(Node)context.getValue("lifelineClusters[2]/nodes[1]"), 
					(Row)context.getValue("rows[3]"), 
					(Column)context.getValue("columns[2]"));
			GraphAssert.assertReceivingMessageNode(
					(Node)context.getValue("lifelineClusters[2]/nodes[1]"),
					lf2, 
					msg2.getReceiveEvent(), null, //(View)msgEdge2.getTransientChildren().get(1),
					(Node)context.getValue("lifelineClusters[1]/nodes[2]"), 
					(Row)context.getValue("rows[3]"), 
					(Column)context.getValue("columns[3]"));
	
			GraphAssert.assertSendMessageNode(
					(Node)context.getValue("lifelineClusters[2]/nodes[2]"),
					lf2, 
					msg3.getSendEvent(), null, //(View)msgEdge3.getTransientChildren().get(0),
					(Node)context.getValue("lifelineClusters[1]/nodes[3]"), 
					(Row)context.getValue("rows[4]"), 
					(Column)context.getValue("columns[3]"));
			GraphAssert.assertReceivingMessageNode(
					(Node)context.getValue("lifelineClusters[1]/nodes[3]"),
					lf1, 
					msg3.getReceiveEvent(), null, //(View)msgEdge3.getTransientChildren().get(1),
					(Node)context.getValue("lifelineClusters[2]/nodes[2]"), 
					(Row)context.getValue("rows[4]"), 
					(Column)context.getValue("columns[2]"));
	
			GraphAssert.assertSendMessageNode(
					(Node)context.getValue("lifelineClusters[1]/nodes[4]"),
					lf1, 
					msg4.getSendEvent(), null, //(View)msgEdge4.getTransientChildren().get(0),
					(Node)context.getValue("formalGates[2]"),
					(Row)context.getValue("rows[5]"), 
					(Column)context.getValue("columns[2]"));
	
			GraphAssert.assertReceivingMessageNode(
					(Node)context.getValue("formalGates[2]"),
					interaction, 
					msg4.getReceiveEvent(), gateView2,
					(Node)context.getValue("lifelineClusters[1]/nodes[4]"),
					(Row)context.getValue("rows[5]"), 
					(Column)context.getValue("columns[1]"));
		} finally {
			editor.waitForClose(diagram);
		}
	}

	/**
	 *   +---+            +---+
	 *   | A |            | B |
	 *   +---+            +---+
	 *     |                |
	 *     |                |<-- G
	 *     |--------------->|
	 *     |                |
	 *     |<---------------|
	 *     |                |--> G
	 *     |                |
	 */
	@Test
	//@Ignore(value="Horizaontal layouting for gates not implemented.")
	public void testBuildGraph2LifelinesAsynchMessageWithGates2() {
		ModelSet modelSet = editor.getResourceSet();
		InteractionModelHelper.startTransaction(modelSet);		
		Interaction interaction = UMLFactory.eINSTANCE.createInteraction();
		interaction.setName("test");		
		Diagram diagram = InteractionNotationHelper.createSequenceDiagram(interaction);
		diagram.setName("test");				
		editor.initDiagramAndModel(interaction, diagram);				
		InteractionModelHelper.endTransaction();

		editor.openDiagram(diagram);
		EditPartViewer viewer = InteractionNotationHelper.getEditPartViewer(diagram);
				
		InteractionModelHelper.startTransaction(interaction);
		Lifeline lf1 = interaction.createLifeline("Lifeline1");		
		View lifelineView1 = InteractionNotationHelper.createLifeline(viewer, diagram, lf1);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Lifeline lf2 = interaction.createLifeline("Lifeline2");
		View lifelineView2 = InteractionNotationHelper.createLifeline(viewer, diagram, lf2);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Gate gate1 = interaction.createFormalGate("Gate1");
		View gateView1 = InteractionNotationHelper.createGate(viewer, diagram, gate1, SWT.RIGHT);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Message msg1 = InteractionModelHelper.createMessage(interaction, "G-AsynchCall", gate1, lf2, MessageSort.ASYNCH_CALL_LITERAL);
		Edge msgEdge1 = InteractionNotationHelper.createMessage(viewer, diagram, msg1);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Message msg2 = InteractionModelHelper.createMessage(interaction, "AsynchCall", lf1, lf2, MessageSort.ASYNCH_CALL_LITERAL);
		Edge msgEdge2 = InteractionNotationHelper.createMessage(viewer, diagram, msg2);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Message msg3 = InteractionModelHelper.createMessage(interaction, "AsynchCallResp", lf2, lf1, MessageSort.ASYNCH_CALL_LITERAL);		
		Edge msgEdge3 = InteractionNotationHelper.createMessage(viewer, diagram, msg3);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Gate gate2 = interaction.createFormalGate("Gate2");
		View gateView2 = InteractionNotationHelper.createGate(viewer, diagram, gate2, SWT.RIGHT);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();
		
		InteractionModelHelper.startTransaction(interaction);
		Message msg4 = InteractionModelHelper.createMessage(interaction, "G-AsynchCallResp", lf2, gate2, MessageSort.ASYNCH_CALL_LITERAL);		
		Edge msgEdge4 = InteractionNotationHelper.createMessage(viewer, diagram, msg4);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionGraph graph = InteractionGraphFactory.getInstance().createInteractionGraph(interaction, diagram, viewer);
		JXPathContext context = JXPathContext.newContext(graph);
		GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[1]"), null, 
				lf1, lifelineView1, 
				(Row)context.getValue("rows[1]"), 
				(Column)context.getValue("columns[1]"),2);
		GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[2]"), null, 
				lf2, lifelineView2, 
				(Row)context.getValue("rows[1]"), 
				(Column)context.getValue("columns[2]"),4);
		
		GraphAssert.assertSendMessageNode(
				(Node)context.getValue("formalGates[1]"),
				interaction,
				msg1.getSendEvent(), gateView1,
				(Node)context.getValue("lifelineClusters[2]/nodes[1]"),
				(Row)context.getValue("rows[2]"), 
				(Column)context.getValue("columns[3]"));

		GraphAssert.assertReceivingMessageNode(
				(Node)context.getValue("lifelineClusters[2]/nodes[1]"),
				lf2, 
				msg1.getReceiveEvent(), null, //(View)msgEdge1.getTransientChildren().get(0),
				(Node)context.getValue("formalGates[1]"), 
				(Row)context.getValue("rows[2]"), 
				(Column)context.getValue("columns[2]"));

		GraphAssert.assertSendMessageNode(
				(Node)context.getValue("lifelineClusters[1]/nodes[1]"),
				lf1, 
				msg2.getSendEvent(), null, //(View)msgEdge2.getTransientChildren().get(0),
				(Node)context.getValue("lifelineClusters[2]/nodes[2]"), 
				(Row)context.getValue("rows[3]"), 
				(Column)context.getValue("columns[1]"));
		GraphAssert.assertReceivingMessageNode(
				(Node)context.getValue("lifelineClusters[2]/nodes[2]"),
				lf2, 
				msg2.getReceiveEvent(), null, //(View)msgEdge2.getTransientChildren().get(1),
				(Node)context.getValue("lifelineClusters[1]/nodes[1]"), 
				(Row)context.getValue("rows[3]"), 
				(Column)context.getValue("columns[2]"));

		GraphAssert.assertSendMessageNode(
				(Node)context.getValue("lifelineClusters[2]/nodes[3]"),
				lf2, 
				msg3.getSendEvent(), null, //(View)msgEdge3.getTransientChildren().get(0),
				(Node)context.getValue("lifelineClusters[1]/nodes[2]"), 
				(Row)context.getValue("rows[4]"), 
				(Column)context.getValue("columns[2]"));
		GraphAssert.assertReceivingMessageNode(
				(Node)context.getValue("lifelineClusters[1]/nodes[2]"),
				lf1, 
				msg3.getReceiveEvent(), null, //(View)msgEdge3.getTransientChildren().get(1),
				(Node)context.getValue("lifelineClusters[2]/nodes[3]"), 
				(Row)context.getValue("rows[4]"), 
				(Column)context.getValue("columns[1]"));

		GraphAssert.assertSendMessageNode(
				(Node)context.getValue("lifelineClusters[2]/nodes[4]"),
				lf2, 
				msg4.getSendEvent(), null, //(View)msgEdge4.getTransientChildren().get(0),
				(Node)context.getValue("formalGates[2]"),
				(Row)context.getValue("rows[5]"), 
				(Column)context.getValue("columns[2]"));

		GraphAssert.assertReceivingMessageNode(
				(Node)context.getValue("formalGates[2]"),
				interaction, 
				msg4.getReceiveEvent(), gateView2,
				(Node)context.getValue("lifelineClusters[2]/nodes[4]"),
				(Row)context.getValue("rows[5]"), 
				(Column)context.getValue("columns[3]"));
	}

	/**
	 *   +---+            +---+
	 *   | A |            | B |
	 *   +---+            +---+
	 *     |                |
	 *     |                |
	 *    +++               |
	 *    | |-----msg1---|>+++
	 *    | |              | |
	 *    |+++<|--msg2-----| |
	 *    || |             | |
	 *    |+++....msg3....>| |
	 *    | |              | |
	 *    | |<....msg4.....+++
	 *    | |               |
	 *    +++               |
	 *     |                |
	 */
	@Test
	public void testBuildGraph2LifelinesSynchMessage2Levels() {
		ModelSet modelSet = editor.getResourceSet();
		InteractionModelHelper.startTransaction(modelSet);		
		Interaction interaction = UMLFactory.eINSTANCE.createInteraction();
		interaction.setName("test");		
		Diagram diagram = InteractionNotationHelper.createSequenceDiagram(interaction);
		diagram.setName("test");				
		editor.initDiagramAndModel(interaction, diagram);				
		InteractionModelHelper.endTransaction();

		editor.openDiagram(diagram);
		EditPartViewer viewer = InteractionNotationHelper.getEditPartViewer(diagram);
		
		InteractionModelHelper.startTransaction(interaction);
		Lifeline lf1 = interaction.createLifeline("Lifeline1");		
		View lifelineView1 = InteractionNotationHelper.createLifeline(viewer, diagram, lf1);		
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Lifeline lf2 = interaction.createLifeline("Lifeline2");
		View lifelineView2 = InteractionNotationHelper.createLifeline(viewer, diagram, lf2);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);		
		ActionExecutionSpecification exec1 = InteractionModelHelper.startActionExecutionSpecification(interaction, "InitExec", lf1);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);		
		Message msg1 = InteractionModelHelper.createMessage(interaction, "AsynchCall", lf1, lf2, MessageSort.SYNCH_CALL_LITERAL);
		Edge msgEdge1 = InteractionNotationHelper.createMessage(viewer, diagram, msg1);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Message msg2 = InteractionModelHelper.createMessage(interaction, "AsynchCallLevel2", lf2, lf1, MessageSort.SYNCH_CALL_LITERAL);		
		Edge msgEdge2 = InteractionNotationHelper.createMessage(viewer, diagram, msg2);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Message msg3 = InteractionModelHelper.createReturnMessage(interaction, "return AsynchCallLevel2", lf1, lf2, msg2);		
		Edge msgEdge3 = InteractionNotationHelper.createMessage(viewer, diagram, msg3);		
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		Message msg4 = InteractionModelHelper.createReturnMessage(interaction, "return AsynchCallLevel1", lf2, lf1, msg1);		
		Edge msgEdge4 = InteractionNotationHelper.createMessage(viewer, diagram, msg4);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		InteractionModelHelper.startTransaction(interaction);
		InteractionModelHelper.endExecutionSpecification(exec1);
		View exec1View = InteractionNotationHelper.createExecutionSpecification(viewer, diagram, exec1);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();

		ExecutionSpecification[] execSpecs = interaction.getFragments().stream().
				filter(ActionExecutionSpecification.class::isInstance).
				map(ActionExecutionSpecification.class::cast).
				collect(Collectors.toList()).toArray(new ExecutionSpecification[0]);

		InteractionGraph graph = InteractionGraphFactory.getInstance().createInteractionGraph(interaction, diagram, viewer);
		JXPathContext context = JXPathContext.newContext(graph);

		GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[1]"), null, 
				lf1, lifelineView1, 
				(Row)context.getValue("rows[1]"), 
				(Column)context.getValue("columns[1]"),1);
		GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[2]"), null, 
				lf2, lifelineView2, 
				(Row)context.getValue("rows[1]"), 
				(Column)context.getValue("columns[2]"),1);
		
		// Lifeline1
		GraphAssert.assertExecuteSpecificationGroup(
				(Cluster)context.getValue("lifelineClusters[1]/nodes[1]"), 
				lf1,
				execSpecs[0], exec1View, 
				execSpecs[0].getStart(), null, //(View)exec1View.getTransientChildren().get(0), 
				execSpecs[0].getFinish(), null, //(View)exec1View.getTransientChildren().get(1),
				(Row)context.getValue("rows[2]"), 
				(Row)context.getValue("rows[7]"), 
				(Column)context.getValue("columns[1]"), 6);
		{
			// Lifeline1::ExecSpec1
			GraphAssert.assertNode(
					(Node)context.getValue("lifelineClusters[1]/nodes[1]/nodes[1]"),
					execSpecs[0],
					execSpecs[0].getStart(), null,
					(Row)context.getValue("rows[2]"), 
					(Column)context.getValue("columns[1]"));
	
			GraphAssert.assertNode(
					(Node)context.getValue("lifelineClusters[1]/nodes[1]/nodes[2]"),
					execSpecs[0],
					execSpecs[0], exec1View,
					(Row)context.getValue("rows[2]"), 
					(Column)context.getValue("columns[1]"));
			
			GraphAssert.assertSendMessageNode(
					(Node)context.getValue("lifelineClusters[1]/nodes[1]/nodes[3]"),
					execSpecs[0],
					msg1.getSendEvent(), null, //(View)msgEdge1.getTransientChildren().get(0),
					(Node)context.getValue("lifelineClusters[2]/nodes[1]"), 
					(Row)context.getValue("rows[3]"), 
					(Column)context.getValue("columns[1]"));
	
			GraphAssert.assertExecuteSpecificationGroup(
					(Cluster)context.getValue("lifelineClusters[1]/nodes[1]/nodes[4]"), 
					execSpecs[0],
					execSpecs[2], msgEdge2.getTarget(),  
					execSpecs[2].getStart(), null, //(View)msgEdge2.getTransientChildren().get(1), 
					execSpecs[2].getFinish(), null, //(View)msgEdge3.getTransientChildren().get(0),
					(Node)context.getValue("lifelineClusters[2]/nodes[1]/nodes[3]"),
					(Node)context.getValue("lifelineClusters[2]/nodes[1]/nodes[4]"),
					(Row)context.getValue("rows[4]"), 
					(Row)context.getValue("rows[5]"), 
					(Column)context.getValue("columns[1]"), 3);
			{
				// Lifeline1::ExecSpec1::ExecSpec3		
				GraphAssert.assertNode(
						(Node)context.getValue("lifelineClusters[1]/nodes[1]/nodes[4]/nodes[1]"),
						execSpecs[2],
						msg2.getReceiveEvent(), null, //(View)msgEdge2.getTransientChildren().get(1), 
						(Row)context.getValue("rows[4]"), 
						(Column)context.getValue("columns[1]"));
				
				GraphAssert.assertNode(
						(Node)context.getValue("lifelineClusters[1]/nodes[1]/nodes[4]/nodes[2]"),
						execSpecs[2],
						execSpecs[2],msgEdge2.getTarget(), 
						(Row)context.getValue("rows[4]"), 
						(Column)context.getValue("columns[1]"));

				GraphAssert.assertSendMessageNode(
						(Node)context.getValue("lifelineClusters[1]/nodes[1]/nodes[4]/nodes[3]"),
						execSpecs[2],
						msg3.getSendEvent(), null, //(View)msgEdge3.getTransientChildren().get(0), 
						(Node)context.getValue("lifelineClusters[2]/nodes[1]/nodes[4]"), 
						(Row)context.getValue("rows[5]"), 
						(Column)context.getValue("columns[1]"));
			}
			GraphAssert.assertReceivingMessageNode(
					(Node)context.getValue("lifelineClusters[1]/nodes[1]/nodes[5]"),
					execSpecs[0],
					msg4.getReceiveEvent(), null, //(View)msgEdge4.getTransientChildren().get(1),
					(Node)context.getValue("lifelineClusters[2]/nodes[1]/nodes[5]"), 
					(Row)context.getValue("rows[6]"), 
					(Column)context.getValue("columns[1]"));

			GraphAssert.assertNode(
					(Node)context.getValue("lifelineClusters[1]/nodes[1]/nodes[6]"),
					execSpecs[0],
					execSpecs[0].getFinish(), null,
					(Row)context.getValue("rows[7]"), 
					(Column)context.getValue("columns[1]"));
		}

		// Lifeline 2
		GraphAssert.assertExecuteSpecificationGroup(
				(Cluster)context.getValue("lifelineClusters[2]/nodes[1]"), 
				lf2,
				execSpecs[1], msgEdge1.getTarget(), 
				execSpecs[1].getStart(), null, //(View)msgEdge1.getTransientChildren().get(1), 
				execSpecs[1].getFinish(), null, //(View)msgEdge4.getTransientChildren().get(0),
				(Node)context.getValue("lifelineClusters[1]/nodes[1]/nodes[3]"),
				(Node)context.getValue("lifelineClusters[1]/nodes[1]/nodes[5]"),
				(Row)context.getValue("rows[3]"), 
				(Row)context.getValue("rows[6]"), 
				(Column)context.getValue("columns[2]"), 5);
		{
			GraphAssert.assertNode(
					(Node)context.getValue("lifelineClusters[2]/nodes[1]/nodes[1]"),
					execSpecs[1],
					execSpecs[1].getStart(), null, //(View)msgEdge1.getTransientChildren().get(1),
					(Row)context.getValue("rows[3]"), 
					(Column)context.getValue("columns[2]"));
	
			GraphAssert.assertNode(
					(Node)context.getValue("lifelineClusters[2]/nodes[1]/nodes[2]"),
					execSpecs[1],
					execSpecs[1], msgEdge1.getTarget(),
					(Row)context.getValue("rows[3]"), 
					(Column)context.getValue("columns[2]"));			

			GraphAssert.assertSendMessageNode(
					(Node)context.getValue("lifelineClusters[2]/nodes[1]/nodes[3]"),
					execSpecs[1],
					msg2.getSendEvent(), null, //(View)msgEdge2.getTransientChildren().get(0),
					(Node)context.getValue("lifelineClusters[1]/nodes[1]/nodes[4]"), 
					(Row)context.getValue("rows[4]"), 
					(Column)context.getValue("columns[2]"));

			GraphAssert.assertReceivingMessageNode(
					(Node)context.getValue("lifelineClusters[2]/nodes[1]/nodes[4]"),
					execSpecs[1],
					msg3.getReceiveEvent(), null, //(View)msgEdge3.getTransientChildren().get(1),
					(Node)context.getValue("lifelineClusters[1]/nodes[1]/nodes[4]/nodes[3]"), 
					(Row)context.getValue("rows[5]"), 
					(Column)context.getValue("columns[2]"));

			GraphAssert.assertSendMessageNode(
					(Node)context.getValue("lifelineClusters[2]/nodes[1]/nodes[5]"),
					execSpecs[1],
					msg4.getSendEvent(), null, //(View)msgEdge4.getTransientChildren().get(0),
					(Node)context.getValue("lifelineClusters[1]/nodes[1]/nodes[5]"), 
					(Row)context.getValue("rows[6]"), 
					(Column)context.getValue("columns[2]"));
		}		
	}
	
	/**
	 *   +---+            
	 *   | A |    <--- Row 0          
	 *   +---+            
	 *     |              
	 *     |---+  <--- Row 1
	 *     |   |
	 *     |<--+  <--- Row 2 
	 *     |   
	 */	
	@Test
	public void testSelfMessage() {
		ModelSet modelSet = editor.getResourceSet();
		InteractionModelHelper.startTransaction(modelSet);		
		Interaction interaction = UMLFactory.eINSTANCE.createInteraction();
		interaction.setName("test");		
		Diagram diagram = InteractionNotationHelper.createSequenceDiagram(interaction);
		diagram.setName("test");				
		editor.initDiagramAndModel(interaction, diagram);				
		InteractionModelHelper.endTransaction();

		editor.openDiagram(diagram);
		EditPartViewer viewer = InteractionNotationHelper.getEditPartViewer(diagram);
		
		InteractionModelHelper.startTransaction(interaction);
		Lifeline lf1 = interaction.createLifeline("Lifeline1");		
		View lifelineView1 = InteractionNotationHelper.createLifeline(viewer, diagram, lf1);		
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();
		
		InteractionModelHelper.startTransaction(interaction);
		Message msg1 = InteractionModelHelper.createMessage(interaction, "AsynchCall", lf1, lf1, MessageSort.ASYNCH_CALL_LITERAL);
		Edge msgEdge1 = InteractionNotationHelper.createMessage(viewer, diagram, msg1);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();
		
		InteractionGraph graph = InteractionGraphFactory.getInstance().createInteractionGraph(interaction, diagram, viewer);
		
		JXPathContext context = JXPathContext.newContext(graph);
		GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[1]"), null, 
				lf1, lifelineView1, 
				(Row)context.getValue("rows[1]"), 
				(Column)context.getValue("columns[1]"), 2);
		
		GraphAssert.assertSendMessageNode(
				(Node)context.getValue("lifelineClusters[1]/nodes[1]"),
				lf1, 
				msg1.getSendEvent(), null, //(View)msgEdge1.getTransientChildren().get(0),
				(Node)context.getValue("lifelineClusters[1]/nodes[2]"), 
				(Row)context.getValue("rows[2]"), 
				(Column)context.getValue("columns[1]"));
		GraphAssert.assertReceivingMessageNode(
				(Node)context.getValue("lifelineClusters[1]/nodes[2]"),
				lf1, 
				msg1.getReceiveEvent(), null, //(View)msgEdge1.getTransientChildren().get(1),
				(Node)context.getValue("lifelineClusters[1]/nodes[1]"), 
				(Row)context.getValue("rows[3]"), 
				(Column)context.getValue("columns[1]"));
	}

	@Rule
	public WorkspaceAndPapyrusEditor editor = new WorkspaceAndPapyrusEditor();
}
