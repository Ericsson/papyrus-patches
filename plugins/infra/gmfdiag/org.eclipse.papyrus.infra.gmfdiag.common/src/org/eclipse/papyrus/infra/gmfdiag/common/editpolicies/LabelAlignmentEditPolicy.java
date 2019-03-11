package org.eclipse.papyrus.infra.gmfdiag.common.editpolicies;

/*****************************************************************************
 * Copyright (c) 2014 CEA LIST.
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
 * Céline Janssens (ALL4TEC) celine.janssens@all4tec.net - Initial API and implementation
 *
 *****************************************************************************/
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.AbstractEditPolicy;
import org.eclipse.gef.requests.AlignmentRequest;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.diagram.ui.editparts.AbstractBorderItemEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;

/**
 * This EditPolicy provides the command for the alignment. In the case of
 * Label.
 */
public class LabelAlignmentEditPolicy extends AbstractEditPolicy{

	/** 
	 * the key for the Port Alignment EditPolicy 
	 */
	public static final String LABEL_ALIGNMENT_KEY = "Label Alignment"; //$NON-NLS-1$


	/** Integer corresponding to the alignment type ( p.e. Left = 1, Bottom = 32) */
	private int alignment ;

	/** Current edit Part */
	private GraphicalEditPart label ;

	/** Reference rectangle on which the Label is aligned */
	private Rectangle refBounds;

	/** Bounds of the current label */
	private Rectangle bounds;

	/**
	 * Get Command associate to the corresponding Request
	 * Only REQ_ALIGN request return a Command
	 * @return Command to move the label in case of REQ_ALIGN, null if not.
	 * @param request Request for which a command is required. 
	 */
	public Command getCommand(Request request) {
		Command cmd = null;
		if (REQ_ALIGN.equals(request.getType())){
			 cmd = getAlignLabelCommand ((AlignmentRequest) request);
		}
		return cmd;
	}

	/**
	 * Get The alignment Command for the corresponding Alignment Request.
	 * This method transforms the alignment Request into a move request and return the move command associated to this new Request.
	 * 
	 * @param request Alignment Request for which the Alignment Command is returned 
	 * @return Alignment command 
	 */
	private Command getAlignLabelCommand(AlignmentRequest request) {

		alignment = request.getAlignment();		
		// get the Alignment rectangle on which the selected edit part should be aligned.
		refBounds = request.getAlignmentRectangle().getCopy();

		label = (GraphicalEditPart) getHost(); 
		bounds = getAbsolutePosition(label);

		Point moveDelta = new Point(0, 0);

		if (isAlignmentAllowed()){
			switch (alignment ){
			case PositionConstants.LEFT:
				moveDelta.setX(refBounds.x - bounds.x );
				break;
			case PositionConstants.RIGHT:
				moveDelta.setX(refBounds.getRight().x - bounds.getRight().x );
				break;
			case PositionConstants.TOP:
				moveDelta.setY(refBounds.y - bounds.y );
				break;
			case PositionConstants.BOTTOM:
				moveDelta.setY(refBounds.getBottom().y - bounds.getBottom().y );
				break;
			case PositionConstants.CENTER:
				moveDelta.setX(refBounds.getTop().x - bounds.getTop().x );
				break;
			case PositionConstants.MIDDLE:
				moveDelta.setY(refBounds.getLeft().y - bounds.getLeft().y );
				break;
			}


			//build the Move request
			ChangeBoundsRequest req = new ChangeBoundsRequest(REQ_MOVE);

			req.setEditParts(((ChangeBoundsRequest)request).getEditParts());
			req.setMoveDelta(moveDelta);
			req.setExtendedData(request.getExtendedData());

			return getHost().getCommand(req);

		} 
		else {
			// if alignment not allowed return no command
			return null;

		}

	}

	/**
	 * Determine if the current edit part can be aligned.
	 * 
	 * The label cannot be aligned if its connector target or source moves as well
	 * Because if the source/target moves, the connector moves as well and the label linked to the connector translates with the Edge
	 * Same if the Affixed node is selected. 
	 */
	private boolean isAlignmentAllowed() {
		EditPart parent = label.getParent();
		boolean isAllow = true;
		if (parent instanceof AbstractConnectionEditPart){
			// Label alignment is not allowed if it should moves at the same time as the connector
			// That means if the Source or target of the connector is part of the selection
			int sourceSelectionType = ((ConnectionEditPart)label.getParent()).getSource().getSelected();
			int targetSelectionType = ((ConnectionEditPart)label.getParent()).getTarget().getSelected();

			
			boolean isRefDependent = isRefSibling(parent);
			
			if (!isRefDependent) {
				boolean isExtremitiesSelected = (sourceSelectionType == EditPart.SELECTED)||(targetSelectionType == EditPart.SELECTED);
				isAllow = !isExtremitiesSelected;
			}
			

		} else if (parent instanceof AbstractBorderItemEditPart) {
			// if the label is an affixed label and if the affixed node is part of the selection, the label is not aligned
			isAllow =  !(parent.getSelected() == EditPart.SELECTED);
		}

		return isAllow;
	}

	/**
	 * Define if the Label is sibling of the Reference object.
	 * @param parent Label's Parent
	 * @return true if the reference is a sibling of the Label
	 */
	private boolean isRefSibling(EditPart parent) {

		boolean isRefSibling = false;
		if (parent instanceof AbstractConnectionEditPart){

			List<?> children = parent.getChildren();
			Iterator<?> iter = children.iterator();

			while(iter.hasNext() && !isRefSibling ){
				Object child =  iter.next();
				if (((EditPart)child).getSelected() == EditPart.SELECTED_PRIMARY){
					isRefSibling = true;
				}
			}
		}
		return isRefSibling;
	}

	/**
	 * 
	 * A {@link Rectangle} with the absolute position from the {@link EditPart}
	 * 
	 * @param ep
	 *        the {@link EditPart} that we want the position
	 * @return a {@link Rectangle} with the absolute position from the {@link EditPart}
	 * 
	 */
	public static Rectangle getAbsolutePosition(EditPart ep) {
		GraphicalEditPart part = (GraphicalEditPart)ep;
		Rectangle rect = part.getFigure().getBounds().getCopy();
		part.getFigure().translateToAbsolute(rect);
		return rect;
	}



}
