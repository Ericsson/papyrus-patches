/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tessier (CEA LIST) - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.BendpointRequest;
import org.eclipse.gef.requests.LocationRequest;
import org.eclipse.gef.tools.SelectEditPartTracker;
import org.eclipse.gmf.runtime.draw2d.ui.geometry.PointListUtilities;
import org.eclipse.gmf.runtime.gef.ui.internal.l10n.Cursors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;

/**
 * @since 3.0
 * this class is used to move several links at the same time.
 */
public class SelectSeveralLinksEditPartTracker extends SelectEditPartTracker {



	/**
	 * Key modifier for ignoring snap while dragging.  It's CTRL on Mac, and ALT on all
	 * other platforms.
	 */
	private final int MODIFIER_NO_SNAPPING;
	private Request sourceRequest;
	private int index = -1;
	private String type;
	private boolean bSourceFeedback = false;

	protected HashMap<EditPart, Point> locationsForEditParts= new HashMap<EditPart, Point>();

	int[] relativePosition=null;


	/**
	 * Method SelectConnectionEditPartTracker.
	 * @param owner ConnectionNodeEditPart that creates and owns the tracker object
	 */
	public SelectSeveralLinksEditPartTracker(ConnectionEditPart owner) {
		super(owner);

		if (SWT.getPlatform().equals("carbon"))//$NON-NLS-1$
			MODIFIER_NO_SNAPPING = SWT.CTRL;
		else
			MODIFIER_NO_SNAPPING = SWT.ALT;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.gef.tools.AbstractTool#handleButtonDown(int)
	 */
	@Override
	protected boolean handleButtonDown(int button) {
		if (!super.handleButtonDown(button))
			return false;

		Point p = getLocation();
		getConnection().translateToRelative(p);

		PointList points = getConnection().getPoints();
		Dimension size = new Dimension(7, 7);
		getConnection().translateToRelative(size);
		for (int i=1; i<points.size()-1; i++) {
			Point ptCenter = points.getPoint(i);
			Rectangle rect = new Rectangle( ptCenter.x - size.width / 2, ptCenter.y - size.height / 2, size.width, size.height);

			if (rect.contains(p)) {
				setType(RequestConstants.REQ_MOVE_BENDPOINT);
				setIndex(i);
			}
		}

		if (getIndex() == -1) {
			setIndex(PointListUtilities.findNearestLineSegIndexOfPoint(getConnection().getPoints(), new Point(p.x, p.y)));

			setIndex(getIndex() - 1);
			setType(RequestConstants.REQ_CREATE_BENDPOINT);
		}

		//compute the relative position be fore the beginning of the move
		List editparts = getOperationSet();
		relativePosition=new int[editparts.size()];
		if(editparts.size()>1){
			for (int i=0; i<editparts.size()-1;i++){
				Object currentEditPart = editparts.get(i);
				Object nextEditPart = editparts.get(i+1);
				if(currentEditPart instanceof org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart && nextEditPart instanceof org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart){
					Connection currentConnection=((org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart)currentEditPart).getConnectionFigure();
					Connection nextConnection=((org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart)nextEditPart).getConnectionFigure();
					Point currentConnectionPosition = currentConnection.getPoints().getFirstPoint().getCopy();
					Point nextConnectionPosition = nextConnection.getPoints().getFirstPoint().getCopy();
					relativePosition[i]=nextConnectionPosition.y-currentConnectionPosition.y;
				}
			}
		}

		return true;
	}

	/**
	 * Determines if the the connection should be dragged or not.
	 * 
	 * @return <code>boolean</code> <code>true</code> if dragging can occur, 
	 * <code>false</code> otherwise.
	 */
	protected boolean shouldAllowDrag() {
		return (getIndex() != -1);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.gef.tools.AbstractTool#handleButtonUp(int)
	 */
	@Override
	protected boolean handleButtonUp(int button) {

		boolean bExecuteDrag = isInState(STATE_DRAG_IN_PROGRESS) && shouldAllowDrag();

		boolean bRet = super.handleButtonUp(button);

		if (bExecuteDrag) {
			eraseSourceFeedback();
			setCurrentCommand(getCommand());
			executeCurrentCommand();
		}

		return bRet;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.gef.tools.AbstractTool#handleDragInProgress()
	 */
	@Override
	protected boolean handleDragInProgress() {
		if (isInState(STATE_DRAG_IN_PROGRESS) && shouldAllowDrag()) {
			updateSourceRequest();
			showSourceFeedback();
			setCurrentCommand(getCommand());
		}
		return true;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.gef.tools.AbstractTool#handleDragStarted()
	 */
	protected boolean handleDragStarted() {
		return stateTransition(STATE_DRAG, STATE_DRAG_IN_PROGRESS);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.gef.tools.AbstractTool#calculateCursor()
	 */
	@Override
	protected Cursor calculateCursor() {
		if (getType() == RequestConstants.REQ_MOVE_BENDPOINT) {
			return Cursors.CURSOR_SEG_MOVE;
		}

		return getConnection().getCursor();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.gef.Tool#deactivate()
	 */
	public void deactivate() {
		if (!isInState(STATE_TERMINAL))
			eraseSourceFeedback();
		sourceRequest = null;
		super.deactivate();
	}

	/**
	 * @return boolean true if feedback is being displayed, false otherwise.
	 */
	private boolean isShowingFeedback() {
		return bSourceFeedback;
	}

	/**
	 * Method setShowingFeedback.
	 * @param bSet boolean to set the feedback flag on or off.
	 */
	private void setShowingFeedback(boolean bSet) {
		bSourceFeedback = bSet;
	}

	/**
	 * @see org.eclipse.gef.tools.AbstractTool#createOperationSet()
	 */
	@Override
	protected List createOperationSet() {
		List list = new ArrayList();
		list.add(getConnectionEditPart());
		return list;
	}

	/**
	 * Method showSourceFeedback.
	 * Show the source drag feedback for the drag occurring
	 * within the viewer.
	 */
	private void showSourceFeedback() {
		List editParts = getOperationSet();
		for (int i = 0; i < editParts.size(); i++) {
			EditPart editPart = (EditPart) editParts.get(i);
			LocationRequest locationRequest= (LocationRequest)getSourceRequest();
			if( locationsForEditParts.get(editPart)!=null){
				Point location= locationsForEditParts.get(editPart);
				locationRequest.setLocation(location);
				editPart.showSourceFeedback(locationRequest);
			}
		}
		setShowingFeedback(true);
	}

	/**
	 * Method eraseSourceFeedback.
	 * Show the source drag feedback for the drag occurring
	 * within the viewer.
	 */
	private void eraseSourceFeedback() {	
		if (!isShowingFeedback())
			return;
		setShowingFeedback(false);
		List editParts = getOperationSet();

		for (int i = 0; i < editParts.size(); i++) {
			EditPart editPart = (EditPart) editParts.get(i);
			LocationRequest locationRequest= (LocationRequest)getSourceRequest();
			if( locationsForEditParts.get(editPart)!=null){
				Point location= locationsForEditParts.get(editPart);
				locationRequest.setLocation(location);
				editPart.eraseSourceFeedback(locationRequest);
			}

		}
	}

	/**
	 * Method getSourceRequest.
	 * @return Request
	 */
	private Request getSourceRequest() {
		if (sourceRequest == null)
			sourceRequest = createSourceRequest();
		return sourceRequest;
	}

	/**
	 * Determines the type of request that will be created for the drag
	 * operation.
	 * @return Object
	 */
	protected Object getType() {
		return type;
	}

	/**
	 * Sets the type of request that will be created for the drag operation.
	 * 
	 * @param type the <code>String</code> that represents the type of request.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Creates the source request that is activated when the drag operation
	 * occurs.
	 * 
	 * @return a <code>Request</code> that is the newly created source request
	 */
	protected Request createSourceRequest() {
		BendpointRequest request = new BendpointRequest();
		request.setType(getType());
		request.setIndex(getIndex());
		request.setSource((ConnectionEditPart)getSourceEditPart());
		return request;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.gef.tools.AbstractTool#getCommand()
	 */
	@Override
	protected Command getCommand() {
		List editparts = getOperationSet();
		EditPart part;

		CompoundCommand command = new CompoundCommand();
		command.setDebugLabel("change bounds");//$NON-NLS-1$
		for (int i = 0; i < editparts.size(); i++) {
			part = (EditPart) editparts.get(i);
			LocationRequest locationRequest= (LocationRequest)getSourceRequest();
			if( locationsForEditParts.get(part)!=null){
				Point location= locationsForEditParts.get(part);
				locationRequest.setLocation(location);
				command.add(part.getCommand(locationRequest));
			}
		}
		return command.unwrap();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.gef.tools.AbstractTool#getCommandName()
	 */
	@Override
	protected String getCommandName() {
		return getType().toString();
	}

	/**
	 * @return the <code>Connection</code> that is referenced by the connection edit part.
	 */
	private Connection getConnection() {
		return (Connection) getConnectionEditPart().getFigure();
	}

	/**
	 * Method getConnectionEditPart.
	 * @return ConnectionEditPart
	 */
	private ConnectionEditPart getConnectionEditPart() {
		return (ConnectionEditPart)getSourceEditPart();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.gef.tools.AbstractTool#getDebugName()
	 */
	@Override
	protected String getDebugName() {
		return "Bendpoint Handle Tracker " + getCommandName(); //$NON-NLS-1$
	}

	/**
	 * Gets the current line segment index that the user clicked on to 
	 * activate the drag tracker.
	 * 
	 * @return int
	 */
	protected int getIndex() {
		return index;
	}

	/**
	 * Method setIndex.
	 * Sets the current line segment index based on the location the user 
	 * clicked on the connection.
	 * @param i int representing the line segment index in the connection.
	 */
	public void setIndex(int i) {
		index = i;
	}

	/**
	 * @see org.eclipse.gef.tools.SimpleDragTracker#updateSourceRequest()
	 */
	protected void updateSourceRequest() {
		List editparts = getOperationSet();
		Dimension delta = getDragMoveDelta();
		Point mouseLocation = getStartLocation().getCopy();
		mouseLocation.translate(delta.width, delta.height);
		for (int index=0; index<editparts.size();index++){
			Object currentEditPart = editparts.get(index);
			if(currentEditPart instanceof org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart ){
				if( index>0){
					mouseLocation.translate(new Dimension (0,relativePosition[index-1]));
				}
				locationsForEditParts.put((EditPart)currentEditPart, mouseLocation.getCopy());

			}
		}
	}
	/**
	 * @see org.eclipse.gef.tools.AbstractTool#getOperationSet()
	 *
	 * @return
	 */
	@Override
	protected List getOperationSet() {
		if(getCurrentViewer().getSelectedEditParts().contains(getSourceEditPart())){
			return getCurrentViewer().getSelectedEditParts();}
		ArrayList result= new ArrayList<>();
		result.add(getSourceEditPart());
		return result;
	}
}
