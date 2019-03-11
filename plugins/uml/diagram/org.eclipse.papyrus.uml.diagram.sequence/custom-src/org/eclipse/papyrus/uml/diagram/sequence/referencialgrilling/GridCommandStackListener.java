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
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import java.util.EventObject;

import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.notify.Notification;

/**
 * @author Patrick Tessier
 *
 */
public class GridCommandStackListener implements CommandStackListener {

	protected GridManagementEditPolicy gridManagementEditPolicy;
	/**
	 * Constructor.
	 *
	 */
	public GridCommandStackListener(GridManagementEditPolicy gridManagementEditPolicy) {
		this.gridManagementEditPolicy= gridManagementEditPolicy;
	}
	/**
	 * @see org.eclipse.emf.common.command.CommandStackListener#commandStackChanged(java.util.EventObject)
	 *
	 * @param event
	 */
	@Override
	public void commandStackChanged(EventObject event) {
		gridManagementEditPolicy.notifyChanged(new CommandExecutionNotification(Notification.ADD, true,true));
	}

}
