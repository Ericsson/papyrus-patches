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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphDiff;

/**
 * @author ETXACAM
 *
 */
public class InteractionGraphDiffImpl implements InteractionGraphDiff {
	public static InteractionGraphDiff change(EObject obj, EStructuralFeature feature, Object oldVal, Object newVal) {
		InteractionGraphDiffImpl idiff = new InteractionGraphDiffImpl();
		idiff.type = Type.CHANGE_FEATURE;
		idiff.obj = obj;
		idiff.feature = feature;
		idiff.oldValue = oldVal;
		idiff.newValue = newVal;		
		return idiff;
	}
	
	public static InteractionGraphDiff create(EObject obj, EStructuralFeature feature, EObject parent) {
		InteractionGraphDiffImpl idiff = new InteractionGraphDiffImpl();
		idiff.type = Type.CREATE;
		idiff.obj = obj;
		idiff.parent = parent;
		idiff.feature = feature;
		return idiff;				
	}

	public static InteractionGraphDiff delete(EObject obj, EStructuralFeature feature, EObject parent) {
		InteractionGraphDiffImpl idiff = new InteractionGraphDiffImpl();
		idiff.type = Type.DELETE;
		idiff.obj = obj;
		idiff.parent = parent;
		idiff.feature = feature;
		return idiff;
	}

	private InteractionGraphDiffImpl() {
		
	}
	
	public Type getType() {
		return type;
	}
	
	public EObject get() {
		return obj;
	}
	
	public EObject getParent() {
		return parent;
	}
	
	public EStructuralFeature getFeature() {
		return feature;
	}
	
	public Object getNewValue() {
		return newValue;
	}
	
	public Object getOldValue() {
		return oldValue;
	}
	
	private Type type;
	private EObject obj;
	private EObject parent;
	private EStructuralFeature feature;
	private Object newValue;
	private Object oldValue;

}
