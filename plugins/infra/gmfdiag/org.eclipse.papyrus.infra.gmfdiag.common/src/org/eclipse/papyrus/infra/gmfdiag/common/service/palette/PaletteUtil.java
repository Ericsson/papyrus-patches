/*****************************************************************************
 * Copyright (c) 2009 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Mickael ADAM (ALL4TEC) mickael.adam@all4tec.net - Move from oep.uml.diagram.common, see bug 512343.
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.service.palette;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.gef.Tool;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gmf.runtime.common.core.service.ProviderPriority;
import org.eclipse.gmf.runtime.diagram.ui.internal.services.palette.ContributeToPaletteOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.palette.IPaletteProvider;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.SpecializationType;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.messages.Messages;
import org.eclipse.papyrus.infra.gmfdiag.common.service.palette.PapyrusPaletteService.ProviderDescriptor;
import org.eclipse.ui.IEditorPart;

/**
 * Utility class for palette.
 */
public class PaletteUtil {

	/**
	 * Returns the parent container by its ID
	 *
	 * @param entry
	 *            the palette container for which parent container is looked
	 * @param parentID
	 *            the id of the parent container
	 * @return the parent
	 */
	public static PaletteContainer getContainerByID(PaletteContainer container, String parentID) {
		// check this element is the searched parent;
		if (parentID.equals(container.getId())) {
			return container;
		}

		// element is not the parent. Look its children
		Iterator<PaletteContainer> it = getDirectChildContainers(container).iterator();
		while (it.hasNext()) {
			PaletteContainer tmp = getContainerByID(it.next(), parentID);
			if (tmp != null) {
				return tmp;
			}
		}
		return null;
	}

	/**
	 * Returns the type of metaclasses created by the toolentry
	 *
	 * @param entry
	 *            the entry for which metaclass created is searched
	 * @return the type of metaclasses created by the toolentry or <code>null</code>.
	 */
	public static EClass getToolMetaclass(ToolEntry entry) {
		EClass eClass = null;
		Tool tool = entry.createTool();
		List<IElementType> types = null;
		if (tool instanceof AspectUnspecifiedTypeCreationTool) {
			types = ((AspectUnspecifiedTypeCreationTool) tool).getElementTypes();
		} else if (tool instanceof AspectUnspecifiedTypeConnectionTool) {
			types = ((AspectUnspecifiedTypeConnectionTool) tool).getElementTypes();
		}
		if (types != null && types.size() > 0) {
			IElementType type = types.get(0);

			if (type instanceof SpecializationType) {
				type = ((SpecializationType) type).getSpecializedTypes()[0];
			}

			if (null != type) {
				eClass = type.getEClass();
			}
		}
		return eClass;
	}

	/**
	 * Returns the parent container by its ID
	 *
	 * @param entry
	 *            the palette entry for which parent container is looked
	 * @param parentID
	 *            the id of the parent container
	 * @return the parent
	 */
	public static PaletteContainer getContainerByID(PaletteEntry entry, String parentID) {
		// retrieve the root
		PaletteContainer root = getRoot(entry);
		return getContainerByID(root, parentID);
	}

	/**
	 * Return the child containers directly contained by the specified container
	 *
	 * @param container
	 *            the container to look in.
	 * @return the list of directly contained elements
	 */
	@SuppressWarnings("unchecked")
	public static List<PaletteContainer> getDirectChildContainers(PaletteContainer container) {
		List<PaletteContainer> containers = new ArrayList<>();
		Iterator<PaletteEntry> it = container.getChildren().iterator();
		while (it.hasNext()) {
			PaletteEntry entry = it.next();
			if (entry instanceof PaletteContainer) {
				containers.add((PaletteContainer) entry);
			}
		}
		return containers;
	}

	/**
	 * Retrieves the root element for the given container
	 *
	 * @param container
	 *            the container for which the root is searched
	 * @return the root of the container
	 */
	public static PaletteContainer getRoot(PaletteContainer container) {
		// if container has a parent, returns it.
		if (container.getParent() != null) {
			return getRoot(container.getParent());
		}
		// else, root element is the container itself.
		return container;
	}

	/**
	 * Retrieves the root element for the given palette entry
	 *
	 * @param container
	 *            the container for which the root is searched
	 * @return the root of the container
	 */
	public static PaletteContainer getRoot(PaletteEntry entry) {
		return getRoot(entry.getParent());
	}

	/**
	 * Default constructor. Should never be used, as method are static in this
	 * class.
	 */
	// @unused
	private PaletteUtil() {

	}

	/**
	 * return tool entries for the given {@link PaletteContainer} and its
	 * sub-containers
	 *
	 * @param container
	 *            the container that contains the ToolEntries
	 * @return the list of tool entries or an empty list
	 */
	public static List<ToolEntry> getAllToolEntries(PaletteContainer container) {
		final List<ToolEntry> entries = new ArrayList<>();
		Iterator<PaletteEntry> it = container.getChildren().iterator();
		while (it.hasNext()) {
			PaletteEntry entry = it.next();
			if (entry instanceof ToolEntry) {
				entries.add((ToolEntry) entry);
			}
			if (entry instanceof PaletteContainer) {
				entries.addAll(getAllToolEntries((PaletteContainer) entry));
			}
		}
		return entries;
	}

	/**
	 * Return all entries from a palette
	 *
	 * @param paletteRoot
	 *            the root from which tools are retrieved
	 * @return the list of entries
	 */
	public static List<PaletteEntry> getAllEntries(PaletteContainer container) {
		List<PaletteEntry> elements = new ArrayList<>();
		for (Object object : container.getChildren()) {
			if (object instanceof PaletteContainer) {
				elements.add((PaletteContainer) object);
				elements.addAll(getAllEntries((PaletteContainer) object));
			} else if (object instanceof ToolEntry) {
				elements.add((ToolEntry) object);
			}
		}
		return elements;
	}

	/**
	 * Returns all available entries for the given editor ID
	 *
	 * @param editorID
	 *            the editor to be contributed
	 * @param priority
	 *            the priority max for the entries
	 * @return the set of available entries
	 */
	public static Set<? extends PaletteEntry> getAvailableEntries(IEditorPart part, ProviderPriority priority) {
		Set<? extends PaletteEntry> entries = new HashSet<>();

		// retrieve all provider for the given editor ID
		PaletteRoot root = new PaletteRoot();
		List<? extends PapyrusPaletteService.ProviderDescriptor> providers = (List<? extends ProviderDescriptor>) PapyrusPaletteService.getInstance().getProviders();
		ContributeToPaletteOperation operation = new ContributeToPaletteOperation(part, part.getEditorInput(), root, new HashMap<Object, Object>());

		// generate for each provider, according to priority
		@SuppressWarnings("unchecked")
		List<PapyrusPaletteService.ProviderDescriptor> providerList = (List<PapyrusPaletteService.ProviderDescriptor>) PapyrusPaletteService.getInstance().getProviders();
		for (PapyrusPaletteService.ProviderDescriptor descriptor : providerList) {
			int compare = descriptor.getPriority().compareTo(priority);
			if (compare < 0) {
				if (descriptor.providesWithVisibility(operation)) {
					((IPaletteProvider) descriptor.getProvider()).contributeToPalette(part, part.getEditorInput(), root, new HashMap<Object, Object>());
				}
			}
		}
		return entries;
	}

	/**
	 * Returns all available entries for the given editor ID
	 *
	 * @param editorID
	 *            the editor to be contributed
	 * @param priority
	 *            the priority max for the entries
	 * @return the set of available entries
	 */
	public static Map<String, PaletteEntry> getAvailableEntriesSet(IEditorPart part, ProviderPriority priority) {
		Map<String, PaletteEntry> entries = new HashMap<>();

		// retrieve all provider for the given editor ID
		PaletteRoot root = new PaletteRoot();
		List<? extends PapyrusPaletteService.ProviderDescriptor> providers = (List<? extends ProviderDescriptor>) PapyrusPaletteService.getInstance().getProviders();
		ContributeToPaletteOperation operation = new ContributeToPaletteOperation(part, part.getEditorInput(), root, entries);

		// generate for each provider, according to priority
		@SuppressWarnings("unchecked")
		List<PapyrusPaletteService.ProviderDescriptor> providerList = (List<PapyrusPaletteService.ProviderDescriptor>) PapyrusPaletteService.getInstance().getProviders();
		for (PapyrusPaletteService.ProviderDescriptor descriptor : providerList) {
			int compare = descriptor.getPriority().compareTo(priority);
			if (compare <= 0) {
				if (descriptor.providesWithVisibility(operation)) {
					((IPaletteProvider) descriptor.getProvider()).contributeToPalette(part, part.getEditorInput(), root, entries);
				}
			}
		}
		return entries;
	}

	/**
	 * Returns the list of stereotypes String from a serialize string form
	 *
	 * @param serializedForm
	 *            the serialized form of the list of stereotypes
	 * @return the list of stereotypes String from a serialize string form
	 */
	public static List<String> getStereotypeListFromString(String serializedForm) {
		StringTokenizer tokenizer = new StringTokenizer(serializedForm, ","); //$NON-NLS-1$
		List<String> list = new ArrayList<>();
		while (tokenizer.hasMoreElements()) {
			list.add(tokenizer.nextToken().trim());
		}
		return list;
	}

	public static String convertToCommaSeparatedRepresentation(Collection objects) {
		return convertToFlatRepresentation(objects, ","); //$NON-NLS-1$
	}

	public static String convertToFlatRepresentation(Collection objects, String separator) {
		StringBuilder buffer = new StringBuilder();
		Iterator it = objects.iterator();
		while (it.hasNext()) {
			buffer.append(it.next());
			if (it.hasNext()) {
				buffer.append(separator);
			}
		}
		return buffer.toString();
	}

	/**
	 * Returns the list of profiles String from a serialize string form
	 *
	 * @param serializedForm
	 *            the serialized form of the list of stereotypes
	 * @return the list of profiles String from a serialize string form
	 */
	public static Set<String> getProfileSetFromString(String serializedForm) {
		StringTokenizer tokenizer = new StringTokenizer(serializedForm, ","); //$NON-NLS-1$
		Set<String> list = new HashSet<>();
		while (tokenizer.hasMoreElements()) {
			list.add(tokenizer.nextToken());
		}
		return list;
	}

	/**
	 * Returns the redefinition file URI
	 *
	 * @return the redefinition file URI or <code>null</code> if no local
	 *         redefinition can be found.
	 */
	public static URI getRedefinitionFileURI(final String contributionID) {
		String path = PapyrusPalettePreferences.getPaletteRedefinition(contributionID);
		StringBuilder error = new StringBuilder();

		URI uri = null;
		if (null == path) {
			error.append(Messages.PaletteUtil_ErrorMessage_PaletteNullOnContribution);
			error.append(contributionID);
		} else {
			File stateLocationRootFile = Activator.getInstance().getStateLocation().append(path).toFile();
			if (null == stateLocationRootFile) {
				error.append(Messages.PaletteUtil_ErrorMessage_NoRedefinitionFoundWithId);
				error.append(contributionID);

			} else if (!stateLocationRootFile.exists()) {
				error.append(Messages.PaletteUtil_ErrorMessage_NoLocalDefinition);
				error.append(stateLocationRootFile);

			} else if (!stateLocationRootFile.canRead()) {
				error.append(Messages.PaletteUtil_ErrorMessage_CantReadLocalDefinitionOfFile);
				error.append(stateLocationRootFile);

			} else {
				uri = URI.createFileURI(stateLocationRootFile.getAbsolutePath());
			}
		}

		if (!error.toString().isEmpty()) {
			Activator.log.error(error.toString(), null);
		}

		return uri;
	}

	/**
	 * Returns the list of profile Qualified Names String under a serialized
	 * form
	 *
	 * @param list
	 *            the list of profiles to serialize
	 * @return the list of profiles String under a serialized form
	 */
	public static String getSerializedProfileList(Collection<String> profiles) {
		return convertToCommaSeparatedRepresentation(profiles);
	}

}
