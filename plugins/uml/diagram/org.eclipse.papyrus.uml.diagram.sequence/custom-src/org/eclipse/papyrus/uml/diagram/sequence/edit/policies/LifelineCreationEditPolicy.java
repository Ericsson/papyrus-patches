/*****************************************************************************
 * (c) Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import static java.util.Collections.singleton;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest.ViewAndElementDescriptor;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultCreationEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.papyrus.uml.service.types.element.UMLDIElementTypes;
import org.eclipse.papyrus.uml.service.types.utils.ElementUtil;
import org.eclipse.papyrus.uml.service.types.utils.SequenceRequestConstant;

/**
 * This editPolicy has in charge to redirect creation of element to interaction
 *
 */
public class LifelineCreationEditPolicy extends DefaultCreationEditPolicy {

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.CreationEditPolicy#getCreateElementAndViewCommand(org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest)
	 *
	 * @param request
	 * @return
	 */
	@Override
	protected Command getCreateElementAndViewCommand(CreateViewAndElementRequest request) {
		// Used during the drop from the model explorer
		if (request instanceof CreateViewAndElementRequest) {
			CreateViewAndElementRequest req = request;
			ViewAndElementDescriptor descriptor = (req).getViewAndElementDescriptor();
			IElementType elementType = descriptor.getElementAdapter().getAdapter(IElementType.class);



			if (isControlledByLifeline(elementType)) {
				// get the element descriptor
				CreateElementRequestAdapter requestAdapter = req.getViewAndElementDescriptor().getCreateElementRequestAdapter();
				// get the semantic request
				CreateElementRequest createElementRequest = (CreateElementRequest) requestAdapter.getAdapter(
						CreateElementRequest.class);
				View view = (View) getHost().getModel();
				EObject hostElement = ViewUtil.resolveSemanticElement(view);
				createElementRequest.setContainer(hostElement.eContainer());
				createElementRequest.setParameter(SequenceRequestConstant.COVERED, hostElement);
				createElementRequest.setParameter(SequenceRequestConstant.COVERED_LIFELINES, singleton(hostElement));
				// case of Message Occurence Specification
/*				MessageOccurrenceSpecification mos = displayEvent.getMessageEvent(getHostFigure().getParent().getParent(), ((CreateRequest) request).getLocation());
				if (mos != null) {
					createElementRequest.setParameter(org.eclipse.papyrus.uml.service.types.utils.SequenceRequestConstant.REPLACE_EXECUTION_SPECIFICATION_START, mos);
				}*/
			}
			/*
			 * Fix of Bug 531471 - [SequenceDiagram] Combined Fragment / Interaction Use should be create over a Lifeline.
			 * Recalculation of location of combined fragment for according to interaction compartment position
			 */
			else if (ElementUtil.isTypeOf(elementType, UMLDIElementTypes.COMBINED_FRAGMENT_SHAPE)
					|| ElementUtil.isTypeOf(elementType, UMLDIElementTypes.INTERACTION_USE_SHAPE)) {
				Rectangle boundsLifeline = getHostFigure().getBounds();
				Point pointCombinedFragment = req.getLocation();

				pointCombinedFragment.x = pointCombinedFragment.x + boundsLifeline.x;
				pointCombinedFragment.y = pointCombinedFragment.y + boundsLifeline.y;

				req.setLocation(pointCombinedFragment);

				return getHost().getParent().getCommand(req);
			}
		}
		return super.getCreateElementAndViewCommand(request);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultCreationEditPolicy#getReparentCommand(org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart)
	 */
	@Override
	protected ICommand getReparentCommand(final IGraphicalEditPart gep) {
		// The reparent of execution specification in another life line is not allowed
		if (gep instanceof AbstractExecutionSpecificationEditPart) {
			final LifelineEditPart parentLifeLine = SequenceUtil.getParentLifelinePart(gep);
			if (null != parentLifeLine && !parentLifeLine.equals(getHost())) {
				return null;
			}
		}

		return super.getReparentCommand(gep);
	}
	
	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultCreationEditPolicy#getReparentViewCommand(org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart)
	 *
	 * @param gep
	 * @return
	 */
	@Override
	protected ICommand getReparentViewCommand(IGraphicalEditPart gep) {
		// The reparent of execution specification in another life line is not allowed
		if (gep instanceof AbstractExecutionSpecificationEditPart) {
			final LifelineEditPart parentLifeLine = SequenceUtil.getParentLifelinePart(gep);
			if (null != parentLifeLine && !parentLifeLine.equals(getHost())) {
				return null;
			}
		}
		return super.getReparentViewCommand(gep);
	}

	/**
	 * test if the element Type that is normally not a child of the Lifeline should be controlled by the lifeline.
	 * Then The lifeline will be set as the parent editpart, but not as the semantic parent.
	 *
	 * This is the case of most of the affixed node.
	 *
	 * @param elementType
	 *            the tested element type
	 * @return true if the Lifeline should be the
	 */
	protected boolean isControlledByLifeline(IElementType elementType) {
		boolean controlledByLifeline = false;

		if (ElementUtil.isTypeOf(elementType, UMLDIElementTypes.ACTION_EXECUTION_SPECIFICATION_SHAPE)) {
			controlledByLifeline = true;
		} else if (ElementUtil.isTypeOf(elementType, UMLDIElementTypes.BEHAVIOR_EXECUTION_SPECIFICATION_SHAPE)) {
			controlledByLifeline = true;
		} else if (ElementUtil.isTypeOf(elementType, UMLDIElementTypes.TIME_CONSTRAINT_SHAPE)) {
			controlledByLifeline = true;
		} else if (ElementUtil.isTypeOf(elementType, UMLDIElementTypes.STATE_INVARIANT_SHAPE)) {
			controlledByLifeline = true;
		} else if (ElementUtil.isTypeOf(elementType, UMLDIElementTypes.COMBINED_FRAGMENT_CO_REGION_SHAPE)) {
			controlledByLifeline = true;
		} else if (ElementUtil.isTypeOf(elementType, UMLDIElementTypes.DURATION_CONSTRAINT_SHAPE)) {
			controlledByLifeline = true;
		}

		return controlledByLifeline;

	}

	/**
	 * Return the host's figure.
	 * The super calls getFigure(). This is a problem when used with shapecompartments. Instead,
	 * return getContextPane(). In shape comaprtments this will return the correct containing figure.
	 */
	protected IFigure getHostFigure() {
		return ((GraphicalEditPart) getHost()).getContentPane();
	}
}
