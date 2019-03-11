/*****************************************************************************
 * Copyright (c) 2015 CEA LIST and others.
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
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.commands.requests;

import static org.eclipse.papyrus.infra.tools.util.Iterables2.topoSort;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gmf.runtime.diagram.ui.requests.ArrangeRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
import org.eclipse.gmf.runtime.notation.Connector;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.DiagramHelper;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;

/**
 * A deferred layout of a set of views that rolls along in the future accumulating
 * views to arrange until there are no more to collect, at which point it actually
 * lays them out. This deferral is important because the eventual optimal layout
 * is naturally informed by the relationships between the entire set that needs
 * arranging.
 */
public class RollingDeferredArrangeRequest implements Runnable {
	private static ConcurrentMap<IArrangementContext, RollingDeferredArrangeRequest> deferredArrangements = new MapMaker().weakKeys().weakValues().makeMap();

	private final IArrangementContext context;

	private List<IAdaptable> viewAdapters;

	private volatile boolean cancelled;

	private AtomicBoolean bump = new AtomicBoolean(); // Whether to bump the layout to another deferred/asynchronous execution

	private RollingDeferredArrangeRequest(IArrangementContext context, Iterable<? extends IAdaptable> viewAdapters) {
		super();

		this.context = context;
		this.viewAdapters = Lists.newArrayList(viewAdapters);
	}

	/**
	 * Posts a collection of views (as view adapters) to be arranged later when all views needing
	 * to be arranged are ready.
	 * 
	 * @param context
	 *            the calling edit-policy context in which the arrangement is requested
	 * @param viewAdapters
	 *            the views to arrange, as view adapters
	 * @return the pending deferred arrangement request that will arrange the given views
	 */
	public static RollingDeferredArrangeRequest post(IArrangementContext context, Iterable<? extends IAdaptable> viewAdapters) {
		RollingDeferredArrangeRequest result = deferredArrangements.get(context);

		if (result == null) {
			// Look for opportunities to roll up arrange to a common ancestor edit-part
			result = rollup(context);
		}

		if (result != null) {
			// Just add more to it
			result.addAll(viewAdapters);
		} else {
			// This is the first layout request in this diagram
			result = new RollingDeferredArrangeRequest(context, viewAdapters);

			// Double-check
			RollingDeferredArrangeRequest pending = deferredArrangements.putIfAbsent(context, result);
			if (pending != null) {
				pending.addAll(viewAdapters);
			} else {
				result.schedule();
			}
		}

		return result;
	}

	/**
	 * Rolls up, if necessary, an edit-part arrangement {@code context} with the others
	 * that are already pending. The result of roll-up is either an existing context that
	 * contains this {@code context}, or the common ancestor of this {@code context} and one
	 * or more others already pending. The resulting roll-up maps to the aggregate of all
	 * of the view-adapter collections corresponding to the contexts that are rolled up into
	 * it.
	 * 
	 * @param context
	 *            a context to roll up
	 * @return its roll-up super-context
	 */
	private static RollingDeferredArrangeRequest rollup(IArrangementContext context) {
		RollingDeferredArrangeRequest result = null;

		// First look for a strict ancestor
		for (Map.Entry<IArrangementContext, RollingDeferredArrangeRequest> next : deferredArrangements.entrySet()) {
			if (isAncestor(next.getKey(), context)) {
				result = next.getValue();
				break;
			}
		}

		if (result == null) {
			// Find a common ancestor
			for (Map.Entry<IArrangementContext, RollingDeferredArrangeRequest> next : deferredArrangements.entrySet()) {
				IArrangementContext commonAncestor = commonAncestor(next.getKey(), context);
				if (commonAncestor != null) {
					// Obviously it isn't in the map, otherwise we would have found it in the first loop
					result = new RollingDeferredArrangeRequest(commonAncestor, next.getValue().viewAdapters);
					next.getValue().cancel();
					deferredArrangements.remove(next.getKey());
					deferredArrangements.put(commonAncestor, result);
					break;
				}
			}
		}

		return result;
	}

	private static boolean isAncestor(IArrangementContext putativeAncestor, IArrangementContext context) {
		return isAncestor(putativeAncestor.getHost(), context.getHost());
	}

	private static boolean isAncestor(EditPart putativeAncestor, EditPart editPart) {
		boolean result = false;

		for (EditPart next = editPart; !result && (next != null); next = next.getParent()) {
			result = next == putativeAncestor;
		}

		return result;
	}

	/**
	 * A partial ordering on edit parts that sorts edit parts ahead of those that contain them (directly
	 * or indirectly).
	 * 
	 * @return an ancestors-last partial ordering on edit parts
	 */
	private static Comparator<EditPart> ancestorComparator() {
		return new Comparator<EditPart>() {
			@Override
			public int compare(EditPart o1, EditPart o2) {
				int result = 0;

				if (isAncestor(o1, o2)) {
					result = +1;
				} else if (isAncestor(o2, o1)) {
					result = -1;
				}

				return result;
			}

			@Override
			public boolean equals(Object obj) {
				return (obj != null) && (obj.getClass() == getClass());
			}
		};
	}

	private static IArrangementContext commonAncestor(IArrangementContext context1, IArrangementContext context2) {
		IArrangementContext result = null;
		final EditPart ancestor = commonAncestor(context1.getHost(), context2.getHost());

		if (ancestor != null) {
			final IArrangementContext executor = context1;
			result = new IArrangementContext() {
				@Override
				public EditPart getHost() {
					return ancestor;
				}

				@Override
				public void execute(Command command) {
					executor.execute(command);
				}
			};
		}

		return result;
	}

	private static EditPart commonAncestor(EditPart ep1, EditPart ep2) {
		EditPart result = null;

		if ((ep1 != null) && (ep2 != null)) {
			if (isAncestor(ep1, ep2)) {
				result = ep1;
			} else {
				result = commonAncestor(ep1.getParent(), ep2.getParent());
			}
		}

		return result;
	}

	/**
	 * Schedules me to run at "some time in the future", usually either at the end of the active transaction
	 * (if any) or at the end of the display's event queue.
	 */
	private void schedule() {
		DiagramHelper.asyncExec(context.getHost(), this);
	}

	/**
	 * <p>
	 * Performs the deferred arrangement, unless I am bumped (in which case I re-schedule myself for
	 * still later execution) or I am cancelled (in which case I will never perform the layout).
	 * </p>
	 * <p>
	 * <b>Note</b> that this API must <em>not</em> be invoked by clients. It is for internal use only.
	 * </p>
	 */
	@Override
	public void run() {
		if (bump.compareAndSet(true, false)) {
			schedule();
		} else if (deferredArrangements.remove(context, this) && !cancelled) {
			doArrange();
		}
	}

	/**
	 * Adds more views to be arranged in my edit-part context. This further defers my execution,
	 * waiting for yet more views to be added to the overall arragement operation.
	 * 
	 * @param viewAdapters
	 *            views to be arranged, as view adapters
	 * @return myself, for the convenience of call chaining
	 */
	public RollingDeferredArrangeRequest addAll(Iterable<? extends IAdaptable> viewAdapters) {
		bump();
		Iterables.addAll(this.viewAdapters, viewAdapters);
		return this;
	}

	/**
	 * Re-defers my execution. If I am pending execution, I shall skip execution when my
	 * turn comes and instead just re-schedule myself.
	 */
	void bump() {
		if (!cancelled) {
			bump.set(true);
		}
	}

	/**
	 * Cancels my execution. This is appropriate when I am obsoleted by a {@linkplain #rollup(IArrangementContext) roll-up}
	 * transformation.
	 */
	void cancel() {
		cancelled = true;
		bump.set(false);
	}

	/**
	 * Computes and executes the layout commands that arrange the views I have accumulated.
	 */
	protected void doArrange() {
		// Partition the views to be arranged by their parent edit-parts because each parent
		// is responsible for arranging its children
		ListMultimap<EditPart, IAdaptable> toArrange = partition(viewAdapters);

		if (!toArrange.isEmpty()) {
			CompoundCommand arrange = new CompoundCommand("Arrange Views");

			// Process arrange commands bottom-up in the edit part tree to ensure that
			// details are arranged before the macro level is arranged
			for (EditPart targetEditPart : topoSort(toArrange.keySet(), ancestorComparator())) {
				ArrangeRequest request = new ArrangeRequest(RequestConstants.REQ_ARRANGE_DEFERRED);
				request.setViewAdaptersToArrange(toArrange.get(targetEditPart));
				Command command = targetEditPart.getCommand(request);
				if ((command != null) && command.canExecute()) {
					arrange.add(command);
				}
			}

			if (!arrange.isEmpty()) {
				context.execute(arrange.unwrap());
			}
		}
	}

	/**
	 * Partitions a collection of view adapters by the edit-parts that are the parents of (containing)
	 * the edit-parts managing the given views. The idea being that these parent edit-parts are the
	 * ones that are responsible for providing the layout commands of their child views.
	 * 
	 * @param viewAdapters
	 *            views to be arranged, as view adapters
	 * @return the views, partitioned by parent edit-part
	 */
	private ListMultimap<EditPart, IAdaptable> partition(Iterable<? extends IAdaptable> viewAdapters) {
		ListMultimap<EditPart, IAdaptable> result = ArrayListMultimap.create();
		EditPartViewer viewer = context.getHost().getViewer();

		@SuppressWarnings("unchecked")
		Map<?, ? extends EditPart> registry = viewer.getEditPartRegistry();
		for (IAdaptable next : viewAdapters) {
			View view = resolveArrangeableView(next);
			if (view != null) {
				EditPart editPart = registry.get(view);
				if (editPart != null) {
					EditPart parent = editPart.getParent();
					if (parent == null) {
						Activator.log.warn("Attempt to arrange the root edit part: " + editPart); //$NON-NLS-1$
					} else {
						result.put(parent, next);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Resolves a view adapter as an arrangeable view. Labels (as for messages in communication diagrams)
	 * are not arrangeable, as such, but the views (shapes and connectors) that they decorate are the views
	 * that should be arranged.
	 * 
	 * @param viewAdapter
	 *            a view adapter
	 * @return the arrangeable view that it adapts, or {@code null} if no arrangeable view can be resolved
	 */
	private View resolveArrangeableView(IAdaptable viewAdapter) {
		View result = null;

		for (View view = viewAdapter.getAdapter(View.class); view != null; view = (View) view.eContainer()) {
			if ((view instanceof Shape) || (view instanceof Connector)) {
				result = view;
				break;
			}
		}
		return result;
	}

	//
	// Nested types
	//

	/**
	 * The context in which an arrangement of views is requested.
	 * 
	 * @see RollingDeferredArrangeRequest#post(IArrangementContext, Iterable)
	 */
	public interface IArrangementContext {
		/**
		 * The edit-part requesting an arrangement of views, which presumably has children
		 * managing those views (the child edit-parts need not yet exist; that is one reason
		 * why arrangement may be deferred).
		 * 
		 * @return the requesting edit-part
		 */
		EditPart getHost();

		/**
		 * Executes an arrange command in the appropriate context, which usually should be
		 * folded into some other user-triggered operation, but which context is determined
		 * by the edit-part requesting the arrangement.
		 * 
		 * @param command
		 *            a command to execute
		 */
		void execute(Command command);
	}
}
