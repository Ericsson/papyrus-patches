/*****************************************************************************
 * Copyright (c)  2018 CEA LIST, EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		EclipseSource - Bug 537562
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.helpers.advice;


import java.util.Collection;

import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.type.core.edithelper.AbstractEditHelperAdvice;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.SetRequest;
import org.eclipse.gmf.runtime.notation.Connector;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.services.edit.service.ElementEditServiceUtils;
import org.eclipse.papyrus.infra.services.edit.service.IElementEditService;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.GeneralOrderingEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.SequenceDiagramEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.util.GeneralOrderingUtil;
import org.eclipse.uml2.uml.GeneralOrdering;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * <p>
 * A sequence diagram advice to clear {@link GeneralOrderingEditPart GeneralOrdering links} when the
 * orderings's before or after events are changed.
 * </p>
 */
public class GeneralOrderingLinkHelperAdvice extends AbstractEditHelperAdvice {

	@Override
	protected ICommand getAfterSetCommand(SetRequest request) {
		if ((request.getFeature() == UMLPackage.Literals.GENERAL_ORDERING__AFTER
				|| request.getFeature() == UMLPackage.Literals.GENERAL_ORDERING__BEFORE)
				&& request.getElementToEdit() instanceof GeneralOrdering) {
			Collection<Setting> usages = EMFHelper.getUsages(request.getElementToEdit());

			// We need to delegate to the command provider; otherwise the view is not correctly destroyed,
			// and the diagram may still display a ghost connection (referencing a view that is no longer
			// attached to the notation model)
			IElementEditService provider = ElementEditServiceUtils.getCommandProvider(request.getElementToEdit());
			if (provider == null) {
				return null;
			}

			CompositeCommand deletions = new CompositeCommand("Delete inconsistent GeneralOrdering views");
			for (Setting usage : usages) {
				if (usage.getEObject() instanceof Connector && usage.getEStructuralFeature() == NotationPackage.Literals.VIEW__ELEMENT) {
					Connector connector = (Connector) usage.getEObject();
					if (GeneralOrderingEditPart.VISUAL_ID.equals(connector.getType()) //
							&& connector.getDiagram() != null //
							&& SequenceDiagramEditPart.MODEL_ID.equals(connector.getDiagram().getType())) {
						if (!GeneralOrderingUtil.isConsistent(connector, request)) {
							// Retrieve delete command from the Element Edit service
							DestroyElementRequest destroyRequest = new DestroyElementRequest(request.getEditingDomain(), connector, false);
							ICommand deleteCommand = provider.getEditCommand(destroyRequest);
							deletions.add(deleteCommand);
						}
					}
				}
			}
			return deletions.isEmpty() ? null : deletions.reduce();
		}
		return super.getAfterSetCommand(request);
	}

}
