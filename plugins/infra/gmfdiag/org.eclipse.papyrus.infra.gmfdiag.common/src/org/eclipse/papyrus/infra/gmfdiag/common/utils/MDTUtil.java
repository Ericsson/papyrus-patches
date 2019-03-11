/*******************************************************************************
 * Copyright (c) 2008, 2014 Conselleria de Infraestructuras y Transporte, Generalitat
 * de la Comunitat Valenciana, CEA, and others. All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which accompanies this distribution, and is
t https://www.eclipse.org/legal/epl-2.0/
t
t SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *   Francisco Javier Cano Muñoz (Prodevelop) – Initial implementation.
 *   Gabriel Merin Cubero (Prodevelop) – Operations to add a diagram version
 *   Christian W. Damus (CEA) - bug 422257
 *
 ******************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.ui.util.EditorHelper;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.uml2.common.util.CacheAdapter;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * The Class MDTUtil.
 *
 * @author <a href="fjcano@prodevelop.es">Francisco Javier Cano Muñoz</a>
 * @author <a href="gmerin@prodevelop.es">Grabriel Merin Cubero</a>
 *
 */
public class MDTUtil {

	/**
	 * Obtain the plugin's version of the editor.
	 *
	 * @param editor
	 *            Editor Part
	 *
	 * @return the editor version
	 *
	 * @author <a href="mailto:gmerin@prodevelop.es">Gabriel Merin</a> The
	 *         version of the editor
	 */
	// @unused
	public static String getEditorVersion(IEditorPart editor) {
		return getBundleVersion(editor.getSite().getPluginId());
	}

	/**
	 * Obtain the plugin's version of the editor.
	 *
	 * @param pluginId
	 *            Plugin ID
	 *
	 * @return The version of the plugin
	 *
	 * @author gmerin
	 * @author <a href="mailto:fjcano@prodevelop.es">Francisco Javier Cano Muñoz</a>
	 */
	public static String getBundleVersion(String pluginId) {
		Bundle bundle = Platform.getBundle(pluginId);
		if (bundle == null) {
			return null;
		}
		String version = bundle.getHeaders().get(Constants.BUNDLE_VERSION);
		return version;
	}

	/**
	 * Gets the workspace location.
	 *
	 * @return the workspace location
	 */
	public static IPath getWorkspaceLocation() {
		try {
			return ResourcesPlugin.getWorkspace().getRoot().getLocation();
		} catch (NullPointerException ex) {
			IStatus status = new Status(IStatus.ERROR, Activator.ID, "Error getting workspace", ex);
			Activator.getInstance().getLog().log(status);
			return null;
		}
	}

	/**
	 * Full file path to resource.
	 *
	 * @param filePath
	 *            the file path
	 *
	 * @return the string
	 */
	public static String fullFilePathToResource(String filePath) {
		String workspaceLocation = getWorkspaceLocation().toString();
		return filePath.replaceFirst(workspaceLocation, "");
	}

	/**
	 * Full file path to resource uri.
	 *
	 * @param filePath
	 *            the file path
	 *
	 * @return the uRI
	 */
	public static URI fullFilePathToResourceURI(String filePath) {
		String uri = fullFilePathToResource(filePath);
		return URI.createPlatformResourceURI(uri, true);
	}

	/**
	 * Retieves the <Diagram> from the given <IEditorPart>.
	 *
	 * @author <a href="mailto:fjcano@prodevelop.es">Francisco Javier Cano Muñoz</a>
	 * @param editor
	 * @return
	 */
	public static Diagram getDiagramFomEditor(IEditorPart editor) {
		if (editor instanceof DiagramEditor) {
			return ((DiagramEditor) editor).getDiagram();
		} else {
			if (editor != null) {
				Object adapted = editor.getAdapter(Diagram.class);
				if (adapted instanceof Diagram) {
					return (Diagram) adapted;
				}
			}
		}
		return null;
	}

	/**
	 * Gets the root element resource.
	 *
	 * @param editorPart
	 *            the editor part
	 *
	 * @return the root element resource
	 */
	public static Resource getRootElementResource(IEditorPart editorPart) {
		EObject rootElement = getEditorRootelement(editorPart);
		Resource resource = null;
		if (rootElement != null) {
			resource = rootElement.eResource();
		}
		return resource;
	}

	/**
	 * Gets the editor rootelement.
	 *
	 * @param editorPart
	 *            the editor part
	 *
	 * @return the editor rootelement
	 */
	public static EObject getEditorRootelement(IEditorPart editorPart) {
		EditPart rootEditPart = editorPart.getAdapter(EditPart.class);
		if (rootEditPart == null) {
			return null;
		}
		EObject rootElement = null;
		Object object = ((EditPart) rootEditPart.getChildren().get(0)).getModel();
		if (object instanceof View) {
			rootElement = ((View) object).getElement();
		}
		return rootElement;
	}

	/**
	 * @deprecated This creates a new resource set, which will leak UML model content forever in the {@link CacheAdapter}. Use the {@link #getRootElementsFromFile(IEditorInput, ResourceSet)} method, instead. Note that if the JVM has assertions
	 *             enabled for this package, then this method will throw an assertion error
	 */
	@Deprecated
	public static List<EObject> getRootElementsFromFile(IEditorInput input) {
		assert false : "use the getRootElementsFromFile(IEditorInput, ResourceSet) API"; //$NON-NLS-1$
		return getRootElementsFromFile(input, null);
	}

	/**
	 * Get the root elements in the file indicated by the specified editor {@code input}.
	 *
	 * @param input
	 *            an editor input
	 * @param resourceSet
	 *            a resource set in which to load the indicated resource. Must not be {@code null}
	 * @return the root elements
	 *
	 * @throws NullPointerException
	 *             if the {@code resourceSet} is {@code null}
	 */
	public static List<EObject> getRootElementsFromFile(IEditorInput input, ResourceSet resourceSet) {
		URI uri = null;
		IURIEditorInput uriEditorInput = Platform.getAdapterManager().getAdapter(input, IURIEditorInput.class);
		if (uriEditorInput != null) {
			uri = URI.createURI(uriEditorInput.getURI().toString());
		} else {
			IFileEditorInput fileEditorInput = Platform.getAdapterManager().getAdapter(input, IFileEditorInput.class);
			if (fileEditorInput != null) {
				uri = URI.createURI(fileEditorInput.getFile().getLocationURI().toString());
			}
		}
		if (uri != null) {
			Resource resource = resourceSet.getResource(uri, true);
			if (resource != null) {
				List<EObject> rootEObjects = new ArrayList<EObject>();
				for (EObject eObject : resource.getContents()) {
					rootEObjects.add(eObject);
				}
				return rootEObjects;
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Gets the object name or empty string.
	 *
	 * @param object
	 *            the object
	 *
	 * @return the object name or empty string
	 */
	// @unused
	public static String getObjectNameOrEmptyString(Object object) {
		String name = getObjectName(object);
		return name == null ? "" : name;
	}

	/** The Constant getNameNames. */
	private static final String[] getNameNames = { "getName", "getname" };

	/**
	 * Gets the object name.
	 *
	 * @param object
	 *            the object
	 *
	 * @return the object name
	 */
	public static String getObjectName(Object object) {
		if (object == null) {
			return null;
		}
		Method method = null;
		Object o = null;
		for (String methodName : getNameNames) {
			try {
				method = object.getClass().getMethod(methodName, (Class[]) null);
			} catch (NoSuchMethodException e) {
				method = null;
			}
			if (method != null) {
				break;
			}
		}
		if (method != null) {
			try {
				o = method.invoke(object, (Object[]) null);
			} catch (IllegalAccessException ex) {
				return null;
			} catch (InvocationTargetException ex) {
				return null;
			}
			if (o instanceof String) {
				return (String) o;
			}
		}
		return null;
	}

	/**
	 * Resolve semantic.
	 *
	 * @param object
	 *            the object
	 *
	 * @return the e object
	 */
	public static EObject resolveSemantic(Object object) {
		if (object instanceof EditPart) {
			object = ((EditPart) object).getModel();
		}
		if (object instanceof View) {
			object = ((View) object).getElement();
		}
		if (object instanceof EObject) {
			return (EObject) object;
		}
		return null;
	}

	/**
	 * Searchs for an EObject in a ResourceSet by its fragment.
	 *
	 * @param uriFragment
	 * @param resourceSet
	 * @return
	 */
	public static EObject searchEObjectFromFragment(URI uri, ResourceSet resourceSet) {
		if (uri == null) {
			return null;
		}
		String uriFragment = uri.fragment();
		if (uriFragment != null && uriFragment.length() > 0 && resourceSet != null) {
			resourceSet.getResource(uri.trimFragment(), true);
			for (Resource resource : resourceSet.getResources()) {
				// search for the EObject in each Resource
				EObject eObject = searchEObjectFromFragment(uriFragment, resource);
				if (eObject != null) {
					// if we find the target EObject in any of the Resources,
					// return it.
					return eObject;
				}
			}
		}
		return null;
	}

	/**
	 * Searchs for an EObject in a Resource by its fragment.
	 *
	 * @param uriFragment
	 * @param resource
	 * @return
	 */
	public static EObject searchEObjectFromFragment(String uriFragment, Resource resource) {
		if (uriFragment != null && uriFragment.length() > 0 && resource != null) {
			// search for the EObject in the Resource.
			return resource.getEObject(uriFragment);
		}
		return null;
	}

	/**
	 * Returns the first Diagram in a GMFResource.
	 *
	 * @param resource
	 * @return
	 */
	public static Diagram getFirstDiagramFromResource(Resource resource) {
		if (resource != null && resource.getContents().size() > 0) {
			// look for a Diagram in the whole Resource
			for (EObject eObject : resource.getContents()) {
				Diagram diagram = Platform.getAdapterManager().getAdapter(eObject, Diagram.class);
				if (diagram != null) {
					// the first Diagram found is returned.
					return diagram;
				}
			}
		}
		return null;
	}

	/**
	 * Looks in the class hierarchy for a Class or Interface with qualified name
	 * className.
	 *
	 * @param clazz
	 * @param className
	 * @return
	 */
	public static boolean isOfType(Class<?> clazz, String className) {
		if (clazz == null || className == null) {
			return false;
		}
		// check class
		if (clazz.getName().equals(className)) {
			return true;
		}
		// look interfaces
		for (Class<?> c : clazz.getInterfaces()) {
			if (isOfType(c, className)) {
				return true;
			}
		}
		// look superclass
		return isOfType(clazz.getSuperclass(), className);
	}

	/**
	 * @return
	 */
	public static IEditorPart getActiveEditor() {
		return EditorHelper.getCurrentEditor();
	}

}
