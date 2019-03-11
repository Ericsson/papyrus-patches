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
 *  Benoit Maggi (CEA LIST) benoit.maggi@cea.fr - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste;

import java.util.Collection;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.commands.wrappers.EMFtoGEFCommandWrapper;
import org.eclipse.papyrus.infra.core.clipboard.PapyrusClipboard;
import org.eclipse.swt.graphics.Image;


/**
 * This class offer a default strategy for the graphical paste :
 * use the semantic command
 */
public abstract class AbstractPasteStrategy implements IPasteStrategy {


	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#getLabel()
	 */
	@Override
	public abstract String getLabel();

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#getDescription()
	 */
	@Override
	public abstract String getDescription();

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#getID()
	 */
	@Override
	public abstract String getID();

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#dependsOn()
	 */
	@Override
	public abstract IPasteStrategy dependsOn();



	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#getSemanticCommand(org.eclipse.emf.edit.domain.EditingDomain, org.eclipse.emf.ecore.EObject, org.eclipse.papyrus.infra.core.clipboard.PapyrusClipboard)
	 */
	@Override
	public abstract Command getSemanticCommand(EditingDomain domain, EObject targetOwner, PapyrusClipboard<Object> papyrusClipboard);



	/*
	 * (non-Javadoc)
	 * By default, if there is no specific graphical command use the semantic one
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#getGraphicalCommand(org.eclipse.emf.edit.domain.EditingDomain, org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart,
	 * org.eclipse.papyrus.infra.core.clipboard.PapyrusClipboard)
	 */
	@Override
	public org.eclipse.gef.commands.Command getGraphicalCommand(EditingDomain domain, GraphicalEditPart targetEditPart, PapyrusClipboard<Object> papyrusClipboard) {
		if (targetEditPart != null) {
			Object model = targetEditPart.getModel();
			if (model instanceof View) {
				View view = (View) model;
				EObject targetOwner = view.getElement();
				Command semanticCommand = this.getSemanticCommand(domain, targetOwner, papyrusClipboard);
				if (semanticCommand != null) {
					return new EMFtoGEFCommandWrapper(semanticCommand);
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#prepare(org.eclipse.papyrus.infra.core.clipboard.PapyrusClipboard, java.util.List)
	 */
	@Override
	public void prepare(PapyrusClipboard<Object> papyrusClipboard, Collection<EObject> selection) {
		// by default nothing to prepare
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#getImage()
	 */
	@Override
	public Image getImage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#getPriority()
	 */
	@Override
	public int getPriority() {
		return 1;
	}


}
