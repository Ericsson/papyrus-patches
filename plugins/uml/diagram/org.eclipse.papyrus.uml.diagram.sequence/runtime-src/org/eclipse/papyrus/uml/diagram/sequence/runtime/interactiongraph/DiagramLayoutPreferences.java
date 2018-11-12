/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph;

import org.eclipse.draw2d.geometry.Insets;

public interface DiagramLayoutPreferences {
	
	public Insets getInteractionInsets();
	
	public int getLifelineDefaultHeight();
	public Insets getLifelineHeaderInsets();
	public Insets getLifelinePadding();
	
	public Insets getMessagePadding();	
	public Insets getInteractionUsePadding();

	public int getExecutionSpecificationWidth();
	public int getExecutionSpecificationOffset();
	public Insets getExecutionSpecificationPadding();	
}
