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
 *   Patrick Tessier (CEA LIST) - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.util;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.NotificationImpl;
import org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener;

/**
 * this class is to delegate all of an operation History about done operation to for an example an editpolicy
 *
 * @since 4.0
 */
public class RedirectionOperationListener implements IOperationHistoryListener {


	protected NotificationListener notificationListener;

	/**
	 * Constructor.
	 *
	 */
	public RedirectionOperationListener(NotificationListener notificationListener) {
		this.notificationListener = notificationListener;
	}

	/**
	 * @see org.eclipse.core.commands.operations.IOperationHistoryListener#historyNotification(org.eclipse.core.commands.operations.OperationHistoryEvent)
	 *
	 * @param arg0
	 */
	@Override
	public void historyNotification(OperationHistoryEvent arg0) {
		if (arg0.getEventType() == OperationHistoryEvent.DONE) {
			notificationListener.notifyChanged(new NotificationImpl(Notification.SET, null, null));
		}
	}
}
