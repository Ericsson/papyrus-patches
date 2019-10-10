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

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Link;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.NamedElement;

/**
 * @author ETXACAM
 *
 */
public class LinkImpl extends GraphItemImpl implements Link {
	public static final String SYNCH_TYPE_PROPERTY = "Synch_Type";
	public static final String SYNCH_TYPE_ACTION = "action";
	public static final String SYNCH_TYPE_BEHAVIOR = "behavior";
	
	public LinkImpl(Element element) {
		this.element = (Message)element;
	}

	@Override
	public InteractionGraphImpl getInteractionGraph() {
		return graph;
	}

	void setInteractionGraph(InteractionGraphImpl graph) {
		this.graph = graph;
	}

	public Message getElement() {
		return element;
	}

	void setElement(Element element) {
		this.element = (Message)element;
	}
	
	public NodeImpl getSource() {
		return source;
	}
	
	void setSource(NodeImpl source) {
		this.source = source;
	}
	
	public Point getSourceLocation() {
		return source.getBounds().getCenter();
	}

	public Point getTargetLocation() {
		return target.getBounds().getCenter();
	}

	public Rectangle getBounds() {
		Rectangle r = new Rectangle(getSourceLocation(), new Dimension(0,0));
		r = Draw2dUtils.union(r.getCopy(),getTargetLocation());
		r.width = Math.max(1,r.width);
		r.height = Math.max(1,r.height);			
		return r;
	}
	
	public NodeImpl getTarget() {
		return target;
	}
	
	void setTarget(NodeImpl target) {
		this.target = target;
	}
	
	@Override
	public Edge getView() {
		return getEdge();
	}

	public void setView(View view) {
		setEdge((Edge)view);
	}

	public NodeImpl getSourceAnchoringNode() {
		NodeImpl src = getSource();
		if (src == null)
			return null;
		
		if (src.getView() != null)
			return src;
		
		if (src.getElement() instanceof Gate) {
			return src;
		}

		return src.getParent();
	}
	
	public NodeImpl getTargetAnchoringNode() {
		NodeImpl trg = getTarget();
		if (trg == null)
			return null;
		
		if (trg.getView() != null)
			return trg;
		
		if (trg.getElement() instanceof DestructionOccurrenceSpecification) {
			return trg;
		}
		
		if (trg.getElement() instanceof Gate) {
			return trg;
		}
		
		return trg.getParent();
	}

	public Edge getEdge() {
		return edge;
	}
	
	void setEdge(Edge edge) {
		this.edge = edge;
		if (this.edge == null) {
			setEditPart(null);
		} else {
			EditPartViewer viewer = getInteractionGraph().getEditPartViewer();
			if (viewer != null) {
				setEditPart((GraphicalEditPart) viewer.getEditPartRegistry().get(edge));
			}
		}

	}

	@Override
	public GraphicalEditPart getEditPart() {
		return editPart;
	}

	void setEditPart(GraphicalEditPart editPart) {
		this.editPart = editPart;
	}

	public String toString() {
		if (getElement() == null)
			return "Link[--]";
		return String.format("Link[%s]",
				getPrintableString(getElement()));

	}

	private String getPrintableString(Element element) {
		return getElement() instanceof NamedElement ? ((NamedElement)getElement()).getName() : "A " + getElement().eClass().getName();
	}
	
	private InteractionGraphImpl graph;
	private  Message element;
	protected NodeImpl source;
	protected NodeImpl target;
	protected Edge edge;
	protected GraphicalEditPart editPart;
}
