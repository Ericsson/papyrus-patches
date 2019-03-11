/*****************************************************************************
 * Copyright (c) 2015 Christian W. Damus and others.
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

package org.eclipse.papyrus.infra.gmfdiag.common.sync;

import java.util.Collections;
import java.util.Iterator;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramEditPartsUtil;
import org.eclipse.papyrus.infra.sync.service.AbstractSyncTrigger;
import org.eclipse.papyrus.infra.sync.service.CascadeTriggers;
import org.eclipse.papyrus.infra.sync.service.ISyncAction;
import org.eclipse.papyrus.infra.sync.service.ISyncService;

import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;

/**
 * A synchronization trigger for the diagram edit-part in a GMF diagram viewer.
 */
public class DiagramSyncTrigger extends AbstractSyncTrigger {

	public DiagramSyncTrigger() {
		super();
	}

	/**
	 * Cascades the trigger to all edit-parts in the diagram whose views have a synchronization style applied.
	 */
	@Override
	public ISyncAction trigger(ISyncService syncService, Object object) {
		return new CascadeTriggers(synchronizedEditPartsFunction());
	}

	Function<Object, Iterable<?>> synchronizedEditPartsFunction() {
		return new Function<Object, Iterable<?>>() {
			@Override
			public Iterable<?> apply(Object input) {
				return getSynchronizedEditParts(input);
			}
		};
	}

	Iterable<? extends EditPart> getSynchronizedEditParts(Object root) {
		Iterable<? extends EditPart> result;

		if (!(root instanceof EditPartViewer)) {
			result = Collections.emptyList();
		} else {
			result = getSynchronizedEditParts((EditPartViewer) root);
		}

		return result;
	}

	Iterable<? extends EditPart> getSynchronizedEditParts(final EditPartViewer viewer) {
		return new Iterable<EditPart>() {
			@Override
			public Iterator<EditPart> iterator() {
				return new AbstractIterator<EditPart>() {
					TreeIterator<EditPart> delegate = DiagramEditPartsUtil.getAllContents(viewer.getRootEditPart(), false);

					@Override
					protected EditPart computeNext() {
						while (delegate.hasNext()) {
							EditPart next = delegate.next();
							if (!(next.getModel() instanceof View)) {
								delegate.prune();
							} else if (shouldTrigger((View) next.getModel())) {
								return next;
							}
						}

						return endOfData();
					}
				};
			}
		};
	}

	protected boolean shouldTrigger(View view) {
		boolean result;

		if (view instanceof Diagram) {
			// Always trigger on the diagram edit-part, for diagrams that are synchronized by default
			result = true;
		} else {
			result = SyncStyles.getSyncKind(view) != SyncKind.NONE;
		}

		return result;
	}
}
