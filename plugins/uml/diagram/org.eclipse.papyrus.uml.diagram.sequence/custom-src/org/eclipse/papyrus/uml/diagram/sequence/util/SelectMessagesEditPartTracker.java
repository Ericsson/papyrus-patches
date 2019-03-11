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
 *   Celine Janssens (ALL4TEC) - Bug 507348
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.selection.SelectSeveralLinksEditPartTracker;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractMessageEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;

/**
 * @author Patrick Tessier
 * @since 3.0
 *
 */
public class SelectMessagesEditPartTracker extends SelectSeveralLinksEditPartTracker {

	protected int MinDistancetop = Integer.MAX_VALUE;
	protected int MinDistancebottom = Integer.MAX_VALUE;
	protected Dimension delta = null;

	/**
	 * This allows to determinate if this is a reorder or not.
	 */
	private boolean allowReorder;

	private boolean isOneMessageDeleteSelected;

	/**
	 * Constructor.
	 *
	 * @param owner
	 * @param shiftDown
	 */
	public SelectMessagesEditPartTracker(ConnectionEditPart owner) {
		super(owner);
		this.allowReorder = ((AbstractMessageEditPart) owner).mustReorderMessage();
		this.isOneMessageDeleteSelected = false;
	}

	/**
	 * @see org.eclipse.gef.tools.AbstractTool#activate()
	 *
	 */
	@Override
	public void activate() {
		super.activate();
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.selection.SelectSeveralLinksEditPartTracker#handleButtonDown(int)
	 *
	 * @param button
	 * @return
	 */
	@Override
	protected boolean handleButtonDown(int button) {
		MinDistancetop = Integer.MAX_VALUE;
		MinDistancebottom = Integer.MAX_VALUE;

		this.isOneMessageDeleteSelected = false;

		// 1. look for all Nodes connected by connections
		// and find the MinDistancetop (maximum movement without reorder to the top) and the MinDistancebottom (maximum distance without reorder to the bottom)

		ArrayList<GraphicalEditPart> nodeEditPart = new ArrayList<>();

		List selectedEditparts = getOperationSet();

		for (int i = 0; i < selectedEditparts.size(); i++) {
			Object currentEditPart = selectedEditparts.get(i);
			if (currentEditPart instanceof org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart) {
				org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart currentConnectionEdiPart = (org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart) currentEditPart;
				nodeEditPart.add((GraphicalEditPart) currentConnectionEdiPart.getSource());
				if (currentConnectionEdiPart instanceof MessageCreateEditPart) {
					isOneMessageDeleteSelected = true;
				}
			}

		}
		// 2. take all messages between this nodes
		ArrayList<AbstractMessageEditPart> messageEditPartList = new ArrayList<>();
		for (GraphicalEditPart anodeEditPart : nodeEditPart) {
			for (Object connection : anodeEditPart.getSourceConnections()) {

				if (connection instanceof AbstractMessageEditPart) {
					if (!(selectedEditparts.contains(connection))) {
						messageEditPartList.add((AbstractMessageEditPart) connection);
					}
				}
			}
			for (Object connection : anodeEditPart.getTargetConnections()) {
				if (!(selectedEditparts.contains(connection))) {
					if (connection instanceof AbstractMessageEditPart) {
						messageEditPartList.add((AbstractMessageEditPart) connection);
					}
				}
			}
		}

		// 3 calculate minimum distance
		for (AbstractMessageEditPart abstractMessageEditPart : messageEditPartList) {
			Point currentConnectionPosition = abstractMessageEditPart.getConnectionFigure().getPoints().getFirstPoint().getCopy();
			for (Object selectedEditPart : selectedEditparts) {
				if (selectedEditPart instanceof AbstractMessageEditPart) {
					AbstractMessageEditPart currentSelectedMessage = (AbstractMessageEditPart) selectedEditPart;
					Point currentSelectedConnectionPosition = currentSelectedMessage.getConnectionFigure().getPoints().getFirstPoint().getCopy();

					// selected Message is below the currentConnection
					if (currentConnectionPosition.y < currentSelectedConnectionPosition.y) {
						if (MinDistancetop > (currentSelectedConnectionPosition.y - currentConnectionPosition.y)) {
							MinDistancetop = (currentSelectedConnectionPosition.y - currentConnectionPosition.y);
						}
					} else if (isOneMessageDeleteSelected) {
						// selected Message is above the currentConnection
						if (MinDistancebottom > (currentConnectionPosition.y - currentSelectedConnectionPosition.y)) {
							MinDistancebottom = (currentConnectionPosition.y - currentSelectedConnectionPosition.y);
						}
					}
				}

			}
		}

		return super.handleButtonDown(button);
	}


	/**
	 * @see org.eclipse.gef.tools.SimpleDragTracker#updateSourceRequest()
	 */
	@Override
	protected void updateSourceRequest() {
		// When the reorder is called and there is at least one MessageCreate selected, we need to hold the message create movement
		if (isOneMessageDeleteSelected) {
			if (allowReorder) {
				Dimension computedDelta = getLocation().getDifference(getStartLocation());
				delta = null;
				if (computedDelta.height < 0) {
					UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_UTIL, "Move " + computedDelta.height + " MinDistancetop" + MinDistancetop);//$NON-NLS-1$
					if (MinDistancetop + computedDelta.height < 0) {
						computedDelta.height = -MinDistancetop;
						delta = computedDelta.getCopy();
					}
				} else {
					UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_UTIL, "Move " + computedDelta.height + " MinDistancebottom" + MinDistancebottom);//$NON-NLS-1$
					if (MinDistancebottom - computedDelta.height - 10 < 0) {
						computedDelta.height = MinDistancebottom - 10;
						delta = computedDelta.getCopy();
					}

				}
			}
		} else if (!allowReorder) {
			Dimension computedDelta = getLocation().getDifference(getStartLocation());
			delta = null;
			if (computedDelta.height < 0) {
				UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_UTIL, "Move " + computedDelta.height + " MinDistancetop" + MinDistancetop);//$NON-NLS-1$
				if (MinDistancetop + computedDelta.height < 0) {
					computedDelta.height = -MinDistancetop;
					delta = computedDelta.getCopy();
				}
			} else {
				UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_UTIL, "Move " + computedDelta.height + " MinDistancebottom" + MinDistancebottom);//$NON-NLS-1$
				if (MinDistancebottom - computedDelta.height - 10 < 0) {
					computedDelta.height = MinDistancebottom - 10;
					delta = computedDelta.getCopy();
				}

			}
		}
		super.updateSourceRequest();
	}

	@Override
	protected Dimension getDragMoveDelta() {
		if (delta == null) {
			return getLocation().getDifference(getStartLocation());
		} else {
			return delta;
		}
	}

	/**
	 * Walks up the editpart hierarchy to find and return the
	 * <code>TopGraphicEditPart</code> instance.
	 */
	public DiagramEditPart getDiagramEditPart(EditPart editPart) {
		while (editPart instanceof IGraphicalEditPart) {
			if (editPart instanceof DiagramEditPart) {
				return (DiagramEditPart) editPart;
			}

			editPart = editPart.getParent();
		}
		if (editPart instanceof DiagramRootEditPart) {
			return (DiagramEditPart) ((DiagramRootEditPart) editPart).getChildren().get(0);
		}
		return null;
	}
}
