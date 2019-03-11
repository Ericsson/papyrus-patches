/*****************************************************************************
 * Copyright (c) 2014-2017 CEA LIST and others.
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
 *   Alex Paperno - bug 444956
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 519408
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gef.requests.TargetRequest;
import org.eclipse.gmf.runtime.common.core.util.StringStatics;
import org.eclipse.gmf.runtime.diagram.core.commands.SetConnectionEndsCommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DestructionOccurrenceSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageDeleteEditPart;

/**
 * @since 3.1
 */
public class LifelineMessageDeleteHelper {

	/*
	 * Set edge.target to the target lifeline
	 */
	public static Command getSetEdgeTargetCommand(TargetRequest request, TransactionalEditingDomain editingDomain) {
		if (request instanceof ReconnectRequest) {
			ReconnectRequest reconnectRequest = (ReconnectRequest) request;
			EditPart targetEP = reconnectRequest.getTarget().getParent();
			SetConnectionEndsCommand sceCommand = new SetConnectionEndsCommand(editingDomain, StringStatics.BLANK);
			sceCommand.setEdgeAdaptor(new EObjectAdapter((EObject) reconnectRequest.getConnectionEditPart().getModel()));
			sceCommand.setNewTargetAdaptor(targetEP);
			return new ICommandProxy(sceCommand);
		} else if (request instanceof CreateConnectionViewAndElementRequest) {
			CreateConnectionViewAndElementRequest createRequest = (CreateConnectionViewAndElementRequest) request;
			EditPart targetEP = createRequest.getTargetEditPart();
			if (targetEP instanceof DestructionOccurrenceSpecificationEditPart) {
				targetEP = targetEP.getParent();
			}
			SetConnectionEndsCommand sceCommand = new SetConnectionEndsCommand(editingDomain, StringStatics.BLANK);
			sceCommand.setEdgeAdaptor(createRequest.getConnectionViewDescriptor());
			sceCommand.setNewTargetAdaptor(targetEP);
			return new ICommandProxy(sceCommand);
		}
		return null;
	}

	/**
	 * @return true if a Message Delete exist between the source and the target.
	 * @since 4.0
	 */
	public static boolean hasMessageDelete(final GraphicalEditPart sourceEditPart, final EditPart targetEditPart) {
		List<?> list = sourceEditPart.getSourceConnections();
		for (Object o : list) {
			if (o instanceof MessageDeleteEditPart && targetEditPart.equals(((MessageDeleteEditPart) o).getTarget())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return true if edit part as has incoming message delete.
	 * @since 4.0
	 */
	public static boolean hasIncomingMessageDelete(final EditPart target) {
		return !getIncomingMessageDelete(target).isEmpty();
	}

	/**
	 * Get the list of incoming message delete of an edit part.
	 *
	 * @since 4.0
	 */
	public static List<?> getIncomingMessageDelete(final EditPart target) {
		List<EditPart> create = new ArrayList<>();
		if (target instanceof LifelineEditPart) {
			List<?> list = ((LifelineEditPart) target).getTargetConnections();
			if (list != null && list.size() > 0) {
				for (Object l : list) {
					if (l instanceof MessageDeleteEditPart) {
						create.add((MessageDeleteEditPart) l);
					}
				}
			}
		}
		return create;
	}
}
