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
 *   Atos Origin - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.requests.AlignmentRequest;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.common.commands.PreserveAnchorsPositionCommand;
import org.eclipse.papyrus.uml.diagram.sequence.command.CustomZOrderCommand;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetResizeAndLocationCommand;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.BehaviorExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CCombinedCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DestructionOccurrenceSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.OLDLifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.util.LifelineEditPartUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.OccurrenceSpecificationMoveHelper;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;

/**
 * The custom LayoutEditPolicy for LifelineEditPart.
 *
 * @deprecated will be removed in Oxygen
 */
@Deprecated
public class OLDLifelineXYLayoutEditPolicy {

	/** Initialization width of Execution Specification. */
	private final static int EXECUTION_INIT_WIDTH = 16;

	/** The default spacing used between Execution Specification */
	private final static int SPACING_HEIGHT = 5;

	@Deprecated
	private OLDLifelineXYLayoutEditPolicy() {
		// Deprecated
	}

	private static Command resizeParentExecutionSpecification(LifelineEditPart lifelinePart, ShapeNodeEditPart part, Rectangle childBounds, List<ShapeNodeEditPart> list) {
		Rectangle bounds = getRelativeBounds(part.getFigure());
		childBounds.x = bounds.x;
		childBounds.width = bounds.width;
		Rectangle rect = bounds.getCopy();
		int spacingY = OLDLifelineXYLayoutEditPolicy.SPACING_HEIGHT;
		if (childBounds.y - spacingY < rect.y) {
			rect.height += rect.y - childBounds.y + spacingY;
			rect.y = childBounds.y - spacingY;
		} else if (childBounds.bottom() + spacingY > rect.bottom()) {
			rect.height = childBounds.bottom() - rect.y + spacingY;
		} else {
			return null;
		}
		Rectangle newBounds = rect.getCopy();
		CompoundCommand command = new CompoundCommand();
		Command c = new ICommandProxy(new SetResizeAndLocationCommand(part.getEditingDomain(), "Resize of Parent Bar", part, newBounds.getCopy()));
		command.add(c);
		Point moveDelta = new Point(newBounds.x - bounds.x, newBounds.y - bounds.y);
		Dimension sizeDelta = new Dimension(newBounds.width() - bounds.width(), newBounds.height() - bounds.height());
		if (moveDelta.y != 0 || sizeDelta.height() != 0) {
			ChangeBoundsRequest request = new ChangeBoundsRequest();
			request.setEditParts(part);
			request.setMoveDelta(moveDelta);
			request.setSizeDelta(sizeDelta);
			command = OccurrenceSpecificationMoveHelper.completeMoveExecutionSpecificationCommand(command, part, newBounds.getCopy(), request);
		}
		list.remove(part);
		ShapeNodeEditPart parent = getParent(lifelinePart, part.getFigure().getBounds(), list);
		if (parent == null) {
			return command.unwrap();
		}
		return command.unwrap().chain(resizeParentExecutionSpecification(lifelinePart, parent, newBounds.getCopy(), list));
	}

	/**
	 * Useful operation to know where the figure of a ExecutionSpecification EditPart should be
	 * positioned within a Lifeline EditPart. The notToCheckList is needed to avoid checking those
	 * ExecutionSpecification EditParts. The returned bounds are relative to the Lifeline figure so
	 * they can be used, directly, within a SetBoundsCommand.
	 *
	 * @param lifelineEP
	 *                                                 the lifeline ep
	 * @param oldBounds
	 *                                                 The old bounds of the ES
	 * @param newBounds
	 *                                                 The new initial bounds
	 * @param notToCheckExecutionSpecificationList
	 *                                                 The ExecutionSpecification EditPart's List that won't be checked
	 *
	 * @return The new bounds of the executionSpecificationEP figure
	 */
	private final static Rectangle getExecutionSpecificationNewBounds(boolean isMove, LifelineEditPart lifelineEP, Rectangle oldBounds, Rectangle newBounds, List<ShapeNodeEditPart> notToCheckExecutionSpecificationList, boolean useFixedXPos) {
		// Lifeline's figure where the child is drawn
		Rectangle dotLineBounds = lifelineEP.getPrimaryShape().getFigureLifelineDotLineFigure().getBounds();
		// if ExecutionSpecification is resize outside of the lifeline bounds
		if (newBounds.y <= dotLineBounds.y || newBounds.x < dotLineBounds.x || newBounds.x > dotLineBounds.right()) {
			return null;
		}
		List<ShapeNodeEditPart> toCheckExecutionSpecificationList = LifelineEditPartUtil.getChildShapeNodeEditPart(lifelineEP);
		toCheckExecutionSpecificationList.removeAll(notToCheckExecutionSpecificationList);
		if (isMove) {
			ShapeNodeEditPart parent = getParent(lifelineEP, newBounds, toCheckExecutionSpecificationList);
			if (useFixedXPos) {
				newBounds.x = oldBounds.x;
			} else if (parent == null) {
				// No mother, center position
				int width = newBounds.width > 0 ? newBounds.width : EXECUTION_INIT_WIDTH;
				newBounds.x = dotLineBounds.x + dotLineBounds.width / 2 - width / 2;
			} else {
				Rectangle parentBounds = parent.getFigure().getBounds();
				int width = parentBounds.width > 0 ? parentBounds.width : EXECUTION_INIT_WIDTH;
				newBounds.x = parentBounds.x + width / 2 + 1;
			}
		} else {
			ShapeNodeEditPart oldParent = getParent(lifelineEP, oldBounds, toCheckExecutionSpecificationList);
			// forbid resize if the new bounds exceed Y-wise the bounds of a non-parent ES
			for (ShapeNodeEditPart esPart : toCheckExecutionSpecificationList) {
				Rectangle esBounds = esPart.getFigure().getBounds();
				int esYBottom = esBounds.y + esBounds.height;
				if (esPart != oldParent) {
					if (((oldBounds.y + oldBounds.height) <= esBounds.y && (newBounds.y + newBounds.height) >= esBounds.y) || (oldBounds.y >= esYBottom && newBounds.y <= esYBottom)) {
						return null;
					}
				}
			}
		}
		// Change to relative bounds of the LifelineEP
		newBounds.x -= dotLineBounds.x;
		newBounds.y -= dotLineBounds.y;
		return newBounds;
	}

	@SuppressWarnings("unchecked")
	public static Command getResizeOrMoveChildrenCommand(LifelineEditPart lifelineEP, ChangeBoundsRequest request, boolean isMove, boolean updateEnclosingInteraction, boolean useFixedXPos) {
		List<EditPart> editParts = request.getEditParts();
		if (editParts != null) {
			CompoundCommand compoundCmd = new CompoundCommand();
			compoundCmd.setLabel("Move or resize");
			compoundCmd.setDebugLabel("Debug: Move or resize of a Lifeline's children");
			for (EditPart ep : editParts) {
				if (ep instanceof CCombinedCompartmentEditPart || ep instanceof BehaviorExecutionSpecificationEditPart) {
					// an execution specification have been moved or resized
					ShapeNodeEditPart executionSpecificationEP = (ShapeNodeEditPart) ep;
					// Check if height is within the limits of the figure
					Dimension newSizeDelta = adaptSizeDeltaToMaxHeight(executionSpecificationEP.getFigure(), request.getSizeDelta());
					// Current bounds of the ExecutionSpecification
					Rectangle oldBounds = executionSpecificationEP.getFigure().getBounds().getCopy();
					Rectangle newBounds = oldBounds.getCopy();
					// According to the parameters, the new bounds would be the following
					if (request instanceof AlignmentRequest) {
						AlignmentRequest alignmentRequest = (AlignmentRequest) request;
						// Horizontal-only alignment is not allowed
						switch (alignmentRequest.getAlignment()) {
						case PositionConstants.LEFT:
						case PositionConstants.CENTER:
						case PositionConstants.RIGHT:
						case PositionConstants.HORIZONTAL:
							return UnexecutableCommand.INSTANCE;
						}
						newBounds = alignmentRequest.getAlignmentRectangle().getCopy();
						executionSpecificationEP.getFigure().translateToRelative(newBounds);
						// Remove X component of the alignment
						newBounds.x = oldBounds.x;
					} else {
						Dimension unZoomedMoveDelta = new Dimension(request.getMoveDelta().x, request.getMoveDelta().y);
						executionSpecificationEP.getFigure().translateToRelative(unZoomedMoveDelta);
						newBounds.x += unZoomedMoveDelta.width;
						newBounds.y += unZoomedMoveDelta.height;

						Dimension unZoomedSizeDelta = newSizeDelta.getCopy();
						executionSpecificationEP.getFigure().translateToRelative(unZoomedSizeDelta);
						newBounds.height += unZoomedSizeDelta.height;
					}
					// Not to check list
					List<ShapeNodeEditPart> notToCheckExecutionSpecificationList = new BasicEList<>();
					// Affixed ExecutionSpecification List
					notToCheckExecutionSpecificationList.addAll(getAffixedExecutionSpecificationEditParts(executionSpecificationEP));
					// Add also current ExecutionSpecification EditPart
					notToCheckExecutionSpecificationList.add(executionSpecificationEP);
					// find parent bar
					List<ShapeNodeEditPart> executionSpecificationList = LifelineEditPartUtil.getChildShapeNodeEditPart(lifelineEP);
					executionSpecificationList.remove(executionSpecificationEP);
					ShapeNodeEditPart parentBar = getParent(lifelineEP, newBounds, executionSpecificationList);
					// change bounds to relative
					newBounds = getExecutionSpecificationNewBounds(isMove, lifelineEP, oldBounds, newBounds, notToCheckExecutionSpecificationList, useFixedXPos);
					if (newBounds == null) {
						return UnexecutableCommand.INSTANCE;
					}
					if (parentBar != null) {
						compoundCmd.add(resizeParentExecutionSpecification(lifelineEP, parentBar, newBounds.getCopy(), executionSpecificationList));
					}
					// Create and add the command to the compound command
					SetResizeAndLocationCommand setBoundsCmd = new SetResizeAndLocationCommand(executionSpecificationEP.getEditingDomain(), "Resize of a ExecutionSpecification", executionSpecificationEP, newBounds);
					compoundCmd.add(new ICommandProxy(setBoundsCmd));
					Rectangle realMoveDelta = getRealMoveDelta(getRelativeBounds(executionSpecificationEP.getFigure()), newBounds);
					if (isMove) {
						// Move also children
						compoundCmd.add(createMovingAffixedExecutionSpecificationCommand(executionSpecificationEP, realMoveDelta, newBounds.getCopy()));
						compoundCmd.add(createZOrderCommand(lifelineEP, executionSpecificationEP, newBounds.getCopy(), notToCheckExecutionSpecificationList));
					}
					// Move also linked Time elements
					compoundCmd = OccurrenceSpecificationMoveHelper.completeMoveExecutionSpecificationCommand(compoundCmd, executionSpecificationEP, newBounds, request);
					IFigure parentFigure = executionSpecificationEP.getFigure().getParent();
					parentFigure.translateToAbsolute(newBounds);
					// translateToAbsolute only does half of the work, I don't know why
					newBounds.translate(parentFigure.getBounds().getLocation());
					if (updateEnclosingInteraction) {
						// update the enclosing interaction of a moved execution specification
						compoundCmd.add(SequenceUtil.createUpdateEnclosingInteractionCommand(executionSpecificationEP, request.getMoveDelta(), newSizeDelta));
					}
					// keep absolute position of anchors
					compoundCmd.add(new ICommandProxy(new OLDLifelineEditPart.PreserveAnchorsPositionCommandEx(executionSpecificationEP, new Dimension(realMoveDelta.width, realMoveDelta.height), PreserveAnchorsPositionCommand.PRESERVE_Y,
							executionSpecificationEP.getFigure(), request.getResizeDirection())));
				}
				// if (ep instanceof CombinedFragment2EditPart) {
				// CombinedFragment2EditPart cf2EP = (CombinedFragment2EditPart) ep;
				// IFigure cf2Figure = cf2EP.getFigure();
				// Rectangle bounds = cf2Figure.getBounds().getCopy();
				// cf2Figure.getParent().translateToAbsolute(bounds);
				// Dimension sizeDelta = request.getSizeDelta();
				// if (sizeDelta != null) {
				// if (sizeDelta.width != 0) {
				// return UnexecutableCommand.INSTANCE;
				// }
				// bounds.resize(sizeDelta);
				// }
				// Point moveDelta = request.getMoveDelta();
				// if (moveDelta != null) {
				// bounds.translate(moveDelta);
				// }
				// // Create and add the set bounds command to the compound command
				// SetBoundsCommand setBoundsCmd = new SetBoundsCommand(cf2EP.getEditingDomain(), "Resize of a CoRegion", cf2EP, getNewBoundsForChild(lifelineEP, bounds, COREGION_INIT_WIDTH));
				// compoundCmd.add(new ICommandProxy(setBoundsCmd));
				// // keep absolute position of anchors
				// if (sizeDelta != null && sizeDelta.height != 0) {
				// compoundCmd.add(new ICommandProxy(new OLDLifelineEditPart.PreserveAnchorsPositionCommandEx(cf2EP, new Dimension(0, sizeDelta.height), PreserveAnchorsPositionCommand.PRESERVE_Y, cf2EP.getPrimaryShape().getCentralVerticalLine(),
				// request.getResizeDirection())));
				// }
				// }
				if (ep instanceof DestructionOccurrenceSpecificationEditPart) {
					Rectangle rectLifeline = lifelineEP.getFigure().getBounds();
					compoundCmd = getSetLifelineHeightCommand(compoundCmd, lifelineEP, rectLifeline.height + request.getMoveDelta().y);
				}
			}
			if (!compoundCmd.isEmpty()) {
				return compoundCmd;
			}
		}
		return null;
	}

	/**
	 * Modifies Lifeline's height and keeps anchors' positions intact.
	 *
	 * @param compoundCmd
	 *                        command to add to
	 * @param lifelineEP
	 *                        Lifeline's edit part
	 * @param newHeight
	 *                        new height of the Lifeline
	 *
	 * @return the compound command
	 */
	@Deprecated
	private static CompoundCommand getSetLifelineHeightCommand(CompoundCommand compoundCmd, LifelineEditPart lifelineEP, int newHeight) {
		// XXX During SeqD refactoring, this method still references a class that doesn't exist, without checking if
		// it is valid. This method can't work. Whoever still calls that method would crash anyway; so just throw immediately.
		throw new IllegalStateException();
	}

	/**
	 * Command for change ZOrder of ExecutionSpecification ordered from parent to children.
	 *
	 * @param lifelineEP
	 *                                                 the lifeline ep
	 * @param executionSpecificationEP
	 *                                                 the execution specification ep
	 * @param newBounds
	 *                                                 the new bounds
	 * @param notToCheckExecutionSpecificationList
	 *                                                 the not to check bes list
	 *
	 * @return the command
	 */
	private final static Command createZOrderCommand(LifelineEditPart lifelineEP, ShapeNodeEditPart executionSpecificationEP, Rectangle newBounds, List<ShapeNodeEditPart> notToCheckExecutionSpecificationList) {
		List<ShapeNodeEditPart> toCheckExecutionSpecificationList = LifelineEditPartUtil.getChildShapeNodeEditPart(lifelineEP);
		toCheckExecutionSpecificationList.removeAll(notToCheckExecutionSpecificationList);
		CompoundCommand cmd = new CompoundCommand();
		for (ShapeNodeEditPart externalExecutionSpecificationEP : toCheckExecutionSpecificationList) {
			Rectangle externalExecutionSpecificationBounds = getRelativeBounds(externalExecutionSpecificationEP.getFigure());
			// Check if there is any contact
			if (externalExecutionSpecificationBounds.touches(newBounds)) {
				View containerView = ViewUtil.getContainerView(executionSpecificationEP.getPrimaryView());
				if (containerView != null) {
					int i = 0;
					int parentIndex = -1;
					int childIndex = -1;
					for (Object child : containerView.getChildren()) {
						if (child == externalExecutionSpecificationEP.getPrimaryView()) {
							parentIndex = i;
						} else if (child == executionSpecificationEP.getPrimaryView()) {
							childIndex = i;
						}
						if (parentIndex != -1 && childIndex != -1) {
							if (childIndex > parentIndex) {
								cmd.add(new ICommandProxy(new CustomZOrderCommand(executionSpecificationEP.getEditingDomain(), executionSpecificationEP.getPrimaryView(), parentIndex)));
								cmd.add(new ICommandProxy(new CustomZOrderCommand(externalExecutionSpecificationEP.getEditingDomain(), externalExecutionSpecificationEP.getPrimaryView(), childIndex)));
							} else {
								break;
							}
						}
						i++;
					}
				}
			}
		}
		if (!cmd.isEmpty()) {
			return cmd;
		}
		return null;
	}

	/**
	 * Useful operation to know where the figure of a ExecutionSpecification EditPart should be
	 * positioned within a Lifeline EditPart. The notToCheckList is needed to avoid checking those
	 * ExecutionSpecification EditParts. The returned bounds are relative to the Lifeline figure so
	 * they can be used, directly, within a SetBoundsCommand.
	 *
	 * @param lifelineDotLineFigure
	 *                                                 TODO
	 * @param newBounds
	 *                                                 The new initial bounds
	 * @param executionSpecifactionEditPart
	 *                                                 TODO
	 * @param notToCheckExecutionSpecificationList
	 *                                                 The ExecutionSpecification EditPart's List that won't be checked
	 *
	 * @return The new bounds of the executionSpecificationEP figure
	 */
	/**
	 * Get the (futur) parent of a ExecutionSpecification
	 *
	 * @param lifelinePart
	 *
	 * @param childBounds
	 *                                              the child bounds
	 * @param toCheckExecutionSpecificationList
	 *                                              List of EditPart to check
	 * @return The parent
	 */
	public static final ShapeNodeEditPart getParent(LifelineEditPart lifelinePart, Rectangle childBounds, List<ShapeNodeEditPart> toCheckExecutionSpecificationList) {
		ShapeNodeEditPart parent = null;
		// Loop through the ExecutionSpecification list and try to find the most to the right
		// ExecutionSpecification within the executionSpecificationEP Y-axis bounds
		Rectangle externalBounds = childBounds.getCopy();
		for (ShapeNodeEditPart externalExecutionSpecificationEP : toCheckExecutionSpecificationList) {
			Rectangle externalExecutionSpecificationBounds = externalExecutionSpecificationEP.getFigure().getBounds();
			externalBounds.x = externalExecutionSpecificationBounds.x;
			externalBounds.width = externalExecutionSpecificationBounds.width;
			if (externalExecutionSpecificationBounds.touches(externalBounds) && externalExecutionSpecificationBounds.x <= childBounds.x) {
				if (parent == null || externalExecutionSpecificationBounds.x > parent.getFigure().getBounds().x) {
					parent = externalExecutionSpecificationEP;
				}
			}
		}
		return parent;
	}

	/**
	 * Used to modify the sizeDelta if the given value is higher/lower than the highest/lowest
	 * allowed values of the figure.
	 *
	 * @param figure
	 *                      the figure
	 * @param sizeDelta
	 *                      the size delta
	 *
	 * @return a corrected sizeDelta
	 */
	public static final Dimension adaptSizeDeltaToMaxHeight(IFigure figure, Dimension sizeDelta) {
		Dimension newSizeDelta = new Dimension(sizeDelta);
		int figureHeight = figure.getBounds().height;
		int maximunFigureHeight = figure.getMaximumSize().height;
		int minimunFigureHeight = figure.getMinimumSize().height;
		int height = figureHeight + newSizeDelta.height;
		if (height > maximunFigureHeight) {
			newSizeDelta.height = maximunFigureHeight - figureHeight;
		} else if (height < minimunFigureHeight) {
			newSizeDelta.height = minimunFigureHeight - figureHeight;
		}
		return newSizeDelta;
	}

	/**
	 * Returns all the ExecutionSpecification EditParts that are affixed to the right side of the
	 * given ExecutionSpecification EditPart. Not only the ones directly affixed to the
	 * executionSpecificationEP are returned, but the ones that are indirectly affixed as well (this
	 * is done recursively)
	 *
	 * @param executionSpecificationEP
	 *                                     the execution specification ep
	 *
	 * @return the list of affixed ExecutionSpecification. If there is no affixed
	 *         ExecutionSpecification, then an empty list will be returned
	 */
	public static final List<ShapeNodeEditPart> getAffixedExecutionSpecificationEditParts(ShapeNodeEditPart executionSpecificationEP) {
		List<ShapeNodeEditPart> notToCheckExecutionSpecificationList = new ArrayList<>();
		return getAffixedExecutionSpecificationEditParts(executionSpecificationEP, notToCheckExecutionSpecificationList);
	}

	/**
	 * Operation used by the above operation. It's main goal is to obtain, recursively, all the
	 * affixed ExecutionSpecification. In order to do so, it is needed a ExecutionSpecification
	 * EditPart and the notToCheckList.
	 *
	 * @param executionSpecificationEP
	 *                                                 the execution specification ep
	 * @param notToCheckExecutionSpecificationList
	 *                                                 the not to check ExecutionSpecification list
	 *
	 * @return the list of affixed ExecutionSpecification. If there is no affixed
	 *         ExecutionSpecification, then an empty list will be returned
	 */
	private final static List<ShapeNodeEditPart> getAffixedExecutionSpecificationEditParts(ShapeNodeEditPart executionSpecificationEP, List<ShapeNodeEditPart> notToCheckExecutionSpecificationList) {
		// Add itself to the notToCheck list
		List<ShapeNodeEditPart> newNotToCheckExecutionSpecificationList = new ArrayList<>(notToCheckExecutionSpecificationList);
		newNotToCheckExecutionSpecificationList.add(executionSpecificationEP);
		// LifelineEditPart where the ExecutionSpecification EditPart is contained
		LifelineEditPart lifelineEP = (LifelineEditPart) executionSpecificationEP.getParent();
		// ExecutionSpecification EditParts list
		List<ShapeNodeEditPart> executionSpecificationList = LifelineEditPartUtil.getChildShapeNodeEditPart(lifelineEP);
		executionSpecificationList.removeAll(newNotToCheckExecutionSpecificationList);
		// List to store the Affixed ExecutionSpecification
		List<ShapeNodeEditPart> affixedExecutionSpecificationList = new ArrayList<>();
		// Loop ExecutionSpecificationough the ExecutionSpecification list
		for (ShapeNodeEditPart childExecutionSpecificationEP : executionSpecificationList) {
			if (isAffixedToRight(executionSpecificationEP.getFigure().getBounds(), childExecutionSpecificationEP.getFigure().getBounds())) {
				affixedExecutionSpecificationList.add(childExecutionSpecificationEP);
				// Add also it's affixed ExecutionSpecification
				affixedExecutionSpecificationList.addAll(getAffixedExecutionSpecificationEditParts(childExecutionSpecificationEP, newNotToCheckExecutionSpecificationList));
			}
		}
		// To the ExecutionSpecification list
		return affixedExecutionSpecificationList;
	}

	/**
	 * Checks whether the right EditPart is affixed to the left EditPart. In order to do so, the
	 * operation checks if the right figure is really on the right and, if so, it just returns true
	 * if figures touch each other.
	 *
	 * @param leftFigure
	 *                        The left rectangle
	 * @param rightFigure
	 *                        The right rectangle
	 *
	 * @return true if the rectangles of both figures touch and the right figure is really on the
	 *         right. False otherwise
	 */
	public static final boolean isAffixedToRight(Rectangle leftFigure, Rectangle rightFigure) {
		// return leftFigure.touches(rightFigure) && leftFigure.x < rightFigure.x;
		return leftFigure.contains(rightFigure.getLocation()) && leftFigure.x < rightFigure.x;
	}

	/**
	 * If a ExecutionSpecification EditPart is going to be moved according to a moveDelta, this
	 * operation returns a compoundCommand that also moves the affixed ExecutionSpecification
	 * according to that delta.
	 *
	 * @param executionSpecificationEP
	 *                                     The ExecutionSpecification EditPart that is going to be moved
	 * @param moveDelta
	 *                                     The moveDelta of the previous EditPart
	 * @param newBounds
	 *                                     the new bounds
	 *
	 * @return the compound command
	 */
	private final static CompoundCommand createMovingAffixedExecutionSpecificationCommand(ShapeNodeEditPart executionSpecificationEP, Rectangle moveDelta, Rectangle newBounds) {
		if (moveDelta.y != 0 || moveDelta.height != 0) {
			CompoundCommand compoundCmd = new CompoundCommand();
			for (ShapeNodeEditPart childExecutionSpecificationEP : getAffixedExecutionSpecificationEditParts(executionSpecificationEP)) {
				// Get Relative Bounds
				Rectangle childBounds = getRelativeBounds(childExecutionSpecificationEP.getFigure());
				// Apply delta
				childBounds.y += moveDelta.y;
				childBounds.x += moveDelta.x;
				// Create the child's SetBoundsCommand
				SetResizeAndLocationCommand childSetBoundsCmd = new SetResizeAndLocationCommand(executionSpecificationEP.getEditingDomain(), "Movement of affixed ExecutionSpecification", childExecutionSpecificationEP, childBounds);
				compoundCmd.add(new ICommandProxy(childSetBoundsCmd));
				IFigure parentFigure = childExecutionSpecificationEP.getFigure().getParent();
				parentFigure.translateToAbsolute(newBounds);
				// translateToAbsolute only does half of the work, I don't know why
				newBounds.translate(parentFigure.getBounds().getLocation());
				// change the enclosing interaction of the moved affixed child if necessary
				compoundCmd.add(SequenceUtil.createUpdateEnclosingInteractionCommand(childExecutionSpecificationEP, moveDelta.getLocation(), moveDelta.getSize()));
				ChangeBoundsRequest request = new ChangeBoundsRequest();
				request.setMoveDelta(new Point(0, moveDelta.y));
				OccurrenceSpecificationMoveHelper.completeMoveExecutionSpecificationCommand(compoundCmd, childExecutionSpecificationEP, childBounds.getCopy(), request);
				// Move it's children as well
				if (!getAffixedExecutionSpecificationEditParts(childExecutionSpecificationEP).isEmpty()) {
					compoundCmd.add(createMovingAffixedExecutionSpecificationCommand(childExecutionSpecificationEP, moveDelta, childBounds));
				}
			}
			if (!compoundCmd.isEmpty()) {
				return compoundCmd;
			}
		}
		return null;
	}

	/**
	 * Given an AbstractGraphialEditPart and the new relative bounds that the EditPart will have, it
	 * returns the real delta applied to the movement.
	 *
	 * @param oldRelativeBounds
	 *                              The old position of the mentioned EditPart
	 * @param newRelativeBounds
	 *                              The new position of the mentioned EditPart
	 *
	 * @return The real MoveDelta applied
	 */
	public static final Rectangle getRealMoveDelta(Rectangle oldRelativeBounds, Rectangle newRelativeBounds) {
		Rectangle realMoveDelta = new Rectangle();
		realMoveDelta.x = newRelativeBounds.x - oldRelativeBounds.x;
		realMoveDelta.y = newRelativeBounds.y - oldRelativeBounds.y;
		realMoveDelta.height = newRelativeBounds.height - oldRelativeBounds.height;
		realMoveDelta.width = newRelativeBounds.width - oldRelativeBounds.width;
		return realMoveDelta;
	}

	/**
	 * It returns the relative bounds of an Figure.
	 *
	 * @param figure
	 *                   The Figure
	 *
	 * @return The relative bounds regarding it's parent figure
	 */
	public static final Rectangle getRelativeBounds(IFigure figure) {
		Rectangle relBounds = figure.getBounds().getCopy();
		Rectangle parentRectangle = figure.getParent().getBounds();
		relBounds.x -= parentRectangle.x;
		relBounds.y -= parentRectangle.y;
		return relBounds;
	}
}
