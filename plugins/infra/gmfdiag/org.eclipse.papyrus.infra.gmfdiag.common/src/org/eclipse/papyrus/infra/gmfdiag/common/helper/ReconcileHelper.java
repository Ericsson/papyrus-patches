/*****************************************************************************
 * Copyright (c) 2010, 2015 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Patrick Tessier (CEA LIST) Patrick.tessier@cea.fr - Initial API and implementation
 *  Christian W. Damus (CEA) - bug 437217
 *  Christian W. Damus - bug 451683
 *  Christian W. Damus - bug 465416
 *  Simon Delisle - bug 507076
 *
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.helper;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.transaction.RollbackException;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditDomain;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.infra.emf.gmf.util.GMFUnsafe;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.reconciler.DiagramReconciler;
import org.eclipse.papyrus.infra.gmfdiag.common.reconciler.DiagramReconcilersReader;
import org.eclipse.papyrus.infra.gmfdiag.common.reconciler.DiagramVersioningUtils;

/**
 * Diagram migration between version of Papyrus.
 * @since 3.0
 */
public class ReconcileHelper {
	
	private final TransactionalEditingDomain domain;

	private static final String ALL_DIAGRAMS = "AllDiagrams";//$NON-NLS-1$

	/**
	 * Instantiates helper that will work with given {@link TransactionalEditingDomain}.
	 * Note that reconcile operations are performed outside the diagram command stack using {@link GMFUnsafe}.
	 */
	public ReconcileHelper(TransactionalEditingDomain domain) {
		this.domain = domain;
	}

	/**
	 * Process diagram reconcilers to migrate models. Does nothing if the diagram is already of the current Papyrus version based on {@link DiagramVersioningUtils#isOfCurrentPapyrusVersion(Diagram)} check.
	 * <p/>
	 * This method needs configured {@link DiagramEditDomain} to execute collected {@link ICommand} when needed, so it can't be called from constructor
	 *
	 * @param diagram
	 *            the diagram to reconcile
	 * @throws CoreException
	 *             subclass may throw wrapping any problem thrown from execution of reconcile using {@link GMFUnsafe}. Default implementation does not
	 *             throw it however
	 */
	public void reconcileDiagram(Diagram diagram) throws CoreException {
		CompositeCommand migration = buildReconcileCommand(diagram);
		if (migration == null) {
			return;
		}
		migration.add(DiagramVersioningUtils.createStampCurrentVersionCommand(diagram));
		try {
			GMFUnsafe.write(domain, migration);
		} catch (ExecutionException e) {
			handleReconcileException(diagram, e);
		} catch (InterruptedException e) {
			handleReconcileException(diagram, e);
		} catch (RollbackException e) {
			handleReconcileException(diagram, e);
		}
	}

	/**
	 * Process diagram reconcilers to migrate models.
	 *
	 * Returns <code>null</code> if the diagram is already of the current Papyrus version based on {@link DiagramVersioningUtils#isOfCurrentPapyrusVersion(Diagram)} check.
	 * <p/>
	 * If one of the reconcilers returns un-executable command, this method logs the problem and returns <code>null</code>
	 *
	 * @param diagram
	 *            the diagram to reconcile
	 */
	protected CompositeCommand buildReconcileCommand(Diagram diagram) {

		CompositeCommand reconcileCommand = new CompositeCommand("Reconciling");

		if (!DiagramVersioningUtils.isOfCurrentPapyrusVersion(diagram)) {

			String sourceVersion = DiagramVersioningUtils.getCompatibilityVersion(diagram);
			Map<String, Collection<DiagramReconciler>> diagramReconcilers = DiagramReconcilersReader.getInstance().load();
			String diagramType = diagram.getType();
			Collection<DiagramReconciler> reconcilers = new LinkedList<DiagramReconciler>();
			if (diagramReconcilers.containsKey(diagramType)) {
				reconcilers.addAll(diagramReconcilers.get(diagramType));
			}

			if (diagramReconcilers.containsKey(ALL_DIAGRAMS)) {
				reconcilers.addAll(diagramReconcilers.get(ALL_DIAGRAMS));
			}

			boolean someFailed = false;
			Iterator<DiagramReconciler> reconciler = reconcilers.iterator();
			while (reconciler.hasNext() && !someFailed) {
				DiagramReconciler next = reconciler.next();

				if (!next.canReconcileFrom(diagram, sourceVersion)) {
					// asked for ignore it for this instance, all fine
					continue;
				}
				ICommand nextCommand = next.getReconcileCommand(diagram);
				if (nextCommand == null) {
					// legitimate no-op response, all fine
					continue;
				}
				if (nextCommand.canExecute()) {
					reconcileCommand.add(nextCommand);
				} else {
					Activator.log.error("Diagram reconciler " + next + " failed to reconcile diagram : " + diagram, null); //$NON-NLS-1$ //$NON-NLS-2$
					someFailed = true;
				}
			}



			if (someFailed) {
				// probably better to fail the whole reconcile process as user will have a chance to reconcile later when we fix the problem with one of the reconcilers
				// executing partial reconciliation will leave the diagram in the state with partially current and partially outdated versions
				reconcileCommand = null;
			}

		}

		return reconcileCommand;
	}

	/**
	 * Handles exception from running the diagram reconciler under {@link GMFUnsafe}.
	 * At the time method is called the diagram is probably broken, but default implementation just logs error.
	 * <p/>
	 * This is to allow subclass to decide whether it is worth opening the problem diagram.
	 */
	protected void handleReconcileException(Diagram diagram, Exception e) throws CoreException {
		Activator.getInstance().logError("Reconciling the diagram: " + diagram, e); //$NON-NLS-1$
	}
}
