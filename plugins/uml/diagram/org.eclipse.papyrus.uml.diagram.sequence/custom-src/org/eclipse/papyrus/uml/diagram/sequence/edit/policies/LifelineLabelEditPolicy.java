/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
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
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.AbstractMaskManagedEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.util.LifelineLabelHelper;
import org.eclipse.uml2.uml.ConnectableElement;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.ValueSpecification;

public class LifelineLabelEditPolicy extends AbstractMaskManagedEditPolicy {

	@Override
	public void addAdditionalListeners() {
		super.addAdditionalListeners();

		Lifeline lifeline = getUMLElement();
		// check host semantic element is not null
		if (lifeline == null) {
			return;
		}
		// adds a listener to the element itself, and to linked elements, like Type
		getDiagramEventBroker().addNotificationListener(lifeline, this);
		ConnectableElement ce = lifeline.getRepresents();
		if (ce != null) {
			getDiagramEventBroker().addNotificationListener(ce, this);
			if (ce.getType() != null) {
				getDiagramEventBroker().addNotificationListener(ce.getType(), this);
			}
		}
		ValueSpecification selector = lifeline.getSelector();
		if (selector != null) {
			getDiagramEventBroker().addNotificationListener(selector, this);
		}
	}

	@Override
	protected void removeAdditionalListeners() {
		super.removeAdditionalListeners();
		Lifeline lifeline = getUMLElement();
		// check host semantic element is not null
		if (lifeline == null) {
			return;
		}
		getDiagramEventBroker().removeNotificationListener(lifeline, this);
		ConnectableElement ce = lifeline.getRepresents();
		if (ce != null) {
			getDiagramEventBroker().removeNotificationListener(ce, this);
			if (ce.getType() != null) {
				getDiagramEventBroker().removeNotificationListener(ce.getType(), this);
			}
		}
		ValueSpecification selector = lifeline.getSelector();
		if (selector != null) {
			getDiagramEventBroker().removeNotificationListener(selector, this);
		}
	}

	@Override
	public void notifyChanged(Notification notification) {
		super.notifyChanged(notification);
		Object object = notification.getNotifier();
		if (object == null || getUMLElement() == null) {
			return;
		}
		if (notification.getFeature() != null) {
			if (notification.getFeature().equals(UMLPackage.eINSTANCE.getNamedElement_Name())) {
				refreshDisplay();
			} else if (notification.getFeature().equals(UMLPackage.Literals.LIFELINE__REPRESENTS)) {
				// change represent element
				if (notification.getNewValue() instanceof ConnectableElement) {
					ConnectableElement ce = (ConnectableElement) notification.getNewValue();
					getDiagramEventBroker().addNotificationListener(ce, this);
					if (ce.getType() != null) {
						getDiagramEventBroker().addNotificationListener(ce.getType(), this);
					}
				}
				if (notification.getOldValue() instanceof ConnectableElement) {
					ConnectableElement ce = (ConnectableElement) notification.getOldValue();
					getDiagramEventBroker().removeNotificationListener(ce, this);
					if (ce.getType() != null) {
						getDiagramEventBroker().removeNotificationListener(ce.getType(), this);
					}
				}
				refreshDisplay();

			} else if (isMaskManagedAnnotation(object) || isRemovedMaskManagedLabelAnnotation(object, notification)) {
				refreshDisplay();
			} else if (object.equals(getUMLElement().getRepresents())) {
				// change represent type
				if (notification.getNewValue() instanceof Type && notification.getNewValue() instanceof EObject) {
					getDiagramEventBroker().addNotificationListener((EObject) notification.getNewValue(), this);
				}
				if (notification.getOldValue() instanceof Type && notification.getOldValue() instanceof EObject) {
					getDiagramEventBroker().removeNotificationListener((EObject) notification.getOldValue(), this);
				}
				refreshDisplay();
			} else if (object.equals(getUMLElement().getSelector())
					|| notification.getFeature().equals(UMLPackage.Literals.LIFELINE__SELECTOR)) {
				// change selector
				if (notification.getNewValue() instanceof ValueSpecification) {
					getDiagramEventBroker().addNotificationListener((ValueSpecification) notification.getNewValue(), this);
				}
				if (notification.getOldValue() instanceof ValueSpecification) {
					getDiagramEventBroker().removeNotificationListener((ValueSpecification) notification.getOldValue(), this);
				}
				refreshDisplay();
			}
		}
	}

	@Override
	public void refreshDisplay() {
		// calls the helper for this edit Part
		LifelineEditPart lp = (LifelineEditPart) getHost();
		List children = lp.getChildren();
		for (Object p : children) {
			if (p instanceof LifelineNameEditPart) {
				LifelineLabelHelper.getInstance().refreshEditPartDisplay((GraphicalEditPart) p);
			}
		}
	}

	@Override
	public Collection<String> getDefaultDisplayValue() {
		return LifelineLabelHelper.DEFAULT_LABEL_DISPLAY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getMasks() {
		return LifelineLabelHelper.getInstance().getMasks();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Lifeline getUMLElement() {
		return (Lifeline) hostSemanticElement;
	}
}
