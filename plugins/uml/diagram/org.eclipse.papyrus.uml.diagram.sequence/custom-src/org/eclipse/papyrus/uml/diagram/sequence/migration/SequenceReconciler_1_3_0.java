/*****************************************************************************
 * Copyright (c) 2010, 2015 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	 Florian Noyrit  (CEA) florian.noyrit@cea.fr - Initial API and Implementation
 *   Mickael ADAM (ALL4TEC) mickael.adam@all4tec.net - reconciler to add floating label
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.migration;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.command.AbstractCommand;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.reconciler.DiagramReconciler;

/**
 * Sequence Diagram Reconciler from 1.1.0 to 1.2.0
 * update Gates
 * update position of combinedFragments
 */
public class SequenceReconciler_1_3_0 extends DiagramReconciler {

	@Override
	public ICommand getReconcileCommand(Diagram diagram) {
		CompositeCommand cc = new CompositeCommand("Migrate diagram from 1.1.0 to 1.3.0");
		cc.add(new ChangeVisualIDsCommand(diagram));
		cc.add(new ChangeCombinedFragmentOrder(diagram));
		return cc;
	}

	protected class ChangeCombinedFragmentOrder extends AbstractCommand {


		protected final Diagram diagram;

		public ChangeCombinedFragmentOrder(Diagram diagram) {
			super("Change order of combinedFragment the diagram's visual ids from 1.2.0 to 1.3.0");
			this.diagram = diagram;
		}

		@Override
		protected CommandResult doExecuteWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
			//look for all combined fragment
			View interactionShape=null;
			View interactionCompartmentShape=null;
			if( diagram.getChildren().size()==1) {
				interactionShape=(View)diagram.getChildren().get(0);
				if(interactionShape.getChildren().size()>0) {
					for (int i = 0; i < interactionShape.getChildren().size(); i++) {
						View child=(View)interactionShape.getChildren().get(i);
						if( "Interaction_SubfragmentCompartment".equals(child.getType())) {
							interactionCompartmentShape=child;
						}
					}
				}
				if(interactionCompartmentShape!=null) {
					ArrayList<View>  combinedViews= new ArrayList<>();
					for (int i = 0; i < interactionCompartmentShape.getChildren().size(); i++) {
						View child=(View)interactionCompartmentShape.getChildren().get(i);
						if( "CombinedFragment_Shape".equals(child.getType())) {
							combinedViews.add(child);
						}
					}

					for (Iterator iterator = combinedViews.iterator(); iterator.hasNext();) {
						View view = (View) iterator.next();
						interactionCompartmentShape.getPersistedChildren().remove(view);
						interactionCompartmentShape.getPersistedChildren().add(0, view);

					}
				}
			}
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
			throw new ExecutionException("Should not be called, canRedo false");
		}

		@Override
		protected CommandResult doUndoWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
			throw new ExecutionException("Should not be called, canUndo false");
		}
	}




	protected class ChangeVisualIDsCommand extends AbstractCommand {

		protected final Diagram diagram;

		public ChangeVisualIDsCommand(Diagram diagram) {
			super("Change the diagram's visual ids from 1.2.0 to 1.3.0");
			this.diagram = diagram;
		}

		@Override
		protected CommandResult doExecuteWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
			TreeIterator<EObject> allContentIterator = diagram.eAllContents();

			while (allContentIterator.hasNext()) {
				EObject eObject = allContentIterator.next();
				if (eObject instanceof View) {
					View view = (View) eObject;
					view.setType(getNewVisualID(view.getType()));
				}
			}

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
			throw new ExecutionException("Should not be called, canRedo false");
		}

		@Override
		protected CommandResult doUndoWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
			throw new ExecutionException("Should not be called, canUndo false");
		}
	}

	public static String getNewVisualID(String oldVisualID) {
		switch (oldVisualID) {
		case "Gate":
			return "Gate_Shape";
		default:
			return oldVisualID;
		}
	}
}
