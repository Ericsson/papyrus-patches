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

package org.eclipse.papyrus.infra.gmfdiag.common.commands;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.CanonicalStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.Style;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.IPapyrusCanonicalEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramEditPartsUtil;

import com.google.common.collect.Iterables;

/**
 * A command that sets the canonical synchronization state of an {@link EditPart}.
 */
public class SetCanonicalCommand extends AbstractTransactionalCommand {
	private View view;
	private boolean canonical;

	public SetCanonicalCommand(TransactionalEditingDomain domain, View view, boolean canonical) {
		super(domain, "Set Canonical", getWorkspaceFiles(view));

		this.view = view;
		this.canonical = canonical;
	}

	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		CommandResult result = null;

		Iterable<EditPart> editParts = DiagramEditPartsUtil.findEditParts(view);
		if (Iterables.isEmpty(editParts)) {
			result = CommandResult.newErrorCommandResult("Notation view has no EditParts");
		} else {
			setCanonical(view, canonical);
			refreshCanonical(editParts);
			result = CommandResult.newOKCommandResult(canonical);
		}

		return result;
	}

	protected void setCanonical(View view, boolean canonical) {
		CanonicalStyle style = (CanonicalStyle) view.getStyle(NotationPackage.Literals.CANONICAL_STYLE);
		if (style == null) {
			style = (CanonicalStyle) view.createStyle(NotationPackage.Literals.CANONICAL_STYLE);
		}
		style.setCanonical(canonical);
		if (style.eContainer() == null) {
			// It's a CSS-inferred style. Persist it
			@SuppressWarnings("unchecked")
			List<Style> styles = view.getStyles();
			styles.add(style);
		}
	}

	protected void refreshCanonical(Iterable<? extends EditPart> editParts) {
		for (Iterator<? extends EditPart> all = DiagramEditPartsUtil.getAllContents(editParts); all.hasNext();) {
			// Make sure that we record the creation of new views now for potential undo/redo
			EditPolicy editPolicy = all.next().getEditPolicy(EditPolicyRoles.CANONICAL_ROLE);
			if (editPolicy instanceof IPapyrusCanonicalEditPolicy) {
				// Now it should be able to activate or deactivate
				((IPapyrusCanonicalEditPolicy) editPolicy).refreshActive();
			}
		}
	}

	@Override
	protected IStatus doUndo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		IStatus result = super.doUndo(monitor, info);

		refreshCanonical(DiagramEditPartsUtil.findEditParts(view));

		return result;
	}

	@Override
	protected IStatus doRedo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		IStatus result = super.doRedo(monitor, info);

		refreshCanonical(DiagramEditPartsUtil.findEditParts(view));

		return result;
	}
}
