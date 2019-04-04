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
 *   Yoann Farre (CIL4Sys) <yoann.farre@cil4sys.com> - Bug 542434
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.draw2d.ui.figures.BaseSlidableAnchor;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.NodeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CLifeLineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.swt.graphics.Color;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.OccurrenceSpecification;

/**
 * this class is used to display event of messages or event of execution specifications
 *
 */
public class DisplayEvent {

	/**
	 * delta use to create the region around an Event in which the mouse position is consider on the Event.
	 */
	private static final int EVENT_SELECTION_DELTA = 8;

	/**
	 * Edit Part on which the Event are displayed (i.e. : {@link CLifeLineEditPart})
	 */
	private EditPart editpart;

	/**
	 * Constructor.
	 *
	 */
	public DisplayEvent(EditPart editpart) {
		this.editpart = editpart;
	}

	public class EventFig extends Ellipse implements IEventFig {

	}

	/**
	 * if the position is the same a an event it return the event.
	 *
	 * @param container
	 *            the figure container
	 * @param locationOntheScreen
	 * @return the event under the position
	 */
	public MessageOccurrenceSpecification getMessageEvent(IFigure container, Point locationOntheScreen) {
		Point LocationAbsolute = locationOntheScreen.getCopy();
		IFigure editPartFigure = ((GraphicalEditPart) editpart).getFigure();
		editPartFigure.getParent().translateToRelative(LocationAbsolute);
		Rectangle recLoacal = new Rectangle(50, 200, 100, 100);

		IFigure fig = editPartFigure;
		Point ptOnscreen = recLoacal.getTopLeft().translate(fig.getBounds().getTopLeft());
		fig.getParent().translateToAbsolute(ptOnscreen);

		// display all events from messages
		Node node = (Node) editpart.getModel();
		@SuppressWarnings("unchecked")
		java.util.List<Edge> sourceEdge = node.getSourceEdges();
		for (Edge edge : sourceEdge) {
			MessageOccurrenceSpecification m = getMessageEvent((NodeEditPart) editpart, node, edge, LocationAbsolute);
			if (m != null) {
				return m;
			}
		}
		@SuppressWarnings("unchecked")
		java.util.List<Edge> targetEdge = node.getTargetEdges();
		for (Edge edge : targetEdge) {
			MessageOccurrenceSpecification m = getMessageEvent((NodeEditPart) editpart, node, edge, LocationAbsolute);
			if (m != null) {
				return m;
			}
		}

		return null;

	}

	/**
	 * if the position is the same a an event it return the event.
	 *
	 * @param container
	 *            the figure container
	 * @param locationOntheScreen
	 * @return the event under the position
	 */
	public OccurrenceSpecification getActionExecutionSpecificationEvent(IFigure container, Point locationOntheScreen) {
		Point LocationAbsolute = locationOntheScreen.getCopy();
		IFigure editPartFigure = ((GraphicalEditPart) editpart).getFigure();
		editPartFigure.getParent().translateToRelative(LocationAbsolute);
		Rectangle recLoacal = new Rectangle(50, 200, 100, 100);

		IFigure fig = editPartFigure;
		Point ptOnscreen = recLoacal.getTopLeft().translate(fig.getBounds().getTopLeft());
		fig.getParent().translateToAbsolute(ptOnscreen);

		// display all events from messages
		Node node = (Node) editpart.getModel();

		for (Object part : editpart.getChildren()) {
			if (part instanceof AbstractExecutionSpecificationEditPart) {
				OccurrenceSpecification occurrenceSpecification = getEventFromExecutionSpecification((NodeEditPart) editpart, (AbstractExecutionSpecificationEditPart) part, LocationAbsolute);
				if (occurrenceSpecification != null) {
					return occurrenceSpecification;
				}
			}

		}
		return null;

	}

	/**
	 * Get the Exact location of an Event around the location (from Execution Specification or Message )
	 *
	 * @param location
	 *            absolute value on the screen
	 *
	 * @return the location if no event found around, or the event precise location if existing
	 */
	public Point getRealEventLocation(final Point location) {
		Point realLocationEvent = getRealEventLocationFromExecutionSpecification(location.getCopy());
		if (location.equals(realLocationEvent)) {
			realLocationEvent = getRealEventLocationFromMessage(location.getCopy());
		}
		realLocationEvent = SequenceUtil.getSnappedLocation(editpart, realLocationEvent).getCopy();
		return realLocationEvent;
	}

	/**
	 * Get the Exact location of an Event from execution Specification
	 *
	 * @param location
	 *            absolute value on the screen
	 * @return the location if no event belonging to executionSpecification found around, or the event precise location if existing.
	 */
	public Point getRealEventLocationFromExecutionSpecification(final Point location) {

		// The Relative Position of the Mouse on screen
		Point relativeLocation = location.getCopy();
		IFigure editPartFigure = ((GraphicalEditPart) editpart).getFigure();
		editPartFigure.getParent().translateToRelative(relativeLocation);

		Point eventLocation = relativeLocation.getCopy();

		for (Object part : editpart.getChildren()) {
			if (part instanceof AbstractExecutionSpecificationEditPart) {
				AbstractExecutionSpecificationEditPart esEditPart = (AbstractExecutionSpecificationEditPart) part;

				int y = ((Bounds) (((Node) esEditPart.getNotationView()).getLayoutConstraint())).getY();
				int height = ((Bounds) (((Node) esEditPart.getNotationView()).getLayoutConstraint())).getHeight();

				int startY = y;
				int finishY = y + height;

				// First Event of the Execution Specification
				if (eventLocation.equals(relativeLocation)) {
					eventLocation = getNewEventLocationY(relativeLocation, editPartFigure.getBounds().y + startY, editPartFigure);
				}
				// Second Event of the Execution Specification
				if (eventLocation.equals(relativeLocation)) {
					eventLocation = getNewEventLocationY(relativeLocation, editPartFigure.getBounds().y + finishY, editPartFigure);
				}
			}
		}

		editPartFigure.getParent().translateToAbsolute(eventLocation);
		return eventLocation;
	}

	/**
	 * Get the new position of an event located at the reference Y if the mouse location is around (delta is 8px ) the reference value of Y
	 *
	 * @param relativeMouseLocation
	 *            Location of Mouse relatively to the Lifeline
	 * @param editPartFigure
	 *            Lifeline Figure to compute the Absolute Coordinate from the Interaction
	 * @param referenceY
	 *            The reference Y coordinate where the event is located
	 *
	 * @return a new Point in the absolute coordinate with the Y updated to the Event exact position or the initial position if not in the interval
	 */
	private Point getNewEventLocationY(Point relativeMouseLocation, int referenceY, IFigure editPartFigure) {
		Point newPoint = new Point(relativeMouseLocation);

		// Point newPoint = SequenceUtil.getSnappedLocation(editpart, relativeMouseLocation);
		if (referenceY - EVENT_SELECTION_DELTA < relativeMouseLocation.y() && relativeMouseLocation.y() < referenceY + EVENT_SELECTION_DELTA) {
			newPoint.setY(referenceY);
			newPoint.setX(relativeMouseLocation.x());
		}

		return newPoint;
	}

	/**
	 * Get the Exact location of an Event from Messages
	 *
	 * @param location
	 *            absolute value on the screen
	 * @return the location if no event belonging to Messages found around, or the event precise location if existing.
	 */
	public Point getRealEventLocationFromMessage(final Point location) {

		// The Relative Position of the Mouse on screen
		// Point relativeLocation = location.getCopy();
		Point relativeLocation = SequenceUtil.getSnappedLocation(editpart, location);
		IFigure editPartFigure = ((GraphicalEditPart) editpart).getFigure();
		editPartFigure.getParent().translateToRelative(relativeLocation);

		// The new Location
		Point eventLocation = relativeLocation.getCopy();

		// For Message Sources Event
		Node node = (Node) editpart.getModel();
		@SuppressWarnings("unchecked")
		java.util.List<Edge> sourceEdge = node.getSourceEdges();
		for (Edge edge : sourceEdge) {
			IdentityAnchor anchor = (IdentityAnchor) edge.getSourceAnchor();
			int y = getYfromAnchor(node, anchor);
			if (eventLocation.equals(relativeLocation)) {
				eventLocation = getNewEventLocationY(relativeLocation, editPartFigure.getBounds().y + y, editPartFigure);
			}
		}

		// For Message Target Event
		@SuppressWarnings("unchecked")
		java.util.List<Edge> targetEdges = node.getTargetEdges();
		for (Edge edge : targetEdges) {

			IdentityAnchor anchor = (IdentityAnchor) edge.getTargetAnchor();
			if (anchor != null) {
				int y = getYfromAnchor(node, anchor);

				if (eventLocation.equals(relativeLocation)) {
					eventLocation = getNewEventLocationY(relativeLocation, editPartFigure.getBounds().y + y, editPartFigure);
				}
			}
		}

		editPartFigure.getParent().translateToAbsolute(eventLocation);
		return eventLocation;
	}

	/**
	 * Get the y value of an anchor from the height ratio of a node
	 *
	 * @param node
	 *            where the Anchor is on
	 * @param anchor
	 *            the Anchor we want to compute the y
	 * @return the y relative to it's node. (here is a lifeline)
	 */
	public int getYfromAnchor(Node node, IdentityAnchor anchor) {
		PrecisionPoint point = BaseSlidableAnchor.parseTerminalString(anchor.getId());
		if (point == null) {
			return 0;
		}

		double yPercent = point.preciseY();

		// calculate bounds from notation
		int height = BoundForEditPart.getHeightFromView(node);

		int y = (int) (yPercent * height);
		return y;
	}

	/**
	 * @param lifelinedEditPArt
	 * @param part
	 *            the execution specification editpart
	 * @param locationAbsolute
	 *            the position of the mouse
	 */
	protected OccurrenceSpecification getEventFromExecutionSpecification(NodeEditPart lifelineEditPart, AbstractExecutionSpecificationEditPart executionSpecEditPart, Point locationAbsolute) {
		Node executionNode = (Node) executionSpecEditPart.getNotationView();
		ExecutionSpecification executionSpecification = (ExecutionSpecification) executionSpecEditPart.resolveSemanticElement();
		IFigure lifelineFigure = lifelineEditPart.getFigure();
		double y = ((Bounds) executionNode.getLayoutConstraint()).getY();
		if (lifelineFigure.getBounds().y + (int) y - EVENT_SELECTION_DELTA < locationAbsolute.y() && locationAbsolute.y() < lifelineFigure.getBounds().y + (int) y + EVENT_SELECTION_DELTA) {
			return executionSpecification.getStart();
		}

		y = ((Bounds) executionNode.getLayoutConstraint()).getY() + BoundForEditPart.getHeightFromView(executionNode);
		if (lifelineFigure.getBounds().y + (int) y - EVENT_SELECTION_DELTA < locationAbsolute.y() && locationAbsolute.y() < lifelineFigure.getBounds().y + (int) y + EVENT_SELECTION_DELTA) {
			return executionSpecification.getFinish();
		}
		return null;
	}

	/**
	 *
	 * @param container
	 *            the container edipart
	 * @param node
	 *            the node where edge are connected
	 * @param edge
	 *            the current edge
	 * @param currentPosition
	 *            the position of the mouse
	 * @return
	 */
	protected MessageOccurrenceSpecification getMessageEvent(NodeEditPart container, Node node, Edge edge, Point currentPosition) {
		IdentityAnchor anchor = null;
		if (edge.getSource().equals(node)) {
			anchor = (IdentityAnchor) edge.getSourceAnchor();
		} else {
			anchor = (IdentityAnchor) edge.getTargetAnchor();
		}
		double posY = getYfromAnchor(node, anchor);

		IFigure lifelineFigure = ((GraphicalEditPart) editpart).getFigure();
		if (lifelineFigure.getBounds().y + (int) posY - EVENT_SELECTION_DELTA < currentPosition.y() && currentPosition.y() < lifelineFigure.getBounds().y + (int) posY + EVENT_SELECTION_DELTA) {
			if ((edge.getElement() instanceof Message)) {
				if (anchor == (IdentityAnchor) edge.getSourceAnchor()) {
					return (MessageOccurrenceSpecification) ((Message) edge.getElement()).getSendEvent();
				} else {
					return (MessageOccurrenceSpecification) ((Message) edge.getElement()).getReceiveEvent();
				}
			}
		}
		return null;
	}

	/**
	 *
	 * @param container
	 *            the figure container where we want display event
	 * @param location
	 *            position on the screen
	 */
	public void addFigureEvent(IFigure container, Point locationOntheScreen) {
		Point LocationAbsolute = locationOntheScreen.getCopy();
		IFigure editPartFigure = ((GraphicalEditPart) editpart).getFigure();
		editPartFigure.getParent().translateToRelative(LocationAbsolute);
		Rectangle recLoacal = new Rectangle(50, 200, 100, 100);

		IFigure fig = editPartFigure;
		Point ptOnscreen = recLoacal.getTopLeft().translate(fig.getBounds().getTopLeft());
		fig.getParent().translateToAbsolute(ptOnscreen);

		// display all events from messages
		Node node = (Node) editpart.getModel();
		@SuppressWarnings("unchecked")
		java.util.List<Edge> sourceEdge = node.getSourceEdges();
		for (Edge edge : sourceEdge) {
			displayEventFromMessages((NodeEditPart) editpart, node, edge, LocationAbsolute);
		}
		@SuppressWarnings("unchecked")
		java.util.List<Edge> targetEdge = node.getTargetEdges();
		for (Edge edge : targetEdge) {
			displayEventFromMessages((NodeEditPart) editpart, node, edge, LocationAbsolute);
		}

		for (Object part : editpart.getChildren()) {
			if (part instanceof AbstractExecutionSpecificationEditPart) {
				displayEventFromExecutionSpecification((NodeEditPart) editpart, (AbstractExecutionSpecificationEditPart) part, LocationAbsolute);
			}
		}
	}

	/**
	 * @param lifelineEditPart
	 *            the lifeline editpart
	 * @param executionSpecEditPart
	 *            the execution specification
	 * @param locationAbsolute
	 *            the position of the mouse
	 */
	protected void displayEventFromExecutionSpecification(NodeEditPart lifelineEditPart, AbstractExecutionSpecificationEditPart executionSpecEditPart, Point locationAbsolute) {
		Node executionNode = (Node) executionSpecEditPart.getNotationView();
		double posY = ((Bounds) executionNode.getLayoutConstraint()).getY();
		addAnEvent(lifelineEditPart.getFigure(), posY, ColorConstants.white, locationAbsolute);
		posY = ((Bounds) executionNode.getLayoutConstraint()).getY() + BoundForEditPart.getHeightFromView(executionNode);
		addAnEvent(lifelineEditPart.getFigure(), posY, ColorConstants.white, locationAbsolute);
	}

	/**
	 *
	 * @param container
	 *            the container editpart
	 * @param node
	 *            the node where is connected the edge
	 * @param edge
	 *            the edge where want to display anchor
	 * @param CurrentPosition
	 *            the position of the mouse
	 */
	protected void displayEventFromMessages(NodeEditPart container, Node node, Edge edge, Point currentPosition) {
		IdentityAnchor anchor = null;
		if (edge.getSource().equals(node)) {
			anchor = (IdentityAnchor) edge.getSourceAnchor();
		} else {
			anchor = (IdentityAnchor) edge.getTargetAnchor();
		}

		if (null != anchor) {
			double posY = getYfromAnchor(node, anchor);

			addAnEvent(container.getFigure(), posY, ColorConstants.white, currentPosition);
		}
	}

	/**
	 * @param container
	 *            the figure container that will contain the event
	 * @param y
	 *            the location to display the event
	 * @param color
	 *            the color of the event
	 * @param currentPosition
	 *            the current position of the mouse to know if we display in green
	 */
	protected void addAnEvent(IFigure container, double y, Color color, Point currentPosition) {
		EventFig ellipseFigure = new EventFig();
		IFigure lifelineFigure = ((GraphicalEditPart) editpart).getFigure();
		// code without grid
		// calculate position of the Event in the screen references
		PrecisionRectangle EventBoundsOnScreen = new PrecisionRectangle(lifelineFigure.getBounds().x + lifelineFigure.getBounds().width / 2 - EVENT_SELECTION_DELTA, lifelineFigure.getBounds().y + (int) y - EVENT_SELECTION_DELTA, 16, 16);
		if (lifelineFigure.getBounds().y + (int) y - EVENT_SELECTION_DELTA < currentPosition.y() && currentPosition.y() < lifelineFigure.getBounds().y + (int) y + EVENT_SELECTION_DELTA) {
			ellipseFigure.setBackgroundColor(ColorConstants.green);
		} else {
			ellipseFigure.setBackgroundColor(color);
		}
		ellipseFigure.setLineWidth(2);
		ellipseFigure.setBounds(EventBoundsOnScreen);
		container.add(ellipseFigure);
	}

	/**
	 * use to remove all event from the figures
	 *
	 * @param container
	 *            the container figure
	 */
	public void removeFigureEvent(IFigure container) {
		ArrayList<IFigure> eventFigureList = new ArrayList<>();
		for (Object iFigure : container.getChildren()) {
			if (iFigure instanceof IEventFig) {
				eventFigureList.add((IFigure) iFigure);
			}
		}
		for (Iterator<IFigure> iterator = eventFigureList.iterator(); iterator.hasNext();) {
			IFigure iFigure = iterator.next();
			container.remove(iFigure);
		}
	}

}
