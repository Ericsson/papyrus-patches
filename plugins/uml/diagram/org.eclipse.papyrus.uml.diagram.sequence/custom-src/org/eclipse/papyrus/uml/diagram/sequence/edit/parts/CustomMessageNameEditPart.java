/*****************************************************************************
 * Copyright (c) 2010 CEA
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
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.notation.FontStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.extensionpoints.editors.Activator;
import org.eclipse.papyrus.extensionpoints.editors.utils.DirectEditorsUtil;
import org.eclipse.papyrus.extensionpoints.editors.utils.IDirectEditorsIds;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.DefaultSemanticEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.IMaskManagedLabelEditPolicy;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.IDirectEdition;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.MessageLabelEditPolicy.ICustomMessageLabel;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.UMLTextSelectionEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.util.SelfMessageHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class CustomMessageNameEditPart extends MessageSyncNameEditPart implements ICustomMessageLabel {

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public CustomMessageNameEditPart(View view) {
		super(view);
	}

	@Override
	protected void updateExtendedEditorConfiguration() {
		String languagePreferred = Activator.getDefault().getPreferenceStore().getString(
				IDirectEditorsIds.EDITOR_FOR_ELEMENT + resolveSemanticElement().eClass().getInstanceClassName());
		if (languagePreferred != null && !languagePreferred.equals("")
				&& (configuration == null || !languagePreferred.equals(configuration.getLanguage()))) {
			configuration = DirectEditorsUtil.findEditorConfiguration(languagePreferred, resolveSemanticElement(),
					this);
		} else if (IDirectEditorsIds.SIMPLE_DIRECT_EDITOR.equals(languagePreferred)) {
			configuration = null;
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageSyncNameEditPart#createDefaultEditPolicies()
	 */
	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new DefaultSemanticEditPolicy());
	}

	@Override
	public int getDirectEditionType() {
		return IDirectEdition.EXTENDED_DIRECT_EDITOR;
	}

	@Override
	public void refreshBounds() {
		super.refreshBounds();
		MessageSyncEditPart parent = (MessageSyncEditPart) getParent();
		// Update location of label for self linked message.
		if (SelfMessageHelper.isSelfLink(parent)) {
			SelfMessageHelper.updateLabelLocation(this);
		}
	}

	@Override
	protected void refreshLabel() {
		EditPolicy maskLabelPolicy = getParent().getEditPolicy(IMaskManagedLabelEditPolicy.MASK_MANAGED_LABEL_EDIT_POLICY);
		if (maskLabelPolicy == null) {
			setLabelTextHelper(getFigure(), getLabelText());
			setLabelIconHelper(getFigure(), getLabelIcon());
		}
		Object pdEditPolicy = getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
		if (pdEditPolicy instanceof UMLTextSelectionEditPolicy) {
			((UMLTextSelectionEditPolicy) pdEditPolicy).refreshFeedback();
		}
		Object sfEditPolicy = getEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE);
		if (sfEditPolicy instanceof UMLTextSelectionEditPolicy) {
			((UMLTextSelectionEditPolicy) sfEditPolicy).refreshFeedback();
		}
	}

	@Override
	public void refreshFont() {
		FontStyle style = (FontStyle) getFontStyleOwnerView().getStyle(NotationPackage.eINSTANCE.getFontStyle());
		if (style != null) {
			FontData fontData = new FontData(style.getFontName(), style.getFontHeight(), (style.isBold() ? SWT.BOLD : SWT.NORMAL) | (style.isItalic() ? SWT.ITALIC : SWT.NORMAL));
			setFont(fontData);
		}
	}

	private View getFontStyleOwnerView() {
		return (View) getParent().getModel();
	}
}
