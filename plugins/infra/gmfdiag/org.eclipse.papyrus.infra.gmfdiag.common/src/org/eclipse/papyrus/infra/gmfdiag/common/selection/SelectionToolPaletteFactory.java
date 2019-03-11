/*****************************************************************************
 * Copyright (c) 2012, 2014 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *    Celine Janssens (ALL4TEC) celine.janssens@all4tec.net - initial API and implementation
 *
 ****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.selection;

import org.eclipse.gef.Tool;
import org.eclipse.gmf.runtime.diagram.ui.services.palette.PaletteFactory;


/**
 * This Factory creates the different selection tools of the Palette depending on the wished behavior.
 *
 * <p>
 * There are 2 types of behaviors for the marquee selection:
 * <li>Rubberband : Select the items intersected or included into the marquee rectangle</li>
 * <li>Inside : Select the items completely included into the marquee rectangle</li>
 * </p>
 * <br/>
 * On top of this 2 different behaviors, the tool can define if labels, connectors or nodes are part of the selection Tool.
 * By default they are all part of the selection.
 * This factory creates 8 tools combining those criteria.<br/>
 *
 * @author Celine JANSSENS
 *
 */
public class SelectionToolPaletteFactory extends PaletteFactory.Adapter {


	private static final String TOOL_SELECTION_RUBBERBAND = "selectionRubberband"; //$NON-NLS-1$

	private static final String TOOL_SELECTION_RUBBERBAND_NODE = "selectionRubberbandNodeOnly"; //$NON-NLS-1$

	private static final String TOOL_SELECTION_RUBBERBAND_CONNECTION = "selectionRubberbandConnectionOnly"; //$NON-NLS-1$

	private static final String TOOL_SELECTION_RUBBERBAND_LABEL = "selectionRubberbandLabelOnly"; //$NON-NLS-1$



	private static final String TOOL_SELECTION_INSIDE = "selectionInside"; //$NON-NLS-1$

	private static final String TOOL_SELECTION_INSIDE_NODE = "selectionInsideNodeOnly"; //$NON-NLS-1$

	private static final String TOOL_SELECTION_INSIDE_CONNECTION = "selectionInsideConnectionOnly"; //$NON-NLS-1$

	private static final String TOOL_SELECTION_INSIDE_LABEL = "selectionInsideLabelOnly"; //$NON-NLS-1$



	@Override
	public Tool createTool(String toolId) {

		// Rubberband with all item
		if (toolId.equals(TOOL_SELECTION_RUBBERBAND)) {
			PapyrusRubberbandSelectionTool tool = new PapyrusRubberbandSelectionTool();
			tool.setMarqueeBehavior(PapyrusRubberbandSelectionTool.BEHAVIOR_OBJECT_TOUCHED);
			return tool;

			// Standard Marquee Selection with all item
		} else if (toolId.equals(TOOL_SELECTION_INSIDE)) {
			PapyrusRubberbandSelectionTool tool = new PapyrusRubberbandSelectionTool();
			tool.setMarqueeBehavior(PapyrusRubberbandSelectionTool.BEHAVIOR_OBJECT_INCLUDED);
			return tool;

			// Rubberband with only Nodes
		} else if (toolId.equals(TOOL_SELECTION_RUBBERBAND_NODE)) {
			PapyrusRubberbandSelectionTool tool = new PapyrusRubberbandSelectionTool();
			tool.setMarqueeBehavior(PapyrusRubberbandSelectionTool.BEHAVIOR_OBJECT_TOUCHED);
			tool.setSelectLabels(false);
			tool.setSelectNodes(true);
			tool.setSelectConnections(false);
			return tool;

			// Rubberband with only Connection
		} else if (toolId.equals(TOOL_SELECTION_RUBBERBAND_CONNECTION)) {
			PapyrusRubberbandSelectionTool tool = new PapyrusRubberbandSelectionTool();
			tool.setMarqueeBehavior(PapyrusRubberbandSelectionTool.BEHAVIOR_OBJECT_TOUCHED);
			tool.setSelectLabels(false);
			tool.setSelectNodes(false);
			tool.setSelectConnections(true);
			return tool;

			// Rubberband with only Labels
		} else if (toolId.equals(TOOL_SELECTION_RUBBERBAND_LABEL)) {
			PapyrusRubberbandSelectionTool tool = new PapyrusRubberbandSelectionTool();
			tool.setMarqueeBehavior(PapyrusRubberbandSelectionTool.BEHAVIOR_OBJECT_TOUCHED);
			tool.setSelectLabels(true);
			tool.setSelectNodes(false);
			tool.setSelectConnections(false);
			return tool;

			// Standard marquee with only Nodes
		} else if (toolId.equals(TOOL_SELECTION_INSIDE_NODE)) {
			PapyrusRubberbandSelectionTool tool = new PapyrusRubberbandSelectionTool();
			tool.setMarqueeBehavior(PapyrusRubberbandSelectionTool.BEHAVIOR_OBJECT_INCLUDED);
			tool.setSelectLabels(false);
			tool.setSelectNodes(true);
			tool.setSelectConnections(false);
			return tool;

			// Standard marquee with only Labels
		} else if (toolId.equals(TOOL_SELECTION_INSIDE_LABEL)) {
			PapyrusRubberbandSelectionTool tool = new PapyrusRubberbandSelectionTool();
			tool.setMarqueeBehavior(PapyrusRubberbandSelectionTool.BEHAVIOR_OBJECT_INCLUDED);
			tool.setSelectLabels(true);
			tool.setSelectNodes(false);
			tool.setSelectConnections(false);
			return tool;

			// Standard marquee with only Connections
		} else if (toolId.equals(TOOL_SELECTION_INSIDE_CONNECTION)) {
			PapyrusRubberbandSelectionTool tool = new PapyrusRubberbandSelectionTool();
			tool.setMarqueeBehavior(PapyrusRubberbandSelectionTool.BEHAVIOR_OBJECT_INCLUDED);
			tool.setSelectLabels(false);
			tool.setSelectNodes(false);
			tool.setSelectConnections(true);
			return tool;

		}

		return super.createTool(toolId);
	}
}
