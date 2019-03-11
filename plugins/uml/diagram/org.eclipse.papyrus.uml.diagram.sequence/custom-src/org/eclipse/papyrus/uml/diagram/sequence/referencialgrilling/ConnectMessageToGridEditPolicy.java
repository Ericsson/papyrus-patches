/*****************************************************************************
 * Copyright (c) 2016, 2017 CEA LIST and others.
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
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 525372
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.core.listener.DiagramEventBroker;
import org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.gef.ui.internal.editpolicies.GraphicalEditPolicyEx;
import org.eclipse.gmf.runtime.notation.DecorationNode;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.Location;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.AutomaticNotationEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.IdentityAnchorHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.NotationHelper;
import org.eclipse.papyrus.uml.diagram.sequence.command.SetLocationCommand;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.util.LogOptions;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.NamedElement;

/**
 * this editpolicy is used to manage messages by the grid
 */
public class ConnectMessageToGridEditPolicy extends GraphicalEditPolicyEx implements AutomaticNotationEditPolicy, NotificationListener, IGrillingEditpolicy {

	protected GrillingEditpart gridCompartment = null;

	public static String CONNECT_TO_GRID_MANAGEMENT = "CONNECT_TO_GRID_MANAGEMENT"; //$NON-NLS-1$
	protected int displayImprecision = 2;


	private View rowSource;

	private View rowTarget;

	/**
	 * Constructor.
	 *
	 */
	public ConnectMessageToGridEditPolicy() {
	}


	/**
	 * update an axis of the grid from coordinate X or Y
	 *
	 * @param axis
	 *            the axis to update
	 * @param x
	 *            the coordinate x
	 * @param y
	 *            the coordinate y
	 */
	protected void updatePositionGridAxis(DecorationNode axis, int x, int y) {
		Location currentBounds = (Location) axis.getLayoutConstraint();
		if (x < currentBounds.getX() - displayImprecision || x > currentBounds.getX() + displayImprecision) {
			UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "+---->ACTION: modifiy AXIS to x=" + x + " y=" + y);//$NON-NLS-1$ //$NON-NLS-2$
			execute(new SetLocationCommand(getDiagramEditPart(getHost()).getEditingDomain(), "update Column", new EObjectAdapter(axis), new Point(x, y))); //$NON-NLS-1$

		}
		if (y < currentBounds.getY() - displayImprecision || y > currentBounds.getY() + displayImprecision) {
			UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "+---->ACTION: modifiy AXIS to x=" + x + " y=" + y);//$NON-NLS-1$ //$NON-NLS-2$
			execute(new SetLocationCommand(getDiagramEditPart(getHost()).getEditingDomain(), "update row", new EObjectAdapter(axis), new Point(x, y))); //$NON-NLS-1$
		}
	}

	/**
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#activate()
	 *
	 */
	@Override
	public void activate() {
		super.activate();
		getDiagramEventBroker().addNotificationListener(((EObject) getHost().getModel()), this);
		DiagramEditPart diagramEditPart = getDiagramEditPart(getHost());
		try {
			GridManagementEditPolicy grilling = (GridManagementEditPolicy) diagramEditPart.getEditPolicy(GridManagementEditPolicy.GRID_MANAGEMENT);
			if (grilling != null) {
				ConnectionEditPart connectionEditPart = (ConnectionEditPart) getHost();
				Edge edge = (Edge) connectionEditPart.getModel();
				IdentityAnchor sourceAnchor = (IdentityAnchor) edge.getSourceAnchor();
				IdentityAnchor targetAnchor = (IdentityAnchor) edge.getTargetAnchor();
				GraphicalEditPart sourceEditPart = (GraphicalEditPart) connectionEditPart.getSource();
				GraphicalEditPart targetEditPart = (GraphicalEditPart) connectionEditPart.getTarget();
				if (sourceAnchor != null && targetAnchor != null) {
					// source
					if (sourceAnchor.getId() != null && !(sourceAnchor.getId().equals(""))) {
						Message m = (Message) connectionEditPart.resolveSemanticElement();
						double absoluteY = computeAnchorPositionNotation(sourceAnchor, sourceEditPart);

						// Ensure that the target is always below the source
						if (null != targetEditPart) {
							int targetAnchorY = computeAnchorPositionNotation(targetAnchor, targetEditPart);
							if (targetAnchorY <= absoluteY) {
								absoluteY = targetAnchorY - 1;
							}
						}

						if (m.getSendEvent() == null) {
							rowSource = grilling.createRowTolisten((int) absoluteY, m);
						} else {
							rowSource = grilling.createRowTolisten((int) absoluteY, m.getSendEvent());
						}

						getDiagramEventBroker().addNotificationListener(rowSource, this);
					}
					// target
					if (null != targetEditPart && targetAnchor.getId() != null && !(targetAnchor.getId().equals(""))) {
						Message m = (Message) connectionEditPart.resolveSemanticElement();
						double absoluteY = computeAnchorPositionNotation(targetAnchor, targetEditPart);

						// Ensure that the target is always below the source
						if (null != sourceEditPart) {
							int sourceAnchorY = computeAnchorPositionNotation(sourceAnchor, sourceEditPart);
							if (sourceAnchorY >= absoluteY) {
								absoluteY = sourceAnchorY + 1;
							}
						}

						if (m.getReceiveEvent() == null) {
							rowTarget = grilling.createRowTolisten((int) absoluteY, m);
						} else {
							rowTarget = grilling.createRowTolisten((int) absoluteY, m.getReceiveEvent());
						}
						getDiagramEventBroker().addNotificationListener(rowTarget, this);
					}
				}

			}
		} catch (NoGrillElementFound e) {
			UMLDiagramEditorPlugin.log.error(e);
		}
	}

	/**
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
		super.deactivate();
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 *
	 * @param notification
	 */
	@Override
	public void notifyChanged(Notification notification) {
		DiagramEditPart diagramEditPart = getDiagramEditPart(getHost());
		if (diagramEditPart != null) {
			// CREATION
			if (notification.getNotifier().equals((getHost().getModel())) && NotationPackage.eINSTANCE.getEdge_SourceAnchor().equals(notification.getFeature()) && notification.getNewValue() != null) {
				UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "+ EVENT :CREATION add SourceAnchor " + notification.getNotifier());//$NON-NLS-1$
				IdentityAnchor sourceAnchor = (IdentityAnchor) notification.getNewValue();
				if (sourceAnchor.getId() != null && !(sourceAnchor.getId().equals(""))) {
					ConnectionEditPart connectionEditPart = (ConnectionEditPart) getHost();
					Message m = (Message) connectionEditPart.resolveSemanticElement();
					if (connectionEditPart.getSource() instanceof GraphicalEditPart) {
						GraphicalEditPart sourceEditpart = (GraphicalEditPart) connectionEditPart.getSource();
						int sourceAnchorY = computeAnchorPositionNotation(sourceAnchor, sourceEditpart);

						// Ensure that the target is always below the source
						Edge edge = (Edge) connectionEditPart.getNotationView();
						IdentityAnchor targetAnchor = (IdentityAnchor) edge.getTargetAnchor();
						EditPart targetEditpart = connectionEditPart.getTarget();
						if (targetEditpart instanceof GraphicalEditPart && null != targetAnchor) {
							int targetAnchorY = computeAnchorPositionNotation(targetAnchor, (GraphicalEditPart) targetEditpart);
							if (targetAnchorY <= sourceAnchorY) {
								sourceAnchorY = targetAnchorY - 1;
							}
						}

						try {
							GridManagementEditPolicy grilling = (GridManagementEditPolicy) diagramEditPart.getEditPolicy(GridManagementEditPolicy.GRID_MANAGEMENT);
							if (grilling != null) {
								if (rowSource == null) {
									if (m.getSendEvent() == null) {
										rowSource = grilling.createRowTolisten(sourceAnchorY, m);
									} else {
										rowSource = grilling.createRowTolisten(sourceAnchorY, m.getSendEvent());
									}
								}
								getDiagramEventBroker().addNotificationListener(rowSource, this);

							}
						} catch (NoGrillElementFound e) {
							UMLDiagramEditorPlugin.log.error(e);
						}
					}
				}
			}

			// CREATION
			if (notification.getNotifier().equals((getHost().getModel())) && NotationPackage.eINSTANCE.getEdge_TargetAnchor().equals(notification.getFeature()) && notification.getNewValue() != null) {
				UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "+ EVENT: CREATION add targetAnchor " + notification.getNotifier());//$NON-NLS-1$
				IdentityAnchor targetAnchor = (IdentityAnchor) notification.getNewValue();
				if (targetAnchor.getId() != null && !(targetAnchor.getId().equals(""))) {
					ConnectionEditPart connectionEditPart = (ConnectionEditPart) getHost();
					if (connectionEditPart.getTarget() instanceof GraphicalEditPart) {
						GraphicalEditPart targetEditpart = (GraphicalEditPart) connectionEditPart.getTarget();
						Message m = (Message) connectionEditPart.resolveSemanticElement();
						int targetAnchorY = computeAnchorPositionNotation(targetAnchor, targetEditpart);

						// Ensure that the target is always below the source
						Edge edge = (Edge) connectionEditPart.getNotationView();
						IdentityAnchor sourceAnchor = (IdentityAnchor) edge.getSourceAnchor();
						EditPart sourceEditpart = connectionEditPart.getSource();
						if (sourceEditpart instanceof GraphicalEditPart && null != sourceAnchor) {
							int sourceAnchorY = computeAnchorPositionNotation(sourceAnchor, (GraphicalEditPart) sourceEditpart);
							if (targetAnchorY <= sourceAnchorY) {
								targetAnchorY = sourceAnchorY + 1;
							}
						}


						try {
							GridManagementEditPolicy grilling = (GridManagementEditPolicy) diagramEditPart.getEditPolicy(GridManagementEditPolicy.GRID_MANAGEMENT);
							if (grilling != null) {
								if (rowTarget == null) {
									if (m.getReceiveEvent() == null) {
										rowTarget = grilling.createRowTolisten(targetAnchorY, m);
									} else {
										rowTarget = grilling.createRowTolisten(targetAnchorY, m.getReceiveEvent());
									}
								}
								getDiagramEventBroker().addNotificationListener(rowTarget, this);
							}
						} catch (NoGrillElementFound e) {
							UMLDiagramEditorPlugin.log.error(e);
						}
					}
				}
			}
			if (notification.getEventType() == Notification.SET && notification.getFeature().equals(NotationPackage.eINSTANCE.getEdge_Source())) {
				UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "+EVENT Source change " + notification.getNotifier());//$NON-NLS-1$
				ConnectionEditPart connectionEditPart = (ConnectionEditPart) getHost();
				Edge edge = (Edge) connectionEditPart.getNotationView();
				if (edge.getSourceAnchor() != null && rowSource != null) {
					IdentityAnchor sourceAnchor = (IdentityAnchor) edge.getSourceAnchor();
					if (connectionEditPart.getSource() instanceof GraphicalEditPart) {
						GraphicalEditPart sourceEditpart = (GraphicalEditPart) connectionEditPart.getSource();
						int sourceAnchorY = computeAnchorPositionNotation(sourceAnchor, sourceEditpart);

						// Ensure that the target is always below the source
						IdentityAnchor targetAnchor = (IdentityAnchor) edge.getTargetAnchor();
						EditPart targetEditpart = connectionEditPart.getTarget();
						if (targetEditpart instanceof GraphicalEditPart && null != targetAnchor) {
							int targetAnchorY = computeAnchorPositionNotation(targetAnchor, (GraphicalEditPart) targetEditpart);
							if (targetAnchorY <= sourceAnchorY) {
								sourceAnchorY = targetAnchorY - 1;
							}
						}

						UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "+--> SOURCE change for " + ((NamedElement) connectionEditPart.resolveSemanticElement()).getName() + " to " + sourceAnchorY + " ");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						updatePositionGridAxis((DecorationNode) rowSource, 0, sourceAnchorY);
					}
				}
			}

			if (notification.getEventType() == Notification.SET && notification.getFeature().equals(NotationPackage.eINSTANCE.getEdge_Target())) {
				UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "+EVENT target change " + notification.getNotifier());//$NON-NLS-1$
				ConnectionEditPart connectionEditPart = (ConnectionEditPart) getHost();
				Edge edge = (Edge) connectionEditPart.getNotationView();
				if (edge.getTargetAnchor() != null && rowTarget != null) {
					IdentityAnchor targetAnchor = (IdentityAnchor) edge.getTargetAnchor();
					if (connectionEditPart.getTarget() instanceof GraphicalEditPart) {
						GraphicalEditPart targetEditpart = (GraphicalEditPart) connectionEditPart.getTarget();
						int targetAnchorY = computeAnchorPositionNotation(targetAnchor, targetEditpart);

						// Ensure that the target is always below the source
						IdentityAnchor sourceAnchor = (IdentityAnchor) edge.getSourceAnchor();
						EditPart sourceEditpart = connectionEditPart.getSource();
						if (sourceEditpart instanceof GraphicalEditPart && null != sourceAnchor) {
							int sourceAnchorY = computeAnchorPositionNotation(sourceAnchor, (GraphicalEditPart) sourceEditpart);
							if (targetAnchorY <= sourceAnchorY) {
								targetAnchorY = sourceAnchorY + 1;
							}
						}

						UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "+--> SOURCE change for " + ((NamedElement) connectionEditPart.resolveSemanticElement()).getName() + " to " + targetAnchorY + " ");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						updatePositionGridAxis((DecorationNode) rowTarget, 0, targetAnchorY);
					}
				}

			}

			// A move has been done by the user
			if (notification.getEventType() == Notification.SET && notification.getNotifier() instanceof IdentityAnchor) {
				UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "+EVENT IdentificationAnchor change " + notification.getNotifier());//$NON-NLS-1$
				ConnectionEditPart connectionEditPart = (ConnectionEditPart) getHost();
				Edge edge = (Edge) connectionEditPart.getNotationView();
				if (notification.getNotifier().equals(edge.getSourceAnchor()) && rowSource != null) {
					IdentityAnchor sourceAnchor = (IdentityAnchor) edge.getSourceAnchor();
					if (connectionEditPart.getSource() instanceof GraphicalEditPart) {
						GraphicalEditPart sourceEditpart = (GraphicalEditPart) connectionEditPart.getSource();
						int sourceAnchorY = computeAnchorPositionNotation(sourceAnchor, sourceEditpart);

						// Ensure that the target is always below the source
						IdentityAnchor targetAnchor = (IdentityAnchor) edge.getTargetAnchor();
						EditPart targetEditpart = connectionEditPart.getTarget();
						if (targetEditpart instanceof GraphicalEditPart && null != targetAnchor) {
							int targetAnchorY = computeAnchorPositionNotation(targetAnchor, (GraphicalEditPart) targetEditpart);
							if (targetAnchorY <= sourceAnchorY) {
								sourceAnchorY = targetAnchorY - 1;
							}
						}

						UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "+--> SOURCE change for " + ((NamedElement) connectionEditPart.resolveSemanticElement()).getName() + " to " + sourceAnchorY + " ");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						updatePositionGridAxis((DecorationNode) rowSource, 0, sourceAnchorY);
					}
				}
				if (notification.getNotifier().equals(edge.getTargetAnchor()) && rowTarget != null) {
					if (connectionEditPart.getTarget() instanceof GraphicalEditPart) {
						GraphicalEditPart targetEditpart = (GraphicalEditPart) connectionEditPart.getTarget();
						IdentityAnchor targetAnchor = (IdentityAnchor) edge.getTargetAnchor();
						int targetAnchorY = computeAnchorPositionNotation(targetAnchor, targetEditpart);

						// Ensure that the target is always below the source
						IdentityAnchor sourceAnchor = (IdentityAnchor) edge.getSourceAnchor();
						EditPart sourceEditpart = connectionEditPart.getSource();
						if (sourceEditpart instanceof GraphicalEditPart && null != sourceAnchor) {
							int sourceAnchorY = computeAnchorPositionNotation(sourceAnchor, (GraphicalEditPart) sourceEditpart);
							if (targetAnchorY <= sourceAnchorY) {
								targetAnchorY = sourceAnchorY + 1;
							}
						}

						UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "+-->TARGET change " + ((NamedElement) connectionEditPart.resolveSemanticElement()).getName() + " to " + targetAnchorY + " ");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						updatePositionGridAxis((DecorationNode) rowTarget, 0, targetAnchorY);
					}
				}

			}

		}
	}


	public static int computeAnchorPositionNotation(IdentityAnchor anchor, GraphicalEditPart nodeEditPart) {
		double yPercent = IdentityAnchorHelper.getYPercentage(anchor);
		Node node = (Node) nodeEditPart.getNotationView();
		PrecisionRectangle bounds = NotationHelper.getAbsoluteBounds(node);
		double height = BoundForEditPart.getHeightFromView(node);
		int anchorY = (int) (height * yPercent) + bounds.y;
		return anchorY;
	}
}