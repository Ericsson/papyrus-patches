/*****************************************************************************
 * Copyright (c) 2014 CEA LIST.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *	Gabriel Pascual (ALL4TEC) gabriel.pascual@all4tec.net - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import java.util.Arrays;
import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editpolicies.SelectionHandlesEditPolicy;
import org.eclipse.gef.handles.MoveHandle;

/**
 * Edit policy for primary selection of Label.
 *
 * @author Gabriel Pascual
 */
public class LabelPrimarySelectionEditPolicy extends SelectionHandlesEditPolicy {

	/** The Constant LABEL_PRIMARY_SELECTION_KEY. */
	public static final String LABEL_PRIMARY_SELECTION_KEY = "primary_label";


	/**
	 * Constructor.
	 *
	 */
	public LabelPrimarySelectionEditPolicy() {
		super();
	}

	/**
	 * Creates the selection handles.
	 *
	 * @return the list
	 * @see org.eclipse.gef.editpolicies.SelectionHandlesEditPolicy#createSelectionHandles()
	 */
	@Override
	protected List<IFigure> createSelectionHandles() {
		IFigure labelHandle = new PapyrusLabelHandle((GraphicalEditPart) getHost());
		return Arrays.asList(labelHandle);
	}


	/**
	 * The Class PapyrusLabelHandle.
	 */
	private class PapyrusLabelHandle extends MoveHandle {

		/** The line border style. */
		private LineBorder lineBorder;

		/**
		 * Constructor.
		 *
		 * @param owner
		 *            the owner
		 */
		public PapyrusLabelHandle(GraphicalEditPart owner) {
			super(owner);
		}

		/**
		 * Initialize.
		 *
		 * @see org.eclipse.gef.handles.MoveHandle#initialize()
		 */
		@Override
		protected void initialize() {
			lineBorder = new LineBorder(null, 1, Graphics.LINE_DOT);
			setOpaque(false);
			setBorder(getStyleLine());
		}

		/**
		 * Returns the style line for the border of the handle.
		 * 
		 * @return the style line of the handle
		 */
		protected LineBorder getStyleLine() {
			return (isPrimary()) ? lineBorder : null;
		}

		/**
		 * Returns <code>true</code> if the handle's owner is the primary selection.
		 * 
		 * @return <code>true</code> if the handles owner has primary selection.
		 */
		protected boolean isPrimary() {
			return getOwner().getSelected() == EditPart.SELECTED_PRIMARY;
		}

	}

}