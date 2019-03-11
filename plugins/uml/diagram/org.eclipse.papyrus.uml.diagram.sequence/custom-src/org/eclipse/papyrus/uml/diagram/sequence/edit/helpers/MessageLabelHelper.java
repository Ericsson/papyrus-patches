/*****************************************************************************
 * Copyright (c) 2016 CEA LIST.
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
 *   Nicolas FAUVERGUE (ALL4TEC) nicolas.fauvergue@all4tec.net - Bug 496905
 *
 *****************************************************************************/
 package org.eclipse.papyrus.uml.diagram.sequence.edit.helpers;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.LabelEditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.IMaskManagedLabelEditPolicy;
import org.eclipse.papyrus.infra.tools.util.StringHelper;
import org.eclipse.papyrus.uml.diagram.common.helper.StereotypedElementLabelHelper;
import org.eclipse.papyrus.uml.diagram.sequence.util.OperationUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.SignalUtil;
import org.eclipse.papyrus.uml.internationalization.utils.utils.UMLLabelInternationalization;
import org.eclipse.papyrus.uml.tools.utils.ICustomAppearance;
import org.eclipse.papyrus.uml.tools.utils.TypedElementUtil;
import org.eclipse.papyrus.uml.tools.utils.ValueSpecificationUtil;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.ValueSpecification;

public class MessageLabelHelper extends StereotypedElementLabelHelper {

	public static final Collection<String> DEFAULT_LABEL_DISPLAY = Arrays.asList(ICustomAppearance.DISP_NAME, ICustomAppearance.DISP_PARAMETER_NAME, ICustomAppearance.DISP_PARAMETER_TYPE, ICustomAppearance.DISP_RT_TYPE);

	/**
	 * singelton instance
	 */
	private static MessageLabelHelper labelHelper;

	/** Map for masks */
	protected final Map<String, String> masks = new HashMap<>();

	protected MessageLabelHelper() {
		// initialize the map
		masks.put(ICustomAppearance.DISP_VISIBILITY, "Visibility");
		masks.put(ICustomAppearance.DISP_NAME, "Name");
		masks.put(ICustomAppearance.DISP_PARAMETER_NAME, "Parameters Name");
		masks.put(ICustomAppearance.DISP_PARAMETER_DIRECTION, "Parameters Direction");
		masks.put(ICustomAppearance.DISP_PARAMETER_TYPE, "Parameters Type");
		masks.put(ICustomAppearance.DISP_RT_TYPE, "Return Type");
		masks.put(ICustomAppearance.DISP_PARAMETER_MULTIPLICITY, "Parameters Multiplicity");
		masks.put(ICustomAppearance.DISP_PARAMETER_DEFAULT, "Parameters Default Value");
		masks.put(ICustomAppearance.DISP_DERIVE, "Parameters Value");
		masks.put(ICustomAppearance.DISP_PARAMETER_MODIFIERS, "Parameters Modifiers");
		masks.put(ICustomAppearance.DISP_MODIFIERS, "Modifiers");
	}

	/**
	 * Returns the singleton instance of this class
	 *
	 * @return the singleton instance.
	 */
	public static MessageLabelHelper getInstance() {
		if (labelHelper == null) {
			labelHelper = new MessageLabelHelper();
		}
		return labelHelper;
	}

	@Override
	public Message getUMLElement(GraphicalEditPart editPart) {
		EObject e = ((View) editPart.getModel()).getElement();
		if (e instanceof Message) {
			return ((Message) e);
		}
		return null;
	}

	@Override
	protected String elementLabel(GraphicalEditPart editPart) {
		if (editPart instanceof LabelEditPart) {
			editPart = (GraphicalEditPart) editPart.getParent();
		}
		Collection<String> displayValue = DEFAULT_LABEL_DISPLAY;
		IMaskManagedLabelEditPolicy policy = (IMaskManagedLabelEditPolicy) editPart.getEditPolicy(IMaskManagedLabelEditPolicy.MASK_MANAGED_LABEL_EDIT_POLICY);
		if (policy != null) {
			displayValue = policy.getCurrentDisplayValue();
		}
		Message e = getUMLElement(editPart);
		if (e == null) {
			return null;
		}
		NamedElement signature = e.getSignature();
		if (signature instanceof Operation) {
			return OperationUtil.getCustomLabel(e, (Operation) signature, displayValue);
		} else if (signature instanceof Signal) {
			return SignalUtil.getCustomLabel(e, (Signal) signature, displayValue);
		} else if (signature != null) {
			return UMLLabelInternationalization.getInstance().getLabel(signature);
		}
		// signature is null
		return getMessageLabel(e, displayValue);
	}

	private String getMessageLabel(Message message, Collection<String> displayValue) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(UMLLabelInternationalization.getInstance().getLabel(message));
		// parameters : '(' parameter-list ')'
		EList<ValueSpecification> arguments = message.getArguments();
		if (arguments.size() > 0 && (displayValue.contains(ICustomAppearance.DISP_PARAMETER_NAME) || displayValue.contains(ICustomAppearance.DISP_DERIVE))) {
			buffer.append("(");
			for (int i = 0; i < arguments.size(); i++) {
				if (i > 0) {
					buffer.append(", ");
				}
				ValueSpecification arg = arguments.get(i);
				// type
				if (displayValue.contains(ICustomAppearance.DISP_PARAMETER_TYPE)) {
					String type = TypedElementUtil.getTypeAsString(arg);
					if (type != null) {
						buffer.append(type);
					}
				}
				boolean showEqualMark = false;
				// name
				if (displayValue.contains(ICustomAppearance.DISP_PARAMETER_NAME)) {
					buffer.append(" ");
					String name = StringHelper.trimToEmpty(UMLLabelInternationalization.getInstance().getLabel(arg));
					buffer.append(name);
					if (name.trim().length() > 0) {
						showEqualMark = true;
					}
				}
				// value
				if (displayValue.contains(ICustomAppearance.DISP_DERIVE)) {
					String value = ValueSpecificationUtil.getSpecificationValue(arg, true);
					if (value != null) {
						if (showEqualMark) {
							buffer.append(" = ");
						}
						buffer.append(value);
					}
				}
			}
			buffer.append(")");
		}
		return buffer.toString();
	}

	public Map<String, String> getMasks() {
		return masks;
	}

	public Collection<String> getDefaultValue() {
		return DEFAULT_LABEL_DISPLAY;
	}
}
