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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.NotificationImpl;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener;

/**
 * this class is to delegate all notification in all trre of an eObject to a notofication listener
 * for example an editpolicy
 *
 * @since 4.0
 */
public class RedirectionCommandStackListener implements CommandStackEventListener {


	protected NotificationListener notificationListener;

	/**
	 * Constructor.
	 *
	 */
	public RedirectionCommandStackListener(NotificationListener notificationListener) {
		this.notificationListener = notificationListener;
	}



	/**
	 * @see org.eclipse.gef.commands.CommandStackEventListener#stackChanged(org.eclipse.gef.commands.CommandStackEvent)
	 *
	 * @param event
	 */
	@Override
	public void stackChanged(CommandStackEvent event) {
		notificationListener.notifyChanged(new NotificationImpl(Notification.SET, null, null));

	}
}
