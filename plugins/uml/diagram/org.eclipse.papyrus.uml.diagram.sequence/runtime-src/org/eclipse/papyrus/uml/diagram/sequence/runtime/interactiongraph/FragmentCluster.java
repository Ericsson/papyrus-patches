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

import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.InteractionUse;

/**
 * A {@link FragmentCluster} represent indivisible structures that extends across Lifeline and are composed
 * of other {@link Cluster}. It is used to represent a {@link InteractionUse}, {@link CombinedFragment} and
 * {@link InteractionOperand}.<br>
 * Some of the operation in a cluster may have an effect on its {@link FragmentCluster}.
 */
public interface FragmentCluster extends Cluster {
	/**
	 * Returns the Cluster that forms this {@link FragmentCluster}. <br/>
	 * The list of clusters is ordering following the order of Lifelines defined in the {@link Interaction}.
	 * 
	 * @return a list of {@link Cluster}.
	 */
	public List<Cluster> getClusters();

	/**
	 * Returns the list of {@link FragmentCluster} contained in this this {@link FragmentCluster}.
	 * 
	 * @return the list of {@link FragmentCluster}
	 */
	public List<FragmentCluster> getOwnedFragmentClusters();

	public List<Node> getAllGates();
	public List<Node> getInnerGates(); // Actual Gates
	public List<Node> getOuterGates(); // Actual Gates

	public List<Node> getFloatingNodes();

}
