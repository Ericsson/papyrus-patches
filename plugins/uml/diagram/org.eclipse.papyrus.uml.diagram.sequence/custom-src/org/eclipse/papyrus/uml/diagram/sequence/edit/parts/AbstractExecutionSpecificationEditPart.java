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
 *   CEA LIST - Initial API and implementation
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 526079
 *   Christian W. Damus - bug 536486
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import java.util.List;
import java.util.Optional;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.DelegatingLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.common.core.util.Log;
import org.eclipse.gmf.runtime.common.core.util.Trace;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IBorderItemEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.DragDropEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.diagram.ui.figures.IBorderItemLocator;
import org.eclipse.gmf.runtime.diagram.ui.internal.DiagramUIDebugOptions;
import org.eclipse.gmf.runtime.diagram.ui.internal.DiagramUIPlugin;
import org.eclipse.gmf.runtime.diagram.ui.internal.DiagramUIStatusCodes;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest.ConnectionViewDescriptor;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeConnectionRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
import org.eclipse.gmf.runtime.emf.type.core.IHintedType;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.notation.Anchor;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.FillStyle;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.gmf.runtime.notation.datatype.GradientData;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IPapyrusNodeFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.SelectableBorderedNodeFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.snap.PapyrusDragEditPartsTrackerEx;
import org.eclipse.papyrus.uml.diagram.common.editparts.RoundedCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.AffixedNodeAlignmentEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.anchors.NodeBottomAnchor;
import org.eclipse.papyrus.uml.diagram.sequence.anchors.NodeTopAnchor;
import org.eclipse.papyrus.uml.diagram.sequence.edit.helpers.AnchorHelper;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.AppliedStereotypeCommentCreationEditPolicyEx;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.ExecutionSpecificationAffixedChildAlignmentPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.ExecutionSpecificationResizableEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.figures.ExecutionSpecificationNodePlate;
import org.eclipse.papyrus.uml.diagram.sequence.locator.CenterLocator;
import org.eclipse.papyrus.uml.diagram.sequence.locator.TimeElementLocator;
import org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes;
import org.eclipse.papyrus.uml.diagram.sequence.tools.PapyrusSequenceDragEditPartsTracker;
import org.eclipse.papyrus.uml.diagram.sequence.util.DurationLinkUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.GeneralOrderingUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.OccurrenceSpecificationUtil;
import org.eclipse.papyrus.uml.diagram.stereotype.edition.editpolicies.AppliedStereotypeCommentEditPolicy;
import org.eclipse.swt.graphics.Color;
import org.eclipse.uml2.uml.MessageEnd;

/**
 * Add implementing IPapyrusEditPart to displaying Stereotypes.
 *
 * @author Jin Liu (jin.liu@soyatec.com)
 */
@SuppressWarnings("restriction")
public abstract class AbstractExecutionSpecificationEditPart extends RoundedCompartmentEditPart {

	/**
	 * @deprecated since 5.1 this constant is not used anymore.
	 */
	@Deprecated
	public static int DEFAUT_HEIGHT = 100;
	public static int DEFAUT_WIDTH = 20;

	// private List<ExecutionSpecificationEndEditPart> executionSpecificationEndParts;

	public AbstractExecutionSpecificationEditPart(View view) {
		super(view);
	}

	static class FillParentLocator implements Locator {

		@Override
		public void relocate(IFigure target) {
			target.setBounds(target.getParent().getBounds());
		}
	}

	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		// -- Grilling -- installEditPolicy(ConnectYCoordinateToGrillingEditPolicy.CONNECT_TO_GRILLING_MANAGEMENT, new ConnectExecutionToGridEditPolicy());
		installEditPolicy(AppliedStereotypeCommentEditPolicy.APPLIED_STEREOTYPE_COMMENT, new AppliedStereotypeCommentCreationEditPolicyEx());
		//installEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE, new SequenceReferenceEditPolicy()); 
		//installEditPolicy(UpdateConnectionReferenceEditPolicy.UDPATE_CONNECTION_REFERENCE, new UpdateConnectionReferenceEditPolicy());
		//installEditPolicy(UpdateWeakReferenceForExecSpecEditPolicy.UDPATE_WEAK_REFERENCE_FOR_EXECSPEC, new UpdateWeakReferenceForExecSpecEditPolicy());

		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new ExecutionSpecificationResizableEditPolicy());
		installEditPolicy(AffixedNodeAlignmentEditPolicy.AFFIXED_CHILD_ALIGNMENT_ROLE, new ExecutionSpecificationAffixedChildAlignmentPolicy());
		installEditPolicy(EditPolicyRoles.DRAG_DROP_ROLE+"Ex", new DragDropEditPolicy() {
			public Command getCommand(Request request) {
				if (RequestConstants.REQ_DRAG.equals(request.getType()) || 
					RequestConstants.REQ_DROP.equals(request.getType()) ||
					RequestConstants.REQ_DROP_OBJECTS.equals(request.getType())) {
					Command cmd = super.getCommand(request);
					if (cmd == null) {
						return getHost().getParent().getCommand(request);
					}
				}
				return super.getCommand(request);
			}
		});
	}

	@Override
	protected void addBorderItem(IFigure borderItemContainer, IBorderItemEditPart borderItemEditPart) {
		if (borderItemEditPart instanceof TimeConstraintBorderNodeEditPart ||
				borderItemEditPart instanceof TimeObservationBorderNodeEditPart) {
			Optional<MessageEnd> messageEnd = Optional.of(borderItemEditPart)
					.filter(ITimeElementBorderNodeEditPart.class::isInstance)
					.map(ITimeElementBorderNodeEditPart.class::cast)
					.flatMap(ITimeElementBorderNodeEditPart::getMessageEnd);

			IBorderItemLocator locator = messageEnd
					// It needs to remain anchored to the message end
					.map(__ -> new CenterLocator(getMainFigure(), PositionConstants.NONE))
					.map(IBorderItemLocator.class::cast)
					.orElseGet(() -> new TimeElementLocator(getMainFigure(),
							constraint -> findNearestSide(getMainFigure(), constraint)));
			borderItemContainer.add(borderItemEditPart.getFigure(), locator);
		} else {
			super.addBorderItem(borderItemContainer, borderItemEditPart);
		}
	}

	/**
	 * Given a {@code constraint} proposed for a border item (usually a time element)
	 * on an execution figure, compute the side to which it should be attached.
	 *
	 * @param execFig
	 *            an execution specification figure
	 * @param constraint
	 *            a proposed time element border item bounds
	 *
	 * @return a {@link PositionConstants} side
	 */
	public static int findNearestSide(IFigure execFig, Rectangle constraint) {
		Rectangle figBounds = execFig.getBounds().getCopy();
		Point where = constraint.getTopLeft();
		where.setX((figBounds.width() - constraint.width()) / 2);

		if (DurationLinkUtil.isStart(execFig, where)) {
			// Pin it to the top of the execution
			return PositionConstants.NORTH;
		} else {
			// Pin it to the bottome
			return PositionConstants.SOUTH;
		}
	}

	@Override
	public EditPart getTargetEditPart(Request request) {
		if (request instanceof CreateRequest) {
			return super.getTargetEditPart(request);
		}
		return super.getTargetEditPart(request);
	}

	@Override
	protected void setLineWidth(int width) {
		if (getPrimaryShape() instanceof NodeFigure) {
			((NodeFigure) getPrimaryShape()).setLineWidth(width);
		}
	}

	/**
	 * Override to set the transparency to the correct figure
	 */
	@Override
	protected void setTransparency(int transp) {
		getPrimaryShape().setTransparency(transp);
	}

	/**
	 * sets the back ground color of this edit part
	 *
	 * @param color
	 *            the new value of the back ground color
	 */
	@Override
	protected void setBackgroundColor(Color color) {
		getPrimaryShape().setBackgroundColor(color);
		getPrimaryShape().setIsUsingGradient(false);
		getPrimaryShape().setGradientData(-1, -1, 0);
	}

	/**
	 * Override to set the gradient data to the correct figure
	 */
	@Override
	protected void setGradient(GradientData gradient) {
		IPapyrusNodeFigure fig = getPrimaryShape();
		FillStyle style = (FillStyle) getPrimaryView().getStyle(NotationPackage.Literals.FILL_STYLE);
		if (gradient != null) {
			fig.setIsUsingGradient(true);
			fig.setGradientData(style.getFillColor(), gradient.getGradientColor1(), gradient.getGradientStyle());
		} else {
			fig.setIsUsingGradient(false);
		}
	}

	@Override
	public boolean supportsGradient() {
		return true;
	}

	// @Override
	// protected void handleNotificationEvent(Notification event) {
	// super.handleNotificationEvent(event);
	// Object feature = event.getFeature();
	// if ((getModel() != null) && (getModel() == event.getNotifier())) {
	// if (NotationPackage.eINSTANCE.getLineStyle_LineWidth().equals(feature)) {
	// refreshLineWidth();
	// } else if (NotationPackage.eINSTANCE.getLineTypeStyle_LineType().equals(feature)) {
	// refreshLineType();
	// }
	// } else if (NotationPackage.eINSTANCE.getLocation_X().equals(feature) || NotationPackage.eINSTANCE.getLocation_Y().equals(feature) || NotationPackage.eINSTANCE.getSize_Height().equals(feature)
	// || NotationPackage.eINSTANCE.getSize_Width().equals(feature)) {
	// getParent().refresh();
	// } else if (UMLPackage.eINSTANCE.getExecutionSpecification_Finish().equals(feature) || UMLPackage.eINSTANCE.getExecutionSpecification_Start().equals(feature)) {
	// if (executionSpecificationEndParts != null) {
	// for (ExecutionSpecificationEndEditPart child : executionSpecificationEndParts) {
	// removeChild(child);
	// child.removeFromResource();
	// }
	// executionSpecificationEndParts = null;
	// }
	// refreshChildren();
	// }
	// refreshShadow();
	// }

	@Override
	public abstract ExecutionSpecificationRectangleFigure getPrimaryShape();

	// /**
	// * Override for add elements on ExecutionSpecification
	// */
	// @Override
	// public Command getCommand(Request request) {
	// if (request instanceof CreateUnspecifiedTypeRequest) {
	// return getParent().getCommand(request);
	// }
	// return super.getCommand(request);
	// }
	//
	// /**
	// * @generated NOT Override for redirecting creation request to the lifeline
	// */
	// @Override
	// public void showSourceFeedback(Request request) {
	// if (request instanceof CreateUnspecifiedTypeRequest) {
	// getParent().showSourceFeedback(request);
	// }
	// super.showSourceFeedback(request);
	// }
	//
	// /**
	// * @generated NOT Override for redirecting creation request to the lifeline
	// */
	// @Override
	// public void eraseSourceFeedback(Request request) {
	// if (request instanceof CreateUnspecifiedTypeRequest) {
	// getParent().eraseSourceFeedback(request);
	// }
	// super.eraseSourceFeedback(request);
	// }
	//
	// @Override
	// public void showTargetFeedback(Request request) {
	// if (request instanceof CreateUnspecifiedTypeRequest) {
	// getParent().showTargetFeedback(request);
	// }
	// super.showTargetFeedback(request);
	// }
	//
	// @Override
	// public void eraseTargetFeedback(Request request) {
	// if (request instanceof CreateUnspecifiedTypeRequest) {
	// getParent().eraseTargetFeedback(request);
	// }
	// super.eraseTargetFeedback(request);
	// }

	/**
	 * Add connection on top off the figure during the feedback.
	 */
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		if (request instanceof CreateUnspecifiedTypeConnectionRequest) {
			CreateUnspecifiedTypeConnectionRequest createRequest = (CreateUnspecifiedTypeConnectionRequest) request;
			List<?> relationshipTypes = createRequest.getElementTypes();
			for (Object type : relationshipTypes) {
				if (UMLElementTypes.Message_SynchEdge.equals(type)) {
					// Sync Message
					if (!createRequest.getTargetEditPart().equals(createRequest.getSourceEditPart())) {
						return new AnchorHelper.FixedAnchorEx(getFigure(), PositionConstants.TOP);
					}
					// otherwise, this is a recursive call, let destination free
				} else if (UMLElementTypes.DurationConstraint_Edge.equals(type) || UMLElementTypes.DurationObservation_Edge.equals(type) || UMLElementTypes.GeneralOrdering_Edge.equals(type)) {
					return OccurrenceSpecificationUtil.isStart(getFigure(), createRequest.getLocation()) ? new NodeTopAnchor(getFigure()) : new NodeBottomAnchor(getFigure());
				}
			}
		} else if (request instanceof ReconnectRequest) {
			ReconnectRequest reconnectRequest = (ReconnectRequest) request;
			ConnectionEditPart connectionEditPart = reconnectRequest.getConnectionEditPart();
			if (connectionEditPart instanceof MessageSyncEditPart) {
				// Sync Message
				return new AnchorHelper.FixedAnchorEx(getFigure(), PositionConstants.TOP);
			} else if (DurationLinkUtil.isDurationLink(reconnectRequest) || GeneralOrderingUtil.isGeneralOrderingLink(reconnectRequest)) {
				return OccurrenceSpecificationUtil.isStart(getFigure(), reconnectRequest.getLocation()) ? new NodeTopAnchor(getFigure()) : new NodeBottomAnchor(getFigure());
			}
		}
		// Fixed bug about computing target anchor when creating message sync.
		else if (request instanceof CreateConnectionViewRequest) {
			CreateConnectionViewRequest createRequest = (CreateConnectionViewRequest) request;
			ConnectionViewDescriptor viewDesc = ((CreateConnectionViewRequest) request).getConnectionViewDescriptor();
			if (((IHintedType) UMLElementTypes.Message_SynchEdge).getSemanticHint().equals(viewDesc.getSemanticHint())) {
				// Sync Message
				if (!createRequest.getTargetEditPart().equals(createRequest.getSourceEditPart())) {
					return new AnchorHelper.FixedAnchorEx(getFigure(), PositionConstants.TOP);
				}
			}
			if (DurationLinkUtil.isDurationLink(createRequest) || GeneralOrderingUtil.isGeneralOrderingLink(createRequest)) {
				return OccurrenceSpecificationUtil.isStart(getFigure(), createRequest.getLocation()) ? new NodeTopAnchor(getFigure()) : new NodeBottomAnchor(getFigure());
			}
		}
		ConnectionAnchor anchor = super.getTargetConnectionAnchor(request);
		return anchor;
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 *
	 * @param connEditPart
	 *            The connection edit part.
	 * @return The anchor.
	 */
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connEditPart) {
		if (connEditPart instanceof MessageSyncEditPart) {
			// Sync Message
			return new AnchorHelper.FixedAnchorEx(getFigure(), PositionConstants.TOP);
		}
		final org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart connection = (org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart) connEditPart;
		String t = null;
		try {
			t = (String) getEditingDomain().runExclusive(new RunnableWithResult.Impl<String>() {

				@Override
				public void run() {
					Anchor a = ((Edge) connection.getModel()).getTargetAnchor();
					if (a instanceof IdentityAnchor) {
						setResult(((IdentityAnchor) a).getId());
					} else {
						setResult(""); //$NON-NLS-1$
					}
				}
			});
		} catch (InterruptedException e) {
			Trace.catching(DiagramUIPlugin.getInstance(), DiagramUIDebugOptions.EXCEPTIONS_CATCHING, getClass(), "getTargetConnectionAnchor", e); //$NON-NLS-1$
			Log.error(DiagramUIPlugin.getInstance(), DiagramUIStatusCodes.IGNORED_EXCEPTION_WARNING, "getTargetConnectionAnchor", e); //$NON-NLS-1$
		}
		if (t != null && !"".equals(t)) {
			int position = AnchorHelper.FixedAnchorEx.parsePosition(t);
			if (position != -1) {
				return new AnchorHelper.FixedAnchorEx(getFigure(), position);
			}
		}
		return super.getTargetConnectionAnchor(connEditPart);
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.Request)
	 *
	 * @param request
	 *            The request
	 * @return The anchor
	 */
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		if (request instanceof CreateUnspecifiedTypeConnectionRequest) {
			CreateUnspecifiedTypeConnectionRequest createRequest = (CreateUnspecifiedTypeConnectionRequest) request;
			List<?> relationshipTypes = createRequest.getElementTypes();
			for (Object type : relationshipTypes) {
				if (UMLElementTypes.Message_ReplyEdge.equals(type)) {
					// Reply Message
					return new AnchorHelper.FixedAnchorEx(getFigure(), PositionConstants.BOTTOM);
				} else if (UMLElementTypes.DurationConstraint_Edge.equals(type) || UMLElementTypes.DurationObservation_Edge.equals(type) || UMLElementTypes.GeneralOrdering_Edge.equals(type)) {
					return OccurrenceSpecificationUtil.isStart(getFigure(), createRequest.getLocation()) ? new NodeTopAnchor(getFigure()) : new NodeBottomAnchor(getFigure());
				}
			}
		} else if (request instanceof ReconnectRequest) {
			ReconnectRequest reconnectRequest = (ReconnectRequest) request;
			ConnectionEditPart connectionEditPart = reconnectRequest.getConnectionEditPart();
			if (connectionEditPart instanceof MessageReplyEditPart) {
				// Reply Message
				return new AnchorHelper.FixedAnchorEx(getFigure(), PositionConstants.BOTTOM);
			} else if (DurationLinkUtil.isDurationLink(reconnectRequest) || GeneralOrderingUtil.isGeneralOrderingLink(reconnectRequest)) {
				return OccurrenceSpecificationUtil.isStart(getFigure(), reconnectRequest.getLocation()) ? new NodeTopAnchor(getFigure()) : new NodeBottomAnchor(getFigure());
			}
		} else if (request instanceof CreateConnectionViewRequest) {
			CreateConnectionViewRequest createRequest = (CreateConnectionViewRequest) request;
			if (DurationLinkUtil.isDurationLink(createRequest) || GeneralOrderingUtil.isGeneralOrderingLink(createRequest)) {
				return OccurrenceSpecificationUtil.isStart(getFigure(), createRequest.getLocation()) ? new NodeTopAnchor(getFigure()) : new NodeBottomAnchor(getFigure());
			}
		}
		ConnectionAnchor anchor = super.getSourceConnectionAnchor(request);
		return anchor;
		
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 *
	 * @param connEditPart
	 *            The connection edit part.
	 * @return The anchor.
	 */
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connEditPart) {
		if (connEditPart instanceof MessageReplyEditPart) {
			// Reply Message
			return new AnchorHelper.FixedAnchorEx(getFigure(), PositionConstants.BOTTOM);
		}
		final org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart connection = (org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart) connEditPart;
		String t = null;
		try {
			t = (String) getEditingDomain().runExclusive(new RunnableWithResult.Impl<String>() {

				@Override
				public void run() {
					Anchor a = ((Edge) connection.getModel()).getSourceAnchor();
					if (a instanceof IdentityAnchor) {
						setResult(((IdentityAnchor) a).getId());
					} else {
						setResult(""); //$NON-NLS-1$
					}
				}
			});
		} catch (InterruptedException e) {
			Trace.catching(DiagramUIPlugin.getInstance(), DiagramUIDebugOptions.EXCEPTIONS_CATCHING, getClass(), "getSourceConnectionAnchor", e); //$NON-NLS-1$
			Log.error(DiagramUIPlugin.getInstance(), DiagramUIStatusCodes.IGNORED_EXCEPTION_WARNING, "getSourceConnectionAnchor", e); //$NON-NLS-1$
		}
		if (t != null && !"".equals(t)) {
			int position = AnchorHelper.FixedAnchorEx.parsePosition(t);
			if (position != -1) {
				return new AnchorHelper.FixedAnchorEx(getFigure(), position);
			}
		}
		return super.getSourceConnectionAnchor(connEditPart);
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		refreshTransparency();
		refreshShadow();
	}

	@Override
	protected NodeFigure createNodeFigure() {
		return new SelectableBorderedNodeFigure(createMainFigureWithSVG()) {

			@Override
			public void setBounds(Rectangle rect) {
				// TODO Auto-generated method stub
				super.setBounds(rect);
			}
			
		};
	}

	@Override
	protected NodeFigure createMainFigureWithSVG() {
		NodeFigure figure = createSVGNodePlate();
		// bug 494019: [Sequence Diagram] Opening Luna Sequence Diagram into Neon doesn't work : change the layout from DelegatingLayout to StackLayout
		figure.setLayoutManager(new DelegatingLayout() {
			/**
			 * Override it to verify type of constraint.
			 *
			 * @see org.eclipse.draw2d.DelegatingLayout#layout(org.eclipse.draw2d.IFigure)
			 */
			@Override
			public void layout(IFigure parent) {
				List<?> children = parent.getChildren();
				for (int i = 0; i < children.size(); i++) {
					IFigure child = (IFigure) children.get(i);

					Object locator = getConstraint(child);
					if (locator instanceof Locator) {
						((Locator) locator).relocate(child);
					}
				}
			}
		});
		shape = createNodeShape();
		figure.add(shape, new FillParentLocator());
		setupContentPane(shape);
		return figure;
	}

	@Override
	protected NodeFigure createSVGNodePlate() {
		ExecutionSpecificationNodePlate svgNodePlateFigure = new ExecutionSpecificationNodePlate(this, -1, -1);
		svgNodePlateFigure.setMinimumSize(new Dimension(getMapMode().DPtoLP(16), getMapMode().DPtoLP(20))); // min height 20
		svgNodePlate = svgNodePlateFigure.withLinkLFEnabled();
		svgNodePlate.setDefaultNodePlate(createNodePlate());
		return svgNodePlate;
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#showTargetFeedback(org.eclipse.gef.Request)
	 *
	 * @param request
	 */
	@Override
	public void showTargetFeedback(Request request) {
		if (request instanceof ChangeBoundsRequest) {
			ChangeBoundsRequest changeBoundsRequest = (ChangeBoundsRequest) request;

			if (changeBoundsRequest.getEditParts().get(0) instanceof LifelineEditPart) {
				changeBoundsRequest.setMoveDelta(new Point(changeBoundsRequest.getMoveDelta().x, 0));
			}
		}
		super.showTargetFeedback(request);
	}

	@Override
	public DragTracker getDragTracker(Request req) {
		return new PapyrusSequenceDragEditPartsTracker(this, true, false, false) {
			@Override
			protected void setCloneActive(boolean cloneActive) {
				super.setCloneActive(false); // Disable cloning
			}			
		};
	}
}
