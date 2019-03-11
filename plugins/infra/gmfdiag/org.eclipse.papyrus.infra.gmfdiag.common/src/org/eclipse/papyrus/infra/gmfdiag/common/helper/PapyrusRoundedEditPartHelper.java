/*****************************************************************************
 * Copyright (c) 2010, 2014 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and Implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.helper;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.notation.NamedStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.StringValueStyle;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.IPapyrusEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IRoundedRectangleFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationUtils;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.LineStyleEnum;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.NamedStyleProperties;

/**
 * The Class RoundedCompartmentEditPart provides refresh method to apply notation properties specific to RoundedRectangleFigure.
 */
public abstract class PapyrusRoundedEditPartHelper implements NamedStyleProperties {

	/**
	 * Refresh border style.
	 *
	 * @param editpart
	 *            the editpart
	 * @param defaultLineCustomValue
	 *            the default line custom value
	 * @param defaultBorderStyle
	 */
	public static void refreshBorderStyle(IPapyrusEditPart editpart, int defaultBorderStyle, int[] defaultLineCustomValue) {
		// get the Figure
		if (editpart.getPrimaryShape() instanceof IRoundedRectangleFigure) {
			IRoundedRectangleFigure roundedRectangleFigure = (IRoundedRectangleFigure) editpart.getPrimaryShape();

			int borderStyle = getNotationBorderStyle(editpart, defaultBorderStyle);

			// set the border style of the figure
			roundedRectangleFigure.setBorderStyle(borderStyle);


			// get/set the custom dash value
			int[] customDash = NotationUtils.getIntListValue((View) ((GraphicalEditPart) editpart).getModel(), LINE_CUSTOM_VALUE, defaultLineCustomValue);
			roundedRectangleFigure.setCustomDash(customDash);

		}
	}

	/**
	 * Gets the Border style of the edit part defined in notation
	 * 
	 * @param editpart
	 * @param defaultBorderStyle
	 * @return the border style
	 */
	public static int getNotationBorderStyle(IPapyrusEditPart editpart, int defaultBorderStyle) {
		EClass stringValueStyle = NotationPackage.eINSTANCE.getStringValueStyle();

		int borderStyle = defaultBorderStyle;

		if (stringValueStyle != null) {

			if (((GraphicalEditPart) editpart).getModel() instanceof View) {
				// Get the border style on notation
				NamedStyle borderValueStyle = ((View) ((GraphicalEditPart) editpart).getModel()).getNamedStyle(stringValueStyle, BORDER_STYLE);
				// convert the string style name in integer
				if (borderValueStyle instanceof StringValueStyle) {
					String str = ((StringValueStyle) borderValueStyle).getStringValue();
					final LineStyleEnum lineStyle = LineStyleEnum.getByLiteral(str);
					if (lineStyle != null) {
						borderStyle = lineStyle.getLineStyle();
					}
				}
			}
		}
		return borderStyle;
	}

	/**
	 * Refresh floating name.
	 *
	 * @param editpart
	 *            the editpart
	 * @param defaultIsFloatingNameConstrained
	 *            the default is floating name constrained
	 * @param defaultFloatingLabelOffsetWidth
	 *            the default floating label offset width
	 * @param defaultFloatingLabelOffsetHeight
	 *            the default floating label offset height
	 */
	public static void refreshFloatingName(IPapyrusEditPart editpart, boolean defaultIsFloatingNameConstrained, int defaultFloatingLabelOffsetWidth, int defaultFloatingLabelOffsetHeight) {
		if (editpart.getPrimaryShape() instanceof IRoundedRectangleFigure) {
			// The figure
			IRoundedRectangleFigure roundedRectangleFigure = (IRoundedRectangleFigure) editpart.getPrimaryShape();

			if (((GraphicalEditPart) editpart).getModel() instanceof View) {
				// Get CSS value for the name attached properties
				boolean isNameConstrained = NotationUtils.getBooleanValue((View) ((GraphicalEditPart) editpart).getModel(), FLOATING_LABEL_CONSTRAINED, defaultIsFloatingNameConstrained);

				// get CSS the value of offset width and height
				int width = NotationUtils.getIntValue((View) ((GraphicalEditPart) editpart).getModel(), FLOATING_LABEL_OFFSET_WIDTH, defaultFloatingLabelOffsetWidth);
				int height = NotationUtils.getIntValue((View) ((GraphicalEditPart) editpart).getModel(), FLOATING_LABEL_OFFSET_HEIGHT, defaultFloatingLabelOffsetHeight);

				// Set the name attached properties in figure
				roundedRectangleFigure.setFloatingNameConstrained(isNameConstrained);

				// Set the floating name offset
				roundedRectangleFigure.setFloatingNameOffset(new Dimension(width, height));
			}
		}
	}

	/**
	 * Refresh oval.
	 *
	 * @param editpart
	 *            the editpart
	 * @param defaultIsOvalValue
	 *            the default is oval value
	 */
	public static void refreshOval(IPapyrusEditPart editpart, boolean defaultIsOvalValue) {
		if (editpart.getPrimaryShape() instanceof IRoundedRectangleFigure) {
			if (((GraphicalEditPart) editpart).getModel() instanceof View) {
				// The figure
				IRoundedRectangleFigure roundedRectangleFigure = (IRoundedRectangleFigure) editpart.getPrimaryShape();

				// get the CSS value of isOval
				boolean isOval = NotationUtils.getBooleanValue((View) ((GraphicalEditPart) editpart).getModel(), IS_OVAL, defaultIsOvalValue);

				// Set isOval
				roundedRectangleFigure.setOval(isOval);
			}
		}
	}

	/**
	 * Refresh package.
	 *
	 * @param editpart
	 *            the editpart
	 * @param defaultIsPackageValue
	 *            the default is package value
	 */
	public static void refreshPackage(IPapyrusEditPart editpart, boolean defaultIsPackageValue) {
		if (editpart.getPrimaryShape() instanceof IRoundedRectangleFigure) {
			if (((GraphicalEditPart) editpart).getModel() instanceof View) {
				// The figure
				IRoundedRectangleFigure roundedRectangleFigure = (IRoundedRectangleFigure) editpart.getPrimaryShape();

				// get the CSS value of isPackage
				boolean isPackage = NotationUtils.getBooleanValue((View) ((GraphicalEditPart) editpart).getModel(), IS_PACKAGE, defaultIsPackageValue);

				// Set isPackage
				roundedRectangleFigure.setIsPackage(isPackage);
			}
		}
	}

	/**
	 * Refresh shadow width.
	 *
	 * @param editpart
	 *            the editpart
	 * @param defaultShadowWidthValue
	 *            the default shadow width value
	 */
	public static void refreshShadowWidth(IPapyrusEditPart editpart, int defaultShadowWidthValue) {
		if (editpart.getPrimaryShape() instanceof IRoundedRectangleFigure) {
			if (((GraphicalEditPart) editpart).getModel() instanceof View) {
				// The figure
				IRoundedRectangleFigure roundedRectangleFigure = (IRoundedRectangleFigure) editpart.getPrimaryShape();

				// get the CSS value of isOval
				int shadowWidth = NotationUtils.getIntValue((View) ((GraphicalEditPart) editpart).getModel(), SHADOW_WIDTH, defaultShadowWidthValue);

				// Set isOval
				roundedRectangleFigure.setShadowWidth(shadowWidth >= 0 ? shadowWidth : defaultShadowWidthValue);
			}
		}
	}

	/**
	 * Refresh radius.
	 *
	 * @param editpart
	 *            the editpart
	 * @param defaultCornerWidth
	 *            the default corner width
	 * @param defaultCornerHeight
	 *            the default corner height
	 */
	public static void refreshRadius(IPapyrusEditPart editpart, int defaultCornerWidth, int defaultCornerHeight) {
		if (editpart.getPrimaryShape() instanceof IRoundedRectangleFigure) {
			// The figure
			IRoundedRectangleFigure roundedRectangleFigure = (IRoundedRectangleFigure) editpart.getPrimaryShape();

			if (((GraphicalEditPart) editpart).getModel() instanceof View) {
				// get CSS the value of radius Width
				int width = NotationUtils.getIntValue((View) ((GraphicalEditPart) editpart).getModel(), RADIUS_WIDTH, defaultCornerWidth);

				// get CSS the value of radius Height
				int height = NotationUtils.getIntValue((View) ((GraphicalEditPart) editpart).getModel(), RADIUS_HEIGHT, defaultCornerHeight);

				roundedRectangleFigure.setCornerDimensions(new Dimension(width, height));
			}
		}
	}



	/**
	 * Refresh has header.
	 *
	 * @param editpart
	 *            the editpart
	 * @param defaultHasHeader
	 *            the default has header
	 */
	public static void refreshHasHeader(IPapyrusEditPart editpart, boolean defaultHasHeader) {
		if (editpart.getPrimaryShape() instanceof IRoundedRectangleFigure) {
			if (((GraphicalEditPart) editpart).getModel() instanceof View) {
				// The figure
				IRoundedRectangleFigure roundedRectangleFigure = (IRoundedRectangleFigure) editpart.getPrimaryShape();

				// get the CSS value of hasHeader
				boolean hasHeader = NotationUtils.getBooleanValue((View) ((GraphicalEditPart) editpart).getModel(), DISPLAY_HEADER, defaultHasHeader);

				// Set hasHeader
				roundedRectangleFigure.setHasHeader(hasHeader);
			}
		}

	}

	/**
	 * Refresh the shadow color.
	 *
	 * @param editpart
	 *            the editpart
	 * @param defaultShadowColor
	 *            the default shadow color
	 */
	public static void refreshShadowColor(IPapyrusEditPart editpart, String defaultShadowColor) {
		if (editpart.getPrimaryShape() instanceof IRoundedRectangleFigure) {
			if (((GraphicalEditPart) editpart).getModel() instanceof View) {
				// The figure
				IRoundedRectangleFigure roundedRectangleFigure = (IRoundedRectangleFigure) editpart.getPrimaryShape();

				// get the CSS value of hasHeader
				String shadowColor = NotationUtils.getStringValue((View) ((GraphicalEditPart) editpart).getModel(), SHADOW_COLOR, defaultShadowColor);
				// Set color of the shadow
				roundedRectangleFigure.setShadowColor(shadowColor);
			}
		}
	}

	/**
	 * Refresh the Name Label background shadow color.
	 *
	 * @param editpart
	 *            the editpart
	 * @param defaultLabelColor
	 *            the default shadow color
	 */
	public static void refreshNameLabelColor(final IPapyrusEditPart editpart, final String defaultLabelColor) {
		if (editpart.getPrimaryShape() instanceof IRoundedRectangleFigure) {
			if (((GraphicalEditPart) editpart).getModel() instanceof View) {
				// The figure
				IRoundedRectangleFigure roundedRectangleFigure = (IRoundedRectangleFigure) editpart.getPrimaryShape();

				String labelColor = NotationUtils.getStringValue((View) ((GraphicalEditPart) editpart).getModel(), NAME_BACKGROUND_COLOR, defaultLabelColor);

				// Set color of the Name Label background
				roundedRectangleFigure.setNameBackgroundColor(labelColor);
			}
		}

	}
}
