/*****************************************************************************
 * Copyright (c) 2014, 2015 CEA LIST, Christian W. Damus, and others.
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
 *   Christian W. Damus - bug 465416
 *
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.sync;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.Size;
import org.eclipse.papyrus.infra.sync.EMFDispatch;
import org.eclipse.papyrus.infra.sync.SyncItem;

/**
 * Represents a listener for the size of GMF notation node
 *
 * @author Laurent Wouters
 */
public abstract class NodeSizeSyncDispatcher<M extends EObject, T extends EditPart> extends EMFDispatch {
	/**
	 * The item being listened to
	 */
	protected SyncItem<M, T> item;
	/**
	 * The current width
	 */
	protected int currentWidth;
	/**
	 * The current height
	 */
	protected int currentHeight;

	/**
	 * Initializes this listener
	 *
	 * @param item
	 *            The item being listened to
	 */
	public NodeSizeSyncDispatcher(SyncItem<M, T> item) {
		this.item = item;
		Size size = (Size) ((Node) item.getBackend().getModel()).getLayoutConstraint();
		currentWidth = size.getWidth();
		currentHeight = size.getHeight();
	}

	/**
	 * Gets the item being listened to
	 *
	 * @return The item being listened to
	 */
	public SyncItem<M, T> getItem() {
		return item;
	}

	@Override
	public EObject getNotifier() {
		Node node = (Node) item.getBackend().getModel();
		return node.getLayoutConstraint();
	}

	@Override
	public EStructuralFeature getFeature() {
		// get notification of all changes on the layout constraint
		return null;
	}

	@Override
	public void onChange(Notification notification) {
		Object feature = notification.getFeature();
		if (feature == NotationPackage.Literals.SIZE__WIDTH || feature == NotationPackage.Literals.SIZE__HEIGHT) {
			Object lc = notification.getNotifier();
			Size size = (Size) lc;
			int width = size.getWidth();
			int height = size.getHeight();
			if (currentWidth != width || currentHeight != height) {
				onFilteredChange(notification);
				currentWidth = width;
				currentHeight = height;
			}
		}
	}

	/**
	 * Reacts to the specified notification that has been verified to be of interest
	 *
	 * @param notification
	 */
	protected abstract void onFilteredChange(Notification notification);
}
