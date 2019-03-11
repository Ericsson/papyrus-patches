/*****************************************************************************
 * Copyright (c) 2012, 2017 CEA LIST, ALL4TEC and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Camille Letavernier (CEA LIST) camille.letavernier@cea.fr - Initial API and implementation
 *  Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 515661, 522305
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpart;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.RotatableDecoration;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.gmf.runtime.notation.Connector;
import org.eclipse.gmf.runtime.notation.IntValueStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.StringValueStyle;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.databinding.custom.CustomStringStyleObservableValue;
import org.eclipse.papyrus.infra.gmfdiag.common.decoration.ConnectionDecorationRegistry;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.PapyrusConnectionEndEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.edge.PapyrusEdgeFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.NamedStyleProperties;
import org.eclipse.papyrus.infra.gmfdiag.tooling.runtime.linklf.editparts.LinkLFConnectionNodeEditPart;
import org.eclipse.papyrus.infra.tools.util.ClassLoaderHelper;


/**
 * Abstract edit part for all connection nodes.
 */
public abstract class ConnectionEditPart extends LinkLFConnectionNodeEditPart implements IPapyrusEditPart {

	/**
	 * CSS property for the line style
	 */
	protected static final String LINE_STYLE = "lineStyle"; //$NON-NLS-1$

	/**
	 * Supported values of the CSS property lineStyle
	 */
	protected static final String[] LINE_STYLE_VALUES = { "none", "hidden", "dotted", "dashed", "solid", "double" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

	/**
	 * CSS property for the line dashes' length
	 */
	protected static final String LINE_DASH_LENGTH = "lineDashLength"; //$NON-NLS-1$

	/**
	 * CSS property for the length between line dashes
	 */
	protected static final String LINE_DASH_GAP = "lineDashGap"; //$NON-NLS-1$

	/**
	 * CSS property for the source decoration
	 *
	 * @since 3.1
	 */
	public static final String SOURCE_DECORATION = "sourceDecoration"; //$NON-NLS-1$

	/**
	 * CSS property for the target decoration
	 *
	 * @since 3.1
	 */
	public static final String TARGET_DECORATION = "targetDecoration"; //$NON-NLS-1$

	/**
	 * Supported values of the CSS property targetDecoration
	 *
	 * @since 3.1
	 */
	public static final String[] DECORATION_VALUES = { "default", "none" }; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Minimum length of dashes for dashed connectors
	 */
	protected static final int LINE_DASH_MIN_LENGTH = 2;

	/**
	 * Minimum length of the gaps between dashes
	 */
	protected static final int LINE_GAP_MIN_LENGTH = 2;

	/** The source decoration Observable. */
	private IObservableValue sourceDecorationObservable;

	/** The target decoration Observable. */
	private IObservableValue targetDecorationObservable;

	/**
	 * The namedStyle Listener. Refresh the edit part when handle a change.
	 *
	 * @since 3.1
	 */
	private IChangeListener namedStyleListener = new IChangeListener() {

		@Override
		public void handleChange(ChangeEvent event) {
			refresh();
		}

	};

	/**
	 * Constructor.
	 */
	public ConnectionEditPart(View view) {
		super(view);
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart#refresh()
	 */
	@Override
	public void refresh() {
		super.refresh();
		IFigure figure = this.getFigure();
		Object model = this.getModel();
		if (figure instanceof PapyrusEdgeFigure && model instanceof Connector) {
			Connector connector = (Connector) model;
			PapyrusEdgeFigure edge = (PapyrusEdgeFigure) figure;
			// Reset the style
			edge.resetStyle();
			// Re-apply the CSS-defined style if any
			String lineStyle = extract((StringValueStyle) connector.getNamedStyle(NotationPackage.eINSTANCE.getStringValueStyle(), LINE_STYLE));
			int lineDashLength = extract((IntValueStyle) connector.getNamedStyle(NotationPackage.eINSTANCE.getIntValueStyle(), LINE_DASH_LENGTH));
			int lineDashGap = extract((IntValueStyle) connector.getNamedStyle(NotationPackage.eINSTANCE.getIntValueStyle(), LINE_DASH_GAP));
			if (lineStyle != null) {
				setupLineStyle(edge, lineStyle, connector.getLineWidth(), lineDashLength < LINE_DASH_MIN_LENGTH ? LINE_DASH_MIN_LENGTH : lineDashLength, lineDashGap < LINE_GAP_MIN_LENGTH ? LINE_GAP_MIN_LENGTH : lineDashGap);
			}

			refreshConnectionDecoration(connector, edge);
		}
	}

	/**
	 * Refresh the connection arrow decoration.
	 *
	 * @param connector
	 *            The notation connector
	 * @param edge
	 *            the edge figure
	 */
	private void refreshConnectionDecoration(final Connector connector, final PapyrusEdgeFigure edge) {

		// source refresh
		String sourceDecoration = extract((StringValueStyle) connector.getNamedStyle(NotationPackage.eINSTANCE.getStringValueStyle(), SOURCE_DECORATION));
		if (null != sourceDecoration) {// not null
			edge.setSourceDecoration(getConnectionDecoration(sourceDecoration));
		}

		// target refresh
		String targetDecoration = extract((StringValueStyle) connector.getNamedStyle(NotationPackage.eINSTANCE.getStringValueStyle(), TARGET_DECORATION));
		if (null != targetDecoration) {// not null
			edge.setTargetDecoration(getConnectionDecoration(targetDecoration));
		}
	}

	/**
	 * Extracts the primitive value from the given style
	 *
	 * @param style
	 *            The style
	 * @return The primitive value
	 */
	private String extract(StringValueStyle style) {
		if (style == null || style.getStringValue() == null || style.getStringValue().isEmpty()) {
			return null;
		}
		return style.getStringValue();
	}

	/**
	 * Extracts the primitive value from the given style
	 *
	 * @param style
	 *            The style
	 * @return The primitive value
	 */
	private int extract(IntValueStyle style) {
		if (style == null) {
			return 0;
		}
		return style.getIntValue();
	}

	/**
	 * Setups the line style of the edge according to the given CSS style
	 *
	 * @param edge
	 *            The shape to setup
	 * @param style
	 *            The CSS style
	 * @param originalWidth
	 *            Original width of the connector
	 * @param lineDashLength
	 *            Length of the dashes
	 * @param lineDashGap
	 *            Length of the gap between dashes
	 */
	private void setupLineStyle(PapyrusEdgeFigure edge, String style, int originalWidth, int lineDashLength, int lineDashGap) {
		int width = originalWidth < 0 ? 1 : originalWidth;
		if ("hidden".equals(style)) {//$NON-NLS-1$
			edge.setLineStyle(Graphics.LINE_SOLID);
			edge.setLineWidth(0);
			edge.setVisible(false);
		} else if ("dotted".equals(style)) {//$NON-NLS-1$
			edge.setLineStyle(Graphics.LINE_DOT);
			edge.setLineWidth(width);
		} else if ("dashed".equals(style)) {//$NON-NLS-1$
			edge.setLineStyle(Graphics.LINE_CUSTOM);
			edge.setLineWidth(width);
			edge.setLineDash(new int[] { lineDashLength, lineDashGap });
		} else if ("solid".equals(style)) {//$NON-NLS-1$
			edge.setLineStyle(Graphics.LINE_SOLID);
			edge.setLineWidth(width);
		} else if ("double".equals(style)) {//$NON-NLS-1$
			edge.setLineWidth(width * 2);
		} else if ("dash".equals(style)) {//$NON-NLS-1$
			edge.setLineStyle(Graphics.LINE_DASH);
			edge.setLineWidth(width);
		}
	}

	/**
	 * Refresh the diagram when changing the label filters (Bug 491811: [CSS][Diagram] Connectors not refreshed after change of routing style (eg rectilinear->oblique))
	 *
	 * @since 2.0
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.LabelEditPart#handleNotificationEvent(org.eclipse.emf.common.notify.Notification)
	 *
	 * @param notification
	 */
	@Override
	protected void handleNotificationEvent(Notification notification) {
		super.handleNotificationEvent(notification);

		Object notifier = notification.getNotifier();
		Object oldValue = notification.getOldValue();
		// DO
		if (notifier instanceof EAnnotation) {
			if (((EAnnotation) notifier).getSource().equalsIgnoreCase(NamedStyleProperties.CSS_FORCE_VALUE)) {
				super.refresh();
			}
		}
		// UNDO
		else if (oldValue instanceof EAnnotation) {
			if (((EAnnotation) oldValue).getSource().equalsIgnoreCase(NamedStyleProperties.CSS_FORCE_VALUE)) {
				super.refresh();
			}
		}
	}

	/**
	 * Gets the connection decoration in {@link ConnectionDecorationRegistry} for a String.
	 *
	 * @param arrowType
	 *            the arrow type
	 * @return the {@link RotatableDecoration} use as connection decoration (null if not found)
	 * @since 3.1
	 */
	protected RotatableDecoration getConnectionDecoration(final String arrowType) {
		RotatableDecoration decoration = null;
		// Gets the decoration into the decoration registry
		Class<? extends RotatableDecoration> decorationClass = ConnectionDecorationRegistry.getInstance().getDecorationClass(arrowType);
		if (null != decorationClass) {
			// load the class
			decoration = ClassLoaderHelper.newInstance(decorationClass);
			// set the width
			if (decoration instanceof Polyline) {
				IMapMode mm = getMapMode();
				int width = getLineWidth();
				if (width < 0) {
					width = 1;
				}
				((Polyline) decoration).setLineWidth(mm.DPtoLP(width));
			}
		}

		return decoration;
	}

	/**
	 * Adds listener to handle named Style modifications.
	 */
	@Override
	protected void addNotationalListeners() {
		super.addNotationalListeners();

		View view = (View) getModel();
		EditingDomain domain = EMFHelper.resolveEditingDomain(view);

		sourceDecorationObservable = new CustomStringStyleObservableValue(view, domain, SOURCE_DECORATION);
		sourceDecorationObservable.addChangeListener(namedStyleListener);

		targetDecorationObservable = new CustomStringStyleObservableValue(view, domain, TARGET_DECORATION);
		targetDecorationObservable.addChangeListener(namedStyleListener);

	}

	/**
	 * Removes the notational listeners.
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#removeNotationalListeners()
	 */
	@Override
	protected void removeNotationalListeners() {
		super.removeNotationalListeners();
		sourceDecorationObservable.dispose();
		targetDecorationObservable.dispose();
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionNodeEditPart#createDefaultEditPolicies()
	 */
	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new PapyrusConnectionEndEditPolicy());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart#refreshVisuals()
	 */
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		refreshLineWidth();
		installRouter();
	}

}
