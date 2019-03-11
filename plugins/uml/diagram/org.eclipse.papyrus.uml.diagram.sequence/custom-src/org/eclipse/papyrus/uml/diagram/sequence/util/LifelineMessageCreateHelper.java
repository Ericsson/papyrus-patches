/*****************************************************************************
 * Copyright (c) 2009 CEA
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
package org.eclipse.papyrus.uml.diagram.sequence.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ComponentEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetLocationCommand;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CLifeLineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.OLDLifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.figures.LifelineDotLineCustomFigure;
import org.eclipse.papyrus.uml.diagram.sequence.locator.MessageCreateLifelineAnchor;

public class LifelineMessageCreateHelper {

	public static class ComponentEditPolicyEx extends ComponentEditPolicy {

		@Override
		protected Command createDeleteViewCommand(GroupRequest deleteRequest) {
			Command command = super.createDeleteViewCommand(deleteRequest);
			if (command != null && getHost() instanceof LifelineEditPart) {
				command = restoreLifelineOnDelete(command, (LifelineEditPart) getHost());
			}
			return command;
		}
	}


	public static ConnectionAnchor getCreateMessageAnchor(LifelineEditPart part, Request request, Point location) {
		IFigure fig = part.getPrimaryShape();
		fig.translateToRelative(location);
		return new MessageCreateLifelineAnchor(part.getPrimaryShape(), (CLifeLineEditPart) part);
	}

	@Deprecated // TODO_MIA TO_DELETE
	static ConnectionAnchor getTargetConnectionAnchor(LifelineEditPart part, Request request) {
		NodeFigure nodeFigure = null;
		if (part.getContentPane() instanceof LifelineDotLineCustomFigure) {
			nodeFigure = ((LifelineDotLineCustomFigure) part.getContentPane()).getDashLineRectangle();
		}
		if (nodeFigure == null) {
			return null;
		}
		if (request instanceof ReconnectRequest) {
			if (((DropRequest) request).getLocation() == null) {
				return nodeFigure.getTargetConnectionAnchorAt(null);
			}
			Point pt = ((DropRequest) request).getLocation().getCopy();
			return nodeFigure.getTargetConnectionAnchorAt(pt);
		} else if (request instanceof DropRequest) {
			return nodeFigure.getTargetConnectionAnchorAt(((DropRequest) request).getLocation());
		}
		return nodeFigure.getTargetConnectionAnchorAt(null);
	}

	// TODO_MIA TO_DELETE
	// when a lifleine is deleted, move its created lifelines up
	public static Command restoreLifelineOnDelete(Command command, LifelineEditPart part) {
		List<?> list = part.getSourceConnections();
		if (list != null && list.size() > 0) {
			for (Object l : list) {
				if (l instanceof MessageCreateEditPart) {
					EditPart target = ((MessageCreateEditPart) l).getTarget();
					if (target instanceof LifelineEditPart && getIncomingMessageCreate(target).size() == 1) {
						LifelineEditPart lp = (LifelineEditPart) target;
						Rectangle bounds = lp.getPrimaryShape().getBounds();
						Point location = new Point(bounds.x, SequenceUtil.LIFELINE_VERTICAL_OFFSET);
						int dy = location.y - bounds.y;
						ICommand boundsCommand = new SetLocationCommand(lp.getEditingDomain(), DiagramUIMessages.SetLocationCommand_Label_Resize, new EObjectAdapter(lp.getNotationView()), location);
						command = command.chain(new ICommandProxy(boundsCommand));
						command = moveCascadeLifeline(lp, command, dy);
					}
				}
			}
		}
		return command;
	}

	public static boolean hasMessageCreate(GraphicalEditPart sourceEditPart, EditPart targetEditPart) {
		List<?> list = sourceEditPart.getSourceConnections();
		for (Object o : list) {
			if (o instanceof MessageCreateEditPart && targetEditPart.equals(((MessageCreateEditPart) o).getTarget())) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasIncomingMessageCreate(EditPart target) {
		return getIncomingMessageCreate(target).size() > 0;
	}

	public static List<?> getIncomingMessageCreate(EditPart target) {
		List<EditPart> create = new ArrayList<>();
		if (target instanceof LifelineEditPart) {
			List<?> list = ((LifelineEditPart) target).getTargetConnections();
			if (list != null && list.size() > 0) {
				for (Object l : list) {
					if (l instanceof MessageCreateEditPart) {
						create.add((MessageCreateEditPart) l);
					}
				}
			}
		}
		return create;
	}


	public static boolean canReconnectMessageCreate(ReconnectRequest request) {
		MessageCreateEditPart connPart = (MessageCreateEditPart) request.getConnectionEditPart();
		if (request.isMovingStartAnchor()) { // reconnect source
			if (hasMessageCreate((GraphicalEditPart) request.getTarget(), (GraphicalEditPart) connPart.getTarget())) {
				return false;
			}
		} else { // reconnect target
			if (hasMessageCreate((GraphicalEditPart) connPart.getSource(), request.getTarget())) {
				return false;
			}
		}
		return true;
	}

	@Deprecated // TODO_MIA TO_DELETE
	public static Command reconnectMessageCreateTarget(ReconnectRequest request, Command command) {
		LifelineEditPart oldTarget = (LifelineEditPart) request.getConnectionEditPart().getTarget();
		// LifelineEditPart source = (LifelineEditPart)request.getConnectionEditPart().getSource();
		OLDLifelineEditPart newTarget = (OLDLifelineEditPart) request.getTarget();
		// move up the original connection target lifeline, it has only one create message, which will be removed
		if (getIncomingMessageCreate(oldTarget).size() == 1) {
			Rectangle bounds = oldTarget.getPrimaryShape().getBounds();
			Point location = new Point(bounds.x, SequenceUtil.LIFELINE_VERTICAL_OFFSET);
			int dy = location.y - bounds.y;
			ICommand boundsCommand = new SetLocationCommand(oldTarget.getEditingDomain(), DiagramUIMessages.SetLocationCommand_Label_Resize, new EObjectAdapter(oldTarget.getNotationView()), location);
			command = command.chain(new ICommandProxy(boundsCommand));
			command = moveCascadeLifeline(oldTarget, command, dy);
		}
		// move down the new connection target lifeline
		command = moveLifelineDown(command, newTarget, request.getLocation().getCopy());
		return command;
	}

	@Deprecated // TODO_MIA TO_DELETE
	public static Command moveLifelineDown(Command command, OLDLifelineEditPart part, Point sourcePointCopy) {
		// XXX during refactoring, this method still references classes that are no longer used.
		// If anyone called this method, it would necessarily crash; so just make that explicit by throwing immediately.
		throw new IllegalStateException();
	}

	@Deprecated // TODO_MIA TO_DELETE
	public static Command moveCascadeLifeline(LifelineEditPart part, Command command, int dy) {
		// command = moveCascadeLifelineRecursive(part, command, dy);
		// CInteractionEditPart interactionEP = (CInteractionEditPart) part.getParent().getParent();
		// command = interactionEP.getUpdateLifelinesHeightsCommand(command);
		return command;
	}

	// move all lifelines which has incoming create link from part
	@Deprecated // TODO_MIA TO_DELETE
	public static Command moveCascadeLifelineRecursive(LifelineEditPart part, Command command, int dy) {
		List<?> list = part.getSourceConnections();
		if (list != null && list.size() > 0) {
			for (Object l : list) {
				if (l instanceof MessageCreateEditPart) {
					// EditPart target = ((MessageCreateEditPart) l).getTarget();
					// if (target instanceof CLifeLineEditPart) {
					// CLifeLineEditPart lp = (CLifeLineEditPart) target;
					// Rectangle bounds = lp.getFigure().getBounds().getCopy();
					// View targetView = lp.getNotationView();
					// Point location = bounds.getLocation().getCopy().translate(0, dy);
					// Command boundsCommand = new ICommandProxy(new SetBoundsCommand(part.getEditingDomain(), DiagramUIMessages.SetLocationCommand_Label_Resize, new EObjectAdapter(targetView), location));
					// // Take care of the order of commands, to make sure target is always bellow the source.
					// if (dy < 0) { // move up
					// command = command == null ? boundsCommand : command.chain(boundsCommand);
					// command = moveCascadeLifelineRecursive(lp, command, dy);
					// } else { // move down
					// command = moveCascadeLifelineRecursive(lp, command, dy);
					// command = command == null ? boundsCommand : command.chain(boundsCommand);
					// }
					// }
				}
			}
		}
		return command;
	}

	public static boolean canMoveLifelineVertical(LifelineEditPart child, Rectangle newBounds) {
		int halfHeight = child.getPrimaryShape().getFigureLifelineNameContainerFigure().getBounds().height / 2; // TODO_MIA check the use of this methode
		// check outgoing links
		List<?> list = child.getSourceConnections();
		if (list != null && list.size() > 0) {
			for (Object l : list) {
				if (l instanceof MessageCreateEditPart && ((MessageCreateEditPart) l).getTarget() instanceof LifelineEditPart) {
					LifelineEditPart lowPart = (LifelineEditPart) ((MessageCreateEditPart) l).getTarget();
					Rectangle outBounds = lowPart.getFigure().getBounds();
					if (outBounds.y - newBounds.y < halfHeight) {
						return false;
					}
				}
			}
		}
		// check incoming links
		list = child.getTargetConnections();
		if (list != null && list.size() > 0) {
			for (Object l : list) {
				if (l instanceof MessageCreateEditPart && ((MessageCreateEditPart) l).getSource() instanceof LifelineEditPart) {
					LifelineEditPart highPart = (LifelineEditPart) ((MessageCreateEditPart) l).getSource();
					Rectangle inBounds = highPart.getFigure().getBounds();
					if (newBounds.y - inBounds.y < halfHeight) {
						return false;
					}
					if (newBounds.y > inBounds.getBottom().y - halfHeight) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
