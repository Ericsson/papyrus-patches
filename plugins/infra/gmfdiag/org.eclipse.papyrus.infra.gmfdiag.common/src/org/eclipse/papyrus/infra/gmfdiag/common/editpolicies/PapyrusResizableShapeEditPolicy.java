/*****************************************************************************
 * Copyright (c) 2013 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *		CEA LIST - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.tools.ResizeTracker;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ResizableShapeEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.snap.ResizeTrackerWithPreferences;

/**
 *
 * See Bug 424943 ResizableEditPolicy#getResizeCommand duplicates request ignoring some request values
 *
 */
public class PapyrusResizableShapeEditPolicy extends ResizableShapeEditPolicy {

	/**
	 * The same {@link ChangeBoundsRequest} is sent to all moved edit parts,
	 * so we can cache the info about them in request potentially improving o(N^2) performance.
	 */
	private static final String PARAM_CACHED_EDIT_PARTS_SET = PapyrusResizableShapeEditPolicy.class.getName() + ":CachedMovedEPs";

	/**
	 * Tries to find the cached instance of {@link CachedEditPartsSet} in the request extended data map.
	 * If not found, initializes the new instance and caches it in request for other edit-policy instances.
	 * 
	 * @param req
	 * @return never returns <code>null</code>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected static CachedEditPartsSet getMovedEditPartsSet(ChangeBoundsRequest req) {
		Map extData = req.getExtendedData();
		CachedEditPartsSet set = (CachedEditPartsSet) extData.get(PARAM_CACHED_EDIT_PARTS_SET);
		if (set == null) {
			set = new CachedEditPartsSet(req.getEditParts());
			extData.put(PARAM_CACHED_EDIT_PARTS_SET, set);
		}
		return set;
	}

	/**
	 * This implementation overrides {@link org.eclipse.gef.editpolicies.NonResizableEditPolicy#getMoveCommand} and keeps all parent's logic.
	 * Multi-selection request identified by the presence of {@link PapyrusResizableShapeEditPolicy#PARAM_CACHED_EDIT_PARTS_SET} key
	 * for request extended data
	 * see {@link org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.XYLayoutWithConstrainedResizedEditPolicy#getConnectionsToElementsNotBeingMoved}
	 * 
	 * @param request
	 *            the change bounds request
	 * @return the command contribution to the request
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Command getMoveCommand(ChangeBoundsRequest request) {
		ChangeBoundsRequest req = new ChangeBoundsRequest(REQ_MOVE_CHILDREN);
		req.setEditParts(getHost());
		req.setMoveDelta(request.getMoveDelta());
		req.setSizeDelta(request.getSizeDelta());
		req.setLocation(request.getLocation());
		Map<Object, Object> extendedData = new HashMap<Object, Object>();
		extendedData.putAll(request.getExtendedData());
		CachedEditPartsSet set = new CachedEditPartsSet(request.getEditParts());
		extendedData.put(PARAM_CACHED_EDIT_PARTS_SET, set);
		req.setExtendedData(extendedData);
		return getHost().getParent().getCommand(req);
	}

	/**
	 * See Bug 424943 ResizableEditPolicy#getResizeCommand duplicates request ignoring some request values
	 * TODO : remove this override when the bug will be fixed
	 *
	 * Returns the command contribution for the given resize request. By
	 * default, the request is re-dispatched to the host's parent as a {@link org.eclipse.gef.RequestConstants#REQ_RESIZE_CHILDREN}. The
	 * parent's edit policies determine how to perform the resize based on the
	 * layout manager in use.
	 *
	 * @param request
	 *            the resize request
	 * @return the command contribution obtained from the parent
	 */

	@Override
	protected Command getResizeCommand(ChangeBoundsRequest request) {
		ChangeBoundsRequest req = new ChangeBoundsRequest(REQ_RESIZE_CHILDREN);
		req.setCenteredResize(request.isCenteredResize());
		req.setConstrainedMove(request.isConstrainedMove());
		req.setConstrainedResize(request.isConstrainedResize());
		req.setSnapToEnabled(request.isSnapToEnabled());
		req.setEditParts(getHost());

		req.setMoveDelta(request.getMoveDelta());
		req.setSizeDelta(request.getSizeDelta());
		req.setLocation(request.getLocation());
		req.setExtendedData(request.getExtendedData());
		req.setResizeDirection(request.getResizeDirection());

		if (getHost().getParent() == null) {
			return null;
		}

		return getHost().getParent().getCommand(req);
	}

	/**
	 *
	 * @see org.eclipse.gef.editpolicies.ResizableEditPolicy#getResizeTracker(int)
	 *
	 * @param direction
	 * @return
	 */
	@Override
	protected ResizeTracker getResizeTracker(int direction) {
		return new ResizeTrackerWithPreferences((GraphicalEditPart) getHost(), direction);
	}

	protected static enum MovedNodeKind {
		DIRECTLY, INDIRECTLY, NO
	}

	protected static class CachedEditPartsSet {

		private final Set<EditPart> myDirectlyMoved;

		private final Set<EditPart> myKnownIndirectlyYes;

		private final Set<EditPart> myKnownIndirectlyNo;

		public CachedEditPartsSet(List<EditPart> directlyMoved) {
			myDirectlyMoved = new HashSet<EditPart>(directlyMoved);
			myKnownIndirectlyNo = new HashSet<EditPart>(directlyMoved.size() * 5 + 1);
			myKnownIndirectlyYes = new HashSet<EditPart>(directlyMoved.size() * 5 + 1);
		}
		
		public boolean isMovedEditPart(EditPart ep) {
			return isMoved(ep) != MovedNodeKind.NO;
		}

		public MovedNodeKind isMoved(EditPart ep) {
			List<EditPart> chainUp = new LinkedList<EditPart>();
			EditPart cur = ep;
			MovedNodeKind kind = null;
			while (cur != null) {
				kind = getKnownKind(cur);
				if (kind != null) {
					break;
				}
				chainUp.add(cur);
				cur = cur.getParent();
			}
			if (cur == null || kind == null) {
				kind = MovedNodeKind.NO;
			} else if (kind == MovedNodeKind.DIRECTLY && cur != ep) {
				kind = MovedNodeKind.INDIRECTLY;
			}

			Set<EditPart> forKind;
			switch (kind) {
			case DIRECTLY:
				forKind = myDirectlyMoved;
				break;
			case INDIRECTLY:
				forKind = myKnownIndirectlyYes;
				break;
			case NO:
				forKind = myKnownIndirectlyNo;
				break;
			default:
				throw new IllegalArgumentException("Wow: " + kind);
			}

			if (kind != MovedNodeKind.DIRECTLY) {
				forKind.addAll(chainUp);
			}

			return kind;
		}

		private MovedNodeKind getKnownKind(EditPart ep) {
			if (myDirectlyMoved.contains(ep)) {
				return MovedNodeKind.DIRECTLY;
			}
			if (myKnownIndirectlyYes.contains(ep)) {
				return MovedNodeKind.INDIRECTLY;
			}
			if (myKnownIndirectlyNo.contains(ep)) {
				return MovedNodeKind.NO;
			}
			return null;
		}

	}

}
