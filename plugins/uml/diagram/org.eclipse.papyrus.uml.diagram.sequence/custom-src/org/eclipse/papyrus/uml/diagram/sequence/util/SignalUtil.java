/*****************************************************************************
 * Copyright (c) 2016 CEA LIST and others.
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
package org.eclipse.papyrus.uml.diagram.sequence.util;

import java.util.Collection;

import org.eclipse.emf.common.util.EList;
import org.eclipse.papyrus.infra.tools.util.StringHelper;
import org.eclipse.papyrus.uml.internationalization.utils.utils.UMLLabelInternationalization;
import org.eclipse.papyrus.uml.tools.utils.ICustomAppearance;
import org.eclipse.papyrus.uml.tools.utils.MultiplicityElementUtil;
import org.eclipse.papyrus.uml.tools.utils.NamedElementUtil;
import org.eclipse.papyrus.uml.tools.utils.PropertyUtil;
import org.eclipse.papyrus.uml.tools.utils.TypeUtil;
import org.eclipse.papyrus.uml.tools.utils.ValueSpecificationUtil;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.ValueSpecification;

public class SignalUtil {

	private static String getCustomPropertyLabel(Message e, Property property, Collection<String> displayValue) {
		StringBuffer buffer = new StringBuffer();
		// visibility
		buffer.append(" ");
		if (displayValue.contains(ICustomAppearance.DISP_VISIBILITY)) {
			buffer.append(NamedElementUtil.getVisibilityAsSign(property));
		}
		// derived property
		if (displayValue.contains(ICustomAppearance.DISP_DERIVE)) {
			if (property.isDerived()) {
				buffer.append("/");
			}
		}
		boolean showEqualMark = false;
		// name
		if (displayValue.contains(ICustomAppearance.DISP_PARAMETER_NAME)) {
			buffer.append(" ");
			String name = StringHelper.trimToEmpty(UMLLabelInternationalization.getInstance().getLabel(property));
			if (name.trim().length() > 0) {
				showEqualMark = true;
			}
			buffer.append(name);
		}
		if (displayValue.contains(ICustomAppearance.DISP_PARAMETER_TYPE)) {
			// type
			if (property.getType() != null) {
				buffer.append(": " + StringHelper.trimToEmpty(property.getType().getName()));
			} else {
				buffer.append(": " + TypeUtil.UNDEFINED_TYPE_NAME);
			}
			showEqualMark = true;
		}
		if (displayValue.contains(ICustomAppearance.DISP_MULTIPLICITY)) {
			// multiplicity -> do not display [1]
			String multiplicity = MultiplicityElementUtil.getMultiplicityAsString(property);
			buffer.append(multiplicity);
		}
		if (displayValue.contains(ICustomAppearance.DISP_DERIVE)) {
			String value = getValue(e, property);
			if (value != null) {
				if (showEqualMark) {
					buffer.append(" = ");
				}
				buffer.append(value);
			}
		} else if (displayValue.contains(ICustomAppearance.DISP_PARAMETER_DEFAULT)) {
			// default value
			if (property.getDefaultValue() != null) {
				if (showEqualMark) {
					buffer.append(" = ");
				}
				buffer.append(ValueSpecificationUtil.getSpecificationValue(property.getDefaultValue(), true));
			}
		}
		if (displayValue.contains(ICustomAppearance.DISP_MODIFIERS)) {
			boolean multiLine = displayValue.contains(ICustomAppearance.DISP_MULTI_LINE);
			// property modifiers
			String modifiers = PropertyUtil.getModifiersAsString(property, multiLine);
			if (!modifiers.equals("")) {
				if (multiLine) {
					buffer.append("\n");
				}
				if (!buffer.toString().endsWith(" ")) {
					buffer.append(" ");
				}
				buffer.append(modifiers);
			}
		}
		return buffer.toString();
	}

	private static String getValue(Message e, Property property) {
		try {
			Signal signal = (Signal) property.getOwner();
			int index = signal.getOwnedAttributes().indexOf(property);
			EList<ValueSpecification> arguments = e.getArguments();
			if (arguments.size() > index) {
				return ValueSpecificationUtil.getSpecificationValue(arguments.get(index), true);
			}
		} catch (Exception e1) {
		}
		return null;
	}

	/**
	 * return the custom label of the signal, given UML2 specification and a custom style.
	 *
	 * @param message
	 *
	 * @param style
	 *            the integer representing the style of the label
	 *
	 * @return the string corresponding to the label of the signal
	 */
	public static String getCustomLabel(Message message, Signal signal, Collection<String> displayValue) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(" "); // adds " " first for correct display considerations
		// visibility
		if (displayValue.contains(ICustomAppearance.DISP_VISIBILITY)) {
			buffer.append(NamedElementUtil.getVisibilityAsSign(signal));
		}
		// name
		if (displayValue.contains(ICustomAppearance.DISP_NAME)) {
			buffer.append(" ");
			buffer.append(StringHelper.trimToEmpty(UMLLabelInternationalization.getInstance().getLabel(signal)));
		}
		//
		// parameters : '(' parameter-list ')'
		buffer.append("(");
		buffer.append(getPropertiesAsString(message, signal, displayValue));
		buffer.append(")");
		return buffer.toString();
	}

	/**
	 * Returns signal properties as a string, the label is customized using a bit mask
	 *
	 * @return a string containing all properties separated by commas
	 */
	private static String getPropertiesAsString(Message e, Signal signal, Collection<String> displayValue) {
		StringBuffer propertiesString = new StringBuffer();
		boolean firstProperty = true;
		for (Property property : signal.getOwnedAttributes()) {
			// get the label for this property
			String propertyString = getCustomPropertyLabel(e, property, displayValue).trim();
			if (!propertyString.equals("")) {
				if (!firstProperty) {
					propertiesString.append(", ");
				}
				propertiesString.append(propertyString);
				firstProperty = false;
			}
		}
		return propertiesString.toString();
	}
}
