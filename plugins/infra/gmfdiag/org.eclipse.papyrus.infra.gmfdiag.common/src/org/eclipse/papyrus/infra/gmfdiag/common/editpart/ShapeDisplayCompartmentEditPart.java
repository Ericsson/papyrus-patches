/*****************************************************************************
 * Copyright (c) 2011 CEA LIST.
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
package org.eclipse.papyrus.infra.gmfdiag.common.editpart;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ResizableCompartmentEditPart;
import org.eclipse.gmf.runtime.diagram.ui.figures.ResizableCompartmentFigure;
import org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout;
import org.eclipse.gmf.runtime.draw2d.ui.render.RenderedImage;
import org.eclipse.gmf.runtime.draw2d.ui.render.figures.ScalableImageFigure;
import org.eclipse.gmf.runtime.notation.BooleanValueStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.commands.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.BorderDisplayEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.MaintainSymbolRatioEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.BorderedScalableImageFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.ScalableCompartmentFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.ShapeFlowLayout;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.SubCompartmentLayoutManager;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationUtils;
import org.eclipse.papyrus.infra.gmfdiag.common.service.shape.ShapeService;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.NamedStyleProperties;

/**
 * CompartmentEditPart in charge of shpae display.
 */
public class ShapeDisplayCompartmentEditPart extends ResizableCompartmentEditPart {

	/** Title of this compartment */
	public static final String COMPARTMENT_NAME = "symbol"; // $NON-NLS-1$

	/**
	 * Creates a new ShapeDisplayCompartmentEditPart
	 *
	 * @param model
	 *            The resizable compartment view
	 */
	public ShapeDisplayCompartmentEditPart(EObject model) {
		super(model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void activate() {
		super.activate();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deactivate() {
		super.deactivate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		// Start of user code custom edit policies
		installEditPolicy(MaintainSymbolRatioEditPolicy.MAINTAIN_SYMBOL_RATIO_EDITPOLICY, new MaintainSymbolRatioEditPolicy());
		installEditPolicy(BorderDisplayEditPolicy.BORDER_DISPLAY_EDITPOLICY, new BorderDisplayEditPolicy());
		// End of user code
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCompartmentName() {
		return COMPARTMENT_NAME;
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ResizableCompartmentEditPart#handleNotificationEvent(org.eclipse.emf.common.notify.Notification)
	 *
	 * @param notification
	 */
	@Override
	protected void handleNotificationEvent(Notification notification) {
		Object feature = notification.getFeature();
		if (NotationPackage.eINSTANCE.getSize_Width().equals(feature) || NotationPackage.eINSTANCE.getSize_Height().equals(feature) || NotationPackage.eINSTANCE.getLocation_X().equals(feature) || NotationPackage.eINSTANCE.getLocation_Y().equals(feature)) {
			// Bug 457695: refresh shape when resizing
			refresh();
		}
		super.handleNotificationEvent(notification);
	}

	/**
	 * this method is used to set the ratio of the figure.
	 * pay attention if the ratio is true, the only figure is displayed
	 *
	 * @param maintainRatio
	 */
	protected void maintainRatio(boolean maintainRatio) {
		IFigure contentPane = ((ResizableCompartmentFigure) getFigure()).getContentPane();
		for (Object subFigure : contentPane.getChildren()) {
			if (subFigure instanceof BorderedScalableImageFigure) {
				((BorderedScalableImageFigure) subFigure).setMaintainAspectRatio(maintainRatio);
			}

		}
		int nbShapeToDisplay = ShapeService.getInstance().getShapesToDisplay(getPrimaryView()).size();
		if (!maintainRatio && nbShapeToDisplay == 1) {
			OneShapeLayoutManager layout = new OneShapeLayoutManager();
			contentPane.setLayoutManager(layout);
		} else {
			ShapeFlowLayout layout = new ShapeFlowLayout();
			contentPane.setLayoutManager(layout);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IFigure createFigure() {
		ResizableCompartmentFigure result = new ScalableCompartmentFigure(getCompartmentName(), getMapMode());
		ShapeCompartmentLayoutManager layoutManager = new ShapeCompartmentLayoutManager();
		result.setLayoutManager(layoutManager);
		ShapeFlowLayout layout = new ShapeFlowLayout();

		result.getContentPane().setLayoutManager(layout);

		return result;
	}

	/**
	 * Refresh.
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#refresh()
	 */
	@Override
	public void refresh() {
		refreshShapes(getContentPane());
		super.refresh();
	}

	/**
	 * Refreshes the displayed shapes on the figure.
	 * <P>
	 * To be sure everything is clean, it removes all the current displayed shapes and then redraw all of the demanded shapes. This could be probably improved in case of performance issues.
	 * </P>
	 *
	 * @param contentPane
	 *            the figure where to add the new shapes
	 */
	protected void refreshShapes(IFigure contentPane) {
		List<Object> children = new ArrayList<Object>(contentPane.getChildren());
		for (Object child : children) {
			if (child instanceof IFigure) {
				contentPane.remove((IFigure) child);
			}
		}

		List<RenderedImage> shapesToDisplay = ShapeService.getInstance().getShapesToDisplay(getNotationView().eContainer());
		if (shapesToDisplay != null && !shapesToDisplay.isEmpty()) {
			for (RenderedImage image : shapesToDisplay) {
				if (image != null) {
					IFigure imageFigure = new BorderedScalableImageFigure(image, false, getUseOriginalColors(), true, true);
					imageFigure.setOpaque(false);
					imageFigure.setVisible(true);
					contentPane.add(imageFigure);
				} else {
					Activator.log.debug("No image will be drawn");
				}
			}
		}
	}


	/**
	 * Gets the use original colors.
	 *
	 * @return the use original colors
	 */
	private boolean getUseOriginalColors() {
		// set the useOriginal color property
		boolean useOriginalColors = true;
		if (getParent().getModel() instanceof View) {
			// get the CSS value if SVG use original colors
			useOriginalColors = NotationUtils.getBooleanValue((View) getParent().getModel(), NamedStyleProperties.USE_ORIGINAL_COLORS, true);
			// Set the shape display compartment
		}
		return useOriginalColors;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		refreshSymbolCompartment();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setRatio(Double ratio) {
		if (getFigure().getParent().getLayoutManager() instanceof ConstrainedToolbarLayout) {
			super.setRatio(ratio);
		}
	}

	/**
	 * Specific layout manager for the shape compartment. The main goal of this class is to ease the debug process. no specific implementation is
	 * planned yet.
	 * We prevent to display the label of the compartment shape
	 */
	public class ShapeCompartmentLayoutManager extends SubCompartmentLayoutManager {

		public static final int MIN_PREFERRED_SIZE = 40;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void layout(IFigure container) {
			super.layout(container);
			for (int i = 0; i < container.getChildren().size(); i++) {
				if (container.getChildren().get(i) instanceof ScrollPane) {
					((ScrollPane) container.getChildren().get(i)).setBounds(container.getBounds());
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Dimension calculatePreferredSize(IFigure figure, int wHint, int hHint) {
			Dimension dim = super.calculatePreferredSize(figure, wHint, hHint);

			if (figure.getParent().getBounds().height > MIN_PREFERRED_SIZE) // Patch to permit to have shape dimension < 40
				dim.height = Math.max(MIN_PREFERRED_SIZE, dim.height);

			return dim;
		}


	}

	public class OneShapeLayoutManager extends AbstractLayout {

		/**
		 *
		 * {@inheritDoc}
		 */
		@Override
		protected Dimension calculatePreferredSize(IFigure container, int hint, int hint2) {

			int minimumWith = 16;
			int minimumHeight = 16;

			return new Dimension(minimumWith, minimumHeight);
		}

		/**
		 *
		 * {@inheritDoc}
		 */
		@Override
		public void layout(IFigure container) {
			Rectangle compartmentBound = new Rectangle(container.getBounds());
			if (container.getBorder() instanceof MarginBorder) {
				MarginBorder marginBorder = ((MarginBorder) container.getBorder());
				compartmentBound = compartmentBound.shrink(marginBorder.getInsets(container));
			}


			IFigure contentPane = ((ResizableCompartmentFigure) getFigure()).getContentPane();
			ScalableImageFigure scalableImageFigure = null;
			if (contentPane.getChildren().size() > 0) {
				Object lastFig = contentPane.getChildren().get(contentPane.getChildren().size() - 1);
				if (lastFig instanceof ScalableImageFigure) {
					scalableImageFigure = (ScalableImageFigure) lastFig;
				}
			}
			if (scalableImageFigure != null) {
				scalableImageFigure.setBounds(compartmentBound);
			}

		}
	}

	/**
	 * refresh the qualified name
	 */
	protected void refreshSymbolCompartment() {
		BooleanValueStyle maintainRatio = getMaintainSymbolRatioStyle(getNotationView());
		if (maintainRatio != null && maintainRatio.isBooleanValue() == false) {
			maintainRatio(false);
		} else {
			maintainRatio(true);
		}
	}

	/**
	 *
	 * @param currentView
	 * @return the current Style that reperesent the boder
	 */
	protected BooleanValueStyle getMaintainSymbolRatioStyle(View currentView) {
		View parentView = currentView;
		while (parentView.getElement() == currentView.getElement()) {
			BooleanValueStyle style = (BooleanValueStyle) parentView.getNamedStyle(NotationPackage.eINSTANCE.getBooleanValueStyle(), MaintainSymbolRatioEditPolicy.MAINTAIN_SYMBOL_RATIO);
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

	@Override
	public boolean isSelectable() {
		return false;
	}
}
