/*****************************************************************************
 * Copyright (c) 2011 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Remi Schnekenburger (CEA LIST) remi.schnekenburger@cea.fr - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.advice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.type.core.edithelper.AbstractEditHelperAdvice;
import org.eclipse.gmf.runtime.emf.type.core.requests.DuplicateElementsRequest;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.emf.commands.IPapyrusDuplicateCommandConstants;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.DuplicateDiagramCommand;


/**
 * Edit Helper advice to add GMF diagram duplication when duplicating
 */
public class GMFDiagramDuplicateEditHelperAdvice extends AbstractEditHelperAdvice {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ICommand getBeforeDuplicateCommand(DuplicateElementsRequest request) {
		Object additional = request.getParameter(IPapyrusDuplicateCommandConstants.ADDITIONAL_DUPLICATED_ELEMENTS);
		// additional element should be a set of elements that will be duplicated. If this is null, the request will be ignored.
		if (!(additional instanceof Set<?>)) {
			return super.getBeforeDuplicateCommand(request);
		}

		Set<Object> duplicatedObjects = ((Set<Object>) additional);
		EObject object = getDuplicatedEObject(request);
		if (object == null || object.eResource() == null) {
			return super.getBeforeDuplicateCommand(request);
		}

		// retrieve the diagrams linked to the object
		List<Diagram> diagramsToDuplicate = new ArrayList<Diagram>();

		ResourceSet resourceSet = object.eResource().getResourceSet();
		ECrossReferenceAdapter adapter = ECrossReferenceAdapter.getCrossReferenceAdapter(resourceSet);
		if (adapter == null) {
			adapter = new ECrossReferenceAdapter();
			resourceSet.eAdapters().add(adapter);
		}

		// do not proceed for graphical elements, which will have evident relationships to diagrams (owner, etc.)
		if (object instanceof View) {
			return super.getBeforeDuplicateCommand(request);
		}

		// check for the element itself
		Collection<Setting> settings = adapter.getInverseReferences(object, false);
		for (Setting setting : settings) {
			EObject value = setting.getEObject();
			if (value instanceof Diagram) {
				diagramsToDuplicate.add((Diagram) value);
			}
		}

		// check for sub-elements
		for (Iterator<EObject> it = object.eAllContents(); it.hasNext();) {
			EObject child = it.next();
			settings = adapter.getInverseReferences(child, false);

			for (Setting setting : settings) {
				EObject value = setting.getEObject();
				if (value instanceof Diagram) {
					diagramsToDuplicate.add((Diagram) value);
				}
			}
		}


		if (!diagramsToDuplicate.isEmpty()) {
			CompositeCommand command = null;
			// create the command for all the diagrams that have no command ready
			for (Diagram diagramToDuplicate : diagramsToDuplicate) {
				if (!duplicatedObjects.contains(diagramToDuplicate)) {
					if (command == null) {
						command = new CompositeCommand("", Arrays.asList(new DuplicateDiagramCommand(request.getEditingDomain(), "Duplicate Diagram", diagramToDuplicate, request.getAllDuplicatedElementsMap()))); //$NON-NLS-1$  //$NON-NLS-2$
					} else {
						command.add(new DuplicateDiagramCommand(request.getEditingDomain(), "Duplicate Diagram", diagramToDuplicate, request.getAllDuplicatedElementsMap())); //$NON-NLS-1$
					}
					duplicatedObjects.add(diagramToDuplicate);
				}
			}

			if (command != null) {
				if (super.getBeforeDuplicateCommand(request) != null) {
					command.add(super.getBeforeDuplicateCommand(request));
					return command.reduce();
				} else {
					return command.reduce();

				}
			}
		}

		return super.getBeforeDuplicateCommand(request);
	}

	/**
	 * Returns the EObject to be duplicated
	 *
	 * @return the EObject to be duplicated
	 */
	protected EObject getDuplicatedEObject(DuplicateElementsRequest request) {
		List<Object> elementsToBeDuplicated = request.getElementsToBeDuplicated();
		if (elementsToBeDuplicated == null || elementsToBeDuplicated.isEmpty()) {
			return null;
		}
		Object elementToBeDuplicated = elementsToBeDuplicated.get(0);
		if (elementToBeDuplicated instanceof EObject) {
			return (EObject) elementToBeDuplicated;
		}
		return null;
	}

}
