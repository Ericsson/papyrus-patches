/*****************************************************************************
 * Copyright (c) 2011 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *		CEA LIST - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.commands;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.diagram.core.services.ViewService;
import org.eclipse.gmf.runtime.diagram.ui.commands.CreateCommand;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest.ViewDescriptor;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;

/**
 * A replacement for CreateCommand that avoids that takes into account the incorrect
 * generation of ViewProvider by GMFTooling and modifies SemanticAdapter in call to {@link ViewService#provides(Class, org.eclipse.core.runtime.IAdaptable, View, String, int, boolean, org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint)} .
 */
public class CreateViewCommand extends CreateCommand {

	/** Constructor */
	public CreateViewCommand(TransactionalEditingDomain editingDomain, ViewDescriptor viewDescriptor, View containerView) {
		super(editingDomain, viewDescriptor, containerView);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canExecute() {
		IAdaptable semanticAdapter = null;

		if (viewDescriptor.getElementAdapter() != null) {
			// Try to adapt the descriptor ElementAdapter in EObject
			EObject element = EMFHelper.getEObject(viewDescriptor.getElementAdapter());
			IElementType elementType = (IElementType) viewDescriptor.getElementAdapter().getAdapter(IElementType.class);
			semanticAdapter = new SemanticElementAdapter(element, elementType);
			// Use the semanticAdapter instead of view descriptor element adapter to avoid the use of provides(ViewForKind) method
			// from ViewProvider which is incorrectly implemented in GMF Tooling generated editors (other editors may have undesired side-effect on each-other).
		}

		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=450921
		// it should be still possible to create elements without semantic if they have a view provider
		// (like oval or note)
		return ViewService.getInstance().provides(viewDescriptor.getViewKind(), semanticAdapter, containerView, viewDescriptor.getSemanticHint(), viewDescriptor.getIndex(), viewDescriptor.isPersisted(), viewDescriptor.getPreferencesHint());
	}



}
