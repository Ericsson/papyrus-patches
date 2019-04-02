/*****************************************************************************
 * (c) Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.gmf.runtime.emf.type.core.commands.CreateElementCommand;
import org.eclipse.gmf.runtime.emf.type.core.internal.EMFTypePlugin;
import org.eclipse.gmf.runtime.emf.type.core.internal.l10n.EMFTypeCoreMessages;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;

/**
 * @author ETXACAM
 *
 */
public class CreateNodeElementCommand extends CreateElementCommand {
	public CreateNodeElementCommand(CreateElementRequest request, EObject element) {
		this(request, element, -1);
	}

	public CreateNodeElementCommand(CreateElementRequest request, EObject element, int index) {
		super(request);
		this.element = element;
		this.index = index;
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "restriction" })
	@Override
	protected EObject doDefaultElementCreation() {
		EObject result = null;
		EReference containment = getContainmentFeature();

		if (containment != null) {
			EObject container = getElementToEdit();

			if (container != null) {
				if (FeatureMapUtil.isMany(container, containment)) {
					Collection col = (Collection) container.eGet(containment);
					if (index != -1 && col instanceof List) {
						((List) col).add(index, element);
					} else {
						col.add(element);
					}
				} else {
					container.eSet(containment, element);
				}
				result = element;
			}
		}

		IStatus status = (result != null) ? Status.OK_STATUS
				: new Status(
						Status.ERROR,
						EMFTypePlugin.getPluginId(),
						EMFTypeCoreMessages
								.bind(
										EMFTypeCoreMessages.createElementCommand_noElementCreated,
										getElementType().getDisplayName()));

		setDefaultElementCreationStatus(status);

		return result;
	}

	private EObject element;
	private int index;
}
