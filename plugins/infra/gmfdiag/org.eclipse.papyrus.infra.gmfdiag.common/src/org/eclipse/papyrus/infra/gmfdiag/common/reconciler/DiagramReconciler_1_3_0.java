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
 * 	 Maged Elaasar  - Initial API and Implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.reconciler;

import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gmf.runtime.common.core.command.AbstractCommand;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Style;
import org.eclipse.papyrus.infra.gmfdiag.representation.PapyrusDiagram;
import org.eclipse.papyrus.infra.gmfdiag.style.PapyrusDiagramStyle;
import org.eclipse.papyrus.infra.gmfdiag.style.StyleFactory;
import org.eclipse.papyrus.infra.viewpoints.style.PapyrusViewStyle;

/**
 * Diagram reconciler form 1.2.0 to 1.3.0 that replaces the old PapyrusViewStyle
 * by the new PapyrusDiagramStyle
 * @since 3.0
 */
public abstract class DiagramReconciler_1_3_0 extends DiagramReconciler {

	@Override
	public ICommand getReconcileCommand(Diagram diagram) {
		PapyrusViewStyle oldStyle = null;
		for(Object obj : new ArrayList<Style>(diagram.getStyles())) {
			if (obj instanceof PapyrusViewStyle) {
				oldStyle = (PapyrusViewStyle) obj;
			}
		}

		PapyrusDiagram diagramKind = getDiagramKind(diagram, oldStyle);

		PapyrusDiagramStyle newStyle = null;
		for(Object obj : new ArrayList<Style>(diagram.getStyles())) {
			if (obj instanceof PapyrusDiagramStyle) {
				newStyle = (PapyrusDiagramStyle) obj;
			}
		}

		if (newStyle == null && diagramKind != null) {
			newStyle = StyleFactory.eINSTANCE.createPapyrusDiagramStyle();
			if (oldStyle != null)
				newStyle.setOwner(oldStyle.getOwner());
			else
				newStyle.setOwner(diagram.getElement());
			newStyle.setDiagramKindId(diagramKind.getId());
			return new ReplacePapyrusViewStyleCommand(diagram, oldStyle, newStyle);
		}
		
		return null;
	}
	
	/**
	 *  Gets the new diagram kind that based on the given diagram and its old PapyrusViewStyle
	 *  
	 * @param diagram
	 * @param oldStyle
	 * @return
	 */
	protected abstract PapyrusDiagram getDiagramKind(Diagram diagram, PapyrusViewStyle oldStyle);
	
	/**
	 * A command to replace the old PapyrusViewStyle with the new PapyrusDiagramStyle
	 */
	protected class ReplacePapyrusViewStyleCommand extends AbstractCommand {

		private Diagram diagram;
		private PapyrusViewStyle oldStyle;
		private PapyrusDiagramStyle newStyle;

		public ReplacePapyrusViewStyleCommand(Diagram diagram, PapyrusViewStyle oldStyle, PapyrusDiagramStyle newStyle) {
			super("Replace the papyrus view style from 1.2.0 to 1.3.0");
			this.diagram = diagram;
			this.oldStyle = oldStyle;
			this.newStyle = newStyle;
		}

		@Override
		protected CommandResult doExecuteWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
			int index = diagram.getStyles().indexOf(oldStyle);
			if (index > -1) {
				diagram.getStyles().remove(index);
				diagram.getStyles().add(index, newStyle);
			} else
				diagram.getStyles().add(newStyle);
			return CommandResult.newOKCommandResult();
		}

		@Override
		public boolean canUndo() {
			return false;
		}

		@Override
		public boolean canRedo() {
			return false;
		}

		@Override
		protected CommandResult doRedoWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
			throw new ExecutionException("Should not be called, canRedo false"); //$NON-NLS-1$
		}

		@Override
		protected CommandResult doUndoWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
			throw new ExecutionException("Should not be called, canUndo false"); //$NON-NLS-1$
		}
	}
}
