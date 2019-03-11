/*****************************************************************************
 * Copyright (c) 2009 CEA
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
 *   Soyatec - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.util;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.emf.gmf.command.GMFtoEMFCommandWrapper;
import org.eclipse.papyrus.uml.diagram.common.commands.PreserveAnchorsPositionCommand;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.OLDLifelineEditPart.PreserveAnchorsPositionCommandEx;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * This class updates the property CoveredBy of Lifeline when a Lifeline gets created, moved/resize and also the resize of moving of each
 * CombinedFragment
 *
 * @author yyang
 *
 */
public class LifelineCoveredByUpdater {

	protected GraphicalEditPart context;

	protected Map<LifelineEditPart, Rectangle> lifelines = new HashMap<>();

	protected HashMap<GraphicalEditPart, Rectangle> interactionFragments = new HashMap<>();

	protected TransactionalEditingDomain editingDomain;


	protected void init() {
		editingDomain = this.context.getEditingDomain();
		GraphicalEditPart parent = context;
		while (true) {
			EditPart editPart = parent.getParent();
			if (editPart instanceof GraphicalEditPart) {
				parent = (GraphicalEditPart) editPart;
			} else {
				break;
			}
		}
		childrenCollect(parent);
	}

	private void childrenCollect(GraphicalEditPart editPart) {
		if (editPart instanceof LifelineEditPart) {
			IFigure figure = editPart.getFigure();
			Rectangle childBounds = figure.getBounds().getCopy();
			figure.translateToAbsolute(childBounds);
			Rectangle centralLineBounds = new Rectangle(childBounds.x() + childBounds.width() / 2, childBounds.y(), 1, childBounds.height());
			lifelines.put((LifelineEditPart) editPart, centralLineBounds);
		}
		if (editPart instanceof GraphicalEditPart) {
			EObject modelObject = ViewUtil.resolveSemanticElement((View) editPart.getModel());
			if (modelObject instanceof InteractionFragment
					&& false == modelObject instanceof Interaction) {
				IFigure figure = editPart.getFigure();
				Rectangle childBounds = figure.getBounds().getCopy();
				figure.translateToAbsolute(childBounds);
				interactionFragments.put(editPart, childBounds);
			}
		}
		for (Object child : editPart.getChildren()) {
			if (child instanceof GraphicalEditPart) {
				childrenCollect((GraphicalEditPart) child);
			}
		}
	}

	public void update(GraphicalEditPart context) {
		this.context = context;
		this.init();
		for (Map.Entry<LifelineEditPart, Rectangle> entry : lifelines.entrySet()) {
			LifelineEditPart editPart = entry.getKey();
			Rectangle childBounds = entry.getValue();
			updateLifeline(editPart, childBounds);
		}
	}

	public void updateLifeline(LifelineEditPart lifelineEditpart, Rectangle rect) {
		Lifeline lifeline = (Lifeline) lifelineEditpart.resolveSemanticElement();
		if (lifeline == null) {
			return;
		}
		EList<InteractionFragment> coveredByLifelines = lifeline.getCoveredBys();
		Set<InteractionFragment> coveredByLifelinesToAdd = new LinkedHashSet<>();
		Set<InteractionFragment> coveredByLifelinesToRemove = new LinkedHashSet<>();
		// Update height of Lifeline when coveredBy some InteractionFragments.
		int bottom = 0;
		for (Map.Entry<GraphicalEditPart, Rectangle> entry : interactionFragments.entrySet()) {
			GraphicalEditPart editPart = entry.getKey();
			Rectangle childBounds = entry.getValue();
			InteractionFragment interactionFragment = (InteractionFragment) editPart.resolveSemanticElement();
			if (rect.intersects(childBounds)) {
				coveredByLifelinesToAdd.add(interactionFragment);
				bottom = Math.max(childBounds.bottom(), bottom);
			} else if (coveredByLifelines.contains(interactionFragment)) {
				coveredByLifelinesToRemove.add(interactionFragment);
			}
		}

		coveredByLifelinesToRemove.removeAll(coveredByLifelinesToAdd);
		coveredByLifelinesToAdd.removeAll(coveredByLifelines);

		if (!coveredByLifelinesToAdd.isEmpty()) {
			CommandHelper.executeCommandWithoutHistory(editingDomain, AddCommand.create(editingDomain, lifeline, UMLPackage.eINSTANCE.getLifeline_CoveredBy(), coveredByLifelinesToAdd), true);
			// Update Lifeline height.
			int newHeight = bottom - rect.y;
			if (newHeight > rect.height) {
				Bounds bounds = (Bounds) ((Shape) lifelineEditpart.getModel()).getLayoutConstraint();
				int height = bounds.getHeight();
				// Fixed bug when has default height(-1).
				if (height == -1) {
					height = lifelineEditpart.getPrimaryShape().getBounds().height;
				}
				int heightDelta = newHeight - height;
				CommandHelper.executeCommandWithoutHistory(editingDomain, SetCommand.create(editingDomain, bounds, NotationPackage.Literals.SIZE__HEIGHT, newHeight), true);
				PreserveAnchorsPositionCommandEx preserveAnchorsCommand = new PreserveAnchorsPositionCommandEx(lifelineEditpart, new Dimension(0, heightDelta), PreserveAnchorsPositionCommand.PRESERVE_Y, lifelineEditpart.getPrimaryShape()
						.getFigureLifelineDotLineFigure(), PositionConstants.SOUTH);
				if (preserveAnchorsCommand.canExecute()) {
					CommandHelper.executeCommandWithoutHistory(editingDomain, new GMFtoEMFCommandWrapper(preserveAnchorsCommand), true);
				}
			}
		}
		if (!coveredByLifelinesToRemove.isEmpty()) {
			CommandHelper.executeCommandWithoutHistory(editingDomain, RemoveCommand.create(editingDomain, lifeline, UMLPackage.eINSTANCE.getLifeline_CoveredBy(), coveredByLifelinesToRemove), true);
		}
	}
}
