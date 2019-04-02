/*****************************************************************************
 * (c) Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph;

import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.InteractionUse;

/**
 * A {@link MarkNode} represent a graphical mark needed to represent an event that it is not explicitly
 * defined in the semantical model, but it is inferred from it and it helps to position the associated
 * fragments in the diagram.<br/>
 * Examples of a {@link MarkNode} is the lifeine's creation mark, the start and end marks for
 * {@link InteractionUse}, {@link CombinedFragment} and {@link InteractionOperand}. Lifeline's destruction
 * marks can be directly represented with a single {@link Node} associated with the
 * {@link DestructionOccurrenceSpecification}.
 */
public interface MarkNode extends Node {
	/**
	 * Enumeration type that defines the type of the mark.
	 */
	enum Kind {
		/**
		 * A Lifeline creation mark.
		 */
		creation,
		/**
		 * A fragment start mark.
		 */
		start,
		/**
		 * A fragment end mark.
		 */
		end,
		/**
		 * A mark to represent the send event associated to a found message.
		 */
		foundMessageEnd,
		/**
		 * A mark to represent the send event associated to a lost message.
		 */
		lostMessageEnd
	}

	/**
	 * Return the type of the mark.
	 * 
	 * @return the {@link Kind} of the mark.
	 */
	public Kind getKind();
}
