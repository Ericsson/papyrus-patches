/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.ArrowLocator;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RotatableDecoration;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.papyrus.uml.diagram.common.figure.node.IPapyrusUMLElementFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * @since 3.0
 *
 */
public class CustomGeneralOrderingDescriptor extends GeneralOrderingDescriptor implements IPapyrusUMLElementFigure {

	private WrappingLabel fAppliedStereotypeLabel;

	/**
	 * Decoration to place in the middle of the connection
	 *
	 *
	 */
	private RotatableDecoration middleDecoration;

	private IMapMode mapMode;

	/**
	 * call the super constructor, create super contents, set the middle decoration
	 */
	public CustomGeneralOrderingDescriptor(IMapMode mapMode) {
		// call the super constructor
		super();
		this.mapMode = mapMode;
		// remove old target decoration created by super class.
		setTargetDecoration(null, null);
		// create super contents
		setAntialias(SWT.ON);
		createContents();
		this.setLineStyle(Graphics.LINE_DASH);
		this.setForegroundColor(ColorConstants.black);
		// set the middle decoration
		setMiddleDecoration(createTargetDecoration());

	}


	public IMapMode getMapMode() {
		return mapMode;
	}

	public void setMapMode(IMapMode mapMode) {
		this.mapMode = mapMode;
	}


	protected void createContents() {
		createStereotypeLabel();
	}

	protected void createStereotypeLabel() {
		fAppliedStereotypeLabel = new WrappingLabel();
		fAppliedStereotypeLabel.setTextWrap(true);
		fAppliedStereotypeLabel.setTextJustification(PositionConstants.CENTER);
		fAppliedStereotypeLabel.setText("");
		add(fAppliedStereotypeLabel);
	}

	private RotatableDecoration createTargetDecoration() {
		PolygonDecoration df = new PolygonDecoration();
		df.setFill(true);
		df.setForegroundColor(getForegroundColor());
		df.setBackgroundColor(getForegroundColor());
		PointList pl = new PointList();
		pl.addPoint(getMapMode().DPtoLP(-2), getMapMode().DPtoLP(2));
		pl.addPoint(getMapMode().DPtoLP(0), getMapMode().DPtoLP(0));
		pl.addPoint(getMapMode().DPtoLP(-2), getMapMode().DPtoLP(-2));
		pl.addPoint(getMapMode().DPtoLP(-2), getMapMode().DPtoLP(2));
		df.setTemplate(pl);
		df.setScale(getMapMode().DPtoLP(7), getMapMode().DPtoLP(3));
		return df;
	}

	/**
	 */
	@Override
	public WrappingLabel getAppliedStereotypeLabel() {
		return fAppliedStereotypeLabel;
	}



	/**
	 * Get the decoration of the middle
	 *
	 * @return the middle decoration - possibly null
	 */
	protected RotatableDecoration getMiddleDecoration() {
		return middleDecoration;
	}

	/**
	 * Set the foreground background
	 *
	 * @see org.eclipse.draw2d.IFigure#setForegroundColor(org.eclipse.swt.graphics.Color)
	 */
	@Override
	public void setForegroundColor(Color fg) {
		super.setForegroundColor(fg);
		if (getMiddleDecoration() != null) {
			getMiddleDecoration().setForegroundColor(fg);
			getMiddleDecoration().setBackgroundColor(fg);
		}
	}

	@Override
	public void setLineWidth(int w) {
		super.setLineWidth(w);
		if (getMiddleDecoration() instanceof Shape) {
			((Shape) getMiddleDecoration()).setLineWidth(w);
		}
	}

	/**
	 * Set the middle decoration
	 *
	 * @see org.eclipse.draw2d.PolylineConnection#setTargetDecoration(org.eclipse.draw2d.RotatableDecoration)
	 */
	public void setMiddleDecoration(RotatableDecoration dec) {
		if (getMiddleDecoration() != null) {
			remove(getMiddleDecoration());
		}
		middleDecoration = dec;
		if (dec != null) {
			add(dec, getArrowLocator());
		}
	}

	/**
	 * Create the arrow locator to draw the arrow to the proper location
	 *
	 * @return Arrow Locator using the middle point as reference
	 */
	private ArrowLocator getArrowLocator() {
		return new ArrowLocator(this, ConnectionLocator.MIDDLE) {

			/**
			 * Relocates the passed in figure (which must be a {@link RotatableDecoration}) at the middle of the connection.
			 *
			 * @param target
			 *            The RotatableDecoration to relocate
			 */
			@Override
			public void relocate(IFigure target) {
				PointList points = getConnection().getPoints();
				RotatableDecoration arrow = (RotatableDecoration) target;
				arrow.setLocation(getLocation(points));
				Point refPoint;
				if (points.size() % 2 == 0) {
					int i = points.size() / 2;
					refPoint = points.getPoint(i - 1);
				} else {
					int i = (points.size() - 1) / 2;
					refPoint = points.getPoint(i - 1);
				}
				arrow.setReferencePoint(refPoint);
			}
		};
	}

	@Override
	public void setStereotypeDisplay(String stereotypes, Image image) {
		// Set stereotype text on figure
		if (!"".equals(stereotypes)) {
			fAppliedStereotypeLabel.setText(stereotypes);
		} else {
			fAppliedStereotypeLabel.setText("");
		}
		fAppliedStereotypeLabel.setIcon(image);
	}
}


