/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph;

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
}
