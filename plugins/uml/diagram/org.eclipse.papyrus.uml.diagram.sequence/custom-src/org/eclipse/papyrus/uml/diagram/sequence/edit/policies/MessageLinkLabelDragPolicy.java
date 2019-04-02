/*****************************************************************************
 * (c) Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.commands.SetBoundsCommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.LabelEditPart;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.PapyrusLabelEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.PapyrusLinkLabelDragPolicy;

public class MessageLinkLabelDragPolicy extends PapyrusLinkLabelDragPolicy {

	public MessageLinkLabelDragPolicy() {
	}

	@Override
	protected Command getMoveCommand(ChangeBoundsRequest request) {
		LabelEditPart editPart = (LabelEditPart) getHost();
		Point refPoint = editPart.getReferencePoint();

		// translate the feedback figure
		PrecisionRectangle rect = new PrecisionRectangle(getInitialFeedbackBounds().getCopy());
		getHostFigure().translateToAbsolute(rect);
		rect.translate(request.getMoveDelta());
		rect.resize(request.getSizeDelta());
		getHostFigure().translateToRelative(rect);

		if (editPart instanceof PapyrusLabelEditPart) {
			// translate according to the text alignments
			switch (((PapyrusLabelEditPart) editPart).getTextAlignment()) {
			case PositionConstants.LEFT:
				rect.translate(-getHostFigure().getBounds().width / 2, 0);
				break;
			case PositionConstants.CENTER:
				break;
			case PositionConstants.RIGHT:
				rect.translate(getHostFigure().getBounds().width / 2, 0);
				break;
			default:
				break;
			}
		}

		Point pt = rect.getCenter().getCopy();
		pt.translate(-refPoint.x, -refPoint.y);

		ICommand moveCommand = new SetBoundsCommand(editPart.getEditingDomain(), DiagramUIMessages.MoveLabelCommand_Label_Location,
				new EObjectAdapter((View) editPart.getModel()), pt);
		return new ICommandProxy(moveCommand);
	}

}
