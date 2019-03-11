/*****************************************************************************
 * Copyright (c) 2016 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 490251
 *   Vincent LORENZO (CEA-LIST) vincent.lorenzo@cea.fr - Bug 490251, 493874
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.migration;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.gmf.runtime.common.core.command.AbstractCommand;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationUtils;
import org.eclipse.papyrus.infra.gmfdiag.common.reconciler.DiagramReconciler;
import org.eclipse.papyrus.uml.diagram.sequence.util.LifelineResizeHelper;
import org.eclipse.uml2.uml.ActionExecutionSpecification;
import org.eclipse.uml2.uml.BehaviorExecutionSpecification;
import org.eclipse.uml2.uml.Lifeline;

/**
 * This class has been created to fix the bugs 490251 and 493874.
 *
 * The size of the lifeline was saved in Luna, but ignored in Eclipse Mars, so the following problem appears:
 * <ul>
 * <li>the width of the lifeline name edit part changed, so:</li>
 * <ul>
 * </li>the middle of the lifeline moved, so {@link ActionExecutionSpecification} and {@link BehaviorExecutionSpecification} were not align on the Lifeline</li>
 * </ul>
 * <li>the height of the Lifeline changed to take the whole available height in the Interaction</li>
 * <ul>
 * <li>the method {@link OldCustomInteractionEditPart#refreshBounds()} changes the Lifeline height and tries to repears the anchors executing commands outside of the stack on each refresh
 * (calling {@link OldCustomInteractionEditPart#synchronizeSize()}), unfortunately some sizes of elements are not serialized (default values) and some size seems not set in the figure, so the calculus of anchors locations was wrong and all messages were
 * displayed
 * just under the name of the lifeline. + recalculate the anchors location with this new size.
 * </li>
 * </ul>
 *
 * The solutions:
 * <ul>
 * <li>bad solution: To fix the bug we tried to determine the futur height of the lifeline, to reset them to the good location in order to keep alignment between Lifeline and {@link ActionExecutionSpecification} and {@link BehaviorExecutionSpecification},
 * and to set the default sizes used in Luna to Lifeline shape when required. This first trial improved a lot the display of the diagram, but was not correct.</li>
 * <li>good solution: just add the good EAnnotation to the lifeline avoid all the problems described by the bug and the first solution. Nevertheless, the method {@link OldCustomInteractionEditPart#refreshBounds()} should be rewritten (bug 493999)</li>
 * </ul>
 */
public class SequenceDiagramLunaToMarsReconciler extends DiagramReconciler {

	/** The the for the value style of the diagram version */
	private static final String DIAGRAM_COMPATIBILITY_VERSION_KEY = "diagram_compatibility_version";//$NON-NLS-1$

	/** The source version. This reconciler must only occur on luna model */
	private static final String sourceDiagramVersion = "1.0.0";//$NON-NLS-1$

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.reconciler.DiagramReconciler#getReconcileCommand(org.eclipse.gmf.runtime.notation.Diagram)
	 */
	@Override
	public ICommand getReconcileCommand(final Diagram diagram) {
		String diagramVersion = NotationUtils.getStringValue(diagram, DIAGRAM_COMPATIBILITY_VERSION_KEY, "");//$NON-NLS-1$
		ICommand cmd = null;

		if (sourceDiagramVersion.equals(diagramVersion)) {
			cmd = new AbstractCommand("Reconcile Sequence Diagram locations.") {//$NON-NLS-1$

				@Override
				protected CommandResult doExecuteWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
					addManualSizeEAnnotation(diagram);
					return CommandResult.newOKCommandResult();
				}

				/**
				 * @see org.eclipse.gmf.runtime.common.core.command.AbstractCommand#doRedoWithResult(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
				 */
				@Override
				protected CommandResult doRedoWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
					return null;
				}

				/**
				 * @see org.eclipse.gmf.runtime.common.core.command.AbstractCommand#doUndoWithResult(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
				 */
				@Override
				protected CommandResult doUndoWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
					return null;
				}
			};
		}
		return cmd;
	}


	/**
	 * Eclipse Mars forces changes on the Lifeline width (ignoring saved values), excepted adding a specific EAnnoation to the Lifeline shapes
	 *
	 * @param diagram
	 *                    the diagram to update
	 */
	private void addManualSizeEAnnotation(final Diagram diagram) {
		final TreeIterator<EObject> allContentIterator = diagram.eAllContents();

		// we look for all Lifeline and we add a tag to tell to LifelineResizeHelper.isManualSize(this) to return true, and do nothing about the size of the lifeline
		while (allContentIterator.hasNext()) {
			EObject eObject = allContentIterator.next();
			if (eObject instanceof Shape) {
				EObject element = ((Shape) eObject).getElement();
				if (element instanceof Lifeline) {
					Shape shape = (Shape) eObject;
					EAnnotation eannotation = shape.getEAnnotation(LifelineResizeHelper.CUSTOM_EXTENSION_INFO);
					if (null != eannotation) {
						String value = eannotation.getDetails().get(LifelineResizeHelper.MANUAL_LABEL_SIZE);
						if (Boolean.FALSE.toString().equals(value) || value == null) {
							eannotation.getDetails().put(LifelineResizeHelper.MANUAL_LABEL_SIZE, Boolean.TRUE.toString());
						}
					}
					if (eannotation == null) {
						eannotation = EcoreFactory.eINSTANCE.createEAnnotation();
						eannotation.getDetails().put(LifelineResizeHelper.MANUAL_LABEL_SIZE, Boolean.TRUE.toString());
						eannotation.setSource(LifelineResizeHelper.CUSTOM_EXTENSION_INFO);
						shape.getEAnnotations().add(eannotation);
					}
				}
			}
		}
	}

	// the first version of the reconcilier -> not the good result, but I prefer save it before to be fully sure of the current version is the good one

	// /**
	// * the default width for the lifeline in Mars, always used excepted when it was bigger in Luna
	// */
	// private static final int LIFELINE_WIDTH_IN_MARS = 100;
	//
	// /** Default lifeline height on Luna version. */
	// protected static final int DEFAULT_LUNA_HEIGHT = 250;
	//
	// /** Default lifeline height since Mars version. */
	// protected static final int DEFAULT_MARS_HEIGHT = 699;
	//
	// /**
	// * the height saved when it is the default one
	// */
	// protected static final int DEFAULT_HEIGHT = -1;
	//
	//
	//
	// /**
	// * @see org.eclipse.papyrus.infra.gmfdiag.common.reconciler.DiagramReconciler#getReconcileCommand(org.eclipse.gmf.runtime.notation.Diagram)
	// */
	// @Override
	// public ICommand getReconcileCommand(final Diagram diagram) {
	// String diagramVersion = NotationUtils.getStringValue(diagram, DIAGRAM_COMPATIBILITY_VERSION_KEY, "");//$NON-NLS-1$
	// ICommand cmd = null;
	//
	// // other stuff exist in org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CustomInteractionEditPart.refreshBounds()
	// if (sourceDiagramVersion.equals(diagramVersion)) {
	// cmd = new AbstractCommand("Reconcile Sequence Diagram locations.") {//$NON-NLS-1$
	//
	// @Override
	// protected CommandResult doExecuteWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
	// // 1. we reset the good location to the lifeline and to their children
	// updateLifeLineLocationAndPreserveRelatedNodeLocation(diagram);
	//
	// // 2. we look for the interaction height. This height is used to change the height of all Lifeline and to recalculate all anchors locations
	// int newLifelineHeight = DEFAULT_HEIGHT;
	// TreeIterator<EObject> allContentIterator = diagram.eAllContents();
	// while (newLifelineHeight == -1 && allContentIterator.hasNext()) {
	// EObject current = allContentIterator.next();
	// if (current instanceof Shape) {
	// EObject element = ((Shape) current).getElement();
	// if (element instanceof Interaction) {
	// Bounds bounds = (Bounds) ((Shape) current).getLayoutConstraint();
	// newLifelineHeight = bounds.getHeight();
	// }
	// }
	// }
	// if (newLifelineHeight == -1) {
	// newLifelineHeight = DEFAULT_MARS_HEIGHT;
	// }
	// allContentIterator = diagram.eAllContents();
	//
	//
	// // 3. we cross the model to update all anchors locations
	// while (allContentIterator.hasNext()) {
	// // We cross all model elements
	// EObject eObject = allContentIterator.next();
	//
	// // we look for all edges
	// if (eObject instanceof Edge) {
	// Edge currentEdge = (Edge) eObject;
	// preserveAnchors(currentEdge, newLifelineHeight);
	// }
	// }
	//
	//
	// // we set a new size for all lifeline, required, because we use this new height to preserve the anchors
	// updateLifelineHeight(diagram, newLifelineHeight);
	// return CommandResult.newOKCommandResult();
	// }
	//
	// /**
	// * @see org.eclipse.gmf.runtime.common.core.command.AbstractCommand#doRedoWithResult(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	// */
	// @Override
	// protected CommandResult doRedoWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
	// return null;
	// }
	//
	// /**
	// * @see org.eclipse.gmf.runtime.common.core.command.AbstractCommand#doUndoWithResult(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	// */
	// @Override
	// protected CommandResult doUndoWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
	// return null;
	// }
	// };
	// }
	// return cmd;
	// }
	//
	// /**
	// *
	// * @param diagram
	// * the sequence diagram
	// * @param newLifelineHeight
	// * the new height for all Lifeline
	// */
	// private void updateLifelineHeight(Diagram diagram, int newLifelineHeight) {
	// TreeIterator<EObject> allContentIterator = diagram.eAllContents();
	// while (allContentIterator.hasNext()) {
	// EObject eObject = allContentIterator.next();
	// if (eObject instanceof Shape) {
	// EObject element = ((Shape) eObject).getElement();
	// if (element instanceof Lifeline) {
	// Object constraints = ((Shape) eObject).getLayoutConstraint();
	// if (constraints instanceof Bounds) {
	// ((Bounds) constraints).setHeight(newLifelineHeight);
	// ((Shape) eObject).setLayoutConstraint((LayoutConstraint) constraints);
	// }
	// }
	// }
	// }
	// }
	//
	// /**
	// * Set the new percentage to the anchors
	// *
	// * @param edge
	// * an edge
	// * @param lifelineHeigthInMars
	// * the height of the lifeline, required to calculate the new anchor percentage
	// */
	// private void preserveAnchors(Edge edge, int lifelineHeigthInMars) {
	// View sourceView = edge.getSource();
	// if (sourceView.getElement() instanceof Lifeline) {
	// preserveAnchorConnectedTo((IdentityAnchor) edge.getSourceAnchor(), sourceView, lifelineHeigthInMars);
	// }
	//
	// View targetView = edge.getTarget();
	// if (targetView.getElement() instanceof Lifeline) {
	// preserveAnchorConnectedTo((IdentityAnchor) edge.getTargetAnchor(), targetView, lifelineHeigthInMars);
	// }
	// }
	//
	// /**
	// *
	// * @param anchor
	// * the anchor to update
	// * @param lifelineView
	// * the lifeline view on which the anchor is connected
	// * @param lifelineHeigthInMars
	// * the size of the lifeline in mars, required to calculate the new percentage for the anchor
	// */
	// private void preserveAnchorConnectedTo(IdentityAnchor anchor, View lifelineView, int lifelineHeigthInMars) {
	// if (lifelineView instanceof Shape) {
	// Shape currentShape = (Shape) lifelineView;
	// Object constraints = currentShape.getLayoutConstraint();
	// if (constraints instanceof Bounds) {
	// int lifeLineLunaHeight = ((Bounds) constraints).getHeight();
	// if (lifeLineLunaHeight == DEFAULT_HEIGHT) {
	// lifeLineLunaHeight = DEFAULT_LUNA_HEIGHT;
	// }
	// // IdentityAnchor sourceAnchor = (IdentityAnchor) ((Edge) eObject).getSourceAnchor();
	// anchor.setId(getNewAnchorPosition(anchor, lifeLineLunaHeight, lifelineHeigthInMars));
	// }
	// }
	// }
	//
	// /**
	// * Eclipse Mars forces changes on the Lifeline width (ignoring saved values), so, we need to change the Lifeline position in order to keep alignment of Action Execution Specification
	// * and Behavior Exceution Specification aligned with it
	// *
	// * @param diagram
	// * the diagram to update
	// */
	// private void updateLifeLineLocationAndPreserveRelatedNodeLocation(Diagram diagram) {
	// TreeIterator<EObject> allContentIterator = diagram.eAllContents();
	// Map<Shape, Integer> lifeLinesToUpdate = new HashMap<Shape, Integer>();
	// Map<Shape, Integer> childrenToUpdate = new HashMap<Shape, Integer>();
	//
	// // we look for all Lifeline and their children
	// while (allContentIterator.hasNext()) {
	// EObject eObject = allContentIterator.next();
	// if (eObject instanceof Shape) {
	// EObject element = ((Shape) eObject).getElement();
	// if (element instanceof Lifeline) {
	// Object constraints = ((Shape) eObject).getLayoutConstraint();
	// if (constraints instanceof Bounds) {
	// int lifelineWidth = ((Bounds) constraints).getWidth();
	// // nothing to do in order case
	// if (LIFELINE_WIDTH_IN_MARS >= lifelineWidth) {
	// lifeLinesToUpdate.put((Shape) eObject, Integer.valueOf(lifelineWidth));
	// for (Object current : ((Shape) eObject).getChildren()) {
	// if (current instanceof Shape) {
	// childrenToUpdate.put((Shape) current, Integer.valueOf(lifelineWidth));
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	//
	// // we move the lifeline on the X axis, in order to keep the middle of the lifeline at the same position
	// for (Shape shape : lifeLinesToUpdate.keySet()) {
	// Integer value = lifeLinesToUpdate.get(shape);
	// Bounds bounds = (Bounds) shape.getLayoutConstraint();
	// int delta = (int) ((LIFELINE_WIDTH_IN_MARS - value) / 2);
	// double delta2 = ((double) (LIFELINE_WIDTH_IN_MARS - value)) / 2.0;
	// int delta3 = (int) delta2;
	// // we move the lifeline to the left
	// bounds.setX(bounds.getX() - delta);
	// shape.setLayoutConstraint(bounds);
	//
	//
	// }
	//
	// // we move the children of the lifeline to keep them on the vertical axis of the lifeline
	// for (Shape shape : childrenToUpdate.keySet()) {
	// Integer value = childrenToUpdate.get(shape);
	// Bounds bounds = (Bounds) shape.getLayoutConstraint();
	// int delta = (int) ((LIFELINE_WIDTH_IN_MARS - value) / 2.0);
	// // we move the lifeline contents to the right
	// bounds.setX(bounds.getX() + delta);
	// shape.setLayoutConstraint(bounds);
	// }
	// }
	//
	// /**
	// * Get The new corrected position.
	// *
	// * @param lifelineHeightInMars
	// */
	// private String getNewAnchorPosition(IdentityAnchor anchor, int lunaHeight, int lifelineHeightInMars) {
	// PrecisionPoint pp = BaseSlidableAnchor.parseTerminalString(anchor.getId());
	// if (null == pp) {
	// pp = new PrecisionPoint();
	// }
	// // int anchorYLunaPos = (int) Math.round(lunaHeight * pp.preciseY());
	// // pp.setPreciseY((double) anchorYLunaPos / lifelineHeightInMars);
	//
	// // working with double is mandatory, with the previous version, we get a bad result!
	// double anchorYLunaPos = lunaHeight * pp.preciseY();
	// double y = anchorYLunaPos / lifelineHeightInMars;
	// pp.setPreciseY(y);
	// // pp.setPreciseX(0.5);
	// return (new BaseSlidableAnchor(null, pp)).getTerminal();
	// }
	//

}
