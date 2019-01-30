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

package org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.NodeImpl;

/**
 * @author ETXACAM
 *
 */
public interface Link extends GraphItem {
	Node getSource();
	Node getSourceAnchoringNode();
	Point getSourceLocation();
	
	Node getTarget();
	Node getTargetAnchoringNode();
	Point getTargetLocation();

	Edge getEdge();
	Rectangle getBounds();
}
