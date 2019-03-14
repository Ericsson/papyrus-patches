package com.ericsson.papyrus.devtools.views;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;

public class NotationModelView extends GMFExplorerView {
	public NotationModelView() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see com.ericsson.papyrus.nwa.devtools.views.GMFExplorerView#getEditPart(java.lang.Object)
	 */
	@Override
	protected EditPart getEditPart(Object obj) {
		EditPartViewer viewer = getEditPartViewer();
		if (getEditPartViewer() == null)
			return null;
		
		return (EditPart)viewer.getEditPartRegistry().get(obj);
	}

	/* (non-Javadoc)
	 * @see com.ericsson.papyrus.nwa.devtools.views.GMFExplorerView#getInputObject(org.eclipse.gef.GraphicalEditPart)
	 */
	@Override
	protected Object getInputObject(GraphicalEditPart graphicalEP) {
		Object obj = graphicalEP.getModel();
		while (!(obj instanceof View)) {
			if (!(obj instanceof EObject))
				return null;
			obj = ((EObject)obj).eContainer();
		}
		return ((View)graphicalEP.getModel()).getDiagram();	
	}


	/* (non-Javadoc)
	 * @see com.ericsson.papyrus.nwa.devtools.views.GMFExplorerView#getSelectedObject(org.eclipse.gef.GraphicalEditPart)
	 */
	@Override
	protected Object getSelectedObject(GraphicalEditPart graphicalEP) {
		return graphicalEP.getModel();
	}

	/* (non-Javadoc)
	 * @see com.ericsson.papyrus.nwa.devtools.views.GMFExplorerView#getExplorerLabelProvider()
	 */
	@Override
	protected LabelProvider getExplorerLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				return getClassName(element.getClass()) + "("+((View)element).getType()+")"; 
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
				if (element instanceof View) {
					return ((View) element).eContainer();
				}
				return null;
			}

			@Override
			public Object[] getChildren(final Object parentElement) {
				if (parentElement instanceof View) {
					return ((View) parentElement).eContents().stream().filter(View.class::isInstance).toArray();
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
				if (!(inputElement instanceof View))
					return new Object[0];
				
				View view = (View)inputElement;;

				EditPart ep = null;
				if (getEditPartViewer() != null)
					ep = (EditPart)getEditPartViewer().getEditPartRegistry().get(view);
				
				EObject el = null;
				if (view != null)
					el = view.getElement();
				
				Object[] objs = null;
				if (inputElement instanceof Edge) {
					Edge c = (Edge)inputElement;					
					objs = new Object[][] {
						new Object[] {"Class", getClassName(view.getClass())},
						new Object[] {"EObject", el},
						new Object[] {"EditPart", ep},
						new Object[] {"Figure", ep instanceof IGraphicalEditPart ? ((IGraphicalEditPart)ep).getFigure() : null},
						new Object[] {"Active", view.isVisible()},
						new Object[] {"Persisted", ((View)view.eContainer()).getPersistedChildren().contains(view)},
						new Object[] {"Source", c.getSource()},
						new Object[] {"SourceAnchor", c.getSourceAnchor()},
						new Object[] {"Target", c.getTarget()},
						new Object[] {"TargetAnchor", c.getTargetAnchor()},
						new Object[] {"Bendpoints", c.getBendpoints()},						
					};
				} else if (inputElement instanceof Node){ 
					Node c = (Node)inputElement;					
					objs = new Object[][] {
						new Object[] {"Class", getClassName(view.getClass())},
						new Object[] {"EObject", el},
						new Object[] {"EditPart", ep},
						new Object[] {"Figure", ep instanceof IGraphicalEditPart ? ((IGraphicalEditPart)ep).getFigure() : null},
						new Object[] {"Active", view.isVisible()},
						new Object[] {"Persisted", ((View)view.eContainer()).getPersistedChildren().contains(view)},
						new Object[] {"LayoutConstraints", c.getLayoutConstraint()},
					};
				} else {
					objs = new Object[][] {
						new Object[] {"Class", getClassName(view.getClass())},
						new Object[] {"EObject", el},
						new Object[] {"EditPart", ep},
						new Object[] {"Figure", ep instanceof IGraphicalEditPart ? ((IGraphicalEditPart)ep).getFigure() : null},
						new Object[] {"Active", view.isVisible()},
						new Object[] {"Persisted", view.eContainer() != null ? ((View)view.eContainer()).getPersistedChildren().contains(view) : null},
					};
				}
				
				return objs;
			}
		};
	}
	
}