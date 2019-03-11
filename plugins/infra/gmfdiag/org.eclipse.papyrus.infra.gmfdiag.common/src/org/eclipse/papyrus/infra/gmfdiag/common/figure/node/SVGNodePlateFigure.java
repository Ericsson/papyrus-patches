/*****************************************************************************
 * Copyright (c) 2010, 2014, 2018 CEA LIST and others.
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
 *  Laurent Wouters (CEA LIST) laurent.wouters@cea.fr - Refactoring, cleanup, added support for PapyrusLabel element
 *  Mickael ADAM (ALL4TEC) mickael.adam@all4tec.net - Add IRoundedRectangleFigure use case(436547)
 *  Ansgar Radermacher (CEA LIST) ansgar.radermacher@cea.fr - NPE if SVG unit is not in pixels (521232)  
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.figure.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.batik.dom.svg.AbstractSVGPathSegList.SVGPathSegMovetoLinetoItem;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionDimension;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.draw2d.ui.figures.IOvalAnchorableFigure;
import org.eclipse.gmf.runtime.draw2d.ui.render.figures.ScalableImageFigure;
import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.SlidableEllipseAnchor;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.FigureUtils;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGPathElement;
import org.w3c.dom.svg.SVGPathSeg;
import org.w3c.dom.svg.SVGPathSegList;
import org.w3c.dom.svg.SVGRectElement;
import org.w3c.dom.svg.SVGSVGElement;

/**
 * This figure is used to make links following SVG shape
 */
public class SVGNodePlateFigure extends DefaultSizeNodeFigure {

	/**
	 * Represents a transformation from SVG to Draw2D coordinates.
	 * This class replaces the Draw2D Transform class which can only operate over Draw2D points.
	 * This class always defines a transformation that is composed of a scaling operation followed by a translation operation.
	 *
	 * @author Laurent Wouters
	 */
	private static class SvgToDraw2DTransform {

		private double scaleX;

		private double scaleY;

		private double translationX;

		private double translationY;

		/**
		 * Initializes this transformation
		 *
		 * @param scaleX
		 *            Scale on the X axis
		 * @param scaleY
		 *            Scale on the Y axis
		 * @param translationX
		 *            Translation on the X axis
		 * @param translationY
		 *            Translation on the Y axis
		 */
		public SvgToDraw2DTransform(double scaleX, double scaleY, double translationX, double translationY) {
			this.scaleX = scaleX;
			this.scaleY = scaleY;
			this.translationX = translationX;
			this.translationY = translationY;
		}

		/**
		 * Transforms the given points in the target frame of reference
		 *
		 * @param point
		 *            The point to transform
		 * @return The transformed point in the target frame of reference
		 */
		public PrecisionPoint transform(PrecisionPoint point) {
			return new PrecisionPoint(point.preciseX() * scaleX + translationX, point.preciseY() * scaleY + translationY);
		}

		/**
		 * Transforms the given rectangle in the target frame of reference
		 *
		 * @param rectangle
		 *            The rectangle to transform
		 * @return The transformed rectangle in the target frame of reference
		 */
		public PrecisionRectangle transform(PrecisionRectangle rectangle) {
			return new PrecisionRectangle(rectangle.preciseX() * scaleX + translationX, rectangle.preciseY() * scaleY + translationY, rectangle.preciseWidth() * scaleX, rectangle.preciseHeight() * scaleY);
		}
	}

	/** The svg document. */
	protected SVGDocument svgDocument = null;

	/** The svg dimension. */
	private PrecisionDimension svgDimension = null;

	/** The outline points for the svg Figure. */
	private List<PrecisionPoint> outlinePoints = null;

	/** The outline dimension. */
	private PrecisionDimension outlineDimension = null;

	/** The label bounds. */
	private PrecisionRectangle labelBounds = null;

	/** The default node plate. */
	protected DefaultSizeNodeFigure defaultNodePlate;

	/** The follow svg papyrus path. */
	protected boolean followSVGPapyrusPath = false;

	/**
	 * Sets if has to follow svg papyrus path.
	 *
	 * @param followSVGPapyrusPath
	 *            the followSVGPapyrusPath to set
	 */
	public void setFollowSVGPapyrusPath(boolean followSVGPapyrusPath) {
		this.followSVGPapyrusPath = followSVGPapyrusPath;
	}

	/**
	 * Initializes the figure.
	 *
	 * @param width
	 *            The figure's original width
	 * @param height
	 *            The figure's original height
	 */
	public SVGNodePlateFigure(int width, int height) {
		super(width, height);
	}

	/**
	 * Associates the given SVG document to this figure
	 *
	 * @param svgDocument
	 *            the SVG document
	 */
	public void setSVGDocument(SVGDocument svgDocument) {
		this.svgDocument = svgDocument;
		if (svgDocument != null) {
			this.svgDimension = getSvgDimension(svgDocument);
			Element element = svgDocument.getElementById("PapyrusPath");
			if (element != null) {
				outlinePoints = toDraw2DPoints(((SVGPathElement) element).getPathSegList());
				outlineDimension = getDimensionOf(outlinePoints);
			} else {
				// If you don't have papyrusPath
				outlinePoints = null;
				outlineDimension = null;
			}
			element = svgDocument.getElementById("PapyrusLabel");
			if (element != null) {
				labelBounds = toDraw2DRectangle((SVGRectElement) element);
			}
		} else {
			this.svgDimension = null;
			this.outlinePoints = null;
			this.outlineDimension = null;
			this.labelBounds = null;
		}
	}

	/**
	 * Transforms the given SVG animated length to a base value, assuming the units in the SVG are pixels
	 *
	 * @param length
	 *            The SVG length
	 * @return The base value as a double
	 */
	private double getValueOf(SVGAnimatedLength length) {
		if (length == null) {
			return 0;
		}
		SVGLength base = length.getBaseVal();
		if (base == null) {
			return 0;
		}
		try {
			return base.getValue();
		}
		catch (NullPointerException e) {
			// NPE during getValue (bug 521232) => retry using getValueInSpecifiedUnits)
			return base.getValueInSpecifiedUnits();
		}
	}

	/**
	 * Gets the dimension of the SVG document, assuming the units in the SVG are pixels
	 *
	 * @param svgDocument
	 *            The SVG document
	 * @return The equivalent Draw2D dimension
	 */
	private PrecisionDimension getSvgDimension(SVGDocument svgDocument) {
		double svgWidth = 0;
		double svgHeight = 0;
		SVGSVGElement svgElement = svgDocument.getRootElement();
		if (svgElement != null) {
			svgWidth = getValueOf(svgElement.getWidth());
			svgHeight = getValueOf(svgElement.getHeight());
		}
		return new PrecisionDimension(svgWidth, svgHeight);
	}

	/**
	 * Transforms the given SVG path to a list of Draw2D precision points, assuming the units in the SVG are pixels
	 *
	 * @param segments
	 *            The SVG path as a list of segments
	 * @return The list of the corresponding Draw2D points
	 */
	private List<PrecisionPoint> toDraw2DPoints(SVGPathSegList segments) {
		ArrayList<PrecisionPoint> pointList = new ArrayList<PrecisionPoint>();

		// current coordinates
		double currentX = 0;
		double currentY = 0;
		PrecisionPoint firstPoint = new PrecisionPoint();
		Boolean firstPointAbsolue = true;
		for (int i = 0; i < segments.getNumberOfItems(); i++) {
			SVGPathSeg seg = segments.getItem(i);
			if (seg instanceof SVGPathSegMovetoLinetoItem) {
				SVGPathSegMovetoLinetoItem linetoItem = (SVGPathSegMovetoLinetoItem) seg;
				String letter = linetoItem.getPathSegTypeAsLetter();
				double x = linetoItem.getX();
				double y = linetoItem.getY();
				if (letter.equals("M")) {
					currentX = x;
					currentY = y;
					firstPoint.setPreciseLocation(currentX, currentY);
					firstPointAbsolue = true;
					pointList.add(new PrecisionPoint(currentX, currentY));
				} else if (letter.equals("m")) {
					currentX = currentX + x;
					currentY = currentY + y;
					firstPoint.setPreciseLocation(currentX, currentY);
					firstPointAbsolue = false;
					pointList.add(new PrecisionPoint(currentX, currentY));
				} else if (letter.equals("L")) {
					currentX = x;
					currentY = y;
					pointList.add(new PrecisionPoint(currentX, currentY));
				} else if (letter.equals("l")) {
					currentX = currentX + x;
					currentY = currentY + y;
					pointList.add(new PrecisionPoint(currentX, currentY));
				}
			} else if (seg instanceof SVGPathSeg) {
				// Take into account the z letter
				String letter = seg.getPathSegTypeAsLetter();
				if (letter.equals("z")) {
					if (firstPointAbsolue) {
						pointList.add(firstPoint);
					} else {
						currentX = currentX + firstPoint.preciseX();
						currentY = currentY + firstPoint.preciseY();
						pointList.add(new PrecisionPoint(currentX, currentY));
					}
				}
			} else {
				System.err.println("Unsupported SVG segment in PapyrusPath at index " + i + " in SVG document");
			}
		}

		return pointList;
	}

	/**
	 * Gets the dimension of the given collection of points
	 *
	 * @param points
	 *            A list of points
	 * @return The dimension of the points
	 */
	private PrecisionDimension getDimensionOf(Collection<PrecisionPoint> points) {
		double maxWidth = 0;
		double maxHeight = 0;
		for (PrecisionPoint point : points) {
			maxWidth = Math.max(maxWidth, point.preciseX());
			maxHeight = Math.max(maxHeight, point.preciseY());
		}
		return new PrecisionDimension(maxWidth, maxHeight);
	}

	/**
	 * Transforms the given SVG rectangle to a Draw2D rectangle, assuming the units in the SVG are pixels
	 *
	 * @param element
	 *            The SVG rectangle
	 * @return The equivalent Draw2D rectangle
	 */
	private PrecisionRectangle toDraw2DRectangle(SVGRectElement element) {
		return new PrecisionRectangle(getValueOf(element.getX()), getValueOf(element.getY()), getValueOf(element.getWidth()), getValueOf(element.getHeight()));
	}



	/**
	 * Sets the node plate that is wrapped by it.
	 *
	 * @param defaultNodePlate
	 */
	public void setDefaultNodePlate(IFigure defaultNodePlate) {
		if (defaultNodePlate instanceof DefaultSizeNodeFigure) {
			this.defaultNodePlate = (DefaultSizeNodeFigure) defaultNodePlate;
			this.setDefaultSize(((DefaultSizeNodeFigure) defaultNodePlate).getDefaultSize());
		}
		if (defaultNodePlate instanceof ICustomNodePlate) {
			((ICustomNodePlate) this.defaultNodePlate).setSVGNodePlateContainer(this);
		}
	}

	/**
	 * Gets the transformation from SVG to Draw2D positions
	 *
	 * @param innerWidth
	 *            Maximum width of the elements to transform
	 * @param innerHeight
	 *            Maximum height of the elements to transform
	 * @param anchor
	 *            The Draw2D rectangle anchoring the SVG figure
	 * @return The transformation
	 */
	private SvgToDraw2DTransform getTransform(double innerWidth, double innerHeight, Rectangle anchor) {
		PrecisionDimension maxDim = new PrecisionDimension(Math.max(svgDimension.preciseWidth(), innerWidth), Math.max(svgDimension.preciseHeight(), innerHeight));

		// Look for the ScalableImage to know if the ration is maintain
		boolean isRatioMaintained = false;
		ScalableImageFigure scalableImage = FigureUtils.findChildFigureInstance(getParent(), ScalableImageFigure.class);
		if (scalableImage != null) {
			isRatioMaintained = scalableImage.isMaintainAspectRatio();
		}
		if (isRatioMaintained) {
			// Calculate Transform if we want to keep the ratio of the Figure.
			double ratio = svgDimension.preciseWidth() / svgDimension.preciseHeight();
			// double ratio = scalableImage.getBounds().preciseWidth() / scalableImage.getBounds().preciseHeight();
			double scaleX = 0;
			double scaleY = 0;
			double tranlationX = anchor.x;
			double tranlationY = anchor.y;

			if (anchor.height < anchor.width) {
				if (anchor.height * ratio < anchor.width) {
					scaleX = (anchor.height / maxDim.preciseHeight());
					scaleY = (anchor.height / maxDim.preciseHeight());
					tranlationX = anchor.x + (anchor.preciseWidth() / 2 - (anchor.preciseHeight() * ratio) / 2);
				} else {
					scaleX = (anchor.width / maxDim.preciseWidth());
					scaleY = (anchor.width / maxDim.preciseWidth());
					tranlationY = anchor.y + (anchor.preciseHeight() / 2 - (anchor.preciseWidth() / ratio) / 2);
				}
			} else {
				if (anchor.height > anchor.width / ratio) {
					scaleX = (anchor.width / maxDim.preciseWidth());
					scaleY = (anchor.width / maxDim.preciseWidth());
					tranlationY = anchor.y + (anchor.preciseHeight() / 2 - (anchor.preciseWidth() / ratio) / 2);
				} else {
					scaleX = (anchor.height / maxDim.preciseHeight());
					scaleY = (anchor.height / maxDim.preciseHeight());
					tranlationX = anchor.x + (anchor.preciseWidth() / 2 - (anchor.preciseHeight() * ratio) / 2);
				}
			}

			return new SvgToDraw2DTransform(scaleX, scaleY, tranlationX, tranlationY);
		} else {
			return new SvgToDraw2DTransform(anchor.width / maxDim.preciseWidth(), anchor.height / maxDim.preciseHeight(), anchor.x, anchor.y);
		}
	}

	/**
	 * Gets the current Draw2D anchor for the SVG figure
	 *
	 * @return The Draw2D anchor as a Rectangle
	 */
	private Rectangle getDraw2DAnchor() {
		if (this.getChildren().size() > 0 && this.getChildren().get(0) instanceof IFigure) {
			IFigure primaryShape = (IFigure) this.getChildren().get(0);
			for (Object subFigure : primaryShape.getChildren()) {
				if (subFigure instanceof ScalableCompartmentFigure) {
					return ((IFigure) subFigure).getBounds();
				}
			}
		}
		return getHandleBounds();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure#createAnchor(org.eclipse.draw2d.geometry.PrecisionPoint)
	 */
	@Override
	protected ConnectionAnchor createAnchor(PrecisionPoint p) {
		if (this.outlinePoints == null || !followSVGPapyrusPath) {
			if (defaultNodePlate instanceof IOvalAnchorableFigure) {
				defaultNodePlate.setBounds(this.getBounds());
				if (p != null) {
					// If the old terminal for the connection anchor cannot be resolved (by SlidableAnchor) a null
					// PrecisionPoint will passed in - this is handled here
					return new SlidableEllipseAnchor(this, p);
				}
			}
			if (defaultNodePlate instanceof IRoundedRectangleFigure) {
				defaultNodePlate.setBounds(this.getBounds());
				if (p != null) {
					// If the old terminal for the connection anchor cannot be resolved (by SlidableAnchor) a null
					// PrecisionPoint will passed in - this is handled here
					return new SlidableRoundedRectangleAnchor(this, p);
				}
			}
			return super.createAnchor(p);
		}

		return super.createAnchor(p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure#createDefaultAnchor()
	 */
	@Override
	protected ConnectionAnchor createDefaultAnchor() {
		if (this.outlinePoints == null || !followSVGPapyrusPath) {
			if (defaultNodePlate instanceof IOvalAnchorableFigure) {
				defaultNodePlate.setBounds(this.getBounds());
				return new SlidableEllipseAnchor(this);
			}
			if (defaultNodePlate instanceof IRoundedRectangleFigure) {
				defaultNodePlate.setBounds(this.getBounds());
				return new SlidableRoundedRectangleAnchor(this);
			}
		}
		return super.createDefaultAnchor();
	}


	/**
	 * @see org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure#getPolygonPoints()
	 */
	@Override
	public PointList getPolygonPoints() {
		if (this.outlinePoints == null || !followSVGPapyrusPath) {
			if (defaultNodePlate != null) {
				defaultNodePlate.setBounds(this.getBounds());
				return defaultNodePlate.getPolygonPoints();
			}
			return super.getPolygonPoints();
		}

		SvgToDraw2DTransform transform = getTransform(outlineDimension.preciseWidth(), outlineDimension.preciseHeight(), getDraw2DAnchor());
		PointList points = new PointList(5);
		for (PrecisionPoint point : outlinePoints) {
			points.addPoint(transform.transform(point));
		}
		return points;
	}

	/**
	 * Determines whether this figure defines the bounds of a possible label
	 *
	 * @return <code>true</code> if this figures defines the bounds of the label
	 */
	public boolean hasLabelBounds() {
		return (labelBounds != null);
	}

	/**
	 * Gets the bounds of the label, if they are defined
	 *
	 * @param anchor
	 *            The Draw2D rectangle anchoring the SVG figure
	 * @return The label's bounds, or <code>null</code> if they are not defined
	 */
	public Rectangle getLabelBounds(Rectangle anchor) {
		if (labelBounds == null) {
			return null;
		}
		SvgToDraw2DTransform transform = getTransform(labelBounds.preciseRight(), labelBounds.preciseBottom(), anchor);
		return transform.transform(labelBounds);
	}
}
