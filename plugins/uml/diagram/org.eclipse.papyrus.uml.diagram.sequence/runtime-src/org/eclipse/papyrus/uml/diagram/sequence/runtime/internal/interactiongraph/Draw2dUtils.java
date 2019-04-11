/*****************************************************************************
 * (c) Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Functions to handle Rectangle operations to take in account Rectangles of width or height = 0.
 * <br>
 * The issue is right() and bottom() function return the x and y position that are outside the rectangle. 
 * Based on how contains() is implemented, r.x+r.height is outside the rectangle. and that applies for 
 * union and intersect functions.
 *       
 * @author ETXACAM
 *
 */
public class Draw2dUtils {
	public static int SHRINK_SIZE = 2;
	
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
	
	public static boolean contains(Rectangle r, int x, int y, int width, int height) {
		return contains(r, new Rectangle(x,y,width,height));
	}

	public static boolean contains(Rectangle r, Rectangle o) {
		return r.contains(o); // This seems to work as expected
	}
	
	public static boolean contains(Rectangle r, Point p) {
		return contains(r,p.x,p.y);
	}

	public static boolean contains(Rectangle r, int x, int y) {
		return x >=  r.x && y >= r.y && x <= r.right() && y <= r.bottom();
	}
	

	public static Rectangle insideRectangle(Rectangle rectangle) {
		return rectangle.shrink(SHRINK_SIZE, SHRINK_SIZE);
	}
	
	public static Rectangle insideRectangle(Rectangle rectangle, boolean horz, boolean vert) {
		return rectangle.shrink(horz ? SHRINK_SIZE : 0, vert ? SHRINK_SIZE : 0);
	}

	public static Rectangle outsideRectangle(Rectangle rectangle) {
		return rectangle.expand(SHRINK_SIZE, SHRINK_SIZE);		
	}

	public static Rectangle outsideRectangle(Rectangle rectangle, boolean horz, boolean vert) {
		return rectangle.expand(horz ? SHRINK_SIZE : 0, vert ? SHRINK_SIZE : 0);
	}
}
