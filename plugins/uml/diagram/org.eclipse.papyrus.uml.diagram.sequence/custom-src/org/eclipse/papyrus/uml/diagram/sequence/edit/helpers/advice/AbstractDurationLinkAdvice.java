/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.helpers.advice;

import java.util.Collection;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
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
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.SequenceDiagramEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.util.DurationLinkUtil;

/**
 * <p>
 * Abstract sequence diagram advice to clear DurationLinks when the corresponding
 * semantic Duration element becomes inconsistent (typically, the source/target changes
 * or is removed).
 * </p>
 */
public abstract class AbstractDurationLinkAdvice extends AbstractEditHelperAdvice {

	private EClass durationElementType;
	private EReference eventsReference;
	private String durationLinkType;

	protected AbstractDurationLinkAdvice(EClass durationElementType, EReference eventsReference, String durationLinkType) {
		this.durationElementType = durationElementType;
		this.eventsReference = eventsReference;
		this.durationLinkType = durationLinkType;
	}

	@Override
	protected ICommand getAfterSetCommand(SetRequest request) {
		if (request.getFeature() == eventsReference && durationElementType.isInstance(request.getElementToEdit())) {
			// We need to delegate to the command provider; otherwise the view is not correctly destroyed,
			// and the diagram may still display a ghost connection (referencing a view that is no longer
			// attached to the notation model)
			IElementEditService provider = ElementEditServiceUtils.getCommandProvider(request.getElementToEdit());
			if (provider == null) {
				return null;
			}

			Collection<Setting> usages = EMFHelper.getUsages(request.getElementToEdit());

			CompositeCommand deletions = new CompositeCommand("Delete inconsistent DurationLink views"); //$NON-NLS-1$
			for (Setting usage : usages) {
				if (usage.getEObject() instanceof Connector && usage.getEStructuralFeature() == NotationPackage.Literals.VIEW__ELEMENT) {
					Connector connector = (Connector) usage.getEObject();
					if (durationLinkType.equals(connector.getType()) //
							&& connector.getDiagram() != null //
							&& SequenceDiagramEditPart.MODEL_ID.equals(connector.getDiagram().getType())) {
						if (!DurationLinkUtil.isConsistent(connector, request)) {
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
