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
 * Contributors:
 *  CEA LIST - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.commands;

import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.notation.View;

public class SetNodeVisibilityCommand extends RecordingCommand {

	/** view to modify */
	protected View view;

	/** visibility to set */
	protected Boolean isVisible;

	public SetNodeVisibilityCommand(TransactionalEditingDomain domain, View view, Boolean isVisible) {
		super(domain, "Set Node Visibility");
		this.view = view;
		this.isVisible = isVisible;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doExecute() {
		if (null != view) {
			if (view.isVisible() != isVisible) {
				view.setVisible(isVisible);
			}
		}
	}


}
