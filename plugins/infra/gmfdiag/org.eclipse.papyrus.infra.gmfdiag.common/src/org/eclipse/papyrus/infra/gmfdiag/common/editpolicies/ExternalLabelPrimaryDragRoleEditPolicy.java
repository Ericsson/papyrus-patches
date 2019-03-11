/*****************************************************************************
 * Copyright (c) 2010, 2014 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Yann Tanguy (CEA LIST) yann.tanguy@cea.fr - Initial API and implementation
 *   Mickael ADAM (ALL4TEC) mickael.adam@all4tec.net - Move to common
 * 
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.handles.MoveHandle;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.commands.SetBoundsCommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IBorderItemEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.LabelEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.NonResizableLabelEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.figures.IBorderItemLocator;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.PapyrusLabelEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramEditPartsUtil;

/**
 * This policy provides the selection handles, feedback and move command for
 * external node label.
 * The expected behavior is to provide an external label that can freely move and with a link feedback
 * towards its parent figure during the move.
 */
public class ExternalLabelPrimaryDragRoleEditPolicy extends NonResizableLabelEditPolicy {

	private Polyline tether = null;

	/**
	 * Creates the selection handles.
	 *
	 * @return the list
	 * @see org.eclipse.gef.editpolicies.NonResizableEditPolicy#createSelectionHandles()
	 */
	@Override
	protected List<MoveHandle> createSelectionHandles() {
		MoveHandle mh = new MoveHandle((GraphicalEditPart) getHost());
		mh.setBorder(null);
		return Collections.singletonList(mh);
	}

	/**
	 * Gets the move command.
	 *
	 * @param request
	 *            the request
	 * @return the move command
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.NonResizableLabelEditPolicy#getMoveCommand(org.eclipse.gef.requests.ChangeBoundsRequest)
	 */
	@Override
	protected Command getMoveCommand(ChangeBoundsRequest request) {
		LabelEditPart editPart = (LabelEditPart) getHost();
		// FeedBack - Port + Delta
		Rectangle updatedRect = new Rectangle();
		PrecisionRectangle initialRect = new PrecisionRectangle(getInitialFeedbackBounds().getCopy());
		updatedRect = initialRect.getTranslated(getHostFigure().getParent().getBounds().getLocation().getNegated());

		// take into account the zoom
		double zoomLevel = DiagramEditPartsUtil.getDiagramZoomLevel(editPart);
		updatedRect = updatedRect.getTranslated(request.getMoveDelta().scale(1 / zoomLevel));

		// translate according to the text alignments
		if (editPart instanceof PapyrusLabelEditPart) {
			switch (((PapyrusLabelEditPart) editPart).getTextAlignment()) {
			case PositionConstants.LEFT:
				break;
			case PositionConstants.CENTER:
				updatedRect.translate(getHostFigure().getBounds().width / 2, 0);
				break;
			case PositionConstants.RIGHT:
				updatedRect.translate(getHostFigure().getBounds().width, 0);
				break;
			default:
				break;
			}
		}

		ICommand moveCommand = new SetBoundsCommand(editPart.getEditingDomain(), DiagramUIMessages.MoveLabelCommand_Label_Location, new EObjectAdapter((View) editPart.getModel()), updatedRect);
		return new ICommandProxy(moveCommand);
	}

	/**
	 * Shows or updates feedback for a change bounds request.
	 *
	 * @param request
	 *            the request
	 */
	@Override
	protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {

		IBorderItemEditPart borderItemEP = (IBorderItemEditPart) getHost();
		IBorderItemLocator borderItemLocator = borderItemEP.getBorderItemLocator();

		if (borderItemLocator != null) {
			IFigure feedback = getDragSourceFeedbackFigure();
			PrecisionRectangle rect = new PrecisionRectangle(getInitialFeedbackBounds().getCopy());
			getHostFigure().translateToAbsolute(rect);
			rect.translate(request.getMoveDelta());
			rect.resize(request.getSizeDelta());
			getHostFigure().translateToRelative(rect);
			Rectangle realLocation = borderItemLocator.getValidLocation(rect.getCopy(), borderItemEP.getFigure());
			getHostFigure().translateToAbsolute(realLocation);
			feedback.translateToRelative(realLocation);
			feedback.setBounds(realLocation);
		}

		drawTether(request);
	}

	protected IFigure createDragSourceFeedbackFigure() {
		IFigure feedback = super.createDragSourceFeedbackFigure();
		tether = new Polyline();
		tether.setLineStyle(Graphics.LINE_DASH);
		tether.setForegroundColor(((IGraphicalEditPart) getHost()).getFigure()
				.getForegroundColor());
		addFeedback(tether);
		return feedback;
	}

	/**
	 * @param request
	 */
	private void drawTether(ChangeBoundsRequest request) {
		IFigure p = getDragSourceFeedbackFigure();
		Point refPoint = ((LabelEditPart) getHost()).getReferencePoint();

		Rectangle centerMain = null;
		if (((IGraphicalEditPart) getHost().getParent()).getFigure() instanceof Connection) {
			centerMain = new Rectangle(refPoint.x, refPoint.y, 0, 0);
			getHostFigure().translateToAbsolute(centerMain);
			p.translateToRelative(centerMain);
		} else {
			centerMain = ((IGraphicalEditPart) getHost().getParent()).getFigure().getBounds().getCopy();
			getHostFigure().translateToAbsolute(centerMain);
			centerMain.translate(centerMain.width / 2, centerMain.height / 2);
			p.translateToRelative(centerMain);
		}

		PrecisionRectangle ref = new PrecisionRectangle(centerMain);
		Rectangle r = p.getBounds();
		Point midTop = new Point(r.x + r.width / 2, r.y);
		Point midBottom = new Point(r.x + r.width / 2, r.y + r.height);
		Point midLeft = new Point(r.x, r.y + r.height / 2);
		Point midRight = new Point(r.x + r.width, r.y + r.height / 2);

		Point startPoint = midTop;

		int x = r.x + r.width / 2 - refPoint.x;
		int y = r.y + r.height / 2 - refPoint.y;

		if (y > 0 && y > x && y > -x)
			startPoint = midTop;
		else if (y < 0 && y < x && y < -x)
			startPoint = midBottom;
		else if (x < 0 && y > x && y < -x)
			startPoint = midRight;
		else
			startPoint = midLeft;

		tether.setStart(startPoint);
		tether.setEnd(ref.getLocation());
	}

	protected void eraseChangeBoundsFeedback(ChangeBoundsRequest request) {
		super.eraseChangeBoundsFeedback(request);
		if (tether != null)
			removeFeedback(tether);
		tether = null;
	}


}
