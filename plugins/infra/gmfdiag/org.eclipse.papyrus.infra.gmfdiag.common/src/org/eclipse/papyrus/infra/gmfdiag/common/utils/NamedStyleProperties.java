/*****************************************************************************
 * Copyright (c) 2010, 2018 CEA LIST, EclipseSource and others.
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
 *   EclipseSource - Bug 535519
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.utils;

import org.eclipse.papyrus.infra.gmfdiag.common.providers.StyleBasedShapeProvider;

/**
 * Contains constants for specific NamedStyle, used to customize figure.
 */
public interface NamedStyleProperties {

	/* NamedStyle used on PapyrusRoundedEditPartHelper for generic figure */

	/**
	 * The NamedStyle property controlling whether the header have to be display or not.
	 */
	public static final String DISPLAY_HEADER = "displayHeader"; //$NON-NLS-1$

	/**
	 * The NamedStyle property controlling whether the svg picture use the original color or the color can be chosen by the user.
	 */
	public static final String USE_ORIGINAL_COLORS = "useOriginalColors"; //$NON-NLS-1$

	/**
	 * The NamedStyle property to select the border style of the figure.
	 */
	public static final String BORDER_STYLE = "borderStyle"; //$NON-NLS-1$

	/**
	 * The NamedStyle property controlling whether the floating label is constrained or not.
	 */
	public static final String FLOATING_LABEL_CONSTRAINED = "isFloatingLabelConstrained";//$NON-NLS-1$

	/**
	 * The NamedStyle property to select the offset height of the floating label when it is not constrained.
	 */
	public static final String FLOATING_LABEL_OFFSET_HEIGHT = "floatingLabelOffsetHeight"; //$NON-NLS-1$

	/**
	 * The NamedStyle property to select the offset width of the floating label when it is not constrained.
	 */
	public static final String FLOATING_LABEL_OFFSET_WIDTH = "floatingLabelOffsetWidth"; //$NON-NLS-1$

	/**
	 * The NamedStyle property controlling whether the figure will be oval or not.
	 */
	public static final String IS_OVAL = "isOval"; //$NON-NLS-1$

	/**
	 * The NamedStyle property to select the offset height of the corner radius.
	 */
	public static final String RADIUS_HEIGHT = "radiusHeight";//$NON-NLS-1$

	/**
	 * The NamedStyle property to select the offset width of the corner radius.
	 */
	public static final String RADIUS_WIDTH = "radiusWidth"; //$NON-NLS-1$

	/**
	 * The NamedStyle property to select the custom dash when the style of the line is set to custom.
	 */
	public static final String LINE_CUSTOM_VALUE = "customDash"; //$NON-NLS-1$

	/**
	 * The NamedStyle property to set the shadow width.
	 */
	public static final String SHADOW_WIDTH = "shadowWidth"; //$NON-NLS-1$

	/**
	 * The NamedStyle property to set the shadow color.
	 */
	public static final String SHADOW_COLOR = "shadowColor"; //$NON-NLS-1$

	/**
	 * The NamedStyle property to set the nameLabel background color.
	 */
	public static final String NAME_BACKGROUND_COLOR = "nameBackgroundColor"; //$NON-NLS-1$

	/**
	 * The NamedStyle property to define if the figure must be draw as a package.
	 */
	public static final String IS_PACKAGE = "isPackage"; //$NON-NLS-1$

	/* NamedStyle use on PapyrusLabelEditPart */

	/**
	 * NamedStyle property to define the horizontal Label Margin
	 */
	public static final String TOP_MARGIN_PROPERTY = "topMarginLabel"; //$NON-NLS-1$

	/**
	 * NamedStyle property to define the vertical Label Margin
	 */
	public static final String LEFT_MARGIN_PROPERTY = "leftMarginLabel"; //$NON-NLS-1$

	/**
	 * NamedStyle property to define the horizontal Label Margin
	 */
	public static final String BOTTOM_MARGIN_PROPERTY = "bottomMarginLabel"; //$NON-NLS-1$

	/**
	 * NamedStyle property to define the vertical Label Margin
	 */
	public static final String RIGHT_MARGIN_PROPERTY = "rightMarginLabel"; //$NON-NLS-1$

	/** The NamedStyle property to define the text alignment. */
	public static final String TEXT_ALIGNMENT = "textAlignment"; //$NON-NLS-1$

	/** The NamedStyle property to define the text wrap. */
	public static final String WRAP_NAME = "isNameWrap"; //$NON-NLS-1$

	/** NamedStyle property to define the label offset on Y. */
	public static final String LABEL_OFFSET_Y = "labelOffsetY"; //$NON-NLS-1$

	/** NamedStyle property to define the label offset on X. */
	public static final String LABEL_OFFSET_X = "labelOffsetX"; //$NON-NLS-1$

	/** NamedStyle property controlling whether the label is constrained or not. */
	public static final String LABEL_CONSTRAINED = "labelConstrained"; //$NON-NLS-1$

	/* NamedStyle used on floatingLabelEditPart */

	/** NamedStyle property to display floating name. */
	public static final String DISPLAY_FLOATING_LABEL = "visible"; //$NON-NLS-1$

	/** NamedStyle property to define the position for External Label */
	public static final String POSITION = "position"; //$NON-NLS-1$

	/* NamedStyle properties used for border display. */

	/** The notation NameStyle property to define line position from compartment. */
	public static final String LINE_POSITION = "linePosition"; //$NON-NLS-1$

	/** The notation NameStyle property to define the line length ratio of compartment's topLine. */
	public static final String LINE_LENGTH_RATIO = "lineLengthRatio"; //$NON-NLS-1$

	/** The notation NameStyle property to define the line length of compartment's topLine. */
	public static final String LINE_LENGTH = "lineLength"; //$NON-NLS-1$

	/** The notation NameStyle property to display. */
	public static final String DISPLAY_BORDER = "displayBorder"; //$NON-NLS-1$

	/* NamedStyle for NamedElementEditPart */

	/** CSS boolean property controlling whether stereotypes should be displayed. */
	public static final String DISPLAY_STEREOTYPES = "displayStereotypes"; //$NON-NLS-1$

	/** CSS boolean property controlling whether tags should be displayed. */
	public static final String DISPLAY_TAGS = "displayTags"; //$NON-NLS-1$

	/* NamedStyle for affixed node */
	/** The port position namedStyle property */
	public static final String PORT_POSITION = "portPosition"; //$NON-NLS-1$

	/** The port resizable namestyle propery. */
	public static final String IS_PORT_RESIZABLE = "isPortResizable";//$NON-NLS-1$

	/* Label used to identify the forced CSS values */
	public static final String CSS_FORCE_VALUE = "PapyrusCSSForceValue"; // $NON-NLS-1$

	/** name of the CSS property that manages the enablement of the {@link StyleBasedShapeProvider} */
	public static final String SHAPE_STYLE_PROPERTY = "shapeStyle"; //$NON-NLS-1$

	/** name of the CSS property that manages the enablement of the {@link StyleBasedShapeProvider} for decoration */
	public static final String SHAPE_DECORATION_STYLE_PROPERTY = "shapeDecorationStyle"; //$NON-NLS-1$

	/** NameStyle property to show an icon next to the label of an element.
	 * @since 3.0*/
	public static final String ELEMENT_ICON = "elementIcon"; //$NON-NLS-1$

	/** NameStyle property to define the depth of the qualified name to display.
	 * @since 3.0*/
	public static final String QUALIFIED_NAME_DEPTH = "qualifiedNameDepth"; //$NON-NLS-1$

	/** NameStyle property to display a shadow under the shape.
	 * @since 3.0*/
	public static final String SHADOW = "shadow"; //$NON-NLS-1$

	/** NamedStyle property used to show an image on the symbol compartment.
	 * @since 3.0*/
	public static final String IMAGE_PATH = "imagePath"; //$NON-NLS-1$

	/**
	 * NamedStyle property used to set a fixed height for a Label, in pixels
	 * @since 3.101
	 */
	public static final String LABEL_HEIGHT = "labelHeight"; //$NON-NLS-1$

	/**
	 * NamedStyle property used to set a fixed width for a Label, in pixels
	 * @since 3.101
	 */
	public static final String LABEL_WIDTH = "labelWidth"; //$NON-NLS-1$

}
