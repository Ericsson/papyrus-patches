/*****************************************************************************
 * Copyright (c) 2017 CEA LIST, ALL4TEC and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpart;

import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramColorRegistry;
import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
import org.eclipse.gmf.runtime.notation.FontStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.IMaskManagedLabelEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.IndirectMaskLabelEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.PapyrusLinkLabelDragPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.TextSelectionEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;

/**
 * {@link PapyrusLabelEditPart} for the Reference edge.
 * 
 * @since 3.1
 */
public class ReferenceEdgeNameEditPart extends PapyrusLabelEditPart {

	/** The Constant FEATURE_TO_SET_KEY. */
	private static final String FEATURE_TO_SET_KEY = "featureToSet"; //$NON-NLS-1$

	/** The Constant EDGE_LABEL_KEY. */
	private static final String EDGE_LABEL_KEY = "edgeLabel"; //$NON-NLS-1$

	/** The Constant STEREOTYPE_PROPERTY_REFERENCE_EDGE_KEY. */
	private static final String STEREOTYPE_PROPERTY_REFERENCE_EDGE_KEY = "StereotypePropertyReferenceEdge"; //$NON-NLS-1$

	/** The Constant CSS_LABEL_VALUE_ATTRIBUTE_KEY. */
	public static final String CSS_LABEL_VALUE_ATTRIBUTE_KEY = "label"; //$NON-NLS-1$

	/**
	 * The Visual Id.
	 */
	public static final String VISUAL_ID = "StereotypePropertyReferenceEdgeName"; //$NON-NLS-1$

	/**
	 * The default text.
	 */
	private String defaultText;


	/**
	 * Register the snap back position;
	 */
	static {
		registerSnapBackPosition(ReferenceEdgeNameEditPart.VISUAL_ID, new Point(0, 60));
	}

	/**
	 * Constructor.
	 *
	 * @param view
	 *            The view.
	 */
	public ReferenceEdgeNameEditPart(final View view) {
		super(view);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpart.PapyrusLabelEditPart#createDefaultEditPolicies()
	 */
	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new TextSelectionEditPolicy());
		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new PapyrusLinkLabelDragPolicy());
	}


	/**
	 * Gets the label text helper.
	 * 
	 * @param figure
	 *            the figure
	 * @return the label text helper
	 */
	protected String getLabelTextHelper(final IFigure figure) {
		if (figure instanceof WrappingLabel) {
			return ((WrappingLabel) figure).getText();
		} else {
			return ((Label) figure).getText();
		}
	}


	/**
	 * Sets the label text helper.
	 *
	 * @param figure
	 *            the figure
	 * @param text
	 *            the text
	 */
	protected void setLabelTextHelper(final IFigure figure, final String text) {
		if (figure instanceof WrappingLabel) {
			((WrappingLabel) figure).setText(text);
		} else {
			((Label) figure).setText(text);
		}
	}

	/**
	 * Gets the label icon helper.
	 *
	 * @param figure
	 *            the figure
	 * @return the label icon helper
	 */
	protected Image getLabelIconHelper(final IFigure figure) {
		if (figure instanceof WrappingLabel) {
			return ((WrappingLabel) figure).getIcon();
		} else {
			return ((Label) figure).getIcon();
		}
	}

	/**
	 * Sets the label icon helper.
	 *
	 * @param figure
	 *            the figure
	 * @param icon
	 *            the icon
	 */
	protected void setLabelIconHelper(IFigure figure, Image icon) {
		if (figure instanceof WrappingLabel) {
			((WrappingLabel) figure).setIcon(icon);
		} else {
			((Label) figure).setIcon(icon);
		}
	}

	/**
	 * Set the label {@link IFigure}.
	 *
	 * @param figure
	 *            the new label
	 */
	public void setLabel(IFigure figure) {
		unregisterVisuals();
		setFigure(figure);
		defaultText = getLabelTextHelper(figure);
		registerVisuals();
		refreshVisuals();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#getModelChildren()
	 */
	protected List<?> getModelChildren() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#getChildBySemanticHint(java.lang.String)
	 */
	public IGraphicalEditPart getChildBySemanticHint(String semanticHint) {
		return null;
	}


	/**
	 * Gets the label icon.
	 *
	 * @return the label icon
	 */
	protected Image getLabelIcon() {
		return null;
	}

	/**
	 * Gets the label text.
	 *
	 * @return the label text
	 */
	protected String getLabelText() {
		String text = null;
		// Get Text from Notation/CSS
		if (text == null || text.length() == 0) {
			text = NotationUtils.getStringValue((View) getParent().getModel(), CSS_LABEL_VALUE_ATTRIBUTE_KEY, null);
		}
		if (text == null || text.length() == 0) {
			EAnnotation eAnnotation = ((View) getParent().getModel()).getEAnnotation(STEREOTYPE_PROPERTY_REFERENCE_EDGE_KEY);
			if (eAnnotation != null) {
				// Get Text from Advice link label value
				text = eAnnotation.getDetails().get(EDGE_LABEL_KEY);
				if (text == null || text.length() == 0) {
					// Get Text from feature name
					text = eAnnotation.getDetails().get(FEATURE_TO_SET_KEY);
				}
			}
		}

		if (text == null || text.length() == 0) {
			text = defaultText;
		}
		return text;
	}

	/**
	 * Sets the label text.
	 *
	 * @param text
	 *            the new label text
	 */
	public void setLabelText(final String text) {
		setLabelTextHelper(getFigure(), text);
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpart.PapyrusLabelEditPart#refreshVisuals()
	 *
	 */
	protected void refreshVisuals() {
		super.refreshVisuals();
		refreshLabel();
		refreshFont();
		refreshFontColor();
		refreshUnderline();
		refreshStrikeThrough();
	}

	/**
	 * Refresh label.
	 */
	protected void refreshLabel() {

		EditPolicy maskLabelPolicy = getEditPolicy(IMaskManagedLabelEditPolicy.MASK_MANAGED_LABEL_EDIT_POLICY);
		if (maskLabelPolicy == null) {
			maskLabelPolicy = getEditPolicy(IndirectMaskLabelEditPolicy.INDRIRECT_MASK_MANAGED_LABEL);
		}
		if (maskLabelPolicy == null) {
			setLabelTextHelper(getFigure(), getLabelText());
			setLabelIconHelper(getFigure(), getLabelIcon());
		}
		Object pdEditPolicy = getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
		if (pdEditPolicy instanceof TextSelectionEditPolicy) {
			((TextSelectionEditPolicy) pdEditPolicy).refreshFeedback();
		}
		Object sfEditPolicy = getEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE);
		if (sfEditPolicy instanceof TextSelectionEditPolicy) {
			((TextSelectionEditPolicy) sfEditPolicy).refreshFeedback();
		}
	}

	/**
	 * Refresh underline.
	 */
	protected void refreshUnderline() {
		FontStyle style = (FontStyle) getFontStyleOwnerView().getStyle(NotationPackage.eINSTANCE.getFontStyle());
		if (style != null && getFigure() instanceof WrappingLabel) {
			((WrappingLabel) getFigure()).setTextUnderline(style.isUnderline());
		}
	}

	/**
	 * Refresh strike through.
	 */
	protected void refreshStrikeThrough() {
		FontStyle style = (FontStyle) getFontStyleOwnerView().getStyle(NotationPackage.eINSTANCE.getFontStyle());
		if (style != null && getFigure() instanceof WrappingLabel) {
			((WrappingLabel) getFigure()).setTextStrikeThrough(style.isStrikeThrough());
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#refreshFont()
	 */
	protected void refreshFont() {
		FontStyle style = (FontStyle) getFontStyleOwnerView().getStyle(NotationPackage.eINSTANCE.getFontStyle());
		if (style != null) {
			FontData fontData = new FontData(style.getFontName(), style.getFontHeight(),
					(style.isBold() ? SWT.BOLD : SWT.NORMAL) | (style.isItalic() ? SWT.ITALIC : SWT.NORMAL));
			setFont(fontData);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#setFontColor(org.eclipse.swt.graphics.Color)
	 */
	protected void setFontColor(final Color color) {
		getFigure().setForegroundColor(color);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#addSemanticListeners()
	 */
	protected void addSemanticListeners() {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#removeSemanticListeners()
	 */
	protected void removeSemanticListeners() {
		// Do nothing
	}

	/**
	 * Get the Font Style Owner View.
	 *
	 * @return the font style owner view
	 */
	private View getFontStyleOwnerView() {
		return getPrimaryView();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.editpart.PapyrusLabelEditPart#handleNotificationEvent(org.eclipse.emf.common.notify.Notification)
	 */
	protected void handleNotificationEvent(final Notification event) {
		Object feature = event.getFeature();
		if (NotationPackage.eINSTANCE.getFontStyle_FontColor().equals(feature)) {
			Integer c = (Integer) event.getNewValue();
			setFontColor(DiagramColorRegistry.getInstance().getColor(c));
		} else if (NotationPackage.eINSTANCE.getFontStyle_Underline().equals(feature)) {
			refreshUnderline();
		} else if (NotationPackage.eINSTANCE.getFontStyle_StrikeThrough().equals(feature)) {
			refreshStrikeThrough();
		} else if (NotationPackage.eINSTANCE.getFontStyle_FontHeight().equals(feature)
				|| NotationPackage.eINSTANCE.getFontStyle_FontName().equals(feature)
				|| NotationPackage.eINSTANCE.getFontStyle_Bold().equals(feature)
				|| NotationPackage.eINSTANCE.getFontStyle_Italic().equals(feature)) {
			refreshFont();
		}
		super.handleNotificationEvent(event);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.LabelEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		// Parent should assign one using setLabel() method
		return null;
	}
}
