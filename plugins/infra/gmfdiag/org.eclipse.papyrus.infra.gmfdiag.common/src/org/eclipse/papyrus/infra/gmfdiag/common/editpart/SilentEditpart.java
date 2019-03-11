/*****************************************************************************
 * Copyright (c) 2016 CEA LIST and others.
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

package org.eclipse.papyrus.infra.gmfdiag.common.editpart;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;

/**
 * @author Patrick Tessier
 * @since 3.0
 * The purpose is to display nothing equivalent of DefaultNodeEditPart
 *
 */
public class SilentEditpart extends GraphicalEditPart {

	/**
	 * Constructor.
	 *
	 * @param model
	 */
	public SilentEditpart(EObject model) {
		super(model);
	}

	public static final String VISUAL_ID="SilentEditPart"; 
	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#setVisibility(boolean)
	 *
	 * @param vis
	 */
	@Override
	protected void setVisibility(boolean vis) {
		super.setVisibility(false);
	}
	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#createFigure()
	 *
	 * @return
	 */
	@Override
	protected IFigure createFigure() {
		Figure fig= new RectangleFigure();
		return fig;
	}
	
	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#addChild(org.eclipse.gef.EditPart, int)
	 *
	 * @param child
	 * @param index
	 */
	@Override
	protected void addChild(EditPart child, int index) {
		// do nothing
	}
	
}
