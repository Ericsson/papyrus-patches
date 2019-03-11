/**
 * Copyright (c) 2016 CEA LIST.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License 2.0
  * which accompanies this distribution, and is available at
  * https://www.eclipse.org/legal/epl-2.0/
  *
  * SPDX-License-Identifier: EPL-2.0
  *
  * Contributors:
  *  CEA LIST - Initial API and implementation
 */
package org.eclipse.papyrus.uml.diagram.sequence.providers;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.validation.AbstractModelConstraint;
import org.eclipse.emf.validation.IValidationContext;
import org.eclipse.emf.validation.model.IClientSelector;
import org.eclipse.emf.validation.service.IBatchValidator;
import org.eclipse.emf.validation.service.ITraversalStrategy;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.SequenceDiagramEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry;
import org.eclipse.papyrus.uml.diagram.sequence.validation.UMLValidationHelper;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionOperand;

/**
 * @generated
 */
public class UMLValidationProvider {

	/**
	 * @generated
	 */
	private static boolean constraintsActive = false;

	/**
	 * @generated
	 */
	public static boolean shouldConstraintsBePrivate() {
		return false;
	}

	/**
	 * @generated
	 */
	public static void runWithConstraints(TransactionalEditingDomain editingDomain, Runnable operation) {
		final Runnable op = operation;
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					constraintsActive = true;
					op.run();
				} finally {
					constraintsActive = false;
				}
			}
		};
		if (editingDomain != null) {
			try {
				editingDomain.runExclusive(task);
			} catch (Exception e) {
				UMLDiagramEditorPlugin.getInstance().logError("Validation failed", e); //$NON-NLS-1$
			}
		} else {
			task.run();
		}
	}

	/**
	 * @generated
	 */
	static boolean isInDefaultEditorContext(Object object) {
		if (shouldConstraintsBePrivate() && !constraintsActive) {
			return false;
		}
		if (object instanceof View) {
			return constraintsActive
					&& SequenceDiagramEditPart.MODEL_ID.equals(UMLVisualIDRegistry.getModelID((View) object));
		}
		return true;
	}

	/**
	 * @generated
	 */
	public static class DefaultCtx implements IClientSelector {

		/**
		 * @generated
		 */
		@Override
		public boolean selects(Object object) {
			return isInDefaultEditorContext(object);
		}
	}

	/**
	 * @generated
	 */
	public static class Ctx_3001 implements IClientSelector {

		/**
		 * @generated
		 */
		@Override
		public boolean selects(Object object) {
			if (isInDefaultEditorContext(object) && object instanceof View) {
				final String id = UMLVisualIDRegistry.getVisualID((View) object);
				boolean result = false;
				result = result || LifelineEditPart.VISUAL_ID.equals(id);
				return result;
			}
			return false;
		}
	}

	/**
	 * @generated
	 */
	public static ITraversalStrategy getNotationTraversalStrategy(IBatchValidator validator) {
		return new CtxSwitchStrategy(validator);
	}

	/**
	 * @generated
	 */
	private static class CtxSwitchStrategy implements ITraversalStrategy {

		/**
		 * @generated
		 */
		private ITraversalStrategy defaultStrategy;

		/**
		 * @generated
		 */
		private String currentSemanticCtxId;

		/**
		 * @generated
		 */
		private boolean ctxChanged = true;

		/**
		 * @generated
		 */
		private EObject currentTarget;

		/**
		 * @generated
		 */
		private EObject preFetchedNextTarget;

		/**
		 * @generated
		 */
		private final String[] contextSwitchingIdentifiers;

		/**
		 * @generated
		 */
		CtxSwitchStrategy(IBatchValidator validator) {
			this.defaultStrategy = validator.getDefaultTraversalStrategy();
			this.contextSwitchingIdentifiers = new String[] { LifelineEditPart.VISUAL_ID };
			Arrays.sort(this.contextSwitchingIdentifiers);
		}

		/**
		 * @generated
		 */
		@Override
		public void elementValidated(EObject element, IStatus status) {
			defaultStrategy.elementValidated(element, status);
		}

		/**
		 * @generated
		 */
		@Override
		public boolean hasNext() {
			return defaultStrategy.hasNext();
		}

		/**
		 * @generated
		 */
		@Override
		public boolean isClientContextChanged() {
			if (preFetchedNextTarget == null) {
				preFetchedNextTarget = next();
				prepareNextClientContext(preFetchedNextTarget);
			}
			return ctxChanged;
		}

		/**
		 * @generated
		 */
		@Override
		public EObject next() {
			EObject nextTarget = preFetchedNextTarget;
			if (nextTarget == null) {
				nextTarget = defaultStrategy.next();
			}
			this.preFetchedNextTarget = null;
			return this.currentTarget = nextTarget;
		}

		/**
		 * @generated
		 */
		@Override
		public void startTraversal(Collection traversalRoots, IProgressMonitor monitor) {
			defaultStrategy.startTraversal(traversalRoots, monitor);
		}

		/**
		 * @generated
		 */
		private void prepareNextClientContext(EObject nextTarget) {
			if (nextTarget != null && currentTarget != null) {
				if (nextTarget instanceof View) {
					final String id = UMLVisualIDRegistry.getVisualID((View) nextTarget);
					String nextSemanticId = (id != null && Arrays.binarySearch(contextSwitchingIdentifiers, id) >= 0)
							? id
							: null;
					if ((currentSemanticCtxId != null && currentSemanticCtxId != nextSemanticId)
							|| (nextSemanticId != null && nextSemanticId != currentSemanticCtxId)) {
						this.ctxChanged = true;
					}
					currentSemanticCtxId = nextSemanticId;
				} else {
					// context of domain model
					this.ctxChanged = currentSemanticCtxId != null;
					currentSemanticCtxId = null;
				}
			} else {
				this.ctxChanged = false;
			}
		}
	}

	/**
	 * @generated
	 */
	public static class Adapter1 extends AbstractModelConstraint {

		/**
		 * @generated
		 */
		@Override
		public IStatus validate(IValidationContext ctx) {
			Interaction context = (Interaction) ctx.getTarget();
			return UMLValidationHelper.validateFragmentsOrder(context, ctx);
		}
	}

	/**
	 * @generated
	 */
	public static class Adapter2 extends AbstractModelConstraint {

		/**
		 * @generated
		 */
		@Override
		public IStatus validate(IValidationContext ctx) {
			InteractionOperand context = (InteractionOperand) ctx.getTarget();
			return UMLValidationHelper.validateFragmentsOrder(context, ctx);
		}
	}

	/**
	 * @generated
	 */
	public static class Adapter3 extends AbstractModelConstraint {

		/**
		 * @generated
		 */
		@Override
		public IStatus validate(IValidationContext ctx) {
			Node context = (Node) ctx.getTarget();
			return UMLValidationHelper.validateFragmentsOrder(context, ctx);
		}
	}

	/**
	 * @generated
	 */
	static String formatElement(EObject object) {
		return EMFCoreUtil.getQualifiedName(object, true);
	}
}
