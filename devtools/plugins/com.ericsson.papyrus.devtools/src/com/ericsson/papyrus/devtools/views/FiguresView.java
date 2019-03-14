package com.ericsson.papyrus.devtools.views;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.GraphicalRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;

public class FiguresView extends GMFExplorerView {
	public FiguresView() {
		super();
	}
	
	@Override
	protected EditPart getEditPart(Object obj) {
		EditPartViewer viewer = getEditPartViewer();
		if (getEditPartViewer() == null)
			return null;
		
		return (EditPart)viewer.getVisualPartMap().get(obj);
	}
	
	/* (non-Javadoc)
	 * @see com.ericsson.papyrus.nwa.devtools.views.GMFExplorerView#getInputObject(org.eclipse.gef.GraphicalEditPart)
	 */
	@Override
	protected Object getInputObject(GraphicalEditPart graphicalEP) {
		IFigure root = graphicalEP.getFigure();
		while (root != null && root.getParent() != null)
			root = root.getParent();
		return root;	
	}


	/* (non-Javadoc)
	 * @see com.ericsson.papyrus.nwa.devtools.views.GMFExplorerView#getSelectedObject(org.eclipse.gef.GraphicalEditPart)
	 */
	@Override
	protected Object getSelectedObject(GraphicalEditPart graphicalEP) {
		return graphicalEP.getFigure();
	}


	/* (non-Javadoc)
	 * @see com.ericsson.papyrus.nwa.devtools.views.GMFExplorerView#getExplorerLabelProvider()
	 */
	@Override
	protected LabelProvider getExplorerLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				return getClassName(element.getClass()); 
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
				if (element instanceof IFigure) {
					return ((IFigure) element).getParent();
				}
				return null;
			}

			@Override
			public Object[] getChildren(final Object parentElement) {
				if (parentElement instanceof IFigure) {
					return ((IFigure) parentElement).getChildren().toArray();
				}
				return new Object[0];
			}
		};
	}


	/* (non-Javadoc)
	 * @see com.ericsson.papyrus.nwa.devtools.views.GMFExplorerView#getPropertiesLabelProvider()
	 */
	@Override
	protected ITableLabelProvider getPropertiesLabelProvider() {
		return new PropertyLabelProvider();
	}

	protected ITreeContentProvider getPropertiesContentProvider() {
		return new PropertyContentProvider() {
			@Override
			public Object[] getRootProperties(Object inputElement) {
				if (!(inputElement instanceof IFigure))
					return new Object[0];
				
				IFigure f = (IFigure)inputElement;
				EditPart ep = (EditPart)getEditPartViewer().getVisualPartMap().get(inputElement);
				IFigure epFig = f;
				while (epFig != null && ep == null) {
					epFig = epFig.getParent();
					ep = (EditPart)getEditPartViewer().getVisualPartMap().get(epFig);
				}
					
				View view = null;
				if (ep != null && ep.getModel() instanceof View)
					view = (View)ep.getModel();
				
				IFigure diaFig = ((DiagramRootEditPart)getEditPartViewer().getRootEditPart()).getFigure();
				EObject el = null;
				if (view != null) {
					el = view.getElement();
					Diagram diag = view.getDiagram();
					GraphicalEditPart diaEp = (GraphicalEditPart)getEditPartViewer().getEditPartRegistry().get(diag);
					diaFig = diaEp.getFigure();
				}
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
						new Object[] {"Target Anchor Loc (Abs.)", absTrgAnchor},
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
				
				return objs;
			}
		};
	}
}