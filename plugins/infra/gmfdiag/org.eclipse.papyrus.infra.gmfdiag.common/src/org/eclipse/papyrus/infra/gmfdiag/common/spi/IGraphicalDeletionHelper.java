/*****************************************************************************
 * Copyright (c) 2016 Christian W. Damus and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.spi;

import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramEditPartsUtil;

/**
 * Protocol of a pluggable graphical deletion helper strategy.
 */
public interface IGraphicalDeletionHelper {
	/** The default instance just checks whether the edit-part is not read-only. */
	IGraphicalDeletionHelper DEFAULT = ep -> !DiagramEditPartsUtil.isReadOnly(ep);

	/**
	 * Queries whether the specified edit-part can be deleted. If the helper
	 * does not have a specific positive answer for the edit-part, it should
	 * return {@code false} to let another helper answer (the ultimate fall-back
	 * checks the standard read-only state of the edit-part).
	 * 
	 * @param editPart
	 *            an edit-part that is proposed for deletion
	 * 
	 * @return whether it may be delete, or {@code false} if I don't know
	 */
	boolean canDelete(IGraphicalEditPart editPart);

	/**
	 * Composes me with an{@code other} helper in a disjuntion
	 * on the {@link #canDelete(IGraphicalEditPart)} operation.
	 * 
	 * @param other
	 *            another graphical deletion helper
	 * 
	 * @return the composed helper
	 */
	default IGraphicalDeletionHelper compose(IGraphicalDeletionHelper other) {
		return ep -> other.canDelete(ep) || this.canDelete(ep);
	}
}
