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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * @author ETXACAM
 *
 */
public interface InteractionGraphDiff {
	public static enum Type {
		CREATE,
		DELETE,
		CHANGE_FEATURE
	}
	
	public Type getType();
	public EObject get();
	public EObject getParent();
	public EStructuralFeature getFeature();
	public Object getNewValue();
	public Object getOldValue();
}
