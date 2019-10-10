/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   Celine Janssens (ALL4TEC) - Bug 507348
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.util;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.SharedCursors;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.LocationRequest;
import org.eclipse.papyrus.infra.gmfdiag.common.selection.SelectSeveralLinksEditPartTracker;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphSelectionCommandProxy;
import org.eclipse.swt.graphics.Cursor;

/**
 * @author Patrick Tessier
 * @since 3.0
 *
 */
public class SelectMessagesEditPartTracker extends SelectSeveralLinksEditPartTracker {
	public SelectMessagesEditPartTracker(ConnectionEditPart owner) {
		super(owner);
		setDisabledCursor(SharedCursors.NO);
	}

	protected Cursor calculateCursor() {
		if (isInState(STATE_TERMINAL))
			return null;
		Command command = getCurrentCommand();
		if (command == null || !command.canExecute())
			return getDisabledCursor();
		
		return super.calculateCursor();
	}

	@Override
	protected Command getCommand() {
		LocationRequest request = (LocationRequest)getSourceRequest();
		EditPart editPart = getSourceEditPart();
		Point p = getLocation();
		request.setLocation(p);
		request.getExtendedData().put(InteractionGraphSelectionCommandProxy.START_LOCATION, getStartLocation());
		selectionCommandProxy.setRequest(request, editPart);
		// Check if command is null. 
		Command cmd = selectionCommandProxy.getMultiSelectionCommand();
		if (cmd != null)
			return cmd;
		return super.getCommand();
	}
	
	protected Request getSourceRequest() {
		if (sourceRequest == null)
			sourceRequest = createSourceRequest();
		return sourceRequest;		
	}
	
	private Request sourceRequest;
	private InteractionGraphSelectionCommandProxy selectionCommandProxy = new InteractionGraphSelectionCommandProxy();
}
