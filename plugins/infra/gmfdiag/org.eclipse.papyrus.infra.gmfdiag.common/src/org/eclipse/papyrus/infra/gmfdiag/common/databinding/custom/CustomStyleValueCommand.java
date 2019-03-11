/*****************************************************************************
 * Copyright (c) 2012 CEA LIST.
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
package org.eclipse.papyrus.infra.gmfdiag.common.databinding.custom;

import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gmf.runtime.notation.NamedStyle;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.View;

public class CustomStyleValueCommand extends AbstractCommand {

	private boolean createStyle;

	private Object value;

	private Object oldValue;

	private EClass styleClass;

	private View view;

	private String styleName;

	private EStructuralFeature styleFeature;

	public CustomStyleValueCommand(View view, Object value, EClass styleClass, EStructuralFeature styleFeature, String styleName) {
		this.value = value;
		this.styleClass = styleClass;
		this.view = view;
		this.styleName = styleName;
		this.styleFeature = styleFeature;
	}

	@Override
	public void execute() {
		NamedStyle valueStyle = view.getNamedStyle(styleClass, styleName);
		createStyle = valueStyle == null;
		if (createStyle) {
			valueStyle = createStyle();
		} else {
			oldValue = valueStyle.eGet(styleFeature);
		}

		valueStyle.eSet(styleFeature, value);
		createStyle = valueStyle.eContainer() == null; // if CSS valueStyle is set but no Style on notation. Set the createStyle to remove it at the undo.
		if (createStyle) {
			view.getStyles().add(valueStyle);
		}
	}

	protected NamedStyle createStyle() {
		NamedStyle result = (NamedStyle) NotationFactory.eINSTANCE.create(styleClass);
		result.setName(styleName);
		return result;
	}

	@Override
	public void redo() {
		execute();
	}

	@Override
	public void undo() {
		NamedStyle valueStyle = view.getNamedStyle(styleClass, styleName);

		if (createStyle) {
			view.getStyles().remove(valueStyle);
		} else {
			valueStyle.eSet(styleFeature, oldValue);
		}
	}

	@Override
	public boolean prepare() {
		return true;
	}
}
