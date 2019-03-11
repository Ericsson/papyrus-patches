/*****************************************************************************
 * Copyright (c) 2010, 2015 CEA, Christian W. Damus, and others
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
 *   Soyatec - Initial API and implementation
 *   Christian W. Damus - bug 433206
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.papyrus.uml.diagram.common.commands.PreserveAnchorsPositionCommand;
import org.eclipse.uml2.uml.ConnectableElement;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StructuredClassifier;
import org.eclipse.uml2.uml.Type;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 * @deprecated will be remove in Oxygen
 */
@Deprecated
public class OLDLifelineEditPart {

	public static class PreserveAnchorsPositionCommandEx extends PreserveAnchorsPositionCommand {

		public PreserveAnchorsPositionCommandEx(ShapeNodeEditPart shapeEP, Dimension sizeDelta, int preserveAxis) {
			super(shapeEP, sizeDelta, preserveAxis);
		}

		public PreserveAnchorsPositionCommandEx(ShapeNodeEditPart shapeEP, Dimension sizeDelta, int preserveAxis, IFigure figure, int resizeDirection) {
			super(shapeEP, sizeDelta, preserveAxis, figure, resizeDirection);
		}

		@Override
		protected String getNewIdStr(IdentityAnchor anchor) {
			// DestructionOccurrenceSpecification is always on the bottom
			if (anchor.eContainer() instanceof Edge) {
				Edge edge = (Edge) anchor.eContainer();
				if (edge.getElement() instanceof Message && ((Message) edge.getElement()).getReceiveEvent() instanceof DestructionOccurrenceSpecification) {
					if (anchor.equals(edge.getTargetAnchor())) {
						return "(0.5, 1.0)";
					}
				}
			}
			String res = super.getNewIdStr(anchor);
			String id = anchor.getId();
			int start = id.indexOf('{');
			if (start > 0) {
				res = res + id.substring(start);
			}
			return res;
		}
	}

	/**
	 * Return the inner ConnectableElements of the lifeline
	 *
	 * @param lifeline
	 *                     The lifeline
	 * @return inner ConnectableElements
	 */
	// TODO Extract in a helper
	public static List<Property> getProperties(Lifeline lifeline) {
		if (lifeline != null) {
			ConnectableElement represents = lifeline.getRepresents();
			if (represents != null) {
				Type type = represents.getType();
				if (type instanceof StructuredClassifier) {
					StructuredClassifier structuredClassifier = (StructuredClassifier) type;
					if (!structuredClassifier.getAllAttributes().isEmpty()) {
						return new ArrayList<>(((StructuredClassifier) type).getAllAttributes());
					}
				}
			}
		}
		return null;
	}

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	@Deprecated
	private OLDLifelineEditPart() {
		// Deprecated
	}

}
