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
package org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph;

import java.util.List;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;

/**
 * The {@link InteractionGraph} encapsulates the dependencies and composition of the
 * {@link InteractionFragment} for a given {@link Interaction}.<br>
 * It maintains a ordered list of Lifelines corresponding to the order defined in the{@link Interaction}, a
 * list of {@link CombinedFragment} and {@link InteractionUse} defined in the {@link Interaction} and a list
 * of floating elements, like Gates or marks associated to lost or found messages.<br>
 * The {@link InteractionGraph} can be used for reordering of InteractionFragments and Lifelines in the
 * {@link Interaction} and for layout and repositioning operation of the fragments in the {@link Diagram}.
 */
public interface InteractionGraph {

	/**
	 * Returns the diagram used to build this graph, basically to tag the nodes graphical positions.
	 *
	 * @return a gmf notation diagram
	 */
	public Diagram getDiagram();
	public DiagramEditPart getDiagramEditPart();

	public EditPartViewer getEditPartViewer();

	/**
	 * Returns the interaction used to build this graph.
	 *
	 * @return an Interaction uml element
	 */
	public Interaction getInteraction();

	/**
	 * Return a list with the nodes representing the formal gates of the {@link Interaction}.
	 *
	 * @return a list with nodes
	 */
	public List<Node> getFormalGates();

	/**
	 * Return a list with the nodes representing floating elements defined in the interaction, such as a
	 * {@link Comment}, lost and found {@link Message} endpoints, ...
	 *
	 * @return a list with nodes
	 */
	public List<MarkNode> getFloatingNodes();

	/**
	 * Return a list with the clusters representing the lifelines defined in the interaction.<br>
	 *
	 * The order of the list correspond with the order of lifelines in the interaction
	 *
	 * @return a list with nodes
	 */
	public List<Cluster> getLifelineClusters();

	/**
	 * Return a list with the clusters representing {@link InteractionUse} and {@link CombinedFragment}.<br>
	 *
	 * @return a list with nodes
	 */
	public List<FragmentCluster> getFragmentClusters();

	/**
	 * Return a list with the clusters representing the lifelines defined in the interaction.<br>
	 *
	 * The order of the list correspond with the order of lifelines in the interaction
	 *
	 * @return a list with nodes
	 */
	public List<Link> getMessageLinks();

	/**
	 * Return the ordered list of rows used in the graph.
	 *
	 * @return an ordered list with the rows.
	 */
	public List<Row> getRows();

	/**
	 * Return the ordered list of cols used in the graph.
	 *
	 * @return an ordered list with the rows.
	 */
	public List<Column> getColumns();
	
	/** 
	 * Return the ordered and flatten list of nodes (excluding clusters) that represent fragments, gates, start and end 
	 * of combined fragments and interaction usages.
	 * 
	 * The normal usage of that function is to find which nodes needs to be nudge when the position of a visual element 
	 * is changed.
	 * @return a list with the nodes.
	 */
	public List<Node> getOrderedNodes();
	public List<Node> getLayoutNodes();

	/**
	 * Return the node representing the given {@link Element}
	 *
	 * @param element
	 *            the element for which the {@link Node} is requested
	 * @return a Node representing the given element
	 */
	public Node getNodeFor(Element element);
	public Link getLinkFor(Element element);
	public Cluster getClusterFor(Element element);
	
	public GraphItem getItemFor(Element element);
	
	public void layout();

	/**
	 * Create a {@link Node} for given element. All the nodes and cluster referenced by messages
	 * are also created.
	 *
	 * @param node
	 *            the node to delete
	 * @return true if the node could be deleted, false otherwise
	 */
	// public Node createNodeFor(Element element);

	/**
	 * Delete the given {@link Node} from the graph. All the nodes and cluster referenced by messages
	 * are also deleted.
	 *
	 * @param node
	 *            the node to delete
	 * @return true if the node could be deleted, false otherwise
	 */
	// public boolean deleteNode(Node node);

	/**
	 * Move the given node into a new cluster at the end of the current cluster. All the nodes and cluster
	 * referenced by messages are also moved.
	 *
	 * @param node
	 *            the Node to move.
	 * @param cluster
	 *            the Cluster to move the Node into.
	 * @return true if the node could be move into the new cluster, false otherwise
	 */
	// public boolean moveNodeTo(Node node, Cluster cluster);

	/**
	 * Move the given node into a new cluster at the end of the current cluster. All the nodes and cluster
	 * referenced by messages are also moved.
	 *
	 * @param node
	 *            the Node to move.
	 * @param cluster
	 *            the Cluster to move the Node into.
	 * @param beforeNode
	 *            the node marking the insertion point. The node will be inserted before it.
	 * @return true if the node could be move into the new cluster, false otherwise
	 */
	// public boolean moveNodeTo(Node node, Cluster cluster, Node beforeNode);


	public Cluster getLifeline(Lifeline lifeline);
	public Cluster addLifeline(Lifeline lifeline);
	public Cluster addLifeline(Lifeline lifeline, Cluster beforeLifeline);
	public void moveLifeline(Lifeline lifeline, Lifeline beforeLifeline);

	// TODO: @etxacam Review this APIs... Are they need with out positional info??? 
	//       They could provide "default" positions, for a nice layout....
	
	public Link getMessage(Message message);
	public Link addMessage(Message message);
	public Link addMessage(Message message, Link insertBefore);

	public Node getMessageOccurrenceSpecification(Lifeline lifeline, MessageOccurrenceSpecification mos);
	public Node addMessageOccurrenceSpecification(Lifeline lifeline, MessageOccurrenceSpecification mos);
	public Node addMessageOccurrenceSpecification(Lifeline lifeline, MessageOccurrenceSpecification mos, Node insertBefore);
	public Node getGate(Interaction interaction, Gate gate);
	public Node addGate(Interaction interaction, Gate gate, Node insertBefore);
	public Node getGate(InteractionUse interaction, Gate gate);
	public Node addGate(InteractionUse interaction, Gate gate, Node insertBefore);
	
	public Link connectMessageEnds(MessageEnd send, MessageEnd recv);

	public Cluster getExecutionSpecification(Lifeline lifeline, ExecutionSpecification exec);
	public Cluster addExecutionSpecification(Lifeline lifeline, ExecutionSpecification exec);
	
	public FragmentCluster getInteractionUse(InteractionUse interactionUse);
	public FragmentCluster addInteractionUse(InteractionUse interactionUse, List<Lifeline> lifelines, InteractionFragment beforeFragment);
	public FragmentCluster addInteractionUseToLifeline(InteractionUse interactionUse, Lifeline lifeline);
	public FragmentCluster removeInteractionUseFromLifeline(InteractionUse interactionUse, Lifeline lifeline);	

}
