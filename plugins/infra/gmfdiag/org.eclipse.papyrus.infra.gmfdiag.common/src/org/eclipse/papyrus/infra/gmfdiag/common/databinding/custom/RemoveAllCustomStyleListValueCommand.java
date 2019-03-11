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

import java.util.Collection;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.notation.View;


public class RemoveAllCustomStyleListValueCommand extends AbstractCustomStyleListValueCommand {

	protected Collection<?> values;

	public RemoveAllCustomStyleListValueCommand(EditingDomain domain, View view, String styleName, EClass eClass, EStructuralFeature feature, Collection<?> values) {
		super(domain, view, styleName, eClass, feature);
		this.values = values;
	}

	@Override
	protected Command createCommand() {
		return RemoveCommand.create(domain, style, styleFeature, values);
	}

}
