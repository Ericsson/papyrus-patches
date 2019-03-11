/*****************************************************************************
 * Copyright (c) 2012 CEA LIST.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.commands;

import org.eclipse.gef.GraphicalEditPart;

/**
 * Refresh the {@link IFigure} of a given {@link GraphicalEditPart} on undo of this command. <br/>
 * <strong>This command must be created before the other ones in a CompoundCommand</strong> since the commands are
 * executed in reverse when undoing.
 * @since 3.0
 */
public class RefreshCommandForUndo extends AbstractRefreshCommand {

	public RefreshCommandForUndo(final GraphicalEditPart editPartToRefresh) {
		super(editPartToRefresh);
	}

	@Override
	public void undo() {
		refresh();
	}
}
