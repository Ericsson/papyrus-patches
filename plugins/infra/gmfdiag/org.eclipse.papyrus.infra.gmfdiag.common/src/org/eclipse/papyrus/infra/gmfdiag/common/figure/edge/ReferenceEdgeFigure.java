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
package org.eclipse.papyrus.infra.gmfdiag.common.figure.edge;

import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.PapyrusWrappingLabel;

/**
 * {@link PapyrusEdgeFigure} for references.
 * 
 * @author Mickaël ADAM
 * @since 3.1
 */
public final class ReferenceEdgeFigure extends PapyrusEdgeFigure {

	/**
	 * Creates a new DashEdgeFigure.
	 */
	public ReferenceEdgeFigure() {
		super();
		createContents();
	}

	/**
	 * The name label.
	 */
	private WrappingLabel nameLabel;

	/**
	 * Get the name label.
	 */
	public WrappingLabel getEdgeLabel() {
		return nameLabel;
	}

	/**
	 * Create the content.
	 */
	protected void createContents() {
		nameLabel = new PapyrusWrappingLabel();
		nameLabel.setText("");//$NON-NLS-1$
		this.add(nameLabel);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.figure.edge.PapyrusEdgeFigure#resetStyle()
	 */
	@Override
	public void resetStyle() {
		setSourceDecoration(null);
		setTargetDecoration(null);
		super.resetStyle();
	}
}