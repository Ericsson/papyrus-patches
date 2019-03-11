/*****************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   IBM Corporation - Initial API and implementation
 *   Christian W. Damus - Adapted to the Papyrus environment (too many private APIs to simply extend/override)
 *   Christian W. Damus - bug 451230
 *
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.Tool;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.tools.SelectionTool;
import org.eclipse.gmf.runtime.common.ui.services.icon.IconService;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.DiagramAssistantEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.PopupBarEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramWorkbenchPart;
import org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants;
import org.eclipse.gmf.runtime.diagram.ui.tools.AbstractPopupBarTool;
import org.eclipse.gmf.runtime.diagram.ui.tools.PopupBarTool;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.ui.services.modelingassistant.ModelingAssistantService;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Papyrus adaptation of the GMF popup bar diagram assistant.
 *
 * @author affrantz@us.ibm.com
 * @author cmahoney
 */
public class PapyrusPopupBarEditPolicy extends DiagramAssistantEditPolicy {

	/** Y postion offset from shape where the balloon top begin. */
	static private int BALLOON_Y_OFFSET = 10;

	/** Y postion offset from shape where the balloon top begin. */
	static private double BALLOON_X_OFFSET_RHS = 0.65;

	/** Y postion offset from shape where the balloon top begin. */
	static private int ACTION_WIDTH_HGT = 30;

	static protected int ACTION_BUTTON_START_X = 5;

	static protected int ACTION_BUTTON_START_Y = 5;

	static protected int ACTION_MARGIN_RIGHT = 10;

	/** popup bar bits */
	static private int POPUPBAR_ACTIVATEONHOVER = 0x01; /* Display the action when hovering */
	static private int POPUPBAR_MOVE_FIGURE = 0x02; /* Ignore the first figureMoved event when creating elements inside a shape via a popup bar */
	static private int POPUPBAR_INSTALLEDONSURFACE = 0x04; /* Display the popup bar at the mouse location used by diagrams and machine edit parts */
	static private int POPUPBAR_ONDIAGRAMACTIVATED = 0x10; /* For popup bars on diagram and machine edit parts, where we POPUPBAR_DISPLAYATMOUSEHOVERLOCATION, don't display popup bar until user clicks on surface */
	static private int POPUPBAR_HOST_IS_CONNECTION = 0x20; /* For popup bars on connection edit parts */

	/** Bit field for the actrionbar associated bits */
	private int myPopupBarFlags = POPUPBAR_ACTIVATEONHOVER;

	private double myBallonOffsetPercent = BALLOON_X_OFFSET_RHS;

	/** the figure used to surround the action buttons */
	protected IFigure myBalloon = null;

	/** The popup bar descriptors for the popup bar buttons */
	private List<PopupBarDescriptor> myPopupBarDescriptors = new ArrayList<PopupBarDescriptor>();

	/** Images created that must be deleted when popup bar is removed */
	protected List<Image> imagesToBeDisposed = null;

	/** mouse keys listener for the owner shape */
	protected PopupBarMouseListener myMouseKeyListener = new PopupBarMouseListener();

	/** listener for owner shape movement */
	private OwnerMovedListener myOwnerMovedListener = new OwnerMovedListener();

	private Point activationLocation;

	/** flag for whether mouse cursor within shape */

	private void setFlag(int bit, boolean b) {
		if (b) {
			myPopupBarFlags |= bit;
		} else if (getFlag(bit)) {
			myPopupBarFlags ^= bit;
		}

	}

	private boolean getFlag(int bit) {
		return ((myPopupBarFlags & bit) > 0);
	}



	private void setPopupBarOnDiagramActivated(boolean bVal) {
		setFlag(POPUPBAR_ONDIAGRAMACTIVATED, bVal);
	}

	private boolean getPopupBarOnDiagramActivated() {
		return getFlag(POPUPBAR_ONDIAGRAMACTIVATED);
	}

	void setActivationLocation(Point location) {
		this.activationLocation = location;
	}

	Point getActivationLocation() {
		return activationLocation;
	}

	/**
	 * set the host is connection flag
	 *
	 * @param bVal
	 *            the new value
	 */
	protected void setHostConnection(boolean bVal) {
		setFlag(POPUPBAR_HOST_IS_CONNECTION, bVal);
	}

	/**
	 * get the host is connection flag
	 *
	 * @return true or false
	 */
	protected boolean isHostConnection() {
		return getFlag(POPUPBAR_HOST_IS_CONNECTION);
	}

	/**
	 * Populates the popup bar with popup bar descriptors added by suclassing
	 * this editpolicy (i.e. <code>fillPopupBarDescriptors</code> and by
	 * querying the modeling assistant service for all types supported on the
	 * popup bar of this host. For those types added by the modeling assistant
	 * service the icons are retrieved using the Icon Service.
	 */
	protected void populatePopupBars() {
		prependPopupBarDescriptors();

		@SuppressWarnings("unchecked")
		List<IElementType> types = ModelingAssistantService.getInstance().getTypesForPopupBar(getHost());
		for (Iterator<IElementType> iter = types.iterator(); iter.hasNext();) {
			IElementType type = iter.next();
			Image icon = IconService.getInstance().getIcon(type);
			if (icon == null) {
				IElementType[] supertypes = type.getAllSuperTypes();
				for (int i = supertypes.length - 1; i >= 0; i--) {
					icon = IconService.getInstance().getIcon(supertypes[i]);
					if (icon != null) {
						break;
					}
				}
			}
			addPopupBarDescriptor(type, icon);
		}

		appendPopupBarDescriptors();
	}

	/**
	 * Overridden by subclasses if necessary to add popup bar descriptors before of those provided by the
	 * Modeling Assistant Service.
	 */
	protected void prependPopupBarDescriptors() {
		// Pass
	}

	/**
	 * Overridden by subclasses if necessary to add popup bar descriptors after of those provided by the
	 * Modeling Assistant Service.
	 */
	protected void appendPopupBarDescriptors() {
		// Pass
	}

	private boolean isSelectionToolActive() {
		// getViewer calls getParent so check for null
		if (getHost().getParent() != null && getHost().isActive()) {
			Tool theTool = getHost().getViewer().getEditDomain().getActiveTool();
			if ((theTool != null) && theTool instanceof SelectionTool) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Replaces DiagramAssistantEditPolicy::shouldShowDiagramAssistant() method that depends on private methods of that class.
	 */
	protected boolean basicShouldShowDiagramAssistant() {
		return getHost().isActive() && isPreferenceOn() && isHostEditable()
				&& isHostResolvable() && isDiagramPartActive();
	}

	@Override
	protected boolean shouldShowDiagramAssistant() {
		if (!basicShouldShowDiagramAssistant()) {
			return false;
		}

		if (this.getIsInstalledOnSurface()) {
			if (isHostConnection()) {
				return isSelectionToolActive();
			} else if (getPopupBarOnDiagramActivated()) {
				return isSelectionToolActive();
			} else {
				return false;
			}
		} else {
			return isSelectionToolActive();
		}
	}

	/*
	 * Replaces DiagramAssistantEditPolicy::isHostEditable() method that is private.
	 */
	protected boolean isHostEditable() {
		if (getHost() instanceof GraphicalEditPart) {
			return ((GraphicalEditPart) getHost()).isEditModeEnabled();
		}
		return true;
	}

	/*
	 * Replaces DiagramAssistantEditPolicy::isHostResolvable() method that is private.
	 */
	protected boolean isHostResolvable() {
		final View view = (View) getHost().getModel();
		EObject element = view.getElement();
		if (element != null) {
			return !element.eIsProxy();
		}
		return true;
	}


	/*
	 * Replaces DiagramAssistantEditPolicy::isDiagramPartActive() method that is private and
	 * does not account for the fact that Papyrus nests its diagrams in a multi-editor.
	 */
	protected boolean isDiagramPartActive() {
		boolean result = false;

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IWorkbenchPart activePart = page.getActivePart();
				IDiagramWorkbenchPart editorPart = null;

				if (activePart instanceof IDiagramWorkbenchPart) {
					editorPart = (IDiagramWorkbenchPart) activePart;
				} else if (activePart instanceof IAdaptable) {
					editorPart = ((IAdaptable) activePart).getAdapter(IDiagramWorkbenchPart.class);
				}

				if (editorPart != null) {
					result = editorPart.getDiagramEditPart().getRoot().equals(((IGraphicalEditPart) getHost()).getRoot());
				}
			}
		}

		return result;
	}

	/**
	 * allows plugins to add their own popup bar tools and tips
	 *
	 * @param elementType
	 * @param theImage
	 * @param theTracker
	 * @param theTip
	 */
	protected void addPopupBarDescriptor(
			IElementType elementType,
			Image theImage,
			DragTracker theTracker,
			String theTip) {

		if (!(theTracker instanceof AbstractPopupBarTool) || ((AbstractPopupBarTool) theTracker).isCommandEnabled()) {
			// We only add pop-up bar tools whose commands are actually executable in this context
			PopupBarDescriptor desc = new PopupBarDescriptor(theTip, theImage, elementType, theTracker);
			myPopupBarDescriptors.add(desc);
		}
	}

	/**
	 * adds popup bar descriptor
	 *
	 * @param elementType
	 * @param theImage
	 * @param theTracker
	 */
	protected void addPopupBarDescriptor(
			IElementType elementType,
			Image theImage,
			DragTracker theTracker) {

		String theInputStr = DiagramUIMessages.PopupBar_AddNew;
		String theTip = NLS.bind(theInputStr, elementType.getDisplayName());

		this.addPopupBarDescriptor(elementType, theImage, theTracker, theTip);
	}

	/**
	 * default method for plugins which passes along the PopupBarTool
	 * as the tool to be used.
	 *
	 * @param elementType
	 * @param theImage
	 */
	protected void addPopupBarDescriptor(IElementType elementType,
			Image theImage) {

		this.addPopupBarDescriptor(elementType, theImage, createPopupBarTool(elementType));
	}

	protected AbstractPopupBarTool createPopupBarTool(IElementType elementType) {
		return new DefaultTool(getHost(), elementType);
	}

	/**
	 * @param elementType
	 * @param theImage
	 * @param theTip
	 */
	protected void addPopupBarDescriptor(
			IElementType elementType,
			Image theImage,
			String theTip) {

		AbstractPopupBarTool theTracker = createPopupBarTool(elementType);
		this.addPopupBarDescriptor(elementType, theImage, theTracker, theTip);
	}

	/**
	 * method used primarily to add UnspecifiedTypeCreationTool
	 *
	 * @param elementType
	 * @param theImage
	 * @param theRequest
	 *            the create request to be used
	 */
	protected void addPopupBarDescriptor(
			IElementType elementType,
			Image theImage,
			CreateRequest theRequest) {

		AbstractPopupBarTool theTracker = createPopupBarTool(theRequest);
		this.addPopupBarDescriptor(elementType, theImage, theTracker);
	}

	protected AbstractPopupBarTool createPopupBarTool(CreateRequest request) {
		return new DefaultTool(getHost(), request);
	}

	/**
	 * Add a drag-tracker/tool that doesn't create an element type.
	 *
	 * @param image
	 *            the image to show in the popup bar
	 * @param tracker
	 *            the drag-tracker or tool to invoke
	 * @param tooltip
	 *            the tool-tip to show on hover
	 */
	protected void addPopupBarDescriptor(Image image, DragTracker tracker, String tooltip) {
		this.addPopupBarDescriptor(null, image, tracker, tooltip);
	}

	/**
	 * gets the popup bar descriptors
	 *
	 * @return list
	 */
	protected List<PopupBarDescriptor> getPopupBarDescriptors() {
		return myPopupBarDescriptors;
	}

	/**
	 * initialize the popup bars from the list of action descriptors.
	 */
	protected void initPopupBars() {

		List<PopupBarDescriptor> theList = getPopupBarDescriptors();
		if (theList.isEmpty()) {
			return;
		}
		myBalloon = createPopupBarFigure();

		int iTotal = ACTION_WIDTH_HGT * theList.size() + ACTION_MARGIN_RIGHT;

		getBalloon().setSize(
				iTotal,
				ACTION_WIDTH_HGT + 2 * ACTION_BUTTON_START_Y);

		int xLoc = ACTION_BUTTON_START_X;
		int yLoc = ACTION_BUTTON_START_Y;

		for (PopupBarDescriptor theDesc : theList) {
			// Button b = new Button(theDesc.myButtonIcon);
			PopupBarLabelHandle b = new PopupBarLabelHandle(
					theDesc.getDragTracker(),
					theDesc.getIcon());

			Rectangle r1 = new Rectangle();
			r1.setLocation(xLoc, yLoc);
			xLoc += ACTION_WIDTH_HGT;
			r1.setSize(
					ACTION_WIDTH_HGT,
					ACTION_WIDTH_HGT - ACTION_MARGIN_RIGHT);

			Label l = new Label();
			l.setText(theDesc.getToolTip());

			b.setToolTip(l);
			b.setPreferredSize(ACTION_WIDTH_HGT, ACTION_WIDTH_HGT);
			b.setBounds(r1);

			getBalloon().add(b);

			b.addMouseMotionListener(this);
			b.addMouseListener(this.myMouseKeyListener);

		}
	}

	/**
	 * Overrides the superclass method because the package-private method that it uses to get the preference name cannot be overridden in this package.
	 */
	@Override
	protected boolean isPreferenceOn() {
		IPreferenceStore preferenceStore = (IPreferenceStore) ((IGraphicalEditPart) getHost()).getDiagramPreferencesHint().getPreferenceStore();
		return preferenceStore.getBoolean(IPreferenceConstants.PREF_SHOW_POPUP_BARS);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.DiagramAssistantEditPolicy#isDiagramAssistantShowing()
	 */
	@Override
	protected boolean isDiagramAssistantShowing() {
		return getBalloon() != null;
	}

	protected IFigure getBalloon() {
		return myBalloon;
	}

	protected IFigure createPopupBarFigure() {
		return new PopupBarFigure();
	}

	@Override
	protected void showDiagramAssistant(Point referencePoint) {

		// already have one?
		if ((getBalloon() != null) && (getBalloon().getParent() != null)) {
			return;
		}

		if (this.myPopupBarDescriptors.isEmpty()) {
			populatePopupBars();
			initPopupBars();

			if (myPopupBarDescriptors.isEmpty()) {
				return; // nothing to show
			}
		}

		getBalloon().addMouseMotionListener(this);
		getBalloon().addMouseListener(myMouseKeyListener);

		// the feedback layer figures do not recieve mouse events so do not use
		// it for popup bars
		IFigure layer = getLayer(LayerConstants.HANDLE_LAYER);
		layer.add(getBalloon());

		if (referencePoint == null) {
			referencePoint = getHostFigure().getBounds().getCenter();
		}

		Point thePoint = getBalloonPosition(referencePoint);

		getBalloon().setLocation(thePoint);

		// dismiss the popup bar after a delay
		if (!shouldAvoidHidingDiagramAssistant()) {
			hideDiagramAssistantAfterDelay(getDisappearanceDelay());
		}

	}

	/**
	 * getter for the IsInstalledOnSurface flag
	 *
	 * @return true or false
	 */
	protected boolean getIsInstalledOnSurface() {
		return getFlag(POPUPBAR_INSTALLEDONSURFACE);
	}

	/**
	 * setter for the IsInstalledOnSurface flag
	 *
	 * @param bVal
	 */
	protected void setIsInstalledOnSurface(boolean bVal) {
		setFlag(POPUPBAR_INSTALLEDONSURFACE, bVal);
	}

	/**
	 * For editparts that consume the entire viewport, statechart, structure,
	 * communication, we want to display the popup bar at the mouse location.
	 *
	 * @param referencePoint
	 *            The reference point which may be used to determine where the
	 *            diagram assistant should be located. This is most likely the
	 *            current mouse location.
	 * @return Point
	 */
	private Point getBalloonPosition(Point referencePoint) {
		Point thePoint = new Point();
		boolean atMouse = getIsInstalledOnSurface();
		if (atMouse) {
			thePoint.setLocation(referencePoint);
			getHostFigure().translateToAbsolute(thePoint);
			getBalloon().translateToRelative(thePoint);

			// shift the ballon so it is above the cursor.
			thePoint.y -= ACTION_WIDTH_HGT;
			adjustToFitInViewport(thePoint);
		} else {
			Dimension theoffset = new Dimension();
			Rectangle rcBounds = getHostFigure().getBounds().getCopy();

			getHostFigure().translateToAbsolute(rcBounds);
			getBalloon().translateToRelative(rcBounds);

			theoffset.height = -(BALLOON_Y_OFFSET + ACTION_WIDTH_HGT);
			theoffset.width = (int) (rcBounds.width * myBallonOffsetPercent);

			thePoint.x = rcBounds.x + theoffset.width;
			thePoint.y = rcBounds.y + theoffset.height;
			adjustToFitInViewport(thePoint);
		}
		return thePoint;
	}

	/**
	 * Uses the balloon location passed in and its size to determine if the
	 * balloon will appear outside the viewport. If so, the balloon location
	 * will be modified accordingly.
	 *
	 * @param balloonLocation
	 *            the suggested balloon location passed in and potentially
	 *            modified when this method completes
	 */
	private void adjustToFitInViewport(Point balloonLocation) {
		Control control = getHost().getViewer().getControl();
		if (control instanceof FigureCanvas) {
			Rectangle viewportRect = ((FigureCanvas) control).getViewport()
					.getClientArea();
			Rectangle balloonRect = new Rectangle(balloonLocation, getBalloon()
					.getSize());

			int yDiff = viewportRect.y - balloonRect.y;
			if (yDiff > 0) {
				// balloon is above the viewport, shift down
				balloonLocation.translate(0, yDiff);
			}
			int xDiff = balloonRect.right() - viewportRect.right();
			if (xDiff > 0) {
				// balloon is to the right of the viewport, shift left
				balloonLocation.translate(-xDiff, 0);
			}
		}
	}

	private void teardownPopupBar() {
		getBalloon().removeMouseMotionListener(this);
		getBalloon().removeMouseListener(myMouseKeyListener);
		// the feedback layer figures do not recieve mouse events
		IFigure layer = getLayer(LayerConstants.HANDLE_LAYER);
		if (myBalloon.getParent() != null) {
			layer.remove(myBalloon);
		}
		myBalloon = null;

		this.myPopupBarDescriptors.clear();

		if (imagesToBeDisposed != null) {
			for (Image next : imagesToBeDisposed) {
				next.dispose();
			}
			imagesToBeDisposed.clear();
		}

	}

	@Override
	protected void hideDiagramAssistant() {
		if (getBalloon() != null) {

			teardownPopupBar();
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.DiagramAssistantEditPolicy#showDiagramAssistantAfterDelay(int)
	 */
	@Override
	protected void showDiagramAssistantAfterDelay(int theDelay) {
		// only show the popup bar if it isn't already showing
		if (!isDiagramAssistantShowing()) {
			super.showDiagramAssistantAfterDelay(theDelay);
		}
	}

	@SuppressWarnings("restriction")
	@Override
	public void activate() {
		super.activate();

		getHostFigure().addMouseListener(this.myMouseKeyListener);
		getHostFigure().addFigureListener(this.myOwnerMovedListener);

		if (getHost() instanceof org.eclipse.gmf.runtime.diagram.ui.internal.editparts.ISurfaceEditPart) {
			setIsInstalledOnSurface(true);
		}
	}

	@Override
	public void deactivate() {
		getHostFigure().removeMouseListener(this.myMouseKeyListener);
		getHostFigure().removeFigureListener(this.myOwnerMovedListener);

		super.deactivate();

	}

	/**
	 * Gets the amount of time to wait before showing the popup bar if the
	 * popup bar is to be shown at the mouse location {@link #getIsDisplayAtMouseHoverLocation()}.
	 *
	 * @return the time to wait in milliseconds
	 */
	protected int getAppearanceDelayLocationSpecific() {
		return getAppearanceDelay();
	}

	@Override
	protected int getAppearanceDelay() {
		// 420201: [All diagrams - Hyperlinks] The Hyperlinks button (green +) pops up too early
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=420201
		return 1000; // milliseconds (superclass value is 200)
	}

	@Override
	protected boolean isDiagramAssistant(Object object) {
		return (object instanceof PopupBarFigure) || (object instanceof PopupBarLabelHandle);
	}

	@Override
	protected String getDiagramAssistantID() {
		return PopupBarEditPolicy.class.getName();
	}

	//
	// Mouselistener protocol
	//

	/**
	 * Adds the popup bar after a delay
	 */
	@Override
	public void mouseHover(MouseEvent me) {
		// if the cursor is inside the popup bar
		// or the keyboar triggred activation
		// then we do not want to deactivate
		if (!isDiagramAssistant(me.getSource())) {
			setAvoidHidingDiagramAssistant(false);
		}

		setMouseLocation(me.getLocation());
		if (getIsInstalledOnSurface()) {
			showDiagramAssistantAfterDelay(getAppearanceDelayLocationSpecific()); // no delay
		} else if (shouldShowDiagramAssistant()) {
			showDiagramAssistant(getMouseLocation()); // no delay
		}
	}

	/**
	 * @see org.eclipse.draw2d.MouseMotionListener#mouseMoved(org.eclipse.draw2d.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent me) {

		if (getIsInstalledOnSurface()) {
			Object srcObj = me.getSource();
			if ((srcObj != null) && srcObj.equals(getHostFigure())) {
				hideDiagramAssistant();
			}
		}
		setAvoidHidingDiagramAssistant(true);
		setMouseLocation(me.getLocation());

		if (!getIsInstalledOnSurface()) {
			// if the cursor is inside the popup bar
			// or the keyboar triggred activation
			// then we do not want to deactivate
			if (!isDiagramAssistant(me.getSource())) {
				setAvoidHidingDiagramAssistant(false);
			}

			showDiagramAssistantAfterDelay(getAppearanceDelay());
		}
	}

	//
	// Nested types
	//

	/**
	 *
	 * Class to hold pertinent information about the tool placed on the popup bar
	 *
	 * @author affrantz@us.ibm.com
	 */
	protected class PopupBarDescriptor {

		/** The action button tooltip */
		private String _tooltip = new String();

		/** The image for the button */
		private Image _icon = null;

		/** The typeinfo used to create the Request for the command */
		@SuppressWarnings("unused")
		private IElementType _elementType;

		/** The DragTracker / Tool associatd with the popup bar button */
		private DragTracker _dragTracker = null;

		/**
		 * constructor
		 *
		 * @param s
		 * @param i
		 * @param elementType
		 * @param theTracker
		 */
		public PopupBarDescriptor(
				String s,
				Image i,
				IElementType elementType,
				DragTracker theTracker) {
			_tooltip = s;
			_icon = i;
			_dragTracker = theTracker;
			_elementType = elementType;

		}

		/**
		 * gets the icon associated with this Descriptor
		 *
		 * @return Image
		 */
		public final Image getIcon() {
			return _icon;
		}

		/**
		 * gets the drag tracker associated with this Descriptor
		 *
		 * @return drag tracker
		 */
		public final DragTracker getDragTracker() {
			return _dragTracker;
		}

		/**
		 * gets the tool tip associated with this Descriptor
		 *
		 * @return string
		 */
		public final String getToolTip() {
			return _tooltip;
		}

	} // end PopupBarDescriptor

	/**
	 * Default tool placed on the popup bar
	 *
	 * @author affrantz@us.ibm.com
	 */
	protected class PopupBarLabelHandle extends Label implements Handle {
		/**
		 * flag to drawFocus rect around the handle when the mouse rolls over
		 * it
		 */
		protected boolean myMouseOver = false;

		private Image myDisabledImage = null;

		/** The dragTracker CreationTool associated with the handle * */
		private DragTracker myDragTracker = null;

		private Image getDisabledImage() {
			if (myDisabledImage != null) {
				return myDisabledImage;
			}

			Image theImage = this.getIcon();
			if (theImage == null) {
				return null;
			}

			myDisabledImage = new Image(Display.getCurrent(), theImage, SWT.IMAGE_DISABLE);
			if (imagesToBeDisposed == null) {
				imagesToBeDisposed = new ArrayList<Image>();
			}
			imagesToBeDisposed.add(myDisabledImage);
			return myDisabledImage;
		}

		/**
		 * cnostructor
		 *
		 * @param tracker
		 * @param theImage
		 */
		public PopupBarLabelHandle(DragTracker tracker, Image theImage) {
			super(theImage);
			myDragTracker = tracker;
			this.setOpaque(true);
			this.setBackgroundColor(ColorConstants.buttonLightest);
			calculateEnabled();
		}

		/**
		 * @see org.eclipse.gef.Handle#getAccessibleLocation()
		 */
		@Override
		public Point getAccessibleLocation() {
			return null;
		}

		/**
		 * @see org.eclipse.gef.Handle#getDragTracker()
		 */
		@Override
		public DragTracker getDragTracker() {
			return myDragTracker;
		}

		/**
		 * @see org.eclipse.draw2d.Figure#paintBorder(org.eclipse.draw2d.Graphics)
		 *      paint a focus rectangle for the label if the mouse is inside
		 *      the label
		 */
		@Override
		protected void paintBorder(Graphics graphics) {
			super.paintBorder(graphics);

			if (myMouseOver) {

				Rectangle area = getClientArea();
				graphics.setForegroundColor(ColorConstants.black);
				graphics.setBackgroundColor(ColorConstants.white);

				graphics.drawFocus(
						area.x,
						area.y,
						area.width - 1,
						area.height - 1);

			}

		}

		/**
		 * @see org.eclipse.draw2d.IFigure#handleMouseEntered(org.eclipse.draw2d.MouseEvent)
		 *      flip myMouseOver bit and repaint
		 */
		@Override
		public void handleMouseEntered(MouseEvent event) {

			calculateEnabled();

			super.handleMouseEntered(event);
			myMouseOver = true;
			repaint();
		}

		/**
		 * @see org.eclipse.draw2d.IFigure#handleMouseExited(org.eclipse.draw2d.MouseEvent)
		 *      flip myMouseOver bit and repaint
		 */
		@Override
		public void handleMouseExited(MouseEvent event) {

			super.handleMouseExited(event);
			myMouseOver = false;
			repaint();
		}

		/**
		 * @see org.eclipse.draw2d.IFigure#handleMousePressed(org.eclipse.draw2d.MouseEvent)
		 *      set PopupBarEditPolicy.myActionMoveFigure bit so the popup bar
		 *      is not dismissed after creating an item in the editpart
		 *
		 */
		@Override
		public void handleMousePressed(MouseEvent event) {

			if (1 == event.button) {
				// this is the flag in PopupBarEditPolicy that
				// prevents the popup bar from dismissing after a new item
				// is added to a shape, which causes the editpart to be
				// resized.
				this.setFlag(POPUPBAR_MOVE_FIGURE, true);
				// future: when other tools besides PopupBarTool are
				// used
				// we will need a way in which to call

			}

			super.handleMousePressed(event);
		}

		protected void calculateEnabled() {
			if ((myDragTracker != null) && (myDragTracker instanceof AbstractPopupBarTool)) {
				AbstractPopupBarTool abarTool = (AbstractPopupBarTool) myDragTracker;
				setEnabled(abarTool.isCommandEnabled());
			} else {
				setEnabled(true);
			}
		}

		/**
		 * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
		 */
		@Override
		protected void paintFigure(Graphics graphics) {
			if (!isEnabled()) {
				Image theImage = getDisabledImage();
				if (theImage != null) {
					graphics.translate(bounds.x, bounds.y);
					graphics.drawImage(theImage, getIconLocation());
					graphics.translate(-bounds.x, -bounds.y);
					return;
				}

			}
			super.paintFigure(graphics);

		}
	}

	/**
	 * The rounded-rectangle figure for a Popup Bar. Unlike the superclass, we do not draw a speech-balloon "tail".
	 * Also, we draw an orange border.
	 */
	private class PopupBarFigure extends RoundedRectangle {

		public PopupBarFigure() {
			this.setFill(true);
			this.setBackgroundColor(ColorConstants.white);
			this.setForegroundColor(ColorConstants.orange);
			this.setVisible(true);
			this.setEnabled(true);
			this.setOpaque(true);
		}

	}

	/**
	 * Listens to the owner figure being moved so the handles can be removed
	 * when this occurs.
	 *
	 * @author affrantz@us.ibm.com
	 *
	 */
	private class OwnerMovedListener implements FigureListener {

		private Point myPopupBarLastPosition = new Point(0, 0);

		boolean hasPositionChanged(Rectangle theBounds) {
			if (theBounds.x != myPopupBarLastPosition.x) {
				return true;
			}
			if (theBounds.y != myPopupBarLastPosition.y) {
				return true;
			}
			return false;
		}

		/**
		 * @see org.eclipse.draw2d.FigureListener#figureMoved(org.eclipse.draw2d.IFigure)
		 */
		@Override
		public void figureMoved(IFigure source) {
			// for some reason we get more than one
			// figure moved call after compartment items are added
			// myActionMoveFigure handles the first one which we expect
			// hasPositionChanged handles the others caused by the selection of
			// the compartment
			// item.
			if (getFlag(POPUPBAR_MOVE_FIGURE)
					&& hasPositionChanged(source.getBounds())) {
				hideDiagramAssistant(); // without delay
			} else {
				setFlag(POPUPBAR_MOVE_FIGURE, false); // toggle flag back
				Rectangle theBounds = source.getBounds();
				myPopupBarLastPosition.setLocation(theBounds.x, theBounds.y);

			}

		}
	}

	/**
	 * Listens for mouse key presses so the popup bar can be dismissed if the context
	 * menu is displayed
	 *
	 * @author affrantz@us.ibm.com
	 */
	private class PopupBarMouseListener extends MouseListener.Stub {

		/**
		 * @see org.eclipse.draw2d.MouseListener#mousePressed(org.eclipse.draw2d.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent me) {
			if (3 == me.button) // context menu, hide the popup bar
			{
				hideDiagramAssistant();
			}
			super.mousePressed(me);
			setPopupBarOnDiagramActivated(true);
			setActivationLocation(me.getLocation());
		}

		@Override
		public void mouseReleased(MouseEvent me) {
			super.mouseReleased(me);

		}
	}

	protected class DefaultTool extends PopupBarTool {

		private static final int Y_OFFSET = 32; // Y_OFFSET in the superclass is not visible

		private final Point location = getActivationLocation();

		public DefaultTool(EditPart epHost, CreateRequest theRequest) {
			super(epHost, theRequest);
		}

		public DefaultTool(EditPart epHost, IElementType elementType) {
			super(epHost, elementType);
		}

		/**
		 * We don't fall back on creating the element only if a nested view cannot be created.
		 */
		@Override
		protected Command getCommand() {
			Request theRequest = this.getTargetRequest();

			if (theRequest instanceof CreateRequest) {
				Point location = this.location;
				if (location == null) {
					location = this.getCurrentInput().getMouseLocation();
					location.y += Y_OFFSET;
				}
				((CreateRequest) theRequest).setLocation(location);
			}

			EditPart target = getHost().getTargetEditPart(theRequest);
			if (target == null) {
				target = getHost();
			}
			Command theCmd = target.getCommand(theRequest);
			// if we return a cmd that cannot execute then later downstream an
			// NPE can be generated.
			if (theCmd != null && theCmd.canExecute()) {
				return theCmd;
			}

			return UnexecutableCommand.INSTANCE;
		}
	}
}
