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

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.notation.View;


public class AddCustomStyleListValueCommand extends AbstractCustomStyleListValueCommand {

	protected int index = CommandParameter.NO_INDEX;

	protected Object value;

	public AddCustomStyleListValueCommand(EditingDomain domain, View view, String styleName, EClass styleClass, EStructuralFeature styleFeature, Object value, int index) {
		super(domain, view, styleName, styleClass, styleFeature);
		this.index = index;
		this.value = value;
	}

	public AddCustomStyleListValueCommand(EditingDomain domain, View view, String styleName, EClass styleClass, EStructuralFeature styleFeature, Object value) {
		this(domain, view, styleName, styleClass, styleFeature, value, CommandParameter.NO_INDEX);
	}

	@Override
	protected Command createCommand() {
		return AddCommand.create(domain, style, styleFeature, value, index);
	}

}
