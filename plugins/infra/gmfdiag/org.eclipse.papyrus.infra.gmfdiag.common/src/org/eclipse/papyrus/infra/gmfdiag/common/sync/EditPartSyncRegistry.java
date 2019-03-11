/*****************************************************************************
 * Copyright (c) 2015 Christian W. Damus and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.sync;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.sync.SyncRegistry;
import org.eclipse.papyrus.infra.tools.util.TypeUtils;

/**
 * A synchronization registry for synchronization of the {@link EditPart}s visualizing model elements in GMF diagrams.
 */
public class EditPartSyncRegistry<M extends EObject, T extends EditPart> extends SyncRegistry<M, T, Notification> {

	public EditPartSyncRegistry() {
		super();

		initializeSyncPolicyDelegates();
	}

	private void initializeSyncPolicyDelegates() {
		NodePositionSyncFeature.createPolicyDelegate().register(NodePositionSyncFeature.class);
		NodeSizeSyncFeature.createPolicyDelegate().register(NodeSizeSyncFeature.class);
	}

	@Override
	public M getModelOf(EditPart backend) {
		View view = TypeUtils.as(backend.getModel(), View.class);
		return (view == null) ? null : TypeUtils.as(view.getElement(), getModelType());
	}

}
