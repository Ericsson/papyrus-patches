package com.ericsson.papyrus.devtools.views;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class PropertyLabelProvider implements ITableLabelProvider {
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
		if (values == null || values.length <= columnIndex)
			return "-/-";
		return getText(values[columnIndex]);
	}				

	private static Set<Class<?>> PRIMITIVE_WRAPPERS = new HashSet<Class<?>>(Arrays.asList(
		Boolean.class,
		Character.class,
		Byte.class,
		Short.class,
		Integer.class,
		Long.class,
		Float.class,
		Double.class,
		Void.class));
	 
	public static boolean isPrimitiveOrWrapperClass(Class<?> cls) {
		return cls.isPrimitive() || PRIMITIVE_WRAPPERS.contains(cls);
	}

	public static String getClassName(Class<?> cls) {
		return cls.isAnonymousClass() ? 
				cls.getName().substring(cls.getName().lastIndexOf('.')+1) :
				cls.getSimpleName()	;
	}
 
	public static String getText(Object val) {
		if (val == null)
			return "null";
		if (val.getClass().isArray()) {
			if (!isPrimitiveOrWrapperClass(val.getClass().getComponentType()))
				return Arrays.deepToString((Object[])val);
			try {
				if (val.getClass().getComponentType() == char.class)
					return String.valueOf((char[])val);
				return (String)Arrays.class.getMethod("toString", val.getClass()).invoke(null, val);
			} catch (Exception e) {}
			return val.toString();
		}

		if (val instanceof Collection) {
			return getText(((Collection)val).toArray());
		}
		
		Class<?> cls = val.getClass();
		if (isPrimitiveOrWrapperClass(cls) || val instanceof String) {
			if (cls == char.class) {
				return "'"+val+"' ("+Integer.toHexString((int)val)+")";
			}
			return val.toString();
		}
		return getClassName(cls);		
	}
}
