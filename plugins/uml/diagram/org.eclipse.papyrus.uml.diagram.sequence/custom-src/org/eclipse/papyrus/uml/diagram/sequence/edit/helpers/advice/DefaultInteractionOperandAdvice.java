/*****************************************************************************
 * Copyright (c) 2018 Christian W. Damus and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.helpers.advice;

import static org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramEditPartsUtil.getChildByEObject;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.CommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
import org.eclipse.gmf.runtime.emf.type.core.edithelper.AbstractEditHelperAdvice;
import org.eclipse.gmf.runtime.emf.type.core.requests.ConfigureRequest;
import org.eclipse.papyrus.infra.ui.editor.IMultiDiagramEditor;
import org.eclipse.papyrus.infra.ui.util.EditorHelper;
import org.eclipse.papyrus.uml.diagram.sequence.UmlSequenceDiagramForMultiEditor;
import org.eclipse.papyrus.uml.diagram.sequence.command.AsynchronousCommand;
import org.eclipse.papyrus.uml.diagram.sequence.validation.AsyncValidateCommand;
import org.eclipse.ui.IEditorPart;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.InteractionOperand;

/**
 * Advice for the configuration of the view of the default interaction operand
 * in a new combined fragment.
 */
public class DefaultInteractionOperandAdvice extends AbstractEditHelperAdvice {

	/**
	 * Initializes me.
	 */
	public DefaultInteractionOperandAdvice() {
		super();
	}

	@Override
	protected ICommand getAfterConfigureCommand(ConfigureRequest request) {
		ICommand result = null;

		DiagramEditPart sequenceDiagram = getActiveSequenceDiagram();

		if (sequenceDiagram != null) {
			CombinedFragment cfrag = (CombinedFragment) request.getElementToConfigure();
			result = new AsynchronousCommand("Configure Default Operand", request.getEditingDomain(),
					() -> configureOperand(cfrag, sequenceDiagram));
		}

		return result;
	}

	protected DiagramEditPart getActiveSequenceDiagram() {
		DiagramEditPart result = null;

		IEditorPart editor = EditorHelper.getCurrentEditor();
		if (editor instanceof IMultiDiagramEditor) {
			editor = ((IMultiDiagramEditor) editor).getActiveEditor();
		}

		if (editor instanceof UmlSequenceDiagramForMultiEditor) {
			result = ((UmlSequenceDiagramForMultiEditor) editor).getDiagramEditPart();
		}

		return result;
	}

	protected ICommand configureOperand(CombinedFragment cfrag, DiagramEditPart diagram) {
		if (cfrag.getOperands().size() != 1) {
			return null;
		}
		InteractionOperand operand = cfrag.getOperands().get(0);

		IGraphicalEditPart cfragShape = getChildByEObject(cfrag, diagram, false);
		if (cfragShape == null) {
			return null;
		}

		IGraphicalEditPart operandShape = getChildByEObject(operand, cfragShape, false);
		if (operandShape == null) {
			return null;
		}

		return fill(operandShape, cfragShape);
	}

	protected ICommand fill(IGraphicalEditPart operand, IGraphicalEditPart cfrag) {
		Rectangle cfragBounds = cfrag.getFigure().getBounds();
		Rectangle operandBounds = operand.getFigure().getBounds();

		if (cfragBounds.height() <= 0 || operandBounds.height() <= 0) {
			// Initial layout not computed, yet
			return null;
		}

		// Subtract one to avoid scroll bars
		int operandHeight = cfragBounds.y() + cfragBounds.height() - operandBounds.y() - 1;

		ChangeBoundsRequest request = new ChangeBoundsRequest(RequestConstants.REQ_RESIZE);
		request.setSizeDelta(new Dimension(0, operandHeight - operandBounds.height()));
		request.setResizeDirection(PositionConstants.SOUTH);
		request.setSnapToEnabled(false);
		request.setEditParts(operand);
		org.eclipse.gef.commands.Command command = operand.getCommand(request);

		ICommand result = (command == null) ? null : new CommandProxy(command);
		if (result != null) {
			// Trigger validation of the operand
			result = result.compose(new AsyncValidateCommand(operand.resolveSemanticElement()));
		}
		return result;
	}

}
