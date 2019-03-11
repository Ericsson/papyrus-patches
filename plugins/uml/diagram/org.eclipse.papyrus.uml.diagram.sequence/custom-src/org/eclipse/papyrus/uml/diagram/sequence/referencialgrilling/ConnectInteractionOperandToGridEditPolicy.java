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
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.NotationHelper;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentCombinedFragmentCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CombinedFragmentEditPart;
import org.eclipse.uml2.uml.Element;

/**
 * this editpolicy is overloaded because the width of the interaction operand is managed by the editpartparent
 *
 */
public class ConnectInteractionOperandToGridEditPolicy  extends ConnectRectangleToGridEditPolicy{

	private CombinedFragmentEditPart combinedFragmentEditPart;
	/**
	 * Constructor.
	 *
	 */
	public ConnectInteractionOperandToGridEditPolicy() {
		super();
		margin=27;
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectNodeToGridEditPolicy#activate()
	 *
	 */
	@Override
	public void activate() {
		//the parent is the compartment of combinedFragment so we look for its great parent
		if( getHost().getParent() instanceof CombinedFragmentCombinedFragmentCompartmentEditPart){
			if(getHost().getParent().getParent() instanceof CombinedFragmentEditPart){
				combinedFragmentEditPart = (CombinedFragmentEditPart)getHost().getParent().getParent();
				getDiagramEventBroker().addNotificationListener(combinedFragmentEditPart.getNotationView(), this);
			}
		}
		super.activate();
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectNodeToGridEditPolicy#deactivate()
	 *
	 */
	@Override
	public void deactivate() {
		if( combinedFragmentEditPart!=null){
			getDiagramEventBroker().removeNotificationListener(combinedFragmentEditPart.getNotationView(), this);
		}
		super.deactivate();
	}
	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectNodeToGridEditPolicy#initListeningColumnFinish(org.eclipse.gmf.runtime.notation.Node, org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.GrillingManagementEditPolicy, org.eclipse.uml2.uml.Element, org.eclipse.draw2d.geometry.PrecisionRectangle)
	 *
	 * @param node
	 * @param grilling
	 * @param element
	 * @param bounds
	 * @throws NoGrillElementFound
	 */
	@Override
	protected void initListeningColumnFinish(Node node, GridManagementEditPolicy grilling, Element element, PrecisionRectangle bounds) throws NoGrillElementFound {
		if(combinedFragmentEditPart!=null){
			Node cfNode=(Node)combinedFragmentEditPart.getNotationView();
			columnFinish=grilling.createColumnTolisten(bounds.x+BoundForEditPart.getWidthFromView(cfNode), element);
			getDiagramEventBroker().addNotificationListener(columnFinish, this);
		}
	}



	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectNodeToGridEditPolicy#updateColumFinishFromWitdhNotification(org.eclipse.draw2d.geometry.PrecisionRectangle)
	 *
	 * @param notationBound
	 */
	@Override
	protected void updateColumFinishFromWitdhNotification(PrecisionRectangle notationBound) {
		if( combinedFragmentEditPart!=null){
			Node cfNode=(Node)combinedFragmentEditPart.getNotationView();
			int newX=notationBound.x+BoundForEditPart.getWidthFromView(cfNode);
			updatePositionGridAxis(columnFinish, newX,0);
		}
	}
	/**
	 * @see org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.ConnectNodeToGridEditPolicy#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 *
	 * @param notification
	 */
	@Override
	public void notifyChanged(Notification notification) {
		super.notifyChanged(notification);
		if( notification.getEventType()==Notification.SET && notification.getNotifier() instanceof Bounds && (((EObject)notification.getNotifier()).eContainer().equals(combinedFragmentEditPart.getNotationView()))){
			PrecisionRectangle bounds=NotationHelper.getAbsoluteBounds((Node)((GraphicalEditPart)getHost()).getNotationView());
			if( notification.getFeature().equals(NotationPackage.eINSTANCE.getSize_Width())){
				updateColumFinishFromWitdhNotification(bounds);
			}
		}
	}

}
