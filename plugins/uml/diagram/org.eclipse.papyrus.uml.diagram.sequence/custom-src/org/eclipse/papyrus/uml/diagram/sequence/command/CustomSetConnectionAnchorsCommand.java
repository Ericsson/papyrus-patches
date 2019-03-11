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

package org.eclipse.papyrus.uml.diagram.sequence.command;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.ui.editparts.INodeEditPart;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.View;

/**
 * This class redefine the SetConnectionAnchorsCommand with the position calculation during the execution instead of in the initialisation.
 */
public class CustomSetConnectionAnchorsCommand extends AbstractTransactionalCommand {

	/**
	 * The edge adaptor.
	 */
	private IAdaptable edgeAdaptor;

	/**
	 * The node edit part.
	 */
	private INodeEditPart node;

	/**
	 * The source reconnect request.
	 */
	private ReconnectRequest sourceReconnectRequest;

	/**
	 * Boolean to determinate if there is a source reconnection in this command.
	 */
	private boolean isSourceSet;

	/**
	 * The target reconnect request.
	 */
	private ReconnectRequest targetReconnectRequest;

	/**
	 * Boolean to determinate if there is a target reconnection in this command.
	 */
	private boolean isTargetSet;

	/**
	 * Constructor.
	 *
	 * @param editingDomain
	 *            The editing domain through which model changes are made.
	 * @param label
	 *            The command label.
	 */
	public CustomSetConnectionAnchorsCommand(final TransactionalEditingDomain editingDomain, final String label) {
		super(editingDomain, label, null);
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
	 * @return IAdaptable The edge adapter.
	 */
	public IAdaptable getEdgeAdaptor() {
		return edgeAdaptor;
	}

	/**
	 * Sets the edge adaptor.
	 *
	 * @param edgeAdaptor
	 *            The edgeAdaptor to set.
	 */
	public void setEdgeAdaptor(final IAdaptable edgeAdaptor) {
		this.edgeAdaptor = edgeAdaptor;
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
	 * Gets the source ReconnectRequest.
	 *
	 * @return The source ReconnectRequest.
	 */
	public ReconnectRequest getSourceReconnectRequest() {
		return sourceReconnectRequest;
	}

	/**
	 * Sets the source ReconnectRequest.
	 *
	 * @param sourceReconnectRequest
	 *            The source ReconnectRequest to set.
	 */
	public void setSourceReconnectRequest(final ReconnectRequest sourceReconnectRequest) {
		this.sourceReconnectRequest = sourceReconnectRequest;
	}

	/**
	 * Gets the target ReconnectRequest.
	 *
	 * @return The target ReconnectRequest.
	 */
	public ReconnectRequest getTargetReconnectRequest() {
		return targetReconnectRequest;
	}

	/**
	 * Sets the target ReconnectRequest.
	 *
	 * @param targetReconnectRequest
	 *            The target ReconnectRequest to set.
	 */
	public void setTargetReconnectRequest(final ReconnectRequest targetReconnectRequest) {
		this.targetReconnectRequest = targetReconnectRequest;
	}

	/**
	 * Sets the isSourceSet.
	 *
	 * @return the isSourceSet
	 */
	public boolean isSourceSet() {
		return isSourceSet;
	}

	/**
	 * Gets the isSourceSet.
	 *
	 * @param isSourceSet
	 *            The isSourceSet to set.
	 */
	public void setSourceSet(final boolean isSourceSet) {
		this.isSourceSet = isSourceSet;
	}

	/**
	 * Sets the isTargetSet.
	 *
	 * @return the isTargetSet
	 */
	public boolean isTargetSet() {
		return isTargetSet;
	}

	/**
	 * Gets the isTargetSet.
	 *
	 * @param isSourceSet
	 *            The isSourceSet to set.
	 */
	public void setTargetSet(final boolean isTargetSet) {
		this.isTargetSet = isTargetSet;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand#doExecuteWithResult(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	protected CommandResult doExecuteWithResult(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {
		assert null != edgeAdaptor : "Null child in CustomSetConnectionAnchorsCommand"; //$NON-NLS-1$

		final Edge edge = getEdgeAdaptor().getAdapter(Edge.class);
		assert null != edge : "Null edge in CustomSetConnectionAnchorsCommand"; //$NON-NLS-1$

		if (isSourceSet()) {
			final ConnectionAnchor sourceAnchor = getNode().getSourceConnectionAnchor(getSourceReconnectRequest());
			final String newSourceTerminal = getNode().mapConnectionAnchorToTerminal(sourceAnchor);

			if (newSourceTerminal.length() == 0) {
				edge.setSourceAnchor(null);
			} else {
				IdentityAnchor a = (IdentityAnchor) edge.getSourceAnchor();
				if (a == null) {
					a = NotationFactory.eINSTANCE.createIdentityAnchor();
				}
				a.setId(newSourceTerminal);
				edge.setSourceAnchor(a);
			}
		}

		if (isTargetSet()) {
			final ConnectionAnchor targetAnchor = getNode().getTargetConnectionAnchor(getTargetReconnectRequest());
			final String newTargetTerminal = getNode().mapConnectionAnchorToTerminal(targetAnchor);

			if (newTargetTerminal.length() == 0) {
				edge.setTargetAnchor(null);
			} else {
				IdentityAnchor a = (IdentityAnchor) edge.getTargetAnchor();
				if (a == null) {
					a = NotationFactory.eINSTANCE.createIdentityAnchor();
				}
				a.setId(newTargetTerminal);
				edge.setTargetAnchor(a);
			}
		}

		return CommandResult.newOKCommandResult();
	}
}
