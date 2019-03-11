/*****************************************************************************
 * Copyright (c) 2010-2017 CEA
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
 *   Atos Origin - Initial API and implementation
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 519408, 525372, 526628
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Bug 531596
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.AbstractRouter;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.requests.BendpointRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionNodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ConnectionBendpointEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.diagram.ui.requests.EditCommandRequestWrapper;
import org.eclipse.gmf.runtime.diagram.ui.util.SelectInDiagramHelper;
import org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.gef.ui.internal.editpolicies.LineMode;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.gmf.runtime.notation.impl.ShapeImpl;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.NodeEditPart;
import org.eclipse.papyrus.uml.diagram.common.editparts.RoundedCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.CustomMessages;
import org.eclipse.papyrus.uml.diagram.sequence.command.DropDestructionOccurenceSpecification;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetLocationCommand;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetResizeCommand;
import org.eclipse.papyrus.uml.diagram.sequence.draw2d.routers.MessageRouter.RouterKind;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractMessageEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CInteractionEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CLifeLineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageAsyncEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageDeleteEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageFoundEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageLostEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageSyncEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.figures.MessageCreate;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.preferences.CustomDiagramGeneralPreferencePage;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.BoundForEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.util.LifelineEditPartUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.LifelineMessageCreateHelper;
import org.eclipse.papyrus.uml.diagram.sequence.util.LifelineMessageDeleteHelper;
import org.eclipse.papyrus.uml.diagram.sequence.util.LogOptions;
import org.eclipse.papyrus.uml.diagram.sequence.util.OccurrenceSpecificationMoveHelper;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceRequestConstant;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.OccurrenceSpecification;

/**
 * This bendpoint edit policy is used to allow drag of horizontal messages and forbid drag otherwise.
 *
 * @author mvelten
 *
 */
@SuppressWarnings("restriction")
public class MessageConnectionLineSegEditPolicy extends ConnectionBendpointEditPolicy {

	/**
	 * Set up moving LineSeg data for self linked message, the value should be one of MOVED_UP, MOVED_DOWN and MOVED_HORIAONTZL.
	 * See showMoveLineSegFeedback() for self linked message for details.
	 */
	private static final String MOVE_LINE_ORIENTATION_DATA = "Move line orientation";

	private static final String MOVED_UP = "Moved Up";

	private static final String MOVED_DOWN = "Moved Down";

	private static final String MOVED_HORIZONTAL = "Moved Horizontal";

	/** The minimum height of the figure. */
	private static final int LIFELINE_MIN_HEIGHT = 100;

	public MessageConnectionLineSegEditPolicy() {
		super(LineMode.ORTHOGONAL_FREE);
	}

	@Override
	protected List<?> createManualHandles() {
		RouterKind kind = RouterKind.getKind(getConnection(), getConnection().getPoints());
		if (kind == RouterKind.SELF || kind == RouterKind.HORIZONTAL || getConnection() instanceof MessageCreate) {
			// Removed the handles for self message.
			return Collections.emptyList();
		}
		return super.createManualHandles();
	}

	@Override
	public Command getCommand(Request request) {
		// get the command in case of deletion of a message
		if (request instanceof EditCommandRequestWrapper
				&& (getHost() instanceof AbstractMessageEditPart)
				&& !(getHost() instanceof MessageDeleteEditPart)
				&& !(getHost() instanceof MessageCreateEditPart)) {

			// Check that this is a delete command, in this case, we have to recalculate the other execution specification positions
			final IEditCommandRequest editCommandRequest = ((EditCommandRequestWrapper) request).getEditCommandRequest();
			if (editCommandRequest instanceof DestroyElementRequest
					&& ((DestroyElementRequest) editCommandRequest).getElementToDestroy() instanceof Message) {
				return getUpdateWeakRefForMessageDelete((EditCommandRequestWrapper) request);
			}
		}

		RouterKind kind = RouterKind.getKind(getConnection(), getConnection().getPoints());
		if (kind == RouterKind.SELF || kind == RouterKind.HORIZONTAL || kind == RouterKind.OBLIQUE || getConnection() instanceof MessageCreate) {
			return super.getCommand(request);
		} else if (request instanceof BendpointRequest) {
			return getMoveMessageCommand((BendpointRequest) request);
		}
		return null;
	}

	/**
	 * Get the command to update weak references of the message for a deletion.
	 *
	 * @param request
	 *            the delete command wrapped into a {@link EditCommandRequestWrapper}.
	 * @return the command
	 */
	private Command getUpdateWeakRefForMessageDelete(final EditCommandRequestWrapper request) {
		CompoundCommand command = null;
		ConnectionEditPart hostConnectionEditPart = (ConnectionEditPart) getHost();

		// compute Delta
		Point moveDelta = new Point(0, -UpdateWeakReferenceEditPolicy.deltaMoveAtCreationAndDeletion);

		if (moveDelta.y < 0) {
			// get the edit policy of references
			if (hostConnectionEditPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE) != null) {
				SequenceReferenceEditPolicy references = (SequenceReferenceEditPolicy) hostConnectionEditPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE);
				if (!SenderRequestUtils.isASender(request, getHost())) {
					CompoundCommand compoundCommand = new CompoundCommand();

					// Gets weak references
					List<EditPart> weakReferences = new ArrayList<>();
					HashMap<EditPart, String> allWeakReferences = references.getWeakReferences();

					allWeakReferences.forEach((editPart, value) -> {
						if (SequenceReferenceEditPolicy.ROLE_FINISH != value) {// Do not take into account finish event of ecexution specification
							weakReferences.add(editPart);
						}
					});

					// for each weak reference move it
					for (Iterator<EditPart> iterator = weakReferences.iterator(); iterator.hasNext();) {
						EditPart editPart = iterator.next();
						if (!hostConnectionEditPart.equals(editPart) && !SenderRequestUtils.isASender(request, editPart)) {// avoid loop
							UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+--> try to Move " + editPart);//$NON-NLS-1$
							ArrayList<EditPart> senderList = SenderRequestUtils.getSenders(request);
							if (editPart instanceof ConnectionEditPart) {
								ConnectionEditPart connectionEditPart = (ConnectionEditPart) editPart;
								// move up, source must be moved before
								UpdateWeakReferenceEditPolicy.moveSourceConnectionEditPart(hostConnectionEditPart, moveDelta, compoundCommand, connectionEditPart, senderList);
								UpdateWeakReferenceEditPolicy.moveTargetConnectionEditPart(hostConnectionEditPart, moveDelta, compoundCommand, connectionEditPart, senderList);
							}
							if (editPart instanceof RoundedCompartmentEditPart) {
								UpdateWeakReferenceEditPolicy.moveRoundedEditPart(hostConnectionEditPart, moveDelta, compoundCommand, editPart, senderList);
							}
						}
						if (!compoundCommand.isEmpty()) {
							command = compoundCommand;
						}
					}
				}
			}
		}
		return null != command && command.canExecute() ? command : null; // Don't return unexecutive command just null
	}

	/**
	 * Add impossible to move the message lost/found by drag the middle line
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=403138
	 */
	protected Command getMoveMessageCommand(BendpointRequest request) {
		if (getHost() instanceof MessageLostEditPart || getHost() instanceof MessageFoundEditPart) {
			PointList points = getConnection().getPoints().getCopy();
			CompoundCommand command = new CompoundCommand("Move");//$NON-NLS-1$
			AbstractMessageEditPart messageEditPart = (AbstractMessageEditPart) getHost();

			// Before to do any move, we need to check if the life lines need to be resized
			// Get the source and target location
			Point sourceLocation = points.getFirstPoint().getCopy();
			getConnection().translateToAbsolute(sourceLocation);
			sourceLocation = SequenceUtil.getSnappedLocation(getHost(), sourceLocation);
			Point targetLocation = points.getLastPoint().getCopy();
			getConnection().translateToAbsolute(targetLocation);
			targetLocation = SequenceUtil.getSnappedLocation(getHost(), targetLocation);

			// Get the life lines
			final EditPart source = messageEditPart.getSource();
			final EditPart target = messageEditPart.getTarget();
			final Collection<EditPart> editParts = new HashSet<>(2);
			editParts.add(source);
			editParts.add(target);

			// This field determine the max y position for the life lines
			// If this one is equals to '-1', there is no resize needed
			int maxY = -1;

			// Loop on possible source and target edit parts to check if this is needed to resize life lines
			for (final EditPart editPart : editParts) {
				final LifelineEditPart lifeLineEditPart = SequenceUtil.getParentLifelinePart(editPart);

				if (null != lifeLineEditPart) {
					if (lifeLineEditPart.getModel() instanceof Shape) {
						final Shape view = (ShapeImpl) lifeLineEditPart.getModel();
						final Bounds bounds = BoundForEditPart.getBounds(view);

						if (sourceLocation.y > (bounds.getY() + bounds.getHeight())) {
							maxY = sourceLocation.y;
						}
						if (targetLocation.y > (bounds.getY() + bounds.getHeight())) {
							maxY = targetLocation.y;
						}
					}
				}
			}

			// If the max y position is greater than '-1', resize is needed for life lines
			if (maxY > -1) {
				LifelineEditPartUtil.resizeAllLifeLines(command, messageEditPart, maxY, null);
			}

			// move source
			ReconnectRequest sourceReq = new ReconnectRequest(REQ_RECONNECT_SOURCE);
			sourceReq.setConnectionEditPart(messageEditPart);
			sourceReq.setLocation(sourceLocation);
			sourceReq.setTargetEditPart(source);
			Command moveSourceCommand = source.getCommand(sourceReq);
			command.add(moveSourceCommand);
			// move target
			ReconnectRequest targetReq = new ReconnectRequest(REQ_RECONNECT_TARGET);
			targetReq.setConnectionEditPart(messageEditPart);
			targetReq.setLocation(targetLocation);
			targetReq.setTargetEditPart(target);
			Command moveTargetCommand = target.getCommand(targetReq);
			command.add(moveTargetCommand);
			return command.unwrap();
		}
		return null;
	}

	/**
	 * Move the anchors along with the line and update bendpoints accordingly.
	 */
	@Override
	protected Command getBendpointsChangedCommand(BendpointRequest request) {
		// snap to grid the location request
		request.setLocation(SequenceUtil.getSnappedLocation(getHost(), request.getLocation()));
		if ((getHost().getViewer() instanceof ScrollingGraphicalViewer) && (getHost().getViewer().getControl() instanceof FigureCanvas)) {
			SelectInDiagramHelper.exposeLocation((FigureCanvas) getHost().getViewer().getControl(), request.getLocation().getCopy());
		}
		if (getHost() instanceof ConnectionNodeEditPart) {
			ConnectionNodeEditPart connectionPart = (ConnectionNodeEditPart) getHost();
			EObject message = connectionPart.resolveSemanticElement();
			if (message instanceof Message) {
				MessageEnd send = ((Message) message).getSendEvent();
				MessageEnd rcv = ((Message) message).getReceiveEvent();
				EditPart srcPart = connectionPart.getSource();
				CLifeLineEditPart srcLifelinePart = (CLifeLineEditPart) SequenceUtil.getParentLifelinePart(srcPart);
				EditPart tgtPart = connectionPart.getTarget();
				CLifeLineEditPart targetLifelinePart = (CLifeLineEditPart) SequenceUtil.getParentLifelinePart(tgtPart);
				if (/* send instanceof OccurrenceSpecification && rcv instanceof OccurrenceSpecification && */srcLifelinePart != null && targetLifelinePart != null) {
					RouterKind kind = RouterKind.getKind(getConnection(), getConnection().getPoints());
					if ((getHost() instanceof MessageSyncEditPart || getHost() instanceof MessageAsyncEditPart) && kind == RouterKind.SELF) {
						// TODO_MIA Test it
						return getSelfLinkMoveCommand(request, connectionPart, send, rcv, srcLifelinePart);
					} else if (getHost() instanceof MessageCreateEditPart) {
						// Move message End
						int y = request.getLocation().y;
						Command srcCmd = createMoveMessageEndCommand((Message) message, srcPart, send, y, srcLifelinePart, request);
						Command tgtCmd = createMoveMessageEndCommand((Message) message, tgtPart, rcv, y, targetLifelinePart, request);

						CompoundCommand compoudCmd = new CompoundCommand(CustomMessages.MoveMessageCommand_Label);
						Point oldLocation = SequenceUtil.getAbsoluteEdgeExtremity(connectionPart, true);
						if (oldLocation != null) {
							int oldY = oldLocation.y;

							// Calculate if this is needed to enlarge the lifelines
							final int yMoveDelta = y - oldY;
							final Command updateLifeLinesBounds = getUpdateLifeLinesBounds(request, connectionPart, yMoveDelta);

							if (null != updateLifeLinesBounds) {
								compoudCmd.add(updateLifeLinesBounds);
							}
							if (oldY < y) {
								compoudCmd.add(tgtCmd);
								compoudCmd.add(srcCmd);
							} else {
								compoudCmd.add(srcCmd);
								compoudCmd.add(tgtCmd);
							}
							return compoudCmd;
						}
					} else if (getHost() instanceof MessageDeleteEditPart) {
						// Reposition lifeline
						IFigure targetFigure = targetLifelinePart.getPrimaryShape();

						Point refPoint = SequenceUtil.getSnappedLocation(targetLifelinePart, request.getLocation().getCopy());
						targetFigure.getParent().translateToRelative(refPoint);
						Bounds bounds = ((Bounds) ((Node) targetLifelinePart.getModel()).getLayoutConstraint());

						ICommand setSizeCommand = new SetResizeCommand(targetLifelinePart.getEditingDomain(), "Size LifeLine", new EObjectAdapter(((GraphicalEditPart) targetLifelinePart).getNotationView()), //$NON-NLS-1$
								new Dimension(bounds.getWidth(), refPoint.y - bounds.getY()));

						CompoundCommand compoudCmd = new CompoundCommand(CustomMessages.MoveMessageCommand_Label);
						if (kind == RouterKind.SELF) {
							// Only resize for down moved
							if (MOVED_DOWN.equals(request.getExtendedData().get(MOVE_LINE_ORIENTATION_DATA))) {
								compoudCmd.add(new ICommandProxy(setSizeCommand));
							}
						} else {
							// Move message End
							int y = request.getLocation().y;
							Command srcCmd = createMoveMessageEndCommand((Message) message, srcPart, send, y, srcLifelinePart, request);
							Command tgtCmd = createMoveMessageEndCommand((Message) message, tgtPart, rcv, y, targetLifelinePart, request);
							DropDestructionOccurenceSpecification dropDestructionOccurenceSpecification = new DropDestructionOccurenceSpecification(((ConnectionEditPart) getHost()).getEditingDomain(), request, targetLifelinePart,
									request.getLocation().getCopy());

							Point oldLocation = SequenceUtil.getAbsoluteEdgeExtremity(connectionPart, true);
							if (oldLocation != null) {
								int oldY = oldLocation.y;

								// Calculate if this is needed to enlarge the lifelines
								final int yMoveDelta = y - oldY;
								final Command updateLifeLinesBounds = getUpdateLifeLinesBounds(request, connectionPart, yMoveDelta);

								if (null != updateLifeLinesBounds) {
									compoudCmd.add(updateLifeLinesBounds);
								}
								if (oldY < y) {// down
									compoudCmd.add(tgtCmd);
									compoudCmd.add(srcCmd);
									compoudCmd.add(new ICommandProxy(dropDestructionOccurenceSpecification));
								} else {// up
									compoudCmd.add(srcCmd);
									compoudCmd.add(tgtCmd);
									compoudCmd.add(new ICommandProxy(dropDestructionOccurenceSpecification));
								}
							}
						}
						return compoudCmd;
					} else {
						int y = request.getLocation().y;
						int yDelta = 0;

						PolylineConnectionEx polyline = (PolylineConnectionEx) connectionPart.getFigure();
						Point sourceAnchorPosition = polyline.getSourceAnchor().getReferencePoint();
						Point targetAnchorPosition = polyline.getTargetAnchor().getReferencePoint();

						if (kind == RouterKind.OBLIQUE) {
							yDelta = targetAnchorPosition.y - sourceAnchorPosition.y;
						}

						Command srcCmd = createMoveMessageEndCommand((Message) message, srcPart, send, y, srcLifelinePart, request);
						Command tgtCmd = createMoveMessageEndCommand((Message) message, tgtPart, rcv, y + yDelta, targetLifelinePart, request);
						CompoundCommand compoudCmd = new CompoundCommand(CustomMessages.MoveMessageCommand_Label);

						/*
						 * Take care of the order of commands, to make sure target is always bellow the source.
						 * Otherwise, moving the target above the source would cause order conflict with existing CF.
						 */
						Point oldLocation = SequenceUtil.getAbsoluteEdgeExtremity(connectionPart, true);
						if (oldLocation != null) {
							int oldY = oldLocation.y;

							// Calculate if this is needed to enlarge the lifelines
							final int yMoveDelta = y - oldY;
							final Command updateLifeLinesBounds = getUpdateLifeLinesBounds(request, connectionPart, yMoveDelta);

							if (null != updateLifeLinesBounds) {
								compoudCmd.add(updateLifeLinesBounds);
							}
							if (oldY < y) {
								compoudCmd.add(tgtCmd);
								compoudCmd.add(srcCmd);
							} else {
								compoudCmd.add(srcCmd);
								compoudCmd.add(tgtCmd);
							}
							return compoudCmd;
						}
					}
				} else
				// Found message case && Lost message case
				if ((srcLifelinePart == null) && (targetLifelinePart != null) || (srcLifelinePart != null && targetLifelinePart == null)) {
					return getMoveMessageCommand(request);
				}
			}
		}
		return UnexecutableCommand.INSTANCE;
	}

	/**
	 * This allows to updates life lines height if necessary.
	 *
	 * @param request
	 *            The initial request.
	 * @param hostConnectionEditPart
	 *            The connection edit part corresponding to the moved message.
	 * @param yMoveDelta
	 *            The height of the move.
	 * @return The command to update life lines or <code>null</code>.
	 * @since 5.0
	 */
	protected Command getUpdateLifeLinesBounds(final BendpointRequest request, final ConnectionEditPart hostConnectionEditPart, final int yMoveDelta) {
		CompoundCommand command = null;
		UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+ Calculate lifelines height modification for " + hostConnectionEditPart.getClass().getName());//$NON-NLS-1$

		boolean mustMoveBelowAtMovingDown = UMLDiagramEditorPlugin.getInstance().getPreferenceStore().getBoolean(CustomDiagramGeneralPreferencePage.PREF_MOVE_BELOW_ELEMENTS_AT_MESSAGE_DOWN);

		if (yMoveDelta != 0) {
			if (yMoveDelta > 0 && mustMoveBelowAtMovingDown && hostConnectionEditPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE) != null) {
				final SequenceReferenceEditPolicy references = (SequenceReferenceEditPolicy) hostConnectionEditPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE);
				if (!SenderRequestUtils.isASender(request, getHost())) {

					// Gets weak references
					final List<EditPart> weakAndStrongReferences = new ArrayList<>();
					weakAndStrongReferences.addAll(references.getWeakReferences().keySet());
					weakAndStrongReferences.addAll(references.getStrongReferences().keySet());

					// The needed y position and heights
					// Get the initial source and target positions of the message
					final PolylineConnectionEx polyline = (PolylineConnectionEx) hostConnectionEditPart.getFigure();
					final Point initialSourcePosition = polyline.getSourceAnchor().getReferencePoint();
					final Point initialTargetPosition = polyline.getTargetAnchor().getReferencePoint();

					final Set<LifelineEditPart> lifelineEditParts = new HashSet<>();
					if (hostConnectionEditPart.getSource() instanceof LifelineEditPart) {
						lifelineEditParts.add((LifelineEditPart) hostConnectionEditPart.getSource());
					}
					if (hostConnectionEditPart.getTarget() instanceof LifelineEditPart) {
						lifelineEditParts.add((LifelineEditPart) hostConnectionEditPart.getTarget());
					}

					// This variable store the maximum y position depending to the message moved
					int maxY = initialTargetPosition.y > initialSourcePosition.y ? initialTargetPosition.y + yMoveDelta : initialTargetPosition.y + yMoveDelta;

					// Loop on each references to get the maximum y and to watch if it is needed to resize lifelines
					for (int index = 0; index < weakAndStrongReferences.size(); index++) {
						final EditPart editPart = weakAndStrongReferences.get(index);
						if (!SenderRequestUtils.isASender(request, editPart)) {
							if (editPart instanceof ConnectionEditPart) {
								ConnectionEditPart connectionEditPart = (ConnectionEditPart) editPart;

								// create the request
								if (yMoveDelta > 0) {

									// Calculate the anchor target Y
									final PolylineConnectionEx subPolyline = (PolylineConnectionEx) connectionEditPart.getFigure();
									final Point targetPositionOnScreen = subPolyline.getTargetAnchor().getReferencePoint();
									final int newYTargetPoint = targetPositionOnScreen.y + yMoveDelta;
									final Point sourcePositionOnScreen = subPolyline.getSourceAnchor().getReferencePoint();
									final int newYSourcePoint = sourcePositionOnScreen.y + yMoveDelta;

									// Get the max y
									if (maxY < newYTargetPoint) {
										maxY = newYTargetPoint;
									}
									if (maxY < newYSourcePoint) {
										maxY = newYSourcePoint;
									}

									if (connectionEditPart.getSource() instanceof LifelineEditPart) {
										lifelineEditParts.add((LifelineEditPart) connectionEditPart.getSource());
									}
									if (connectionEditPart.getTarget() instanceof LifelineEditPart) {
										lifelineEditParts.add((LifelineEditPart) connectionEditPart.getTarget());
									}
								}
							} else if (editPart instanceof IGraphicalEditPart) {
								if (editPart.getModel() instanceof Node) {
									final Rectangle absoluteBounds = SequenceUtil.getAbsoluteBounds((IGraphicalEditPart) editPart);
									if (absoluteBounds.height == -1) {
										absoluteBounds.setHeight(BoundForEditPart.getDefaultHeightFromView((Node) editPart.getModel()));
									}

									final LifelineEditPart parentLifeline = SequenceUtil.getParentLifelinePart(editPart);
									if (null != parentLifeline) {
										lifelineEditParts.add(parentLifeline);
									}

									if (maxY < absoluteBounds.y + absoluteBounds.height + yMoveDelta) {
										maxY = absoluteBounds.y + absoluteBounds.height + yMoveDelta;
									}
								}
							}

							// Get the weak and strong references of the current edit part
							if (editPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE) != null) {
								final SequenceReferenceEditPolicy subReferences = (SequenceReferenceEditPolicy) editPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE);
								for (final EditPart refEditPart : subReferences.getWeakReferences().keySet()) {
									if (!weakAndStrongReferences.contains(refEditPart)) {
										weakAndStrongReferences.add(refEditPart);
									}
								}
								for (final EditPart refEditPart : subReferences.getStrongReferences().keySet()) {
									if (!weakAndStrongReferences.contains(refEditPart)) {
										weakAndStrongReferences.add(refEditPart);
									}
								}
							}
						}
					}

					// Manage the lifelines to resize if needed
					if (!weakAndStrongReferences.isEmpty() || !(hostConnectionEditPart instanceof MessageCreateEditPart || hostConnectionEditPart instanceof MessageDeleteEditPart)) {
						// Get all the life lines in the model to resize them if needed
						final Set<LifelineEditPart> lifeLinesToResize = SequenceUtil.getLifeLinesFromEditPart(hostConnectionEditPart);

						final CompoundCommand compoundCommand = new CompoundCommand();

						// Loop on each life line
						for (final LifelineEditPart lifeLineEP : lifeLinesToResize) {
							if (lifeLineEP.getModel() instanceof Shape) {
								final Shape view = (ShapeImpl) lifeLineEP.getModel();

								// Check if there is message delete on life line
								final boolean hasIncomingMessageDelete = LifelineMessageDeleteHelper.hasIncomingMessageDelete(lifeLineEP);

								// Check if there is message create on life line located after the moved message
								final List<?> incomingMessagesCreate = LifelineMessageCreateHelper.getIncomingMessageCreate(lifeLineEP);
								boolean hasIncomingMessageCreate = incomingMessagesCreate.size() > 0;
								if (hasIncomingMessageCreate) {
									hasIncomingMessageCreate = false;

									final Iterator<?> incomingMessagesCreateIt = incomingMessagesCreate.iterator();
									while (incomingMessagesCreateIt.hasNext() && !hasIncomingMessageCreate) {
										final Object incomingMessageCreate = incomingMessagesCreateIt.next();
										if (incomingMessageCreate instanceof ConnectionEditPart) {
											final PolylineConnectionEx subPolyline = (PolylineConnectionEx) ((ConnectionEditPart) incomingMessageCreate).getFigure();
											final Point targetPositionOnScreen = subPolyline.getTargetAnchor().getReferencePoint();
											final Point sourcePositionOnScreen = subPolyline.getSourceAnchor().getReferencePoint();

											// If the source position is located after the moved source message, in this case, consider the message create as after
											// As same, if the target position is located after the moved target message, in this case, consider the message create as after
											if (sourcePositionOnScreen.y >= initialSourcePosition.y || targetPositionOnScreen.y >= initialTargetPosition.y) {
												hasIncomingMessageCreate = true;
											}
										}
									}
								}

								final Rectangle absoluteBounds = SequenceUtil.getAbsoluteBounds(lifeLineEP);
								if (absoluteBounds.height == -1) {
									absoluteBounds.setHeight(CLifeLineEditPart.DEFAUT_HEIGHT);
								}

								// If there is no Message delete, resize the lifeline if needed
								if (!hasIncomingMessageDelete && !hasIncomingMessageCreate) {
									// Check if this is needed to resize the life line
									if (maxY > absoluteBounds.y + absoluteBounds.height) {
										if (view.getLayoutConstraint() instanceof Bounds) {
											// Create the command to change bounds
											final Bounds bounds = (Bounds) view.getLayoutConstraint();
											final Point newLocation = new Point(bounds.getX(), bounds.getY());
											final Dimension newDimension = new Dimension(bounds.getWidth(), maxY - absoluteBounds.y);

											final ICommand heightCommand = new SetResizeCommand(lifeLineEP.getEditingDomain(), DiagramUIMessages.SetLocationCommand_Label_Resize, new EObjectAdapter(view), newDimension);
											compoundCommand.add(new ICommandProxy(heightCommand));
											final ICommand locationCommand = new SetLocationCommand(lifeLineEP.getEditingDomain(), DiagramUIMessages.SetLocationCommand_Label_Resize, new EObjectAdapter(view), newLocation);
											compoundCommand.add(new ICommandProxy(locationCommand));
										}
									}
								} else if (lifelineEditParts.contains(lifeLineEP)) {

									if (view.getLayoutConstraint() instanceof Bounds) {
										// Create the command to change bounds
										final Bounds bounds = (Bounds) view.getLayoutConstraint();

										// We need to calculate the new y and the new height for the life line
										int newY = bounds.getY();
										int newHeight = bounds.getHeight();

										// If a message create exists, move the life line with yDelta
										if (hasIncomingMessageCreate) {
											newY = bounds.getY() + yMoveDelta;
											// If a message create and no message delete exists, calculate the new height of the life line depending to the current height and to the new maximum Y position
											if (!hasIncomingMessageDelete) {
												newHeight = (absoluteBounds.y + absoluteBounds.height) - newY;
												if (maxY > (newY + newHeight)) {
													newHeight = maxY - newY;
												}
											}
										} else if (hasIncomingMessageDelete) {
											newHeight = absoluteBounds.height + yMoveDelta;
										}

										final Point newLocation = new Point(bounds.getX(), newY);
										final Dimension newDimension = new Dimension(bounds.getWidth(), newHeight);

										final ICommand heightCommand = new SetResizeCommand(lifeLineEP.getEditingDomain(), DiagramUIMessages.SetLocationCommand_Label_Resize, new EObjectAdapter(view), newDimension);
										compoundCommand.add(new ICommandProxy(heightCommand));
										final ICommand locationCommand = new SetLocationCommand(lifeLineEP.getEditingDomain(), DiagramUIMessages.SetLocationCommand_Label_Resize, new EObjectAdapter(view), newLocation);
										compoundCommand.add(new ICommandProxy(locationCommand));
									}
								}
							}
						}

						if (!compoundCommand.isEmpty()) {
							command = compoundCommand;
						}
					}
				}
			}

			// If the command is null and the connection is a message create or a message delete, we need to recalculate the target lifeline bounds
			if (null == command) {
				if (hostConnectionEditPart instanceof MessageCreateEditPart) {
					final CompoundCommand updateLifeLinesBoundsForOnlyOneMessageCreate = getUpdateLifeLinesBoundsForOnlyOneMessageCreate(request, hostConnectionEditPart, yMoveDelta);

					if (!updateLifeLinesBoundsForOnlyOneMessageCreate.isEmpty()) {
						command = updateLifeLinesBoundsForOnlyOneMessageCreate;
					}
				} else if (hostConnectionEditPart instanceof MessageDeleteEditPart) {
					final CompoundCommand updateLifeLinesBoundsForOnlyOneMessageDelete = getUpdateLifeLinesBoundsForOnlyOneMessageDelete(request, hostConnectionEditPart, yMoveDelta);

					if (!updateLifeLinesBoundsForOnlyOneMessageDelete.isEmpty()) {
						command = updateLifeLinesBoundsForOnlyOneMessageDelete;
					}
				}
			}
		}

		return command;
	}

	/**
	 * This allows to get the command for bounds of life lines corresponding to the initial request when only one message create is modifying life lines.
	 *
	 * @param request
	 *            The initial request.
	 * @param hostConnectionEditPart
	 *            The connection edit part corresponding to the moved message.
	 * @param yMoveDelta
	 *            The height of the move.
	 * @return The compound command to update life lines bounds or <code>null</code>.
	 * @since 5.0
	 */
	protected CompoundCommand getUpdateLifeLinesBoundsForOnlyOneMessageCreate(final BendpointRequest request, final ConnectionEditPart hostConnectionEditPart, final int yMoveDelta) {
		final CompoundCommand compoundCommand = new CompoundCommand();

		if (hostConnectionEditPart.getTarget() instanceof LifelineEditPart) {
			final LifelineEditPart targetLifeLine = (LifelineEditPart) hostConnectionEditPart.getTarget();
			if (targetLifeLine.getModel() instanceof Shape) {
				final Shape view = (ShapeImpl) targetLifeLine.getModel();
				// Create the command to change bounds
				final Bounds bounds = (Bounds) view.getLayoutConstraint();
				final Point newLocation = new Point(bounds.getX(), bounds.getY() + yMoveDelta);
				final int initialHeight = bounds.getHeight() == -1 ? BoundForEditPart.getDefaultHeightFromView(view) : bounds.getHeight();
				Dimension newDimension = new Dimension(bounds.getWidth(), initialHeight - yMoveDelta);

				// Check here if this is needed to resize other life lines
				if (yMoveDelta > 0 && initialHeight < yMoveDelta) {
					newDimension.height = LIFELINE_MIN_HEIGHT;
					final int maxY = newLocation.y + newDimension.height;

					final Collection<LifelineEditPart> lifeLineEditPartsToSkip = new HashSet<>(1);
					lifeLineEditPartsToSkip.add(targetLifeLine);
					LifelineEditPartUtil.resizeAllLifeLines(compoundCommand, hostConnectionEditPart, maxY, lifeLineEditPartsToSkip);
				}

				final ICommand heightCommand = new SetResizeCommand(targetLifeLine.getEditingDomain(), DiagramUIMessages.SetLocationCommand_Label_Resize, new EObjectAdapter(view), newDimension);
				compoundCommand.add(new ICommandProxy(heightCommand));
				final ICommand locationCommand = new SetLocationCommand(targetLifeLine.getEditingDomain(), DiagramUIMessages.SetLocationCommand_Label_Resize, new EObjectAdapter(view), newLocation);
				compoundCommand.add(new ICommandProxy(locationCommand));
			}
		}

		return compoundCommand;
	}

	/**
	 * This allows to get the command for bounds of life lines corresponding to the initial request when only one message delete is modifying life lines.
	 *
	 * @param request
	 *            The initial request.
	 * @param hostConnectionEditPart
	 *            The connection edit part corresponding to the moved message.
	 * @param yMoveDelta
	 *            The height of the move.
	 * @return The compound command to update life lines bounds or <code>null</code>.
	 * @since 5.0
	 */
	protected CompoundCommand getUpdateLifeLinesBoundsForOnlyOneMessageDelete(final BendpointRequest request, final ConnectionEditPart hostConnectionEditPart, final int yMoveDelta) {
		final CompoundCommand compoundCommand = new CompoundCommand();

		if (hostConnectionEditPart.getTarget() instanceof LifelineEditPart) {
			final LifelineEditPart targetLifeLine = (LifelineEditPart) hostConnectionEditPart.getTarget();
			if (targetLifeLine.getModel() instanceof Shape) {
				final Shape view = (ShapeImpl) targetLifeLine.getModel();
				final Bounds bounds = (Bounds) view.getLayoutConstraint();
				Dimension newDimension = new Dimension(bounds.getWidth(), bounds.getHeight() + yMoveDelta);

				if (yMoveDelta > 0) {
					// get the max Y of the current life line with the delta
					final int maxY = bounds.getY() + newDimension.height();

					// We need to check if this is needed to resize other life lines
					final Collection<LifelineEditPart> lifeLineEditPartsToSkip = new HashSet<>(1);
					lifeLineEditPartsToSkip.add(targetLifeLine);
					LifelineEditPartUtil.resizeAllLifeLines(compoundCommand, hostConnectionEditPart, maxY, lifeLineEditPartsToSkip);
				}

				final ICommand boundsCommand = new SetResizeCommand(targetLifeLine.getEditingDomain(), DiagramUIMessages.SetLocationCommand_Label_Resize, new EObjectAdapter(view), newDimension);
				compoundCommand.add(new ICommandProxy(boundsCommand));
			}
		}

		return compoundCommand;
	}

	/**
	 * Add impossible to move the anchor connected inside of CoRegion
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=402970
	 */
	protected Command createMoveMessageEndCommand(Message message, EditPart endEditPart, MessageEnd end, int yLocation, LifelineEditPart lifeline, Request request) {
		if (end instanceof OccurrenceSpecification) {
			List<EditPart> empty = Collections.emptyList();
			return OccurrenceSpecificationMoveHelper.getMoveOccurrenceSpecificationsCommand((OccurrenceSpecification) end, null, yLocation, -1, lifeline, empty, request);
		} else if (end instanceof Gate) {
			boolean isSource = (end == message.getSendEvent());
			ConnectionNodeEditPart connection = (ConnectionNodeEditPart) getHost();
			if (isSource) {
				ReconnectRequest req = new ReconnectRequest(REQ_RECONNECT_SOURCE);
				req.getExtendedData().put(SequenceUtil.DO_NOT_CHECK_HORIZONTALITY, true);
				req.setConnectionEditPart(connection);
				req.setTargetEditPart(endEditPart);
				Point location = SequenceUtil.getAbsoluteEdgeExtremity(connection, true);
				location.setY(yLocation);
				req.setLocation(location);
				Command command = endEditPart.getCommand(req);
				return command;
			} else {
				ReconnectRequest req = new ReconnectRequest(REQ_RECONNECT_TARGET);
				req.getExtendedData().put(SequenceUtil.DO_NOT_CHECK_HORIZONTALITY, true);
				req.setConnectionEditPart(connection);
				req.setTargetEditPart(endEditPart);
				Point location = SequenceUtil.getAbsoluteEdgeExtremity(connection, false);
				location.setY(yLocation);
				req.setLocation(location);
				Command command = endEditPart.getCommand(req);
				return command;
			}
		}
		return UnexecutableCommand.INSTANCE;
	}

	protected Command getSelfLinkMoveCommand(BendpointRequest request, ConnectionNodeEditPart connectionPart, MessageEnd send, MessageEnd rcv, LifelineEditPart srcLifelinePart) {
		// Just do it, checking was finished by showing feedback.
		Object moveData = request.getExtendedData().get(MOVE_LINE_ORIENTATION_DATA);
		CompoundCommand compoudCmd = new CompoundCommand(CustomMessages.MoveMessageCommand_Label);
		// And make sure the self linked message can be customized by using bendpoints.
		compoudCmd.add(super.getBendpointsChangedCommand(request));
		PointList points = getConnection().getPoints();
		if (MOVED_UP.equals(moveData)) {
			Point sourceRefPoint = points.getFirstPoint();
			getConnection().translateToAbsolute(sourceRefPoint);
			Command srcCmd = getReconnectCommand(connectionPart, connectionPart.getSource(), sourceRefPoint, RequestConstants.REQ_RECONNECT_SOURCE);
			compoudCmd.add(srcCmd);
		} else if (MOVED_DOWN.equals(moveData)) {
			Point targetRefPoint = points.getLastPoint();
			getConnection().translateToAbsolute(targetRefPoint);
			// Self message not always has same source and target, such as MessageSync.
			Command tgtCmd = getReconnectCommand(connectionPart, connectionPart.getTarget(), targetRefPoint, RequestConstants.REQ_RECONNECT_TARGET);
			compoudCmd.add(tgtCmd);
		}
		return compoudCmd.unwrap();
	}

	protected Command getReconnectCommand(ConnectionNodeEditPart connectionPart, EditPart targetPart, Point location, String requestType) {
		// Create and set the properties of the request
		ReconnectRequest reconnReq = new ReconnectRequest();
		reconnReq.setConnectionEditPart(connectionPart);
		reconnReq.setLocation(location);
		reconnReq.setTargetEditPart(targetPart);
		reconnReq.setType(requestType);
		// add a parameter to bypass the move impact to avoid infinite loop
		reconnReq.getExtendedData().put(SequenceRequestConstant.DO_NOT_MOVE_EDIT_PARTS, true);
		Command cmd = targetPart.getCommand(reconnReq);
		return cmd;
	}

	/**
	 * don't show feedback if the drag is forbidden (message not horizontal).
	 */
	@Override
	public void showSourceFeedback(Request request) {
		if (request instanceof BendpointRequest) {
			RouterKind kind = RouterKind.getKind(getConnection(), getConnection().getPoints());
			if (getHost() instanceof MessageFoundEditPart || getHost() instanceof MessageLostEditPart) {
				showMoveLineSegFeedback((BendpointRequest) request);
			} else if (kind == RouterKind.SELF || kind == RouterKind.HORIZONTAL || kind == RouterKind.OBLIQUE || getConnection() instanceof MessageCreate) {
				if (getLineSegMode() != LineMode.OBLIQUE && REQ_MOVE_BENDPOINT.equals(request.getType())) {
					// Fixed bug about show feedback for moving bendpoints, make sure at least 3 points.
					List constraint = (List) getConnection().getRoutingConstraint();
					if (constraint.size() > 2) {
						super.showSourceFeedback(request);
					}
				} else {
					super.showSourceFeedback(request);
				}
				if (getLineSegMode() != LineMode.OBLIQUE && REQ_MOVE_BENDPOINT.equals(request.getType())) {
					showMoveLineSegFeedback((BendpointRequest) request);
				}
			}
		}
	}

	private ConnectionRouter router;

	static class DummyRouter extends AbstractRouter {

		@Override
		public void route(Connection conn) {
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void showMoveLineSegFeedback(BendpointRequest request) {
		RouterKind kind = RouterKind.getKind(getConnection(), getConnection().getPoints());
		if (((getHost() instanceof MessageSyncEditPart || getHost() instanceof MessageAsyncEditPart || getHost() instanceof MessageDeleteEditPart) && kind == RouterKind.SELF) || kind == RouterKind.OBLIQUE) {
			if (router == null) {
				router = getConnection().getConnectionRouter();
				getConnection().setConnectionRouter(new DummyRouter());
			}
			PointList linkPoints = getConnection().getPoints().getCopy();
			Point ptLoc = SequenceUtil.getSnappedLocation(getHost(), request.getLocation());

			getConnection().translateToRelative(ptLoc);
			int dy = 0;
			int dx = 0;
			int from = 0, to = 0;
			int index = request.getIndex();
			if (index == 0) {
				dy = ptLoc.y - linkPoints.getFirstPoint().y;
				from = 0;
				to = 1;
				request.getExtendedData().put(MOVE_LINE_ORIENTATION_DATA, MOVED_UP);
			} else if (index == 1) {
				dx = ptLoc.x - linkPoints.getMidpoint().x;
				from = 1;
				to = 2;
				request.getExtendedData().put(MOVE_LINE_ORIENTATION_DATA, MOVED_HORIZONTAL);
			} else if (index == 2) {
				dy = ptLoc.y - linkPoints.getLastPoint().y;
				from = 2;
				to = 3;
				request.getExtendedData().put(MOVE_LINE_ORIENTATION_DATA, MOVED_DOWN);
			}
			if (getHost() instanceof MessageSyncEditPart && index > 1) {
				dy = 0;
			}
			// move points on link
			int size = linkPoints.size();
			if (from >= 0 && from < size && to >= 0 && to < size && from <= to) {
				for (int i = from; i <= to; i++) {
					Point p = linkPoints.getPoint(i);
					p.translate(dx, dy);
					linkPoints.setPoint(p, i);
				}
			}
			// link should not exceed lifeline bounds
			getConnection().setPoints(linkPoints);
			getConnection().getLayoutManager().layout(getConnection());
			return;
		}
		// Add impossible to dragging MessageLost and MessageFound. See bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=403138
		if (getHost() instanceof MessageCreateEditPart || getHost() instanceof MessageDeleteEditPart || getHost() instanceof MessageLostEditPart || getHost() instanceof MessageFoundEditPart) {
			if (router == null) {
				router = getConnection().getConnectionRouter();
				getConnection().setConnectionRouter(new DummyRouter());
			}
			PointList linkPoints = getConnection().getPoints().getCopy();
			Point ptLoc = new Point(request.getLocation());
			ptLoc = SequenceUtil.getSnappedLocation(getHost(), ptLoc);
			getConnection().translateToRelative(ptLoc);

			int dy = ptLoc.y - linkPoints.getFirstPoint().y;
			int size = linkPoints.size();
			for (int i = 0; i < size; i++) {
				Point p = linkPoints.getPoint(i).translate(0, dy);
				linkPoints.setPoint(p, i);
			}
			if (checkBounds(linkPoints)) {
				getConnection().setPoints(linkPoints);
				getConnection().getLayoutManager().layout(getConnection());
			}
			return;
		}
		super.showMoveLineSegFeedback(request);
	}

	protected boolean checkBounds(PointList linkPoints) {
		if (linkPoints.getFirstPoint().y > linkPoints.getLastPoint().y) {
			return false;
		}
		EditPart sourcePart = ((ConnectionNodeEditPart) getHost()).getSource();
		if (sourcePart instanceof CInteractionEditPart && getHost() instanceof MessageFoundEditPart) {
			sourcePart = ((ConnectionNodeEditPart) getHost()).getTarget();
		}
		if (sourcePart instanceof CLifeLineEditPart) {
			CLifeLineEditPart sourceLifelineEditPart = (CLifeLineEditPart) sourcePart;
			NodeFigure sourceFigure = sourceLifelineEditPart.getPrimaryShape();
			Rectangle boundsToMatch = sourceFigure.getBounds().getCopy();
			// The bounds to match must be the top of the dashline of the source lifeline and the bottom of the target or source lifeline.
			// TODO_MIA case where target is not a lifeline(AbstractExecutionSpecificationEditPart)
			EditPart targetPart = ((ConnectionNodeEditPart) getHost()).getTarget();
			if (sourceLifelineEditPart.getStickerHeight() != -1) {
				boundsToMatch.setHeight(boundsToMatch.height - sourceLifelineEditPart.getStickerHeight());
				boundsToMatch.setY(boundsToMatch.y + sourceLifelineEditPart.getStickerHeight());
			}

			if (getHost() instanceof MessageCreateEditPart && targetPart instanceof CLifeLineEditPart) {
				NodeFigure targetFigure = (NodeFigure) ((NodeEditPart) targetPart).getPrimaryShape();
				// If the bottom of the target is higher
				int bottom = targetFigure.getBounds().bottom();
				if (bottom < boundsToMatch.bottom()) {
					int delta = boundsToMatch.bottom() - bottom + LIFELINE_MIN_HEIGHT;
					boundsToMatch.setHeight(boundsToMatch.height - delta);
				}
			}
			sourceFigure.translateToAbsolute(boundsToMatch);
			Rectangle boundsToCheck = linkPoints.getBounds();
			getConnection().translateToAbsolute(boundsToCheck);
			// check top y limit
			if (boundsToCheck.getTop().y <= boundsToMatch.getTop().y) {
				return false;
			}
		}
		// It seems the self message can be created on ES, too
		else if (sourcePart instanceof AbstractExecutionSpecificationEditPart) {
			AbstractExecutionSpecificationEditPart esep = (AbstractExecutionSpecificationEditPart) sourcePart;
			IFigure fig = esep.getFigure();
			Rectangle bounds = fig.getBounds().getCopy();
			fig.translateToAbsolute(bounds);
			Rectangle conBounds = linkPoints.getBounds().getCopy();
			getConnection().translateToAbsolute(conBounds);
			if (getHost() instanceof MessageSyncEditPart) {// Sync message is linked between two executions.
				if (conBounds.width < 2 || conBounds.height < 2
				// check top and bottom y limit
						|| conBounds.y <= bounds.y) {
					return false;
				}
			} else if ( // Don't change the orientation of self message.
			bounds.intersects(conBounds.getShrinked(1, 1))
					// make sure the line is not closest.
					|| conBounds.width < 2 || conBounds.height < 2
					// check top and bottom y limit
					|| conBounds.y <= bounds.y || conBounds.getBottom().y >= bounds.getBottom().y) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void eraseConnectionFeedback(BendpointRequest request, boolean removeFeedbackFigure) {
		getConnection().setVisible(true);
		super.eraseConnectionFeedback(request, removeFeedbackFigure);
		if (router != null) {
			getConnection().setConnectionRouter(router);
		}
		router = null;
	}
	// private boolean isHorizontal() {
	// Connection connection = getConnection();
	// RouterKind kind = RouterKind.getKind(connection, connection.getPoints());
	//
	// if(kind.equals(RouterKind.HORIZONTAL)) {
	// return true;
	// }
	// return false;
	// }
	//
	// final private static char TERMINAL_START_CHAR = '(';
	//
	// final private static char TERMINAL_DELIMITER_CHAR = ',';
	//
	// final private static char TERMINAL_END_CHAR = ')';
	//
	// private static String composeTerminalString(PrecisionPoint p) {
	// StringBuffer s = new StringBuffer(24);
	// s.append(TERMINAL_START_CHAR); // 1 char
	// s.append(p.preciseX); // 10 chars
	// s.append(TERMINAL_DELIMITER_CHAR); // 1 char
	// s.append(p.preciseY); // 10 chars
	// s.append(TERMINAL_END_CHAR); // 1 char
	// return s.toString(); // 24 chars max (+1 for safety, i.e. for string termination)
	// }
}
