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

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.validation.internal.service.GetBatchConstraintsOperation;

/**
 * @author ETXACAM
 *
 */
public interface InteractionNodeLayout {
	public default void layout(NodeImpl node) {
		Rectangle r = node.getBounds();
		if (r != null) {				
			r.x = node.getColumn().getXPosition() - (r.width / 2);
			r.y = node.getRow().getYPosition() - (r.height / 2);			
		}				
		node.setBounds(r);
	};
	
	public default Rectangle getConstraints(NodeImpl node) {
		return node.getBounds().getCopy();
	}

	public default Dimension getMinimumSize(NodeImpl node) {
		return new Dimension(0,0);
	}

}
