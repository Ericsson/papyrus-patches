package com.ericsson.papyrus.devtools.views;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.gef.Request;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.part.ViewPart;

import com.ericsson.papyrus.devtools.views.RequestsLogService.RequestsLogListener;

public class RequestsView extends ViewPart {
	public RequestsView() {
		reqListener = new RequestsLogListener() {			
			@Override
			public void requestLogged(Request r) {
				if (tableViewer != null)
					tableViewer.refresh();
			}
		};
		RequestsLogService.getInstance().addListener(reqListener);
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		Viewer viewer = tableViewer;
		if (viewer != null && !viewer.getControl().isDisposed()) {
			viewer.getControl().setFocus();
		}
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
		tableViewer = new TreeViewer(createTable(parent));
		tableViewer.setLabelProvider(new ITableLabelProvider(){
			@Override
			public void addListener(ILabelProviderListener listener) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			@Override
			public void removeListener(ILabelProviderListener listener) {
			}

			@Override
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			@Override
			public String getColumnText(Object element, int columnIndex) {
				Object[] values = (Object[])element;
				if (values.length <= columnIndex)
					return "";
				if (values == null)
					return "-/-";
				if (values[columnIndex] == null)
					return "null";
				if (columnIndex == 0)
					return values[columnIndex].toString();
				else if (values[columnIndex] instanceof RequestLogEntry)
					return values[columnIndex].toString();
				else 
					return PropertyLabelProvider.getText(values[columnIndex]);
			}			
		});
		tableViewer.setContentProvider(getPropertiesContentProvider());
		tableViewer.setInput(RequestsLogService.getInstance());
		RequestsLogService.getInstance().enable();		
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


	protected ITreeContentProvider getPropertiesContentProvider() {
		return new ITreeContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {				
				if (!(inputElement instanceof RequestsLogService))
					return new Object[0];
				
				List<RequestLogEntry> requests = RequestsLogService.getInstance().getLoggedRequests();
				Object[] objs = new Object[requests.size()];
				int count = 0;
				for (RequestLogEntry logEntry : requests) {
					objs[count++] = new Object[] {logEntry.getTimeString(), logEntry};
				}
				return objs;
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				if (!(parentElement instanceof Object[]))
					return null;
				parentElement = ((Object[])parentElement)[1];
				if (parentElement == null)
					return null;
				Class<?> cls = parentElement.getClass();
				if (cls.isArray()) {
					Object[][] res = new Object[Array.getLength(parentElement)][];
					for (int i = 0; i<res.length; i++) {
						res[i] = new Object[] {"["+i+"]", Array.get(parentElement, i)};
					}
					return res;
				} else if (parentElement instanceof Collection) {
					Collection it = (Collection)parentElement;
					Object[][] res = new Object[it.size()][];
					int i = 0;
					for (Object obj : it) {
						res[i] = new Object[] {"["+i+"]", obj};
						i++;						
					}
					return res;
				}
				
				Set<Field> fs = getFields(parentElement.getClass(), new LinkedHashSet<Field>());
				int i=0;
				Object[][] res = new Object[fs.size()][];
				
				for (Field f : fs) {
					f.setAccessible(true);
					try {
						res[i] = new Object[] {f.getName(), f.get(parentElement)};
					} catch (Exception e) {
						res[i] = new Object[] {f.getName(), "ERROR: " + e.getMessage()};
					}
					i++;
				}
				return res;
			}

			public Set<Field> getFields(Class<?> cls, Set<Field> l) {
				if (cls == Object.class)
					return l;
				for (Field f : cls.getDeclaredFields()) {
					if (f.isEnumConstant() || Modifier.isStatic(f.getModifiers()))
						continue;
					l.add(f);
				}
				return getFields(cls.getSuperclass(),l);
			}

			@Override
			public Object getParent(Object element) {
				return null;
			}

			@Override
			public boolean hasChildren(Object element) {
				return (Object.class.isAssignableFrom(element.getClass()) && !(element instanceof String));
			}
			
		};
	}
	
	@Override
	public void dispose() {
		super.dispose();
		RequestsLogService.getInstance().removeListener(reqListener);
		RequestsLogService.getInstance().disable();
	}

	private void clear() {
		RequestsLogService.getInstance().clear();
		if (tableViewer != null)
			tableViewer.refresh();
	}

	private TreeViewer tableViewer;
	private RequestsLogListener reqListener;
}