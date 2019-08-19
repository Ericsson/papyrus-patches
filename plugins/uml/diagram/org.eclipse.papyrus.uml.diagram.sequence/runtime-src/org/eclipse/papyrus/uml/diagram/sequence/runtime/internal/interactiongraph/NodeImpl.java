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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Link;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.NamedElement;


public class NodeImpl extends GraphItemImpl implements Node {
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
		return n == null ? null : (InteractionGraphImpl) n.parent;
	}

	@Override
	public ClusterImpl getParent() {
		if (parent == this) {			
			// Avoid infinity loops if something went really wrong.
			// We crash instead. hopefully transaction is aborted.
			throw new IllegalStateException("he node it is own parent.");
		}
		return parent;
	}

	@Override
	public NodeImpl getConnectedNode() {
		if (oppositeNode == null) {
			return null;
		}

		if (oppositeNode == this) {			
			// Avoid infinity loops if something went really wrong.
			// We crash instead. hopefully transaction is aborted.
			throw new IllegalStateException("The node it is linked to itself.");
		}
		return (connects ? oppositeNode : null);
	}

	@Override
	public NodeImpl getConnectedByNode() {
		if (oppositeNode == null) {
			return null;
		}

		if (oppositeNode == this) {			
			// Avoid infinity loops if something went really wrong.
			// We crash instead. hopefully transaction is aborted.
			throw new IllegalStateException("The node it is linked to itself.");
		}

		return (!connects ? oppositeNode : null);
	}
	@Override
	public LinkImpl getConnectedByLink() {
		return connectingLink;
	}

	void connectNode(NodeImpl connectedNode, Link link) {
		disconnectNode();
		connectedNode.disconnectNode();
		this.oppositeNode = connectedNode;
		this.connects = true;
		connectedNode.oppositeNode = this;
		connectedNode.connects = false;
		this.connectingLink = (LinkImpl)link;
		connectedNode.connectingLink= (LinkImpl)link;
	}

	void disconnectNode() {
		if (this.oppositeNode != null) {
			this.oppositeNode.oppositeNode = null;
			this.oppositeNode.connects = false;
			this.oppositeNode = null;
			this.connects = false;
			this.connectingLink = null;
		}
	}

	@Override
	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
	}

	@Override
	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
		if (this.view == null) {
			setEditPart(null);
		} else {
			EditPartViewer viewer = getInteractionGraph().getEditPartViewer();
			if (viewer != null) {
				setEditPart((GraphicalEditPart) viewer.getEditPartRegistry().get(view));
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

	EditPartViewer getViewer() {
		InteractionGraphImpl graph = getInteractionGraph();
		return graph.getEditPartViewer();
	}

	Rectangle extractBounds() {
		Rectangle r = ViewUtilities.getBounds(getViewer(), view);
		if (r == null) {
			return null;
		}
		r = r.getCopy();
		if (!(this instanceof Cluster)) {
			if (r.width != 1 && r.height != 1) {
				r.x = r.getCenter().x;
				r.y = r.getCenter().y;
			}
			r.width = 0;
			r.height = 0;
		} else {
			if (r.width == 1 && r.height == 1) {
				r.width = 0;
				r.height = 0;				
			}
		}
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

	public void setBounds(Rectangle bounds) {
		if (bounds.equals(this.bounds)) {
			return;
		}
		
		this.bounds = bounds;
	}

	@Override
	public Point getLocation() {
		return getBounds().getTopLeft();
	}

	@Override
	public Dimension getSize() {
		return getBounds().getSize();
	}

	@Override
	public Rectangle getConstraints() {
		return getInteractionGraph().getLayoutManager().getConstraints(this);
	}
	
	@Override
	public String toString() {
		if (element == null) {
			return "Node[--]";
		}
		return String.format("Node[%s - %s Col:%s Row:%s]",
				getPrintableString(element),
				bounds == null ? "--" : bounds.toString(),
				column == null ? "-" : ("[" + column.getIndex() +"]("+column.getXPosition()+")"),
				row == null ? "-" : ("[" + row.getIndex() +"]("+row.getYPosition()+")"));
	}

	private String getPrintableString(Element element) {
		if (element instanceof MessageOccurrenceSpecification) {
			MessageOccurrenceSpecification mos = (MessageOccurrenceSpecification)element;
			Message msg = mos.getMessage();
			if (msg != null)
				return String.format("%s - %s", msg.getSendEvent() == mos ? "Send" : "Recieve", msg.getName());
		} else if (element instanceof ExecutionOccurrenceSpecification) {
			ExecutionOccurrenceSpecification eos = (ExecutionOccurrenceSpecification)element;			
			ExecutionSpecification exSpec = eos.getExecution();			
			return String.format("%s - %s", exSpec.getStart() == eos ? "Start" : "Finish", exSpec.getName());
		} else if (element instanceof NamedElement) {
			return ((NamedElement) element).getName();
		}
		return "--";
	}
	
	private ClusterImpl parent;
	private Element element;

	private boolean connects;
	protected NodeImpl oppositeNode;
	protected LinkImpl connectingLink;  
	
	protected View view;
	protected GraphicalEditPart editPart;
	protected RowImpl row;
	protected ColumnImpl column;
	protected Rectangle bounds;
}
