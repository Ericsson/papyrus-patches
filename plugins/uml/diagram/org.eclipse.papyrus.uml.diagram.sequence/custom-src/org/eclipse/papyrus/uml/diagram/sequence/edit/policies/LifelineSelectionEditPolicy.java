/*****************************************************************************
 * Copyright (c) 2010, 2017-2018 CEA List, ALL4TEC and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA List - Initial API and implementation
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 522228
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Bug 538466
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.handles.MoveHandle;
import org.eclipse.gef.handles.MoveHandleLocator;
import org.eclipse.gef.handles.RelativeHandleLocator;
import org.eclipse.gef.handles.ResizeHandle;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.tools.ResizeTracker;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IPrimaryEditPart;
import org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.SemanticFromGMFElement;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.PapyrusResizableShapeEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractMessageEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.figures.LifelineFigure;
import org.eclipse.papyrus.uml.diagram.sequence.util.LifelineEditPartUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.LifelineMessageDeleteHelper;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.OccurrenceSpecification;

public class LifelineSelectionEditPolicy extends PapyrusResizableShapeEditPolicy {

	public LifelineSelectionEditPolicy() {
		setResizeDirections(PositionConstants.WEST | PositionConstants.EAST | PositionConstants.SOUTH);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.PapyrusResizableShapeEditPolicy#getResizeCommand(org.eclipse.gef.requests.ChangeBoundsRequest)
	 */
	@Override
	protected Command getResizeCommand(final ChangeBoundsRequest request) {
		LifelineEditPart llEditPart = (LifelineEditPart) getHost();
		if (!LifelineMessageDeleteHelper.hasIncomingMessageDelete(llEditPart)) {

			final Dimension sizeDelta = request.getSizeDelta();
			final int moveHeight = sizeDelta.height();

			if (moveHeight != 0) {

				final Rectangle absoluteBounds = SequenceUtil.getAbsoluteBounds(llEditPart);
				final Point pointFromWhichOneSearch = new Point(absoluteBounds.x + (absoluteBounds.width / 2), absoluteBounds.y + absoluteBounds.height);

				// Get the last Y position of elements on life line
				final int maxY = getLastPointOfLifeLineChildren(llEditPart, pointFromWhichOneSearch);

				// Compare the last Y position with the current move to determinate if the resize can be done
				if (maxY > (absoluteBounds.y + absoluteBounds.height() + moveHeight)) {
					return null;
				}
			}

			return super.getResizeCommand(request);
		}
		return null;
	}

	/**
	 * This allows to calculate the last Y position of elements on life line.
	 *
	 * @param lifelineEditPart
	 *            The life line edit part.
	 * @param pointFromWhichOneSearch
	 *            The last point of the life line.
	 * @return the last Y position of the last event on the life line.
	 */
	protected int getLastPointOfLifeLineChildren(final LifelineEditPart lifelineEditPart, final Point pointFromWhichOneSearch) {
		int maxY = 0;

		final List<OccurrenceSpecification> previousEvents = LifelineEditPartUtil.getPreviousEventsFromPosition(pointFromWhichOneSearch, lifelineEditPart);

		if (previousEvents != null && !previousEvents.isEmpty()) {
			final OccurrenceSpecification lastPreviousEvent = previousEvents.get(previousEvents.size() - 1);
			if (lastPreviousEvent instanceof ExecutionOccurrenceSpecification) {
				final ExecutionSpecification execSpec = ((ExecutionOccurrenceSpecification) lastPreviousEvent).getExecution();
				final IGraphicalEditPart editPart = getEditPartFromSemantic(execSpec);
				if (editPart instanceof AbstractExecutionSpecificationEditPart) {
					final EObject element = ((View) ((AbstractExecutionSpecificationEditPart) editPart).getAdapter(View.class)).getElement();

					if (element instanceof ExecutionSpecification && null != ((ExecutionSpecification) element).getStart() && ((ExecutionSpecification) element).getStart().equals(lastPreviousEvent)) {
						final Rectangle absoluteBounds = SequenceUtil.getAbsoluteBounds(editPart);

						if (maxY < (absoluteBounds.y + absoluteBounds.height())) {
							maxY = absoluteBounds.y + absoluteBounds.height();
						}
					}
				}

			} else if (lastPreviousEvent instanceof MessageOccurrenceSpecification) {
				final Message message = ((MessageOccurrenceSpecification) lastPreviousEvent).getMessage();
				final IGraphicalEditPart editPart = getEditPartFromSemantic(message);
				if (editPart instanceof ConnectionEditPart) {
					final EObject element = ((View) ((AbstractMessageEditPart) editPart).getAdapter(View.class)).getElement();
					if (element instanceof Message && null != ((Message) element).getSendEvent() && ((Message) element).getSendEvent().equals(lastPreviousEvent)
							|| element instanceof Message && null != ((Message) element).getReceiveEvent() && ((Message) element).getReceiveEvent().equals(lastPreviousEvent)) {

						PolylineConnectionEx polyline = (PolylineConnectionEx) ((ConnectionEditPart) editPart).getFigure();
						Point anchorPositionOnScreen;
						if (((Message) element).getSendEvent().equals(lastPreviousEvent)) {
							anchorPositionOnScreen = polyline.getSourceAnchor().getReferencePoint();
						} else {
							anchorPositionOnScreen = polyline.getTargetAnchor().getReferencePoint();
						}

						if (maxY < anchorPositionOnScreen.y) {
							maxY = anchorPositionOnScreen.y;
						}
					}
				}
			}
		}

		return maxY;
	}

	/**
	 * This method return the controller attached to the semantic element.
	 * The complexity of this algorithm is N (N the number of controller in the opened sequence diagram).
	 *
	 * @param semanticElement
	 *            This must be different from null.
	 * @return The reference to the controller or null.
	 */
	protected IGraphicalEditPart getEditPartFromSemantic(final Object semanticElement) {
		IGraphicalEditPart researchedEditPart = null;
		final SemanticFromGMFElement semanticFromGMFElement = new SemanticFromGMFElement();
		final EditPartViewer editPartViewer = getHost().getViewer();
		if (editPartViewer != null) {
			// look for all edit part if the semantic is contained in the list
			final Iterator<?> iter = editPartViewer.getEditPartRegistry().values().iterator();

			while (iter.hasNext() && researchedEditPart == null) {
				final Object currentEditPart = iter.next();
				// look only amidst IPrimary editpart to avoid compartment and labels of links
				if (currentEditPart instanceof IPrimaryEditPart) {
					final Object currentElement = semanticFromGMFElement.getSemanticElement(currentEditPart);
					if (semanticElement.equals(currentElement)) {
						researchedEditPart = ((IGraphicalEditPart) currentEditPart);
					}
				}
			}
		}
		return researchedEditPart;
	}

	@Override
	protected List<?> createSelectionHandles() {
		final LifelineEditPart host = (LifelineEditPart) getHost();
		final LifelineFigure primaryShape = host.getPrimaryShape();
		// resizable in at least one direction
		List<Handle> list = new ArrayList<>();
		// createMoveHandle(list);
		final RectangleFigure figure = primaryShape.getFigureLifelineNameContainerFigure();
		final Locator locator = new MoveHandleLocator(figure);
		final MoveHandle moveHandle = new MoveHandle((GraphicalEditPart) getHost(), locator);
		figure.addFigureListener(new FigureListener() {

			@Override
			public void figureMoved(IFigure source) {
				locator.relocate(moveHandle);
			}
		});
		moveHandle.setCursor(Cursors.SIZEALL);
		list.add(moveHandle);
		// createResizeHandle(list, PositionConstants.NORTH);
		final IFigure fig = primaryShape.getFigureLifelineNameContainerFigure();
		createResizeHandle(host, list, fig, PositionConstants.WEST);
		createResizeHandle(host, list, fig, PositionConstants.EAST);
		createResizeHandle(host, list, primaryShape, PositionConstants.SOUTH);
		return list;
	}

	private void createResizeHandle(LifelineEditPart host, List<Handle> list, IFigure fig, int location) {
		final Locator locator = new RelativeHandleLocator(fig, location);
		Cursor cursor = Cursors.getDirectionalCursor(location, fig.isMirrored());
		final ResizeHandle westResizer = new ResizeHandle(host, locator, cursor);
		ResizeTracker resizeTracker = new ResizeTracker(host, location);
		westResizer.setDragTracker(resizeTracker);

		final RectangleFigure figure = host.getPrimaryShape().getFigureLifelineNameContainerFigure();
		figure.addFigureListener(new FigureListener() {

			@Override
			public void figureMoved(IFigure source) {
				locator.relocate(westResizer);
			}
		});
		list.add(westResizer);
	}

	// TODO_MIA check it
	// @Override
	// protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
	// IFigure feedback = getDragSourceFeedbackFigure();
	// PrecisionRectangle rect = new PrecisionRectangle(getInitialFeedbackBounds().getCopy());
	// getHostFigure().translateToAbsolute(rect);
	// boolean skipMinSize = false;
	// // Only enable horizontal dragging on lifelines(except lifelines that are result of a create message).
	// // https://bugs.eclipse.org/bugs/show_bug.cgi?id=364688
	// if (this.getHost() instanceof OLDLifelineEditPart) {
	// skipMinSize = true;
	// OLDLifelineEditPart lifelineEP = (OLDLifelineEditPart) this.getHost();
	// if (!SequenceUtil.isCreateMessageEndLifeline(lifelineEP)) {
	// request.getMoveDelta().y = 0;
	// }
	// // keepNameLabelBounds(lifelineEP, request, rect);
	// // restrict child size within parent bounds
	// keepInParentBounds(lifelineEP, request, rect);
	// changeCombinedFragmentBounds(request, lifelineEP);
	// // Adjust lifeline's height if no DestructionOccurrenceSpecification
	// List<?> children = lifelineEP.getChildren();
	// boolean hasDOS = false;
	// for (Object child : children) {
	// if (child instanceof DestructionOccurrenceSpecificationEditPart) {
	// hasDOS = true;
	// }
	// }
	// if (!hasDOS) {
	// rect.height -= request.getMoveDelta().y;
	// }
	// }
	// Point left = rect.getLeft();
	// Point right = rect.getRight();
	// rect.translate(request.getMoveDelta());
	// rect.resize(request.getSizeDelta());
	// IFigure f = getHostFigure();
	// Dimension min = f.getMinimumSize().getCopy();
	// Dimension max = f.getMaximumSize().getCopy();
	// // IMapMode mmode = MapModeUtil.getMapMode(f);
	// // min.height = mmode.LPtoDP(min.height);
	// // min.width = mmode.LPtoDP(min.width);
	// // max.height = mmode.LPtoDP(max.height);
	// // max.width = mmode.LPtoDP(max.width);
	// getHostFigure().translateToAbsolute(min);
	// getHostFigure().translateToAbsolute(max);
	// // In manual mode, there is no minimal width, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=383723
	// if (min.width > rect.width && !skipMinSize) {
	// if (request.getMoveDelta().x > 0 && request.getSizeDelta().width != 0) {
	// rect.x = right.x - min.width;
	// request.getMoveDelta().x = rect.x - left.x;
	// }
	// rect.width = min.width;
	// } else if (max.width < rect.width) {
	// rect.width = max.width;
	// }
	// if (min.height > rect.height) {
	// rect.height = min.height;
	// } else if (max.height < rect.height) {
	// rect.height = max.height;
	// }
	// feedback.translateToRelative(rect);
	// feedback.setBounds(rect);
	// }

	private void keepNameLabelBounds(LifelineEditPart lifelineEP, ChangeBoundsRequest request, PrecisionRectangle rect) {
		PrecisionRectangle size = getMovedRectangle(rect, request);
		Dimension preferSize = lifelineEP.getPrimaryShape().getFigureLifelineNameContainerFigure().getPreferredSize(-1, -1).getCopy();
		getHostFigure().translateToAbsolute(preferSize); // handle scale size
		if (size.width < preferSize.width) {
			request.getSizeDelta().width = preferSize.width - rect.width;
			if (request.getMoveDelta().x > 0) {
				request.getMoveDelta().x = rect.width - preferSize.width;
			}
		}
	}

	protected Rectangle getCurrentConstraintFor(GraphicalEditPart child) {
		IFigure fig = child.getFigure();
		return (Rectangle) fig.getParent().getLayoutManager().getConstraint(fig);
	}

	private void keepInParentBounds(LifelineEditPart lifelineEP, ChangeBoundsRequest request, PrecisionRectangle rect) {
		if (lifelineEP.getParent() instanceof LifelineEditPart) {
			LifelineEditPart parent = (LifelineEditPart) lifelineEP.getParent();
			Rectangle p = parent.getFigure().getBounds().getCopy();
			parent.getFigure().translateToAbsolute(p);
			PrecisionRectangle c = getMovedRectangle(rect, request);
			Dimension preferSize = getHostFigure().getPreferredSize();
			getHostFigure().translateToAbsolute(preferSize); // handle scale size
			if (request.getType().equals(RequestConstants.REQ_RESIZE)) {
				switch (request.getResizeDirection()) {
				case PositionConstants.WEST:
					if (c.getLeft().x <= p.getLeft().x) { // exceed left edge
						int delta = (p.getLeft().x - c.getLeft().x);
						request.getMoveDelta().x += delta;
						request.getSizeDelta().width -= delta;
					}
					break;
				case PositionConstants.EAST:
					if (c.getRight().x + request.getSizeDelta().width >= p.getRight().x) { // exceed right edge
						int delta = (c.getRight().x - p.getRight().x);
						request.getSizeDelta().width -= delta;
					}
					break;
				}
			} else {
				if (c.getLeft().x <= p.getLeft().x) { // exceed left edge
					int delta = (p.getLeft().x - c.getLeft().x);
					request.getMoveDelta().x += delta;
				} else if (c.getRight().x >= p.getRight().x) { // exceed right edge
					int delta = (c.getRight().x - p.getRight().x);
					request.getMoveDelta().x -= delta;
				}
			}
			// check lifeline intersect with each other
			c = getMovedRectangle(rect, request);
			Rectangle other = getLifelineIntersectBounds(lifelineEP, parent, request, c);
			if (other != null) {
				if (request.getSizeDelta().width == 0) { // move only
					// Fixed bug about moving child lifelines, do NOT allow move out now.
					if (request.getMoveDelta().x > 0) { // move right
						if ((p.right() - other.right()) > 0) {
							request.getMoveDelta().x = other.getRight().x - rect.getLeft().x;
						} else {
							request.getMoveDelta().x = 0;// no margin for moving
						}
					} else {
						if ((other.x - p.x) > 0) {
							request.getMoveDelta().x = other.getLeft().x - rect.getRight().x;
						} else {
							request.getMoveDelta().x = 0;// no margin for moving
						}
					}
				} else {
					if (request.getMoveDelta().x == 0) { // resize right edge
						request.getSizeDelta().width = other.getLeft().x - rect.getRight().x;
					} else { // resize left edge
						request.getMoveDelta().x = other.getRight().x - rect.getLeft().x;
						request.getSizeDelta().width = -request.getMoveDelta().x;
					}
				}
			}
		}
	}

	private Rectangle getLifelineIntersectBounds(LifelineEditPart lifelineEP, LifelineEditPart parent, ChangeBoundsRequest request, PrecisionRectangle rect) {
		List children = parent.getChildren();
		for (Object o : children) {
			if (o instanceof LifelineEditPart && o != lifelineEP) {
				LifelineEditPart p = (LifelineEditPart) o;
				Rectangle bounds = p.getFigure().getBounds().getCopy();
				p.getFigure().translateToAbsolute(bounds);
				if (bounds.intersects(rect)) {
					return bounds;
				}
			}
		}
		return null;
	}

	private PrecisionRectangle getMovedRectangle(PrecisionRectangle rect, ChangeBoundsRequest request) {
		PrecisionRectangle c = rect.getPreciseCopy();
		c.translate(request.getMoveDelta());
		c.resize(request.getSizeDelta());
		return c;
	}

	private void changeCombinedFragmentBounds(ChangeBoundsRequest request, LifelineEditPart lifelineEP) {
		if (request.getMoveDelta().x > 0) {
			return;
		}
		View shape = (View) lifelineEP.getModel();
		Lifeline element = (Lifeline) shape.getElement();
		EList<InteractionFragment> covereds = element.getCoveredBys();
		EditPart parent = lifelineEP.getParent();
		List<?> children = parent.getChildren();
		Rectangle bounds = lifelineEP.getFigure().getBounds().getCopy();
		bounds.translate(request.getMoveDelta());
		Point center = bounds.getCenter();
		for (Object obj : children) {
			if (obj instanceof CombinedFragmentEditPart) {
				CombinedFragmentEditPart et = (CombinedFragmentEditPart) obj;
				View sp = (View) et.getModel();
				if (!covereds.contains(sp.getElement())) {
					continue;
				}
				// If the center vertical line is covered by the CombibedFragment, do NOT move the CF again.
				Rectangle rect = ((GraphicalEditPart) et).getFigure().getBounds();
				if (rect.x < center.x && rect.right() > center.x) {
					continue;
				}
				changeCombinedFragmentBounds(request, et, lifelineEP);
			}
		}
	}

	Point maxMoveDelta;

	@Override
	protected void eraseChangeBoundsFeedback(ChangeBoundsRequest request) {
		super.eraseChangeBoundsFeedback(request);
		maxMoveDelta = null;
	}

	void changeCombinedFragmentBounds(ChangeBoundsRequest request, CombinedFragmentEditPart cep, LifelineEditPart lifelineEP) {
		Rectangle rect = getTransformedRectangle(cep, request);
		if (rect.x <= 0) {
			if (maxMoveDelta != null) {
				request.getMoveDelta().x = maxMoveDelta.x;
			} else {
				Point p = new Point(Math.abs(rect.x), 0);
				cep.getFigure().translateToAbsolute(p);
				request.getMoveDelta().x = Math.min(0, request.getMoveDelta().x + p.x);
				maxMoveDelta = request.getMoveDelta().getCopy();
			}
		}
	}

	private Rectangle getTransformedRectangle(CombinedFragmentEditPart cep, ChangeBoundsRequest request) {
		Rectangle rect = new PrecisionRectangle(cep.getFigure().getBounds());
		cep.getFigure().translateToAbsolute(rect);
		rect = request.getTransformedRectangle(rect);
		cep.getFigure().translateToRelative(rect);
		return rect;
	}
}
