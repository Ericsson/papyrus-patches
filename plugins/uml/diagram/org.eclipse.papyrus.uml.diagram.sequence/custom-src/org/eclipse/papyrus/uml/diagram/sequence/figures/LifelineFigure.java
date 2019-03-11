/*****************************************************************************
 * Copyright (c) 2010 - 2018 CEA
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
 *   Soyatec - Initial API and implementation
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 519408
 *   Vincent LORENZO (CEA LIST) vincent.lorenzo@cea.fr - Bug 531520
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.figures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeUtil;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.papyrus.internal.uml.diagram.sequence.elk.RectilinearConvexHull;
import org.eclipse.papyrus.uml.diagram.common.figure.node.RoundedCompartmentFigure;
import org.eclipse.swt.graphics.Color;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class LifelineFigure extends RoundedCompartmentFigure {

	/**
	 * Utility figure to get header. Used for selection bounds.
	 *
	 * @author Mickael ADAM
	 *
	 */
	public final class LifelineHeaderFigure extends RectangleFigure {
		/**
		 *
		 * @see org.eclipse.draw2d.RectangleFigure#fillShape(org.eclipse.draw2d.Graphics)
		 */
		@Override
		protected void fillShape(Graphics graphics) {
			// do nothing
		}

		/**
		 * @see org.eclipse.draw2d.RectangleFigure#outlineShape(org.eclipse.draw2d.Graphics)
		 */
		@Override
		protected void outlineShape(Graphics graphics) {
			// do nothing
		}
	}

	protected RectangleFigure lifelineHeaderBoundsFigure;

	@Deprecated
	protected RectangleFigure fFigureExecutionsContainerFigure;
	@Deprecated
	protected LifelineDotLineCustomFigure fFigureLifelineDotLineFigure;

	/**
	 * This field contains the figure of the children to include into the PolygonPoint list.
	 *
	 * @see bug 531520 : all messages are connected to the Lifeline, but they must be drawn as attached to the ExecutionSpecification when required.
	 * @since 5.0
	 */
	private List<NodeFigure> childrenFigure = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public LifelineFigure() {
		super();
		setLayoutManager(new LifeLineLayoutManager());
		// This line has been used in order to display combinedFragment
		setTransparency(100);
		createContents();
	}

	/**
	 * This method has been used in order to display combinedFragment
	 *
	 * @see org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure#setTransparency(int)
	 */
	@Override
	public void setTransparency(int transparency) {
		super.setTransparency(100);
	}

	/**
	 * @see org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure#getPolygonPoints()
	 */
	@Override
	public PointList getPolygonPoints() {
		// we create the nude Lifeline figure
		final PointList points = new PointList(8);
		// top left corner
		points.addPoint(this.getBounds().x, this.getBounds().y);
		// top right corner
		points.addPoint(this.getBounds().x + this.getBounds().width, this.getBounds().y);
		// bottom header right corner
		points.addPoint(this.getBounds().x + this.getBounds().width, ((LifeLineLayoutManager) this.getLifeLineLayoutManager()).getBottomHeader());
		// bottom middle header
		points.addPoint(this.getBounds().x + this.getBounds().width / 2, ((LifeLineLayoutManager) this.getLifeLineLayoutManager()).getBottomHeader());
		// middle bottom lifeline
		points.addPoint(this.getBounds().x + this.getBounds().width / 2, this.getBounds().y + this.getBounds().height);
		// bottom middle header
		points.addPoint(this.getBounds().x + this.getBounds().width / 2, ((LifeLineLayoutManager) this.getLifeLineLayoutManager()).getBottomHeader());
		// bottom left header
		points.addPoint(this.getBounds().x, ((LifeLineLayoutManager) this.getLifeLineLayoutManager()).getBottomHeader());
		// top left header
		points.addPoint(this.getBounds().x, this.getBounds().y);

		if (this.childrenFigure.isEmpty()) {
			return points;
		} else {
			// for bug 531520:
			// all messages are now attached in the notation to the Lifeline
			// we continue to represent them attached to the ExecutionSpeficiation, that why we complete the polygon list with the ExecutionSpeficiation of the Lifeline
			return completeFigureWithChildren(points);
		}
	}

	/**
	 *
	 * @return
	 * 		the LifelineFigure including its children (ExecutionSpecification)
	 */
	private PointList completeFigureWithChildren(final PointList nudeLifelinePointList) {
		if (this.childrenFigure.isEmpty()) {
			return nudeLifelinePointList;
		}

		// 1. there are children figure, so we create a new point list an dwe initialize it with the beginning of the Lifeline Header
		final PointList newPointList = new PointList();
		int index = 0;
		for (; index < 4; index++) {
			// we copy the 4 first points of the header figures (from the top left corner to the bottom middle of the header points
			newPointList.addPoint(nudeLifelinePointList.getPoint(index));
		}

		// 2. we need to group children rectangles (a group is a set of rectangle which have intersections
		final List<Set<Rectangle>> groups = getGroupedRectangles(getChildrenRectangle());

		// 3. we get the set of points composition the global shape of each group of rectangle
		final List<List<Point>> groupPointsLists = new ArrayList<>();
		for (final Set<Rectangle> currentGroup : groups) {
			groupPointsLists.add(PapyrusRectilinearConvexHull.getExternalShapeForRectangle(currentGroup));
		}

		// 4. as we draw the Lifeline figure from the top to the bottom, we sort the point lists by abscissa
		// the list owning the point with the smallest abscissa is at the beginning, the list with with the "biggest smallest" one is at the end
		groupPointsLists.sort(new OrdinatePointListComparator());

		// 5. we divide each list in 2 parts: 1 one for the top to bottom way, and the second one, for the bottom to the top way
		final List<List<Point>> descendingList = new ArrayList<>();
		final List<List<Point>> ascendingList = new ArrayList<>();
		for (final List<Point> groupList : groupPointsLists) {
			final List<Point> topToBottom = new ArrayList<>();
			final List<Point> bottomToTop = new ArrayList<>();
			divideIn2PointLists(groupList, topToBottom, bottomToTop);
			descendingList.add(topToBottom);
			ascendingList.add(0, bottomToTop);
		}

		// 6. Now we can draw the way from the bottom of the header to the bottom of the lifeline
		for (int i = 0; i < descendingList.size(); i++) {
			// we add the lifeline segment to go from the last points of the lifeline list to the first point of the rectangle list
			newPointList.addPoint(newPointList.getLastPoint().x, descendingList.get(i).get(0).y);

			// we add all the descendingList list
			for (Point current : descendingList.get(i)) {
				newPointList.addPoint(current);
			}

			// we go to the lifeline vertical part
			newPointList.addPoint(this.getBounds().x + this.getBounds().width / 2, newPointList.getLastPoint().y);
		}

		// 7. we add the last bottom segment of the lifeline
		newPointList.addPoint(newPointList.getLastPoint().x, this.getBounds().y + this.getBounds().height);


		// 8. we are going to the bottom of the lifeline to the top
		for (int i = 0; i < ascendingList.size(); i++) {
			// we add the lifeline segment to go from the last points of the lifeline to the first point of the ascending list
			newPointList.addPoint(newPointList.getLastPoint().x, ascendingList.get(i).get(0).y);

			// we add all the descendingList list
			for (Point current : ascendingList.get(i)) {
				newPointList.addPoint(current);
			}

			// we go to the lifeline vertical part
			newPointList.addPoint(this.getBounds().x + this.getBounds().width / 2, newPointList.getLastPoint().y);
		}

		// 9. we finish the header
		index++;// we ignore the points at the end of the middle bottom of the lifeline, because we already add it (at the step 7.)
		for (; index < nudeLifelinePointList.size(); index++) {
			newPointList.addPoint(nudeLifelinePointList.getPoint(index));
		}

		return newPointList;

	}

	/**
	 *
	 * @return
	 * 		the list of {@link Rectangle} representing each child figure
	 */
	private Collection<Rectangle> getChildrenRectangle() {
		final List<Rectangle> rectangles = new ArrayList<>();
		for (final NodeFigure figure : this.childrenFigure) {
			rectangles.add(figure.getBounds());
		}
		return rectangles;
	}

	/**
	 *
	 * @param ptList
	 *            the initial list of points
	 * @param topToBottomList
	 *            the list of points describing the way to go from the top to the bottom of the initial list
	 * @param bottomToTopList
	 *            the list of points describing the way to go from the bottom to the top of the initial list
	 * @since 5.0
	 */
	private static final void divideIn2PointLists(final List<Point> ptList, final List<Point> topToBottomList, final List<Point> bottomToTopList) {
		Assert.isNotNull(topToBottomList);
		Assert.isNotNull(bottomToTopList);

		final Point topPoint = getTopPoint(ptList);
		final Point bottomPoint = getBottomPoint(ptList);

		boolean topToBottom = true;
		for (int i = ptList.indexOf(topPoint);; i++) {
			Point current = ptList.get(i);
			if (i + 1 == ptList.size()) {
				i = 0;
			}
			if (topToBottom) {
				topToBottomList.add(current);
				if (current.equals(bottomPoint)) {
					topToBottom = false;
					bottomToTopList.add(current);
				}
			} else {
				// we are doing the bottom to top list
				bottomToTopList.add(current);
				if (current.equals(topPoint)) {
					break;
				}
			}
		}
	}

	/**
	 * This class allows to compare list of points. The term used for the comparison is the point with the smallest y value
	 *
	 * @since 5.0
	 */
	private static final class OrdinatePointListComparator implements Comparator<Collection<Point>> {

		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 *
		 * @param o1
		 * @param o2
		 * @return
		 */
		@Override
		public int compare(final Collection<Point> o1, final Collection<Point> o2) {
			return Integer.valueOf(getTopPoint(o1).y).compareTo(getTopPoint(o2).y);
		}
	}

	/**
	 *
	 * @param points
	 *            a points' collection
	 * @return
	 * 		the point with the smallest ordinate
	 * @since 5.0
	 */
	private static final Point getTopPoint(final Collection<Point> points) {
		Point topPoint = new Point(0, Integer.MAX_VALUE);
		for (final Point current : points) {
			if (current.y < topPoint.y) {
				topPoint = current;
			}
		}
		return topPoint;
	}

	/**
	 *
	 * @param points
	 *            a points' collection
	 * @return
	 * 		the point with the highest ordinate
	 * @since 5.0
	 */
	private static final Point getBottomPoint(final Collection<Point> points) {
		Point bottomPoint = new Point(0, Integer.MIN_VALUE);
		for (final Point current : points) {
			if (current.y > bottomPoint.y) {
				bottomPoint = current;
			}
		}
		return bottomPoint;
	}

	/**
	 *
	 * @param rectangles
	 *            a collections of rectangle
	 * @return
	 * 		a list of set of rectangles. Each Set in the List contains the rectangles which have intersections between them
	 *
	 * @since 5.0
	 */
	private List<Set<Rectangle>> getGroupedRectangles(final Collection<Rectangle> rectangles) {
		List<Set<Rectangle>> groups = new ArrayList<>();
		final List<Rectangle> localRectangles = new ArrayList<>(rectangles);
		Iterator<Rectangle> iter = localRectangles.iterator();
		while (iter.hasNext()) {
			Set<Rectangle> result = getIntersectingRectangles(iter.next(), localRectangles);
			groups.add(result);
			localRectangles.removeAll(result);
			iter = localRectangles.iterator();
		}
		return groups;
	}

	/**
	 *
	 * @param aRectangle
	 *            a rectangle
	 * @param allAvailableRectangle
	 *            a collections of rectangle
	 * @return
	 * 		a set of rectangles. Each Set contains the rectangles which have intersections between them
	 * @since 5.0
	 */
	private Set<Rectangle> getIntersectingRectangles(final Rectangle aRectangle, Collection<Rectangle> allAvailableRectangle) {
		allAvailableRectangle = new ArrayList<>(allAvailableRectangle);
		allAvailableRectangle.remove(aRectangle);
		final Set<Rectangle> intersectingRectangles = new HashSet<>();
		for (Rectangle current : allAvailableRectangle) {
			if (current.intersects(aRectangle)) {
				intersectingRectangles.add(current);
				intersectingRectangles.addAll(getIntersectingRectangles(current, allAvailableRectangle));
			}
		}
		intersectingRectangles.add(aRectangle);
		return intersectingRectangles;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void paint(Graphics graphics) {
		Rectangle rect = this.getBounds();
		graphics.pushState();
		graphics.setForegroundColor(getForegroundColor());
		// do not forget to set line width to 1, if not the color will
		// change because of the anti-aliasing
		graphics.setLineWidth(1);
		graphics.drawRectangle(rect.x, rect.y, rect.width - 1, ((LifeLineLayoutManager) this.getLifeLineLayoutManager()).getBottomHeader() - rect.y);
		// Draw dash line first to be under child
		graphics.setLineDash(new int[] { 5, 5 });
		graphics.drawLine(new Point(rect.x + rect.width / 2, ((LifeLineLayoutManager) this.getLifeLineLayoutManager()).getBottomHeader()), new Point(rect.x + rect.width / 2, rect.y + rect.height - 1));
		graphics.popState();

		// to draw the anchor shape for debug (bug 531520)
		// if (false == this.childrenFigure.isEmpty()) {
		// graphics.setForegroundColor(new Color(Display.getDefault(), new RGB(255, 0, 0)));
		// PointList pol = getPolygonPoints();
		// graphics.drawPolygon(pol);
		// graphics.setForegroundColor(getForegroundColor());
		// }


		// Then finish to draw figure.
		super.paint(graphics);
	}

	/**
	 * [{@inheritDoc}
	 *
	 * @see org.eclipse.draw2d.Figure#getLayoutManager()
	 */
	@Override
	public LayoutManager getLayoutManager() {
		return new XYLayout();
	}

	public LayoutManager getLifeLineLayoutManager() {
		return super.getLayoutManager();
	}

	/**
	 * Paint the label rectangle as background instead of the whole figure
	 *
	 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.PapyrusNodeFigure#paintBackground(org.eclipse.draw2d.Graphics, org.eclipse.draw2d.geometry.Rectangle)
	 * @param graphics
	 *            graphics tool
	 * @param rectangle
	 *            unused
	 */
	@Override
	protected void paintBackground(Graphics graphics, Rectangle rectangle) {
		super.paintBackground(graphics, getFigureLifelineNameContainerFigure().getBounds());
	}

	/**
	 * Get the figure on which the border must be drawn.
	 *
	 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.PapyrusNodeFigure#getBorderedFigure()
	 * @return the rectangle containing labels
	 */
	@Override
	protected IFigure getBorderedFigure() {
		return getFigureLifelineNameContainerFigure();
	}

	/**
	 * Construct the appropriate border
	 *
	 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.PapyrusNodeFigure#getDefaultBorder(org.eclipse.swt.graphics.Color)
	 * @param borderColor
	 *            the color of the border
	 * @return the border
	 */
	@Override
	protected Border getDefaultBorder(Color borderColor) {
		int margin = getMapMode().DPtoLP(7);
		MarginBorder defaultBorder = new MarginBorder(margin, margin, margin, margin);
		return defaultBorder;
	}

	/**
	 * remove label creation, change layout
	 */
	private void createContents() {
		lifelineHeaderBoundsFigure = new LifelineHeaderFigure();
		this.add(lifelineHeaderBoundsFigure);
	}

	protected IMapMode getMapMode() {
		return MapModeUtil.getMapMode();
	}

	/**
	 * get label from super figure
	 */
	public WrappingLabel getFigureLifelineLabelFigure() {
		return getNameLabel();
	}

	@Deprecated
	public RectangleFigure getFigureLifelineNameContainerFigure() {
		return lifelineHeaderBoundsFigure;
	}

	@Deprecated
	public RectangleFigure getFigureExecutionsContainerFigure() {
		return fFigureExecutionsContainerFigure;
	}

	@Deprecated
	public LifelineDotLineCustomFigure getFigureLifelineDotLineFigure() {
		return fFigureLifelineDotLineFigure;
	}

	/**
	 * @param childrenFigure
	 *            the children figures used to define the PolygonList
	 * @since 5.0
	 */
	public void setChildrenFigure(List<NodeFigure> childrenFigure) {
		this.childrenFigure = childrenFigure == null ? Collections.emptyList() : childrenFigure;
	}


	/**
	 *
	 * @author Vincent LORENZO
	 *         Papyrus integration of the ELK class RectilinearConvexHull.
	 *         This class allow to get the list of points describing the polygon represented by a set of overlaping rectangles
	 *
	 *         N.B.: In some specific cases, the result is not the better
	 * @since 5.0
	 */
	private static final class PapyrusRectilinearConvexHull {

		/**
		 *
		 * @param rectangles
		 *            overlaping rectangles
		 * @return
		 * 		the list of points describing the polygon represented by a set of overlaping rectangles
		 */
		public static final List<Point> getExternalShapeForRectangle(final Collection<Rectangle> rectangles) {
			return getExternalShapeForPoints(getAllPoints(rectangles));
		}

		/**
		 *
		 * @param inputPoints
		 *            a collection of points
		 * @return
		 * 		the list of points describing the external shape of the input points
		 */
		private static final List<Point> getExternalShapeForPoints(final Collection<Point> inputPoints) {
			// 1. convert to ELK points
			final List<org.eclipse.papyrus.internal.uml.diagram.sequence.elk.Point> elkPTS = convertToELKPoints(inputPoints);

			// 2. get the hull
			final List<Point> pt = convertToPoints(RectilinearConvexHull.of(elkPTS).getHull());
			// we add the first point at the end to close the figure
			pt.add(pt.iterator().next());
			return pt;
		}

		/**
		 *
		 * @param rectangles
		 *            the input rectangles
		 * @return
		 * 		a set of points composed by all the corners of the input rectangles AND the intersection's points of the rectangles between them
		 */
		private static final Set<Point> getAllPoints(final Collection<Rectangle> rectangles) {
			final Set<Point> points = new HashSet<>();
			for (final Rectangle current : rectangles) {
				for (final Rectangle internalCurrent : rectangles) {
					if (internalCurrent.intersects(current)) {
						final Rectangle rect = internalCurrent.getIntersection(current);
						points.add(rect.getTopLeft());
						points.add(rect.getTopRight());
						points.add(rect.getBottomLeft());
						points.add(rect.getBottomRight());
					}
				}
				points.add(current.getTopLeft());
				points.add(current.getTopRight());
				points.add(current.getBottomLeft());
				points.add(current.getBottomRight());
			}
			return points;
		}

		/**
		 *
		 * @param draw2DPoints
		 *            drawd2D points
		 * @return
		 * 		a list of ELK Points
		 */
		private static final List<org.eclipse.papyrus.internal.uml.diagram.sequence.elk.Point> convertToELKPoints(final Collection<Point> draw2DPoints) {
			final List<org.eclipse.papyrus.internal.uml.diagram.sequence.elk.Point> elkPoints = new ArrayList<>();
			for (final Point current : draw2DPoints) {
				elkPoints.add(new org.eclipse.papyrus.internal.uml.diagram.sequence.elk.Point(current.preciseX(), current.preciseY()));
			}
			return elkPoints;
		}

		/**
		 *
		 * @param elkPoints
		 *            ELK points
		 * @return
		 * 		a list of draw2D points
		 */
		private static final List<Point> convertToPoints(final Collection<org.eclipse.papyrus.internal.uml.diagram.sequence.elk.Point> elkPoints) {
			final List<Point> draw2DPoints = new ArrayList<>();
			for (final org.eclipse.papyrus.internal.uml.diagram.sequence.elk.Point current : elkPoints) {
				draw2DPoints.add(new Point((int) current.x, (int) current.y));
			}
			return draw2DPoints;
		}
	}
}
