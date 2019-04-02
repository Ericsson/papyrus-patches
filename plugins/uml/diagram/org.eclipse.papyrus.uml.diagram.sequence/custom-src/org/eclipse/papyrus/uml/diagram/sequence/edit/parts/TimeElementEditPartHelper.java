/*****************************************************************************
 * Copyright (c) 2018 Christian W. Damus, CEA LIST, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.Disposable;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.figures.IBorderItemLocator;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.papyrus.infra.core.utils.OneShotExecutor;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.DiagramHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramEditPartsUtil;
import org.eclipse.papyrus.infra.ui.util.TransactionUIHelper;
import org.eclipse.uml2.uml.MessageEnd;

/**
 * Common behaviour that time-element edit-parts can delegate.
 */
class TimeElementEditPartHelper {
	private static final MessageTracker NONE = new MessageTracker();

	private final IGraphicalEditPart owner;
	private final Supplier<? extends Optional<MessageEnd>> messageEndSupplier;
	private final Executor executor;
	private MessageTracker messageTracker = NONE;

	/**
	 * Initializes me with the edit-part that I help.
	 *
	 * @param owner
	 *            my owner
	 * @param messageEndSupplier
	 *            extracts the message-end from my {@code owner}'s semantic element
	 */
	public TimeElementEditPartHelper(IGraphicalEditPart owner,
			Supplier<? extends Optional<MessageEnd>> messageEndSupplier) {

		super();

		this.owner = owner;
		this.messageEndSupplier = messageEndSupplier;

		// Don't post redundant refreshes
		this.executor = new OneShotExecutor(TransactionUIHelper.getExecutor(owner.getEditingDomain()));
	}

	boolean refreshBounds(IBorderItemLocator locator) {
		boolean result = false;

		if (locator != null) {
			Optional<Point> messageEndLoc = Optional.ofNullable(getLocation());
			if (messageEndLoc.isPresent()) {
				// We are fixed by a message end, then
				Dimension size = new Dimension(
						((Integer) owner.getStructuralFeatureValue(NotationPackage.eINSTANCE.getSize_Width())).intValue(),
						((Integer) owner.getStructuralFeatureValue(NotationPackage.eINSTANCE.getSize_Height())).intValue());

				locator.setConstraint(new Rectangle(messageEndLoc.get(), size));
				result = true;
			}
		}

		return result;
	}

	// Asynchronously post a refresh of my owner edit-part
	private void postRefresh() {
		executor.execute(() -> DiagramHelper.refresh(owner, true));
	}

	/**
	 * Obtain the message end that is my observedor constrained event, if any.
	 *
	 * @return my message end, or {@code null} if I do not observe or constrain a message end
	 */
	Optional<MessageEnd> getMessageEnd() {
		return messageEndSupplier.get();
	}

	/**
	 * Compute the location of a message end.
	 *
	 * @param messageEnd
	 *            a message end
	 * @return the location of that end relative to my parent, or {@code null} if it cannot
	 *         be determined
	 */
	Point getLocation() {
		return getMessage().getLocation();
	}

	private MessageTracker getMessage() {
		if (!messageTracker.isValid()) {
			messageTracker.dispose();

			// Refresh our idea of what the message is
			messageTracker = messageEndSupplier.get()
					.map(end -> new MessageTracker(this, end))
					.filter(MessageTracker::isValid)
					.orElse(NONE);
		}

		return messageTracker;
	}

	//
	// Nested types
	//

	/**
	 * A helper for tracking the message, if any, the end of which is linked to a time
	 * element to cause that time element to follow (track) the movement of the message
	 * end.
	 */
	private static final class MessageTracker implements Supplier<ConnectionEditPart>, Disposable {
		private final PropertyChangeListener connectionListener = this::connectionMoved;
		private final TimeElementEditPartHelper owner;
		private final ConnectionEditPart connectionEP;
		private final Connection connection;
		private final boolean source;
		private final Point anchor = new Point();

		MessageTracker() {
			super();

			this.owner = null;
			connectionEP = null;
			connection = null;
			source = false;
		}

		MessageTracker(TimeElementEditPartHelper owner, MessageEnd end) {
			super();

			this.owner = owner;

			IGraphicalEditPart ep = DiagramEditPartsUtil.getChildByEObject(end.getMessage(),
					(IGraphicalEditPart) owner.owner.getRoot().getContents(), true);
			if ((ep instanceof ConnectionEditPart) && ep.isActive()) {
				connectionEP = (ConnectionEditPart) ep;
				connection = (Connection) connectionEP.getFigure();
				connection.addPropertyChangeListener(Connection.PROPERTY_POINTS, connectionListener);
				source = end.isSend();
				anchor.setLocation(source ? connection.getPoints().getFirstPoint() : connection.getPoints().getLastPoint());
			} else {
				connectionEP = null;
				connection = null;
				source = false;
			}
		}

		@Override
		public void dispose() {
			if (connection != null) {
				connection.removePropertyChangeListener(Connection.PROPERTY_POINTS, connectionListener);
			}
		}

		@Override
		public ConnectionEditPart get() {
			return isValid() ? connectionEP : null;
		}

		public Point getLocation() {
			Point result = null;

			if (isValid()) {
				// If we were ever valid, we have a connection
				ConnectionAnchor anchor = source ? connection.getSourceAnchor() : connection.getTargetAnchor();
				result = anchor.getReferencePoint().getCopy();
				owner.owner.getFigure().getParent().translateToRelative(result);
			}

			return result;
		}

		boolean isValid() {
			return (connectionEP != null) && connectionEP.isActive();
		}

		private void connectionMoved(PropertyChangeEvent event) {
			PointList points = (PointList) event.getNewValue();
			Point newAnchor = source ? points.getFirstPoint() : points.getLastPoint();
			if (!anchor.equals(newAnchor)) {
				anchor.setLocation(newAnchor);
				owner.postRefresh();
			}
		}
	}

}
