/*****************************************************************************
 * Copyright (c) 2010 CEA
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
 *  Atos Origin - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest.ConnectionViewDescriptor;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.Bendpoints;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.RelativeBendpoints;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.gmf.runtime.notation.datatype.RelativeBendpoint;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.uml.diagram.sequence.util.OccurrenceSpecificationHelper;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.UMLFactory;

/**
 * Command used to change the target of an edge.
 * It create an IdentityAnchor to attach the edge.
 *
 * @author Mathieu Velten
 *
 */
public class ChangeEdgeTargetCommand extends AbstractTransactionalCommand {

	protected CreateElementAndNodeCommand createElementAndNodeCommand;

	protected ConnectionViewDescriptor descriptor;

	protected Edge edge;

	protected String anchorId;

	/**
	 *
	 * @param editingDomain
	 *            the editing domain.
	 * @param createElementAndNodeCommand
	 *            used to retrieve the target new node of the edge.
	 * @param descriptor
	 *            used to retrieve the edge.
	 * @param anchorId
	 *            the identity of the anchor which will be created to attach the edge.
	 */
	public ChangeEdgeTargetCommand(TransactionalEditingDomain editingDomain, CreateElementAndNodeCommand createElementAndNodeCommand, ConnectionViewDescriptor descriptor, String anchorId) {
		super(editingDomain, "Change message graphical target", null);
		this.createElementAndNodeCommand = createElementAndNodeCommand;
		this.descriptor = descriptor;
		this.anchorId = anchorId;
	}

	/**
	 *
	 * @param editingDomain
	 *            the editing domain.
	 * @param createElementAndNodeCommand
	 *            used to retrieve the target new node of the edge.
	 * @param edge
	 *            the edge.
	 * @param anchorId
	 *            the identity of the anchor which will be created to attach the edge.
	 */
	public ChangeEdgeTargetCommand(TransactionalEditingDomain editingDomain, CreateElementAndNodeCommand createElementAndNodeCommand, Edge edge, String anchorId) {
		super(editingDomain, "Change message graphical target", null);
		this.createElementAndNodeCommand = createElementAndNodeCommand;
		this.edge = edge;
		this.anchorId = anchorId;
	}

	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		// retrieve the edge from the descriptor
		if (descriptor != null) {
	 		Object obj = descriptor.getAdapter(Edge.class);
			if (false == obj instanceof Edge) {
				return null;
			}
			edge = (Edge) obj;
		}
		View newTarget = createElementAndNodeCommand.getCreatedView();
		IdentityAnchor anchor = NotationFactory.eINSTANCE.createIdentityAnchor();
		anchor.setId(anchorId);
		edge.setTargetAnchor(anchor);
		// reset bendpoints to target
		Bendpoints bendpoints = edge.getBendpoints();
		if (bendpoints instanceof RelativeBendpoints) {
			List points = ((RelativeBendpoints) bendpoints).getPoints();
			if (!points.isEmpty()) {
				List<RelativeBendpoint> newPoints = new ArrayList<>();
				RelativeBendpoint first = (RelativeBendpoint) points.get(0);
				RelativeBendpoint last = (RelativeBendpoint) points.get(1);
				RelativeBendpoint rb1 = new RelativeBendpoint(first.getSourceX(), first.getSourceY(), first.getTargetX() - 8, first.getTargetY());
				RelativeBendpoint rb2 = new RelativeBendpoint(last.getSourceX() + 8, last.getSourceY(), last.getTargetX(), 0);
				newPoints.add(rb1);
				for (int i = 1; i < points.size() - 1; i++) {
					newPoints.add((RelativeBendpoint) points.get(i));
				}
				newPoints.add(rb2);
				((RelativeBendpoints) bendpoints).setPoints(newPoints);
			}
		}
		// Reset message end to target ExecutionSpecification, See https://bugs.eclipse.org/bugs/show_bug.cgi?id=402975
		EObject edgeElement = ViewUtil.resolveSemanticElement(edge);
		EObject targetElement = ViewUtil.resolveSemanticElement(newTarget);
		if (edgeElement instanceof Message && MessageSort.SYNCH_CALL_LITERAL == ((Message) edgeElement).getMessageSort() && targetElement instanceof ExecutionSpecification) {
			MessageEnd receiveEvent = ((Message) edgeElement).getReceiveEvent();

			Collection<EStructuralFeature.Setting> collection = EMFHelper.getUsages(receiveEvent);
			for (EStructuralFeature.Setting nonNavigableInverseReference : collection) {
				EObject eObject = nonNavigableInverseReference.getEObject();
				if (eObject instanceof ExecutionSpecification && eObject != targetElement) {
					if (((ExecutionSpecification) eObject).getStart() == receiveEvent) {
						OccurrenceSpecificationHelper.resetExecutionStart((ExecutionSpecification) eObject, UMLFactory.eINSTANCE.createExecutionOccurrenceSpecification());
					}
				}
			}

			OccurrenceSpecificationHelper.resetExecutionStart((ExecutionSpecification) targetElement, receiveEvent);
		}
		if (edgeElement instanceof Message && MessageSort.DELETE_MESSAGE_LITERAL == ((Message) edgeElement).getMessageSort() && targetElement instanceof DestructionOccurrenceSpecification) {
			// Set Message target to newly created DOS, destroy old MessageOccurrenceSpecification
			//LifelineMessageDeleteHelper.setMessageEndDos((Message)edgeElement, (DestructionOccurrenceSpecification)targetElement);
		}
		return null;
	}
}
