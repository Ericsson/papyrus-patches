/*****************************************************************************
 * Copyright (c) 2019 CEA LIST and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.tools;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
import org.eclipse.papyrus.infra.gmfdiag.common.snap.PapyrusDragEditPartsTrackerEx;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphSelectionCommandProxy;

/**
 * @author etxacam
 *
 */
public class PapyrusSequenceDragEditPartsTracker extends PapyrusDragEditPartsTrackerEx {

	public PapyrusSequenceDragEditPartsTracker(EditPart sourceEditPart) {
		super(sourceEditPart);
		// TODO Auto-generated constructor stub
	}

	public PapyrusSequenceDragEditPartsTracker(EditPart sourceEditPart, boolean snapOnCorners, boolean snapOnMiddles,
			boolean snapOnCenter) {
		super(sourceEditPart, snapOnCorners, snapOnMiddles, snapOnCenter);
	}

	@Override
	protected void addSourceCommands(boolean isMove, CompoundCommand command) {
		Request request = getTargetRequest();
		request.setType(isMove ? REQ_MOVE : RequestConstants.REQ_DRAG);
		EditPart editPart = getSourceEditPart(); 
		if (editPart == null)
			return;
		
		selectionCommandProxy.setRequest(request, editPart);
		Command cmd = selectionCommandProxy.getMultiSelectionCommand();
		if (cmd != null) {
			command.add(cmd);
		} else {
			super.addSourceCommands(isMove, command);
		}
		request.setType(RequestConstants.REQ_DROP);
	}
	
	private InteractionGraphSelectionCommandProxy selectionCommandProxy = new InteractionGraphSelectionCommandProxy();
}
