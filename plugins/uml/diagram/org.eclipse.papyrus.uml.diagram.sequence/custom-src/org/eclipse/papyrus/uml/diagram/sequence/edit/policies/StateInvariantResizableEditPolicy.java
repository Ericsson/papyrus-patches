/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
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

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IBorderItemEditPart;
import org.eclipse.gmf.runtime.diagram.ui.figures.IBorderItemLocator;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.BorderItemResizableEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetLocationCommand;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetResizeAndLocationCommand;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;

/**
 * @author Celine JANSSENS
 *
 */
public class StateInvariantResizableEditPolicy extends BorderItemResizableEditPolicy {

	@Override
	protected Command getResizeCommand(ChangeBoundsRequest request) {
		IBorderItemEditPart borderItemEP = (IBorderItemEditPart) getHost();
		IBorderItemLocator borderItemLocator = borderItemEP.getBorderItemLocator();
		if (borderItemLocator != null) {
			PrecisionRectangle rect = new PrecisionRectangle(getInitialFeedbackBounds().getCopy());
			getHostFigure().translateToAbsolute(rect);
			rect.translate(request.getMoveDelta());
			rect.resize(request.getSizeDelta());
			getHostFigure().translateToRelative(rect);
			Rectangle realLocation = borderItemLocator.getValidLocation(rect.getCopy(), borderItemEP.getFigure());
			if (borderItemEP.getParent() instanceof LifelineEditPart && !restrictInParentBounds((LifelineEditPart) borderItemEP.getParent(), borderItemEP, realLocation.getCopy())) {
				return null;
			}
			ICommand moveCommand = new SetResizeAndLocationCommand(borderItemEP.getEditingDomain(), DiagramUIMessages.Commands_MoveElement, new EObjectAdapter((View) getHost().getModel()), realLocation);
			return new ICommandProxy(moveCommand);
		}
		return null;
	}

	@Override
	protected Command getMoveCommand(ChangeBoundsRequest request) {
		IBorderItemEditPart borderItemEP = (IBorderItemEditPart) getHost();
		IBorderItemLocator borderItemLocator = borderItemEP.getBorderItemLocator();
		if (borderItemLocator != null) {
			PrecisionRectangle rect = new PrecisionRectangle(getInitialFeedbackBounds().getCopy());
			getHostFigure().translateToAbsolute(rect);
			rect.translate(request.getMoveDelta());
			rect.resize(request.getSizeDelta());
			getHostFigure().translateToRelative(rect);
			Rectangle realLocation = borderItemLocator.getValidLocation(rect.getCopy(), borderItemEP.getFigure());
			if (borderItemEP.getParent() instanceof LifelineEditPart && !restrictInParentBounds((LifelineEditPart) borderItemEP.getParent(), borderItemEP, realLocation.getCopy())) {
				return null;
			}
			Point location = realLocation.getTopLeft();
			ICommand moveCommand = new SetLocationCommand(borderItemEP.getEditingDomain(), DiagramUIMessages.Commands_MoveElement, new EObjectAdapter((View) getHost().getModel()), location);
			return new ICommandProxy(moveCommand);
		}
		return null;
	}

	private boolean restrictInParentBounds(LifelineEditPart ep, IBorderItemEditPart borderItemEP, Rectangle realLocation) {
		borderItemEP.getFigure().translateToAbsolute(realLocation);
		Rectangle bounds = ep.getPrimaryShape().getBounds().getCopy();
		ep.getPrimaryShape().translateToAbsolute(bounds);

		return true;
	}


}
