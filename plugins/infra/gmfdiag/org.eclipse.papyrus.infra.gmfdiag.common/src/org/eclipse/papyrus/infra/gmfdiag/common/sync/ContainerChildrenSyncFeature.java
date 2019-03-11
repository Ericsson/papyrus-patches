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

import java.util.Iterator;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeCompartmentEditPart;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.papyrus.infra.sync.SyncBucket;

import com.google.common.collect.Iterators;

/**
 * Represents a synchronization feature for the children of a GMF notation node
 *
 * @author Laurent Wouters
 *
 * @param <M>
 *            The type of the underlying model element common to all synchronized items in a single bucket
 * @param <N>
 *            The type of the model element visualized by the nested diagram views that I synchronize
 * @param <T>
 *            The type of the backend element to synchronize
 */
public abstract class ContainerChildrenSyncFeature<M extends EObject, N extends EObject, T extends EditPart> extends AbstractNestedDiagramViewsSyncFeature<M, N, T> {
	/**
	 * Initialized this feature
	 *
	 * @param bucket
	 *            The bucket doing the synchronization
	 */
	public ContainerChildrenSyncFeature(SyncBucket<M, T, Notification> bucket) {
		super(bucket, NotationPackage.Literals.VIEW__PERSISTED_CHILDREN, NotationPackage.Literals.VIEW__TRANSIENT_CHILDREN);
	}

	/**
	 * Gets the edit part that shall be observed and modified from the specified one
	 *
	 * @param parent
	 *            The edit part we work on
	 * @return The effective edit part that is observed and modified
	 */
	@Override
	protected EditPart getEffectiveEditPart(EditPart parent) {
		EditPart result = parent;

		// If the edit part includes a shape compartment, use that
		Iterator<ShapeCompartmentEditPart> shapeCompartments = Iterators.filter(parent.getChildren().iterator(), ShapeCompartmentEditPart.class);
		if (shapeCompartments.hasNext()) {
			result = shapeCompartments.next();
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	Iterable<? extends T> basicGetContents(T backend) {
		return getEffectiveEditPart(backend).getChildren();
	}
}
