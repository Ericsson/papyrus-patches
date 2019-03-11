/*****************************************************************************
 * Copyright (c) 2014 CEA LIST.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.gef.ui.internal.editpolicies.GraphicalEditPolicyEx;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.commands.wrappers.GEFtoEMFCommandWrapper;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.databinding.custom.CustomStringStyleObservableValue;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.PapyrusLabelEditPart;

/**
 * this edit policy has in charge to refresh the edit part when text alignment change.
 */
public class RefreshTextAlignmentEditPolicy extends GraphicalEditPolicyEx implements IChangeListener {

	/** key for this edit policy. */
	public static final String REFRESH_TEXT_ALIGNMENT_EDITPOLICY = "Refresh text alignment edit policy";//$NON-NLS-N$

	/** The style observable. */
	protected IObservableValue styleObservable;

	/** The view. */
	private View view;

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void activate() {
		// retrieve the view and the element managed by the edit part
		view = (View) getHost().getModel();
		if (view == null) {
			return;
		}

		if (getHost() instanceof PapyrusLabelEditPart) {
			// add style observable value
			styleObservable = new CustomStringStyleObservableValue(view, EMFHelper.resolveEditingDomain(view), PapyrusLabelEditPart.TEXT_ALIGNMENT);
			styleObservable.addChangeListener(this);
		}
		getHost().refresh();
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void deactivate() {
		if (styleObservable != null) {
			styleObservable.removeChangeListener(this);
			styleObservable.dispose();
			styleObservable = null;
		}
	}

	/**
	 * @see org.eclipse.core.databinding.observable.IChangeListener#handleChange(org.eclipse.core.databinding.observable.ChangeEvent)
	 *
	 * @param event
	 */
	@Override
	public void handleChange(ChangeEvent event) {

		if (getHost() instanceof PapyrusLabelEditPart) {
			// set the position to avoid that position move on text alignment change
			PapyrusLabelEditPart editPart = (PapyrusLabelEditPart) getHost();

			IFigure figure = editPart.getFigure();

			ChangeBoundsRequest req = new ChangeBoundsRequest(REQ_MOVE);
			req.setEditParts(editPart);
			req.setLocation(figure.getBounds().getLocation());
			Command command = editPart.getCommand(req);
			if (command != null && command.canExecute()) {
				TransactionUtil.getEditingDomain(view).getCommandStack().execute(GEFtoEMFCommandWrapper.wrap(command));
			}

		}

		getHost().refresh();
	}
}
