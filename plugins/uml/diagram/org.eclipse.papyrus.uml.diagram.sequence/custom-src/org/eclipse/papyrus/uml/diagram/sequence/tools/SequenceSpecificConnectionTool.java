/*****************************************************************************
 * Copyright (c) 2017 CEA LIST, ALL4TEC and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Micka�l ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.tools;

import java.util.List;

import org.eclipse.draw2d.Cursors;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.papyrus.infra.gmfdiag.common.service.palette.AspectUnspecifiedTypeConnectionTool;
import org.eclipse.papyrus.uml.diagram.sequence.util.TooltipHook;

/**
 * Specific connection tool for sequence diagram.
 */
public class SequenceSpecificConnectionTool extends AspectUnspecifiedTypeConnectionTool {

	private EditPart source;

	private TooltipHook tooltipHook = null;

	public SequenceSpecificConnectionTool(List<IElementType> elementTypes) {
		super(elementTypes);
		setDisabledCursor(Cursors.NO);
	}

	@Override
	public void setViewer(final EditPartViewer viewer) {
		super.setViewer(viewer);
		if (tooltipHook == null || !tooltipHook.isHooked(viewer)) {
			if (tooltipHook != null) {
				tooltipHook.dispose();
			}
			tooltipHook = new TooltipHook(viewer);
		}
	}

	@Override
	public void deactivate() {
		super.deactivate();
		if (tooltipHook != null) {
			tooltipHook.dispose();
			tooltipHook = null;
		}
	}

	@Override
	protected void setConnectionSource(final EditPart source) {
		this.source = source;
		super.setConnectionSource(source);
	}

	public void clearConnectionFeedback() {
		if (!isShowingSourceFeedback()) {
			return;
		}
		if (source != null) {
			Request req = getSourceRequest();
			req.setType(REQ_CONNECTION_END);
			source.eraseSourceFeedback(req);
		}
	}
}