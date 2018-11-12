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
 * A {@link Column} represent a horizontal position in the diagram.<br/>
 * A {@link Column} can hold a number of nodes depending of the element they are associated to:
 * <ol>
 * <li>The one or both endpoints of a {@link Message}, in general {@link MessageOccurrenceSpecification}, or
 * {@link Gate}. If the message receive endpoint is also the start of an {@link ExecutionSpecification}, the
 * corresponding {@link ExecutionOccurrenceSpecification} is included in the same {@link Column}.</li>
 * <li>The start {@link ExecutionOccurrenceSpecification} and the corresponding
 * {@link ExecutionOccurrenceSpecification} when it does not correspond with a message end.</li>
 * <li>All start or end marks of a {@link CombinedFragment} or a {@link InteractionUse}.</li>
 * <li>A {@link DestructionOccurrenceSpecification}, if it is not the end point of a message.</li>
 * </ol>
 * The cols are fundamental part to recalculate the graphical location of fragments during a edit operation.<br/>
 * They help calculating the visual feedbacks provided by the {@link EditPolicy}.
 * The visual feedback guides the user and help to understand the result of a operation by providing visual
 * marks and hints.
 */
public interface Column {
	/**
	 * Provides a convenient method to query the index of this {@link Column} in the list of cols hold in the
	 * {@link InteractionGraph}.
	 * 
	 * @return a 0-based integer.
	 */
	public int getIndex();

	/**
	 * Returns the list of Nodes located in this {@link Column}.
	 * 
	 * @return a unmodificable list of nodes
	 */
	public List<Node> getNodes();

	/**
	 * Convenient method to query the actual x-coordinate of this {@link Column}. This value correspond with the
	 * x-coordinate of the location of the view associated with the nodes located in this {@link Column}.<br/>
	 * The x-coordinate of each view should be provided by the {@link Node} itself, as it is based on the
	 * depth in the {@link ExecutionSpecification} chain and the side of the opposite endpoint, if any, with
	 * respect of the middle point of the lifeline.
	 * 
	 * @return the y-coordinate of the nodes in the {@link Column}.
	 */
	public int getXPosition();

	/**
	 * Returns an unmodificable list with the anchor points of the nodes contained in the {@link Column}.<br>
	 * Each point in the list correspond to the nodes returned by {@link #getNodes()}, in the same order.
	 * 
	 * @return a list of points.
	 */
	public List<Point> getAnchorPoints();
}
