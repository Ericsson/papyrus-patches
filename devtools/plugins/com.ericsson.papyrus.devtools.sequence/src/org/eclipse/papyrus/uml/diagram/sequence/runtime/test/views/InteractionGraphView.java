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
 *  Antonio Campesino (Ericsson) antonio.campesino.robles@ericsson.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.runtime.test.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Cluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.FragmentCluster;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphFactory;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Link;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.test.debug.DebugDiagramRootEditPart;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interaction;

import com.ericsson.papyrus.devtools.views.GMFExplorerView;
import com.ericsson.papyrus.devtools.views.PropertyContentProvider;
import com.ericsson.papyrus.devtools.views.PropertyLabelProvider;

public class InteractionGraphView extends GMFExplorerView {
	private InteractionGraph graph; 
	
	@Override
	protected void clear() {
		super.clear();
		graph = null;
	}

	public InteractionGraphView() {
		super();
	}
	
	@Override
	protected EditPart getEditPart(Object obj) {
		EditPartViewer viewer = getEditPartViewer();
		if (getEditPartViewer() == null)
			return null;
		
		if (obj instanceof Node) {
			EditPart ep = ((Node) obj).getEditPart(); 
			/*while (obj != null && ep == null) {
				obj = ((Node) obj).getParent();
				if (obj != null)
					ep = ((Node) obj).getEditPart();
			}*/
			return ep;
		} else if (obj instanceof Link) {
			return ((Link) obj).getEditPart();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.ericsson.papyrus.nwa.devtools.views.GMFExplorerView#getInputObject(org.eclipse.gef.GraphicalEditPart)
	 */
	@Override
	protected Object getInputObject(GraphicalEditPart graphicalEP) {
		EditPart editPart = graphicalEP;
		if (graph == null) {
			while (editPart != null && !(editPart instanceof DiagramEditPart)) {
				editPart = editPart.getParent();
			}
			if (!(editPart instanceof DiagramEditPart))
				return null;
			
			Diagram dia = ((DiagramEditPart)editPart).getDiagramView();
			if (dia == null || !(dia.getElement() instanceof Interaction))
				return null;
			graph = InteractionGraphFactory.getInstance().createInteractionGraph((Interaction)dia.getElement(), dia, graphicalEP.getViewer());
		}
		return graph; 			
	}

	/* (non-Javadoc)
	 * @see com.ericsson.papyrus.nwa.devtools.views.GMFExplorerView#getSelectedObject(org.eclipse.gef.GraphicalEditPart)
	 */
	@Override
	protected Object getSelectedObject(GraphicalEditPart graphicalEP) {
		View v = (View)graphicalEP.getModel();
		Object curSel = super.getExplorerViewer().getStructuredSelection().getFirstElement();
		if (curSel instanceof Node && ((Node) curSel).getView() == v)
			return curSel;
		
		if (graph == null)
			return null;
		if (!(v.getElement() instanceof Element))
			return null;
		Element el = (Element)v.getElement();
		Object obj = graph.getNodeFor(el);
		if (obj == null)
			obj = graph.getLinkFor(el);
		if (obj == null)
			obj = graph.getClusterFor(el);
		if (obj == null && v instanceof Diagram)
			return graph;
		return obj;
	}


	/* (non-Javadoc)
	 * @see com.ericsson.papyrus.nwa.devtools.views.GMFExplorerView#getExplorerLabelProvider()
	 */
	@Override
	protected LabelProvider getExplorerLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				return getClassName(element.getClass()) + " " + element.toString(); 
			}
			
		};
	}


	/* (non-Javadoc)
	 * @see com.ericsson.papyrus.nwa.devtools.views.GMFExplorerView#getExplorerContentProvider()
	 */
	@Override
	protected IContentProvider getExplorerContentProvider() {
		return new ITreeContentProvider() {

			@Override
			public Object[] getElements(final Object inputElement) {
				if (inputElement instanceof Object[]) {
					return (Object[]) inputElement;
				} 
				return null;
			}

			@Override
			public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
				// nothing
			}

			@Override
			public void dispose() {
				// nothing
			}

			@Override
			public boolean hasChildren(final Object element) {
				return getChildren(element).length != 0;
			}

			@Override
			public Object getParent(final Object element) {
				if (element instanceof InteractionGraph) {
					return null;
				}
				if (element instanceof FragmentCluster) {
					return ((FragmentCluster)element).getInteractionGraph();					
				} else if (element instanceof Node) {
					Node n = (Node)element;
					Node parent = n.getParent();
					if (parent == null && parent != n.getInteractionGraph())
						parent = (Node)n.getInteractionGraph();
					return parent;
				} else if (element instanceof Link) {
					return ((Link)element).getInteractionGraph();
				}
				return null;
			}

			@Override
			public Object[] getChildren(final Object parentElement) {
				List<Object> objs = new ArrayList<Object>();
				if (parentElement instanceof InteractionGraph) {
					objs.addAll(((InteractionGraph) parentElement).getLifelineClusters());
					objs.addAll(((InteractionGraph) parentElement).getMessageLinks());
				}
				
				if (parentElement instanceof FragmentCluster) {
					objs.addAll(((FragmentCluster) parentElement).getOwnedFragmentClusters());
					objs.addAll(((FragmentCluster) parentElement).getAllGates());
					objs.addAll(((FragmentCluster) parentElement).getFloatingNodes());
				} else if (parentElement instanceof Cluster) {
					return ((Cluster) parentElement).getNodes().toArray();
				}
				return objs.toArray();
			}
		};
	}


	/* (non-Javadoc)
	 * @see com.ericsson.papyrus.nwa.devtools.views.GMFExplorerView#getPropertiesLabelProvider()
	 */
	@Override
	protected ITableLabelProvider getPropertiesLabelProvider() {
		return new PropertyLabelProvider() {
			public String getColumnText(Object element, int columnIndex) {
				if (columnIndex == 1 && ((Object[])element)[1] instanceof Node) {
					Node n = (Node)((Object[])element)[1];
					return super.getColumnText(element, columnIndex) + " - " + n.toString();
				}
				return super.getColumnText(element, columnIndex);
			}
		};
	}

	protected ITreeContentProvider getPropertiesContentProvider() {
		return new PropertyContentProvider() {
			@Override
			public Object[] getRootProperties(Object inputElement) {
				return new Object[0];
				/*if (!(inputElement instanceof IFigure))
					return new Object[0];
				
				IFigure f = (IFigure)inputElement;
				EditPart ep = (EditPart)getEditPartViewer().getVisualPartMap().get(inputElement);
				View view = null;
				if (ep != null && ep.getModel() instanceof View)
					view = (View)ep.getModel();
				
				EObject el = null;
				if (view != null)
					el = view.getElement();
				Diagram diag = view.getDiagram();
				GraphicalEditPart diaEp = (GraphicalEditPart)getEditPartViewer().getEditPartRegistry().get(diag);
				IFigure diaFig = diaEp.getFigure();
				Object[] objs = null;				
				if (inputElement instanceof Connection) {
					Connection c = (Connection)inputElement;					
					Point absSrcAnchor = c.getSourceAnchor().getLocation(c.getSourceAnchor().getReferencePoint()).getCopy();
					diaFig.translateToRelative(absSrcAnchor);
					Point absTrgAnchor = c.getTargetAnchor().getLocation(c.getTargetAnchor().getReferencePoint()).getCopy();
					diaFig.translateToRelative(absTrgAnchor);
					objs = new Object[][] {
						new Object[] {"Class", getClassName(c.getClass())},
						new Object[] {"EObject", el},
						new Object[] {"View", view},
						new Object[] {"Visible", f.isVisible()},
						new Object[] {"Source Anchor", c.getSourceAnchor()},
						new Object[] {"Source Anchor Loc (Abs.)", absSrcAnchor},
						new Object[] {"Target Anchor", c.getTargetAnchor()},
						new Object[] {"Target Anchor Loc (Abs.)", absSrcAnchor},
						new Object[] {"Routing Constraint", c.getRoutingConstraint()},
					};
				} else { 
					Rectangle absBounds = f.getBounds().getCopy();
					f.translateToAbsolute(absBounds);
					diaFig.translateToRelative(absBounds);
					objs = new Object[][] {
						new Object[] {"Class", getClassName(f.getClass())},
						new Object[] {"EObject", el},
						new Object[] {"View", view},
						new Object[] {"Visible", f.isVisible()},
						new Object[] {"Coord Syst", f.isCoordinateSystem()},
						new Object[] {"Bounds", f.getBounds()},
						new Object[] {"Bounds (abs.)", absBounds},
						new Object[] {"Client Area", f.getClientArea()},
						new Object[] {"Border", f.getBorder()},
					};
				}
				
				return objs;*/
			}
		};
	}
}