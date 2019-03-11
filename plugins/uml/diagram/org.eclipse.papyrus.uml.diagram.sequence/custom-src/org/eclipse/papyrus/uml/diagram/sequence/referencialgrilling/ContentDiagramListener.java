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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;

/**
 *this calss is to delegate all notification in a diagram to the grill system in order to ensure consistency of rows and columns
 *
 */
public class ContentDiagramListener extends EContentAdapter {

	protected GridManagementEditPolicy grillingManagementEditPolicy;
	/**
	 * Constructor.
	 *
	 */
	public ContentDiagramListener(GridManagementEditPolicy grillingManagementEditPolicy) {
		this.grillingManagementEditPolicy= grillingManagementEditPolicy;
	}
	/**
	 * @see org.eclipse.emf.ecore.util.EContentAdapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 *
	 * @param notification
	 */
	@Override
	public void notifyChanged(Notification notification) {
		super.notifyChanged(notification);
		grillingManagementEditPolicy.notifyChanged(notification);
	}
}
