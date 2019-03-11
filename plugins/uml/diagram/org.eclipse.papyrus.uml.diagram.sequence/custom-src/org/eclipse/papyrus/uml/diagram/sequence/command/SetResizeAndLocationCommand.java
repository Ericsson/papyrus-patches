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
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.command;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;

/**
 * This allows to redefine the SetBoundsCommand into 3 different commands. This one manage a resize before a relocate because the listeners of the life lines need this order.
 */
public class SetResizeAndLocationCommand extends AbstractTransactionalCommand {

	/**
	 * The adapter to the <code>View</code>.
	 */
	private IAdaptable adapter;

	/**
	 * The bounds to set.
	 */
	private Rectangle bounds;

	/**
	 * Creates a <code>CustomSetBoundsCommand</code> for the given view adapter with a given bounds.
	 *
	 * @param editingDomain
	 *            the editing domain through which model changes are made.
	 * @param label
	 *            The command label.
	 * @param adapter
	 *            An adapter to the <code>View</code>.
	 * @param bounds
	 *            The new bounds.
	 */
	public SetResizeAndLocationCommand(final TransactionalEditingDomain editingDomain, final String label, final IAdaptable adapter, final Rectangle bounds) {
		super(editingDomain, label, null);
		Assert.isNotNull(adapter, "view cannot be null"); //$NON-NLS-1$
		Assert.isNotNull(bounds, "bounds cannot be null"); //$NON-NLS-1$
		this.adapter = adapter;
		this.bounds = bounds;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand#doExecuteWithResult(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	protected CommandResult doExecuteWithResult(final IProgressMonitor monitor, final IAdaptable info)
			throws ExecutionException {

		if (adapter == null) {
			return CommandResult.newErrorCommandResult("SetResizeAndLocationCommand: viewAdapter does not adapt to IView.class"); //$NON-NLS-1$
		}

		final View view = adapter.getAdapter(View.class);

		if (bounds != null) {
			final Point location = bounds.getLocation();
			final Dimension size = bounds.getSize();
			ViewUtil.setStructuralFeatureValue(view, NotationPackage.eINSTANCE.getSize_Width(), Integer.valueOf(size.width));
			ViewUtil.setStructuralFeatureValue(view, NotationPackage.eINSTANCE.getSize_Height(), Integer.valueOf(size.height));
			ViewUtil.setStructuralFeatureValue(view, NotationPackage.eINSTANCE.getLocation_X(), Integer.valueOf(location.x));
			ViewUtil.setStructuralFeatureValue(view, NotationPackage.eINSTANCE.getLocation_Y(), Integer.valueOf(location.y));
		}
		return CommandResult.newOKCommandResult();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand#getAffectedFiles()
	 */
	@Override
	public List<?> getAffectedFiles() {
		if (adapter != null) {
			View view = adapter.getAdapter(View.class);
			if (view != null) {
				return getWorkspaceFiles(view);
			}
		}
		return super.getAffectedFiles();
	}

}
