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

import org.eclipse.gmf.runtime.notation.NamedStyle;
import org.eclipse.gmf.runtime.notation.StringValueStyle;

/**
 * The kinds of diagram-to-diagram synchronization supported by {@linkplain SyncStyles notation styles}.
 * 
 * @see SyncStyles
 */
public enum SyncKind {
	/** Not participating in synchronization. */
	NONE, //
	/** Participating in a peer synchronization group. */
	PEER, //
	/** Participating as the master role in a master-slave synchronization group. */
	MASTER, //
	/** Participating as a slave role in a master-slave synchronization group. */
	SLAVE;

	public String styleKey() {
		return (this == NONE) ? null : name().toLowerCase();
	}

	public static SyncKind forStyle(NamedStyle style) {
		String key = (style instanceof StringValueStyle) ? ((StringValueStyle) style).getStringValue() : null;

		return (key == null) ? NONE : nullSafe(valueOf(key.toUpperCase()));
	}

	public static SyncKind nullSafe(SyncKind kind) {
		return (kind == null) ? NONE : kind;
	}
}
