/*****************************************************************************
 * Copyright (c) 2016 CEA LIST and others.
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
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Bug 533004
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.diagram.core.listener.DiagramEventBroker;
import org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.draw2d.ui.figures.BaseSlidableAnchor;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.DecorationNode;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.LayoutConstraint;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.AutomaticNotationEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.IdentityAnchorHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.NotationHelper;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetLocationCommand;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.util.LogOptions;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionSpecification;

/**
 * this class is used to connect a node to the grid
 * X, Y, X+Width, Y+HEIGHT
 *
 */
public class ConnectRectangleToGridEditPolicy extends ConnectToGridEditPolicy implements AutomaticNotationEditPolicy, NotificationListener, IGrillingEditpolicy {

	protected GrillingEditpart grillingCompartment = null;

	public static String CONNECT_TO_GRILLING_MANAGEMENT = "CONNECT_TO_GRILLING_MANAGEMENT"; //$NON-NLS-1$

	protected DecorationNode rowStart = null;
	protected DecorationNode rowFinish = null;
	protected DecorationNode columnStart = null;
	protected DecorationNode columnFinish = null;
	protected int margin = 0;

	/**
	 * Constructor.
	 *
	 */
	public ConnectRectangleToGridEditPolicy() {
	}

	/**
	 * avoid to modify it directly, try to modify call of sub-methods: initListeningXXX
	 *
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#activate()
	 *
	 */
	@Override
	public void activate() {
		super.activate();
		getDiagramEventBroker().addNotificationListener(((EObject) getHost().getModel()), this);
		DiagramEditPart diagramEditPart = getDiagramEditPart(getHost());
		Node node = ((Node) ((GraphicalEditPart) getHost()).getNotationView());
		try {
			GridManagementEditPolicy grilling = (GridManagementEditPolicy) diagramEditPart.getEditPolicy(GridManagementEditPolicy.GRID_MANAGEMENT);
			Node nodeContainer = (Node) (((GraphicalEditPart) getHost()).getNotationView()).eContainer();
			Element element = (Element) ((GraphicalEditPart) getHost()).resolveSemanticElement();
			if (grilling != null) {
				PrecisionRectangle p = NotationHelper.getAbsoluteBounds((Node) ((GraphicalEditPart) getHost()).getNotationView());
				initListeningRowStart(grilling, element, p);
				initListeningColumnStart(grilling, element, p);
				initListeningRowFinish(node, grilling, element, p);
				initListeningColumnFinish(node, grilling, element, p);
			}
		} catch (NoGrillElementFound e) {
			UMLDiagramEditorPlugin.log.error(e);
		}

	}

	/**
	 * this method is called during the activate
	 * It initialize a columnFinish and listen it
	 *
	 * @param grilling
	 *            the grid manager that allow creating rows
	 * @param element
	 *            the semantic element
	 * @param bounds
	 *            the absolute position of the current node (the origin of the referencial is the diagram)
	 * @throws NoGrillElementFound
	 */
	protected void initListeningColumnFinish(Node node, GridManagementEditPolicy grilling, Element element, PrecisionRectangle bounds) throws NoGrillElementFound {
		columnFinish = grilling.createColumnTolisten(bounds.x + BoundForEditPart.getWidthFromView(node), element);
		getDiagramEventBroker().addNotificationListener(columnFinish, this);
	}

	/**
	 * this method is called during the activate
	 * It initialize a rowFinish and listen it
	 *
	 * @param grilling
	 *            the grid manager that allow creating rows
	 * @param element
	 *            the semantic element
	 * @param bounds
	 *            the absolute position of the current node (the origin of the referential is the diagram)
	 * @throws NoGrillElementFound
	 */
	protected void initListeningRowFinish(Node node, GridManagementEditPolicy grilling, Element element, PrecisionRectangle bounds) throws NoGrillElementFound {
		rowFinish = grilling.createRowTolisten(bounds.y + BoundForEditPart.getHeightFromView(node) + margin, element);
		getDiagramEventBroker().addNotificationListener(rowFinish, this);
	}

	/**
	 * this method is called during the activate
	 * It initialize a ColumnStart and listen it
	 *
	 * @param grilling
	 *            the grid manager that allow creating rows
	 * @param element
	 *            the semantic element
	 * @param bounds
	 *            the absolute position of the current node ( the origin of the referential is the diagram)
	 * @throws NoGrillElementFound
	 */
	protected void initListeningColumnStart(GridManagementEditPolicy grilling, Element element, PrecisionRectangle bounds) throws NoGrillElementFound {
		columnStart = grilling.createColumnTolisten(bounds.x(), element);
		getDiagramEventBroker().addNotificationListener(columnStart, this);
	}

	/**
	 * this method is called during the activate
	 * It initialize a rowStart and listen it
	 *
	 * @param grilling
	 *            the grid manager that allow creating rows
	 * @param element
	 *            the semantic element
	 * @param bounds
	 *            position of the current node in absolute ( the origin is the diagram)
	 * @throws NoGrillElementFound
	 */
	protected void initListeningRowStart(GridManagementEditPolicy grid, Element element, PrecisionRectangle bounds) throws NoGrillElementFound {
		rowStart = grid.createRowTolisten(bounds.y + margin, element);
		getDiagramEventBroker().addNotificationListener(rowStart, this);
	}

	/*
	 * Gets the diagram event broker from the editing domain.
	 *
	 * @return the diagram event broker
	 */
	protected DiagramEventBroker getDiagramEventBroker() {
		TransactionalEditingDomain theEditingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();
		if (null != theEditingDomain) {
			return DiagramEventBroker.getInstance(theEditingDomain);
		}
		return null;
	}


	/**
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#deactivate()
	 *
	 */
	@Override
	public void deactivate() {
		getDiagramEventBroker().removeNotificationListener(((EObject) getHost().getModel()), this);
		if (rowStart != null) {
			getDiagramEventBroker().removeNotificationListener(rowStart, this);
		}
		if (columnStart != null) {
			getDiagramEventBroker().removeNotificationListener(columnStart, this);
		}
		if (rowFinish != null) {
			getDiagramEventBroker().removeNotificationListener(rowFinish, this);
		}
		if (columnFinish != null) {
			getDiagramEventBroker().removeNotificationListener(columnFinish, this);
		}
		super.deactivate();
	}

	/**
	 * avoid to modify it directly, try to modify call of sub-methods: updateXXX
	 *
	 * @see org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 *
	 * @param notification
	 */
	@Override
	public void notifyChanged(Notification notification) {
		// Display imprecision
		if (notification.getEventType() == Notification.REMOVE) {
			return;
		}
		Node nodeContainer = (Node) (((GraphicalEditPart) getHost()).getNotationView()).eContainer();
		if (nodeContainer != null) {

			// UPDATE COLUM AND ROW of THE GRID
			if (notification.getEventType() == Notification.SET && notification.getNotifier() instanceof Bounds) {
				PrecisionRectangle bounds = NotationHelper.getAbsoluteBounds((Node) ((GraphicalEditPart) getHost()).getNotationView());
				UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "+ EVENT: BOUNDS change " + notification.getNewValue());//$NON-NLS-1$

				if (notification.getFeature().equals(NotationPackage.eINSTANCE.getSize_Height())) {

					// Get the old and new values (but care about the -1 default value)
					final int oldIntValue = notification.getOldIntValue() == -1 ? BoundForEditPart.getDefaultHeightFromView((Node) ((GraphicalEditPart) getHost()).getNotationView()) : notification.getOldIntValue();
					final int newIntValue = notification.getNewIntValue() == -1 ? BoundForEditPart.getDefaultHeightFromView((Node) ((GraphicalEditPart) getHost()).getNotationView()) : notification.getNewIntValue();

					updateRowFinishFromHeightNotification(bounds);
					// update anchors
					if ((((EObject) notification.getNotifier()).eContainer().equals((getHost().getModel())))) {
						Node node = (Node) this.getHost().getModel();
						java.util.List<Edge> sourceEdge = node.getSourceEdges();
						for (Edge edge : sourceEdge) {
							IdentityAnchor anchor = (IdentityAnchor) edge.getSourceAnchor();
							if (anchor instanceof IdentityAnchor) {
								updateAnchorFromHeight(anchor, ((Node) getHost().getModel()), newIntValue - oldIntValue);
							}
						}
						java.util.List<Edge> targetEdge = node.getTargetEdges();
						for (Edge edge : targetEdge) {
							IdentityAnchor anchor = (IdentityAnchor) edge.getTargetAnchor();
							if (anchor instanceof IdentityAnchor) {
								updateAnchorFromHeight(anchor, ((Node) getHost().getModel()), newIntValue - oldIntValue);
							}
						}
					}
				}
				if (notification.getFeature().equals(NotationPackage.eINSTANCE.getSize_Width())) {
					updateColumFinishFromWitdhNotification(bounds);
				}
				if (notification.getFeature().equals(NotationPackage.eINSTANCE.getLocation_Y())) {
					// compute next position for RowStart
					updateRowStartFromYNotification(bounds);
					// updateAnchors
					if (((EObject) notification.getNotifier()).eContainer().equals((getHost().getModel()))) {
						Node node = (Node) this.getHost().getModel();

						// children case
						for (Object child : node.getChildren()) {
							// Nodes
							if (child instanceof Node) {
								EObject element = ((Node) child).getElement();
								// ExecutionSpecification
								if (element instanceof ExecutionSpecification) {
									updateExecutionSpecificationFromY((Node) child, notification.getNewIntValue(), notification.getOldIntValue());
								}
							}
						}

						java.util.List<Edge> sourceEdge = node.getSourceEdges();
						for (Edge edge : sourceEdge) {
							IdentityAnchor anchor = (IdentityAnchor) edge.getSourceAnchor();
							if (anchor instanceof IdentityAnchor) {
								updateAnchorFromY(anchor, ((Node) getHost().getModel()), notification.getOldIntValue(), notification.getNewIntValue());
							}
						}
						java.util.List<Edge> targetEdge = node.getTargetEdges();
						for (Edge edge : targetEdge) {
							IdentityAnchor anchor = (IdentityAnchor) edge.getTargetAnchor();
							if (anchor instanceof IdentityAnchor) {
								updateAnchorFromY(anchor, ((Node) getHost().getModel()), notification.getOldIntValue(), notification.getNewIntValue());
							}
						}
					}
				}
				if (notification.getFeature().equals(NotationPackage.eINSTANCE.getLocation_X())) {
					// compute next position for RowStart
					updateColumnStartFromXNotification(bounds);
				}
			}
		}
	}

	/**
	 * This update the position of {@link ExecutionSpecification} {@link Node} after the Y move of lifeline parent.
	 *
	 * @param execSpecNode
	 *            the {@link ExecutionSpecification} {@link Node}
	 * @param newYValue
	 *            the new Y value
	 * @param oldYValue
	 *            the old Y value
	 */
	protected void updateExecutionSpecificationFromY(final Node execSpecNode, final int newYValue, final int oldYValue) {
		LayoutConstraint layoutConstraint = execSpecNode.getLayoutConstraint();
		if (layoutConstraint instanceof Bounds) {
			int delta = newYValue - oldYValue;
			execute(new SetLocationCommand(getDiagramEditPart(getHost()).getEditingDomain(), "update ExecutionSpecification", new EObjectAdapter(execSpecNode), //$NON-NLS-1$
					new Point(((Bounds) layoutConstraint).getX(), ((Bounds) layoutConstraint).getY() - delta)));
		}
	}

	/**
	 * When the bounds of the notation has change the axis must change
	 * In this case this is the height that has change so rowFinish must change
	 *
	 * @param originPosition
	 *            the position of the node is the relative position ( relative to the container)
	 */
	protected void updateRowFinishFromHeightNotification(PrecisionRectangle p) {
		int newY = p.y + p.height + margin;
		updatePositionGridAxis(rowFinish, 0, newY);
	}

	/**
	 * When the bounds of the notation has change the axis must change
	 * In this case this is the width that has changed so ColumnFinish must change
	 *
	 * @param notationBound
	 *            the position of the node is the absolute position ( the origin to the referential is the diagram)
	 */
	protected void updateColumFinishFromWitdhNotification(PrecisionRectangle notationBound) {
		int newX = notationBound.x + notationBound.width;
		updatePositionGridAxis(columnFinish, newX, 0);
		UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "+---->ACTION: modifiy AXIS to  x=" + newX);//$NON-NLS-1$

	}

	/**
	 * When the bounds of the notation has change the axis must change
	 * In this case this is the position Y that has change so RowStart must change
	 *
	 * @param bounds
	 *            the position of the node is the absolute position ( the origin to the referential is the diagram)
	 */
	protected void updateRowStartFromYNotification(PrecisionRectangle bounds) {

		int newY = bounds.y() + margin;
		updatePositionGridAxis(rowStart, 0, newY);

		if (rowFinish != null) {
			newY = bounds.y + bounds.height + margin;
			updatePositionGridAxis(rowFinish, 0, newY);
		}
	}

	/**
	 * When the bounds of the notation has change the axis must change
	 * In this case this is the position X that has change so ColumnStart must change
	 *
	 * @param bounds
	 *            the position of the node is the absolute position ( the origin to the referential is the diagram)
	 */
	protected void updateColumnStartFromXNotification(PrecisionRectangle bounds) {
		int newX = bounds.x();
		updatePositionGridAxis(columnStart, newX, 0);
		if (columnFinish != null) {
			newX = bounds.x() + bounds.width();
			updatePositionGridAxis(columnFinish, newX, 0);
		}
		UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "+---->ACTION: modifiy AXIS START to  x=" + newX);//$NON-NLS-1$

	}





	/**
	 * this class update the position of anchor after the resize
	 *
	 * @param edge
	 *            the edge anchor to update
	 * @param node
	 * @param deltaHeight
	 * @param isSource
	 *            true is it is the anchor source to update, false for the target.
	 */
	protected void updateAnchorFromHeight(IdentityAnchor anchor, Node node, int deltaHeight) {
		if (null != anchor) {
			PrecisionPoint anchorLocation = BaseSlidableAnchor.parseTerminalString(anchor.getId());
			if (anchorLocation != null) {
				double yPercent = anchorLocation.preciseY();
				double xPercent = anchorLocation.preciseX();

				// calculate bounds from notation
				int nodeHeight = BoundForEditPart.getHeightFromView(node);
				double oldHeight = nodeHeight - deltaHeight;
				double preciseHeight = nodeHeight;

				double newPercentY = (yPercent * oldHeight) / preciseHeight;
				final String newIdValue = IdentityAnchorHelper.createNewAnchorIdValue(xPercent, newPercentY);
				execute(new SetCommand(getDiagramEditPart(getHost()).getEditingDomain(), anchor, NotationPackage.eINSTANCE.getIdentityAnchor_Id(), newIdValue));
			}
		}
	}

	/**
	 * This allows to update the position of anchor after the move.
	 *
	 * @param anchor
	 *            The anchor to recalculate.
	 * @param node
	 *            The moved node.
	 * @param oldY
	 *            The old Y position.
	 * @param newY
	 *            The new Y position.
	 */
	protected void updateAnchorFromY(IdentityAnchor anchor, Node node, int oldY, int newY) {
		if (null != anchor && !anchor.getId().trim().equals("")) { //$NON-NLS-1$
			PrecisionPoint anchorLocation = BaseSlidableAnchor.parseTerminalString(anchor.getId());
			if (anchorLocation == null) {
				return;
			}
			double yPercent = anchorLocation.preciseY();
			double xPercent = anchorLocation.preciseX();

			// calculate bounds from notation
			double height = BoundForEditPart.getHeightFromView(node);

			double newPercentY = (oldY - newY) / (height) + yPercent;
			if (newPercentY < 0) {
				newPercentY = 0.01;
			} else if (newPercentY > 1) {
				newPercentY = 0.99;
			}

			final String newIdValue = IdentityAnchorHelper.createNewAnchorIdValue(xPercent, newPercentY);
			execute(new SetCommand(getDiagramEditPart(getHost()).getEditingDomain(), anchor, NotationPackage.eINSTANCE.getIdentityAnchor_Id(), newIdValue));
		}
	}

}
