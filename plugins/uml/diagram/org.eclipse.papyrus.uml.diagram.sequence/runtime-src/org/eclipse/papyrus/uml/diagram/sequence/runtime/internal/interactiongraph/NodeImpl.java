/*****************************************************************************
 * (c) Copyright 2018 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) antonio.campesino.robles@ericsson.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;


public class NodeImpl implements Node {
	public NodeImpl(Element element) {
		this.element = element;
	}
		
	void setParent(ClusterImpl cluster) {
		this.parent = cluster;
	}
	
	@Override
	public InteractionGraphImpl getInteractionGraph() {
		NodeImpl n = this;
		while (n != null && !(n.parent instanceof InteractionGraph)) {
			n = n.parent;
		}
		return (InteractionGraphImpl)n.parent;
	}
	
	@Override
	public ClusterImpl getParent() {
		if (parent instanceof InteractionGraph) {
			return null;
		}
		return parent;
	}
	
	@Override
	public NodeImpl getConnectedNode() {
		if (oppositeNode == null) {
			return null;
		}
		
		return (connects ? oppositeNode : null);
	}

	@Override
	public NodeImpl getConnectedByNode() {
		if (oppositeNode == null) {
			return null;
		}
		
		return (!connects ? oppositeNode : null);
	}
	
	void connectNode(NodeImpl connectedNode) {
		disconnectNode();
		connectedNode.disconnectNode();
		this.oppositeNode = connectedNode;
		this.connects = true;
		connectedNode.oppositeNode = this;
		connectedNode.connects = false; 
	}
	
	void disconnectNode() {
		if (this.oppositeNode != null) {
			this.oppositeNode.oppositeNode = null;
			this.oppositeNode.connects = false;
			this.oppositeNode = null;
			this.connects = false;
		}		
	}
	
	@Override
	public Element getElement() {
		return element;
	}

	@Override
	public View getView() {
		return view;
	}

	void setView(View view) {
		this.view = view;
		if (this.view == null) {
			setEditPart(null);
		} else {
			EditPartViewer viewer = getInteractionGraph().getEditPartViewer(); 
			if (viewer != null) {
				setEditPart((GraphicalEditPart)viewer.getEditPartRegistry().get(view));
			}
		}
	}
	
	@Override
	public GraphicalEditPart getEditPart() {
	    return editPart;
	}

	void setEditPart(GraphicalEditPart editPart) {
	    this.editPart = editPart;
	    bounds = extractBounds();
	}
	
	Rectangle extractBounds() {
		Rectangle r = ViewUtilities.getBounds(editPart != null ? editPart.getViewer() : null , view);
		if ( r == null)
			return null;
				
		return r.getCopy();
	}

	@Override
	public RowImpl getRow() {
		return row;
	}
	
	void setRow(RowImpl row) {
		this.row = row;
	}

	@Override
	public ColumnImpl getColumn() {
		return column;
	}
	
	void setColumn(ColumnImpl column) {
		this.column = column;
	}

	public Rectangle getBounds() {
		return bounds;
	}
	
	void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}
	
	public String toString() {
		if (element == null)
			return "Node[--]";
		return String.format("Node[%s]", 
				element instanceof NamedElement ? ((NamedElement)element).getName() : "A " + element.eClass().getName());
	}

	private ClusterImpl parent;
	private Element element;
	
	private boolean connects;
	protected NodeImpl oppositeNode;
	
	protected View view;
	protected GraphicalEditPart editPart;
	protected RowImpl row;
	protected ColumnImpl column;
	protected Rectangle bounds;
}
