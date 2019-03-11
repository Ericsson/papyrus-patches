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
package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.HighlightUtil;

/**
 * EditPolicy dedicated to highligh node/edge in case of mouseover.
 * 
 * @author flefevre
 *
 */
public class HighlightEditPolicy extends GraphicalEditPolicy {

	public static final String HIGHLIGHT_ROLE = "Highlight Edit Policy";


	@Override
	public void showTargetFeedback(Request request) {
		EditPart host = getHost();

		if (RequestConstants.REQ_DROP_OBJECTS.equals(request.getType())) {
			highlight(host);
		}
	}

	/**
	 * Highlight, subclass can override it.
	 */
	protected void highlight(EditPart object) {
		HighlightUtil.highlight(object);
	}

	/**
	 * Erase highlighted figure, subclass can override it.
	 */
	protected void unhighlight(EditPart object) {
		HighlightUtil.unhighlight(object);
	}

	/**
	 * Erase all highlighted figures, subclass can override it.
	 */
	protected void unhighlight() {
		HighlightUtil.unhighlight();
	}



	@Override
	public void eraseTargetFeedback(Request request) {
		unhighlight();
	}

	@Override
	public void eraseSourceFeedback(Request request) {
		super.eraseSourceFeedback(request);

	}


}
