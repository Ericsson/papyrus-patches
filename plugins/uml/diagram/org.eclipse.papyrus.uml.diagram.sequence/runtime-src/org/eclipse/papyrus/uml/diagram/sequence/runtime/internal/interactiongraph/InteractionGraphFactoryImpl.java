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

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphFactory;
import org.eclipse.uml2.uml.Interaction;
 
public class InteractionGraphFactoryImpl implements InteractionGraphFactory {
	public static final InteractionGraphFactory INSTANCE = new InteractionGraphFactoryImpl(); 

	@Override
	public InteractionGraph createInteractionGraph(Interaction interaction, Diagram diagram, EditPartViewer viewer) {
		InteractionGraphBuilder builder = new InteractionGraphBuilder(interaction, diagram, viewer);
		builder.build();
		return builder.graph;
	}
}
