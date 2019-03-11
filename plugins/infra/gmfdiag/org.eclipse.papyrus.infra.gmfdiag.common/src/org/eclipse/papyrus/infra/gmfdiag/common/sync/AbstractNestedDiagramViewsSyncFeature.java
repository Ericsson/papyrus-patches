/*****************************************************************************
 * Copyright (c) 2014, 2015 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   Christian W. Damus - bug 465416
 *   Christian W. Damus - bug 477384
 *
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.sync;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandWrapper;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.edit.command.DeleteCommand;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.SemanticElementAdapter;
import org.eclipse.papyrus.infra.gmfdiag.common.service.visualtype.VisualTypeService;
import org.eclipse.papyrus.infra.sync.EStructuralFeatureSyncFeature;
import org.eclipse.papyrus.infra.sync.SyncBucket;
import org.eclipse.papyrus.infra.sync.SyncItem;
import org.eclipse.papyrus.infra.sync.SyncRegistry;
import org.eclipse.papyrus.infra.sync.service.ISyncService;
import org.eclipse.papyrus.infra.sync.service.SyncServiceRunnable;
import org.eclipse.papyrus.infra.tools.util.TypeUtils;
import org.eclipse.swt.widgets.Control;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;

/**
 * Common implementation of a synchronization feature for nested views in a GMF diagram.
 *
 * @author Laurent Wouters
 *
 * @param <M>
 *            The type of the underlying model element common to all synchronized items in a single bucket
 * @param <N>
 *            The type of the model element visualized by the nested diagram views that I synchronize
 * @param <T>
 *            The type of the backend element to synchronize
 */
public abstract class AbstractNestedDiagramViewsSyncFeature<M extends EObject, N extends EObject, T extends EditPart> extends EStructuralFeatureSyncFeature<M, T> {
	private final SyncRegistry<N, T, Notification> nestedRegistry;

	private Map<EObject, EObject> lastKnownElements = new MapMaker().weakKeys().weakValues().makeMap();

	/**
	 * Initializes me with my controlling bucket and one or more references
	 * in the GMF notation model that provide nested views.
	 *
	 * @param bucket
	 *            The bucket doing the synchronization
	 * @param reference
	 *            a notation view reference
	 * @param more
	 *            additional (optional) notation view references
	 */
	public AbstractNestedDiagramViewsSyncFeature(SyncBucket<M, T, Notification> bucket, EReference reference, EReference... more) {
		super(bucket, reference, more);

		nestedRegistry = getSyncRegistry(getNestedSyncRegistryType());
	}

	protected abstract Class<? extends SyncRegistry<N, T, Notification>> getNestedSyncRegistryType();

	protected SyncRegistry<N, T, Notification> getNestedSyncRegistry() {
		return nestedRegistry;
	}

	/**
	 * Gets the edit part that shall be observed and modified from the specified one
	 *
	 * @param parent
	 *            The edit part we work on
	 * @return The effective edit part that is observed and modified
	 */
	protected abstract EditPart getEffectiveEditPart(EditPart parent);

	@Override
	protected final Iterable<? extends T> getContents(T backend) {
		return filterContents(basicGetContents(backend));
	}

	/**
	 * Gets an unfiltered view of the nested edit-parts with the specified {@code backend} edit-part, which depends
	 * on the kind of edit-part that it is.
	 * 
	 * @param backend
	 *            a back-end edit-part
	 * @return the raw view of its children, either nested nodes or contained/attached connections, as appropriate
	 */
	abstract Iterable<? extends T> basicGetContents(T backend);

	protected Iterable<? extends T> filterContents(Iterable<? extends T> rawContents) {
		return Iterables.filter(rawContents, new Predicate<T>() {
			private final Class<? extends N> nestedType = nestedRegistry.getModelType();

			@Override
			public boolean apply(T input) {
				View view = TypeUtils.as(input.getModel(), View.class);
				return (view != null) && nestedType.isInstance(view.getElement());
			}
		});
	}

	/**
	 * Filters the addition of new elements to add only those that are of the nested model element type.
	 */
	@Override
	protected boolean shouldAdd(SyncItem<M, T> from, SyncItem<M, T> to, EObject newSource) {
		return getNestedSyncRegistry().getModelType().isInstance(newSource);
	}

	/**
	 * Override this one because we need to execute certain post-actions asynchronously.
	 */
	@Override
	protected Command getAddCommand(final SyncItem<M, T> from, final SyncItem<M, T> to, final EObject newModel) {
		return new AbstractCommand() {
			private T child;

			private EObject objectToDrop;
			private org.eclipse.gef.commands.Command dropCommand;

			@Override
			protected boolean prepare() {
				return true;
			}

			/**
			 * We need to defer the calculation of the drop command until it is time to execute it,
			 * because otherwise the object that we need to drop may not yet exist in the target
			 * model if there is some kind of master/slave mapping in the semantic model, also.
			 * 
			 * @return the drop command
			 */
			private org.eclipse.gef.commands.Command getDropCommand() {
				if (dropCommand == null) {
					EditPartViewer viewer = getEffectiveEditPart(to.getBackend()).getViewer();
					Control control = viewer.getControl();
					FigureCanvas figureCanvas = (FigureCanvas) control;
					Point location = figureCanvas.getViewport().getViewLocation();

					objectToDrop = getTargetModel(from, to, newModel);
					IGraphicalEditPart dropEditPart = (IGraphicalEditPart) getEffectiveEditPart(to.getBackend());

					CreateRequest createRequest = getCreateRequest(dropEditPart, objectToDrop, location);
					if (createRequest != null) {
						dropCommand = getCreateCommand(dropEditPart, createRequest);
					}

					if (dropCommand == null) {
						dropCommand = org.eclipse.gef.commands.UnexecutableCommand.INSTANCE;
					}
				}

				return dropCommand;
			}

			@Override
			public void execute() {
				getDropCommand().execute();
				onDo();
			}

			@Override
			public void undo() {
				if (child != null) {
					Command additional = onTargetRemoved(to, child);
					if (additional != null) {
						additional.execute();
					}
				}
				getDropCommand().undo();
			}

			@Override
			public void redo() {
				getDropCommand().redo();
				onDo();
			}

			private void onDo() {
				runAsync(new SyncServiceRunnable.Safe<T>() {
					@Override
					public T run(ISyncService syncService) {
						child = findChild(to.getBackend(), getTargetModel(from, to, objectToDrop));
						if (child != null) {
							Command additional = onTargetAdded(from, newModel, to, child);
							if (additional != null) {
								syncService.execute(additional);
							}
						}
						return child;
					}
				});
			}
		};
	}

	protected CreateRequest getCreateRequest(IGraphicalEditPart parentPart, EObject element, Point atLocation) {
		CreateRequest result = null;

		View parentView = parentPart.getNotationView();

		// Consult the visual type service to get the appropriate view type
		String viewType = VisualTypeService.getInstance().getNodeType(parentView, element);
		if (viewType != null) {
			IElementType elementType = VisualTypeService.getInstance().getElementType(parentView.getDiagram(), viewType);
			IAdaptable elementAdapter = new SemanticElementAdapter(element, elementType);

			CreateViewRequest.ViewDescriptor descriptor = new CreateViewRequest.ViewDescriptor(
					elementAdapter,
					Node.class,
					viewType,
					parentPart.getDiagramPreferencesHint());
			result = new CreateViewRequest(descriptor);
			result.setLocation(atLocation);
		}

		return result;
	}

	protected org.eclipse.gef.commands.Command getCreateCommand(IGraphicalEditPart parentPart, CreateRequest request) {
		EditPart targetEditPart = getTargetEditPart(parentPart, request);
		if (targetEditPart instanceof IGraphicalEditPart) {
			parentPart = (IGraphicalEditPart) targetEditPart;
		}

		return parentPart.getCommand(request);
	}

	protected EditPart getTargetEditPart(EditPart parentEditPart, Request request) {
		return parentEditPart.getTargetEditPart(request);
	}

	/**
	 * Retrieves the child edit part that represents the specified model element
	 *
	 * @param parent
	 *            The parent edit part
	 * @param model
	 *            The model element to look for
	 * @return The child edit part, or <code>null</code> if none is found
	 */
	protected T findChild(T parent, EObject model) {
		if (parent == null || model == null) {
			return null;
		}
		Iterable<? extends T> children = getContents(parent);
		for (Iterator<? extends T> iter = children.iterator(); iter.hasNext();) {
			T child = iter.next();
			if (model == getModelOf(child)) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Override this one because we need to execute certain post-actions asynchronously.
	 */
	@Override
	protected Command getRemoveCommand(final SyncItem<M, T> from, final EObject oldSource, final SyncItem<M, T> to, T _oldTarget) {
		final View oldView = (View) _oldTarget.getModel();

		return new CommandWrapper(doGetRemoveCommand(from, oldSource, to, _oldTarget)) {
			private T oldTarget;

			@Override
			public void execute() {
				updateOldTarget();
				super.execute();
				onDo();
			}

			private void updateOldTarget() {
				Object editPart = to.getBackend().getViewer().getEditPartRegistry().get(oldView);
				oldTarget = AbstractNestedDiagramViewsSyncFeature.this.getNestedSyncRegistry().getBackendType().cast(editPart);
			}

			@Override
			public void undo() {
				super.undo();

				// Only notify of add if we had done that in the first place (this is not an initial sync that is being undone)
				if (oldSource != null) {
					runAsync(new SyncServiceRunnable.Safe<Command>() {
						@Override
						public Command run(ISyncService syncService) {
							// A new edit-part is always created when a deleted view is restored, so find it.
							updateOldTarget();

							Command additional = onTargetAdded(from, oldSource, to, oldTarget);
							if (additional != null) {
								syncService.execute(additional);
							}
							return additional;
						}
					});
				}
			}

			@Override
			public void redo() {
				updateOldTarget();
				super.redo();
				onDo();
			}

			private void onDo() {
				Command additional = onTargetRemoved(to, oldTarget);
				if (additional != null) {
					additional.execute();
				}
			}
		};
	}

	@Override
	protected Command doGetRemoveCommand(final SyncItem<M, T> from, final EObject oldSource, final SyncItem<M, T> to, final T oldTarget) {
		return DeleteCommand.create(getEditingDomain(), oldTarget.getModel());
	}

	@Override
	protected EObject getModelOfNotifier(EObject backendNotifier) {
		EObject result = ((View) backendNotifier).getElement();

		if (result != null) {
			// Update the cache of the last known element, if required
			if (lastKnownElements.get(backendNotifier) != result) {
				lastKnownElements.put(backendNotifier, result);
			}
		} else {
			result = lastKnownElements.get(backendNotifier);
		}

		return result;
	}

	@Override
	protected EObject getNotifier(T backend) {
		return (View) getEffectiveEditPart(backend).getModel();
	}
}
