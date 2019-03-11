/*****************************************************************************
 * Copyright (c) 2017 CEA LIST, ALL4TEC and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpart;

import org.eclipse.draw2d.Connection;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ITreeBranchEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultSemanticEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.edge.ReferenceEdgeFigure;

/**
 * {@link ConnectionEditPart} for Reference edge.
 * 
 * @author Mickael ADAM
 * 
 * @since 3.1
 */
public class ReferenceEdgeEditPart extends ConnectionEditPart implements ITreeBranchEditPart {

	/**
	 * The visual Id.
	 */
	public static final String VISUAL_ID = "ReferenceLink";//$NON-NLS-1$

	/**
	 * Constructor.
	 * 
	 * @param view
	 *            The view.
	 */
	public ReferenceEdgeEditPart(final View view) {
		super(view);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpart.ConnectionEditPart#createDefaultEditPolicies()
	 */
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new DefaultSemanticEditPolicy());
	}

	/**
	 * Add fixed child.
	 */
	protected boolean addFixedChild(final EditPart childEditPart) {
		if (childEditPart instanceof ReferenceEdgeNameEditPart) {
			((ReferenceEdgeNameEditPart) childEditPart).setLabel(getPrimaryShape().getEdgeLabel());
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#addChildVisual(org.eclipse.gef.EditPart, int)
	 */
	protected void addChildVisual(final EditPart childEditPart, final int index) {
		if (addFixedChild(childEditPart)) {
			return;
		}
		super.addChildVisual(childEditPart, -1);
	}

	/**
	 * Remove fixed child.
	 */
	protected boolean removeFixedChild(final EditPart childEditPart) {
		if (childEditPart instanceof ReferenceEdgeNameEditPart) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#removeChildVisual(org.eclipse.gef.EditPart)
	 */
	protected void removeChildVisual(final EditPart childEditPart) {
		if (removeFixedChild(childEditPart)) {
			return;
		}
		super.removeChildVisual(childEditPart);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart#createConnectionFigure()
	 */
	protected Connection createConnectionFigure() {
		return new ReferenceEdgeFigure();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpart.IPapyrusEditPart#getPrimaryShape()
	 */
	public ReferenceEdgeFigure getPrimaryShape() {
		return (ReferenceEdgeFigure) getFigure();
	}

}
