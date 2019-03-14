package com.ericsson.papyrus.devtools.views;

import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.ViewPart;

public abstract class GMFExplorerView extends ViewPart {
	public GMFExplorerView() {
		myEditPartlistener = new ISelectionListener() {
			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {				
				if (!(part instanceof EditorPart) && !(part instanceof GMFExplorerView) && part != GMFExplorerView.this)
					return;
				
				if  (!(selection instanceof IStructuredSelection))
					return;

				Object selectedobject = ((IStructuredSelection) selection).getFirstElement();
				if (selectedobject == null)
					return;
				if (part instanceof GMFExplorerView) {
					selectedobject = ((GMFExplorerView)part).getEditPart(selectedobject);
					if (selectedobject == null) {
						if (editPartViewer != null)
							editPartViewer.setSelection(new StructuredSelection());
						return;
					}
				}
				
				if (selectedobject instanceof GraphicalEditPart) {
					selectEditPart((GraphicalEditPart) selectedobject);
					return;
				}

				selectEditPart(null);
			}
		};
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		selectionService.addSelectionListener(myEditPartlistener);
		commandStackListener = new CommandStackListener() {			
			@Override
			public void commandStackChanged(EventObject event) {
				if (explorerViewer != null)
					explorerViewer.refresh();
			}
		};
	}
	
	protected abstract LabelProvider getExplorerLabelProvider();
	protected abstract IContentProvider getExplorerContentProvider();
	protected abstract ITableLabelProvider getPropertiesLabelProvider();
	protected abstract ITreeContentProvider getPropertiesContentProvider();
	
	protected abstract EditPart getEditPart(Object obj);
	protected abstract Object getInputObject(GraphicalEditPart graphicalEP);
	protected abstract Object getSelectedObject(GraphicalEditPart graphicalEP);
	
	protected TreeViewer getExplorerViewer() {
		return explorerViewer;
	}

	protected TreeViewer getPropertyViewer() {
		return propertyViewer;
	}

	public EditPartViewer getEditPartViewer() {
		return editPartViewer;
	}
	
	private void selectEditPart(GraphicalEditPart graphicalEP) {
		if (graphicalEP != null) {
			editPartViewer = graphicalEP.getViewer();			
			if (graphicalEP.getModel() instanceof EObject && 
				TransactionUtil.getEditingDomain((EObject)graphicalEP.getModel()) != null) {
				EObject model = (EObject)graphicalEP.getModel();
				TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(model);
				CommandStack cs = editingDomain.getCommandStack();
				if (commandStack != cs) {
					if (commandStack != null)
						commandStack.removeCommandStackListener(commandStackListener);
					
					commandStack = cs;
					
					if (commandStack != null)
						commandStack.addCommandStackListener(commandStackListener);
					explorerViewer.refresh();
				}
			} else {
				if (commandStack != null && commandStackListener != null)
					commandStack.removeCommandStackListener(commandStackListener);
				commandStack = null;
			}
			Object sel = getSelectedObject(graphicalEP);
			Object input = getInputObject(graphicalEP);
			
			if (explorerViewer != null) {
				if (!(explorerViewer.getInput() instanceof Object[]) || ((Object[])explorerViewer.getInput())[0] != input) 
					explorerViewer.setInput(new Object[] {input});
				IStructuredSelection curSel = (IStructuredSelection)explorerViewer.getSelection(); 
				if ((curSel.getFirstElement() == null || curSel.getFirstElement() != sel) && sel != null) {
					explorerViewer.reveal(sel);
					explorerViewer.setSelection(new StructuredSelection(sel), true);
				}
			}
			
			if (propertyViewer != null) {
				propertyViewer.setInput(sel);
			}
		} else {
			if (explorerViewer != null) {
				explorerViewer.setInput(null);
			}
			if (propertyViewer != null) {
				propertyViewer.setInput(null);
			}	
			
			if (commandStack != null && commandStackListener != null)
				commandStack.removeCommandStackListener(commandStackListener);
				
			commandStack = null;
			editPartViewer = null;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		Viewer viewer = explorerViewer;
		if (viewer != null && !viewer.getControl().isDisposed()) {
			viewer.getControl().setFocus();
		}
	}

	/**
	 * Create the main tree control
	 *
	 * @param parent
	 * @return Tree
	 */
	protected Tree createExplorerTree(Composite parent) {
		Tree tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		return tree;
	}

	protected Tree createTable(Composite parent) {
		Tree table = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		TreeColumn c = new TreeColumn(table, SWT.LEFT, 0);
		c.setText("Property");
		c.setWidth(150);

		c = new TreeColumn(table, SWT.LEFT, 0);
		c.setText("Value");
		c.setWidth(250);
		
		return table;
	}

	@Override
	public void createPartControl(final org.eclipse.swt.widgets.Composite parent) {
		final IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
		tbm.add(getClearViewAction());
		SashForm sash = new SashForm(parent, SWT.BORDER | SWT.VERTICAL);
		
		explorerViewer = new TreeViewer(createExplorerTree(sash));
		explorerViewer.setContentProvider(getExplorerContentProvider());
		explorerViewer.setLabelProvider(getExplorerLabelProvider());
				
		propertyViewer = new TreeViewer(createTable(sash));
		propertyViewer.setLabelProvider(getPropertiesLabelProvider());
		propertyViewer.setContentProvider(getPropertiesContentProvider());
	
		sash.setWeights(new int[] {50,50});
		
		explorerViewer.addSelectionChangedListener(new ISelectionChangedListener() {			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection)explorerViewer.getSelection();
				Object obj = sel.getFirstElement();
				propertyViewer.setInput(obj);
				EditPart ep = getEditPart(obj);
				if (ep != null) {
					if (ep.isSelectable()) {
						editPartViewer.select(ep);
					}
				}
			}
		});
		getSite().setSelectionProvider(explorerViewer);
	}

	protected Action getClearViewAction() {
		return new Action() {

			@Override
			public void run() {
				clear();
			}

			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public String getToolTipText() {
				return getText();
			}

			@Override
			public String getText() {
				return "Clear the view";
			}

		};
	}

	@Override
	public void dispose() {
		super.dispose();
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		selectionService.removeSelectionListener(myEditPartlistener);
		
		if (commandStack != null && commandStackListener != null) {
			commandStack.removeCommandStackListener(commandStackListener);
		}
		commandStack = null;
	}

	protected void clear() {
		explorerViewer.setInput(null);
		propertyViewer.setInput(null);
	}

	protected String getClassName(Class cls) {
		return PropertyLabelProvider.getClassName(cls);
	}
	
	private ISelectionListener myEditPartlistener;
	private TreeViewer explorerViewer;
	private TreeViewer propertyViewer;
	private EditPartViewer editPartViewer;
	private CommandStack commandStack;
	private CommandStackListener commandStackListener;
}