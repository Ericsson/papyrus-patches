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

import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Column;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Link;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.MarkNode;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.MarkNode.Kind;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Row;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.junit.Assert;

public class GraphAssert {
	public static void assertNode(Node node, Element parent, Element el, View view, Row row, Column col) {
		assertNode(node, parent, el, view, null, null, row, col);
	}

	public static void assertReceivingMessageNode(Node node, Element parent, Element el, View view, Node connectedBy, Row row, Column col) {
		assertNode(node, parent, el, view, null, connectedBy, row, col);
	}

	public static void assertMessageLink(Link link, Message msg1, Edge msgEdge1, Node src, Node trg) {
		Assert.assertNotNull(link);
		Assert.assertEquals(msg1, link.getElement());
		Assert.assertEquals(msgEdge1, link.getView());
		Assert.assertEquals(src, link.getSource());
		Assert.assertEquals(trg, link.getTarget());
		
	}

	public static void assertSendMessageNode(Node node, Element parent, Element el, View view, Node connectedNode, Row row, Column col) {
		assertNode(node, parent, el, view, connectedNode, null, row, col);
	}
	
	public static void assertNode(Node node, Element parent, Element el, View view, 
			Node connectedNode, Node connectedBy, Row row, Column col) {
		Assert.assertNotNull(node);
		Assert.assertEquals(parent, node.getParent() == null ? null : node.getParent().getElement());
		Assert.assertEquals(el, node.getElement());
		Assert.assertEquals(view, node.getView());
		
		Assert.assertEquals(connectedNode, node.getConnectedNode());
		Assert.assertEquals(connectedBy, node.getConnectedByNode());
		
		Assert.assertEquals(row, node.getRow());
		Assert.assertEquals(col, node.getColumn());
	}

	public static void assertReceivingClusterNode(Cluster node, Element parent, Element el, View view, 
			Node connectedBy, int size) {
		assertReceivingMessageNode(node, parent, el, view, connectedBy, null, null);		
		Assert.assertEquals(size, node.getNodes().size());
	}

	public static void assertCluster(Cluster node, Element parent, Element el, View view, Row row, Column col, int size) {
		assertNode(node, parent, el, view, null, null, row, col);
		Assert.assertEquals(size, node.getNodes().size());
	}

	public static void assertExecuteSpecificationGroup(Cluster node, Element parent,
			ExecutionSpecification execSpec, View execSpecView, 
			OccurrenceSpecification start, View startView,
			OccurrenceSpecification finish, View finishView, 
			Row startRow, Row endRow, Column col,
			int size) {
		assertExecuteSpecificationGroup(
				node, parent, execSpec, execSpecView, 
				start, startView, finish, finishView, 
				null,null,
				startRow, endRow, col, size);
	}
	
	public static void assertExecuteSpecificationGroup(Cluster node, Element parent,
			ExecutionSpecification execSpec, View execSpecView, 
			OccurrenceSpecification start, View startView,
			OccurrenceSpecification finish, View finishView, 
			Node connectedBy, Node returningNode, 
			Row startRow, Row endRow, Column col, int size) {
		assertReceivingClusterNode(node, parent, execSpec, execSpecView, connectedBy, size);
		assertNode(node.getNodes().get(0), execSpec, start, startView, startRow, col);		
		assertNode(node.getNodes().get(1), execSpec, execSpec, execSpecView, startRow, col);
		assertSendMessageNode(node.getNodes().get(size-1), execSpec, finish, finishView, returningNode, endRow, col);
	}

	public static void assertFragmentCluster(FragmentCluster node, Element element, View view, Cluster... clusters) {
		assertCluster(node, null, element, view, null, null, 0);	
		Assert.assertEquals(clusters.length, node.getClusters().size());
		for (int i=0;i<clusters.length;i++) {
			Assert.assertEquals(clusters[i], node.getClusters().get(i));
		}
	}

	public static void assertInteractionUseGroup(Cluster cluster, Element parent, Element element, View view,
			Node start, Node end, Row startRow, Row endRow, Column col) {
		assertCluster(cluster,parent,element,view,null,null,2);
		Assert.assertEquals(start, cluster.getNodes().get(0));
		Assert.assertEquals(end, cluster.getNodes().get(cluster.getNodes().size()-1));
		Assert.assertEquals(startRow, start.getRow());
		Assert.assertEquals(endRow, end.getRow());
		Assert.assertEquals(col, start.getColumn());
		Assert.assertEquals(col, end.getColumn());
	}

	public static void assertInteractionUseMark(MarkNode node, Element element, Kind start, Row row, Column col) {
		assertNode(node, element, element, null, row, col);
		Assert.assertEquals(start,node.getKind());
	}
}
