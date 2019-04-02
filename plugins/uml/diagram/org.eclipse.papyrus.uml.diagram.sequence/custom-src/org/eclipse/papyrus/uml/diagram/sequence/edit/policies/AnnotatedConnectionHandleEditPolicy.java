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
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.handles.ConnectionHandle;
import org.eclipse.gmf.runtime.diagram.ui.handles.ConnectionHandle.HandleDirection;
import org.eclipse.gmf.runtime.diagram.ui.internal.tools.ConnectionHandleTool;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest.ConnectionViewDescriptor;
import org.eclipse.gmf.runtime.emf.type.core.IHintedType;
import org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes;

/**
 * Custom ConnectionHandleEditPolicy for Comment, Observation and Constraint, only one outgoing supported.
 *
 * @author Jin Liu (jin.liu@soyatec.com)
 */
@SuppressWarnings("restriction")
public class AnnotatedConnectionHandleEditPolicy extends SequenceConnectionHandleEditPolicy {

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected List getHandleFigures() {
		List list = new ArrayList(1);
		String tooltip = buildTooltip(HandleDirection.OUTGOING);
		if (tooltip != null) {
			ConnectionHandle connectionHandle = new ConnectionHandle((IGraphicalEditPart) getHost(), HandleDirection.OUTGOING, tooltip);
			connectionHandle.setDragTracker(new ConnectionHandleTool(connectionHandle) {

				@Override
				protected String getCommandName() {
					if (isInState(STATE_CONNECTION_STARTED | STATE_ACCESSIBLE_DRAG_IN_PROGRESS)) {
						return AnnotatedLinkEndEditPolicy.REQ_ANNOTATED_LINK_END;
					} else {
						return AnnotatedLinkStartEditPolicy.REQ_ANNOTATED_LINK_START;
					}
				}

				@Override
				protected CreateConnectionRequest createTargetRequest() {
					IHintedType type = (IHintedType) UMLElementTypes.Comment_AnnotatedElementEdge;
					return new CreateConnectionViewRequest(new ConnectionViewDescriptor(type, type.getSemanticHint(), getPreferencesHint()));
				}
			});
			list.add(connectionHandle);
		}
		return list;
	}
}
