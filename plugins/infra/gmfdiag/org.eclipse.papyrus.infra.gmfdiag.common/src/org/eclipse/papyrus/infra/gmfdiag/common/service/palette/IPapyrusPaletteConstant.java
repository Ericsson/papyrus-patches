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
 *  Remi Schnekenburger (CEA LIST) remi.schnekenburger@cea.fr - Initial API and implementation
 *  Micka�l ADAM (ALL4TEC) mickael.adam@all4tec.net - Move from oep.uml.diagram.common and remove aspect actions framework, see bug 512343.
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.service.palette;


import org.eclipse.papyrus.infra.gmfdiag.common.Activator;

/**
 * Constant for the papyrus palette extension point
 * @since 3.0
 */
public interface IPapyrusPaletteConstant {

	/** Papyrus palette definition extension point name */
	public final String PALETTE_DEFINITION = "paletteDefinition"; //$NON-NLS-1$

	/** Papyrus palette definition full extension point identifier */
	public final String PALETTE_DEFINITION_FULL_ID = Activator.ID + "." + PALETTE_DEFINITION; //$NON-NLS-1$

	/** name for the field giving the path to the XML file */
	public final String CONTENT = "content"; //$NON-NLS-1$

	/** name for the field giving the path to the XML file */
	public final String PATH = "path"; //$NON-NLS-1$

	/** name of the properties node */
	public final String PALETTE_DESCRIPTION_PROPERTIES = "properties";//$NON-NLS-1$

	/** name of the drawer node */
	public final String DRAWER = "drawer";//$NON-NLS-1$

	/** name of the stack node */
	public final String STACK = "stack";//$NON-NLS-1$

	/** name of the tool node */
	public final String TOOL = "tool";//$NON-NLS-1$

	/** name of the separator node */
	public final String SEPARATOR = "separator";//$NON-NLS-1$

	/** name of the ID attribute */
	public final String ID = "id";//$NON-NLS-1$

	/** name of the editor attribute */
	public final String EDITOR = "editor";//$NON-NLS-1$

	/** name of the name attribute */
	public final String NAME = "name";//$NON-NLS-1$

	/** name of the editor id attribute */
	public final String EDITOR_ID = "editorID";//$NON-NLS-1$

	/** name of the priority attribute */
	public final String PRIORITY = "priority";//$NON-NLS-1$

	/** name of the description attribute */
	public final String DESCRIPTION = "description";//$NON-NLS-1$

	/** id for the preference store for palette customizations */
	public final String PALETTE_CUSTOMIZATIONS_ID = "paletteCustomization";//$NON-NLS-1$

	/** id for the preference store for palette redefinitions */
	public final String PALETTE_REDEFINITIONS = "paletteRedefinitions";//$NON-NLS-1$

	/** id for the node: palette redefinition */
	public final String PALETTE_REDEFINITION = "paletteRedefinition";//$NON-NLS-1$

	/** id for the preference store for workspace palette definitions based on model */
	public final String EXTENDED_PALETTE_WORKSPACE_DEFINITIONS = "workspaceExtendedPaletteDefinition";//$NON-NLS-1$

	/** id for the preference store for palette definitions based on model */
	public final String LOCAL_EXTENDED_PALETTE_DEFINITIONS = "extendedPaletteDefinition"; //$NON-NLS-1$

	/** id for the preference definitions */
	public final String HIDDEN_PALETTES = "hiddenPalettes";//$NON-NLS-1$

	/** id for the palette attribute */
	public final String PALETTE = "palette";//$NON-NLS-1$

	/** id for the class attribute */
	public final String CLASS = "class";//$NON-NLS-1$

	/** id for the icon path attribute */
	public final String ICON_PATH = "iconpath";//$NON-NLS-1$

	/** id for the referenced tool in aspect tools */
	public final String REF_TOOL_ID = "refToolId";//$NON-NLS-1$

	/** id for the attribute stereotypes to apply qualified name */
	public final String STEREOTYPES_TO_APPLY = "stereotypesToApply";//$NON-NLS-1$

	/** id for the attribute advice to apply */
	public final String ADVICES_TO_APPLY = "advicesToApply";//$NON-NLS-1$

	/** key for the properties tool */
	public final String STEREOTYPES_TO_APPLY_KEY = "StereotypesToApply";//$NON-NLS-1$

	/** key for the profile list attribute */
	public final String PROFILE_LIST = "requiredProfiles";//$NON-NLS-1$

	/** name of the value attribute */
	public final String VALUE = "value";//$NON-NLS-1$

	/** name of the feature node for features defined statically */
	public final String FEATURE_NODE_NAME = "feature";//$NON-NLS-1$

	/** name of the attribute or node for display kind */
	public final String DISPLAY_KIND = "displayKind";//$NON-NLS-1$

	/** name of the attribute or node for display place */
	public final String DISPLAY_PLACE = "displayPlace";//$NON-NLS-1$

}
