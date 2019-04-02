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
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.DiagramHelper;
import org.eclipse.swt.widgets.Display;

/**
 * Refresh the {@link IFigure} of a given {@link GraphicalEditPart}.
 * @since 3.0
 */
public abstract class AbstractRefreshCommand extends Command {

	private final GraphicalEditPart editPartToRefresh;

	private final RootEditPart rootEditPart;

	public AbstractRefreshCommand(final GraphicalEditPart editPartToRefresh) {
		this.editPartToRefresh = editPartToRefresh;
		this.rootEditPart = editPartToRefresh.getRoot();
	}

	public GraphicalEditPart getEditPartToRefresh() {
		return this.editPartToRefresh;
	}

	protected void refresh() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (AbstractRefreshCommand.this.editPartToRefresh.isActive()) {
					DiagramHelper.refresh(AbstractRefreshCommand.this.editPartToRefresh, true);
					// this is needed in order to update the bounds of the Figures from their layout manager constraints
					AbstractRefreshCommand.this.editPartToRefresh.getFigure().invalidateTree();
				} else if (AbstractRefreshCommand.this.rootEditPart.isActive()) {
					/*
					 * If the EditPart we needed to refresh is not active anymore, then it probably means that we undid its
					 * creation, and re-created the object as part of a redo, with a different EditPart. In this case, just
					 * refresh everything.
					 */
					DiagramHelper.refresh(AbstractRefreshCommand.this.rootEditPart, true);
				}
			}
		});
	}
}
