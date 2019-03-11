/*****************************************************************************
 * Copyright (c) 2011 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *		CEA LIST - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpart;

/**
 * Interface for Edit Parts that are responsible of the display of a shape.
 * <p>
 * This is currently only a marker interface, no method are planned to be added yet.
 * </p>
 */
public interface IShapeCompartmentEditPart {

	/** type given to the view model of this edit part */
	public static final String VIEW_TYPE = "compartment_shape_display";
}
