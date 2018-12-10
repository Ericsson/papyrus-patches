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

import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.OccurrenceSpecification;

/**
 * A {@link Cluster} represent a set of Nodes that should be handle together. A examples is the set of Nodes
 * that represent a {@link ExecutionSpecification} defined with <br/>
 * <ol>
 * <li>The start mark, defined with a {@link OccurrenceSpecification}</li>
 * <li>The {@link ExecutionSpecification} itself </it>
 * <li>The end mark, defined with a {@link OccurrenceSpecification}</li>
 * </ol>
 * Note that the start and end mark may be {@link MessageOccurrenceSpecification} or
 * {@link ExecutionOccurrenceSpecification}, depending if the {@link ExecutionSpecification} is started with a
 * {@link Message} or not.<br/>
 * A is included in a container {@link Cluster}, except for the {@link Cluster} associated with the
 * {@link Interaction}, in the same way that single nodes. But it also be part of other Clusters that
 * represent indivisible structures that extends across Lifelines, the {@link FragmentCluster}. Some of the
 * operation on the {@link Cluster} may have an effect on the {@link FragmentCluster}. An example of a
 * {@link FragmentCluster} is the {@link InteractionUse}.
 */
public interface Cluster extends Node {
	/**
	 * Returns the {@link FragmentCluster} that contains this {@link Cluster}.
	 * 
	 * @return the {@link FragmentCluster}
	 */
	public FragmentCluster getFragmentCluster();

	/**
	 * Returns the list of nodes contained in this {@link Cluster}. <br/>
	 * The list of nodes is ordering following the semantical order defined in the {@link Interaction}.
	 * 
	 * @return a list of {@link Node}
	 */
	public List<Node> getNodes();

	public List<Node> getAllNodes();
}
