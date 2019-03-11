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

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gmf.runtime.draw2d.ui.figures.BaseSlidableAnchor;
import org.eclipse.gmf.runtime.notation.Anchor;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractMessageEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageFoundEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageLostEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.figures.StereotypeInteractionFigure;
import org.eclipse.papyrus.uml.diagram.sequence.util.CommandHelper;


/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class InteractionHeadImpactLayoutEditPolicy extends AbstractHeadImpactLayoutEditPolicy {

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.policies.AbstractHeadImpactLayoutEditPolicy#getHeadHeight()
	 *
	 * @return
	 */
	@Override
	protected int getHeadHeight() {
		IFigure primaryShape = getPrimaryShape();
		if (primaryShape instanceof StereotypeInteractionFigure) {
			IFigure headContainer = ((StereotypeInteractionFigure) primaryShape).getNameLabel().getParent();
			Rectangle boundsRect = getBoundsRect();
			return headContainer.getPreferredSize(boundsRect.width, -1).height;
		}
		return 0;
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.policies.AbstractHeadImpactLayoutEditPolicy#doImpactLayout(int)
	 *
	 * @param resizeDelta
	 */

	@Override
	protected void doImpactLayout(int resizeDelta) {
		CompoundCommand commands = new CompoundCommand();
		// 2. move FoundMessage
		{
			List<?> sourceConnections = ((NodeEditPart) getHost()).getSourceConnections();
			for (Object object : sourceConnections) {
				if (object instanceof MessageFoundEditPart) {
					Edge edge = (Edge) ((MessageFoundEditPart) object).getNotationView();
					Anchor sourceAnchor = edge.getSourceAnchor();
					if (sourceAnchor instanceof IdentityAnchor) {
						String terminal = ((IdentityAnchor) sourceAnchor).getId();
						PrecisionPoint pt = BaseSlidableAnchor.parseTerminalString(terminal);
						if (pt.preciseY() > 1) {
							pt.translate(0, resizeDelta);
							commands.appendIfCanExecute(SetCommand.create(getEditingDomain(), sourceAnchor, NotationPackage.eINSTANCE.getIdentityAnchor_Id(), "(" + pt.preciseX() + "," + pt.preciseY() + ")"));
						}
					}
				}
			}
		}
		// 3. Move LostMessage
		{
			List<?> targetConnections = ((NodeEditPart) getHost()).getTargetConnections();
			for (Object object : targetConnections) {
				if (object instanceof MessageLostEditPart) {
					AbstractMessageEditPart message = (AbstractMessageEditPart) object;
					Edge edge = (Edge) message.getNotationView();
					Anchor targetAnchor = edge.getTargetAnchor();
					if (targetAnchor instanceof IdentityAnchor) {
						String terminal = ((IdentityAnchor) targetAnchor).getId();
						PrecisionPoint pt = BaseSlidableAnchor.parseTerminalString(terminal);
						if (pt.preciseY() > 1) {
							pt.translate(0, resizeDelta);
							commands.appendIfCanExecute(SetCommand.create(getEditingDomain(), targetAnchor, NotationPackage.eINSTANCE.getIdentityAnchor_Id(), "(" + pt.preciseX() + "," + pt.preciseY() + ")"));
						}
					}
				}
			}
		}
		if (commands.isEmpty() || !commands.canExecute()) {
			return;
		}
		CommandHelper.executeCommandWithoutHistory(getEditingDomain(), commands, true);
	}

}
