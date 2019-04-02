/*****************************************************************************
 * Copyright (c) 2010 CEA
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Soyatec - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import org.eclipse.gef.Request;
import org.eclipse.gmf.runtime.notation.View;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomInteractionUseName2EditPart extends InteractionUseName2EditPart {

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CustomInteractionUseName2EditPart(View view) {
		super(view);
	}

	@Override
	public void performRequest(Request request) {
		if (request.getType().equals(REQ_OPEN)) {
			getParent().performRequest(request);
			return;
		}
		super.performRequest(request);
	}
}
