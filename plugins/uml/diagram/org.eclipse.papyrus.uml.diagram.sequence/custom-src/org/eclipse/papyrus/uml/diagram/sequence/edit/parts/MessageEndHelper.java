/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
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
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.uml2.uml.MessageEnd;

public class MessageEndHelper {

	public static void removeConnectionSourceFromMessageEnd(MessageEnd messageEnd, EObject connectionSource) {
		EAnnotation annotation = messageEnd.getEAnnotation("Connections");
		if (annotation != null) {
			annotation.getReferences().remove(connectionSource);
		}
	}

	public static void addConnectionSourceToMessageEnd(MessageEnd messageEnd, EObject connectionSource) {
		EAnnotation annotation = messageEnd.getEAnnotation("Connections");
		if (annotation == null) {
			annotation = EcoreFactory.eINSTANCE.createEAnnotation();
			annotation.setSource("Connections"); //$NON-NLS-1$
			messageEnd.getEAnnotations().add(annotation);
		}
		if (!annotation.getReferences().contains(connectionSource)) {
			annotation.getReferences().add(connectionSource);
		}
	}
}
