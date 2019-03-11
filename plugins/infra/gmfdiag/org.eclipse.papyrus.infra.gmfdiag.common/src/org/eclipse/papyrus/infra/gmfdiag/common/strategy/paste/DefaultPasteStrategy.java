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
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.papyrus.commands.wrappers.GMFtoGEFCommandWrapper;
import org.eclipse.papyrus.infra.core.clipboard.PapyrusClipboard;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.DefaultDiagramPasteCommand;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.DefaultPasteCommand;

/**
 * The Class DefaultPasteStrategy.
 */
public class DefaultPasteStrategy extends AbstractPasteStrategy implements IPasteStrategy {


	/** The instance. */
	private static IPasteStrategy instance = new DefaultPasteStrategy();

	/**
	 * Gets the single instance of DefaultPasteStrategy.
	 *
	 * @return single instance of DefaultPasteStrategy
	 */
	public static IPasteStrategy getInstance() {
		return instance;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#getLabel()
	 */
	@Override
	public String getLabel() {
		return "DefaultPasteStrategy"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#getID()
	 */
	@Override
	public String getID() {
		return Activator.ID + ".DefaultPasteStrategy"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Default Paste Strategy"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#getPriority()
	 */
	@Override
	public int getPriority() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#dependsOn()
	 */
	@Override
	public IPasteStrategy dependsOn() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#getSemanticCommand(org.eclipse.emf.edit.domain.EditingDomain, org.eclipse.emf.ecore.EObject, org.eclipse.papyrus.infra.core.clipboard.PapyrusClipboard)
	 */
	@Override
	public org.eclipse.emf.common.command.Command getSemanticCommand(EditingDomain domain, EObject targetOwner, PapyrusClipboard<Object> papyrusClipboard) {
		return new DefaultPasteCommand(domain, targetOwner, papyrusClipboard);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#getGraphicalCommand(org.eclipse.emf.edit.domain.EditingDomain, org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart,
	 * org.eclipse.papyrus.infra.core.clipboard.PapyrusClipboard)
	 */
	@Override
	public Command getGraphicalCommand(EditingDomain domain, org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart targetEditPart, PapyrusClipboard<Object> papyrusClipboard) {
		CompoundCommand compoundCommand = new CompoundCommand("Semantic And Graphical paste"); //$NON-NLS-1$
		// (View)targetEditPart.getModel()
		DefaultDiagramPasteCommand defaultDiagramPasteCommand = new DefaultDiagramPasteCommand(targetEditPart.getEditingDomain(), "DefaultDiagramPasteCommand", papyrusClipboard, targetEditPart); //$NON-NLS-1$
		GMFtoGEFCommandWrapper gmFtoGEFCommandWrapper = new GMFtoGEFCommandWrapper(defaultDiagramPasteCommand);
		compoundCommand.add(gmFtoGEFCommandWrapper);
		return compoundCommand;
	}


}
