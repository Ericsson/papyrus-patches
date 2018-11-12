package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph;

import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.MarkNode;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;

public class MarkNodeImpl extends NodeImpl implements MarkNode {

	public MarkNodeImpl(Kind kind) {
		super(null);
		this.kind = kind;
	}

	@Override
	public Kind getKind() {
		return kind;
	}
	
	public String toString() {
		Element el = getParent().getElement(); 
		return String.format("Mark[%s][%s]",
				kind.name(),
				el instanceof NamedElement ? ((NamedElement)el).getName() : "A " + el.eClass().getName());
	}

	private Kind kind;
}
