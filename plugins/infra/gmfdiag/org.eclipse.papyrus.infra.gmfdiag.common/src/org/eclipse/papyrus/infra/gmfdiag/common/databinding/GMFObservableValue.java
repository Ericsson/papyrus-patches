/*****************************************************************************
 * Copyright (c) 2010, 2016 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Camille Letavernier (CEA LIST) camille.letavernier@cea.fr - Initial API and implementation
 *  Thibault Le Ouay t.leouay@sherpa-eng.com - Add binding implementation
 *  Christian W. Damus (CEA) - bugs 440108, 417409
 *  Gabriel Pascual (ALL4TEC) gabriel.pascual@all4tec.net - bug 447698
 *  Christian W. Damus - bug 485220
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.databinding;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.papyrus.infra.emf.gmf.command.GMFtoEMFCommandWrapper;
import org.eclipse.papyrus.infra.services.edit.ui.databinding.PapyrusObservableValue;

/**
 * An ObservableValue used to edit EObject properties through
 * Papyrus commands
 *
 * @author Camille Letavernier
 *
 */
public class GMFObservableValue extends PapyrusObservableValue {

	/**
	 *
	 * Constructor.
	 *
	 * @param eObject
	 *            The EObject to edit
	 * @param eStructuralFeature
	 *            The structural feature to edit
	 * @param domain
	 *            The editing domain on which the commands will be executed
	 */
	public GMFObservableValue(EObject eObject, EStructuralFeature eStructuralFeature, EditingDomain domain) {
		super(eObject, eStructuralFeature, domain, GMFtoEMFCommandWrapper::wrap);
	}

	/**
	 *
	 * Constructor.
	 *
	 * @param realm
	 * @param eObject
	 *            The EObject to edit
	 * @param eStructuralFeature
	 *            The structural feature to edit
	 * @param domain
	 *            The editing domain on which the commands will be executed
	 */
	public GMFObservableValue(Realm realm, EObject eObject, EStructuralFeature eStructuralFeature, EditingDomain domain) {
		super(realm, eObject, eStructuralFeature, domain, GMFtoEMFCommandWrapper::wrap);
	}
}
