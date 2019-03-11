/*****************************************************************************
 * Copyright (c) 2017 CEA LIST, ALL4TEC and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Micka�l ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IPrimaryEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeEditPart;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceRequestConstant;
import org.eclipse.swt.widgets.Display;

/**
 * Tool to create Message Connection for sequence diagram.
 * it is used for lost and found messages
 */
public class MessageLostFoundConnectionTool extends SequenceSpecificConnectionTool {

	/**
	 * Constructor.
	 *
	 * @param elementTypes
	 */
	public MessageLostFoundConnectionTool(final List<IElementType> elementTypes) {
		super(elementTypes);
	}

	/**
	 * Returns the current x, y position of the mouse cursor.
	 * Sets the Y coordinate to the one of the start location
	 * for messages created with SHIFT being pressed.
	 *
	 * @return the mouse location
	 */
	@Override
	protected Point getLocation() {
		Point mouseLocation = getCurrentInput().getMouseLocation();
		// Horizontal connection if Shift is pressed
		if (getCurrentInput().isShiftKeyDown()) {
			return new Point(mouseLocation.x, getStartLocation().y);
		} else {
			return mouseLocation;
		}
	}

	/**
	 * Updates the target editpart and returns <code>true</code> if the target
	 * changes. The target is updated by using the target conditional and the
	 * target request. If the target has been locked, this method does nothing
	 * and returns <code>false</code>.
	 *
	 * @return <code>true</code> if the target was changed
	 */
	@Override
	protected boolean updateTargetUnderMouse() {
		if (!isTargetLocked()) {
			EditPart editPart = null;
			if (getCurrentViewer() != null) {
				editPart = getCurrentViewer().findObjectAtExcluding(
						getLocation(), getExclusionSet(),
						getTargetingConditional());
			}
			if( editPart instanceof InteractionInteractionCompartmentEditPart){
				editPart = getCurrentViewer().findObjectAtExcluding(
						getLocation(), getExclusionSet(),
						getTargetingConditional());
				editPart= editPart.getParent();
			}
			if (editPart != null) {
				editPart = editPart.getTargetEditPart(getTargetRequest());
			}
			boolean changed = getTargetEditPart() != editPart;
			setTargetEditPart(editPart);
			return changed;
		} else {
			return false;
		}
	}
	/**
	 * Queries the target editpart for a command.
	 *
	 * @see org.eclipse.gef.tools.AbstractTool#getCommand()
	 */
	@Override
	protected Command getCommand() {
		if (getTargetEditPart() == null) {
			return null;
		}
		return getTargetEditPart().getCommand(getTargetRequest());
	}
	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.tools.ConnectionCreationTool#selectAddedObject(org.eclipse.gef.EditPartViewer, java.util.Collection)
	 */
	@Override
	protected void selectAddedObject(final EditPartViewer viewer, final Collection objects) {
		final List editparts = new ArrayList();
		final EditPart[] primaryEP = new EditPart[1];
		for (Iterator i = objects.iterator(); i.hasNext();) {
			Object object = i.next();
			if (object instanceof IAdaptable) {
				Object editPart = viewer.getEditPartRegistry().get(((IAdaptable) object).getAdapter(View.class));
				if (editPart instanceof IPrimaryEditPart) {
					editparts.add(editPart);
				}
				// Priority is to put a shape into direct edit mode.
				if (editPart instanceof ShapeEditPart) {
					primaryEP[0] = (ShapeEditPart) editPart;
				}
			}
		}
		if (!editparts.isEmpty()) {
			viewer.setSelection(new StructuredSelection(editparts));
			// automatically put the first shape into edit-mode
			Display.getCurrent().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (primaryEP[0] == null) {
						primaryEP[0] = (EditPart) editparts.get(0);
					}
					//
					// add active test since test scripts are failing on this
					// basically, the editpart has been deleted when this
					// code is being executed. (see RATLC00527114)
					if (primaryEP[0].isActive()) {
						Request request = new Request(org.eclipse.gef.RequestConstants.REQ_DIRECT_EDIT);
						// Mark this request as the first direct edit after creation.
						request.getExtendedData().put(SequenceRequestConstant.DIRECT_EDIT_AFTER_CREATION, true);
						primaryEP[0].performRequest(request);
					}
				}
			});
		}
	}

}