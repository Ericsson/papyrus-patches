/*****************************************************************************
 * Copyright (c) 2010, 2014 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tessier (CEA LIST) Patrick.tessier@cea.fr - Initial API and implementation
 *   Mickael ADAM (ALL4TEC) mickael.adam@all4tec.net
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.core.listener.DiagramEventBroker;
import org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener;
import org.eclipse.gmf.runtime.diagram.ui.editparts.CompartmentEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart;
import org.eclipse.gmf.runtime.diagram.ui.figures.ResizableCompartmentFigure;
import org.eclipse.gmf.runtime.gef.ui.internal.editpolicies.GraphicalEditPolicyEx;
import org.eclipse.gmf.runtime.notation.BooleanValueStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.databinding.custom.CustomBooleanStyleObservableValue;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.IPapyrusEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.OneTopLineResizableBorder;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationUtils;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.NamedStyleProperties;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.PositionEnum;

/**
 * this edit policy has in charge to toggle the border of its edit part's figure.
 */
public class BorderDisplayEditPolicy extends GraphicalEditPolicyEx implements IChangeListener, NotificationListener, NamedStyleProperties {

	/** The default value for the line position of compartment. */
	private static final String LINE_POSITION_DEFAULT_VALUE = PositionEnum.CENTER.toString();

	/** The default value for the line length ratio of compartment. */
	private static final String LINE_LENGTH_RATIO_DEFAULT_VALUE = "1.0";//$NON-NLS-1$

	/** the default value for the line length of compartment. If the length is set to this default value, the length will be not forced */
	private static final int DEFAULT_LENGTH_VALUE = -1;

	/** key for this edit policy. */
	public static final String BORDER_DISPLAY_EDITPOLICY = "BORDER_DISPLAY_EDITPOLICY";//$NON-NLS-1$

	/** The style observable. */
	protected IObservableValue styleObservable;

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void activate() {
		// retrieve the view and the element managed by the edit part
		View view = (View) getHost().getModel();
		if (view == null) {
			return;
		}

		styleObservable = new CustomBooleanStyleObservableValue(view, EMFHelper.resolveEditingDomain(view), DISPLAY_BORDER);
		styleObservable.addChangeListener(this);

		// adds a listener on the view and the element controlled by the editpart
		getDiagramEventBroker().addNotificationListener(view, this);

		EObject semanticElement = EMFHelper.getEObject(getHost());
		if (semanticElement != null) {
			getDiagramEventBroker().addNotificationListener(semanticElement, this);
		}

		refreshBorderDisplay();
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
	 * Notify changed.
	 *
	 * @param notification
	 *            the notification
	 * @see org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 */
	@Override
	public void notifyChanged(Notification notification) {
		refreshBorderDisplay();
	}

	/**
	 * Handle change.
	 *
	 * @param event
	 *            the event
	 * @see org.eclipse.core.databinding.observable.IChangeListener#handleChange(org.eclipse.core.databinding.observable.ChangeEvent)
	 */
	@Override
	public void handleChange(ChangeEvent event) {
		getHost().refresh();
	}

	/**
	 * Refresh.
	 *
	 * @see org.eclipse.gmf.runtime.gef.ui.internal.editpolicies.GraphicalEditPolicyEx#refresh()
	 */
	@Override
	public void refresh() {
		refreshBorderDisplay();
	}

	/**
	 * Gets the primary shape.
	 *
	 * @return the primary shape
	 */
	protected IFigure getPrimaryShape() {
		EditPart host = getHost();
		if (host instanceof IPapyrusEditPart) {
			IPapyrusEditPart graphicalHost = (IPapyrusEditPart) host;
			return graphicalHost.getPrimaryShape();
		}

		return getHostFigure();
	}

	/**
	 * Gets the notation view.
	 *
	 * @return the notation view
	 */
	protected View getNotationView() {
		EditPart host = getHost();
		if (host instanceof org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart) {
			return ((org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart) host).getNotationView();
		}

		View view = (View) host.getAdapter(View.class);
		return view;
	}

	/** The default border. */
	protected Border defaultBorder;

	/**
	 * Refresh border display.
	 */
	protected void refreshBorderDisplay() {
		View view = getNotationView();
		if (view == null) {
			return;
		}

		BooleanValueStyle displayBorderStyle = findDisplayBorderStyle(view);

		// if not diplayBorder
		if (displayBorderStyle != null && !displayBorderStyle.isBooleanValue()) {
			if (defaultBorder == null) {
				defaultBorder = getPrimaryShape().getBorder();
			}
			getPrimaryShape().setBorder(null);
		} else {
			// if diplayBorder
			// If the shape is a compartment
			if (getPrimaryShape() instanceof ResizableCompartmentFigure) {
				if (!(defaultBorder instanceof OneTopLineResizableBorder)) {
					defaultBorder = new OneTopLineResizableBorder(2);
				}
				// Set OneLineResizableBorder as border
				getPrimaryShape().setBorder(defaultBorder);
				// If a length or a length ratio is defined in notation or CSS
				((OneTopLineResizableBorder) defaultBorder).setLength(NotationUtils.getIntValue(view, LINE_LENGTH, DEFAULT_LENGTH_VALUE));
				((OneTopLineResizableBorder) defaultBorder).setLengthRatio(new Float(NotationUtils.getStringValue(view, LINE_LENGTH_RATIO, LINE_LENGTH_RATIO_DEFAULT_VALUE).replace(',', '.')));// $NON-NLS-1$ //$NON-NLS-2$
				((OneTopLineResizableBorder) defaultBorder).setLinePosition(getlinePosition());

				// If it's the first one, set border to null.
				final List<View> allVisibleCompartments = getAllVisibleCompartments(view, getHost().getParent());
				// If the view is the first compartment set border to null
				if (allVisibleCompartments.size() > 0 && allVisibleCompartments.get(0).equals(view)) {
					getPrimaryShape().setBorder(null);
				}
			} else {
				if (defaultBorder != null) {
					getPrimaryShape().setBorder(defaultBorder);
				}
			}
		}
	}

	/**
	 * Gets the line position from notation or CSS.
	 *
	 * @return the line position
	 */
	public int getlinePosition() {
		// get the value of the CSS property
		View view = getNotationView();

		String linePosition = NotationUtils.getStringValue(view, LINE_POSITION, LINE_POSITION_DEFAULT_VALUE);

		int position = PositionConstants.CENTER;

		if (PositionEnum.LEFT.toString().equals(linePosition)) {
			position = PositionConstants.LEFT;
		}
		if (PositionEnum.RIGHT.toString().equals(linePosition)) {
			position = PositionConstants.RIGHT;
		}
		if (PositionEnum.CENTER.toString().equals(linePosition)) {
			position = PositionConstants.CENTER;
		}

		return position;
	}

	/**
	 * Returns all the displayed compartments for the EditPart.
	 *
	 * @param view
	 *            the view
	 * @param editpart
	 *            an editpart
	 * @return all the displayed compartments for the EditPart
	 */
	public static List<View> getAllVisibleCompartments(View view, EditPart editpart) {
		List<View> visibleCompartments = new ArrayList<View>();
		if (editpart == null)
			return visibleCompartments;
		List<?> localChildren = editpart.getChildren();
		for (Object current : localChildren) {
			if (current instanceof CompartmentEditPart) {
				// If the height is > to 0 (for name compartment which is hide by set the height to 0)
				BooleanValueStyle displayNameStyle = (BooleanValueStyle) view.getNamedStyle(NotationPackage.eINSTANCE.getBooleanValueStyle(), NameDisplayEditPolicy.DISPLAY_NAME);
				final boolean isNameDiplayed = displayNameStyle != null ? displayNameStyle.isBooleanValue() : true;

				// if it's the name compartment and it is diplayed
				if (current instanceof ITextAwareEditPart) {
					if (isNameDiplayed) {
						visibleCompartments.add(((View) ((CompartmentEditPart) current).getModel()));
					}
				} else {
					visibleCompartments.add(((View) ((CompartmentEditPart) current).getModel()));
				}
			}
		}
		return visibleCompartments;
	}

	/**
	 * Find display border style.
	 *
	 * @param view
	 *            the view
	 * @return the boolean value style
	 */
	private BooleanValueStyle findDisplayBorderStyle(View view) {
		View parentView = view;
		while (parentView.getElement() == view.getElement()) {
			BooleanValueStyle style = (BooleanValueStyle) parentView.getNamedStyle(NotationPackage.eINSTANCE.getBooleanValueStyle(), DISPLAY_BORDER);
			if (style != null) {
				return style;
			}

			if (parentView.eContainer() instanceof View) {
				parentView = (View) parentView.eContainer();
			} else {
				break;
			}
		}
		return null;
	}
}
