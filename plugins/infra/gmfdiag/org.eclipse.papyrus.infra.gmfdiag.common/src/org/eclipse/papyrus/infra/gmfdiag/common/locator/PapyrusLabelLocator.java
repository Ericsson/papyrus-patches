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
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and Implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.locator;


import org.eclipse.draw2d.AbstractLocator;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.diagram.ui.internal.util.LabelViewConstants;
import org.eclipse.gmf.runtime.draw2d.ui.geometry.PointListUtilities;
import org.eclipse.gmf.runtime.notation.NamedStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.PapyrusLabelHelper;

/**
 * Label locator that supports locating labels whose parent is either a Node or
 * and Edge.
 *
 */
public class PapyrusLabelLocator extends AbstractLocator {

	/** The alignment. */
	private int alignment;

	/** The extent. */
	private Dimension extent;

	/** The margin. */
	private Point margin = new Point();

	/** The offset. */
	private Point offset;

	/** the parent figure of this locator. */
	protected IFigure parent;

	/** The text alignment. */
	private int textAlignment = PositionConstants.MIDDLE;

	/** The view. */
	private View view;

	public static final String IS_UPDATED_POSITION = "IS_UPDATED_POSITION";

	/**
	 * Constructor to create a an instance of <code>LabelLocator</code> which locates an IFigure offset relative to a calculated reference point.
	 *
	 * @param parent
	 *            the parent figure
	 * @param offSet
	 *            the relative location of the label
	 * @param alignment
	 *            the alignment hint in the case the parent is a <code>Connection</code>
	 */

	public PapyrusLabelLocator(IFigure parent, Point offSet, int alignment) {
		this.parent = parent;
		this.offset = offSet;
		this.alignment = alignment;
	}

	/**
	 * Constructor for figure who are located and sized.
	 *
	 * @param parent
	 *            the parent
	 * @param bounds
	 *            the bounds
	 * @param alignment
	 *            the alignment
	 */
	public PapyrusLabelLocator(IFigure parent, Rectangle bounds, int alignment) {
		this(parent, bounds.getLocation(), alignment);
		this.extent = bounds.getSize();
	}

	/**
	 * Returns the alignment of ConnectionLocator.
	 *
	 * @return The alignment
	 *
	 */
	public int getAlignment() {
		return alignment;
	}

	/**
	 * Gets the location.
	 *
	 * @return the location
	 */
	private int getLocation() {
		switch (getAlignment()) {
		case ConnectionLocator.SOURCE:
			return LabelViewConstants.TARGET_LOCATION;
		case ConnectionLocator.TARGET:
			return LabelViewConstants.SOURCE_LOCATION;
		case ConnectionLocator.MIDDLE:
			return LabelViewConstants.MIDDLE_LOCATION;
		default:
			return LabelViewConstants.MIDDLE_LOCATION;
		}
	}

	private int getUpdatedLocation() {
		switch (getAlignment()) {
		case ConnectionLocator.SOURCE:
			return LabelViewConstantsEx.TARGET_LOCATION;
		case ConnectionLocator.TARGET:
			return LabelViewConstantsEx.SOURCE_LOCATION;
		case ConnectionLocator.MIDDLE:
			return LabelViewConstantsEx.MIDDLE_LOCATION;
		default:
			return LabelViewConstantsEx.MIDDLE_LOCATION;
		}
	}

	/**
	 * getter for the offset point.
	 *
	 * @return point
	 */
	public Point getOffset() {
		return this.offset;
	}

	/**
	 * Returns the <code>PointList</code> describing the label's parent.
	 *
	 * @return pointList
	 */
	protected PointList getPointList() {
		if (parent instanceof Connection) {
			return ((Connection) parent).getPoints();
		} else {
			PointList ptList = new PointList();
			ptList.addPoint(parent.getBounds().getLocation());
			return ptList;
		}
	}

	/**
	 * Returns the reference point for the locator.
	 *
	 * @return the reference point
	 */
	@Override
	public Point getReferencePoint() {
		if (parent instanceof Connection) {
			NamedStyle style = view.getNamedStyle(NotationPackage.eINSTANCE.getBooleanValueStyle(), IS_UPDATED_POSITION); 
			Boolean updated = style == null ? false : (Boolean) style.eGet(NotationPackage.eINSTANCE.getBooleanValueStyle_BooleanValue());
			return getConnectionReferancePoint(updated);
		} else {
			return parent.getBounds().getLocation();
		}
	}

	public Point getConnectionReferancePoint(boolean updated) {
		PointList ptList = ((Connection) parent).getPoints();
		return PointListUtilities.calculatePointRelativeToLine(ptList, 0, updated ? getUpdatedLocation() : getLocation(), true);
	}

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	public Dimension getSize() {
		return extent.getCopy();
	}


	/**
	 * Gets the text alignment.
	 *
	 * @return the textAlignment
	 */
	public int getTextAlignment() {
		return textAlignment;
	}

	/**
	 * Positions the lable relative to the reference point with the
	 * given offsets.
	 *
	 * @param target
	 *            the target
	 */
	@Override
	public void relocate(IFigure target) {

		// The calculation of the location depends on the size of the shape so the size must be set first.
		Dimension size = new Dimension();

		if (extent != null) {
			PapyrusLabelLocator currentConstraint = (PapyrusLabelLocator) target.getParent().getLayoutManager().getConstraint(target);
			Dimension currentExtent = currentConstraint.getSize();
			size = new Dimension(currentExtent);
			if (currentExtent.width == -1) {
				size.width = target.getPreferredSize().width;
			}
			if (currentExtent.height == -1) {
				size.height = target.getPreferredSize().height;
			}
			target.setSize(size);
		} else {
			target.setSize(new Dimension(target.getPreferredSize().width, target.getPreferredSize().height));
		}

		Point location = null;

		// Calculate the position
		location = PapyrusLabelHelper.relativeCoordinateFromOffset(target, getReferencePoint(), offset);

		// Translate the position according to the justification
		switch (textAlignment) {
		case PositionConstants.LEFT:
			location.translate(target.getBounds().width / 2, 0);
			break;
		case PositionConstants.RIGHT:
			location.translate(-target.getBounds().width / 2, 0);
			break;
		case PositionConstants.CENTER:
			break;
		default:
			break;
		}

		// Set the location
		target.setLocation(location);
	}

	/**
	 * Sets the alignment.
	 *
	 * @param alignment
	 *            the alignment to set
	 */
	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}


	/**
	 * Sets the margin.
	 *
	 * @param margin
	 *            the margin to set
	 */
	public void setMargin(Point margin) {
		this.margin = margin;
	}

	/**
	 * setter for the offset point.
	 *
	 * @param offset
	 *            the new offset
	 */
	public void setOffset(Point offset) {
		this.offset = offset;
	}

	/**
	 * Sets the text alignment.
	 *
	 * @param textAlignment
	 *            the textAlignment to set
	 */
	public void setTextAlignment(int textAlignment) {
		this.textAlignment = textAlignment;
	}

	/**
	 * Sets the view.
	 *
	 * @param view
	 *            the new view
	 */
	public void setView(View view) {
		this.view = view;
	}


}