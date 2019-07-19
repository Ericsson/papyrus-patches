/*****************************************************************************
 * (c) Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.type.core.IClientContext;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.services.edit.context.TypeContext;
import org.eclipse.papyrus.infra.services.edit.service.ElementEditServiceUtils;
import org.eclipse.papyrus.infra.services.edit.service.IElementEditService;
import org.eclipse.papyrus.uml.service.types.utils.SequenceRequestConstant;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.UMLFactory;

/**
 * @author ETXACAM
 *
 */
public class SemanticElementsService {
	private static IElementEditService getElementEditService(EditingDomain editingDomain, Object context) throws ServiceException {
		IClientContext clientContext = TypeContext.getContext(editingDomain);
		return ElementEditServiceUtils.getCommandProvider(context, clientContext);
		
	}

	public static final CreateElementRequest getCreateElementRequest(TransactionalEditingDomain editingDomain, EObject container, IElementType elementType) {
		return new CreateElementRequest(editingDomain, container, elementType);					
	}
	
	public static final <T extends EObject> T createElement(TransactionalEditingDomain editingDomain, EObject container, IElementType elementType) {
		try {
			IElementEditService service = getElementEditService(editingDomain, container);
			if (service == null)
				return null;
			ICommand cmd = service.getEditCommand(getCreateElementRequest(editingDomain, container, elementType));
			cmd.execute(null, null);
			return detach((T)cmd.getCommandResult().getReturnValue());
		} catch (Exception e) {
			return null;
		}
	}
	
	public static final MessageEnd FAKE_FOR_BUG542802 = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
	
	public static final CreateElementRequest getCreateRelationshipRequest(TransactionalEditingDomain editingDomain, EObject container, EObject source, EObject target, IElementType elementType) {
		CreateRelationshipRequest req = new CreateRelationshipRequest(editingDomain, container, source, target, elementType);
		req.setParameter(SequenceRequestConstant.PREVIOUS_EVENT, FAKE_FOR_BUG542802);
		req.setParameter(SequenceRequestConstant.SECOND_PREVIOUS_EVENT, FAKE_FOR_BUG542802);
		return req;
	}

	public static final <T extends EObject> T createRelationship(TransactionalEditingDomain editingDomain, EObject container, EObject source, EObject target, IElementType elementType) {
		try {
			IElementEditService service = getElementEditService(editingDomain, container);
			if (service == null)
				return null;
			ICommand cmd = service.getEditCommand(getCreateRelationshipRequest(editingDomain, container, source, target, elementType));
			cmd.execute(null, null);
			return detach((T)cmd.getCommandResult().getReturnValue());
		} catch (Exception e) {
			return null;
		}
	}
	
	private static <T extends EObject> T detach(T object) {
		EcoreUtil.remove(object);
		if (object instanceof Message) {
			detach(((Message) object).getSendEvent());
			detach(((Message) object).getReceiveEvent());
		}
		return object;
	}
	
}
