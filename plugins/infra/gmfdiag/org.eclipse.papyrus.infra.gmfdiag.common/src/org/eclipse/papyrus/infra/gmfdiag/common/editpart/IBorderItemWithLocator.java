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

package org.eclipse.papyrus.infra.gmfdiag.common.editpart;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gmf.runtime.diagram.ui.figures.IBorderItemLocator;

/**
 * @author Patrick Tessier
 *
 */
public interface IBorderItemWithLocator {
	
	public IBorderItemLocator getNewBorderItemLocator(IFigure mainFigure);

}
