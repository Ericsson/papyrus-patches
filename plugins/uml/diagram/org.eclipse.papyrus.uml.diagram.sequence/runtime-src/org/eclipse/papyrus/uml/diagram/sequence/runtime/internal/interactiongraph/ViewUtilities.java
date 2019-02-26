/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramGraphicalViewer;
import org.eclipse.gmf.runtime.gef.ui.figures.SlidableAnchor;
import org.eclipse.gmf.runtime.notation.Anchor;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.LayoutConstraint;
import org.eclipse.gmf.runtime.notation.Location;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationUtils;
import org.eclipse.papyrus.infra.gmfdiag.common.preferences.PreferencesConstantsHelper;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.figures.LifelineFigure.LifelineHeaderFigure;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.Lifeline;

/**
 * @author ETXACAM
 *
 */
public class ViewUtilities {
	public enum EdgeSide {
		Source,
		Target
	};
	
	public static final int ROW_PADDING = 10;
	public static final int COL_PADDING = 10;

	public static final int LIFELINE_HEADER_HEIGHT = 19;
	public static final int LIFELINE_DEFAULT_WIDTH = 100;
	public static final int EXECUTION_SPECIFICATION_WIDTH = AbstractExecutionSpecificationEditPart.DEFAUT_WIDTH;

	public static View getViewForElement(View containerView, Element element) {
		if (containerView == null) {
			return null;
		}
		for (Object v : containerView.getChildren()) {
			if (!(v instanceof View)) {
				continue;
			}

			EObject obj = ViewUtil.resolveSemanticElement((View) v);
			if (obj == element) {
				return (View) v;
			}

			View vw = getViewForElement((View) v, element);
			if (vw != null) {
				return vw;
			}
		}

		if (containerView instanceof Diagram) {
			for (Object v : containerView.getDiagram().getEdges()) {
				if (!(v instanceof View)) {
					continue;
				}

				EObject obj = ViewUtil.resolveSemanticElement((View) v);
				if (obj == element) {
					return (View) v;
				}

				View vw = getViewForElement((View) v, element);
				if (vw != null) {
					return vw;
				}
			}
		}
		return null;
	}

	public static View getViewWithType(View containerView, String type) {
		if (containerView == null) {
			return null;
		}

		for (Object v : containerView.getChildren()) {
			if (!(v instanceof View)) {
				continue;
			}

			if (type.equals(((View) v).getType())) {
				return (View) v;
			}

			View vw = getViewWithType((View) v, type);
			if (vw != null) {
				return vw;
			}
		}

		if (containerView instanceof Diagram) {
			for (Object v : containerView.getDiagram().getEdges()) {
				if (!(v instanceof View)) {
					continue;
				}

				if (type.equals(((View) v).getType())) {
					return (View) v;
				}

				View vw = getViewWithType((View) v, type);
				if (vw != null) {
					return vw;
				}
			}
		}
		return null;
	}

	public static boolean hasLayoutConstraints(View view) {
		Location constraint = getLayoutConstraint(view);
		if (constraint instanceof Bounds) {
			Bounds b = (Bounds) constraint;
			if (b.getWidth() == -1 && b.getHeight() == -1 && b.getX() == 0 && b.getY() == 0) {
				return false;
			}
		}
		return (constraint != null);
	}

	public static Location getLayoutConstraint(View view) {
		if (view instanceof org.eclipse.gmf.runtime.notation.Node) {
			org.eclipse.gmf.runtime.notation.Node node = (org.eclipse.gmf.runtime.notation.Node) view;
			LayoutConstraint constraints = node.getLayoutConstraint();
			if (constraints instanceof Location) {
				return (Location) constraints;
			}
		}
		return null;
	}

	public static Rectangle absoluteLayoutConstraint(EditPartViewer viewer, View view) {
		Location loc = getLayoutConstraint(view);
		if (loc == null) {
			loc = NotationFactory.eINSTANCE.createBounds();
		}
		View parent = (View) view.eContainer();
		Rectangle r = new Rectangle();
		if (parent != null && !(parent instanceof Diagram)) {
			r = getBounds(viewer, parent).getCopy();
			GraphicalEditPart ep = getEditPart(viewer, parent);
			if (ep != null) {
				Rectangle rcp = getAbsoluteBounds(ep.getContentPane());
				Rectangle rlf = getAbsoluteBounds(ep.getFigure());
				r.x += (rcp.x - rlf.x);
				r.y += (rcp.y - rlf.y);
			} // TODO: Add +30 about the Lifeline header... if lifeline has not editpart
		}

		r.x += loc.getX();
		r.y += loc.getY();
		if (loc instanceof Bounds) {
			r.width = ((Bounds) loc).getWidth();
			r.height = ((Bounds) loc).getHeight();
		} else {
			r.width = 0;
			r.height = 0;
		}

		return r;
	}

	public static Rectangle getBounds(EditPartViewer viewer, List<View> viewList) {
		Rectangle r = null;
		for (View vw : viewList) {
			if (r == null) {
				r = getBounds(viewer, vw);
			} else {
				r.union(getBounds(viewer, vw));
			}
		}
		
		return r;
	}

	public static Rectangle getBounds(EditPartViewer viewer, View view) {
		if (view instanceof org.eclipse.gmf.runtime.notation.Node) {
			org.eclipse.gmf.runtime.notation.Node node = (org.eclipse.gmf.runtime.notation.Node) view;
			GraphicalEditPart ep = getEditPart(viewer, view);
			if (ep == null) {
				return absoluteLayoutConstraint(viewer, view);
			}

			Rectangle r = getAbsoluteBounds(ep.getFigure());
			cancelViewportEffects(ep, r);
			return r;
		} else if (view instanceof Edge) {
			Rectangle r = null;
			Edge e = (Edge) view;
			Point p = getAnchorLocationForView(viewer, e, EdgeSide.Source);
			for (int i = 0; i < 2; i++) {
				if (r == null) {
					r = new Rectangle(p.x, p.y, 0, 0);
				} else {
					r.union(p);
				}
				p = getAnchorLocationForView(viewer, e, EdgeSide.Target);
			}
			return r;
		} 
		return null; // Can not happen
	}

	public static Rectangle getClientAreaBounds(EditPartViewer viewer, View view) {
		if (view instanceof org.eclipse.gmf.runtime.notation.Node) {
			org.eclipse.gmf.runtime.notation.Node node = (org.eclipse.gmf.runtime.notation.Node) view;
			GraphicalEditPart ep = getEditPart(viewer, view);
			if (ep == null) {
				Rectangle r = absoluteLayoutConstraint(viewer, view);
				if (view.getElement() instanceof Lifeline) {
					r.y += 19;
					r.height -= 19;
				}
				return r;
			}

			Rectangle r = getAbsoluteBounds(ep.getContentPane());
			cancelViewportEffects(ep, r);

			if (view.getElement() instanceof Lifeline) {
				// Remove the header.
				LifelineHeaderFigure header = findFigure(ep.getContentPane(), LifelineHeaderFigure.class);
				Rectangle rh = getAbsoluteBounds(header);
				cancelViewportEffects(ep, rh);
				int rem = (rh.y + rh.height) - r.y;
				r.y = rh.y + rh.height;
				r.height -= rem;
			}

			return r;
		}
		return null; // Can not happen
	}

	private static Rectangle getAbsoluteBounds(IFigure f) {
		Rectangle r = f.getBounds().getCopy();
		f.translateToAbsolute(r);
		return r;
	}

	private static Point cancelViewportEffects(EditPart ep, Point p) {
		if (ep == ep.getRoot()) {
			return p;
		}
		/*
		IFigure viewport = ((GraphicalEditPart) ep.getRoot()).getFigure();
		if (viewport instanceof Viewport) {
			Point pvp = ((Viewport) viewport).getViewLocation();
			p.x += pvp.x;
			p.y += pvp.y;
		}*/
		controlToViewer(ep.getViewer(), p);
		return p;
	}

	// TODO: Cancel other viewport effects...???
	private static Rectangle cancelViewportEffects(EditPart ep, Rectangle rect) {
		if (ep == ep.getRoot()) {
			return rect;
		}
		/*IFigure viewport = ((GraphicalEditPart) ep.getRoot()).getFigure();
		if (viewport instanceof Viewport) {
			Point pvp = ((Viewport) viewport).getViewLocation();
			rect.x += pvp.x;
			rect.y += pvp.y;
		}*/
		controlToViewer(ep.getViewer(), rect);
		return rect;
	}

	public static boolean isLifelineView(View view) {
		if (view == null)
			return false;
		return view.getType().equals(LifelineEditPart.VISUAL_ID);
	}
	
	public static boolean isSelfEdge(Edge edge) {
		View srcLifeline = edge.getSource();
		if (srcLifeline != null && !isLifelineView(srcLifeline)) {
			srcLifeline = (View)srcLifeline.eContainer();
			if (!isLifelineView(srcLifeline))
				srcLifeline = null;
		}
		
		View trgLifeline = edge.getTarget();
		if (trgLifeline != null && !isLifelineView(trgLifeline)) {
			trgLifeline = (View)srcLifeline.eContainer();
			if (!isLifelineView(trgLifeline))
				trgLifeline = null;
		}
		
		return srcLifeline == trgLifeline; 
	}
	
	public static Point getAnchorLocationForView(EditPartViewer viewer, Edge edge, EdgeSide side) {
		View anchoredView = side == EdgeSide.Source ? edge.getSource() : edge.getTarget();
		if (anchoredView.getElement() instanceof Gate) {
			return getBounds(viewer, anchoredView).getCenter();
		}

		Anchor anchor = null;
		if (side == EdgeSide.Source) {
			anchor = edge.getSourceAnchor();
		} else if (side == EdgeSide.Target) {
			anchor = edge.getTargetAnchor();
		}

		IdentityAnchor anch = (IdentityAnchor) anchor;
		String id = anch.getId();
		return parseAnchorId(viewer, anchoredView, id);
	}

	public static void setAnchorLocationForView(EditPartViewer viewer, Edge edge, EdgeSide side, Point point) {
		View anchoredView = side == EdgeSide.Source ? edge.getSource() : edge.getTarget();
		Anchor anchor = null;
		if (side == EdgeSide.Source) {
			anchor = edge.getSourceAnchor();
		} else if (side == EdgeSide.Target) {
			anchor = edge.getTargetAnchor();
		}

		IdentityAnchor anch = (IdentityAnchor) anchor;
		String id = formatAnchorId(viewer, anchoredView, point);
		anch.setId(id);
	}

	public static Point parseAnchorId(EditPartViewer viewer, View anchoringView, String anchorId) {
		PrecisionPoint loc = SlidableAnchor.parseTerminalString(anchorId);
		GraphicalEditPart anchoringEp = getEditPart(viewer, anchoringView);
		Rectangle rect = getBounds(viewer, anchoringView);
		if (loc == null) {
			return rect.getCenter().getCopy();
		}

		if (anchoringEp != null) {
			rect = anchoringEp.getContentPane().getBounds().getCopy();
			anchoringEp.getContentPane().translateToAbsolute(rect);
			cancelViewportEffects(anchoringEp, rect);
		} else if (anchoringView.getElement() instanceof Lifeline) {

		}
		Point p = new Point((int) (rect.x + (rect.width * loc.preciseX())),
				(int) (rect.y + (rect.height * loc.preciseY())));
		return p;
	}

	public static String formatAnchorId(EditPartViewer viewer, View anchoringView, Point p) {
		GraphicalEditPart anchoringEp = getEditPart(viewer, anchoringView);
		Rectangle rect = getBounds(viewer, anchoringView);
		if (anchoringEp != null) {
			rect = anchoringEp.getContentPane().getBounds().getCopy();
			anchoringEp.getContentPane().translateToAbsolute(rect);
			cancelViewportEffects(anchoringEp, rect);
		}
		float newX = ((float) p.x - (float) rect.x) / rect.width;
		float newY = ((float) p.y - (float) rect.y) / rect.height;

		StringBuffer b = new StringBuffer();
		b.append("(").append(Float.toString(newX)).append(",").append(Float.toString(newY)).append(")");
		return b.toString();
	}

	public static String formatAnchorId(Rectangle rect, Point p) {
		float newX = ((float) p.x - (float) rect.x) / rect.width;
		float newY = ((float) p.y - (float) rect.y) / rect.height;

		StringBuffer b = new StringBuffer();
		b.append("(").append(Float.toString(newX)).append(",").append(Float.toString(newY)).append(")");
		return b.toString();
	}

	public static Point toRelativeForLayoutConstraints(EditPartViewer viewer, View container, Point pt) {
		pt = pt.getCopy();
		Rectangle r = new Rectangle();
		r = getBounds(viewer, container).getCopy();
		GraphicalEditPart ep = getEditPart(viewer, container);
		if (ep != null) {
			Rectangle rcp2 = getAbsoluteBounds(ep.getContentPane());
			rcp2 = cancelViewportEffects(ep, rcp2);
			pt.x -= rcp2.x;
			pt.y -= rcp2.y;
			return pt;
		}

		pt.performTranslate(-r.x, -r.y);
		return pt;
	}

	public static Rectangle toRelativeForLayoutConstraints(EditPartViewer viewer, View container, Rectangle res) {
		res = res.getCopy();
		Point pt = toRelativeForLayoutConstraints(viewer, container, res.getTopLeft());
		res.x = pt.x;
		res.y = pt.y;
		return res;
	}

	public static Rectangle getBoundsForLayoutConstraint(EditPartViewer viewer, View view) {
		View containerView = (View) view.eContainer();
		return toRelativeForLayoutConstraints(viewer, containerView, getBounds(viewer, view));
	}

	public static Rectangle controlToViewer(EditPartViewer viewer, Rectangle rect) {
		((GraphicalEditPart)viewer.getContents()).getFigure().translateToRelative(rect);
		return rect;
	}
	
	public static Point controlToViewer(EditPartViewer viewer, Point point) {
		((GraphicalEditPart)viewer.getContents()).getFigure().translateToRelative(point);
		return point;
	}
	
	public static Dimension controlToViewer(EditPartViewer viewer, Dimension dimension) {
		((GraphicalEditPart)viewer.getContents()).getFigure().translateToRelative(dimension);
		return dimension;
	}

	public static Rectangle viewerToControl(EditPartViewer viewer, Rectangle rect) {
		((GraphicalEditPart)viewer.getContents()).getFigure().translateToAbsolute(rect);
		return rect;
	}

	public static Point viewerToControl(EditPartViewer viewer, Point point) {
		((GraphicalEditPart)viewer.getContents()).getFigure().translateToAbsolute(point);
		return point;
	}

	public static Dimension viewerToControl(EditPartViewer viewer, Dimension dimension) {
		((GraphicalEditPart)viewer.getContents()).getFigure().translateToAbsolute(dimension);
		return dimension;
	}

	/*
	 * public static boolean hasConstraints(View view) {
	 * Location constraint = getLocationConstraint(view);
	 * if (constraint instanceof Bounds) {
	 * Bounds b = (Bounds)constraint;
	 * if (b.getWidth() == -1 && b.getHeight() == -1 && b.getX() == 0 && b.getY() == 0)
	 * return false;
	 * }
	 * return (constraint != null);
	 * }
	 *
	 * public static Point getLocationForView(EditPartViewer viewer, View view) {
	 * Location constraint = getLocationConstraint(viewer, view);
	 * if (constraint == null) {
	 * return null;
	 * }
	 * return absolutePoint(viewer, (View)view.eContainer(), new Point(constraint.getX(), constraint.getY()));
	 * }
	 *
	 * public static void setAbsoluteBounds(EditPartViewer viewer, View view, Rectangle bb) {
	 * if (view instanceof org.eclipse.gmf.runtime.notation.Node) {
	 * org.eclipse.gmf.runtime.notation.Node node = (org.eclipse.gmf.runtime.notation.Node)view;
	 * Bounds b = NotationFactory.eINSTANCE.createBounds();
	 * Rectangle parentBounds = getBounds(viewer, (View)view.eContainer());
	 * Point tl = relativePoint(viewer, (View)view.eContainer(), bb.getTopLeft());
	 * b.setX(tl.x);
	 * b.setY(tl.y);
	 * b.setHeight(bb.height);
	 * b.setWidth(bb.width);
	 * node.setLayoutConstraint(b);
	 *
	 * }
	 * }
	 *
	 * public static Rectangle getBounds(EditPartViewer viewer, View view) {
	 * if (view instanceof org.eclipse.gmf.runtime.notation.Node) {
	 * org.eclipse.gmf.runtime.notation.Node node = (org.eclipse.gmf.runtime.notation.Node)view;
	 * Location constraints = getLocationConstraint(viewer,view);
	 * if (constraints == null)
	 * return null;
	 * Point p = absolutePoint(viewer, (View)view.eContainer(), (Location)constraints);
	 * if (constraints instanceof Size) {
	 * Size sz = (Size)constraints;
	 * return new Rectangle(p.x, p.y, sz.getWidth(), sz.getHeight());
	 * } else {
	 * return new Rectangle(p.x, p.y, 0, 0);
	 * }
	 * }
	 * return null;
	 * }
	 *
	 * public static Rectangle getChildrenBounds(EditPartViewer viewer, View container, List<View> filter, String... types) {
	 * Set<String> typeSet = new HashSet<String>(Arrays.asList(types));
	 * List<View> views = new ArrayList(container.getChildren());
	 * views.removeAll(filter);
	 * Iterator<View> it = views.iterator();
	 * while (it.hasNext()) {
	 * View vw = it.next();
	 * if (typeSet.contains(vw.getType())) {
	 * it.remove();
	 * }
	 * if (vw.getElement() == null) {
	 * it.remove();
	 * }
	 * }
	 * return getBounds(viewer, views);
	 * }
	 *
	 * public static Rectangle getBounds(EditPartViewer viewer, List<View> viewList) {
	 * Rectangle r = null;
	 * for (View vw : viewList) {
	 * if (vw instanceof Edge) {
	 * Edge e = (Edge)vw;
	 * Point p = getAnchorLocationForView(viewer, e, e.getSource());
	 * for (int i=0; i<2; i++) {
	 * if (r == null) {
	 * r = new Rectangle(p.x, p.y, 0, 0);
	 * } else {
	 * r.union(p);
	 * }
	 * p = getAnchorLocationForView(viewer, e, e.getTarget());
	 * }
	 * } else {
	 * if (r == null)
	 * r = getBounds(viewer, vw);
	 * else
	 * r.union(getBounds(viewer, vw));
	 * }
	 * }
	 * return r;
	 * }
	 *
	 * private static Rectangle getLocationConstraint(EditPartViewer viewer, View view) {
	 * View parentView = (View)view.eContainer();
	 * GraphicalEditPart ep = getEditPart(viewer,view);
	 * if (ep == null)
	 * return getLocationConstraint(view);
	 *
	 * Location loc = getAbsoluteLocationConstraint(ep.getFigure());
	 * if (loc == null) {
	 * return getLocationConstraint(view);
	 * }
	 * cancelViewportEffects(ep,loc);
	 *
	 * GraphicalEditPart parentEp = getEditPart(viewer,parentView);
	 * if (parentEp == null)
	 * return loc;
	 * Location parentLoc = getAbsoluteLocationConstraint(parentEp.getContentPane());
	 * cancelViewportEffects(parentEp,parentLoc);
	 * loc.setX(loc.getX() - parentLoc.getX());
	 * loc.setY(loc.getY() - parentLoc.getY());
	 * return loc;
	 * }
	 *
	 * private static Location getLocationConstraint(View view) {
	 * if (view instanceof org.eclipse.gmf.runtime.notation.Node) {
	 * org.eclipse.gmf.runtime.notation.Node node = (org.eclipse.gmf.runtime.notation.Node)view;
	 * LayoutConstraint constraints = node.getLayoutConstraint();
	 * if (constraints instanceof Location) {
	 * return (Location)constraints;
	 * }
	 * }
	 * return null;
	 * }
	 *
	 * private static Location cancelViewportEffects(EditPart ep, Location loc) {
	 * if (ep == ep.getRoot())
	 * return loc;
	 * IFigure viewport = ((GraphicalEditPart)ep.getRoot()).getFigure();
	 * if (viewport instanceof Viewport) {
	 * Point p = ((Viewport) viewport).getViewLocation();
	 * loc.setX(loc.getX()+p.x);
	 * loc.setY(loc.getY()+p.y);
	 * }
	 * return loc;
	 * }
	 *
	 *
	 * public static Point absolutePoint(EditPartViewer viewer, View view, Point p) {
	 * p = p.getCopy();
	 * View container = view;
	 * while (container != null) {
	 * Rectangle r = getLocationConstraint(viewer, container);
	 * if (r == null) {
	 * return p;
	 * }
	 * p.x += r.x;
	 * p.y += r.y;
	 * container = (View)container.eContainer();
	 * }
	 * return p;
	 * }
	 *
	 * private static Point relativePoint(EditPartViewer viewer, View view, Location loc) {
	 * return relativePoint(viewer, view, new Point(loc.getX(), loc.getY()));
	 * }
	 *
	 * public static Point relativePoint(EditPartViewer viewer, View view, Point p) {
	 * List<View> containers = new ArrayList<View>();
	 * View container = view;
	 * while (container != null) {
	 * containers.add(container);
	 * container = (View)container.eContainer();
	 * }
	 *
	 * Collections.reverse(containers);
	 * p = p.getCopy();
	 *
	 *
	 * for (View parent : containers) {
	 * Location loc = getLocationConstraint(viewer, parent);
	 * if (loc == null) {
	 * continue;
	 * }
	 * p.x -= loc.getX();
	 * p.y -= loc.getY();
	 * }
	 *
	 * return p;
	 * }
	 */
	private static GraphicalEditPart getEditPart(EditPartViewer viewer, View v) {
		if (viewer == null) {
			return null;
		}
		return ((GraphicalEditPart) viewer.getEditPartRegistry().get(v));
	}

	public static Bounds toBounds(Rectangle r) {
		return toBounds(r, null);
	}

	public static Bounds toBounds(Rectangle r, Bounds bounds) {
		return toBounds(r.x, r.y, r.width, r.height, bounds);
	}

	public static Bounds toBounds(int x, int y, int width, int height) {
		return toBounds(x, y, width, height, null);
	}

	public static Bounds toBounds(int x, int y, int width, int height, Bounds bounds) {
		if (bounds == null) {
			bounds = NotationFactory.eINSTANCE.createBounds();
		}
		bounds.setX(x);
		bounds.setY(y);
		bounds.setWidth(width);
		bounds.setHeight(height);
		return bounds;
	}

	public static Rectangle snapToGrid(EditPartViewer viewer, Diagram diagram, Rectangle r) {
		Point pt = snapToGrid(viewer, diagram, r.getTopLeft());
		r.x = pt.x; 
		r.y = pt.y;
		return r;
	}

	public static Point snapToGrid(EditPartViewer viewer, Diagram diagram, int x, int y) {
		return snapToGrid(viewer, diagram, new Point(x,y));		
	}

	public static Point snapToGrid(EditPartViewer viewer, Diagram diagram, Point pt) {
		double gridSpacing = getGridSpacing(viewer, diagram);
		pt.x = (int)(Math.round((double)pt.x / gridSpacing) * gridSpacing);
		pt.y = (int)(Math.round((double)pt.y / gridSpacing) * gridSpacing);
		return pt;		
	}
	
	public static boolean isSnapToGrid(EditPartViewer viewer, Diagram diagram) {
		boolean snapToGrid = false;
		if (viewer instanceof DiagramGraphicalViewer) {
			IPreferenceStore preferenceStore = ((DiagramGraphicalViewer)viewer).getWorkspaceViewerPreferenceStore();
			snapToGrid = preferenceStore.getBoolean(PreferencesConstantsHelper.SNAP_TO_GRID_CONSTANT);
		}
		return NotationUtils.getBooleanValue(diagram, PreferencesConstantsHelper.SNAP_TO_GRID_CONSTANT, snapToGrid);
	}

	private static double getGridSpacing(EditPartViewer viewer, Diagram diagram) {
		double gridSpacing = 1.0;
		if (viewer instanceof DiagramGraphicalViewer) {
			String diagramType = diagram.getType();
			IPreferenceStore preferenceStore = ((DiagramGraphicalViewer)viewer).getWorkspaceViewerPreferenceStore();
			gridSpacing = preferenceStore.getDouble(PreferencesConstantsHelper.GRID_SPACING_CONSTANT);
		}
		return NotationUtils.getDoubleValue(diagram, PreferencesConstantsHelper.GRID_SPACING_CONSTANT, gridSpacing);
	}
	
	private static <T extends IFigure> T findFigure(IFigure container, Class<T> cls) {
		for (IFigure ch : (List<IFigure>)container.getChildren()) {
			if (cls.isInstance(ch)) {
				return (T)ch;
			}
		}

		for (IFigure ch : (List<IFigure>)container.getChildren()) {
			IFigure res = findFigure(ch, cls);
			if (res != null)
				return (T)res;
		}
		return null;
	}
}
