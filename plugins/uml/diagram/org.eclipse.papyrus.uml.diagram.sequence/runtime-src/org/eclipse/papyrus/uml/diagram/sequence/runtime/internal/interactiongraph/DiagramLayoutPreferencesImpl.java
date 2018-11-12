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

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.DiagramLayoutPreferences;

public class DiagramLayoutPreferencesImpl implements DiagramLayoutPreferences {
	
	public Insets getInteractionInsets() {
		return interactionInsets;
	}

	public void setInteractionInsets(Insets interactionInsets) {
		this.interactionInsets = interactionInsets;
	}

	public int getLifelineDefaultHeight() {
		return lifelineDefaultHeight;
	}

	public void setLifelineDefaultHeight(int lifelineDefaultHeight) {
		this.lifelineDefaultHeight = lifelineDefaultHeight;
	}

	public Insets getLifelineHeaderInsets() {
		return lifelineHeaderInsets;
	}

	public void setLifelineHeaderInsets(Insets lifelineHeaderInsets) {
		this.lifelineHeaderInsets = lifelineHeaderInsets;
	}

	public Insets getLifelinePadding() {
		return lifelinePadding;
	}

	public void setLifelinePadding(Insets lifelinePadding) {
		this.lifelinePadding = lifelinePadding;
	}

	public Insets getMessagePadding() {
		return messagePadding;
	}

	public void setMessagePadding(Insets messagePadding) {
		this.messagePadding = messagePadding;
	}

	public Insets getInteractionUsePadding() {
		return interactionUsePadding;
	}

	public void setInteractionUsePadding(Insets interactionUsePadding) {
		this.interactionUsePadding = interactionUsePadding;
	}

	public int getExecutionSpecificationWidth() {
		return executionSpecificationWidth;
	}

	public void setExecutionSpecificationWidth(int executionSpecificationWidth) {
		this.executionSpecificationWidth = executionSpecificationWidth;
	}

	public int getExecutionSpecificationOffset() {
		return executionSpecificationOffset;
	}

	public void setExecutionSpecificationOffset(int executionSpecificationOffset) {
		this.executionSpecificationOffset = executionSpecificationOffset;
	}

	public Insets getExecutionSpecificationPadding() {
		return executionSpecificationPadding;
	}

	public void setExecutionSpecificationPadding(Insets executionSpecificationPadding) {
		this.executionSpecificationPadding = executionSpecificationPadding;
	}
	
	private Insets interactionInsets = new Insets(10,10,10,10);
	private int lifelineDefaultHeight = 30;
	private Insets lifelineHeaderInsets = new Insets(5,5,5,5); 
	private Insets lifelinePadding = new Insets(0,10,0,10);
	
	private Insets messagePadding = new Insets(10,0,10,0);	
	private Insets interactionUsePadding = new Insets(10,0,10,0);

	private int executionSpecificationWidth = 15;
	private int executionSpecificationOffset = 5;
	private Insets executionSpecificationPadding = new Insets(10,0,10,0);	
}
