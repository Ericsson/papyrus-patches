/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.core.internal.command.ICommandWithSettableResult;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.helpers.AnchorHelper.FixedAnchorEx;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Link;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.InteractionGraphImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.LinkImpl;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.uml2.uml.Element;

/**
 * @author ETXACAM
 *
 */
@SuppressWarnings({ "rawtypes", "restriction" })
public class SetLinkViewAnchorCommand extends AbstractTransactionalCommand
		implements ICommand, ICommandWithSettableResult {
	public static enum Anchor {
		SOURCE, TARGET
	}
	
	public SetLinkViewAnchorCommand(TransactionalEditingDomain domain, Link link, Anchor anchor, View anchoringView, Point anchoringPoint, String label, List affectedFiles) {
		super(domain, label, affectedFiles);
		this.graphLink = (LinkImpl) link;
		this.anchor = anchor;
		this.anchoringView = anchoringView;
		this.point = anchoringPoint;
	}

	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		InteractionGraphImpl graph = graphLink.getInteractionGraph();
		Edge edge = graphLink.getEdge();
		if (anchor == Anchor.SOURCE) {
			if (edge.getSource() != anchoringView) {
				edge.setSource(anchoringView);
			}
			
			Node n = graph.getClusterFor((Element)anchoringView.getElement());
			if (n == null)
				n = graph.getNodeFor((Element)anchoringView.getElement());
			//String anchorId = ViewUtilities.formatAnchorId(graph.getEditPartViewer(), anchoringView, point);
			String anchorId = ViewUtilities.formatAnchorId(n.getConstraints(), point);
			IdentityAnchor anchor = NotationFactory.eINSTANCE.createIdentityAnchor();
			anchor.setId(anchorId);
			edge.setSourceAnchor(anchor);
		} else {
			if (edge.getTarget() != anchoringView) {
				edge.setTarget(anchoringView);
			}

			Node n = graph.getClusterFor((Element)anchoringView.getElement());
			if (n == null)
				n = graph.getNodeFor((Element)anchoringView.getElement());
			//String anchorId = ViewUtilities.formatAnchorId(graph.getEditPartViewer(), anchoringView, point);
			String anchorId = ViewUtilities.formatAnchorId(n.getConstraints(), point);
			IdentityAnchor anchor = NotationFactory.eINSTANCE.createIdentityAnchor();
			anchor.setId(anchorId);
			edge.setTargetAnchor(anchor);			
		}
		return CommandResult.newOKCommandResult();
	}

	private LinkImpl graphLink;
	private Anchor anchor;
	private View anchoringView; 
	private Point point;
}
