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

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPolicy;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;

/**
 * A {@link Row} represent a vertical position in the diagram and indirectly the list of rows in the graph
 * keeps the semantical order in the diagram.<br/>
 * A {@link Row} can hold a number of nodes depending of the element they are associated to:
 * <ol>
 * <li>The one or both endpoints of a {@link Message}, in general {@link MessageOccurrenceSpecification}, or
 * {@link Gate}. If the message receive endpoint is also the start of an {@link ExecutionSpecification}, the
 * corresponding {@link ExecutionOccurrenceSpecification} is included in the same {@link Row}.</li>
 * <li>The start {@link ExecutionOccurrenceSpecification} and the corresponding
 * {@link ExecutionOccurrenceSpecification} when it does not correspond with a message end.</li>
 * <li>All start or end marks of a {@link CombinedFragment} or a {@link InteractionUse}.</li>
 * <li>A {@link DestructionOccurrenceSpecification}, if it is not the end point of a message.</li>
 * </ol>
 * The rows can be used to help with the fragment reordering as can easily point out the position in the list
 * of fragments when inserting a new fragment based on its coordinates. They are also a fundamental part to
 * recalculate the graphical location of fragments during a edit operation.<br/>
 * They can also a fundamental part in calculating the visual feedbacks provided by the {@link EditPolicy}.
 * The visual feedback guides the user and help to understand the result of a operation by providing visual
 * marks and hints.
 */
public interface Row {
	/**
	 * Provides a convenient method to query the index of this {@link Row} in the list of rows hold in the
	 * {@link InteractionGraph}.
	 * 
	 * @return a 0-based integer.
	 */
	public int getIndex();

	/**
	 * Returns the list of Nodes located in this {@link Row}.
	 * 
	 * @return a unmodificable list of nodes
	 */
	public List<Node> getNodes();

	/**
	 * Convenient method to query the actual y-coordinate of this {@link Row}. This value correspond with the
	 * y-coordinate of the location of the view associated with the nodes located in this {@link Row}.<br/>
	 * The x-coordinate of each view should be provided by the {@link Node} itself, as it is based on the
	 * depth in the {@link ExecutionSpecification} chain and the side of the opposite endpoint, if any, with
	 * respect of the middle point of the lifeline.
	 * 
	 * @return the y-coordinate of the nodes in the {@link Row}.
	 */
	public int getYPosition();

	/**
	 * Returns an unmodificable list with the anchor points of the nodes contained in the {@link Row}.<br>
	 * Each point in the list correspond to the nodes returned by {@link #getNodes()}, in the same order.
	 * 
	 * @return a list of points.
	 */
	public List<Point> getAnchorPoints();
}
