/*****************************************************************************
 * Copyright (c) 2019 CEA LIST and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author ETXACAM
 *
 */
public class Draw2dUtils {
	public static Rectangle union(Rectangle r, int x, int y, int width, int height) {
		int x1 = Math.min(r.x, x);
		int x2 = Math.max(r.x + r.width, x + width);
		int y1 = Math.min(r.y, y);
		int y2 = Math.max(r.y + r.height, y + height);
		return r.setBounds(x1,y1,x2-x1,y2-y1);
	}
	
	public static Rectangle union(Rectangle r1, Rectangle r2) {
		Rectangle r = r1;
		return union(r,r2.x,r2.y,r2.width,r2.height); 
	}
	
	public static Rectangle union(Rectangle r1, Point pt) {
		Rectangle r = r1;
		return union(r,pt.x,pt.y,0,0); 
	}

	public static boolean intersects(Rectangle r1, Rectangle r2) {
		int x1 = Math.max(r1.x(), r2.x());
		int x2 = Math.min(r1.x() + r1.width(), r2.x() + r2.width());
		int y1 = Math.max(r1.y(), r2.y());
		int y2 = Math.min(r1.y() + r1.height(), r2.y() + r2.height());
		return !(((x2 - x1) < 0) || ((y2 - y1) < 0));
	}
	
}
