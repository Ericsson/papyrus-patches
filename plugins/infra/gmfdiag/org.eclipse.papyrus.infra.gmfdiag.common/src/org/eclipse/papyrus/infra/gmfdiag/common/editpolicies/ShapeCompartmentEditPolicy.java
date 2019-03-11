/*****************************************************************************
 * Copyright (c) 2012, 2016 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Patrick Tessier (CEA LIST) Patrick.tessier@cea.fr - Initial API and implementation
 *  Christian W. Damus (CEA) - bug 323802
 *  Christian W. Damus - bug 492482
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;
import org.eclipse.gmf.runtime.diagram.core.listener.DiagramEventBroker;
import org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.core.listenerservice.IPapyrusListener;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.CreateShapeCompartmentViewCommand;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.SetNodeVisibilityCommand;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.IPapyrusEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.IShapeCompartmentEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.service.shape.NotificationManager;
import org.eclipse.papyrus.infra.gmfdiag.common.service.shape.ShapeService;
import org.eclipse.swt.widgets.Display;

/**
 * this edit policy can be apply only on {@link IPapyrusEditPart} in order to
 * access to primary figure. the primary figure has to be a {@link IPapyrusNodeUMLElementFigure}.
 * This edit policy manage the display of a shape in a new compartment and toggles the compartment display.
 */
public class ShapeCompartmentEditPolicy extends GraphicalEditPolicy implements AutomaticNotationEditPolicy, NotificationListener, IPapyrusListener {

	/** constant for this edit policy role */
	public static final String SHAPE_COMPARTMENT_EDIT_POLICY = "ShapeCompartmentEditPolicy"; //$NON-NLS-1$

	/** manager for notifications: should the compartment react to the notification? */
	protected NotificationManager notificationManager;

	/**
	 * Creates a new AppliedStereotype display edit policy
	 */
	public ShapeCompartmentEditPolicy() {
		super();
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void activate() {
		super.activate();
		// retrieve the view and the element managed by the edit part
		View view = getView();
		if (view == null) {
			return;
		}
		notificationManager = ShapeService.getInstance().createNotificationManager(getDiagramEventBroker(), view, this);
		createShapeCompartment();

	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void deactivate() {
		// retrieve the view and the element managed by the edit part
		View view = getView();
		if (view == null) {
			return;
		}
		notificationManager.dispose();
		notificationManager = null;
		super.deactivate();
	}

	/**
	 * Returns the view controlled by the host edit part
	 *
	 * @return the view controlled by the host edit part
	 */
	protected View getView() {
		return (View) getHost().getModel();
	}

	/**
	 * {@inheritDoc}
	 */
	public void refreshDisplay() {
		final IGraphicalEditPart editPart = (IGraphicalEditPart) getHost();
		editPart.refresh();
	}

	protected boolean hasToDisplayCompartment(EObject view) {
		return ShapeService.getInstance().hasShapeToDisplay(view);
	}

	/**
	 * the goal of this method is to execute the a command to create a notation node for a compartment of stereotype
	 *
	 * @param editPart
	 *            the editpart owner of the new compartment
	 * @param appliedstereotype
	 *            the stereotype application
	 */
	protected void executeShapeCompartmentCreation(final IGraphicalEditPart editPart) {
		try {
			// boolean isVisible = hasToDisplayCompartment(editPart.getNotationView());
			TransactionalEditingDomain domain = getEditingDomain(editPart);
			CreateShapeCompartmentViewCommand command = new CreateShapeCompartmentViewCommand(domain, "Create Compartment", "Command that creates the compartment displaying shapes", editPart.getNotationView(), /* isVisible */false);
			
			// This should not change the command stack, as this transaction will only manipulate transient views. Create a transaction manually, if needed
			execute(command);
		} catch (Exception e) {
			Activator.log.error(e);
		}
	}

	/**
	 * Returns the editing domain for the given edit Part
	 *
	 * @param editPart
	 *            the edit part from which editing domain is searched
	 * @return the editing domain
	 */
	protected TransactionalEditingDomain getEditingDomain(IGraphicalEditPart editPart) {
		return editPart.getEditingDomain();
	}

	/**
	 * Gets the diagram event broker from the editing domain.
	 *
	 * @return the diagram event broker
	 */
	protected DiagramEventBroker getDiagramEventBroker() {
		TransactionalEditingDomain theEditingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();
		if (theEditingDomain != null) {
			return DiagramEventBroker.getInstance(theEditingDomain);
		}
		return null;
	}

	/**
	 *
	 * {@inheritedDoc}
	 */
	@Override
	public void notifyChanged(Notification notification) {
		refreshDisplay();
	}

	/**
	 * this method creates a node for the compartment of stereotype if it does not exist.
	 *
	 * @param stereotypeApplication
	 *            the stereotype application
	 */
	public void createShapeCompartment() {
		final IGraphicalEditPart editPart = (IGraphicalEditPart) getHost();
		final View node = editPart.getNotationView();
		// Look for the node for the shape compartment
		View shapeCompartment = getShapeCompartmentView(node);
		// it does not exist
		if (shapeCompartment == null) {
			executeShapeCompartmentCreation(editPart);
		}
	}

	/**
	 * Returns the view corresponding to the shape compartment
	 *
	 * @param node
	 * @return
	 */
	private View getShapeCompartmentView(View node) {
		for (Object child : node.getChildren()) {
			if (child instanceof View && IShapeCompartmentEditPart.VIEW_TYPE.equals(((View) child).getType())) {
				return (View) child;
			}
		}
		return null;
	}

	/**
	 * the goal of this method is to execute the a command to create a notation node for a compartment of stereotype
	 *
	 * @param editPart
	 *            the editpart owner of the new compartment
	 * @param appliedstereotype
	 *            the stereotype application
	 */
	protected void setVisibility(final View view, final boolean isVisible) {
		final GraphicalEditPart editPart = (GraphicalEditPart) getHost();
		Display.getCurrent().asyncExec(new Runnable() {

			@Override
			public void run() {
				SetNodeVisibilityCommand setCommand = new SetNodeVisibilityCommand(editPart.getEditingDomain(), view, isVisible);
				// use to avoid to put it in the command stack
				execute(setCommand);
			}
		});
	}

}
