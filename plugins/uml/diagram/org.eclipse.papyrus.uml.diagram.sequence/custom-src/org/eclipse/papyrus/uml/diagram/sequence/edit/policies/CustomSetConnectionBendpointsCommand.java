/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.ui.editparts.INodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.RelativeBendpoints;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.gmf.runtime.notation.datatype.RelativeBendpoint;

/**
 * This class redefine the SetConnectionBendpointsCommand with the anchors calculation during the execution instead of during the initialisation.
 * @since 5.0
 */
public class CustomSetConnectionBendpointsCommand extends AbstractTransactionalCommand {

	/**
	 * The edge adaptor.
	 */
	private IAdaptable edgeAdaptor;

	/**
	 * The reconnect request.
	 */
	private ReconnectRequest request;

	/**
	 * The node edit part.
	 */
	private INodeEditPart node;

	/**
	 * Constructor.
	 *
	 * @param editingDomain
	 *            the transactionnal editing domain.
	 */
	public CustomSetConnectionBendpointsCommand(final TransactionalEditingDomain editingDomain) {
		super(editingDomain, DiagramUIMessages.Commands_SetBendpointsCommand_Label, null);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand#getAffectedFiles()
	 */
	@Override
	public List<?> getAffectedFiles() {
		View view = edgeAdaptor.getAdapter(View.class);
		if (view != null) {
			return getWorkspaceFiles(view);
		}
		return super.getAffectedFiles();
	}

	/**
	 * Gets the edge adaptor.
	 *
	 * @return The edgeAdapter.
	 */
	public IAdaptable getEdgeAdaptor() {
		return edgeAdaptor;
	}

	/**
	 * Sets the edge adaptor.
	 *
	 * @param edgeAdapter
	 *            The edgeAdapter to set.
	 */
	public void setEdgeAdaptor(final IAdaptable edgeAdapter) {
		this.edgeAdaptor = edgeAdapter;
	}

	/**
	 * Gets the request.
	 *
	 * @return The request.
	 */
	public ReconnectRequest getRequest() {
		return request;
	}

	/**
	 * Sets the request.
	 *
	 * @param request
	 *            The request to set.
	 */
	public void setRequest(final ReconnectRequest request) {
		this.request = request;
	}

	/**
	 * Gets the node.
	 *
	 * @return The node.
	 */
	public INodeEditPart getNode() {
		return node;
	}

	/**
	 * Sets the node.
	 *
	 * @param node
	 *            The node to set.
	 */
	public void setNode(final INodeEditPart node) {
		this.node = node;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand#doExecuteWithResult(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	protected CommandResult doExecuteWithResult(final IProgressMonitor progressMonitor, final IAdaptable info)
			throws ExecutionException {

		Assert.isNotNull(request);

		final Edge edge = getEdgeAdaptor().getAdapter(Edge.class);
		Assert.isNotNull(edge);

		final ConnectionAnchor sourceAnchor = node.getSourceConnectionAnchor(request);
		final ConnectionAnchor targetAnchor = node.getTargetConnectionAnchor(request);

		final Point sourceRefPoint = sourceAnchor.getReferencePoint();
		final Point targetRefPoint = targetAnchor.getReferencePoint();

		final PointList pointList = new PointList();
		pointList.addPoint(sourceAnchor.getLocation(targetRefPoint));
		pointList.addPoint(targetAnchor.getLocation(sourceRefPoint));

		final List<RelativeBendpoint> newBendpoints = new ArrayList<>();
		for (short i = 0; i < pointList.size(); i++) {
			final Dimension s = pointList.getPoint(i).getDifference(sourceRefPoint);
			final Dimension t = pointList.getPoint(i).getDifference(targetRefPoint);
			newBendpoints.add(new RelativeBendpoint(s.width, s.height, t.width, t.height));
		}

		final RelativeBendpoints points = (RelativeBendpoints) edge.getBendpoints();
		points.setPoints(newBendpoints);
		return CommandResult.newOKCommandResult();
	}


}
