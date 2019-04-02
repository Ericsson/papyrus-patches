/*****************************************************************************
 * Copyright (c) 2013, 2014 CEA and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Soyatec - Initial API and implementation
 *   Christian W. Damus (CEA) - bug 417409
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.util;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.core.Activator;
import org.eclipse.papyrus.infra.emf.commands.CreateEAnnotationCommand;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.NotationHelper;
import org.eclipse.papyrus.infra.properties.contexts.DataContextElement;
import org.eclipse.papyrus.infra.properties.ui.modelelement.AnnotationModelElement;
import org.eclipse.papyrus.infra.properties.ui.modelelement.AnnotationModelElementFactory;
import org.eclipse.papyrus.infra.ui.emf.databinding.AnnotationObservableValue;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class GateModelElementFactory extends AnnotationModelElementFactory {

	private static final String GATE_SHOW_NAME = "showName";

	@Override
	protected AnnotationModelElement doCreateFromSource(Object sourceElement, DataContextElement context) {
		View source = NotationHelper.findView(sourceElement);
		if (source == null) {
			Activator.log.warn("Unable to resolve the selected element to an EObject"); //$NON-NLS-1$
			return null;
		}
		EditingDomain domain = EMFHelper.resolveEditingDomain(source);
		return new AnnotationModelElement(source, domain, GATE_SHOW_NAME) {

			@Override
			public IObservable doGetObservable(String propertyPath) {
				return new AnnotationObservableValue(source, domain, GATE_SHOW_NAME, GATE_SHOW_NAME) {

					@Override
					protected Command getCommand(final Object value) {
						return new CreateEAnnotationCommand((TransactionalEditingDomain) domain, source, GATE_SHOW_NAME) {

							@Override
							protected void doExecute() {
								EAnnotation annotation = createEAnnotation();
								replaceEannotation(annotation, getObject());
								replaceEntry(annotation, GATE_SHOW_NAME, value == null ? "" : value.toString());
							}
						};
					}

					@Override
					protected Object doGetValue() {
						Object value = super.doGetValue();
						if (value == null) {
							// By default, return true.
							return "true";
						}
						return value;
					}
				};
			}
		};
	}

	public static final boolean isShowNameChanged(Notification msg) {
		if (msg == null || !(msg.getNewValue() instanceof EAnnotation)) {
			return false;
		}
		EAnnotation anno = (EAnnotation) msg.getNewValue();
		return GATE_SHOW_NAME.equals(anno.getSource());
	}

	public static final boolean isShowName(View view) {
		if (view != null) {
			EAnnotation anno = view.getEAnnotation(GATE_SHOW_NAME);
			if (anno != null) {
				return !"false".equalsIgnoreCase(anno.getDetails().get(GATE_SHOW_NAME));
			}
		}
		return true;
	}
}
