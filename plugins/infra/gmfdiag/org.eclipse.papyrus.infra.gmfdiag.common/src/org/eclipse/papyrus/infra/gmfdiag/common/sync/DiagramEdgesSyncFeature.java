/*****************************************************************************
 * Copyright (c) 2015 Christian W. Damus and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.sync;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.SemanticElementAdapter;
import org.eclipse.papyrus.infra.gmfdiag.common.service.visualtype.VisualTypeService;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramEditPartsUtil;
import org.eclipse.papyrus.infra.sync.SyncBucket;

/**
 * Synchronization feature for the edges in a diagram. It is generally a good idea to add edge features to a bucket
 * only after all of the node features, so that nodes are synchronized before the edges that connect them.
 */
public abstract class DiagramEdgesSyncFeature<M extends EObject, N extends EObject, T extends EditPart> extends AbstractNestedDiagramViewsSyncFeature<M, N, T> {
	/**
	 * Initializes me with my controlling bucket.
	 *
	 * @param bucket
	 *            The bucket doing the synchronization
	 */
	public DiagramEdgesSyncFeature(SyncBucket<M, T, Notification> bucket) {
		super(bucket, NotationPackage.Literals.DIAGRAM__PERSISTED_EDGES, NotationPackage.Literals.DIAGRAM__TRANSIENT_EDGES);
	}

	/**
	 * Gets the diagram edit part containing (recursively) the given {@code parent}.
	 *
	 * @param parent
	 *            The edit part we work on
	 * @return The containing diagram
	 */
	@Override
	protected DiagramEditPart getEffectiveEditPart(EditPart parent) {
		return DiagramEditPartsUtil.getDiagramEditPart(parent);
	}

	@SuppressWarnings("unchecked")
	@Override
	Iterable<? extends T> basicGetContents(T backend) {
		return getEffectiveEditPart(backend).getConnections();
	}

	@Override
	protected EObject getNotifier(T backend) {
		EObject result;

		if (backend instanceof ConnectionEditPart) {
			// The connection's view is the proper notifier
			result = (View) backend.getModel();
		} else {
			result = super.getNotifier(backend);
		}

		return result;
	}

	@Override
	protected CreateRequest getCreateRequest(IGraphicalEditPart parentPart, EObject element, Point atLocation) {
		CreateConnectionViewRequest result = null;

		DiagramEditPart diagramPart = DiagramEditPartsUtil.getDiagramEditPart(parentPart);
		Diagram diagram = diagramPart.getDiagramView();
		EObject source = getSourceElement(element);
		EObject target = getTargetElement(element);

		EditPart sourcePart = DiagramEditPartsUtil.getChildByEObject(source, diagramPart, false);
		EditPart targetPart = DiagramEditPartsUtil.getChildByEObject(target, diagramPart, false);

		if ((sourcePart != null) && (targetPart != null)) {
			// Consult the visual type service to get the appropriate view type
			String viewType = VisualTypeService.getInstance().getLinkType(diagram, element);
			if (viewType != null) {
				IElementType elementType = VisualTypeService.getInstance().getElementType(diagram, viewType);
				IAdaptable elementAdapter = new SemanticElementAdapter(element, elementType);

				CreateConnectionViewRequest.ConnectionViewDescriptor descriptor = new CreateConnectionViewRequest.ConnectionViewDescriptor(
						elementAdapter,
						viewType,
						parentPart.getDiagramPreferencesHint());
				result = new CreateConnectionViewRequest(descriptor);
				result.setLocation(atLocation);
				result.setSourceEditPart(sourcePart);
				result.setTargetEditPart(targetPart);
			}
		}

		return result;
	}

	/**
	 * Queries the source element of a semantic connection.
	 * 
	 * @param connectionElement
	 *            a semantic connection element
	 * 
	 * @return its source element
	 */
	protected abstract EObject getSourceElement(EObject connectionElement);

	/**
	 * Queries the target element of a semantic connection.
	 * 
	 * @param connectionElement
	 *            a semantic connection element
	 * 
	 * @return its target element
	 */
	protected abstract EObject getTargetElement(EObject connectionElement);

	@Override
	protected org.eclipse.gef.commands.Command getCreateCommand(IGraphicalEditPart parentPart, CreateRequest request) {
		// Initialize the command
		request.setType(RequestConstants.REQ_CONNECTION_START);
		((CreateConnectionViewRequest) request).getSourceEditPart().getCommand(request);
		request.setType(RequestConstants.REQ_CONNECTION_END);

		// Get the command
		return ((CreateConnectionViewRequest) request).getTargetEditPart().getCommand(request);
	}

}
