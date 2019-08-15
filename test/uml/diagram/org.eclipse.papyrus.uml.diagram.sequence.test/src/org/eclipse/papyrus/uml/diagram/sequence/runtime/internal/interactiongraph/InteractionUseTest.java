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

import org.apache.commons.jxpath.JXPathContext;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Column;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphFactory;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.MarkNode;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Row;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.GraphAssert;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.InteractionModelHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.InteractionNotationHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.WorkspaceAndPapyrusEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.UMLFactory;
import org.junit.Rule;
import org.junit.Test;

public class InteractionGraphInteractionUseTest {
	/**
	 * <pre>   
	 *   +---+            +---+
	 *   | A |            | B |    <--- Row 0
	 *   +---+            +---+
	 *     |                |
	 *     |-----msg1------>|      <--- Row 1
	 *     |                |
	 *   +--------------------+    <--- Row 2
	 *   |      Int. Use      |
	 *   +--------------------+    <--- Row 3
	 *     |                |
	 *     |<-----msg2------|      <--- Row 4
	 *     |                |
	 *  </pre>
	 */
	@Test
	public void testBuildGraph2LifelinesInteractionUse() {
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
		Message msg1 = InteractionModelHelper.createMessage(interaction, "AsynchCall", lf1, lf2, MessageSort.ASYNCH_CALL_LITERAL);
		Edge msgEdge1 = InteractionNotationHelper.createMessage(viewer, diagram, msg1);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		editor.flushDisplayEvents();
		
		InteractionModelHelper.startTransaction(interaction);
		InteractionUse intUse = InteractionModelHelper.createInteractionUse(interaction, "InteractionUse", lf1,lf2);
		View intUseView = InteractionNotationHelper.createInteractionUse(viewer, diagram, intUse);
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
				(Column)context.getValue("columns[1]"),3);
		GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[2]"), null, 
				lf2, lifelineView2, 
				(Row)context.getValue("rows[1]"), 
				(Column)context.getValue("columns[2]"), 3);

		GraphAssert.assertFragmentCluster((FragmentCluster)context.getValue("fragmentClusters[1]"), 
				intUse, intUseView,
				(Cluster)context.getValue("lifelineClusters[1]/nodes[2]"),
				(Cluster)context.getValue("lifelineClusters[2]/nodes[2]"));
		
		
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

		GraphAssert.assertInteractionUseGroup(
				(Cluster)context.getValue("lifelineClusters[1]/nodes[2]"),
				lf1,
				intUse, null,
				(Node)context.getValue("lifelineClusters[1]/nodes[2]/nodes[1]"),
				(Node)context.getValue("lifelineClusters[1]/nodes[2]/nodes[2]"),
				(Row)context.getValue("rows[3]"), 
				(Row)context.getValue("rows[4]"), 
				(Column)context.getValue("columns[1]"));
		{
			GraphAssert.assertInteractionUseMark(
					(MarkNode)context.getValue("lifelineClusters[1]/nodes[2]/nodes[1]"),
					intUse,
					MarkNode.Kind.start,
					(Row)context.getValue("rows[3]"), 
					(Column)context.getValue("columns[1]"));
			GraphAssert.assertInteractionUseMark(
					(MarkNode)context.getValue("lifelineClusters[1]/nodes[2]/nodes[2]"),
					intUse,
					MarkNode.Kind.end,
					(Row)context.getValue("rows[4]"), 
					(Column)context.getValue("columns[1]"));
		}

		GraphAssert.assertInteractionUseGroup(
				(Cluster)context.getValue("lifelineClusters[2]/nodes[2]"),
				lf2,
				intUse, null,
				(Node)context.getValue("lifelineClusters[2]/nodes[2]/nodes[1]"),
				(Node)context.getValue("lifelineClusters[2]/nodes[2]/nodes[2]"),
				(Row)context.getValue("rows[3]"), 
				(Row)context.getValue("rows[4]"), 
				(Column)context.getValue("columns[2]"));
		{
			GraphAssert.assertInteractionUseMark(
					(MarkNode)context.getValue("lifelineClusters[2]/nodes[2]/nodes[1]"),
					intUse,
					MarkNode.Kind.start,
					(Row)context.getValue("rows[3]"), 
					(Column)context.getValue("columns[2]"));
			GraphAssert.assertInteractionUseMark(
					(MarkNode)context.getValue("lifelineClusters[2]/nodes[2]/nodes[2]"),
					intUse,
					MarkNode.Kind.end,
					(Row)context.getValue("rows[4]"), 
					(Column)context.getValue("columns[2]"));
		}

		GraphAssert.assertSendMessageNode(
				(Node)context.getValue("lifelineClusters[2]/nodes[3]"),
				lf2, 
				msg2.getSendEvent(), null,
				(Node)context.getValue("lifelineClusters[1]/nodes[3]"), 
				(Row)context.getValue("rows[5]"), 
				(Column)context.getValue("columns[2]"));
		GraphAssert.assertReceivingMessageNode(
				(Node)context.getValue("lifelineClusters[1]/nodes[3]"),
				lf1, 
				msg2.getReceiveEvent(), null,
				(Node)context.getValue("lifelineClusters[2]/nodes[3]"), 
				(Row)context.getValue("rows[5]"), 
				(Column)context.getValue("columns[1]"));
	}

	/**
	 * <pre>   
	 *   +---+            +---+            +---+
	 *   | A |            | B |            + C +    <--- Row 0
	 *   +---+            +---+            +---+
	 *     |                |                |
	 *     |-----msg1------>|                |      <--- Row 1
	 *     |                |                |
	 *   +--------------------+              |      <--- Row 2
	 *   |      Int. Use      |-----msg2---->|      <--- Row 3 
	 *   +--------------------+              |      <--- Row 4
	 *     |                |                |
	 *     |<-----msg3------|                |      <--- Row 5
	 *     |                |                |
	 *  </pre>
	 */
	@Test
	public void testBuildGraph2LifelinesInteractionUseWhitGate() {
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

		InteractionModelHelper.startTransaction(interaction);
		Lifeline lf2 = interaction.createLifeline("Lifeline2");
		View lifelineView2 = InteractionNotationHelper.createLifeline(viewer, diagram, lf2);		
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);

		InteractionModelHelper.startTransaction(interaction);
		Lifeline lf3 = interaction.createLifeline("Lifeline2");
		View lifelineView3 = InteractionNotationHelper.createLifeline(viewer, diagram, lf3);		
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);

		
		InteractionModelHelper.startTransaction(interaction);
		Message msg1 = InteractionModelHelper.createMessage(interaction, "AsynchCall", lf1, lf2, MessageSort.ASYNCH_CALL_LITERAL);
		Edge msgEdge1 = InteractionNotationHelper.createMessage(viewer, diagram, msg1);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);

		InteractionModelHelper.startTransaction(interaction);
		InteractionUse intUse = InteractionModelHelper.createInteractionUse(interaction, "InteractionUse", lf1,lf2);
		View intUseView = InteractionNotationHelper.createInteractionUse(viewer, diagram, intUse);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);

		InteractionModelHelper.startTransaction(interaction);
		Gate gate1 = intUse.createActualGate("actGate1");
		View gateView1 =  InteractionNotationHelper.createGate(viewer, diagram, gate1, SWT.RIGHT);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);

		InteractionModelHelper.startTransaction(interaction);
		Message msg2 = InteractionModelHelper.createMessage(interaction, "AsynchGate", gate1, lf3, MessageSort.ASYNCH_CALL_LITERAL);		
		Edge msgEdge2 = InteractionNotationHelper.createMessage(viewer, diagram, msg2);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);

		InteractionModelHelper.startTransaction(interaction);
		Message msg3 = InteractionModelHelper.createMessage(interaction, "AsynchCallResp", lf2, lf1, MessageSort.ASYNCH_CALL_LITERAL);		
		Edge msgEdge3 = InteractionNotationHelper.createMessage(viewer, diagram, msg3);
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);

		InteractionGraph graph = InteractionGraphFactory.getInstance().createInteractionGraph(interaction, diagram, viewer);
		JXPathContext context = JXPathContext.newContext(graph);
		
		GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[1]"), null, 
				lf1, lifelineView1 , 
				(Row)context.getValue("rows[1]"), 
				(Column)context.getValue("columns[1]"), 3);
		GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[2]"), null, 
				lf2, lifelineView2, 
				(Row)context.getValue("rows[1]"), 
				(Column)context.getValue("columns[2]"), 3);

		GraphAssert.assertCluster((Cluster)context.getValue("lifelineClusters[3]"), null, 
				lf3, lifelineView3, 
				(Row)context.getValue("rows[1]"), 
				(Column)context.getValue("columns[4]"), 1);

		GraphAssert.assertFragmentCluster((FragmentCluster)context.getValue("fragmentClusters[1]"), 
				intUse, intUseView,
				(Cluster)context.getValue("lifelineClusters[1]/nodes[2]"),
				(Cluster)context.getValue("lifelineClusters[2]/nodes[2]"));
		
		
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

		GraphAssert.assertInteractionUseGroup(
				(Cluster)context.getValue("lifelineClusters[1]/nodes[2]"),
				lf1,
				intUse, null,
				(Node)context.getValue("lifelineClusters[1]/nodes[2]/nodes[1]"),
				(Node)context.getValue("lifelineClusters[1]/nodes[2]/nodes[2]"),
				(Row)context.getValue("rows[3]"), 
				(Row)context.getValue("rows[5]"), 
				(Column)context.getValue("columns[1]"));
		{
			GraphAssert.assertInteractionUseMark(
					(MarkNode)context.getValue("lifelineClusters[1]/nodes[2]/nodes[1]"),
					intUse,
					MarkNode.Kind.start,
					(Row)context.getValue("rows[3]"), 
					(Column)context.getValue("columns[1]"));
			GraphAssert.assertInteractionUseMark(
					(MarkNode)context.getValue("lifelineClusters[1]/nodes[2]/nodes[2]"),
					intUse,
					MarkNode.Kind.end,
					(Row)context.getValue("rows[5]"), 
					(Column)context.getValue("columns[1]"));
		}

		GraphAssert.assertInteractionUseGroup(
				(Cluster)context.getValue("lifelineClusters[2]/nodes[2]"),
				lf2,
				intUse, null,
				(Node)context.getValue("lifelineClusters[2]/nodes[2]/nodes[1]"),
				(Node)context.getValue("lifelineClusters[2]/nodes[2]/nodes[2]"),
				(Row)context.getValue("rows[3]"), 
				(Row)context.getValue("rows[5]"), 
				(Column)context.getValue("columns[2]"));
		{
			GraphAssert.assertInteractionUseMark(
					(MarkNode)context.getValue("lifelineClusters[2]/nodes[2]/nodes[1]"),
					intUse,
					MarkNode.Kind.start,
					(Row)context.getValue("rows[3]"), 
					(Column)context.getValue("columns[2]"));
			GraphAssert.assertInteractionUseMark(
					(MarkNode)context.getValue("lifelineClusters[2]/nodes[2]/nodes[2]"),
					intUse,
					MarkNode.Kind.end,
					(Row)context.getValue("rows[5]"), 
					(Column)context.getValue("columns[2]"));
		}

		GraphAssert.assertSendMessageNode(
				(Node)context.getValue("fragmentClusters[1]/outerGates[1]"),
				intUse, 
				msg2.getSendEvent(), gateView1,
				(Node)context.getValue("lifelineClusters[3]/nodes[1]"), 
				(Row)context.getValue("rows[4]"), 
				(Column)context.getValue("columns[3]"));
		
		GraphAssert.assertReceivingMessageNode(
				(Node)context.getValue("lifelineClusters[3]/nodes[1]"),
				lf3, 
				msg2.getReceiveEvent(), null,
				(Node)context.getValue("fragmentClusters[1]/outerGates[1]"), 
				(Row)context.getValue("rows[4]"), 
				(Column)context.getValue("columns[4]"));
		
		GraphAssert.assertSendMessageNode(
				(Node)context.getValue("lifelineClusters[2]/nodes[3]"),
				lf2, 
				msg3.getSendEvent(), null,
				(Node)context.getValue("lifelineClusters[1]/nodes[3]"), 
				(Row)context.getValue("rows[6]"), 
				(Column)context.getValue("columns[2]"));
		GraphAssert.assertReceivingMessageNode(
				(Node)context.getValue("lifelineClusters[1]/nodes[3]"),
				lf1, 
				msg3.getReceiveEvent(), null,
				(Node)context.getValue("lifelineClusters[2]/nodes[3]"), 
				(Row)context.getValue("rows[6]"), 
				(Column)context.getValue("columns[1]"));
	}

	@Rule
	public WorkspaceAndPapyrusEditor editor = new WorkspaceAndPapyrusEditor();
}
