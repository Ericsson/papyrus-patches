/*****************************************************************************
 * Copyright (c) 2009, 2017 Atos Origin, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Emilien Perico (Atos Origin) emilien.perico@atosorigin.com - Initial API and implementation
 *  Christian W. Damus - bug 482220
 *  Fanch BONNABESSE (ALL4TEC) fanch.bonnabesse@all4tec.net - Bug 491816
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.notation.BooleanValueStyle;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.DoubleValueStyle;
import org.eclipse.gmf.runtime.notation.EObjectValueStyle;
import org.eclipse.gmf.runtime.notation.IntValueStyle;
import org.eclipse.gmf.runtime.notation.NamedStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.StringListValueStyle;
import org.eclipse.gmf.runtime.notation.StringValueStyle;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.core.resource.IModel;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.core.utils.ServiceUtils;
import org.eclipse.papyrus.infra.emf.utils.BusinessModelResolver;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramUtils;
import org.eclipse.papyrus.infra.gmfdiag.style.PapyrusDiagramStyle;
import org.eclipse.papyrus.infra.ui.util.ServiceUtilsForActionHandlers;

/**
 * Utilities method to manage notation models. Should be moved in a more
 * suitable plugin
 */
public class NotationUtils {

	/**
	 * Get the notation Resource.
	 *
	 * @return
	 *
	 * @deprecated Usage of the internal Resource is discouraged.
	 */
	@Deprecated
	public static Resource getNotationResource() {
		return getNotationModel().getResource();
	}

	/**
	 * Gets the NotationModel for the currently selected editor. <br>
	 * Warning: This method is designed to be call from ui.handlers. It is not
	 * designed to be call from Editors. This method can return null if called
	 * during the MultiEditor initialization.
	 *
	 * @see ServiceUtilsForActionHandlers.getInstance().getModelSet()
	 *
	 * @return The {@link NotationModel} of the current editor, or null if not
	 *         found.
	 */
	public static NotationModel getNotationModel() {

		try {
			return (NotationModel) ServiceUtilsForActionHandlers.getInstance().getModelSet().getModel(NotationModel.MODEL_ID);
		} catch (ServiceException e) {
			return null;
		}
	}

	/**
	 * Gets the NotationModel for the currently selected editor. <br>
	 * Warning: This method is designed to be call from ui.handlers. It is not
	 * designed to be call from Editors. This method can return null if called
	 * during the MultiEditor initialization.
	 *
	 * @see ServiceUtilsForActionHandlers.getInstance().getModelSet()
	 *
	 * @return The {@link NotationModel} of the current editor, or null if not
	 *         found.
	 * @throws ServiceException
	 *             If an error occurs while getting or starting the service.
	 */
	public static NotationModel getNotationModelChecked() throws ServiceException {

		return (NotationModel) ServiceUtilsForActionHandlers.getInstance().getModelSet().getModel(NotationModel.MODEL_ID);
	}

	/**
	 * Gets the NotationModel from the {@link ModelSet}. <br>
	 *
	 * @param modelsManager
	 *            The modelManager containing the requested model.
	 *
	 * @return The {@link NotationModel} registered in modelManager, or null if
	 *         not found.
	 */
	public static NotationModel getNotationModel(ModelSet modelsManager) {

		return (NotationModel) modelsManager.getModel(NotationModel.MODEL_ID);
	}

	/**
	 * Gets the direct associated diagram of the specified eObject.
	 *
	 * @param eObject
	 * @param notationResource
	 *
	 * @return the associated diagram
	 */
	public static Diagram getAssociatedDiagram(Resource notationResource, EObject eObject) {
		if (notationResource != null) {
			for (EObject obj : notationResource.getContents()) {
				if (obj instanceof Diagram) {
					Diagram diagram = (Diagram) obj;
					if (DiagramUtils.getOwner(diagram) == eObject) {
						return diagram;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Gets the direct associated diagram of the specified eObject.
	 *
	 * @param eObject
	 * @param notationResource
	 * @param resolve
	 *            the resource if true
	 *
	 * @return the associated diagram
	 */
	public static Diagram getAssociatedDiagram(Resource notationResource, EObject eObject, boolean resolve) {
		if (notationResource != null && resolve) {
			EcoreUtil.resolveAll(notationResource);
		}
		return getAssociatedDiagram(notationResource, eObject);
	}

	/**
	 * Gets the all the diagrams contained in the specified ancestor eObject
	 *
	 * @param notationResource
	 * @param eObject
	 *
	 * @return all the contained diagrams
	 *
	 */
	public static List<Diagram> getDiagrams(Resource notationResource, EObject eObject) {
		List<Diagram> diagrams = new LinkedList<>();
		if (notationResource != null) {
			for (EObject obj : notationResource.getContents()) {
				if (obj instanceof Diagram) {
					Diagram diagram = (Diagram) obj;
					if (EcoreUtil.isAncestor(eObject, DiagramUtils.getOwner(diagram))) {
						diagrams.add(diagram);
					}
				}
			}
		}
		return diagrams;
	}

	/**
	 * @param diagOrViewStyle
	 *            a diagram or its view-style
	 * @param owner
	 *            a semantic owner of a diagram
	 * @return the diagram owned by the 'semantic owner' if it actually is owned by it, null otherwise
	 */
	public static Diagram getOwnedDiagram(EObject diagOrViewStyle, EObject owner) {
		if (diagOrViewStyle instanceof Diagram) {
			Diagram diagram = (Diagram) diagOrViewStyle;
			if (DiagramUtils.getOwner(diagram) == owner) {
				return diagram;
			}
		} else if (diagOrViewStyle instanceof PapyrusDiagramStyle) {
			PapyrusDiagramStyle viewStyle = (PapyrusDiagramStyle) diagOrViewStyle;
			if (viewStyle.getOwner() == owner) {
				if (viewStyle.eContainer() instanceof Diagram) {
					return (Diagram) viewStyle.eContainer();
				}
			}
		}
		return null;
	}

	/**
	 * Gets the all the diagrams contained in the specified ancestor eObject
	 *
	 * @param notationResource
	 * @param eObject
	 * @param resolve
	 *            the resource if true
	 *
	 * @return all the contained diagrams
	 */
	public static List<Diagram> getDiagrams(Resource notationResource, EObject eObject, boolean resolve) {
		if (notationResource != null && resolve) {
			EcoreUtil.resolveAll(notationResource);
		}
		return getDiagrams(notationResource, eObject);
	}

	/**
	 * Obtains all of the notation views (diagrams, tables, etc.) persisted in a resource-set.
	 *
	 * @param resourceSet
	 *            a resource-set
	 *
	 * @return all of the notations within it
	 *
	 * @see #getAllNotations(ResourceSet, Class)
	 */
	public static Iterable<EObject> getAllNotations(ResourceSet resourceSet) {
		return getAllNotations(resourceSet, EObject.class);
	}

	/**
	 * Obtains all of the notation views of some type persisted in a resource-set.
	 *
	 * @param resourceSet
	 *            a resource-set
	 * @param type
	 *            the notation type (diagram, table, etc.)
	 *
	 * @return all of the notations of that {@code type} within it
	 */
	public static <T extends EObject> Iterable<T> getAllNotations(ResourceSet resourceSet, Class<T> type) {
		// Algorithm ported from NavigatorUtils::getNotationRoots() in the Model Explorer View bundle
		return () -> resourceSet.getResources().stream()
				.filter(r -> NotationModel.NOTATION_FILE_EXTENSION.equalsIgnoreCase(r.getURI().fileExtension()))
				.map(Resource::getContents)
				.flatMap(Collection::stream)
				.filter(type::isInstance)
				.map(type::cast)
				.iterator();
	}

	/**
	 * Helper to retrieve the Notation resource associated to a ModelSet. May be null.
	 *
	 * @param from
	 * @return
	 */
	public static Resource getNotationResource(ModelSet from) {
		IModel notationModel = from.getModel(NotationModel.MODEL_ID);
		if (notationModel instanceof NotationModel) {
			return ((NotationModel) notationModel).getResource();
		}
		return null;
	}

	/**
	 * Helper to retrieve the Notation resource associated to a ServicesRegistry. May be null.
	 *
	 * @param from
	 * @return
	 */
	public static Resource getNotationResource(ServicesRegistry registry) {
		try {
			ModelSet modelSet = ServiceUtils.getInstance().getModelSet(registry);
			return getNotationResource(modelSet);
		} catch (ServiceException ex) {
			Activator.log.error(ex);
			return null;
		}
	}


	/**
	 * Returns the notation resource where to add the new diagram
	 *
	 * @param eObject
	 *            the semantic object linked to the diagram or the diagram itself.
	 * @param domain
	 *            the editing domain
	 * @return the resource where the diagram should be added or <code>null</code> if no resource was found
	 *
	 */
	public static Resource getNotationResourceForDiagram(EObject eObject, TransactionalEditingDomain domain) {
		Object object = BusinessModelResolver.getInstance().getBusinessModel(eObject);
		EObject semanticObject;
		if (!(object instanceof EObject)) {
			semanticObject = eObject;
		} else {
			semanticObject = (EObject) object;
		}

		Resource containerResource = semanticObject.eResource();
		if (containerResource == null) {
			return null;
		}
		// retrieve the model set from the container resource
		ResourceSet resourceSet = containerResource.getResourceSet();

		if (resourceSet instanceof ModelSet) {
			ModelSet modelSet = (ModelSet) resourceSet;
			Resource destinationResource = modelSet.getAssociatedResource(semanticObject, NotationModel.NOTATION_FILE_EXTENSION, true);
			return destinationResource;
		} else {
			throw new RuntimeException("Resource Set is not a ModelSet or is null"); //$NON-NLS-1$
		}
	}

	/**
	 * Gets the int value from a NamedStyle property.
	 *
	 * @param view
	 *            the view
	 * @param property
	 *            the property
	 * @param defaultInt
	 *            the default int
	 * @return Integer corresponding to the property
	 */
	public static int getIntValue(View view, String property, int defaultInt) {
		int value = defaultInt;
		EClass intValueStyle = NotationPackage.eINSTANCE.getIntValueStyle();
		NamedStyle style;

		if (intValueStyle != null) {

			style = view.getNamedStyle(intValueStyle, property);
			if (style instanceof IntValueStyle) {
				value = ((IntValueStyle) style).getIntValue();
			}
		}
		return value;
	}



	/**
	 * Gets the boolean value from a NamedStyle property.
	 *
	 * @param view
	 *            the view
	 * @param property
	 *            the property
	 * @param defaultValue
	 *            the default value
	 * @return Boolean corresponding to the property
	 */
	public static boolean getBooleanValue(View view, String property, boolean defaultValue) {
		boolean value = defaultValue;
		EClass booleanValueStyle = NotationPackage.eINSTANCE.getBooleanValueStyle();
		NamedStyle style;

		if (booleanValueStyle != null) {
			style = view.getNamedStyle(booleanValueStyle, property);
			if (style instanceof BooleanValueStyle) {
				value = ((BooleanValueStyle) style).isBooleanValue();
			}
		}
		return value;
	}



	/**
	 * Gets the string value from a NamedStyle property.
	 *
	 * @param view
	 *            the view
	 * @param property
	 *            the property
	 * @param defaultValue
	 *            the default value
	 * @return the string value
	 */
	public static String getStringValue(View view, String property, String defaultValue) {
		String value = defaultValue;
		EClass stringValueStyle = NotationPackage.eINSTANCE.getStringValueStyle();
		NamedStyle style;

		if (stringValueStyle != null) {
			style = view.getNamedStyle(stringValueStyle, property);
			if (style instanceof StringValueStyle) {
				value = ((StringValueStyle) style).getStringValue();
			}
		}
		return value;
	}

	/**
	 * Gets the EObject value from a NamedStyle property.
	 *
	 * @param view
	 *            the view
	 * @param property
	 *            the property
	 * @param defaultValue
	 *            the default value
	 * @return the EObject
	 */
	public static EObject getEObjectValue(View view, String property, EObject defaultValue) {
		EObject value = defaultValue;
		EClass eObjectValueStyle = NotationPackage.eINSTANCE.getEObjectValueStyle();

		if (eObjectValueStyle != null) {
			NamedStyle style = view.getNamedStyle(eObjectValueStyle, property);
			if (style instanceof EObjectValueStyle) {
				value = ((EObjectValueStyle) style).getEObjectValue();
			}
		}
		return value;
	}

	/**
	 * Get the double value from a NamedStyle property.
	 *
	 * @param view
	 *            the view
	 * @param property
	 *            the property
	 * @param defaultDouble
	 *            the default double
	 * @return double corresponding to the property
	 * @since 3.0
	 */
	public static double getDoubleValue(final View view, final String property, final double defaultDouble) {
		double value = defaultDouble;
		EClass doubleValueStyle = NotationPackage.eINSTANCE.getDoubleValueStyle();
		NamedStyle style;

		if (null != doubleValueStyle) {
			style = view.getNamedStyle(doubleValueStyle, property);
			if (style instanceof DoubleValueStyle) {
				value = ((DoubleValueStyle) style).getDoubleValue();
			}
		}
		return value;
	}

	/**
	 * Get the list as a String list and convert it to Int list
	 *
	 * @param model
	 * @param floatingLabelOffsetWidth
	 * @param defaultCustomStyle
	 * @return
	 *
	 */
	public static int[] getIntListValue(View view, String property, int[] defaultIntList) {
		int[] value = defaultIntList;
		EClass intValueStyle = NotationPackage.eINSTANCE.getStringListValueStyle();
		NamedStyle style;

		if (intValueStyle != null) {
			style = view.getNamedStyle(intValueStyle, property);
			if (style instanceof StringListValueStyle) {
				// Get the string list
				EList<String> valueList = ((StringListValueStyle) style).getStringListValue();
				int i = 0;
				value = new int[valueList.size()];
				// Convert list in int array
				for (Iterator<?> iterator = valueList.iterator(); iterator.hasNext();) {
					String string = (String) iterator.next();
					value[i++] = Integer.parseInt(string);
				}
			}
		}
		return value;
	}


	/**
	 * Get the list as a String list
	 *
	 * @param view
	 *            model
	 * @param property
	 *            property name
	 * @param defaultStringList
	 *            default value if empty
	 * @return List of String
	 *
	 */
	public static EList<String> getStringListValue(View view, String property, EList<String> defaultStringList) {
		EList<String> value = defaultStringList;
		if (view != null && property != null && !property.isEmpty()) {
			EClass stringValueStyle = NotationPackage.eINSTANCE.getStringListValueStyle();
			if (stringValueStyle != null) {
				NamedStyle style;
				style = view.getNamedStyle(stringValueStyle, property);
				if (style instanceof StringListValueStyle) {
					// Get the string list
					value = ((StringListValueStyle) style).getStringListValue();
				}
			}
		}
		return value;
	}
}
