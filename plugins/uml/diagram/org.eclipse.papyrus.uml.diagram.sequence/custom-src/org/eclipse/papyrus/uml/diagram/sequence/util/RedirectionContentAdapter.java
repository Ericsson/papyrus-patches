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
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener;

/**
 * this class is to delegate all notification in all trre of an eObject to a notofication listener
 * for example an editpolicy
 *
 * @since 4.0
 */
public class RedirectionContentAdapter extends EContentAdapter {


	protected NotificationListener notificationListener;

	/**
	 * Constructor.
	 *
	 */
	public RedirectionContentAdapter(NotificationListener notificationListener) {
		this.notificationListener = notificationListener;
	}

	/**
	 * @see org.eclipse.emf.ecore.util.EContentAdapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 *
	 * @param notification
	 */
	@Override
	public void notifyChanged(Notification notification) {
		super.notifyChanged(notification);
		notificationListener.notifyChanged(notification);
	}
}
