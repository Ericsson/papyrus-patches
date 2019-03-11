/*****************************************************************************
 * Copyright (c) 2012 CEA LIST.
 *
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
 *
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.figure.node;


/**
 * this class is used to raise that the implemented Default Node plate is custom.
 * So the SVGNodePlate must delegate to it some functions
 *
 */
public interface ICustomNodePlate {
	/**
	 * set the SVG node plate that contain this custom node plate
	 *
	 * @param svgNodePlateFigure
	 *            not null
	 */
	public void setSVGNodePlateContainer(SVGNodePlateFigure svgNodePlateFigure);

	/**
	 * get the SVG node plate that contains this custom node plate
	 *
	 * @return the SVG node plate
	 */
	public SVGNodePlateFigure getSvgNodePlateContainer();
}
