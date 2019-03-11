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

package org.eclipse.papyrus.infra.gmfdiag.common.updater;

import java.util.List;

import org.eclipse.gmf.runtime.notation.View;

/**
 * @since 2.0
 */
public interface DiagramUpdater {

	public List<? extends UpdaterNodeDescriptor> getSemanticChildren(View view);

	public List<? extends UpdaterLinkDescriptor> getContainedLinks(View view);

	public List<? extends UpdaterLinkDescriptor> getIncomingLinks(View view);

	public List<? extends UpdaterLinkDescriptor> getOutgoingLinks(View view);
}
