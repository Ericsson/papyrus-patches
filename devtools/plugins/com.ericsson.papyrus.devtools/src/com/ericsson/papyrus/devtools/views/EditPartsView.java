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
package com.ericsson.papyrus.devtools.views;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;

public class EditPartsView extends GMFExplorerView {
	public EditPartsView() {
		super();
	}

	@Override
	protected EditPart getEditPart(Object obj) {
		return (EditPart)obj;
	}

	/* (non-Javadoc)
	 * @see com.ericsson.papyrus.nwa.devtools.views.GMFExplorerView#getInputObject(org.eclipse.gef.GraphicalEditPart)
	 */
	@Override
	protected Object getInputObject(GraphicalEditPart graphicalEP) {
		return graphicalEP.getRoot();
			
	}


	/* (non-Javadoc)
	 * @see com.ericsson.papyrus.nwa.devtools.views.GMFExplorerView#getSelectedObject(org.eclipse.gef.GraphicalEditPart)
	 */
	@Override
	protected Object getSelectedObject(GraphicalEditPart graphicalEP) {
		return graphicalEP;
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
				if (element instanceof EditPart) {
					return ((EditPart) element).getParent();
				}
				return null;
			}

			@Override
			public Object[] getChildren(final Object parentElement) {
				if (parentElement instanceof EditPart) {
					return ((EditPart) parentElement).getChildren().toArray();
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
				if (!(inputElement instanceof EditPart))
					return new Object[0];
				
				EditPart ep = (EditPart)inputElement;
				View view = null;
				if (ep != null && ep.getModel() instanceof View)
					view = (View)ep.getModel();
				
				EObject el = null;
				if (view != null)
					el = view.getElement();
				
				Object[] objs = null;
				if (inputElement instanceof ConnectionEditPart) {
					ConnectionEditPart c = (ConnectionEditPart)inputElement;					
					objs = new Object[][] {
						new Object[] {"Class", getClassName(c.getClass())},
						new Object[] {"EObject", el},
						new Object[] {"View", view},
						new Object[] {"Figure", c.getFigure()},
						new Object[] {"Content Pane", c.getContentPane()},
						new Object[] {"Active", c.isActive()},
						new Object[] {"Selectable", c.isSelectable()},
						new Object[] {"Source", c.getSource()},
						new Object[] {"Target", c.getTarget()},
					};
				} else if (inputElement instanceof IGraphicalEditPart){ 
					IGraphicalEditPart c = (IGraphicalEditPart)inputElement;					
					objs = new Object[][] {
						new Object[] {"Class", getClassName(ep.getClass())},
						new Object[] {"EObject", el},
						new Object[] {"View", view},
						new Object[] {"Figure", c.getFigure()},
						new Object[] {"Content Pane", c.getContentPane()},
						new Object[] {"Active", ep.isActive()},
						new Object[] {"Selectable", ep.isSelectable()},
					};
				} else {
					objs = new Object[][] {
						new Object[] {"Class", getClassName(ep.getClass())},
						new Object[] {"EObject", el},
						new Object[] {"View", view},
						new Object[] {"Active", ep.isActive()},
						new Object[] {"Selectable", ep.isSelectable()},
					};
				}
				
				return objs;
			}
		};
	}
	
}