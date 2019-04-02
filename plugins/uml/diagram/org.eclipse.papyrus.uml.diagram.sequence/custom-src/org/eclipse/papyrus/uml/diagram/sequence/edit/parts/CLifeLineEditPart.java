/*****************************************************************************
 * Copyright (c) 2017, 2018 CEA LIST, ALL4TEC, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 519621, 526803
 *   Vincent Lorenzo (CEA LIST) vincent.lorenzo@cea.fr - Bug 531520
 *   Christian W. Damus - bugs 533672, 536486
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.common.draw2d.anchors.FixedAnchor;
import org.eclipse.papyrus.uml.diagram.sequence.LifelineNodePlate;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.LifeLineRestorePositionEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.LifelineSelectionEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.UpdateNodeReferenceEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.UpdateWeakReferenceForExecSpecEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.UpdateWeakReferenceForMessageSpecEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.figures.ILifelineInternalFigure;
import org.eclipse.papyrus.uml.diagram.sequence.figures.LifeLineLayoutManager;
import org.eclipse.papyrus.uml.diagram.sequence.figures.LifelineFigure;
import org.eclipse.papyrus.uml.diagram.sequence.figures.LifelineNodeFigure;
import org.eclipse.papyrus.uml.diagram.sequence.locator.MessageCreateLifelineAnchor;
import org.eclipse.papyrus.uml.diagram.sequence.locator.TimeElementLocator;
import org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.DisplayEvent;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageSort;

/**
 * @author Patrick Tessier
 * @since 3.0
 *
 */
public class CLifeLineEditPart extends LifelineEditPart {

	/** The default height of the figure. */
	public static int DEFAUT_HEIGHT = 700;

	/** The default width of the figure. */
	public static int DEFAUT_WIDTH = 100;

	/**
	 * The minimum height of the figure.
	 *
	 * @since 4.0
	 */
	public static int MIN_HEIGHT = 100;
	private EditPart activeCreateFeedbackEditPart;


	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CLifeLineEditPart(View view) {
		super(view);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpart.NodeEditPart#createSVGNodePlate()
	 */
	@Override
	protected NodeFigure createSVGNodePlate() {
		if (null == svgNodePlate) {
			svgNodePlate = new LifelineNodePlate(this, -1, -1).withLinkLFEnabled();
			svgNodePlate.setDefaultNodePlate(createNodePlate());
		}
		return svgNodePlate;
	}

	@Override
	protected NodeFigure createNodeFigure() {
		return new LifelineNodeFigure(createMainFigureWithSVG());
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.editparts.NamedElementEditPart#refresh()
	 *
	 */
	@Override
	public void refresh() {
		if (getPrimaryShape() instanceof LifelineFigure) {
			// Bug 531520: we redefine the border of the lifeline, in order to include the children
			// the message are connected to the middle line of the Lifeline, but they must be drawn as connected on the ExecutionSpeficiation
			final List<NodeFigure> childrenFigure = new ArrayList<>();
			for (final Object current : getChildren()) {
				if (current instanceof AbstractExecutionSpecificationEditPart) {
					NodeFigure figure = ((AbstractExecutionSpecificationEditPart) current).getPrimaryShape();
					childrenFigure.add(figure);
				}
			}
			getPrimaryShape().setChildrenFigure(childrenFigure);
		}

		super.refresh();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart#createDefaultEditPolicies()
	 */
	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new LifelineSelectionEditPolicy());
		installEditPolicy(LifeLineRestorePositionEditPolicy.KEY, new LifeLineRestorePositionEditPolicy());
		installEditPolicy(UpdateNodeReferenceEditPolicy.UDPATE_NODE_REFERENCE, new UpdateNodeReferenceEditPolicy());
		installEditPolicy(UpdateWeakReferenceForMessageSpecEditPolicy.UDPATE_WEAK_REFERENCE_FOR_MESSAGE, new UpdateWeakReferenceForMessageSpecEditPolicy());
		installEditPolicy(UpdateWeakReferenceForExecSpecEditPolicy.UDPATE_WEAK_REFERENCE_FOR_EXECSPEC, new UpdateWeakReferenceForExecSpecEditPolicy());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.uml.diagram.common.editparts.UMLNodeEditPart#setLayoutConstraint(org.eclipse.gef.EditPart, org.eclipse.draw2d.IFigure, java.lang.Object)
	 */
	@Override
	public void setLayoutConstraint(EditPart child, IFigure childFigure, Object constraint) {
		if (!(childFigure instanceof ILifelineInternalFigure)) {
			getPrimaryShape().setConstraint(childFigure, constraint);
		}
	}

	/**
	 * @return the size of the header height
	 *         if the layout is null return -1
	 * @since 4.0
	 */
	public int getStickerHeight() {
		if (getPrimaryShape().getLifeLineLayoutManager() != null) {
			return ((LifeLineLayoutManager) getPrimaryShape().getLifeLineLayoutManager()).getBottomHeader() - getPrimaryShape().getBounds().y();
		}
		return -1;
	}

	/**
	 * Create specific anchor to handle connection on top, on center and on bottom of the lifeline
	 */
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connEditPart) {
		if (connEditPart instanceof MessageCreateEditPart) {
			// Create message anchor
			return new MessageCreateLifelineAnchor(getPrimaryShape(), this);

		} else if (connEditPart instanceof MessageDeleteEditPart) {
			// delete message anchor
			return new FixedAnchor(getPrimaryShape(), FixedAnchor.BOTTOM);
		}
		// if (connEditPart instanceof MessageAsyncEditPart) {// TODO_MIA test it
		// String terminal = AnchorHelper.getAnchorId(getEditingDomain(), connEditPart, false);
		// if (terminal.length() > 0) {
		// int start = terminal.indexOf("{") + 1;
		// PrecisionPoint pt = BaseSlidableAnchor.parseTerminalString(terminal);
		// boolean rightHand = true;
		// if (start > 0) {
		// if (terminal.charAt(start) == 'L') {
		// rightHand = false;
		// }
		// } else {
		// Connection c = (Connection) connEditPart.getFigure();
		// PointList list = c.getPoints();
		// if (list.getPoint(0).x > list.getPoint(1).x) {
		// rightHand = false;
		// }
		// }
		// return new AnchorHelper.SideAnchor(getNodeFigure(), pt, rightHand);
		// }
		// }
		return super.getTargetConnectionAnchor(connEditPart);
	}

	/**
	 * @since 5.0
	 */
	@Override
	public EditPart getTargetEditPart(Request request) {
		if (request instanceof CreateViewAndElementRequest) {
			CreateViewAndElementRequest req = (CreateViewAndElementRequest) request;

			// If we're creating an operand, it needs to be done by the covering combined fragment
			if (UMLElementTypes.InteractionOperand_Shape.equals(req.getViewAndElementDescriptor().getElementAdapter().getAdapter(IElementType.class))) {
				EditPart container = SequenceUtil.findInteractionFragmentContainerEditPartAt(req.getLocation(), this);
				if (container instanceof CInteractionOperandEditPart) {
					// Delegate again to the combined fragment
					container = SequenceUtil.getParentCombinedFragmentPart(container);
				}
				if (container != null) {
					return container.getTargetEditPart(request);
				}
			} else if (UMLElementTypes.CombinedFragment_Shape.equals(req.getViewAndElementDescriptor().getElementAdapter().getAdapter(IElementType.class))
					|| UMLElementTypes.InteractionUse_Shape.equals(req.getViewAndElementDescriptor().getElementAdapter().getAdapter(IElementType.class))) {
				EditPart container = SequenceUtil.getInteractionCompartment(this);
				if (container != null) {
					return container.getTargetEditPart(request);
				}
			}
		}

		return super.getTargetEditPart(request);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void showTargetFeedback(Request request) {
		if (request instanceof CreateUnspecifiedTypeRequest) {
			((CreateUnspecifiedTypeRequest) request).getElementTypes().forEach(t -> {
				CreateRequest req = ((CreateUnspecifiedTypeRequest) request).getRequestForType((IElementType) t);
				EditPart targetEP = getTargetEditPart(req);
				// as target EP may vary during time, moving along a lifeline, crossing several elements for example
				// storing the target EP seems interesting.
				if (activeCreateFeedbackEditPart != targetEP) {
					// erase active feedback if any
					if (activeCreateFeedbackEditPart != null) {
						activeCreateFeedbackEditPart.eraseTargetFeedback(request);
					}
					activeCreateFeedbackEditPart = targetEP;
				}
				if (targetEP != this) {
					targetEP.showTargetFeedback(request);
				} else {
					super.showTargetFeedback(request);
				}
			});
			return;
		}
		super.showTargetFeedback(request);

	}

	@SuppressWarnings("unchecked")
	@Override
	public void eraseTargetFeedback(Request request) {
		if (request instanceof CreateUnspecifiedTypeRequest) {
			((CreateUnspecifiedTypeRequest) request).getElementTypes().forEach(t -> {
				CreateRequest req = ((CreateUnspecifiedTypeRequest) request).getRequestForType((IElementType) t);
				EditPart targetEP = getTargetEditPart(req);
				if (activeCreateFeedbackEditPart != null && activeCreateFeedbackEditPart != this) {
					// erase active feedback from saved EP
					activeCreateFeedbackEditPart.eraseTargetFeedback(request);
					activeCreateFeedbackEditPart = null;
				}
				if (targetEP != this) {
					targetEP.eraseTargetFeedback(request);
				} else {
					super.eraseTargetFeedback(request);
				}
			});
			return;
		}
		super.eraseTargetFeedback(request);
	}

	@Override
	protected boolean addFixedChild(EditPart childEditPart) {
		Optional<MessageEnd> createEnd = TimeElementLocator.getTimedElement(childEditPart, MessageEnd.class)
				.filter(MessageEnd::isReceive)
				.filter(end -> end.getMessage().getMessageSort() == MessageSort.CREATE_MESSAGE_LITERAL);

		return createEnd.map(__ -> {
			getBorderedFigure().getBorderItemContainer()
					.add(((IGraphicalEditPart) childEditPart).getFigure(),
							new TimeElementLocator(getMainFigure(), this::getTimeElementSide));

			return true;
		}).orElseGet(() -> super.addFixedChild(childEditPart));
	}

	public OptionalInt getCreateMessageIncomingSide(Point where) {
		// The proposed location is in relative coordinates, but the DisplayEvent API is
		// in absolute terms (dealing with the mouse pointer)
		Point search = where.getCopy();
		getMainFigure().translateToAbsolute(search);

		DisplayEvent displayEvent = new DisplayEvent(this);
		MessageEnd end = displayEvent.getMessageEvent(getMainFigure(), search);
		if ((end != null) && end.isReceive() && (end.getMessage().getMessageSort() == MessageSort.CREATE_MESSAGE_LITERAL)) {
			return getCreateMessageIncomingSide(end);
		}

		return OptionalInt.empty();
	}

	private OptionalInt getCreateMessageIncomingSide(MessageEnd end) {
		LifelineFigure lifelineFigure = (LifelineFigure) svgNodePlate.getChildren().get(0);
		IFigure headerFigure = lifelineFigure.getHeaderFigure();

		OptionalInt result = OptionalInt.empty();

		for (Object next : getTargetConnections()) {
			ConnectionEditPart incoming = (ConnectionEditPart) next;
			EObject semantic = incoming.getAdapter(EObject.class);
			if (semantic instanceof Message) {
				Message message = (Message) semantic;
				if (message.getMessageSort() == MessageSort.CREATE_MESSAGE_LITERAL) {
					Point headCenter = headerFigure.getBounds().getCenter();
					lifelineFigure.translateToAbsolute(headCenter);
					Connection conn = (Connection) incoming.getFigure();
					Point target = conn.getPoints().getLastPoint();
					if (target.x() > headCenter.x()) {
						// The create message is incoming from the right
						result = OptionalInt.of(PositionConstants.EAST);
					} else {
						result = OptionalInt.of(PositionConstants.WEST);
					}
				}
			}
		}

		return result;
	}

	int getTimeElementSide(Rectangle proposedBounds) {
		OptionalInt incoming = getCreateMessageIncomingSide(proposedBounds.getTopLeft());
		return incoming.isPresent()
				// Put the time element on the side opposite to the incoming create message
				? PositionConstants.EAST_WEST ^ incoming.getAsInt()
				// Center it on the lifeline
				: PositionConstants.CENTER;
	}
}
