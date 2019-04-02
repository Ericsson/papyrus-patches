/*****************************************************************************
 * Copyright (c) 2018 Christian W. Damus, CEA LIST, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.Set;
import java.util.function.Function;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.Request;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.gmf.runtime.diagram.ui.figures.BorderedNodeFigure;
import org.eclipse.gmf.runtime.diagram.ui.figures.IBorderItemLocator;
import org.eclipse.gmf.runtime.diagram.ui.requests.DropObjectsRequest;
import org.eclipse.gmf.runtime.emf.type.core.ElementTypeRegistry;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.ISpecializationType;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.service.palette.AspectUnspecifiedTypeCreationTool.CreateAspectUnspecifiedTypeRequest;
import org.eclipse.papyrus.infra.services.edit.utils.ElementTypeUtils;
import org.eclipse.papyrus.uml.diagram.sequence.locator.CenterLocator;
import org.eclipse.papyrus.uml.service.types.element.UMLElementTypes;

import com.google.common.collect.ImmutableSet;

/**
 * Feed-back edit-policy for creation of time constraints and time observations
 * on various elements in the sequence diagram.
 */
public class TimeElementCreationFeedbackEditPolicy extends GraphicalEditPolicy {

	public static final String ROLE = "TimeElementCreationFeedback"; //$NON-NLS-1$

	private final Function<? super IFigure, ? extends IBorderItemLocator> locatorFunction;
	private IBorderItemLocator locator;
	private IFigure feedback;

	/**
	 * Initializes me.
	 */
	public TimeElementCreationFeedbackEditPolicy() {
		this(fig -> new CenterLocator(fig, PositionConstants.NONE));
	}

	public TimeElementCreationFeedbackEditPolicy(Function<? super IFigure, ? extends IBorderItemLocator> locatorFunction) {
		super();

		this.locatorFunction = locatorFunction;
	}

	protected IBorderItemLocator getFeedbackLocator() {
		if (locator == null) {
			locator = locatorFunction.apply(getFeedbackParent());
		}
		return locator;
	}

	IFigure getFeedbackParent() {
		IFigure result = getHostFigure();
		if (result instanceof BorderedNodeFigure) {
			// Unwrap to get the figure on which border items are presented
			result = ((BorderedNodeFigure) result).getMainFigure();
		}
		return result;
	}

	@Override
	public void showTargetFeedback(Request request) {
		IElementType typeToShow = getCreatedElementType(request);
		if (isTimeElementType(typeToShow)) {
			IFigure feedback = requireFeedback();

			// Use our locator to present the feed-back in the place where the edit-part
			// would end up putting the resulting element
			IBorderItemLocator locator = getFeedbackLocator();
			Point where = getRelativeLocation(request);
			Rectangle proposedLocation = new Rectangle(where, feedback.getSize());
			locator.setConstraint(proposedLocation);

			// Resolve the appropriate location in the feedback layer
			Rectangle resolved = locator.getValidLocation(proposedLocation, feedback);
			resolved.translate(0, -resolved.height() / 2); // Center on the mouse
			getFeedbackParent().translateToAbsolute(resolved);
			getFeedbackLayer().translateToRelative(resolved);
			feedback.setBounds(resolved);
		}
	}

	/**
	 * Determine the element type being created that is a time element for which
	 * we provide feed-back.
	 *
	 * @param request
	 *            the create/drop request
	 * @return the element type to be created/dropped, or {@code null} if indeterminate
	 */
	protected IElementType getCreatedElementType(Request request) {
		IElementType result = null;

		if (request instanceof CreateAspectUnspecifiedTypeRequest) {
			CreateAspectUnspecifiedTypeRequest createReq = (CreateAspectUnspecifiedTypeRequest) request;
			if (createReq.getElementTypes().size() == 1) {
				result = (IElementType) createReq.getElementTypes().get(0);
			}
		} else if (request instanceof DropObjectsRequest) {
			// Not creating the element, but creating the view, so infer the type from the element
			DropObjectsRequest dropReq = (DropObjectsRequest) request;
			if (dropReq.getObjects().size() == 1) {
				EObject element = EMFHelper.getEObject(dropReq.getObjects().get(0));
				if (element != null) {
					result = ElementTypeRegistry.getInstance().getElementType(element, ElementTypeUtils.getDefaultClientContext());
				}
			}
		}

		return result;
	}

	protected boolean isTimeElementType(IElementType type) {
		boolean result = false;

		if (type != null) {
			final Set<IElementType> types = ImmutableSet.of(UMLElementTypes.TIME_CONSTRAINT,
					UMLElementTypes.TIME_OBSERVATION);
			result = types.contains(type);
			if (!result && type instanceof ISpecializationType) {
				// Try subtypes
				ISpecializationType spec = (ISpecializationType) type;
				result = types.stream().anyMatch(spec::isSpecializationOf);
			}
		}

		return result;
	}

	protected Point getRelativeLocation(Request request) {
		Point result = null;

		// There are other requests that implement distinct getLocation API but
		// we aren't interested in those
		if (request instanceof DropRequest) {
			result = ((DropRequest) request).getLocation();
		}
		if (result != null) {
			// Don't destroy the request's data
			result = result.getCopy();
			getFeedbackParent().translateToRelative(result);
		} else {
			// Assume top left for want of anything requested
			result = new Point();
		}

		return result;
	}

	protected IFigure getFeedback() {
		if (feedback == null) {
			feedback = new RectangleFigure();
			feedback.setForegroundColor(ColorConstants.gray);
			feedback.setBackgroundColor(ColorConstants.gray);
			feedback.setSize(60, 5); // Make it thicker so that it's obvious
		}
		return feedback;
	}

	protected IFigure requireFeedback() {
		IFigure result = getFeedback();
		if (result.getParent() == null) {
			// Add it now because we need to show it
			addFeedback(result);
		}
		return result;
	}

	@Override
	public void eraseTargetFeedback(Request request) {
		if (feedback != null && feedback.getParent() != null) {
			// Remove it from the scene
			removeFeedback(feedback);
		}
	}

}
