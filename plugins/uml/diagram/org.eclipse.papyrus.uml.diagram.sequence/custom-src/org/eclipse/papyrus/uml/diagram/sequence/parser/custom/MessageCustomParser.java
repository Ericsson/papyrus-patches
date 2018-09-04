/*****************************************************************************
 * Copyright (c) 2009 Atos Origin.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Atos Origin - Initial API and implementation
 *   Ericsson AB - Antonio Campesino - Conforming to UML2.5 specifications.
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.parser.custom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParser;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParserEditStatus;
import org.eclipse.gmf.runtime.common.ui.services.parser.ParserEditStatus;
import org.eclipse.gmf.runtime.emf.ui.services.parser.ISemanticParser;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.papyrus.uml.diagram.sequence.parsers.MessageFormatParser;
import org.eclipse.papyrus.uml.diagram.sequence.util.MessageLabelUtil;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.ValueSpecification;

public class MessageCustomParser extends MessageFormatParser implements IParser, ISemanticParser {
	public MessageCustomParser() {
		super(new EAttribute[] { UMLPackage.eINSTANCE.getNamedElement_Name() });
	}

	@Override
	public String getPrintString(IAdaptable adapter, int flags) {
		EObject obj = adapter.getAdapter(EObject.class);
		if (obj == null || !(obj instanceof Message)) {
			return "";
		}
		Message msg = (Message) obj;
		return MessageLabelUtil.getMessageLabel(msg);
	}

	@Override
	public String getEditString(IAdaptable adapter, int flags) {
		EObject obj = adapter.getAdapter(EObject.class);
		if (obj == null || !(obj instanceof Message)) {
			return "";
		}

		Message msg = (Message) obj;
		return MessageLabelUtil.getEditLabel(msg);
	}

	@Override
	public boolean areSemanticElementsAffected(EObject listener, Object notification) {
		return true;
	}

	@Override
	public List<Element> getSemanticElementsBeingParsed(EObject element) {
		List<Element> semanticElementsBeingParsed = new ArrayList<>();
		if (element instanceof Message) {
			Message message = (Message) element;
			semanticElementsBeingParsed.add(message);
			NamedElement signature = message.getSignature();
			semanticElementsBeingParsed.add(signature);
			if (signature instanceof Operation) {
				for (Parameter parameter : ((Operation) signature).getOwnedParameters()) {
					semanticElementsBeingParsed.add(parameter);
				}
			}
			if (signature instanceof Signal) {
				for (Property property : ((Signal) signature).getOwnedAttributes()) {
					semanticElementsBeingParsed.add(property);
				}
			}
			for (ValueSpecification valSpec : message.getArguments()) {
				semanticElementsBeingParsed.add(valSpec);
			}
		}
		return semanticElementsBeingParsed;
	}

	@Override
	public IContentAssistProcessor getCompletionProcessor(IAdaptable element) {
		EObject obj = element.getAdapter(EObject.class);
		if (obj == null || !(obj instanceof Message)) {
			return null;
		}
		return null;
	}

	@Override
	public boolean isAffectingEvent(Object event, int flags) {
		return true;
	}

	@Override
	public IParserEditStatus isValidEditString(IAdaptable element, String editString) {
		return ParserEditStatus.EDITABLE_STATUS;
	}

	@Override
	public ICommand getParseCommand(IAdaptable element, String newString, int flags) {
		return super.getParseCommand(element, newString, flags);
	}
}