/*****************************************************************************
 * Copyright (c) 2014, 2015 CEA LIST, Christian W. Damus, and others.
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
 *   Christian W. Damus - bug 465416
 *
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.sync;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.commands.SetBoundsCommand;
import org.eclipse.gmf.runtime.notation.Location;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.papyrus.infra.emf.gmf.command.GMFtoEMFCommandWrapper;
import org.eclipse.papyrus.infra.sync.EMFDispatch;
import org.eclipse.papyrus.infra.sync.EMFDispatchManager;
import org.eclipse.papyrus.infra.sync.SyncBucket;
import org.eclipse.papyrus.infra.sync.SyncFeature;
import org.eclipse.papyrus.infra.sync.SyncItem;
import org.eclipse.papyrus.infra.tools.util.TypeUtils;

/**
 * Represents a synchronization feature for the position of GMF notation nodes
 *
 * @author Laurent Wouters
 *
 * @param <M>
 *            The type of the underlying model element common to all synchronized items in a single bucket
 * @param <T>
 *            The type of the backend element to synchronize
 */
public class NodePositionSyncFeature<M extends EObject, T extends EditPart> extends SyncFeature<M, T, Notification> {
	/**
	 * Represents a dispatcher for this feature
	 *
	 * @author Laurent Wouters
	 */
	private class Dispatcher extends NodePositionSyncDispatcher<M, T> {
		public Dispatcher(SyncItem<M, T> item) {
			super(item);
		}

		@Override
		public void onClear() {
			// clears the parent bucket
			getBucket().clear();
		}

		@Override
		protected void onFilteredChange(Notification notification) {
			NodePositionSyncFeature.this.onChange(item, notification);
		}
	}

	private EMFDispatchManager<Dispatcher> dispatchMgr = createSingleDispatchManager();

	/**
	 * Initialized this feature
	 *
	 * @param bucket
	 *            The bucket doing the synchronization
	 */
	public NodePositionSyncFeature(SyncBucket<M, T, Notification> bucket) {
		super(bucket);
	}

	@Override
	public void observe(SyncItem<M, T> item) {
		dispatchMgr.add(item, new Dispatcher(item));
	}

	@Override
	public void unobserve(SyncItem<M, T> item) {
		dispatchMgr.remove(item);
	}

	@Override
	protected void onClear() {
		dispatchMgr.removeAll();
	}

	@Override
	public void synchronize(SyncItem<M, T> from, SyncItem<M, T> to, Notification message) {
		EditPart fromEditPart = from.getBackend();
		EditPart toEditPart = to.getBackend();

		// retrieve the locations
		Point locationFrom = getLocation(fromEditPart);
		Point locationTo = getLocation(toEditPart);

		if (!locationFrom.equals(locationTo)) {
			// compute the reaction command
			Command reaction = GMFtoEMFCommandWrapper.wrap(new SetBoundsCommand(getEditingDomain(), "Synchronize Node Location", toEditPart, locationFrom));

			// dispatch the reaction
			if (message == null) {
				// this is an initial sync request
				execute(reaction);
			} else {
				// this is reaction to a change
				Dispatcher dispatcher = dispatchMgr.getDispatcher(from, message.getFeature());
				if (dispatcher != null) {
					dispatcher.react(reaction);
				}
			}
		}
	}

	Point getLocation(EditPart editPart) {
		Point result = null;

		Node node = (Node) editPart.getModel();
		Location location = TypeUtils.as(node.getLayoutConstraint(), Location.class);
		if (location != null) {
			result = new Point(location.getX(), location.getY());
		} else {
			IFigure figure = ((GraphicalEditPart) editPart).getFigure();
			result = figure.getBounds().getLocation();
		}

		return result;
	}

	public static <M extends EObject, T extends EditPart> NotationSyncPolicyDelegate<M, T> createPolicyDelegate() {
		return new NotationSyncPolicyDelegate<M, T>(NotationPackage.Literals.LOCATION.getName()) {

			@Override
			protected EMFDispatch createDispatcher(SyncItem<M, T> syncTarget) {
				return new NodePositionSyncDispatcher<M, T>(syncTarget) {
					@Override
					public void onClear() {
						// Nothing to do do
					}

					@Override
					protected void onFilteredChange(Notification notification) {
						overrideOccurred(this, getItem());
					}
				};
			}
		};
	}
}
