/*****************************************************************************
 * Copyright (c) 2014 CEA LIST.
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
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.helper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.notation.NamedStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.emf.appearance.helper.VisualInformationPapyrusConstants;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.databinding.custom.CustomStringStyleObservableList;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationUtils;


public class MaskLabelHelper {


	/**
	 * Return the current mask values, or null if undefined
	 *
	 * @param view
	 * @return
	 */
	public static Collection<String> getMaskValues(View view) {
		EList<String> maskLabel = NotationUtils.getStringListValue(view, VisualInformationPapyrusConstants.CUSTOM_MASK_LABEL, null);
		if (maskLabel != null) {
			CustomStringStyleObservableList values = new CustomStringStyleObservableList(view, EMFHelper.resolveEditingDomain(view), VisualInformationPapyrusConstants.CUSTOM_MASK_LABEL);
			Set<String> result = new HashSet<String>(values);
			values.dispose();
			return result;
		}
		return null;
	}

	public static void setMaskValues(View view, Collection<String> masks) {
		CustomStringStyleObservableList values = new CustomStringStyleObservableList(view, EMFHelper.resolveEditingDomain(view), VisualInformationPapyrusConstants.CUSTOM_MASK_LABEL);
		if (!values.isEmpty()) {
			values.clear();
		}
		values.addAll(masks);
		values.commit(null);
		values.dispose();
	}

	public static void unsetMaskValues(final View view) {
		final NamedStyle style = view.getNamedStyle(NotationPackage.eINSTANCE.getStringListValueStyle(), VisualInformationPapyrusConstants.CUSTOM_MASK_LABEL);
		if (style == null) {
			return;
		}

		TransactionalEditingDomain editingDomain = (TransactionalEditingDomain) EMFHelper.resolveEditingDomain(view);
		editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain, "Restore default label ") {

			@Override
			protected void doExecute() {
				view.getStyles().remove(style);
			}
		});
	}

}
