/*****************************************************************************
 * Copyright (c) 2017, 2018 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Initial API and implementation
 *   Christian W. Damus - bug 539373, 536486
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import static org.eclipse.papyrus.uml.service.types.utils.ElementUtil.isTypeOf;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.SetBoundsCommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest.ViewDescriptor;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultCreationEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.util.DurationLinkUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.papyrus.uml.service.types.element.UMLDIElementTypes;
import org.eclipse.papyrus.uml.service.types.element.UMLElementTypes;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * This allows to define the creation edit policy for the execution specification.
 */
public class CustomExecutionSpecificationCreationEditPolicy extends DefaultCreationEditPolicy {

	private DisplayEvent displayEvent;

	public CustomExecutionSpecificationCreationEditPolicy() {
		super();
	}

	@Override
	public void setHost(EditPart host) {
		super.setHost(host);

		// Note that messages always actually connect to the lifeline, even if their
		// connection figures show as connected to the execution, so we need to use
		// the lifeline to find message ends
		EditPart lifeline = SequenceUtil.getParentLifelinePart(getHost());
		displayEvent = new DisplayEvent(lifeline != null ? lifeline : host);
	}

	@Override
	protected ICommand getSetBoundsCommand(CreateViewRequest request, ViewDescriptor descriptor) {
		if (UMLDIElementTypes.TIME_CONSTRAINT_SHAPE.getSemanticHint().equals(descriptor.getSemanticHint())
				|| UMLDIElementTypes.TIME_OBSERVATION_SHAPE.getSemanticHint().equals(descriptor.getSemanticHint())) {
			// check the position of the request to give the basic constraint for the created shape (should not be moveable)
			Point location = request.getLocation().getCopy();
			location.setX(-10);
			IFigure execFigure = ((IGraphicalEditPart) getHost()).getFigure();
			boolean isStart = DurationLinkUtil.isStart(execFigure, location);
			if (isStart) {
				location.setY(-1);
			} else {
				location.setY(Short.MAX_VALUE);
			}
			Dimension size = new Dimension(40, 1);
			return new SetBoundsCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), DiagramUIMessages.Commands_MoveElement, descriptor, new Rectangle(location, size));
		}
		return super.getSetBoundsCommand(request, descriptor);
	}

	@SuppressWarnings("unchecked")
	@Override
	public EditPart getTargetEditPart(Request request) {
		EditPart result = super.getTargetEditPart(request);
		if (!(request instanceof CreateRequest)) {
			return result;
		}
		CreateRequest create = (CreateRequest) request;

		Stream<? extends IElementType> elementTypes = null;
		if (request instanceof CreateViewAndElementRequest) {
			elementTypes = ((CreateViewAndElementRequest) request).getViewDescriptors().stream()
					.map(v -> v.getElementAdapter().getAdapter(IElementType.class));
		} else if (request instanceof CreateUnspecifiedTypeRequest) {
			elementTypes = ((CreateUnspecifiedTypeRequest) request).getElementTypes().stream();
		}
		if (elementTypes != null) {
			Predicate<IElementType> isInteresting = type -> isTypeOf(type, UMLElementTypes.TIME_CONSTRAINT);
			isInteresting = isInteresting.or(type -> isTypeOf(type, UMLElementTypes.TIME_OBSERVATION));
			isInteresting = isInteresting.or(type -> isTypeOf(type, UMLElementTypes.EXECUTION_SPECIFICATION));
			Optional<EClass> interestingType = elementTypes.map(type -> {
				if (isTypeOf(type, UMLElementTypes.TIME_CONSTRAINT)) {
					return UMLPackage.Literals.TIME_CONSTRAINT;
				} else if (isTypeOf(type, UMLElementTypes.TIME_OBSERVATION)) {
					return UMLPackage.Literals.TIME_OBSERVATION;
				} else if (isTypeOf(type, UMLElementTypes.EXECUTION_SPECIFICATION)) {
					return UMLPackage.Literals.EXECUTION_SPECIFICATION;
				} else {
					return null;
				}
			}).findFirst();

			result = interestingType.<EditPart> map(type -> {
				switch (type.getClassifierID()) {
				case UMLPackage.TIME_CONSTRAINT:
				case UMLPackage.TIME_OBSERVATION:
					Point loc = create.getLocation();
					if (loc != null) {
						// Is a message end, here? Note that messages always actually connect to the
						// lifeline, even if their connection figures show as connected to me, so
						// we need to delegate to the lifeline to create the notation. Otherwise,
						// re-targeting of the message to some other execution or to the lifeline
						// itself will be complicated by the fact of the time element being a border
						// node of this execution. Besides that it is more consistent with the
						// edge anchoring implementation anyways
						MessageOccurrenceSpecification messageOcc = displayEvent.getMessageEvent(((IGraphicalEditPart) getHost()).getFigure(), loc);
						if (messageOcc != null) {
							return SequenceUtil.getParentLifelinePart(getHost());
						}
					}
					break;
				case UMLPackage.EXECUTION_SPECIFICATION:
					// The lifeline is responsible for creating all execution specifications, as they
					// are semantically all children of it (nesting is strictly visual)
					return SequenceUtil.getParentLifelinePart(getHost());
				}
				return null;
			}).orElse(result);
		}

		return result;
	}

}
