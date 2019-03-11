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

package org.eclipse.papyrus.uml.diagram.sequence.util;

import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.swt.widgets.Display;

/**
 * A deferred action that posts itself asynchronously on the {@link Display} thread
 * and re-tries some limited number of times if its initial conditions are not met.
 * 
 * @since 5.0
 */
public abstract class RetryingDeferredAction {
	private static final int DEFAULT_RETRY_LIMIT = 3;

	private final Executor executor;
	private final int retryLimit;
	private volatile int retries;

	/**
	 * Initializes me with the {@code executor} on which to run myself.
	 * 
	 * @param executor
	 *            the executor on which to run myself
	 * @param retryLimit
	 *            the number of times I may retry
	 * 
	 * @throws IllegalArgumentException
	 *             if the retry limit is non-positive
	 */
	public RetryingDeferredAction(Executor executor, int retryLimit) {
		super();

		if (retryLimit <= 0) {
			throw new IllegalArgumentException("retry limit must be positive"); //$NON-NLS-1$
		}

		this.executor = executor;
		this.retryLimit = retryLimit;
	}

	/**
	 * Initializes me with the default number (three) of retries and
	 * an {@code executor} on which to run myself.
	 * 
	 * @param executor
	 *            the executor on which to run myself
	 */
	public RetryingDeferredAction(Executor executor) {
		this(executor, DEFAULT_RETRY_LIMIT);
	}

	/**
	 * Initializes me with a {@code display} on which to executor myself.
	 * 
	 * @param display
	 *            the display on which I post myself for delayed execution
	 * @param retryLimit
	 *            the number of times I may retry
	 * 
	 * @throws IllegalArgumentException
	 *             if the retry limit is non-positive
	 */
	public RetryingDeferredAction(Display display, int retryLimit) {
		this(display::asyncExec, retryLimit);
	}

	/**
	 * Initializes me with the default number (three) of retries and
	 * a {@code display} on which to executor myself.
	 * 
	 * @param display
	 *            the display on which I post myself for delayed execution
	 */
	public RetryingDeferredAction(Display display) {
		this(display, DEFAULT_RETRY_LIMIT);
	}

	/**
	 * Initializes me to execute myself asynchronously on the current display.
	 * 
	 * @param retryLimit
	 *            the number of times I may retry
	 * 
	 * @throws IllegalArgumentException
	 *             if the retry limit is non-positive
	 */
	public RetryingDeferredAction(int retryLimit) {
		this(Display.getCurrent(), retryLimit);
	}

	/**
	 * Initializes me to execute myself asynchronously on the current display and
	 * with the default number (three) of retries.
	 */
	public RetryingDeferredAction() {
		this(Display.getCurrent(), DEFAULT_RETRY_LIMIT);
	}

	/**
	 * Try an {@code action} up to the given number of times, deferred on the {@code display} thread.
	 * This is useful for the simple case where it is only necessary to attempt to perform the
	 * action and there is no need for an explicit preparation step.
	 * 
	 * @param display
	 *            the display on which thread to defer the {@code action}
	 * @param retryLimit
	 *            the maximal number of times to tr-try the {@code action}
	 * @param action
	 *            the action to perform. If it returns {@code false}, then it will
	 *            be re-tried (unless the limit is exceeded, of course)
	 * 
	 * @throws IllegalArgumentException
	 *             if the retry limit is non-positive
	 */
	public static void defer(Display display, int retryLimit, BooleanSupplier action) {
		defer(display::asyncExec, retryLimit, action);
	}

	/**
	 * Try an {@code action} up to the default number (three) of times, deferred on the {@code display} thread.
	 * 
	 * @param display
	 *            the display on which thread to defer the {@code action}
	 * @param action
	 *            the action to perform
	 */
	public static void defer(Display display, BooleanSupplier action) {
		defer(display, DEFAULT_RETRY_LIMIT, action);
	}

	/**
	 * Try an {@code action} up to the given number of times, asynchronously on an {@code executor}.
	 * This is useful for the simple case where it is only necessary to attempt to perform the
	 * action and there is no need for an explicit preparation step.
	 * 
	 * @param executor
	 *            the executor on which to run myself
	 * @param retryLimit
	 *            the maximal number of times to tr-try the {@code action}
	 * @param action
	 *            the action to perform. If it returns {@code false}, then it will
	 *            be re-tried (unless the limit is exceeded, of course)
	 * 
	 * @throws IllegalArgumentException
	 *             if the retry limit is non-positive
	 */
	public static void defer(Executor executor, int retryLimit, BooleanSupplier action) {
		new Wrapper(executor, retryLimit, action).post();
	}

	/**
	 * Try an {@code action} up to the default number (three) of times, asynchronously on an {@code executor}.
	 * 
	 * @param executor
	 *            the executor on which to run myself
	 * @param action
	 *            the action to perform
	 */
	public static void defer(Executor executor, BooleanSupplier action) {
		defer(executor, DEFAULT_RETRY_LIMIT, action);
	}

	/**
	 * Try an {@code action} up to the given number of times, deferred on the current display thread.
	 * 
	 * @param retryLimit
	 *            the maximal number of times to tr-try the {@code action}
	 * @param action
	 *            the action to perform
	 * 
	 * @throws IllegalArgumentException
	 *             if the retry limit is non-positive
	 */
	public static void defer(int retryLimit, BooleanSupplier action) {
		defer(Display.getCurrent(), retryLimit, action);
	}

	/**
	 * Try an {@code action} up to the default number (three) of times, deferred on the current display thread.
	 * 
	 * @param action
	 *            the action to perform
	 */
	public static void defer(BooleanSupplier action) {
		defer(Display.getCurrent(), DEFAULT_RETRY_LIMIT, action);
	}

	/**
	 * Prepares me for execution, testing my initial conditions and setting up any
	 * required state.
	 * 
	 * @return {@code true} if my initial conditions are satisfied and I may {@link #perform()};
	 *         {@code false}, otherwise
	 */
	protected abstract boolean prepare();

	/**
	 * Performs me. Will not be called unless {@link #prepare()} returned {@code true}.
	 */
	protected abstract void perform();

	private void run() {
		if (prepare()) {
			perform();
		} else {
			retries = retries + 1;
			post();
		}
	}

	/**
	 * Post me for deferred execution.
	 */
	public void post() {
		if (retries < retryLimit) {
			executor.execute(this::run);
		} else {
			UMLDiagramEditorPlugin.log.warn("Retry limit exceeded for " + this); //$NON-NLS-1$
		}
	}

	//
	// Nested types
	//

	private static final class Wrapper extends RetryingDeferredAction {
		private final BooleanSupplier action;

		Wrapper(Executor executor, int retryLimit, BooleanSupplier action) {
			super(executor, retryLimit);

			this.action = action;
		}

		@Override
		protected boolean prepare() {
			return action.getAsBoolean();
		}

		@Override
		protected void perform() {
			// Already done in the preparation step
		}
	}
}
