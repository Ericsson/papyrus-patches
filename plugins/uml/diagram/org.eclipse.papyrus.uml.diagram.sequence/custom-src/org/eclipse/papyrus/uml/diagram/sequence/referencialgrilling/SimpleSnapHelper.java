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

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;

/**
 * @author Patrick Tessier
 *
 */
public class SimpleSnapHelper {

	/**
	 * @param update the point by taking account the spacing form the diagram
	 */
	/**
	 * @param update the point by taking account the spacing form the diagram
	 */
	public static  void snapAPoint(PrecisionRectangle ptOnScreen, RootEditPart drep) {
		if( drep instanceof DiagramRootEditPart){
		double spacing = ((DiagramRootEditPart)drep).getGridSpacing();
		int  modulo= (int)((ptOnScreen.y) / spacing);
		double  rest=(ptOnScreen.y) % spacing;
		if( rest>(spacing/2)){
			modulo=modulo+1;
		}
		if (modulo==0){
			ptOnScreen.setPreciseY(0);
		}
		else{
			ptOnScreen.setPreciseY((spacing*modulo));
		}


		modulo= (int)((ptOnScreen.x) / spacing);
		rest=(ptOnScreen.x) % spacing;
		if( rest>(spacing/2)){
			modulo=modulo+1;
		}
		if (modulo==0){
			ptOnScreen.setPreciseX(0);
		}
		else{
			ptOnScreen.setPreciseX((spacing*modulo));
		}
		}
	}
}
