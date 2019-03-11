/*****************************************************************************
 * Copyright (c) 2014 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *    Celine Janssens - inspired by IBM class RubberbandSelectionTool : modify the calculateNewSelection method and add constructors
 *
 ****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.selection;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.plaf.basic.BasicComboBoxUI.KeyHandler;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.tools.AbstractTool;
import org.eclipse.gmf.runtime.diagram.ui.editparts.LabelEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.util.SelectInDiagramHelper;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;

/**
 * A Tool which selects multiple objects inside a rectangular area of a Graphical Viewer.<br/>
 * If the SHIFT key is pressed at the beginning of the drag, the enclosed items will be
 * appended to the current selection.<br/>
 * If the CONTROL key is pressed at the beginning of
 * the drag, the enclosed items will have their selection state inverted.
 * <P>
 * This tool is inspired from RubberbandSelectionTool to add some features.
 * <li>Labels , affixed child nodes and connections are part of the eligible Edit Parts to be selected</li>
 * <li>Additional constructors allow to select which behavior and which kind of editParts will be part of the selection</li>
 * <li>Additional members allow to select which behavior and which kind of editParts are part of the selection</li>
 * <li>Even the not visible EditParts inside of a container (due to scroll bars) will be part of selection</li>
 */
public class PapyrusRubberbandSelectionTool extends AbstractTool {

	/**
	 * Mode in case of CTRL modifier key.
	 */
	static final int TOGGLE_MODE = 1;

	/**
	 * Mode in case of SHIFT modifier key.
	 */
	static final int APPEND_MODE = 2;

	private int mode;

	private Figure marqueeRectangleFigure;

	private Set<EditPart> allChildren = new HashSet<EditPart>();

	private List<EditPart> selectedEditParts;

	private Request targetRequest;

	private Point feedBackStartLocation = null;

	private WeakReference<?> weakReference;

	private static final Request MARQUEE_REQUEST = new Request(RequestConstants.REQ_SELECTION);

	/**
	 * Standard Marquee Selection behavior.
	 */
	public static final int BEHAVIOR_OBJECT_INCLUDED = new Integer(1).intValue();

	/**
	 * Rubberband Marquee Selection behavior.
	 */
	public static final int BEHAVIOR_OBJECT_TOUCHED = new Integer(2).intValue();

	/**
	 * Default Marquee Selection behavior.
	 */
	public static final int DEFAULT_MARQUEE_BEHAVIOR = BEHAVIOR_OBJECT_INCLUDED;


	private int marqueeBehavior = DEFAULT_MARQUEE_BEHAVIOR;

	/**
	 * Define if the nodes should be part of the selection by default
	 */
	public static final boolean DEFAULT_SELECT_NODES = true;

	/**
	 * Define if the Labels should be part of the selection by default
	 */
	public static final boolean DEFAULT_SELECT_LABELS = true;

	/**
	 * Define if the Connections should be part of the selection by default
	 */
	public static final boolean DEFAULT_SELECT_CONNECTIONS = true;

	/**
	 * Parameter to define if Nodes should be part of the selection
	 */
	private boolean selectNodes = DEFAULT_SELECT_NODES;

	/**
	 * Parameter to define if Labels should be part of the selection
	 */
	private boolean selectLabels = DEFAULT_SELECT_LABELS;

	/**
	 * Parameter to define if Connections should be part of the selection
	 */
	private boolean selectConnections = DEFAULT_SELECT_CONNECTIONS;


	/**
	 * Constructor.
	 * Creates a new MarqueeSelectionTool.
	 */
	public PapyrusRubberbandSelectionTool() {
		setDefaultCursor(Cursors.CROSS);
		setUnloadWhenFinished(false);
	}


	/**
	 * Constructor.
	 *
	 * @param behavior
	 *            <li>BEHAVIOR_OBJECT_INCLUDE = 1</li> <li>BEHAVIOR_OBJECT_TOUCHED = 2</li>
	 */
	public PapyrusRubberbandSelectionTool(int behavior) {
		this();
		this.setMarqueeBehavior(behavior);
	}


	/**
	 * Constructor.
	 *
	 * @param behavior
	 *            Define what behavior is expected by the tool.
	 *
	 *            <li>BEHAVIOR_OBJECT_INCLUDE = 1</li> <li>BEHAVIOR_OBJECT_TOUCHED = 2</li>
	 * @param nodeSelectable
	 *            true if Nodes should be selected
	 * @param labelSelectable
	 *            true if Labels should be selected
	 * @param connectionSelectable
	 *            true if Connections should be selected
	 */
	public PapyrusRubberbandSelectionTool(int behavior, boolean nodeSelectable, boolean labelSelectable, boolean connectionSelectable) {
		this(behavior);

		this.setSelectNodes(nodeSelectable);
		this.setSelectLabels(labelSelectable);
		this.setSelectConnections(connectionSelectable);

	}

	/**
	 * Defines if Nodes must be part of the selection
	 *
	 * @return selectNodes
	 */
	public boolean isSelectNodes() {
		return selectNodes;
	}

	/**
	 * Set if the Nodes should be part of the selection.
	 *
	 * @param selectNodes
	 */
	public void setSelectNodes(boolean selectNodes) {
		this.selectNodes = selectNodes;
	}

	/**
	 * Defines if Labels must be part of the selection
	 *
	 * @return selectLabels
	 */
	public boolean isSelectLabels() {
		return selectLabels;
	}

	/**
	 * Set if the Labels should be part of the selection.
	 *
	 * @param selectLabels
	 */
	public void setSelectLabels(boolean selectLabels) {
		this.selectLabels = selectLabels;
	}



	/**
	 * Defines if Connection must be part of the selection
	 *
	 * @return selectNodes
	 */
	public boolean isSelectConnections() {
		return selectConnections;
	}

	/**
	 * Set if the Connections should be part of the selection.
	 *
	 * @param selectConnections
	 */
	public void setSelectConnections(boolean selectConnections) {
		this.selectConnections = selectConnections;
	}


	/**
	 * Calculate the selection of the Rubberband: <li>List all the eligible Edit parts</li> <li>Keep only if include in the Marquee rectangle</li> <li>
	 * Add in the selection if the EditPart type (connection, Label and node) is requested into the selection</li>
	 *
	 * @return List of EditParts which meet the conditions of the selection
	 */
	private List calculateNewSelection() {

		List<EditPart> newSelections = new ArrayList<EditPart>();
		Iterator<EditPart> children = getAllChildren().iterator();

		// Calculate new selections based on which children fall
		// inside the marquee selection rectangle.
		// Select Nodes, Labels and Connections
		while (children.hasNext()) {

			EditPart child = children.next();

			if (!child.isSelectable() || child.getTargetEditPart(MARQUEE_REQUEST) != child) {
				continue;
			}

			// if the child is part of the Marquee selection add it to the list of selected EditParts
			if (isPartOfMarqueeSelection(marqueeBehavior, child)) {

				// if the child is a node and the nodes must be selected then add the child to the selection
				if (isNode(child) && isSelectNodes()) {
					newSelections.add(child);
				} else if (isLabel(child) && isSelectLabels()) {
					newSelections.add(child);
				} else if (isConnection(child) && isSelectConnections()) {
					newSelections.add(child);
				} else {
					continue;
				}
			}
		}

		return newSelections;
	}

	/**
	 * Check if an edit Part is a Node
	 *
	 * @return
	 */
	private boolean isNode(EditPart editPart) {

		return (editPart instanceof ShapeEditPart);
	}


	/**
	 * Check if an edit Part is a Label
	 *
	 * @return
	 */
	private boolean isLabel(EditPart editPart) {

		return (editPart instanceof LabelEditPart);
	}


	/**
	 * Check if an edit Part is a Connection
	 *
	 * @return
	 */
	private boolean isConnection(EditPart editPart) {

		return (editPart instanceof ConnectionEditPart);
	}

	/**
	 * Calculate if the editPart should be part of the selection depending on the wished marquee behavior.
	 *
	 * @param marqueeBehavior
	 *            wished behavior for the selection (BEHAVIOR_OBJECT_INCLUDED, BEHAVIOR_OBJECT_TOUCHED, ...)
	 * @param editPart
	 *            EditPart for which the selection is calculated
	 *
	 * @return <b>true</b> if the editPart is part of the selection.
	 */
	private boolean isPartOfMarqueeSelection(int marqueeBehaviorType, EditPart editPart) {


		boolean isInside = false;
		boolean isTouched = false;
		boolean isPartOfSelection = false;

		IFigure figure = ((GraphicalEditPart) editPart).getFigure();
		Rectangle marqueeBounds = getMarqueeBounds();

		Rectangle figureRectangle;
		if (editPart instanceof ConnectionEditPart) {
			// RATLC00569348 For connection, get the bounds of connection points rather than connection figure since the
			// figure's bounds contain the bounds of all connection editParts and would require selection rectangle
			// to be larger than expected in some cases
			figureRectangle = ((Connection) figure).getPoints().getBounds().getCopy();
		} else {
			figureRectangle = figure.getBounds().getCopy();
		}
		figure.translateToAbsolute(figureRectangle);
		getMarqueeFeedbackFigure().translateToRelative(figureRectangle);

		// If the objects are included into the Marquee Rectangle
		isInside = (marqueeBounds.contains(figureRectangle.getTopLeft()) && marqueeBounds.contains(figureRectangle.getBottomRight()));

		// If the objects are intersected by the Marquee Rectangle
		isTouched = isTouchedByMarquee(marqueeBounds, figureRectangle);

		if (marqueeBehaviorType == BEHAVIOR_OBJECT_INCLUDED) {
			// Part of the selection only if inside
			isPartOfSelection = isInside;

		} else if (marqueeBehaviorType == BEHAVIOR_OBJECT_TOUCHED) {
			// Part of the selection if inside or touched by the marquee
			isPartOfSelection = isInside || isTouched;

		}
		return isPartOfSelection;

	}

	/**
	 * Define if the EditPart intersects the marquee rectangle.
	 *
	 * @param marqueeBounds
	 *            The Marquee rectangle
	 * @param figureBounds
	 *            The EditPart Figure Rectangle
	 * @return true if the intersection is not null
	 */
	private boolean isTouchedByMarquee(Rectangle marqueeBounds, Rectangle figureBounds) {
		// return true if the intersection is not null
		return figureBounds.intersects(marqueeBounds);

	}


	/**
	 * Default getter of the Marquee Behavior
	 *
	 * @return behavior: <li>BEHAVIOR_OBJECT_INCLUDE = 1</li> <li>BEHAVIOR_OBJECT_TOUCHED = 2</li>
	 */
	public int getMarqueeBehavior() {
		return marqueeBehavior;
	}

	/**
	 * Default setter
	 *
	 * @param marqueeBehavior
	 */
	public void setMarqueeBehavior(int marqueeBehavior) {
		this.marqueeBehavior = marqueeBehavior;
	}


	protected Request createTargetRequest() {
		return MARQUEE_REQUEST;
	}

	/**
	 * Erases feedback if necessary and puts the tool into the terminal state.
	 */
	@Override
	public void deactivate() {
		if (isInState(STATE_DRAG_IN_PROGRESS)) {
			eraseMarqueeFeedback();
			eraseTargetFeedback();
		}
		super.deactivate();
		allChildren = new HashSet<EditPart>();
		setState(STATE_TERMINAL);
	}

	private void eraseMarqueeFeedback() {
		if (marqueeRectangleFigure != null) {
			removeFeedback(marqueeRectangleFigure);
			marqueeRectangleFigure = null;
		}
		feedBackStartLocation = null;
	}

	protected void eraseTargetFeedback() {
		if (selectedEditParts == null) {
			return;
		}
		ListIterator<EditPart> oldEditParts = selectedEditParts.listIterator();
		while (oldEditParts.hasNext()) {
			EditPart editPart = oldEditParts.next();
			editPart.eraseTargetFeedback(getTargetRequest());
		}
	}



	/**
	 * Returns a list including all of the children
	 * of the edit part passed in.
	 */
	private HashSet<EditPart> getAllChildren(EditPart editPart, HashSet<EditPart> currentSelection) {

		// List all the editPart children
		List<EditPart> children = editPart.getChildren();

		// for each child, add the child and all its connections to the currentSelection
		for (int i = 0; i < children.size(); i++) {

			GraphicalEditPart child = (GraphicalEditPart) children.get(i);

			currentSelection.add(child);

			getAllChildren(child, currentSelection);

			List sourceConnections = child.getSourceConnections();
			List targetConnections = child.getTargetConnections();


			// Add all the connection and they children into the List of all the Objects to be selected
			// Connection children are all its labels
			for (int j = 0; j < sourceConnections.size(); j++) {

				// Add the connection
				currentSelection.add((GraphicalEditPart) sourceConnections.get(j));
				// Add the connection children (Labels)
				getAllChildren((EditPart) sourceConnections.get(j), currentSelection);
			}

			// Add all the connection and they children into the List of all the Objects to be selected
			for (int k = 0; k < targetConnections.size(); k++) {

				// Add the connection
				currentSelection.add((GraphicalEditPart) targetConnections.get(k));
				// Add the connection children (Labels)
				getAllChildren((EditPart) targetConnections.get(k), currentSelection);

			}

		}


		return currentSelection;
	}

	/**
	 * Return a vector including all of the children
	 * of the root editpart
	 */
	private Set<EditPart> getAllChildren() {
		if (allChildren.isEmpty()) {
			allChildren = getAllChildren(getCurrentViewer().getContents(), new HashSet<EditPart>());
		}
		return allChildren;
	}

	/**
	 * @see org.eclipse.gef.tools.AbstractTool#getCommandName()
	 */
	@Override
	protected String getCommandName() {
		return REQ_SELECTION;
	}

	/**
	 * @see org.eclipse.gef.tools.AbstractTool#getDebugName()
	 */
	@Override
	protected String getDebugName() {
		return "Marquee Tool";//$NON-NLS-1$
	}

	protected IFigure getMarqueeFeedbackFigure() {
		if (marqueeRectangleFigure == null) {
			marqueeRectangleFigure = new MarqueeRectangleFigure();
			addFeedback(marqueeRectangleFigure);
		}
		return marqueeRectangleFigure;
	}

	protected Rectangle getMarqueeSelectionRectangle() {
		return new Rectangle(getStartLocation(), getLocation());
	}

	/**
	 * Gets the relative bounds of the marquee feedback figure.
	 *
	 * @return
	 */
	private Rectangle getMarqueeBounds() {
		if (getMarqueeFeedbackFigure() == null) {
			return new Rectangle();
		}
		Rectangle rect = new Rectangle();
		if (feedBackStartLocation == null) {
			rect = getMarqueeSelectionRectangle();
			getMarqueeFeedbackFigure().translateToRelative(rect);
			feedBackStartLocation = rect.getLocation();
			return rect;
		} else {
			Point location = getLocation().getCopy();
			getMarqueeFeedbackFigure().translateToRelative(location);
			rect = new Rectangle(feedBackStartLocation, location);
			return rect;
		}

	}

	private int getSelectionMode() {
		return mode;
	}

	protected Request getTargetRequest() {
		if (targetRequest == null) {
			targetRequest = createTargetRequest();
		}
		return targetRequest;
	}

	/**
	 * @see org.eclipse.gef.tools.AbstractTool#handleButtonDown(int)
	 */
	@Override
	protected boolean handleButtonDown(int button) {
		if (!isGraphicalViewer()) {
			return true;
		}
		if (button != 1) {
			setState(STATE_INVALID);
			handleInvalidInput();
		}
		if (stateTransition(STATE_INITIAL, STATE_DRAG_IN_PROGRESS)) {
			if (getCurrentInput().isControlKeyDown()) {
				setSelectionMode(TOGGLE_MODE);
			} else if (getCurrentInput().isShiftKeyDown()) {
				setSelectionMode(APPEND_MODE);
			}

			// RATLC00740277:
			// clear current focus (if any) before we start computing selections,
			// because we don't want to select any compartments in the focus
			// edit part if they shouldn't be selectable
			clearFocus();
		}
		return true;
	}

	/**
	 * Effectively clears the current focus edit part by deliberately setting the
	 * diagram contents edit part as the focus. This ensures that the rubber band
	 * won't mistakenly select the selectable compartments and items in the current
	 * focus edit part.
	 */
	private void clearFocus() {
		EditPart focusPart = getCurrentViewer().getFocusEditPart();

		if (focusPart != null) {
			// replace the current focus with the contents edit part, which effectively
			// blocks unwanted selectability of compartments within the previous
			// focus edit part
			getCurrentViewer().setFocus(getCurrentViewer().getContents());
		}
	}

	/**
	 * Extends the inherited method by first restoring the current viewer's focus
	 * edit part to the default (which is the last selected edit part). This undoes
	 * the work-around that sets the diagram root as the focus.
	 *
	 * @see #clearFocus()
	 */
	@Override
	protected void handleFinished() {
		getCurrentViewer().setFocus(null);

		super.handleFinished();
	}


	/**
	 * @see org.eclipse.gef.tools.AbstractTool#handleButtonUp(int)
	 */
	@Override
	protected boolean handleButtonUp(int button) {
		if (stateTransition(STATE_DRAG_IN_PROGRESS, STATE_TERMINAL)) {
			eraseTargetFeedback();
			performMarqueeSelect();
			eraseMarqueeFeedback();
		}
		handleFinished();
		return true;
	}

	/**
	 * @see org.eclipse.gef.tools.AbstractTool#handleDragInProgress()
	 */
	@Override
	protected boolean handleDragInProgress() {
		if (isInState(STATE_DRAG | STATE_DRAG_IN_PROGRESS)) {

			showMarqueeFeedback();
			eraseTargetFeedback();
			selectedEditParts = calculateNewSelection();
			showTargetFeedback();
			SelectInDiagramHelper.exposeLocation((FigureCanvas) getCurrentViewer().getControl(), getLocation());

		}
		return true;
	}

	/**
	 * @see org.eclipse.gef.tools.AbstractTool#handleFocusLost()
	 */
	@Override
	protected boolean handleFocusLost() {
		if (isInState(STATE_DRAG | STATE_DRAG_IN_PROGRESS)) {
			handleFinished();
			return true;
		}
		return false;
	}

	/**
	 * This method is called when mouse or keyboard input is invalid and erases the feedback.
	 *
	 * @return <code>true</code>
	 */
	@Override
	protected boolean handleInvalidInput() {
		eraseTargetFeedback();
		eraseMarqueeFeedback();
		return true;
	}

	/**
	 * Handles high-level processing of a key down event.
	 * KeyEvents are forwarded to the current viewer's {@link KeyHandler},
	 * via {@link KeyHandler#keyPressed(KeyEvent)}.
	 *
	 * @see AbstractTool#handleKeyDown(KeyEvent)
	 */
	@Override
	protected boolean handleKeyDown(KeyEvent e) {
		if (super.handleKeyDown(e)) {
			return true;
		}
		if (getCurrentViewer().getKeyHandler() != null && getCurrentViewer().getKeyHandler().keyPressed(e)) {
			return true;
		}
		return false;
	}

	private boolean isGraphicalViewer() {
		return getCurrentViewer() instanceof GraphicalViewer;
	}

	protected void performMarqueeSelect() {
		EditPartViewer viewer = getCurrentViewer();

		List newSelections = calculateNewSelection();

		// If in multi select mode, add the new selections to the already
		// selected group; otherwise, clear the selection and select the new group
		if (getSelectionMode() == APPEND_MODE) {
			for (int i = 0; i < newSelections.size(); i++) {
				EditPart editPart = (EditPart) newSelections.get(i);
				viewer.appendSelection(editPart);
			}
		} else if (getSelectionMode() == TOGGLE_MODE) {
			List selected = new ArrayList(viewer.getSelectedEditParts());
			for (int i = 0; i < newSelections.size(); i++) {
				EditPart editPart = (EditPart) newSelections.get(i);
				if (editPart.getSelected() != EditPart.SELECTED_NONE) {
					selected.remove(editPart);
				} else {
					selected.add(editPart);
				}
			}
			viewer.setSelection(new StructuredSelection(selected));
		} else {
			viewer.setSelection(new StructuredSelection(newSelections));
		}
	}

	/**
	 * @see org.eclipse.gef.Tool#setViewer(org.eclipse.gef.EditPartViewer)
	 */
	@Override
	public void setViewer(EditPartViewer viewer) {
		if (viewer == getCurrentViewer()) {
			return;
		}
		super.setViewer(viewer);
		// If cursor is over a Graphical editor it will be a cross to indicate that the selectionTool is a Marquee tool
		if (viewer instanceof GraphicalViewer) {
			setDefaultCursor(Cursors.CROSS);
		} else {
			setDefaultCursor(Cursors.NO);
		}
		if (viewer != null) {
			weakReference = new WeakReference(viewer);
		}
	}

	private void setSelectionMode(int mode) {
		this.mode = mode;
	}

	private void showMarqueeFeedback() {
		getMarqueeFeedbackFigure().setBounds(getMarqueeBounds());
	}

	protected void showTargetFeedback() {
		for (int i = 0; i < selectedEditParts.size(); i++) {
			EditPart editPart = selectedEditParts.get(i);
			editPart.showTargetFeedback(getTargetRequest());
		}
	}

	/**
	 * Convenience method to removes a figure from the feedback layer.
	 *
	 * @param figure
	 *            the figure being removed
	 */
	@Override
	protected void removeFeedback(IFigure figure) {
		EditPartViewer viewer = getCurrentViewer();
		if ((viewer == null) && (weakReference != null)) {
			viewer = (EditPartViewer) weakReference.get();
		}
		if (viewer != null) {
			LayerManager lm = (LayerManager) viewer.getEditPartRegistry().get(LayerManager.ID);
			if (lm == null) {
				return;
			}
			lm.getLayer(LayerConstants.FEEDBACK_LAYER).remove(figure);
		}
	}

	// create Marquee figure
	class MarqueeRectangleFigure extends Figure {

		private int offset = 0;

		private boolean schedulePaint = true;

		private static final int DELAY = 110; // animation delay in millisecond

		/**
		 * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
		 */
		@Override
		protected void paintFigure(Graphics graphics) {
			Rectangle graphicBounds = getBounds().getCopy();
			graphics.translate(getLocation());

			graphics.setXORMode(true);
			graphics.setForegroundColor(ColorConstants.white);
			graphics.setBackgroundColor(ColorConstants.black);

			graphics.setLineStyle(Graphics.LINE_DOT);

			int[] points = new int[6];

			points[0] = 0 + offset;
			points[1] = 0;
			points[2] = graphicBounds.width - 1;
			points[3] = 0;
			points[4] = graphicBounds.width - 1;
			points[5] = graphicBounds.height - 1;

			graphics.drawPolyline(points);

			points[0] = 0;
			points[1] = 0 + offset;
			points[2] = 0;
			points[3] = graphicBounds.height - 1;
			points[4] = graphicBounds.width - 1;
			points[5] = graphicBounds.height - 1;

			graphics.drawPolyline(points);

			graphics.translate(getLocation().getNegated());

			if (schedulePaint) {
				Display.getCurrent().timerExec(DELAY, new Runnable() {

					@Override
					public void run() {
						offset++;
						if (offset > 5) {
							offset = 0;
						}
						schedulePaint = true;
						repaint();
					}
				});
			}

			schedulePaint = false;
		}

	}

}
