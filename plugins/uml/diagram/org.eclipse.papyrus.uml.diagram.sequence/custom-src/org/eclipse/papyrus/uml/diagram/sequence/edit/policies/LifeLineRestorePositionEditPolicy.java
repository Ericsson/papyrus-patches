/*****************************************************************************
 * Copyright (c) 2017 CEA LIST, ALL4TEC and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import static org.eclipse.papyrus.uml.diagram.common.Activator.log;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.transaction.Transaction;
import org.eclipse.emf.workspace.AbstractEMFOperation;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeListener;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.common.core.util.StringStatics;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.SemanticEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramGraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.util.EditPartUtil;
import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageDeleteEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.figures.MessageCreate;
import org.eclipse.papyrus.uml.diagram.sequence.util.LifelineEditPartUtil;

/**
 * Edit policy to restore target lifeline line position after a deletion of a {@link MessageCreate}.
 *
 * @author Mickaël ADAM
 */
public class LifeLineRestorePositionEditPolicy extends SemanticEditPolicy implements NodeListener {

	/** Key for this edit policy. */
	public static final String KEY = "LIFELINE_RESTORE_POSITION_EDITPOLICY";//$NON-NLS-1$

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#activate()
	 */
	@Override
	public void activate() {
		// add listener on life line edit part
		EditPart host = getHost();
		if (host instanceof GraphicalEditPart) {
			((GraphicalEditPart) host).addNodeListener(this);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#deactivate()
	 */
	@Override
	public void deactivate() {
		EditPart host = getHost();
		if (host instanceof ConnectionEditPart) {
			((ConnectionEditPart) host).removeNodeListener(this);
		}
	}


	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Overridden to add the request parameters to inform if the event of the target is the first on the lifeline.
	 * </p>
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.SemanticEditPolicy#getSemanticCommand(org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest)
	 */
	@Override
	protected Command getSemanticCommand(IEditCommandRequest request) {
		return null;
	}


	/**
	 * If the removed connection is a MessageCreate, then we restore life line position.
	 *
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gef.NodeListener#removingTargetConnection(org.eclipse.gef.ConnectionEditPart, int)
	 */
	@Override
	public void removingTargetConnection(final ConnectionEditPart connection, final int index) {
		if (connection instanceof MessageCreateEditPart) {
			Command restoreLifelinePositionCommand = LifelineEditPartUtil.getRestoreLifelinePositionOnMessageCreateRemovedCommand(connection);
			if (null != restoreLifelinePositionCommand && restoreLifelinePositionCommand.canExecute()) {
				executeCommand(restoreLifelinePositionCommand);
			}
		} else if (connection instanceof MessageDeleteEditPart) {
			Command restoreLifelinePositionCommand = LifelineEditPartUtil.getRestoreLifelinePositionOnMessageDeleteRemovedCommand(connection);
			if (null != restoreLifelinePositionCommand && restoreLifelinePositionCommand.canExecute()) {
				executeCommand(restoreLifelinePositionCommand);
			}
		}
	}


	/**
	 * Executes the supplied command inside an <code>unchecked action</code>
	 *
	 * @param cmd
	 *            command that can be executed (i.e., cmd.canExecute() == true)
	 */
	protected void executeCommand(final Command cmd) {
		Map<String, Boolean> options = null;
		EditPart ep = getHost();
		boolean isActivating = true;
		// use the viewer to determine if we are still initializing the diagram
		// do not use the DiagramEditPart.isActivating since
		// ConnectionEditPart's
		// parent will not be a diagram edit part
		EditPartViewer viewer = ep.getViewer();
		if (viewer instanceof DiagramGraphicalViewer) {
			isActivating = ((DiagramGraphicalViewer) viewer).isInitializing();
		}

		if (isActivating || !EditPartUtil.isWriteTransactionInProgress((IGraphicalEditPart) getHost(), false, false)) {
			options = Collections.singletonMap(Transaction.OPTION_UNPROTECTED, Boolean.TRUE);
		}

		AbstractEMFOperation operation = new AbstractEMFOperation(((IGraphicalEditPart) getHost()).getEditingDomain(), StringStatics.BLANK, options) {

			@Override
			protected IStatus doExecute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				cmd.execute();
				return Status.OK_STATUS;
			}
		};
		try {
			operation.execute(new NullProgressMonitor(), null);
		} catch (ExecutionException e) {
			log.error(e);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gef.NodeListener#removingSourceConnection(org.eclipse.gef.ConnectionEditPart, int)
	 */
	@Override
	public void removingSourceConnection(final ConnectionEditPart connection, final int index) {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gef.NodeListener#sourceConnectionAdded(org.eclipse.gef.ConnectionEditPart, int)
	 */
	@Override
	public void sourceConnectionAdded(final ConnectionEditPart connection, final int index) {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gef.NodeListener#targetConnectionAdded(org.eclipse.gef.ConnectionEditPart, int)
	 */
	@Override
	public void targetConnectionAdded(final ConnectionEditPart connection, final int index) {
		// Do nothing
	}
}