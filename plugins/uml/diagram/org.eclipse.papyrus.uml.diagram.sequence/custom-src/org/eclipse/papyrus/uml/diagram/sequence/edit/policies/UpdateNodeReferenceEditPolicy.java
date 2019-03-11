/*****************************************************************************
 * Copyright s(c) 2017 CEA LIST and others.
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

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CLifeLineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.BoundForEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.util.CoordinateReferentialUtils;
import org.eclipse.papyrus.uml.diagram.sequence.util.LifelineEditPartUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.LogOptions;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;

/**
 * this editpolicy is to manage the movement of message on node as lifeline
 * It is like a graphical node
 *
 * @since 4.0
 *
 */
public class UpdateNodeReferenceEditPolicy extends GraphicalEditPolicy {
	public static String UDPATE_NODE_REFERENCE = "UdpateNodeReferenceEditPolicy"; //$NON-NLS-1$

	/**
	 * To extract in other EditPolicy
	 * 
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#getCommand(org.eclipse.gef.Request)
	 *
	 * @param request
	 * @return
	 * 
	 * 		<img src="../../../../../../../../../icons/sequenceScheme.png" width="250" />
	 *         <UL>
	 *         <LI>when move B (anchor of the message)-->
	 *         Move E and Move F this is a move of the execution specification
	 *         <LI>when move C (anchor of the message)-->move F but not move E this is a resize of the execution specification
	 *         <UL>
	 */
	@Override
	public Command getCommand(Request request) {
		if (request instanceof ReconnectRequest && !(SenderRequestUtils.isASender(request, getHost()))) {

			final ReconnectRequest reconnectRequest = (ReconnectRequest) request;
			final ConnectionEditPart linkEditPart = reconnectRequest.getConnectionEditPart();
			UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+ MOVE ANCHORS of " + linkEditPart.getClass().getName());//$NON-NLS-1$
			Point locationOnDiagram = CoordinateReferentialUtils.transformPointFromScreenToDiagramReferential(reconnectRequest.getLocation(), (GraphicalViewer) getHost().getViewer());
			UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+-- LocationOnDiagram " + locationOnDiagram);//$NON-NLS-1$

			if (linkEditPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE) != null) {
				final SequenceReferenceEditPolicy references = (SequenceReferenceEditPolicy) linkEditPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE);
				final CompoundCommand compoundCommand = new CompoundCommand();
				for (Entry<EditPart, String> iterator : references.getStrongReferences().entrySet()) {
					final EditPart editPart = iterator.getKey();
					if (!SenderRequestUtils.isASender(request, editPart) && getHost().getChildren().contains(editPart)) {
						final GraphicalEditPart gEditPart = (GraphicalEditPart) editPart;
						final Point GEPlocationOnDiagram = CoordinateReferentialUtils.getFigurePositionRelativeToDiagramReferential(gEditPart.getFigure(), getDiagramEditPart(getHost()));

						locationOnDiagram = CoordinateReferentialUtils.transformPointFromScreenToDiagramReferential(SequenceUtil.getSnappedLocation(gEditPart, reconnectRequest.getLocation().getCopy()), (GraphicalViewer) getHost().getViewer());

						UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+--> try to Move  from " + GEPlocationOnDiagram + " " + editPart.getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
						final ChangeBoundsRequest changeBoundsRequest = new ChangeBoundsRequest(RequestConstants.REQ_RESIZE);
						changeBoundsRequest.setLocation(reconnectRequest.getLocation().getCopy());
						changeBoundsRequest.setEditParts(editPart);

						if (iterator.getValue().equals(SequenceReferenceEditPolicy.ROLE_START)) {
							final int delta = (locationOnDiagram.y() - GEPlocationOnDiagram.y());
							UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+--> Delta " + delta + " " + editPart.getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
							changeBoundsRequest.setMoveDelta(new Point(0, delta));
							changeBoundsRequest.setSizeDelta(new Dimension(0, 0));
						}
						if (iterator.getValue().equals(SequenceReferenceEditPolicy.ROLE_FINISH)) {
							final int delta = (locationOnDiagram.y() - GEPlocationOnDiagram.y() - gEditPart.getFigure().getBounds().height);
							UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+--> Delta " + delta + " " + editPart.getClass().getName());//$NON-NLS-1$ //$NON-NLS-2$
							changeBoundsRequest.setMoveDelta(new Point(0, delta));
							changeBoundsRequest.setSizeDelta(new Dimension(0, 0));
						}
						final ArrayList<EditPart> senderList = SenderRequestUtils.getSenders(request);
						SenderRequestUtils.addRequestSenders(changeBoundsRequest, senderList);
						SenderRequestUtils.addRequestSender(changeBoundsRequest, linkEditPart);
						final Command cmd = editPart.getCommand(changeBoundsRequest);
						compoundCommand.add(cmd);
					}
				}
				if (!compoundCommand.isEmpty()) {
					return compoundCommand;
				}
			}

			// Manage the strong references if this is a change bounds request
		} else if (request instanceof ChangeBoundsRequest && (!org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants.REQ_AUTOSIZE.equals(request.getType()))) {

			final ChangeBoundsRequest initialChangeBoundsRequest = (ChangeBoundsRequest) request;
			if(null == initialChangeBoundsRequest.getEditParts() || initialChangeBoundsRequest.getEditParts().isEmpty()) {
				return UnexecutableCommand.INSTANCE;
			}
			final Iterator<?> editParts = initialChangeBoundsRequest.getEditParts().iterator();
			
			// The reparent of execution specification in another life line is not allowed
			while(editParts.hasNext()) {
				Object childEP = editParts.next();
				if(childEP instanceof AbstractExecutionSpecificationEditPart && getHost() instanceof CLifeLineEditPart) {
					final LifelineEditPart parentLifeLine = SequenceUtil.getParentLifelinePart((AbstractExecutionSpecificationEditPart)childEP);
					if (null != parentLifeLine && !parentLifeLine.equals(getHost())) {
						return UnexecutableCommand.INSTANCE;
					}
				}
			}

			final Point moveDelta = initialChangeBoundsRequest.getMoveDelta();
			
			final CompoundCommand compoundCommand = new CompoundCommand();
			
			if (moveDelta.y != 0) {
				
				UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+ MOVE delta " + moveDelta + " of " + getHost());//$NON-NLS-1$ //$NON-NLS-2$
	
				// The variables needed to check the possible resize of life lines
				int maxY = -1;
				EditPart editPartSaved = null;
				
				for (final Object changedEditPart : initialChangeBoundsRequest.getEditParts()) {
					if (changedEditPart instanceof AbstractExecutionSpecificationEditPart) {
						final AbstractExecutionSpecificationEditPart execSpecEditPart = (AbstractExecutionSpecificationEditPart) changedEditPart;
						editPartSaved = execSpecEditPart;
						
						if(((EditPart) changedEditPart).getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE) != null) {
							if (!SenderRequestUtils.isASender(request, execSpecEditPart)) {
		
								final SequenceReferenceEditPolicy references = (SequenceReferenceEditPolicy) execSpecEditPart.getEditPolicy(SequenceReferenceEditPolicy.SEQUENCE_REFERENCE);
								for (EditPart editPart : references.getStrongReferences().keySet()) {
									if (!SenderRequestUtils.isASender(request, editPart) && editPart instanceof GraphicalEditPart && editPart.getModel() instanceof Node) {
		
										final Bounds initialBoundStrongRef = BoundForEditPart.getBounds((Node)editPart.getModel());
										
										UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+--> try to Move " + editPart.getClass().getName());//$NON-NLS-1$
			
										UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+--> try to Move from " + initialBoundStrongRef.getY() + " " + editPart.getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
										ChangeBoundsRequest changeBoundsRequest = new ChangeBoundsRequest(RequestConstants.REQ_RESIZE);
										changeBoundsRequest.setLocation(new Point(initialBoundStrongRef.getX(), initialBoundStrongRef.getY()));
										changeBoundsRequest.setEditParts(editPart);
		
										UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+--> Delta " + moveDelta.y + " " + editPart.getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
										changeBoundsRequest.setMoveDelta(new Point(0, moveDelta.y));
										changeBoundsRequest.setSizeDelta(new Dimension(0, 0));
		
										final ArrayList<EditPart> senderList = SenderRequestUtils.getSenders(request);
										SenderRequestUtils.addRequestSenders(changeBoundsRequest, senderList);
										SenderRequestUtils.addRequestSender(changeBoundsRequest, execSpecEditPart);
										final Command cmd = editPart.getCommand(changeBoundsRequest);
										compoundCommand.add(cmd);
										
										// If the move delta is going down, keep the maximum Y of the execution specification
										if (moveDelta.y > 0) {
											// The magical number '15' is here to define the Life line header height
											// TODO : We need to remove this magical number
											if (maxY < initialBoundStrongRef.getY() + initialBoundStrongRef.getHeight() + changeBoundsRequest.getMoveDelta().y + 15) {
												maxY = initialBoundStrongRef.getY() + initialBoundStrongRef.getHeight() + changeBoundsRequest.getMoveDelta().y + 15;
											}
										}
									}
								}
							}
						}
						
						// If the move delta is going down, keep the maximum Y of the execution specification
						if (moveDelta.y > 0) {
							final Bounds initialBoundStrongRef = BoundForEditPart.getBounds((Node)execSpecEditPart.getModel());
							
							// The magical number '15' is here to define the Life line header height
							// TODO : We need to remove this magical number
							if (maxY < initialBoundStrongRef.getY() + initialBoundStrongRef.getHeight() + moveDelta.y + 15) {
								maxY = initialBoundStrongRef.getY() + initialBoundStrongRef.getHeight() + moveDelta.y + 15;
							}
						}
					}
				}
				
				// Check if this is needed to resize life lines
				if(null != editPartSaved && maxY > 0) {
					final CompoundCommand resizeCompoundCommand = new CompoundCommand("Resize life lines"); //$NON-NLS-1$
					LifelineEditPartUtil.resizeAllLifeLines(resizeCompoundCommand, editPartSaved, maxY, null);
					
					if(!resizeCompoundCommand.isEmpty()) {
						compoundCommand.add(resizeCompoundCommand);
					}
				}
			}
			
			if (!compoundCommand.isEmpty()) {
				return compoundCommand;
			}
		}
		return super.getCommand(request);
	}


	/**
	 * Walks up the editpart hierarchy to find and return the
	 * <code>TopGraphicEditPart</code> instance.
	 */
	public DiagramEditPart getDiagramEditPart(EditPart editPart) {
		while (editPart instanceof IGraphicalEditPart) {
			if (editPart instanceof DiagramEditPart) {
				return (DiagramEditPart) editPart;
			}

			editPart = editPart.getParent();
		}
		if (editPart instanceof DiagramRootEditPart) {
			return (DiagramEditPart) ((DiagramRootEditPart) editPart).getChildren().get(0);
		}
		return null;
	}
}
