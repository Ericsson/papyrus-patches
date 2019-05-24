/*****************************************************************************
 * Copyright (c) 2018-2019 CEA LIST, Christian W. Damus, and others.
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
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Bug 547864
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.commands.CommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.commands.SemanticCreateCommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest.ViewAndElementDescriptor;
import org.eclipse.gmf.runtime.diagram.ui.requests.EditCommandRequestWrapper;
import org.eclipse.gmf.runtime.diagram.ui.requests.RefreshConnectionsRequest;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultCreationEditPolicy;
import org.eclipse.papyrus.uml.diagram.common.Activator;
import org.eclipse.papyrus.uml.diagram.sequence.util.SequenceUtil;
import org.eclipse.papyrus.uml.service.types.element.UMLElementTypes;
import org.eclipse.papyrus.uml.service.types.helper.advice.InteractionContainerDeletionContext;
import org.eclipse.papyrus.uml.service.types.utils.ElementUtil;
import org.eclipse.papyrus.uml.service.types.utils.RequestParameterUtils;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Custom creation edit policy for containers of {@link InteractionFragment}s, primarily
 * for the creation of such fragments.
 *
 * @since 5.0
 */
public class InteractionFragmentContainerCreationEditPolicy extends DefaultCreationEditPolicy {

	/**
	 * Initializes me.
	 */
	public InteractionFragmentContainerCreationEditPolicy() {
		super();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.CreationEditPolicy#getCreateElementAndViewCommand(org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest)
	 */
	@Override
	protected Command getCreateElementAndViewCommand(CreateViewAndElementRequest request) {
		IElementType typeToCreate = request.getViewAndElementDescriptor().getElementAdapter().getAdapter(IElementType.class);

		if (!ElementUtil.isTypeOf(typeToCreate, UMLElementTypes.LIFELINE)) {
			IEditCommandRequest semanticCreateRequest = (IEditCommandRequest) request.getViewAndElementDescriptor().getCreateElementRequestAdapter().getAdapter(IEditCommandRequest.class);
			if (semanticCreateRequest != null) {
				// What are the lifelines covered?
				Rectangle rectangle = getCreationRectangle(request);
				Set<Lifeline> covered = SequenceUtil.getCoveredLifelines(rectangle, getHost());
				RequestParameterUtils.setCoveredLifelines(semanticCreateRequest, covered);
			}
		}

		// Create a special element and view create command for the combined fragment
		if (ElementUtil.isTypeOf(typeToCreate, UMLElementTypes.COMBINED_FRAGMENT)) {
			return getCreateElementAndViewCommandForCombinedFragmentWithUndo(request);
		}

		return super.getCreateElementAndViewCommand(request);
	}

	/**
	 * This allows to create the element and view command especially for the CombinedFragment (because we need to manage it differently for the undo).
	 *
	 * @param request
	 *            The create element and view request.
	 * @return The command to create the element and its view.
	 */
	private Command getCreateElementAndViewCommandForCombinedFragmentWithUndo(final CreateViewAndElementRequest request) {
		final TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();
		final Command undoCommand = new Command() {

			private CombinedFragment createdCombinedFragment = null;

			private EObject container = null;

			private EStructuralFeature feature = null;

			/**
			 * @see org.eclipse.gef.commands.Command#undo()
			 */
			@Override
			public void undo() {
				// We need to remove objects in InteractionOperand before deleting the created CombinedFragment

				// First step, get the created CombinedFragment
				final CombinedFragment combinedFragment = getCombinedFragment(request.getNewObject());

				// If the CombinedFragment is found, continue process
				if (null != combinedFragment) {

					createdCombinedFragment = combinedFragment;

					// Only one operand must be available
					final InteractionOperand interactionOperand = combinedFragment.getOperands().get(0);

					final CompositeCommand compositeCommand = new CompositeCommand("Move needed fragments"); //$NON-NLS-1$
					// Loop on each contained fragments to move it to super container (calculated automatically)
					for (final InteractionFragment interactionFragment : interactionOperand.getFragments()) {
						final DestroyElementRequest request = new DestroyElementRequest(editingDomain, interactionOperand, false);
						final Optional<InteractionContainerDeletionContext> context = InteractionContainerDeletionContext.get(request);
						final Optional<ICommand> result = context.map(ctx -> ctx.getDestroyCommand(interactionFragment));

						final ICommand undoCommand = result.get();
						if (null != undoCommand && undoCommand.canExecute()) {
							compositeCommand.add(undoCommand);
						}
					}

					// If there is something to move, move it
					if (null != compositeCommand && !compositeCommand.isEmpty() && compositeCommand.canExecute()) {
						try {
							compositeCommand.execute(new NullProgressMonitor(), null);
						} catch (ExecutionException e) {
							Activator.log.error(e);
						}
					}

					// We need to remove manually the combined fragment because it is not at the same position of the contained feature
					container = combinedFragment.eContainer();
					feature = container instanceof Interaction ? UMLPackage.eINSTANCE.getInteraction_Fragment() : container instanceof InteractionOperand ? UMLPackage.eINSTANCE.getInteractionOperand_Fragment() : null;
					final AbstractTransactionalCommand abstractTransactionalCommand = new AbstractTransactionalCommand(editingDomain, "Remove CombinedFragment", Collections.singletonList(combinedFragment.eResource())) { //$NON-NLS-1$

						@Override
						protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
							final Object value = container.eGet(feature);
							if (value instanceof List) {
								((List) value).remove(combinedFragment);
							}
							return CommandResult.newOKCommandResult();
						}
					};
					try {
						abstractTransactionalCommand.execute(new NullProgressMonitor(), null);
					} catch (ExecutionException e) {
						Activator.log.error(e);
					}
				}
			}

			/**
			 * @see org.eclipse.gef.commands.Command#redo()
			 */
			@Override
			public void redo() {
				// We need to manage the redo (only re-add the combined fragment)
				final AbstractTransactionalCommand abstractTransactionalCommand = new AbstractTransactionalCommand(editingDomain, "Remove CombinedFragment", null) { //$NON-NLS-1$

					@Override
					protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
						if (null != container && null != feature && null != createdCombinedFragment) {
							final Object value = container.eGet(feature);
							if (value instanceof List) {
								((List) value).add(createdCombinedFragment);
							}
						}
						return CommandResult.newOKCommandResult();
					}
				};
				try {
					abstractTransactionalCommand.execute(new NullProgressMonitor(), null);
				} catch (ExecutionException e) {
					Activator.log.error(e);
				}
			}
		};

		return undoCommand.chain(getCreateElementAndViewCommandForCombinedFragment(request));
	}

	/**
	 * Method getCreateElementAndViewCommand for the CombinedFragment.
	 * Copied from 'getCreateElementAndViewCommand' super implementation.
	 *
	 * @param request
	 *            The create element and view request.
	 * @return Command Which creates the semantic and the view command for the given CreateViewAndElementRequest.
	 */
	private Command getCreateElementAndViewCommandForCombinedFragment(final CreateViewAndElementRequest request) {
		// get the element descriptor
		final CreateElementRequestAdapter requestAdapter = request.getViewAndElementDescriptor().getCreateElementRequestAdapter();

		// get the semantic request
		final CreateElementRequest createElementRequest = (CreateElementRequest) requestAdapter.getAdapter(CreateElementRequest.class);

		if (createElementRequest.getContainer() == null) {
			// complete the semantic request by filling in the host's semantic element as the context
			final View view = (View) getHost().getModel();
			EObject hostElement = ViewUtil.resolveSemanticElement(view);

			if (hostElement == null && view.getElement() == null) {
				hostElement = view;
			}

			// Returns null if host is unresolvable so that trying to create a
			// new element in an unresolved shape will not be allowed.
			if (hostElement == null) {
				return null;
			}
			createElementRequest.setContainer(hostElement);
		}

		// get the create element command based on the elementdescriptor's request
		final Command createElementCommand = getHost().getCommand(
				new EditCommandRequestWrapper(
						(CreateElementRequest) requestAdapter.getAdapter(
								CreateElementRequest.class),
						request.getExtendedData()));

		if (createElementCommand == null) {
			return UnexecutableCommand.INSTANCE;
		}
		if (!createElementCommand.canExecute()) {
			return createElementCommand;
		}
		// create the semantic create wrapper command
		final SemanticCreateCommand semanticCommand = new SemanticCreateCommand(requestAdapter, createElementCommand) {

			/**
			 * @see org.eclipse.gmf.runtime.diagram.ui.commands.SemanticCreateCommand#doUndoWithResult(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
			 *
			 * @param progressMonitor
			 * @param info
			 * @return
			 * @throws ExecutionException
			 */
			@Override
			protected CommandResult doUndoWithResult(final IProgressMonitor progressMonitor, final IAdaptable info) throws ExecutionException {
				// We need to do nothing
				return CommandResult.newOKCommandResult();
			}

			/**
			 * @see org.eclipse.gmf.runtime.diagram.ui.commands.SemanticCreateCommand#doRedoWithResult(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
			 *
			 * @param progressMonitor
			 * @param info
			 * @return
			 * @throws ExecutionException
			 */
			@Override
			protected CommandResult doRedoWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
				// We need to do nothing
				return CommandResult.newOKCommandResult();
			}

		};
		final Command viewCommand = getCreateCommand(request);

		final Command refreshConnectionCommand = getHost().getCommand(
				new RefreshConnectionsRequest(((List<?>) request.getNewObject())));


		// form the compound command and return
		final CompositeCommand cc = new CompositeCommand(semanticCommand.getLabel());
		cc.compose(semanticCommand);
		cc.compose(new CommandProxy(viewCommand));
		if (refreshConnectionCommand != null) {
			cc.compose(new CommandProxy(refreshConnectionCommand));
		}

		return new ICommandProxy(cc);
	}

	/**
	 * This allows to get the CombinedFragment from the object in the create element and view request.
	 *
	 * @param newObject
	 *            The object in the create element and view request.
	 * @return The created CombinedFragment or <code>null</code>.
	 */
	private CombinedFragment getCombinedFragment(final Object newObject) {
		CombinedFragment result = null;

		if (newObject instanceof Collection) {
			final Iterator<?> collectionIt = ((Collection<?>) newObject).iterator();
			while (collectionIt.hasNext() && null == result) {
				final Object next = collectionIt.next();
				result = getCombinedFragment(next);
			}
		} else if (newObject instanceof CombinedFragment) {
			result = (CombinedFragment) newObject;
		} else if (newObject instanceof ViewAndElementDescriptor && null != ((ViewAndElementDescriptor) newObject).getCreateElementRequestAdapter()) {
			final CreateElementRequest createElementRequest = (CreateElementRequest) ((ViewAndElementDescriptor) newObject).getCreateElementRequestAdapter().getAdapter(CreateElementRequest.class);
			if (null != createElementRequest) {
				final EObject eObject = createElementRequest.getNewElement();
				if (eObject instanceof CombinedFragment) {
					result = (CombinedFragment) eObject;
				}
			}
		}

		return result;
	}

	protected Rectangle getCreationRectangle(CreateViewAndElementRequest request) {
		Point location = request.getLocation();
		Dimension size = request.getSize();

		if (size == null) {
			return new Rectangle(location.x(), location.y(), 1, 1);
		}

		return new Rectangle(location, size);
	}
}
