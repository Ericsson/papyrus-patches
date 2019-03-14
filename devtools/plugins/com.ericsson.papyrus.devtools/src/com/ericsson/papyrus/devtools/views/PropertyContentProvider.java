package com.ericsson.papyrus.devtools.views;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;

public class PropertyContentProvider implements ITreeContentProvider {
	public PropertyContentProvider() {
	}
	
	
	@Override
	public Object[] getElements(Object inputElement) {
		Object[] objs = getRootProperties(inputElement);				
		Object[] ext = getChildren(new Object[] {"",inputElement});
		if (ext != null && objs != null) {
			int s = objs.length;
			objs = Arrays.copyOf(objs, s + ext.length);
			System.arraycopy(ext, 0, objs, s, ext.length);
		}
		return objs;
	}

	protected Object[] getRootProperties(Object inputElement) {
		return null;
	}
	
	@Override
	public Object[] getChildren(Object parentElement) {
		if (!(parentElement instanceof Object[]))
			return null;
		parentElement = ((Object[])parentElement)[1];
		if (parentElement == null)
			return null;
		if (PropertyLabelProvider.isPrimitiveOrWrapperClass(parentElement.getClass())) {
			return new Object[0];
		}
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
		} else if (PropertyLabelProvider.isPrimitiveOrWrapperClass(parentElement.getClass())) {
			return new Object[0];
		}
		
		List<Field> fs = new ArrayList<Field>(getFields(parentElement.getClass(), new LinkedHashSet<Field>()));
		Collections.sort(fs, (a,b)-> a.getName().compareToIgnoreCase(b.getName()));
		
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
}
