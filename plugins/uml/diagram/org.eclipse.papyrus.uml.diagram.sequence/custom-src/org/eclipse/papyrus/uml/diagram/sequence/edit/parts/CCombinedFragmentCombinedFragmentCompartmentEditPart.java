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

package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.figures.ResizableCompartmentFigure;
import org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.figures.layout.SwimlanesCompartmentLayout;

/**
 * This class has been modified for 2 reasons:
 * - refresh in order to ensure the refresh about size of children
 * - compute the ratio for each children.
 *
 * @since 3.0
 *
 */
public class CCombinedFragmentCombinedFragmentCompartmentEditPart extends CombinedFragmentCombinedFragmentCompartmentEditPart {

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CCombinedFragmentCombinedFragmentCompartmentEditPart(View view) {
		super(view);
	}

	/**
	 * this method has been overloaded in order to ensure the refresh about children size
	 */
	@Override
	protected void refreshBounds() {
		super.refreshBounds();

		// this code has been added in order to force the refresh of Sub Combined fragment
		if (children != null) {
			for (Object child : children) {
				if (child instanceof EditPart) {
					((EditPart) child).refresh();
				}
			}
		}
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentCombinedFragmentCompartmentEditPart#createFigure()
	 *
	 * @return
	 */
	@Override
	public IFigure createFigure() {
		ResizableCompartmentFigure rcf = (ResizableCompartmentFigure) super.createFigure();
		SwimlanesCompartmentLayout layout = new SwimlanesCompartmentLayout();
		layout.setStretchMajorAxis(false);
		layout.setStretchMinorAxis(true);
		layout.setMinorAlignment(ConstrainedToolbarLayout.ALIGN_TOPLEFT);
		rcf.getContentPane().setLayoutManager(layout);
		rcf.getContentPane().setBorder(null);
		return rcf;
	}
}
