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

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.MarkNode;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;

public class MarkNodeImpl extends NodeImpl implements MarkNode {

	public MarkNodeImpl(Kind kind) {
		super(null);
		this.kind = kind;
	}

	public MarkNodeImpl(Kind kind, Element element) {
		super(element);
		this.kind = kind;
	}
	
	@Override
	public Kind getKind() {
		return kind;
	}
	
	@Override
	public Rectangle getBounds() {
		// TODO Auto-generated method stub
		return super.getBounds();
	}

	public String toString() {
		Element el = getParent().getElement(); 
		return String.format("Mark[%s][%s]",
				kind.name(),
				el instanceof NamedElement ? ((NamedElement)el).getName() : "A " + el.eClass().getName());
	}

	private Kind kind;
}
