/*****************************************************************************
 * Copyright (c) 2011, 2017 CEA LIST.
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
 *      Mickael ADAM (ALL4TEC) - mickael.adam@all4tec.net - implementation of layout BorderedLayoutManager to provide maintain ratio and color set
 *      Fanch BONNABESSE (ALL4TEC) - fanch.bonnabesse@all4tec.net - Bug 502531
 *      Mickael ADAM (ALL4TEC) - mickael.adam@all4tec.net - Bug 527062
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.figure.node;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.draw2d.ui.render.RenderedImage;
import org.eclipse.gmf.runtime.draw2d.ui.render.figures.ScalableImageFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.FigureUtils;
import org.eclipse.swt.graphics.Color;

/**
 * Scalable Image figure that will be aligned in the middle/center and keep its own ratio.
 *
 * It is also used to serve as specific implementation in Papyrus, as debug items can be easily added.
 */
public class BorderedScalableImageFigure extends ScalableImageFigure {

	private RenderedImage lastRenderedImage;

	public BorderedScalableImageFigure(RenderedImage renderedImage, boolean useDefaultImageSize, boolean useOriginalColors, boolean antiAlias) {
		this(renderedImage, useDefaultImageSize, useOriginalColors, antiAlias, true);
	}

	/**
	 * @since 3.0
	 */
	public BorderedScalableImageFigure(RenderedImage renderedImage, boolean useDefaultImageSize, boolean useOriginalColors, boolean antiAlias, boolean isModificationPreferredSize) {
		super(renderedImage, useDefaultImageSize, useOriginalColors, antiAlias);
		// set a layout manager to override maintain ratio behavior
		setLayoutManager(new BorderedLayoutManager());
		lastRenderedImage = renderedImage;
		// assure that ShapeFlowLayout gets the actual image size as preferred size. Otherwise, it would
		// scale the image to identical width and height which would make it impossible to calculate the
		// original aspect ratio (SVG specific workaround was in place before, see bug 500999).
		if (isModificationPreferredSize) {
			setPreferredImageSize(
					renderedImage.getSWTImage().getBounds().width,
					renderedImage.getSWTImage().getBounds().height);
		}
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		if (!lastRenderedImage.getSWTImage().isDisposed()) { // Fix bug 462850

			// Get the parent bounds
			Rectangle parentBounds = getParent().getBounds().getCopy();

			// set the clip of the graphics to the parent clip
			graphics.setClip(parentBounds);
			super.paintFigure(graphics);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.draw2d.Figure#getBackgroundColor()
	 */
	@Override
	public Color getBackgroundColor() {
		// Get the main figure where are color informations.
		IRoundedRectangleFigure roundedCompartmentFigure = getMainFigure();

		// Get the color from the color of the parent
		if (roundedCompartmentFigure != null) {
			return roundedCompartmentFigure.getBackgroundColor();
		}

		return super.getBackgroundColor();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.draw2d.Figure#getForegroundColor()
	 */
	@Override
	public Color getForegroundColor() {
		// Get the main figure where are color informations.
		IRoundedRectangleFigure roundedCompartmentFigure = getMainFigure();

		// Get the color from the color of the parent
		if (roundedCompartmentFigure != null) {
			return roundedCompartmentFigure.getForegroundColor();
		}
		return super.getForegroundColor();
	}

	/**
	 * Gets the main figure.
	 * 
	 * @return the roundedRectangleFigure
	 */
	private IRoundedRectangleFigure getMainFigure() {
		// If it's called by SVGNodePlate, the parent have not always the foreground color, need to locate
		SVGNodePlateFigure svgNodePlate = FigureUtils.findParentFigureInstance(this, SVGNodePlateFigure.class);
		return svgNodePlate == null ? null : FigureUtils.findChildFigureInstance(svgNodePlate, IRoundedRectangleFigure.class);
	}

	class BorderedLayoutManager extends AbstractLayout {

		/**
		 * @see org.eclipse.draw2d.LayoutManager#layout(org.eclipse.draw2d.IFigure)
		 *
		 * @param container
		 */
		@Override
		public void layout(IFigure container) {
			// Look for a ScrollBarPane to hide ScrollPane
			IFigure scrollPaneFigure = FigureUtils.findParentFigureInstance(container, ScrollPane.class);

			// Hide the ScrollBar if a ScrollPan is found
			if (scrollPaneFigure instanceof ScrollPane) {
				((ScrollPane) scrollPaneFigure).setScrollBarVisibility(org.eclipse.draw2d.ScrollPane.NEVER);
			}

			// if there is aspect ratio and only one figure is set
			if (isMaintainAspectRatio() && container.getParent().getChildren().size() == 1) {
				// If the ratio is maintained
				ScalableCompartmentFigure scalableCompartmentFigure = FigureUtils.findParentFigureInstance(container, ScalableCompartmentFigure.class);

				// Get the image to calculate ratio
				ScalableImageFigure scalableImage = FigureUtils.findChildFigureInstance(getParent(), ScalableImageFigure.class);
				RenderedImage renderedImage = scalableImage.getRenderedImage();
				Rectangle scalableCompartmentBounds = scalableCompartmentFigure != null ? scalableCompartmentBounds = scalableCompartmentFigure.getBounds() : container.getBounds();

				double ratio = (double) renderedImage.getRenderInfo().getWidth() /
						(double) renderedImage.getRenderInfo().getHeight();

				Point center = new Point(
						scalableCompartmentBounds.x + scalableCompartmentBounds.width / 2,
						scalableCompartmentBounds.y + scalableCompartmentBounds.height / 2);

				int width;
				int height;
				// Case width>height
				if (scalableCompartmentBounds.width > scalableCompartmentBounds.height) {
					if (scalableCompartmentBounds.width > scalableCompartmentBounds.height * ratio) {
						width = (int) (scalableCompartmentBounds.height * ratio);
						height = scalableCompartmentBounds.height;
					} else {
						width = scalableCompartmentBounds.width;
						height = (int) (scalableCompartmentBounds.width / ratio);
					}
				} else {// Case height>width
					if (scalableCompartmentBounds.height < scalableCompartmentBounds.width / ratio) {
						width = (int) (scalableCompartmentBounds.height * ratio);
						height = scalableCompartmentBounds.height;
					} else {
						width = scalableCompartmentBounds.width;
						height = (int) (scalableCompartmentBounds.width / ratio);
					}
				}
				int y = center.y - height / 2;
				int x = center.x - width / 2;
				container.setBounds(new Rectangle(x, y, width, height));
			} else {
				// Set bounds
				if (scrollPaneFigure instanceof ScrollPane) {
					container.setBounds(scrollPaneFigure.getBounds());
				}
			}
		}

		/**
		 * @see org.eclipse.draw2d.AbstractLayout#calculatePreferredSize(org.eclipse.draw2d.IFigure, int, int)
		 *
		 * @param container
		 * @param wHint
		 * @param hHint
		 * @return
		 */
		@Override
		protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
			return null;
		}
	}
}
