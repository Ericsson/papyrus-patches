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

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.notation.Location;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.OccurrenceSpecification;

/**
 * A {@link Node} it is the simple part of the graph, it represent an {@link MessageEnd}, an
 * {@link OccurrenceSpecification} or a start / end mark associated with a {@link CombinedFragment} or
 * {@link InteractionUse}. <br/>
 * A {@link Node} it is contained in {@link Cluster}. The {@link Cluster} represent a element that owns a
 * number of nodes that are treated as one when the {@link Cluster}'s element is moved or deleted. <br>
 * A {@link Node} may trigger others {@link Node} or {@link Cluster} through a {@link Message}, a send event
 * end point triggers the receive event end point, which can be also teh start of a
 * {@link ExecutionSpecification}. <br/>
 */
public interface Node {
	/**
	 * Returns the {@link Cluster} that owns this node. Normally a {@link Cluster} associated with a
	 * {@link Lifeline}, an {@link ExecutionSpecification}, a {@link CombinedFragment} or
	 * {@link InteractionUse} part covered in a lifeline.
	 *
	 * @return a {@link Cluster} or null if this {@link Node} is the root.
	 */
	public Cluster getParent();

	/**
	 * Returns the {@link InteractionGraph} this node belongs to.
	 *
	 * @return the {@link InteractionGraph}
	 */
	public InteractionGraph getInteractionGraph();

	/**
	 * Returns the {@link Node} that this node connects to, normally by meaning of a message.<br>
	 *
	 * In other words, from the perspective of this node, the returned node is a part of the same group
	 * of related nodes and cluster that are handle together. But from the perpective of the returned node,
	 * both nodes are unrelated.<br>
	 *
	 * This normally implies the existing of a message
	 * connecting the two nodes, where this node is the sending and the returned node is the receiving.
	 *
	 * @return a {@link Node}
	 */
	public Node getConnectedNode();

	/**
	 * Returns the {@link Node} that connects to this node, normally by meaning of a message.<br>
	 *
	 * In other words, from the perspective of the returned node, this node is a part of the same group
	 * of related nodes and cluster that are handle together. But from the perspective of this node, both
	 * nodes are unrelated.<br>
	 *
	 * This normally implies the existing of a message
	 * connecting the two nodes, where this node is the receiving and the returned node is the sending.
	 *
	 * @return a {@link Node}
	 */
	public Node getConnectedByNode();

	/**
	 * Returns the {@link Element} that this node represents.
	 *
	 * @return a {@link Element} subclass, an {@link InteractionFragment} or a {@link Gate}.
	 */
	public Element getElement();

	/**
	 * The View in the notation model that hold graphical constraints probably, in the form of
	 * {@link Location}.
	 *
	 * @return a {@link View}
	 */
	public View getView();

	/**
	 * The EditPart associated to the view in the notation model. The edit parts are used to calculate the position
	 * in the diagram editor.
	 *
	 * @return a {@link View}
	 */
	public GraphicalEditPart getEditPart();

	/**
	 * The row which holds the logical position in the diagram.
	 *
	 * @return a {@link Row}
	 */
	public Row getRow();

	/**
	 * The col which holds the logical position in the diagram.
	 *
	 * @return a {@link Row}
	 */
	public Column getColumn();

	public Rectangle getBounds();
}
