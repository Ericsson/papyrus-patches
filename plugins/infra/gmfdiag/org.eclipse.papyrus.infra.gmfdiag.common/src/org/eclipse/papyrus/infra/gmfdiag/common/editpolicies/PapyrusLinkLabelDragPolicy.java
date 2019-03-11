/*****************************************************************************
 * Copyright (c) 2014 CEA LIST and others.
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
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.handles.MoveHandle;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.commands.SetBoundsCommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.LabelEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.NonResizableLabelEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.PapyrusLabelEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.PapyrusLabelHelper;


/**
 * The Class PapyrusLinkLabelDragPolicy.
 *
 * @author Mickael ADAM
 */
public class PapyrusLinkLabelDragPolicy extends NonResizableLabelEditPolicy {

	/**
	 * @see org.eclipse.gef.editpolicies.NonResizableEditPolicy#createSelectionHandles()
	 *
	 * @return
	 */
	@Override
	protected List<MoveHandle> createSelectionHandles() {
		MoveHandle mh = new MoveHandle((GraphicalEditPart) getHost());
		mh.setBorder(null);
		return Collections.singletonList(mh);
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.NonResizableLabelEditPolicy#getMoveCommand(org.eclipse.gef.requests.ChangeBoundsRequest)
	 *
	 * @param request
	 * @return
	 */
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

		Point normalPoint = PapyrusLabelHelper.offsetFromRelativeCoordinate(getHostFigure(), rect, refPoint);

		ICommand moveCommand = new SetBoundsCommand(editPart.getEditingDomain(), DiagramUIMessages.MoveLabelCommand_Label_Location,
				new EObjectAdapter((View) editPart.getModel()), normalPoint);
		return new ICommandProxy(moveCommand);
	}
}
