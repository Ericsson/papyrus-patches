/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Francois Le Fevre (CEA LIST) francois.le-fevre@cea.fr - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.Shape;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramColorRegistry;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.notation.FillStyle;
import org.eclipse.gmf.runtime.notation.LineStyle;

import org.eclipse.swt.graphics.Color;

/**
 * Util methods dedicated to help for the highlight of node/edge in case of mouseover.
 * @author flefevre
 *
 */
public class HighlightUtil {

	private static class FigureState {

		private Color bgColor;

		private Color fgColor;

		private int lineWidth;

		public FigureState(Color bgColor, Color fgColor, int lineWidth) {
			this.bgColor = bgColor;
			this.fgColor = fgColor;
			this.lineWidth = lineWidth;
		}
	}

	private static Map<Object, FigureState> figureState = new HashMap<Object, FigureState>();

	/**
	 * Make a private instance, avoid highlight above highlight.
	 */
	private HighlightUtil() {
	}

	/**
	 * Maybe this value can be managed by preferences.
	 *
	 * @param bgColor
	 */
	private static Color getHighlightBackground(Color bgColor) {
		return bgColor;
	}

	/**
	 * Maybe this value can be managed by preferences.
	 *
	 * @param lineWidth
	 */
	private static int getHighlightLineWidth(int lineWidth) {
		return lineWidth * 2;
	}

	private static List<IFigure> getHighlightFigures(EditPart host, boolean parentLooking) {
		List<IFigure> figures = new ArrayList<IFigure>();
		try {
			Method getMethod = getGetPrimaryShapeMethod(host.getClass());
			getMethod.setAccessible(true);
			figures.add((IFigure) getMethod.invoke(host));
		} catch (Exception e) {
			if(!parentLooking){
				List<IFigure> po = getHighlightFigures(host.getParent(), true);
				if(po!=null){
					figures.addAll(po);
				}
			}
					
		}
		return figures;
	}

	/**
	 * Fixed bug when introducing some sub class.
	 *
	 * @param type
	 * @return getPrimaryShape()
	 * @throws Exception
	 */
	protected static Method getGetPrimaryShapeMethod(Class<?> type) throws Exception {
		if (type == null) {
			throw new NoSuchMethodException("getPrimaryShape");
		}
		try {
			return type.getDeclaredMethod("getPrimaryShape");
		} catch (Exception e) {
			Method method = getGetPrimaryShapeMethod(type.getSuperclass());
			if (method == null) {
				throw e;
			}
			return method;
		}
	}

	/**
	 * Maybe this value can be managed by preferences.
	 *
	 * @param fgColor
	 */
	private static Color getHighlightForeground(Color fgColor) {
		return fgColor;
	}

	public static void highlight(EditPart host) {
		List<IFigure> highlightFigures = getHighlightFigures(host, false);
		if (highlightFigures.isEmpty()) {
			return;
		}
		synchronized (figureState) {
			for (IFigure figure : highlightFigures) {
				FigureState fs = figureState.get(figure);
				if (fs == null) {
					fs = createFigureState(host, figure);
					figureState.put(figure, fs);
				}
				updateFigure(figure, getHighlightBackground(fs.bgColor), getHighlightForeground(fs.fgColor), getHighlightLineWidth(fs.lineWidth));
			}
		}
	}

	protected static FigureState createFigureState(EditPart host, IFigure fig) {
		int lineWidth = 1;
		Color bgColor = null, fgColor = null;
		Object model = host.getModel();
		if (model instanceof LineStyle) {
			int modelLineWidth = ((LineStyle) model).getLineWidth();
			if (modelLineWidth != -1) {
				lineWidth = modelLineWidth;
			}
			int lineColor = ((LineStyle) model).getLineColor();
			fgColor = DiagramColorRegistry.getInstance().getColor(Integer.valueOf(lineColor));
		} else {
			Border border = fig.getBorder();
			if (border instanceof LineBorder) {
				lineWidth = ((LineBorder) border).getWidth();
			} else if (fig instanceof Shape) {
				lineWidth = ((Shape) fig).getLineWidth();
			}
			fgColor = fig.getForegroundColor();
		}
		if (model instanceof FillStyle) {
			int fillColor = ((FillStyle) model).getFillColor();
			bgColor = DiagramColorRegistry.getInstance().getColor(Integer.valueOf(fillColor));
		} else {
			bgColor = fig.getBackgroundColor();
		}
		return new FigureState(bgColor, fgColor, lineWidth);
	}

	public static void unhighlight() {
		if (figureState == null || figureState.isEmpty()) {
			return;
		}
		synchronized (figureState) {
			Set<Entry<Object, FigureState>> entrySet = figureState.entrySet();
			for (Entry<Object, FigureState> entry : entrySet) {
				Object key = entry.getKey();
				FigureState value = entry.getValue();
				if (key instanceof IFigure) {
					IFigure fig = (IFigure) key;
					updateFigure(fig, value.bgColor, value.fgColor, value.lineWidth);
				} else if (key instanceof EditPart) {
					List<IFigure> figures = getHighlightFigures((EditPart) key, false);
					for (IFigure fig : figures) {
						updateFigure(fig, value.bgColor, value.fgColor, value.lineWidth);
					}
				}
			}
			figureState.clear();
		}
	}

	public static void unhighlight(EditPart host) {
		List<IFigure> highlightFigures = getHighlightFigures(host, false);
		if (highlightFigures.isEmpty()) {
			return;
		}
		synchronized (figureState) {
			for (IFigure figure : highlightFigures) {
				FigureState s = figureState.remove(figure);
				if (s == null) {
					continue;
				}
				updateFigure(figure, s.bgColor, s.fgColor, s.lineWidth);
			}
		}
	}

	private static void updateFigure(IFigure fig, Color bgColor, Color fgColor, int lineWidth) {
		if (fig == null) {
			return;
		}
		synchronized (fig) {
			if (lineWidth > 0) {
				Border border = fig.getBorder();
				if (border != null && border instanceof LineBorder) {
					((LineBorder) border).setWidth(lineWidth);
				}
				if (fig instanceof Shape) {
					((Shape) fig).setLineWidth(lineWidth);
				}
				if (fig instanceof NodeFigure) {
					((NodeFigure) fig).setLineWidth(lineWidth);
				}
			}
			fig.repaint();
		}
	}
}
