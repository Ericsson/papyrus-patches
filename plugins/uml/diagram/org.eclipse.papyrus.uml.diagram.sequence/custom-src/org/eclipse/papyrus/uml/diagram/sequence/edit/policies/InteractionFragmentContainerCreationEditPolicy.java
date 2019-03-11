/*****************************************************************************
 * Copyright (c) 2018 CEA LIST, Christian W. Damus, and others.
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

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.Set;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultCreationEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.papyrus.uml.service.types.element.UMLElementTypes;
import org.eclipse.papyrus.uml.service.types.utils.ElementUtil;
import org.eclipse.papyrus.uml.service.types.utils.RequestParameterUtils;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;

/**
 * Custom creation edit policy for containers of {@link InteractionFragment}s, primarily
 * for the creation of such fragments.
 * 
 * @since 5.0
 */
public class InteractionFragmentContainerCreationEditPolicy extends DefaultCreationEditPolicy {

	/**
	 * Initializes me.
	 */
	public InteractionFragmentContainerCreationEditPolicy() {
		super();
	}

	@Override
	protected Command getCreateElementAndViewCommand(CreateViewAndElementRequest request) {
		IElementType typeToCreate = request.getViewAndElementDescriptor().getElementAdapter().getAdapter(IElementType.class);

		if (!ElementUtil.isTypeOf(typeToCreate, UMLElementTypes.LIFELINE)) {
			IEditCommandRequest semanticCreateRequest = (IEditCommandRequest) request.getViewAndElementDescriptor().getCreateElementRequestAdapter().getAdapter(IEditCommandRequest.class);
			if (semanticCreateRequest != null) {
				// What are the lifelines covered?
				Rectangle rectangle = getCreationRectangle(request);
				Set<Lifeline> covered = SequenceUtil.getCoveredLifelines(rectangle, getHost());
				RequestParameterUtils.setCoveredLifelines(semanticCreateRequest, covered);
			}
		}

		return super.getCreateElementAndViewCommand(request);
	}

	protected Rectangle getCreationRectangle(CreateViewAndElementRequest request) {
		Point location = request.getLocation();
		Dimension size = request.getSize();

		if (size == null) {
			return new Rectangle(location.x(), location.y(), 1, 1);
		}

		return new Rectangle(location, size);
	}
}
