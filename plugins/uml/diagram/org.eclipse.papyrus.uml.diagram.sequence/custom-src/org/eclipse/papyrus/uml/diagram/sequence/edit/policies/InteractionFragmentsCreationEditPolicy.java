/*****************************************************************************
 * Copyright (c) 2013 CEA
 *
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
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.core.command.UnexecutableCommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.PapyrusCreationEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.util.FragmentsOrdererHelper;
import org.eclipse.papyrus.uml.diagram.stereotype.edition.editpart.AppliedStereotypeCommentEditPart;

/**
 * Ordering fragments after creation.
 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=403233
 *
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class InteractionFragmentsCreationEditPolicy extends PapyrusCreationEditPolicy {

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.editpolicies.PapyrusCreationEditPolicy#getCreateElementAndViewCommand(org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest)
	 *
	 * @param request
	 * @return
	 */
	@Override
	protected Command getCreateElementAndViewCommand(CreateViewAndElementRequest request) {
		Command command = super.getCreateElementAndViewCommand(request);
		if (command != null && command.canExecute()) {
			ICommand orderingFragmentsCommand = FragmentsOrdererHelper.createOrderingFragmentsCommand(getHost(), request);
			if (orderingFragmentsCommand != null) {
				command = command.chain(new ICommandProxy(orderingFragmentsCommand));
			}
		}
		return command;
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.CreationEditPolicy#getReparentViewCommand(org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart)
	 *
	 * @param gep
	 * @return
	 */
	@Override
	protected ICommand getReparentViewCommand(IGraphicalEditPart gep) {
		if (gep instanceof AppliedStereotypeCommentEditPart) {
			return UnexecutableCommand.INSTANCE;
		}
		return super.getReparentViewCommand(gep);
	}
}
